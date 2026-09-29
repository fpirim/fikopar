# 1. Modernize by strangling, verify by parallel run

Status: Proposed · 2026-09-29

## Context
The business has run on five generations since 2010 (RM/COBOL, .NET, PHP B2B, Java Swing, JHipster).
Every rewrite so far was a cutover, and each one left data that only the old system could explain.
The COBOL files still hold the longest history of balances and instruments.

## Decision
Replace one bounded context at a time behind the new API (strangler fig). Legacy data comes in through
a repeatable importer (copybook → fixed-width parser → Postgres), and each import runs a
reconciliation report: record counts per file, per-account Σ debit − Σ credit, cash, open instruments
and stock on hand must match the source exactly. A context is switched over only after a parallel-run
period with zero reconciliation differences.

## Consequences
The importer is production code with tests, not a one-off script. Old and new systems coexist for a
while, so imported rows carry `source_type = legacy_import` and the legacy key. Rollback is cheap: the
old system stays authoritative until sign-off.
