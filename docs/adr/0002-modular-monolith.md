# 2. Modular monolith, not microservices

Status: Proposed · 2026-09-29

## Context
One team, one Postgres, two companies with two depots each. Document posting, stock movements and
ledger entries must change atomically; splitting them across services would need sagas for what is a single
transaction today.

## Decision
One Go binary with one package per bounded context (`identity`, `sicil`, `ledger`, `documents`,
`catalog`, `stock`, `receipts`, `instruments`, `audit`). Contexts call each other only through their application service interfaces or
through outbox events; no cross-context table access. Boundaries are enforced in CI with `go-arch-lint`.

## Consequences
Cross-context invariants stay in one ACID transaction. Deployment is a single image. If a context
later needs to scale or ship independently, its event contract (`api/events`) and outbox already make
extraction a packaging change rather than a redesign.
