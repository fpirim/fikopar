//go:build integration

package router_test

import (
	"fmt"
	"net/http"
	"strconv"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type v15Doc struct {
	docJSON
	HesapNo  string `json:"hesapNo"`
	Warnings []struct {
		Code      string  `json:"code"`
		LineNo    *int    `json:"lineNo"`
		PartID    *uint   `json:"partId"`
		StockNo   *string `json:"stockNo"`
		Available *string `json:"available"`
	} `json:"warnings"`
}

func (d v15Doc) warningsOf(code string) []int {
	var out []int
	for i, w := range d.Warnings {
		if w.Code == code {
			out = append(out, i)
		}
	}
	return out
}

func (f *docFix) getV15(t *testing.T, id uint) v15Doc {
	t.Helper()
	rec := f.do(t, http.MethodGet, f.path("/documents/"+idStr(id)), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[v15Doc](t, rec)
}

func TestV15_PostAndCancelWriteEffectsInTheirTransaction(t *testing.T) {
	f := newDocs(t)
	d := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "12", "10", "")))
	rec := f.postRaw(t, f.token, d.ID, d.Version, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	p := decode[v15Doc](t, rec)
	require.NotNil(t, p.JournalEntryID)
	assert.Equal(t, "-2", f.onHand(t, f.p1, f.depot))
	neg := p.warningsOf("NEGATIVE_STOCK")
	require.Len(t, neg, 1)
	require.NotNil(t, p.Warnings[neg[0]].StockNo)
	assert.Equal(t, "P1", *p.Warnings[neg[0]].StockNo)

	var deliveries int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM outbox_deliveries WHERE consumer IN ('document-ledger', 'document-stock')`).
		Scan(&deliveries).Error)
	assert.Zero(t, deliveries, "the entry and the movements are no relay deliveries")
	f.relay(t)
	var exported int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM outbox_deliveries WHERE consumer = 'document-export'`).Scan(&exported).Error)
	assert.EqualValues(t, 1, exported, "the exporter still gets DocumentPosted")

	c := f.cancel(t, p.docJSON)
	require.NotNil(t, c.ReversalEntryID)
	assert.Equal(t, "10", f.onHand(t, f.p1, f.depot))
	redeliver(t, f.testApp, d.ID)
	var entries, moves int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM journal_entries WHERE id = ? OR reverses_entry_id = ?`,
		*p.JournalEntryID, *p.JournalEntryID).Scan(&entries).Error)
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM stock_movements WHERE source_type = 'document' AND source_id = ?`, idStr(p.Lines[0].ID)).Scan(&moves).Error)
	assert.EqualValues(t, 2, entries, "entry and reversal, once")
	assert.EqualValues(t, 2, moves, "OUT and IN, once")
}

func TestV15_CreditBalanceIsTheCurrentAccount(t *testing.T) {
	f := newDocs(t)
	f.posted(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "100", `,"discount1Pct":"0","discount2Pct":"0"`)))
	f.setCreditLimit(t, f.cust, "100")
	d := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "1", `,"discount1Pct":"0","discount2Pct":"0","vatPct":"0"`)))
	rec := f.postRaw(t, f.token, d.ID, d.Version, "")
	require.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "120.00", decode[struct {
		Details map[string]string `json:"details"`
	}](t, rec).Details["balance"])
}

func TestV15_SalesDiscountsRole(t *testing.T) {
	f := newLedger(t)
	a := f.account(t, "611", "EXPENSE")
	f.setRoles(t, fmt.Sprintf(`[{"role":"SALES_DISCOUNTS","accountId":%d}]`, a))
	roles := decode[[]struct {
		Role      string `json:"role"`
		AccountID uint   `json:"accountId"`
	}](t, f.do(t, http.MethodGet, f.path("/ledger/account-roles"), f.token, ""))
	require.Len(t, roles, 1)
	assert.Equal(t, "SALES_DISCOUNTS", roles[0].Role)
	assert.Equal(t, a, roles[0].AccountID)
}

