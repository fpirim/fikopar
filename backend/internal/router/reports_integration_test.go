//go:build integration

package router_test

import (
	"archive/zip"
	"bytes"
	"fmt"
	"io"
	"net/http"
	"strings"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

const (
	xlsxType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
	pdfType  = "application/pdf"
)

func sheetXML(t *testing.T, body []byte) string {
	t.Helper()
	zr, err := zip.NewReader(bytes.NewReader(body), int64(len(body)))
	require.NoError(t, err)
	for _, f := range zr.File {
		if f.Name == "xl/worksheets/sheet1.xml" {
			rc, err := f.Open()
			require.NoError(t, err)
			defer rc.Close()
			b, err := io.ReadAll(rc)
			require.NoError(t, err)
			return string(b)
		}
	}
	t.Fatal("no sheet1.xml")
	return ""
}

type balanceRow struct {
	AccountID    uint    `json:"accountId"`
	AccountCode  string  `json:"accountCode"`
	AccountName  string  `json:"accountName"`
	SicilID      *uint   `json:"sicilId"`
	SicilTipi    *string `json:"sicilTipi"`
	City         *string `json:"city"`
	DepotID      *uint   `json:"depotId"`
	DebitTotal   string  `json:"debitTotal"`
	CreditTotal  string  `json:"creditTotal"`
	Balance      string  `json:"balance"`
	Overdue      string  `json:"overdue"`
	LastMoveDate *string `json:"lastMoveDate"`
}

type balanceGrid struct {
	Rows            []balanceRow      `json:"rows"`
	TotalCount      int64             `json:"totalCount"`
	UnfilteredCount int64             `json:"unfilteredCount"`
	Totals          map[string]string `json:"totals"`
}

type reportFix struct {
	*ledgerFix
	depot2           uint
	sales, exp       uint
	accA, accB, accC uint
	sicA, sicB, sicC uint
	cash             uint
}

// Customers A (Adana) and B (Bursa), supplier C (Ceyhan), two depots.
func newReport(t *testing.T) *reportFix {
	t.Helper()
	f := &reportFix{ledgerFix: newLedger(t)}
	f.depot2 = f.newDepot(t, "2")
	f.sales = f.account(t, "600", "INCOME")
	f.exp = f.account(t, "770", "EXPENSE")
	f.cash = f.account(t, "100", "ASSET")
	f.setRoles(t, fmt.Sprintf(`[{"role":"CASH","accountId":%d}]`, f.cash))
	mk := func(no, tipi, city, code, typ string) (uint, uint) {
		rec := f.do(t, http.MethodPost, f.path("/sicils"), f.token, fmt.Sprintf(`{"hesap_no":%q,"hesap_adi":"Firma %s","sicil_tipi":%q,"sehir":%q}`, no, no, tipi, city))
		require.Equal(t, http.StatusCreated, rec.Code)
		s := extractID(t, rec.Body.Bytes())
		rec = f.post(t, f.path("/ledger/accounts"), f.token, fmt.Sprintf(`{"code":%q,"name":"Firma %s","type":%q,"sicilId":%d}`, code, no, typ, s))
		require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
		return s, extractID(t, rec.Body.Bytes())
	}
	f.sicA, f.accA = mk("A", "MUSTERI", "Adana", "120.A", "ASSET")
	f.sicB, f.accB = mk("B", "MUSTERI", "Bursa", "120.B", "ASSET")
	f.sicC, f.accC = mk("C", "FIRMA", "Ceyhan", "320.C", "LIABILITY")
	return f
}

func (f *reportFix) post2(t *testing.T, depot uint, date, lines string) {
	t.Helper()
	rec := f.post(t, f.path("/ledger/entries"), f.token, fmt.Sprintf(`{"depotId":%d,"entryDate":%q,"description":"e %s","lines":[%s]}`, depot, date, date, lines))
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
}

func line(account uint, side, amount, due string) string {
	d := ""
	if due != "" {
		d = fmt.Sprintf(`,"dueDate":%q`, due)
	}
	return fmt.Sprintf(`{"accountId":%d,"side":%q,"amount":%q%s}`, account, side, amount, d)
}

func (f *reportFix) seedMarch(t *testing.T) {
	t.Helper()
	f.post2(t, f.depot, "2026-03-01", line(f.accA, "DEBIT", "500.00", "2026-03-15")+","+line(f.sales, "CREDIT", "500.00", ""))
	rec := f.post(t, f.path("/receipts"), f.token, receiptBody("COLLECTION", f.depot, f.sicA, "2026-03-20", "CASH", "100.00", ""))
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	f.post2(t, f.depot, "2026-03-31", line(f.accB, "DEBIT", "300.00", "")+","+line(f.sales, "CREDIT", "300.00", ""))
	f.post2(t, f.depot, "2026-03-05", line(f.exp, "DEBIT", "200.00", "")+","+line(f.accC, "CREDIT", "200.00", ""))
	f.post2(t, f.depot2, "2026-03-06", line(f.accA, "DEBIT", "70.00", "")+","+line(f.sales, "CREDIT", "70.00", ""))
	f.post2(t, f.depot, "2026-04-01", line(f.accA, "DEBIT", "999.00", "")+","+line(f.sales, "CREDIT", "999.00", ""))
}

func (f *reportFix) grid(t *testing.T, body string) balanceGrid {
	t.Helper()
	rec := f.do(t, http.MethodPost, f.path("/ledger/balances/grid"), f.token, body)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[balanceGrid](t, rec)
}

func byCode(rows []balanceRow) map[string]balanceRow {
	m := map[string]balanceRow{}
	for _, r := range rows {
		m[r.AccountCode] = r
	}
	return m
}

func TestBalances_AsOfIsInclusiveWithOverdueAndTotals(t *testing.T) {
	f := newReport(t)
	f.seedMarch(t)

	g := f.grid(t, fmt.Sprintf(`{"asOf":"2026-03-31","depotId":%d,"startRow":0,"endRow":100}`, f.depot))
	assert.Equal(t, int64(3), g.TotalCount)
	assert.Equal(t, int64(3), g.UnfilteredCount)
	rows := byCode(g.Rows)
	a := rows["120.A"]
	assert.Equal(t, "500.00", a.DebitTotal)
	assert.Equal(t, "100.00", a.CreditTotal)
	assert.Equal(t, "400.00", a.Balance)
	assert.Equal(t, "400.00", a.Overdue)
	assert.Equal(t, "Adana", *a.City)
	assert.Equal(t, "MUSTERI", *a.SicilTipi)
	assert.Equal(t, "2026-03-20", *a.LastMoveDate)
	assert.Equal(t, f.depot, *a.DepotID)
	assert.Equal(t, "300.00", rows["120.B"].Balance, "entries on the as-of day are included")
	assert.Equal(t, "0.00", rows["120.B"].Overdue)
	assert.Equal(t, "-200.00", rows["320.C"].Balance)
	assert.Equal(t, map[string]string{"debit": "800.00", "credit": "300.00", "balance": "500.00", "overdue": "400.00"}, g.Totals)

	g = f.grid(t, `{"asOf":"2026-03-30","startRow":0,"endRow":100}`)
	rows = byCode(g.Rows)
	assert.Equal(t, "0.00", rows["120.B"].Balance)
	assert.Equal(t, "470.00", rows["120.A"].Balance, "all depots")
	assert.Nil(t, rows["120.A"].DepotID)
}

func TestBalances_FiltersSortingAndPaging(t *testing.T) {
	f := newReport(t)
	f.seedMarch(t)
	base := fmt.Sprintf(`"asOf":"2026-03-31","depotId":%d`, f.depot)

	g := f.grid(t, `{`+base+`,"sicilTipi":"MUSTERI","startRow":0,"endRow":100}`)
	assert.Equal(t, int64(2), g.TotalCount)
	assert.Equal(t, int64(2), g.UnfilteredCount)

	g = f.grid(t, `{`+base+`,"startRow":0,"endRow":100,"filterModel":{"balance":{"filterType":"number","type":"greaterThan","filter":350}}}`)
	assert.Equal(t, int64(1), g.TotalCount)
	assert.Equal(t, int64(3), g.UnfilteredCount)
	require.Len(t, g.Rows, 1)
	assert.Equal(t, "120.A", g.Rows[0].AccountCode)
	assert.Equal(t, "400.00", g.Totals["balance"], "totals follow the filters")

	g = f.grid(t, `{`+base+`,"startRow":0,"endRow":100,"filterModel":{"city":{"filterType":"text","type":"contains","filter":"bur"},"balance":{"filterType":"number","type":"inRange","filter":"0","filterTo":"1000"}}}`)
	require.Len(t, g.Rows, 1)
	assert.Equal(t, "120.B", g.Rows[0].AccountCode)

	g = f.grid(t, `{`+base+`,"startRow":0,"endRow":100,"filterModel":{"lastMoveDate":{"filterType":"date","type":"inRange","dateFrom":"2026-03-20 00:00:00","dateTo":"2026-03-31 00:00:00"}}}`)
	assert.Equal(t, int64(2), g.TotalCount)

	g = f.grid(t, `{`+base+`,"startRow":0,"endRow":100,"search":"ceyhan"}`)
	require.Len(t, g.Rows, 1)
	assert.Equal(t, "320.C", g.Rows[0].AccountCode)

	g = f.grid(t, `{`+base+`,"startRow":1,"endRow":2,"sortModel":[{"colId":"balance","sort":"desc"}]}`)
	assert.Equal(t, int64(3), g.TotalCount)
	require.Len(t, g.Rows, 1)
	assert.Equal(t, "120.B", g.Rows[0].AccountCode)

	rec := f.do(t, http.MethodPost, f.path("/ledger/balances/grid"), f.token, `{"startRow":0,"endRow":10}`)
	assert.Equal(t, http.StatusBadRequest, rec.Code, "asOf is required")
	rec = f.do(t, http.MethodPost, f.path("/ledger/balances/grid"), f.token, `{`+base+`,"filterModel":{"balance":{"filterType":"number","type":"greaterThan","filter":"abc"}}}`)
	assert.Equal(t, http.StatusBadRequest, rec.Code)
}

func TestBalances_XLSXHasAllFilteredRowsAndTotals(t *testing.T) {
	f := newReport(t)
	f.seedMarch(t)
	rec := f.req(t, http.MethodPost, f.path("/ledger/balances/grid"), f.token,
		fmt.Sprintf(`{"asOf":"2026-03-31","depotId":%d,"startRow":0,"endRow":1,"sicilTipi":"MUSTERI"}`, f.depot), map[string]string{"Accept": xlsxType})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Equal(t, xlsxType, rec.Header().Get("Content-Type"))
	assert.Contains(t, rec.Header().Get("Content-Disposition"), ".xlsx")
	sheet := sheetXML(t, rec.Body.Bytes())
	assert.Contains(t, sheet, "120.A")
	assert.Contains(t, sheet, "120.B", "export ignores paging")
	assert.NotContains(t, sheet, "320.C")
	assert.Contains(t, sheet, "<v>700.00</v>")
}

type statementJSON struct {
	AccountID      uint     `json:"accountId"`
	From           string   `json:"from"`
	To             string   `json:"to"`
	ClosingBalance string   `json:"closingBalance"`
	BroughtForward []stLine `json:"broughtForward"`
	Lines          []stLine `json:"lines"`
	DepotTotals    []struct {
		DepotID uint   `json:"depotId"`
		Debit   string `json:"debit"`
		Credit  string `json:"credit"`
		Balance string `json:"balance"`
	} `json:"depotTotals"`
}

type stLine struct {
	EntryID        *uint   `json:"entryId"`
	EntryDate      string  `json:"entryDate"`
	DepotID        *uint   `json:"depotId"`
	DocumentNo     *string `json:"documentNo"`
	SourceType     string  `json:"sourceType"`
	Subtype        *string `json:"subtype"`
	DueDate        *string `json:"dueDate"`
	Description    string  `json:"description"`
	Debit          string  `json:"debit"`
	Credit         string  `json:"credit"`
	RunningBalance string  `json:"runningBalance"`
}

func TestStatement_BroughtForwardPerDepotRunningBalanceAndDepotTotals(t *testing.T) {
	f := newReport(t)
	f.post2(t, f.depot, "2026-02-10", line(f.accA, "DEBIT", "100.00", "")+","+line(f.sales, "CREDIT", "100.00", ""))
	f.post2(t, f.depot2, "2026-02-20", line(f.accA, "DEBIT", "50.00", "")+","+line(f.sales, "CREDIT", "50.00", ""))
	rec := f.post(t, f.path("/receipts"), f.token, receiptBody("COLLECTION", f.depot, f.sicA, "2026-03-05", "CASH", "30.00", ""))
	require.Equal(t, http.StatusCreated, rec.Code)
	rcpt := decode[receiptJSON](t, rec)
	f.post2(t, f.depot2, "2026-03-31", line(f.accA, "DEBIT", "20.00", "2026-04-30")+","+line(f.sales, "CREDIT", "20.00", ""))
	f.post2(t, f.depot, "2026-04-01", line(f.accA, "DEBIT", "999.00", "")+","+line(f.sales, "CREDIT", "999.00", ""))

	path := f.path("/ledger/accounts/" + idStr(f.accA) + "/statement?from=2026-03-01&to=2026-03-31")
	rec = f.do(t, http.MethodGet, path, f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	s := decode[statementJSON](t, rec)

	require.Len(t, s.BroughtForward, 2)
	assert.Nil(t, s.BroughtForward[0].EntryID)
	assert.Equal(t, "2026-03-01", s.BroughtForward[0].EntryDate)
	assert.Equal(t, "100.00", s.BroughtForward[0].Debit)
	assert.Equal(t, "50.00", s.BroughtForward[1].Debit)
	assert.Equal(t, "150.00", s.BroughtForward[1].RunningBalance)

	require.Len(t, s.Lines, 2)
	assert.Equal(t, "30.00", s.Lines[0].Credit)
	assert.Equal(t, rcpt.ReceiptNo, *s.Lines[0].DocumentNo)
	assert.Equal(t, "receipt", s.Lines[0].SourceType)
	assert.Equal(t, "COLLECTION", *s.Lines[0].Subtype)
	assert.Equal(t, "120.00", s.Lines[0].RunningBalance)
	assert.Equal(t, "2026-03-31", s.Lines[1].EntryDate, "`to` is inclusive")
	assert.Equal(t, "2026-04-30", *s.Lines[1].DueDate)
	assert.Equal(t, "140.00", s.Lines[1].RunningBalance)
	assert.Equal(t, "140.00", s.ClosingBalance)

	require.Len(t, s.DepotTotals, 2)
	assert.Equal(t, f.depot, s.DepotTotals[0].DepotID)
	assert.Equal(t, "100.00", s.DepotTotals[0].Debit)
	assert.Equal(t, "30.00", s.DepotTotals[0].Credit)
	assert.Equal(t, "70.00", s.DepotTotals[0].Balance)
	assert.Equal(t, "70.00", s.DepotTotals[1].Balance)

	one := decode[statementJSON](t, f.do(t, http.MethodGet, path+"&depotId="+idStr(f.depot2), f.token, ""))
	assert.Len(t, one.Lines, 1)
	assert.Equal(t, "70.00", one.ClosingBalance)

	rec = f.req(t, http.MethodGet, path, f.token, "", map[string]string{"Accept": xlsxType})
	require.Equal(t, http.StatusOK, rec.Code)
	sheet := sheetXML(t, rec.Body.Bytes())
	assert.Contains(t, sheet, rcpt.ReceiptNo)
	assert.Contains(t, sheet, "<v>140.00</v>")

	rec = f.req(t, http.MethodGet, path, f.token, "", map[string]string{"Accept": pdfType})
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Equal(t, pdfType, rec.Header().Get("Content-Type"))
	assert.True(t, strings.HasPrefix(rec.Body.String(), "%PDF-"))

	other := f.seedOrg(t, "st-other", f.userID)
	assert.Equal(t, http.StatusNotFound, f.do(t, http.MethodGet, orgPath(other, "/ledger/accounts/"+idStr(f.accA)+"/statement?from=2026-03-01&to=2026-03-31"), f.token, "").Code)
	assert.Equal(t, http.StatusBadRequest, f.do(t, http.MethodGet, f.path("/ledger/accounts/"+idStr(f.accA)+"/statement?from=2026-03-01"), f.token, "").Code)
}

func TestExports_ReceiptPDFAndCashBookAndReceiptGrid(t *testing.T) {
	f := newCash(t)
	r := f.receipt(t, receiptBody("COLLECTION", f.depot, f.customer, "2026-03-10", "CASH", "250.00", `,"description":"Şubat tahsilatı"`))

	rec := f.req(t, http.MethodGet, f.path("/receipts/"+idStr(r.ID)), f.token, "", map[string]string{"Accept": pdfType})
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Equal(t, pdfType, rec.Header().Get("Content-Type"))
	assert.True(t, strings.HasPrefix(rec.Body.String(), "%PDF-"))
	assert.Contains(t, rec.Header().Get("Content-Disposition"), r.ReceiptNo)

	path := f.path("/cash-book?depotId=" + idStr(f.depot) + "&from=2026-03-01&to=2026-03-31")
	rec = f.req(t, http.MethodGet, path, f.token, "", map[string]string{"Accept": xlsxType})
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Contains(t, sheetXML(t, rec.Body.Bytes()), r.ReceiptNo)
	rec = f.req(t, http.MethodGet, path, f.token, "", map[string]string{"Accept": pdfType})
	require.Equal(t, http.StatusOK, rec.Code)
	assert.True(t, strings.HasPrefix(rec.Body.String(), "%PDF-"))

	rec = f.req(t, http.MethodPost, f.path("/receipts/grid"), f.token, `{"startRow":0,"endRow":50}`, map[string]string{"Accept": xlsxType})
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Contains(t, sheetXML(t, rec.Body.Bytes()), r.ReceiptNo)
}
