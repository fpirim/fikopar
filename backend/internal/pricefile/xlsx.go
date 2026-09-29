package pricefile

import (
	"archive/zip"
	"bytes"
	"encoding/xml"
	"io"
	"path"
	"strconv"
	"strings"
)

type xlsxText struct {
	T    string `xml:"t"`
	Runs []struct {
		T string `xml:"t"`
	} `xml:"r"`
}

func (x xlsxText) String() string {
	if len(x.Runs) == 0 {
		return x.T
	}
	var b strings.Builder
	for _, r := range x.Runs {
		b.WriteString(r.T)
	}
	return b.String()
}

type xlsxSheet struct {
	Rows []struct {
		R     int `xml:"r,attr"`
		Cells []struct {
			R  string   `xml:"r,attr"`
			T  string   `xml:"t,attr"`
			V  string   `xml:"v"`
			Is xlsxText `xml:"is"`
		} `xml:"c"`
	} `xml:"sheetData>row"`
}

const maxXLSXPart = 200 << 20

func zipFile(z *zip.Reader, name string) ([]byte, error) {
	name = strings.TrimPrefix(name, "/")
	for _, f := range z.File {
		if f.Name == name {
			rc, err := f.Open()
			if err != nil {
				return nil, formatErr("%s: %v", name, err)
			}
			defer func() { _ = rc.Close() }()
			b, err := io.ReadAll(io.LimitReader(rc, maxXLSXPart))
			if err != nil {
				return nil, formatErr("%s: %v", name, err)
			}
			return b, nil
		}
	}
	return nil, nil
}

func sheetPath(z *zip.Reader, sheet string) (string, error) {
	wb, err := zipFile(z, "xl/workbook.xml")
	if err != nil || wb == nil {
		return "", formatErr("no workbook")
	}
	var w struct {
		Sheets []struct {
			Name string `xml:"name,attr"`
			ID   string `xml:"http://schemas.openxmlformats.org/officeDocument/2006/relationships id,attr"`
		} `xml:"sheets>sheet"`
	}
	if err := xml.Unmarshal(wb, &w); err != nil || len(w.Sheets) == 0 {
		return "", formatErr("no sheets")
	}
	id := w.Sheets[0].ID
	if sheet != "" {
		id = ""
		for _, s := range w.Sheets {
			if s.Name == sheet {
				id = s.ID
			}
		}
		if id == "" {
			return "", formatErr("sheet %q not found", sheet)
		}
	}
	rels, err := zipFile(z, "xl/_rels/workbook.xml.rels")
	if err != nil {
		return "", err
	}
	var r struct {
		Rels []struct {
			ID     string `xml:"Id,attr"`
			Target string `xml:"Target,attr"`
		} `xml:"Relationship"`
	}
	if err := xml.Unmarshal(rels, &r); err != nil {
		return "", formatErr("workbook relations")
	}
	for _, rel := range r.Rels {
		if rel.ID == id {
			if strings.HasPrefix(rel.Target, "/") {
				return rel.Target, nil
			}
			return path.Join("xl", rel.Target), nil
		}
	}
	return "", formatErr("sheet target not found")
}

func cellColumn(ref string) int {
	n := 0
	for _, r := range ref {
		if r < 'A' || r > 'Z' {
			break
		}
		n = n*26 + int(r-'A'+1)
	}
	return n - 1
}

// numberText renders a numeric cell with the mapping's decimal separator, at most four decimals.
func numberText(v, sep string) string {
	f, err := strconv.ParseFloat(v, 64)
	if err != nil {
		return v
	}
	s := strconv.FormatFloat(f, 'f', 4, 64)
	s = strings.TrimRight(strings.TrimRight(s, "0"), ".")
	if sep == "," {
		s = strings.Replace(s, ".", ",", 1)
	}
	return s
}

func readXLSX(content []byte, m Mapping) (*File, error) {
	z, err := zip.NewReader(bytes.NewReader(content), int64(len(content)))
	if err != nil {
		return nil, formatErr("not an XLSX file")
	}
	p, err := sheetPath(z, m.Sheet)
	if err != nil {
		return nil, err
	}
	var shared []string
	if b, err := zipFile(z, "xl/sharedStrings.xml"); err != nil {
		return nil, err
	} else if b != nil {
		var sst struct {
			SI []xlsxText `xml:"si"`
		}
		if err := xml.Unmarshal(b, &sst); err != nil {
			return nil, formatErr("shared strings")
		}
		for _, si := range sst.SI {
			shared = append(shared, si.String())
		}
	}
	b, err := zipFile(z, p)
	if err != nil || b == nil {
		return nil, formatErr("sheet %s missing", p)
	}
	var sh xlsxSheet
	if err := xml.Unmarshal(b, &sh); err != nil {
		return nil, formatErr("sheet: %v", err)
	}
	if len(sh.Rows) > MaxRows+*m.HeaderRow+1 {
		return nil, ErrTooManyRows
	}
	rows := make([][]string, 0, len(sh.Rows))
	nos := make([]int, 0, len(sh.Rows))
	for i, r := range sh.Rows {
		no := r.R
		if no == 0 {
			no = i + 1
		}
		var cells []string
		for j, c := range r.Cells {
			col := j
			if c.R != "" {
				col = cellColumn(c.R)
			}
			if col < 0 {
				continue
			}
			for len(cells) <= col {
				cells = append(cells, "")
			}
			switch c.T {
			case "s":
				if k, err := strconv.Atoi(c.V); err == nil && k >= 0 && k < len(shared) {
					cells[col] = shared[k]
				}
			case "inlineStr":
				cells[col] = c.Is.String()
			case "str", "b", "e":
				cells[col] = c.V
			default:
				cells[col] = numberText(c.V, m.DecimalSeparator)
			}
		}
		rows = append(rows, cells)
		nos = append(nos, no)
	}
	return table(rows, nos, *m.HeaderRow), nil
}