func TestV15_DocumentHesapNo(t *testing.T) {
	f := newDocs(t)
	d := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "10", "")))
	assert.Equal(t, "C1", f.getV15(t, d.ID).HesapNo)
	rec := f.postRaw(t, f.token, d.ID, d.Version, "")
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Equal(t, "C1", decode[v15Doc](t, rec).HesapNo)
}

type sicilRep struct {
	ID             uint    `json:"id"`
	Version        int     `json:"version"`
	SalesRepUserID *uint   `json:"sales_rep_user_id"`
	SalesRepName   *string `json:"sales_rep_name"`
}

func TestV15_SicilSalesRep(t *testing.T) {
	f := newDocs(t)
	rec := f.do(t, http.MethodPost, f.path("/sicils"), f.token,
		fmt.Sprintf(`{"hesap_no":"C2","hesap_adi":"Card C2","sicil_tipi":"MUSTERI","sales_rep_user_id":%d}`, f.clerkID))
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	s := decode[sicilRep](t, rec)
	require.NotNil(t, s.SalesRepUserID)
	assert.Equal(t, f.clerkID, *s.SalesRepUserID)
	require.NotNil(t, s.SalesRepName)

	rec = f.do(t, http.MethodPost, f.path("/sicils"), f.token,
		fmt.Sprintf(`{"hesap_no":"C3","hesap_adi":"Card C3","sales_rep_user_id":%d}`, f.adminID))
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code, rec.Body.String())
	assert.Equal(t, "ERR_USER_NOT_FOUND", errCode(t, rec))
	assert.Equal(t, "sales_rep_user_id", decode[struct {
		Field string `json:"field"`
	}](t, rec).Field)

	got := decode[sicilRep](t, f.do(t, http.MethodGet, f.path("/sicils/"+idStr(s.ID)), f.token, ""))
	assert.Equal(t, s.SalesRepName, got.SalesRepName)

	d := f.create(t, docBody("SALES_INVOICE", f.depot, s.ID, "2026-03-01", "", lineJSON(f.p1, "1", "10", "")))
	require.NotNil(t, d.SalesRepUserID, "the card's salesperson is the default")
	assert.Equal(t, f.clerkID, *d.SalesRepUserID)
	f.postDoc(t, d)
	rec = f.do(t, http.MethodGet, f.path("/sicils/"+idStr(s.ID)+"/summary"), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	sum := decode[struct {
		SalesRepUserID *uint   `json:"salesRepUserId"`
		SalesRepName   *string `json:"salesRepName"`
	}](t, rec)
	require.NotNil(t, sum.SalesRepUserID)
	assert.Equal(t, f.clerkID, *sum.SalesRepUserID)
	assert.Equal(t, s.SalesRepName, sum.SalesRepName)

	rec = f.req(t, http.MethodPut, f.path("/sicils/"+idStr(s.ID)), f.token, fmt.Sprintf(`{"sales_rep_user_id":%d}`, f.adminID),
		map[string]string{"If-Match": `"` + strconv.Itoa(got.Version) + `"`})
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code, rec.Body.String())
	assert.Equal(t, "ERR_USER_NOT_FOUND", errCode(t, rec))
	rec = f.req(t, http.MethodPut, f.path("/sicils/"+idStr(s.ID)), f.token, `{"sales_rep_user_id":null}`,
		map[string]string{"If-Match": `"` + strconv.Itoa(got.Version) + `"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	u := decode[sicilRep](t, rec)
	assert.Nil(t, u.SalesRepUserID)
	assert.Nil(t, u.SalesRepName)
	rec = f.req(t, http.MethodPut, f.path("/sicils/"+idStr(s.ID)), f.token, fmt.Sprintf(`{"sales_rep_user_id":%d}`, f.clerkID),
		map[string]string{"If-Match": `"` + strconv.Itoa(u.Version) + `"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	u = decode[sicilRep](t, rec)
	require.NotNil(t, u.SalesRepUserID)
	assert.Equal(t, f.clerkID, *u.SalesRepUserID)
	assert.NotNil(t, u.SalesRepName)
}

func TestV15_GridBilledAndBillsWaybill(t *testing.T) {
	f := newDocs(t)
	billed := f.posted(t, docBody("SALES_WAYBILL", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "10", "")))
	open := f.posted(t, docBody("SALES_WAYBILL", f.depot, f.cust, "2026-03-02", "", lineJSON(f.p1, "1", "10", "")))
	unbilled := f.posted(t, docBody("SALES_WAYBILL", f.depot, f.cust, "2026-03-03", "", lineJSON(f.p1, "1", "10", "")))
	bill := f.posted(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-04", fmt.Sprintf(`,"waybillDocumentId":%d`, billed.ID)))
	gone := f.cancel(t, f.posted(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-05", fmt.Sprintf(`,"waybillDocumentId":%d`, unbilled.ID))))
	draftBill := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-06", fmt.Sprintf(`,"waybillDocumentId":%d`, open.ID)))
	plain := f.posted(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-07", "", lineJSON(f.p1, "1", "10", "")))

	g := f.grid(t, `{"billed":true,"startRow":0,"endRow":100}`)
	assert.ElementsMatch(t, []uint{billed.ID, open.ID}, g.ids(), "a draft bill bills too")
	g = f.grid(t, `{"billed":false,"startRow":0,"endRow":100}`)
	assert.ElementsMatch(t, []uint{unbilled.ID}, g.ids(), "a cancelled bill does not count")
	g = f.grid(t, `{"billsWaybill":true,"startRow":0,"endRow":100}`)
	assert.ElementsMatch(t, []uint{bill.ID, gone.ID, draftBill.ID}, g.ids())
	g = f.grid(t, `{"billsWaybill":false,"types":["SALES_INVOICE"],"startRow":0,"endRow":100}`)
	assert.ElementsMatch(t, []uint{plain.ID}, g.ids())

	rec := f.req(t, http.MethodPost, f.path("/documents/grid"), f.token, `{"billed":false}`, map[string]string{"Accept": xlsxType})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	sheet := sheetXML(t, rec.Body.Bytes())
	assert.Contains(t, sheet, unbilled.no())
	assert.NotContains(t, sheet, billed.no())
}

type dependentsErr struct {
	Code    string `json:"code"`
	Details struct {
		Dependents []struct {
			ID         uint    `json:"id"`
			DocumentNo *string `json:"documentNo"`
			Type       string  `json:"type"`
		} `json:"dependents"`
	} `json:"details"`
}

func TestV15_DependentsInTheError(t *testing.T) {
	f := newDocs(t)
	o := f.posted(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "5", "10", "")))
	r := f.posted(t, docBody("SALES_RETURN", f.depot, f.cust, "2026-03-02", fmt.Sprintf(`,"linkedDocumentId":%d`, o.ID),
		lineJSON(f.p1, "1", "10", fmt.Sprintf(`,"originLineId":%d`, o.Lines[0].ID))))
	f.create(t, docBody("SALES_RETURN", f.depot, f.cust, "2026-03-02", fmt.Sprintf(`,"linkedDocumentId":%d`, o.ID),
		lineJSON(f.p1, "1", "10", fmt.Sprintf(`,"originLineId":%d`, o.Lines[0].ID))))
	rec := f.cancelRaw(t, o.ID, o.Version, "customer left")
	require.Equal(t, http.StatusConflict, rec.Code)
	e := decode[dependentsErr](t, rec)
	assert.Equal(t, "ERR_DOCUMENT_HAS_DEPENDENTS", e.Code)
	require.Len(t, e.Details.Dependents, 1, "posted dependents only")
	assert.Equal(t, r.ID, e.Details.Dependents[0].ID)
	assert.Equal(t, r.DocumentNo, e.Details.Dependents[0].DocumentNo)
	assert.Equal(t, "SALES_RETURN", e.Details.Dependents[0].Type)

	w := f.create(t, docBody("SALES_WAYBILL", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "10", "")))
	b := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-02", fmt.Sprintf(`,"waybillDocumentId":%d`, w.ID)))
	rec = f.update(t, w.ID, w.Version, docBody("SALES_WAYBILL", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "2", "10", "")))
	require.Equal(t, http.StatusConflict, rec.Code, rec.Body.String())
	e = decode[dependentsErr](t, rec)
	assert.Equal(t, "ERR_DOCUMENT_HAS_DEPENDENTS", e.Code)
	require.Len(t, e.Details.Dependents, 1)
	assert.Equal(t, b.ID, e.Details.Dependents[0].ID)
	assert.Nil(t, e.Details.Dependents[0].DocumentNo)
	assert.Equal(t, "SALES_INVOICE", e.Details.Dependents[0].Type)

	rec = f.req(t, http.MethodDelete, f.path("/documents/"+idStr(w.ID)), f.token, "", map[string]string{"If-Match": `"` + strconv.Itoa(w.Version) + `"`})
	require.Equal(t, http.StatusConflict, rec.Code, rec.Body.String())
	e = decode[dependentsErr](t, rec)
	require.Len(t, e.Details.Dependents, 1)
	assert.Equal(t, b.ID, e.Details.Dependents[0].ID)
}

