package export

import (
	"archive/zip"
	"bytes"
	"encoding/xml"
	"io"
	"strings"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/models"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func sample() Table {
	d, _ := models.ParseDate("2026-03-31")
	return Table{
		Columns: []Column{{Title: "Hesap", Kind: Text}, {Title: "Tarih", Kind: DateCol}, {Title: "Adet", Kind: Int}, {Title: "Bakiye", Kind: MoneyCol}},
		Rows: [][]any{
			{"120.Ş<&>Ğ", d, 3, models.Money(-123456)},
			{nil, nil, nil, models.Money(5)},
		},
		Totals: []any{"Toplam", nil, nil, models.Money(-123451)},
	}
}

func readZip(t *testing.T, b []byte) map[string]string {
	t.Helper()
	zr, err := zip.NewReader(bytes.NewReader(b), int64(len(b)))
	require.NoError(t, err)
	out := map[string]string{}
	for _, f := range zr.File {
		rc, err := f.Open()
		require.NoError(t, err)
		body, err := io.ReadAll(rc)
		require.NoError(t, err)
		require.NoError(t, rc.Close())
		out[f.Name] = string(body)
	}
	return out
}

func TestXLSX_IsAWorkbookWithTypedCells(t *testing.T) {
	b, err := XLSX("Ekstre", sample())
	require.NoError(t, err)
	files := readZip(t, b)
	for _, name := range []string{"[Content_Types].xml", "_rels/.rels", "xl/workbook.xml", "xl/_rels/workbook.xml.rels", "xl/styles.xml", "xl/worksheets/sheet1.xml"} {
		require.Contains(t, files, name)
		var v any
		assert.NoError(t, xml.Unmarshal([]byte(files[name]), &v), name)
	}
	sheet := files["xl/worksheets/sheet1.xml"]
	assert.Contains(t, sheet, "120.Ş&lt;&amp;&gt;Ğ")
	assert.Contains(t, sheet, "<v>-1234.56</v>")
	assert.Contains(t, sheet, "<v>3</v>")
	assert.Contains(t, sheet, "Toplam")
	assert.Contains(t, sheet, "<v>-1234.51</v>")
	assert.Equal(t, 1, strings.Count(files["xl/workbook.xml"], "<sheet "))
	assert.Contains(t, files["xl/workbook.xml"], `name="Ekstre"`)
	// Dates are real Excel dates (serial days since 1899-12-30).
	assert.Contains(t, sheet, "<v>46112</v>")
}

func TestXLSX_SheetNameIsSanitised(t *testing.T) {
	b, err := XLSX("a/b:c*d?e[f]ghijklmnopqrstuvwxyz0123456789", sample())
	require.NoError(t, err)
	wb := readZip(t, b)["xl/workbook.xml"]
	assert.Contains(t, wb, `name="a b c d e f ghijklmnopqrstuvwxy"`)
}

func TestPDF_RendersTurkishTextAndTables(t *testing.T) {
	doc := Document{
		Title:  "Hesap ekstresi — Şişli Güneş İthalat",
		Meta:   [][2]string{{"Dönem", "2026-03-01 – 2026-03-31"}, {"Depo", "Tümü"}},
		Tables: []Table{sample(), {Heading: "Depo toplamları", Columns: []Column{{Title: "Depo", Kind: Text}, {Title: "Bakiye", Kind: MoneyCol}}, Rows: [][]any{{"1", models.Money(100)}}}},
		Footer: "fikopar",
	}
	b, err := PDF(doc)
	require.NoError(t, err)
	assert.True(t, bytes.HasPrefix(b, []byte("%PDF-")))
	assert.Greater(t, len(b), 1000)

	many := sample()
	for i := 0; i < 200; i++ {
		many.Rows = append(many.Rows, []any{"satır", nil, i, models.Money(i)})
	}
	b2, err := PDF(Document{Title: "Uzun", Tables: []Table{many}})
	require.NoError(t, err)
	assert.Greater(t, bytes.Count(b2, []byte("/Type /Page\n")), 1, "long tables break across pages")
}

func TestFormatMoney(t *testing.T) {
	assert.Equal(t, "-1,234.56", formatMoney(models.Money(-123456)))
	assert.Equal(t, "0.05", formatMoney(models.Money(5)))
	assert.Equal(t, "1,000,000.00", formatMoney(models.Money(100000000)))
}

func TestXLSX_DecimalCells(t *testing.T) {
	q := models.Dec4(-25000)
	var none *models.Dec4
	b, err := XLSX("Stock", Table{Columns: []Column{{Title: "Qty", Kind: Number}, {Title: "Min", Kind: Number}},
		Rows: [][]any{{q, &q}, {models.Dec4(10000), none}}})
	require.NoError(t, err)
	sheet := readZip(t, b)["xl/worksheets/sheet1.xml"]
	assert.Contains(t, sheet, `<c r="A2"><v>-2.5</v></c>`)
	assert.Contains(t, sheet, `<c r="B2"><v>-2.5</v></c>`)
	assert.NotContains(t, sheet, `r="B3"`)
	assert.Equal(t, "-2.5", cellText(&q))
	assert.Equal(t, "", cellText(none))
}
