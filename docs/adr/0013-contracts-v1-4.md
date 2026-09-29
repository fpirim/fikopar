# 13. Contracts v1.4: documents (F3)

Status: Accepted · 2026-09-29

## Context
F3 builds the trade documents: invoices, waybills, returns, price differences. v1.3 has their tables as a draft,
a few endpoints and `DocumentPosted`, but no posting rules, rounding, reservations, credit check or read side.
Decisions from the plan: sales drafts reserve stock (released on post, cancel or after 48 h idle); a credit limit
blocks the post unless an admin overrides it with a reason; e-Fatura is out of scope, PDF only.

## Decision
1. **Types and lifecycle.** 11 types (9 + SIRI / AIRI), one prefix each (domain model). `DRAFT → POSTED → CANCELLED`.
   A draft is edited (`If-Match`) or deleted (`deleteDocument`); it is never cancelled. Post allocates the number and
   emits `DocumentPosted`. A posted document is never edited: cancel (`reason`) emits `DocumentCancelled`; the ledger
   consumer writes a reversal entry and the stock consumer opposite movements, both dated the cancel date (today).
2. **Ledger** (Tek Düzen Hesap Planı, periodic inventory; no cost of sales entry). G gross, D discount, N net,
   V VAT, T total; C = the card's current account (`120.` / `320.`). Accounts come from `account_roles`:

   | Type | Debit | Credit |
   |---|---|---|
   | SF, SF with waybill | C T; SALES_DISCOUNTS (611) D | SALES (600) G; OUTPUT_VAT (391) V |
   | SI sales return | SALES_RETURNS (610) N; OUTPUT_VAT V | C T |
   | AF, AF with waybill | PURCHASES (153) N; INPUT_VAT (191) V | C T |
   | AI purchase return | C T | PURCHASE_RETURNS (153) N; INPUT_VAT V |
   | FF INCREASE on a sale | C T | PRICE_DIFFERENCE (600) N; OUTPUT_VAT V |
   | FF DECREASE on a sale | SALES_DISCOUNTS (611) N; OUTPUT_VAT V | C T |
   | FF INCREASE on a purchase | PURCHASES N; INPUT_VAT V | C T |
   | FF DECREASE on a purchase | C T | PURCHASES N; INPUT_VAT V |
   | Waybills (SIR, AIR, SIRI, AIRI) | — | — |

   One entry per document, dated `documentDate`, in its depot; the C posting carries `dueDate`. Zero amounts are
   left out; a zero total posts no entry. A missing role is `422 ERR_ACCOUNT_ROLE_MISSING`. New role
   `SALES_DISCOUNTS`. Purchase discounts reduce cost (net to 153). A sales decrease is a discount after the sale,
   not a return (610 is for goods taken back), so it goes to 611; purchase differences adjust 153 net either way
   (periodic inventory: no stock revaluation).
3. **Stock.** OUT: SF, SF with waybill, SIR, AI, AIRI. IN: AF, AF with waybill, AIR, SI, SIRI. None: FF and any
   invoice or return that bills a waybill. One movement per line, `source = document line`, on `documentDate`;
   cancel writes the opposite kind once, on `cancelDate`.
4. **Waybill billing** (D4). An invoice or return bills at most one waybill of the matching type, card and depot,
   POSTED first, line for line (`billedLineId`, same part and quantity, every waybill line once; prices may differ).
   A waybill is billed by one non-cancelled document; cancelling the bill frees it. A billed waybill cannot be
   cancelled. With `waybillDocumentId` and no lines, create copies the waybill's lines.
5. **Returns** (D6). A return links its original (`linkedDocumentId`) and each line an original line
   (`originLineId`, same part). Originals are the documents that moved the goods: SF / SF with waybill / SIR (sales),
   AF / AF with waybill / AIR (purchases), not a bill. Per original line, Σ qty of posted goods-moving returns
   (return waybills and unbilled returns) ≤ original qty, else `422 ERR_RETURN_QTY_EXCEEDED`. A return that bills a
   return waybill takes its lines' origins and is not counted again. The original locks during the check.
   An original with posted returns or price differences cannot be cancelled: `409 ERR_DOCUMENT_HAS_DEPENDENTS`.
6. **Price difference.** Both directions: `priceDirection` `INCREASE` (sales: customer debited; purchase: supplier
   credited) or `DECREASE` (sales: customer credited; purchase: supplier debited), required on FF and only there,
   fixed once posted. Sales or purchase comes from the linked invoice: a posted SF / SF with waybill / AF / AF with
   waybill (with or without a billed waybill) of the same card and depot; the original locks during the check.
   Lines are amount only: qty 1, unit price = the difference (net), no discounts, VAT % applies (default the
   original line's; required without one). A line may name an original line (`originLineId`, its
   part is copied) or none (then no part, a `description` is printed). No stock. A DECREASE is capped:
   original net − Σ net of posted decreases (this one included) − Σ net of posted goods-moving returns of its lines
   (or of the lines of the waybill it bills) ≥ 0, else `422 ERR_PRICE_DIFFERENCE_EXCEEDED`; increases do not raise
   the cap. Cancel is the reversal like any document; a cancelled FF frees its amount. One prefix `FF` for both
   directions (one series, one legal document kind; the PDF title and the grid show the direction).
