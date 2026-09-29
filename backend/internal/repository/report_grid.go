package repository

import (
	"encoding/json"
	"errors"
	"fmt"
	"strconv"
	"strings"
)

// FlexString accepts a JSON string or number (AG Grid sends numbers for number filters).
type FlexString string

func (f *FlexString) UnmarshalJSON(b []byte) error {
	if string(b) == "null" {
		*f = ""
		return nil
	}
	var s string
	if err := json.Unmarshal(b, &s); err == nil {
		*f = FlexString(s)
		return nil
	}
	var n json.Number
	if err := json.Unmarshal(b, &n); err != nil {
		return err
	}
	*f = FlexString(n.String())
	return nil
}

type TypedFilter struct {
	FilterType string     `json:"filterType"`
	Type       string     `json:"type"`
	Filter     FlexString `json:"filter"`
	FilterTo   FlexString `json:"filterTo"`
	DateFrom   *string    `json:"dateFrom"`
	DateTo     *string    `json:"dateTo"`
}

type ReportGridRequest struct {
	StartRow    int                    `json:"startRow"`
	EndRow      int                    `json:"endRow"`
	SortModel   []SortModel            `json:"sortModel"`
	FilterModel map[string]TypedFilter `json:"filterModel"`
	Search      string                 `json:"search"`
}

type ColKind int

const (
	ColText ColKind = iota
	ColNumber
	ColDate
)

type GridCol struct {
	Expr string
	Kind ColKind
}

var ErrBadFilter = errors.New("bad filter")

// whereClause turns column filters into AND-combined SQL over whitelisted expressions.
func whereClause(filters map[string]TypedFilter, cols map[string]GridCol) (string, []any, error) {
	var parts []string
	var args []any
	for id, f := range filters {
		col, ok := cols[id]
		if !ok {
			continue
		}
		sql, a, err := filterSQL(col, f)
		if err != nil {
			return "", nil, fmt.Errorf("%w: %s", ErrBadFilter, id)
		}
		if sql != "" {
			parts = append(parts, sql)
			args = append(args, a...)
		}
	}
	if len(parts) == 0 {
		return "true", nil, nil
	}
	return strings.Join(parts, " AND "), args, nil
}

func filterSQL(col GridCol, f TypedFilter) (string, []any, error) {
	e := col.Expr
	switch f.Type {
	case "blank":
		return fmt.Sprintf("(%s IS NULL OR %s::text = '')", e, e), nil, nil
	case "notBlank":
		return fmt.Sprintf("(%s IS NOT NULL AND %s::text <> '')", e, e), nil, nil
	}
	switch col.Kind {
	case ColNumber:
		return compare(e, "::numeric", f.Type, string(f.Filter), string(f.FilterTo), number)
	case ColDate:
		from, to := string(f.Filter), string(f.FilterTo)
		if f.DateFrom != nil {
			from = *f.DateFrom
		}
		if f.DateTo != nil {
			to = *f.DateTo
		}
		return compare(e, "::date", f.Type, trimDate(from), trimDate(to), date)
	}
	v := string(f.Filter)
	switch f.Type {
	case "contains":
		return e + " ILIKE ?", []any{"%" + v + "%"}, nil
	case "notContains":
		return "coalesce(" + e + ", '') NOT ILIKE ?", []any{"%" + v + "%"}, nil
	case "equals":
		return e + " = ?", []any{v}, nil
	case "notEqual":
		return e + " IS DISTINCT FROM ?", []any{v}, nil
	case "startsWith":
		return e + " ILIKE ?", []any{v + "%"}, nil
	case "endsWith":
		return e + " ILIKE ?", []any{"%" + v}, nil
	}
	return "", nil, ErrBadFilter
}

var ops = map[string]string{
	"equals": "=", "notEqual": "<>", "lessThan": "<", "lessThanOrEqual": "<=", "greaterThan": ">", "greaterThanOrEqual": ">=",
}

func compare(e, cast, typ, a, b string, valid func(string) bool) (string, []any, error) {
	if typ == "inRange" {
		if !valid(a) || !valid(b) {
			return "", nil, ErrBadFilter
		}
		return fmt.Sprintf("%s BETWEEN ?%s AND ?%s", e, cast, cast), []any{a, b}, nil
	}
	op, ok := ops[typ]
	if !ok || !valid(a) {
		return "", nil, ErrBadFilter
	}
	return fmt.Sprintf("%s %s ?%s", e, op, cast), []any{a}, nil
}

func number(s string) bool {
	_, err := strconv.ParseFloat(s, 64)
	return err == nil
}

func date(s string) bool { return len(s) == 10 && s[4] == '-' && s[7] == '-' }

func trimDate(s string) string {
	if len(s) > 10 {
		return s[:10]
	}
	return s
}

func orderClause(sort []SortModel, cols map[string]GridCol, fallback string) string {
	var parts []string
	for _, s := range sort {
		col, ok := cols[s.ColID]
		if !ok {
			continue
		}
		dir := "ASC"
		if strings.EqualFold(s.Sort, "desc") {
			dir = "DESC"
		}
		parts = append(parts, col.Expr+" "+dir+" NULLS LAST")
	}
	return strings.Join(append(parts, fallback), ", ")
}

func searchClause(term string, exprs []string) (string, []any) {
	term = strings.TrimSpace(term)
	if term == "" {
		return "true", nil
	}
	parts := make([]string, len(exprs))
	args := make([]any, len(exprs))
	for i, e := range exprs {
		parts[i] = e + " ILIKE ?"
		args[i] = "%" + term + "%"
	}
	return "(" + strings.Join(parts, " OR ") + ")", args
}
