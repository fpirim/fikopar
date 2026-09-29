//go:build integration

package router_test

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"strconv"
	"testing"
	"time"

	"github.com/fpirim/fikopar/backend/internal/repository"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestDocuments_DraftDefaultsAmountsAndReservations(t *testing.T) {
	f := newDocs(t)
	d := f.create(t, docBody("SALES_INVOICE_WITH_WAYBILL", f.depot, f.cust, "2026-03-10", "",
		lineJSON(f.p1, "3", "12.3456", ""),
		lineJSON(f.p1, "8", "10", `,"discount1Pct":"0","discount2Pct":"0","vatPct":"20"`)))

	assert.Equal(t, "DRAFT", d.Status)
	assert.Nil(t, d.DocumentNo)
	require.NotNil(t, d.DueDate)
	assert.Equal(t, "2026-04-09", *d.DueDate, "document date + the card's 30 days")
	require.NotNil(t, d.SalesRepUserID)
	assert.Equal(t, f.clerkID, *d.SalesRepUserID)
	assert.Equal(t, "Card C1", d.SicilName)

	l := d.Lines[0]
	assert.Equal(t, []string{"10", "5", "20"}, []string{l.Discount1Pct, l.Discount2Pct, l.VatPct}, "card discounts, part VAT")
	assert.Equal(t, []string{"37.04", "5.37", "31.67", "6.33", "38.00"}, []string{l.GrossAmount, l.DiscountAmount, l.NetAmount, l.VatAmount, l.LineTotal})
	assert.Equal(t, []string{"117.04", "5.37", "111.67", "22.33", "134.00"}, []string{d.GrossTotal, d.DiscountTotal, d.NetTotal, d.VatTotal, d.GrandTotal})
	require.Len(t, d.VatBreakdown, 1)
	assert.Equal(t, []string{"20", "111.67", "22.33"}, []string{d.VatBreakdown[0].VatPct, d.VatBreakdown[0].Base, d.VatBreakdown[0].Vat})

	assert.Equal(t, "3", d.Lines[0].ReservedQty)
	assert.Equal(t, "7", d.Lines[1].ReservedQty, "min(qty, available)")
	assert.Equal(t, "10", d.Lines[1].OnHand)
	assert.Equal(t, "0", d.Lines[1].Available)
	require.Len(t, d.Lines[0].StockByDepot, 2)
	assert.Equal(t, []string{"10", "10", "0"}, []string{d.Lines[0].StockByDepot[0].OnHand, d.Lines[0].StockByDepot[0].Reserved, d.Lines[0].StockByDepot[0].Available})
	require.NotNil(t, d.Reservation)
	exp, err := time.Parse(time.RFC3339Nano, d.Reservation.ExpiresAt)
	require.NoError(t, err)
	assert.WithinDuration(t, time.Now().Add(48*time.Hour), exp, time.Minute)
	short := d.warnings("STOCK_SHORT")
	require.Len(t, short, 1)
	assert.Equal(t, 2, *short[0].LineNo)
	assert.Equal(t, "8", *short[0].Requested)
	assert.Equal(t, "10", *short[0].Available, "available without this draft")
	assert.Equal(t, "10", f.reserved(t, f.p1, f.depot))
	noDrift(t)

	envs := outboxEnvelopes(t, "StockReserved", d.ID)
	require.Len(t, envs, 1)
	validateEvent(t, envs[0])
	p := decodeBytes[struct {
		Lines []struct {
			Reserved       string `json:"reserved"`
			AvailableAfter string `json:"availableAfter"`
		} `json:"lines"`
	}](t, []byte(outboxPayloads(t, "StockReserved", d.ID)[0]))
	assert.Equal(t, "3", p.Lines[0].Reserved)
	assert.Equal(t, "7", p.Lines[0].AvailableAfter)
	assert.Equal(t, "0", p.Lines[1].AvailableAfter)

	// Effects: what the post will do.
	ledger := map[string]string{}
	for _, e := range d.Effects.Ledger {
		ledger[e.Side[:1]+" "+e.AccountCode] = e.Amount
	}
	assert.Equal(t, map[string]string{"D 120.C1": "134.00", "D 611": "5.37", "C 600": "117.04", "C 391": "22.33"}, ledger)
	require.Len(t, d.Effects.Stock, 1)
	assert.Equal(t, "-11", d.Effects.Stock[0].QtyDelta)
}

func TestDocuments_UpdateReplacesReservationsUnderIfMatch(t *testing.T) {
	f := newDocs(t)
	d := f.create(t, docBody("SALES_WAYBILL", f.depot, f.cust, "2026-03-10", "", lineJSON(f.p1, "4", "10", "")))
	body := docBody("SALES_WAYBILL", f.depot, f.cust, "2026-03-11", "", lineJSON(f.p1, "6", "10", ""))

	rec := f.req(t, http.MethodPut, f.path("/documents/"+idStr(d.ID)), f.token, body, nil)
	assert.Equal(t, http.StatusPreconditionRequired, rec.Code)
	rec = f.update(t, d.ID, d.Version+5, body)
	assert.Equal(t, http.StatusPreconditionFailed, rec.Code)
	rec = f.update(t, d.ID, d.Version, body)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	u := decode[docJSON](t, rec)
	assert.Equal(t, d.Version+1, u.Version)
	assert.Equal(t, `"`+strconv.Itoa(u.Version)+`"`, rec.Header().Get("ETag"))
	assert.Equal(t, "6", u.Lines[0].ReservedQty)
	assert.Equal(t, "6", f.reserved(t, f.p1, f.depot))
	noDrift(t)

	rel := outboxPayloads(t, "StockReleased", d.ID)
	require.Len(t, rel, 1)
	assert.Contains(t, string(rel[0]), `"UPDATED"`)
	for _, e := range outboxEnvelopes(t, "StockReleased", d.ID) {
		validateEvent(t, e)
	}
	assert.Len(t, outboxPayloads(t, "StockReserved", d.ID), 2)

	// A stale version after the update.
	assert.Equal(t, http.StatusPreconditionFailed, f.update(t, d.ID, d.Version, body).Code)
}

