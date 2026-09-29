package services

import (
	"sort"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/pricefile"
	"github.com/fpirim/fikopar/backend/internal/repository"
)

type catalogKey struct {
	id   uint
	code int
}

// differ compares file rows with the catalog as it is now.
type differ struct {
	q         *repository.Q
	orgID     uint
	cat       catalogKey
	m         pricefile.Mapping
	validFrom models.Date
	version   int64
	parts     map[string]*repository.DiffPart
	codes     map[uint][]models.PartCode
	prices    map[uint][]repository.PricePoint
	equiv     map[pricefile.Equivalent]*uint
	inFile    map[string]bool
}

func newDiffer(q *repository.Q, orgID uint, cat catalogKey, m pricefile.Mapping, validFrom models.Date) (*differ, error) {
	c, err := q.GetCatalog(orgID, cat.id)
	if err != nil {
		return nil, err
	}
	d := &differ{q: q, orgID: orgID, cat: cat, m: m, validFrom: validFrom, version: c.ContentVersion,
		parts: map[string]*repository.DiffPart{}, codes: map[uint][]models.PartCode{}, prices: map[uint][]repository.PricePoint{},
		equiv: map[pricefile.Equivalent]*uint{}}
	parts, err := q.CatalogDiffParts(orgID, cat.id)
	if err != nil {
		return nil, err
	}
	for i := range parts {
		d.parts[parts[i].StockNo] = &parts[i]
	}
	codes, err := q.CatalogCodes(orgID, cat.id)
	if err != nil {
		return nil, err
	}
	for _, c := range codes {
		d.codes[c.PartID] = append(d.codes[c.PartID], c)
	}
	prices, err := q.CatalogPriceHistory(orgID, cat.id)
	if err != nil {
		return nil, err
	}
	for _, p := range prices {
		d.prices[p.PartID] = append(d.prices[p.PartID], p)
	}
	return d, nil
}

// priceOn is the price valid on the date and whether a row starts exactly then.
func (d *differ) priceOn(partID uint, on models.Date) (*models.Dec4, *models.Dec4) {
	var cur, exact *models.Dec4
	for _, p := range d.prices[partID] {
		if p.ValidFrom.After(on.Time) {
			break
		}
		v := p.Price
		cur = &v
		if p.ValidFrom.Equal(on.Time) {
			exact = &v
		}
	}
	return cur, exact
}

func (d *differ) latest(partID uint) *models.Date {
	h := d.prices[partID]
	if len(h) == 0 {
		return nil
	}
	return &h[len(h)-1].ValidFrom
}

// resolve finds the part an EQUIVALENT points to; a stock no of this catalog that the file creates counts too.
func (d *differ) resolve(e pricefile.Equivalent, fileNew map[string]bool) bool {
	if e.CatalogCode == d.cat.code {
		if _, ok := d.parts[e.StockNo]; ok || fileNew[e.StockNo] {
			return true
		}
	}
	id, ok := d.equiv[e]
	if !ok {
		ref, err := d.q.PartByCatalogCode(d.orgID, e.CatalogCode, e.StockNo)
		if err == nil {
			id = &ref.ID
		}
		d.equiv[e] = id
	}
	return id != nil
}

func codeSet(codes []models.PartCode, kind string) []string {
	var out []string
	for _, c := range codes {
		if c.Kind == kind {
			out = append(out, c.NormalizedCode)
		}
	}
	sort.Strings(out)
	return out
}

func normSet(vals []string) []string {
	out := make([]string, 0, len(vals))
	seen := map[string]bool{}
	for _, v := range vals {
		n := NormalizeCode(v)
		if n != "" && !seen[n] {
			seen[n] = true
			out = append(out, n)
		}
	}
	sort.Strings(out)
	return out
}

func codeValues(codes []models.PartCode, kind string) []string {
	out := []string{}
	for _, c := range codes {
		if c.Kind == kind {
			out = append(out, c.Code)
		}
	}
	return out
}

