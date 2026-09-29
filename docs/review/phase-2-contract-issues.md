# Phase 2 — contract issues

Contracts v1.2 were not changed in phase 2. All items are resolved in v1.3 ([ADR-0012](../adr/0012-contracts-v1-3.md)):
the backend behaviour is the contract, the frontend proposals are taken as noted.

## Backend

| # | Where | Gap | F2 behaviour | Status |
|---|---|---|---|---|
| 1 | `last_purchase_cost()`, count valuation | Documents arrive in F3, so there is no purchase invoice line to read | Function has the LIST_PRICE and NONE branches only; F3 replaces it with the draft `0004_documents` version | resolved in v1.3 |
| 2 | `gridStockMovements` | `label` SALE / PURCHASE / returns, `sicilName`, `documentId`, `documentType` come from documents | Label from `source_type` (TRANSFER, COUNT, OPENING for legacy) and kind; document fields null | resolved in v1.3 |
| 3 | `createPart.initialPrice` | `PriceChanged` is specified for `setPartPrice` / `setPrices` only | The initial price is a MANUAL row and emits `PriceChanged` too | resolved in v1.3 |
| 4 | `price_imports`, `price_import_rows` DDL | API fields `headers`, `sample`, `unmappedTargets`, `failureDetail`, `changedFields`, `priceChangePct` have no draft columns | Columns added in `0008_catalog_stock` | resolved in v1.3 |
| 5 | `stock_levels` privileges | Draft grants the app role every table | App role reads `stock_levels` only; the projection trigger is `SECURITY DEFINER`. F3 reservations need a definer function or a column grant on `reserved` | resolved in v1.3; F3 note in ADR-0012 |
| 6 | `occurred_at`, `started_at` defaults | Draft uses `now()` (transaction start) | `clock_timestamp()`, so the freeze instant orders movements inside long transactions. A movement committed after `startedAt` but stamped before it counts as before the freeze | resolved in v1.3 |
| 7 | Carry-forward step STOCK_COUNTS | "← STOCK_COUNTS_POSTED and YEAR_END_COUNTS"; a failing warning's effect on the step is not stated | Step follows the blocking check; YEAR_END_COUNTS is shown but does not keep the step from DONE | resolved in v1.3 |
| 8 | `getDashboard` tiles | Receivables, payables, cash and instruments are not defined | Receivables = Σ positive balances of `120.*` accounts (count = those accounts), payables = Σ credit balances of `320.*`, as of `date`, depot-scoped with `depotId`; cash = CASH role account per depot (depots without the role left out); `instrumentsDue30` = open receivable instruments due ≤ date + 30, overdue included; `dueThisWeek` = due ≤ date + 7, overdue first | resolved in v1.3 |
| 9 | `getDashboard depotId`, `listStockLevels depotId` | Unknown ids in a query string are not covered by the conventions (path 404, body 422) | Dashboard answers 404 ERR_DEPOT_NOT_FOUND; stock levels return an empty list | resolved in v1.3 |
| 10 | `finalizeStockCount uncounted=ZERO` | Whether uncounted lines keep `countedQty` null | Lines get `countedQty` 0 at finalize, so they show their difference afterwards | resolved in v1.3 |
| 11 | `gridStockMovements` opening / closing | Defined for a `date` filter range | Without a date filter: opening 0, closing = on hand through today | resolved in v1.3 |
| 12 | `gridParts unfilteredCount` | Which filters it ignores | Parts in the scope (`catalogId`, `includeInactive`) before column filters, `search` and `stock` | resolved in v1.3 |
| 13 | `gridParts stock=IN_STOCK` | Not defined | available > 0 in the scope (depot or all depots) | resolved in v1.3 |
| 14 | `globalSearch` parts group | Ranking and length limits vs `searchCatalog` | Same ranking as `searchCatalog`, `limit` per group; terms over 60 characters skip the part group | resolved in v1.3 |
| 15 | `searchCatalog` | A `q` without letters or digits (e.g. `--`) | Code match is skipped, the name match still runs | resolved in v1.3 |
| 16 | `gridPriceImportRows unfilteredCount` | Scope not stated | Rows of the selected tab before column filters | resolved in v1.3 |
| 17 | `PriceImportRow.changedFields` | Whether `price` is listed | PRICE rows list `price` | resolved in v1.3 |
| 18 | `createPriceImport` idempotency | What the request hash covers for multipart | File SHA-256 plus the form fields, so a retry with a new boundary replays | resolved in v1.3 |
| 19 | Import NEW part without description | `PartWrite.name` is required | Stock no becomes the name | resolved in v1.3 |
| 20 | LISAKT op 0 for an unknown part | Not stated | OP_MISMATCH warning, no change | resolved in v1.3 |
| 21 | Upload over 10 MiB | FAILED job vs error | `400 ERR_FILE_TOO_LARGE`, no job | resolved in v1.3 |
| 22 | CP857 decoding | `x/text` has no code page 857 | Own decoding table in `internal/pricefile` | resolved in v1.3 |

