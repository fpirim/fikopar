-- Receipts (makbuz), cash book, cheques and notes (çek / senet).
-- Prefixes: TAH / TED receipts, AC / AS receivable cheque / note, BC / BS payable cheque / note, GDR / VRM cash vouchers.

CREATE TABLE instruments (
  id            bigserial     PRIMARY KEY,
  org_id        bigint        NOT NULL REFERENCES organizations(id),
  depot_id      bigint        NOT NULL,
  instrument_no text          NOT NULL,             -- AC/AS/BC/BS series
  type          text          NOT NULL
                CHECK (type IN ('RECEIVABLE_CHEQUE','RECEIVABLE_NOTE','PAYABLE_CHEQUE','PAYABLE_NOTE')),
  serial_no     text          NOT NULL,
  amount        numeric(18,2) NOT NULL CHECK (amount > 0),
  remaining     numeric(18,2) NOT NULL,             -- amount − Σ collected / paid
  currency      char(3)       NOT NULL DEFAULT 'TRY',
  issue_date    date          NOT NULL,
  due_date      date          NOT NULL,
  sicil_id      bigint        NOT NULL,
  drawer_name   text,
  drawer_tax_no text,
  bank_name     text,
  bank_branch   text,
  bank_city     text,
  account_no    text,
  status        text          NOT NULL
                CHECK (status IN ('PORTFOLIO','DEPOSITED','PARTIALLY_COLLECTED','COLLECTED','BOUNCED','ENDORSED',
                                  'PROTESTED','RETURNED','ISSUED','PAID')),
  version       int           NOT NULL DEFAULT 1,
  created_at    timestamptz   NOT NULL DEFAULT now(),
  updated_at    timestamptz   NOT NULL DEFAULT now(),
  UNIQUE (org_id, instrument_no),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, depot_id) REFERENCES depots (org_id, id),
  FOREIGN KEY (org_id, sicil_id) REFERENCES sicils (organization_id, id),
  CHECK (due_date >= issue_date),
  CHECK (CASE WHEN type LIKE 'PAYABLE_%' THEN status IN ('ISSUED','PAID','RETURNED')
              ELSE status NOT IN ('ISSUED','PAID') END),
  -- Partial collection: 0 <= remaining <= amount; fully settled exactly when remaining = 0.
  CHECK (remaining >= 0 AND remaining <= amount),
  CHECK ((status IN ('COLLECTED','PAID')) = (remaining = 0)),
  CHECK (status <> 'PARTIALLY_COLLECTED' OR remaining < amount)
);
CREATE UNIQUE INDEX instruments_identity_uq
  ON instruments (org_id, type, serial_no, coalesce(bank_name, ''), coalesce(account_no, ''));
CREATE INDEX instruments_due_idx ON instruments (org_id, depot_id, status, due_date);

CREATE TABLE receipts (
  id                 bigserial     PRIMARY KEY,
  org_id             bigint        NOT NULL REFERENCES organizations(id),
  depot_id           bigint        NOT NULL,
  receipt_no         text          NOT NULL,        -- TAH / TED series
  kind               text          NOT NULL CHECK (kind IN ('COLLECTION','PAYMENT')),
  origin             text          NOT NULL DEFAULT 'MANUAL' CHECK (origin IN ('MANUAL','INSTRUMENT')),
  instrument_id      bigint,
  sicil_id           bigint        NOT NULL,
  receipt_date       date          NOT NULL,
  method             text          NOT NULL CHECK (method IN ('CASH','BANK_TRANSFER','CHEQUE','NOTE','ENDORSEMENT')),
  bank_account_id    bigint,
  amount             numeric(18,2) NOT NULL CHECK (amount > 0),
  currency           char(3)       NOT NULL DEFAULT 'TRY',
  description        text,
  journal_entry_id   bigint        NOT NULL,
  voided_by_entry_id bigint,
  voided_at          timestamptz,
  created_by         bigint        REFERENCES users(id),
  created_at         timestamptz   NOT NULL DEFAULT now(),
  UNIQUE (org_id, receipt_no),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, depot_id)           REFERENCES depots (org_id, id),
  FOREIGN KEY (org_id, sicil_id)           REFERENCES sicils (organization_id, id),
  FOREIGN KEY (org_id, instrument_id)      REFERENCES instruments (org_id, id),
  FOREIGN KEY (org_id, bank_account_id)    REFERENCES accounts (org_id, id),
  FOREIGN KEY (org_id, journal_entry_id)   REFERENCES journal_entries (org_id, id),
  FOREIGN KEY (org_id, voided_by_entry_id) REFERENCES journal_entries (org_id, id),
  CHECK ((origin = 'INSTRUMENT') = (instrument_id IS NOT NULL)),
  CHECK (method NOT IN ('CHEQUE','NOTE','ENDORSEMENT') OR origin = 'INSTRUMENT'),
  CHECK ((method = 'BANK_TRANSFER') = (bank_account_id IS NOT NULL)),
  CHECK ((voided_by_entry_id IS NULL) = (voided_at IS NULL))
);
CREATE INDEX receipts_sicil_idx ON receipts (org_id, sicil_id, receipt_date DESC);

