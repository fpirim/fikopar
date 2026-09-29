# Phase 3 — frontend (documents)

Branch `f3/frontend` from `contracts-v1.4`. Mockup: `Invoice.dc.html` (05), `Main.dc.html` latest documents,
`Account.dc.html` links. Screenshots: [`phase-3-screens/`](phase-3-screens) (`npm run screens`, MSW, 1440×900).

## Delivered
- 05 Invoice (`/invoices/new?type=…`, `/invoices/:id`): header (11 types, FF direction, depot codes, dates, due
  from the card's term until typed, account, salesperson, waybill ref. / original picker), AG Grid lines with row
  numbers, focus and edit outlines, Ins / Del / Enter / Tab / F2, part cell over the shared `CatalogSearch` (opens
  below the line), per-depot stock with colours and tooltip, D1 / D2 / VAT / net, pinned totals; side panel totals,
  VAT per rate, "On post" (card ±, stock per depot, credit used after); STOCK / PRICE / CREDIT warnings and the 48 h
  hold; FF amount-only lines (original line, orig. net, description, amount, VAT); return lines capped with "Left".
- Lifecycle: create (`Idempotency-Key`), save (`If-Match`, 412 → reload dialog), delete, post (`Idempotency-Key` +
  `If-Match`), credit limit dialog (limit, balance, total, excess; admin "Post anyway" with a reason), line errors
  on their row, closed year hint, cancel with reason (reversal link, dependents listed), PDF, "Bill this waybill",
  "Create return", "Price difference", CSV import through `resolvePartCodes`.
- Documents list (`/invoices`, sidebar "Invoices & returns"): type (multi), status, account, date range, number,
  floating column filters, pinned totals without cancelled rows, Excel (server), CSV (client), row → editor,
  "New document" by type; account no. and net drop below 1300 px so the grid never scrolls sideways.
- Dashboard latest documents from `gridDocuments`; statement lines, part movements and global search open the
  document; movement reversals marked; unit cost on count lines and movements.
- MSW: `documentHandlers` (posting rules, reservations, return and decrease caps, billing once, credit limit,
  cancel with opposite movements, number series, last purchase cost for counts), `resolvePartCodes`.
- Real seed: roles for the posting table, credit limits, a purchase, a sale, a waybill and its bill, a return, an
  FF increase and decrease, a draft over the limit; `e2e/real/documents.spec.ts` tagged `@v14` (`npm run e2e:real:v14`).

## Mockup comparison
Layout, header grid, toolbar, grid columns, pinned row, side panel and warning line follow 05. Differences: a
draft's heading is the next number of its series (`SF-2026-1-000419`); the mockup's gross / discount (44,070 /
−3,470) do not add up to its lines, the screen shows Σ lines (43,750 / −3,150); the popup sits under the edited line.

## Gates
eslint 0 errors (7 older warnings), tsc, prettier, knip, depcruise clean; vitest 338/338 three times (all lines
92 %, branches 81 %; changed files ≥ 80 % lines); build ok; MSW e2e 95 passed (incl. keyboard entry of an invoice,
no-truncation and no sideways scroll EN / TR at 1440 and 1152).

## Open
- Real e2e `@v14` not run: the F3 backend is not merged yet.
- Contract gaps: [phase-3-contract-issues.md](phase-3-contract-issues.md).
