# Phase 3 — contract issues

Contracts v1.4 were not changed in phase 3. The frontend works around each gap as noted.

## Frontend

| # | Where | Gap | F3 frontend behaviour | Proposal |
|---|---|---|---|---|
| 1 | `AccountRole.role` | ADR-0013 §2 posts sales discounts and FF decreases on a sale to `SALES_DISCOUNTS` (611), but the enum has no such role | Real seed sends the role and falls back to the list without it when the PUT is refused | Add `SALES_DISCOUNTS` to the enum |
| 2 | `Document` | No `hesapNo` / card account code (the summary row has `hesapNo`) | Editor shows the code from `getSicilSummary.accountCode` | Add `hesapNo` to `Document` |
| 3 | `SicilSummary` | The card's salesperson (default of `salesRepUserId`) is not readable; `Sicil.sales_rep_user_id` is "planned" | Picker offers "Card default" (sends null) until the saved draft returns the name | Add `salesRepUserId` / `salesRepName` to `SicilSummary` |
| 4 | `gridDocuments` | No way to list only waybills that are not billed yet | Waybill picker lists posted waybills of the card and depot; a billed one answers `409 ERR_WAYBILL_BILLED` | Add `billed: boolean` filter or `billedByDocumentNo` to `DocumentSummary` |
| 5 | `gridDocuments` | Originals of a return must not be bills; there is no filter for that | Picker drops rows with `waybillDocumentNo` | Add a `billsWaybill` filter, or document the proxy |
| 6 | `cancelDocument` `409 ERR_DOCUMENT_HAS_DEPENDENTS` | The error carries no dependents | The dialog re-reads the document (`dependentDocumentIds`) and each dependent for its number | `details.dependents: [{id, documentNo}]`, or numbers next to `dependentDocumentIds` |
| 7 | `Document.warnings` `NEGATIVE_STOCK` | Only `partId` (the `StockWarning` of transfers has `stockNo`) | Toast takes the stock no from the document lines | Add `stockNo` |
| 8 | `Document.warnings` `STOCK_SHORT` | "a line that holds less than its qty": after the 48 h expiry every line holds 0 and reads as short even with stock | MSW flags a line when available (without the draft) < qty, the `available` field's meaning; the editor shows the server's list when the draft is saved and its own check while it is being edited | State the rule by `available`, not by the hold |
| 9 | `Part`, `PartRow` | No last purchase cost (only count lines `unitCost` / `costSource` and movement `unitCost`) | Counts show a unit cost column (muted and explained when not LAST_PURCHASE); movements show the cost column | Add `lastPurchaseCost` / `costSource` to `getPart` |
| 10 | `Dashboard.latest` | Still in the schema; screens.md takes latest documents from `gridDocuments` | Dashboard uses `gridDocuments` (`statuses` POSTED + CANCELLED, `postedAt` desc, 0–10) | Deprecate `Dashboard.latest` or define it as receipts only |
| 11 | `createDocument` with `waybillDocumentId` | Lines copied on the server when `lines` is empty; the client has no preview of those lines before create | Editor reads the waybill and copies its lines with `billedLineId`, so the grid shows them before the first save | None; noted |
