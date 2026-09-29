# Contracts v1.4

Frozen 2026-09-29, git tag `contracts-v1.4` (earlier: `contracts-v1.3`, `contracts-v1.2`, `contracts-v1.1`, `contracts-v1`). Code is written against these; a change needs
an ADR in [`adr/`](adr/) and a new version here.

| Artifact | Path |
|---|---|
| Domain model, invariants, number prefixes | [`docs/domain-model.md`](domain-model.md) |
| HTTP API (OpenAPI 3.1) | [`api/openapi.yaml`](../api/openapi.yaml) |
| Event schemas (JSON Schema 2020-12) | [`api/events/`](../api/events) |
| Schema and DB invariants | Promoted [`backend/migrations/0001`–`0011`](../backend/migrations) plus documents draft [`draft/0004_documents.sql`](../backend/migrations/draft/0004_documents.sql) (`check/run.sh`, `check/mutations.sh`) |
| Screens → operations | [`docs/screens.md`](screens.md) |
| Design tokens | [`frontend/src/styles/tokens.css`](../frontend/src/styles/tokens.css), [`docs/brand.md`](brand.md) |
| Decisions | [`docs/adr/0001`–`0013`](adr/) |

## Changelog

**v1.4** (2026-09-29, [ADR-0013](adr/0013-contracts-v1-4.md)) — F3 documents.
- Lifecycle DRAFT → POSTED → CANCELLED; drafts are deleted (`deleteDocument`), posted ones cancelled by reversal.
- Posting table on Tek Düzen accounts (600 / 610 / 611 / 153 / 191 / 391, role `SALES_DISCOUNTS`); stock direction
  per type; waybill billing line for line, once; returns capped per original line (`ERR_RETURN_QTY_EXCEEDED`);
  price differences re-price invoice lines, no stock; `ERR_DOCUMENT_HAS_DEPENDENTS`.
- Line rounding half-up per line, totals = Σ lines, checked in the DB; line `grossAmount`, `discountAmount`,
  `originLineId`, `billedLineId`, `stockByDepot`, `reservedQty`, `returnedQty`.
- Reservations for drafts that move stock OUT: 48 h, sweeper, `stock_reservations`, definer projection into
  `stock_levels.reserved`, never above available.
- Credit limit: `409 ERR_CREDIT_LIMIT_EXCEEDED` with `details`; admin `creditOverride {reason}` on post, audited.
- Due date from the card's term; salesperson = company member (`listMembers`).
- `gridDocuments` filters, `DocumentColId`, totals; document PDF; dashboard latest documents; `documentId` on
  statement lines; movement document fields and `reversal`; carry-forward `NO_DRAFT_DOCUMENTS`.
- Events `DocumentCancelled`, `StockReserved`, `StockReleased`; `DocumentPosted` line amounts and links.
- `last_purchase_cost()` reads posted purchase invoices. e-Fatura: `DocumentExporter` port only.

**v1.3** (2026-09-29, [ADR-0012](adr/0012-contracts-v1-3.md)) — resolves
[phase 2 contract issues](review/phase-2-contract-issues.md).
- F2 backend behaviour documented: dashboard tiles, unknown query `depotId`, count ZERO finalize, `clock_timestamp()`,
  movement labels and opening / closing, grid `unfilteredCount` and IN_STOCK, import limits and hashing.
- `searchCatalog`: each word matches the name or any normalized code.
- `PartRow.stockByDepot`, movements `qtyIn` / `qtyOut` column ids, `getPart.equivalents`, EQUIVALENT code = stock no.
- `PriceImportMappingWrite` for requests, effective `PriceImportMapping` in responses.
- `setPartDepotSettings` PUT semantics; `getStockCountLine`.

**v1.2** (2026-09-29, [ADR-0011](adr/0011-contracts-v1-2.md)) — F2 catalog and stock.
- Negative stock allowed: `warnings[]` NEGATIVE_STOCK on post / cancel / transfer / count finalize; `SignedQuantity`;
  NEGATIVE filters on stock levels, parts grid and dashboard. `allowNegativeStock`, `ERR_INSUFFICIENT_STOCK` removed.
- Catalog = price list (`catalogs`, `/catalog/catalogs`); part in one fixed catalog; append-only dated prices,
  `setPartPrice` (future dates), `ERR_PRICE_EXISTS`. `price_lists`, `priceListId` removed.
- Price list import jobs: upload, mapping, preview diff grid, apply with `If-Match` + `Idempotency-Key`, LISAKT preset.
- Parts grid column ids, ALT codes, movements `delta` / opening / closing, `getStockTransfer`, counts with frozen
  system quantity, `countDate`, `cancelStockCount`, `StockCountFinalized`, search ranking and paging.
- Carry forward: `STOCK_COUNTS_POSTED` defined, `YEAR_END_COUNTS`; stock follows closed years.
- Cash book: OPENING `resetsBalance` line across a carried year.
- Events: `PriceChanged`, `PriceListImported`; envelope `depotId` nullable.

**v1.1** (2026-09-29, [ADR-0010](adr/0010-contracts-v1-1.md)) — resolves
[phase 1 contract issues](review/phase-1-contract-issues.md).
- Closed years: `409 ERR_YEAR_CLOSED`, `reopenCarryForward`, status `REOPENED`, carry-forward runs in DDL.
- Statements across a carried year (`resetsBalance`); TRY-only entries.
- New codes: `ERR_DEPOT_CODE_EXISTS`, `ERR_NUMBER_SERIES_NOT_FOUND`, `ERR_METHOD_NOT_AVAILABLE`,
  `ERR_YEAR_CLOSED`, `ERR_NOT_CARRIED_FORWARD`; `Error.owner`.
- Sicil `version` / ETag / `If-Match`; `contacts`, `emails`, `credit_limit` promoted.
- `gridBalances` colIds and totals, `gridReceipts.depotId`, `listAccounts` `codePrefix` / `role`,
  `globalSearch` `accountId`, entry numbers on reversals, `TrailItem.actorLogin`.
- Carry-forward preview version, `previewDepotId`, `steps[]`.
- Conventions: unknown ids (404 path / 422 body), date-only filters, dashed numbers.

**v1** (2026-09-29) — first freeze.
