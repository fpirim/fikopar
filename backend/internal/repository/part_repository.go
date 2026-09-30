package repository

import (
	"github.com/fpirim/fikopar/backend/internal/models"
)

const partSelect = `SELECT p.id, p.org_id, p.catalog_id, c.code AS catalog_code, c.name AS catalog_name, p.stock_no, p.name, p.type,
	p.brand, p.unit, p.package_qty, trim_scale(p.vat_pct)::text AS vat_pct, p.is_active, p.created_at, p.updated_at, p.version,
	lp.price AS list_price, lp.valid_from AS list_price_valid_from,
	coalesce((SELECT sum(l.on_hand) FROM stock_levels l WHERE l.part_id = p.id), 0) AS on_hand
	FROM parts p
	JOIN catalogs c ON c.id = p.catalog_id
	LEFT JOIN LATERAL price_on(p.org_id, p.id, ?::date) lp ON true`

func (q *Q) GetPart(orgID, id uint, on models.Date) (*models.Part, error) {
	return first[models.Part](q.db, partSelect+` WHERE p.org_id = ? AND p.id = ?`, on, orgID, id)
}

func (q *Q) PartsByIDs(orgID uint, ids []uint, on models.Date) ([]models.Part, error) {
	return list[models.Part](q.db, partSelect+` WHERE p.org_id = ? AND p.id = ANY(?::bigint[]) ORDER BY p.stock_no, c.code`, on, orgID, uintArray(ids))
}

// LastPurchases is last_purchase() of each part on the date; parts never bought are left out.
func (q *Q) LastPurchases(orgID uint, ids []uint, on models.Date) ([]models.LastPurchase, error) {
	return list[models.LastPurchase](q.db, `SELECT u.id AS part_id, lp.unit_cost AS amount, lp.document_date AS date, lp.document_no
		FROM unnest(?::bigint[]) u(id) CROSS JOIN LATERAL last_purchase(?, u.id, ?::date) lp`, uintArray(ids), orgID, on)
}

// PartRef is the minimum a caller needs to reference a part.
type PartRef struct {
	ID        uint   `gorm:"column:id"`
	CatalogID uint   `gorm:"column:catalog_id"`
	StockNo   string `gorm:"column:stock_no"`
	IsActive  bool   `gorm:"column:is_active"`
	Version   int    `gorm:"column:version"`
}

func (q *Q) GetPartRef(orgID, id uint) (*PartRef, error) {
	return first[PartRef](q.db, `SELECT id, catalog_id, stock_no, is_active, version FROM parts WHERE org_id = ? AND id = ?`, orgID, id)
}

func (q *Q) PartRefs(orgID uint, ids []uint) ([]PartRef, error) {
	return list[PartRef](q.db, `SELECT id, catalog_id, stock_no, is_active, version FROM parts WHERE org_id = ? AND id = ANY(?::bigint[])`, orgID, uintArray(ids))
}

type PartWrite struct {
	CatalogID  uint
	StockNo    string
	Name       string
	Type       *string
	Brand      *string
	Unit       string
	PackageQty int
	VatPct     string
	IsActive   bool
}

func (q *Q) InsertPart(orgID uint, w PartWrite) (uint, error) {
	var id uint
	err := q.db.Raw(`INSERT INTO parts (org_id, catalog_id, stock_no, name, type, brand, unit, package_qty, vat_pct, is_active)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?::numeric, ?) RETURNING id`,
		orgID, w.CatalogID, w.StockNo, w.Name, w.Type, w.Brand, w.Unit, w.PackageQty, w.VatPct, w.IsActive).Row().Scan(&id)
	return id, err
}

// UpdatePart writes every field at the given version; false when the version moved.
func (q *Q) UpdatePart(orgID, id uint, version int, w PartWrite) (bool, error) {
	res := q.db.Exec(`UPDATE parts SET stock_no = ?, name = ?, type = ?, brand = ?, unit = ?, package_qty = ?, vat_pct = ?::numeric,
		is_active = ?, version = version + 1, updated_at = now()
		WHERE org_id = ? AND id = ? AND version = ?`,
		w.StockNo, w.Name, w.Type, w.Brand, w.Unit, w.PackageQty, w.VatPct, w.IsActive, orgID, id, version)
	return res.RowsAffected == 1, res.Error
}

func (q *Q) SetPartActive(orgID, id uint, active bool) error {
	return q.db.Exec(`UPDATE parts SET is_active = ?, version = version + 1, updated_at = now() WHERE org_id = ? AND id = ?`,
		active, orgID, id).Error
}

func (q *Q) PartCodes(partIDs []uint) ([]models.PartCode, error) {
	return list[models.PartCode](q.db, `SELECT part_id, kind, code, normalized_code, equivalent_part_id FROM part_codes
		WHERE part_id = ANY(?::bigint[]) ORDER BY part_id, CASE kind WHEN 'MANUFACTURER' THEN 1 WHEN 'OEM' THEN 2 WHEN 'ALT' THEN 3 ELSE 4 END, id`,
		uintArray(partIDs))
}

