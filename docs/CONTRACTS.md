# Contracts v1

Frozen 2026-09-29, git tag `contracts-v1`. Code is written against these; a change needs an ADR in
[`adr/`](adr/) and a new version here.

| Artifact | Path |
|---|---|
| Domain model, invariants, number prefixes | [`docs/domain-model.md`](domain-model.md) |
| HTTP API (OpenAPI 3.1) | [`api/openapi.yaml`](../api/openapi.yaml) |
| Event schemas (JSON Schema 2020-12) | [`api/events/`](../api/events) |
| Schema and DB invariants | [`backend/migrations/draft/`](../backend/migrations/draft) (`check/run.sh`) |
| Screens → operations | [`docs/screens.md`](screens.md) |
| Design tokens | [`frontend/src/styles/tokens.css`](../frontend/src/styles/tokens.css), [`docs/brand.md`](brand.md) |
| Decisions | [`docs/adr/0001`–`0009`](adr/) |
