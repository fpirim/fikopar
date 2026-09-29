package services

import (
	"context"
	"fmt"
	"strconv"
	"strings"
	"time"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

// reservationTTL is how long a draft holds stock after its last save (D8).
const reservationTTL = 48 * time.Hour

type DocumentService struct {
	store *repository.Store
	now   func() time.Time
}

func NewDocumentService(store *repository.Store) *DocumentService {
	return &DocumentService{store: store, now: time.Now}
}

type DocumentLineWrite struct {
	PartID       *uint        `json:"partId"`
	Description  *string      `json:"description"`
	Qty          *models.Dec4 `json:"qty"`
	UnitPrice    *models.Dec4 `json:"unitPrice"`
	Discount1Pct *models.Dec4 `json:"discount1Pct"`
	Discount2Pct *models.Dec4 `json:"discount2Pct"`
	VatPct       *models.Dec4 `json:"vatPct"`
	OriginLineID *uint        `json:"originLineId"`
	BilledLineID *uint        `json:"billedLineId"`
}

type DocumentWrite struct {
	Type              string              `json:"type"`
	DepotID           uint                `json:"depotId"`
	SicilID           uint                `json:"sicilId"`
	DocumentDate      models.Date         `json:"documentDate"`
	ShipDate          *models.Date        `json:"shipDate"`
	DueDate           *models.Date        `json:"dueDate"`
	SalesRepUserID    *uint               `json:"salesRepUserId"`
	LinkedDocumentID  *uint               `json:"linkedDocumentId"`
	PriceDirection    *string             `json:"priceDirection"`
	WaybillDocumentID *uint               `json:"waybillDocumentId"`
	Description       string              `json:"description"`
	Lines             []DocumentLineWrite `json:"lines"`
}

func lineField(i int, name string) string { return fmt.Sprintf("lines[%d].%s", i, name) }

func badField(field string) error { return apierror.WithField(apierror.InvalidRequest, field) }

func percentOK(p *models.Dec4) bool { return p == nil || (*p >= 0 && *p <= pctHundred) }

// validateWrite checks what needs no database: type, dates, direction and line shapes (400).
func validateWrite(in DocumentWrite) error {
	k, ok := docKinds[in.Type]
	switch {
	case !ok:
		return badField("type")
	case in.SicilID == 0:
		return badField("sicilId")
	case in.DocumentDate.IsZero():
		return badField("documentDate")
	case in.DueDate != nil && in.DueDate.Before(in.DocumentDate.Time):
		return badField("dueDate")
	case (in.Type == typePriceDifference) != (in.PriceDirection != nil):
		return badField("priceDirection")
	case in.PriceDirection != nil && *in.PriceDirection != increase && *in.PriceDirection != decrease:
		return badField("priceDirection")
	case in.LinkedDocumentID != nil && !k.isReturn() && in.Type != typePriceDifference:
		return badField("linkedDocumentId")
	case len(in.Description) > 2000:
		return badField("description")
	}
	for i, l := range in.Lines {
		switch {
		case l.Qty == nil || *l.Qty <= 0:
			return badField(lineField(i, "qty"))
		case l.UnitPrice == nil || *l.UnitPrice < 0:
			return badField(lineField(i, "unitPrice"))
		case !percentOK(l.Discount1Pct):
			return badField(lineField(i, "discount1Pct"))
		case !percentOK(l.Discount2Pct):
			return badField(lineField(i, "discount2Pct"))
		case !percentOK(l.VatPct):
			return badField(lineField(i, "vatPct"))
		}
		if in.Type == typePriceDifference {
			if err := validatePriceDifferenceLine(i, l); err != nil {
				return err
			}
			continue
		}
		switch {
		case l.PartID == nil:
			return badField(lineField(i, "partId"))
		case l.Description != nil && *l.Description != "":
			return badField(lineField(i, "description"))
		}
	}
	return nil
}

// A price-difference line is amount only: qty 1, no discounts, VAT required without an origin line.
func validatePriceDifferenceLine(i int, l DocumentLineWrite) error {
	switch {
	case *l.Qty != dec4One:
		return badField(lineField(i, "qty"))
	case l.Discount1Pct != nil && *l.Discount1Pct != 0:
		return badField(lineField(i, "discount1Pct"))
	case l.Discount2Pct != nil && *l.Discount2Pct != 0:
		return badField(lineField(i, "discount2Pct"))
	case l.BilledLineID != nil:
		return badField(lineField(i, "billedLineId"))
	case l.OriginLineID == nil && l.PartID != nil:
		return badField(lineField(i, "partId"))
	case l.OriginLineID == nil && l.VatPct == nil:
		return badField(lineField(i, "vatPct"))
	case l.Description != nil && len(*l.Description) > 200:
		return badField(lineField(i, "description"))
	}
	return nil
}

const dec4One = models.Dec4(10_000)

// draft is a validated write with defaults applied and amounts computed.
type draft struct {
	row       repository.DocumentRow
	lines     []repository.LineRow
	card      *repository.DocumentCard
	direction string
}

func (s *DocumentService) Create(ctx context.Context, orgID uint, in DocumentWrite) (*models.Document, error) {
	if err := validateWrite(in); err != nil {
		return nil, err
	}
	var out *models.Document
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		d, err := s.prepare(ctx, q, orgID, in, 0)
		if err != nil {
			return err
		}
		if err := lockLevels(q, orgID, 0, d.row.DepotID, d.lines); err != nil {
			return err
		}
		if err := q.InsertDocument(&d.row, actorFrom(ctx).userID()); err != nil {
			return documentErr(err, nil)
		}
		if err := s.writeLines(ctx, q, d); err != nil {
			return err
		}
		out, err = s.read(q, orgID, d.row.ID)
		if err != nil {
			return err
		}
		return audit(ctx, q, orgID, "document.created", "document", out.ID, nil, out)
	})
	return out, err
}

