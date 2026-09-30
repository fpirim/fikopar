# Phase 3 — contract issues

Contracts v1.4 were not changed in phase 3. All items are resolved in v1.5 ([ADR-0014](../adr/0014-contracts-v1-5.md)).

## Backend

| # | Where | Gap | F3 behaviour | Status |
|---|---|---|---|---|
| 1 | `postDocument`, `cancelDocument` | "The ledger entry and the stock movements follow from the event"; the post and cancel responses must carry `NEGATIVE_STOCK` from the movements | The ledger and stock consumers run inline in the post / cancel transaction on the event just written and record their deliveries; the relay later publishes the event without handling it twice. `journalEntryId` is set in the post response (null for waybills and zero totals) | resolved in v1.5: inline in the same transaction |
| 2 | Credit balance (ADR-0013 §9) | "documents whose ledger event is not delivered yet" | Derived from state: posted documents without their entry plus cancelled ones whose entry is not reversed. Always zero while the ledger consumer runs inline (#1) | resolved in v1.5: term removed |
| 3 | `postDocument creditOverride` | Non-admin sending an override | 403 only when the limit would block; an override that is not needed is ignored for everyone | resolved in v1.5 as documented |
| 4 | `postDocument creditOverride.reason` | Validation error of the reason | 400 ERR_INVALID_REQUEST `field` `creditOverride.reason` (3–500 characters, trimmed), checked before the credit check | resolved in v1.5 as documented |
| 5 | `cancelDocument` on a future-dated document | Cancel date = today, but `cancel_date ≥ document_date` | The cancel date is the later of today (Istanbul, UTC+3) and the document date | resolved in v1.5 as documented |
| 6 | Returns and price differences without `linkedDocumentId` | Only the price-direction rules are listed as 400 | Missing link: 422 ERR_LINKED_DOCUMENT_INVALID `field` `linkedDocumentId`; a link on another type: 400 `linkedDocumentId`. The original must be POSTED already at save | resolved in v1.5 as documented |
| 7 | Return billing a return waybill without `linkedDocumentId` | Not stated | Defaults to the waybill's original | resolved in v1.5 as documented |
| 8 | Bill lines without `billedLineId` | Contract checks line-for-line billing on post | Save needs `billedLineId` on every line of a bill (422 ERR_WAYBILL_INVALID `lines[i].billedLineId`, same part); quantities are checked on post | resolved in v1.5 as documented |
| 9 | Delete or update of a draft that another draft references | A draft bill may reference a draft waybill's lines (FK) | 409 ERR_DOCUMENT_HAS_DEPENDENTS | resolved in v1.5 as documented |
| 10 | `STOCK_SHORT.available` with several lines of one part | "available without this draft" | On hand − reserved + everything this draft holds of the part; every short line of that part reports the same value | resolved in v1.5 as documented |
| 11 | `effects.creditUsedPctAfter` on a POSTED document | "what posting did" | Current balance / limit (the document is in the balance already), rounded half-up | resolved in v1.5 as documented |
| 12 | `effects.ledger` with a missing role | Required `accountCode` | The line is left out; the post answers 422 ERR_ACCOUNT_ROLE_MISSING | resolved in v1.5 as documented |
| 13 | `PRICE_BELOW_LIST` "sales types" | Which types | SF, SF with waybill, SIR (not returns, not price differences), against `price_on(documentDate)` | resolved in v1.5 as documented |
| 14 | Returns and depots | Only price differences need the original's depot | A return may go to another depot of the company | resolved in v1.5 as documented |
| 15 | Relay order | ADR-0013 §12 states per-document order | Applied to every aggregate; `outbox.seq` (0013) orders the events of one transaction, whose `occurred_at` is equal. A run re-reads the outbox after each round (at most 10) so a released event is delivered in the same run | resolved in v1.5 as documented |
| 16 | Stock level locking | Not in the DDL contract | `lock_stock_levels()` (0013, `SECURITY DEFINER`) locks the levels a document touches in (part, depot) order before releasing, reserving or moving; without it opposite part orders deadlock | resolved in v1.5 as documented |
| 17 | Movement `unitCost` of document lines | Not stated | net / qty, 4 decimals half-up | resolved in v1.5 as documented |
| 18 | Dashboard `latest` | Latest documents come from `gridDocuments` | `latest` also lists posted and cancelled documents as `DOCUMENT` items (amount = grand total) | lapsed: `Dashboard.latest` removed (frontend 10) |
| 19 | `gridDocuments documentNo` | "Substring of the number, literal" | Literal (`%`, `_`, `\` escaped), case-insensitive | resolved in v1.5 as documented |
| 20 | `StockReserved` | Emitted on every save | Only by drafts that move stock OUT and have lines; lines holding nothing are listed with `reserved` "0" | resolved in v1.5 as documented |
| 21 | Sweeper | Batch size | 500 reservations per run, every 5 minutes; the rows' levels are locked in order first | resolved in v1.5 as documented |

## Frontend

| # | Where | Gap | F3 frontend behaviour | Proposal | Status |
|---|---|---|---|---|---|
| 1 | `AccountRole.role` | ADR-0013 §2 posts sales discounts and FF decreases on a sale to `SALES_DISCOUNTS` (611), but the enum has no such role | Real seed sends the role and falls back to the list without it when the PUT is refused | Add `SALES_DISCOUNTS` to the enum | resolved in v1.5 as proposed |
| 2 | `Document` | No `hesapNo` / card account code (the summary row has `hesapNo`) | Editor shows the code from `getSicilSummary.accountCode` | Add `hesapNo` to `Document` | resolved in v1.5 as proposed |
| 3 | `SicilSummary` | The card's salesperson (default of `salesRepUserId`) is not readable; `Sicil.sales_rep_user_id` is "planned" | Picker offers "Card default" (sends null) until the saved draft returns the name | Add `salesRepUserId` / `salesRepName` to `SicilSummary` | resolved in v1.5: `Sicil.sales_rep_user_id`, `SicilSummary.salesRepUserId` |
| 4 | `gridDocuments` | No way to list only waybills that are not billed yet | Waybill picker lists posted waybills of the card and depot; a billed one answers `409 ERR_WAYBILL_BILLED` | Add `billed: boolean` filter or `billedByDocumentNo` to `DocumentSummary` | resolved in v1.5: `billed` filter |
| 5 | `gridDocuments` | Originals of a return must not be bills; there is no filter for that | Picker drops rows with `waybillDocumentNo` | Add a `billsWaybill` filter, or document the proxy | resolved in v1.5: `billsWaybill` filter |
| 6 | `cancelDocument` `409 ERR_DOCUMENT_HAS_DEPENDENTS` | The error carries no dependents | The dialog re-reads the document (`dependentDocumentIds`) and each dependent for its number | `details.dependents: [{id, documentNo}]`, or numbers next to `dependentDocumentIds` | resolved in v1.5: `details.dependents [{id, documentNo, type}]` |
| 7 | `Document.warnings` `NEGATIVE_STOCK` | Only `partId` (the `StockWarning` of transfers has `stockNo`) | Toast takes the stock no from the document lines | Add `stockNo` | resolved in v1.5 as proposed |
| 8 | `Document.warnings` `STOCK_SHORT` | "a line that holds less than its qty": after the 48 h expiry every line holds 0 and reads as short even with stock | MSW flags a line when available (without the draft) < qty, the `available` field's meaning; the editor shows the server's list when the draft is saved and its own check while it is being edited | State the rule by `available`, not by the hold | resolved in v1.5: short when on hand − reserved by others < qty |
| 9 | `Part`, `PartRow` | No last purchase cost (only count lines `unitCost` / `costSource` and movement `unitCost`) | Counts show a unit cost column (muted and explained when not LAST_PURCHASE); movements show the cost column | Add `lastPurchaseCost` / `costSource` to `getPart` | resolved in v1.5: `lastPurchaseCost {amount, date, documentNo}` |
| 10 | `Dashboard.latest` | Still in the schema; screens.md takes latest documents from `gridDocuments` | Dashboard uses `gridDocuments` (`statuses` POSTED + CANCELLED, `postedAt` desc, 0–10) | Deprecate `Dashboard.latest` or define it as receipts only | resolved in v1.5: `latest` removed |
| 11 | `createDocument` with `waybillDocumentId` | Lines copied on the server when `lines` is empty; the client has no preview of those lines before create | Editor reads the waybill and copies its lines with `billedLineId`, so the grid shows them before the first save | None; noted | resolved in v1.5: documented, no endpoint |

