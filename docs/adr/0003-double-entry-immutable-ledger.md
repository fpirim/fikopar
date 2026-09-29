# 3. Double-entry, append-only ledger

Status: Proposed · 2026-09-29

## Context
The legacy `CARHAR` file stored one-sided debit/credit rows per customer, edited in place. Balances
could drift from history and there was no trail of who changed what.

## Decision
Every financial effect is a `JournalEntry` with at least two `Posting`s where Σ debit = Σ credit per
currency. Postings and entries are never updated or deleted; a correction is a reversal entry that
references the original (at most one reversal per entry). The database enforces this with a deferred
constraint trigger (balance checked at COMMIT) and append-only triggers. `account_balances` is a
projection updated in the same transaction and checked against postings by a drift view.

## Consequences
Balances are always explainable from history and a trial balance always sums to zero. Mistakes stay
visible as entry + reversal pairs, which is what auditors expect. Amounts are `NUMERIC`, never floats.
