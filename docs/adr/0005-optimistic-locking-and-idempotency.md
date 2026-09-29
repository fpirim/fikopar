# 5. Optimistic locking and idempotency keys

Status: Proposed · 2026-09-29

## Context
Several clerks edit the same documents and account cards. Mobile and flaky connections retry requests,
and a retried "collect cheque" must not post twice.

## Decision
Mutable aggregates carry a `version`. Reads return `ETag: "<version>"`; updates and state transitions
require `If-Match` (428 if missing, 412 if stale) and run `UPDATE … WHERE version = ?`. Unsafe POSTs
require an `Idempotency-Key`; the key is stored per org with a hash of the request, and the response
is replayed for an identical retry (422 if the body differs, 409 while the first attempt is in flight).

Document, receipt and instrument numbers come from a per-(company, depot, kind, year) counter row
updated with `UPDATE … RETURNING`, so concurrent posts never get the same number.

## Consequences
Lost updates become explicit conflicts the UI can resolve. Retries are safe end to end. Clients must
keep the ETag and generate a UUID per user action; keys expire after 24 hours.
