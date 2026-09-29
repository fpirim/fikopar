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
All real suites (incl. `@v14`, `@v15`) twice in a row: 47 passed, 14 screenshot tests skipped; `e2e:real:v13` 35.
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

## Verification fixes (`f3/nits`)
- `listAuditEvents` (`GET /audit-events`) was in the contract with no route. Now: admin only (403 for members),
  scoped to the path company, filters `entityType`, `entityId`, `actorUserId`, `from` / `to` (RFC 3339, inclusive),
  newest first, keyset cursor over (occurredAt, id); `limit` clamped to 1–200 like the other lists, bad filters 400.
  The credit override event reads back with its actor and `after`.
- Reversal description: not a defect. The API writes `Reversal of J-…: <reason>`; the verifier's script appended
  ` reverses <reversesEntryNo>` when printing. Both paths (manual, document cancel) are now pinned by exact tests.
- `Sicil.iskonto2`: writable on create / update (0–100, `ERR_INVALID_ISKONTO`), on the card form, and the default of a
  new line's second discount ([ADR-0014 erratum](../adr/0014-contracts-v1-5.md#erratum-2026-09-30)).
- e2e dates: MSW specs run on a fixed day (`E2E_TODAY`, default 2026-09-29); the Due date is derived. Green with the
  default and with `E2E_TODAY=2026-11-17` (113 passed, 40 screenshot tests skipped). Real specs follow the real day.
- UI: editor discount headers and the FF direction toggle fit at 1440 / 1152 EN / TR; Parts and Balances drop
  columns below 1300 px (`useNarrow`) and no longer scroll sideways at 1152; the dialog close label is translated;
  Esc returns the focus to the opener (shared `DialogContent`). e2e cover all of these.
- `DialogShell` / `ConfirmDialog` sit on the shared Radix `DialogContent`: Esc, focus trap, focus back to the
  opener, translated close. Lists a dialog field portals to the body (`data-dialog-float`) stay clickable.
- Top bar: the search shrinks (280 → 160 px min) before it meets the depot switch; e2e checks the top bar parts
  apart at 1440 / 1152, EN / TR, on the dashboard, the FF editor and carry-forward.
- vitest `testTimeout` 10 s: 5 coverage runs and 3 plain runs 357 / 357, none skipped.

Gates: backend build, vet, golangci-lint 0, go-arch-lint, deadcode (Go 1.25) clean, `-race -count=3
-tags=integration` green; frontend eslint 0 errors, tsc, prettier, knip, depcruise, build clean, vitest 357 × 3;
MSW 125 (40 skipped); real all suites 47 (14 skipped); legacy 47; vitest 361.

## Open
- One vitest run (361 tests) had a single failure while a `docker compose --build` ran alongside; not reproduced in
  ten more runs, three of them with coverage beside the backend race suite.
