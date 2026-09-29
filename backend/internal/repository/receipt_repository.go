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

func (q *Q) GridReceipts(orgID uint, req GridRequest) (*GridResponse, error) {
	cols := map[string]string{
		"receiptNo": "receipt_no", "date": "receipt_date", "kind": "kind", "method": "method",
		"sicilName": "sicil_name", "amount": "amount", "depotId": "depot_id", "origin": "origin",
		"instrumentNo": "instrument_no",
	}
	base := q.db.Table("("+receiptSelect+" WHERE r.org_id = ?) AS g", orgID)
	req.SortModel = append(req.SortModel, SortModel{ColID: "date", Sort: "desc"}, SortModel{ColID: "receiptNo", Sort: "desc"})
	return paginatedGridWithMapping(base, req, &[]models.Receipt{}, cols, []string{"receipt_no", "sicil_name"})
}
