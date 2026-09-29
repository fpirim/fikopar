//go:build integration

package router_test

import (
	"net/http"
	"strconv"
	"testing"

	"github.com/stretchr/testify/require"
)

// stockFix is a company with two depots and one catalog.
type stockFix struct {
	*ledgerFix
	depot2  uint
	catalog uint
}

func newStock(t *testing.T) *stockFix {
	t.Helper()
	f := &stockFix{ledgerFix: newLedger(t)}
	f.depot2 = f.newDepot(t, "2")
	f.catalog = f.newCatalog(t, 10, "Bosch")
	return f
}

func (f *stockFix) newCatalog(t *testing.T, code int, name string) uint {
	t.Helper()
	rec := f.post(t, f.path("/catalog/catalogs"), f.token, `{"code":`+strconv.Itoa(code)+`,"name":"`+name+`"}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	return extractID(t, rec.Body.Bytes())
}

// part creates a part in f.catalog; extra is appended to the JSON body (e.g. `,"codes":[…]`).
func (f *stockFix) part(t *testing.T, stockNo, name, extra string) uint {
	t.Helper()
	return f.partIn(t, f.catalog, stockNo, name, extra)
}

func (f *stockFix) partIn(t *testing.T, catalog uint, stockNo, name, extra string) uint {
	t.Helper()
	rec := f.post(t, f.path("/catalog/parts"), f.token,
		`{"catalogId":`+idStr(catalog)+`,"stockNo":"`+stockNo+`","name":"`+name+`"`+extra+`}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	return extractID(t, rec.Body.Bytes())
}

// move writes a legacy movement straight into the ledger (documents arrive with F3).
func (f *stockFix) move(t *testing.T, part, depot uint, kind, qty, date string) {
	t.Helper()
	require.NoError(t, testDB.Exec(`INSERT INTO stock_movements (org_id, part_id, depot_id, kind, qty, source_type, movement_date)
		VALUES (?, ?, ?, ?, ?::numeric, 'legacy_import', ?::date)`, f.org, part, depot, kind, qty, date).Error)
}

func (f *stockFix) onHand(t *testing.T, part, depot uint) string {
	t.Helper()
	var s string
	require.NoError(t, testDB.Raw(`SELECT coalesce((SELECT trim_scale(on_hand)::text FROM stock_levels WHERE part_id = ? AND depot_id = ?), '0')`,
		part, depot).Scan(&s).Error)
	return s
}