func (d *differ) current(p *repository.DiffPart, on models.Date) *ImportFields {
	codes := d.codes[p.ID]
	f := &ImportFields{StockNo: p.StockNo, Description: &p.Name, Brand: p.Brand, Type: p.Type, PackQty: &p.PackageQty,
		VatPct: &p.VatPct, OEM: codeValues(codes, models.CodeOEM), AltCodes: codeValues(codes, models.CodeALT)}
	if m := codeValues(codes, models.CodeManufacturer); len(m) > 0 {
		f.ManufacturerCode = &m[0]
	}
	f.Price, _ = d.priceOn(p.ID, on)
	f.ValidFrom = &on
	return f
}

func fieldsOf(p pricefile.Parsed, vf models.Date) *ImportFields {
	f := &ImportFields{StockNo: p.StockNo, Description: p.Description, Brand: p.Brand, Type: p.Type,
		ManufacturerCode: p.ManufacturerCode, OEM: p.OEM, AltCodes: p.AltCodes, Price: p.Price, ValidFrom: &vf,
		PackQty: p.PackQty, VatPct: p.VatPct, Equivalent: p.Equivalent}
	if f.OEM == nil {
		f.OEM = []string{}
	}
	if f.AltCodes == nil {
		f.AltCodes = []string{}
	}
	return f
}

func differs(file, cur *string) bool {
	return file != nil && (cur == nil || *file != *cur)
}

// attributes lists the targets whose non-empty file value differs from the part.
func (d *differ) attributes(p pricefile.Parsed, part *repository.DiffPart) []string {
	codes := d.codes[part.ID]
	var out []string
	vat := part.VatPct
	pack := part.PackageQty
	if differs(p.Description, &part.Name) {
		out = append(out, "description")
	}
	if differs(p.Brand, part.Brand) {
		out = append(out, "brand")
	}
	if differs(p.Type, part.Type) {
		out = append(out, "type")
	}
	if p.ManufacturerCode != nil && strings.Join(normSet([]string{*p.ManufacturerCode}), "|") != strings.Join(codeSet(codes, models.CodeManufacturer), "|") {
		out = append(out, "manufacturerCode")
	}
	if len(p.OEM) > 0 && strings.Join(normSet(p.OEM), "|") != strings.Join(codeSet(codes, models.CodeOEM), "|") {
		out = append(out, "oem")
	}
	if len(p.AltCodes) > 0 && strings.Join(normSet(p.AltCodes), "|") != strings.Join(codeSet(codes, models.CodeALT), "|") {
		out = append(out, "altCodes")
	}
	if p.PackQty != nil && *p.PackQty != pack {
		out = append(out, "packQty")
	}
	if differs(p.VatPct, &vat) {
		out = append(out, "vatPct")
	}
	return out
}

func issue(field, code string, value *string) pricefile.Issue {
	return pricefile.Issue{Field: field, Code: code, Value: value}
}

type parsedRow struct {
	no     int
	p      pricefile.Parsed
	issues []pricefile.Issue
}

func (d *differ) run(f *pricefile.File) ([]repository.ImportRowInsert, ImportCounts, error) {
	counts := ImportCounts{Rows: len(f.Rows)}
	parsed := make([]parsedRow, len(f.Rows))
	fileNew := map[string]bool{}
	for i, r := range f.Rows {
		p, issues := pricefile.Parse(d.m, pricefile.Extract(d.m, f.Headers, r))
		parsed[i] = parsedRow{no: r.No, p: p, issues: issues}
		if _, ok := d.parts[p.StockNo]; !ok && p.StockNo != "" {
			fileNew[p.StockNo] = true
		}
	}
	seen := map[string]bool{}
	rows := make([]repository.ImportRowInsert, 0, len(parsed))
	for _, pr := range parsed {
		row := d.row(pr, seen, fileNew)
		rows = append(rows, row)
		counts.add(row)
	}
	for _, stockNo := range sortedKeys(d.parts) {
		part := d.parts[stockNo]
		if seen[stockNo] || !part.IsActive {
			continue
		}
		id, no := part.ID, part.StockNo
		rows = append(rows, repository.ImportRowInsert{PartID: &id, StockNo: &no, Current: d.current(part, d.validFrom),
			Changes: []string{"MISSING"}, ChangedFields: []string{}, Errors: []pricefile.Issue{}, Warnings: []pricefile.Issue{}})
		counts.Missing++
	}
	return rows, counts, nil
}

