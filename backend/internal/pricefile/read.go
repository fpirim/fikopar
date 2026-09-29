package pricefile

import (
	"bytes"
	"encoding/csv"
	"fmt"
	"io"
	"strings"
	"unicode/utf8"

	"golang.org/x/text/encoding/charmap"
)

// Row is one data row; No is its 1-based row in the file (header included).
type Row struct {
	No    int
	Cells []string
	line  []rune
}

type File struct {
	Headers []string
	Rows    []Row
}

// Sample returns up to n raw data rows.
func (f *File) Sample(n int) [][]string {
	out := [][]string{}
	for i := 0; i < len(f.Rows) && i < n; i++ {
		if f.Rows[i].line != nil {
			out = append(out, []string{string(f.Rows[i].line)})
			continue
		}
		out = append(out, f.Rows[i].Cells)
	}
	return out
}

func formatErr(detail string, args ...any) error {
	return fmt.Errorf("%w: "+detail, append([]any{ErrFormat}, args...)...)
}

func decode(b []byte, encoding string) (string, error) {
	switch encoding {
	case "CP857":
		var sb strings.Builder
		sb.Grow(len(b))
		for _, c := range b {
			if c < 0x80 {
				sb.WriteByte(c)
			} else {
				sb.WriteRune(cp857[c-0x80])
			}
		}
		return sb.String(), nil
	case "WINDOWS-1254":
		out, err := charmap.Windows1254.NewDecoder().Bytes(b)
		return string(out), err
	}
	b = bytes.TrimPrefix(b, []byte("\ufeff"))
	if !utf8.Valid(b) {
		return "", formatErr("file is not UTF-8")
	}
	return string(b), nil
}

// Read parses the file with a resolved mapping.
func Read(content []byte, m Mapping) (*File, error) {
	var f *File
	var err error
	switch m.Format {
	case "XLSX":
		f, err = readXLSX(content, m)
	case "FIXED_WIDTH":
		f, err = readFixed(content, m)
	case "CSV":
		f, err = readCSV(content, m)
	default:
		return nil, ErrFormat
	}
	if err != nil {
		return nil, err
	}
	if len(f.Rows) > MaxRows {
		return nil, ErrTooManyRows
	}
	return f, nil
}

func detectDelimiter(text string) rune {
	first := text
	for _, l := range strings.Split(text, "\n") {
		if strings.TrimSpace(l) != "" {
			first = l
			break
		}
	}
	best, bestN := ',', 0
	for _, d := range []rune{';', ',', '\t'} {
		n, quoted := 0, false
		for _, r := range first {
			if r == '"' {
				quoted = !quoted
			} else if r == d && !quoted {
				n++
			}
		}
		if n > bestN {
			best, bestN = d, n
		}
	}
	return best
}

func blank(cells []string) bool {
	for _, c := range cells {
		if strings.TrimSpace(c) != "" {
			return false
		}
	}
	return true
}

func readCSV(content []byte, m Mapping) (*File, error) {
	text, err := decode(content, m.Encoding)
	if err != nil {
		return nil, err
	}
	r := csv.NewReader(strings.NewReader(text))
	r.Comma = detectDelimiter(text)
	if m.Delimiter != "" {
		r.Comma, _ = utf8.DecodeRuneInString(m.Delimiter)
	}
	r.FieldsPerRecord = -1
	r.LazyQuotes = true
	var rows [][]string
	var lines []int
	for {
		rec, err := r.Read()
		if err == io.EOF {
			break
		}
		if err != nil {
			return nil, formatErr("%v", err)
		}
		line, _ := r.FieldPos(0)
		rows = append(rows, rec)
		lines = append(lines, line)
		if len(rows) > MaxRows+*m.HeaderRow+1 {
			return nil, ErrTooManyRows
		}
	}
	return table(rows, lines, *m.HeaderRow), nil
}

// table splits raw rows into the header (row headerRow) and the data rows after it.
func table(rows [][]string, nos []int, headerRow int) *File {
	f := &File{}
	for i, rec := range rows {
		switch {
		case headerRow > 0 && nos[i] == headerRow:
			f.Headers = trimAll(rec)
		case nos[i] > headerRow && !blank(rec):
			f.Rows = append(f.Rows, Row{No: nos[i], Cells: rec})
		}
	}
	return f
}

func trimAll(s []string) []string {
	out := make([]string, len(s))
	for i, v := range s {
		out[i] = strings.TrimSpace(v)
	}
	return out
}

func readFixed(content []byte, m Mapping) (*File, error) {
	text, err := decode(content, m.Encoding)
	if err != nil {
		return nil, err
	}
	f := &File{}
	for i, l := range strings.Split(text, "\n") {
		l = strings.TrimRight(l, "\r")
		if strings.TrimSpace(l) == "" || i+1 <= *m.HeaderRow {
			continue
		}
		f.Rows = append(f.Rows, Row{No: i + 1, line: []rune(l)})
		if len(f.Rows) > MaxRows {
			return nil, ErrTooManyRows
		}
	}
	return f, nil
}

// Extract returns the raw, trimmed text of every mapped target in a row.
func Extract(m Mapping, headers []string, r Row) map[string]string {
	out := map[string]string{}
	for target, c := range m.Columns {
		var v string
		if r.line != nil {
			if c.Start < 1 || c.Length < 1 || c.Start > len(r.line) {
				continue
			}
			end := min(c.Start-1+c.Length, len(r.line))
			v = string(r.line[c.Start-1 : end])
			if c.Scale > 0 {
				v = scaled(strings.TrimSpace(v), c.Scale)
			}
		} else {
			i := columnIndex(c, headers)
			if i < 0 || i >= len(r.Cells) {
				continue
			}
			v = r.Cells[i]
		}
		if v = strings.TrimSpace(v); v != "" {
			out[target] = v
		}
	}
	return out
}

// scaled inserts the implied decimal point of an unsigned zoned number ("00012345", 4 → "0001.2345").
func scaled(digits string, scale int) string {
	for _, r := range digits {
		if r < '0' || r > '9' {
			return digits
		}
	}
	if digits == "" {
		return ""
	}
	for len(digits) <= scale {
		digits = "0" + digits
	}
	return digits[:len(digits)-scale] + "." + digits[len(digits)-scale:]
}