func TestV15_WarningsCarryStockNo(t *testing.T) {
	f := newDocs(t)
	d := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "20", "1", "")))
	g := f.getV15(t, d.ID)
	for _, code := range []string{"STOCK_SHORT", "PRICE_BELOW_LIST"} {
		ws := g.warningsOf(code)
		require.Len(t, ws, 1, code)
		require.NotNil(t, g.Warnings[ws[0]].StockNo, code)
		assert.Equal(t, "P1", *g.Warnings[ws[0]].StockNo, code)
	}
}

func TestV15_StockShortCountsOtherDocumentsOnly(t *testing.T) {
	f := newDocs(t)
	d := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "4", "10", ""), lineJSON(f.p1, "6", "10", "")))
	assert.Empty(t, f.getV15(t, d.ID).warningsOf("STOCK_SHORT"))

	require.NoError(t, testDB.Exec(`UPDATE stock_reservations SET expires_at = reserved_at + interval '1 microsecond'
		WHERE document_id = ?`, d.ID).Error)
	assert.Equal(t, "10", f.reserved(t, f.p1, f.depot), "expired, not swept")
	assert.Empty(t, f.getV15(t, d.ID).warningsOf("STOCK_SHORT"), "an expired hold never makes a line short")

	_, err := f.app.Sweeper.Sweep(t.Context(), time.Now().Add(49*time.Hour))
	require.NoError(t, err)
	assert.Equal(t, "0", f.reserved(t, f.p1, f.depot))
	assert.Empty(t, f.getV15(t, d.ID).warningsOf("STOCK_SHORT"), "nothing held, nothing short")

	other := f.create(t, docBody("SALES_WAYBILL", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "5", "10", "")))
	assert.Equal(t, "5", other.Lines[0].ReservedQty)
	g := f.getV15(t, d.ID)
	short := g.warningsOf("STOCK_SHORT")
	require.Len(t, short, 1, "10 − 5 held by another document < 6")
	w := g.Warnings[short[0]]
	assert.Equal(t, 2, *w.LineNo)
	assert.Equal(t, "5", *w.Available)

	u := f.update(t, d.ID, g.Version, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "7", "10", ""), lineJSON(f.p1, "6", "10", "")))
	require.Equal(t, http.StatusOK, u.Code, u.Body.String())
	g = f.getV15(t, d.ID)
	short = g.warningsOf("STOCK_SHORT")
	require.Len(t, short, 2)
	for _, i := range short {
		assert.Equal(t, "5", *g.Warnings[i].Available, "lines of one part report the same value")
	}
}

