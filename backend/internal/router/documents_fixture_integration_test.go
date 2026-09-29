//go:build integration

package router_test

import (
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"strconv"
	"strings"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/models"

	"github.com/google/uuid"
	"github.com/stretchr/testify/require"
)

type docLine struct {
	ID             uint    `json:"id"`
	LineNo         int     `json:"lineNo"`
	PartID         *uint   `json:"partId"`
	StockNo        string  `json:"stockNo"`
	Description    *string `json:"description"`
	Qty            string  `json:"qty"`
	UnitPrice      string  `json:"unitPrice"`
	Discount1Pct   string  `json:"discount1Pct"`
	Discount2Pct   string  `json:"discount2Pct"`
	VatPct         string  `json:"vatPct"`
	GrossAmount    string  `json:"grossAmount"`
	DiscountAmount string  `json:"discountAmount"`
	NetAmount      string  `json:"netAmount"`
	VatAmount      string  `json:"vatAmount"`
	LineTotal      string  `json:"lineTotal"`
	OriginLineID   *uint   `json:"originLineId"`
	BilledLineID   *uint   `json:"billedLineId"`
	OnHand         string  `json:"onHand"`
	Available      string  `json:"available"`
	ReservedQty    string  `json:"reservedQty"`
	ReturnedQty    *string `json:"returnedQty"`
	StockByDepot   []struct {
		DepotID   uint   `json:"depotId"`
		DepotCode string `json:"depotCode"`
		OnHand    string `json:"onHand"`
		Reserved  string `json:"reserved"`
		Available string `json:"available"`
	} `json:"stockByDepot"`
}

type docWarning struct {
	Code        string  `json:"code"`
	LineNo      *int    `json:"lineNo"`
	PartID      *uint   `json:"partId"`
	DepotID     *uint   `json:"depotId"`
	Requested   *string `json:"requested"`
	Available   *string `json:"available"`
	OnHandAfter *string `json:"onHandAfter"`
}

type docJSON struct {
	ID                   uint    `json:"id"`
	OrgID                uint    `json:"orgId"`
	DocumentNo           *string `json:"documentNo"`
	Type                 string  `json:"type"`
	Status               string  `json:"status"`
	DepotID              uint    `json:"depotId"`
	SicilID              uint    `json:"sicilId"`
	SicilName            string  `json:"sicilName"`
	DocumentDate         string  `json:"documentDate"`
	DueDate              *string `json:"dueDate"`
	SalesRepUserID       *uint   `json:"salesRepUserId"`
	SalesRepName         *string `json:"salesRepName"`
	LinkedDocumentID     *uint   `json:"linkedDocumentId"`
	LinkedDocumentNo     *string `json:"linkedDocumentNo"`
	PriceDirection       *string `json:"priceDirection"`
	WaybillDocumentID    *uint   `json:"waybillDocumentId"`
	WaybillDocumentNo    *string `json:"waybillDocumentNo"`
	BilledByDocumentID   *uint   `json:"billedByDocumentId"`
	DependentDocumentIDs []uint  `json:"dependentDocumentIds"`
	Currency             string  `json:"currency"`
	GrossTotal           string  `json:"grossTotal"`
	DiscountTotal        string  `json:"discountTotal"`
	NetTotal             string  `json:"netTotal"`
	VatTotal             string  `json:"vatTotal"`
	GrandTotal           string  `json:"grandTotal"`
	VatBreakdown         []struct {
		VatPct string `json:"vatPct"`
		Base   string `json:"base"`
		Vat    string `json:"vat"`
	} `json:"vatBreakdown"`
	Lines       []docLine `json:"lines"`
	Reservation *struct {
		ExpiresAt string `json:"expiresAt"`
	} `json:"reservation"`
	Effects struct {
		Ledger []struct {
			Role        *string `json:"role"`
			AccountCode string  `json:"accountCode"`
			Side        string  `json:"side"`
			Amount      string  `json:"amount"`
		} `json:"ledger"`
		Stock []struct {
			DepotID  uint   `json:"depotId"`
			QtyDelta string `json:"qtyDelta"`
		} `json:"stock"`
		CreditUsedPctAfter *int `json:"creditUsedPctAfter"`
	} `json:"effects"`
	Warnings       []docWarning `json:"warnings"`
	CreditOverride *struct {
		Reason string `json:"reason"`
		UserID uint   `json:"userId"`
		At     string `json:"at"`
		Excess string `json:"excess"`
	} `json:"creditOverride"`
	JournalEntryID  *uint   `json:"journalEntryId"`
	ReversalEntryID *uint   `json:"reversalEntryId"`
	PostedAt        *string `json:"postedAt"`
	CancelDate      *string `json:"cancelDate"`
	CancelReason    *string `json:"cancelReason"`
	Version         int     `json:"version"`
}

