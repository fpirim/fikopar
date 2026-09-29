-- Contracts v1.1: sicil version, TRY-only entries, closed years with reopen, preview versions,
-- voidable openings (docs/adr/0010-contracts-v1-1.md, migrations/draft/0002_ledger.sql).

ALTER TABLE sicils ADD COLUMN IF NOT EXISTS version int NOT NULL DEFAULT 1;

ALTER TABLE accounts ADD CONSTRAINT accounts_currency_try CHECK (currency = 'TRY');
ALTER TABLE postings
  ALTER COLUMN currency SET DEFAULT 'TRY',
  ADD CONSTRAINT postings_currency_try CHECK (currency = 'TRY');

CREATE OR REPLACE FUNCTION check_entry_balanced() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
  v_entry bigint := NEW.entry_id;
  v_lines int;
  v_curr  int;
  v_diff  numeric;
BEGIN
  SELECT count(*), count(DISTINCT currency),
         sum(CASE side WHEN 'DEBIT' THEN amount ELSE -amount END)
    INTO v_lines, v_curr, v_diff
    FROM postings WHERE entry_id = v_entry;
  IF v_lines < 2 THEN
    RAISE EXCEPTION 'journal entry % has % posting(s); at least 2 required', v_entry, v_lines
      USING ERRCODE = 'check_violation';
  END IF;
  IF v_curr <> 1 THEN
    RAISE EXCEPTION 'journal entry % mixes currencies', v_entry USING ERRCODE = 'check_violation';
  END IF;
  IF v_diff <> 0 THEN
    RAISE EXCEPTION 'journal entry % is unbalanced by %', v_entry, v_diff
      USING ERRCODE = 'check_violation';
  END IF;
  RETURN NULL;
END $$;

-- Runs: one active (not reopened) run per (company, year); reopening happens once.
DROP TRIGGER carry_forward_runs_append_only ON carry_forward_runs;
ALTER TABLE carry_forward_runs
  DROP CONSTRAINT carry_forward_runs_pkey,
  ADD COLUMN id            bigserial PRIMARY KEY,
  ADD COLUMN reopened_by   bigint REFERENCES users(id),
  ADD COLUMN reopened_at   timestamptz,
  ADD COLUMN reopen_reason text,
  ADD CONSTRAINT carry_forward_runs_org_id_uq UNIQUE (org_id, id),
  ADD CONSTRAINT carry_forward_runs_reopen_ck CHECK ((reopened_at IS NULL) = (reopen_reason IS NULL)),
  ADD CONSTRAINT carry_forward_runs_reason_ck CHECK (reopen_reason IS NULL OR length(reopen_reason) >= 3);
CREATE UNIQUE INDEX carry_forward_runs_active_uq ON carry_forward_runs (org_id, year) WHERE reopened_at IS NULL;

CREATE OR REPLACE FUNCTION carry_forward_runs_guard() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF TG_OP = 'DELETE' OR OLD.reopened_at IS NOT NULL
     OR (to_jsonb(NEW) - 'reopened_by' - 'reopened_at' - 'reopen_reason')
        <> (to_jsonb(OLD) - 'reopened_by' - 'reopened_at' - 'reopen_reason') THEN
    RAISE EXCEPTION 'carry-forward run % is immutable except for reopening once', OLD.id
      USING ERRCODE = 'restrict_violation';
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER carry_forward_runs_guard
  BEFORE UPDATE OR DELETE ON carry_forward_runs
  FOR EACH ROW EXECUTE FUNCTION carry_forward_runs_guard();

CREATE OR REPLACE FUNCTION ledger_start(p_org bigint, p_date date) RETURNS date
LANGUAGE sql STABLE AS $$
  SELECT coalesce(make_date(max(year), 1, 1), '-infinity'::date)
  FROM carry_forward_runs
  WHERE org_id = p_org AND reopened_at IS NULL AND year <= extract(year FROM p_date)::int
$$;

-- Closed years (L7): no entry dated before the latest active run's year. The service answers
-- ERR_YEAR_CLOSED first; this is the backstop.
CREATE OR REPLACE FUNCTION check_year_open() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF EXISTS (SELECT 1 FROM carry_forward_runs
             WHERE org_id = NEW.org_id AND reopened_at IS NULL
               AND year > extract(year FROM NEW.entry_date)::int) THEN
    RAISE EXCEPTION 'ERR_YEAR_CLOSED: % is in a closed year', NEW.entry_date USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER journal_entries_year_open
  BEFORE INSERT ON journal_entries
  FOR EACH ROW EXECUTE FUNCTION check_year_open();

CREATE TABLE carry_forward_previews (
  org_id  bigint NOT NULL REFERENCES organizations(id),
  year    int    NOT NULL CHECK (year BETWEEN 2000 AND 2999),
  version int    NOT NULL DEFAULT 1,
  PRIMARY KEY (org_id, year)
);

-- Openings belong to a run and are voided (reversed) when the run is reopened.
ALTER TABLE opening_balances
  ADD COLUMN run_id             bigint,
  ADD COLUMN voided_by_entry_id bigint;
UPDATE opening_balances o SET run_id = r.id
  FROM carry_forward_runs r WHERE r.org_id = o.org_id AND r.year = o.year AND o.source = 'CARRY_FORWARD';
ALTER TABLE opening_balances
  DROP CONSTRAINT opening_balances_pkey,
  ADD CONSTRAINT opening_balances_entry_uq UNIQUE (journal_entry_id),
  ADD CONSTRAINT opening_balances_run_ck CHECK ((source = 'CARRY_FORWARD') = (run_id IS NOT NULL)),
  ADD CONSTRAINT opening_balances_run_fk FOREIGN KEY (org_id, run_id) REFERENCES carry_forward_runs (org_id, id),
  ADD CONSTRAINT opening_balances_void_fk FOREIGN KEY (org_id, voided_by_entry_id) REFERENCES journal_entries (org_id, id);
CREATE UNIQUE INDEX opening_balances_active_uq
  ON opening_balances (account_id, depot_id, year) WHERE voided_by_entry_id IS NULL;

CREATE OR REPLACE FUNCTION opening_balances_guard() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF TG_OP = 'DELETE' OR OLD.voided_by_entry_id IS NOT NULL
     OR (to_jsonb(NEW) - 'voided_by_entry_id') <> (to_jsonb(OLD) - 'voided_by_entry_id') THEN
    RAISE EXCEPTION 'opening balance % is immutable except for voiding once', OLD.journal_entry_id
      USING ERRCODE = 'restrict_violation';
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER opening_balances_guard
  BEFORE UPDATE OR DELETE ON opening_balances
  FOR EACH ROW EXECUTE FUNCTION opening_balances_guard();
