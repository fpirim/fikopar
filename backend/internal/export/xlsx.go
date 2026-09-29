// Package export renders report tables as XLSX (a minimal SpreadsheetML writer) and PDF.
package export

import (
	"archive/zip"
	"bytes"
	"encoding/xml"
	"fmt"
	"strconv"
	"strings"
	"time"

	"github.com/fpirim/fikopar/backend/internal/models"
)

type Kind int

const (
	Text Kind = iota
	MoneyCol
	DateCol
	Int
	Number
)

type Column struct {
	Title string
	Kind  Kind
}

// Table cells are string, models.Money, models.Date, *models.Date, models.Dec4, *models.Dec4, int, uint or nil.
type Table struct {
	Heading string
	Columns []Column
	Rows    [][]any
	Totals  []any
}

const (
	styleHeader = 1
	styleMoney  = 2
	styleDate   = 3
	styleTotal  = 4
)

func XLSX(sheet string, t Table) ([]byte, error) {
	var buf bytes.Buffer
	zw := zip.NewWriter(&buf)
	files := []struct{ name, body string }{
		{"[Content_Types].xml", contentTypes},
		{"_rels/.rels", rootRels},
		{"xl/workbook.xml", fmt.Sprintf(workbook, escape(sheetName(sheet)))},
		{"xl/_rels/workbook.xml.rels", workbookRels},
		{"xl/styles.xml", styles},
		{"xl/worksheets/sheet1.xml", worksheet(t)},
	}
	for _, f := range files {
		w, err := zw.Create(f.name)
		if err != nil {
			return nil, err
		}
		if _, err := w.Write([]byte(xml.Header + f.body)); err != nil {
			return nil, err
		}
	}
	if err := zw.Close(); err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}

func sheetName(s string) string {
	s = strings.Map(func(r rune) rune {
		if strings.ContainsRune(`/\?*:[]`, r) {
			return ' '
		}
		return r
	}, s)
	if r := []rune(s); len(r) > 31 {
		s = string(r[:31])
	}
	if strings.TrimSpace(s) == "" {
		return "Sheet1"
	}
	return s
}

func worksheet(t Table) string {
	var b strings.Builder
	b.WriteString(`<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetViews><sheetView workbookViewId="0"><pane ySplit="1" topLeftCell="A2" activePane="bottomLeft" state="frozen"/></sheetView></sheetViews><cols>`)
	for i, c := range t.Columns {
		w := 14
		if c.Kind == Text {
			w = 28
		}
		fmt.Fprintf(&b, `<col min="%d" max="%d" width="%d" customWidth="1"/>`, i+1, i+1, w)
	}
	b.WriteString(`</cols><sheetData>`)
	row := 1
	b.WriteString(`<row r="1">`)
	for i, c := range t.Columns {
		textCell(&b, i, row, c.Title, styleHeader)
	}
	b.WriteString(`</row>`)
	for _, r := range t.Rows {
		row++
		writeRow(&b, row, r, false)
	}
	if t.Totals != nil {
		writeRow(&b, row+1, t.Totals, true)
	}
	b.WriteString(`</sheetData></worksheet>`)
	return b.String()
}

func writeRow(b *strings.Builder, row int, cells []any, total bool) {
	fmt.Fprintf(b, `<row r="%d">`, row)
	for i, v := range cells {
		ref := cellRef(i, row)
		switch x := v.(type) {
		case nil:
		case string:
			style := 0
			if total {
				style = styleTotal
			}
			textCell(b, i, row, x, style)
		case models.Money:
			fmt.Fprintf(b, `<c r="%s" s="%d"><v>%s</v></c>`, ref, styleMoney, x.String())
		case models.Date:
			fmt.Fprintf(b, `<c r="%s" s="%d"><v>%d</v></c>`, ref, styleDate, excelSerial(x))
		case *models.Date:
			if x != nil {
				fmt.Fprintf(b, `<c r="%s" s="%d"><v>%d</v></c>`, ref, styleDate, excelSerial(*x))
			}
		case models.Dec4:
			fmt.Fprintf(b, `<c r="%s"><v>%s</v></c>`, ref, x.String())
		case *models.Dec4:
			if x != nil {
				fmt.Fprintf(b, `<c r="%s"><v>%s</v></c>`, ref, x.String())
			}
		case int:
			fmt.Fprintf(b, `<c r="%s"><v>%d</v></c>`, ref, x)
		case uint:
			fmt.Fprintf(b, `<c r="%s"><v>%d</v></c>`, ref, x)
		default:
			textCell(b, i, row, fmt.Sprint(x), 0)
		}
	}
	b.WriteString(`</row>`)
}

func textCell(b *strings.Builder, col, row int, s string, style int) {
	fmt.Fprintf(b, `<c r="%s" t="inlineStr"`, cellRef(col, row))
	if style != 0 {
		fmt.Fprintf(b, ` s="%d"`, style)
	}
	fmt.Fprintf(b, `><is><t xml:space="preserve">%s</t></is></c>`, escape(s))
}

func cellRef(col, row int) string {
	name := ""
	for col >= 0 {
		name = string(rune('A'+col%26)) + name
		col = col/26 - 1
	}
	return name + strconv.Itoa(row)
}

var excelEpoch = time.Date(1899, 12, 30, 0, 0, 0, 0, time.UTC)

func excelSerial(d models.Date) int {
	return int(d.Sub(excelEpoch).Hours() / 24)
}

func escape(s string) string {
	var b bytes.Buffer
	_ = xml.EscapeText(&b, []byte(s))
	return b.String()
}

const contentTypes = `<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/></Types>`

const rootRels = `<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>`

const workbook = `<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="%s" sheetId="1" r:id="rId1"/></sheets></workbook>`

const workbookRels = `<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/></Relationships>`

// Styles: 0 default, 1 bold header, 2 #,##0.00, 3 yyyy-mm-dd, 4 bold.
const styles = `<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><numFmts count="1"><numFmt numFmtId="164" formatCode="yyyy-mm-dd"/></numFmts><fonts count="2"><font><sz val="11"/><name val="Calibri"/></font><font><b/><sz val="11"/><name val="Calibri"/></font></fonts><fills count="2"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills><borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs><cellXfs count="5"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/><xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/><xf numFmtId="4" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/><xf numFmtId="164" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/><xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/></cellXfs></styleSheet>`
