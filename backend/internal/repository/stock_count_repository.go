package repository

import (
	"strings"
	"time"

	"github.com/fpirim/fikopar/backend/internal/models"
)

// countLineSelect reads the lines of the count aliased c. Cost is live (last_purchase_cost on the count date)
// while the count is OPEN and the finalize snapshot afterwards. movedSinceOpen leaves out the count's own
// adjustments, so onHandAfterPost = counted + later movements holds before and after finalize.
const countLineSelect = `SELECT l.id, l.part_id, p.stock_no, p.name, p.brand, p.unit, l.shelf, l.system_qty, l.counted_qty,
		l.counted_qty - l.system_qty AS difference, mv.moved AS moved_since_open,
		l.counted_qty + mv.moved AS on_hand_after_post, l.added,
		cost.unit_cost, cost.cost_source,
		round((l.counted_qty - l.system_qty) * cost.unit_cost, 2) AS value,
		split_part(u.email, '@', 1) AS counted_by, l.counted_at, l.version
	FROM stock_count_lines l
	JOIN parts p ON p.id = l.part_id
	LEFT JOIN users u ON u.id = l.counted_by
	CROSS JOIN LATERAL (
		SELECT coalesce(sum(movement_sign(m.kind) * m.qty), 0) AS moved FROM stock_movements m
		WHERE m.org_id = l.org_id AND m.part_id = l.part_id AND m.depot_id = c.depot_id AND m.occurred_at > c.started_at
		  AND NOT (m.source_type = 'stock_count' AND m.source_id = l.id::text)) mv
	CROSS JOIN LATERAL (
		SELECT lc.unit_cost, lc.cost_source FROM last_purchase_cost(l.org_id, l.part_id, c.count_date) lc WHERE c.status = 'OPEN'
		UNION ALL
		SELECT l.unit_cost, coalesce(l.cost_source, 'NONE') WHERE c.status <> 'OPEN') cost
	WHERE l.count_id = c.id`

// countLinesOf is the line set of one count as a subquery (args: org, count).
const countLinesOf = `(SELECT v.* FROM stock_counts c CROSS JOIN LATERAL (` + countLineSelect + `) v
	WHERE c.org_id = ? AND c.id = ?) x`

const countSelect = `SELECT c.id, c.depot_id, c.count_date, c.shelf_from, c.shelf_to, c.count_no, c.year, c.status, c.started_at,
		c.uncounted, split_part(u.email, '@', 1) AS created_by, c.finalized_at, c.cancelled_at, c.cancel_reason, c.version,
		s.line_count, s.counted_count, s.difference_count, s.difference_value, s.value_in, s.value_out, s.unvalued_lines
	FROM stock_counts c
	LEFT JOIN users u ON u.id = c.created_by
	CROSS JOIN LATERAL (SELECT count(*) AS line_count,
		count(*) FILTER (WHERE v.counted_qty IS NOT NULL) AS counted_count,
		count(*) FILTER (WHERE v.difference <> 0) AS difference_count,
		coalesce(sum(v.value), 0) AS difference_value,
		coalesce(sum(v.value) FILTER (WHERE v.value > 0), 0) AS value_in,
		coalesce(-sum(v.value) FILTER (WHERE v.value < 0), 0) AS value_out,
		count(*) FILTER (WHERE v.difference <> 0 AND v.cost_source IN ('LIST_PRICE', 'NONE')) AS unvalued_lines
		FROM (` + countLineSelect + `) v) s`

func (q *Q) GetStockCount(orgID, id uint) (*models.StockCount, error) {
	return first[models.StockCount](q.db, countSelect+` WHERE c.org_id = ? AND c.id = ?`, orgID, id)
}

type CountFilter struct {
	Year    int
	DepotID uint
	Status  string
}

func (q *Q) ListStockCounts(orgID uint, f CountFilter) ([]models.StockCount, error) {
	sql := countSelect + ` WHERE c.org_id = ?`
	args := []any{orgID}
	if f.Year != 0 {
		sql += ` AND c.year = ?`
		args = append(args, f.Year)
	}
	if f.DepotID != 0 {
		sql += ` AND c.depot_id = ?`
		args = append(args, f.DepotID)
	}
	if f.Status != "" {
		sql += ` AND c.status = ?`
		args = append(args, f.Status)
	}
	return list[models.StockCount](q.db, sql+` ORDER BY c.count_date DESC, c.id DESC`, args...)
}

