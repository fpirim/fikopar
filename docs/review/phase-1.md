# Phase 1 — ledger backend

Branch `f1/backend` from `contracts-v1`.

## Delivered
- SQL migrations embedded and applied at startup (`backend/migrations/000{1..4}`, `schema_migrations`,
  advisory lock). Drafts 0001, 0002, 0005 promoted unchanged; 0004 adds the balance projection trigger,
  posting currency guard, `ledger_start()`, `carry_forward_runs`, `outbox_deliveries`, `ledger_checks`
  and the cash voucher guard.
- `/api/orgs/{orgId}` tenancy gate (DB membership, admin passes, unknown org 404); sicils moved there.
- Depots, number series, accounts (ETag / If-Match), account roles, manual journal entries, reversal,
  entry trail, outbox relay with an idempotent consumer, receipts (cash, bank transfer), cash vouchers
  (GDR, VRM), till counts, cash book, as-of balances grid, statement, carry forward, opening balances.
- XLSX (own minimal writer) and PDF (go-pdf/fpdf + Go fonts) for balances, statement, cash book,
  receipt, receipts and sicils grids, carry-forward preview.

## Invariants
| Invariant | Database | Service | Test |
|---|---|---|---|
| Σ debit = Σ credit, ≥ 2 lines | deferred constraint triggers | `postEntry` | db schema tests, property tests (db, API) |
| Postings / entries immutable | append-only triggers | — | `PostingsAndEntriesAreAppendOnly` |
| One reversal per entry, linked both ways | `UNIQUE(reverses_entry_id)` | row lock + `reversedByEntryId` | reversal + concurrent reversal tests |
| Owned entries reversed via owner | — | `ERR_ENTRY_OWNED_BY_SOURCE` | receipt / opening entry tests |
| Balance = Σ postings | projection trigger, drift view | ledger-check consumer | property tests |
| Numbers unique, gap-free | counter upsert row lock, forward-only trigger | allocated inside the posting TX | 40-goroutine test with rollbacks; 12 concurrent receipts |
| Idempotency per (org, key) | PK on `idempotency_keys` | claim / replay / 409 / 422 | replay, reuse, in-flight, concurrent, per-org |
| Optimistic locking | `version` column | `UPDATE … WHERE version` | 412 / 428 and 6 concurrent PATCHes |
| No double carry-forward | run PK, entry source key, opening PK | advisory lock | re-post, replay, 5 concurrent posts |
| Tenancy | composite FKs | middleware + org-scoped queries | every `/api/orgs/:orgId` route vs. non-member; cross-org ids 404 |

## Mutation checks
| Guard broken | Result |
|---|---|
| Balance trigger returns early | `UnbalancedEntryRejectedAtCommit`, `SingleLineRejected` fail |
| Service Σ check disabled | `RejectsInvalidEntriesWithoutConsumingNumbers` fails (DB answers 500) |
| Idempotent replay bypassed | 3 idempotency tests fail |
| `next_number` read-then-write | concurrency test fails 3/3; concurrent receipts test fails |
| Membership check skipped | 5 org-scope / sicil tests fail |

All restored; suite green after each.

## Gates
`go build`, `go vet`, `golangci-lint` (0 issues), `go-arch-lint` (needs Go 1.25 toolchain; 1.27
crashes the loader), `deadcode`, `go test -race -count=3 -tags=integration ./...` green.

## Open
- `getSicilSummary`, `getTrialBalance`, dashboard, print endpoints: not in F1 scope.
- Router integration tests take ~2 min under `-race` (bcrypt cost in logins).
- Contract gaps: [phase-1-contract-issues.md](phase-1-contract-issues.md).
