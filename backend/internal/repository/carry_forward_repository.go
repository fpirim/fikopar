package repository

import (
	"strconv"
	"time"

	"github.com/fpirim/fikopar/backend/internal/models"
)

type ClosingRow struct {
	AccountID   uint         `gorm:"column:account_id"`
	AccountCode string       `gorm:"column:account_code"`
	AccountName string       `gorm:"column:account_name"`
	DepotID     uint         `gorm:"column:depot_id"`
	Closing     models.Money `gorm:"column:closing"`
}

// ClosingBalances returns every (account, depot) with a non-zero balance at the end of asOf.
func (q *Q) ClosingBalances(orgID uint, asOf models.Date, depotID *uint) ([]ClosingRow, error) {
	return list[ClosingRow](q.db, `
		SELECT a.id AS account_id, a.code AS account_code, a.name AS account_name, e.depot_id,
		       sum(CASE p.side WHEN 'DEBIT' THEN p.amount ELSE -p.amount END) AS closing
		FROM postings p
		JOIN journal_entries e ON e.id = p.entry_id
		JOIN accounts a ON a.id = p.account_id
		WHERE e.org_id = ? AND e.entry_date >= ledger_start(e.org_id, ?) AND e.entry_date <= ?
		  AND (?::bigint IS NULL OR e.depot_id = ?)
		GROUP BY a.id, e.depot_id
		HAVING sum(CASE p.side WHEN 'DEBIT' THEN p.amount ELSE -p.amount END) <> 0
		ORDER BY a.code, e.depot_id`, orgID, asOf, asOf, depotID, depotID)
}

// LedgerNet is Σ debit − Σ credit over the whole company at the end of asOf; zero by construction.
func (q *Q) LedgerNet(orgID uint, asOf models.Date) (models.Money, error) {
	var m models.Money
	err := q.db.Raw(`SELECT coalesce(sum(CASE p.side WHEN 'DEBIT' THEN p.amount ELSE -p.amount END), 0)
		FROM postings p JOIN journal_entries e ON e.id = p.entry_id
		WHERE e.org_id = ? AND e.entry_date >= ledger_start(e.org_id, ?) AND e.entry_date <= ?`, orgID, asOf, asOf).Row().Scan(&m)
	return m, err
}

type Override struct {
	AccountID   uint         `gorm:"column:account_id"`
	AccountCode string       `gorm:"column:account_code"`
	AccountName string       `gorm:"column:account_name"`
	DepotID     uint         `gorm:"column:depot_id"`
	Debit       models.Money `gorm:"column:debit"`
	Credit      models.Money `gorm:"column:credit"`
	Reason      string       `gorm:"column:reason"`
}

func (q *Q) CarryForwardOverrides(orgID uint, year int, depotID *uint) ([]Override, error) {
	return list[Override](q.db, `SELECT o.account_id, a.code AS account_code, a.name AS account_name, o.depot_id, o.debit, o.credit, o.reason
		FROM carry_forward_overrides o JOIN accounts a ON a.id = o.account_id
		WHERE o.org_id = ? AND o.year = ? AND (?::bigint IS NULL OR o.depot_id = ?)`, orgID, year, depotID, depotID)
}

func (q *Q) UpsertCarryForwardOverride(orgID uint, year int, o Override, by *uint) error {
	return q.db.Exec(`INSERT INTO carry_forward_overrides (org_id, year, account_id, depot_id, debit, credit, reason, created_by)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?)
		ON CONFLICT (org_id, year, account_id, depot_id) DO UPDATE
		SET debit = EXCLUDED.debit, credit = EXCLUDED.credit, reason = EXCLUDED.reason,
		    created_by = EXCLUDED.created_by, created_at = now()`,
		orgID, year, o.AccountID, o.DepotID, o.Debit, o.Credit, o.Reason, by).Error
}

