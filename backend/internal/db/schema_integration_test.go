//go:build integration

package db_test

import (
	"fmt"
	"math/rand"
	"sort"
	"sync"
	"testing"
	"testing/fstest"

	"github.com/fpirim/fikopar/backend/internal/db"
	"github.com/fpirim/fikopar/backend/internal/testutil"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"gorm.io/gorm"
)

type fixture struct {
	org, depot   int64
	cash, cust   int64
	sales, other int64
}

func seed(t *testing.T) fixture {
	t.Helper()
	testutil.TruncateAll(t, testDB)
	var f fixture
	require.NoError(t, testDB.Raw(`INSERT INTO organizations (name, slug, created_at, updated_at) VALUES ('A','a',now(),now()) RETURNING id`).Scan(&f.org).Error)
	require.NoError(t, testDB.Raw(`INSERT INTO depots (org_id, code, name) VALUES (?, '1', 'Main') RETURNING id`, f.org).Scan(&f.depot).Error)
	acc := func(code, typ, side string) int64 {
		var id int64
		require.NoError(t, testDB.Raw(`INSERT INTO accounts (org_id, code, name, type, normal_side) VALUES (?,?,?,?,?) RETURNING id`,
			f.org, code, code, typ, side).Scan(&id).Error)
		return id
	}
	f.cash = acc("100", "ASSET", "DEBIT")
	f.cust = acc("120.001", "ASSET", "DEBIT")
	f.sales = acc("600", "INCOME", "CREDIT")
	f.other = acc("770", "EXPENSE", "DEBIT")
	return f
}

type line struct {
	account int64
	side    string
	amount  string
}

func insertEntry(tx *gorm.DB, f fixture, no string, lines ...line) error {
	var id int64
	if err := tx.Raw(`INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description, source_type)
		VALUES (?, ?, ?, DATE '2026-03-01', 't', 'manual') RETURNING id`, f.org, f.depot, no).Scan(&id).Error; err != nil {
		return err
	}
	for i, l := range lines {
		if err := tx.Exec(`INSERT INTO postings (org_id, entry_id, line_no, account_id, side, amount, currency)
			VALUES (?, ?, ?, ?, ?, ?, 'TRY')`, f.org, id, i+1, l.account, l.side, l.amount).Error; err != nil {
			return err
		}
	}
	return nil
}

