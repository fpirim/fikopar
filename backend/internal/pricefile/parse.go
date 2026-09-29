package pricefile

import (
	"math/big"
	"regexp"
	"strconv"
	"strings"
	"time"
	"unicode/utf8"

	"github.com/fpirim/fikopar/backend/internal/models"
)

// Issue is a per-row error or warning (contract PriceImportIssue).
type Issue struct {
	Field string  `json:"field,omitempty"`
	Code  string  `json:"code"`
	Value *string `json:"value"`
}

type Equivalent struct {
	CatalogCode int    `json:"catalogCode"`
	StockNo     string `json:"stockNo"`
}

// Parsed holds the typed values of one row; nil / empty = the cell was empty.
type Parsed struct {
	StockNo          string
	Description      *string
	Brand            *string
	Type             *string
	ManufacturerCode *string
	OEM              []string
	AltCodes         []string
	Price            *models.Dec4
	ValidFrom        *models.Date
	PackQty          *int
	VatPct           *string
	CatalogCode      *int
	Op               *int
	Equivalent       *Equivalent
}

var digits = regexp.MustCompile(`^\d+$`)

type parser struct {
	m      Mapping
	raw    map[string]string
	issues []Issue
}

func (p *parser) issue(field, code string) {
	v, ok := p.raw[field]
	var val *string
	if ok {
		val = &v
	}
	p.issues = append(p.issues, Issue{Field: field, Code: code, Value: val})
}

func (p *parser) text(field string, max int) *string {
	v, ok := p.raw[field]
	if !ok {
		return nil
	}
	if utf8.RuneCountInString(v) > max {
		p.issue(field, "TOO_LONG")
	}
	return &v
}

func (p *parser) list(field string) []string {
	v, ok := p.raw[field]
	if !ok {
		return nil
	}
	var out []string
	for _, s := range strings.Split(v, p.m.ListSeparator) {
		if s = strings.TrimSpace(s); s != "" {
			if utf8.RuneCountInString(s) > 40 {
				p.issue(field, "TOO_LONG")
			}
			out = append(out, s)
		}
	}
	return out
}

// canonical turns a file number into "123.45" per the decimal separator; scaled columns are already canonical.
func (p *parser) canonical(field string) string {
	v := strings.ReplaceAll(p.raw[field], " ", "")
	if p.m.Columns[field].Scale > 0 {
		return v
	}
	if p.m.DecimalSeparator == "," {
		return strings.ReplaceAll(strings.ReplaceAll(v, ".", ""), ",", ".")
	}
	return strings.ReplaceAll(v, ",", "")
}

// decimal parses and rounds half up to four digits.
func decimal(s string) (models.Dec4, bool) {
	r, ok := new(big.Rat).SetString(s)
	if !ok || strings.ContainsAny(s, "eE/") {
		return 0, false
	}
	r.Mul(r, big.NewRat(10000, 1))
	neg := r.Sign() < 0
	r.Abs(r)
	n := new(big.Int).Quo(new(big.Int).Add(new(big.Int).Mul(r.Num(), big.NewInt(2)), r.Denom()), new(big.Int).Mul(r.Denom(), big.NewInt(2)))
	if !n.IsInt64() || n.Int64() > 999_999_999_999_999_999 {
		return 0, false
	}
	if neg {
		return models.Dec4(-n.Int64()), true
	}
	return models.Dec4(n.Int64()), true
}

func (p *parser) zeroIsEmpty(field string) bool {
	return p.m.Format == "FIXED_WIDTH" && strings.Trim(p.raw[field], "0") == ""
}

func (p *parser) integer(field string) *int {
	if _, ok := p.raw[field]; !ok || p.zeroIsEmpty(field) {
		return nil
	}
	n, err := strconv.Atoi(p.canonical(field))
	if err != nil {
		return nil
	}
	return &n
}

var excelEpoch = time.Date(1899, 12, 30, 0, 0, 0, 0, time.UTC)

var dateLayouts = map[string]string{"dd.mm.yyyy": "02.01.2006", "yyyy-mm-dd": "2006-01-02", "yyyymmdd": "20060102"}

func (p *parser) date(field string) *models.Date {
	v, ok := p.raw[field]
	if !ok || strings.Trim(v, "0") == "" {
		return nil
	}
	if p.m.Format == "XLSX" && digits.MatchString(v) && len(v) == 5 {
		n, _ := strconv.Atoi(v)
		t := excelEpoch.AddDate(0, 0, n)
		d := models.NewDate(t.Year(), t.Month(), t.Day())
		return &d
	}
	layouts := []string{dateLayouts[p.m.DateFormat], "02.01.2006", "2006-01-02", "20060102", "2.1.2006"}
	for _, l := range layouts {
		if t, err := time.Parse(l, v); err == nil {
			d := models.NewDate(t.Year(), t.Month(), t.Day())
			return &d
		}
	}
	p.issue(field, "INVALID_DATE")
	return nil
}

// Parse types a row's raw values and reports what is wrong with them.
func Parse(m Mapping, raw map[string]string) (Parsed, []Issue) {
	p := &parser{m: m, raw: raw}
	var out Parsed
	if op, ok := raw["op"]; ok {
		n, err := strconv.Atoi(op)
		if err != nil || n < 0 || n > 2 {
			p.issue("op", "UNKNOWN_OP")
		} else {
			out.Op = &n
		}
	}
	out.StockNo = raw["code"]
	if out.StockNo == "" {
		p.issue("code", "REQUIRED")
	} else if utf8.RuneCountInString(out.StockNo) > 15 {
		p.issue("code", "TOO_LONG")
	}
	out.Description = p.text("description", 60)
	out.Brand = p.text("brand", 30)
	out.Type = p.text("type", 30)
	out.ManufacturerCode = p.text("manufacturerCode", 40)
	out.OEM = p.list("oem")
	out.AltCodes = p.list("altCodes")
	if _, ok := raw["price"]; ok {
		switch d, ok := decimal(p.canonical("price")); {
		case !ok:
			p.issue("price", "INVALID_NUMBER")
		case d < 0:
			p.issue("price", "NEGATIVE_PRICE")
		default:
			out.Price = &d
		}
	} else if out.Op == nil || *out.Op != 0 {
		p.issue("price", "REQUIRED")
	}
	out.ValidFrom = p.date("validFrom")
	if _, ok := raw["packQty"]; ok && !p.zeroIsEmpty("packQty") {
		if out.PackQty = p.integer("packQty"); out.PackQty == nil || *out.PackQty < 1 {
			out.PackQty = nil
			p.issue("packQty", "INVALID_NUMBER")
		}
	}
	if _, ok := raw["vatPct"]; ok {
		d, ok := decimal(p.canonical("vatPct"))
		if !ok || d < 0 || d > 100*10000 {
			p.issue("vatPct", "INVALID_PERCENT")
		} else {
			s := d.String()
			out.VatPct = &s
		}
	}
	if _, ok := raw["catalogCode"]; ok {
		if out.CatalogCode = p.integer("catalogCode"); out.CatalogCode == nil && !p.zeroIsEmpty("catalogCode") {
			p.issue("catalogCode", "INVALID_NUMBER")
		}
	}
	if code, ok := raw["equivalentCode"]; ok {
		if cat := p.integer("equivalentCatalogCode"); cat != nil {
			out.Equivalent = &Equivalent{CatalogCode: *cat, StockNo: code}
		}
	}
	return out, p.issues
}