// ReplaceCodes swaps the code list of a part; kinds limits the swap to those kinds (nil = all).
func (q *Q) ReplaceCodes(orgID, partID uint, codes []models.PartCode, kinds []string) error {
	del := q.db.Exec(`DELETE FROM part_codes WHERE part_id = ? AND (?::text[] IS NULL OR kind = ANY(?::text[]))`, partID, textArray(kinds), textArray(kinds))
	if del.Error != nil {
		return del.Error
	}
	for _, c := range codes {
		if err := q.db.Exec(`INSERT INTO part_codes (org_id, part_id, kind, code, equivalent_part_id) VALUES (?, ?, ?, ?, ?)
			ON CONFLICT DO NOTHING`, orgID, partID, c.Kind, c.Code, c.EquivalentPartID).Error; err != nil {
			return err
		}
	}
	return nil
}

// NextPrice is the earliest price after the date.
func (q *Q) NextPrice(orgID, partID uint, after models.Date) (*models.PriceRef, error) {
	rows, err := list[models.PriceRef](q.db, `SELECT price, valid_from FROM prices WHERE org_id = ? AND part_id = ? AND valid_from > ?
		ORDER BY valid_from LIMIT 1`, orgID, partID, after)
	if err != nil || len(rows) == 0 {
		return nil, err
	}
	return &rows[0], nil
}

// PartIDsByCode matches a normalized code against stock numbers and every part code.
func (q *Q) PartIDsByCode(orgID uint, norm string, activeOnly bool) ([]uint, error) {
	var ids []uint
	err := q.db.Raw(`SELECT DISTINCT p.id FROM parts p
		WHERE p.org_id = ? AND (NOT ? OR p.is_active)
		  AND (p.stock_no_norm = ? OR EXISTS (SELECT 1 FROM part_codes pc WHERE pc.part_id = p.id AND pc.org_id = p.org_id AND pc.normalized_code = ?))
		ORDER BY p.id`, orgID, activeOnly, norm, norm).Scan(&ids).Error
	return ids, err
}

// EquivalentLink is a part linked to OwnerID by an EQUIVALENT code, in either direction.
type EquivalentLink struct {
	OwnerID uint `gorm:"column:owner_id"`
	models.Equivalent
}

func (q *Q) EquivalentsOf(orgID uint, partIDs []uint) ([]EquivalentLink, error) {
	return list[EquivalentLink](q.db, `
		WITH links AS (
			SELECT part_id AS a, equivalent_part_id AS b FROM part_codes WHERE org_id = ? AND kind = 'EQUIVALENT' AND part_id = ANY(?::bigint[])
			UNION
			SELECT equivalent_part_id, part_id FROM part_codes WHERE org_id = ? AND kind = 'EQUIVALENT' AND equivalent_part_id = ANY(?::bigint[]))
		SELECT l.a AS owner_id, p.id AS part_id, p.stock_no, c.code AS catalog_code,
		       coalesce((SELECT sum(s.on_hand) FROM stock_levels s WHERE s.part_id = p.id), 0) AS on_hand_total
		FROM links l JOIN parts p ON p.id = l.b JOIN catalogs c ON c.id = p.catalog_id
		ORDER BY l.a, p.stock_no`, orgID, uintArray(partIDs), orgID, uintArray(partIDs))
}

// PartEquivalent is an equivalent of OwnerID for the part card.
type PartEquivalent struct {
	OwnerID uint `gorm:"column:owner_id"`
	models.PartEquivalent
}

func (q *Q) PartEquivalents(orgID uint, partIDs []uint) ([]PartEquivalent, error) {
	return list[PartEquivalent](q.db, `
		WITH links AS (
			SELECT part_id AS a, equivalent_part_id AS b FROM part_codes WHERE org_id = ? AND kind = 'EQUIVALENT' AND part_id = ANY(?::bigint[])
			UNION
			SELECT equivalent_part_id, part_id FROM part_codes WHERE org_id = ? AND kind = 'EQUIVALENT' AND equivalent_part_id = ANY(?::bigint[]))
		SELECT l.a AS owner_id, p.id AS part_id, p.stock_no, p.name, p.catalog_id, c.name AS catalog_name
		FROM links l JOIN parts p ON p.id = l.b JOIN catalogs c ON c.id = p.catalog_id
		ORDER BY l.a, p.stock_no, c.code, p.id`, orgID, uintArray(partIDs), orgID, uintArray(partIDs))
}

func (q *Q) PartByStockNo(orgID, catalogID uint, stockNo string) (*PartRef, error) {
	return first[PartRef](q.db, `SELECT id, catalog_id, stock_no, is_active, version FROM parts WHERE org_id = ? AND catalog_id = ? AND stock_no = ?`,
		orgID, catalogID, stockNo)
}
