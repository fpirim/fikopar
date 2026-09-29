// Package pricefile reads supplier price lists (CSV, XLSX, legacy LISAKT fixed width) into mapped rows.
package pricefile

import (
	"errors"
	"path/filepath"
	"strings"
	"unicode"
)

var (
	ErrFormat      = errors.New("ERR_IMPORT_FORMAT")
	ErrTooManyRows = errors.New("ERR_IMPORT_TOO_MANY_ROWS")
)

// MaxRows is the data row limit of one file.
const MaxRows = 100_000

// Column locates one target: header / index / letter (CSV, XLSX) or start / length / scale (fixed width).
type Column struct {
	Header string `json:"header,omitempty"`
	Index  int    `json:"index,omitempty"`
	Letter string `json:"letter,omitempty"`
	Start  int    `json:"start,omitempty"`
	Length int    `json:"length,omitempty"`
	Scale  int    `json:"scale,omitempty"`
}

// Mapping is the contract's PriceImportMapping.
type Mapping struct {
	Preset           *string           `json:"preset,omitempty"`
	Format           string            `json:"format,omitempty"`
	Encoding         string            `json:"encoding,omitempty"`
	Delimiter        string            `json:"delimiter,omitempty"`
	DecimalSeparator string            `json:"decimalSeparator,omitempty"`
	DateFormat       string            `json:"dateFormat,omitempty"`
	Sheet            string            `json:"sheet,omitempty"`
	HeaderRow        *int              `json:"headerRow,omitempty"`
	ListSeparator    string            `json:"listSeparator,omitempty"`
	Columns          map[string]Column `json:"columns,omitempty"`
}

// Targets a column can be mapped to.
var Targets = []string{"code", "description", "brand", "type", "manufacturerCode", "oem", "altCodes", "price", "validFrom",
	"packQty", "vatPct", "catalogCode", "op", "equivalentCatalogCode", "equivalentCode"}

var required = []string{"code", "price"}

func lisakt() Mapping {
	preset := "LISAKT"
	zero := 0
	return Mapping{
		Preset: &preset, Format: "FIXED_WIDTH", Encoding: "CP857", DateFormat: "yyyymmdd", DecimalSeparator: ".",
		HeaderRow: &zero, ListSeparator: "|",
		Columns: map[string]Column{
			"op": {Start: 1, Length: 1}, "catalogCode": {Start: 2, Length: 4}, "code": {Start: 6, Length: 15},
			"manufacturerCode": {Start: 21, Length: 30}, "oem": {Start: 51, Length: 30}, "type": {Start: 81, Length: 30},
			"description": {Start: 111, Length: 60}, "brand": {Start: 171, Length: 30}, "price": {Start: 233, Length: 14, Scale: 4},
			"validFrom": {Start: 257, Length: 8}, "packQty": {Start: 265, Length: 5},
			"equivalentCatalogCode": {Start: 285, Length: 4}, "equivalentCode": {Start: 289, Length: 15},
		},
	}
}

type Preset struct {
	Code    string  `json:"code"`
	Name    string  `json:"name"`
	Mapping Mapping `json:"mapping"`
	Notes   string  `json:"notes"`
}

func Presets() []Preset {
	return []Preset{{
		Code: "LISAKT", Name: "Legacy LISAKT price list (COBOL, fixed width, CP857)", Mapping: lisakt(),
		Notes: "Record 319 bytes (OK-OP, ST-KAYIT, BAG). Ignored: MIN/MAX (201-232; per depot now), NOTE, SIRA, " +
			"BISKONTO/BKDV. validFrom 00000000 → job validFrom. op 0 = DELETE, 1 insert, 2 update.",
	}}
}

var aliases = map[string][]string{
	"code":             {"code", "stockno", "stock no", "stok no", "stok kodu", "stok", "stno", "kod"},
	"description":      {"description", "name", "açıklama", "cinsi", "ürün adı", "ad", "tanım"},
	"brand":            {"brand", "marka"},
	"type":             {"type", "tip", "tipi"},
	"manufacturerCode": {"manufacturercode", "manufacturer code", "parça no", "prcno", "üretici kodu"},
	"oem":              {"oem", "oem no", "oemno", "oem kodu"},
	"altCodes":         {"altcodes", "alt codes", "alternatif kod", "eski kod"},
	"price":            {"price", "fiyat", "liste fiyatı", "birim fiyat"},
	"validFrom":        {"validfrom", "valid from", "geçerlilik tarihi", "geçerlilik", "tarih"},
	"packQty":          {"packqty", "pack qty", "paket", "paket adedi"},
	"vatPct":           {"vatpct", "vat", "kdv", "kdv oranı"},
}

// Aliases lists the header names matched per target (case- and Turkish-insensitive).
func Aliases() map[string][]string { return aliases }