func (d docJSON) no() string {
	if d.DocumentNo == nil {
		return ""
	}
	return *d.DocumentNo
}

func (d docJSON) warnings(code string) []docWarning {
	var out []docWarning
	for _, w := range d.Warnings {
		if w.Code == code {
			out = append(out, w)
		}
	}
	return out
}

// docFix is a company with the F3 account roles, a customer (30 days, 10 % + 5 % discounts) and a supplier,
// two parts (VAT 20 and 10) and 10 units of P1 in depot 1.
type docFix struct {
	*stockFix
	cust, supp       uint
	p1, p2           uint
	accounts         map[string]uint
	clerkID, adminID uint
}

func newDocs(t *testing.T) *docFix {
	t.Helper()
	f := &docFix{stockFix: newStock(t), accounts: map[string]uint{}}
	for code, typ := range map[string]string{"600": "INCOME", "610": "EXPENSE", "611": "EXPENSE", "153": "ASSET",
		"191": "ASSET", "391": "LIABILITY"} {
		f.accounts[code] = f.account(t, code, typ)
	}
	roles := []string{}
	for role, code := range map[string]string{"SALES": "600", "SALES_RETURNS": "610", "SALES_DISCOUNTS": "611",
		"PURCHASES": "153", "PURCHASE_RETURNS": "153", "PRICE_DIFFERENCE": "600", "OUTPUT_VAT": "391", "INPUT_VAT": "191"} {
		roles = append(roles, fmt.Sprintf(`{"role":"%s","accountId":%d}`, role, f.accounts[code]))
	}
	f.setRoles(t, "["+strings.Join(roles, ",")+"]")
	f.cust = f.sicil(t, "C1", "MUSTERI")
	f.supp = f.sicil(t, "S1", "FIRMA")
	f.clerkID = f.userID
	require.NoError(t, testDB.Raw(`SELECT id FROM users WHERE email = 'admin@x.com'`).Scan(&f.adminID).Error)
	require.NoError(t, testDB.Exec(`UPDATE sicils SET gun = '30', iskonto = 10, iskonto2 = 5, sales_rep_user_id = ?
		WHERE id = ?`, f.clerkID, f.cust).Error)
	f.p1 = f.part(t, "P1", "Oil filter", `,"vatPct":"20","initialPrice":{"price":"100","validFrom":"2026-01-01"}`)
	f.p2 = f.part(t, "P2", "Brake pad", `,"vatPct":"10"`)
	f.move(t, f.p1, f.depot, "IN", "10", "2026-01-02")
	return f
}

func (f *docFix) setCreditLimit(t *testing.T, sicil uint, limit string) {
	t.Helper()
	require.NoError(t, testDB.Exec(`UPDATE sicils SET credit_limit = ?::numeric WHERE id = ?`, limit, sicil).Error)
}

// body renders a DocumentWrite; lines are raw JSON objects.
func docBody(typ string, depot, sicil uint, date string, extra string, lines ...string) string {
	return fmt.Sprintf(`{"type":"%s","depotId":%d,"sicilId":%d,"documentDate":"%s"%s,"lines":[%s]}`,
		typ, depot, sicil, date, extra, strings.Join(lines, ","))
}

func lineJSON(part uint, qty, price string, extra string) string {
	return fmt.Sprintf(`{"partId":%d,"qty":"%s","unitPrice":"%s"%s}`, part, qty, price, extra)
}

func (f *docFix) createRaw(t *testing.T, token, body string) *httptest.ResponseRecorder {
	t.Helper()
	return f.post(t, f.path("/documents"), token, body)
}

func (f *docFix) create(t *testing.T, body string) docJSON {
	t.Helper()
	rec := f.createRaw(t, f.token, body)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	require.Equal(t, `"`+strconv.Itoa(decode[docJSON](t, rec).Version)+`"`, rec.Header().Get("ETag"))
	return decode[docJSON](t, rec)
}

