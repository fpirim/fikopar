-- Double-entry ledger per company and depot. Postings are immutable; corrections are reversal entries.

CREATE TABLE accounts (
  id          bigserial PRIMARY KEY,
  org_id      bigint    NOT NULL REFERENCES organizations(id),
  code        text      NOT NULL,
  name        text      NOT NULL,
  type        text      NOT NULL CHECK (type IN ('ASSET','LIABILITY','EQUITY','INCOME','EXPENSE')),
  normal_side text      NOT NULL CHECK (normal_side IN ('DEBIT','CREDIT')),
  sicil_id    bigint,
  currency    char(3)   NOT NULL DEFAULT 'TRY' CHECK (currency = 'TRY'),
  is_active   boolean   NOT NULL DEFAULT true,
  version     int       NOT NULL DEFAULT 1,
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, code),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, sicil_id) REFERENCES sicils (organization_id, id)
);
CREATE INDEX accounts_sicil_idx ON accounts (org_id, sicil_id) WHERE sicil_id IS NOT NULL;

CREATE TABLE journal_entries (
  id                bigserial   PRIMARY KEY,
  org_id            bigint      NOT NULL REFERENCES organizations(id),
  depot_id          bigint      NOT NULL,
  entry_no          text        NOT NULL,
  entry_date        date        NOT NULL,
  description       text        NOT NULL,
  source_type       text        NOT NULL
                    CHECK (source_type IN ('manual','document','receipt','instrument','cash_voucher','cash_count','opening','reversal','legacy_import')),
  source_id         text,
  reverses_entry_id bigint      REFERENCES journal_entries(id),
  idempotency_key   text,
  created_by        bigint      REFERENCES users(id),
  created_at        timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, entry_no),
  FOREIGN KEY (org_id, depot_id) REFERENCES depots (org_id, id),
  -- Opening (carry-forward) entries sit on 1 January of a calendar year.
  CHECK (source_type <> 'opening' OR (extract(month FROM entry_date) = 1 AND extract(day FROM entry_date) = 1)),
  UNIQUE (org_id, id),
  UNIQUE (reverses_entry_id),                     -- an entry is reversed at most once
  CHECK (reverses_entry_id IS NULL OR source_type = 'reversal'),
  CHECK (reverses_entry_id IS NULL OR reverses_entry_id <> id)
);
-- Consumer idempotency: a redelivered DocumentPosted cannot post twice.
-- Opening entries use source_id '<year>:<run>:<account>:<depot>' so a reopened year can be posted again.
CREATE UNIQUE INDEX journal_entries_source_uq
  ON journal_entries (org_id, source_type, source_id)
  WHERE source_id IS NOT NULL AND source_type <> 'manual';
CREATE INDEX journal_entries_date_idx ON journal_entries (org_id, depot_id, entry_date DESC, id DESC);

CREATE TABLE postings (
  id         bigserial     PRIMARY KEY,
  org_id     bigint        NOT NULL,
  entry_id   bigint        NOT NULL,
  line_no    int           NOT NULL CHECK (line_no > 0),
  account_id bigint        NOT NULL,
  side       text          NOT NULL CHECK (side IN ('DEBIT','CREDIT')),
  amount     numeric(18,2) NOT NULL CHECK (amount > 0),
  currency   char(3)       NOT NULL DEFAULT 'TRY' CHECK (currency = 'TRY'),   -- v1.1: TRY only
  due_date   date,
  memo       text,
  UNIQUE (entry_id, line_no),
  -- Composite FKs keep entry, account and posting in the same org.
  FOREIGN KEY (org_id, entry_id)   REFERENCES journal_entries (org_id, id),
  FOREIGN KEY (org_id, account_id) REFERENCES accounts (org_id, id)
);
CREATE INDEX postings_account_idx ON postings (account_id, entry_id);

CREATE TRIGGER postings_append_only
  BEFORE UPDATE OR DELETE ON postings
  FOR EACH ROW EXECUTE FUNCTION forbid_mutation();
CREATE TRIGGER journal_entries_append_only
  BEFORE UPDATE OR DELETE ON journal_entries
  FOR EACH ROW EXECUTE FUNCTION forbid_mutation();