func TestDocuments_DeleteDraftReleasesReservations(t *testing.T) {
	f := newDocs(t)
	d := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-10", "", lineJSON(f.p1, "4", "10", "")))
	rec := f.req(t, http.MethodDelete, f.path("/documents/"+idStr(d.ID)), f.token, "", map[string]string{"If-Match": `"99"`})
	assert.Equal(t, http.StatusPreconditionFailed, rec.Code)
	rec = f.req(t, http.MethodDelete, f.path("/documents/"+idStr(d.ID)), f.token, "", map[string]string{"If-Match": `"` + strconv.Itoa(d.Version) + `"`})
	require.Equal(t, http.StatusNoContent, rec.Code, rec.Body.String())
	assert.Equal(t, "0", f.reserved(t, f.p1, f.depot))
	rel := outboxPayloads(t, "StockReleased", d.ID)
	require.Len(t, rel, 1)
	assert.Contains(t, string(rel[0]), `"DELETED"`)
	assert.Equal(t, http.StatusNotFound, f.do(t, http.MethodGet, f.path("/documents/"+idStr(d.ID)), f.token, "").Code)

	p := f.posted(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-10", "", lineJSON(f.p1, "1", "10", "")))
	rec = f.req(t, http.MethodDelete, f.path("/documents/"+idStr(p.ID)), f.token, "", map[string]string{"If-Match": `"` + strconv.Itoa(p.Version) + `"`})
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_DOCUMENT_NOT_DRAFT", errCode(t, rec))
	noDrift(t)
}

func TestDocuments_PostSalesInvoice(t *testing.T) {
	f := newDocs(t)
	d := f.create(t, docBody("SALES_INVOICE_WITH_WAYBILL", f.depot, f.cust, "2026-03-10", "",
		lineJSON(f.p1, "3", "12.3456", ""),
		lineJSON(f.p1, "8", "10", `,"discount1Pct":"0","discount2Pct":"0"`)))

	key := uuid.NewString()
	h := map[string]string{"If-Match": `"` + strconv.Itoa(d.Version) + `"`, "Idempotency-Key": key}
	rec := f.req(t, http.MethodPost, f.path("/documents/"+idStr(d.ID)+"/post"), f.token, "", h)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	p := decode[docJSON](t, rec)
	assert.Equal(t, "POSTED", p.Status)
	assert.Equal(t, "SF-2026-1-000001", p.no())
	assert.Equal(t, `"`+strconv.Itoa(p.Version)+`"`, rec.Header().Get("ETag"))
	neg := p.warnings("NEGATIVE_STOCK")
	require.Len(t, neg, 1)
	assert.Equal(t, "-1", *neg[0].OnHandAfter)
	assert.Equal(t, "-1", f.onHand(t, f.p1, f.depot))
	assert.Equal(t, "0", f.reserved(t, f.p1, f.depot))
	noDrift(t)

	require.NotNil(t, p.JournalEntryID)
	rows := entryPostings(t, *p.JournalEntryID)
	assert.Equal(t, map[string]string{"D C": "134.00", "D 611": "5.37", "C 600": "117.04", "C 391": "22.33"}, postingSet(rows))
	for _, r := range rows {
		if r.Code == "C" {
			require.NotNil(t, r.DueDate)
			assert.Equal(t, "2026-04-09", *r.DueDate)
		}
	}
	var moves []struct {
		Kind string
		Qty  string
		Date string
	}
	require.NoError(t, testDB.Raw(`SELECT kind, trim_scale(qty)::text AS qty, movement_date::text AS date FROM stock_movements
		WHERE source_type = 'document' AND source_id IN (?, ?) ORDER BY id`, idStr(p.Lines[0].ID), idStr(p.Lines[1].ID)).Scan(&moves).Error)
	require.Len(t, moves, 2)
	assert.Equal(t, "OUT", moves[0].Kind)
	assert.Equal(t, "2026-03-10", moves[0].Date)

	posted := outboxEnvelopes(t, "DocumentPosted", d.ID)
	require.Len(t, posted, 1)
	validateEvent(t, posted[0])
	rel := outboxPayloads(t, "StockReleased", d.ID)
	require.Len(t, rel, 1)
	assert.Contains(t, string(rel[0]), `"POSTED"`)

	// Replay: same response, nothing posted twice.
	rec = f.req(t, http.MethodPost, f.path("/documents/"+idStr(d.ID)+"/post"), f.token, "", h)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Equal(t, "true", rec.Header().Get("Idempotent-Replayed"))
	assert.Equal(t, p.no(), decode[docJSON](t, rec).no())
	var entries int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM journal_entries WHERE source_type = 'document' AND source_id = ?`, idStr(d.ID)).Scan(&entries).Error)
	assert.EqualValues(t, 1, entries)

	// A second post with a new key: not a draft any more.
	rec = f.postRaw(t, f.token, d.ID, p.Version, "")
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_DOCUMENT_NOT_DRAFT", errCode(t, rec))

	// The relay delivers the inline-handled event once more without a second entry.
	f.relay(t)
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM journal_entries WHERE source_type = 'document' AND source_id = ?`, idStr(d.ID)).Scan(&entries).Error)
	assert.EqualValues(t, 1, entries)

	// The entry is owned by the document.
	rec = f.post(t, f.path("/ledger/entries/"+idStr(*p.JournalEntryID)+"/reversal"), f.token, `{"reason":"manual try"}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	owner := decode[struct {
		Code  string `json:"code"`
		Owner struct {
			SourceType string  `json:"sourceType"`
			SourceID   string  `json:"sourceId"`
			SourceNo   *string `json:"sourceNo"`
		} `json:"owner"`
	}](t, rec)
	assert.Equal(t, "ERR_ENTRY_OWNED_BY_SOURCE", owner.Code)
	assert.Equal(t, "document", owner.Owner.SourceType)
	assert.Equal(t, idStr(d.ID), owner.Owner.SourceID)
	require.NotNil(t, owner.Owner.SourceNo)
	assert.Equal(t, p.no(), *owner.Owner.SourceNo)
}

func TestDocuments_PostingTable(t *testing.T) {
	f := newDocs(t)
	f.move(t, f.p2, f.depot, "IN", "50", "2026-01-02")
	sale := f.posted(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "",
		lineJSON(f.p2, "10", "20", `,"discount1Pct":"10","discount2Pct":"0"`)))
	buy := f.posted(t, docBody("PURCHASE_INVOICE", f.depot, f.supp, "2026-03-01", "",
		lineJSON(f.p2, "10", "20", `,"discount1Pct":"10"`)))

	cases := []struct {
		name string
		body string
		want map[string]string
	}{
		{"sales invoice", "", map[string]string{"D C": "198.00", "D 611": "20.00", "C 600": "200.00", "C 391": "18.00"}},
		{"purchase invoice", "", map[string]string{"D 153": "180.00", "D 191": "18.00", "C C": "198.00"}},
		{"sales return", docBody("SALES_RETURN", f.depot, f.cust, "2026-03-02", fmt.Sprintf(`,"linkedDocumentId":%d`, sale.ID),
			lineJSON(f.p2, "2", "20", fmt.Sprintf(`,"discount1Pct":"10","discount2Pct":"0","originLineId":%d`, sale.Lines[0].ID))),
			map[string]string{"D 610": "36.00", "D 391": "3.60", "C C": "39.60"}},
		{"purchase return", docBody("PURCHASE_RETURN", f.depot, f.supp, "2026-03-02", fmt.Sprintf(`,"linkedDocumentId":%d`, buy.ID),
			lineJSON(f.p2, "2", "20", fmt.Sprintf(`,"discount1Pct":"10","originLineId":%d`, buy.Lines[0].ID))),
			map[string]string{"D C": "39.60", "C 153": "36.00", "C 191": "3.60"}},
		{"price increase on a sale", docBody("PRICE_DIFFERENCE", f.depot, f.cust, "2026-03-03",
			fmt.Sprintf(`,"linkedDocumentId":%d,"priceDirection":"INCREASE"`, sale.ID), `{"qty":"1","unitPrice":"50","vatPct":"20","description":"Kur farkı"}`),
			map[string]string{"D C": "60.00", "C 600": "50.00", "C 391": "10.00"}},
		{"price decrease on a sale", docBody("PRICE_DIFFERENCE", f.depot, f.cust, "2026-03-03",
			fmt.Sprintf(`,"linkedDocumentId":%d,"priceDirection":"DECREASE"`, sale.ID), fmt.Sprintf(`{"qty":"1","unitPrice":"30","originLineId":%d}`, sale.Lines[0].ID)),
			map[string]string{"D 611": "30.00", "D 391": "3.00", "C C": "33.00"}},
		{"price increase on a purchase", docBody("PRICE_DIFFERENCE", f.depot, f.supp, "2026-03-03",
			fmt.Sprintf(`,"linkedDocumentId":%d,"priceDirection":"INCREASE"`, buy.ID), `{"qty":"1","unitPrice":"50","vatPct":"20","description":"Fark"}`),
			map[string]string{"D 153": "50.00", "D 191": "10.00", "C C": "60.00"}},
		{"price decrease on a purchase", docBody("PRICE_DIFFERENCE", f.depot, f.supp, "2026-03-03",
			fmt.Sprintf(`,"linkedDocumentId":%d,"priceDirection":"DECREASE"`, buy.ID), `{"qty":"1","unitPrice":"50","vatPct":"20","description":"Fark"}`),
			map[string]string{"D C": "60.00", "C 153": "50.00", "C 191": "10.00"}},
	}
	for _, c := range cases {
		t.Run(c.name, func(t *testing.T) {
			d := sale
			switch {
			case c.name == "purchase invoice":
				d = buy
			case c.body != "":
				d = f.posted(t, c.body)
			}
			require.NotNil(t, d.JournalEntryID, c.name)
			assert.Equal(t, c.want, postingSet(entryPostings(t, *d.JournalEntryID)))
			ledger := map[string]string{}
			for _, e := range d.Effects.Ledger {
				code := e.AccountCode
				if e.Role == nil {
					code = "C"
				}
				ledger[e.Side[:1]+" "+code] = e.Amount
			}
			assert.Equal(t, c.want, ledger, "effects mirror the entry")
		})
	}

	// Waybills never reach the ledger; price differences never move stock.
	w := f.posted(t, docBody("PURCHASE_WAYBILL", f.depot, f.supp, "2026-03-04", "", lineJSON(f.p2, "1", "5", "")))
	assert.Nil(t, w.JournalEntryID)
	assert.Empty(t, w.Effects.Ledger)
	var moved int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM stock_movements m JOIN document_lines l ON l.id::text = m.source_id
		JOIN documents d ON d.id = l.document_id WHERE m.source_type = 'document' AND d.type = 'PRICE_DIFFERENCE'`).Scan(&moved).Error)
	assert.Zero(t, moved)
}

