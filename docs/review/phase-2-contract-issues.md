# Phase 2 — contract issues found by the frontend

Against `contracts-v1.2`. The frontend works around each one; none blocks F2.

| # | Where | Issue | Frontend now | Proposal |
|---|---|---|---|---|
| 1 | `searchCatalog` | Words are matched against the name only; the invoice mockup query `pump 37` mixes a name word and a code fragment and would find nothing. | MSW matches every word against the name or any normalized code; rank 4. | Each word matches the name or a normalized code (stock no, MANUFACTURER, OEM, ALT, EQUIVALENT); rank unchanged. |
| 2 | `gridParts` | A row carries stock for one scope (a depot or the sum); the parts list cannot show every depot side by side. | Stock columns follow the top-bar depot (screens.md). | Optional `stockByDepot[]` (`depotId`, `onHand`, `available`) on `PartRow`. |
| 3 | `PriceImportMapping`, `PriceImportColumn` | Defaults (`encoding`, `decimalSeparator`, `scale`, …) make the generated response types require them, so the same schema cannot type a request body. | `MappingWrite` in `api/types.ts` (all optional). | Split `PriceImportMappingWrite` from the effective mapping in the response. |
| 4 | `gridStockMovements` | A transfer is two rows (TRANSFER_OUT, TRANSFER_IN); mockup 09 shows one row `1 → 2` with in and out. | Two rows, depot column `1 → 2` on both. | None needed; noted for the mockup. |
| 5 | `gridStockMovements` | No In / Out column ids; direction filters only as `delta`. | In and Out derived in the client, not filterable or sortable. | Allow `qtyIn` / `qtyOut` column ids (number filters). |
| 6 | `PartCode` EQUIVALENT | Unclear what `code` holds (the other part's stock no?); no name or catalog of the linked part. | Card shows the code as a link to `equivalentPartId`. | Say `code` = linked stock no; `getPart` adds `equivalents[]` like `CatalogHit`. |
| 7 | `setPartDepotSettings` | Not said whether an omitted `maxQty` / `shelf` keeps or clears the value. | The UI always sends all three fields. | PUT semantics: omitted = cleared (null / empty). |
| 8 | `setPartPrice` | 200 (same price) and 201 (new) carry the same body. | Both treated as saved. | None needed. |
| 9 | Count lines | No GET for one line; after a 412 the line is re-read through `gridStockCountLines` filtered by stock no. | Grid re-read. | `GET …/counts/{countId}/lines/{lineId}` with ETag. |
| 10 | `gridPriceImportRows` | Column ids `description` / `brand` do not say whether they filter the file value or the catalog value. | MSW filters the file value, falling back to current. | State "file value, else current". |
