# Draft migrations

Contract DDL (v1.2). F1 promoted `0001_common`, `0002_ledger` and `0005_receipts_instruments` unchanged into
[`../`](..) (applied at startup by `internal/db`, tracked in `schema_migrations`); the other files follow
with their phases. GORM `AutoMigrate` still creates `organizations`, `users`, `user_organizations`,
`sicils` and `sicil_telefons` first.

`check/run.sh` applies the drafts to a throwaway `postgres:16` (with stub `organizations`, `users`, `sicils`) and
runs `check/invariants.sql`.

v1.1 changed `0001_common` (`sicils.version`) and `0002_ledger` (TRY-only postings, one currency per entry,
`carry_forward_runs` with reopen, `carry_forward_previews`, closed-year trigger, `ledger_start()`, voidable
`opening_balances`). The promoted files and `0004_ledger_runtime` (its `carry_forward_runs` has no reopen) are v1;
[`../0005_contracts_v1_1.sql`](../0005_contracts_v1_1.sql) brings them to v1.1. Opening entries posted before it keep
the v1 source key `<year>:<account>:<depot>`.

v1.2 rewrote `0003_catalog_stock` (catalogs as price lists, dated prices, price imports, negative stock, stock level
trigger, counts frozen at open, closed years for movements), dropped `depots.allow_negative_stock` from `0001_common`
and moved the `last_purchase_cost()` fallback in `0004_documents` to the part's catalog price. `check/invariants.sql`
covers price history, import states, the stock projection, transfers and count line versions.
