package repository

import (
	"time"

	"github.com/fpirim/fikopar/backend/internal/models"
)

func (q *Q) InsertEntry(e *models.JournalEntry) error {
	return q.db.Raw(`INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description, source_type,
			source_id, reverses_entry_id, idempotency_key, created_by)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id, created_at`,
		e.OrgID, e.DepotID, e.EntryNo, e.EntryDate, e.Description, e.SourceType,
		e.SourceID, e.ReversesEntryID, e.IdempotencyKey, e.CreatedBy).Row().Scan(&e.ID, &e.CreatedAt)
}

func (q *Q) InsertPosting(orgID uint, p *models.Posting) error {
	return q.db.Raw(`INSERT INTO postings (org_id, entry_id, line_no, account_id, side, amount, currency, due_date, memo)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?, nullif(?, '')) RETURNING id`,
		orgID, p.EntryID, p.LineNo, p.AccountID, p.Side, p.Amount, p.Currency, p.DueDate, p.Memo).Row().Scan(&p.ID)
}

const entrySelect = `SELECT e.id, e.org_id, e.depot_id, e.entry_no, e.entry_date, e.description, e.source_type, e.source_id,
	e.reverses_entry_id, e.idempotency_key, e.created_by, e.created_at,
	(SELECT r.id FROM journal_entries r WHERE r.reverses_entry_id = e.id) AS reversed_by_entry_id,
	coalesce(
	  (SELECT receipt_no FROM receipts x WHERE x.org_id = e.org_id AND e.id IN (x.journal_entry_id, x.voided_by_entry_id) LIMIT 1),
	  (SELECT voucher_no FROM cash_vouchers x WHERE x.org_id = e.org_id AND e.id IN (x.journal_entry_id, x.voided_by_entry_id) LIMIT 1)
	) AS source_no
	FROM journal_entries e`

func (q *Q) GetEntry(orgID, id uint) (*models.JournalEntry, error) {
	e, err := first[models.JournalEntry](q.db, entrySelect+` WHERE e.org_id = ? AND e.id = ?`, orgID, id)
	if err != nil {
		return nil, err
	}
	return e, q.attachPostings([]*models.JournalEntry{e})
}

// LockEntry serialises reversals of one entry.
func (q *Q) LockEntry(orgID, id uint) error {
	return q.db.Exec(`SELECT id FROM journal_entries WHERE org_id = ? AND id = ? FOR UPDATE`, orgID, id).Error
}

type EntryFilter struct {
	DepotID    uint
	From, To   *models.Date
	AccountID  uint
	SourceType string
	Search     string
	Before     *EntryCursor
	Limit      int
}

type EntryCursor struct {
	Date models.Date
	ID   uint
}

func (q *Q) ListEntries(orgID uint, f EntryFilter) ([]models.JournalEntry, error) {
	sql := `SELECT * FROM (` + entrySelect + ` WHERE e.org_id = ?) e WHERE true`
	args := []any{orgID}
	if f.DepotID != 0 {
		sql += ` AND e.depot_id = ?`
		args = append(args, f.DepotID)
	}
	if f.From != nil {
		sql += ` AND e.entry_date >= ?`
		args = append(args, *f.From)
	}
	if f.To != nil {
		sql += ` AND e.entry_date <= ?`
		args = append(args, *f.To)
	}
	if f.AccountID != 0 {
		sql += ` AND EXISTS (SELECT 1 FROM postings p WHERE p.entry_id = e.id AND p.account_id = ?)`
		args = append(args, f.AccountID)
	}
	if f.SourceType != "" {
		sql += ` AND e.source_type = ?`
		args = append(args, f.SourceType)
	}
	if f.Search != "" {
		sql += ` AND (e.entry_no ILIKE ? OR e.source_no ILIKE ?)`
		args = append(args, "%"+f.Search+"%", "%"+f.Search+"%")
	}
	if f.Before != nil {
		sql += ` AND (e.entry_date, e.id) < (?, ?)`
		args = append(args, f.Before.Date, f.Before.ID)
	}
	sql += ` ORDER BY e.entry_date DESC, e.id DESC LIMIT ?`
	args = append(args, f.Limit)
	rows, err := list[models.JournalEntry](q.db, sql, args...)
	if err != nil {
		return nil, err
	}
	ptrs := make([]*models.JournalEntry, len(rows))
	for i := range rows {
		ptrs[i] = &rows[i]
	}
	return rows, q.attachPostings(ptrs)
}

func (q *Q) attachPostings(entries []*models.JournalEntry) error {
	if len(entries) == 0 {
		return nil
	}
	ids := make([]uint, len(entries))
	byID := make(map[uint]*models.JournalEntry, len(entries))
	for i, e := range entries {
		ids[i] = e.ID
		e.Postings = []models.Posting{}
		byID[e.ID] = e
	}
	ps, err := list[models.Posting](q.db, `SELECT p.id, p.entry_id, p.line_no, p.account_id, a.code AS account_code,
			a.name AS account_name, p.side, p.amount, p.currency, p.due_date, coalesce(p.memo, '') AS memo
		FROM postings p JOIN accounts a ON a.id = p.account_id
		WHERE p.entry_id IN ? ORDER BY p.entry_id, p.line_no`, ids)
	if err != nil {
		return err
	}
	for _, p := range ps {
		e := byID[p.EntryID]
		e.Postings = append(e.Postings, p)
	}
	return nil
}

