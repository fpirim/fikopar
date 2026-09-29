# Phase 2 — catalog and stock backend

Branch `f2/backend` from `contracts-v1.2`. Stock counts (`f2/counts`) and price imports (`f2/imports`) were built in
parallel worktrees against the scaffolding commit and merged here.

## Delivered
- Migration `0008_catalog_stock`: draft `0003` promoted with `pg_trgm`, a folded name column for word search,
  `depots.allow_negative_stock` dropped, outbox `depot_id` nullable plus `PriceChanged` / `PriceListImported`,
  `last_purchase_cost()` (catalog fallback until F3), TRUNCATE guards on `prices` and `stock_movements`, grants for
  `fikopar_app` (insert-only prices and movements, read-only `stock_levels` written by a `SECURITY DEFINER` trigger).
- Catalogs, parts with MANUFACTURER / OEM / ALT / EQUIVALENT codes, deactivate, lookup, resolve, dated list prices
  (future dates, `ERR_PRICE_EXISTS`, equal price is a no-op, `PriceChanged`), catalog price list with keyset paging.
- `searchCatalog`: exact / prefix / substring / name ranking over btree prefix and trigram indexes, stock of every
  depot, price on date, equivalents both ways, keyset cursor, total capped at 1000. `globalSearch` lists parts.
- Parts grid: `PartColId` filters, sort, block paging, depot-scoped stock columns, stock segments, totals, Excel.
- Stock: per-depot settings upsert (`If-Match "0"` creates), part stock with a total row, levels with
  BELOW_MIN / NEGATIVE filters and keyset paging, movements grid with running on hand and opening / closing, Excel.
- Transfers: one TRF number, TRANSFER_OUT / TRANSFER_IN per line in one transaction, NEGATIVE_STOCK warnings,
  `StockMoved` per movement, closed years.
- Stock counts: open with frozen system quantity, add line, per-line autosave with its own ETag, grid filters and
  live valuation, count sheet PDF, finalize into ADJUST movements with a cost snapshot, cancel.
- Price imports: CSV / XLSX / LISAKT (CP857 fixed width) upload, auto-mapping and NEEDS_MAPPING, preview diff
  rows, apply once (`If-Match` + `Idempotency-Key`, stale check), discard, expiry, `PriceListImported`.
- Dashboard (all v1.2 fields), cash book OPENING `resetsBalance` across a carried year (L9), carry-forward
  STOCK_COUNTS_POSTED (blocking) and YEAR_END_COUNTS (warning).

## Invariants
| Invariant | Database | Service | Test |
|---|---|---|---|
| S1 negative stock allowed, reported | no check | `postMovements` warnings | transfers, count finalize |
| S2 on hand = Σ movements, append-only | projection trigger, `stock_level_drift`, append-only + TRUNCATE triggers, no UPDATE/DELETE grant | movements only through `postMovements` | property test (8 writers), parallel transfers, app-role test |
| S3 one OPEN count per depot, frozen system qty, line version +1 | partial unique index, `stock_count_lines_guard`, `stock_counts_transition` | count row lock, `If-Match` per line | concurrent autosave, parallel finalize, freeze tests |
| S4 transfer atomic | `UNIQUE(transfer_id, part_id, kind)` | one TX, lock order (part, depot) | 20 opposite transfers, no deadlock, exact levels |
| S5 closed years | `stock_movements_year_open` trigger | `yearOpen` under the ledger lock | transfer, count open, carry-forward tests |
| P1 part in one fixed catalog | `parts_guard`, unique stock no per catalog | `ERR_PART_CATALOG_IMMUTABLE` | parts test |
| P2 one price per (part, date), append-only | `UNIQUE(part_id, valid_from)`, append-only + TRUNCATE triggers | `ERR_PRICE_EXISTS`, equal = no-op | 6 parallel sets → 1 created, 5 × 409 |
| P3 import applied once, from READY, on its catalog version | `price_imports_transition` | `If-Match`, `Idempotency-Key`, `content_version` compare under the catalog row lock | replay, concurrent apply, stale |
| Tenancy | composite FKs | org middleware | every new route vs non-member (`TestOrgScope`) |

## Mutation checks
| Guard broken | Result |
|---|---|
| `stock_movements_append_only` trigger removed | `TestV12_MovementsAndPricesAreAppendOnly` fails |
| Projection trigger ignores the movement sign | `StockLevelIsProjected…`, `OnHandEqualsSumOfMovementsProperty` fail |
| `UNIQUE(part_id, valid_from)` removed | `PricesOnePerDateAndPriceOn` fails |
| Count line `version = old + 1` rule removed | `CountLineVersionMovesByOne…` fails |
| Line update without `AND version = ?` | `AutosaveFlow`, `ConcurrentAutosaveOneWins` fail |
| Finalize without row lock and status/version check | `ParallelFinalizeOneWins` fails |
| Added line from current on hand instead of `on_hand_at(startedAt)` | `ShelfRangeAndAddLine` fails |
| Movement lock order removed | `ParallelOnSamePartsKeepLevelsExact` fails (deadlock) |
| NEGATIVE_STOCK warning suppressed | transfer and count finalize tests fail |
| Different price on an existing date accepted | 3 price tests fail |
| Part catalog change allowed in the service | `CreateGetUpdateDeactivate` fails |
| Import apply idempotency, stale check, `price_imports_transition` | **not run** — Docker went down before the import fork could run them |

All restored; suite green after each.

## Performance
`searchCatalog` at 50 000 parts, 200 000 codes (manufacturer, OEM, ALT, equivalent), 2 depots, a price per part
(`TestCatalogSearch_P95`, `FIKOPAR_PERF=1`; 600 mixed queries: exact codes, prefixes, substrings, name words):
p50 8.5 ms, **p95 18.8 ms**, max 33.8 ms. `BenchmarkCatalogSearch` (600 ops): 10.0 ms/op, p95 28.3 ms.
Postgres 16 in Docker on an M-series Mac, statistics analyzed. Without `ANALYZE` the planner picks the org index
over the trigram index and p95 is ~45 ms.

## Gates
`go build`, `go vet -tags=integration`, `golangci-lint` (0 issues), `go-arch-lint` and `deadcode` (Go 1.25) clean after the
final merge. `go test -race -count=3 -timeout 30m -tags=integration ./...` green on the tree before the price-import
merge (router 97 s). Coverage on that tree: services 84.9 %, handlers 84.5 %, repository 92.0 %, models 95.7 %;
import files 85.5 % (fork run). **Not yet run after the import merge:** the integration suite, `-race -count=3`, and the
switch of the catalog `content_version` triggers to statement level (import apply of 100k rows: 4 m 52 s → 5.8 s,
measured by the import fork). Docker went down during phase 2 and was not restarted.

## Open
- LAST_PURCHASE valuation, document labels and sicil names on movements wait for F3 documents.
- Contract gaps: [phase-2-contract-issues.md](phase-2-contract-issues.md).