func (s *DocumentService) Update(ctx context.Context, orgID, id uint, version int, in DocumentWrite) (*models.Document, error) {
	if err := validateWrite(in); err != nil {
		return nil, err
	}
	var out *models.Document
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		cur, err := lockDraft(q, orgID, id, version)
		if err != nil {
			return err
		}
		before, err := s.read(q, orgID, id)
		if err != nil {
			return err
		}
		d, err := s.prepare(ctx, q, orgID, in, id)
		if err != nil {
			return err
		}
		if err := lockLevels(q, orgID, id, d.row.DepotID, d.lines); err != nil {
			return err
		}
		if err := release(ctx, q, cur, "UPDATED"); err != nil {
			return err
		}
		d.row.OrgID, d.row.ID = orgID, id
		if err := q.UpdateDraft(&d.row); err != nil {
			return documentErr(err, nil)
		}
		if err := q.DeleteDocumentLines(orgID, id); err != nil {
			return documentErr(err, nil)
		}
		if err := s.writeLines(ctx, q, d); err != nil {
			return err
		}
		out, err = s.read(q, orgID, id)
		if err != nil {
			return err
		}
		return audit(ctx, q, orgID, "document.updated", "document", id, before, out)
	})
	return out, err
}

func (s *DocumentService) Delete(ctx context.Context, orgID, id uint, version int) error {
	return s.store.InTx(ctx, func(q *repository.Q) error {
		cur, err := lockDraft(q, orgID, id, version)
		if err != nil {
			return err
		}
		before, err := s.read(q, orgID, id)
		if err != nil {
			return err
		}
		if err := lockLevels(q, orgID, id, cur.DepotID, nil); err != nil {
			return err
		}
		if err := release(ctx, q, cur, "DELETED"); err != nil {
			return err
		}
		if err := q.DeleteDocument(orgID, id); err != nil {
			return documentErr(err, nil)
		}
		return audit(ctx, q, orgID, "document.deleted", "document", id, before, nil)
	})
}

