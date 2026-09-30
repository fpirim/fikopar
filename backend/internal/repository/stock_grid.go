package repository

import (
	"strings"

	"github.com/fpirim/fikopar/backend/internal/models"
)

// movementSelect is the ledger of a scope with labels and numbers; running is the on hand after each
// movement in (date, id) order over every movement of the scope.
const movementSelect = `SELECT m.id, m.part_id, p.stock_no, p.name, m.depot_id, m.kind,
	CASE m.source_type WHEN 'transfer' THEN 'TRANSFER' WHEN 'stock_count' THEN 'COUNT' WHEN 'legacy_import' THEN 'OPENING'
	     ELSE CASE WHEN doc.type IN ('SALES_RETURN','SALES_RETURN_WAYBILL') THEN 'SALES_RETURN'
	               WHEN doc.type IN ('PURCHASE_RETURN','PURCHASE_RETURN_WAYBILL') THEN 'PURCHASE_RETURN'
	               WHEN doc.type LIKE 'PURCHASE_%' THEN 'PURCHASE'
	               WHEN doc.type LIKE 'SALES_%' THEN 'SALE'
	               WHEN m.kind = 'IN' THEN 'PURCHASE' ELSE 'SALE' END END AS label,
	m.qty, movement_sign(m.kind) * m.qty AS delta,
	CASE WHEN movement_sign(m.kind) > 0 THEN m.qty END AS qty_in, CASE WHEN movement_sign(m.kind) < 0 THEN m.qty END AS qty_out,
	m.unit_cost, m.source_type, m.source_id,
	coalesce(t.transfer_no, sc.count_no, doc.document_no) AS source_no, doc.id AS document_id, doc.type AS document_type,
	doc.id IS NOT NULL AND m.kind IS DISTINCT FROM document_stock_kind(doc.type) AS reversal,
	m.transfer_id, sc.id AS count_id, ds.hesap_adi AS sicil_name,
	CASE WHEN t.id IS NULL THEN NULL WHEN m.kind = 'TRANSFER_OUT' THEN t.to_depot_id ELSE t.from_depot_id END AS other_depot_id,
	RUNNING AS running_on_hand,
	m.movement_date, m.occurred_at
	FROM stock_movements m
	JOIN parts p ON p.id = m.part_id
	LEFT JOIN stock_transfers t ON t.id = m.transfer_id
	LEFT JOIN stock_count_lines scl ON m.source_type = 'stock_count' AND scl.id::text = m.source_id
	LEFT JOIN stock_counts sc ON sc.id = scl.count_id
	LEFT JOIN document_lines dl ON m.source_type = 'document' AND dl.id::text = m.source_id
	LEFT JOIN documents doc ON doc.id = dl.document_id
	LEFT JOIN sicils ds ON ds.id = doc.sicil_id`

var movementCols = map[string]GridCol{
	"date": {"movement_date", ColDate}, "kind": {"kind", ColText}, "label": {"label", ColText},
	"sourceNo": {"source_no", ColText}, "sicilName": {"sicil_name", ColText}, "stockNo": {"stock_no", ColText},
	"name": {"name", ColText}, "depotId": {"depot_id", ColNumber}, "qty": {"qty", ColNumber},
	"qtyIn": {"qty_in", ColNumber}, "qtyOut": {"qty_out", ColNumber}, "delta": {"delta", ColNumber},
	"unitCost": {"unit_cost", ColNumber},
}

type MovementGrid struct {
	Rows          []models.StockMovement
	TotalCount    int64
	QtyIn, QtyOut models.Dec4
	Opening       *models.Dec4
	Closing       *models.Dec4
}

// MovementsGrid pages the ledger. running is kept only when the caller asks for it (part + date sort).
func (q *Q) MovementsGrid(orgID uint, partID uint, depotID *uint, req ReportGridRequest, running, all bool) (*MovementGrid, error) {
	if err := knownCols(req, movementCols); err != nil {
		return nil, err
	}
	scope := ` WHERE m.org_id = ?`
	args := []any{orgID}
	if partID != 0 {
		scope += ` AND m.part_id = ?`
		args = append(args, partID)
	}
	if depotID != nil {
		scope += ` AND m.depot_id = ?`
		args = append(args, *depotID)
	}
	where, wargs, err := whereClause(req.FilterModel, movementCols)
	if err != nil {
		return nil, err
	}
	search, sargs := searchClause(req.Search, []string{"stock_no", "name", "source_no"})
	sel := strings.Replace(movementSelect, "RUNNING", "NULL::numeric", 1)
	if running {
		sel = strings.Replace(movementSelect, "RUNNING", "sum(movement_sign(m.kind) * m.qty) OVER (ORDER BY m.movement_date, m.id)", 1)
	}
	base := `SELECT * FROM (` + sel + scope + `) x WHERE ` + where + ` AND ` + search
	fargs := append(append(append([]any{}, args...), wargs...), sargs...)

	out := &MovementGrid{}
	if err := q.db.Raw(`SELECT count(*), coalesce(sum(qty) FILTER (WHERE delta > 0), 0), coalesce(sum(qty) FILTER (WHERE delta < 0), 0)
		FROM (`+base+`) c`, fargs...).Row().Scan(&out.TotalCount, &out.QtyIn, &out.QtyOut); err != nil {
		return nil, err
	}
	sql := base + ` ORDER BY ` + orderClause(req.SortModel, movementCols, "movement_date DESC, id DESC")
	if !all {
		size := req.EndRow - req.StartRow
		if size <= 0 {
			size = 100
		}
		sql += ` OFFSET ? LIMIT ?`
		fargs = append(fargs, req.StartRow, size)
	}
	rows, err := list[models.StockMovement](q.db, sql, fargs...)
	if err != nil {
		return nil, err
	}
	out.Rows = rows
	return out, nil
}

// OnHandThrough is the on hand of a scope from movements dated on or before d (nil = all depots).
func (q *Q) OnHandThrough(orgID, partID uint, depotID *uint, d models.Date) (models.Dec4, error) {
	var v models.Dec4
	err := q.db.Raw(`SELECT coalesce(sum(movement_sign(kind) * qty), 0) FROM stock_movements
		WHERE org_id = ? AND part_id = ? AND (?::bigint IS NULL OR depot_id = ?) AND movement_date <= ?`,
		orgID, partID, depotID, depotID, d).Row().Scan(&v)
	return v, err
}

func (q *Q) MovementsByIDs(orgID uint, ids []uint) ([]models.StockMovement, error) {
	sel := strings.Replace(movementSelect, "RUNNING", "NULL::numeric", 1)
	return list[models.StockMovement](q.db, `SELECT * FROM (`+sel+` WHERE m.org_id = ? AND m.id = ANY(?::bigint[])) x ORDER BY id`,
		orgID, uintArray(ids))
}