func TestDocuments_MissingRoleAndZeroTotal(t *testing.T) {
	f := newDocs(t)
	d := f.posted(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "0", "")))
	assert.Nil(t, d.JournalEntryID, "a zero total posts no entry")

	f.setRoles(t, fmt.Sprintf(`[{"role":"SALES","accountId":%d}]`, f.accounts["600"]))
	d = f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "10", "")))
	rec := f.postRaw(t, f.token, d.ID, d.Version, "")
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_ACCOUNT_ROLE_MISSING", errCode(t, rec))
	assert.Equal(t, "DRAFT", f.get(t, d.ID).Status, "nothing of the post is kept")
	assert.Equal(t, "1", f.get(t, d.ID).Lines[0].ReservedQty)
}

func TestDocuments_CancelReversesLedgerAndStock(t *testing.T) {
	f := newDocs(t)
	d := f.posted(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-10", "", lineJSON(f.p1, "4", "10", "")))
	require.Equal(t, "6", f.onHand(t, f.p1, f.depot))

	rec := f.cancelRaw(t, d.ID, d.Version, "no")
	assert.Equal(t, http.StatusBadRequest, rec.Code, "reason of 3 or more")
	rec = f.cancelRaw(t, d.ID, d.Version+3, "wrong customer")
	assert.Equal(t, http.StatusPreconditionFailed, rec.Code)

	key := uuid.NewString()
	h := map[string]string{"If-Match": `"` + strconv.Itoa(d.Version) + `"`, "Idempotency-Key": key}
	rec = f.req(t, http.MethodPost, f.path("/documents/"+idStr(d.ID)+"/cancel"), f.token, `{"reason":"wrong customer"}`, h)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	c := decode[docJSON](t, rec)
	today := time.Now().In(time.FixedZone("TRT", 3*3600)).Format("2006-01-02")
	assert.Equal(t, "CANCELLED", c.Status)
	require.NotNil(t, c.CancelDate)
	assert.Equal(t, today, *c.CancelDate)
	require.NotNil(t, c.ReversalEntryID)
	assert.Equal(t, "10", f.onHand(t, f.p1, f.depot))
	noDrift(t)

	var rev struct {
		EntryDate string
		Reverses  uint
	}
	require.NoError(t, testDB.Raw(`SELECT entry_date::text AS entry_date, reverses_entry_id AS reverses FROM journal_entries WHERE id = ?`,
		*c.ReversalEntryID).Scan(&rev).Error)
	assert.Equal(t, today, rev.EntryDate)
	assert.Equal(t, *d.JournalEntryID, rev.Reverses)
	var in struct {
		Kind string
		Date string
	}
	require.NoError(t, testDB.Raw(`SELECT kind, movement_date::text AS date FROM stock_movements WHERE source_type = 'document'
		AND source_id = ? AND kind = 'IN'`, idStr(d.Lines[0].ID)).Scan(&in).Error)
	assert.Equal(t, today, in.Date)

	envs := outboxEnvelopes(t, "DocumentCancelled", d.ID)
	require.Len(t, envs, 1)
	validateEvent(t, envs[0])

	rec = f.req(t, http.MethodPost, f.path("/documents/"+idStr(d.ID)+"/cancel"), f.token, `{"reason":"wrong customer"}`, h)
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Equal(t, "true", rec.Header().Get("Idempotent-Replayed"))
	assert.Equal(t, "10", f.onHand(t, f.p1, f.depot), "replay moves nothing")

	rec = f.cancelRaw(t, d.ID, c.Version, "again please")
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_INVALID_STATE_TRANSITION", errCode(t, rec))

	dr := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-10", "", lineJSON(f.p1, "1", "10", "")))
	rec = f.cancelRaw(t, dr.ID, dr.Version, "a draft is deleted")
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_INVALID_STATE_TRANSITION", errCode(t, rec))
}

func TestDocuments_WaybillBillingOnce(t *testing.T) {
	f := newDocs(t)
	w := f.posted(t, docBody("SALES_WAYBILL", f.depot, f.cust, "2026-03-01", "",
		lineJSON(f.p1, "2", "10", ""), lineJSON(f.p1, "1", "10", "")))
	assert.Equal(t, "SIR-2026-1-000001", w.no())
	assert.Equal(t, "7", f.onHand(t, f.p1, f.depot))

	b := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-05", fmt.Sprintf(`,"waybillDocumentId":%d`, w.ID)))
	require.Len(t, b.Lines, 2, "empty lines copy the waybill")
	assert.Equal(t, w.Lines[0].ID, *b.Lines[0].BilledLineID)
	assert.Equal(t, "0", b.Lines[0].ReservedQty, "a bill reserves nothing")
	assert.Empty(t, b.Effects.Stock)

	rec := f.createRaw(t, f.token, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-05", fmt.Sprintf(`,"waybillDocumentId":%d`, w.ID)))
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_WAYBILL_BILLED", errCode(t, rec))
	rec = f.createRaw(t, f.token, docBody("PURCHASE_INVOICE", f.depot, f.supp, "2026-03-05", fmt.Sprintf(`,"waybillDocumentId":%d`, w.ID)))
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_WAYBILL_INVALID", errCode(t, rec))

	bp := f.postDoc(t, b)
	assert.Equal(t, "7", f.onHand(t, f.p1, f.depot), "the bill moves no stock")
	require.NotNil(t, bp.JournalEntryID)
	assert.Equal(t, bp.ID, *f.get(t, w.ID).BilledByDocumentID)

	rec = f.cancelRaw(t, w.ID, w.Version, "wrong waybill")
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_WAYBILL_BILLED", errCode(t, rec))

	f.cancel(t, bp)
	assert.Equal(t, "7", f.onHand(t, f.p1, f.depot))
	assert.Nil(t, f.get(t, w.ID).BilledByDocumentID)
	b2 := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-06", fmt.Sprintf(`,"waybillDocumentId":%d`, w.ID)))
	f.postDoc(t, b2)

	// A bill whose lines do not match the waybill.
	w2 := f.posted(t, docBody("SALES_WAYBILL", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "2", "10", "")))
	bad := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-05", fmt.Sprintf(`,"waybillDocumentId":%d`, w2.ID),
		lineJSON(f.p1, "1", "12", fmt.Sprintf(`,"billedLineId":%d`, w2.Lines[0].ID))))
	rec = f.postRaw(t, f.token, bad.ID, bad.Version, "")
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_WAYBILL_INVALID", errCode(t, rec))
}