// CountHead is the count row as the write paths need it.
type CountHead struct {
	ID        uint        `gorm:"column:id"`
	DepotID   uint        `gorm:"column:depot_id"`
	CountDate models.Date `gorm:"column:count_date"`
	CountNo   string      `gorm:"column:count_no"`
	Status    string      `gorm:"column:status"`
	StartedAt time.Time   `gorm:"column:started_at"`
	Version   int         `gorm:"column:version"`
}

// LockStockCount reads the count with a row lock: FOR UPDATE for transitions, FOR SHARE for line writes.
func (q *Q) LockStockCount(orgID, id uint, exclusive bool) (*CountHead, error) {
	lock := "FOR SHARE"
	if exclusive {
		lock = "FOR UPDATE"
	}
	return first[CountHead](q.db, `SELECT id, depot_id, count_date, count_no, status, started_at, version FROM stock_counts
		WHERE org_id = ? AND id = ? `+lock, orgID, id)
}

type CountOpen struct {
	DepotID   uint
	CountDate models.Date
	CountNo   string
	ShelfFrom *string
	ShelfTo   *string
	CreatedBy *uint
}

func (q *Q) InsertStockCount(orgID uint, c CountOpen) (uint, error) {
	var id uint
	err := q.db.Raw(`INSERT INTO stock_counts (org_id, depot_id, count_date, year, count_no, shelf_from, shelf_to, created_by)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id`,
		orgID, c.DepotID, c.CountDate, c.CountDate.Year(), c.CountNo, c.ShelfFrom, c.ShelfTo, c.CreatedBy).Row().Scan(&id)
	return id, err
}