## Frontend
Against `contracts-v1.2`. The frontend works around each one; none blocks F2.

| # | Where | Issue | Frontend now | Proposal | Status |
|---|---|---|---|---|---|
| 1 | `searchCatalog` | Words are matched against the name only; the invoice mockup query `pump 37` mixes a name word and a code fragment and would find nothing. | MSW matches every word against the name or any normalized code; rank 4. | Each word matches the name or a normalized code (stock no, MANUFACTURER, OEM, ALT, EQUIVALENT); rank unchanged. | resolved in v1.3 |
| 2 | `gridParts` | A row carries stock for one scope (a depot or the sum); the parts list cannot show every depot side by side. | Stock columns follow the top-bar depot (screens.md). | Optional `stockByDepot[]` (`depotId`, `onHand`, `available`) on `PartRow`. | resolved in v1.3 |
| 3 | `PriceImportMapping`, `PriceImportColumn` | Defaults (`encoding`, `decimalSeparator`, `scale`, …) make the generated response types require them, so the same schema cannot type a request body. | `MappingWrite` in `api/types.ts` (all optional). | Split `PriceImportMappingWrite` from the effective mapping in the response. | resolved in v1.3 |
| 4 | `gridStockMovements` | A transfer is two rows (TRANSFER_OUT, TRANSFER_IN); mockup 09 shows one row `1 → 2` with in and out. | Two rows, depot column `1 → 2` on both. | None needed; noted for the mockup. | no change (v1.3) |
| 5 | `gridStockMovements` | No In / Out column ids; direction filters only as `delta`. | In and Out derived in the client, not filterable or sortable. | Allow `qtyIn` / `qtyOut` column ids (number filters). | resolved in v1.3 |
| 6 | `PartCode` EQUIVALENT | Unclear what `code` holds (the other part's stock no?); no name or catalog of the linked part. | Card shows the code as a link to `equivalentPartId`. | Say `code` = linked stock no; `getPart` adds `equivalents[]` like `CatalogHit`. | resolved in v1.3 |
| 7 | `setPartDepotSettings` | Not said whether an omitted `maxQty` / `shelf` keeps or clears the value. | The UI always sends all three fields. | PUT semantics: omitted = cleared (null / empty). | resolved in v1.3 |
| 8 | `setPartPrice` | 200 (same price) and 201 (new) carry the same body. | Both treated as saved. | None needed. | no change (v1.3) |
| 9 | Count lines | No GET for one line; after a 412 the line is re-read through `gridStockCountLines` filtered by stock no. | Grid re-read. | `GET …/counts/{countId}/lines/{lineId}` with ETag. | resolved in v1.3 |
| 10 | `gridPriceImportRows` | Column ids `description` / `brand` do not say whether they filter the file value or the catalog value. | MSW filters the file value, falling back to current. | State "file value, else current". | resolved in v1.3 |
