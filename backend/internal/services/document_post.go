package services

import (
	"context"
	"encoding/json"
	"strings"
	"time"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

type CreditOverrideInput struct {
	Reason string `json:"reason"`
}

type PostInput struct {
	CreditOverride *CreditOverrideInput `json:"creditOverride"`
}

type CreditLimitDetails struct {
	Limit         models.Money `json:"limit"`
	Balance       models.Money `json:"balance"`
	DocumentTotal models.Money `json:"documentTotal"`
	Excess        models.Money `json:"excess"`
}

// istanbul is Turkey's fixed UTC+3 (no DST since 2016); cancel dates are Istanbul days.
var istanbul = time.FixedZone("TRT", 3*60*60)

func (s *DocumentService) today() models.Date {
	n := s.now().In(istanbul)
	return models.NewDate(n.Year(), n.Month(), n.Day())
}

// Post allocates the number, releases the reservations, writes the entry and the movements and emits DocumentPosted
// in one transaction (ADR-0014), so the entry, the movements and the warnings are in the answer.
func (s *DocumentService) Post(ctx context.Context, orgID, id uint, version int, in PostInput) (*models.Document, error) {
	if in.CreditOverride != nil {
		in.CreditOverride.Reason = strings.TrimSpace(in.CreditOverride.Reason)
		if n := len([]rune(in.CreditOverride.Reason)); n < 3 || n > 500 {
			return nil, badField("creditOverride.reason")
		}
	}
	var out *models.Document
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		row, err := lockDraft(q, orgID, id, version)
		if err != nil {
			return err
		}
		if row.LinkedDocumentID != nil {
			if _, err := q.LockDocument(orgID, *row.LinkedDocumentID); err != nil {
				return err
			}
		}
		if err := q.LockLedger(orgID, false); err != nil {
			return err
		}
		if err := yearOpen(q, orgID, row.DocumentDate); err != nil {
			return err
		}
		lines, err := q.DocumentLines(orgID, id)
		if err != nil {
			return err
		}
		if len(lines) == 0 {
			return apierror.DocumentEmpty
		}
		if err := lockLevels(q, orgID, id, row.DepotID, lines); err != nil {
			return err
		}
		if err := activeDocumentParts(q, orgID, lines); err != nil {
			return err
		}
		card, err := q.DocumentCard(orgID, row.SicilID)
		if err != nil {
			return err
		}
		direction, err := documentDirection(q, row)
		if err != nil {
			return err
		}
		post, overridden, err := creditCheck(ctx, q, row, card, direction, in.CreditOverride)
		if err != nil {
			return err
		}
		if err := release(ctx, q, row, "POSTED"); err != nil {
			return err
		}
		if post.No, err = q.NextNumber(orgID, row.DepotID, docKinds[row.Type].prefix, row.DocumentDate.Year()); err != nil {
			return err
		}
		snap, _ := json.Marshal(models.DocumentCardSummary{HesapNo: card.HesapNo, HesapAdi: card.HesapAdi, Adres: card.Adres,
			Ilce: card.Ilce, Sehir: card.Sehir, VDairesi: card.VDairesi, VNo: card.VNo, TCNo: card.TCNo})
		post.Snapshot, post.PostedBy = string(snap), actorFrom(ctx).userID()
		if err := q.PostDocument(orgID, id, post); err != nil {
			return documentErr(err, lines)
		}
		posted, err := q.GetDocument(orgID, id)
		if err != nil {
			return err
		}
		if err := documentEntry(ctx, q, posted, direction); err != nil {
			return err
		}
		if err := documentMoves(ctx, q, posted, lines); err != nil {
			return err
		}
		if err := emit(ctx, q, orgID, row.DepotID, "DocumentPosted", "document", id, &posted.Version,
			documentPostedPayload(posted, lines, direction)); err != nil {
			return err
		}
		if out, err = s.read(q, orgID, id); err != nil {
			return err
		}
		if out.Warnings, err = negativeStock(q, posted, lines); err != nil {
			return err
		}
		if overridden != nil {
			if err := audit(ctx, q, orgID, "document.credit_override", "document", id, nil, overridden); err != nil {
				return err
			}
		}
		return audit(ctx, q, orgID, "document.posted", "document", id, nil, out)
	})
	return out, err
}

