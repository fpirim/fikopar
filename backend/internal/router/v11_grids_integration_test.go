//go:build integration

package router_test

import (
	"encoding/base64"
	"fmt"
	"net/http"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestV11_BalanceColIDsAreTheRowNames(t *testing.T) {
	f := newReport(t)
	f.seedMarch(t)
	base := fmt.Sprintf(`"asOf":"2026-03-31","depotId":%d,"startRow":0,"endRow":100`, f.depot)

	g := f.grid(t, `{`+base+`,"filterModel":{"accountName":{"filterType":"text","type":"contains","filter":"Firma B"},"debitTotal":{"filterType":"number","type":"greaterThan","filter":1}},"sortModel":[{"colId":"creditTotal","sort":"desc"}]}`)
	require.Len(t, g.Rows, 1)
	assert.Equal(t, "120.B", g.Rows[0].AccountCode)
	assert.Equal(t, "300.00", g.Totals["debitTotal"])
	assert.Equal(t, "0.00", g.Totals["creditTotal"])

	for _, bad := range []string{
		`"sortModel":[{"colId":"debit","sort":"asc"}]`,
		`"sortModel":[{"colId":"name","sort":"asc"}]`,
		`"filterModel":{"credit":{"filterType":"number","type":"equals","filter":1}}`,
		`"filterModel":{"sicil_tipi":{"filterType":"text","type":"equals","filter":"MUSTERI"}}`,
	} {
		rec := f.do(t, http.MethodPost, f.path("/ledger/balances/grid"), f.token, `{`+base+`,`+bad+`}`)
		assert.Equal(t, http.StatusBadRequest, rec.Code, bad)
		assert.Equal(t, "ERR_INVALID_REQUEST", errCode(t, rec), bad)
	}
}

func TestV11_ReceiptGridTakesDepotAndDateFilters(t *testing.T) {
	f := newCash(t)
	depot2 := f.newDepot(t, "2")
	f.receipt(t, receiptBody("COLLECTION", f.depot, f.customer, "2026-03-10", "CASH", "5.00", ""))
	f.receipt(t, receiptBody("COLLECTION", f.depot, f.customer, "2026-03-20", "CASH", "6.00", ""))
	f.receipt(t, receiptBody("COLLECTION", depot2, f.customer, "2026-03-10", "CASH", "7.00", ""))
	type grid struct {
		Rows       []receiptJSON `json:"rows"`
		TotalCount int64         `json:"totalCount"`
	}
	get := func(body string) grid {
		rec := f.do(t, http.MethodPost, f.path("/receipts/grid"), f.token, body)
		require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
		return decode[grid](t, rec)
	}
	assert.Equal(t, int64(3), get(`{"startRow":0,"endRow":50,"depotId":null}`).TotalCount)
	g := get(fmt.Sprintf(`{"startRow":0,"endRow":50,"depotId":%d}`, depot2))
	require.Len(t, g.Rows, 1)
	assert.Equal(t, "TAH-2026-2-000001", g.Rows[0].ReceiptNo)

	g = get(fmt.Sprintf(`{"startRow":0,"endRow":50,"depotId":%d,"filterModel":{"date":{"filterType":"date","type":"inRange","dateFrom":"2026-03-15 00:00:00","dateTo":"2026-03-20 00:00:00"}}}`, f.depot))
	require.Len(t, g.Rows, 1, "dateTo is inclusive and the time part is ignored")
	assert.Equal(t, "6.00", g.Rows[0].Amount)

	g = get(`{"startRow":0,"endRow":50,"filterModel":{"amount":{"filterType":"number","type":"greaterThan","filter":5}},"sortModel":[{"colId":"amount","sort":"asc"}]}`)
	require.Len(t, g.Rows, 2)
	assert.Equal(t, "6.00", g.Rows[0].Amount)
}

func TestV11_ListAccountsByCodePrefixAndRole(t *testing.T) {
	f := newCash(t)
	f.account(t, "102.02", "ASSET")
	f.account(t, "102", "ASSET")
	type page struct {
		Items []accountJSON `json:"items"`
	}
	codes := func(q string) []string {
		rec := f.do(t, http.MethodGet, f.path("/ledger/accounts?"+q), f.token, "")
		require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
		var out []string
		for _, a := range decode[page](t, rec).Items {
			out = append(out, a.Code)
		}
		return out
	}
	assert.Equal(t, []string{"102", "102.01", "102.02"}, codes("role=BANK"))
	assert.Equal(t, []string{"770.01"}, codes("role=EXPENSE"))
	assert.Equal(t, []string{"100.01"}, codes("role=CASH"))
	assert.Equal(t, []string{"100.01", "102", "102.01", "102.02"}, codes("codePrefix=10"))
	assert.Equal(t, []string{"102.02"}, codes("codePrefix=102.0&role=BANK&type=ASSET&limit=1&cursor="+cursorAfter("102.01")))
	assert.Empty(t, codes("codePrefix=102&type=EXPENSE"))
	for _, bad := range []string{"role=VAT", "codePrefix=abc", "codePrefix=10%25"} {
		assert.Equal(t, http.StatusBadRequest, f.do(t, http.MethodGet, f.path("/ledger/accounts?"+bad), f.token, "").Code, bad)
	}
}

func TestV11_EntryNumbersOnReversalsAndTrailLogin(t *testing.T) {
	f := newCash(t)
	orig := f.entry(t, "2026-03-05", ln{f.cash, "DEBIT", "8.00"}, ln{f.expense, "CREDIT", "8.00"})
	rec := f.post(t, f.path("/ledger/entries/"+idStr(orig.ID)+"/reversal"), f.token, `{"reason":"wrong"}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	type numbers struct {
		EntryNo           string  `json:"entryNo"`
		ReversesEntryNo   *string `json:"reversesEntryNo"`
		ReversedByEntryNo *string `json:"reversedByEntryNo"`
	}
	rev := decode[numbers](t, rec)
	require.NotNil(t, rev.ReversesEntryNo)
	assert.Equal(t, orig.EntryNo, *rev.ReversesEntryNo)
	assert.Nil(t, rev.ReversedByEntryNo)
	o := decode[numbers](t, f.do(t, http.MethodGet, f.path("/ledger/entries/"+idStr(orig.ID)), f.token, ""))
	require.NotNil(t, o.ReversedByEntryNo)
	assert.Equal(t, rev.EntryNo, *o.ReversedByEntryNo)

	type item struct {
		Kind       string  `json:"kind"`
		ActorLogin *string `json:"actorLogin"`
	}
	items := decode[[]item](t, f.do(t, http.MethodGet, f.path("/ledger/entries/"+idStr(orig.ID)+"/trail"), f.token, ""))
	require.NotEmpty(t, items)
	require.NotNil(t, items[0].ActorLogin)
	assert.Equal(t, "clerk", *items[0].ActorLogin)
}

func cursorAfter(code string) string { return base64.RawURLEncoding.EncodeToString([]byte(code)) }
