package repository

import "github.com/fpirim/fikopar/backend/internal/models"

type SicilRef struct {
	ID        uint   `gorm:"column:id"`
	HesapNo   string `gorm:"column:hesap_no"`
	HesapAdi  string `gorm:"column:hesap_adi"`
	SicilTipi string `gorm:"column:sicil_tipi"`
}

func (q *Q) GetSicilRef(orgID, id uint) (*SicilRef, error) {
	return first[SicilRef](q.db, `SELECT id, hesap_no, hesap_adi, sicil_tipi FROM sicils WHERE organization_id = ? AND id = ?`, orgID, id)
}

const receiptSelect = `SELECT r.id, r.org_id, r.receipt_no, r.kind, r.origin, r.depot_id, r.sicil_id, s.hesap_adi AS sicil_name,
	r.receipt_date, r.method, r.amount, r.currency, r.bank_account_id, coalesce(r.description, '') AS description,
	r.instrument_id, i.instrument_no, r.journal_entry_id, r.voided_by_entry_id
	FROM receipts r
	JOIN sicils s ON s.id = r.sicil_id
	LEFT JOIN instruments i ON i.id = r.instrument_id`

func (q *Q) InsertReceipt(orgID uint, r *models.Receipt, createdBy *uint) error {
	return q.db.Raw(`INSERT INTO receipts (org_id, depot_id, receipt_no, kind, origin, sicil_id, receipt_date, method,
			bank_account_id, amount, currency, description, journal_entry_id, created_by)
		VALUES (?, ?, ?, ?, 'MANUAL', ?, ?, ?, ?, ?, ?, nullif(?, ''), ?, ?) RETURNING id`,
		orgID, r.DepotID, r.ReceiptNo, r.Kind, r.SicilID, r.Date, r.Method, r.BankAccountID, r.Amount, r.Currency,
		r.Description, r.JournalEntryID, createdBy).Row().Scan(&r.ID)
}

func (q *Q) GetReceipt(orgID, id uint) (*models.Receipt, error) {
	return first[models.Receipt](q.db, receiptSelect+` WHERE r.org_id = ? AND r.id = ?`, orgID, id)
}

func (q *Q) LockReceipt(orgID, id uint) error {
	return q.db.Exec(`SELECT id FROM receipts WHERE org_id = ? AND id = ? FOR UPDATE`, orgID, id).Error
}

func (q *Q) VoidReceipt(orgID, id, entryID uint) error {
	return q.db.Exec(`UPDATE receipts SET voided_by_entry_id = ?, voided_at = now() WHERE org_id = ? AND id = ?`, entryID, orgID, id).Error
}

// ReceiptGridRequest is gridReceipts: AG Grid model plus the top-bar depot (nil = all depots).
type ReceiptGridRequest struct {
	ReportGridRequest
	DepotID *uint `json:"depotId"`
}

var receiptCols = map[string]GridCol{
	"receiptNo": {"receipt_no", ColText}, "date": {"receipt_date", ColDate}, "kind": {"kind", ColText},
	"method": {"method", ColText}, "sicilName": {"sicil_name", ColText}, "amount": {"amount", ColNumber},
	"depotId": {"depot_id", ColNumber}, "origin": {"origin", ColText}, "instrumentNo": {"instrument_no", ColText},
}

func (q *Q) GridReceipts(orgID uint, req ReceiptGridRequest) (*GridResponse, error) {
	where, wargs, err := whereClause(req.FilterModel, receiptCols)
	if err != nil {
		return nil, err
	}
	search, sargs := searchClause(req.Search, []string{"receipt_no", "sicil_name"})
	base := `SELECT * FROM (` + receiptSelect + ` WHERE r.org_id = ? AND (?::bigint IS NULL OR r.depot_id = ?)) g WHERE ` + where + ` AND ` + search
	args := concat([]any{orgID, req.DepotID, req.DepotID}, wargs, sargs)
	var total int64
	if err := q.db.Raw(`SELECT count(*) FROM (`+base+`) c`, args...).Scan(&total).Error; err != nil {
		return nil, err
	}
	sql := base + ` ORDER BY ` + orderClause(req.SortModel, receiptCols, "receipt_date DESC, receipt_no DESC")
	if n := req.EndRow - req.StartRow; n > 0 {
		sql += ` LIMIT ? OFFSET ?`
		args = append(args, n, req.StartRow)
	}
	rows, err := list[models.Receipt](q.db, sql, args...)
	if err != nil {
		return nil, err
	}
	return &GridResponse{Rows: rows, TotalCount: total}, nil
}