7. **Lines and rounding.** A line is a catalog part, qty, unit price (4 decimals), two cascaded discounts and VAT %
   (default: the part's; discounts default to the card's `iskonto` / `iskonto2`). Per line, half-up to 2 decimals:
   gross = round(qty × price); net = round(qty × price × (1 − d1) × (1 − d2)) from the unrounded product;
   discount = gross − net; VAT = round(net × VAT%). Document totals = Σ lines; the VAT breakdown sums lines per rate.
   The DB checks every line and, on post, the totals.
8. **Reservations** (D8). A draft that will move stock OUT holds per line `min(qty, available)` in its depot when
   saved; available = on hand − reserved. A shortfall is `STOCK_SHORT` on the draft, never a block. Every save
   replaces the draft's reservations and sets `expiresAt` = save + 48 h. Release: post (same transaction, before
   the movements), delete, line change, sweeper. `stock_levels.reserved` = Σ reservations, written only by the
   `SECURITY DEFINER` trigger `apply_stock_reservation()`, which the `stock_levels` guard admits for `reserved`
   only. A reservation never takes more than available; movements never check reservations (S1), so a later OUT
   can leave available negative. Sweeper: every 5 min, `release_expired_reservations(now, 500)` deletes expired rows
   (`FOR UPDATE SKIP LOCKED`) and emits one `StockReleased EXPIRED` per document in the same transaction; a row is
   deleted once, so reruns and concurrent runs release nothing twice. A post after expiry just moves the stock.
9. **Credit limit.** Posting SF, SF with waybill or an FF INCREASE on a sale, when the card has a limit and
   balance + T > limit, is `409 ERR_CREDIT_LIMIT_EXCEEDED` with `details {limit, balance, documentTotal, excess}`.
   balance = the current account's balance over all depots and dates plus the signed totals of the card's documents
   whose ledger event is not delivered yet. An admin posts anyway with `creditOverride {reason}` (non-admin: 403);
   the reason, user, time and excess are stored on the document and in an audit event `document.credit_override`.
10. **Due date and salesperson.** `dueDate` defaults to `documentDate` + the card's payment term (`gun`, days;
    blank or not a number = 0). Salesperson = a member of the company (`salesRepUserId`, default the card's
    `sales_rep_user_id`); `listMembers` feeds the picker. No separate entity.
11. **Concurrency.** `If-Match` on update, delete, post, cancel; `Idempotency-Key` on create, post, cancel. Create,
    update and post in a closed year, and cancel on a date in one, are `409 ERR_YEAR_CLOSED`. Carry-forward check
    `NO_DRAFT_DOCUMENTS` (blocking): no DRAFT dated on or before 31 December of Y − 1.
12. **Events.** `DocumentPosted` (lines with amounts and links), `DocumentCancelled`, `StockReserved`,
    `StockReleased` (`POSTED | DELETED | UPDATED | EXPIRED`); aggregate `document`. Per aggregate the relay delivers in
    outbox order: an event waits while an earlier one of the same document is unpublished.
13. **Last purchase cost.** `last_purchase_cost()` reads posted purchase invoices (bills of a purchase waybill
    included; returns, waybills, price differences, cancelled invoices ignored), then the list price, then NONE.
    Counts finalized from 0012 on value at LAST_PURCHASE where a purchase exists; finalized lines keep their snapshot.
14. **Read side.** `gridDocuments` (filters type, status, card, depot, date range, number; column filters; totals;
    XLSX), `getDocument` (JSON with per-depot stock, reservations, returned quantities, effects, warnings; PDF),
    dashboard latest documents, `documentId` / `documentType` on statement lines, and on movements the document
    fields F2 left null (`label`, `sourceNo`, `documentId`, `documentType`, `sicilName`, `reversal`).
15. **e-Fatura port.** No endpoint. The backend defines `DocumentExporter` (`Export(ctx, PostedDocument) (ExportRef,
    error)`, `Cancel(ctx, ExportRef, reason) error`), called by an outbox consumer on `DocumentPosted` /
    `DocumentCancelled`; F3 ships a no-op. A UBL-TR integrator plugs in later without API changes.

## Consequences
Draft DDL `0004_documents` (on top of 0001–0011) enforces lines, totals, billing, return caps, reservations and the
movement / entry rules; `check/mutations.sh` removes the billing-once, return-cap and available checks and the checks
fail. `DRAFT → CANCELLED` is gone. `ERR_CREDIT_LIMIT_EXCEEDED` moves from 422 to 409. Credit exposure ignores
cheques in portfolio. A price reduction after the sale is an FF DECREASE (amendment, 2026-09-29: it replaces the
earlier "increase only" decision); amounts stay non-negative and the direction picks the sides. Legacy codes 13 / 14
import as INCREASE, or DECREASE with absolute amounts when the legacy total is negative. `check/mutations.sh` M4
removes the decrease cap.