func TestMigrate_IsRepeatable(t *testing.T) {
	require.NoError(t, db.Migrate(testDB))
	var n int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM schema_migrations`).Scan(&n).Error)
	assert.GreaterOrEqual(t, n, int64(4))
}

func TestLedger_UnbalancedEntryRejectedAtCommit(t *testing.T) {
	f := seed(t)
	err := testDB.Transaction(func(tx *gorm.DB) error {
		return insertEntry(tx, f, "J1", line{f.cash, "DEBIT", "100.00"}, line{f.sales, "CREDIT", "99.99"})
	})
	require.Error(t, err)
	assert.Contains(t, err.Error(), "unbalanced")
	assertNoEntries(t)
}

func TestLedger_SingleLineRejected(t *testing.T) {
	f := seed(t)
	err := testDB.Transaction(func(tx *gorm.DB) error {
		return insertEntry(tx, f, "J1", line{f.cash, "DEBIT", "100.00"})
	})
	require.Error(t, err)
	assertNoEntries(t)
}

func TestLedger_EntryWithoutPostingsRejected(t *testing.T) {
	f := seed(t)
	err := testDB.Transaction(func(tx *gorm.DB) error { return insertEntry(tx, f, "J1") })
	require.Error(t, err)
	assertNoEntries(t)
}

func TestLedger_PostingsAndEntriesAreAppendOnly(t *testing.T) {
	f := seed(t)
	require.NoError(t, testDB.Transaction(func(tx *gorm.DB) error {
		return insertEntry(tx, f, "J1", line{f.cash, "DEBIT", "10.00"}, line{f.sales, "CREDIT", "10.00"})
	}))
	for _, stmt := range []string{
		`UPDATE postings SET amount = 20`,
		`DELETE FROM postings`,
		`UPDATE journal_entries SET description = 'x'`,
		`DELETE FROM journal_entries`,
	} {
		err := testDB.Exec(stmt).Error
		require.Error(t, err, stmt)
		assert.Contains(t, err.Error(), "append-only", stmt)
	}
}

func TestLedger_EntryIsReversedAtMostOnce(t *testing.T) {
	f := seed(t)
	require.NoError(t, testDB.Transaction(func(tx *gorm.DB) error {
		return insertEntry(tx, f, "J1", line{f.cash, "DEBIT", "10.00"}, line{f.sales, "CREDIT", "10.00"})
	}))
	var orig int64
	require.NoError(t, testDB.Raw(`SELECT id FROM journal_entries WHERE entry_no = 'J1'`).Scan(&orig).Error)
	reverse := func(no string) error {
		return testDB.Transaction(func(tx *gorm.DB) error {
			var id int64
			if err := tx.Raw(`INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description, source_type, source_id, reverses_entry_id)
				VALUES (?, ?, ?, DATE '2026-03-02', 'rev', 'reversal', ?, ?) RETURNING id`, f.org, f.depot, no, fmt.Sprint(orig), orig).Scan(&id).Error; err != nil {
				return err
			}
			if err := tx.Exec(`INSERT INTO postings (org_id, entry_id, line_no, account_id, side, amount, currency) VALUES (?,?,1,?,'CREDIT',10,'TRY')`, f.org, id, f.cash).Error; err != nil {
				return err
			}
			return tx.Exec(`INSERT INTO postings (org_id, entry_id, line_no, account_id, side, amount, currency) VALUES (?,?,2,?,'DEBIT',10,'TRY')`, f.org, id, f.sales).Error
		})
	}
	require.NoError(t, reverse("J2"))
	require.Error(t, reverse("J3"))
}

func TestLedger_PostingCurrencyMustMatchAccount(t *testing.T) {
	f := seed(t)
	err := testDB.Transaction(func(tx *gorm.DB) error {
		var id int64
		if err := tx.Raw(`INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description, source_type)
			VALUES (?, ?, 'J1', DATE '2026-03-01', 't', 'manual') RETURNING id`, f.org, f.depot).Scan(&id).Error; err != nil {
			return err
		}
		if err := tx.Exec(`INSERT INTO postings (org_id, entry_id, line_no, account_id, side, amount, currency) VALUES (?,?,1,?,'DEBIT',5,'EUR')`, f.org, id, f.cash).Error; err != nil {
			return err
		}
		return tx.Exec(`INSERT INTO postings (org_id, entry_id, line_no, account_id, side, amount, currency) VALUES (?,?,2,?,'CREDIT',5,'EUR')`, f.org, id, f.sales).Error
	})
	require.Error(t, err)
}

func TestLedger_PostingCannotUseAnotherOrgsAccount(t *testing.T) {
	f := seed(t)
	var org2, foreign int64
	require.NoError(t, testDB.Raw(`INSERT INTO organizations (name, slug, created_at, updated_at) VALUES ('B','b',now(),now()) RETURNING id`).Scan(&org2).Error)
	require.NoError(t, testDB.Raw(`INSERT INTO accounts (org_id, code, name, type, normal_side) VALUES (?, '100', 'c', 'ASSET', 'DEBIT') RETURNING id`, org2).Scan(&foreign).Error)
	err := testDB.Transaction(func(tx *gorm.DB) error {
		return insertEntry(tx, f, "J1", line{foreign, "DEBIT", "1.00"}, line{f.sales, "CREDIT", "1.00"})
	})
	require.Error(t, err)
}

func TestLedger_OpeningEntryMustBeFirstJanuary(t *testing.T) {
	f := seed(t)
	err := testDB.Exec(`INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description, source_type, source_id)
		VALUES (?, ?, 'J1', DATE '2026-01-02', 'o', 'opening', '2026:1:1')`, f.org, f.depot).Error
	require.Error(t, err)
}

// Property: for random balanced entries, the projection equals Σ postings and the trial balance nets to zero.
func TestLedger_BalanceEqualsSumOfPostings_Property(t *testing.T) {
	f := seed(t)
	rng := rand.New(rand.NewSource(42))
	accounts := []int64{f.cash, f.cust, f.sales, f.other}
	for i := 0; i < 150; i++ {
		n := 2 + rng.Intn(4)
		lines := make([]line, 0, n+1)
		var debit int64
		for j := 0; j < n; j++ {
			cents := int64(1 + rng.Intn(1_000_000))
			debit += cents
			lines = append(lines, line{accounts[rng.Intn(len(accounts))], "DEBIT", centsStr(cents)})
		}
		// Split the credit side into two lines so rounding across lines is exercised.
		first := debit / 3
		lines = append(lines,
			line{accounts[rng.Intn(len(accounts))], "CREDIT", centsStr(first)},
			line{accounts[rng.Intn(len(accounts))], "CREDIT", centsStr(debit - first)})
		if first == 0 {
			lines = append(lines[:len(lines)-2], line{accounts[0], "CREDIT", centsStr(debit)})
		}
		require.NoError(t, testDB.Transaction(func(tx *gorm.DB) error {
			return insertEntry(tx, f, fmt.Sprintf("J%d", i), lines...)
		}))
	}

	var drift int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM account_balance_drift`).Scan(&drift).Error)
	assert.Zero(t, drift)

	type row struct {
		AccountID int64
		Proj      string
		Sum       string
	}
	var rows []row
	require.NoError(t, testDB.Raw(`
		SELECT b.account_id, (b.debit_total - b.credit_total)::text AS proj,
		       (SELECT sum(CASE side WHEN 'DEBIT' THEN amount ELSE -amount END) FROM postings p WHERE p.account_id = b.account_id)::text AS sum
		FROM account_balances b`).Scan(&rows).Error)
	require.NotEmpty(t, rows)
	for _, r := range rows {
		assert.Equal(t, r.Sum, r.Proj, "account %d", r.AccountID)
	}
	var net string
	require.NoError(t, testDB.Raw(`SELECT sum(debit_total - credit_total)::text FROM account_balances`).Scan(&net).Error)
	assert.Equal(t, "0.00", net)
}

