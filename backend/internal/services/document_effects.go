package services

import (
	"context"
	"encoding/json"
	"math/big"
	"strconv"

	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

func handlesDocumentEvent(typ string) bool {
	return typ == "DocumentPosted" || typ == "DocumentCancelled"
}

// documentEntry writes the entry of a posted document (ADR-0013 posting table) unless it has one; waybills and zero
// totals have none.
func documentEntry(ctx context.Context, q *repository.Q, d *repository.DocumentRow, direction string) error {
	if !docKinds[d.Type].ledger {
		return nil
	}
	src := strconv.FormatUint(uint64(d.ID), 10)
	existing, err := q.EntryIDBySource(d.OrgID, string(models.SourceDocument), src)
	if err != nil || existing != nil {
		return err
	}
	rules := postingLines(d.Type, direction, priceDirection(d), amountsOf(d))
	if d.GrandTotal == 0 || len(rules) < 2 {
		return nil
	}
	sicil, err := q.GetSicilRef(d.OrgID, d.SicilID)
	if err != nil {
		return err
	}
	lines := make([]LineInput, 0, len(rules))
	for _, r := range rules {
		l := LineInput{Side: r.side, Amount: r.amount}
		if r.role == "" {
			a, err := sicilAccount(ctx, q, d.OrgID, sicil)
			if err != nil {
				return err
			}
			l.AccountID, l.DueDate = a.ID, d.DueDate
		} else {
			a, err := roleAccount(q, d.OrgID, r.role, d.DepotID)
			if err != nil {
				return err
			}
			l.AccountID = a.ID
		}
		lines = append(lines, l)
	}
	_, err = postEntry(ctx, q, d.OrgID, entryInput{depotID: d.DepotID, date: d.DocumentDate,
		description: deref(d.DocumentNo) + " " + sicil.HesapAdi, source: models.SourceDocument, sourceID: &src, lines: lines})
	return err
}

// reverseDocumentEntry reverses a cancelled document's entry on the cancel date, once.
func reverseDocumentEntry(ctx context.Context, q *repository.Q, d *repository.DocumentRow) error {
	existing, err := q.EntryIDBySource(d.OrgID, string(models.SourceDocument), strconv.FormatUint(uint64(d.ID), 10))
	if err != nil || existing == nil {
		return err
	}
	e, err := entry(q, d.OrgID, *existing)
	if err != nil || e.ReversedByEntryID != nil {
		return err
	}
	_, err = reverseEntry(ctx, q, e, *d.CancelDate, *d.CancelReason)
	return err
}

func deref(s *string) string {
	if s == nil {
		return ""
	}
	return *s
}

// documentMoves writes one movement per line of a posted document, or the opposite one of a cancelled document on
// the cancel date, unless the document never moves stock (price differences, bills of a waybill); lines that moved
// already are skipped.
func documentMoves(ctx context.Context, q *repository.Q, d *repository.DocumentRow, lines []repository.LineRow) error {
	kind := movesStock(d.Type, d.WaybillID)
	if kind == "" || len(lines) == 0 {
		return nil
	}
	ids := make([]uint, len(lines))
	for i, l := range lines {
		ids[i] = l.ID
	}
	moved, err := q.DocumentMoved(d.OrgID, ids, kind)
	if err != nil {
		return err
	}
	date := d.DocumentDate
	if d.Status == statusCancelled {
		if !moved {
			return nil
		}
		if moved, err = q.DocumentMoved(d.OrgID, ids, opposite(kind)); err != nil {
			return err
		}
		kind, date = opposite(kind), *d.CancelDate
	}
	if moved {
		return nil
	}
	moves := make([]repository.MovementInsert, len(lines))
	for i, l := range lines {
		src := strconv.FormatUint(uint64(l.ID), 10)
		cost := unitCost(l.NetAmount, l.Qty)
		moves[i] = repository.MovementInsert{PartID: *l.PartID, DepotID: d.DepotID, Kind: kind, Qty: l.Qty, UnitCost: &cost,
			SourceType: string(models.SourceDocument), SourceID: &src, Date: date}
	}
	_, _, err = postMovements(ctx, q, d.OrgID, moves)
	return err
}

// unitCost is net / qty at four decimals, half-up.
func unitCost(net models.Money, qty models.Dec4) models.Dec4 {
	n := new(big.Int).Mul(big.NewInt(int64(net)), big.NewInt(1_000_000))
	return models.Dec4(divRound(n, big.NewInt(int64(qty))))
}

// PostedDocument is what an e-Fatura integrator receives.
type PostedDocument struct {
	OrgID      uint
	DocumentID uint
	DocumentNo string
	Type       string
	Payload    json.RawMessage
}

// ExportRef identifies an exported document at the integrator.
type ExportRef struct {
	DocumentID uint
	Ref        string
}

// DocumentExporter is the e-Fatura port (ADR-0013 §15); F3 ships NoopExporter.
type DocumentExporter interface {
	Export(ctx context.Context, doc PostedDocument) (ExportRef, error)
	Cancel(ctx context.Context, ref ExportRef, reason string) error
}

type NoopExporter struct{}

func (NoopExporter) Export(_ context.Context, doc PostedDocument) (ExportRef, error) {
	return ExportRef{DocumentID: doc.DocumentID}, nil
}

func (NoopExporter) Cancel(context.Context, ExportRef, string) error { return nil }

// DocumentExport hands posted and cancelled documents to the exporter from the relay.
type DocumentExport struct{ Exporter DocumentExporter }

func (DocumentExport) Name() string            { return "document-export" }
func (DocumentExport) Handles(typ string) bool { return handlesDocumentEvent(typ) }

func (c DocumentExport) Handle(ctx context.Context, _ *repository.Q, env Envelope) error {
	var p struct {
		DocumentID uint    `json:"documentId"`
		DocumentNo string  `json:"documentNo"`
		Type       string  `json:"documentType"`
		Reason     *string `json:"reason"`
	}
	if err := json.Unmarshal(env.Payload, &p); err != nil {
		return err
	}
	if env.Type == "DocumentCancelled" {
		return c.Exporter.Cancel(ctx, ExportRef{DocumentID: p.DocumentID, Ref: p.DocumentNo}, deref(p.Reason))
	}
	_, err := c.Exporter.Export(ctx, PostedDocument{OrgID: env.OrgID, DocumentID: p.DocumentID, DocumentNo: p.DocumentNo,
		Type: p.Type, Payload: env.Payload})
	return err
}
