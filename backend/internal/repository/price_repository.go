package repository

import "github.com/fpirim/fikopar/backend/internal/models"

const priceSelect = `SELECT pp.id, pp.part_id, p.catalog_id, p.stock_no, p.name, pp.price, pp.valid_from, pp.valid_to, pp.previous_price,
	(pp.valid_from <= ?::date AND (pp.valid_to IS NULL OR pp.valid_to >= ?::date)) AS is_current,
	pp.source, pp.import_id, split_part(u.email, '@', 1) AS created_by, pp.created_at
	FROM price_periods pp
	JOIN parts p ON p.id = pp.part_id
	LEFT JOIN users u ON u.id = pp.created_by`

// PriceHistory lists a part's prices, newest validFrom first; today marks the current one.
func (q *Q) PriceHistory(orgID, partID uint, today models.Date) ([]models.Price, error) {
	return list[models.Price](q.db, priceSelect+` WHERE pp.org_id = ? AND pp.part_id = ? ORDER BY pp.valid_from DESC`,
		today, today, orgID, partID)
}

func (q *Q) PriceByID(orgID, id uint, today models.Date) (*models.Price, error) {
	return first[models.Price](q.db, priceSelect+` WHERE pp.org_id = ? AND pp.id = ?`, today, today, orgID, id)
}

func (q *Q) PriceOn(orgID, partID uint, on models.Date) (*models.PriceRef, error) {
	rows, err := list[models.PriceRef](q.db, `SELECT price, valid_from FROM price_on(?, ?, ?::date)`, orgID, partID, on)
	if err != nil || len(rows) == 0 {
		return nil, err
	}
	return &rows[0], nil
}

// PriceRow is a stored price on an exact date.
type PriceRow struct {
	ID    uint        `gorm:"column:id"`
	Price models.Dec4 `gorm:"column:price"`
}

func (q *Q) PriceAt(orgID, partID uint, validFrom models.Date) (*PriceRow, error) {
	return first[PriceRow](q.db, `SELECT id, price FROM prices WHERE org_id = ? AND part_id = ? AND valid_from = ?`, orgID, partID, validFrom)
}

type PriceInsert struct {
	PartID    uint
	Price     models.Dec4
	ValidFrom models.Date
	Source    string
	ImportID  *uint
	CreatedBy *uint
}

// InsertPrice adds a row; ok is false when the part already has a price on that date.
func (q *Q) InsertPrice(orgID uint, p PriceInsert) (id uint, ok bool, err error) {
	var ids []uint
	err = q.db.Raw(`INSERT INTO prices (org_id, part_id, price, valid_from, source, import_id, created_by)
		VALUES (?, ?, ?::numeric, ?, ?, ?, ?) ON CONFLICT (part_id, valid_from) DO NOTHING RETURNING id`,
		orgID, p.PartID, p.Price, p.ValidFrom, p.Source, p.ImportID, p.CreatedBy).Scan(&ids).Error
	if err != nil || len(ids) == 0 {
		return 0, false, err
	}
	return ids[0], true, nil
}

// CatalogPrices lists active parts of a catalog with their price on a date, by stock no, after the cursor.
func (q *Q) CatalogPrices(orgID, catalogID uint, on models.Date, after string, limit int) ([]models.Price, error) {
	return list[models.Price](q.db, `SELECT pp.id, p.id AS part_id, p.catalog_id, p.stock_no, p.name, pp.price, pp.valid_from,
		pp.valid_to, pp.previous_price, (pp.id IS NOT NULL AND ?::date = CURRENT_DATE) AS is_current, pp.source, pp.import_id,
		split_part(u.email, '@', 1) AS created_by, pp.created_at
		FROM parts p
		LEFT JOIN LATERAL (SELECT x.* FROM price_periods x WHERE x.part_id = p.id AND x.valid_from <= ?::date
		                   ORDER BY x.valid_from DESC LIMIT 1) pp ON true
		LEFT JOIN users u ON u.id = pp.created_by
		WHERE p.org_id = ? AND p.catalog_id = ? AND p.is_active AND p.stock_no > ?
		ORDER BY p.stock_no LIMIT ?`, on, on, orgID, catalogID, after, limit)
}