func lockDocument(q *repository.Q, orgID, id uint) (*repository.DocumentRow, error) {
	d, err := q.LockDocument(orgID, id)
	if repository.IsNotFound(err) {
		return nil, apierror.DocumentNotFound
	}
	return d, err
}

func lockDraft(q *repository.Q, orgID, id uint, version int) (*repository.DocumentRow, error) {
	d, err := lockDocument(q, orgID, id)
	switch {
	case err != nil:
		return nil, err
	case d.Status != statusDraft:
		return nil, apierror.DocumentNotDraft
	case d.Version != version:
		return nil, apierror.VersionConflict
	}
	return d, nil
}

// prepare resolves the write against the company: references (422), defaults and amounts. self is the draft being
// updated (0 on create).
func (s *DocumentService) prepare(ctx context.Context, q *repository.Q, orgID uint, in DocumentWrite, self uint) (*draft, error) {
	if err := q.LockLedger(orgID, false); err != nil {
		return nil, err
	}
	if err := yearOpen(q, orgID, in.DocumentDate); err != nil {
		return nil, err
	}
	if _, err := depotRef(q, orgID, in.DepotID); err != nil {
		return nil, err
	}
	card, err := q.DocumentCard(orgID, in.SicilID)
	if repository.IsNotFound(err) {
		return nil, apierror.UnknownRef(apierror.SicilNotFound, "sicilId")
	}
	if err != nil {
		return nil, err
	}
	k := docKinds[in.Type]
	if k.isReturn() && in.LinkedDocumentID == nil && in.WaybillDocumentID != nil {
		if w, err := q.GetDocument(orgID, *in.WaybillDocumentID); err == nil {
			in.LinkedDocumentID = w.LinkedDocumentID
		}
	}
	d := &draft{card: card, direction: k.direction, row: repository.DocumentRow{
		OrgID: orgID, DepotID: in.DepotID, Type: in.Type, SicilID: in.SicilID, DocumentDate: in.DocumentDate,
		ShipDate: in.ShipDate, DueDate: in.DueDate, LinkedDocumentID: in.LinkedDocumentID, PriceDirection: in.PriceDirection,
		WaybillID: in.WaybillDocumentID,
	}}
	if in.Description != "" {
		d.row.Description = &in.Description
	}
	if d.row.DueDate == nil {
		due := models.Date{Time: in.DocumentDate.AddDate(0, 0, paymentTermDays(card.Gun))}
		d.row.DueDate = &due
	}
	if d.row.SalesRepUserID, err = salesRep(q, orgID, in.SalesRepUserID, card.SalesRepUserID); err != nil {
		return nil, err
	}
	origin, err := linkedOriginal(q, orgID, in, k, card, d)
	if err != nil {
		return nil, err
	}
	waybill, err := waybillLines(q, orgID, in, k, card, self)
	if err != nil {
		return nil, err
	}
	lines := in.Lines
	if waybill != nil && len(lines) == 0 {
		lines = copyWaybill(waybill, in.Type)
	}
	if err := s.buildLines(q, orgID, d, lines, origin, waybill); err != nil {
		return nil, err
	}
	return d, nil
}

// paymentTermDays reads the card's gun (days); blank or not a number is 0.
func paymentTermDays(gun string) int {
	n, err := strconv.Atoi(strings.TrimSpace(gun))
	if err != nil || n < 0 {
		return 0
	}
	return n
}

// salesRep is the given member, else the card's salesperson while a member of the company.
func salesRep(q *repository.Q, orgID uint, given, cards *uint) (*uint, error) {
	if given != nil {
		ok, err := q.IsMember(orgID, *given)
		if err != nil {
			return nil, err
		}
		if !ok {
			return nil, apierror.UnknownRef(apierror.UserNotFound, "salesRepUserId")
		}
		return given, nil
	}
	if cards == nil {
		return nil, nil
	}
	ok, err := q.IsMember(orgID, *cards)
	if err != nil || !ok {
		return nil, err
	}
	return cards, nil
}

