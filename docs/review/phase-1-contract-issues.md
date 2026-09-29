# Phase 1 — contract issues

Contracts v1 were not changed. Where the contract is silent, F1 chose the behaviour below; each
item needs an ADR or a v2 decision.

| # | Where | Gap | F1 behaviour |
|---|---|---|---|
| 1 | `createDepot` | No `ERR_DEPOT_CODE_EXISTS` | `409 ERR_INVALID_REQUEST` |
| 2 | `updateNumberSeries` | No 404 and no `ERR_NUMBER_SERIES_NOT_FOUND` | `400 ERR_INVALID_REQUEST` with `detail` |
| 3 | `createReceipt` CHEQUE / NOTE | No "not implemented" code | `400 ERR_INVALID_REQUEST`, `field: method` until F4 |
| 4 | Unknown account / depot / sicil in a request body | Endpoints list 422, codes are `ERR_*_NOT_FOUND` | `422 ERR_*_NOT_FOUND`; unknown ids in the path stay 404 |
| 5 | `setCarryForwardOverride` | No 400 listed | Validation errors answer 400 (`ERR_INVALID_AMOUNT`, `ERR_INVALID_REQUEST`) |
| 6 | Closed years | No code for posting into a year that was carried forward | Accepted; the next year's openings do not change (openings are fixed at post time) |
| 7 | `getAccountStatement` across a carried year | Brought-forward is defined for `from` only | Lines exclude opening entries; with an override, `closingBalance` can differ from the as-of balance by the override delta |
| 8 | `LedgerEntryPosted` | One `currency` / `total` per entry | Multi-currency entries report the first posting's currency |
| 9 | `Receipt.receiptNo` example `TAH20261000042` | `NumberSeries` says `TAH-2026-1-000042` | Dashed format everywhere |
| 10 | Carry-forward checks `NO_DRAFT_DOCUMENTS`, `STOCK_COUNTS_POSTED` | Documents and stock arrive in F2/F3 | Always `ok: true` in F1 |
