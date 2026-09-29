# Phase 3 — contracts v1.5 and stabilization

Branch `f3/v15` from `f3/integration`; tag `contracts-v1.5` ([ADR-0014](../adr/0014-contracts-v1-5.md)) resolves
[phase-3-contract-issues.md](phase-3-contract-issues.md). Merged: `f3/v15-backend`, `f3/v15-frontend`, `f3/v15-flake`.

## Delivered
- Backend: post / cancel write the entry and the movements inline in their transaction (`document_effects.go`; the
  relay keeps `LedgerCheck` and the exporter); credit balance = current account only (`PendingLedgerDocuments`
  removed); `Document.hesapNo`; card `sales_rep_user_id` (write, member check) with `SicilSummary.salesRepUserId` /
  `salesRepName`; `gridDocuments billed` / `billsWaybill` (grid and XLSX); `details.dependents` on cancel and draft
  update / delete; `stockNo` on warnings; STOCK_SHORT by other documents' holds; `lastPurchaseCost` on parts
  (`0014_last_purchase.sql`, `last_purchase()`); `Dashboard.latest` removed.
- Frontend: types regenerated; workarounds removed (role fallback, summary account code, "Card default", billed
  waybills by 409, originals by `waybillDocumentNo`, dependents re-read, stock no from lines); salesperson on the card
  dialog; last purchase cost on the part card; MSW on v1.5.

## Real e2e
All real suites (incl. `@v14`, `@v15`) twice in a row: 47 passed, 6 screenshot tests skipped; `e2e:real:v13` 35.
Legacy 47, MSW 96. Found and fixed:
- Seed: the sale on card 10482 was over its 250,000 limit (244,820 + 37,920); the card gets 400,000.
- Ledger and stock checks assumed no documents: they now count the seeded sale, return and increase and avoid
  cards and rows the document specs post to, so subsets (`e2e:real:v13`) and the full run agree.
- `hesapNo` is the card number (`10482`), not the account code; editor checks wait for the editor.

## Flake
`BalancesPage › combines column filters…` was vitest's default 5 s **test** timeout, not a `waitFor`: under v8
coverage on a loaded machine the grid tests take 3–7 s (this one 5.7 s, PartsPage up to 7.1 s), so raising
`asyncUtilTimeout` could not help. Reproduced under load (runs 8–9: 9 grid tests "timed out in 5000ms").
`testTimeout` 20 s: 20 / 20 full coverage runs green (338 tests) with the backend race suite running alongside.

## Gates
Backend: build, vet, golangci-lint 0, go-arch-lint, deadcode (Go 1.25) clean; `-race -count=3 -tags=integration`
green (router 143 s). Frontend: eslint 0 errors (7 older warnings), tsc, prettier, knip, depcruise clean, vitest
354 / 354 three times, build ok.

## Open
- TR editor headers `İsk1…` / `İsk2…` are cut at 1440 px (older, also in the MSW shot).
- A reservation cannot be expired in place (the guard admits only deletes), so "expired but not swept" is covered by
  the rule, not a test.
