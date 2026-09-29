package repository

import (
	"encoding/json"
	"time"

	"github.com/fpirim/fikopar/backend/internal/models"
)

// ImportRecord is one price_imports row; JSON columns stay raw.
type ImportRecord struct {
	ID             uint        `gorm:"column:id"`
	CatalogID      uint        `gorm:"column:catalog_id"`
	CatalogCode    int         `gorm:"column:catalog_code"`
	FileName       string      `gorm:"column:file_name"`
	FileSha256     string      `gorm:"column:file_sha256"`
	FileSize       int         `gorm:"column:file_size"`
	Format         string      `gorm:"column:format"`
	Preset         *string     `gorm:"column:preset"`
	Mapping        string      `gorm:"column:mapping"`
	ValidFrom      models.Date `gorm:"column:valid_from"`
	Missing        string      `gorm:"column:missing"`
	Status         string      `gorm:"column:status"`
	FailureCode    *string     `gorm:"column:failure_code"`
	FailureDetail  *string     `gorm:"column:failure_detail"`
	Headers        string      `gorm:"column:headers"`
	Sample         string      `gorm:"column:sample"`
	Unmapped       string      `gorm:"column:unmapped"`
	CatalogVersion *int64      `gorm:"column:catalog_version"`
	RowCount       int         `gorm:"column:row_count"`
	Counts         string      `gorm:"column:counts"`
	Result         *string     `gorm:"column:result"`
	DuplicateOf    *uint       `gorm:"column:duplicate_of"`
	CreatedBy      *string     `gorm:"column:created_by"`
	CreatedAt      time.Time   `gorm:"column:created_at"`
	ExpiresAt      time.Time   `gorm:"column:expires_at"`
	AppliedBy      *string     `gorm:"column:applied_by"`
	AppliedAt      *time.Time  `gorm:"column:applied_at"`
	Version        int         `gorm:"column:version"`
}

const importSelect = `SELECT i.id, i.catalog_id, c.code AS catalog_code, i.file_name, i.file_sha256, i.file_size, i.format, i.preset,
	i.mapping::text AS mapping, i.valid_from, i.missing, i.status, i.failure_code, i.failure_detail, i.headers::text AS headers,
	i.sample::text AS sample, array_to_json(i.unmapped)::text AS unmapped, i.catalog_version, i.row_count, i.counts::text AS counts,
	i.result::text AS result,
	(SELECT max(d.id) FROM price_imports d WHERE d.org_id = i.org_id AND d.catalog_id = i.catalog_id
	   AND d.file_sha256 = i.file_sha256 AND d.status = 'APPLIED' AND d.id <> i.id) AS duplicate_of,
	split_part(cu.email, '@', 1) AS created_by, i.created_at, i.expires_at, split_part(au.email, '@', 1) AS applied_by,
	i.applied_at, i.version
	FROM price_imports i
	JOIN catalogs c ON c.id = i.catalog_id
	LEFT JOIN users cu ON cu.id = i.created_by
	LEFT JOIN users au ON au.id = i.applied_by`

type ImportInsert struct {
	CatalogID uint
	FileName  string
	Sha256    string
	Size      int
	Format    string
	Preset    *string
	Mapping   string
	ValidFrom models.Date
	CreatedBy *uint
}

// InsertImport stores a new job in PARSING with its file.
func (q *Q) InsertImport(orgID uint, in ImportInsert, content []byte) (uint, error) {
	var id uint
	err := q.db.Raw(`INSERT INTO price_imports (org_id, catalog_id, file_name, file_sha256, file_size, format, preset, mapping,
			valid_from, created_by)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?) RETURNING id`,
		orgID, in.CatalogID, in.FileName, in.Sha256, in.Size, in.Format, in.Preset, in.Mapping, in.ValidFrom, in.CreatedBy).Row().Scan(&id)
	if err != nil {
		return 0, err
	}
	return id, q.db.Exec(`INSERT INTO price_import_files (import_id, content) VALUES (?, ?)`, id, content).Error
}

func (q *Q) GetImport(orgID, id uint) (*ImportRecord, error) {
	return first[ImportRecord](q.db, importSelect+` WHERE i.org_id = ? AND i.id = ?`, orgID, id)
}

// LockImport takes the job row lock; later reads in the transaction see the committed state.
func (q *Q) LockImport(orgID, id uint) error {
	var ids []uint
	if err := q.db.Raw(`SELECT id FROM price_imports WHERE org_id = ? AND id = ? FOR UPDATE`, orgID, id).Scan(&ids).Error; err != nil {
		return err
	}
	if len(ids) == 0 {
		return errNotFound
	}
	return nil
}