func TestDocuments_ReturnsAreCapped(t *testing.T) {
	f := newDocs(t)
	o := f.posted(t, docBody("SALES_INVOICE_WITH_WAYBILL", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "5", "10", "")))
	ret := func(typ, qty string) docJSON {
		return f.create(t, docBody(typ, f.depot, f.cust, "2026-03-02", fmt.Sprintf(`,"linkedDocumentId":%d`, o.ID),
			lineJSON(f.p1, qty, "10", fmt.Sprintf(`,"originLineId":%d`, o.Lines[0].ID))))
	}
	r1 := f.postDoc(t, ret("SALES_RETURN", "3"))
	assert.Equal(t, "SI-2026-1-000001", r1.no())
	assert.Equal(t, "8", f.onHand(t, f.p1, f.depot), "5 out, 3 back")
	assert.Equal(t, "3", *f.get(t, o.ID).Lines[0].ReturnedQty)

	r2 := ret("SALES_RETURN_WAYBILL", "3")
	rec := f.postRaw(t, f.token, r2.ID, r2.Version, "")
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_RETURN_QTY_EXCEEDED", errCode(t, rec))
	assert.Equal(t, "lines[0].qty", decode[struct {
		Field string `json:"field"`
	}](t, rec).Field)
	r3 := f.postDoc(t, ret("SALES_RETURN_WAYBILL", "2"))
	assert.Equal(t, "5", *f.get(t, o.ID).Lines[0].ReturnedQty)

	rec = f.cancelRaw(t, o.ID, o.Version, "customer left")
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_DOCUMENT_HAS_DEPENDENTS", errCode(t, rec))
	assert.ElementsMatch(t, []uint{r1.ID, r3.ID}, f.get(t, o.ID).DependentDocumentIDs)

	// A return of another card's invoice or of an unposted document.
	rec = f.createRaw(t, f.token, docBody("SALES_RETURN", f.depot, f.supp, "2026-03-02", fmt.Sprintf(`,"linkedDocumentId":%d`, o.ID),
		lineJSON(f.p1, "1", "10", fmt.Sprintf(`,"originLineId":%d`, o.Lines[0].ID))))
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_LINKED_DOCUMENT_INVALID", errCode(t, rec))
	rec = f.createRaw(t, f.token, docBody("SALES_RETURN", f.depot, f.cust, "2026-03-02", "", lineJSON(f.p1, "1", "10", "")))
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_LINKED_DOCUMENT_INVALID", errCode(t, rec))
}

