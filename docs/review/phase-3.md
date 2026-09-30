# Phase 3 — documents backend

Branch `f3/backend` from `contracts-v1.4`.

## Delivered
- Migrations: draft `0004_documents` promoted as `0012_documents` (documents, lines with the rounding checks, billing,
  return and decrease caps, cancel-only posted documents, reservations with the definer projection, sweeper function,
  LAST_PURCHASE `last_purchase_cost()`); `0013_documents_runtime`: `outbox.seq` for per-aggregate order, TRUNCATE
  guards on documents / lines / reservations, reservations insert-and-delete only for `fikopar_app`,
  `lock_stock_levels()`. `draft/check/run.sh` and `mutations.sh` now run against `0012`.
- All 11 types. Drafts: create (`Idempotency-Key`), update and delete (`If-Match`), card defaults (discounts, VAT of
  the part, due date from `gun`, salesperson if a member), waybill line copy, return / price-difference links, amounts
  per line half-up, totals = Σ lines, per-depot stock, reservations `min(qty, available)` for 48 h with STOCK_SHORT,
  CREDIT_LIMIT and PRICE_BELOW_LIST warnings, `effects`.
- Post (`Idempotency-Key` + `If-Match`, one transaction): number, card snapshot, credit check with admin override
  (document fields + `document.credit_override` audit), reservations released, `DocumentPosted`, then the ledger
  consumer (ADR-0013 posting table on account roles, due date on the card posting) and the stock consumer inline.
  Cancel: dated today (Istanbul), `DocumentCancelled`, reversal entry and opposite movements inline, frees a billed
  waybill. Consumers are idempotent on their source keys and registered on the relay with the no-op `DocumentExporter`.
- Sweeper every 5 min (`Sweep(ctx, now)` takes the clock), relay per-aggregate order, `listMembers`,
  `gridDocuments` (filters, column filters, totals without cancelled, XLSX), document PDF (company, card, lines, VAT
  per rate, totals, DRAFT / CANCELLED marks), dashboard `latest` and search with documents, statement `documentId` /
  `documentType`, movement document fields and `reversal`, carry-forward `NO_DRAFT_DOCUMENTS`, count valuation at
  LAST_PURCHASE.

## Invariants
| Invariant | Database | Service | Test |
|---|---|---|---|
| D1 state machine, drafts deleted, posted fixed | `documents_state_machine`, `document_lines_draft_only` | `lockDraft`, cancel from POSTED only | delete / cancel / second post tests |
| D2/D3 entry once, movement once per line, reversal on the cancel date | entry and movement source keys, `stock_movements_document_rules` | consumers skip what exists | posting table, cancel, redelivery tests |
| D4 bill once, line for line | `documents_waybill_rules`, `documents_waybill_once` | `BilledBy` at save | billing, 8 parallel bills |
| D5 amounts, totals = Σ lines | line CHECKs, `documents_post_rules` | `lineAmounts` (big ints) | property: 30 random documents vs a big.Rat model |
| D6 return cap | `documents_post_rules` (locks the original) | post locks the original first | cap, 8 parallel returns, returns into two depots |
| D7 decrease cap | `documents_post_rules` | — | price differences |
| D8 reservations ≤ available, projection | `apply_stock_reservation()`, guard, drift view | `min(qty, available)` with savepoint retry, ordered level locks | reservation vs post, sweeper vs updates (reserved = released + held) |
| D9 credit override | CHECKs | admin only, audit | credit limit test |
| S2 on hand = Σ movements | projection | — | property: 40 random posts / cancels / returns vs a model |
| L1 Σ = 0 | deferred trigger | `postEntry` | property tests over every entry |
| Tenancy | composite FKs | org-scoped queries | two-company test (documents, grid, search, links) |

## Mutation checks
Each guard broken alone on the committed tree; the named tests fail, the file restored after each.

| Guard broken | Result |
|---|---|
| Return cap in `documents_post_rules` (0012 M2) | `ReturnsAreCapped`, `ParallelReturnsStayWithinTheOriginal` fail |
| Decrease cap (0012 M4) | `PriceDifferences` fails |
| Credit check skipped | `CreditLimitBlocksUnlessAdminOverrides` fails |
| Override without the admin check | `CreditLimitBlocksUnlessAdminOverrides` fails |
| Reservation takes qty instead of `min(qty, available)` | `DraftDefaultsAmountsAndReservations` fails |
| Draft `If-Match` ignored | `UpdateReplacesReservationsUnderIfMatch` fails |
| Level pre-lock in post removed | `OpposingPartOrdersDoNotDeadlock` fails (deadlock, 500) 3/3 |
| Relay ignores aggregate order | `RelayKeepsDocumentOrder` fails |
| Ledger consumer not idempotent | `PostSalesInvoice` fails (redelivery errors) |
| Stock consumer not idempotent | `PostSalesInvoice`, `CancelReversesLedgerAndStock` fail |
| Cancel dated on the document date | `CancelReversesLedgerAndStock` fails |
| Closed year not checked on save | `ClosedYear` fails |
| Net from the rounded gross | `Property_TotalsAreSumOfRoundedLines…`, `LineAmounts…` fail |
| Document read without the company filter | `CompaniesAreSeparate` fails |
| Sweeper ignores expiry | `SweeperReleasesExpiredReservations` fails |
| Original lock removed in the trigger **and** the service | `ReturnsIntoTwoDepotsStayWithinTheOriginal` fails |
| Billing once removed in the service, trigger **and** unique index | `ParallelBillsOfOneWaybill`, `WaybillBillingOnce` fail |

Layered guards: the original lock in the trigger or in the service alone, and the billing check in the service or
the trigger alone (the unique index stays), are equivalent mutations; each layer holds the other. `draft/check/mutations.sh`
M1–M4 on `0012` are killed as before.

## Gates
`go build`, `go vet -tags=integration`, `golangci-lint` (0 issues; with `--build-tags=integration` only the 5 older
findings), `go-arch-lint` and `deadcode` (Go 1.25) clean (deadcode lists only the older `repository/mocks`).
`go test -race -count=3 -timeout 30m -tags=integration ./...` green (router 142 s, whole run 2 min 26 s).
`draft/check/run.sh` ok, `mutations.sh` M1–M4 killed. Coverage (integration run, `-coverpkg=./internal/...`):
services 85.0 %, handlers 84.9 %, repository 91.3 %, models 95.7 %, apierror 92.0 %; new files 82–96 %.

## Open
- Contract gaps and readings: [phase-3-contract-issues.md](phase-3-contract-issues.md).
- e-Fatura: `DocumentExporter` port with the no-op exporter only (no endpoint).