func (q *Q) DeleteCarryForwardOverride(orgID uint, year int, accountID, depotID uint) error {
	return q.db.Exec(`DELETE FROM carry_forward_overrides WHERE org_id = ? AND year = ? AND account_id = ? AND depot_id = ?`,
		orgID, year, accountID, depotID).Error
}

// LockCarryForward serialises changes to one company's year.
func (q *Q) LockCarryForward(orgID uint, year int) error {
	return q.db.Exec(`SELECT pg_advisory_xact_lock(hashtextextended(?, 0))`,
		"carry-forward:"+strconv.FormatUint(uint64(orgID), 10)+":"+strconv.Itoa(year)).Error
}

// LockLedger orders postings against carry-forward runs: every entry holds it shared, posting or
// reopening a run holds it exclusively, so no entry lands in a year while it is being closed.
func (q *Q) LockLedger(orgID uint, exclusive bool) error {
	fn := "pg_advisory_xact_lock_shared"
	if exclusive {
		fn = "pg_advisory_xact_lock"
	}
	return q.db.Exec(`SELECT `+fn+`(hashtextextended(?, 0))`, "ledger:"+strconv.FormatUint(uint64(orgID), 10)).Error
}

// YearClosed reports whether an active run lies after the date's year.
func (q *Q) YearClosed(orgID uint, d models.Date) (bool, error) {
	var closed bool
	err := q.db.Raw(`SELECT EXISTS (SELECT 1 FROM carry_forward_runs WHERE org_id = ? AND reopened_at IS NULL AND year > ?)`,
		orgID, d.Year()).Scan(&closed).Error
	return closed, err
}

type CarryForwardRun struct {
	ID           uint       `gorm:"column:id"`
	Year         int        `gorm:"column:year"`
	PostedAt     time.Time  `gorm:"column:posted_at"`
	ReopenedAt   *time.Time `gorm:"column:reopened_at"`
	ReopenReason *string    `gorm:"column:reopen_reason"`
}

// CarryForwardRuns returns the year's active run and its latest reopened run; either may be nil.
func (q *Q) CarryForwardRuns(orgID uint, year int) (active, reopened *CarryForwardRun, err error) {
	runs, err := list[CarryForwardRun](q.db, `SELECT id, year, posted_at, reopened_at, reopen_reason FROM carry_forward_runs
		WHERE org_id = ? AND year = ? ORDER BY id DESC`, orgID, year)
	for i := range runs {
		r := &runs[i]
		switch {
		case r.ReopenedAt == nil && active == nil:
			active = r
		case r.ReopenedAt != nil && reopened == nil:
			reopened = r
		}
	}
	return active, reopened, err
}

func (q *Q) CarryForwardPosted(orgID uint, year int) (bool, error) {
	active, _, err := q.CarryForwardRuns(orgID, year)
	return active != nil, err
}

func (q *Q) InsertCarryForwardRun(orgID uint, year, entries int, debit, credit models.Money, by *uint) (uint, error) {
	var id uint
	err := q.db.Raw(`INSERT INTO carry_forward_runs (org_id, year, entry_count, total_debit, total_credit, posted_by)
		VALUES (?, ?, ?, ?, ?, ?) RETURNING id`, orgID, year, entries, debit, credit, by).Row().Scan(&id)
	return id, err
}

func (q *Q) ReopenCarryForwardRun(runID uint, by *uint, reason string) error {
	return q.db.Exec(`UPDATE carry_forward_runs SET reopened_by = ?, reopened_at = now(), reopen_reason = ? WHERE id = ?`,
		by, reason, runID).Error
}

func (q *Q) InsertOpeningBalance(orgID, runID uint, ob models.OpeningBalance) error {
	return q.db.Exec(`INSERT INTO opening_balances (org_id, account_id, depot_id, year, debit, credit, source, run_id, journal_entry_id)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`, orgID, ob.AccountID, ob.DepotID, ob.Year, ob.Debit, ob.Credit, ob.Source, runID, ob.JournalEntryID).Error
}