func sortedKeys(m map[string]*repository.DiffPart) []string {
	keys := make([]string, 0, len(m))
	for k := range m {
		keys = append(keys, k)
	}
	sort.Strings(keys)
	return keys
}

func (c *ImportCounts) add(r repository.ImportRowInsert) {
	errs := r.Errors.([]pricefile.Issue)
	warns := r.Warnings.([]pricefile.Issue)
	if len(errs) > 0 {
		c.Errors++
	}
	if len(warns) > 0 {
		c.Warnings++
	}
	for _, ch := range r.Changes {
		switch ch {
		case "NEW":
			c.New++
		case "PRICE":
			c.Price++
		case "ATTRIBUTES":
			c.Attributes++
		case "DELETE":
			c.Delete++
		}
	}
	if len(r.Changes) == 0 && len(errs) == 0 {
		c.Unchanged++
	}
}

func (d *differ) row(pr parsedRow, seen, fileNew map[string]bool) repository.ImportRowInsert {
	p := pr.p
	no := pr.no
	errs := append([]pricefile.Issue{}, pr.issues...)
	warns := []pricefile.Issue{}
	vf := d.validFrom
	if p.ValidFrom != nil {
		vf = *p.ValidFrom
	}
	row := repository.ImportRowInsert{RowNo: &no, Parsed: fieldsOf(p, vf), Changes: []string{}, ChangedFields: []string{}}
	if p.StockNo != "" {
		s := p.StockNo
		row.StockNo = &s
	}
	if p.CatalogCode != nil && *p.CatalogCode != d.cat.code {
		v := itoa(*p.CatalogCode)
		errs = append(errs, issue("catalogCode", "CATALOG_MISMATCH", &v))
	}
	if p.StockNo != "" {
		if seen[p.StockNo] {
			errs = append(errs, issue("code", "DUPLICATE_IN_FILE", row.StockNo))
		}
		seen[p.StockNo] = true
	}
	if p.Equivalent != nil && !d.resolve(*p.Equivalent, fileNew) {
		v := p.Equivalent.StockNo
		warns = append(warns, issue("equivalentCode", "EQUIVALENT_NOT_FOUND", &v))
	}
	isDelete := p.Op != nil && *p.Op == 0
	part := d.parts[p.StockNo]
	switch {
	case p.StockNo == "":
	case part == nil && isDelete:
		warns = append(warns, issue("op", "OP_MISMATCH", nil))
	case part == nil:
		row.Changes = append(row.Changes, "NEW")
		if p.Op != nil && *p.Op == 2 {
			warns = append(warns, issue("op", "OP_MISMATCH", nil))
		}
	default:
		id := part.ID
		row.PartID = &id
		row.Current = d.current(part, vf)
		if isDelete {
			row.Changes = append(row.Changes, "DELETE")
			break
		}
		if p.Op != nil && *p.Op == 1 {
			warns = append(warns, issue("op", "OP_MISMATCH", nil))
		}
		if !part.IsActive {
			warns = append(warns, issue("", "INACTIVE_PART", nil))
		}
		if p.Price != nil {
			cur, exact := d.priceOn(part.ID, vf)
			switch {
			case exact != nil && *exact != *p.Price:
				v := p.Price.String()
				errs = append(errs, issue("price", "PRICE_EXISTS", &v))
			case cur == nil || *cur != *p.Price:
				row.Changes = append(row.Changes, "PRICE")
				row.ChangedFields = append(row.ChangedFields, "price")
				if cur != nil {
					row.PriceChangePct = models.PercentChange(*cur, *p.Price)
				}
				if l := d.latest(part.ID); l != nil && l.After(vf.Time) {
					warns = append(warns, issue("validFrom", "BACKDATED", nil))
				}
			}
		}
		if attrs := d.attributes(p, part); len(attrs) > 0 {
			row.Changes = append(row.Changes, "ATTRIBUTES")
			row.ChangedFields = append(row.ChangedFields, attrs...)
		}
	}
	row.Errors, row.Warnings = errs, warns
	return row
}