// linkedOriginal checks the original of a return or price difference and returns its lines by id.
func linkedOriginal(q *repository.Q, orgID uint, in DocumentWrite, k docKind, card *repository.DocumentCard, d *draft) (map[uint]repository.LineRow, error) {
	if !k.isReturn() && in.Type != typePriceDifference {
		return nil, nil
	}
	invalid := apierror.UnknownRef(apierror.LinkedDocumentInvalid, "linkedDocumentId")
	if in.LinkedDocumentID == nil {
		return nil, invalid
	}
	o, err := q.GetDocument(orgID, *in.LinkedDocumentID)
	if repository.IsNotFound(err) {
		return nil, invalid
	}
	if err != nil {
		return nil, err
	}
	ok := o.Status == statusPosted && o.SicilID == card.ID
	if k.isReturn() {
		ok = ok && contains(k.originals, o.Type) && o.WaybillID == nil
	} else {
		ok = ok && contains(invoiceTypes, o.Type) && o.DepotID == in.DepotID
		d.direction = docKinds[o.Type].direction
	}
	if !ok {
		return nil, invalid
	}
	lines, err := q.DocumentLines(orgID, o.ID)
	if err != nil {
		return nil, err
	}
	byID := make(map[uint]repository.LineRow, len(lines))
	for _, l := range lines {
		byID[l.ID] = l
	}
	return byID, nil
}

// waybillLines checks the waybill a document bills and returns its lines in order.
func waybillLines(q *repository.Q, orgID uint, in DocumentWrite, k docKind, card *repository.DocumentCard, self uint) ([]repository.LineRow, error) {
	if in.WaybillDocumentID == nil {
		return nil, nil
	}
	invalid := apierror.UnknownRef(apierror.WaybillInvalid, "waybillDocumentId")
	if k.bills == "" {
		return nil, invalid
	}
	w, err := q.GetDocument(orgID, *in.WaybillDocumentID)
	if repository.IsNotFound(err) {
		return nil, invalid
	}
	if err != nil {
		return nil, err
	}
	if w.Type != k.bills || w.SicilID != card.ID || w.DepotID != in.DepotID || w.Status == statusCancelled {
		return nil, invalid
	}
	by, err := q.BilledBy(orgID, w.ID)
	if err != nil {
		return nil, err
	}
	if by != nil && *by != self {
		return nil, apierror.WaybillBilled
	}
	return q.DocumentLines(orgID, w.ID)
}

// copyWaybill bills every waybill line as it is; a return keeps the origin of the return waybill's line.
func copyWaybill(lines []repository.LineRow, typ string) []DocumentLineWrite {
	out := make([]DocumentLineWrite, len(lines))
	for i, l := range lines {
		id, qty, price, d1, d2, vat := l.ID, l.Qty, l.UnitPrice, l.Discount1Pct, l.Discount2Pct, l.VatPct
		out[i] = DocumentLineWrite{PartID: l.PartID, Qty: &qty, UnitPrice: &price, Discount1Pct: &d1, Discount2Pct: &d2,
			VatPct: &vat, BilledLineID: &id}
		if docKinds[typ].isReturn() {
			out[i].OriginLineID = l.OriginLineID
		}
	}
	return out
}

