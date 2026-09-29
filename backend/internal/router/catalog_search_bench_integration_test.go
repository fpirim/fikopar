//go:build integration

package router_test

import (
	"context"
	"fmt"
	"math/rand"
	"os"
	"sort"
	"sync"
	"testing"
	"time"

	"github.com/fpirim/fikopar/backend/internal/repository"
	"github.com/fpirim/fikopar/backend/internal/services"
	"github.com/fpirim/fikopar/backend/internal/testutil"

	"github.com/stretchr/testify/require"
)

// seedCatalogVolume loads 50 000 parts in 10 catalogs, 200 000 codes (manufacturer, OEM, ALT, equivalent),
// stock in 2 depots and a price per part, the volume searchCatalog is specified for.
func seedCatalogVolume(t testing.TB) (org uint, codes []string) {
	require.NoError(t, testDB.Raw(`INSERT INTO organizations (name, slug, created_at, updated_at) VALUES ('Volume', ?, now(), now()) RETURNING id`,
		fmt.Sprintf("volume-%d", time.Now().UnixNano())).Scan(&org).Error)
	exec := func(sql string, args ...any) { require.NoError(t, testDB.Exec(sql, args...).Error) }
	exec(`INSERT INTO depots (org_id, code, name) VALUES (?, '1', 'D1'), (?, '2', 'D2')`, org, org)
	exec(`INSERT INTO catalogs (org_id, code, name) SELECT ?, g, 'Catalog ' || g FROM generate_series(1, 10) g`, org)
	exec(`SELECT setseed(0.42)`)
	exec(`INSERT INTO parts (org_id, catalog_id, stock_no, name, brand)
		SELECT ?, c.id, lpad(to_hex((g * 2654435761) % 4294967291), 8, '0') || '-' || (g % 97),
		       (ARRAY['Yağ filtresi','Hava filtresi','Fren balatası','Debriyaj seti','Kabin filtresi','Mazot filtresi',
		              'Amortisör','Rulman','Conta takımı','Triger kayışı'])[1 + g % 10] || ' ' || g,
		       (ARRAY['Bosch','Mahle','Mann','Valeo','SKF'])[1 + g % 5]
		FROM generate_series(1, 50000) g JOIN catalogs c ON c.org_id = ? AND c.code = 1 + g % 10`, org, org)
	exec(`INSERT INTO part_codes (org_id, part_id, kind, code)
		SELECT p.org_id, p.id, k.kind, upper(substr(md5(p.id::text || k.kind), 1, 4)) || '.' || substr(md5(k.kind || p.id::text), 1, 6)
		FROM parts p CROSS JOIN (VALUES ('MANUFACTURER'), ('OEM'), ('ALT')) k(kind) WHERE p.org_id = ?`, org)
	exec(`INSERT INTO part_codes (org_id, part_id, kind, code, equivalent_part_id)
		SELECT p.org_id, p.id, 'EQUIVALENT', e.stock_no, e.id FROM parts p JOIN parts e ON e.org_id = p.org_id AND e.id = p.id + 1
		WHERE p.org_id = ?`, org)
	exec(`INSERT INTO stock_movements (org_id, part_id, depot_id, kind, qty, source_type, movement_date)
		SELECT p.org_id, p.id, d.id, 'IN', 1 + p.id % 13, 'legacy_import', DATE '2026-01-01'
		FROM parts p JOIN depots d ON d.org_id = p.org_id WHERE p.org_id = ? AND p.id % 3 <> 0`, org)
	exec(`INSERT INTO prices (org_id, part_id, price, valid_from) SELECT org_id, id, 10 + id % 500, DATE '2026-01-01' FROM parts WHERE org_id = ?`, org)
	// Only the owner may ANALYZE (autovacuum does it in production).
	require.NoError(t, testutil.Owner().Exec(`ANALYZE parts, part_codes, stock_levels, prices, catalogs, depots`).Error)
	require.NoError(t, testDB.Raw(`SELECT code FROM part_codes WHERE org_id = ? ORDER BY random() LIMIT 100`, org).Scan(&codes).Error)
	var nos []string
	require.NoError(t, testDB.Raw(`SELECT stock_no FROM parts WHERE org_id = ? ORDER BY random() LIMIT 100`, org).Scan(&nos).Error)
	return org, append(codes, nos...)
}

func searchQueries(codes []string) []string {
	r := rand.New(rand.NewSource(7))
	var qs []string
	for _, c := range codes {
		norm := services.NormalizeCode(c)
		switch r.Intn(4) {
		case 0:
			qs = append(qs, c)
		case 1:
			qs = append(qs, norm[:min(len(norm), 4)])
		case 2:
			qs = append(qs, norm[1:min(len(norm), 6)])
		default:
			qs = append(qs, []string{"yağ filtre", "FREN", "kabin filt", "rulman 12", "conta", "triger kayış"}[r.Intn(6)])
		}
	}
	return qs
}

func p95(d []time.Duration) time.Duration {
	sort.Slice(d, func(i, j int) bool { return d[i] < d[j] })
	return d[(len(d)*95)/100]
}

// TestCatalogSearch_P95 is the performance gate of searchCatalog (FIKOPAR_PERF=1): p95 < 50 ms at 50 000 parts.
func TestCatalogSearch_P95(t *testing.T) {
	if os.Getenv("FIKOPAR_PERF") == "" {
		t.Skip("set FIKOPAR_PERF=1")
	}
	org, codes := seedCatalogVolume(t)
	svc := services.NewCatalogService(repository.NewStore(testDB))
	qs := searchQueries(codes)
	ctx := context.Background()
	for _, q := range qs[:20] {
		_, err := svc.Search(ctx, org, services.SearchRequest{Q: q})
		require.NoError(t, err)
	}
	var lat []time.Duration
	hits := 0
	for round := 0; round < 3; round++ {
		for _, q := range qs {
			start := time.Now()
			res, err := svc.Search(ctx, org, services.SearchRequest{Q: q})
			lat = append(lat, time.Since(start))
			require.NoError(t, err, q)
			hits += len(res.Items)
		}
	}
	got := p95(lat)
	t.Logf("searchCatalog: %d queries, %d hits, p50 %v, p95 %v, max %v", len(lat), hits, lat[len(lat)/2], got, lat[len(lat)-1])
	require.Less(t, got, 50*time.Millisecond)
}

var benchSeed struct {
	sync.Once
	org   uint
	codes []string
}

func BenchmarkCatalogSearch(b *testing.B) {
	benchSeed.Do(func() { benchSeed.org, benchSeed.codes = seedCatalogVolume(b) })
	org, codes := benchSeed.org, benchSeed.codes
	svc := services.NewCatalogService(repository.NewStore(testDB))
	qs := searchQueries(codes)
	ctx := context.Background()
	lat := make([]time.Duration, 0, b.N)
	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		start := time.Now()
		if _, err := svc.Search(ctx, org, services.SearchRequest{Q: qs[i%len(qs)]}); err != nil {
			b.Fatal(err)
		}
		lat = append(lat, time.Since(start))
	}
	b.ReportMetric(float64(p95(lat).Microseconds())/1000, "p95-ms")
}
