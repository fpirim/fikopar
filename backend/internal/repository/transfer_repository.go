package repository

import "github.com/fpirim/fikopar/backend/internal/models"

func (q *Q) InsertTransfer(orgID uint, t *models.StockTransfer, createdBy *uint) error {
	return q.db.Raw(`INSERT INTO stock_transfers (org_id, transfer_no, from_depot_id, to_depot_id, transfer_date, note, created_by)
		VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id`,
		orgID, t.TransferNo, t.FromDepotID, t.ToDepotID, t.Date, t.Note, createdBy).Row().Scan(&t.ID)
}

func (q *Q) GetTransfer(orgID, id uint) (*models.StockTransfer, error) {
	return first[models.StockTransfer](q.db, `SELECT t.id, t.transfer_no, t.from_depot_id, t.to_depot_id, t.transfer_date, t.note,
		split_part(u.email, '@', 1) AS created_by, t.created_at
		FROM stock_transfers t LEFT JOIN users u ON u.id = t.created_by WHERE t.org_id = ? AND t.id = ?`, orgID, id)
}

func (q *Q) TransferMovementIDs(orgID, id uint) ([]uint, error) {
	var ids []uint
	err := q.db.Raw(`SELECT id FROM stock_movements WHERE org_id = ? AND transfer_id = ? ORDER BY id`, orgID, id).Scan(&ids).Error
	return ids, err
}
