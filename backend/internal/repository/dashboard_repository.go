package repository

import (
	"time"

	"github.com/fpirim/fikopar/backend/internal/models"
)

// OpenBalances sums the debit-positive balances of accounts under a code prefix as of a date; sign +1 counts
// accounts owed to the company, −1 accounts the company owes.
func (q *Q) OpenBalances(orgID uint, prefix string, sign int, asOf models.Date, depotID *uint) (models.Money, int, error) {
	var total models.Money
	var n int
	err := q.db.Raw(`SELECT coalesce(sum(? * b), 0), count(*) FROM (
		SELECT a.id, sum(CASE p.side WHEN 'DEBIT' THEN p.amount ELSE -p.amount END) AS b
		FROM postings p JOIN journal_entries e ON e.id = p.entry_id JOIN accounts a ON a.id = p.account_id
		WHERE e.org_id = ? AND a.code LIKE ? AND `+inWindow+` AND (?::bigint IS NULL OR e.depot_id = ?)
		GROUP BY a.id) x WHERE ? * b > 0`, sign, orgID, prefix+"%", asOf, asOf, depotID, depotID, sign).Row().Scan(&total, &n)
	return total, n, err
}

type InstrumentDue struct {
	InstrumentID uint         `json:"instrumentId" gorm:"column:id"`
	InstrumentNo string       `json:"instrumentNo" gorm:"column:instrument_no"`
	SicilName    string       `json:"sicilName" gorm:"column:sicil_name"`
	DueDate      models.Date  `json:"dueDate" gorm:"column:due_date"`
	DaysToDue    int          `json:"daysToDue" gorm:"column:days_to_due"`
	Remaining    models.Money `json:"remaining" gorm:"column:remaining"`
}

const openReceivables = `i.org_id = ? AND i.type IN ('RECEIVABLE_CHEQUE','RECEIVABLE_NOTE') AND i.remaining > 0
	AND i.status NOT IN ('COLLECTED','ENDORSED','RETURNED','PAID') AND (?::bigint IS NULL OR i.depot_id = ?)`

// InstrumentsDue sums open receivable instruments due within days of the date and counts the overdue ones.
func (q *Q) InstrumentsDue(orgID uint, on models.Date, days int, depotID *uint) (models.Money, int, error) {
	var total models.Money
	var overdue int
	err := q.db.Raw(`SELECT coalesce(sum(i.remaining) FILTER (WHERE i.due_date <= ?::date + ?::int), 0),
		count(*) FILTER (WHERE i.due_date < ?::date) FROM instruments i WHERE `+openReceivables,
		on, days, on, orgID, depotID, depotID).Row().Scan(&total, &overdue)
	return total, overdue, err
}

func (q *Q) InstrumentsDueList(orgID uint, on models.Date, days int, depotID *uint, limit int) ([]InstrumentDue, error) {
	return list[InstrumentDue](q.db, `SELECT i.id, i.instrument_no, s.hesap_adi AS sicil_name, i.due_date,
		(i.due_date - ?::date) AS days_to_due, i.remaining
		FROM instruments i JOIN sicils s ON s.id = i.sicil_id
		WHERE `+openReceivables+` AND i.due_date <= ?::date + ?::int
		ORDER BY i.due_date, i.id LIMIT ?`, on, orgID, depotID, depotID, on, days, limit)
}

type LedgerCheckRow struct {
	Status    string     `gorm:"column:status"`
	CheckedAt *time.Time `gorm:"column:checked_at"`
}

func (q *Q) LastLedgerCheck(orgID uint) (*LedgerCheckRow, error) {
	rows, err := list[LedgerCheckRow](q.db, `SELECT status, checked_at FROM ledger_checks WHERE org_id = ?`, orgID)
	if err != nil || len(rows) == 0 {
		return nil, err
	}
	return &rows[0], nil
}