// buildLines resolves parts, origins and billed lines, applies defaults and computes amounts and totals.
func (s *DocumentService) buildLines(q *repository.Q, orgID uint, d *draft, in []DocumentLineWrite,
	origin map[uint]repository.LineRow, waybill []repository.LineRow) error {
	parts, err := documentParts(q, orgID, in)
	if err != nil {
		return err
	}
	billable := make(map[uint]repository.LineRow, len(waybill))
	for _, l := range waybill {
		billable[l.ID] = l
	}
	k := docKinds[d.row.Type]
	d.lines = make([]repository.LineRow, len(in))
	d.row.GrossTotal, d.row.DiscountTotal, d.row.NetTotal, d.row.VatTotal, d.row.GrandTotal = 0, 0, 0, 0, 0
	for i, l := range in {
		row := repository.LineRow{LineNo: i + 1, PartID: l.PartID, Qty: *l.Qty, UnitPrice: *l.UnitPrice,
			OriginLineID: l.OriginLineID, BilledLineID: l.BilledLineID}
		if d.row.Type == typePriceDifference {
			if err := priceDifferenceLine(i, l, origin, &row); err != nil {
				return err
			}
		} else {
			p, ok := parts[*l.PartID]
			switch {
			case !ok:
				return apierror.UnknownRef(apierror.PartNotFound, lineField(i, "partId"))
			case !p.IsActive:
				return apierror.UnknownRef(apierror.PartInactive, lineField(i, "partId"))
			}
			row.VatPct = pick(l.VatPct, p.VatPct)
			row.Discount1Pct = pick(l.Discount1Pct, d.card.Iskonto)
			row.Discount2Pct = pick(l.Discount2Pct, d.card.Iskonto2)
			if err := lineLinks(i, k, l, origin, billable, d.row.WaybillID != nil); err != nil {
				return err
			}
		}
		row.GrossAmount, row.DiscountAmount, row.NetAmount, row.VatAmount = lineAmounts(row.Qty, row.UnitPrice,
			row.Discount1Pct, row.Discount2Pct, row.VatPct)
		d.row.GrossTotal += row.GrossAmount
		d.row.DiscountTotal += row.DiscountAmount
		d.row.NetTotal += row.NetAmount
		d.row.VatTotal += row.VatAmount
		d.lines[i] = row
	}
	d.row.GrandTotal = d.row.NetTotal + d.row.VatTotal
	return nil
}

func pick(v *models.Dec4, def models.Dec4) models.Dec4 {
	if v != nil {
		return *v
	}
	return def
}

func documentParts(q *repository.Q, orgID uint, in []DocumentLineWrite) (map[uint]repository.DocumentPart, error) {
	var ids []uint
	for _, l := range in {
		if l.PartID != nil {
			ids = append(ids, *l.PartID)
		}
	}
	out := map[uint]repository.DocumentPart{}
	if len(ids) == 0 {
		return out, nil
	}
	rows, err := q.DocumentParts(orgID, ids)
	for _, p := range rows {
		out[p.ID] = p
	}
	return out, err
}

// priceDifferenceLine takes the part and default VAT of its origin line, which must be a line of the original.
func priceDifferenceLine(i int, l DocumentLineWrite, origin map[uint]repository.LineRow, row *repository.LineRow) error {
	row.Description = l.Description
	if l.OriginLineID == nil {
		row.VatPct = *l.VatPct
		return nil
	}
	o, ok := origin[*l.OriginLineID]
	if !ok {
		return apierror.UnknownRef(apierror.LinkedDocumentInvalid, lineField(i, "originLineId"))
	}
	if l.PartID != nil && (o.PartID == nil || *l.PartID != *o.PartID) {
		return badField(lineField(i, "partId"))
	}
	row.PartID = o.PartID
	row.VatPct = pick(l.VatPct, o.VatPct)
	return nil
}