func (q *Q) ListImports(orgID, catalogID, beforeID uint, limit int) ([]ImportRecord, error) {
	return list[ImportRecord](q.db, importSelect+` WHERE i.org_id = ? AND (?::bigint = 0 OR i.catalog_id = ?) AND (?::bigint = 0 OR i.id < ?)
		ORDER BY i.id DESC LIMIT ?`, orgID, catalogID, catalogID, beforeID, beforeID, limit)
}

func (q *Q) ImportFile(id uint) ([]byte, error) {
	var b []byte
	err := q.db.Raw(`SELECT content FROM price_import_files WHERE import_id = ?`, id).Row().Scan(&b)
	return b, err
}

// ExpireImports marks unapplied jobs past their expiry EXPIRED and drops their files.
func (q *Q) ExpireImports(orgID uint) error {
	return q.db.Exec(`WITH e AS (
			UPDATE price_imports SET status = 'EXPIRED', version = version + 1
			WHERE org_id = ? AND expires_at < now() AND status IN ('PARSING','NEEDS_MAPPING','READY') RETURNING id)
		DELETE FROM price_import_files f USING e WHERE f.import_id = e.id`, orgID).Error
}

// ImportPreview is what a (re)parse writes back to the job.
type ImportPreview struct {
	Status         string
	Mapping        string
	ValidFrom      models.Date
	FailureCode    *string
	FailureDetail  *string
	Headers        string
	Sample         string
	Unmapped       []string
	CatalogVersion *int64
	RowCount       int
	Counts         string
}

func (q *Q) SaveImportPreview(id uint, p ImportPreview, bump bool) error {
	unmapped := p.Unmapped
	if unmapped == nil {
		unmapped = []string{}
	}
	inc := 0
	if bump {
		inc = 1
	}
	return q.db.Exec(`UPDATE price_imports SET status = ?, mapping = ?::jsonb, valid_from = ?, failure_code = ?, failure_detail = ?,
		headers = ?::jsonb, sample = ?::jsonb, unmapped = ?::text[], catalog_version = ?, row_count = ?, counts = ?::jsonb,
		version = version + ? WHERE id = ?`,
		p.Status, p.Mapping, p.ValidFrom, p.FailureCode, p.FailureDetail, p.Headers, p.Sample, textArray(unmapped),
		p.CatalogVersion, p.RowCount, p.Counts, inc, id).Error
}

func (q *Q) DiscardImport(id uint) error {
	if err := q.db.Exec(`UPDATE price_imports SET status = 'DISCARDED', version = version + 1 WHERE id = ?`, id).Error; err != nil {
		return err
	}
	return q.db.Exec(`DELETE FROM price_import_files WHERE import_id = ?`, id).Error
}

func (q *Q) SetImportApplying(id uint) error {
	return q.db.Exec(`UPDATE price_imports SET status = 'APPLYING' WHERE id = ?`, id).Error
}

func (q *Q) MarkImportApplied(id uint, missing, result string, by *uint) error {
	if err := q.db.Exec(`UPDATE price_imports SET status = 'APPLIED', missing = ?, result = ?::jsonb, applied_by = ?, applied_at = now(),
		version = version + 1 WHERE id = ?`, missing, result, by, id).Error; err != nil {
		return err
	}
	return q.db.Exec(`DELETE FROM price_import_files WHERE import_id = ?`, id).Error
}

// DiffPart is a catalog part as the preview compares it.
type DiffPart struct {
	ID         uint    `gorm:"column:id"`
	StockNo    string  `gorm:"column:stock_no"`
	Name       string  `gorm:"column:name"`
	Type       *string `gorm:"column:type"`
	Brand      *string `gorm:"column:brand"`
	PackageQty int     `gorm:"column:package_qty"`
	VatPct     string  `gorm:"column:vat_pct"`
	IsActive   bool    `gorm:"column:is_active"`
}

func (q *Q) CatalogDiffParts(orgID, catalogID uint) ([]DiffPart, error) {
	return list[DiffPart](q.db, `SELECT id, stock_no, name, type, brand, package_qty, trim_scale(vat_pct)::text AS vat_pct, is_active
		FROM parts WHERE org_id = ? AND catalog_id = ? ORDER BY stock_no`, orgID, catalogID)
}

func (q *Q) CatalogCodes(orgID, catalogID uint) ([]models.PartCode, error) {
	return list[models.PartCode](q.db, `SELECT pc.part_id, pc.kind, pc.code, pc.normalized_code, pc.equivalent_part_id
		FROM part_codes pc JOIN parts p ON p.id = pc.part_id
		WHERE p.org_id = ? AND p.catalog_id = ? ORDER BY pc.part_id, pc.id`, orgID, catalogID)
}

