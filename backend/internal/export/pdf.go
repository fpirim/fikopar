package export

import (
	"bytes"
	"fmt"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/models"

	"github.com/go-pdf/fpdf"
	"golang.org/x/image/font/gofont/gobold"
	"golang.org/x/image/font/gofont/goregular"
)

type Document struct {
	Title     string
	Meta      [][2]string
	Tables    []Table
	Footer    string
	Landscape bool
}

const (
	margin = 12.0
	rowH   = 5.5
	font   = "go"
)

// PDF lays out a title, key/value metadata and tables; table headers repeat on each page.
func PDF(doc Document) ([]byte, error) {
	orient := "P"
	if doc.Landscape {
		orient = "L"
	}
	p := fpdf.New(orient, "mm", "A4", "")
	p.AddUTF8FontFromBytes(font, "", goregular.TTF)
	p.AddUTF8FontFromBytes(font, "B", gobold.TTF)
	p.SetMargins(margin, margin, margin)
	p.SetAutoPageBreak(false, margin)
	p.AliasNbPages("")
	p.SetFooterFunc(func() {
		p.SetY(-9)
		p.SetFont(font, "", 7)
		p.SetTextColor(110, 110, 110)
		p.CellFormat(0, 4, strings.TrimSpace(doc.Footer+"  "+fmt.Sprintf("%d/{nb}", p.PageNo())), "", 0, "R", false, 0, "")
		p.SetTextColor(0, 0, 0)
	})
	p.AddPage()

	p.SetFont(font, "B", 14)
	p.MultiCell(0, 7, doc.Title, "", "L", false)
	p.Ln(1)
	p.SetFont(font, "", 9)
	for _, kv := range doc.Meta {
		p.SetFont(font, "B", 9)
		p.CellFormat(35, 5, kv[0], "", 0, "L", false, 0, "")
		p.SetFont(font, "", 9)
		p.CellFormat(0, 5, kv[1], "", 1, "L", false, 0, "")
	}
	for _, t := range doc.Tables {
		p.Ln(4)
		table(p, t)
	}
	var buf bytes.Buffer
	if err := p.Output(&buf); err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}

func table(p *fpdf.Fpdf, t Table) {
	pageW, pageH := p.GetPageSize()
	widths := columnWidths(t.Columns, pageW-2*margin)
	if t.Heading != "" {
		p.SetFont(font, "B", 10)
		p.CellFormat(0, 6, t.Heading, "", 1, "L", false, 0, "")
	}
	header := func() {
		p.SetFont(font, "B", 8)
		p.SetFillColor(236, 236, 232)
		for i, c := range t.Columns {
			p.CellFormat(widths[i], rowH, fit(p, c.Title, widths[i]), "B", 0, align(c.Kind), true, 0, "")
		}
		p.Ln(-1)
		p.SetFont(font, "", 8)
	}
	header()
	row := func(cells []any, bold bool) {
		if p.GetY()+rowH > pageH-margin-6 {
			p.AddPage()
			header()
		}
		style := ""
		border := ""
		if bold {
			style = "B"
			border = "T"
		}
		p.SetFont(font, style, 8)
		for i, c := range t.Columns {
			var v any
			if i < len(cells) {
				v = cells[i]
			}
			p.CellFormat(widths[i], rowH, fit(p, cellText(v), widths[i]), border, 0, align(c.Kind), false, 0, "")
		}
		p.Ln(-1)
	}
	for _, r := range t.Rows {
		row(r, false)
	}
	if t.Totals != nil {
		row(t.Totals, true)
	}
}

func columnWidths(cols []Column, total float64) []float64 {
	weights := make([]float64, len(cols))
	sum := 0.0
	for i, c := range cols {
		weights[i] = 1
		if c.Kind == Text {
			weights[i] = 2.2
		}
		sum += weights[i]
	}
	out := make([]float64, len(cols))
	for i := range cols {
		out[i] = total * weights[i] / sum
	}
	return out
}

func align(k Kind) string {
	if k == MoneyCol || k == Int || k == Number {
		return "R"
	}
	return "L"
}

func fit(p *fpdf.Fpdf, s string, w float64) string {
	if p.GetStringWidth(s) <= w-1.5 {
		return s
	}
	r := []rune(s)
	for len(r) > 0 && p.GetStringWidth(string(r)+"…") > w-1.5 {
		r = r[:len(r)-1]
	}
	return string(r) + "…"
}

func cellText(v any) string {
	switch x := v.(type) {
	case nil:
		return ""
	case string:
		return x
	case models.Money:
		return formatMoney(x)
	case models.Date:
		return x.String()
	case *models.Date:
		if x == nil {
			return ""
		}
		return x.String()
	case *models.Dec4:
		if x == nil {
			return ""
		}
		return x.String()
	}
	return fmt.Sprint(v)
}

// formatMoney groups thousands: -1,234.56.
func formatMoney(m models.Money) string {
	s := m.Abs().String()
	whole, frac, _ := strings.Cut(s, ".")
	var b strings.Builder
	if m < 0 {
		b.WriteByte('-')
	}
	for i, c := range whole {
		if i > 0 && (len(whole)-i)%3 == 0 {
			b.WriteByte(',')
		}
		b.WriteRune(c)
	}
	return b.String() + "." + frac
}
