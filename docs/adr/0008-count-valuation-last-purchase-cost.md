# 8. Stock count differences at last purchase cost

Status: Accepted · 2026-09-29

## Context
A count posts ADJUST movements; the report needs a money value per difference. There is no costing
engine (FIFO / average) and the business prices by list and last invoice.

## Decision
Value = difference × last purchase cost: `net_amount / qty` of the latest POSTED purchase invoice line
for the part in the company, any depot, on or before the count date. Fallback: default list price,
flagged `LIST_PRICE`; otherwise `NONE` with no value. `last_purchase_cost()` computes it; the line
stores cost and source at finalize.

## Consequences
Simple and explainable at the counter. Not an inventory valuation for accounts; a costing method can
replace the function later without changing the API (`unitCost`, `costSource`).
