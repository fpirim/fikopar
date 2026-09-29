# 11. Contracts v1.2: catalog and stock

Status: Accepted · 2026-09-29

## Context
F2 builds catalog and stock. Decisions taken for it (FIKOPAR-PLAN, "F2 kararları"): negative stock is allowed,
a catalog is a price list with dated prices, supplier lists are imported with a previewed diff. v1.1 still had a
non-negative trigger, a separate `price_lists` table with a default list, a count that froze the system quantity at
finalize, and two carry-forward details left open (STOCK_COUNTS_POSTED, cash book across a carried year).

## Decision
1. **Negative stock.** No check on `on_hand`; `depots.allow_negative_stock` and `Depot.allowNegativeStock` are dropped,
   `ERR_INSUFFICIENT_STOCK` removed. Document post/cancel, `transferStock` and `finalizeStockCount` return
   `warnings[]` `{code: NEGATIVE_STOCK, partId, depotId, onHandAfter}`. `SignedQuantity` for on hand, available,
   differences. `listStockLevels only=NEGATIVE|BELOW_MIN|BELOW_MIN_OR_NEGATIVE`, `gridParts stock=…`, dashboard
   `belowMin` list (negative first) with `belowMinCount` / `negativeCount`.
2. **Catalog = price list.** `catalogs` replaces `price_lists`; `parts.catalog_id` is required and fixed. `prices`
   keyed by part, one row per `valid_from`, append-only; price on D = latest `valid_from` ≤ D (`price_on()`), so
   periods cannot overlap. `setPartPrice` / `setPrices` take any date, future included; a different price on an
   existing date is `409 ERR_PRICE_EXISTS`. Count fallback (ADR-0008) reads the part's catalog price.
3. **Price list import.** `createPriceImport` (multipart CSV / XLSX / LISAKT text, 10 MiB, 100k rows) → job
   PARSING → NEEDS_MAPPING | READY → preview rows (NEW, PRICE, ATTRIBUTES, UNCHANGED, MISSING, DELETE, per-row errors
   and warnings) → `applyPriceImport` (`If-Match` job version, `Idempotency-Key`, one transaction, refused when the
   catalog's `content_version` moved: `ERR_IMPORT_STALE`) → result. Mapping targets: code, description, brand, type,
   manufacturer / OEM / alt codes, price, validFrom, packQty, VAT. LISAKT is a fixed-width preset
   (domain-model, "LISAKT preset"). Parts are never deleted; MISSING / DELETE deactivate.
4. **F2 surface.** Part codes gain `ALT`. `gridParts` column ids (`PartColId`), depot-scoped stock columns, Excel.
   Per-depot settings upsert with `If-Match "0"`. Movements grid: `delta`, running on-hand in (date, id) order,
   opening / closing on hand. Transfers: one transaction, `getStockTransfer`, closed years apply. Counts: one OPEN per
   depot, `countDate`, `systemQty` frozen at `startedAt` (`on_hand_at()`), one line per part, line version +1 per
   save (DB-enforced), `only` + shelf range filters, `movedSinceOpen`, `cancelStockCount`, sheet PDF BLIND /
   WITH_SYSTEM, finalize returns `StockCountFinalized`. `searchCatalog`: rank exact / prefix / substring / name,
   stock of every depot, catalog price on date, equivalents, keyset paging, p95 < 100 ms at 50k parts.
5. **Carry forward.** `STOCK_COUNTS_POSTED` (blocking) = no OPEN count dated ≤ 31 December of Y − 1.
   New `YEAR_END_COUNTS` (warning) = every depot has a FINALIZED count in Y − 1. Stock movements dated in a closed year
   are rejected (`ERR_YEAR_CLOSED`, trigger).
6. **Cash book across a carried year** (L9): the CASH opening entry is an OPENING line on 1 January with
   `resetsBalance`; the running balance restarts there; totals leave it out; closing = balance as of `to`.

Events: `PriceChanged` (manual prices), `PriceListImported` (one per applied import), envelope `depotId` nullable for
company-wide events, `StockMoved.onHandAfter` may be negative.

## Consequences
F2 backend adds a migration: drop `depots.allow_negative_stock` (and the field in the depot model), then the draft
`0003_catalog_stock` and the `last_purchase_cost()` change. Stock levels are maintained by a trigger, not by the
service. Dashboard and cash book responses gain required fields; the frontend regenerates types. The import keeps
files in the database until apply, discard or expiry (7 days). Imports touch the catalog row once per price, so
concurrent writes to one catalog serialize; acceptable at this volume.
