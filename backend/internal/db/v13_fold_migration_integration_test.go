//go:build integration

package db_test

import (
	"io/fs"
	"testing"
	"testing/fstest"

	"github.com/fpirim/fikopar/backend/internal/db"
	"github.com/fpirim/fikopar/backend/internal/testutil"
	"github.com/fpirim/fikopar/backend/migrations"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// Codes of one part and kind that only differ in Turkish letters collide after 0011: the lowest id stays, the
// others move to part_code_fold_duplicates with their spelling.
func TestV13_Migration0011KeepsTheFirstOfFoldedDuplicates(t *testing.T) {
	fresh := testutil.EmptyDatabase(t, "fold_upgrade")
	before := fstest.MapFS{}
	names, err := fs.Glob(migrations.FS, "*.sql")
	require.NoError(t, err)
	for _, n := range names {
		if n < "0011" {
			b, err := fs.ReadFile(migrations.FS, n)
			require.NoError(t, err)
			before[n] = &fstest.MapFile{Data: b}
		}
	}
	require.NoError(t, db.MigrateFS(fresh, before))

	var org, catalog, part int64
	require.NoError(t, fresh.Raw(`INSERT INTO organizations (name, slug, created_at, updated_at) VALUES ('A','a',now(),now()) RETURNING id`).Scan(&org).Error)
	require.NoError(t, fresh.Raw(`INSERT INTO catalogs (org_id, code, name) VALUES (?, 10, 'Bosch') RETURNING id`, org).Scan(&catalog).Error)
	require.NoError(t, fresh.Raw(`INSERT INTO parts (org_id, catalog_id, stock_no, name) VALUES (?, ?, 'P-1', 'Şalter') RETURNING id`, org, catalog).Scan(&part).Error)
	code := func(kind, c string) int64 {
		var id int64
		require.NoError(t, fresh.Raw(`INSERT INTO part_codes (org_id, part_id, kind, code) VALUES (?, ?, ?, ?) RETURNING id`, org, part, kind, c).Scan(&id).Error)
		return id
	}
	first := code("OEM", "ŞALTER-1")
	dup := code("OEM", "SALTER-1")
	other := code("MANUFACTURER", "SALTER-1")
	cig := code("ALT", "ÇIĞ")
	cig2 := code("ALT", "CIG")
	cig3 := code("ALT", "çig")

	require.NoError(t, db.MigrateFS(fresh, migrations.FS))

	var kept []int64
	require.NoError(t, fresh.Raw(`SELECT id FROM part_codes WHERE part_id = ? ORDER BY id`, part).Scan(&kept).Error)
	assert.Equal(t, []int64{first, other, cig}, kept)
	var norm string
	require.NoError(t, fresh.Raw(`SELECT normalized_code FROM part_codes WHERE id = ?`, first).Scan(&norm).Error)
	assert.Equal(t, "SALTER1", norm)

	type dropped struct {
		ID       int64
		PartID   int64
		Kind     string
		Code     string
		KeptID   int64
		KeptCode string
	}
	var got []dropped
	require.NoError(t, fresh.Raw(`SELECT id, part_id, kind, code, kept_id, kept_code FROM part_code_fold_duplicates ORDER BY id`).Scan(&got).Error)
	assert.Equal(t, []dropped{
		{dup, part, "OEM", "SALTER-1", first, "ŞALTER-1"},
		{cig2, part, "ALT", "CIG", cig, "ÇIĞ"},
		{cig3, part, "ALT", "çig", cig, "ÇIĞ"},
	}, got)
}
