package repository

import (
	"fmt"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/models"
)

type SearchQuery struct {
	Norm      string   // normalized code: upper case, 0-9 A-Z
	Words     []string // folded name words, each must occur
	CatalogID uint
	InStock   uint // depot with available > 0
	Date      models.Date
	After     *SearchCursor
	Limit     int
}

// SearchCursor is the keyset position in the ranked list.
type SearchCursor struct {
	Rank        int    `json:"r"`
	StockNo     string `json:"s"`
	CatalogCode int    `json:"c"`
	PartID      uint   `json:"p"`
}

// searchCandidates ranks every match per part: 1 exact code or stock no, 2 prefix, 3 substring (3+ characters),
// 4 name. Prefixes use the text_pattern_ops indexes, substrings and names the trigram indexes.
func searchCandidates(orgID uint, sq SearchQuery) (string, []any) {
	var parts []string
	var args []any
	if sq.Norm != "" {
		pattern := sq.Norm + "%"
		if len(sq.Norm) >= 3 {
			pattern = "%" + sq.Norm + "%"
		}
		rank := `CASE WHEN %[1]s = ? THEN 1 WHEN %[1]s LIKE ? THEN 2 ELSE 3 END`
		parts = append(parts,
			`SELECT p.id AS part_id, `+fmt.Sprintf(rank, "p.stock_no_norm")+` AS rank, 0 AS kind_order,
				'STOCK_NO' AS match_kind, p.stock_no AS matched_code
			 FROM parts p WHERE p.org_id = ? AND p.stock_no_norm LIKE ?`,
			`SELECT pc.part_id, `+fmt.Sprintf(rank, "pc.normalized_code")+`,
				CASE pc.kind WHEN 'MANUFACTURER' THEN 1 WHEN 'OEM' THEN 2 WHEN 'ALT' THEN 3 ELSE 4 END, pc.kind, pc.code
			 FROM part_codes pc WHERE pc.org_id = ? AND pc.normalized_code LIKE ?`)
		args = append(args, sq.Norm, sq.Norm+"%", orgID, pattern, sq.Norm, sq.Norm+"%", orgID, pattern)
	}
	if len(sq.Words) > 0 {
		cond := make([]string, len(sq.Words))
		args = append(args, orgID)
		for i, w := range sq.Words {
			cond[i] = `p.name_fold LIKE ?`
			args = append(args, "%"+escapeLike(w)+"%")
		}
		parts = append(parts, `SELECT p.id, 4, 9, 'NAME', NULL FROM parts p WHERE p.org_id = ? AND `+strings.Join(cond, " AND "))
	}
	return strings.Join(parts, " UNION ALL "), args
}

