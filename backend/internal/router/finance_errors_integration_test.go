//go:build integration

package router_test

import (
	"context"
	"fmt"
	"net/http"
	"testing"
	"time"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestFinance_BadInputsAreRejected(t *testing.T) {
	f := newCash(t)
	get, post, patch, put, del := http.MethodGet, http.MethodPost, http.MethodPatch, http.MethodPut, http.MethodDelete
	cases := []struct {
		method, path, body string
		status             int
	}{
		{get, "/sicils/abc", "", 400},
		{post, "/sicils/grid", "{bad", 400},
		{get, "/ledger/accounts/0", "", 400},
		{get, "/ledger/accounts?sicilId=x", "", 400},
		{get, "/ledger/accounts?cursor=!!!", "", 400},
		{patch, "/ledger/accounts/x", `{}`, 400},
		{patch, fmt.Sprintf("/ledger/accounts/%d", f.cash), `{"name":""}`, 400},
		{patch, "/ledger/accounts/999999", `{"name":"x"}`, 404},
		{get, "/number-series?year=x", "", 400},
		{get, "/number-series?depotId=x", "", 400},
		{get, "/ledger/entries/x", "", 400},
		{get, "/ledger/entries/999999", "", 404},
		{get, "/ledger/entries/x/trail", "", 400},
		{post, "/ledger/entries/x/reversal", `{"reason":"abc"}`, 400},
		{post, "/ledger/entries", `{bad`, 400},
		{post, "/ledger/entries", fmt.Sprintf(`{"depotId":%d,"description":"x","lines":[]}`, f.depot), 400},
		{post, "/ledger/entries", fmt.Sprintf(`{"depotId":%d,"entryDate":"2026-01-01","description":" ","lines":[]}`, f.depot), 400},
		{post, "/ledger/entries", `{"entryDate":"2026-01-01","description":"x","lines":[]}`, 400},
		{get, "/ledger/entries?depotId=x", "", 400},
		{get, "/ledger/entries?accountId=x", "", 400},
		{get, "/ledger/entries?from=2026-13-01", "", 400},
		{get, "/ledger/entries?to=x", "", 400},
		{get, "/ledger/entries?cursor=bad", "", 400},
		{get, "/ledger/entries?cursor=eHh4", "", 400},
		{get, "/ledger/accounts/x/statement?from=2026-01-01&to=2026-01-31", "", 400},
		{get, fmt.Sprintf("/ledger/accounts/%d/statement?from=x&to=2026-01-31", f.cash), "", 400},
		{get, fmt.Sprintf("/ledger/accounts/%d/statement?from=2026-01-01&to=x", f.cash), "", 400},
		{get, fmt.Sprintf("/ledger/accounts/%d/statement?from=2026-01-01&to=2026-01-31&depotId=x", f.cash), "", 400},
		{get, fmt.Sprintf("/ledger/accounts/%d/statement?from=2026-01-01&to=2026-01-31&depotId=999999", f.cash), "", 404},
		{post, "/ledger/balances/grid", `{bad`, 400},
		{post, "/ledger/balances/grid", `{"asOf":"2026-01-01","sicilTipi":"X"}`, 400},
		{post, "/ledger/balances/grid", `{"asOf":"2026-01-01","depotId":999999}`, 422},
		{get, "/ledger/carry-forwards/x", "", 400},
		{get, "/ledger/carry-forwards/2027?depotId=x", "", 400},
		{get, "/ledger/opening-balances?year=1999", "", 400},
		{get, "/ledger/opening-balances?year=2027&depotId=x", "", 400},
		{get, "/receipts/x", "", 400},
		{get, "/receipts/999999", "", 404},
		{post, "/receipts/x/void", `{"reason":"abc"}`, 400},
		{post, "/receipts/grid", `{bad`, 400},
		{post, "/receipts", `{bad`, 400},
		{post, "/receipts", receiptBody("COLLECTION", f.depot, f.customer, "", "CASH", "1.00", ""), 400},
		{post, "/receipts", receiptBody("COLLECTION", f.depot, f.customer, "2026-01-01", "CASH", "1.00", `,"currency":"tl"`), 400},
		{post, "/receipts", receiptBody("COLLECTION", f.depot, f.customer, "2026-01-01", "WIRE", "1.00", ""), 400},
		{post, "/receipts", receiptBody("PAYMENT", f.depot, f.supplier, "2026-01-01", "BANK_TRANSFER", "1.00", `,"bankAccountId":999999`), 422},
		{post, "/receipts/1/void", `{"reason":"x"}`, 400},
		{post, "/receipts/1/void", `{bad`, 400},
		{get, "/cash-book?depotId=x&from=2026-01-01&to=2026-01-31", "", 400},
		{get, "/cash-book?depotId=1&from=x&to=2026-01-31", "", 400},
		{get, "/cash-book?depotId=1&from=2026-01-01&to=x", "", 400},
		{get, "/cash-book?depotId=999999&from=2026-01-01&to=2026-01-31", "", 422},
		{post, "/cash-vouchers", `{bad`, 400},
		{post, "/cash-vouchers", fmt.Sprintf(`{"kind":"GIFT","depotId":%d,"date":"2026-01-01","accountId":%d,"amount":"1.00","description":"x"}`, f.depot, f.expense), 400},
		{post, "/cash-vouchers", fmt.Sprintf(`{"kind":"EXPENSE","depotId":%d,"accountId":%d,"amount":"1.00","description":"x"}`, f.depot, f.expense), 400},
		{post, "/cash-vouchers", fmt.Sprintf(`{"kind":"EXPENSE","depotId":%d,"date":"2026-01-01","accountId":%d,"amount":"1.00","description":""}`, f.depot, f.expense), 400},
		{post, "/cash-vouchers", fmt.Sprintf(`{"kind":"EXPENSE","depotId":%d,"date":"2026-01-01","accountId":%d,"amount":"0.00","description":"x"}`, f.depot, f.expense), 400},
		{post, "/cash-vouchers", fmt.Sprintf(`{"kind":"EXPENSE","depotId":%d,"date":"2026-01-01","accountId":999999,"amount":"1.00","description":"x"}`, f.depot), 422},
		{post, "/cash-vouchers", fmt.Sprintf(`{"kind":"EXPENSE","depotId":999999,"date":"2026-01-01","accountId":%d,"amount":"1.00","description":"x"}`, f.expense), 422},
		{post, "/cash-vouchers/x/void", `{"reason":"abc"}`, 400},
		{post, "/cash-vouchers/1/void", `{"reason":"x"}`, 400},
		{post, "/cash-vouchers/1/void", `{bad`, 400},
		{post, "/cash-counts", `{bad`, 400},
		{post, "/cash-counts", fmt.Sprintf(`{"depotId":%d,"countedAmount":"-1.00"}`, f.depot), 400},
		{post, "/cash-counts", `{"depotId":999999,"countedAmount":"1.00"}`, 422},
		{get, "/cash-counts", "", 400},
		{get, "/cash-counts?depotId=x", "", 400},
		{get, fmt.Sprintf("/cash-counts?depotId=%d&cursor=x", f.depot), "", 400},
		{post, "/ledger/accounts", `{bad`, 400},
	}
	for _, tc := range cases {
		rec := f.req(t, tc.method, f.path(tc.path), f.token, tc.body, map[string]string{"Idempotency-Key": uuid.NewString(), "If-Match": `"1"`})
		assert.Equal(t, tc.status, rec.Code, "%s %s: %s", tc.method, tc.path, rec.Body.String())
	}

	adminCases := []struct {
		method, path, body string
		status             int
	}{
		{patch, "/number-series/x", `{}`, 400},
		{patch, "/number-series/999999", `{"width":6}`, 400},
		{patch, "/number-series/1", `{bad`, 400},
		{put, "/ledger/account-roles", `{bad`, 400},
		{post, "/depots", `{bad`, 400},
		{put, "/ledger/carry-forwards/x/overrides", `{}`, 400},
		{put, "/ledger/carry-forwards/2027/overrides", `{bad`, 400},
		{put, "/ledger/carry-forwards/2027/overrides", fmt.Sprintf(`{"accountId":999999,"depotId":%d,"debit":"1.00","credit":"0.00","reason":"abc"}`, f.depot), 404},
		{put, "/ledger/carry-forwards/2027/overrides", fmt.Sprintf(`{"accountId":%d,"depotId":999999,"debit":"1.00","credit":"0.00","reason":"abc"}`, f.cash), 404},
		{put, "/ledger/carry-forwards/1999/overrides", fmt.Sprintf(`{"accountId":%d,"depotId":%d,"debit":"1.00","credit":"0.00","reason":"abc"}`, f.cash, f.depot), 400},
		{del, "/ledger/carry-forwards/x/overrides?accountId=1&depotId=1", "", 400},
		{del, "/ledger/carry-forwards/2027/overrides?accountId=x&depotId=1", "", 400},
		{del, "/ledger/carry-forwards/2027/overrides?accountId=1&depotId=x", "", 400},
		{del, "/ledger/carry-forwards/1999/overrides?accountId=1&depotId=1", "", 400},
		{post, "/ledger/carry-forwards/x/post", "", 400},
		{post, "/ledger/carry-forwards/1999/post", "", 400},
	}
	for _, tc := range adminCases {
		h := map[string]string{"If-Match": `"99"`, "Idempotency-Key": uuid.NewString()}
		rec := f.req(t, tc.method, f.path(tc.path), f.admin, tc.body, h)
		assert.Equal(t, tc.status, rec.Code, "%s %s: %s", tc.method, tc.path, rec.Body.String())
	}
}

func TestCashCounts_KeysetPaging(t *testing.T) {
	f := newCash(t)
	for i := 0; i < 3; i++ {
		rec := f.post(t, f.path("/cash-counts"), f.token, fmt.Sprintf(`{"depotId":%d,"countedAmount":"%d.00"}`, f.depot, i))
		require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	}
	type page struct {
		Items      []struct{ ID uint } `json:"items"`
		NextCursor *string             `json:"nextCursor"`
	}
	p := decode[page](t, f.do(t, http.MethodGet, f.path("/cash-counts?limit=2&depotId="+idStr(f.depot)), f.token, ""))
	require.Len(t, p.Items, 2)
	require.NotNil(t, p.NextCursor)
	p2 := decode[page](t, f.do(t, http.MethodGet, f.path("/cash-counts?limit=2&depotId="+idStr(f.depot)+"&cursor="+*p.NextCursor), f.token, ""))
	require.Len(t, p2.Items, 1)
	assert.Less(t, p2.Items[0].ID, p.Items[1].ID)
	assert.Nil(t, p2.NextCursor)
}

func TestSicilGrid_XLSX(t *testing.T) {
	f := newCash(t)
	rec := f.req(t, http.MethodPost, f.path("/sicils/grid"), f.token, `{"startRow":0,"endRow":1}`, map[string]string{"Accept": xlsxType})
	require.Equal(t, http.StatusOK, rec.Code)
	sheet := sheetXML(t, rec.Body.Bytes())
	assert.Contains(t, sheet, "Card M1")
	assert.Contains(t, sheet, "Card F1", "exports ignore paging")
}

func TestOutboxRelay_RunDeliversUntilCancelled(t *testing.T) {
	f := newLedger(t)
	cash := f.account(t, "100", "ASSET")
	sales := f.account(t, "600", "INCOME")
	f.entry(t, "2026-03-05", ln{cash, "DEBIT", "1.00"}, ln{sales, "CREDIT", "1.00"})

	ctx, cancel := context.WithCancel(context.Background())
	done := make(chan struct{})
	go func() {
		f.app.Relay.Run(ctx, 20*time.Millisecond)
		close(done)
	}()
	require.Eventually(t, func() bool {
		var n int64
		_ = testDB.Raw(`SELECT count(*) FROM outbox WHERE org_id = ? AND published_at IS NULL`, f.org).Scan(&n).Error
		return n == 0
	}, 5*time.Second, 20*time.Millisecond)
	cancel()
	<-done
}

func TestMembership_AdminOnUnknownOrgAndMissingClaims(t *testing.T) {
	app, admin := adminApp(t)
	rec := app.do(t, http.MethodGet, "/api/orgs/0/depots", admin, "")
	assert.Equal(t, http.StatusBadRequest, rec.Code)
	rec = app.do(t, http.MethodGet, "/api/orgs/424242/ledger/accounts", admin, "")
	assert.Equal(t, http.StatusNotFound, rec.Code)
}
