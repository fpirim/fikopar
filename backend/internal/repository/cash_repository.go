package repository

import (
	"github.com/fpirim/fikopar/backend/internal/models"
)

// Balance window: the latest carried-forward year up to the date starts the books; opening
// entries of that year are included and earlier postings are summarised by them.
const inWindow = `e.entry_date >= ledger_start(e.org_id, ?) AND e.entry_date <= ?`

// AccountBalanceAsOf is Σ debit − Σ credit on the account for one depot, dates inclusive.
func (q *Q) AccountBalanceAsOf(accountID, depotID uint, asOf models.Date) (models.Money, error) {
	var m models.Money
	err := q.db.Raw(`SELECT coalesce(sum(CASE p.side WHEN 'DEBIT' THEN p.amount ELSE -p.amount END), 0)
		FROM postings p JOIN journal_entries e ON e.id = p.entry_id
		WHERE p.account_id = ? AND e.depot_id = ? AND `+inWindow, accountID, depotID, asOf, asOf).Row().Scan(&m)
	return m, err
}

// OpeningBalanceAt is the balance at the start of the day: the window of `from` up to the day
// before, with that year's opening entries folded in.
func (q *Q) OpeningBalanceAt(accountID, depotID uint, from models.Date) (models.Money, error) {
	var m models.Money
	err := q.db.Raw(`SELECT coalesce(sum(CASE p.side WHEN 'DEBIT' THEN p.amount ELSE -p.amount END), 0)
		FROM postings p JOIN journal_entries e ON e.id = p.entry_id
		WHERE p.account_id = ? AND e.depot_id = ? AND e.entry_date >= ledger_start(e.org_id, ?)
		  AND (e.entry_date < ? OR (e.source_type = 'opening' AND e.entry_date = ?))`,
		accountID, depotID, from, from, from).Row().Scan(&m)
	return m, err
}

type CashLine struct {
	Date           models.Date  `gorm:"column:entry_date"`
	Kind           string       `gorm:"column:kind"`
	DocumentNo     *string      `gorm:"column:document_no"`
	ReceiptID      *uint        `gorm:"column:receipt_id"`
	VoucherID      *uint        `gorm:"column:voucher_id"`
	JournalEntryID uint         `gorm:"column:journal_entry_id"`
	SicilID        *uint        `gorm:"column:sicil_id"`
	SicilName      *string      `gorm:"column:sicil_name"`
	Description    string       `gorm:"column:description"`
	Income         models.Money `gorm:"column:income"`
	Expense        models.Money `gorm:"column:expense"`
}

func (q *Q) CashLines(orgID, accountID, depotID uint, from, to models.Date) ([]CashLine, error) {
	return list[CashLine](q.db, `
		SELECT e.entry_date, e.id AS journal_entry_id, e.description,
		       coalesce(r.receipt_no, v.voucher_no, e.entry_no) AS document_no,
		       CASE WHEN r.id IS NOT NULL THEN r.kind WHEN v.id IS NOT NULL THEN v.kind
		            WHEN e.source_type = 'cash_count' THEN 'CASH_COUNT' ELSE 'OTHER' END AS kind,
		       r.id AS receipt_id, v.id AS voucher_id, r.sicil_id, s.hesap_adi AS sicil_name,
		       CASE p.side WHEN 'DEBIT' THEN p.amount ELSE 0 END AS income,
		       CASE p.side WHEN 'CREDIT' THEN p.amount ELSE 0 END AS expense
		FROM postings p
		JOIN journal_entries e ON e.id = p.entry_id
		LEFT JOIN receipts r ON r.org_id = e.org_id AND e.id IN (r.journal_entry_id, r.voided_by_entry_id)
		LEFT JOIN cash_vouchers v ON v.org_id = e.org_id AND e.id IN (v.journal_entry_id, v.voided_by_entry_id)
		LEFT JOIN sicils s ON s.id = r.sicil_id
		WHERE e.org_id = ? AND p.account_id = ? AND e.depot_id = ? AND e.entry_date BETWEEN ? AND ?
		  AND e.source_type <> 'opening'
		ORDER BY e.entry_date, e.id, p.line_no`, orgID, accountID, depotID, from, to)
}

const voucherCols = `id, voucher_no, kind, depot_id, voucher_date, account_id, amount, description, journal_entry_id, voided_by_entry_id`

func (q *Q) InsertCashVoucher(orgID uint, v *models.CashVoucher, createdBy *uint) error {
	return q.db.Raw(`INSERT INTO cash_vouchers (org_id, depot_id, voucher_no, kind, voucher_date, account_id, amount,
			description, journal_entry_id, created_by)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id`,
		orgID, v.DepotID, v.VoucherNo, v.Kind, v.Date, v.AccountID, v.Amount, v.Description, v.JournalEntryID, createdBy).Row().Scan(&v.ID)
}

func (q *Q) GetCashVoucherForUpdate(orgID, id uint) (*models.CashVoucher, error) {
	return first[models.CashVoucher](q.db, `SELECT `+voucherCols+` FROM cash_vouchers WHERE org_id = ? AND id = ? FOR UPDATE`, orgID, id)
}

func (q *Q) GetCashVoucher(orgID, id uint) (*models.CashVoucher, error) {
	return first[models.CashVoucher](q.db, `SELECT `+voucherCols+` FROM cash_vouchers WHERE org_id = ? AND id = ?`, orgID, id)
}

func (q *Q) VoidCashVoucher(orgID, id, entryID uint) error {
	return q.db.Exec(`UPDATE cash_vouchers SET voided_by_entry_id = ? WHERE org_id = ? AND id = ?`, entryID, orgID, id).Error
}

const countCols = `id, depot_id, counted_amount, coalesce(note, '') AS note, counted_at, book_balance, difference, journal_entry_id, counted_by`

func (q *Q) NextCashCountID() (uint, error) {
	var id uint
	err := q.db.Raw(`SELECT nextval(pg_get_serial_sequence('cash_counts', 'id'))`).Row().Scan(&id)
	return id, err
}

func (q *Q) InsertCashCount(orgID uint, c *models.CashCount) error {
	return q.db.Raw(`INSERT INTO cash_counts (id, org_id, depot_id, book_balance, counted_amount, journal_entry_id, note, counted_by)
		VALUES (?, ?, ?, ?, ?, ?, nullif(?, ''), ?) RETURNING `+countCols,
		c.ID, orgID, c.DepotID, c.BookBalance, c.CountedAmount, c.JournalEntryID, c.Note, c.CountedBy).Scan(c).Error
}

func (q *Q) ListCashCounts(orgID, depotID, beforeID uint, limit int) ([]models.CashCount, error) {
	sql := `SELECT ` + countCols + ` FROM cash_counts WHERE org_id = ? AND depot_id = ?`
	args := []any{orgID, depotID}
	if beforeID != 0 {
		sql += ` AND id < ?`
		args = append(args, beforeID)
	}
	return list[models.CashCount](q.db, sql+` ORDER BY id DESC LIMIT ?`, append(args, limit)...)
}