// creditCheck refuses a sale over the card's limit (409 with details) unless an admin overrides it with a reason;
// an override that is not needed is ignored. The card row is locked so the card's posts are checked one by one.
func creditCheck(ctx context.Context, q *repository.Q, row *repository.DocumentRow, card *repository.DocumentCard,
	direction string, override *CreditOverrideInput) (repository.DocumentPost, map[string]any, error) {
	var post repository.DocumentPost
	if !creditChecked(row.Type, direction, priceDirection(row)) || card.CreditLimit == nil {
		return post, nil, nil
	}
	if err := q.LockCard(row.OrgID, card.ID); err != nil {
		return post, nil, err
	}
	balance, err := creditBalance(q, row.OrgID, card)
	if err != nil {
		return post, nil, err
	}
	excess := balance + row.GrandTotal - *card.CreditLimit
	if excess <= 0 {
		return post, nil, nil
	}
	details := CreditLimitDetails{Limit: *card.CreditLimit, Balance: balance, DocumentTotal: row.GrandTotal, Excess: excess}
	actor := actorFrom(ctx)
	switch {
	case override == nil:
		return post, nil, apierror.WithDetails(apierror.CreditLimitExceeded, details)
	case !actor.Admin:
		return post, nil, apierror.Forbidden
	}
	post.OverrideReason, post.OverrideBy, post.OverrideExcess = &override.Reason, actor.userID(), &excess
	return post, map[string]any{"reason": override.Reason, "userId": actor.UserID, "excess": excess,
		"limit": details.Limit, "balance": balance, "documentTotal": row.GrandTotal}, nil
}

func activeDocumentParts(q *repository.Q, orgID uint, lines []repository.LineRow) error {
	var ids []uint
	for _, l := range lines {
		if l.PartID != nil {
			ids = append(ids, *l.PartID)
		}
	}
	if len(ids) == 0 {
		return nil
	}
	parts, err := q.DocumentParts(orgID, ids)
	if err != nil {
		return err
	}
	active := map[uint]bool{}
	for _, p := range parts {
		active[p.ID] = p.IsActive
	}
	for i, l := range lines {
		if l.PartID != nil && !active[*l.PartID] {
			return apierror.UnknownRef(apierror.PartInactive, lineField(i, "partId"))
		}
	}
	return nil
}

// negativeStock reports every part left below zero in the document's depot by its movements.
func negativeStock(q *repository.Q, row *repository.DocumentRow, lines []repository.LineRow) ([]models.DocumentWarning, error) {
	out := []models.DocumentWarning{}
	if movesStock(row.Type, row.WaybillID) == "" {
		return out, nil
	}
	seen := map[uint]bool{}
	for _, l := range lines {
		if l.PartID == nil || seen[*l.PartID] {
			continue
		}
		seen[*l.PartID] = true
		lvl, err := q.StockLevel(*l.PartID, row.DepotID)
		if err != nil {
			return nil, err
		}
		if lvl.OnHand < 0 {
			part, depot, after := *l.PartID, row.DepotID, lvl.OnHand
			out = append(out, models.DocumentWarning{Code: models.WarningNegativeStock, PartID: &part, StockNo: l.StockNo,
				DepotID: &depot, OnHandAfter: &after})
		}
	}
	return out, nil
}

