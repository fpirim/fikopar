package pricefile

import (
	"archive/zip"
	"bytes"
	"errors"
	"fmt"
	"strings"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/models"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func toCP857(t *testing.T, s string) []byte {
	t.Helper()
	var b []byte
	for _, r := range s {
		if r < 0x80 {
			b = append(b, byte(r))
			continue
		}
		found := false
		for i, c := range cp857 {
			if c == r {
				b = append(b, byte(0x80+i))
				found = true
				break
			}
		}
		require.True(t, found, "no CP857 byte for %q", r)
	}
	return b
}

func read(t *testing.T, name string, content []byte, m Mapping) (Mapping, *File) {
	t.Helper()
	m, err := Resolve(m, name)
	require.NoError(t, err)
	f, err := Read(content, m)
	require.NoError(t, err)
	return m, f
}

func TestResolve_FormatFromExtensionAndPreset(t *testing.T) {
	m, err := Resolve(Mapping{}, "list.CSV")
	require.NoError(t, err)
	assert.Equal(t, "CSV", m.Format)
	assert.Equal(t, "UTF-8", m.Encoding)
	assert.Equal(t, ",", m.DecimalSeparator)
	assert.Equal(t, "dd.mm.yyyy", m.DateFormat)
	assert.Equal(t, 1, *m.HeaderRow)
	assert.Equal(t, "|", m.ListSeparator)

	m, err = Resolve(Mapping{}, "list.xlsx")
	require.NoError(t, err)
	assert.Equal(t, "XLSX", m.Format)

	lisakt := "LISAKT"
	m, err = Resolve(Mapping{Preset: &lisakt, Columns: map[string]Column{"price": {Start: 233, Length: 14, Scale: 2}}}, "LISAKT.TXT")
	require.NoError(t, err)
	assert.Equal(t, "FIXED_WIDTH", m.Format)
	assert.Equal(t, "CP857", m.Encoding)
	assert.Equal(t, 2, m.Columns["price"].Scale, "fields given override the preset")
	assert.Equal(t, 6, m.Columns["code"].Start)

	_, err = Resolve(Mapping{Format: "FIXED_WIDTH"}, "a.txt")
	assert.ErrorIs(t, err, ErrFormat)
	bad := "NOPE"
	_, err = Resolve(Mapping{Preset: &bad}, "a.txt")
	assert.ErrorIs(t, err, ErrFormat)
	_, err = Resolve(Mapping{Encoding: "LATIN9"}, "a.csv")
	assert.ErrorIs(t, err, ErrFormat)
}

func TestCSV_DelimiterDetectionQuotesAndAutoMap(t *testing.T) {
	for name, delim := range map[string]string{"semicolon": ";", "comma": ",", "tab": "\t"} {
		t.Run(name, func(t *testing.T) {
			body := strings.Join([]string{
				"Stok Kodu" + delim + "Açıklama" + delim + "Fiyat" + delim + "Marka",
				"A-1" + delim + `"Filtre` + delim + ` büyük"` + delim + `"1.234,5"` + delim + "Bosch",
				"",
				"B-2" + delim + "Kayış" + delim + "10" + delim + "",
			}, "\r\n")
			m, f := read(t, "x.csv", []byte("\ufeff"+body), Mapping{})
			assert.Equal(t, []string{"Stok Kodu", "Açıklama", "Fiyat", "Marka"}, f.Headers)
			require.Len(t, f.Rows, 2)
			assert.Equal(t, 2, f.Rows[0].No)
			assert.Equal(t, 4, f.Rows[1].No)
			m = AutoMap(m, f.Headers)
			assert.Empty(t, Unmapped(m, f.Headers))
			raw := Extract(m, f.Headers, f.Rows[0])
			assert.Equal(t, "Filtre"+delim+" büyük", raw["description"])
			p, issues := Parse(m, raw)
			assert.Empty(t, issues)
			assert.Equal(t, "A-1", p.StockNo)
			assert.Equal(t, models.Dec4(12345000), *p.Price)
			assert.Equal(t, "Bosch", *p.Brand)
			p, _ = Parse(m, Extract(m, f.Headers, f.Rows[1]))
			assert.Nil(t, p.Brand)
		})
	}
}

