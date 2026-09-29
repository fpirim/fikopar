package services

import (
	"context"
	"encoding/base64"
	"fmt"
	"strconv"
	"strings"
	"time"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

type LineInput struct {
	AccountID uint         `json:"accountId"`
	Side      models.Side  `json:"side"`
	Amount    models.Money `json:"amount"`
	DueDate   *models.Date `json:"dueDate"`
	Memo      string       `json:"memo"`
}

type EntryCreate struct {
	DepotID     uint        `json:"depotId"`
	EntryDate   models.Date `json:"entryDate"`
	Description string      `json:"description"`
	Lines       []LineInput `json:"lines"`
}

type entryInput struct {
	depotID     uint
	date        models.Date
	description string
	source      models.SourceType
	sourceID    *string
	reverses    *uint
	lines       []LineInput
}

func today() models.Date {
	n := time.Now()
	return models.NewDate(n.Year(), n.Month(), n.Day())
}

func (s *LedgerService) PostManual(ctx context.Context, orgID uint, in EntryCreate) (*models.JournalEntry, error) {
	in.Description = strings.TrimSpace(in.Description)
	if in.EntryDate.IsZero() {
		return nil, apierror.WithField(apierror.InvalidRequest, "entryDate")
	}
	if in.Description == "" {
		return nil, apierror.WithField(apierror.InvalidRequest, "description")
	}
	var out *models.JournalEntry
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		var err error
		out, err = postEntry(ctx, q, orgID, entryInput{
			depotID: in.DepotID, date: in.EntryDate, description: in.Description,
			source: models.SourceManual, lines: in.Lines,
		})
		return err
	})
	return out, err
}

func depotRef(q *repository.Q, orgID, depotID uint) (*models.Depot, error) {
	if depotID == 0 {
		return nil, apierror.DepotRequired
	}
	d, err := q.GetDepot(orgID, depotID)
	if repository.IsNotFound(err) {
		return nil, apierror.UnknownRef(apierror.DepotNotFound, "depotId")
	}
	return d, err
}

// postEntry is the only writer of journal entries. The service rejects what the database would
// reject at commit (Σ debit ≠ Σ credit, < 2 lines) so callers get a contract error, not a 500.
func postEntry(ctx context.Context, q *repository.Q, orgID uint, in entryInput) (*models.JournalEntry, error) {
	if _, err := depotRef(q, orgID, in.depotID); err != nil {
		return nil, err
	}
	if len(in.lines) < 2 {
		return nil, apierror.EntryTooFewLines
	}
	ids := make([]uint, 0, len(in.lines))
	for _, l := range in.lines {
		if l.Amount <= 0 {
			return nil, apierror.InvalidAmount
		}
		if l.Side != models.Debit && l.Side != models.Credit {
			return nil, apierror.WithField(apierror.InvalidRequest, "side")
		}
		ids = append(ids, l.AccountID)
	}
	accounts, err := q.AccountsForUpdate(orgID, ids)
	if err != nil {
		return nil, err
	}
	byID := make(map[uint]models.Account, len(accounts))
	for _, a := range accounts {
		byID[a.ID] = a
	}
	net := map[string]models.Money{}
	for _, l := range in.lines {
		a, ok := byID[l.AccountID]
		if !ok {
			return nil, apierror.UnknownRef(apierror.AccountNotFound, "accountId")
		}
		if !a.IsActive {
			return nil, apierror.WithDetail(apierror.AccountInactive, a.Code)
		}
		if l.Side == models.Debit {
			net[a.Currency] += l.Amount
		} else {
			net[a.Currency] -= l.Amount
		}
	}
	for cur, d := range net {
		if d != 0 {
			return nil, apierror.WithDetail(apierror.EntryUnbalanced, fmt.Sprintf("%s off by %s", cur, d))
		}
	}

	no, err := q.NextNumber(orgID, in.depotID, "J", in.date.Year())
	if err != nil {
		return nil, err
	}
	actor := actorFrom(ctx)
	e := &models.JournalEntry{
		OrgID: orgID, DepotID: in.depotID, EntryNo: no, EntryDate: in.date, Description: in.description,
		SourceType: in.source, SourceID: in.sourceID, ReversesEntryID: in.reverses, CreatedBy: actor.userID(),
	}
	if in.source == models.SourceManual || in.source == models.SourceReversal {
		e.IdempotencyKey = idempotencyKeyFrom(ctx)
	}
	if err := q.InsertEntry(e); err != nil {
		if repository.IsUniqueViolation(err) && in.reverses != nil {
			return nil, apierror.EntryAlreadyReversed
		}
		return nil, err
	}
	for i, l := range in.lines {
		p := &models.Posting{EntryID: e.ID, LineNo: i + 1, AccountID: l.AccountID, Side: l.Side, Amount: l.Amount,
			Currency: byID[l.AccountID].Currency, DueDate: l.DueDate, Memo: l.Memo}
		if err := q.InsertPosting(orgID, p); err != nil {
			return nil, err
		}
	}
	full, err := q.GetEntry(orgID, e.ID)
	if err != nil {
		return nil, err
	}
	if err := emit(ctx, q, orgID, full.DepotID, "LedgerEntryPosted", "journal_entry", full.ID, nil, ledgerEntryPayload(full)); err != nil {
		return nil, err
	}
	return full, audit(ctx, q, orgID, "journal_entry.posted", "journal_entry", full.ID, nil, full)
}

