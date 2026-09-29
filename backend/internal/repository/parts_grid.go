package repository

import (
	"github.com/fpirim/fikopar/backend/internal/models"
)

var partCols = map[string]GridCol{
	"stockNo": {"stock_no", ColText}, "name": {"name", ColText}, "type": {"type", ColText}, "brand": {"brand", ColText},
	"catalogCode": {"catalog_code::text", ColText}, "codes": {"codes_norm", ColText}, "unit": {"unit", ColText},
	"shelf": {"shelf", ColText}, "isActive": {"is_active::text", ColText},
	"vatPct": {"vat_pct::numeric", ColNumber}, "packageQty": {"package_qty", ColNumber}, "listPrice": {"list_price", ColNumber},
	"onHand": {"on_hand", ColNumber}, "reserved": {"reserved", ColNumber}, "available": {"available", ColNumber},
	"minQty": {"min_qty", ColNumber}, "maxQty": {"max_qty", ColNumber},
	"listPriceValidFrom": {"list_price_valid_from", ColDate}, "updatedAt": {"updated_at::date", ColDate},
}

type PartGridQuery struct {
	Grid            ReportGridRequest
	CatalogID       *uint
	DepotID         *uint
	Stock           string
	IncludeInactive bool
	PriceDate       models.Date
	SearchNorm      string
}

type PartGrid struct {
	Rows                        []models.PartRow
	TotalCount, UnfilteredCount int64
	OnHand, Reserved, Available models.Dec4
}

// partBase has one row per part. Stock columns are for the depot, or summed over depots; min, max and shelf only
// with a depot. belowMin / negative without a depot mean "in any depot".
const partBase = `SELECT p.id, p.catalog_id, c.code AS catalog_code, p.stock_no, p.name, p.type, p.brand, p.unit,
	trim_scale(p.vat_pct)::text AS vat_pct, p.package_qty, lp.price AS list_price, lp.valid_from AS list_price_valid_from,
	s.on_hand, s.reserved, s.on_hand - s.reserved AS available,
	pd.min_qty, pd.max_qty, CASE WHEN ?::bigint IS NULL THEN NULL ELSE coalesce(pd.shelf, '') END AS shelf,
	CASE WHEN ?::bigint IS NULL THEN s.any_below ELSE coalesce(pd.min_qty, 0) > 0 AND s.on_hand < pd.min_qty END AS below_min,
	CASE WHEN ?::bigint IS NULL THEN s.any_negative ELSE s.on_hand < 0 END AS negative,
	p.is_active, p.updated_at, p.version,
	coalesce((SELECT string_agg(pc.normalized_code, ' ') FROM part_codes pc WHERE pc.part_id = p.id), '') AS codes_norm
	FROM parts p
	JOIN catalogs c ON c.id = p.catalog_id
	LEFT JOIN LATERAL price_on(p.org_id, p.id, ?::date) lp ON true
	LEFT JOIN LATERAL (
		SELECT coalesce(sum(l.on_hand) FILTER (WHERE ?::bigint IS NULL OR l.depot_id = ?), 0) AS on_hand,
		       coalesce(sum(l.reserved) FILTER (WHERE ?::bigint IS NULL OR l.depot_id = ?), 0) AS reserved,
		       coalesce(bool_or(l.on_hand < 0), false) AS any_negative,
		       coalesce(bool_or(x.min_qty > 0 AND coalesce(l.on_hand, 0) < x.min_qty), false) OR EXISTS (
		         SELECT 1 FROM part_depots y WHERE y.part_id = p.id AND y.min_qty > 0
		           AND NOT EXISTS (SELECT 1 FROM stock_levels z WHERE z.part_id = p.id AND z.depot_id = y.depot_id)) AS any_below
		FROM stock_levels l LEFT JOIN part_depots x ON x.part_id = l.part_id AND x.depot_id = l.depot_id
		WHERE l.part_id = p.id) s ON true
	LEFT JOIN part_depots pd ON pd.part_id = p.id AND pd.depot_id = ?
	WHERE p.org_id = ? AND (?::bigint IS NULL OR p.catalog_id = ?) AND (? OR p.is_active)`

// PartsGrid filters, sorts and pages parts; all returns every filtered row (Excel).
func (q *Q) PartsGrid(orgID uint, in PartGridQuery, all bool) (*PartGrid, error) {
	if err := knownCols(in.Grid, partCols); err != nil {
		return nil, err
	}
	args := []any{in.DepotID, in.DepotID, in.DepotID, in.PriceDate, in.DepotID, in.DepotID, in.DepotID, in.DepotID, in.DepotID,
		orgID, in.CatalogID, in.CatalogID, in.IncludeInactive}
	where, wargs, err := whereClause(in.Grid.FilterModel, partCols)
	if err != nil {
		return nil, err
	}
	stock := map[string]string{
		"": "true", "ALL": "true", "IN_STOCK": "available > 0", "BELOW_MIN": "below_min", "NEGATIVE": "negative",
		"BELOW_MIN_OR_NEGATIVE": "(below_min OR negative)",
	}[in.Stock]
	search := "true"
	var sargs []any
	if in.Grid.Search != "" {
		search = `(stock_no ILIKE ? OR name ILIKE ? OR brand ILIKE ? OR (? <> '' AND codes_norm LIKE ?))`
		t := "%" + escapeLike(in.Grid.Search) + "%"
		sargs = []any{t, t, t, in.SearchNorm, "%" + in.SearchNorm + "%"}
	}
	out := &PartGrid{}
	if err := q.db.Raw(`SELECT count(*) FROM parts p WHERE p.org_id = ? AND (?::bigint IS NULL OR p.catalog_id = ?) AND (? OR p.is_active)`,
		orgID, in.CatalogID, in.CatalogID, in.IncludeInactive).Row().Scan(&out.UnfilteredCount); err != nil {
		return nil, err
	}
	base := `SELECT * FROM (` + partBase + `) x WHERE ` + where + ` AND ` + stock + ` AND ` + search
	fargs := append(append(append([]any{}, args...), wargs...), sargs...)
	if err := q.db.Raw(`SELECT count(*), coalesce(sum(on_hand), 0), coalesce(sum(reserved), 0), coalesce(sum(available), 0) FROM (`+base+`) t`,
		fargs...).Row().Scan(&out.TotalCount, &out.OnHand, &out.Reserved, &out.Available); err != nil {
		return nil, err
	}
	sql := base + ` ORDER BY ` + orderClause(in.Grid.SortModel, partCols, "stock_no, catalog_code, id")
	if all {
		sql += ` LIMIT 100000`
	} else {
		size := in.Grid.EndRow - in.Grid.StartRow
		if size <= 0 {
			size = 100
		}
		sql += ` OFFSET ? LIMIT ?`
		fargs = append(fargs, in.Grid.StartRow, size)
	}
	rows, err := list[models.PartRow](q.db, sql, fargs...)
	out.Rows = rows
	return out, err
}