type PricePoint struct {
	PartID    uint        `gorm:"column:part_id"`
	ValidFrom models.Date `gorm:"column:valid_from"`
	Price     models.Dec4 `gorm:"column:price"`
}

// CatalogPriceHistory lists every price of the catalog's parts in (part, valid_from) order.
func (q *Q) CatalogPriceHistory(orgID, catalogID uint) ([]PricePoint, error) {
	return list[PricePoint](q.db, `SELECT pr.part_id, pr.valid_from, pr.price FROM prices pr JOIN parts p ON p.id = pr.part_id
		WHERE p.org_id = ? AND p.catalog_id = ? ORDER BY pr.part_id, pr.valid_from`, orgID, catalogID)
}

// PartByCatalogCode resolves (catalog code, stock no) inside the company.
func (q *Q) PartByCatalogCode(orgID uint, catalogCode int, stockNo string) (*PartRef, error) {
	return first[PartRef](q.db, `SELECT p.id, p.catalog_id, p.stock_no, p.is_active, p.version FROM parts p
		JOIN catalogs c ON c.id = p.catalog_id WHERE p.org_id = ? AND c.code = ? AND p.stock_no = ?`, orgID, catalogCode, stockNo)
}

// ImportRowInsert is one preview row; JSON fields are already encoded.
type ImportRowInsert struct {
	RowNo          *int     `json:"row_no"`
	PartID         *uint    `json:"part_id"`
	StockNo        *string  `json:"stock_no"`
	Parsed         any      `json:"parsed"`
	Current        any      `json:"current"`
	Changes        []string `json:"changes"`
	ChangedFields  []string `json:"changed_fields"`
	PriceChangePct *string  `json:"price_change_pct"`
	Errors         any      `json:"errors"`
	Warnings       any      `json:"warnings"`
}

const importRowChunk = 2000

func (q *Q) ReplaceImportRows(orgID, importID uint, rows []ImportRowInsert) error {
	if err := q.db.Exec(`DELETE FROM price_import_rows WHERE import_id = ?`, importID).Error; err != nil {
		return err
	}
	for start := 0; start < len(rows); start += importRowChunk {
		b, err := json.Marshal(rows[start:min(start+importRowChunk, len(rows))])
		if err != nil {
			return err
		}
		if err := q.db.Exec(`INSERT INTO price_import_rows (org_id, import_id, row_no, part_id, stock_no, parsed, current, changes,
				changed_fields, price_change_pct, errors, warnings)
			SELECT ?, ?, x.row_no, x.part_id, x.stock_no, coalesce(x.parsed, '{}'), x.current,
			       ARRAY(SELECT jsonb_array_elements_text(coalesce(x.changes, '[]'))),
			       ARRAY(SELECT jsonb_array_elements_text(coalesce(x.changed_fields, '[]'))),
			       x.price_change_pct::numeric, coalesce(x.errors, '[]'), coalesce(x.warnings, '[]')
			FROM jsonb_to_recordset(?::jsonb) AS x(row_no int, part_id bigint, stock_no text, parsed jsonb, current jsonb,
			     changes jsonb, changed_fields jsonb, price_change_pct text, errors jsonb, warnings jsonb)`,
			orgID, importID, string(b)).Error; err != nil {
			return err
		}
	}
	return nil
}

// ApplyRow is a preview row as apply reads it.
type ApplyRow struct {
	RowNo     *int   `gorm:"column:row_no"`
	PartID    *uint  `gorm:"column:part_id"`
	Parsed    string `gorm:"column:parsed"`
	Changes   string `gorm:"column:changes"`
	Changed   string `gorm:"column:changed_fields"`
	HasErrors bool   `gorm:"column:has_errors"`
	Inactive  bool   `gorm:"column:inactive"`
}

func (q *Q) ImportApplyRows(importID uint) ([]ApplyRow, error) {
	return list[ApplyRow](q.db, `SELECT r.row_no, r.part_id, r.parsed::text AS parsed, array_to_json(r.changes)::text AS changes,
		array_to_json(r.changed_fields)::text AS changed_fields, jsonb_array_length(r.errors) > 0 AS has_errors,
		EXISTS (SELECT 1 FROM jsonb_array_elements(r.warnings) w WHERE w->>'code' = 'INACTIVE_PART') AS inactive
		FROM price_import_rows r WHERE r.import_id = ? ORDER BY r.row_no NULLS LAST, r.id`, importID)
}