-- One currency, Σ debit = Σ credit and >= 2 lines, checked at COMMIT so lines can be inserted one by one.
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

CREATE CONSTRAINT TRIGGER postings_balanced
  AFTER INSERT ON postings
  DEFERRABLE INITIALLY DEFERRED
  FOR EACH ROW EXECUTE FUNCTION check_entry_balanced();

-- An entry without postings would bypass the check above.
CREATE OR REPLACE FUNCTION check_entry_has_postings() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM postings WHERE entry_id = NEW.id) THEN
    RAISE EXCEPTION 'journal entry % has no postings', NEW.id USING ERRCODE = 'check_violation';
  END IF;
  RETURN NULL;
END $$;

CREATE CONSTRAINT TRIGGER journal_entries_not_empty
  AFTER INSERT ON journal_entries
  DEFERRABLE INITIALLY DEFERRED
  FOR EACH ROW EXECUTE FUNCTION check_entry_has_postings();

-- A posted carry-forward into `year` closes year - 1 and earlier. Reopening voids the run (its opening
-- entries are reversed); the row stays and a new run can be posted. At most one active run per year.
CREATE TABLE carry_forward_runs (
  id            bigserial     PRIMARY KEY,
  org_id        bigint        NOT NULL REFERENCES organizations(id),
  year          int           NOT NULL CHECK (year BETWEEN 2000 AND 2999),
  entry_count   int           NOT NULL,
  total_debit   numeric(18,2) NOT NULL,
  total_credit  numeric(18,2) NOT NULL,
  posted_by     bigint        REFERENCES users(id),
  posted_at     timestamptz   NOT NULL DEFAULT now(),
  reopened_by   bigint        REFERENCES users(id),
  reopened_at   timestamptz,
  reopen_reason text,
  UNIQUE (org_id, id),
  CHECK ((reopened_at IS NULL) = (reopen_reason IS NULL)),
  CHECK (reopen_reason IS NULL OR length(reopen_reason) >= 3)
);
CREATE UNIQUE INDEX carry_forward_runs_active_uq ON carry_forward_runs (org_id, year) WHERE reopened_at IS NULL;

-- Runs are append-only except for reopening once.
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

-- Balances start at 1 January of the latest active run <= the date (L6, statements).
CREATE OR REPLACE FUNCTION ledger_start(p_org bigint, p_date date) RETURNS date
LANGUAGE sql STABLE AS $$
  SELECT coalesce(make_date(max(year), 1, 1), '-infinity'::date)
  FROM carry_forward_runs
  WHERE org_id = p_org AND reopened_at IS NULL AND year <= extract(year FROM p_date)::int
$$;

-- Closed years: no entry may be dated before the latest active run (ERR_YEAR_CLOSED).
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

-- Preview version per (company, year): ETag of getCarryForward, If-Match on override changes and reopen.
CREATE TABLE carry_forward_previews (
  org_id  bigint NOT NULL REFERENCES organizations(id),
  year    int    NOT NULL CHECK (year BETWEEN 2000 AND 2999),
  version int    NOT NULL DEFAULT 1,
  PRIMARY KEY (org_id, year)
);

-- One active opening per (account, depot, calendar year). A reopened run's rows keep their reversal.
CREATE TABLE opening_balances (
  org_id             bigint        NOT NULL,
  account_id         bigint        NOT NULL,
  depot_id           bigint        NOT NULL,
  year               int           NOT NULL CHECK (year BETWEEN 2000 AND 2999),
  debit              numeric(18,2) NOT NULL DEFAULT 0 CHECK (debit >= 0),
  credit             numeric(18,2) NOT NULL DEFAULT 0 CHECK (credit >= 0),
  source             text          NOT NULL CHECK (source IN ('CARRY_FORWARD','LEGACY_IMPORT')),
  run_id             bigint,
  journal_entry_id   bigint        NOT NULL,
  voided_by_entry_id bigint,
  UNIQUE (journal_entry_id),
  CHECK ((source = 'CARRY_FORWARD') = (run_id IS NOT NULL)),
  FOREIGN KEY (org_id, account_id)         REFERENCES accounts (org_id, id),
  FOREIGN KEY (org_id, depot_id)           REFERENCES depots (org_id, id),
  FOREIGN KEY (org_id, run_id)             REFERENCES carry_forward_runs (org_id, id),
  FOREIGN KEY (org_id, journal_entry_id)   REFERENCES journal_entries (org_id, id),
  FOREIGN KEY (org_id, voided_by_entry_id) REFERENCES journal_entries (org_id, id)
);
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