// lineLinks: a return line takes back a line of its original with the same part; a bill line bills a line of its
// waybill with the same part; other lines link nothing.
func lineLinks(i int, k docKind, l DocumentLineWrite, origin, billable map[uint]repository.LineRow, bills bool) error {
	if k.isReturn() {
		o, ok := origin[derefUint(l.OriginLineID)]
		if !ok || o.PartID == nil || *o.PartID != *l.PartID {
			return apierror.UnknownRef(apierror.LinkedDocumentInvalid, lineField(i, "originLineId"))
		}
	} else if l.OriginLineID != nil {
		return apierror.UnknownRef(apierror.LinkedDocumentInvalid, lineField(i, "originLineId"))
	}
	if !bills {
		if l.BilledLineID != nil {
			return apierror.UnknownRef(apierror.WaybillInvalid, lineField(i, "billedLineId"))
		}
		return nil
	}
	w, ok := billable[derefUint(l.BilledLineID)]
	if !ok || w.PartID == nil || *w.PartID != *l.PartID {
		return apierror.UnknownRef(apierror.WaybillInvalid, lineField(i, "billedLineId"))
	}
	return nil
}

func derefUint(v *uint) uint {
	if v == nil {
		return 0
	}
	return *v
}

// writeLines inserts the lines and, for a draft that moves stock out, reserves them (StockReserved).
func (s *DocumentService) writeLines(ctx context.Context, q *repository.Q, d *draft) error {
	for i := range d.lines {
		d.lines[i].DocumentID = d.row.ID
		if err := q.InsertDocumentLine(d.row.OrgID, &d.lines[i]); err != nil {
			return documentErr(err, nil)
		}
	}
	if !reserves(d.row.Type, d.row.WaybillID) || len(d.lines) == 0 {
		return nil
	}
	expires := s.now().Add(reservationTTL)
	held := make([]map[string]any, 0, len(d.lines))
	for _, l := range d.lines {
		hold, after, err := reserveLine(q, repository.ReservationRow{OrgID: d.row.OrgID, DocumentID: d.row.ID, LineID: l.ID,
			PartID: *l.PartID, DepotID: d.row.DepotID, Qty: l.Qty, ExpiresAt: expires})
		if err != nil {
			return err
		}
		held = append(held, map[string]any{"lineId": l.ID, "partId": *l.PartID, "requested": l.Qty, "reserved": hold,
			"availableAfter": after})
	}
	return emit(ctx, q, d.row.OrgID, d.row.DepotID, "StockReserved", "document", d.row.ID, nil, map[string]any{
		"documentId": d.row.ID, "expiresAt": expires.UTC(), "lines": held})
}

// reserveLine holds min(qty, available) and returns what it held and the availability after. A concurrent writer
// may take stock between the read and the insert; the projection refuses, and the line is retried on a fresh read.
func reserveLine(q *repository.Q, r repository.ReservationRow) (models.Dec4, models.Dec4, error) {
	want := r.Qty
	for attempt := 0; ; attempt++ {
		lvl, err := q.StockLevel(r.PartID, r.DepotID)
		if err != nil {
			return 0, 0, err
		}
		avail := lvl.OnHand - lvl.Reserved
		r.Qty = min(want, max(avail, 0))
		if r.Qty == 0 || attempt == 5 {
			return 0, avail, nil
		}
		ok, err := q.TryInsertReservation(r)
		if err != nil || ok {
			return r.Qty, avail - r.Qty, err
		}
	}
}

// lockLevels locks, in (part, depot) order, the levels the document holds and the ones its lines will touch, before
// any of them is written: reservations released, reserved or moved in one transaction never wait in a cycle.
func lockLevels(q *repository.Q, orgID, documentID, depotID uint, lines []repository.LineRow) error {
	var keys []repository.StockKey
	if documentID != 0 {
		held, err := q.ReservationKeys(orgID, documentID)
		if err != nil {
			return err
		}
		keys = held
	}
	for _, l := range lines {
		if l.PartID != nil {
			keys = append(keys, repository.StockKey{PartID: *l.PartID, DepotID: depotID})
		}
	}
	return q.LockStockLevels(keys)
}

