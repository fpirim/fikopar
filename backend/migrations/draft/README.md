# Draft migrations

Contract DDL (v1). F1 promoted `0001_common`, `0002_ledger` and `0005_receipts_instruments` unchanged into
[`../`](..) (applied at startup by `internal/db`, tracked in `schema_migrations`); the other files follow
with their phases. GORM `AutoMigrate` still creates `organizations`, `users`, `user_organizations`,
`sicils` and `sicil_telefons` first.

`check/run.sh` applies the drafts to a throwaway `postgres:16` (with stub `organizations`, `users`, `sicils`) and
runs `check/invariants.sql`.
