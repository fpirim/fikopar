-- Receipts (makbuz), cash book, cheques and notes (çek / senet).

CREATE TABLE instruments (
  id            bigserial     PRIMARY KEY,
  org_id        bigint        NOT NULL REFERENCES organizations(id),
  depot_id      bigint        NOT NULL,
  instrument_no text          NOT NULL,             -- AC/AS/BC/BS series
  type          text          NOT NULL
                CHECK (type IN ('RECEIVABLE_CHEQUE','RECEIVABLE_NOTE','PAYABLE_CHEQUE','PAYABLE_NOTE')),
  serial_no     text          NOT NULL,
  amount        numeric(18,2) NOT NULL CHECK (amount > 0),
  currency      char(3)       NOT NULL DEFAULT 'TRY',
  issue_date    date          NOT NULL,
  due_date      date          NOT NULL,
  sicil_id      bigint        NOT NULL REFERENCES sicils(id),
  drawer_name   text,
  drawer_tax_no text,
  bank_name     text,
  bank_branch   text,
  bank_city     text,
  account_no    text,
  status        text          NOT NULL
                CHECK (status IN ('PORTFOLIO','DEPOSITED','COLLECTED','ENDORSED','PROTESTED','RETURNED','ISSUED','PAID')),
  version       int           NOT NULL DEFAULT 1,
  created_at    timestamptz   NOT NULL DEFAULT now(),
  updated_at    timestamptz   NOT NULL DEFAULT now(),
  UNIQUE (org_id, instrument_no),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, depot_id) REFERENCES depots (org_id, id),
  CHECK (due_date >= issue_date),
  CHECK (CASE WHEN type LIKE 'PAYABLE_%' THEN status IN ('ISSUED','PAID','RETURNED')
              ELSE status NOT IN ('ISSUED','PAID') END)
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
  sicil_id           bigint        NOT NULL REFERENCES sicils(id),
  receipt_date       date          NOT NULL,
  method             text          NOT NULL CHECK (method IN ('CASH','BANK_TRANSFER','INSTRUMENT')),
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
  FOREIGN KEY (org_id, instrument_id)      REFERENCES instruments (org_id, id),
  FOREIGN KEY (org_id, journal_entry_id)   REFERENCES journal_entries (org_id, id),
  FOREIGN KEY (org_id, voided_by_entry_id) REFERENCES journal_entries (org_id, id),
  CHECK ((origin = 'INSTRUMENT') = (instrument_id IS NOT NULL)),
  CHECK (origin = 'MANUAL' OR method = 'INSTRUMENT'),
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
                                          'ENDORSE','PROTEST','RETURN','PAY')),
  from_status           text,
  to_status             text        NOT NULL,
  counterparty_sicil_id bigint      REFERENCES sicils(id),
  receipt_id            bigint,
  journal_entry_id      bigint,
  note                  text,
  actor_user_id         bigint      REFERENCES users(id),
  occurred_at           timestamptz NOT NULL DEFAULT now(),
  UNIQUE (instrument_id, seq),
  FOREIGN KEY (org_id, instrument_id)    REFERENCES instruments (org_id, id),
  FOREIGN KEY (org_id, receipt_id)       REFERENCES receipts (org_id, id),
  FOREIGN KEY (org_id, journal_entry_id) REFERENCES journal_entries (org_id, id),
  CHECK (action <> 'ENDORSE' OR counterparty_sicil_id IS NOT NULL),
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
       (OLD.status = 'PORTFOLIO' AND NEW.status IN ('DEPOSITED','COLLECTED','ENDORSED','PROTESTED','RETURNED'))
    OR (OLD.status = 'DEPOSITED' AND NEW.status IN ('COLLECTED','PROTESTED'))
    OR (OLD.status = 'ENDORSED'  AND NEW.status = 'PROTESTED')
    OR (OLD.status = 'PROTESTED' AND NEW.status IN ('COLLECTED','RETURNED'))
    OR (OLD.status = 'ISSUED'    AND NEW.status IN ('PAID','RETURNED'))) THEN
    RAISE EXCEPTION 'illegal instrument transition % -> %', OLD.status, NEW.status USING ERRCODE = 'check_violation';
  END IF;
  IF NEW.version <> OLD.version + 1 THEN
    RAISE EXCEPTION 'instrument % update must bump version', OLD.id USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER instruments_state_machine
  BEFORE UPDATE ON instruments
  FOR EACH ROW EXECUTE FUNCTION check_instrument_transition();

-- Cash book = postings on cash accounts (100.*) per depot.
CREATE VIEW cash_book AS
SELECT e.org_id, e.depot_id, e.entry_date, e.id AS journal_entry_id, e.description,
       r.id AS receipt_id, r.receipt_no, r.sicil_id,
       CASE p.side WHEN 'DEBIT'  THEN p.amount ELSE 0 END AS income,
       CASE p.side WHEN 'CREDIT' THEN p.amount ELSE 0 END AS expense
FROM postings p
JOIN accounts a        ON a.id = p.account_id AND a.code LIKE '100%'
JOIN journal_entries e ON e.id = p.entry_id
LEFT JOIN receipts r   ON r.journal_entry_id = e.id;
