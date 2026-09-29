//go:build integration

package db_test

import (
	"testing"

	"github.com/fpirim/fikopar/backend/internal/testutil"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"gorm.io/gorm"
)

var appendOnly = []string{
	"postings", "journal_entries", "receipts", "cash_vouchers", "opening_balances",
	"carry_forward_runs", "cash_counts", "instrument_events", "audit_events", "outbox", "prices", "stock_movements",
}

func TestAppendOnlyTablesRefuseTruncate(t *testing.T) {
	f := seed(t)
	require.NoError(t, testDB.Transaction(func(tx *gorm.DB) error {
		return insertEntry(tx, f, "J1", line{f.cash, "DEBIT", "10.00"}, line{f.sales, "CREDIT", "10.00"})
	}))
	for _, table := range appendOnly {
		err := testDB.Exec(`TRUNCATE ` + table + ` CASCADE`).Error
		require.Error(t, err, table)
		assert.Contains(t, err.Error(), "TRUNCATE on "+table+" is not allowed", table)
	}
	var n int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM postings`).Scan(&n).Error)
	assert.EqualValues(t, 2, n)
}

func TestAppRoleWritesRowsButNotTheSchema(t *testing.T) {
	f := seed(t)
	app := testutil.App(t)
	require.NoError(t, app.Transaction(func(tx *gorm.DB) error {
		return insertEntry(tx, f, "J1", line{f.cash, "DEBIT", "10.00"}, line{f.sales, "CREDIT", "10.00"})
	}))
	for _, stmt := range []string{
		`ALTER TABLE postings ADD COLUMN x int`,
		`ALTER TABLE sicils DROP COLUMN hesap_adi`,
		`ALTER TABLE postings DISABLE TRIGGER postings_append_only`,
		`DROP TABLE postings`,
		`DROP TABLE sicils CASCADE`,
		`DROP FUNCTION forbid_mutation() CASCADE`,
		`CREATE TABLE x (id int)`,
		`TRUNCATE postings`,
		`TRUNCATE sicils`,
		`SET session_replication_role = replica`,
		`UPDATE postings SET amount = amount`,
		`UPDATE journal_entries SET description = description`,
		`DELETE FROM journal_entries`,
		`DELETE FROM receipts`,
		`DELETE FROM cash_vouchers`,
		`DELETE FROM audit_events`,
		`DELETE FROM schema_migrations`,
	} {
		err := app.Exec(stmt).Error
		require.Error(t, err, stmt)
		assert.Regexp(t, `permission denied|must be owner`, err.Error(), stmt)
	}
	var n int64
	require.NoError(t, app.Raw(`SELECT count(*) FROM postings`).Scan(&n).Error)
	assert.EqualValues(t, 2, n)
}
