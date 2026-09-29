# Screens → API

Operation IDs from [`api/openapi.yaml`](../api/openapi.yaml). All data paths are under `/api/orgs/{orgId}`.
The top-bar depot switch sets `depotId` on every call that takes it. Grids use the AG Grid Community
infinite model; Excel comes from the server (`Accept: …spreadsheetml.sheet`), CSV from the client.

| Screen | Operations | Notes |
|---|---|---|
| Login | `login`, `loginDemo`, `getHealth` | Build hash from `getHealth.version`. EN/TR is client state. |
| Sidebar, top bar | `getMe`, `listDepots`, `globalSearch` | Company switch lists `getMe.organizations`. Search groups accounts, parts, document numbers. |
| Dashboard | `getDashboard`, `gridDocuments` | One call for the four tiles, latest activity, due this week, below minimum, ledger check. |
| Balances | `gridBalances`, `printStatements`, `printReminderLetters` | As-of date, type segment and column filters (city, balance, overdue, last move) in one request; pinned row = `totals`, "6 of 412" = `totalCount` / `unfilteredCount`. |
| Statement | `getAccountStatement`, `getSicilSummary`, `getSicil`, `updateSicil` | Brought-forward row per depot, depot totals, Excel/PDF via Accept. Credit panel from the summary. |
| Invoice | `createDocument`, `updateDocument`, `getDocument`, `postDocument`, `cancelDocument`, `searchCatalog`, `resolvePartCodes`, `getSicilSummary`, `gridSicil`, `gridDocuments`, `listNumberSeries` | 11 types, return waybills included. An invoice or return can bill a posted waybill (`waybillDocumentId`) and then posts to the ledger only. Line stock, totals, VAT breakdown, "on post" effects and stock warnings come back with the draft. F2 popup = `searchCatalog` (both depots, list price). CSV import parsed in the client, codes via `resolvePartCodes`. Number shown on a draft is `nextValue`, allocated on post. |
| Receipt | `createReceipt`, `getReceipt`, `voidReceipt`, `gridReceipts`, `getSicilSummary`, `listAccounts`, `listNumberSeries` | Methods cash / bank / cheque / note; cheque and note register the instrument in the same call. "Post & print" = create, then PDF. |
| Cheques & notes | `gridInstruments`, `getInstrumentSummary`, `getInstrument`, `listInstrumentEvents`, `operateInstrument`, `createInstrument` | Tabs and counts from the summary. Collect takes an amount (partial). "Returned by bank" = `BOUNCE`. History carries receipt numbers. |
| Cash book | `getCashBook`, `createCashVoucher`, `voidCashVoucher`, `createCashCount`, `listCashCounts` | Opening / in / out / closing in the response. Expense = GDR voucher, bank deposit = VRM voucher. |
| Parts & stock | `getPart`, `updatePart`, `getPartStock`, `setPartDepotSettings`, `getPartPriceHistory`, `setPrices`, `gridStockMovements`, `transferStock` | Per-depot on hand, reserved, available, min, max, shelf. Movements with running on-hand and Excel. |
| Stock count | `listStockCounts`, `openStockCount`, `getStockCount`, `gridStockCountLines`, `setStockCountLine`, `addStockCountLine`, `finalizeStockCount` | Autosave per line with its own ETag. "Differences only" / "Not counted" = `only`. Count sheet PDF via Accept. Post asks SKIP or ZERO for uncounted lines. Value = difference × last purchase cost; `costSource` LIST_PRICE / NONE lines are flagged. |
| Carry forward | `getCarryForward`, `setCarryForwardOverride`, `deleteCarryForwardOverride`, `postCarryForward`, `listOpeningBalances` | Checklist, KPIs and READY / SKIP / EDITED rows in the preview; preview PDF via Accept. |
| Journal entry | `getJournalEntry`, `getJournalEntryTrail`, `reverseJournalEntry`, `listJournalEntries` | Trail = audit, outbox, deliveries, stock. Owned entries answer 409 with the owner. |
| Migration | `listMigrationRuns`, `getMigrationRun`, `startMigrationRun`, `listQuarantine`, `getCopybookLayout` | Report PDF/XLSX via Accept. Demo allows dry runs only. |

PDFs: invoice (`getDocument`), receipt (`getReceipt`), statement (`getAccountStatement`, `printStatements`),
reminder letters, count sheet (`getStockCount`), cash book, carry-forward preview, migration report.

Not covered: the Order mockup (B2B order with reservations) is outside the 13 approved screens; its
contract comes with F3. `reserved` is already in the stock responses.