-- Receipts are immutable except voiding; instrument receipts change only inside an instrument
-- operation, which sets `SET LOCAL fikopar.instrument_op = 'on'`.
CREATE OR REPLACE FUNCTION check_receipt_update() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF TG_OP = 'DELETE' THEN
    RAISE EXCEPTION 'receipts cannot be deleted; void them' USING ERRCODE = 'restrict_violation';
  END IF;
  IF OLD.origin = 'INSTRUMENT' AND coalesce(current_setting('fikopar.instrument_op', true), '') <> 'on' THEN
    RAISE EXCEPTION 'receipt % belongs to instrument %; change it through the instrument', OLD.id, OLD.instrument_id
      USING ERRCODE = 'restrict_violation';
  END IF;
  IF (to_jsonb(NEW) - 'voided_by_entry_id' - 'voided_at') <> (to_jsonb(OLD) - 'voided_by_entry_id' - 'voided_at')
     OR OLD.voided_at IS NOT NULL THEN
    RAISE EXCEPTION 'receipt % is immutable except for voiding once', OLD.id USING ERRCODE = 'restrict_violation';
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER receipts_guard
  BEFORE UPDATE OR DELETE ON receipts
  FOR EACH ROW EXECUTE FUNCTION check_receipt_update();

CREATE TABLE instrument_events (
  id                    bigserial   PRIMARY KEY,
  org_id                bigint      NOT NULL,
  instrument_id         bigint      NOT NULL,
  seq                   int         NOT NULL CHECK (seq > 0),
  action                text        NOT NULL
                        CHECK (action IN ('REGISTER','DEPOSIT_TO_BANK','COLLECT_AT_BANK','COLLECT_BY_HAND',
                                          'BOUNCE','ENDORSE','PROTEST','RETURN','PAY')),
  from_status           text,
  to_status             text        NOT NULL,
  amount                numeric(18,2) CHECK (amount > 0),   -- collected / paid in this step
  remaining_after       numeric(18,2) NOT NULL CHECK (remaining_after >= 0),
  counterparty_sicil_id bigint,
  receipt_id            bigint,
  journal_entry_id      bigint,
  note                  text,
  actor_user_id         bigint      REFERENCES users(id),
  occurred_at           timestamptz NOT NULL DEFAULT now(),
  UNIQUE (instrument_id, seq),
  FOREIGN KEY (org_id, instrument_id)    REFERENCES instruments (org_id, id),
  FOREIGN KEY (org_id, counterparty_sicil_id) REFERENCES sicils (organization_id, id),
  FOREIGN KEY (org_id, receipt_id)       REFERENCES receipts (org_id, id),
  FOREIGN KEY (org_id, journal_entry_id) REFERENCES journal_entries (org_id, id),
  CHECK (action <> 'ENDORSE' OR counterparty_sicil_id IS NOT NULL),
  CHECK ((action IN ('COLLECT_AT_BANK','COLLECT_BY_HAND','PAY')) = (amount IS NOT NULL)),
  CHECK ((seq = 1) = (action = 'REGISTER')),
  CHECK ((seq = 1) = (from_status IS NULL))
);
CREATE TRIGGER instrument_events_append_only
  BEFORE UPDATE OR DELETE ON instrument_events
  FOR EACH ROW EXECUTE FUNCTION forbid_mutation();

