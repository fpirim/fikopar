# 4. Transactional outbox for domain events

Status: Proposed · 2026-09-29

## Context
Posting an invoice must produce a ledger entry and stock movements; an instrument operation must
issue a receipt and an entry. Doing both inline couples contexts; publishing to a broker after commit can lose events.

## Decision
Producers insert the event into an `outbox` table in the same transaction as the state change. An
in-process relay reads unpublished rows with `FOR UPDATE SKIP LOCKED` and delivers them at least once.
Consumers are idempotent: ledger entries are unique on `(org_id, source_type, source_id)`, and stock movements
on `(source_type, source_id, kind)`, so a redelivered `DocumentPosted` cannot post twice. Events use a versioned envelope (`api/events`).

## Consequences
No dual-write gap and no broker to operate. Delivery is at-least-once, so every consumer must be
idempotent, and tests replay events to prove it. The envelope is broker-ready if one is added later.