func TestDocuments_PriceDifferences(t *testing.T) {
	f := newDocs(t)
	o := f.posted(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "",
		lineJSON(f.p1, "2", "100", `,"discount1Pct":"0","discount2Pct":"0"`)))
	fb := func(dir, body string) string {
		return docBody("PRICE_DIFFERENCE", f.depot, f.cust, "2026-03-02", fmt.Sprintf(`,"linkedDocumentId":%d%s`, o.ID, dir), body)
	}

	for _, c := range []struct{ body, field string }{
		{fb("", `{"qty":"1","unitPrice":"5","vatPct":"20","description":"x"}`), "priceDirection"},
		{fb(`,"priceDirection":"INCREASE"`, `{"qty":"2","unitPrice":"5","vatPct":"20","description":"x"}`), "lines[0].qty"},
		{fb(`,"priceDirection":"INCREASE"`, `{"qty":"1","unitPrice":"5","vatPct":"20","discount1Pct":"5","description":"x"}`), "lines[0].discount1Pct"},
		{fb(`,"priceDirection":"INCREASE"`, `{"qty":"1","unitPrice":"5","description":"x"}`), "lines[0].vatPct"},
		{docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-02", `,"priceDirection":"DECREASE"`, lineJSON(f.p1, "1", "1", "")), "priceDirection"},
		{docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-02", "", `{"qty":"1","unitPrice":"1"}`), "lines[0].partId"},
	} {
		rec := f.createRaw(t, f.token, c.body)
		require.Equal(t, http.StatusBadRequest, rec.Code, rec.Body.String())
		assert.Equal(t, c.field, decode[struct {
			Field string `json:"field"`
		}](t, rec).Field)
	}

	inc := f.posted(t, fb(`,"priceDirection":"INCREASE"`, fmt.Sprintf(`{"qty":"1","unitPrice":"50","originLineId":%d}`, o.Lines[0].ID)))
	assert.Equal(t, "FF-2026-1-000001", inc.no())
	assert.Equal(t, "20", inc.Lines[0].VatPct, "VAT of the origin line")
	assert.Equal(t, f.p1, *inc.Lines[0].PartID, "part of the origin line")

	d1 := f.posted(t, fb(`,"priceDirection":"DECREASE"`, `{"qty":"1","unitPrice":"150","vatPct":"20","description":"İskonto"}`))
	d2 := f.create(t, fb(`,"priceDirection":"DECREASE"`, `{"qty":"1","unitPrice":"50.01","vatPct":"20","description":"İskonto"}`))
	rec := f.postRaw(t, f.token, d2.ID, d2.Version, "")
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code, "the increase does not raise the cap")
	assert.Equal(t, "ERR_PRICE_DIFFERENCE_EXCEEDED", errCode(t, rec))
	assert.Equal(t, "lines", decode[struct {
		Field string `json:"field"`
	}](t, rec).Field)
	f.cancel(t, d1)
	f.postDoc(t, f.get(t, d2.ID))

	rec = f.cancelRaw(t, o.ID, o.Version, "customer left")
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_DOCUMENT_HAS_DEPENDENTS", errCode(t, rec))
}