func ledgerEntryPayload(e *models.JournalEntry) map[string]any {
	postings := make([]map[string]any, len(e.Postings))
	currency := e.Postings[0].Currency
	var total models.Money
	for i, p := range e.Postings {
		m := map[string]any{"accountId": p.AccountID, "accountCode": p.AccountCode, "side": p.Side, "amount": p.Amount}
		if p.DueDate != nil {
			m["dueDate"] = p.DueDate
		}
		postings[i] = m
		if p.Side == models.Debit && p.Currency == currency {
			total += p.Amount
		}
	}
	return map[string]any{
		"entryId": e.ID, "entryNo": e.EntryNo, "entryDate": e.EntryDate, "sourceType": e.SourceType,
		"sourceId": e.SourceID, "reversesEntryId": e.ReversesEntryID, "currency": currency, "total": total,
		"postings": postings,
	}
}

func (s *LedgerService) GetEntry(ctx context.Context, orgID, id uint) (*models.JournalEntry, error) {
	return entry(s.store.Read(ctx), orgID, id)
}

func entry(q *repository.Q, orgID, id uint) (*models.JournalEntry, error) {
	e, err := q.GetEntry(orgID, id)
	if repository.IsNotFound(err) {
		return nil, apierror.EntryNotFound
	}
	return e, err
}

func (s *LedgerService) Trail(ctx context.Context, orgID, id uint) ([]models.TrailItem, error) {
	q := s.store.Read(ctx)
	if _, err := entry(q, orgID, id); err != nil {
		return nil, err
	}
	return q.EntryTrail(orgID, id)
}

// Reverse reverses a manual entry; entries with an owner go through the owner.
func (s *LedgerService) Reverse(ctx context.Context, orgID, id uint, reason string) (*models.JournalEntry, error) {
	reason = strings.TrimSpace(reason)
	if len([]rune(reason)) < 3 {
		return nil, apierror.WithField(apierror.InvalidRequest, "reason")
	}
	var out *models.JournalEntry
	err := s.store.InTx(ctx, func(q *repository.Q) error {
		if err := q.LockEntry(orgID, id); err != nil {
			return err
		}
		e, err := entry(q, orgID, id)
		if err != nil {
			return err
		}
		switch e.SourceType {
		case models.SourceManual:
		case models.SourceReversal:
			return apierror.InvalidStateTransition
		default:
			return apierror.WithOwner(apierror.EntryOwnedBySource, owner(e))
		}
		out, err = reverseEntry(ctx, q, e, reason)
		return err
	})
	return out, err
}

func owner(e *models.JournalEntry) apierror.Owner {
	o := apierror.Owner{SourceType: string(e.SourceType), SourceNo: e.SourceNo}
	switch {
	case e.OwnerID != nil:
		o.SourceID = *e.OwnerID
	case e.SourceID != nil:
		o.SourceID = *e.SourceID
	}
	return o
}

func reverseEntry(ctx context.Context, q *repository.Q, e *models.JournalEntry, reason string) (*models.JournalEntry, error) {
	if e.ReversedByEntryID != nil {
		return nil, apierror.EntryAlreadyReversed
	}
	lines := make([]LineInput, len(e.Postings))
	for i, p := range e.Postings {
		lines[i] = LineInput{AccountID: p.AccountID, Side: p.Side.Opposite(), Amount: p.Amount, DueDate: p.DueDate}
	}
	src := strconv.FormatUint(uint64(e.ID), 10)
	return postEntry(ctx, q, e.OrgID, entryInput{
		depotID: e.DepotID, date: today(), description: "Reversal of " + e.EntryNo + ": " + reason,
		source: models.SourceReversal, sourceID: &src, reverses: &e.ID, lines: lines,
	})
}

type EntryPage struct {
	Items      []models.JournalEntry `json:"items"`
	NextCursor *string               `json:"nextCursor"`
}

type EntryQuery struct {
	DepotID    uint
	From, To   *models.Date
	AccountID  uint
	SourceType string
	Search     string
	Limit      int
	Cursor     string
}

func (s *LedgerService) ListEntries(ctx context.Context, orgID uint, in EntryQuery) (*EntryPage, error) {
	limit := clampLimit(in.Limit)
	f := repository.EntryFilter{DepotID: in.DepotID, From: in.From, To: in.To, AccountID: in.AccountID,
		SourceType: in.SourceType, Search: strings.TrimSpace(in.Search), Limit: limit + 1}
	if in.Cursor != "" {
		c, err := decodeEntryCursor(in.Cursor)
		if err != nil {
			return nil, err
		}
		f.Before = c
	}
	rows, err := s.store.Read(ctx).ListEntries(orgID, f)
	if err != nil {
		return nil, err
	}
	page := &EntryPage{Items: rows}
	if len(rows) > limit {
		page.Items = rows[:limit]
		last := rows[limit-1]
		c := base64.RawURLEncoding.EncodeToString([]byte(last.EntryDate.String() + "|" + strconv.FormatUint(uint64(last.ID), 10)))
		page.NextCursor = &c
	}
	return page, nil
}

func decodeEntryCursor(s string) (*repository.EntryCursor, error) {
	bad := apierror.WithField(apierror.InvalidRequest, "cursor")
	b, err := base64.RawURLEncoding.DecodeString(s)
	if err != nil {
		return nil, bad
	}
	d, id, ok := strings.Cut(string(b), "|")
	if !ok {
		return nil, bad
	}
	date, err := models.ParseDate(d)
	if err != nil {
		return nil, bad
	}
	n, err := strconv.ParseUint(id, 10, 32)
	if err != nil {
		return nil, bad
	}
	return &repository.EntryCursor{Date: date, ID: uint(n)}, nil
}
