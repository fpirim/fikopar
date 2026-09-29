//go:build integration

package router_test

import (
	"fmt"
	"net/http"
	"sort"
	"sync"
	"testing"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type receiptJSON struct {
	ID              uint    `json:"id"`
	OrgID           uint    `json:"orgId"`
	ReceiptNo       string  `json:"receiptNo"`
	Kind            string  `json:"kind"`
	Origin          string  `json:"origin"`
	DepotID         uint    `json:"depotId"`
	SicilID         uint    `json:"sicilId"`
	SicilName       string  `json:"sicilName"`
	Date            string  `json:"date"`
	Method          string  `json:"method"`
	Amount          string  `json:"amount"`
	Currency        string  `json:"currency"`
	BankAccountID   *uint   `json:"bankAccountId"`
	InstrumentID    *uint   `json:"instrumentId"`
	JournalEntryID  uint    `json:"journalEntryId"`
	VoidedByEntryID *uint   `json:"voidedByEntryId"`
	Description     string  `json:"description"`
	InstrumentNo    *string `json:"instrumentNo"`
}

type cashFix struct {
	*ledgerFix
	cash, bank, expense, overShort uint
	customer, supplier             uint
}

func newCash(t *testing.T) *cashFix {
	t.Helper()
	f := &cashFix{ledgerFix: newLedger(t)}
	f.cash = f.account(t, "100.01", "ASSET")
	f.bank = f.account(t, "102.01", "ASSET")
	f.expense = f.account(t, "770.01", "EXPENSE")
	f.overShort = f.account(t, "689", "EXPENSE")
	f.setRoles(t, fmt.Sprintf(`[{"role":"CASH","accountId":%d}]`, f.cash))
	f.customer = f.sicil(t, "M1", "MUSTERI")
	f.supplier = f.sicil(t, "F1", "FIRMA")
	return f
}

func receiptBody(kind string, depot, sicil uint, date, method, amount, extra string) string {
	return fmt.Sprintf(`{"kind":%q,"depotId":%d,"sicilId":%d,"date":%q,"method":%q,"amount":%q%s}`, kind, depot, sicil, date, method, amount, extra)
}

func (f *cashFix) receipt(t *testing.T, body string) receiptJSON {
	t.Helper()
	rec := f.post(t, f.path("/receipts"), f.token, body)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	return decode[receiptJSON](t, rec)
}

func (f *cashFix) balance(t *testing.T, account uint) string {
	t.Helper()
	var s string
	require.NoError(t, testDB.Raw(`SELECT coalesce(sum(debit_total - credit_total), 0)::numeric(18,2)::text FROM account_balances WHERE account_id = ?`, account).Scan(&s).Error)
	return s
}

func TestReceipts_CashCollectionPostsToCashAndCustomer(t *testing.T) {
	f := newCash(t)
	recorder := &recorder{}
	f.app.Relay.Register(recorder)

	r := f.receipt(t, receiptBody("COLLECTION", f.depot, f.customer, "2026-03-10", "CASH", "250.00", `,"description":"March"`))
	assert.Equal(t, "TAH-2026-1-000001", r.ReceiptNo)
	assert.Equal(t, "MANUAL", r.Origin)
	assert.Equal(t, "Card M1", r.SicilName)
	assert.Equal(t, "TRY", r.Currency)
	assert.Equal(t, "250.00", r.Amount)

	e := f.getEntry(t, r.JournalEntryID)
	assert.Equal(t, "receipt", e.SourceType)
	require.NotNil(t, e.SourceNo)
	assert.Equal(t, r.ReceiptNo, *e.SourceNo)
	require.Len(t, e.Postings, 2)
	assert.Equal(t, "DEBIT", e.Postings[0].Side)
	assert.Equal(t, f.cash, e.Postings[0].AccountID)
	assert.Equal(t, "120.M1", e.Postings[1].AccountCode)

	type page struct {
		Items []accountJSON `json:"items"`
	}
	accs := decode[page](t, f.do(t, http.MethodGet, f.path("/ledger/accounts?sicilId="+idStr(f.customer)), f.token, "")).Items
	require.Len(t, accs, 1, "the card's current account is opened on first use")
	assert.Equal(t, "ASSET", accs[0].Type)
	assert.Equal(t, "250.00", f.balance(t, f.cash))
	assert.Equal(t, "-250.00", f.balance(t, accs[0].ID))

	got := decode[receiptJSON](t, f.do(t, http.MethodGet, f.path("/receipts/"+idStr(r.ID)), f.token, ""))
	assert.Equal(t, r, got)

	f.relay(t)
	posted := recorder.byType(t, "ReceiptPosted")
	require.Len(t, posted, 1)
	validateEvent(t, posted[0])
	for _, e := range recorder.byType(t, "LedgerEntryPosted") {
		validateEvent(t, e)
	}
}

func TestReceipts_PaymentByBankTransfer(t *testing.T) {
	f := newCash(t)
	r := f.receipt(t, receiptBody("PAYMENT", f.depot, f.supplier, "2026-03-11", "BANK_TRANSFER", "90.00", fmt.Sprintf(`,"bankAccountId":%d`, f.bank)))
	assert.Equal(t, "TED-2026-1-000001", r.ReceiptNo)
	require.NotNil(t, r.BankAccountID)
	e := f.getEntry(t, r.JournalEntryID)
	assert.Equal(t, "320.F1", e.Postings[0].AccountCode)
	assert.Equal(t, "DEBIT", e.Postings[0].Side)
	assert.Equal(t, f.bank, e.Postings[1].AccountID)
	assert.Equal(t, "CREDIT", e.Postings[1].Side)
	assert.Equal(t, "-90.00", f.balance(t, f.bank))
	assert.Equal(t, "0.00", f.balance(t, f.cash))
}

func TestReceipts_Validation(t *testing.T) {
	f := newCash(t)
	other := f.seedOrg(t, "rcpt-other", f.userID)
	rec := f.do(t, http.MethodPost, orgPath(other, "/sicils"), f.token, `{"hesap_no":"X","hesap_adi":"Foreign"}`)
	require.Equal(t, http.StatusCreated, rec.Code)
	foreign := extractID(t, rec.Body.Bytes())

	cases := []struct {
		name   string
		body   string
		status int
		code   string
	}{
		{"no bank account", receiptBody("PAYMENT", f.depot, f.supplier, "2026-03-11", "BANK_TRANSFER", "1.00", ""), 422, "ERR_BANK_ACCOUNT_REQUIRED"},
		{"not a bank account", receiptBody("PAYMENT", f.depot, f.supplier, "2026-03-11", "BANK_TRANSFER", "1.00", fmt.Sprintf(`,"bankAccountId":%d`, f.expense)), 422, "ERR_BANK_ACCOUNT_REQUIRED"},
		{"zero", receiptBody("COLLECTION", f.depot, f.customer, "2026-03-11", "CASH", "0.00", ""), 400, "ERR_INVALID_AMOUNT"},
		{"foreign sicil", receiptBody("COLLECTION", f.depot, foreign, "2026-03-11", "CASH", "1.00", ""), 422, "ERR_SICIL_NOT_FOUND"},
		{"unknown depot", receiptBody("COLLECTION", 999999, f.customer, "2026-03-11", "CASH", "1.00", ""), 422, "ERR_DEPOT_NOT_FOUND"},
		{"currency", receiptBody("COLLECTION", f.depot, f.customer, "2026-03-11", "CASH", "1.00", `,"currency":"EUR"`), 400, "ERR_INVALID_REQUEST"},
		{"kind", receiptBody("REFUND", f.depot, f.customer, "2026-03-11", "CASH", "1.00", ""), 400, "ERR_INVALID_REQUEST"},
		{"cheque until F4", receiptBody("COLLECTION", f.depot, f.customer, "2026-03-11", "CHEQUE", "1.00", ""), 400, "ERR_METHOD_NOT_AVAILABLE"},
		{"note until F4", receiptBody("PAYMENT", f.depot, f.supplier, "2026-03-11", "NOTE", "1.00", ""), 400, "ERR_METHOD_NOT_AVAILABLE"},
		{"endorsement", receiptBody("PAYMENT", f.depot, f.supplier, "2026-03-11", "ENDORSEMENT", "1.00", ""), 400, "ERR_INVALID_REQUEST"},
	}
	for _, tc := range cases {
		rec := f.post(t, f.path("/receipts"), f.token, tc.body)
		assert.Equal(t, tc.status, rec.Code, "%s: %s", tc.name, rec.Body.String())
		assert.Equal(t, tc.code, errCode(t, rec), tc.name)
	}

	f.setRoles(t, `[]`)
	rec = f.post(t, f.path("/receipts"), f.token, receiptBody("COLLECTION", f.depot, f.customer, "2026-03-11", "CASH", "1.00", ""))
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_ACCOUNT_ROLE_MISSING", errCode(t, rec))
	assert.Zero(t, f.countEntries(t))
}

func TestReceipts_VoidReversesOnceAndOwnsItsEntry(t *testing.T) {
	f := newCash(t)
	r := f.receipt(t, receiptBody("COLLECTION", f.depot, f.customer, "2026-03-10", "CASH", "40.00", ""))

	rec := f.post(t, f.path("/ledger/entries/"+idStr(r.JournalEntryID)+"/reversal"), f.token, `{"reason":"direct"}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_ENTRY_OWNED_BY_SOURCE", errCode(t, rec))
	assert.Contains(t, rec.Body.String(), r.ReceiptNo)

	rec = f.post(t, f.path("/receipts/"+idStr(r.ID)+"/void"), f.token, `{"reason":"duplicate"}`)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	voided := decode[receiptJSON](t, rec)
	require.NotNil(t, voided.VoidedByEntryID)
	rev := f.getEntry(t, *voided.VoidedByEntryID)
	assert.Equal(t, "reversal", rev.SourceType)
	require.NotNil(t, rev.SourceNo)
	assert.Equal(t, r.ReceiptNo, *rev.SourceNo)
	assert.Equal(t, r.JournalEntryID, *rev.ReversesEntryID)
	assert.Equal(t, "0.00", f.balance(t, f.cash))

	rec = f.post(t, f.path("/receipts/"+idStr(r.ID)+"/void"), f.token, `{"reason":"again"}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_RECEIPT_ALREADY_VOIDED", errCode(t, rec))

	rec = f.post(t, f.path("/receipts/"+idStr(r.ID+99)+"/void"), f.token, `{"reason":"missing"}`)
	assert.Equal(t, http.StatusNotFound, rec.Code)
	assert.Equal(t, "ERR_RECEIPT_NOT_FOUND", errCode(t, rec))

	err := testDB.Exec(`UPDATE receipts SET amount = 1 WHERE id = ?`, r.ID).Error
	require.Error(t, err, "receipts are immutable in the database")
}

func TestReceipts_IdempotentRetryIssuesOneNumber(t *testing.T) {
	f := newCash(t)
	key := map[string]string{"Idempotency-Key": uuid.NewString()}
	body := receiptBody("COLLECTION", f.depot, f.customer, "2026-03-10", "CASH", "5.00", "")
	a := f.req(t, http.MethodPost, f.path("/receipts"), f.token, body, key)
	b := f.req(t, http.MethodPost, f.path("/receipts"), f.token, body, key)
	require.Equal(t, http.StatusCreated, a.Code)
	require.Equal(t, http.StatusCreated, b.Code)
	assert.Equal(t, decode[receiptJSON](t, a).ReceiptNo, decode[receiptJSON](t, b).ReceiptNo)
	assert.Equal(t, "TAH-2026-1-000002", f.receipt(t, body).ReceiptNo)
}

func TestReceipts_ConcurrentReceiptsGetUniqueGapFreeNumbers(t *testing.T) {
	f := newCash(t)
	const n = 12
	nos := make([]string, n)
	var wg sync.WaitGroup
	for i := 0; i < n; i++ {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			rec := f.post(t, f.path("/receipts"), f.token, receiptBody("COLLECTION", f.depot, f.customer, "2026-03-10", "CASH", "1.00", ""))
			if assert.Equal(t, http.StatusCreated, rec.Code, rec.Body.String()) {
				nos[i] = decode[receiptJSON](t, rec).ReceiptNo
			}
		}(i)
	}
	wg.Wait()
	sort.Strings(nos)
	for i, no := range nos {
		assert.Equal(t, fmt.Sprintf("TAH-2026-1-%06d", i+1), no)
	}
}

func TestReceipts_GridIsScopedAndFiltered(t *testing.T) {
	f := newCash(t)
	f.receipt(t, receiptBody("COLLECTION", f.depot, f.customer, "2026-03-10", "CASH", "5.00", ""))
	f.receipt(t, receiptBody("PAYMENT", f.depot, f.supplier, "2026-03-11", "CASH", "6.00", ""))
	type grid struct {
		Rows       []receiptJSON `json:"rows"`
		TotalCount int64         `json:"totalCount"`
	}
	g := decode[grid](t, f.do(t, http.MethodPost, f.path("/receipts/grid"), f.token, `{"startRow":0,"endRow":50,"filterModel":{"kind":{"filterType":"text","type":"equals","filter":"PAYMENT"}}}`))
	assert.Equal(t, int64(1), g.TotalCount)
	require.Len(t, g.Rows, 1)
	assert.Equal(t, "TED-2026-1-000001", g.Rows[0].ReceiptNo)
	assert.Equal(t, "Card F1", g.Rows[0].SicilName)

	other := f.seedOrg(t, "grid-other", f.userID)
	g = decode[grid](t, f.do(t, http.MethodPost, orgPath(other, "/receipts/grid"), f.token, `{"startRow":0,"endRow":50}`))
	assert.Zero(t, g.TotalCount)
}

type voucherJSON struct {
	ID              uint   `json:"id"`
	VoucherNo       string `json:"voucherNo"`
	Kind            string `json:"kind"`
	AccountID       uint   `json:"accountId"`
	Amount          string `json:"amount"`
	JournalEntryID  uint   `json:"journalEntryId"`
	VoidedByEntryID *uint  `json:"voidedByEntryId"`
}

func (f *cashFix) voucher(t *testing.T, kind string, account uint, date, amount string) voucherJSON {
	t.Helper()
	rec := f.post(t, f.path("/cash-vouchers"), f.token, fmt.Sprintf(`{"kind":%q,"depotId":%d,"date":%q,"accountId":%d,"amount":%q,"description":"%s"}`, kind, f.depot, date, account, amount, kind))
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	return decode[voucherJSON](t, rec)
}

func TestCashVouchers_ExpenseAndBankMoves(t *testing.T) {
	f := newCash(t)
	f.receipt(t, receiptBody("COLLECTION", f.depot, f.customer, "2026-03-01", "CASH", "500.00", ""))

	exp := f.voucher(t, "EXPENSE", f.expense, "2026-03-02", "20.00")
	dep := f.voucher(t, "BANK_DEPOSIT", f.bank, "2026-03-03", "300.00")
	wd := f.voucher(t, "BANK_WITHDRAWAL", f.bank, "2026-03-04", "50.00")
	assert.Equal(t, "GDR-2026-1-000001", exp.VoucherNo)
	assert.Equal(t, "VRM-2026-1-000001", dep.VoucherNo)
	assert.Equal(t, "VRM-2026-1-000002", wd.VoucherNo)
	assert.Equal(t, "230.00", f.balance(t, f.cash))
	assert.Equal(t, "250.00", f.balance(t, f.bank))
	assert.Equal(t, "20.00", f.balance(t, f.expense))
	e := f.getEntry(t, exp.JournalEntryID)
	assert.Equal(t, "cash_voucher", e.SourceType)
	assert.Equal(t, exp.VoucherNo, *e.SourceNo)

	bad := f.post(t, f.path("/cash-vouchers"), f.token, fmt.Sprintf(`{"kind":"BANK_DEPOSIT","depotId":%d,"date":"2026-03-05","accountId":%d,"amount":"1.00","description":"x"}`, f.depot, f.expense))
	assert.Equal(t, http.StatusUnprocessableEntity, bad.Code)
	assert.Equal(t, "ERR_BANK_ACCOUNT_REQUIRED", errCode(t, bad))
	bad = f.post(t, f.path("/cash-vouchers"), f.token, fmt.Sprintf(`{"kind":"EXPENSE","depotId":%d,"date":"2026-03-05","accountId":%d,"amount":"1.00","description":"x"}`, f.depot, f.bank))
	assert.Equal(t, http.StatusUnprocessableEntity, bad.Code)

	rec := f.post(t, f.path("/cash-vouchers/"+idStr(exp.ID)+"/void"), f.token, `{"reason":"typo"}`)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.NotNil(t, decode[voucherJSON](t, rec).VoidedByEntryID)
	assert.Equal(t, "250.00", f.balance(t, f.cash))
	rec = f.post(t, f.path("/cash-vouchers/"+idStr(exp.ID)+"/void"), f.token, `{"reason":"typo"}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_CASH_VOUCHER_ALREADY_VOIDED", errCode(t, rec))
	rec = f.post(t, f.path("/cash-vouchers/999999/void"), f.token, `{"reason":"typo"}`)
	assert.Equal(t, http.StatusNotFound, rec.Code)
	assert.Equal(t, "ERR_CASH_VOUCHER_NOT_FOUND", errCode(t, rec))
}

type cashBookJSON struct {
	DepotID        uint   `json:"depotId"`
	From           string `json:"from"`
	To             string `json:"to"`
	OpeningBalance string `json:"openingBalance"`
	TotalIn        string `json:"totalIn"`
	TotalOut       string `json:"totalOut"`
	ClosingBalance string `json:"closingBalance"`
	Lines          []struct {
		Date           string  `json:"date"`
		Kind           string  `json:"kind"`
		DocumentNo     *string `json:"documentNo"`
		ReceiptID      *uint   `json:"receiptId"`
		VoucherID      *uint   `json:"voucherId"`
		SicilName      *string `json:"sicilName"`
		Income         string  `json:"income"`
		Expense        string  `json:"expense"`
		RunningBalance string  `json:"runningBalance"`
	} `json:"lines"`
}

func TestCashBook_OpeningInOutClosingPerDepotAndPeriod(t *testing.T) {
	f := newCash(t)
	depot2 := f.newDepot(t, "2")
	cash2 := f.account(t, "100.02", "ASSET")
	f.setRoles(t, fmt.Sprintf(`[{"role":"CASH","accountId":%d},{"role":"CASH","depotId":%d,"accountId":%d}]`, f.cash, depot2, cash2))

	f.receipt(t, receiptBody("COLLECTION", f.depot, f.customer, "2026-02-28", "CASH", "1000.00", ""))
	in := f.receipt(t, receiptBody("COLLECTION", f.depot, f.customer, "2026-03-01", "CASH", "200.00", ""))
	f.voucher(t, "EXPENSE", f.expense, "2026-03-15", "50.00")
	f.voucher(t, "BANK_DEPOSIT", f.bank, "2026-03-31", "100.00")
	f.receipt(t, receiptBody("COLLECTION", f.depot, f.customer, "2026-04-01", "CASH", "999.00", ""))
	f.receipt(t, receiptBody("COLLECTION", depot2, f.customer, "2026-03-10", "CASH", "77.00", ""))

	rec := f.do(t, http.MethodGet, f.path("/cash-book?depotId="+idStr(f.depot)+"&from=2026-03-01&to=2026-03-31"), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	b := decode[cashBookJSON](t, rec)
	assert.Equal(t, "1000.00", b.OpeningBalance)
	assert.Equal(t, "200.00", b.TotalIn)
	assert.Equal(t, "150.00", b.TotalOut)
	assert.Equal(t, "1050.00", b.ClosingBalance)
	require.Len(t, b.Lines, 3)
	assert.Equal(t, []string{"COLLECTION", "EXPENSE", "BANK_DEPOSIT"}, []string{b.Lines[0].Kind, b.Lines[1].Kind, b.Lines[2].Kind})
	assert.Equal(t, []string{"1200.00", "1150.00", "1050.00"}, []string{b.Lines[0].RunningBalance, b.Lines[1].RunningBalance, b.Lines[2].RunningBalance})
	assert.Equal(t, in.ReceiptNo, *b.Lines[0].DocumentNo)
	assert.Equal(t, in.ID, *b.Lines[0].ReceiptID)
	assert.Equal(t, "Card M1", *b.Lines[0].SicilName)
	assert.NotNil(t, b.Lines[1].VoucherID)
	assert.Equal(t, "0.00", b.Lines[1].Income)
	assert.Equal(t, "50.00", b.Lines[1].Expense)

	b2 := decode[cashBookJSON](t, f.do(t, http.MethodGet, f.path("/cash-book?depotId="+idStr(depot2)+"&from=2026-01-01&to=2026-12-31"), f.token, ""))
	assert.Equal(t, "0.00", b2.OpeningBalance)
	assert.Equal(t, "77.00", b2.ClosingBalance)
	require.Len(t, b2.Lines, 1)

	for _, q := range []string{"?from=2026-03-01&to=2026-03-31", "?depotId=" + idStr(f.depot) + "&to=2026-03-31", "?depotId=" + idStr(f.depot) + "&from=2026-04-01&to=2026-03-01"} {
		assert.Equal(t, http.StatusBadRequest, f.do(t, http.MethodGet, f.path("/cash-book"+q), f.token, "").Code, q)
	}
}

func TestCashCounts_RecordAndPostDifference(t *testing.T) {
	f := newCash(t)
	f.receipt(t, receiptBody("COLLECTION", f.depot, f.customer, "2026-03-01", "CASH", "100.00", ""))

	rec := f.post(t, f.path("/cash-counts"), f.token, fmt.Sprintf(`{"depotId":%d,"countedAmount":"90.00","postDifference":true}`, f.depot))
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_ACCOUNT_ROLE_MISSING", errCode(t, rec))

	f.setRoles(t, fmt.Sprintf(`[{"role":"CASH","accountId":%d},{"role":"CASH_OVER_SHORT","accountId":%d}]`, f.cash, f.overShort))
	type countJSON struct {
		ID             uint   `json:"id"`
		BookBalance    string `json:"bookBalance"`
		CountedAmount  string `json:"countedAmount"`
		Difference     string `json:"difference"`
		JournalEntryID *uint  `json:"journalEntryId"`
		CountedBy      *uint  `json:"countedBy"`
	}
	rec = f.post(t, f.path("/cash-counts"), f.token, fmt.Sprintf(`{"depotId":%d,"countedAmount":"90.00","postDifference":true,"note":"short"}`, f.depot))
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	c := decode[countJSON](t, rec)
	assert.Equal(t, "100.00", c.BookBalance)
	assert.Equal(t, "-10.00", c.Difference)
	require.NotNil(t, c.JournalEntryID)
	assert.Equal(t, f.userID, *c.CountedBy)
	assert.Equal(t, "90.00", f.balance(t, f.cash))
	assert.Equal(t, "10.00", f.balance(t, f.overShort))

	rec = f.post(t, f.path("/cash-counts"), f.token, fmt.Sprintf(`{"depotId":%d,"countedAmount":"90.00"}`, f.depot))
	require.Equal(t, http.StatusCreated, rec.Code)
	assert.Nil(t, decode[countJSON](t, rec).JournalEntryID)

	type list struct {
		Items []countJSON `json:"items"`
	}
	l := decode[list](t, f.do(t, http.MethodGet, f.path("/cash-counts?depotId="+idStr(f.depot)), f.token, ""))
	require.Len(t, l.Items, 2)
	assert.Greater(t, l.Items[0].ID, l.Items[1].ID)
	assert.Equal(t, "0.00", l.Items[0].Difference)
}
