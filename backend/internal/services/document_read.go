package services

import (
	"context"
	"encoding/json"
	"sort"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

// read assembles the document with its lines, stock, links, effects and (for a draft) warnings.
func (s *DocumentService) read(q *repository.Q, orgID, id uint) (*models.Document, error) {
	row, err := q.GetDocument(orgID, id)
	if repository.IsNotFound(err) {
		return nil, apierror.DocumentNotFound
	}
	if err != nil {
		return nil, err
	}
	lines, err := q.DocumentLines(orgID, id)
	if err != nil {
		return nil, err
	}
	card, err := q.DocumentCard(orgID, row.SicilID)
	if err != nil {
		return nil, err
	}
	doc := documentHeader(row, card)
	if err := s.readLinks(q, row, doc); err != nil {
		return nil, err
	}
	if err := s.readLines(q, row, lines, doc); err != nil {
		return nil, err
	}
	direction, err := documentDirection(q, row)
	if err != nil {
		return nil, err
	}
	balance, err := creditBalance(q, orgID, card)
	if err != nil {
		return nil, err
	}
	if doc.Effects, err = effects(q, row, card, direction, balance); err != nil {
		return nil, err
	}
	if row.Status == statusDraft {
		doc.Warnings, err = draftWarnings(q, row, card, direction, balance, doc.Lines)
	}
	return doc, err
}

func documentHeader(row *repository.DocumentRow, card *repository.DocumentCard) *models.Document {
	doc := &models.Document{
		ID: row.ID, OrgID: row.OrgID, DocumentNo: row.DocumentNo, Type: row.Type, Status: row.Status, DepotID: row.DepotID,
		SicilID: row.SicilID, SicilName: card.HesapAdi, DocumentDate: row.DocumentDate, ShipDate: row.ShipDate,
		DueDate: row.DueDate, SalesRepUserID: row.SalesRepUserID, LinkedDocumentID: row.LinkedDocumentID,
		PriceDirection: row.PriceDirection, WaybillDocumentID: row.WaybillID, Currency: models.TRY,
		GrossTotal: row.GrossTotal, DiscountTotal: row.DiscountTotal, NetTotal: row.NetTotal, VatTotal: row.VatTotal,
		GrandTotal: row.GrandTotal, PostedAt: row.PostedAt, CancelledAt: row.CancelledAt, CancelDate: row.CancelDate,
		CancelReason: row.CancelReason, Version: row.Version, Warnings: []models.DocumentWarning{},
		DependentDocumentIDs: []uint{}, VatBreakdown: []models.VatBreakdown{}, Lines: []models.DocumentLine{},
		Snapshot: &models.DocumentCardSummary{HesapNo: card.HesapNo, HesapAdi: card.HesapAdi, Adres: card.Adres,
			Ilce: card.Ilce, Sehir: card.Sehir, VDairesi: card.VDairesi, VNo: card.VNo, TCNo: card.TCNo},
	}
	if row.Description != nil {
		doc.Description = *row.Description
	}
	if row.SicilSnapshot != nil {
		var snap models.DocumentCardSummary
		if json.Unmarshal([]byte(*row.SicilSnapshot), &snap) == nil {
			doc.Snapshot = &snap
		}
	}
	return doc
}

func (s *DocumentService) readLinks(q *repository.Q, row *repository.DocumentRow, doc *models.Document) error {
	var err error
	if row.SalesRepUserID != nil {
		name, err := q.UserName(*row.SalesRepUserID)
		if err != nil {
			return err
		}
		doc.SalesRepName = &name
	}
	var ids []uint
	for _, p := range []*uint{row.LinkedDocumentID, row.WaybillID} {
		if p != nil {
			ids = append(ids, *p)
		}
	}
	if len(ids) > 0 {
		nos, err := q.DocumentNos(row.OrgID, ids)
		if err != nil {
			return err
		}
		doc.LinkedDocumentNo = numberOf(nos, row.LinkedDocumentID)
		doc.WaybillDocumentNo = numberOf(nos, row.WaybillID)
	}
	if docKinds[row.Type].bills == "" && !docKinds[row.Type].ledger {
		if doc.BilledByDocumentID, err = q.BilledBy(row.OrgID, row.ID); err != nil {
			return err
		}
	}
	if doc.DependentDocumentIDs, err = q.DependentDocumentIDs(row.OrgID, row.ID); err != nil {
		return err
	}
	if doc.JournalEntryID, doc.ReversalEntryID, err = q.DocumentEntries(row.OrgID, row.ID); err != nil {
		return err
	}
	if row.CreditOverrideReason != nil && row.CreditOverrideBy != nil && row.CreditOverrideAt != nil && row.CreditOverrideExcess != nil {
		name, err := q.UserName(*row.CreditOverrideBy)
		if err != nil {
			return err
		}
		doc.CreditOverride = &models.CreditOverride{Reason: *row.CreditOverrideReason, UserID: *row.CreditOverrideBy,
			UserName: name, At: *row.CreditOverrideAt, Excess: *row.CreditOverrideExcess}
	}
	return nil
}

func numberOf(nos map[uint]string, id *uint) *string {
	if id == nil {
		return nil
	}
	if no, ok := nos[*id]; ok {
		return &no
	}
	return nil
}

func (s *DocumentService) readLines(q *repository.Q, row *repository.DocumentRow, lines []repository.LineRow, doc *models.Document) error {
	var parts, lineIDs []uint
	seen := map[uint]bool{}
	for _, l := range lines {
		lineIDs = append(lineIDs, l.ID)
		if l.PartID != nil && !seen[*l.PartID] {
			seen[*l.PartID] = true
			parts = append(parts, *l.PartID)
		}
	}
	stock := map[uint][]models.LineDepotStock{}
	if len(parts) > 0 {
		rows, err := q.LineStockByDepot(row.OrgID, parts)
		if err != nil {
			return err
		}
		for _, r := range rows {
			stock[r.PartID] = append(stock[r.PartID], r.LineDepotStock)
		}
	}
	held := map[uint]models.Dec4{}
	if row.Status == statusDraft {
		res, err := q.DocumentReservations(row.OrgID, row.ID)
		if err != nil {
			return err
		}
		for _, r := range res {
			held[r.LineID] = r.Qty
			if doc.Reservation == nil || r.ExpiresAt.After(doc.Reservation.ExpiresAt) {
				doc.Reservation = &models.Reservation{ExpiresAt: r.ExpiresAt.UTC()}
			}
		}
	}
	var returned map[uint]models.Dec4
	original := row.WaybillID == nil && (contains(salesOriginals, row.Type) || contains(purchaseOriginals, row.Type))
	if original && len(lineIDs) > 0 {
		var err error
		if returned, err = q.ReturnedQty(row.OrgID, lineIDs); err != nil {
			return err
		}
	}
	vat := map[models.Dec4]*models.VatBreakdown{}
	for _, l := range lines {
		ml := models.DocumentLine{ID: l.ID, LineNo: l.LineNo, PartID: l.PartID, CatalogCode: l.CatalogCode,
			Description: l.Description, Qty: l.Qty, UnitPrice: l.UnitPrice, Discount1Pct: l.Discount1Pct,
			Discount2Pct: l.Discount2Pct, VatPct: l.VatPct, GrossAmount: l.GrossAmount, DiscountAmount: l.DiscountAmount,
			NetAmount: l.NetAmount, VatAmount: l.VatAmount, LineTotal: l.NetAmount + l.VatAmount,
			OriginLineID: l.OriginLineID, BilledLineID: l.BilledLineID}
		if l.StockNo != nil {
			ml.StockNo = *l.StockNo
		}
		if l.Name != nil {
			ml.Name = *l.Name
		}
		if l.PartID != nil {
			for _, st := range stock[*l.PartID] {
				if st.DepotID == row.DepotID {
					onHand, avail := st.OnHand, st.Available
					ml.OnHand, ml.Available = &onHand, &avail
				}
			}
			if row.Status == statusDraft {
				ml.StockByDepot = stock[*l.PartID]
			}
		}
		if row.Status == statusDraft {
			h := held[l.ID]
			ml.ReservedQty = &h
		}
		if original {
			r := returned[l.ID]
			ml.ReturnedQty = &r
		}
		b, ok := vat[l.VatPct]
		if !ok {
			b = &models.VatBreakdown{VatPct: l.VatPct}
			vat[l.VatPct] = b
		}
		b.Base += l.NetAmount
		b.Vat += l.VatAmount
		doc.Lines = append(doc.Lines, ml)
	}
	for _, b := range vat {
		doc.VatBreakdown = append(doc.VatBreakdown, *b)
	}
	sort.Slice(doc.VatBreakdown, func(i, j int) bool { return doc.VatBreakdown[i].VatPct < doc.VatBreakdown[j].VatPct })
	return nil
}

// documentDirection is SALES or PURCHASE; a price difference takes it from the invoice it links.
func documentDirection(q *repository.Q, row *repository.DocumentRow) (string, error) {
	if row.Type != typePriceDifference || row.LinkedDocumentID == nil {
		return docKinds[row.Type].direction, nil
	}
	o, err := q.GetDocument(row.OrgID, *row.LinkedDocumentID)
	if err != nil {
		return "", err
	}
	return docKinds[o.Type].direction, nil
}

func priceDirection(row *repository.DocumentRow) string {
	if row.PriceDirection == nil {
		return ""
	}
	return *row.PriceDirection
}

func amountsOf(row *repository.DocumentRow) docAmounts {
	return docAmounts{gross: row.GrossTotal, discount: row.DiscountTotal, net: row.NetTotal, vat: row.VatTotal, total: row.GrandTotal}
}

// creditBalance is the card's current account over all depots and dates plus the documents whose ledger effect is
// not written yet (ADR-0013 §9).
func creditBalance(q *repository.Q, orgID uint, card *repository.DocumentCard) (models.Money, error) {
	var balance models.Money
	acct, err := q.AccountBySicil(orgID, card.ID)
	switch {
	case err == nil:
		if balance, err = q.AccountBalance(acct.ID); err != nil {
			return 0, err
		}
	case !repository.IsNotFound(err):
		return 0, err
	}
	pending, err := q.PendingLedgerDocuments(orgID, card.ID)
	if err != nil {
		return 0, err
	}
	for _, p := range pending {
		dir := docKinds[p.Type].direction
		if p.LinkedType != nil && p.Type == typePriceDifference {
			dir = docKinds[*p.LinkedType].direction
		}
		pd := ""
		if p.PriceDirection != nil {
			pd = *p.PriceDirection
		}
		signed := cardDebit(p.Type, dir, pd, p.GrandTotal)
		if p.Reversal {
			signed = -signed
		}
		balance += signed
	}
	return balance, nil
}

var cardGroup = map[string]string{string(models.SicilTipiMusteri): "120", string(models.SicilTipiFirma): "320", string(models.SicilTipiBanka): "102"}

// effects is what posting will do (DRAFT) or did: the ADR-0013 postings, the stock per depot and the credit used.
func effects(q *repository.Q, row *repository.DocumentRow, card *repository.DocumentCard, direction string, balance models.Money) (models.DocumentEffects, error) {
	out := models.DocumentEffects{Ledger: []models.LedgerEffect{}, Stock: []models.StockEffect{}}
	if docKinds[row.Type].ledger {
		for _, l := range postingLines(row.Type, direction, priceDirection(row), amountsOf(row)) {
			e := models.LedgerEffect{Side: l.side, Amount: l.amount}
			if l.role == "" {
				e.AccountCode, e.AccountName = cardGroup[card.SicilTipi]+"."+card.HesapNo, card.HesapAdi
				if a, err := q.AccountBySicil(row.OrgID, card.ID); err == nil {
					e.AccountCode, e.AccountName = a.Code, a.Name
				}
			} else {
				a, err := q.RoleAccount(row.OrgID, l.role, row.DepotID)
				if repository.IsNotFound(err) {
					continue
				}
				if err != nil {
					return out, err
				}
				role := l.role
				e.Role, e.AccountCode, e.AccountName = &role, a.Code, a.Name
			}
			out.Ledger = append(out.Ledger, e)
		}
	}
	if kind := movesStock(row.Type, row.WaybillID); kind != "" {
		lines, err := q.DocumentLines(row.OrgID, row.ID)
		if err != nil {
			return out, err
		}
		var delta models.Dec4
		for _, l := range lines {
			if kind == models.MoveIn {
				delta += l.Qty
			} else {
				delta -= l.Qty
			}
		}
		if len(lines) > 0 {
			out.Stock = append(out.Stock, models.StockEffect{DepotID: row.DepotID, QtyDelta: delta})
		}
	}
	if creditChecked(row.Type, direction, priceDirection(row)) && card.CreditLimit != nil && *card.CreditLimit > 0 {
		used := balance
		if row.Status == statusDraft {
			used += row.GrandTotal
		}
		pct := int((int64(used)*100*2 + int64(*card.CreditLimit)) / (2 * int64(*card.CreditLimit)))
		out.CreditUsedPctAfter = &pct
	}
	return out, nil
}

// draftWarnings: STOCK_SHORT per line holding less than its qty, CREDIT_LIMIT, PRICE_BELOW_LIST on sales.
func draftWarnings(q *repository.Q, row *repository.DocumentRow, card *repository.DocumentCard, direction string,
	balance models.Money, lines []models.DocumentLine) ([]models.DocumentWarning, error) {
	out := []models.DocumentWarning{}
	if reserves(row.Type, row.WaybillID) {
		byPart := map[uint]models.Dec4{}
		for _, l := range lines {
			byPart[*l.PartID] += *l.ReservedQty
		}
		for _, l := range lines {
			if *l.ReservedQty >= l.Qty {
				continue
			}
			lvl, err := q.StockLevel(*l.PartID, row.DepotID)
			if err != nil {
				return nil, err
			}
			no, part, depot, qty := l.LineNo, *l.PartID, row.DepotID, l.Qty
			avail := lvl.OnHand - lvl.Reserved + byPart[part]
			out = append(out, models.DocumentWarning{Code: models.WarningStockShort, LineNo: &no, PartID: &part,
				DepotID: &depot, Requested: &qty, Available: &avail})
		}
	}
	if creditChecked(row.Type, direction, priceDirection(row)) && card.CreditLimit != nil && balance+row.GrandTotal > *card.CreditLimit {
		out = append(out, models.DocumentWarning{Code: models.WarningCreditLimit})
	}
	if direction == dirSales && row.Type != typePriceDifference && !docKinds[row.Type].isReturn() {
		for _, l := range lines {
			p, err := q.PriceOn(row.OrgID, *l.PartID, row.DocumentDate)
			if err != nil {
				return nil, err
			}
			if p != nil && l.UnitPrice < p.Price {
				no, part := l.LineNo, *l.PartID
				out = append(out, models.DocumentWarning{Code: models.WarningPriceBelowList, LineNo: &no, PartID: &part})
			}
		}
	}
	return out, nil
}

// DocumentPrint is a document with what its printout adds: the company and the depot.
type DocumentPrint struct {
	Document  *models.Document
	Company   string
	DepotCode string
}

func (s *DocumentService) Print(ctx context.Context, orgID, id uint) (*DocumentPrint, error) {
	q := s.store.Read(ctx)
	doc, err := s.read(q, orgID, id)
	if err != nil {
		return nil, err
	}
	company, err := q.OrgName(orgID)
	if err != nil {
		return nil, err
	}
	depot, err := q.GetDepot(orgID, doc.DepotID)
	if err != nil {
		return nil, err
	}
	return &DocumentPrint{Document: doc, Company: company, DepotCode: depot.Code}, nil
}
