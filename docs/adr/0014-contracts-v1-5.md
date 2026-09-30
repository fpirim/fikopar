# 14. Contracts v1.5: F3 gaps

Status: Accepted · 2026-09-29

## Context
Phase 3 built documents against v1.4 without changing it. The gaps are listed in
[phase-3-contract-issues.md](../review/phase-3-contract-issues.md): 21 backend points where the contract was silent or
read one way, and 11 frontend points the UI worked around.

## Decision
1. **Post and cancel write their effects in the same transaction** (backend 1–2). The journal entry and the stock
   movements of a post, and the reversal entry and the opposite movements of a cancel, are written by inline
   handlers in the post / cancel transaction, not by outbox consumers. `DocumentPosted` / `DocumentCancelled` are
   still written to the outbox for external consumers (the `DocumentExporter`). The post response carries
   `journalEntryId` (null for waybills and zero totals), the cancel response `reversalEntryId`, both responses the
   `NEGATIVE_STOCK` warnings of their movements. The credit balance is the card's current account over all depots
   and dates; the ADR-0013 §9 term "documents whose ledger event is not delivered yet" is removed.
2. **Backend behaviour becomes the contract** (backend 3–21), as listed in the issue file:
   - Override: 403 only when the limit would block; an override that is not needed is ignored for everyone.
     `creditOverride.reason` 3–500 characters after trimming, else `400 ERR_INVALID_REQUEST` (`creditOverride.reason`),
     checked before the credit check.
   - Cancel date = the later of today (Europe/Istanbul) and the document date.
   - Links: a return or price difference without `linkedDocumentId` is `422 ERR_LINKED_DOCUMENT_INVALID`; a link on
     another type is `400 ERR_INVALID_REQUEST`; the original must be POSTED at save. A return billing a return
     waybill defaults its link to the waybill's original. A return may go to another depot of the company.
   - Every bill line needs `billedLineId` at save (same part); quantities are checked on post.
   - Update or delete of a draft that another draft bills or links: `409 ERR_DOCUMENT_HAS_DEPENDENTS`.
   - `effects.creditUsedPctAfter` on a POSTED document = current balance / limit, half-up; a posting without its role
     account is left out of `effects.ledger` (the post answers `422 ERR_ACCOUNT_ROLE_MISSING`).
   - `PRICE_BELOW_LIST` on SF, SF with waybill and SIR against `price_on(documentDate)`.
   - Relay: per-aggregate order for every aggregate; `outbox.seq` orders one transaction's events; a run re-reads
     the outbox after each round, at most 10.
   - `lock_stock_levels()` (definer) locks the levels a document touches in (part, depot) order before releasing,
     reserving or moving.
   - Document movement `unitCost` = net / qty, 4 decimals half-up.
   - `gridDocuments documentNo` is literal (`%`, `_`, `\` escaped) and case-insensitive.
   - `StockReserved` only from drafts that move stock OUT and have lines; lines holding nothing list `reserved` "0".
   - Sweeper: every 5 minutes, 500 reservations per run, levels locked in order first.
3. **Frontend proposals taken** (frontend 1–11):
   1. `SALES_DISCOUNTS` joins the `AccountRole` enum.
   2. `Document.hesapNo`: the card's account no.
   3. The card's salesperson is readable: `Sicil.sales_rep_user_id` (no longer planned; writable on create and update,
      a company member or `422 ERR_USER_NOT_FOUND`) with `sales_rep_name`, and `SicilSummary.salesRepUserId` /
      `salesRepName`. The editor uses it as the salesperson default.
   4. `gridDocuments billed` (true / false): waybills only, billed by a non-cancelled document or not.
   5. `gridDocuments billsWaybill` (true / false): documents that bill a waybill or not (return originals: false).
   6. `ERR_DOCUMENT_HAS_DEPENDENTS` carries `details.dependents [{id, documentNo, type}]` (`DocumentDependents`).
   7. `NEGATIVE_STOCK` warnings carry `stockNo`; every document warning with a part does.
   8. `STOCK_SHORT`: a line is short when on hand − reserved by other documents < qty. What the draft holds, expired
      or not, does not count against it, so an expired reservation never makes a line look short. `available` is
      that value; lines of one part report the same value (backend 10).
   9. `Part` / `PartRow.lastPurchaseCost {amount, date, documentNo}`, null without a posted purchase invoice
      (LAST_PURCHASE branch of `last_purchase_cost()` as of today).
   10. `Dashboard.latest` is removed; screens take latest documents from `gridDocuments` (backend 18 lapses).
   11. The waybill line copy before the first save is client-side: the editor reads the waybill and sends its lines
       with `billedLineId`. No preview endpoint.

## Consequences
The ledger and stock document consumers leave the relay; the relay keeps delivering document events to the exporter.
`PendingLedgerDocuments` goes. The backend adds the two grid filters, `hesapNo`, the card salesperson, dependents in
the error, `stockNo` on warnings, `lastPurchaseCost` and the enum value, and drops `Dashboard.latest`. The frontend
regenerates types and drops its workarounds (role fallback in the seed, summary account code, "Card default",
billed-waybill filtering by 409, original filtering by `waybillDocumentNo`, dependents re-read, stock no from the
lines, its own short-line check).

## Erratum (2026-09-30)
- `Sicil.iskonto2` is no longer planned: writable on create and update (0–100, else `400 ERR_INVALID_ISKONTO`) like
  `iskonto`, and the default of a new line's `discount2Pct`, as the card defaults of §3.3 already assumed.
