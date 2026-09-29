package repository

import (
	"github.com/fpirim/fikopar/backend/internal/models"
)

type MovementInsert struct {
	PartID     uint
	DepotID    uint
	Kind       string
	Qty        models.Dec4
	UnitCost   *models.Dec4
	SourceType string
	SourceID   *string
	TransferID *uint
	Date       models.Date
	CreatedBy  *uint
}

// InsertMovement appends one movement; the trigger moves the level, whose on hand is returned.
func (q *Q) InsertMovement(orgID uint, m MovementInsert) (uint, models.Dec4, error) {
	var id uint
	err := q.db.Raw(`INSERT INTO stock_movements (org_id, part_id, depot_id, kind, qty, unit_cost, source_type, source_id,
			transfer_id, movement_date, created_by)
		VALUES (?, ?, ?, ?, ?::numeric, ?::numeric, ?, ?, ?, ?, ?) RETURNING id`,
		orgID, m.PartID, m.DepotID, m.Kind, m.Qty, m.UnitCost, m.SourceType, m.SourceID, m.TransferID, m.Date, m.CreatedBy).Row().Scan(&id)
	if err != nil {
		return 0, 0, err
	}
	var after models.Dec4
	err = q.db.Raw(`SELECT on_hand FROM stock_levels WHERE part_id = ? AND depot_id = ?`, m.PartID, m.DepotID).Row().Scan(&after)
	return id, after, err
}

const levelCols = `p.id AS part_id, p.stock_no, p.name, c.code AS catalog_code, d.id AS depot_id,
	coalesce(l.on_hand, 0) AS on_hand, coalesce(l.reserved, 0) AS reserved,
	coalesce(l.on_hand, 0) - coalesce(l.reserved, 0) AS available,
	coalesce(pd.min_qty, 0) AS min_qty, pd.max_qty, coalesce(pd.shelf, '') AS shelf,
	coalesce(pd.min_qty, 0) > 0 AND coalesce(l.on_hand, 0) < pd.min_qty AS below_min,
	coalesce(l.on_hand, 0) < 0 AS negative, coalesce(pd.version, 0) AS version`

// PartStock has one row per depot of the company, zeros where nothing moved.
func (q *Q) PartStock(orgID, partID uint) ([]models.StockLevel, error) {
	return list[models.StockLevel](q.db, `SELECT `+levelCols+`
		FROM parts p JOIN catalogs c ON c.id = p.catalog_id
		JOIN depots d ON d.org_id = p.org_id
		LEFT JOIN stock_levels l ON l.part_id = p.id AND l.depot_id = d.id
		LEFT JOIN part_depots pd ON pd.part_id = p.id AND pd.depot_id = d.id
		WHERE p.org_id = ? AND p.id = ? ORDER BY d.code`, orgID, partID)
}

func (q *Q) PartDepotLevel(orgID, partID, depotID uint) (*models.StockLevel, error) {
	return first[models.StockLevel](q.db, `SELECT `+levelCols+`
		FROM parts p JOIN catalogs c ON c.id = p.catalog_id
		JOIN depots d ON d.org_id = p.org_id AND d.id = ?
		LEFT JOIN stock_levels l ON l.part_id = p.id AND l.depot_id = d.id
		LEFT JOIN part_depots pd ON pd.part_id = p.id AND pd.depot_id = d.id
		WHERE p.org_id = ? AND p.id = ?`, depotID, orgID, partID)
}

type DepotSettings struct {
	MinQty models.Dec4
	MaxQty *models.Dec4
	Shelf  string
}

// UpsertPartDepot writes the settings when the stored version equals version (0 = no row yet).
func (q *Q) UpsertPartDepot(orgID, partID, depotID uint, version int, s DepotSettings) (bool, error) {
	if version == 0 {
		res := q.db.Exec(`INSERT INTO part_depots (org_id, part_id, depot_id, min_qty, max_qty, shelf) VALUES (?, ?, ?, ?::numeric, ?::numeric, ?)
			ON CONFLICT (part_id, depot_id) DO NOTHING`, orgID, partID, depotID, s.MinQty, s.MaxQty, s.Shelf)
		return res.RowsAffected == 1, res.Error
	}
	res := q.db.Exec(`UPDATE part_depots SET min_qty = ?::numeric, max_qty = ?::numeric, shelf = ?, version = version + 1
		WHERE org_id = ? AND part_id = ? AND depot_id = ? AND version = ?`, s.MinQty, s.MaxQty, s.Shelf, orgID, partID, depotID, version)
	return res.RowsAffected == 1, res.Error
}

