//go:build integration

package router_test

import (
	"fmt"
	"net/http"
	"testing"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type cashBookV12 struct {
	OpeningBalance string `json:"openingBalance"`
	TotalIn        string `json:"totalIn"`
	TotalOut       string `json:"totalOut"`
	ClosingBalance string `json:"closingBalance"`
	Lines          []struct {
		Date           string `json:"date"`
		Kind           string `json:"kind"`
		ResetsBalance  bool   `json:"resetsBalance"`
		Income         string `json:"income"`
		Expense        string `json:"expense"`
		RunningBalance string `json:"runningBalance"`
	} `json:"lines"`
}

func (f *cfFix) cashReceipt(t *testing.T, date, amount string) {
	t.Helper()
	rec := f.post(t, f.path("/receipts"), f.token, receiptBody("COLLECTION", f.depot, f.sicA, date, "CASH", amount, ""))
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
}

// L9: across a carried 1 January the cash book restarts at the carried opening; totals leave the opening out.
func TestV12_CashBookRestartsAtCarriedOpening(t *testing.T) {
	f := newCF(t)
	f.cashReceipt(t, "2026-03-10", "100.00")
	rec := f.req(t, http.MethodPut, f.cfPath(2027, "/overrides"), f.admin,
		fmt.Sprintf(`{"accountId":%d,"depotId":%d,"debit":"90.00","credit":"0.00","reason":"till short"}`, f.cash, f.depot), map[string]string{"If-Match": `"1"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	require.Equal(t, http.StatusCreated, f.cfPost(t, 2027).Code)
	f.cashReceipt(t, "2027-01-05", "30.00")

	get := func(q string) cashBookV12 {
		rec := f.do(t, http.MethodGet, f.path("/cash-book?depotId="+idStr(f.depot)+"&"+q), f.token, "")
		require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
		return decode[cashBookV12](t, rec)
	}
	b := get("from=2026-01-01&to=2027-01-31")
	require.Len(t, b.Lines, 3)
	assert.Equal(t, "0.00", b.OpeningBalance)
	assert.False(t, b.Lines[0].ResetsBalance)
	assert.Equal(t, "100.00", b.Lines[0].RunningBalance)
	assert.Equal(t, "OPENING", b.Lines[1].Kind)
	assert.True(t, b.Lines[1].ResetsBalance)
	assert.Equal(t, "2027-01-01", b.Lines[1].Date)
	assert.Equal(t, "90.00", b.Lines[1].Income)
	assert.Equal(t, "90.00", b.Lines[1].RunningBalance)
	assert.Equal(t, "120.00", b.Lines[2].RunningBalance)
	assert.Equal(t, "130.00", b.TotalIn, "opening left out")
	assert.Equal(t, "0.00", b.TotalOut)
	assert.Equal(t, "120.00", b.ClosingBalance, "balance as of to")

	b = get("from=2027-01-01&to=2027-01-31")
	require.Len(t, b.Lines, 1, "an opening on `from` is the opening balance, not a line")
	assert.Equal(t, "90.00", b.OpeningBalance)
	assert.Equal(t, "120.00", b.ClosingBalance)

	b = get("from=2026-01-01&to=2027-01-02")
	assert.Equal(t, "90.00", b.ClosingBalance)
}

// Carry-forward stock checks: an OPEN count in the closing year blocks; a missing year-end count warns.
func TestV12_CarryForwardStockCountChecks(t *testing.T) {
	f := newCF(t)
	rec := f.post(t, f.path("/stock/counts"), f.token, `{"depotId":`+idStr(f.depot)+`,"countDate":"2026-12-20"}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	countID := extractID(t, rec.Body.Bytes())
	etag := rec.Header().Get("ETag")

	type check struct {
		Code     string `json:"code"`
		OK       bool   `json:"ok"`
		Blocking bool   `json:"blocking"`
		Detail   string `json:"detail"`
	}
	type preview struct {
		Checks []check `json:"checks"`
		Steps  []struct {
			Code   string `json:"code"`
			Status string `json:"status"`
		} `json:"steps"`
	}
	get := func() (map[string]check, map[string]string) {
		p := decode[preview](t, f.do(t, http.MethodGet, f.cfPath(2027, ""), f.token, ""))
		checks := map[string]check{}
		for _, c := range p.Checks {
			checks[c.Code] = c
		}
		steps := map[string]string{}
		for _, s := range p.Steps {
			steps[s.Code] = s.Status
		}
		return checks, steps
	}
	checks, steps := get()
	assert.False(t, checks["STOCK_COUNTS_POSTED"].OK)
	assert.True(t, checks["STOCK_COUNTS_POSTED"].Blocking)
	assert.Contains(t, checks["STOCK_COUNTS_POSTED"].Detail, "SAY-2026-1-")
	assert.False(t, checks["YEAR_END_COUNTS"].OK)
	assert.False(t, checks["YEAR_END_COUNTS"].Blocking)
	assert.Equal(t, "BLOCKED", steps["STOCK_COUNTS"])

	rec = f.cfPost(t, 2027)
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_CARRY_FORWARD_BLOCKED", errCode(t, rec))
	assert.Contains(t, rec.Body.String(), "STOCK_COUNTS_POSTED")

	// A count opened for the new year does not block.
	require.Equal(t, http.StatusCreated, f.post(t, f.path("/stock/counts"), f.token, `{"depotId":`+idStr(f.depot2)+`,"countDate":"2027-01-02"}`).Code)
	rec = f.req(t, http.MethodPost, f.path("/stock/counts/"+idStr(countID)+"/finalize"), f.token, `{"uncounted":"SKIP"}`,
		map[string]string{"If-Match": etag, "Idempotency-Key": uuid.NewString()})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	checks, steps = get()
	assert.True(t, checks["STOCK_COUNTS_POSTED"].OK)
	assert.False(t, checks["YEAR_END_COUNTS"].OK, "depot 2 has no count in 2026")
	assert.Contains(t, checks["YEAR_END_COUNTS"].Detail, "2")
	assert.Equal(t, "DONE", steps["STOCK_COUNTS"])
	require.Equal(t, http.StatusCreated, f.cfPost(t, 2027).Code, "a warning does not block")
}

func TestV12_CarryForwardYearEndCountsOkWhenEveryDepotCounted(t *testing.T) {
	f := newCF(t)
	for _, d := range []uint{f.depot, f.depot2} {
		rec := f.post(t, f.path("/stock/counts"), f.token, `{"depotId":`+idStr(d)+`,"countDate":"2026-12-31"}`)
		require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
		rec = f.req(t, http.MethodPost, f.path("/stock/counts/"+idStr(extractID(t, rec.Body.Bytes()))+"/finalize"), f.token, `{"uncounted":"SKIP"}`,
			map[string]string{"If-Match": rec.Header().Get("ETag"), "Idempotency-Key": uuid.NewString()})
		require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	}
	p := decode[struct {
		Checks []struct {
			Code string `json:"code"`
			OK   bool   `json:"ok"`
		} `json:"checks"`
	}](t, f.do(t, http.MethodGet, f.cfPath(2027, ""), f.token, ""))
	for _, c := range p.Checks {
		if c.Code == "YEAR_END_COUNTS" || c.Code == "STOCK_COUNTS_POSTED" {
			assert.True(t, c.OK, c.Code)
		}
	}
}

// Stock follows closed years: a count cannot be finalized into a carried year.
func TestV12_StockMovementsFollowClosedYears(t *testing.T) {
	f := newCF(t)
	require.Equal(t, http.StatusCreated, f.cfPost(t, 2027).Code)
	rec := f.post(t, f.path("/stock/counts"), f.token, `{"depotId":`+idStr(f.depot)+`,"countDate":"2026-12-31"}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_YEAR_CLOSED", errCode(t, rec))
}

type dashboardJSON struct {
	Date        string `json:"date"`
	Receivables struct {
		Total    string `json:"total"`
		Accounts int    `json:"accounts"`
	} `json:"receivables"`
	Payables struct {
		Total    string `json:"total"`
		Accounts int    `json:"accounts"`
	} `json:"payables"`
	Cash struct {
		Total  string `json:"total"`
		Depots []struct {
			DepotID uint   `json:"depotId"`
			Balance string `json:"balance"`
		} `json:"depots"`
	} `json:"cash"`
	InstrumentsDue30 struct {
		Total        string `json:"total"`
		OverdueCount int    `json:"overdueCount"`
	} `json:"instrumentsDue30"`
	DueThisWeek []struct {
		InstrumentNo string `json:"instrumentNo"`
		DaysToDue    int    `json:"daysToDue"`
	} `json:"dueThisWeek"`
	BelowMin      []levelJSON `json:"belowMin"`
	BelowMinCount int         `json:"belowMinCount"`
	NegativeCount int         `json:"negativeCount"`
	LedgerCheck   struct {
		Status    string  `json:"status"`
		CheckedAt *string `json:"checkedAt"`
	} `json:"ledgerCheck"`
}

func TestV12_DashboardAggregates(t *testing.T) {
	f := newCF(t)
	f.cashReceipt(t, "2026-03-10", "100.00")
	cat := f.post(t, f.path("/catalog/catalogs"), f.token, `{"code":1,"name":"C"}`)
	require.Equal(t, http.StatusCreated, cat.Code)
	catalog := extractID(t, cat.Body.Bytes())
	part := func(no string) uint {
		rec := f.post(t, f.path("/catalog/parts"), f.token, `{"catalogId":`+idStr(catalog)+`,"stockNo":"`+no+`","name":"`+no+`"}`)
		require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
		return extractID(t, rec.Body.Bytes())
	}
	a, b := part("A"), part("B")
	sf := &stockFix{ledgerFix: f.ledgerFix}
	sf.move(t, a, f.depot, "OUT", "2", "2026-03-01")
	sf.move(t, b, f.depot2, "IN", "1", "2026-03-01")
	require.Equal(t, http.StatusOK, f.req(t, http.MethodPut, f.path("/catalog/parts/"+idStr(b)+"/depots/"+idStr(f.depot2)), f.token,
		`{"minQty":"5"}`, map[string]string{"If-Match": `"0"`}).Code)

	for i, due := range []string{"2026-12-20", "2027-01-03", "2027-03-01"} {
		require.NoError(t, testDB.Exec(`INSERT INTO instruments (org_id, depot_id, instrument_no, type, serial_no, amount, remaining,
			issue_date, due_date, sicil_id, status) VALUES (?, ?, ?, 'RECEIVABLE_CHEQUE', ?, 50, 50, '2026-12-01', ?, ?, 'PORTFOLIO')`,
			f.org, f.depot, fmt.Sprintf("AC-2026-1-00000%d", i+1), fmt.Sprint(i), due, f.sicA).Error)
	}
	rec := f.do(t, http.MethodGet, f.path("/dashboard?date=2026-12-31"), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	d := decode[dashboardJSON](t, rec)
	assert.Equal(t, "2026-12-31", d.Date)
	assert.Equal(t, "470.00", d.Receivables.Total, "A: 500 + 70 − 100 collected")
	assert.Equal(t, 1, d.Receivables.Accounts)
	assert.Equal(t, "200.00", d.Payables.Total)
	assert.Equal(t, 1, d.Payables.Accounts)
	assert.Equal(t, "100.00", d.Cash.Total)
	require.Len(t, d.Cash.Depots, 2)
	assert.Equal(t, "100.00", d.InstrumentsDue30.Total)
	assert.Equal(t, 1, d.InstrumentsDue30.OverdueCount)
	require.Len(t, d.DueThisWeek, 2)
	assert.Equal(t, -11, d.DueThisWeek[0].DaysToDue)
	assert.Equal(t, 3, d.DueThisWeek[1].DaysToDue)
	require.Len(t, d.BelowMin, 2)
	assert.True(t, d.BelowMin[0].Negative, "negative first")
	assert.Equal(t, b, d.BelowMin[1].PartID)
	assert.Equal(t, 1, d.BelowMinCount)
	assert.Equal(t, 1, d.NegativeCount)
	assert.Contains(t, []string{"BALANCED", "NOT_RUN"}, d.LedgerCheck.Status)

	one := decode[dashboardJSON](t, f.do(t, http.MethodGet, f.path("/dashboard?date=2026-12-31&depotId="+idStr(f.depot2)), f.token, ""))
	require.Len(t, one.BelowMin, 1)
	assert.Equal(t, 0, one.NegativeCount)
	require.Len(t, one.Cash.Depots, 1)
	assert.Equal(t, "70.00", one.Receivables.Total)
}
