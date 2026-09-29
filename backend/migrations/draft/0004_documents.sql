-- Trade documents (invoices, waybills, returns, price differences). Posting emits DocumentPosted via outbox.
-- Type → prefix, ledger and stock effect: docs/domain-model.md, "Documents".

CREATE TABLE documents (
  id                 bigserial     PRIMARY KEY,
  org_id             bigint        NOT NULL REFERENCES organizations(id),
  depot_id           bigint        NOT NULL,
  document_no        text,                          -- allocated from number_series on post
  type               text          NOT NULL CHECK (type IN (
                       'SALES_INVOICE_WITH_WAYBILL','SALES_INVOICE','SALES_WAYBILL',
                       'SALES_RETURN','SALES_RETURN_WAYBILL',
                       'PURCHASE_INVOICE_WITH_WAYBILL','PURCHASE_INVOICE','PURCHASE_WAYBILL',
                       'PURCHASE_RETURN','PURCHASE_RETURN_WAYBILL',
                       'PRICE_DIFFERENCE')),
  status             text          NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','POSTED','CANCELLED')),
  sicil_id           bigint        NOT NULL,
  document_date      date          NOT NULL,
  ship_date          date,
  due_date           date,
  sales_rep_user_id  bigint        REFERENCES users(id),
  linked_document_id bigint,                        -- original of a return, return waybill or price difference
  waybill_id         bigint,                        -- waybill billed by this invoice or return
  currency           char(3)       NOT NULL DEFAULT 'TRY',
  gross_total        numeric(18,2) NOT NULL DEFAULT 0 CHECK (gross_total >= 0),
  discount_total     numeric(18,2) NOT NULL DEFAULT 0 CHECK (discount_total >= 0),
  net_total          numeric(18,2) NOT NULL DEFAULT 0 CHECK (net_total >= 0),
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
  FOREIGN KEY (org_id, sicil_id)           REFERENCES sicils (organization_id, id),
  FOREIGN KEY (org_id, linked_document_id) REFERENCES documents (org_id, id),
  FOREIGN KEY (org_id, waybill_id)         REFERENCES documents (org_id, id),
  UNIQUE (org_id, waybill_id),
  CHECK (net_total = gross_total - discount_total),
  CHECK (grand_total = net_total + vat_total),
  CHECK (due_date IS NULL OR due_date >= document_date),
  CHECK ((status = 'DRAFT') = (document_no IS NULL)),
  CHECK ((status = 'DRAFT') = (posted_at IS NULL) OR status = 'CANCELLED'),
  CHECK ((status = 'CANCELLED') = (cancelled_at IS NOT NULL)),
  CHECK (type <> 'PRICE_DIFFERENCE' OR linked_document_id IS NOT NULL),
  CHECK (waybill_id IS NULL OR type IN ('SALES_INVOICE','PURCHASE_INVOICE','SALES_RETURN','PURCHASE_RETURN'))
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

-- The waybill type an invoice or return may bill.
CREATE OR REPLACE FUNCTION billable_waybill_type(p_type text) RETURNS text
LANGUAGE sql IMMUTABLE AS $$
  SELECT CASE p_type
    WHEN 'SALES_INVOICE'    THEN 'SALES_WAYBILL'
    WHEN 'PURCHASE_INVOICE' THEN 'PURCHASE_WAYBILL'
    WHEN 'SALES_RETURN'     THEN 'SALES_RETURN_WAYBILL'
    WHEN 'PURCHASE_RETURN'  THEN 'PURCHASE_RETURN_WAYBILL'
  END
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

-- D4: a billed waybill matches type, sicil and depot, is POSTED when the bill posts, and stays POSTED while billed.
CREATE OR REPLACE FUNCTION check_document_waybill() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE w documents%ROWTYPE;
BEGIN
  IF NEW.waybill_id IS NOT NULL THEN
    SELECT * INTO w FROM documents WHERE org_id = NEW.org_id AND id = NEW.waybill_id;
    IF w.type IS DISTINCT FROM billable_waybill_type(NEW.type)
       OR w.sicil_id <> NEW.sicil_id OR w.depot_id <> NEW.depot_id THEN
      RAISE EXCEPTION 'document % cannot bill document % (%)', NEW.id, NEW.waybill_id, w.type
        USING ERRCODE = 'check_violation';
    END IF;
    IF NEW.status = 'POSTED' AND w.status <> 'POSTED' THEN
      RAISE EXCEPTION 'waybill % is %', w.id, w.status USING ERRCODE = 'check_violation';
    END IF;
  END IF;
  IF TG_OP = 'UPDATE' AND is_waybill(NEW.type) AND NEW.status = 'CANCELLED' AND OLD.status <> 'CANCELLED'
     AND EXISTS (SELECT 1 FROM documents b
                 WHERE b.org_id = NEW.org_id AND b.waybill_id = NEW.id AND b.status <> 'CANCELLED') THEN
    RAISE EXCEPTION 'waybill % is billed and cannot be cancelled', NEW.id USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER documents_waybill_rules
  BEFORE INSERT OR UPDATE ON documents
  FOR EACH ROW EXECUTE FUNCTION check_document_waybill();

-- D2/D4: stock moves once, from the waybill. Price differences and documents that bill a waybill never move stock.
CREATE OR REPLACE FUNCTION check_document_movement() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE d documents%ROWTYPE;
BEGIN
  IF NEW.source_type = 'document' THEN
    SELECT doc.* INTO d
      FROM document_lines l JOIN documents doc ON doc.org_id = l.org_id AND doc.id = l.document_id
     WHERE l.org_id = NEW.org_id AND l.id = NEW.source_id::bigint;
    IF d.id IS NULL OR d.status = 'DRAFT' OR d.type = 'PRICE_DIFFERENCE' OR d.waybill_id IS NOT NULL THEN
      RAISE EXCEPTION 'document line % (%) cannot move stock', NEW.source_id, coalesce(d.type, 'missing')
        USING ERRCODE = 'check_violation';
    END IF;
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER stock_movements_document_rules
  BEFORE INSERT ON stock_movements
  FOR EACH ROW EXECUTE FUNCTION check_document_movement();

-- Count valuation. Last purchase cost = net_amount / qty of the latest POSTED purchase invoice line for the part in
-- the company, any depot (document_date, then posted_at, then line id). Returns and price differences do not count.
-- Fallback: the list price of the part's catalog valid on p_date, flagged LIST_PRICE; neither → NONE.
CREATE OR REPLACE FUNCTION last_purchase_cost(p_org bigint, p_part bigint, p_date date)
RETURNS TABLE (unit_cost numeric(18,4), cost_source text)
LANGUAGE sql STABLE AS $$
  SELECT c.unit_cost, c.cost_source FROM (
    (SELECT round(l.net_amount / l.qty, 4), 'LAST_PURCHASE', 1
       FROM document_lines l
       JOIN documents d ON d.org_id = l.org_id AND d.id = l.document_id
      WHERE l.org_id = p_org AND l.part_id = p_part
        AND d.type IN ('PURCHASE_INVOICE_WITH_WAYBILL','PURCHASE_INVOICE')
        AND d.status = 'POSTED' AND d.document_date <= p_date
      ORDER BY d.document_date DESC, d.posted_at DESC, l.id DESC
      LIMIT 1)
    UNION ALL
    (SELECT p.price, 'LIST_PRICE', 2 FROM price_on(p_org, p_part, p_date) p)
    UNION ALL
    SELECT NULL, 'NONE', 3
  ) AS c(unit_cost, cost_source, rank)
  ORDER BY c.rank
  LIMIT 1
$$;
