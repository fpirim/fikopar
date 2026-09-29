package repository

import (
	"fmt"
	"strings"

	"gorm.io/gorm"
)

type SortModel struct {
	ColID string `json:"colId"`
	Sort  string `json:"sort"`
}

type FilterModel map[string]FilterItem

type FilterItem struct {
	FilterType string `json:"filterType"`
	Type       string `json:"type"`
	Filter     string `json:"filter"`
	FilterTo   string `json:"filterTo,omitempty"`
}

type GridRequest struct {
	StartRow    int         `json:"startRow"`
	EndRow      int         `json:"endRow"`
	SortModel   []SortModel `json:"sortModel"`
	FilterModel FilterModel `json:"filterModel"`
	// Search is a free-text term applied across the entity's searchable text
	// columns (OR ILIKE). Empty means no search.
	Search string `json:"search"`
}

type GridResponse struct {
	Rows       any   `json:"rows"`
	TotalCount int64 `json:"totalCount"`
}

const ilikeClause = "%s ILIKE ?"

// dbCol comes from the caller's whitelist; values are always bound parameters.
func applyColumnFilter(query *gorm.DB, dbCol string, filter FilterItem) *gorm.DB {
	switch filter.Type {
	case "contains":
		return query.Where(fmt.Sprintf(ilikeClause, dbCol), "%"+filter.Filter+"%")
	case "notContains":
		return query.Where(fmt.Sprintf("%s NOT ILIKE ?", dbCol), "%"+filter.Filter+"%")
	case "equals":
		return query.Where(fmt.Sprintf("%s = ?", dbCol), filter.Filter)
	case "notEqual":
		return query.Where(fmt.Sprintf("%s != ?", dbCol), filter.Filter)
	case "startsWith":
		return query.Where(fmt.Sprintf(ilikeClause, dbCol), filter.Filter+"%")
	case "endsWith":
		return query.Where(fmt.Sprintf(ilikeClause, dbCol), "%"+filter.Filter)
	case "blank":
		return query.Where(fmt.Sprintf("%s = '' OR %s IS NULL", dbCol, dbCol))
	case "notBlank":
		return query.Where(fmt.Sprintf("%s != '' AND %s IS NOT NULL", dbCol, dbCol))
	}
	return query
}

// paginatedGridWithMapping applies AG-Grid filters, an optional free-text search,
// sorting and pagination. colMap whitelists which colIds may be filtered/sorted
// (-> safe column names). searchCols lists the DB columns the free-text Search
// term is matched against (OR ILIKE); both column lists come from code, never
// from user input, and all values are parameterized.
func paginatedGridWithMapping(db *gorm.DB, req GridRequest, dest any, colMap map[string]string, searchCols []string) (*GridResponse, error) {
	query := db

	for colID, filter := range req.FilterModel {
		dbCol, ok := colMap[colID]
		if !ok {
			continue
		}
		query = applyColumnFilter(query, dbCol, filter)
	}

	// Free-text search: OR ILIKE across the searchable columns.
	if term := strings.TrimSpace(req.Search); term != "" && len(searchCols) > 0 {
		clauses := make([]string, len(searchCols))
		args := make([]any, len(searchCols))
		for i, col := range searchCols {
			clauses[i] = fmt.Sprintf(ilikeClause, col)
			args[i] = "%" + term + "%"
		}
		query = query.Where("("+strings.Join(clauses, " OR ")+")", args...)
	}

	// Count the filtered set (before pagination). Use a session clone so the
	// count(*) projection does not leak into the Find below.
	var totalCount int64
	if err := query.Session(&gorm.Session{}).Count(&totalCount).Error; err != nil {
		return nil, err
	}

	for _, s := range req.SortModel {
		dbCol, ok := colMap[s.ColID]
		if !ok {
			continue
		}
		dir := "ASC"
		if strings.ToLower(s.Sort) == "desc" {
			dir = "DESC"
		}
		query = query.Order(fmt.Sprintf("%s %s", dbCol, dir))
	}

	pageSize := req.EndRow - req.StartRow
	if pageSize > 0 {
		query = query.Offset(req.StartRow).Limit(pageSize)
	}

	if err := query.Find(dest).Error; err != nil {
		return nil, err
	}

	return &GridResponse{Rows: dest, TotalCount: totalCount}, nil
}
