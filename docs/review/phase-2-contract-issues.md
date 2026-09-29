# Phase 2 — contract issues

Contracts v1.2 were not changed in phase 2. The backend behaviour for each gap is listed; a later version decides.

## Backend

| # | Where | Gap | F2 behaviour |
|---|---|---|---|
| 1 | `last_purchase_cost()`, count valuation | Documents arrive in F3, so there is no purchase invoice line to read | Function has the LIST_PRICE and NONE branches only; F3 replaces it with the draft `0004_documents` version |
| 2 | `gridStockMovements` | `label` SALE / PURCHASE / returns, `sicilName`, `documentId`, `documentType` come from documents | Label from `source_type` (TRANSFER, COUNT, OPENING for legacy) and kind; document fields null |
| 3 | `createPart.initialPrice` | `PriceChanged` is specified for `setPartPrice` / `setPrices` only | The initial price is a MANUAL row and emits `PriceChanged` too |
| 4 | `price_imports`, `price_import_rows` DDL | API fields `headers`, `sample`, `unmappedTargets`, `failureDetail`, `changedFields`, `priceChangePct` have no draft columns | Columns added in `0008_catalog_stock` |
| 5 | `stock_levels` privileges | Draft grants the app role every table | App role reads `stock_levels` only; the projection trigger is `SECURITY DEFINER`. F3 reservations need a definer function or a column grant on `reserved` |
| 6 | `occurred_at`, `started_at` defaults | Draft uses `now()` (transaction start) | `clock_timestamp()`, so the freeze instant orders movements inside long transactions. A movement committed after `startedAt` but stamped before it counts as before the freeze |
| 7 | Carry-forward step STOCK_COUNTS | "← STOCK_COUNTS_POSTED and YEAR_END_COUNTS"; a failing warning's effect on the step is not stated | Step follows the blocking check; YEAR_END_COUNTS is shown but does not keep the step from DONE |
| 8 | `getDashboard` tiles | Receivables, payables, cash and instruments are not defined | Receivables = Σ positive balances of `120.*` accounts (count = those accounts), payables = Σ credit balances of `320.*`, as of `date`, depot-scoped with `depotId`; cash = CASH role account per depot (depots without the role left out); `instrumentsDue30` = open receivable instruments due ≤ date + 30, overdue included; `dueThisWeek` = due ≤ date + 7, overdue first |
| 9 | `getDashboard depotId`, `listStockLevels depotId` | Unknown ids in a query string are not covered by the conventions (path 404, body 422) | Dashboard answers 404 ERR_DEPOT_NOT_FOUND; stock levels return an empty list |
| 10 | `finalizeStockCount uncounted=ZERO` | Whether uncounted lines keep `countedQty` null | Lines get `countedQty` 0 at finalize, so they show their difference afterwards |
| 11 | `gridStockMovements` opening / closing | Defined for a `date` filter range | Without a date filter: opening 0, closing = on hand through today |
| 12 | `gridParts unfilteredCount` | Which filters it ignores | Parts in the scope (`catalogId`, `includeInactive`) before column filters, `search` and `stock` |
| 13 | `gridParts stock=IN_STOCK` | Not defined | available > 0 in the scope (depot or all depots) |
| 14 | `globalSearch` parts group | Ranking and length limits vs `searchCatalog` | Same ranking as `searchCatalog`, `limit` per group; terms over 60 characters skip the part group |
| 15 | `searchCatalog` | A `q` without letters or digits (e.g. `--`) | Code match is skipped, the name match still runs |
| 16 | `gridPriceImportRows unfilteredCount` | Scope not stated | Rows of the selected tab before column filters |
| 17 | `PriceImportRow.changedFields` | Whether `price` is listed | PRICE rows list `price` |
| 18 | `createPriceImport` idempotency | What the request hash covers for multipart | File SHA-256 plus the form fields, so a retry with a new boundary replays |
| 19 | Import NEW part without description | `PartWrite.name` is required | Stock no becomes the name |
| 20 | LISAKT op 0 for an unknown part | Not stated | OP_MISMATCH warning, no change |
| 21 | Upload over 10 MiB | FAILED job vs error | `400 ERR_FILE_TOO_LARGE`, no job |
| 22 | CP857 decoding | `x/text` has no code page 857 | Own decoding table in `internal/pricefile` |