func TestDocuments_CreditLimitBlocksUnlessAdminOverrides(t *testing.T) {
	f := newDocs(t)
	f.setCreditLimit(t, f.cust, "100")
	d := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "",
		lineJSON(f.p1, "1", "100", `,"discount1Pct":"0","discount2Pct":"0"`)))
	assert.Len(t, d.warnings("CREDIT_LIMIT"), 1)
	require.NotNil(t, d.Effects.CreditUsedPctAfter)
	assert.Equal(t, 120, *d.Effects.CreditUsedPctAfter)

	rec := f.postRaw(t, f.token, d.ID, d.Version, "")
	require.Equal(t, http.StatusConflict, rec.Code, rec.Body.String())
	e := decode[struct {
		Code    string            `json:"code"`
		Details map[string]string `json:"details"`
	}](t, rec)
	assert.Equal(t, "ERR_CREDIT_LIMIT_EXCEEDED", e.Code)
	assert.Equal(t, map[string]string{"limit": "100.00", "balance": "0.00", "documentTotal": "120.00", "excess": "20.00"}, e.Details)

	override := `{"creditOverride":{"reason":"known customer"}}`
	rec = f.postRaw(t, f.token, d.ID, d.Version, override)
	assert.Equal(t, http.StatusForbidden, rec.Code, "only admins override")
	rec = f.postRaw(t, f.admin, d.ID, d.Version, `{"creditOverride":{"reason":"x"}}`)
	assert.Equal(t, http.StatusBadRequest, rec.Code, "reason of 3 or more")

	rec = f.postRaw(t, f.admin, d.ID, d.Version, override)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	p := decode[docJSON](t, rec)
	require.NotNil(t, p.CreditOverride)
	assert.Equal(t, "known customer", p.CreditOverride.Reason)
	assert.Equal(t, f.adminID, p.CreditOverride.UserID)
	assert.Equal(t, "20.00", p.CreditOverride.Excess)
	var audits []struct {
		Action string
		After  string
	}
	require.NoError(t, testDB.Raw(`SELECT action, after::text AS after FROM audit_events WHERE entity_type = 'document' AND entity_id = ?
		AND action = 'document.credit_override'`, idStr(d.ID)).Scan(&audits).Error)
	require.Len(t, audits, 1)
	assert.Contains(t, audits[0].After, "known customer")
	assert.Contains(t, audits[0].After, "20.00")
	posted := outboxPayloads(t, "DocumentPosted", d.ID)
	assert.Contains(t, string(posted[0]), `"known customer"`)

	// Balance now 120: a purchase is not checked, another sale is.
	f.posted(t, docBody("PURCHASE_INVOICE", f.depot, f.supp, "2026-03-01", "", lineJSON(f.p1, "1", "1000", "")))
	d2 := f.create(t, docBody("SALES_WAYBILL", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "1000", "")))
	f.postDoc(t, d2)
	d3 := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "0.01", `,"discount1Pct":"0","discount2Pct":"0","vatPct":"0"`)))
	rec = f.postRaw(t, f.token, d3.ID, d3.Version, "")
	require.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "120.00", decode[struct {
		Details map[string]string `json:"details"`
	}](t, rec).Details["balance"])

	// An override that is not needed is ignored.
	f.setCreditLimit(t, f.cust, "100000")
	rec = f.postRaw(t, f.admin, d3.ID, d3.Version, override)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Nil(t, decode[docJSON](t, rec).CreditOverride)
}

