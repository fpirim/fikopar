# 7. Return waybills and waybill billing

Status: Accepted · 2026-09-29

## Context
Returned goods arrive with a return waybill; the return invoice follows days later. The live system has
no return waybill, so the goods and the money move together or the stock is fixed by hand. Invoices
that bill a waybill used the same `linked_document_id` as returns and price differences, and "don't move
stock twice" was only a consumer rule.

## Decision
Two stock-only types: `SALES_RETURN_WAYBILL` (SIRI, IN) and `PURCHASE_RETURN_WAYBILL` (AIRI, OUT). They
never post to the ledger. Billing gets its own column, `waybill_id`: invoice → waybill, return → return
waybill, same sicil and depot, POSTED first, billed at most once. A document with `waybill_id` never moves
stock; a billed waybill cannot be cancelled until its bill is. Triggers on `documents` and
`stock_movements` enforce it (invariant D4). `linked_document_id` keeps its other meaning (original of a
return or price difference). Legacy `TIP` 8 and 11 map to the new types; `BAG` feeds either column.

## Consequences
The ledger and stock effects of every type are a lookup, not a consumer decision. A return can reference
both its original sale and its return waybill. The prefix list lives in one place (domain model) and the
`number_series` CHECK follows it.
