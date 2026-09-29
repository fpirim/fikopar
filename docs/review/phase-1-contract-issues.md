# Phase 1 — contract issues

Contracts v1 were not changed in phase 1. All items are resolved in v1.1 ([ADR-0010](../adr/0010-contracts-v1-1.md)).

## Backend

| # | Where | Gap | F1 behaviour | Status |
|---|---|---|---|---|
| 1 | `createDepot` | No `ERR_DEPOT_CODE_EXISTS` | `409 ERR_INVALID_REQUEST` | resolved in v1.1 |
| 2 | `updateNumberSeries` | No 404 and no `ERR_NUMBER_SERIES_NOT_FOUND` | `400 ERR_INVALID_REQUEST` with `detail` | resolved in v1.1 |
| 3 | `createReceipt` CHEQUE / NOTE | No "not implemented" code | `400 ERR_INVALID_REQUEST`, `field: method` until F4 | resolved in v1.1 |
| 4 | Unknown account / depot / sicil in a request body | Endpoints list 422, codes are `ERR_*_NOT_FOUND` | `422 ERR_*_NOT_FOUND`; unknown ids in the path stay 404 | resolved in v1.1 |
| 5 | `setCarryForwardOverride` | No 400 listed | Validation errors answer 400 (`ERR_INVALID_AMOUNT`, `ERR_INVALID_REQUEST`) | resolved in v1.1 |
| 6 | Closed years | No code for posting into a year that was carried forward | Accepted; the next year's openings do not change (openings are fixed at post time) | resolved in v1.1 |
| 7 | `getAccountStatement` across a carried year | Brought-forward is defined for `from` only | Lines exclude opening entries; with an override, `closingBalance` can differ from the as-of balance by the override delta | resolved in v1.1 |
| 8 | `LedgerEntryPosted` | One `currency` / `total` per entry | Multi-currency entries report the first posting's currency | resolved in v1.1 |
| 9 | `Receipt.receiptNo` example `TAH20261000042` | `NumberSeries` says `TAH-2026-1-000042` | Dashed format everywhere | resolved in v1.1 |
| 10 | Carry-forward checks `NO_DRAFT_DOCUMENTS`, `STOCK_COUNTS_POSTED` | Documents and stock arrive in F2/F3 | Always `ok: true` in F1 | resolved in v1.1 |

## Frontend

| # | Where | Issue | Frontend today | Proposal | Status |
|---|---|---|---|---|---|
| 1 | `gridBalances` | Description lists columns `name, debit, credit`; `BalanceRow` has `accountName, debitTotal, creditTotal`. Keys of `totals` are not named. | Sends colIds `accountCode, name, city, sicilTipi, debit, credit, balance, overdue, lastMoveDate`; reads `totals.debit/credit/balance/overdue`. | State the colIds and the `totals` keys explicitly. | resolved in v1.1 |
| 2 | `gridReceipts` | No `depotId`, although the top-bar depot applies to every call. | Sends `filterModel.depotId = {number, equals}`. | Add `depotId` to the request body, as `gridBalances` has. | resolved in v1.1 |
| 3 | `listAccounts` | No code-prefix or role filter. Bank (102) and expense (770) pickers must page through every ASSET / EXPENSE account. | Follows `nextCursor` (limit 200) and filters by prefix in the client. | `codePrefix` query, or `role=BANK/EXPENSE`. | resolved in v1.1 |
| 4 | `globalSearch` | Account hits carry `sicilId` only; the statement needs `accountId`. | One extra `getSicilSummary` call per click. | Add `accountId` to account hits. | resolved in v1.1 |
| 5 | `reverseJournalEntry` 409 | "Owned entries answer 409 with the owner", but `Error` has no owner field and `detail` is not for users. | Shows the translated code and the owner number from `getJournalEntry.sourceNo`. | Structured `owner {sourceType, sourceId, sourceNo}` on `ERR_ENTRY_OWNED_BY_SOURCE`. | resolved in v1.1 |
| 6 | `JournalEntry` | `reversedByEntryId` / `reversesEntryId` without numbers. | Derives the number from the id (`J-YEAR-` + zero-padded id). Wrong if ids and counters diverge. | Add `reversedByEntryNo` and `reversesEntryNo`. | resolved in v1.1 |
| 7 | `TrailItem` | `actorUserId` only; the approved design shows a login (`a.kaya`). | Shows `user 12`. | Add `actorLogin` or `actorName`. | resolved in v1.1 |
| 8 | `updateSicil` | Conventions say updates carry `If-Match`, but `Sicil` has no version/ETag. Two editors overwrite each other. | Plain PUT from the statement card dialog. | ETag on `getSicil`, `If-Match` on `updateSicil`. | resolved in v1.1 |
| 9 | `Sicil.contacts`, `credit_limit`, `emails` | Marked "planned"; the statement card (contact line) and credit panel depend on them. | Uses `contacts[0]` and `getSicilSummary.creditLimit`; shows `—` when absent. | Promote to v1 fields or drop from the card. | resolved in v1.1 |
| 10 | `getCashBook` | `depotId` is required; the top bar also offers "All depots". | Falls back to the first depot and names it in the subtitle. | Either keep (one till per depot) and say so, or allow a combined book. | resolved in v1.1 |
| 11 | `setCarryForwardOverride` / `deleteCarryForwardOverride` | Return the whole preview without a `depotId` filter, and have no `If-Match` / version. | Discards the returned preview and refetches with the depot. | Accept `depotId`; add a preview version for concurrent edits. | resolved in v1.1 |
| 12 | `CarryForward.checks` | Four check codes map onto the four design steps only loosely (`LEDGER_BALANCED` = "Account balances", `YEAR_NOT_OPENED` = "Open {year}"). | Steps 1–2 follow their checks; 3–4 are done when `status = POSTED`; a failing blocking check marks its step. | Document the step mapping, or return `steps[]`. | resolved in v1.1 |
| 13 | `createReceipt` | Cheque / note need `instrument`; the design shows both options. | Visible but disabled until F4. | — (F4). | resolved in v1.1 |
| 14 | Date filters | AG Grid date models are `yyyy-mm-dd hh:mm:ss`; the contract says `yyyy-mm-dd`. | Trims to the date before sending. | — (note for the backend: accept only the date part). | resolved in v1.1 |