func TestCSV_ExplicitMappingByIndexNoHeaderAndDates(t *testing.T) {
	zero := 0
	m, f := read(t, "x.csv", []byte("A;9.5;01.03.2026;X|Y;4\nB;7;2026-04-01;;\n"), Mapping{
		HeaderRow: &zero, DecimalSeparator: ".",
		Columns: map[string]Column{"code": {Index: 1}, "price": {Index: 2}, "validFrom": {Index: 3}, "altCodes": {Index: 4}, "packQty": {Index: 5}},
	})
	assert.Nil(t, f.Headers)
	require.Len(t, f.Rows, 2)
	assert.Equal(t, 1, f.Rows[0].No)
	p, issues := Parse(m, Extract(m, f.Headers, f.Rows[0]))
	assert.Empty(t, issues)
	assert.Equal(t, models.Dec4(95000), *p.Price)
	assert.Equal(t, "2026-03-01", p.ValidFrom.String())
	assert.Equal(t, []string{"X", "Y"}, p.AltCodes)
	assert.Equal(t, 4, *p.PackQty)
	p, issues = Parse(m, Extract(m, f.Headers, f.Rows[1]))
	assert.Empty(t, issues, "other date formats are accepted too")
	assert.Equal(t, "2026-04-01", p.ValidFrom.String())
}

func TestCSV_EncodingsAndUnmapped(t *testing.T) {
	m, f := read(t, "x.csv", toCP857(t, "Kod;Ürün Adı;Tutar\nŞ-1;Işık;5\n"), Mapping{Encoding: "CP857"})
	assert.Equal(t, "Ürün Adı", f.Headers[1])
	m = AutoMap(m, f.Headers)
	assert.Equal(t, []string{"price"}, Unmapped(m, f.Headers))
	assert.Equal(t, [][]string{{"Ş-1", "Işık", "5"}}, f.Sample(5))

	win := []byte{'K', 'o', 'd', ';', 'F', 'i', 'y', 'a', 't', '\n', 0xDD, 0xFE, ';', '1', '\n'}
	m, f = read(t, "x.csv", win, Mapping{Encoding: "WINDOWS-1254"})
	m = AutoMap(m, f.Headers)
	assert.Empty(t, Unmapped(m, f.Headers))
	assert.Equal(t, "İş", Extract(m, f.Headers, f.Rows[0])["code"])

	m.Columns["price"] = Column{Header: "missing"}
	assert.Equal(t, []string{"price"}, Unmapped(m, f.Headers))
}

func TestParse_Issues(t *testing.T) {
	m, err := Resolve(Mapping{}, "x.csv")
	require.NoError(t, err)
	codes := func(raw map[string]string) []string {
		_, issues := Parse(m, raw)
		var out []string
		for _, i := range issues {
			out = append(out, i.Field+":"+i.Code)
		}
		return out
	}
	assert.Equal(t, []string{"code:REQUIRED", "price:REQUIRED"}, codes(map[string]string{}))
	assert.Equal(t, []string{"price:INVALID_NUMBER"}, codes(map[string]string{"code": "A", "price": "abc"}))
	assert.Equal(t, []string{"price:NEGATIVE_PRICE"}, codes(map[string]string{"code": "A", "price": "-1"}))
	assert.Equal(t, []string{"validFrom:INVALID_DATE"}, codes(map[string]string{"code": "A", "price": "1", "validFrom": "31.02.2026"}))
	assert.Equal(t, []string{"vatPct:INVALID_PERCENT"}, codes(map[string]string{"code": "A", "price": "1", "vatPct": "120"}))
	assert.Equal(t, []string{"packQty:INVALID_NUMBER"}, codes(map[string]string{"code": "A", "price": "1", "packQty": "0"}))
	assert.Equal(t, []string{"code:TOO_LONG", "description:TOO_LONG"},
		codes(map[string]string{"code": strings.Repeat("x", 16), "price": "1", "description": strings.Repeat("y", 61)}))
	assert.Equal(t, []string{"op:UNKNOWN_OP"}, codes(map[string]string{"code": "A", "price": "1", "op": "7"}))
	assert.Empty(t, codes(map[string]string{"code": "A", "op": "0"}), "a delete needs no price")
	p, _ := Parse(m, map[string]string{"code": "A", "price": "1,23456", "vatPct": "20,0"})
	assert.Equal(t, models.Dec4(12346), *p.Price)
	assert.Equal(t, "20", *p.VatPct)
}