-- Edited rows of a carry-forward preview; applied when the year is posted.
CREATE TABLE carry_forward_overrides (
  org_id     bigint        NOT NULL,
  year       int           NOT NULL CHECK (year BETWEEN 2000 AND 2999),
  account_id bigint        NOT NULL,
  depot_id   bigint        NOT NULL,
  debit      numeric(18,2) NOT NULL DEFAULT 0 CHECK (debit >= 0),
  credit     numeric(18,2) NOT NULL DEFAULT 0 CHECK (credit >= 0),
  reason     text          NOT NULL CHECK (length(reason) >= 3),
  created_by bigint        REFERENCES users(id),
  created_at timestamptz   NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, year, account_id, depot_id),
  FOREIGN KEY (org_id, account_id) REFERENCES accounts (org_id, id),
  FOREIGN KEY (org_id, depot_id)   REFERENCES depots (org_id, id),
  CHECK (debit = 0 OR credit = 0)
);

-- Which account a posting rule uses. depot_id NULL = company default; a depot row overrides it.
CREATE TABLE account_roles (
  id         bigserial PRIMARY KEY,
  org_id     bigint    NOT NULL,
  role       text      NOT NULL CHECK (role IN (
               'CASH','SALES','SALES_RETURNS','PURCHASES','PURCHASE_RETURNS','PRICE_DIFFERENCE',
               'OUTPUT_VAT','INPUT_VAT','RECEIVABLE_CHEQUES','RECEIVABLE_NOTES','CHEQUES_AT_BANK',
               'PAYABLE_CHEQUES','PAYABLE_NOTES','OPENING','CASH_OVER_SHORT')),
  depot_id   bigint,
  account_id bigint    NOT NULL,
  FOREIGN KEY (org_id, depot_id)   REFERENCES depots (org_id, id),
  FOREIGN KEY (org_id, account_id) REFERENCES accounts (org_id, id)
);
CREATE UNIQUE INDEX account_roles_uq ON account_roles (org_id, role, coalesce(depot_id, 0));

-- Projection maintained in the posting transaction; rebuildable from postings.
CREATE TABLE account_balances (
  account_id    bigint        NOT NULL REFERENCES accounts(id),
  depot_id      bigint        NOT NULL REFERENCES depots(id),
  org_id        bigint        NOT NULL,
  debit_total   numeric(18,2) NOT NULL DEFAULT 0 CHECK (debit_total >= 0),
  credit_total  numeric(18,2) NOT NULL DEFAULT 0 CHECK (credit_total >= 0),
  last_entry_id bigint,
  version       int           NOT NULL DEFAULT 0,
  updated_at    timestamptz   NOT NULL DEFAULT now(),
  PRIMARY KEY (account_id, depot_id)
);

CREATE VIEW account_balance_drift AS
SELECT b.account_id, b.depot_id,
       b.debit_total  - coalesce(p.d, 0) AS debit_drift,
       b.credit_total - coalesce(p.c, 0) AS credit_drift
FROM account_balances b
LEFT JOIN (
  SELECT p.account_id, e.depot_id,
         sum(p.amount) FILTER (WHERE p.side = 'DEBIT')  AS d,
         sum(p.amount) FILTER (WHERE p.side = 'CREDIT') AS c
  FROM postings p JOIN journal_entries e ON e.id = p.entry_id
  GROUP BY p.account_id, e.depot_id) p USING (account_id, depot_id)
WHERE b.debit_total <> coalesce(p.d, 0) OR b.credit_total <> coalesce(p.c, 0);
