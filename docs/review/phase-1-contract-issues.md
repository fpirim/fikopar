# Phase 1 — contract issues found by the frontend

Contracts v1 were not changed. Each item says what the F1 frontend does today and what a v1.1 could say.

| # | Where | Issue | Frontend today | Proposal |
|---|---|---|---|---|
| 1 | `gridBalances` | Description lists columns `name, debit, credit`; `BalanceRow` has `accountName, debitTotal, creditTotal`. Keys of `totals` are not named. | Sends colIds `accountCode, name, city, sicilTipi, debit, credit, balance, overdue, lastMoveDate`; reads `totals.debit/credit/balance/overdue`. | State the colIds and the `totals` keys explicitly. |
| 2 | `gridReceipts` | No `depotId`, although the top-bar depot applies to every call. | Sends `filterModel.depotId = {number, equals}`. | Add `depotId` to the request body, as `gridBalances` has. |
| 3 | `listAccounts` | No code-prefix or role filter. Bank (102) and expense (770) pickers must page through every ASSET / EXPENSE account. | Follows `nextCursor` (limit 200) and filters by prefix in the client. | `codePrefix` query, or `role=BANK/EXPENSE`. |
| 4 | `globalSearch` | Account hits carry `sicilId` only; the statement needs `accountId`. | One extra `getSicilSummary` call per click. | Add `accountId` to account hits. |
| 5 | `reverseJournalEntry` 409 | "Owned entries answer 409 with the owner", but `Error` has no owner field and `detail` is not for users. | Shows the translated code and the owner number from `getJournalEntry.sourceNo`. | Structured `owner {sourceType, sourceId, sourceNo}` on `ERR_ENTRY_OWNED_BY_SOURCE`. |
| 6 | `JournalEntry` | `reversedByEntryId` / `reversesEntryId` without numbers. | Derives the number from the id (`J-YEAR-` + zero-padded id). Wrong if ids and counters diverge. | Add `reversedByEntryNo` and `reversesEntryNo`. |
| 7 | `TrailItem` | `actorUserId` only; the approved design shows a login (`a.kaya`). | Shows `user 12`. | Add `actorLogin` or `actorName`. |
| 8 | `updateSicil` | Conventions say updates carry `If-Match`, but `Sicil` has no version/ETag. Two editors overwrite each other. | Plain PUT from the statement card dialog. | ETag on `getSicil`, `If-Match` on `updateSicil`. |
| 9 | `Sicil.contacts`, `credit_limit`, `emails` | Marked "planned"; the statement card (contact line) and credit panel depend on them. | Uses `contacts[0]` and `getSicilSummary.creditLimit`; shows `—` when absent. | Promote to v1 fields or drop from the card. |
| 10 | `getCashBook` | `depotId` is required; the top bar also offers "All depots". | Falls back to the first depot and names it in the subtitle. | Either keep (one till per depot) and say so, or allow a combined book. |
| 11 | `setCarryForwardOverride` / `deleteCarryForwardOverride` | Return the whole preview without a `depotId` filter, and have no `If-Match` / version. | Discards the returned preview and refetches with the depot. | Accept `depotId`; add a preview version for concurrent edits. |
| 12 | `CarryForward.checks` | Four check codes map onto the four design steps only loosely (`LEDGER_BALANCED` = "Account balances", `YEAR_NOT_OPENED` = "Open {year}"). | Steps 1–2 follow their checks; 3–4 are done when `status = POSTED`; a failing blocking check marks its step. | Document the step mapping, or return `steps[]`. |
| 13 | `createReceipt` | Cheque / note need `instrument`; the design shows both options. | Visible but disabled until F4. | — (F4). |
| 14 | Date filters | AG Grid date models are `yyyy-mm-dd hh:mm:ss`; the contract says `yyyy-mm-dd`. | Trims to the date before sending. | — (note for the backend: accept only the date part). |
