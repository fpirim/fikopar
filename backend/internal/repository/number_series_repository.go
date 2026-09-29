package repository

import "github.com/fpirim/fikopar/backend/internal/models"

const seriesCols = `id, depot_id, year, prefix, width, next_value, version`

type SeriesFilter struct {
	Year    int
	Prefix  string
	DepotID uint
}

func (q *Q) ListNumberSeries(orgID uint, f SeriesFilter) ([]models.NumberSeries, error) {
	sql := `SELECT ` + seriesCols + ` FROM number_series WHERE org_id = ?`
	args := []any{orgID}
	if f.Year != 0 {
		sql += ` AND year = ?`
		args = append(args, f.Year)
	}
	if f.Prefix != "" {
		sql += ` AND prefix = ?`
		args = append(args, f.Prefix)
	}
	if f.DepotID != 0 {
		sql += ` AND depot_id = ?`
		args = append(args, f.DepotID)
	}
	return list[models.NumberSeries](q.db, sql+` ORDER BY year DESC, depot_id, prefix`, args...)
}

func (q *Q) GetNumberSeries(orgID, id uint) (*models.NumberSeries, error) {
	return first[models.NumberSeries](q.db, `SELECT `+seriesCols+` FROM number_series WHERE org_id = ? AND id = ?`, orgID, id)
}

func (q *Q) UpdateNumberSeries(orgID, id uint, version int, width *int, next *int64) (*models.NumberSeries, error) {
	rows, err := list[models.NumberSeries](q.db, `UPDATE number_series
		SET width = coalesce(?, width), next_value = coalesce(?, next_value), version = version + 1
		WHERE org_id = ? AND id = ? AND version = ? RETURNING `+seriesCols, width, next, orgID, id, version)
	if err != nil || len(rows) == 0 {
		return nil, err
	}
	return &rows[0], nil
}

// NextNumber allocates inside the caller's transaction; a rollback returns the number.
func (q *Q) NextNumber(orgID, depotID uint, prefix string, year int) (string, error) {
	var no string
	err := q.db.Raw(`SELECT next_number(?, ?, ?, ?)`, orgID, depotID, prefix, year).Scan(&no).Error
	return no, err
}
