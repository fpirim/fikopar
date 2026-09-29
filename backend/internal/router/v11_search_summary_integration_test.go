//go:build integration

package router_test

import (
	"fmt"
	"net/http"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type summaryJSON struct {
	SicilID            uint    `json:"sicilId"`
	AccountID          uint    `json:"accountId"`
	AccountCode        string  `json:"accountCode"`
	Balance            string  `json:"balance"`
	Overdue            string  `json:"overdue"`
	CreditLimit        *string `json:"creditLimit"`
	CreditUsedPct      *int    `json:"creditUsedPct"`
	PaymentTermDays    *int    `json:"paymentTermDays"`
	Discount1Pct       string  `json:"discount1Pct"`
	Discount2Pct       string  `json:"discount2Pct"`
	ChequesInPortfolio string  `json:"chequesInPortfolio"`
	LastMoveDate       *string `json:"lastMoveDate"`
}

func (f *reportFix) instrument(t *testing.T, sicil uint, no, status, amount, remaining string) {
	t.Helper()
	require.NoError(t, testDB.Exec(`INSERT INTO instruments (org_id, depot_id, instrument_no, type, serial_no, amount, remaining,
		issue_date, due_date, sicil_id, status) VALUES (?, ?, ?, 'RECEIVABLE_CHEQUE', ?, ?, ?, '2026-03-01', '2026-06-01', ?, ?)`,
		f.org, f.depot, no, no, amount, remaining, sicil, status).Error)
}

func TestV11_SicilSummaryFeedsTheCreditPanel(t *testing.T) {
	f := newReport(t)
	f.seedMarch(t)
	require.NoError(t, testDB.Exec(`UPDATE sicils SET credit_limit = 1000, gun = '30', iskonto = 5, iskonto2 = 2.5 WHERE id = ?`, f.sicA).Error)
	f.instrument(t, f.sicA, "AC-2026-1-000001", "PORTFOLIO", "300", "300")
	f.instrument(t, f.sicA, "AC-2026-1-000002", "PARTIALLY_COLLECTED", "100", "40")
	f.instrument(t, f.sicA, "AC-2026-1-000003", "COLLECTED", "50", "0")
	f.instrument(t, f.sicB, "AC-2026-1-000004", "PORTFOLIO", "80", "80")

	get := func(sicil uint, query string) summaryJSON {
		rec := f.do(t, http.MethodGet, f.path(fmt.Sprintf("/sicils/%d/summary%s", sicil, query)), f.token, "")
		require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
		return decode[summaryJSON](t, rec)
	}
	s := get(f.sicA, fmt.Sprintf("?asOf=2026-03-31&depotId=%d", f.depot))
	assert.Equal(t, f.sicA, s.SicilID)
	assert.Equal(t, f.accA, s.AccountID)
	assert.Equal(t, "120.A", s.AccountCode)
	assert.Equal(t, "400.00", s.Balance)
	assert.Equal(t, "400.00", s.Overdue)
	require.NotNil(t, s.CreditLimit)
	assert.Equal(t, "1000.00", *s.CreditLimit)
	require.NotNil(t, s.CreditUsedPct)
	assert.Equal(t, 40, *s.CreditUsedPct)
	require.NotNil(t, s.PaymentTermDays)
	assert.Equal(t, 30, *s.PaymentTermDays)
	assert.Equal(t, "5", s.Discount1Pct)
	assert.Equal(t, "2.5", s.Discount2Pct)
	assert.Equal(t, "340.00", s.ChequesInPortfolio)
	require.NotNil(t, s.LastMoveDate)
	assert.Equal(t, "2026-03-20", *s.LastMoveDate)

	assert.Equal(t, "470.00", get(f.sicA, "?asOf=2026-03-31").Balance, "all depots")
	assert.Equal(t, "1469.00", get(f.sicA, "").Balance, "asOf defaults to today")

	b := get(f.sicB, "?asOf=2026-03-31")
	assert.Nil(t, b.CreditLimit)
	assert.Nil(t, b.CreditUsedPct)
	assert.Nil(t, b.PaymentTermDays)
	assert.Equal(t, "80.00", b.ChequesInPortfolio)

	bare := f.sicil(t, "N1", "MUSTERI")
	for _, c := range []struct {
		path   string
		status int
		code   string
	}{
		{"/sicils/999999/summary", 404, "ERR_SICIL_NOT_FOUND"},
		{"/sicils/" + idStr(bare) + "/summary", 404, "ERR_ACCOUNT_NOT_FOUND"},
		{"/sicils/" + idStr(f.sicA) + "/summary?asOf=x", 400, "ERR_INVALID_REQUEST"},
		{"/sicils/" + idStr(f.sicA) + "/summary?depotId=999999", 404, "ERR_DEPOT_NOT_FOUND"},
	} {
		rec := f.do(t, http.MethodGet, f.path(c.path), f.token, "")
		assert.Equal(t, c.status, rec.Code, c.path)
		assert.Equal(t, c.code, errCode(t, rec), c.path)
	}
}

type searchJSON struct {
	Accounts []struct {
		SicilID   uint   `json:"sicilId"`
		AccountID *uint  `json:"accountId"`
		HesapNo   string `json:"hesapNo"`
		HesapAdi  string `json:"hesapAdi"`
	} `json:"accounts"`
	Parts     []any `json:"parts"`
	Documents []struct {
		Kind      string  `json:"kind"`
		ID        uint    `json:"id"`
		Number    string  `json:"number"`
		Subtype   string  `json:"subtype"`
		SicilID   *uint   `json:"sicilId"`
		SicilName *string `json:"sicilName"`
		Date      string  `json:"date"`
		DepotID   uint    `json:"depotId"`
		Amount    string  `json:"amount"`
	} `json:"documents"`
}

func TestV11_GlobalSearchFindsCardsWithAccountsAndDocuments(t *testing.T) {
	f := newReport(t)
	f.seedMarch(t)
	bare := f.sicil(t, "N1", "MUSTERI")
	search := func(q string) searchJSON {
		rec := f.do(t, http.MethodGet, f.path("/search?"+q), f.token, "")
		require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
		return decode[searchJSON](t, rec)
	}

	r := search("q=firma")
	require.Len(t, r.Accounts, 3)
	assert.Equal(t, "A", r.Accounts[0].HesapNo)
	require.NotNil(t, r.Accounts[0].AccountID)
	assert.Equal(t, f.accA, *r.Accounts[0].AccountID)
	assert.NotNil(t, r.Parts)
	assert.Empty(t, r.Parts)
	assert.Len(t, search("q=firma&limit=2").Accounts, 2)

	r = search("q=120.B")
	require.Len(t, r.Accounts, 1, "account codes match too")
	assert.Equal(t, f.sicB, r.Accounts[0].SicilID)

	r = search("q=N1")
	require.Len(t, r.Accounts, 1)
	assert.Equal(t, bare, r.Accounts[0].SicilID)
	assert.Nil(t, r.Accounts[0].AccountID)

	r = search("q=TAH-2026")
	assert.Empty(t, r.Accounts)
	require.Len(t, r.Documents, 1)
	d := r.Documents[0]
	assert.Equal(t, "RECEIPT", d.Kind)
	assert.Equal(t, "TAH-2026-1-000001", d.Number)
	assert.Equal(t, "COLLECTION", d.Subtype)
	assert.Equal(t, "Firma A", *d.SicilName)
	assert.Equal(t, "2026-03-20", d.Date)
	assert.Equal(t, "100.00", d.Amount)
	assert.Equal(t, f.depot, d.DepotID)
	assert.Empty(t, search("q=TAH_2026").Documents, "LIKE wildcards are literal")

	other := f.seedOrg(t, "search-other", f.userID)
	rec := f.do(t, http.MethodGet, orgPath(other, "/search?q=firma"), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Empty(t, decode[searchJSON](t, rec).Accounts)

	for _, bad := range []string{"", "q=a", "q=ab&limit=21", "q=ab&limit=x"} {
		assert.Equal(t, http.StatusBadRequest, f.do(t, http.MethodGet, f.path("/search?"+bad), f.token, "").Code, bad)
	}
}

func TestV11_GlobalSearchKeepsDocumentsInTheirOrg(t *testing.T) {
	f := newReport(t)
	f.seedMarch(t)
	rec := f.post(t, f.path("/cash-vouchers"), f.token, fmt.Sprintf(`{"kind":"EXPENSE","depotId":%d,"date":"2026-03-21","accountId":%d,"amount":"10.00","description":"x"}`, f.depot, f.exp))
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	docs := func(org uint) []string {
		rec := f.do(t, http.MethodGet, orgPath(org, "/search?q=2026-1-"), f.token, "")
		require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
		var kinds []string
		for _, d := range decode[searchJSON](t, rec).Documents {
			kinds = append(kinds, d.Kind)
		}
		return kinds
	}
	assert.ElementsMatch(t, []string{"RECEIPT", "CASH_VOUCHER"}, docs(f.org))

	other := f.seedOrg(t, "search-docs-other", f.userID)
	assert.Empty(t, docs(other), "receipt and voucher numbers of another org")
}
