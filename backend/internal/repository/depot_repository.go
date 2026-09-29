package repository

import "github.com/fpirim/fikopar/backend/internal/models"

const depotCols = `id, org_id, code, name, vat_rate::text AS vat_rate, version`

func (q *Q) ListDepots(orgID uint) ([]models.Depot, error) {
	return list[models.Depot](q.db, `SELECT `+depotCols+` FROM depots WHERE org_id = ? ORDER BY code`, orgID)
}

func (q *Q) GetDepot(orgID, id uint) (*models.Depot, error) {
	return first[models.Depot](q.db, `SELECT `+depotCols+` FROM depots WHERE org_id = ? AND id = ?`, orgID, id)
}

func (q *Q) CreateDepot(d *models.Depot) error {
	return q.db.Raw(`INSERT INTO depots (org_id, code, name, vat_rate)
		VALUES (?, ?, ?, ?) RETURNING `+depotCols,
		d.OrgID, d.Code, d.Name, d.VatRate).Scan(d).Error
}
