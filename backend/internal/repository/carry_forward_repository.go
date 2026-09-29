package repository

import (
	"strconv"

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

// LockCarryForward serialises posting of one company's year.
func (q *Q) LockCarryForward(orgID uint, year int) error {
	return q.db.Exec(`SELECT pg_advisory_xact_lock(hashtextextended(?, 0))`,
		"carry-forward:"+strconv.FormatUint(uint64(orgID), 10)+":"+strconv.Itoa(year)).Error
}

func (q *Q) CarryForwardPosted(orgID uint, year int) (bool, error) {
	var n int64
	err := q.db.Raw(`SELECT count(*) FROM carry_forward_runs WHERE org_id = ? AND year = ?`, orgID, year).Scan(&n).Error
	return n > 0, err
}

func (q *Q) InsertCarryForwardRun(orgID uint, year, entries int, debit, credit models.Money, by *uint) error {
	return q.db.Exec(`INSERT INTO carry_forward_runs (org_id, year, entry_count, total_debit, total_credit, posted_by)
		VALUES (?, ?, ?, ?, ?, ?)`, orgID, year, entries, debit, credit, by).Error
}

func (q *Q) InsertOpeningBalance(orgID uint, ob models.OpeningBalance) error {
	return q.db.Exec(`INSERT INTO opening_balances (org_id, account_id, depot_id, year, debit, credit, source, journal_entry_id)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?)`, orgID, ob.AccountID, ob.DepotID, ob.Year, ob.Debit, ob.Credit, ob.Source, ob.JournalEntryID).Error
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
		WHERE o.org_id = ? AND o.year = ? AND (?::bigint IS NULL OR o.depot_id = ?)
		ORDER BY a.code, o.depot_id`, orgID, year, depotID, depotID)
}

func (q *Q) RoleAccountIDs(orgID uint, role string) ([]uint, error) {
	var ids []uint
	err := q.db.Raw(`SELECT account_id FROM account_roles WHERE org_id = ? AND role = ?`, orgID, role).Scan(&ids).Error
	return ids, err
}
