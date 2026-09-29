package repository

import (
	"errors"

	"github.com/fpirim/fikopar/backend/internal/models"
)

type BalanceQuery struct {
	AsOf      models.Date
	DepotID   *uint
	AccountID *uint
	SicilTipi *string
	Grid      ReportGridRequest
	All       bool
}

type BalanceTotals struct {
	TotalCount      int64        `gorm:"column:total_count"`
	UnfilteredCount int64        `gorm:"column:unfiltered_count"`
	Debit           models.Money `gorm:"column:debit"`
	Credit          models.Money `gorm:"column:credit"`
	Balance         models.Money `gorm:"column:balance"`
	Overdue         models.Money `gorm:"column:overdue"`
}

var balanceCols = map[string]GridCol{
	"accountCode":  {"account_code", ColText},
	"accountName":  {"account_name", ColText},
	"city":         {"city", ColText},
	"sicilTipi":    {"sicil_tipi", ColText},
	"debitTotal":   {"debit_total", ColNumber},
	"creditTotal":  {"credit_total", ColNumber},
	"balance":      {"balance", ColNumber},
	"overdue":      {"overdue", ColNumber},
	"lastMoveDate": {"last_move_date", ColDate},
}

// Overdue applies all credits to the oldest debits: debits due before the date minus credits,
// capped at the balance.
const balancesCTE = `WITH b AS (
	SELECT a.id AS account_id, a.code AS account_code, a.name AS account_name, a.sicil_id, s.sicil_tipi,
	       nullif(s.sehir, '') AS city, ?::bigint AS depot_id,
	       coalesce(sum(p.amount) FILTER (WHERE p.side = 'DEBIT'), 0) AS debit_total,
	       coalesce(sum(p.amount) FILTER (WHERE p.side = 'CREDIT'), 0) AS credit_total,
	       coalesce(sum(p.amount) FILTER (WHERE p.side = 'DEBIT' AND coalesce(p.due_date, e.entry_date) < ?), 0) AS due_debit,
	       max(e.entry_date) FILTER (WHERE e.source_type <> 'opening') AS last_move_date
	FROM accounts a
	JOIN sicils s ON s.id = a.sicil_id
	LEFT JOIN (postings p JOIN journal_entries e ON e.id = p.entry_id)
	       ON p.account_id = a.id AND e.entry_date >= ledger_start(a.org_id, ?) AND e.entry_date <= ?
	      AND (?::bigint IS NULL OR e.depot_id = ?)
	WHERE a.org_id = ? AND (?::text IS NULL OR s.sicil_tipi = ?) AND (?::bigint IS NULL OR a.id = ?)
	GROUP BY a.id, s.id
), r AS (
	SELECT *, debit_total - credit_total AS balance,
	       greatest(0, least(debit_total - credit_total, due_debit - credit_total)) AS overdue
	FROM b
)`

func (q *Q) Balances(orgID uint, in BalanceQuery) ([]models.BalanceRow, *BalanceTotals, error) {
	cteArgs := []any{in.DepotID, in.AsOf, in.AsOf, in.AsOf, in.DepotID, in.DepotID, orgID, in.SicilTipi, in.SicilTipi, in.AccountID, in.AccountID}
	if err := knownCols(in.Grid, balanceCols); err != nil {
		return nil, nil, err
	}
	where, wargs, err := whereClause(in.Grid.FilterModel, balanceCols)
	if err != nil {
		return nil, nil, err
	}
	search, sargs := searchClause(in.Grid.Search, []string{"account_code", "account_name", "city"})
	cond := where + " AND " + search
	condArgs := append(wargs, sargs...)

	var totals BalanceTotals
	err = q.db.Raw(balancesCTE+`
		SELECT count(*) FILTER (WHERE `+cond+`) AS total_count, count(*) AS unfiltered_count,
		       coalesce(sum(debit_total) FILTER (WHERE `+cond+`), 0) AS debit,
		       coalesce(sum(credit_total) FILTER (WHERE `+cond+`), 0) AS credit,
		       coalesce(sum(balance) FILTER (WHERE `+cond+`), 0) AS balance,
		       coalesce(sum(overdue) FILTER (WHERE `+cond+`), 0) AS overdue
		FROM r`, concat(cteArgs, condArgs, condArgs, condArgs, condArgs, condArgs)...).Scan(&totals).Error
	if err != nil {
		return nil, nil, err
	}

	sql := balancesCTE + ` SELECT account_id, account_code, account_name, sicil_id, sicil_tipi, city, depot_id,
		debit_total, credit_total, balance, overdue, last_move_date FROM r WHERE ` + cond +
		` ORDER BY ` + orderClause(in.Grid.SortModel, balanceCols, "account_code, account_id")
	args := concat(cteArgs, condArgs)
	if n := in.Grid.EndRow - in.Grid.StartRow; !in.All && n > 0 {
		sql += ` LIMIT ? OFFSET ?`
		args = append(args, n, in.Grid.StartRow)
	}
	rows, err := list[models.BalanceRow](q.db, sql, args...)
	return rows, &totals, err
}

func concat(parts ...[]any) []any {
	var out []any
	for _, p := range parts {
		out = append(out, p...)
	}
	return out
}

func IsBadFilter(err error) bool { return errors.Is(err, ErrBadFilter) }

// StatementLines lists the account's postings in [from, to]. Openings dated `from` are part of the
// brought-forward row; later active openings are resetsBalance lines; voided ones and their
// reversals net to zero and are left out.
func (q *Q) StatementLines(orgID, accountID uint, depotID *uint, from, to models.Date) ([]models.StatementLine, error) {
	return list[models.StatementLine](q.db, `
		SELECT e.id AS entry_id, e.entry_date, e.depot_id, coalesce(r.receipt_no, v.voucher_no, doc.document_no, e.entry_no) AS document_no,
		       e.source_type, e.source_id, coalesce(r.kind, v.kind, doc.type) AS subtype, doc.id AS document_id,
		       doc.type AS document_type, p.due_date, e.description,
		       CASE p.side WHEN 'DEBIT' THEN p.amount ELSE 0 END AS debit,
		       CASE p.side WHEN 'CREDIT' THEN p.amount ELSE 0 END AS credit,
		       e.source_type = 'opening' AS resets_balance
		FROM postings p
		JOIN journal_entries e ON e.id = p.entry_id
		LEFT JOIN receipts r ON r.org_id = e.org_id AND e.id IN (r.journal_entry_id, r.voided_by_entry_id)
		LEFT JOIN cash_vouchers v ON v.org_id = e.org_id AND e.id IN (v.journal_entry_id, v.voided_by_entry_id)
		LEFT JOIN journal_entries oe ON oe.id = e.reverses_entry_id
		LEFT JOIN documents doc ON doc.org_id = e.org_id AND doc.id::text = CASE
		     WHEN e.source_type = 'document' THEN e.source_id WHEN oe.source_type = 'document' THEN oe.source_id END
		WHERE e.org_id = ? AND p.account_id = ? AND e.entry_date BETWEEN ? AND ?
		  AND (?::bigint IS NULL OR e.depot_id = ?)
		  AND (NOT `+isOpening+` OR (e.source_type = 'opening' AND e.entry_date > ?
		       AND NOT EXISTS (SELECT 1 FROM journal_entries x WHERE x.reverses_entry_id = e.id)))
		ORDER BY e.entry_date, resets_balance DESC, e.id, p.line_no`, orgID, accountID, from, to, depotID, depotID, from)
}
