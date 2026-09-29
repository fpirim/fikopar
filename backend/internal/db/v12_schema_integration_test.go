//go:build integration

package db_test

import (
	"fmt"
	"math/rand"
	"sync"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/testutil"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"gorm.io/gorm"
)

type stockFixture struct {
	fixture
	depot2  int64
	catalog int64
	part    int64
	part2   int64
}

func seedStock(t *testing.T) stockFixture {
	t.Helper()
	f := stockFixture{fixture: seed(t)}
	require.NoError(t, testDB.Raw(`INSERT INTO depots (org_id, code, name) VALUES (?, '2', 'Second') RETURNING id`, f.org).Scan(&f.depot2).Error)
	require.NoError(t, testDB.Raw(`INSERT INTO catalogs (org_id, code, name) VALUES (?, 10, 'Bosch') RETURNING id`, f.org).Scan(&f.catalog).Error)
	part := func(no string) int64 {
		var id int64
		require.NoError(t, testDB.Raw(`INSERT INTO parts (org_id, catalog_id, stock_no, name) VALUES (?, ?, ?, 'Filter') RETURNING id`,
			f.org, f.catalog, no).Scan(&id).Error)
		return id
	}
	f.part = part("F-100")
	f.part2 = part("F-200")
	return f
}

func move(db *gorm.DB, f stockFixture, part, depot int64, kind, qty, date string) error {
	src := "legacy_import"
	return db.Exec(`INSERT INTO stock_movements (org_id, part_id, depot_id, kind, qty, source_type, movement_date)
		VALUES (?, ?, ?, ?, ?, ?, ?::date)`, f.org, part, depot, kind, qty, src, date).Error
}

func onHand(t *testing.T, part, depot int64) string {
	t.Helper()
	var s string
	require.NoError(t, testDB.Raw(`SELECT coalesce((SELECT on_hand::text FROM stock_levels WHERE part_id = ? AND depot_id = ?), '0')`, part, depot).Scan(&s).Error)
	return s
}

func TestV12_DepotsHaveNoNegativeStockFlag(t *testing.T) {
	var n int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM information_schema.columns
		WHERE table_name = 'depots' AND column_name = 'allow_negative_stock'`).Scan(&n).Error)
	assert.Zero(t, n)
}

func TestV12_StockLevelIsProjectedAndMayGoNegative(t *testing.T) {
	f := seedStock(t)
	require.NoError(t, move(testDB, f, f.part, f.depot, "IN", "5", "2026-03-01"))
	require.NoError(t, move(testDB, f, f.part, f.depot, "OUT", "7.5", "2026-03-02"))
	require.NoError(t, move(testDB, f, f.part, f.depot2, "IN", "1", "2026-03-02"))
	assert.Equal(t, "-2.5000", onHand(t, f.part, f.depot))
	assert.Equal(t, "1.0000", onHand(t, f.part, f.depot2))
	var drift int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM stock_level_drift`).Scan(&drift).Error)
	assert.Zero(t, drift)
}

