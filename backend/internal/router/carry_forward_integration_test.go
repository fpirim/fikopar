//go:build integration

package router_test

import (
	"fmt"
	"net/http"
	"strings"
	"sync"
	"testing"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type cfRow struct {
	AccountID      uint    `json:"accountId"`
	AccountCode    string  `json:"accountCode"`
	DepotID        uint    `json:"depotId"`
	ClosingBalance string  `json:"closingBalance"`
	OpeningDebit   string  `json:"openingDebit"`
	OpeningCredit  string  `json:"openingCredit"`
	Status         string  `json:"status"`
	OverrideReason *string `json:"overrideReason"`
	JournalEntryID *uint   `json:"journalEntryId"`
}

type carryForward struct {
	Year   int    `json:"year"`
	Status string `json:"status"`
	Checks []struct {
		Code     string `json:"code"`
		OK       bool   `json:"ok"`
		Blocking bool   `json:"blocking"`
	} `json:"checks"`
	AccountCount int     `json:"accountCount"`
	EntryCount   int     `json:"entryCount"`
	TotalDebit   string  `json:"totalDebit"`
	TotalCredit  string  `json:"totalCredit"`
	Rows         []cfRow `json:"rows"`
}

func (c carryForward) row(t *testing.T, account, depot uint) cfRow {
	t.Helper()
	for _, r := range c.Rows {
		if r.AccountID == account && r.DepotID == depot {
			return r
		}
	}
	t.Fatalf("no row for account %d depot %d", account, depot)
	return cfRow{}
}

type cfFix struct {
	*reportFix
	opening uint
}

// 2026: A owes 500 in depot 1 and 70 in depot 2, supplier C is owed 200.
func newCF(t *testing.T) *cfFix {
	t.Helper()
	f := &cfFix{reportFix: newReport(t)}
	f.opening = f.account(t, "500", "EQUITY")
	f.setRoles(t, fmt.Sprintf(`[{"role":"CASH","accountId":%d},{"role":"OPENING","accountId":%d}]`, f.cash, f.opening))
	f.post2(t, f.depot, "2026-03-01", line(f.accA, "DEBIT", "500.00", "")+","+line(f.sales, "CREDIT", "500.00", ""))
	f.post2(t, f.depot, "2026-12-31", line(f.exp, "DEBIT", "200.00", "")+","+line(f.accC, "CREDIT", "200.00", ""))
	f.post2(t, f.depot2, "2026-06-06", line(f.accA, "DEBIT", "70.00", "")+","+line(f.sales, "CREDIT", "70.00", ""))
	f.post2(t, f.depot, "2027-01-01", line(f.accA, "DEBIT", "9.00", "")+","+line(f.sales, "CREDIT", "9.00", ""))
	return f
}

func (f *cfFix) preview(t *testing.T, year string) carryForward {
	t.Helper()
	rec := f.do(t, http.MethodGet, f.path("/ledger/carry-forwards/"+year), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[carryForward](t, rec)
}

func (f *cfFix) override(t *testing.T, token string, account, depot uint, debit, credit, reason string) *carryForwardRec {
	t.Helper()
	body := fmt.Sprintf(`{"accountId":%d,"depotId":%d,"debit":%q,"credit":%q,"reason":%q}`, account, depot, debit, credit, reason)
	rec := f.req(t, http.MethodPut, f.path("/ledger/carry-forwards/2027/overrides"), token, body, nil)
	return &carryForwardRec{rec.Code, rec.Body.String()}
}

type carryForwardRec struct {
	code int
	body string
}

func TestCarryForward_PreviewOverridePostAndReopen(t *testing.T) {
	f := newCF(t)

	p := f.preview(t, "2027")
	assert.Equal(t, "PREVIEW", p.Status)
	assert.Equal(t, 2027, p.Year)
	for _, c := range p.Checks {
		assert.True(t, c.OK, c.Code)
	}
	a1 := p.row(t, f.accA, f.depot)
	assert.Equal(t, "500.00", a1.ClosingBalance)
	assert.Equal(t, "500.00", a1.OpeningDebit)
	assert.Equal(t, "0.00", a1.OpeningCredit)
	assert.Equal(t, "READY", a1.Status)
	c1 := p.row(t, f.accC, f.depot)
	assert.Equal(t, "200.00", c1.OpeningCredit, "31 December is included")
	assert.Equal(t, 6, p.EntryCount)
	assert.Equal(t, 4, p.AccountCount)
	assert.Equal(t, p.TotalDebit, p.TotalCredit)
	assert.Equal(t, "770.00", p.TotalDebit)
	for _, r := range p.Rows {
		assert.NotEqual(t, f.opening, r.AccountID, "the OPENING account itself is not carried")
	}

	rec := f.override(t, f.token, f.accA, f.depot, "450.00", "0.00", "discount agreed")
	assert.Equal(t, http.StatusForbidden, rec.code, "admin only")
	rec = f.override(t, f.admin, f.accA, f.depot, "450.00", "0.00", "discount agreed")
	require.Equal(t, http.StatusOK, rec.code, rec.body)
	rec = f.override(t, f.admin, f.sales, f.depot2, "0.00", "0.00", "skip this one")
	require.Equal(t, http.StatusOK, rec.code, rec.body)
	for _, bad := range [][3]string{{"1.00", "1.00", "both sides"}, {"-1.00", "0.00", "negative"}, {"1.00", "0.00", "x"}} {
		rec = f.override(t, f.admin, f.accA, f.depot, bad[0], bad[1], bad[2])
		assert.Equal(t, http.StatusBadRequest, rec.code, bad[2])
	}

	p = f.preview(t, "2027")
	a1 = p.row(t, f.accA, f.depot)
	assert.Equal(t, "EDITED", a1.Status)
	assert.Equal(t, "450.00", a1.OpeningDebit)
	assert.Equal(t, "discount agreed", *a1.OverrideReason)
	assert.Equal(t, "SKIP", p.row(t, f.sales, f.depot2).Status)
	assert.Equal(t, 5, p.EntryCount)

	del := f.req(t, http.MethodDelete, f.path(fmt.Sprintf("/ledger/carry-forwards/2027/overrides?accountId=%d&depotId=%d", f.sales, f.depot2)), f.admin, "", nil)
	require.Equal(t, http.StatusOK, del.Code, del.Body.String())
	assert.Equal(t, "READY", decode[carryForward](t, del).row(t, f.sales, f.depot2).Status)

	key := map[string]string{"Idempotency-Key": uuid.NewString()}
	postPath := f.path("/ledger/carry-forwards/2027/post")
	assert.Equal(t, http.StatusForbidden, f.req(t, http.MethodPost, postPath, f.token, "", key).Code)
	res := f.req(t, http.MethodPost, postPath, f.admin, "", key)
	require.Equal(t, http.StatusCreated, res.Code, res.Body.String())
	posted := decode[carryForward](t, res)
	assert.Equal(t, "POSTED", posted.Status)
	assert.Equal(t, 6, posted.EntryCount)
	a1 = posted.row(t, f.accA, f.depot)
	assert.Equal(t, "POSTED", a1.Status)
	require.NotNil(t, a1.JournalEntryID)
	e := f.getEntry(t, *a1.JournalEntryID)
	assert.Equal(t, "opening", e.SourceType)
	assert.Equal(t, "2027-01-01", e.EntryDate)
	assert.Equal(t, "450.00", e.Postings[0].Amount)
	assert.Equal(t, f.opening, e.Postings[1].AccountID)

	entries := f.countEntries(t)
	replay := f.req(t, http.MethodPost, postPath, f.admin, "", key)
	assert.Equal(t, http.StatusCreated, replay.Code)
	assert.Equal(t, "true", replay.Header().Get("Idempotent-Replayed"))
	again := f.post(t, postPath, f.admin, "")
	assert.Equal(t, http.StatusConflict, again.Code)
	assert.Equal(t, "ERR_ALREADY_CARRIED_FORWARD", errCode(t, again))
	assert.Equal(t, entries, f.countEntries(t), "re-running must not post twice")
	assert.Equal(t, http.StatusConflict, f.override(t, f.admin, f.accA, f.depot, "1.00", "0.00", "too late").code)

	assert.Equal(t, "POSTED", f.preview(t, "2027").Status)

	type ob struct {
		AccountID      uint   `json:"accountId"`
		DepotID        uint   `json:"depotId"`
		Year           int    `json:"year"`
		Debit          string `json:"debit"`
		Credit         string `json:"credit"`
		JournalEntryID uint   `json:"journalEntryId"`
		Source         string `json:"source"`
	}
	obs := decode[[]ob](t, f.do(t, http.MethodGet, f.path("/ledger/opening-balances?year=2027"), f.token, ""))
	assert.Len(t, obs, 6)
	obs = decode[[]ob](t, f.do(t, http.MethodGet, f.path("/ledger/opening-balances?year=2027&depotId="+idStr(f.depot2)), f.token, ""))
	assert.Len(t, obs, 2)
	assert.Equal(t, "CARRY_FORWARD", obs[0].Source)

	// The new year starts from the (edited) openings; last year's postings are not counted twice.
	g := f.grid(t, `{"asOf":"2027-01-31","startRow":0,"endRow":100}`)
	assert.Equal(t, "529.00", byCode(g.Rows)["120.A"].Balance)
	g = f.grid(t, `{"asOf":"2026-12-31","startRow":0,"endRow":100}`)
	assert.Equal(t, "570.00", byCode(g.Rows)["120.A"].Balance, "the closed year is unchanged")

	st := decode[statementJSON](t, f.do(t, http.MethodGet, f.path("/ledger/accounts/"+idStr(f.accA)+"/statement?from=2027-01-01&to=2027-01-31"), f.token, ""))
	require.Len(t, st.BroughtForward, 2)
	assert.Equal(t, "450.00", st.BroughtForward[0].Debit, "opening entries of the year are folded into brought forward")
	assert.Len(t, st.Lines, 1)
	assert.Equal(t, "529.00", st.ClosingBalance)

	rec2 := f.post(t, f.path("/ledger/entries/"+idStr(*a1.JournalEntryID)+"/reversal"), f.token, `{"reason":"undo opening"}`)
	assert.Equal(t, http.StatusConflict, rec2.Code)
	assert.Equal(t, "ERR_ENTRY_OWNED_BY_SOURCE", errCode(t, rec2))
}

func TestCarryForward_ConcurrentPostsPostOnce(t *testing.T) {
	f := newCF(t)
	codes := make([]int, 5)
	var wg sync.WaitGroup
	for i := range codes {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			codes[i] = f.post(t, f.path("/ledger/carry-forwards/2027/post"), f.admin, "").Code
		}(i)
	}
	wg.Wait()
	won := 0
	for _, c := range codes {
		if c == http.StatusCreated {
			won++
		} else {
			assert.Equal(t, http.StatusConflict, c)
		}
	}
	assert.Equal(t, 1, won)
	var n int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM journal_entries WHERE org_id = ? AND source_type = 'opening'`, f.org).Scan(&n).Error)
	assert.Equal(t, int64(6), n)
}

func TestCarryForward_BlockingChecksAndRoles(t *testing.T) {
	f := newCF(t)
	f.setRoles(t, fmt.Sprintf(`[{"role":"CASH","accountId":%d}]`, f.cash))
	rec := f.post(t, f.path("/ledger/carry-forwards/2027/post"), f.admin, "")
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_ACCOUNT_ROLE_MISSING", errCode(t, rec))

	f.setRoles(t, fmt.Sprintf(`[{"role":"OPENING","accountId":%d}]`, f.opening))
	require.NoError(t, testDB.Exec(`UPDATE account_balances SET debit_total = debit_total + 1 WHERE account_id = ?`, f.accA).Error)
	p := f.preview(t, "2027")
	for _, c := range p.Checks {
		if c.Code == "LEDGER_BALANCED" {
			assert.False(t, c.OK)
			assert.True(t, c.Blocking)
		}
	}
	rec = f.post(t, f.path("/ledger/carry-forwards/2027/post"), f.admin, "")
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_CARRY_FORWARD_BLOCKED", errCode(t, rec))
	assert.Contains(t, rec.Body.String(), "LEDGER_BALANCED")
}

func TestCarryForward_PreviewPDFAndDepotFilter(t *testing.T) {
	f := newCF(t)
	p := decode[carryForward](t, f.do(t, http.MethodGet, f.path("/ledger/carry-forwards/2027?depotId="+idStr(f.depot2)), f.token, ""))
	assert.Len(t, p.Rows, 2)
	rec := f.req(t, http.MethodGet, f.path("/ledger/carry-forwards/2027"), f.token, "", map[string]string{"Accept": pdfType})
	require.Equal(t, http.StatusOK, rec.Code)
	assert.True(t, strings.HasPrefix(rec.Body.String(), "%PDF-"))
	assert.Equal(t, http.StatusBadRequest, f.do(t, http.MethodGet, f.path("/ledger/carry-forwards/1999"), f.token, "").Code)
	assert.Equal(t, http.StatusBadRequest, f.do(t, http.MethodGet, f.path("/ledger/opening-balances"), f.token, "").Code)
}