type lpcJSON struct {
	Amount     string `json:"amount"`
	Date       string `json:"date"`
	DocumentNo string `json:"documentNo"`
}

func TestV15_LastPurchaseCost(t *testing.T) {
	f := newDocs(t)
	part := func(id uint) *lpcJSON {
		return decode[struct {
			LastPurchaseCost *lpcJSON `json:"lastPurchaseCost"`
		}](t, f.do(t, http.MethodGet, f.path("/catalog/parts/"+idStr(id)), f.token, "")).LastPurchaseCost
	}
	raw := f.do(t, http.MethodGet, f.path("/catalog/parts/"+idStr(f.p1)), f.token, "").Body.String()
	assert.Contains(t, raw, `"lastPurchaseCost":null`)

	older := f.posted(t, docBody("PURCHASE_INVOICE", f.depot, f.supp, "2026-02-01", "", lineJSON(f.p1, "3", "10", `,"discount1Pct":"0","discount2Pct":"0"`)))
	w := f.posted(t, docBody("PURCHASE_WAYBILL", f.depot2, f.supp, "2026-02-05", "", lineJSON(f.p1, "3", "7", `,"discount1Pct":"0","discount2Pct":"0"`)))
	bill := f.posted(t, docBody("PURCHASE_INVOICE", f.depot2, f.supp, "2026-02-10", fmt.Sprintf(`,"waybillDocumentId":%d`, w.ID)))
	f.posted(t, docBody("PURCHASE_WAYBILL", f.depot, f.supp, "2026-02-20", "", lineJSON(f.p1, "1", "99", "")))
	f.posted(t, docBody("PURCHASE_RETURN", f.depot, f.supp, "2026-02-21", fmt.Sprintf(`,"linkedDocumentId":%d`, older.ID),
		lineJSON(f.p1, "1", "50", fmt.Sprintf(`,"originLineId":%d`, older.Lines[0].ID))))
	f.cancel(t, f.posted(t, docBody("PURCHASE_INVOICE", f.depot, f.supp, "2026-02-22", "", lineJSON(f.p1, "1", "77", ""))))
	f.posted(t, docBody("PRICE_DIFFERENCE", f.depot, f.supp, "2026-02-23", fmt.Sprintf(`,"linkedDocumentId":%d,"priceDirection":"INCREASE"`, older.ID),
		fmt.Sprintf(`{"qty":"1","unitPrice":"5","originLineId":%d}`, older.Lines[0].ID)))
	f.posted(t, docBody("PURCHASE_INVOICE", f.depot, f.supp, "2099-01-01", "", lineJSON(f.p1, "1", "55", "")))

	lpc := part(f.p1)
	require.NotNil(t, lpc)
	assert.Equal(t, "7", lpc.Amount)
	assert.Equal(t, "2026-02-10", lpc.Date)
	assert.Equal(t, bill.no(), lpc.DocumentNo)
	assert.Nil(t, part(f.p2))

	f.posted(t, docBody("PURCHASE_INVOICE", f.depot, f.supp, "2026-02-11", "", lineJSON(f.p2, "3", "10", `,"discount1Pct":"10","discount2Pct":"0"`)))
	rows := decode[struct {
		Rows []struct {
			ID               uint     `json:"id"`
			LastPurchaseCost *lpcJSON `json:"lastPurchaseCost"`
		} `json:"rows"`
	}](t, f.req(t, http.MethodPost, f.path("/catalog/parts/grid"), f.token, `{"startRow":0,"endRow":100}`, nil))
	require.Len(t, rows.Rows, 2)
	got := map[uint]string{}
	for _, r := range rows.Rows {
		require.NotNil(t, r.LastPurchaseCost)
		got[r.ID] = r.LastPurchaseCost.Amount
	}
	assert.Equal(t, map[uint]string{f.p1: "7", f.p2: "9"}, got)
}

func TestV15_DashboardHasNoLatest(t *testing.T) {
	f := newDocs(t)
	f.posted(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "10", "")))
	d := decode[map[string]any](t, f.do(t, http.MethodGet, f.path("/dashboard?date=2026-12-31"), f.token, ""))
	assert.NotContains(t, d, "latest")
	assert.Contains(t, d, "receivables")
}
