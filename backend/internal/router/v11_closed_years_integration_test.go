//go:build integration

package router_test

import (
	"fmt"
	"net/http"
	"net/http/httptest"
	"sync"
	"testing"
	"time"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type cfV11 struct {
	carryForward
	Version      int     `json:"version"`
	PostedAt     *string `json:"postedAt"`
	ReopenedAt   *string `json:"reopenedAt"`
	ReopenReason *string `json:"reopenReason"`
	Steps        []struct {
		Code   string `json:"code"`
		Status string `json:"status"`
	} `json:"steps"`
}

func (c cfV11) stepStatus() []string {
	out := make([]string, len(c.Steps))
	for i, s := range c.Steps {
		out[i] = s.Status
	}
	return out
}

func (f *cfFix) cfPath(year int, rest string) string {
	return f.path(fmt.Sprintf("/ledger/carry-forwards/%d%s", year, rest))
}

func (f *cfFix) cfGet(t *testing.T, year int, query string) (cfV11, string) {
	t.Helper()
	rec := f.do(t, http.MethodGet, f.cfPath(year, query), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[cfV11](t, rec), rec.Header().Get("ETag")
}

func (f *cfFix) cfPost(t *testing.T, year int) *httptest.ResponseRecorder {
	t.Helper()
	return f.post(t, f.cfPath(year, "/post"), f.admin, "")
}

func (f *cfFix) reopen(t *testing.T, year int, token, etag, reason string) *httptest.ResponseRecorder {
	t.Helper()
	h := map[string]string{"Idempotency-Key": uuid.NewString()}
	if etag != "" {
		h["If-Match"] = etag
	}
	return f.req(t, http.MethodPost, f.cfPath(year, "/reopen"), token, `{"reason":"`+reason+`"}`, h)
}

func (f *cfFix) balanceOf(t *testing.T, asOf, code string) string {
	t.Helper()
	return byCode(f.grid(t, `{"asOf":"`+asOf+`","startRow":0,"endRow":100}`).Rows)[code].Balance
}

func (f *cfFix) statement(t *testing.T, account uint, query string) statementV11 {
	t.Helper()
	rec := f.do(t, http.MethodGet, f.path("/ledger/accounts/"+idStr(account)+"/statement?"+query), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[statementV11](t, rec)
}

type statementV11 struct {
	statementJSON
	Lines []struct {
		stLine
		ResetsBalance bool `json:"resetsBalance"`
	} `json:"lines"`
}

func TestV11_CarryForwardClosesTheYearAndReopenOpensIt(t *testing.T) {
	f := newCF(t)

	p, etag := f.cfGet(t, 2027, "")
	assert.Equal(t, `"1"`, etag)
	assert.Equal(t, 1, p.Version)
	assert.Equal(t, []string{"DONE", "DONE", "DONE", "CURRENT"}, p.stepStatus())
	assert.Equal(t, "CLOSE_DOCUMENTS", p.Steps[0].Code)
	assert.Equal(t, "OPEN_YEAR", p.Steps[3].Code)

	rec := f.cfPost(t, 2027)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	assert.Equal(t, `"2"`, rec.Header().Get("ETag"))
	posted := decode[cfV11](t, rec)
	assert.Equal(t, []string{"DONE", "DONE", "DONE", "DONE"}, posted.stepStatus())
	assert.NotNil(t, posted.PostedAt)

	entries := f.countEntries(t)
	closed := []struct{ path, body string }{
		{"/ledger/entries", entryBody(f.depot, "2026-12-31", ln{f.accA, "DEBIT", "5.00"}, ln{f.sales, "CREDIT", "5.00"})},
		{"/receipts", receiptBody("COLLECTION", f.depot, f.sicA, "2026-12-31", "CASH", "5.00", "")},
		{"/cash-vouchers", fmt.Sprintf(`{"kind":"EXPENSE","depotId":%d,"date":"2026-06-30","accountId":%d,"amount":"1.00","description":"x"}`, f.depot, f.exp)},
	}
	for _, c := range closed {
		rec = f.post(t, f.path(c.path), f.token, c.body)
		assert.Equal(t, http.StatusConflict, rec.Code, "%s: %s", c.path, rec.Body.String())
		assert.Equal(t, "ERR_YEAR_CLOSED", errCode(t, rec), c.path)
	}
	assert.Equal(t, entries, f.countEntries(t))
	f.entry(t, "2027-01-02", ln{f.accB, "DEBIT", "1.00"}, ln{f.sales, "CREDIT", "1.00"})

	path := f.cfPath(2027, "/reopen")
	assert.Equal(t, http.StatusForbidden, f.reopen(t, 2027, f.token, `"2"`, "late invoice").Code)
	assert.Equal(t, http.StatusPreconditionRequired, f.reopen(t, 2027, f.admin, "", "late invoice").Code)
	assert.Equal(t, http.StatusPreconditionFailed, f.reopen(t, 2027, f.admin, `"1"`, "late invoice").Code)
	assert.Equal(t, http.StatusBadRequest, f.reopen(t, 2027, f.admin, `"2"`, "x").Code)

	key := map[string]string{"Idempotency-Key": uuid.NewString(), "If-Match": `"2"`}
	rec = f.req(t, http.MethodPost, path, f.admin, `{"reason":"late invoice"}`, key)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Equal(t, `"3"`, rec.Header().Get("ETag"))
	re := decode[cfV11](t, rec)
	assert.Equal(t, "REOPENED", re.Status)
	assert.Equal(t, 3, re.Version)
	require.NotNil(t, re.ReopenReason)
	assert.Equal(t, "late invoice", *re.ReopenReason)
	assert.NotNil(t, re.ReopenedAt)
	assert.Nil(t, re.PostedAt)
	assert.Equal(t, "READY", re.row(t, f.accA, f.depot).Status)
	replay := f.req(t, http.MethodPost, path, f.admin, `{"reason":"late invoice"}`, key)
	assert.Equal(t, http.StatusOK, replay.Code)
	assert.Equal(t, "true", replay.Header().Get("Idempotent-Replayed"))

	var reversals int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM journal_entries r JOIN journal_entries o ON o.id = r.reverses_entry_id
		WHERE r.org_id = ? AND o.source_type = 'opening' AND r.source_type = 'reversal' AND r.entry_date = '2027-01-01'`, f.org).Scan(&reversals).Error)
	assert.Equal(t, int64(6), reversals, "every opening is reversed on 1 January")
	assert.Empty(t, decode[[]map[string]any](t, f.do(t, http.MethodGet, f.path("/ledger/opening-balances?year=2027"), f.token, "")))
	assert.Equal(t, "579.00", f.balanceOf(t, "2027-01-31", "120.A"), "balances fall back to the uncarried ledger")

	f.entry(t, "2026-12-31", ln{f.accA, "DEBIT", "5.00"}, ln{f.sales, "CREDIT", "5.00"})
	rec = f.reopen(t, 2027, f.admin, `"3"`, "again")
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_NOT_CARRIED_FORWARD", errCode(t, rec))

	rec = f.cfPost(t, 2027)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	assert.Equal(t, "505.00", decode[cfV11](t, rec).row(t, f.accA, f.depot).OpeningDebit)
	assert.Len(t, decode[[]map[string]any](t, f.do(t, http.MethodGet, f.path("/ledger/opening-balances?year=2027"), f.token, "")), 6)
	assert.Equal(t, "584.00", f.balanceOf(t, "2027-01-31", "120.A"))

	st := f.statement(t, f.accA, "from=2026-12-01&to=2027-01-31")
	require.Len(t, st.Lines, 4, "voided openings and their reversals are not lines")
	assert.False(t, st.Lines[0].ResetsBalance)
	assert.True(t, st.Lines[1].ResetsBalance)
	assert.True(t, st.Lines[2].ResetsBalance)
	assert.Equal(t, "opening", st.Lines[1].SourceType)
	assert.Equal(t, "584.00", st.ClosingBalance)
}

func TestV11_OverridesNeedThePreviewVersion(t *testing.T) {
	f := newCF(t)
	body := fmt.Sprintf(`{"accountId":%d,"depotId":%d,"debit":"60.00","credit":"0.00","reason":"agreed"}`, f.accA, f.depot2)
	put := func(query, etag string) *httptest.ResponseRecorder {
		h := map[string]string{}
		if etag != "" {
			h["If-Match"] = etag
		}
		return f.req(t, http.MethodPut, f.cfPath(2027, "/overrides"+query), f.admin, body, h)
	}
	assert.Equal(t, http.StatusPreconditionRequired, put("", "").Code)
	assert.Equal(t, http.StatusPreconditionFailed, put("", `"9"`).Code)
	rec := put("?previewDepotId="+idStr(f.depot2), `"1"`)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Equal(t, `"2"`, rec.Header().Get("ETag"))
	p := decode[cfV11](t, rec)
	assert.Equal(t, 2, p.Version)
	assert.Len(t, p.Rows, 2, "previewDepotId restricts the returned preview")
	assert.Equal(t, "EDITED", p.row(t, f.accA, f.depot2).Status)

	del := func(etag string) *httptest.ResponseRecorder {
		q := fmt.Sprintf("/overrides?accountId=%d&depotId=%d&previewDepotId=%d", f.accA, f.depot2, f.depot)
		return f.req(t, http.MethodDelete, f.cfPath(2027, q), f.admin, "", map[string]string{"If-Match": etag})
	}
	assert.Equal(t, http.StatusPreconditionFailed, del(`"1"`).Code)
	rec = del(`"2"`)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Equal(t, `"3"`, rec.Header().Get("ETag"))
	for _, r := range decode[cfV11](t, rec).Rows {
		assert.Equal(t, f.depot, r.DepotID)
	}
	_, etag := f.cfGet(t, 2027, "")
	assert.Equal(t, `"3"`, etag)

	require.NoError(t, testDB.Exec(`UPDATE account_balances SET debit_total = debit_total + 1 WHERE account_id = ?`, f.accA).Error)
	p, _ = f.cfGet(t, 2027, "")
	assert.Equal(t, []string{"DONE", "DONE", "BLOCKED", "TODO"}, p.stepStatus())
}

func TestV11_LaterCarriedYearMustBeReopenedFirst(t *testing.T) {
	f := newCF(t)
	require.Equal(t, http.StatusCreated, f.cfPost(t, 2027).Code)
	require.Equal(t, http.StatusCreated, f.cfPost(t, 2028).Code)

	_, etag := f.cfGet(t, 2027, "")
	rec := f.reopen(t, 2027, f.admin, etag, "late invoice")
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_YEAR_CLOSED", errCode(t, rec))

	_, etag28 := f.cfGet(t, 2028, "")
	require.Equal(t, http.StatusOK, f.reopen(t, 2028, f.admin, etag28, "redo 2028").Code)
	require.Equal(t, http.StatusOK, f.reopen(t, 2027, f.admin, etag, "redo 2027").Code)

	require.Equal(t, http.StatusCreated, f.cfPost(t, 2028).Code)
	rec = f.cfPost(t, 2027)
	assert.Equal(t, http.StatusConflict, rec.Code, "2027 openings would sit in a closed year")
	assert.Equal(t, "ERR_YEAR_CLOSED", errCode(t, rec))
}

func TestV11_StatementRestartsAtCarriedOpenings(t *testing.T) {
	f := newCF(t)
	rec := f.req(t, http.MethodPut, f.cfPath(2027, "/overrides"), f.admin,
		fmt.Sprintf(`{"accountId":%d,"depotId":%d,"debit":"450.00","credit":"0.00","reason":"discount"}`, f.accA, f.depot), map[string]string{"If-Match": `"1"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	require.Equal(t, http.StatusCreated, f.cfPost(t, 2027).Code)

	st := f.statement(t, f.accA, "from=2026-06-01&to=2027-01-31")
	require.Len(t, st.BroughtForward, 2)
	require.Len(t, st.Lines, 4)
	got := make([][2]any, len(st.Lines))
	for i, l := range st.Lines {
		got[i] = [2]any{l.ResetsBalance, l.RunningBalance}
	}
	assert.Equal(t, [][2]any{{false, "570.00"}, {true, "450.00"}, {true, "520.00"}, {false, "529.00"}}, got)
	assert.Equal(t, "529.00", st.ClosingBalance, "closing = balance as of `to`")
	assert.Equal(t, f.balanceOf(t, "2027-01-31", "120.A"), st.ClosingBalance)
	require.Len(t, st.DepotTotals, 2)
	assert.Equal(t, "9.00", st.DepotTotals[0].Debit, "brought forward and opening lines are left out")
	assert.Equal(t, "459.00", st.DepotTotals[0].Balance)
	assert.Equal(t, "70.00", st.DepotTotals[1].Balance)

	one := f.statement(t, f.accA, "from=2026-06-01&to=2027-01-31&depotId="+idStr(f.depot))
	assert.Equal(t, "459.00", one.ClosingBalance)
}

func TestV11_ConcurrentLatePostingsCannotSlipPastTheCarryForward(t *testing.T) {
	f := newCF(t)
	var wg sync.WaitGroup
	codes := make([]int, 12)
	for i := range codes {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			if i == 6 {
				codes[i] = f.cfPost(t, 2027).Code
				return
			}
			codes[i] = f.post(t, f.path("/ledger/entries"), f.token, entryBody(f.depot, "2026-12-31", ln{f.accA, "DEBIT", "1.00"}, ln{f.sales, "CREDIT", "1.00"})).Code
		}(i)
	}
	wg.Wait()
	require.Equal(t, http.StatusCreated, codes[6])
	for i, c := range codes {
		assert.Contains(t, []int{http.StatusCreated, http.StatusConflict}, c, i)
	}
	p, _ := f.cfGet(t, 2027, "")
	require.Equal(t, "POSTED", p.Status)
	for _, r := range p.Rows {
		assert.Equal(t, r.ClosingBalance, net(t, r.OpeningDebit, r.OpeningCredit), "account %s depot %d", r.AccountCode, r.DepotID)
	}
}

func TestV11_ReversalsDatedTodayRespectClosedYears(t *testing.T) {
	f := newCF(t)
	y := time.Now().Year()
	e := f.entry(t, fmt.Sprintf("%d-02-01", y), ln{f.accB, "DEBIT", "3.00"}, ln{f.sales, "CREDIT", "3.00"})
	rec := f.post(t, f.path("/receipts"), f.token, receiptBody("COLLECTION", f.depot, f.sicA, fmt.Sprintf("%d-02-02", y), "CASH", "4.00", ""))
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	r := decode[receiptJSON](t, rec)
	require.Equal(t, http.StatusCreated, f.cfPost(t, y+1).Code)

	rec = f.post(t, f.path("/ledger/entries/"+idStr(e.ID)+"/reversal"), f.token, `{"reason":"too late"}`)
	assert.Equal(t, http.StatusConflict, rec.Code, rec.Body.String())
	assert.Equal(t, "ERR_YEAR_CLOSED", errCode(t, rec))
	rec = f.post(t, f.path("/receipts/"+idStr(r.ID)+"/void"), f.token, `{"reason":"too late"}`)
	assert.Equal(t, http.StatusConflict, rec.Code, rec.Body.String())
	assert.Equal(t, "ERR_YEAR_CLOSED", errCode(t, rec))
}

func TestV11_EntriesAreTRY(t *testing.T) {
	f := newCF(t)
	e := f.entry(t, "2026-05-05", ln{f.accB, "DEBIT", "3.00"}, ln{f.sales, "CREDIT", "3.00"})
	rec := f.do(t, http.MethodGet, f.path("/ledger/entries/"+idStr(e.ID)), f.token, "")
	assert.Equal(t, "TRY", decode[struct {
		Currency string `json:"currency"`
	}](t, rec).Currency)
}

func net(t *testing.T, debit, credit string) string {
	t.Helper()
	var d, c float64
	_, err := fmt.Sscanf(debit+" "+credit, "%f %f", &d, &c)
	require.NoError(t, err)
	return fmt.Sprintf("%.2f", d-c)
}