func documentPostedPayload(d *repository.DocumentRow, lines []repository.LineRow, direction string) map[string]any {
	ls := make([]map[string]any, len(lines))
	for i, l := range lines {
		ls[i] = map[string]any{"lineId": l.ID, "lineNo": l.LineNo, "partId": l.PartID, "qty": l.Qty, "unitPrice": l.UnitPrice,
			"discount1Pct": l.Discount1Pct, "discount2Pct": l.Discount2Pct, "vatPct": l.VatPct, "grossAmount": l.GrossAmount,
			"discountAmount": l.DiscountAmount, "netAmount": l.NetAmount, "vatAmount": l.VatAmount,
			"originLineId": l.OriginLineID, "billedLineId": l.BilledLineID, "description": l.Description}
	}
	var override any
	if d.CreditOverrideReason != nil && d.CreditOverrideBy != nil && d.CreditOverrideExcess != nil {
		override = map[string]any{"reason": *d.CreditOverrideReason, "userId": *d.CreditOverrideBy, "excess": *d.CreditOverrideExcess}
	}
	return map[string]any{
		"documentId": d.ID, "documentNo": d.DocumentNo, "documentType": d.Type, "direction": direction,
		"priceDirection": d.PriceDirection, "sicilId": d.SicilID, "documentDate": d.DocumentDate, "dueDate": d.DueDate,
		"linkedDocumentId": d.LinkedDocumentID, "waybillDocumentId": d.WaybillID, "postsToLedger": docKinds[d.Type].ledger,
		"movesStock": movesStock(d.Type, d.WaybillID) != "", "currency": models.TRY, "grossTotal": d.GrossTotal,
		"discountTotal": d.DiscountTotal, "netTotal": d.NetTotal, "vatTotal": d.VatTotal, "grandTotal": d.GrandTotal,
		"lines": ls, "salesRepUserId": d.SalesRepUserID, "creditOverride": override,
	}
}

// Cancel reverses a posted document on the later of today (Istanbul) and its date: the reversal entry, the opposite
// movements and DocumentCancelled in one transaction.
func (s *DocumentService) Cancel(ctx context.Context, orgID, id uint, version int, reason string) (*models.Document, error) {
	reason = strings.TrimSpace(reason)
	if len([]rune(reason)) < 3 {
		return nil, badField("reason")
	}
	var out *models.Document
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		row, err := lockDocument(q, orgID, id)
		switch {
		case err != nil:
			return err
		case row.Status != statusPosted:
			return apierror.InvalidStateTransition
		case row.Version != version:
			return apierror.VersionConflict
		}
		if err := noDependents(q.PostedDependents(orgID, id)); err != nil {
			return err
		}
		date := s.today()
		if date.Before(row.DocumentDate.Time) {
			date = row.DocumentDate
		}
		if err := q.LockLedger(orgID, false); err != nil {
			return err
		}
		if err := yearOpen(q, orgID, date); err != nil {
			return err
		}
		lines, err := q.DocumentLines(orgID, id)
		if err != nil {
			return err
		}
		if err := lockLevels(q, orgID, 0, row.DepotID, lines); err != nil {
			return err
		}
		if err := q.CancelDocument(orgID, id, actorFrom(ctx).userID(), date, reason); err != nil {
			return documentErr(err, lines)
		}
		cancelled, err := q.GetDocument(orgID, id)
		if err != nil {
			return err
		}
		direction, err := documentDirection(q, cancelled)
		if err != nil {
			return err
		}
		if err := reverseDocumentEntry(ctx, q, cancelled); err != nil {
			return err
		}
		if err := documentMoves(ctx, q, cancelled, lines); err != nil {
			return err
		}
		if err := emit(ctx, q, orgID, row.DepotID, "DocumentCancelled", "document", id, &cancelled.Version,
			documentCancelledPayload(cancelled, lines, direction)); err != nil {
			return err
		}
		if out, err = s.read(q, orgID, id); err != nil {
			return err
		}
		if out.Warnings, err = negativeStock(q, cancelled, lines); err != nil {
			return err
		}
		return audit(ctx, q, orgID, "document.cancelled", "document", id, nil, out)
	})
	return out, err
}

func documentCancelledPayload(d *repository.DocumentRow, lines []repository.LineRow, direction string) map[string]any {
	ls := []map[string]any{}
	for _, l := range lines {
		if l.PartID != nil {
			ls = append(ls, map[string]any{"lineId": l.ID, "partId": *l.PartID, "qty": l.Qty})
		}
	}
	return map[string]any{
		"documentId": d.ID, "documentNo": d.DocumentNo, "documentType": d.Type, "direction": direction,
		"priceDirection": d.PriceDirection, "sicilId": d.SicilID, "cancelDate": d.CancelDate, "reason": d.CancelReason,
		"postsToLedger": docKinds[d.Type].ledger, "movesStock": movesStock(d.Type, d.WaybillID) != "", "lines": ls,
	}
}
