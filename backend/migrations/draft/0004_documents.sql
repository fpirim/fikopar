-- Trade documents (invoices, waybills, returns, price differences). Posting emits DocumentPosted via outbox.

CREATE TABLE documents (
  id                 bigserial     PRIMARY KEY,
  org_id             bigint        NOT NULL REFERENCES organizations(id),
  depot_id           bigint        NOT NULL,
  document_no        text,                          -- allocated from number_series on post
  type               text          NOT NULL CHECK (type IN (
                       'SALES_INVOICE','SALES_INVOICE_WITH_WAYBILL','SALES_WAYBILL',
                       'PURCHASE_INVOICE','PURCHASE_INVOICE_WITH_WAYBILL','PURCHASE_WAYBILL',
                       'SALES_RETURN_INVOICE','SALES_RETURN_WAYBILL','PURCHASE_RETURN_INVOICE','PURCHASE_RETURN_WAYBILL',
                       'SALES_PRICE_DIFFERENCE','PURCHASE_PRICE_DIFFERENCE')),
  status             text          NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','POSTED','CANCELLED')),
  sicil_id           bigint        NOT NULL REFERENCES sicils(id),
  document_date      date          NOT NULL,
  due_date           date,
  linked_document_id bigint,
  currency           char(3)       NOT NULL DEFAULT 'TRY',
  subtotal           numeric(18,2) NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
  discount_total     numeric(18,2) NOT NULL DEFAULT 0 CHECK (discount_total >= 0),
  vat_total          numeric(18,2) NOT NULL DEFAULT 0 CHECK (vat_total >= 0),
  grand_total        numeric(18,2) NOT NULL DEFAULT 0 CHECK (grand_total >= 0),
  description        text,
  sicil_snapshot     jsonb,                         -- name, address, tax data as printed
  posted_at          timestamptz,
  cancelled_at       timestamptz,
  cancel_reason      text,
  version            int           NOT NULL DEFAULT 1,
  created_by         bigint        REFERENCES users(id),
  created_at         timestamptz   NOT NULL DEFAULT now(),
  updated_at         timestamptz   NOT NULL DEFAULT now(),
  UNIQUE (org_id, document_no),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, depot_id)           REFERENCES depots (org_id, id),
  FOREIGN KEY (org_id, linked_document_id) REFERENCES documents (org_id, id),
  CHECK (grand_total = subtotal - discount_total + vat_total),
  CHECK (due_date IS NULL OR due_date >= document_date),
  CHECK ((status = 'DRAFT') = (document_no IS NULL)),
  CHECK ((status = 'DRAFT') = (posted_at IS NULL) OR status = 'CANCELLED'),
  CHECK ((status = 'CANCELLED') = (cancelled_at IS NOT NULL)),
  CHECK (type NOT LIKE '%PRICE_DIFFERENCE' OR linked_document_id IS NOT NULL)
);
CREATE INDEX documents_sicil_idx ON documents (org_id, sicil_id, document_date DESC);
CREATE INDEX documents_depot_idx ON documents (org_id, depot_id, status, document_date DESC);

CREATE TABLE document_lines (
  id            bigserial     PRIMARY KEY,
  org_id        bigint        NOT NULL,
  document_id   bigint        NOT NULL,
  line_no       int           NOT NULL CHECK (line_no > 0),
  part_id       bigint        NOT NULL,
  qty           numeric(18,4) NOT NULL CHECK (qty > 0),
  unit_price    numeric(18,4) NOT NULL CHECK (unit_price >= 0),
  discount1_pct numeric(7,4)  NOT NULL DEFAULT 0 CHECK (discount1_pct BETWEEN 0 AND 100),
  discount2_pct numeric(7,4)  NOT NULL DEFAULT 0 CHECK (discount2_pct BETWEEN 0 AND 100),
  vat_pct       numeric(7,4)  NOT NULL DEFAULT 0 CHECK (vat_pct BETWEEN 0 AND 100),
  net_amount    numeric(18,2) NOT NULL CHECK (net_amount >= 0),
  vat_amount    numeric(18,2) NOT NULL CHECK (vat_amount >= 0),
  UNIQUE (document_id, line_no),
  FOREIGN KEY (org_id, document_id) REFERENCES documents (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, part_id)     REFERENCES parts (org_id, id)
);

CREATE OR REPLACE FUNCTION is_waybill(p_type text) RETURNS boolean
LANGUAGE sql IMMUTABLE AS $$
  SELECT p_type IN ('SALES_WAYBILL','PURCHASE_WAYBILL','SALES_RETURN_WAYBILL','PURCHASE_RETURN_WAYBILL')
$$;

CREATE OR REPLACE FUNCTION check_document_transition() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF OLD.status <> 'DRAFT' AND NEW.status = OLD.status THEN
    RAISE EXCEPTION 'document % is % and cannot be edited', OLD.id, OLD.status USING ERRCODE = 'check_violation';
  END IF;
  IF NEW.status <> OLD.status AND NOT (
       (OLD.status = 'DRAFT'  AND NEW.status IN ('POSTED','CANCELLED'))
    OR (OLD.status = 'POSTED' AND NEW.status = 'CANCELLED')) THEN
    RAISE EXCEPTION 'illegal document transition % -> %', OLD.status, NEW.status USING ERRCODE = 'check_violation';
  END IF;
  IF NEW.version <> OLD.version + 1 THEN
    RAISE EXCEPTION 'document % update must bump version', OLD.id USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER documents_state_machine
  BEFORE UPDATE ON documents
  FOR EACH ROW EXECUTE FUNCTION check_document_transition();

-- Waybills move goods, not money: they must never reach the current account.
CREATE OR REPLACE FUNCTION check_document_entry() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE v_type text;
BEGIN
  IF NEW.source_type = 'document' THEN
    SELECT type INTO v_type FROM documents WHERE org_id = NEW.org_id AND id = NEW.source_id::bigint;
    IF v_type IS NULL OR is_waybill(v_type) THEN
      RAISE EXCEPTION 'document % (%) cannot post to the ledger', NEW.source_id, coalesce(v_type, 'missing')
        USING ERRCODE = 'check_violation';
    END IF;
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER journal_entries_document_rules
  BEFORE INSERT ON journal_entries
  FOR EACH ROW EXECUTE FUNCTION check_document_entry();