CREATE OR REPLACE FUNCTION check_instrument_transition() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.status IS DISTINCT FROM OLD.status AND NOT (
       (OLD.status = 'PORTFOLIO' AND NEW.status IN ('DEPOSITED','PARTIALLY_COLLECTED','COLLECTED','ENDORSED','PROTESTED','RETURNED'))
    OR (OLD.status = 'DEPOSITED' AND NEW.status IN ('COLLECTED','BOUNCED'))
    OR (OLD.status = 'ENDORSED'  AND NEW.status = 'BOUNCED')
    OR (OLD.status IN ('PARTIALLY_COLLECTED','BOUNCED','PROTESTED')
                                 AND NEW.status IN ('PARTIALLY_COLLECTED','COLLECTED','PROTESTED','RETURNED'))
    OR (OLD.status = 'ISSUED'    AND NEW.status IN ('PAID','RETURNED'))) THEN
    RAISE EXCEPTION 'illegal instrument transition % -> %', OLD.status, NEW.status USING ERRCODE = 'check_violation';
  END IF;
  IF NEW.remaining > OLD.remaining OR (NEW.amount <> OLD.amount) THEN
    RAISE EXCEPTION 'instrument % remaining can only decrease', OLD.id USING ERRCODE = 'check_violation';
  END IF;
  IF NEW.version <> OLD.version + 1 THEN
    RAISE EXCEPTION 'instrument % update must bump version', OLD.id USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER instruments_state_machine
  BEFORE UPDATE ON instruments
  FOR EACH ROW EXECUTE FUNCTION check_instrument_transition();

-- Expenses paid from the till and cash moved to or from a bank (GDR / VRM series).
CREATE TABLE cash_vouchers (
  id                 bigserial     PRIMARY KEY,
  org_id             bigint        NOT NULL,
  depot_id           bigint        NOT NULL,
  voucher_no         text          NOT NULL,
  kind               text          NOT NULL CHECK (kind IN ('EXPENSE','BANK_DEPOSIT','BANK_WITHDRAWAL')),
  voucher_date       date          NOT NULL,
  account_id         bigint        NOT NULL,        -- expense account or bank account
  amount             numeric(18,2) NOT NULL CHECK (amount > 0),
  description        text          NOT NULL,
  journal_entry_id   bigint        NOT NULL,
  voided_by_entry_id bigint,
  created_by         bigint        REFERENCES users(id),
  created_at         timestamptz   NOT NULL DEFAULT now(),
  UNIQUE (org_id, voucher_no),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, depot_id)           REFERENCES depots (org_id, id),
  FOREIGN KEY (org_id, account_id)         REFERENCES accounts (org_id, id),
  FOREIGN KEY (org_id, journal_entry_id)   REFERENCES journal_entries (org_id, id),
  FOREIGN KEY (org_id, voided_by_entry_id) REFERENCES journal_entries (org_id, id)
);

-- Till count; a difference posts to CASH_OVER_SHORT when post_difference is set.
CREATE TABLE cash_counts (
  id               bigserial     PRIMARY KEY,
  org_id           bigint        NOT NULL,
  depot_id         bigint        NOT NULL,
  counted_at       timestamptz   NOT NULL DEFAULT now(),
  book_balance     numeric(18,2) NOT NULL,
  counted_amount   numeric(18,2) NOT NULL CHECK (counted_amount >= 0),
  difference       numeric(18,2) GENERATED ALWAYS AS (counted_amount - book_balance) STORED,
  journal_entry_id bigint,
  note             text,
  counted_by       bigint        REFERENCES users(id),
  FOREIGN KEY (org_id, depot_id)         REFERENCES depots (org_id, id),
  FOREIGN KEY (org_id, journal_entry_id) REFERENCES journal_entries (org_id, id)
);
CREATE TRIGGER cash_counts_append_only
  BEFORE UPDATE OR DELETE ON cash_counts
  FOR EACH ROW EXECUTE FUNCTION forbid_mutation();

-- Cash book = postings on the depot's CASH account.
CREATE VIEW cash_book AS
SELECT e.org_id, e.depot_id, e.entry_date, e.id AS journal_entry_id, e.description,
       coalesce(r.receipt_no, v.voucher_no, e.entry_no) AS document_no,
       coalesce(r.kind, v.kind, e.source_type) AS kind,
       r.id AS receipt_id, v.id AS voucher_id, r.sicil_id,
       CASE p.side WHEN 'DEBIT'  THEN p.amount ELSE 0 END AS income,
       CASE p.side WHEN 'CREDIT' THEN p.amount ELSE 0 END AS expense
FROM postings p
JOIN journal_entries e    ON e.id = p.entry_id
LEFT JOIN receipts r      ON r.journal_entry_id = e.id
LEFT JOIN cash_vouchers v ON v.journal_entry_id = e.id
WHERE EXISTS (SELECT 1 FROM account_roles ar
              WHERE ar.org_id = e.org_id AND ar.role = 'CASH' AND ar.account_id = p.account_id
                AND (ar.depot_id = e.depot_id OR ar.depot_id IS NULL));