// shelfRange matches [from, to]; to also matches as a prefix ("B" includes "B-04-2").
func shelfRange(expr string, from, to *string) (string, []any) {
	var parts []string
	var args []any
	if from != nil {
		parts = append(parts, expr+` >= ?`)
		args = append(args, *from)
	}
	if to != nil {
		parts = append(parts, `(`+expr+` <= ? OR `+expr+` LIKE ?)`)
		args = append(args, *to, strings.NewReplacer(`\`, `\\`, `%`, `\%`, `_`, `\_`).Replace(*to)+"%")
	}
	if len(parts) == 0 {
		return "true", nil
	}
	return strings.Join(parts, " AND "), args
}

// InsertCountLines freezes the sheet: active parts with on hand ≠ 0 in the depot or a shelf there.
func (q *Q) InsertCountLines(orgID, countID, depotID uint, startedAt time.Time, from, to *string) error {
	cond, cargs := shelfRange(`coalesce(pd.shelf, '')`, from, to)
	args := append([]any{countID, depotID, startedAt, depotID, depotID, orgID}, cargs...)
	return q.db.Exec(`INSERT INTO stock_count_lines (org_id, count_id, part_id, shelf, system_qty)
		SELECT p.org_id, ?, p.id, coalesce(pd.shelf, ''), on_hand_at(p.org_id, p.id, ?, ?)
		FROM parts p
		LEFT JOIN stock_levels l ON l.part_id = p.id AND l.depot_id = ?
		LEFT JOIN part_depots pd ON pd.part_id = p.id AND pd.depot_id = ?
		WHERE p.org_id = ? AND p.is_active AND (coalesce(l.on_hand, 0) <> 0 OR coalesce(pd.shelf, '') <> '') AND `+cond, args...).Error
}

type CountLineAdd struct {
	PartID     uint
	Shelf      *string
	CountedQty *models.Dec4
	CountedBy  *uint
}

// AddCountLine inserts a line frozen at the count's startedAt; the shelf defaults to the part's depot shelf.
func (q *Q) AddCountLine(orgID uint, c *CountHead, l CountLineAdd) (uint, error) {
	var id uint
	err := q.db.Raw(`INSERT INTO stock_count_lines (org_id, count_id, part_id, shelf, system_qty, counted_qty, counted_by, counted_at, added)
		SELECT ?, ?, ?, coalesce(?, (SELECT pd.shelf FROM part_depots pd WHERE pd.part_id = ? AND pd.depot_id = ?), ''),
		       on_hand_at(?, ?, ?, ?), ?::numeric, CASE WHEN ?::numeric IS NULL THEN NULL ELSE ?::bigint END,
		       CASE WHEN ?::numeric IS NULL THEN NULL ELSE now() END, true
		RETURNING id`,
		orgID, c.ID, l.PartID, l.Shelf, l.PartID, c.DepotID, orgID, l.PartID, c.DepotID, c.StartedAt,
		l.CountedQty, l.CountedQty, l.CountedBy, l.CountedQty).Row().Scan(&id)
	return id, err
}

// SetCountLine saves one counted quantity at the given line version; false when the version moved.
func (q *Q) SetCountLine(orgID, countID, lineID uint, version int, qty *models.Dec4, shelf *string, by *uint) (bool, error) {
	res := q.db.Exec(`UPDATE stock_count_lines SET counted_qty = ?::numeric,
			counted_at = CASE WHEN ?::numeric IS NULL THEN NULL ELSE now() END,
			counted_by = CASE WHEN ?::numeric IS NULL THEN NULL ELSE ?::bigint END,
			shelf = coalesce(?, shelf), version = version + 1
		WHERE org_id = ? AND count_id = ? AND id = ? AND version = ?`,
		qty, qty, qty, by, shelf, orgID, countID, lineID, version)
	return res.RowsAffected == 1, res.Error
}

func (q *Q) CountLineExists(orgID, countID, lineID uint) (bool, error) {
	var ok bool
	err := q.db.Raw(`SELECT EXISTS (SELECT 1 FROM stock_count_lines WHERE org_id = ? AND count_id = ? AND id = ?)`, orgID, countID, lineID).Scan(&ok).Error
	return ok, err
}

func (q *Q) GetCountLine(orgID, countID, lineID uint) (*models.StockCountLine, error) {
	return first[models.StockCountLine](q.db, `SELECT * FROM `+countLinesOf+` WHERE x.id = ?`, orgID, countID, lineID)
}

// SnapshotCountLines values every line at the count date and, with zero, counts the uncounted ones as 0.
func (q *Q) SnapshotCountLines(orgID uint, c *CountHead, zero bool, by *uint) error {
	return q.db.Exec(`UPDATE stock_count_lines l SET
			counted_qty = CASE WHEN ? AND counted_qty IS NULL THEN 0 ELSE counted_qty END,
			counted_by  = CASE WHEN ? AND counted_qty IS NULL THEN ?::bigint ELSE counted_by END,
			counted_at  = CASE WHEN ? AND counted_qty IS NULL THEN now() ELSE counted_at END,
			(unit_cost, cost_source) = (SELECT lc.unit_cost, lc.cost_source FROM last_purchase_cost(l.org_id, l.part_id, ?) lc),
			version = version + 1
		WHERE org_id = ? AND count_id = ?`, zero, zero, by, zero, c.CountDate, orgID, c.ID).Error
}

// CountAdjustment is a counted line whose quantity differs from the frozen system quantity.
type CountAdjustment struct {
	LineID     uint         `gorm:"column:id"`
	PartID     uint         `gorm:"column:part_id"`
	StockNo    string       `gorm:"column:stock_no"`
	Difference models.Dec4  `gorm:"column:difference"`
	UnitCost   *models.Dec4 `gorm:"column:unit_cost"`
}

func (q *Q) CountAdjustments(orgID, countID uint) ([]CountAdjustment, error) {
	return list[CountAdjustment](q.db, `SELECT l.id, l.part_id, p.stock_no, l.counted_qty - l.system_qty AS difference, l.unit_cost
		FROM stock_count_lines l JOIN parts p ON p.id = l.part_id
		WHERE l.org_id = ? AND l.count_id = ? AND l.counted_qty IS NOT NULL AND l.counted_qty <> l.system_qty
		ORDER BY l.part_id`, orgID, countID)
}

func (q *Q) FinalizeStockCount(orgID, id uint, uncounted string) error {
	return q.db.Exec(`UPDATE stock_counts SET status = 'FINALIZED', uncounted = ?, finalized_at = now(), version = version + 1
		WHERE org_id = ? AND id = ?`, uncounted, orgID, id).Error
}

func (q *Q) CancelStockCount(orgID, id uint, reason string) error {
	return q.db.Exec(`UPDATE stock_counts SET status = 'CANCELLED', cancelled_at = now(), cancel_reason = ?, version = version + 1
		WHERE org_id = ? AND id = ?`, reason, orgID, id).Error
}

type CountLineGridRequest struct {
	ReportGridRequest
	Only      string  `json:"only"`
	ShelfFrom *string `json:"shelfFrom"`
	ShelfTo   *string `json:"shelfTo"`
}

var countLineCols = map[string]GridCol{
	"shelf": {"x.shelf", ColText}, "stockNo": {"x.stock_no", ColText}, "name": {"x.name", ColText},
	"brand": {"x.brand", ColText}, "systemQty": {"x.system_qty", ColNumber}, "countedQty": {"x.counted_qty", ColNumber},
	"difference": {"x.difference", ColNumber}, "movedSinceOpen": {"x.moved_since_open", ColNumber},
	"unitCost": {"x.unit_cost", ColNumber}, "costSource": {"x.cost_source", ColText}, "value": {"x.value", ColNumber},
	"countedBy": {"x.counted_by", ColText}, "countedAt": {"x.counted_at", ColDate},
}

var countOnly = map[string]string{
	"": "true", "ALL": "true",
	"DIFFERENCES": "x.difference <> 0",
	"NOT_COUNTED": "x.counted_qty IS NULL",
	"COUNTED":     "x.counted_qty IS NOT NULL",
}

// CountLineTotals is the pinned row of the lines grid.
type CountLineTotals struct {
	SystemQty  models.Dec4  `gorm:"column:system_qty"`
	CountedQty models.Dec4  `gorm:"column:counted_qty"`
	Difference models.Dec4  `gorm:"column:difference"`
	Value      models.Money `gorm:"column:value"`
	ValueIn    models.Money `gorm:"column:value_in"`
	ValueOut   models.Money `gorm:"column:value_out"`
	Count      int64        `gorm:"column:n"`
}

// GridCountLines pages the lines with filters; all = every filtered row (export).
func (q *Q) GridCountLines(orgID, countID uint, req CountLineGridRequest, all bool) ([]models.StockCountLine, *CountLineTotals, error) {
	only, ok := countOnly[req.Only]
	if !ok {
		return nil, nil, ErrBadFilter
	}
	if err := knownCols(req.ReportGridRequest, countLineCols); err != nil {
		return nil, nil, err
	}
	where, wargs, err := whereClause(req.FilterModel, countLineCols)
	if err != nil {
		return nil, nil, err
	}
	shelf, sargs := shelfRange("x.shelf", req.ShelfFrom, req.ShelfTo)
	search, qargs := searchClause(req.Search, []string{"x.stock_no", "x.name", "x.shelf", "coalesce(x.brand, '')"})
	base := ` FROM ` + countLinesOf + ` WHERE ` + only + ` AND ` + where + ` AND ` + shelf + ` AND ` + search
	args := append(append(append([]any{orgID, countID}, wargs...), sargs...), qargs...)

	totals, err := first[CountLineTotals](q.db, `SELECT count(*) AS n, coalesce(sum(x.system_qty), 0) AS system_qty,
		coalesce(sum(x.counted_qty), 0) AS counted_qty, coalesce(sum(x.difference), 0) AS difference,
		coalesce(sum(x.value), 0) AS value, coalesce(sum(x.value) FILTER (WHERE x.value > 0), 0) AS value_in,
		coalesce(-sum(x.value) FILTER (WHERE x.value < 0), 0) AS value_out`+base, args...)
	if err != nil {
		return nil, nil, err
	}
	sql := `SELECT x.*` + base + ` ORDER BY ` + orderClause(req.SortModel, countLineCols, "x.shelf, x.stock_no, x.id")
	if size := req.EndRow - req.StartRow; !all && size > 0 {
		sql += ` LIMIT ? OFFSET ?`
		args = append(args, size, req.StartRow)
	}
	rows, err := list[models.StockCountLine](q.db, sql, args...)
	return rows, totals, err
}