// EntryTrail joins audit rows, outbox events and consumer deliveries for one entry.
// Events of the owning receipt or voucher reference the entry in their payload.
func (q *Q) EntryTrail(orgID, entryID uint) ([]models.TrailItem, error) {
	id := uintText(entryID)
	return list[models.TrailItem](q.db, `
		WITH ev AS (
		  SELECT * FROM outbox WHERE org_id = ? AND (
		    (aggregate_type = 'journal_entry' AND aggregate_id = ?) OR payload->>'journalEntryId' = ?)
		)
		SELECT at, kind, label, ref, actor_user_id FROM (
		  SELECT occurred_at AS at, 'AUDIT' AS kind, action AS label, request_id AS ref, actor_user_id, 0 AS rank
		    FROM audit_events WHERE org_id = ? AND entity_type = 'journal_entry' AND entity_id = ?
		  UNION ALL
		  SELECT occurred_at, 'OUTBOX', event_type, id::text, actor_user_id, 1 FROM ev
		  UNION ALL
		  SELECT d.delivered_at, 'DELIVERY', d.consumer, ev.id::text, NULL, 2
		    FROM outbox_deliveries d JOIN ev ON ev.id = d.event_id
		) t ORDER BY at, rank, label`, orgID, id, id, orgID, id)
}

type OutboxEvent struct {
	ID               string    `gorm:"column:id"`
	OrgID            uint      `gorm:"column:org_id"`
	DepotID          uint      `gorm:"column:depot_id"`
	EventType        string    `gorm:"column:event_type"`
	EventVersion     int       `gorm:"column:event_version"`
	AggregateType    string    `gorm:"column:aggregate_type"`
	AggregateID      string    `gorm:"column:aggregate_id"`
	AggregateVersion *int      `gorm:"column:aggregate_version"`
	Payload          string    `gorm:"column:payload"`
	CorrelationID    *string   `gorm:"column:correlation_id"`
	ActorUserID      *uint     `gorm:"column:actor_user_id"`
	OccurredAt       time.Time `gorm:"column:occurred_at"`
}

func (q *Q) AppendOutbox(e OutboxEvent) error {
	return q.db.Exec(`INSERT INTO outbox (id, org_id, depot_id, event_type, event_version, aggregate_type, aggregate_id,
			aggregate_version, payload, correlation_id, actor_user_id)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?)`,
		e.ID, e.OrgID, e.DepotID, e.EventType, e.EventVersion, e.AggregateType, e.AggregateID,
		e.AggregateVersion, e.Payload, e.CorrelationID, e.ActorUserID).Error
}

const maxOutboxAttempts = 10

func (q *Q) PendingOutboxIDs(limit int) ([]string, error) {
	var ids []string
	err := q.db.Raw(`SELECT id::text FROM outbox WHERE published_at IS NULL AND attempts < ?
		ORDER BY occurred_at, id LIMIT ?`, maxOutboxAttempts, limit).Scan(&ids).Error
	return ids, err
}

// LockOutboxEvent returns nil when another relay holds the row or it was published meanwhile.
func (q *Q) LockOutboxEvent(id string) (*OutboxEvent, error) {
	rows, err := list[OutboxEvent](q.db, `SELECT id::text AS id, org_id, depot_id, event_type, event_version, aggregate_type,
			aggregate_id, aggregate_version, payload::text AS payload, correlation_id, actor_user_id, occurred_at
		FROM outbox WHERE id = ? AND published_at IS NULL FOR UPDATE SKIP LOCKED`, id)
	if err != nil || len(rows) == 0 {
		return nil, err
	}
	return &rows[0], nil
}

func (q *Q) MarkOutboxPublished(id string) error {
	return q.db.Exec(`UPDATE outbox SET published_at = now(), attempts = attempts + 1, last_error = NULL WHERE id = ?`, id).Error
}

func (q *Q) RecordOutboxFailure(id, msg string) error {
	return q.db.Exec(`UPDATE outbox SET attempts = attempts + 1, last_error = ? WHERE id = ?`, msg, id).Error
}

// MarkDelivered returns false when the consumer already processed the event.
func (q *Q) MarkDelivered(eventID, consumer string) (bool, error) {
	res := q.db.Exec(`INSERT INTO outbox_deliveries (event_id, consumer) VALUES (?, ?) ON CONFLICT DO NOTHING`, eventID, consumer)
	return res.RowsAffected == 1, res.Error
}

// EntryUnbalancedCurrencies counts currencies whose postings in the entry do not net to zero.
func (q *Q) EntryUnbalancedCurrencies(entryID uint) (int64, error) {
	var n int64
	err := q.db.Raw(`SELECT count(*) FROM (SELECT currency FROM postings WHERE entry_id = ?
		GROUP BY currency HAVING sum(CASE side WHEN 'DEBIT' THEN amount ELSE -amount END) <> 0) s`, entryID).Scan(&n).Error
	return n, err
}

func (q *Q) OrgDriftCount(orgID uint) (int64, error) {
	var n int64
	err := q.db.Raw(`SELECT count(*) FROM account_balance_drift d JOIN accounts a ON a.id = d.account_id WHERE a.org_id = ?`, orgID).Scan(&n).Error
	return n, err
}

func (q *Q) RecordLedgerCheck(orgID uint, balanced bool) error {
	status := "DRIFT"
	if balanced {
		status = "BALANCED"
	}
	return q.db.Exec(`INSERT INTO ledger_checks (org_id, status, entries_checked) VALUES (?, ?, 1)
		ON CONFLICT (org_id) DO UPDATE SET status = EXCLUDED.status, checked_at = now(),
			entries_checked = ledger_checks.entries_checked + 1`, orgID, status).Error
}
