# 12. Contracts v1.3: F2 gaps

Status: Accepted · 2026-09-29

## Context
Phase 2 built catalog and stock against v1.2 without changing it. The gaps are listed in
[phase-2-contract-issues.md](../review/phase-2-contract-issues.md): 22 backend points where the contract was silent
and the backend chose a behaviour, and 10 frontend points where the UI worked around the contract.

## Decision
1. **Backend behaviour becomes the contract** (backend 1–22), as listed in the issue file. In particular:
   - Dashboard tiles: receivables = Σ positive balances of `120.*` accounts (count = those accounts), payables = Σ
     credit balances of `320.*`, as of `date`, depot-scoped with `depotId`; cash = the CASH role account per depot,
     depots without the role left out; `instrumentsDue30` = open receivable instruments due ≤ date + 30, overdue
     included; `dueThisWeek` = due ≤ date + 7, overdue first. Unknown `depotId`: dashboard 404, stock levels empty.
   - `finalizeStockCount uncounted=ZERO` writes `countedQty` 0 on the uncounted lines.
   - `occurred_at` and `started_at` are `clock_timestamp()`.
   - Movements: label from the source until documents (F3); opening 0 and closing through today without a date filter.
   - Price imports: 10 MiB → `400 ERR_FILE_TOO_LARGE` without a job; idempotency hash = file SHA-256 + form fields;
     a NEW row without a description is named by its stock no; LISAKT op 0 for an unknown part is OP_MISMATCH.
   - For catalog and stock the promoted migration `0008_catalog_stock` is the reference DDL; draft `0003` is superseded.
2. **Search** (frontend 1): each word of `q` matches the name or, normalized, a substring of the stock no or any code
   (MANUFACTURER, OEM, ALT, EQUIVALENT). Ranks 1–3 (whole `q` against codes) and rank 4 (words) are unchanged;
   rank-4 hits report the first code a word occurs in as `matchedCode`. `pump 37` finds HP-37xx pumps.
3. **Parts grid per depot** (frontend 2): `PartRow.stockByDepot[] {depotId, depotCode, onHand, available}` when the
   grid runs over all depots.
4. **Import mapping** (frontend 3): `PriceImportMappingWrite` (all optional, no schema defaults) for requests;
   `PriceImportMapping` is the effective mapping in responses with its resolved fields required; a mapping that
   cannot be resolved is null.
5. **Movements** (frontend 4, 5): a transfer stays two rows; `qtyIn` / `qtyOut` are number column ids and row fields.
6. **Equivalents** (frontend 6): an EQUIVALENT code holds the linked part's stock no (set by the server);
   `getPart` returns `equivalents[] {partId, stockNo, name, catalogId, catalogName}`, either direction.
7. **Depot settings** (frontend 7): `setPartDepotSettings` is a PUT; an omitted field is cleared.
8. **Count line** (frontend 9): `GET …/counts/{countId}/lines/{lineId}` with ETag.
9. `gridPriceImportRows` `description` / `brand` use the file value, else the current value (frontend 10).
   `setPartPrice` 200 / 201 (frontend 8) stays.

## Consequences
The backend gains the multi-word code match, per-depot stock on the parts grid, `qtyIn` / `qtyOut` columns, part
equivalents, the count line GET and a null mapping for unresolvable imports. The frontend regenerates types and drops
its workarounds (client-side In / Out, grid re-read after a 412, local mapping write type).

F3 note (backend 5): the app role reads `stock_levels` only and the projection trigger is `SECURITY DEFINER`.
Reservations need a definer function or a column grant on `reserved`. F3 also replaces `last_purchase_cost()` and
fills the document fields of movements.

## Errata (2026-09-29, no API change)
- Codes and names fold the Turkish letters to ASCII before matching (ı İ i → I, ş → S, ğ → G, ü → U, ö → O,
  ç → C): `NormalizeCode("ısı")` is `ISI`, not `S`; `cig` finds "Çığ mili", `salter` finds "Şalter". Migration
  `0011` rebuilds `stock_no_norm`, `normalized_code` and `name_fold` with `fold_code()` / `fold_name()`. This widens
  "case- and Turkish-dotless-i-insensitive" to Turkish-letter-insensitive. Codes of one part and kind that now fold
  alike (ŞALTER / SALTER) keep the lowest id; the others are removed and recorded, spelling included, in
  `part_code_fold_duplicates` (kept id and code alongside). A write whose codes fold alike within one kind is
  `422 ERR_DUPLICATE_LINE` (`codes[i].code`).
- A second EQUIVALENT link to the same part in one `codes` list is `422 ERR_DUPLICATE_LINE`
  (`codes[i].equivalentPartId`) instead of being collapsed.
- `globalSearch` cards and document numbers match `q` literally; LIKE wildcards are escaped as in the parts group.
- `stock_levels`: the guard recognises the projection by its call stack, not by the `fikopar.stock_projection`
  setting any session could set; TRUNCATE is refused, for the owner too.
