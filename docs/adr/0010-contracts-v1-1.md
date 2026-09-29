# 10. Contracts v1.1

Status: Accepted · 2026-09-29

## Context
F1 built the ledger, receipts and cash screens against contracts v1 and left 24 gaps in
[`review/phase-1-contract-issues.md`](../review/phase-1-contract-issues.md) (backend B1–B10, frontend F1–F14).
The largest is that posting into a year after its carry-forward was accepted and silently diverged from the
next year's openings.

## Decision
Backend
- B1 `createDepot`: `409 ERR_DEPOT_CODE_EXISTS`.
- B2 `updateNumberSeries`: `404 ERR_NUMBER_SERIES_NOT_FOUND`.
- B3 `createReceipt` CHEQUE / NOTE: `400 ERR_METHOD_NOT_AVAILABLE` (`field: method`) until F4.
- B4 Convention: unknown id in the path → 404, in the body → `422 ERR_*_NOT_FOUND` with `field`.
- B5 `setCarryForwardOverride` lists 400 (`ERR_INVALID_AMOUNT`, `ERR_INVALID_REQUEST`).
- B6 Closed years. The active carry-forward run into Y closes Y − 1 and earlier; any entry dated there is
  rejected with `409 ERR_YEAR_CLOSED` (DB trigger as backstop). New `reopenCarryForward` (admin, `If-Match`,
  reason) reverses the run's opening entries on 1 January and marks the run reopened; status `REOPENED`, then
  the year can be posted again. A later carried year must be reopened first. `409 ERR_NOT_CARRIED_FORWARD` if
  nothing is posted.
- B7 Statements: balance as of D starts at the latest active run ≤ D. A range crossing a carried 1 January shows
  the opening entries as `resetsBalance` lines; `closingBalance` = balance as of `to`.
- B8 One currency per entry, TRY only (`Currency` enum, CHECKs on accounts and postings, events `const`).
- B9 Dashed numbers everywhere (`TAH-2026-1-000042`).
- B10 `NO_DRAFT_DOCUMENTS` and `STOCK_COUNTS_POSTED` are always ok until F2 / F3.

Frontend
- F1 `gridBalances`: `BalanceColId` = `BalanceRow` names; `totals` is `BalanceTotals`.
- F2 `gridReceipts`: `depotId` in the body.
- F3 `listAccounts`: `codePrefix` and `role` (CASH 100, BANK 102, CUSTOMER 120, SUPPLIER 320, EXPENSE 770).
- F4 `globalSearch` account hits carry `accountId`.
- F5 `Error.owner {sourceType, sourceId, sourceNo}` with `ERR_ENTRY_OWNED_BY_SOURCE`.
- F6 `JournalEntry.reversesEntryNo`, `reversedByEntryNo`.
- F7 `TrailItem.actorLogin`.
- F8 `Sicil.version`; ETag on read, `If-Match` on `updateSicil` (412 / 428).
- F9 `Sicil.contacts[]`, `emails[]`, `credit_limit` are v1 fields, writable.
- F10 `getCashBook` stays one depot; "All depots" is not valid there, the UI asks for a depot.
- F11 Override endpoints take `previewDepotId` (`depotId` on DELETE already names the row) and `If-Match`
  against the preview version (`CarryForward.version`, ETag).
- F12 `CarryForward.steps[]` `{code, status}` for the four screen steps.
- F13 No change (F4).
- F14 Date parameters and grid date filters take `yyyy-mm-dd`; a time part is ignored.

## Consequences
Backend needs a migration to v1.1 DDL (sicil version, run reopen, voidable openings, closed-year trigger,
TRY CHECKs) and the new codes. Frontend regenerates types, renames balance colIds, sends `If-Match` on sicil and
override writes, and drops its workarounds (prefix paging, id → number, extra summary call). Events stay at
version 1; `currency` is narrowed to TRY.