func escapeLike(s string) string {
	return strings.NewReplacer(`\`, `\\`, `%`, `\%`, `_`, `\_`).Replace(s)
}

func (q *Q) searchBase(orgID uint, sq SearchQuery) (string, []any) {
	cand, args := searchCandidates(orgID, sq)
	sql := `WITH cand AS (` + cand + `),
		best AS (SELECT DISTINCT ON (part_id) part_id, rank, match_kind, matched_code FROM cand ORDER BY part_id, rank, kind_order),
		hits AS (
			SELECT b.part_id, b.rank, b.match_kind, b.matched_code, p.catalog_id, c.code AS catalog_code, p.stock_no, p.name,
			       p.brand, p.type, trim_scale(p.vat_pct)::text AS vat_pct, p.unit, p.package_qty
			FROM best b JOIN parts p ON p.id = b.part_id JOIN catalogs c ON c.id = p.catalog_id
			WHERE p.is_active AND (?::bigint = 0 OR p.catalog_id = ?)
			  AND (?::bigint = 0 OR EXISTS (SELECT 1 FROM stock_levels l WHERE l.part_id = p.id AND l.depot_id = ? AND l.on_hand - l.reserved > 0)))`
	args = append(args, sq.CatalogID, sq.CatalogID, sq.InStock, sq.InStock)
	return sql, args
}

// SearchCount counts matches up to limit.
func (q *Q) SearchCount(orgID uint, sq SearchQuery, limit int) (int, error) {
	base, args := q.searchBase(orgID, sq)
	var n int
	err := q.db.Raw(base+` SELECT count(*) FROM (SELECT 1 FROM hits LIMIT ?) x`, append(args, limit)...).Row().Scan(&n)
	return n, err
}

// SearchPage returns one ranked page with the list price on sq.Date.
func (q *Q) SearchPage(orgID uint, sq SearchQuery) ([]models.CatalogHit, error) {
	base, args := q.searchBase(orgID, sq)
	where := ``
	if sq.After != nil {
		where = ` WHERE (h.rank, h.stock_no, h.catalog_code, h.part_id) > (?, ?, ?, ?)`
		args = append(args, sq.After.Rank, sq.After.StockNo, sq.After.CatalogCode, sq.After.PartID)
	}
	args = append(args, sq.Limit, orgID, sq.Date)
	return list[models.CatalogHit](q.db, base+`,
		page AS (SELECT * FROM hits h`+where+` ORDER BY h.rank, h.stock_no, h.catalog_code, h.part_id LIMIT ?)
		SELECT page.*, lp.price AS list_price, lp.valid_from AS list_price_valid_from
		FROM page LEFT JOIN LATERAL price_on(?, page.part_id, ?::date) lp ON true
		ORDER BY page.rank, page.stock_no, page.catalog_code, page.part_id`, args...)
}

// HitsByIDs loads parts as hits (resolve); rank and match fields are the caller's.
func (q *Q) HitsByIDs(orgID uint, ids []uint, on models.Date) ([]models.CatalogHit, error) {
	return list[models.CatalogHit](q.db, `SELECT p.id AS part_id, p.catalog_id, c.code AS catalog_code, p.stock_no, p.name, p.brand, p.type,
		trim_scale(p.vat_pct)::text AS vat_pct, p.unit, p.package_qty, lp.price AS list_price, lp.valid_from AS list_price_valid_from
		FROM parts p JOIN catalogs c ON c.id = p.catalog_id
		LEFT JOIN LATERAL price_on(p.org_id, p.id, ?::date) lp ON true
		WHERE p.org_id = ? AND p.id = ANY(?::bigint[]) ORDER BY p.stock_no, c.code, p.id`, on, orgID, uintArray(ids))
}

type DepotStock struct {
	PartID    uint        `gorm:"column:part_id"`
	DepotID   uint        `gorm:"column:depot_id"`
	OnHand    models.Dec4 `gorm:"column:on_hand"`
	Available models.Dec4 `gorm:"column:available"`
}

// StockByDepot has a row for every (part, depot of the company), zeros included.
func (q *Q) StockByDepot(orgID uint, partIDs []uint) ([]DepotStock, error) {
	return list[DepotStock](q.db, `SELECT p.id AS part_id, d.id AS depot_id, coalesce(l.on_hand, 0) AS on_hand,
		coalesce(l.on_hand, 0) - coalesce(l.reserved, 0) AS available
		FROM unnest(?::bigint[]) AS p(id) JOIN depots d ON d.org_id = ?
		LEFT JOIN stock_levels l ON l.part_id = p.id AND l.depot_id = d.id
		ORDER BY p.id, d.code`, uintArray(partIDs), orgID)
}

type CodeMatch struct {
	Norm      string `gorm:"column:norm"`
	PartID    uint   `gorm:"column:part_id"`
	MatchKind string `gorm:"column:match_kind"`
	Code      string `gorm:"column:code"`
}

// ResolveCodes matches normalized codes exactly against stock numbers and codes of active parts.
func (q *Q) ResolveCodes(orgID uint, norms []string) ([]CodeMatch, error) {
	return list[CodeMatch](q.db, `
		SELECT DISTINCT ON (n.norm, m.part_id) n.norm, m.part_id, m.match_kind, m.code
		FROM unnest(?::text[]) AS n(norm)
		JOIN LATERAL (
			SELECT p.id AS part_id, 'STOCK_NO' AS match_kind, p.stock_no AS code, 0 AS ord FROM parts p
			 WHERE p.org_id = ? AND p.stock_no_norm = n.norm AND p.is_active
			UNION ALL
			SELECT pc.part_id, pc.kind, pc.code, 1 FROM part_codes pc JOIN parts p ON p.id = pc.part_id
			 WHERE pc.org_id = ? AND pc.normalized_code = n.norm AND p.is_active) m ON true
		ORDER BY n.norm, m.part_id, m.ord`, textArray(norms), orgID, orgID)
}