// RunOpeningEntryIDs lists the journal entries of a run's openings that are not voided yet.
func (q *Q) RunOpeningEntryIDs(runID uint) ([]uint, error) {
	var ids []uint
	err := q.db.Raw(`SELECT journal_entry_id FROM opening_balances WHERE run_id = ? AND voided_by_entry_id IS NULL
		ORDER BY journal_entry_id`, runID).Scan(&ids).Error
	return ids, err
}

func (q *Q) VoidOpening(entryID, reversalID uint) error {
	return q.db.Exec(`UPDATE opening_balances SET voided_by_entry_id = ? WHERE journal_entry_id = ?`, reversalID, entryID).Error
}

// PreviewVersion is the ETag of a year's carry-forward preview; 1 until the first change.
func (q *Q) PreviewVersion(orgID uint, year int) (int, error) {
	var v int
	err := q.db.Raw(`SELECT coalesce((SELECT version FROM carry_forward_previews WHERE org_id = ? AND year = ?), 1)`, orgID, year).Scan(&v).Error
	return v, err
}

func (q *Q) BumpPreviewVersion(orgID uint, year int) error {
	return q.db.Exec(`INSERT INTO carry_forward_previews (org_id, year, version) VALUES (?, ?, 2)
		ON CONFLICT (org_id, year) DO UPDATE SET version = carry_forward_previews.version + 1`, orgID, year).Error
}

// ActiveRunYears lists carried-forward years in [from, to].
func (q *Q) ActiveRunYears(orgID uint, from, to int) ([]int, error) {
	var years []int
	err := q.db.Raw(`SELECT year FROM carry_forward_runs WHERE org_id = ? AND reopened_at IS NULL AND year BETWEEN ? AND ?
		ORDER BY year`, orgID, from, to).Scan(&years).Error
	return years, err
}

type OpeningRow struct {
	models.OpeningBalance
	AccountCode string `gorm:"column:account_code"`
	AccountName string `gorm:"column:account_name"`
}

func (q *Q) OpeningBalances(orgID uint, year int, depotID *uint) ([]OpeningRow, error) {
	return list[OpeningRow](q.db, `SELECT o.account_id, o.depot_id, o.year, o.debit, o.credit, o.journal_entry_id, o.source,
			a.code AS account_code, a.name AS account_name
		FROM opening_balances o JOIN accounts a ON a.id = o.account_id
		WHERE o.org_id = ? AND o.year = ? AND o.voided_by_entry_id IS NULL AND (?::bigint IS NULL OR o.depot_id = ?)
		ORDER BY a.code, o.depot_id`, orgID, year, depotID, depotID)
}

func (q *Q) RoleAccountIDs(orgID uint, role string) ([]uint, error) {
	var ids []uint
	err := q.db.Raw(`SELECT account_id FROM account_roles WHERE org_id = ? AND role = ?`, orgID, role).Scan(&ids).Error
	return ids, err
}

// OpenCountsThrough lists OPEN stock counts dated on or before d.
func (q *Q) OpenCountsThrough(orgID uint, d models.Date) ([]string, error) {
	var nos []string
	err := q.db.Raw(`SELECT count_no FROM stock_counts WHERE org_id = ? AND status = 'OPEN' AND count_date <= ? ORDER BY count_no`,
		orgID, d).Scan(&nos).Error
	return nos, err
}

// DepotsWithoutFinalizedCount lists depot codes that have no FINALIZED count dated in the year.
func (q *Q) DepotsWithoutFinalizedCount(orgID uint, year int) ([]string, error) {
	var codes []string
	err := q.db.Raw(`SELECT d.code FROM depots d WHERE d.org_id = ? AND NOT EXISTS (
		SELECT 1 FROM stock_counts c WHERE c.org_id = d.org_id AND c.depot_id = d.id AND c.status = 'FINALIZED' AND c.year = ?)
		ORDER BY d.code`, orgID, year).Scan(&codes).Error
	return codes, err
}
