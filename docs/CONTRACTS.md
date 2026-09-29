# Contracts v1.1

Frozen 2026-09-29, git tag `contracts-v1.1` (v1: `contracts-v1`). Code is written against these; a change needs
an ADR in [`adr/`](adr/) and a new version here.

| Artifact | Path |
|---|---|
| Domain model, invariants, number prefixes | [`docs/domain-model.md`](domain-model.md) |
| HTTP API (OpenAPI 3.1) | [`api/openapi.yaml`](../api/openapi.yaml) |
| Event schemas (JSON Schema 2020-12) | [`api/events/`](../api/events) |
| Schema and DB invariants | [`backend/migrations/draft/`](../backend/migrations/draft) (`check/run.sh`) |
| Screens → operations | [`docs/screens.md`](screens.md) |
| Design tokens | [`frontend/src/styles/tokens.css`](../frontend/src/styles/tokens.css), [`docs/brand.md`](brand.md) |
| Decisions | [`docs/adr/0001`–`0010`](adr/) |

## Changelog

**v1.1** (2026-09-29, [ADR-0010](adr/0010-contracts-v1-1.md)) — resolves
[phase 1 contract issues](review/phase-1-contract-issues.md).
- Closed years: `409 ERR_YEAR_CLOSED`, `reopenCarryForward`, status `REOPENED`, carry-forward runs in DDL.
- Statements across a carried year (`resetsBalance`); TRY-only entries.
- New codes: `ERR_DEPOT_CODE_EXISTS`, `ERR_NUMBER_SERIES_NOT_FOUND`, `ERR_METHOD_NOT_AVAILABLE`,
  `ERR_YEAR_CLOSED`, `ERR_NOT_CARRIED_FORWARD`; `Error.owner`.
- Sicil `version` / ETag / `If-Match`; `contacts`, `emails`, `credit_limit` promoted.
- `gridBalances` colIds and totals, `gridReceipts.depotId`, `listAccounts` `codePrefix` / `role`,
  `globalSearch` `accountId`, entry numbers on reversals, `TrailItem.actorLogin`.
- Carry-forward preview version, `previewDepotId`, `steps[]`.
- Conventions: unknown ids (404 path / 422 body), date-only filters, dashed numbers.

**v1** (2026-09-29) — first freeze.
