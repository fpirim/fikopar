//go:build integration

package db_test

import (
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"gorm.io/gorm"
)

func insertEntryOn(tx *gorm.DB, f fixture, no, date, source string, sourceID *string, lines ...line) (int64, error) {
	var id int64
	if err := tx.Raw(`INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description, source_type, source_id)
		VALUES (?, ?, ?, ?::date, 't', ?, ?) RETURNING id`, f.org, f.depot, no, date, source, sourceID).Scan(&id).Error; err != nil {
		return 0, err
	}
	for i, l := range lines {
		if err := tx.Exec(`INSERT INTO postings (org_id, entry_id, line_no, account_id, side, amount)
			VALUES (?, ?, ?, ?, ?, ?)`, f.org, id, i+1, l.account, l.side, l.amount).Error; err != nil {
			return 0, err
		}
	}
	return id, nil
}

func postRun(t *testing.T, f fixture, year int) int64 {
	t.Helper()
	var id int64
	require.NoError(t, testDB.Raw(`INSERT INTO carry_forward_runs (org_id, year, entry_count, total_debit, total_credit)
		VALUES (?, ?, 0, 0, 0) RETURNING id`, f.org, year).Scan(&id).Error)
	return id
}

func TestV11_AccountsAndPostingsAreTRYOnly(t *testing.T) {
	f := seed(t)
	err := testDB.Exec(`INSERT INTO accounts (org_id, code, name, type, normal_side, currency) VALUES (?, '102.U', 'u', 'ASSET', 'DEBIT', 'USD')`, f.org).Error
	require.Error(t, err)
	assert.Contains(t, err.Error(), "23514")

	err = testDB.Transaction(func(tx *gorm.DB) error {
		id, err := insertEntryOn(tx, f, "J1", "2026-03-01", "manual", nil, line{f.cash, "DEBIT", "1.00"}, line{f.sales, "CREDIT", "1.00"})
		if err != nil {
			return err
		}
		return tx.Exec(`INSERT INTO postings (org_id, entry_id, line_no, account_id, side, amount, currency) VALUES (?, ?, 3, ?, 'DEBIT', 1, 'USD')`, f.org, id, f.cash).Error
	})
	require.Error(t, err)
}

func TestV11_ClosedYearRejectsEntriesUntilReopened(t *testing.T) {
	f := seed(t)
	run := postRun(t, f, 2026)
	for _, date := range []string{"2025-12-31", "2024-06-01"} {
		err := testDB.Transaction(func(tx *gorm.DB) error {
			_, err := insertEntryOn(tx, f, "J-"+date, date, "manual", nil, line{f.cash, "DEBIT", "1.00"}, line{f.sales, "CREDIT", "1.00"})
			return err
		})
		require.Error(t, err, date)
		assert.Contains(t, err.Error(), "ERR_YEAR_CLOSED", date)
	}
	require.NoError(t, testDB.Transaction(func(tx *gorm.DB) error {
		_, err := insertEntryOn(tx, f, "J-open", "2026-01-01", "manual", nil, line{f.cash, "DEBIT", "1.00"}, line{f.sales, "CREDIT", "1.00"})
		return err
	}))

	var start string
	require.NoError(t, testDB.Raw(`SELECT ledger_start(?, '2026-05-01')::text`, f.org).Scan(&start).Error)
	assert.Equal(t, "2026-01-01", start)

	require.NoError(t, testDB.Exec(`UPDATE carry_forward_runs SET reopened_at = now(), reopen_reason = 'late invoice' WHERE id = ?`, run).Error)
	require.NoError(t, testDB.Raw(`SELECT ledger_start(?, '2026-05-01')::text`, f.org).Scan(&start).Error)
	assert.Equal(t, "-infinity", start)
	require.NoError(t, testDB.Transaction(func(tx *gorm.DB) error {
		_, err := insertEntryOn(tx, f, "J-late", "2025-12-31", "manual", nil, line{f.cash, "DEBIT", "1.00"}, line{f.sales, "CREDIT", "1.00"})
		return err
	}))

	err := testDB.Exec(`UPDATE carry_forward_runs SET reopen_reason = 'again' WHERE id = ?`, run).Error
	require.Error(t, err, "a run reopens once")
	require.Error(t, testDB.Exec(`DELETE FROM carry_forward_runs WHERE id = ?`, run).Error)
	require.Error(t, testDB.Exec(`UPDATE carry_forward_runs SET reopened_at = now(), reopen_reason = 'x' WHERE id = ?`, postRun(t, f, 2027)).Error, "reason needs 3 characters")
}

func TestV11_OneActiveRunAndOpeningPerYear(t *testing.T) {
	f := seed(t)
	run := postRun(t, f, 2026)
	require.Error(t, testDB.Exec(`INSERT INTO carry_forward_runs (org_id, year, entry_count, total_debit, total_credit) VALUES (?, 2026, 0, 0, 0)`, f.org).Error)

	opening := func(runID int64, no, src string) (int64, error) {
		var id int64
		err := testDB.Transaction(func(tx *gorm.DB) error {
			var err error
			id, err = insertEntryOn(tx, f, no, "2026-01-01", "opening", &src, line{f.cust, "DEBIT", "5.00"}, line{f.sales, "CREDIT", "5.00"})
			if err != nil {
				return err
			}
			return tx.Exec(`INSERT INTO opening_balances (org_id, account_id, depot_id, year, debit, source, run_id, journal_entry_id)
				VALUES (?, ?, ?, 2026, 5, 'CARRY_FORWARD', ?, ?)`, f.org, f.cust, f.depot, runID, id).Error
		})
		return id, err
	}
	first, err := opening(run, "J-O1", "2026:1:1:1")
	require.NoError(t, err)
	_, err = opening(run, "J-O2", "2026:1:1:2")
	require.Error(t, err, "one active opening per account, depot and year")

	require.Error(t, testDB.Exec(`UPDATE opening_balances SET debit = 6 WHERE journal_entry_id = ?`, first).Error)
	var rev int64
	require.NoError(t, testDB.Transaction(func(tx *gorm.DB) error {
		src := "rev"
		var err error
		rev, err = insertEntryOn(tx, f, "J-R1", "2026-01-01", "reversal", &src, line{f.cust, "CREDIT", "5.00"}, line{f.sales, "DEBIT", "5.00"})
		return err
	}))
	require.NoError(t, testDB.Exec(`UPDATE opening_balances SET voided_by_entry_id = ? WHERE journal_entry_id = ?`, rev, first).Error)
	require.Error(t, testDB.Exec(`UPDATE opening_balances SET voided_by_entry_id = NULL WHERE journal_entry_id = ?`, first).Error, "voided once")
	require.NoError(t, testDB.Exec(`UPDATE carry_forward_runs SET reopened_at = now(), reopen_reason = 'redo' WHERE id = ?`, run).Error)

	_, err = opening(postRun(t, f, 2026), "J-O3", "2026:2:1:1")
	require.NoError(t, err, "the reopened year can be posted again")
}

func TestV11_SicilsHaveAVersion(t *testing.T) {
	f := seed(t)
	var v int
	require.NoError(t, testDB.Raw(`INSERT INTO sicils (organization_id, hesap_no, hesap_adi, sicil_tipi, created_at, updated_at)
		VALUES (?, 'S1', 'S', 'FIRMA', now(), now()) RETURNING version`, f.org).Scan(&v).Error)
	assert.Equal(t, 1, v)
}
