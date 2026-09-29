package repository

import "github.com/fpirim/fikopar/backend/internal/models"

const catalogSelect = `SELECT c.id, c.code, c.name, c.supplier_sicil_id, s.hesap_adi AS supplier_name, c.is_active, c.currency,
	c.content_version, c.version,
	(SELECT count(*) FROM parts p WHERE p.catalog_id = c.id AND p.is_active) AS part_count,
	(SELECT max(i.applied_at) FROM price_imports i WHERE i.catalog_id = c.id AND i.status = 'APPLIED') AS last_import_at
	FROM catalogs c LEFT JOIN sicils s ON s.id = c.supplier_sicil_id`

func (q *Q) ListCatalogs(orgID uint, includeInactive bool) ([]models.Catalog, error) {
	return list[models.Catalog](q.db, catalogSelect+` WHERE c.org_id = ? AND (? OR c.is_active) ORDER BY c.code`, orgID, includeInactive)
}

func (q *Q) GetCatalog(orgID, id uint) (*models.Catalog, error) {
	return first[models.Catalog](q.db, catalogSelect+` WHERE c.org_id = ? AND c.id = ?`, orgID, id)
}

type CatalogWrite struct {
	Code            int
	Name            string
	SupplierSicilID *uint
	IsActive        bool
}

func (q *Q) InsertCatalog(orgID uint, w CatalogWrite) (uint, error) {
	var id uint
	err := q.db.Raw(`INSERT INTO catalogs (org_id, code, name, supplier_sicil_id, is_active) VALUES (?, ?, ?, ?, ?) RETURNING id`,
		orgID, w.Code, w.Name, w.SupplierSicilID, w.IsActive).Row().Scan(&id)
	return id, err
}

// UpdateCatalog changes name, supplier and active flag at the given version; false when the version moved.
func (q *Q) UpdateCatalog(orgID, id uint, version int, w CatalogWrite) (bool, error) {
	res := q.db.Exec(`UPDATE catalogs SET name = ?, supplier_sicil_id = ?, is_active = ?, version = version + 1
		WHERE org_id = ? AND id = ? AND version = ?`, w.Name, w.SupplierSicilID, w.IsActive, orgID, id, version)
	return res.RowsAffected == 1, res.Error
}

// LockCatalog takes the catalog row lock (imports and price writes of one catalog serialize) and
// returns its content version.
func (q *Q) LockCatalog(orgID, id uint) (int64, error) {
	var v []int64
	err := q.db.Raw(`SELECT content_version FROM catalogs WHERE org_id = ? AND id = ? FOR UPDATE`, orgID, id).Scan(&v).Error
	if err != nil {
		return 0, err
	}
	if len(v) == 0 {
		return 0, errNotFound
	}
	return v[0], nil
}

func (q *Q) CatalogByCode(orgID uint, code int) (*models.Catalog, error) {
	return first[models.Catalog](q.db, `SELECT id, code, name FROM catalogs WHERE org_id = ? AND code = ?`, orgID, code)
}