// PartAttrs are the non-empty file values that replace part fields.
type PartAttrs struct {
	Name       *string
	Type       *string
	Brand      *string
	PackageQty *int
	VatPct     *string
	Activate   bool
}

func (q *Q) UpdatePartAttrs(orgID, id uint, a PartAttrs) error {
	return q.db.Exec(`UPDATE parts SET name = coalesce(?, name), type = coalesce(?, type), brand = coalesce(?, brand),
		package_qty = coalesce(?, package_qty), vat_pct = coalesce(?::numeric, vat_pct), is_active = is_active OR ?,
		version = version + 1, updated_at = now() WHERE org_id = ? AND id = ?`,
		a.Name, a.Type, a.Brand, a.PackageQty, a.VatPct, a.Activate, orgID, id).Error
}

// DeactivatePart reports whether the part was active.
func (q *Q) DeactivatePart(orgID, id uint) (bool, error) {
	res := q.db.Exec(`UPDATE parts SET is_active = false, version = version + 1, updated_at = now()
		WHERE org_id = ? AND id = ? AND is_active`, orgID, id)
	return res.RowsAffected == 1, res.Error
}

// ImportGridRow is a preview row for the grid.
type ImportGridRow struct {
	ID             uint    `gorm:"column:id"`
	RowNo          *int    `gorm:"column:row_no"`
	PartID         *uint   `gorm:"column:part_id"`
	StockNo        *string `gorm:"column:stock_no"`
	Parsed         string  `gorm:"column:parsed"`
	Current        *string `gorm:"column:current"`
	Changes        string  `gorm:"column:changes"`
	Changed        string  `gorm:"column:changed_fields"`
	PriceChangePct *string `gorm:"column:price_change_pct"`
	Errors         string  `gorm:"column:errors"`
	Warnings       string  `gorm:"column:warnings"`
}

var importGridCols = map[string]GridCol{
	"rowNo":          {Expr: "r.row_no", Kind: ColNumber},
	"stockNo":        {Expr: "r.stock_no", Kind: ColText},
	"description":    {Expr: "(r.parsed->>'description')", Kind: ColText},
	"brand":          {Expr: "(r.parsed->>'brand')", Kind: ColText},
	"price":          {Expr: "(r.parsed->>'price')::numeric", Kind: ColNumber},
	"currentPrice":   {Expr: "(r.current->>'price')::numeric", Kind: ColNumber},
	"priceChangePct": {Expr: "r.price_change_pct", Kind: ColNumber},
	"validFrom":      {Expr: "(r.parsed->>'validFrom')::date", Kind: ColDate},
	"changes":        {Expr: "array_to_string(r.changes, ',')", Kind: ColText},
	"errors":         {Expr: "(SELECT string_agg(e->>'code', ',') FROM jsonb_array_elements(r.errors) e)", Kind: ColText},
}

var changeFilters = map[string]string{
	"ALL":        "true",
	"NEW":        "'NEW' = ANY(r.changes)",
	"PRICE":      "'PRICE' = ANY(r.changes)",
	"ATTRIBUTES": "'ATTRIBUTES' = ANY(r.changes)",
	"MISSING":    "'MISSING' = ANY(r.changes)",
	"DELETE":     "'DELETE' = ANY(r.changes)",
	"UNCHANGED":  "r.changes = '{}' AND r.errors = '[]'",
	"ERROR":      "jsonb_array_length(r.errors) > 0",
	"WARNING":    "jsonb_array_length(r.warnings) > 0",
}

// ValidImportChange reports whether change names a preview tab.
func ValidImportChange(change string) bool { _, ok := changeFilters[change]; return ok }

// GridImportRows pages the preview; all returns every filtered row.
func (q *Q) GridImportRows(importID uint, req ReportGridRequest, change string, all bool) ([]ImportGridRow, int64, int64, error) {
	if err := knownCols(req, importGridCols); err != nil {
		return nil, 0, 0, err
	}
	where, args, err := whereClause(req.FilterModel, importGridCols)
	if err != nil {
		return nil, 0, 0, err
	}
	tab := changeFilters[change]
	base := ` FROM price_import_rows r WHERE r.import_id = ? AND ` + tab
	var unfiltered, total int64
	if err := q.db.Raw(`SELECT count(*)`+base, importID).Row().Scan(&unfiltered); err != nil {
		return nil, 0, 0, err
	}
	filtered := base + ` AND ` + where
	fargs := append([]any{importID}, args...)
	if err := q.db.Raw(`SELECT count(*)`+filtered, fargs...).Row().Scan(&total); err != nil {
		return nil, 0, 0, err
	}
	sql := `SELECT r.id, r.row_no, r.part_id, r.stock_no, r.parsed::text AS parsed, r.current::text AS current,
		array_to_json(r.changes)::text AS changes, array_to_json(r.changed_fields)::text AS changed_fields,
		r.price_change_pct::text AS price_change_pct, r.errors::text AS errors, r.warnings::text AS warnings` + filtered +
		` ORDER BY ` + orderClause(req.SortModel, importGridCols, "r.row_no ASC NULLS LAST, r.stock_no, r.id")
	if !all {
		size := req.EndRow - req.StartRow
		if size <= 0 || size > 1000 {
			size = 100
		}
		sql += ` LIMIT ? OFFSET ?`
		fargs = append(fargs, size, max(req.StartRow, 0))
	} else {
		sql += ` LIMIT 100000`
	}
	rows, err := list[ImportGridRow](q.db, sql, fargs...)
	return rows, total, unfiltered, err
}

