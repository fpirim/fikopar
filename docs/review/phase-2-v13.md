# Phase 2 — contracts v1.3

Branch `f2/v13` from `f2/integration`; contracts tagged `contracts-v1.3` ([ADR-0012](../adr/0012-contracts-v1-3.md)).
Frontend built in parallel on `f2/v13-fe` and merged.

## Delivered
- Backend: word search (each word in the name or a code, `matchedCode` for rank 4), `PartRow.stockByDepot`,
  movements `qtyIn` / `qtyOut`, `getPart.equivalents`, EQUIVALENT code = linked stock no, `getStockCountLine`,
  effective or null import mapping. Search ranks the candidates once for page and total.
- Migrations: `0009_equivalent_codes` (links unique per linked part, codes follow a stock no change),
  `0010_stock_levels_guard` (only the movement projection writes `stock_levels`, owner included).
- Verification fixes: name-only search (`--`, `çığ`, `%%`) answered 500; company separation per match branch;
  deterministic transfer lock-order test; UI count parsing per language, 1152 px toolbars, focus after Esc, TR label.
- Frontend: regenerated types, per-depot columns, server In / Out filters, equivalents card, line re-read after 412,
  write / effective mapping types; workarounds removed.

## Mutation checks
| Guard broken | Result |
|---|---|
| Words AND → OR | `SearchWordsMatchNameOrCode`, `RanksExactPrefixSubstringName` fail |
| Words match the name only / no stock no / no part codes | `SearchWordsMatchNameOrCode` fails |
| Matched code order (kind, stock no first) | `SearchWordsMatchNameOrCode` fails |
| Org filter dropped in the stock no, code or word branch | `SearchKeepsCompaniesApart` fails |
| Transfer sort by part / by depot removed | `TransferLocksLevelsInPartDepotOrder` fails 10/10 each |
| `stock_levels_guard` lets writes through | `StockLevelsAreWrittenOnlyByTheProjection` fails |

Org filter on the word-branch code subquery: equivalent mutation (the outer `parts.org_id` filter holds).

## Performance
`TestCatalogSearch_P95` (50 000 parts, 200 000 codes): p50 5.3 ms, p95 23.0 ms (v1.2: 8.5 / 18.8 ms).

## Gates
`go test -race -count=3 -timeout 30m -tags=integration ./...` green; golangci-lint 0 issues; go-arch-lint and deadcode
(Go 1.25) clean. Frontend: tsc, prettier, knip, depcruise clean, eslint 0 errors (7 older warnings), vitest 276/276
three times, build ok. MSW e2e 82 passed; real API all suites 35 passed (6 screenshot tests skipped); legacy e2e 47.