type LevelFilter struct {
	PartID  uint
	DepotID uint
	Only    string
	After   *LevelCursor
	Limit   int
}

// LevelCursor is the keyset position (sort value, stock no, part, depot).
type LevelCursor struct {
	Key     string `json:"k"`
	StockNo string `json:"s"`
	PartID  uint   `json:"p"`
	DepotID uint   `json:"d"`
}

type LevelRow struct {
	models.StockLevel
	SortKey string `gorm:"column:sort_key"`
}

// StockLevels lists (part, depot) pairs with a level row or a minimum. NEGATIVE sorts by on hand, the rest by
// on hand − min, then stock no.
func (q *Q) StockLevels(orgID uint, f LevelFilter) ([]LevelRow, error) {
	sortExpr := `coalesce(l.on_hand, 0) - coalesce(pd.min_qty, 0)`
	if f.Only == "NEGATIVE" {
		sortExpr = `coalesce(l.on_hand, 0)`
	}
	where := `WHERE p.org_id = ? AND (l.part_id IS NOT NULL OR pd.part_id IS NOT NULL)`
	args := []any{orgID}
	if f.PartID != 0 {
		where += ` AND p.id = ?`
		args = append(args, f.PartID)
	}
	if f.DepotID != 0 {
		where += ` AND d.id = ?`
		args = append(args, f.DepotID)
	}
	switch f.Only {
	case "BELOW_MIN":
		where += ` AND coalesce(pd.min_qty, 0) > 0 AND coalesce(l.on_hand, 0) < pd.min_qty`
	case "NEGATIVE":
		where += ` AND coalesce(l.on_hand, 0) < 0`
	case "BELOW_MIN_OR_NEGATIVE":
		where += ` AND (coalesce(l.on_hand, 0) < 0 OR (coalesce(pd.min_qty, 0) > 0 AND coalesce(l.on_hand, 0) < pd.min_qty))`
	}
	if f.Only == "BELOW_MIN_OR_NEGATIVE" {
		sortExpr = `(CASE WHEN coalesce(l.on_hand, 0) < 0 THEN 0 ELSE 1 END)::numeric * 1e15 + ` + sortExpr
	}
	base := `SELECT * FROM (SELECT (` + sortExpr + `)::numeric AS sort_num, (` + sortExpr + `)::text AS sort_key, ` + levelCols + `
		FROM parts p JOIN catalogs c ON c.id = p.catalog_id
		JOIN depots d ON d.org_id = p.org_id
		LEFT JOIN stock_levels l ON l.part_id = p.id AND l.depot_id = d.id
		LEFT JOIN part_depots pd ON pd.part_id = p.id AND pd.depot_id = d.id ` + where + `) x`
	if f.After != nil {
		base += ` WHERE (x.sort_num, x.stock_no, x.part_id, x.depot_id) > (?::numeric, ?, ?, ?)`
		args = append(args, f.After.Key, f.After.StockNo, f.After.PartID, f.After.DepotID)
	}
	order := `ORDER BY x.sort_num, x.stock_no, x.part_id, x.depot_id LIMIT ?`
	return list[LevelRow](q.db, base+` `+order, append(args, f.Limit)...)
}

// StockLevelCounts counts (part, depot) pairs below minimum and negative.
func (q *Q) StockLevelCounts(orgID, depotID uint) (belowMin, negative int64, err error) {
	err = q.db.Raw(`SELECT
		count(*) FILTER (WHERE coalesce(pd.min_qty, 0) > 0 AND coalesce(l.on_hand, 0) < pd.min_qty),
		count(*) FILTER (WHERE coalesce(l.on_hand, 0) < 0)
		FROM parts p JOIN depots d ON d.org_id = p.org_id
		LEFT JOIN stock_levels l ON l.part_id = p.id AND l.depot_id = d.id
		LEFT JOIN part_depots pd ON pd.part_id = p.id AND pd.depot_id = d.id
		WHERE p.org_id = ? AND (?::bigint = 0 OR d.id = ?) AND (l.part_id IS NOT NULL OR pd.part_id IS NOT NULL)`,
		orgID, depotID, depotID).Row().Scan(&belowMin, &negative)
	return belowMin, negative, err
}