func TestDocuments_ClosedYear(t *testing.T) {
	f := newDocs(t)
	d := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2025-12-31", "", lineJSON(f.p1, "1", "10", "")))
	p := f.posted(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-01-05", "", lineJSON(f.p1, "1", "10", "")))
	require.NoError(t, testDB.Exec(`INSERT INTO carry_forward_runs (org_id, year, entry_count, total_debit, total_credit)
		VALUES (?, 2026, 0, 0, 0)`, f.org).Error)

	rec := f.createRaw(t, f.token, docBody("SALES_INVOICE", f.depot, f.cust, "2025-12-31", "", lineJSON(f.p1, "1", "10", "")))
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_YEAR_CLOSED", errCode(t, rec))
	rec = f.update(t, d.ID, d.Version, docBody("SALES_INVOICE", f.depot, f.cust, "2025-12-30", "", lineJSON(f.p1, "1", "10", "")))
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_YEAR_CLOSED", errCode(t, rec))
	rec = f.postRaw(t, f.token, d.ID, d.Version, "")
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_YEAR_CLOSED", errCode(t, rec))
	assert.Equal(t, "DRAFT", f.get(t, d.ID).Status)

	// The cancel date (today) in a closed year.
	next := time.Now().Year() + 1
	require.NoError(t, testDB.Exec(`INSERT INTO carry_forward_runs (org_id, year, entry_count, total_debit, total_credit)
		VALUES (?, ?, 0, 0, 0)`, f.org, next).Error)
	rec = f.cancelRaw(t, p.ID, p.Version, "too late")
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_YEAR_CLOSED", errCode(t, rec))
	assert.Equal(t, "POSTED", f.get(t, p.ID).Status)
}

func TestDocuments_ValidationErrors(t *testing.T) {
	f := newDocs(t)
	other := f.seedOrg(t, "other-docs", f.clerkID)
	var foreignSicil, foreignDepot uint
	require.NoError(t, testDB.Raw(`INSERT INTO sicils (organization_id, hesap_no, hesap_adi, sicil_tipi, created_at, updated_at)
		VALUES (?, 'X1', 'Foreign', 'MUSTERI', now(), now()) RETURNING id`, other).Scan(&foreignSicil).Error)
	require.NoError(t, testDB.Raw(`INSERT INTO depots (org_id, code, name) VALUES (?, '9', 'Foreign') RETURNING id`, other).Scan(&foreignDepot).Error)
	outsider := f.seedUser(t, "outsider@x.com", "pw", "user")

	for _, c := range []struct {
		body, code, field string
		status            int
	}{
		{docBody("SALES_INVOICE", f.depot, foreignSicil, "2026-03-01", "", lineJSON(f.p1, "1", "1", "")), "ERR_SICIL_NOT_FOUND", "sicilId", 422},
		{docBody("SALES_INVOICE", foreignDepot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "1", "")), "ERR_DEPOT_NOT_FOUND", "depotId", 422},
		{docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(999999, "1", "1", "")), "ERR_PART_NOT_FOUND", "lines[0].partId", 422},
		{docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", fmt.Sprintf(`,"salesRepUserId":%d`, outsider.ID), lineJSON(f.p1, "1", "1", "")), "ERR_USER_NOT_FOUND", "salesRepUserId", 422},
		{docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "0", "1", "")), "ERR_INVALID_REQUEST", "lines[0].qty", 400},
		{docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "1", `,"discount1Pct":"101"`)), "ERR_INVALID_REQUEST", "lines[0].discount1Pct", 400},
		{docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", `,"dueDate":"2026-02-01"`, lineJSON(f.p1, "1", "1", "")), "ERR_INVALID_REQUEST", "dueDate", 400},
		{docBody("NOT_A_TYPE", f.depot, f.cust, "2026-03-01", ""), "ERR_INVALID_REQUEST", "type", 400},
		{docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "1", `,"description":"free text"`)), "ERR_INVALID_REQUEST", "lines[0].description", 400},
	} {
		rec := f.createRaw(t, f.token, c.body)
		require.Equal(t, c.status, rec.Code, "%s: %s", c.code, rec.Body.String())
		e := decode[struct {
			Code  string `json:"code"`
			Field string `json:"field"`
		}](t, rec)
		assert.Equal(t, c.code, e.Code)
		assert.Equal(t, c.field, e.Field)
	}

	require.NoError(t, testDB.Exec(`UPDATE parts SET is_active = false WHERE id = ?`, f.p2).Error)
	rec := f.createRaw(t, f.token, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p2, "1", "1", "")))
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_PART_INACTIVE", errCode(t, rec))

	rec = f.do(t, http.MethodPost, f.path("/documents"), f.token, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", ""))
	assert.Equal(t, http.StatusBadRequest, rec.Code)
	assert.Equal(t, "ERR_IDEMPOTENCY_KEY_REQUIRED", errCode(t, rec))

	empty := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", ""))
	rec = f.postRaw(t, f.token, empty.ID, empty.Version, "")
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_DOCUMENT_EMPTY", errCode(t, rec))

	require.NoError(t, testDB.Exec(`UPDATE parts SET is_active = true WHERE id = ?`, f.p2).Error)
	dr := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p2, "1", "1", "")))
	require.NoError(t, testDB.Exec(`UPDATE parts SET is_active = false WHERE id = ?`, f.p2).Error)
	rec = f.postRaw(t, f.token, dr.ID, dr.Version, "")
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_PART_INACTIVE", errCode(t, rec))
}

