# Draft migrations

Contract DDL (v1.1). F1 promoted `0001_common`, `0002_ledger` and `0005_receipts_instruments` unchanged into
[`../`](..) (applied at startup by `internal/db`, tracked in `schema_migrations`); the other files follow
with their phases. GORM `AutoMigrate` still creates `organizations`, `users`, `user_organizations`,
`sicils` and `sicil_telefons` first.

`check/run.sh` applies the drafts to a throwaway `postgres:16` (with stub `organizations`, `users`, `sicils`) and
runs `check/invariants.sql`.

v1.1 changed `0001_common` (`sicils.version`) and `0002_ledger` (TRY-only postings, one currency per entry,
`carry_forward_runs` with reopen, `carry_forward_previews`, closed-year trigger, `ledger_start()`, voidable
`opening_balances`). The promoted files and `0004_ledger_runtime` (its `carry_forward_runs` has no reopen) are v1; the
backend brings them to v1.1 with a new migration.
