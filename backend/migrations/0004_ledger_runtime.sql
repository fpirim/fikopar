-- Runtime pieces the drafts left to F1: balance projection, currency guard, closed years,
-- consumer deliveries, ledger check and cash voucher guard.

CREATE OR REPLACE FUNCTION check_posting_currency() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM accounts WHERE id = NEW.account_id AND currency = NEW.currency) THEN
    RAISE EXCEPTION 'posting currency % does not match account %', NEW.currency, NEW.account_id
      USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER postings_currency
  BEFORE INSERT ON postings
  FOR EACH ROW EXECUTE FUNCTION check_posting_currency();

CREATE OR REPLACE FUNCTION project_posting_balance() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
  v_depot bigint;
BEGIN
  SELECT depot_id INTO v_depot FROM journal_entries WHERE id = NEW.entry_id;
  INSERT INTO account_balances (account_id, depot_id, org_id, debit_total, credit_total, last_entry_id, version)
  VALUES (NEW.account_id, v_depot, NEW.org_id,
          CASE NEW.side WHEN 'DEBIT' THEN NEW.amount ELSE 0 END,
          CASE NEW.side WHEN 'CREDIT' THEN NEW.amount ELSE 0 END,
          NEW.entry_id, 1)
  ON CONFLICT (account_id, depot_id) DO UPDATE
    SET debit_total   = account_balances.debit_total + EXCLUDED.debit_total,
        credit_total  = account_balances.credit_total + EXCLUDED.credit_total,
        last_entry_id = EXCLUDED.last_entry_id,
        version       = account_balances.version + 1,
        updated_at    = now();
  RETURN NULL;
END $$;

CREATE TRIGGER postings_project_balance
  AFTER INSERT ON postings
  FOR EACH ROW EXECUTE FUNCTION project_posting_balance();

CREATE UNIQUE INDEX journal_entries_idempotency_uq
  ON journal_entries (org_id, idempotency_key) WHERE idempotency_key IS NOT NULL;
CREATE INDEX postings_entry_idx ON postings (entry_id);

-- A posted carry-forward closes the previous year: balances start from its opening entries.
CREATE TABLE carry_forward_runs (
  org_id       bigint        NOT NULL REFERENCES organizations(id),
  year         int           NOT NULL CHECK (year BETWEEN 2000 AND 2999),
  entry_count  int           NOT NULL,
  total_debit  numeric(18,2) NOT NULL,
  total_credit numeric(18,2) NOT NULL,
  posted_by    bigint        REFERENCES users(id),
  posted_at    timestamptz   NOT NULL DEFAULT now(),
  PRIMARY KEY (org_id, year)
);
CREATE TRIGGER carry_forward_runs_append_only
  BEFORE UPDATE OR DELETE ON carry_forward_runs
  FOR EACH ROW EXECUTE FUNCTION forbid_mutation();

CREATE OR REPLACE FUNCTION ledger_start(p_org bigint, p_date date) RETURNS date
LANGUAGE sql STABLE AS $$
  SELECT coalesce(make_date(max(year), 1, 1), '-infinity'::date)
  FROM carry_forward_runs
  WHERE org_id = p_org AND year <= extract(year FROM p_date)::int
$$;

CREATE INDEX outbox_aggregate_idx ON outbox (org_id, aggregate_type, aggregate_id);

-- Consumer idempotency: one row per (event, consumer), written in the consumer's transaction.
CREATE TABLE outbox_deliveries (
  event_id     uuid        NOT NULL REFERENCES outbox(id) ON DELETE CASCADE,
  consumer     text        NOT NULL,
  delivered_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (event_id, consumer)
);

CREATE TABLE ledger_checks (
  org_id          bigint      PRIMARY KEY REFERENCES organizations(id),
  status          text        NOT NULL CHECK (status IN ('BALANCED','DRIFT')),
  entries_checked bigint      NOT NULL DEFAULT 0,
  checked_at      timestamptz NOT NULL DEFAULT now()
);

CREATE OR REPLACE FUNCTION check_cash_voucher_update() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF TG_OP = 'DELETE' THEN
    RAISE EXCEPTION 'cash vouchers cannot be deleted; void them' USING ERRCODE = 'restrict_violation';
  END IF;
  IF (to_jsonb(NEW) - 'voided_by_entry_id') <> (to_jsonb(OLD) - 'voided_by_entry_id')
     OR OLD.voided_by_entry_id IS NOT NULL THEN
    RAISE EXCEPTION 'cash voucher % is immutable except for voiding once', OLD.id USING ERRCODE = 'restrict_violation';
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER cash_vouchers_guard
  BEFORE UPDATE OR DELETE ON cash_vouchers
  FOR EACH ROW EXECUTE FUNCTION check_cash_voucher_update();