func TestDocuments_CompaniesAreSeparate(t *testing.T) {
	f := newDocs(t)
	a := f.posted(t, docBody("SALES_WAYBILL", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "10", "")))

	g := &docFix{stockFix: &stockFix{ledgerFix: &ledgerFix{testApp: f.testApp, admin: f.admin}}, accounts: map[string]uint{}}
	u := f.seedUser(t, "clerk2@x.com", "pw", "user")
	g.org = f.seedOrg(t, "docs-b", u.ID)
	g.token = f.login(t, "clerk2@x.com", "pw")
	g.depot = g.newDepot(t, "1")
	g.catalog = g.newCatalog(t, 10, "Bosch")
	g.cust = g.sicil(t, "C1", "MUSTERI")
	g.p1 = g.part(t, "P1", "Oil filter", "")
	b := g.posted(t, docBody("SALES_WAYBILL", g.depot, g.cust, "2026-03-01", "", lineJSON(g.p1, "1", "10", "")))
	assert.Equal(t, a.no(), b.no(), "numbers are per company")

	assert.Equal(t, http.StatusNotFound, g.do(t, http.MethodGet, g.path("/documents/"+idStr(a.ID)), g.token, "").Code)
	assert.Equal(t, http.StatusForbidden, g.do(t, http.MethodGet, f.path("/documents/"+idStr(a.ID)), g.token, "").Code)
	rec := g.postRaw(t, g.token, a.ID, a.Version, "")
	assert.Equal(t, http.StatusNotFound, rec.Code)
	rec = g.req(t, http.MethodPost, g.path("/documents/"+idStr(a.ID)+"/cancel"), g.token, `{"reason":"not mine"}`,
		map[string]string{"If-Match": `"` + strconv.Itoa(a.Version) + `"`, "Idempotency-Key": uuid.NewString()})
	assert.Equal(t, http.StatusNotFound, rec.Code)

	rec = g.createRaw(t, g.token, docBody("SALES_RETURN_WAYBILL", g.depot, g.cust, "2026-03-02", fmt.Sprintf(`,"linkedDocumentId":%d`, a.ID),
		lineJSON(g.p1, "1", "10", fmt.Sprintf(`,"originLineId":%d`, a.Lines[0].ID))))
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_LINKED_DOCUMENT_INVALID", errCode(t, rec))
	rec = g.createRaw(t, g.token, docBody("SALES_WAYBILL", g.depot, g.cust, "2026-03-01", "", lineJSON(f.p1, "1", "10", "")))
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_PART_NOT_FOUND", errCode(t, rec))

	grid := decode[struct {
		Rows []struct {
			ID uint `json:"id"`
		} `json:"rows"`
		TotalCount      int `json:"totalCount"`
		UnfilteredCount int `json:"unfilteredCount"`
	}](t, g.post(t, g.path("/documents/grid"), g.token, `{"startRow":0,"endRow":100}`))
	require.Len(t, grid.Rows, 1)
	assert.Equal(t, b.ID, grid.Rows[0].ID)
	assert.Equal(t, 1, grid.UnfilteredCount)

	search := decode[struct {
		Documents []struct {
			ID   uint   `json:"id"`
			Kind string `json:"kind"`
		} `json:"documents"`
	}](t, g.do(t, http.MethodGet, g.path("/search?q="+a.no()), g.token, ""))
	require.Len(t, search.Documents, 1)
	assert.Equal(t, b.ID, search.Documents[0].ID)
	assert.Equal(t, "DOCUMENT", search.Documents[0].Kind)
}

func TestDocuments_SweeperReleasesExpiredReservations(t *testing.T) {
	f := newDocs(t)
	d := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "4", "10", "")))
	n, err := f.app.Sweeper.Sweep(t.Context(), time.Now())
	require.NoError(t, err)
	assert.Zero(t, n, "live reservations stay")
	assert.Equal(t, "4", f.reserved(t, f.p1, f.depot))

	later := time.Now().Add(49 * time.Hour)
	n, err = f.app.Sweeper.Sweep(t.Context(), later)
	require.NoError(t, err)
	assert.Equal(t, 1, n)
	assert.Equal(t, "0", f.reserved(t, f.p1, f.depot))
	n, err = f.app.Sweeper.Sweep(t.Context(), later)
	require.NoError(t, err)
	assert.Zero(t, n, "a rerun releases nothing")
	noDrift(t)

	rel := outboxEnvelopes(t, "StockReleased", d.ID)
	require.Len(t, rel, 1)
	validateEvent(t, rel[0])
	assert.Contains(t, string(rel[0]), `"EXPIRED"`)

	g := f.get(t, d.ID)
	assert.Equal(t, "DRAFT", g.Status)
	assert.Nil(t, g.Reservation)
	assert.Equal(t, "0", g.Lines[0].ReservedQty)
	assert.Len(t, g.warnings("STOCK_SHORT"), 1)

	p := f.postDoc(t, g)
	assert.Equal(t, "6", f.onHand(t, f.p1, f.depot), "a post after expiry just moves the stock")
	assert.Len(t, outboxPayloads(t, "StockReleased", p.ID), 1, "nothing left to release")
}

func TestDocuments_RelayKeepsDocumentOrder(t *testing.T) {
	f := newDocs(t)
	rec := &docFailOnce{}
	f.app.Relay.Register(rec)
	d := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "4", "10", "")))
	f.postDoc(t, d)

	for i := 0; i < 5; i++ {
		f.relay(t)
	}
	var types []string
	for _, e := range rec.envelopes {
		h := decodeBytes[struct {
			Type          string `json:"type"`
			AggregateType string `json:"aggregateType"`
			AggregateID   string `json:"aggregateId"`
		}](t, e)
		if h.AggregateType == "document" && h.AggregateID == idStr(d.ID) {
			types = append(types, h.Type)
		}
	}
	assert.Equal(t, []string{"StockReserved", "StockReleased", "DocumentPosted"}, types,
		"a failed delivery holds back the later events of the same document")
	var exported int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM outbox_deliveries WHERE consumer = 'document-export'`).Scan(&exported).Error)
	assert.EqualValues(t, 1, exported)
}

// docFailOnce records every envelope and fails the first delivery of a document event.
type docFailOnce struct {
	envelopes [][]byte
	failed    bool
}

func (r *docFailOnce) Name() string          { return "test-doc-fail-once" }
func (r *docFailOnce) Handles(_ string) bool { return true }

func (r *docFailOnce) Handle(_ context.Context, _ *repository.Q, env services.Envelope) error {
	if env.AggregateType == "document" && !r.failed {
		r.failed = true
		return errors.New("consumer down")
	}
	b, err := json.Marshal(env)
	r.envelopes = append(r.envelopes, b)
	return err
}

func TestDocuments_ListMembers(t *testing.T) {
	f := newDocs(t)
	rec := f.do(t, http.MethodGet, f.path("/members"), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	var members []struct {
		UserID uint   `json:"userId"`
		Name   string `json:"name"`
		Email  string `json:"email"`
	}
	require.NoError(t, json.Unmarshal(rec.Body.Bytes(), &members))
	require.Len(t, members, 1)
	assert.Equal(t, f.clerkID, members[0].UserID)
	assert.Equal(t, "clerk@x.com", members[0].Email)
}