func TestNumberSeries_FormatAndForwardOnly(t *testing.T) {
	f := seed(t)
	var no string
	require.NoError(t, testDB.Raw(`SELECT next_number(?, ?, 'TAH', 2026)`, f.org, f.depot).Scan(&no).Error)
	assert.Equal(t, "TAH-2026-1-000001", no)
	require.NoError(t, testDB.Raw(`SELECT next_number(?, ?, 'TAH', 2026)`, f.org, f.depot).Scan(&no).Error)
	assert.Equal(t, "TAH-2026-1-000002", no)
	require.Error(t, testDB.Exec(`UPDATE number_series SET next_value = 1`).Error)
	require.Error(t, testDB.Raw(`SELECT next_number(?, ?, 'XX', 2026)`, f.org, f.depot).Scan(&no).Error)
}

// N concurrent transactions, every fifth rolls back: committed numbers are unique and gap-free.
func TestNumberSeries_ConcurrentAllocationIsUniqueAndGapFree(t *testing.T) {
	f := seed(t)
	const workers = 40
	var (
		mu        sync.Mutex
		committed []int
		wg        sync.WaitGroup
	)
	errRollback := fmt.Errorf("rollback")
	for i := 0; i < workers; i++ {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			var no string
			err := testDB.Transaction(func(tx *gorm.DB) error {
				if err := tx.Raw(`SELECT next_number(?, ?, 'J', 2026)`, f.org, f.depot).Scan(&no).Error; err != nil {
					return err
				}
				if i%5 == 0 {
					return errRollback
				}
				return nil
			})
			if err == nil {
				var n int
				_, _ = fmt.Sscanf(no, "J-2026-1-%d", &n)
				mu.Lock()
				committed = append(committed, n)
				mu.Unlock()
			} else {
				assert.ErrorIs(t, err, errRollback)
			}
		}(i)
	}
	wg.Wait()
	sort.Ints(committed)
	require.Len(t, committed, workers-workers/5)
	for i, n := range committed {
		assert.Equal(t, i+1, n)
	}
}

func TestIdempotencyKeys_UniquePerOrg(t *testing.T) {
	f := seed(t)
	var org2 int64
	require.NoError(t, testDB.Raw(`INSERT INTO organizations (name, slug, created_at, updated_at) VALUES ('B','b',now(),now()) RETURNING id`).Scan(&org2).Error)
	const key = "8c7e1f36-4a55-4c52-9d9e-0d1c2a3b4c5d"
	ins := `INSERT INTO idempotency_keys (org_id, key, endpoint, request_hash) VALUES (?, ?, 'e', '\x00')`
	require.NoError(t, testDB.Exec(ins, f.org, key).Error)
	require.Error(t, testDB.Exec(ins, f.org, key).Error)
	require.NoError(t, testDB.Exec(ins, org2, key).Error)
}

func assertNoEntries(t *testing.T) {
	t.Helper()
	var n int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM journal_entries`).Scan(&n).Error)
	assert.Zero(t, n)
}

func centsStr(c int64) string { return fmt.Sprintf("%d.%02d", c/100, c%100) }

func TestApplySQL_AppliesNewFilesOnceAndRollsBackFailures(t *testing.T) {
	good := fstest.MapFS{"9001_extra.sql": {Data: []byte(`CREATE TABLE f1_extra (id int);`)}}
	require.NoError(t, db.ApplySQL(testDB, good))
	require.NoError(t, db.ApplySQL(testDB, good), "an applied file is skipped")

	bad := fstest.MapFS{"9002_bad.sql": {Data: []byte(`CREATE TABLE f1_bad (;`)}}
	err := db.ApplySQL(testDB, bad)
	require.Error(t, err)
	assert.Contains(t, err.Error(), "9002_bad.sql")
	var n int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM schema_migrations WHERE version = '9002_bad'`).Scan(&n).Error)
	assert.Zero(t, n)
	require.NoError(t, testDB.Exec(`DROP TABLE f1_extra`).Error)
	require.NoError(t, testDB.Exec(`DELETE FROM schema_migrations WHERE version = '9001_extra'`).Error)
}
