//go:build integration

package router_test

import (
	"fmt"
	"net/http"
	"net/url"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type apiErr struct {
	Code  string `json:"code"`
	Field string `json:"field"`
	Owner *struct {
		SourceType string  `json:"sourceType"`
		SourceID   string  `json:"sourceId"`
		SourceNo   *string `json:"sourceNo"`
	} `json:"owner"`
}

func TestV11_NewErrorCodes(t *testing.T) {
	f := newCash(t)

	rec := f.post(t, f.path("/depots"), f.admin, `{"code":"1","name":"Again"}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_DEPOT_CODE_EXISTS", errCode(t, rec))

	rec = f.req(t, http.MethodPatch, f.path("/number-series/999999"), f.admin, `{"width":6}`, map[string]string{"If-Match": `"1"`})
	assert.Equal(t, http.StatusNotFound, rec.Code)
	assert.Equal(t, "ERR_NUMBER_SERIES_NOT_FOUND", errCode(t, rec))

	for _, m := range []string{"CHEQUE", "NOTE"} {
		rec = f.post(t, f.path("/receipts"), f.token, receiptBody("COLLECTION", f.depot, f.customer, "2026-03-11", m, "1.00", ""))
		assert.Equal(t, http.StatusBadRequest, rec.Code, m)
		e := decode[apiErr](t, rec)
		assert.Equal(t, "ERR_METHOD_NOT_AVAILABLE", e.Code, m)
		assert.Equal(t, "method", e.Field, m)
	}
}

func TestV11_UnknownIDsInBodyAre422WithField(t *testing.T) {
	f := newCash(t)
	cases := []struct {
		path, body, code, field string
	}{
		{"/ledger/accounts", `{"code":"120.Z","name":"z","type":"ASSET","sicilId":999999}`, "ERR_SICIL_NOT_FOUND", "sicilId"},
		{"/receipts", receiptBody("COLLECTION", f.depot, 999999, "2026-03-11", "CASH", "1.00", ""), "ERR_SICIL_NOT_FOUND", "sicilId"},
		{"/receipts", receiptBody("COLLECTION", 999999, f.customer, "2026-03-11", "CASH", "1.00", ""), "ERR_DEPOT_NOT_FOUND", "depotId"},
		{"/receipts", receiptBody("PAYMENT", f.depot, f.supplier, "2026-03-11", "BANK_TRANSFER", "1.00", `,"bankAccountId":999999`), "ERR_ACCOUNT_NOT_FOUND", "bankAccountId"},
		{"/ledger/entries", entryBody(f.depot, "2026-03-11", ln{999999, "DEBIT", "1.00"}, ln{f.cash, "CREDIT", "1.00"}), "ERR_ACCOUNT_NOT_FOUND", "accountId"},
		{"/cash-vouchers", fmt.Sprintf(`{"kind":"EXPENSE","depotId":%d,"date":"2026-03-11","accountId":999999,"amount":"1.00","description":"x"}`, f.depot), "ERR_ACCOUNT_NOT_FOUND", "accountId"},
	}
	for _, tc := range cases {
		rec := f.post(t, f.path(tc.path), f.token, tc.body)
		assert.Equal(t, http.StatusUnprocessableEntity, rec.Code, "%s: %s", tc.path, rec.Body.String())
		e := decode[apiErr](t, rec)
		assert.Equal(t, tc.code, e.Code, tc.path)
		assert.Equal(t, tc.field, e.Field, tc.path)
	}

	rec := f.do(t, http.MethodGet, f.path("/ledger/accounts/999999"), f.token, "")
	assert.Equal(t, http.StatusNotFound, rec.Code)
	assert.Equal(t, "ERR_ACCOUNT_NOT_FOUND", errCode(t, rec))
}

func TestV11_OwnedEntryErrorNamesItsOwner(t *testing.T) {
	f := newCash(t)
	r := f.receipt(t, receiptBody("COLLECTION", f.depot, f.customer, "2026-03-10", "CASH", "40.00", ""))
	rec := f.post(t, f.path("/ledger/entries/"+idStr(r.JournalEntryID)+"/reversal"), f.token, `{"reason":"direct"}`)
	require.Equal(t, http.StatusConflict, rec.Code)
	e := decode[apiErr](t, rec)
	assert.Equal(t, "ERR_ENTRY_OWNED_BY_SOURCE", e.Code)
	require.NotNil(t, e.Owner, rec.Body.String())
	assert.Equal(t, "receipt", e.Owner.SourceType)
	assert.Equal(t, idStr(r.ID), e.Owner.SourceID)
	require.NotNil(t, e.Owner.SourceNo)
	assert.Equal(t, r.ReceiptNo, *e.Owner.SourceNo)

	v := f.voucher(t, "EXPENSE", f.expense, "2026-03-11", "5.00")
	e = decode[apiErr](t, f.post(t, f.path("/ledger/entries/"+idStr(v.JournalEntryID)+"/reversal"), f.token, `{"reason":"direct"}`))
	require.NotNil(t, e.Owner)
	assert.Equal(t, "cash_voucher", e.Owner.SourceType)
	assert.Equal(t, idStr(v.ID), e.Owner.SourceID)
	assert.Equal(t, v.VoucherNo, *e.Owner.SourceNo)
}

func TestV11_CashBookNeedsOneDepotAndDatesIgnoreTime(t *testing.T) {
	f := newCash(t)
	f.receipt(t, receiptBody("COLLECTION", f.depot, f.customer, "2026-03-31", "CASH", "10.00", ""))

	rec := f.do(t, http.MethodGet, f.path("/cash-book?from=2026-03-01&to=2026-03-31"), f.token, "")
	assert.Equal(t, http.StatusBadRequest, rec.Code)
	assert.Equal(t, "ERR_DEPOT_REQUIRED", errCode(t, rec))

	q := url.Values{"depotId": {idStr(f.depot)}, "from": {"2026-03-01 10:30:00"}, "to": {"2026-03-31T00:00:00"}}
	rec = f.do(t, http.MethodGet, f.path("/cash-book?"+q.Encode()), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	b := decode[cashBookJSON](t, rec)
	assert.Equal(t, "2026-03-01", b.From)
	assert.Equal(t, "10.00", b.ClosingBalance, "the whole last day is included")

	rec = f.do(t, http.MethodGet, f.path("/ledger/entries?from=2026-03-31%2023:59:59&to=2026-03-31%2000:00:00"), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Len(t, decode[struct {
		Items []entryJSON `json:"items"`
	}](t, rec).Items, 1)
}