// ImportPrice is one price an import applies.
type ImportPrice struct {
	PartID    uint        `json:"part_id"`
	Price     models.Dec4 `json:"price"`
	ValidFrom models.Date `json:"valid_from"`
}

// InsertImportPrices adds the rows in chunks and returns how many were written; a price already on the
// date is a unique violation (the preview said otherwise, so the catalog moved).
func (q *Q) InsertImportPrices(orgID, importID uint, by *uint, prices []ImportPrice) (int64, error) {
	var n int64
	for start := 0; start < len(prices); start += importRowChunk {
		b, err := json.Marshal(prices[start:min(start+importRowChunk, len(prices))])
		if err != nil {
			return 0, err
		}
		res := q.db.Exec(`INSERT INTO prices (org_id, part_id, price, valid_from, source, import_id, created_by)
			SELECT ?, x.part_id, x.price::numeric, x.valid_from, 'IMPORT', ?, ?
			FROM jsonb_to_recordset(?::jsonb) AS x(part_id bigint, price text, valid_from date)`, orgID, importID, by, string(b))
		if res.Error != nil {
			return 0, res.Error
		}
		n += res.RowsAffected
	}
	return n, nil
}

// NewPart is a part an import creates.
type NewPart struct {
	StockNo    string  `json:"stock_no"`
	Name       string  `json:"name"`
	Type       *string `json:"type"`
	Brand      *string `json:"brand"`
	PackageQty int     `json:"package_qty"`
	VatPct     string  `json:"vat_pct"`
}

// InsertImportParts creates parts in chunks and returns their ids by stock no.
func (q *Q) InsertImportParts(orgID, catalogID uint, parts []NewPart) (map[string]uint, error) {
	ids := make(map[string]uint, len(parts))
	for start := 0; start < len(parts); start += importRowChunk {
		b, err := json.Marshal(parts[start:min(start+importRowChunk, len(parts))])
		if err != nil {
			return nil, err
		}
		var rows []struct {
			ID      uint   `gorm:"column:id"`
			StockNo string `gorm:"column:stock_no"`
		}
		if err := q.db.Raw(`INSERT INTO parts (org_id, catalog_id, stock_no, name, type, brand, unit, package_qty, vat_pct)
			SELECT ?, ?, x.stock_no, x.name, x.type, x.brand, 'PCS', x.package_qty, x.vat_pct::numeric
			FROM jsonb_to_recordset(?::jsonb) AS x(stock_no text, name text, type text, brand text, package_qty int, vat_pct text)
			RETURNING id, stock_no`, orgID, catalogID, string(b)).Scan(&rows).Error; err != nil {
			return nil, err
		}
		for _, r := range rows {
			ids[r.StockNo] = r.ID
		}
	}
	return ids, nil
}

// NewCode is a code an import adds.
type NewCode struct {
	PartID           uint   `json:"part_id"`
	Kind             string `json:"kind"`
	Code             string `json:"code"`
	EquivalentPartID *uint  `json:"equivalent_part_id"`
}

func (q *Q) InsertImportCodes(orgID uint, codes []NewCode) error {
	for start := 0; start < len(codes); start += importRowChunk {
		b, err := json.Marshal(codes[start:min(start+importRowChunk, len(codes))])
		if err != nil {
			return err
		}
		if err := q.db.Exec(`INSERT INTO part_codes (org_id, part_id, kind, code, equivalent_part_id)
			SELECT ?, x.part_id, x.kind, x.code, x.equivalent_part_id
			FROM jsonb_to_recordset(?::jsonb) AS x(part_id bigint, kind text, code text, equivalent_part_id bigint)
			ON CONFLICT DO NOTHING`, orgID, string(b)).Error; err != nil {
			return err
		}
	}
	return nil
}