func lisaktRecord(t *testing.T, op, katno, stno, prcno, oem, typ, name, brand, price, deg, paket, bkatno, bstno string) []byte {
	t.Helper()
	pad := func(s string, n int) string { return s + strings.Repeat(" ", n-len([]rune(s))) }
	rec := op + katno + pad(stno, 15) + pad(prcno, 30) + pad(oem, 30) + pad(typ, 30) + pad(name, 60) + pad(brand, 30) +
		strings.Repeat("0", 32) + price + pad("", 10) + deg + paket + strings.Repeat("0", 15) + bkatno + pad(bstno, 15) + strings.Repeat("0", 16)
	require.Equal(t, 319, len([]rune(rec)))
	return toCP857(t, rec)
}

func TestLISAKT_FixedWidthCP857(t *testing.T) {
	var buf bytes.Buffer
	buf.Write(lisaktRecord(t, "1", "0010", "ŞAFT-1", "PRC 1", "OEM-1", "Mil", "Şaft göbeği ığü", "Özdemir", "00000000012345", "20260301", "00005", "0000", ""))
	buf.WriteString("\r\n")
	buf.Write(lisaktRecord(t, "0", "0011", "X", "", "", "", "", "", "00000000000000", "00000000", "00000", "0010", "ŞAFT-1"))
	buf.WriteString("\n\n")
	lisakt := "LISAKT"
	m, f := read(t, "LISAKT.DAT", buf.Bytes(), Mapping{Preset: &lisakt})
	require.Len(t, f.Rows, 2)
	assert.Equal(t, 1, f.Rows[0].No)
	assert.Empty(t, Unmapped(m, nil))
	p, issues := Parse(m, Extract(m, nil, f.Rows[0]))
	assert.Empty(t, issues)
	assert.Equal(t, "ŞAFT-1", p.StockNo)
	assert.Equal(t, "Şaft göbeği ığü", *p.Description)
	assert.Equal(t, "Özdemir", *p.Brand)
	assert.Equal(t, "PRC 1", *p.ManufacturerCode)
	assert.Equal(t, []string{"OEM-1"}, p.OEM)
	assert.Equal(t, models.Dec4(12345), *p.Price, "scale 4: 1.2345")
	assert.Equal(t, "2026-03-01", p.ValidFrom.String())
	assert.Equal(t, 5, *p.PackQty)
	assert.Equal(t, 10, *p.CatalogCode)
	assert.Equal(t, 1, *p.Op)
	assert.Nil(t, p.Equivalent)

	p, issues = Parse(m, Extract(m, nil, f.Rows[1]))
	assert.Empty(t, issues)
	assert.Equal(t, 0, *p.Op)
	assert.Nil(t, p.ValidFrom, "00000000 falls back to the job date")
	assert.Nil(t, p.PackQty)
	require.NotNil(t, p.Equivalent)
	assert.Equal(t, 10, p.Equivalent.CatalogCode)
	assert.Equal(t, "ŞAFT-1", p.Equivalent.StockNo)

	_, issues = Parse(m, map[string]string{"code": "A", "price": "12x", "op": "1"})
	require.Len(t, issues, 1)
	assert.Equal(t, "INVALID_NUMBER", issues[0].Code)
}

type xlsxCell struct {
	ref, typ, val string
}