var fold = strings.NewReplacer("İ", "i", "I", "i", "ı", "i", "Ş", "s", "ş", "s", "Ğ", "g", "ğ", "g", "Ü", "u", "ü", "u",
	"Ö", "o", "ö", "o", "Ç", "c", "ç", "c")

func headerKey(s string) string {
	var b strings.Builder
	for _, r := range strings.ToLower(fold.Replace(s)) {
		if unicode.IsLetter(r) || unicode.IsDigit(r) {
			b.WriteRune(r)
		}
	}
	return b.String()
}

// Resolve applies the preset, derives the format from the file name and fills defaults.
func Resolve(m Mapping, fileName string) (Mapping, error) {
	if m.Preset != nil {
		if *m.Preset != "LISAKT" {
			return m, ErrFormat
		}
		base := lisakt()
		for k, v := range m.Columns {
			base.Columns[k] = v
		}
		m.Columns = base.Columns
		if m.Format == "" {
			m.Format = base.Format
		}
		if m.Encoding == "" {
			m.Encoding = base.Encoding
		}
		if m.DateFormat == "" {
			m.DateFormat = base.DateFormat
		}
		if m.DecimalSeparator == "" {
			m.DecimalSeparator = base.DecimalSeparator
		}
		if m.HeaderRow == nil {
			m.HeaderRow = base.HeaderRow
		}
	}
	if m.Format == "" {
		m.Format = "CSV"
		if strings.EqualFold(filepath.Ext(fileName), ".xlsx") {
			m.Format = "XLSX"
		}
	}
	switch m.Format {
	case "CSV", "XLSX":
	case "FIXED_WIDTH":
		if m.Preset == nil {
			return m, ErrFormat
		}
	default:
		return m, ErrFormat
	}
	if m.Encoding == "" {
		m.Encoding = "UTF-8"
	}
	if m.Encoding != "UTF-8" && m.Encoding != "CP857" && m.Encoding != "WINDOWS-1254" {
		return m, ErrFormat
	}
	if m.DecimalSeparator == "" {
		m.DecimalSeparator = ","
	}
	if m.DecimalSeparator != "," && m.DecimalSeparator != "." {
		return m, ErrFormat
	}
	if m.DateFormat == "" {
		m.DateFormat = "dd.mm.yyyy"
	}
	if m.HeaderRow == nil {
		one := 1
		m.HeaderRow = &one
	}
	if *m.HeaderRow < 0 || len([]rune(m.Delimiter)) > 1 {
		return m, ErrFormat
	}
	if m.ListSeparator == "" {
		m.ListSeparator = "|"
	}
	if m.Columns == nil {
		m.Columns = map[string]Column{}
	}
	for k := range m.Columns {
		if !isTarget(k) {
			return m, ErrFormat
		}
	}
	return m, nil
}

func isTarget(k string) bool {
	for _, t := range Targets {
		if t == k {
			return true
		}
	}
	return false
}

// AutoMap maps unmapped targets by header name; explicit columns stay as they are.
func AutoMap(m Mapping, headers []string) Mapping {
	if m.Format == "FIXED_WIDTH" || len(headers) == 0 {
		return m
	}
	cols := make(map[string]Column, len(m.Columns))
	for k, v := range m.Columns {
		cols[k] = v
	}
	used := map[int]bool{}
	for _, c := range cols {
		if i := columnIndex(c, headers); i >= 0 {
			used[i] = true
		}
	}
	for _, target := range Targets {
		if _, ok := cols[target]; ok {
			continue
		}
		for _, alias := range aliases[target] {
			key := headerKey(alias)
			found := -1
			for i, h := range headers {
				if !used[i] && headerKey(h) == key {
					found = i
					break
				}
			}
			if found >= 0 {
				cols[target] = Column{Header: headers[found]}
				used[found] = true
				break
			}
		}
	}
	m.Columns = cols
	return m
}

// Unmapped lists required targets without a usable column.
func Unmapped(m Mapping, headers []string) []string {
	out := []string{}
	for _, t := range required {
		c, ok := m.Columns[t]
		if !ok || (m.Format == "FIXED_WIDTH" && (c.Start < 1 || c.Length < 1)) ||
			(m.Format != "FIXED_WIDTH" && columnIndex(c, headers) < 0) {
			out = append(out, t)
		}
	}
	return out
}

// columnIndex is the 0-based cell index of a CSV / XLSX column, or −1.
func columnIndex(c Column, headers []string) int {
	switch {
	case c.Index > 0:
		return c.Index - 1
	case c.Letter != "":
		n := 0
		for _, r := range strings.ToUpper(c.Letter) {
			if r < 'A' || r > 'Z' {
				return -1
			}
			n = n*26 + int(r-'A'+1)
		}
		return n - 1
	case c.Header != "":
		key := headerKey(c.Header)
		for i, h := range headers {
			if headerKey(h) == key {
				return i
			}
		}
	}
	return -1
}
