# Screens → API

Operation IDs from [`api/openapi.yaml`](../api/openapi.yaml). All data paths are under `/api/orgs/{orgId}`.
The top-bar depot switch sets `depotId` on every call that takes it. Grids use the AG Grid Community
infinite model; Excel comes from the server (`Accept: …spreadsheetml.sheet`), CSV from the client.

| Screen | Operations | Notes |
|---|---|---|
| Login | `login`, `loginDemo`, `getHealth` | Build hash from `getHealth.version`. EN/TR is client state. |
| Sidebar, top bar | `getMe`, `listDepots`, `globalSearch` | Company switch lists `getMe.organizations`. Search groups accounts, parts, document numbers. |
| Dashboard | `getDashboard`, `gridDocuments`, `listStockLevels` | One call for the four tiles, latest activity, due this week, below minimum / negative (top 10, negative first, `belowMinCount` / `negativeCount`), ledger check. "Show all" = `listStockLevels` `only=BELOW_MIN_OR_NEGATIVE`. |
| Balances | `gridBalances`, `printStatements`, `printReminderLetters` | As-of date, type segment and column filters (city, balance, overdue, last move) in one request; pinned row = `totals`, "6 of 412" = `totalCount` / `unfilteredCount`. |
| Statement | `getAccountStatement`, `getSicilSummary`, `getSicil`, `updateSicil` | Brought-forward row per depot, depot totals, Excel/PDF via Accept. Credit panel from the summary. |
| Invoice | `createDocument`, `updateDocument`, `getDocument`, `postDocument`, `cancelDocument`, `searchCatalog`, `resolvePartCodes`, `getSicilSummary`, `gridSicil`, `gridDocuments`, `listNumberSeries` | 11 types, return waybills included. An invoice or return can bill a posted waybill (`waybillDocumentId`) and then posts to the ledger only. Line stock, totals, VAT breakdown, "on post" effects and stock warnings come back with the draft. F2 popup = `searchCatalog`: code / OEM / ALT / equivalent / name, stock of every depot, list price of the part's catalog on the document date, equivalents, keyset paging. Post and cancel return `warnings` NEGATIVE_STOCK (toast, not a block). CSV import parsed in the client, codes via `resolvePartCodes`. Number shown on a draft is `nextValue`, allocated on post. |
| Receipt | `createReceipt`, `getReceipt`, `voidReceipt`, `gridReceipts`, `getSicilSummary`, `listAccounts`, `listNumberSeries` | Methods cash / bank / cheque / note; cheque and note register the instrument in the same call. "Post & print" = create, then PDF. |
| Cheques & notes | `gridInstruments`, `getInstrumentSummary`, `getInstrument`, `listInstrumentEvents`, `operateInstrument`, `createInstrument` | Tabs and counts from the summary. Collect takes an amount (partial). "Returned by bank" = `BOUNCE`. History carries receipt numbers. |
| Cash book | `getCashBook`, `createCashVoucher`, `voidCashVoucher`, `createCashCount`, `listCashCounts` | Opening / in / out / closing in the response. Across a carried 1 January the OPENING line has `resetsBalance` and the running balance restarts there. Expense = GDR voucher, bank deposit = VRM voucher. |
| Parts & stock | `gridParts`, `createPart`, `getPart`, `updatePart`, `lookupPartByCode`, `getPartStock`, `setPartDepotSettings`, `getPartPriceHistory`, `setPartPrice`, `gridStockMovements`, `transferStock`, `getStockTransfer`, `listCatalogs` | Grid: floating filters per `PartColId`, sort, infinite paging, Excel (server) / CSV (client); depot switch scopes the stock columns, All depots adds one on-hand column per depot (`stockByDepot`); `stock` = In stock / Below min / Negative. Card: codes (OEM, ALT, equivalents), per-depot on hand, reserved, available, min, max, shelf (`If-Match` per depot row, `"0"` creates). Price history with future prices; new price with a date. Movements with running on-hand (part + date sort) and Excel. Transfer returns `warnings`. |
| Catalogs & price import | `listCatalogs`, `createCatalog`, `updateCatalog`, `listPrices`, `setPrices`, `listPriceImports`, `createPriceImport`, `getPriceImport`, `updatePriceImport`, `gridPriceImportRows`, `applyPriceImport`, `discardPriceImport`, `listPriceImportPresets` | Upload CSV / XLSX / LISAKT, map columns (headers + sample when NEEDS_MAPPING), preview tabs New / Price / Attributes / Missing / Errors from `counts` and `change`, apply with `If-Match` (job version) and an `Idempotency-Key`; ERR_IMPORT_STALE → refresh with `updatePriceImport {}`. |
| Stock count | `listStockCounts`, `openStockCount`, `getStockCount`, `gridStockCountLines`, `setStockCountLine`, `getStockCountLine`, `addStockCountLine`, `finalizeStockCount`, `cancelStockCount` | One open session per depot; system quantity frozen at start (`startedAt`). Autosave per line with its own ETag; a 412 re-reads the line (`getStockCountLine`). Filters: shelf range, "Differences only" / "Not counted" / "Counted" = `only`. `movedSinceOpen` flags lines to recount. Count sheet PDF via Accept (`sheet` BLIND / WITH_SYSTEM). Post asks SKIP or ZERO for uncounted lines and returns `warnings`. Value = difference × last purchase cost; `costSource` LIST_PRICE / NONE lines are flagged. |
| Carry forward | `getCarryForward`, `setCarryForwardOverride`, `deleteCarryForwardOverride`, `postCarryForward`, `reopenCarryForward`, `listOpeningBalances` | Steps (`steps[]`), KPIs and READY / SKIP / EDITED rows in the preview; overrides send `If-Match` (preview version); preview PDF via Accept. |
| Journal entry | `getJournalEntry`, `getJournalEntryTrail`, `reverseJournalEntry`, `listJournalEntries` | Trail = audit, outbox, deliveries, stock. Owned entries answer 409 with the owner. |
| Migration | `listMigrationRuns`, `getMigrationRun`, `startMigrationRun`, `listQuarantine`, `getCopybookLayout` | Report PDF/XLSX via Accept. Demo allows dry runs only. |

PDFs: invoice (`getDocument`), receipt (`getReceipt`), statement (`getAccountStatement`, `printStatements`),
reminder letters, count sheet (`getStockCount`), cash book, carry-forward preview, migration report.

Carry forward step STOCK_COUNTS: blocked by an OPEN count dated in the closing year (`STOCK_COUNTS_POSTED`);
`YEAR_END_COUNTS` is a warning.

Not covered: the Order mockup (B2B order with reservations) is outside the 13 approved screens; its
contract comes with F3. `reserved` is already in the stock responses.