func buildXLSX(t *testing.T, sheetName string, shared []string, rows [][]xlsxCell) []byte {
	t.Helper()
	var buf bytes.Buffer
	z := zip.NewWriter(&buf)
	put := func(name, body string) {
		w, err := z.Create(name)
		require.NoError(t, err)
		_, err = w.Write([]byte(body))
		require.NoError(t, err)
	}
	put("xl/workbook.xml", `<?xml version="1.0"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
		xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets>
		<sheet name="Other" sheetId="1" r:id="rId9"/><sheet name="`+sheetName+`" sheetId="2" r:id="rId1"/></sheets></workbook>`)
	put("xl/_rels/workbook.xml.rels", `<?xml version="1.0"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
		<Relationship Id="rId1" Type="x" Target="worksheets/sheet1.xml"/><Relationship Id="rId9" Type="x" Target="/xl/worksheets/sheet9.xml"/></Relationships>`)
	var ss strings.Builder
	for _, s := range shared {
		ss.WriteString(`<si><r><t>` + s[:1] + `</t></r><r><t xml:space="preserve">` + s[1:] + `</t></r></si>`)
	}
	put("xl/sharedStrings.xml", `<?xml version="1.0"?><sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">`+ss.String()+`</sst>`)
	var sh strings.Builder
	for _, r := range rows {
		fmt.Fprintf(&sh, `<row r="%s">`, strings.TrimLeft(r[0].ref, "ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
		for _, c := range r {
			switch c.typ {
			case "inlineStr":
				fmt.Fprintf(&sh, `<c r="%s" t="inlineStr"><is><t>%s</t></is></c>`, c.ref, c.val)
			case "":
				fmt.Fprintf(&sh, `<c r="%s"><v>%s</v></c>`, c.ref, c.val)
			default:
				fmt.Fprintf(&sh, `<c r="%s" t="%s"><v>%s</v></c>`, c.ref, c.typ, c.val)
			}
		}
		sh.WriteString(`</row>`)
	}
	put("xl/worksheets/sheet1.xml", `<?xml version="1.0"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>`+sh.String()+`</sheetData></worksheet>`)
	put("xl/worksheets/sheet9.xml", `<?xml version="1.0"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData><row r="1"><c r="A1" t="inlineStr"><is><t>wrong</t></is></c></row></sheetData></worksheet>`)
	require.NoError(t, z.Close())
	return buf.Bytes()
}

func TestXLSX_SharedInlineNumbersAndDates(t *testing.T) {
	data := buildXLSX(t, "Liste", []string{"Stok No", "Fiyat", "A-1"}, [][]xlsxCell{
		{{"A1", "s", "0"}, {"B1", "s", "1"}, {"C1", "inlineStr", "Tarih"}, {"D1", "inlineStr", "Marka"}},
		{{"A2", "s", "2"}, {"B2", "", "12.3456000001"}, {"C2", "", "46082"}, {"D2", "str", "Bosch"}},
		{{"A4", "", "1001"}, {"B4", "", "7"}, {"D4", "b", "1"}},
	})
	m, f := read(t, "list.xlsx", data, Mapping{Sheet: "Liste"})
	assert.Equal(t, []string{"Stok No", "Fiyat", "Tarih", "Marka"}, f.Headers)
	require.Len(t, f.Rows, 2)
	assert.Equal(t, 4, f.Rows[1].No)
	m = AutoMap(m, f.Headers)
	p, issues := Parse(m, Extract(m, f.Headers, f.Rows[0]))
	assert.Empty(t, issues)
	assert.Equal(t, "A-1", p.StockNo)
	assert.Equal(t, models.Dec4(123456), *p.Price)
	assert.Equal(t, "2026-03-01", p.ValidFrom.String())
	assert.Equal(t, "Bosch", *p.Brand)
	p, _ = Parse(m, Extract(m, f.Headers, f.Rows[1]))
	assert.Equal(t, "1001", p.StockNo)

	m, f = read(t, "list.xlsx", data, Mapping{Sheet: "Liste", Columns: map[string]Column{"code": {Letter: "A"}, "price": {Letter: "B"}}})
	assert.Empty(t, Unmapped(m, f.Headers))
	assert.Equal(t, "A-1", Extract(m, f.Headers, f.Rows[0])["code"])

	m, err := Resolve(Mapping{}, "list.xlsx")
	require.NoError(t, err)
	f, err = Read(data, m)
	require.NoError(t, err)
	assert.Equal(t, []string{"wrong"}, f.Headers, "first sheet when none is named")

	_, err = Read(data, Mapping{Format: "XLSX", Sheet: "Nope", HeaderRow: new(int)})
	assert.ErrorIs(t, err, ErrFormat)
	_, err = Read([]byte("not a zip"), m)
	assert.ErrorIs(t, err, ErrFormat)
}

func TestRead_TooManyRows(t *testing.T) {
	m, err := Resolve(Mapping{}, "x.csv")
	require.NoError(t, err)
	body := "code;price\n" + strings.Repeat("a;1\n", MaxRows+1)
	_, err = Read([]byte(body), m)
	assert.True(t, errors.Is(err, ErrTooManyRows))
}

func TestPresetsAndAliases(t *testing.T) {
	p := Presets()
	require.Len(t, p, 1)
	assert.Equal(t, "LISAKT", p[0].Code)
	assert.Equal(t, 233, p[0].Mapping.Columns["price"].Start)
	assert.Contains(t, Aliases()["price"], "fiyat")
}