// on_hand = Σ movements for any sequence, also when movements race on one level row.
func TestV12_OnHandEqualsSumOfMovementsProperty(t *testing.T) {
	f := seedStock(t)
	app := testutil.App(t)
	kinds := []string{"IN", "OUT"}
	var wg sync.WaitGroup
	for w := 0; w < 8; w++ {
		wg.Add(1)
		go func(seed int64) {
			defer wg.Done()
			r := rand.New(rand.NewSource(seed))
			for i := 0; i < 25; i++ {
				kind := kinds[r.Intn(2)]
				part, depot := []int64{f.part, f.part2}[r.Intn(2)], []int64{f.depot, f.depot2}[r.Intn(2)]
				qty := fmt.Sprintf("%d.%04d", r.Intn(50)+1, r.Intn(10000))
				assert.NoError(t, move(app, f, part, depot, kind, qty, "2026-04-01"))
			}
		}(int64(w))
	}
	wg.Wait()
	var bad int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM (
		SELECT m.part_id, m.depot_id, sum(movement_sign(m.kind) * m.qty) AS q FROM stock_movements m GROUP BY 1, 2) s
		JOIN stock_levels l USING (part_id, depot_id) WHERE l.on_hand <> s.q`).Scan(&bad).Error)
	assert.Zero(t, bad)
	var drift, levels int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM stock_level_drift`).Scan(&drift).Error)
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM stock_levels`).Scan(&levels).Error)
	assert.Zero(t, drift)
	assert.EqualValues(t, 4, levels)
}

func TestV12_MovementsAndPricesAreAppendOnly(t *testing.T) {
	f := seedStock(t)
	require.NoError(t, move(testDB, f, f.part, f.depot, "IN", "5", "2026-03-01"))
	require.NoError(t, testDB.Exec(`INSERT INTO prices (org_id, part_id, price, valid_from) VALUES (?, ?, 10, '2026-01-01')`, f.org, f.part).Error)
	for _, stmt := range []string{
		`UPDATE stock_movements SET qty = 1`, `DELETE FROM stock_movements`,
		`UPDATE prices SET price = 1`, `DELETE FROM prices`,
		`TRUNCATE stock_movements CASCADE`, `TRUNCATE prices CASCADE`,
	} {
		assert.Error(t, testDB.Exec(stmt).Error, stmt)
	}
	assert.Equal(t, "5.0000", onHand(t, f.part, f.depot))
}

func TestV12_AppRoleCannotRewriteStockOrPrices(t *testing.T) {
	f := seedStock(t)
	app := testutil.App(t)
	require.NoError(t, move(app, f, f.part, f.depot, "IN", "5", "2026-03-01"))
	require.NoError(t, app.Exec(`INSERT INTO prices (org_id, part_id, price, valid_from) VALUES (?, ?, 10, '2026-01-01')`, f.org, f.part).Error)
	for _, stmt := range []string{
		`UPDATE stock_movements SET qty = 1`, `DELETE FROM stock_movements`,
		`UPDATE prices SET price = 1`, `DELETE FROM prices`,
		`UPDATE stock_levels SET on_hand = 100`, `INSERT INTO stock_levels (org_id, part_id, depot_id, on_hand) VALUES (1, 1, 1, 1)`,
		`DELETE FROM stock_levels`, `TRUNCATE stock_movements`,
	} {
		err := app.Exec(stmt).Error
		require.Error(t, err, stmt)
		assert.Regexp(t, `permission denied`, err.Error(), stmt)
	}
	assert.Equal(t, "5.0000", onHand(t, f.part, f.depot))
}

func TestV12_MovementInClosedYearIsRejected(t *testing.T) {
	f := seedStock(t)
	postRun(t, f.fixture, 2026)
	err := move(testDB, f, f.part, f.depot, "IN", "1", "2025-12-31")
	require.Error(t, err)
	assert.Contains(t, err.Error(), "ERR_YEAR_CLOSED")
	require.NoError(t, move(testDB, f, f.part, f.depot, "IN", "1", "2026-01-01"))
}

func TestV12_PricesOnePerDateAndPriceOn(t *testing.T) {
	f := seedStock(t)
	ins := func(price, from string) error {
		return testDB.Exec(`INSERT INTO prices (org_id, part_id, price, valid_from) VALUES (?, ?, ?, ?::date)`, f.org, f.part, price, from).Error
	}
	require.NoError(t, ins("10", "2026-01-01"))
	require.NoError(t, ins("12", "2026-06-01"))
	assert.Error(t, ins("11", "2026-06-01"))
	price := func(d string) string {
		var s string
		require.NoError(t, testDB.Raw(`SELECT coalesce((SELECT price::text FROM price_on(?, ?, ?::date)), 'none')`, f.org, f.part, d).Scan(&s).Error)
		return s
	}
	assert.Equal(t, "none", price("2025-12-31"))
	assert.Equal(t, "10.0000", price("2026-05-31"))
	assert.Equal(t, "12.0000", price("2026-06-01"))
	var cv int64
	require.NoError(t, testDB.Raw(`SELECT content_version FROM catalogs WHERE id = ?`, f.catalog).Scan(&cv).Error)
	assert.Positive(t, cv)
}

func TestV12_PartCatalogIsFixed(t *testing.T) {
	f := seedStock(t)
	var other int64
	require.NoError(t, testDB.Raw(`INSERT INTO catalogs (org_id, code, name) VALUES (?, 11, 'Other') RETURNING id`, f.org).Scan(&other).Error)
	assert.Error(t, testDB.Exec(`UPDATE parts SET catalog_id = ? WHERE id = ?`, other, f.part).Error)
}

func openCount(t *testing.T, f stockFixture, depot int64) int64 {
	t.Helper()
	var id int64
	require.NoError(t, testDB.Raw(`INSERT INTO stock_counts (org_id, depot_id, count_date, year, count_no)
		VALUES (?, ?, '2026-05-01', 2026, ?) RETURNING id`, f.org, depot, fmt.Sprintf("SAY-2026-%d-%d", depot, rand.Int())).Scan(&id).Error)
	return id
}

func TestV12_CountLineVersionMovesByOneAndSystemQtyIsFrozen(t *testing.T) {
	f := seedStock(t)
	c := openCount(t, f, f.depot)
	var line int64
	require.NoError(t, testDB.Raw(`INSERT INTO stock_count_lines (org_id, count_id, part_id, system_qty) VALUES (?, ?, ?, 3) RETURNING id`,
		f.org, c, f.part).Scan(&line).Error)
	assert.Error(t, testDB.Exec(`UPDATE stock_count_lines SET counted_qty = 1, counted_at = now(), version = version + 2 WHERE id = ?`, line).Error)
	assert.Error(t, testDB.Exec(`UPDATE stock_count_lines SET counted_qty = 1, counted_at = now() WHERE id = ?`, line).Error)
	assert.Error(t, testDB.Exec(`UPDATE stock_count_lines SET system_qty = 4, version = version + 1 WHERE id = ?`, line).Error)
	require.NoError(t, testDB.Exec(`UPDATE stock_count_lines SET counted_qty = 1, counted_at = now(), version = version + 1 WHERE id = ?`, line).Error)
	assert.Error(t, testDB.Exec(`INSERT INTO stock_count_lines (org_id, count_id, part_id, system_qty) VALUES (?, ?, ?, 0)`, f.org, c, f.part).Error)

	require.NoError(t, testDB.Exec(`UPDATE stock_counts SET status = 'CANCELLED', cancelled_at = now(), cancel_reason = 'x' WHERE id = ?`, c).Error)
	err := testDB.Exec(`UPDATE stock_count_lines SET counted_qty = 2, counted_at = now(), version = version + 1 WHERE id = ?`, line).Error
	require.Error(t, err)
	assert.Contains(t, err.Error(), "ERR_STOCK_COUNT_NOT_OPEN")
}

func TestV12_OneOpenCountPerDepot(t *testing.T) {
	f := seedStock(t)
	openCount(t, f, f.depot)
	openCount(t, f, f.depot2)
	err := testDB.Exec(`INSERT INTO stock_counts (org_id, depot_id, count_date, year, count_no) VALUES (?, ?, '2026-05-01', 2026, 'X')`, f.org, f.depot).Error
	assert.Error(t, err)
}

func TestV12_ImportAppliedOnlyFromApplying(t *testing.T) {
	f := seedStock(t)
	var id int64
	require.NoError(t, testDB.Raw(`INSERT INTO price_imports (org_id, catalog_id, file_name, file_sha256, file_size, format, mapping, valid_from, status)
		VALUES (?, ?, 'a.csv', repeat('a', 64), 1, 'CSV', '{}', '2026-01-01', 'READY') RETURNING id`, f.org, f.catalog).Scan(&id).Error)
	assert.Error(t, testDB.Exec(`UPDATE price_imports SET status = 'APPLIED', applied_at = now() WHERE id = ?`, id).Error)
	require.NoError(t, testDB.Exec(`UPDATE price_imports SET status = 'APPLYING' WHERE id = ?`, id).Error)
	require.NoError(t, testDB.Exec(`UPDATE price_imports SET status = 'APPLIED', applied_at = now() WHERE id = ?`, id).Error)
	assert.Error(t, testDB.Exec(`UPDATE price_imports SET status = 'READY', applied_at = NULL WHERE id = ?`, id).Error)
}

func TestV12_OutboxTakesCompanyWideCatalogEvents(t *testing.T) {
	f := seedStock(t)
	require.NoError(t, testDB.Exec(`INSERT INTO outbox (id, org_id, depot_id, event_type, aggregate_type, aggregate_id, payload)
		VALUES (gen_random_uuid(), ?, NULL, 'PriceChanged', 'part', '1', '{}')`, f.org).Error)
	require.NoError(t, testDB.Exec(`INSERT INTO outbox (id, org_id, depot_id, event_type, aggregate_type, aggregate_id, payload)
		VALUES (gen_random_uuid(), ?, NULL, 'PriceListImported', 'price_import', '1', '{}')`, f.org).Error)
}