func (f *docFix) get(t *testing.T, id uint) docJSON {
	t.Helper()
	rec := f.do(t, http.MethodGet, f.path("/documents/"+idStr(id)), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[docJSON](t, rec)
}

func (f *docFix) update(t *testing.T, id uint, version int, body string) *httptest.ResponseRecorder {
	t.Helper()
	return f.req(t, http.MethodPut, f.path("/documents/"+idStr(id)), f.token, body, map[string]string{"If-Match": `"` + strconv.Itoa(version) + `"`})
}

func (f *docFix) postRaw(t *testing.T, token string, id uint, version int, body string) *httptest.ResponseRecorder {
	t.Helper()
	return f.req(t, http.MethodPost, f.path("/documents/"+idStr(id)+"/post"), token, body,
		map[string]string{"If-Match": `"` + strconv.Itoa(version) + `"`, "Idempotency-Key": uuid.NewString()})
}

func (f *docFix) postDoc(t *testing.T, d docJSON) docJSON {
	t.Helper()
	rec := f.postRaw(t, f.token, d.ID, d.Version, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[docJSON](t, rec)
}

func (f *docFix) cancelRaw(t *testing.T, id uint, version int, reason string) *httptest.ResponseRecorder {
	t.Helper()
	return f.req(t, http.MethodPost, f.path("/documents/"+idStr(id)+"/cancel"), f.token, `{"reason":"`+reason+`"}`,
		map[string]string{"If-Match": `"` + strconv.Itoa(version) + `"`, "Idempotency-Key": uuid.NewString()})
}

func (f *docFix) cancel(t *testing.T, d docJSON) docJSON {
	t.Helper()
	rec := f.cancelRaw(t, d.ID, d.Version, "wrong customer")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[docJSON](t, rec)
}

// posted creates and posts a document.
func (f *docFix) posted(t *testing.T, body string) docJSON {
	t.Helper()
	return f.postDoc(t, f.create(t, body))
}

type postingRow struct {
	Code    string       `gorm:"column:code"`
	Side    string       `gorm:"column:side"`
	Amount  models.Money `gorm:"column:amount"`
	DueDate *string      `gorm:"column:due_date"`
}

// entryPostings reads the postings of an entry, card accounts as "C".
func entryPostings(t *testing.T, entryID uint) []postingRow {
	t.Helper()
	var rows []postingRow
	require.NoError(t, testDB.Raw(`SELECT CASE WHEN a.sicil_id IS NULL THEN a.code ELSE 'C' END AS code, p.side, p.amount,
			p.due_date::text AS due_date
		FROM postings p JOIN accounts a ON a.id = p.account_id WHERE p.entry_id = ? ORDER BY p.line_no`, entryID).Scan(&rows).Error)
	return rows
}

func postingSet(rows []postingRow) map[string]string {
	out := map[string]string{}
	for _, r := range rows {
		out[r.Side[:1]+" "+r.Code] = r.Amount.String()
	}
	return out
}

func (f *docFix) reserved(t *testing.T, part, depot uint) string {
	t.Helper()
	var s string
	require.NoError(t, testDB.Raw(`SELECT coalesce((SELECT trim_scale(reserved)::text FROM stock_levels WHERE part_id = ? AND depot_id = ?), '0')`,
		part, depot).Scan(&s).Error)
	return s
}

// noDrift asserts the stock and reservation projections equal their sources.
func noDrift(t *testing.T) {
	t.Helper()
	var n int64
	require.NoError(t, testDB.Raw(`SELECT (SELECT count(*) FROM stock_level_drift) + (SELECT count(*) FROM stock_reservation_drift)`).Scan(&n).Error)
	require.Zero(t, n, "stock projections drift")
}

func outboxPayloads(t *testing.T, eventType string, docID uint) []json.RawMessage {
	t.Helper()
	var rows []string
	require.NoError(t, testDB.Raw(`SELECT payload::text FROM outbox WHERE event_type = ? AND aggregate_type = 'document'
		AND aggregate_id = ? ORDER BY seq`, eventType, idStr(docID)).Scan(&rows).Error)
	out := make([]json.RawMessage, len(rows))
	for i, r := range rows {
		out[i] = json.RawMessage(r)
	}
	return out
}

// outboxEnvelopes renders the document's outbox rows as envelopes for schema validation.
func outboxEnvelopes(t *testing.T, eventType string, docID uint) [][]byte {
	t.Helper()
	var rows []string
	require.NoError(t, testDB.Raw(`SELECT json_build_object('id', id, 'type', event_type, 'version', event_version,
			'occurredAt', to_char(occurred_at AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"'), 'orgId', org_id,
			'depotId', depot_id, 'aggregateType', aggregate_type, 'aggregateId', aggregate_id, 'actorUserId', actor_user_id,
			'payload', payload)::text
		FROM outbox WHERE event_type = ? AND aggregate_type = 'document' AND aggregate_id = ? ORDER BY seq`, eventType, idStr(docID)).Scan(&rows).Error)
	out := make([][]byte, len(rows))
	for i, r := range rows {
		out[i] = []byte(r)
	}
	return out
}