// release deletes the draft's reservations and emits one StockReleased with the reason.
func release(ctx context.Context, q *repository.Q, d *repository.DocumentRow, reason string) error {
	rows, err := q.ReleaseReservations(d.OrgID, d.ID)
	if err != nil || len(rows) == 0 {
		return err
	}
	return emitReleased(ctx, q, d.OrgID, d.DepotID, d.ID, reason, rows)
}

func emitReleased(ctx context.Context, q *repository.Q, orgID, depotID, docID uint, reason string, rows []repository.ReservationRow) error {
	lines := make([]map[string]any, len(rows))
	for i, r := range rows {
		lines[i] = map[string]any{"lineId": r.LineID, "partId": r.PartID, "qty": r.Qty}
	}
	return emit(ctx, q, orgID, depotID, "StockReleased", "document", docID, nil, map[string]any{
		"documentId": docID, "reason": reason, "lines": lines})
}

// documentErr maps the documents triggers' contract codes (message prefixes) to API errors. lines locate a
// return cap failure by the original line it names.
func documentErr(err error, lines []repository.LineRow) error {
	if err == nil {
		return nil
	}
	msg := repository.PgMessage(err)
	code, rest, _ := strings.Cut(msg, ":")
	switch code {
	case "ERR_DOCUMENT_EMPTY":
		return apierror.DocumentEmpty
	case "ERR_WAYBILL_INVALID":
		return apierror.WaybillInvalid
	case "ERR_WAYBILL_BILLED":
		return apierror.WaybillBilled
	case "ERR_LINKED_DOCUMENT_INVALID":
		return apierror.LinkedDocumentInvalid
	case "ERR_YEAR_CLOSED":
		return apierror.YearClosed
	case "ERR_DOCUMENT_HAS_DEPENDENTS":
		return apierror.DocumentHasDependents
	case "ERR_INVALID_STATE_TRANSITION":
		return apierror.InvalidStateTransition
	case "ERR_DOCUMENT_NOT_DRAFT":
		return apierror.DocumentNotDraft
	case "ERR_PRICE_DIFFERENCE_EXCEEDED":
		return apierror.WithField(apierror.PriceDifferenceExceeded, "lines")
	case "ERR_RETURN_QTY_EXCEEDED":
		return apierror.WithField(apierror.ReturnQtyExceeded, returnCapField(rest, lines))
	}
	switch {
	case repository.IsUniqueViolation(err) && strings.Contains(err.Error(), "documents_waybill_once"):
		return apierror.WaybillBilled
	case repository.IsForeignKeyViolation(err):
		return apierror.DocumentHasDependents
	}
	return err
}

// returnCapField is lines[i].qty of the first line returning the original line named in the message.
func returnCapField(msg string, lines []repository.LineRow) string {
	var id uint
	if _, err := fmt.Sscanf(strings.TrimSpace(msg), "original line %d", &id); err == nil {
		for i, l := range lines {
			if l.OriginLineID != nil && *l.OriginLineID == id {
				return lineField(i, "qty")
			}
		}
	}
	return "lines"
}

func (s *DocumentService) Get(ctx context.Context, orgID, id uint) (*models.Document, error) {
	return s.read(s.store.Read(ctx), orgID, id)
}

func (s *DocumentService) Members(ctx context.Context, orgID uint) ([]models.Member, error) {
	return s.store.Read(ctx).Members(orgID)
}

func (s *DocumentService) Grid(ctx context.Context, orgID uint, req repository.DocumentGridRequest, all bool) (*repository.DocumentGrid, error) {
	for _, t := range req.Types {
		if _, ok := docKinds[t]; !ok {
			return nil, badField("types")
		}
	}
	for _, st := range req.Statuses {
		if st != statusDraft && st != statusPosted && st != statusCancelled {
			return nil, badField("statuses")
		}
	}
	g, err := s.store.Read(ctx).GridDocuments(orgID, req, all)
	if repository.IsBadFilter(err) {
		return nil, apierror.WithDetail(apierror.InvalidRequest, err.Error())
	}
	return g, err
}
