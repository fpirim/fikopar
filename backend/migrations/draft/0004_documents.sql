-- Contracts v1.4 (docs/adr/0013-contracts-v1-4.md): trade documents, lines, reservations, last purchase cost.
-- Applies after the promoted migrations 0001–0011; F3 promotes it as 0012. Type → prefix, ledger and stock effect:
-- docs/domain-model.md, "Documents". Lines marked M1, M2, M3 are mutated by check/mutations.sh.

ALTER TABLE account_roles
  DROP CONSTRAINT account_roles_role_check,
  ADD CONSTRAINT account_roles_role_check CHECK (role IN (
    'CASH','SALES','SALES_RETURNS','SALES_DISCOUNTS','PURCHASES','PURCHASE_RETURNS','PRICE_DIFFERENCE',
    'OUTPUT_VAT','INPUT_VAT','RECEIVABLE_CHEQUES','RECEIVABLE_NOTES','CHEQUES_AT_BANK',
    'PAYABLE_CHEQUES','PAYABLE_NOTES','OPENING','CASH_OVER_SHORT'));

ALTER TABLE outbox
  DROP CONSTRAINT outbox_event_type_check,
  ADD CONSTRAINT outbox_event_type_check CHECK (event_type IN ('DocumentPosted','DocumentCancelled','StockReserved',
    'StockReleased','StockMoved','LedgerEntryPosted','ReceiptPosted','InstrumentStatusChanged','PriceChanged',
    'PriceListImported'));

CREATE OR REPLACE FUNCTION is_waybill(p_type text) RETURNS boolean
LANGUAGE sql IMMUTABLE AS $$
  SELECT p_type IN ('SALES_WAYBILL','PURCHASE_WAYBILL','SALES_RETURN_WAYBILL','PURCHASE_RETURN_WAYBILL')
$$;

CREATE OR REPLACE FUNCTION is_return(p_type text) RETURNS boolean
LANGUAGE sql IMMUTABLE AS $$
  SELECT p_type IN ('SALES_RETURN','SALES_RETURN_WAYBILL','PURCHASE_RETURN','PURCHASE_RETURN_WAYBILL')
$$;

-- Stock direction of a type when it moves stock (not when it bills a waybill); NULL = never.
CREATE OR REPLACE FUNCTION document_stock_kind(p_type text) RETURNS text
LANGUAGE sql IMMUTABLE AS $$
  SELECT CASE
    WHEN p_type IN ('SALES_INVOICE_WITH_WAYBILL','SALES_INVOICE','SALES_WAYBILL',
                    'PURCHASE_RETURN','PURCHASE_RETURN_WAYBILL') THEN 'OUT'
    WHEN p_type IN ('PURCHASE_INVOICE_WITH_WAYBILL','PURCHASE_INVOICE','PURCHASE_WAYBILL',
                    'SALES_RETURN','SALES_RETURN_WAYBILL') THEN 'IN'
  END
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

-- Types whose lines a return may take back: the documents that moved the goods out (sales) or in (purchases).
CREATE OR REPLACE FUNCTION returnable_types(p_type text) RETURNS text[]
LANGUAGE sql IMMUTABLE AS $$
  SELECT CASE
    WHEN p_type IN ('SALES_RETURN','SALES_RETURN_WAYBILL')
      THEN ARRAY['SALES_INVOICE_WITH_WAYBILL','SALES_INVOICE','SALES_WAYBILL']
    WHEN p_type IN ('PURCHASE_RETURN','PURCHASE_RETURN_WAYBILL')
      THEN ARRAY['PURCHASE_INVOICE_WITH_WAYBILL','PURCHASE_INVOICE','PURCHASE_WAYBILL']
  END
$$;

CREATE TABLE documents (
  id                     bigserial     PRIMARY KEY,
  org_id                 bigint        NOT NULL REFERENCES organizations(id),
  depot_id               bigint        NOT NULL,
  document_no            text,                          -- allocated from number_series on post
  type                   text          NOT NULL CHECK (type IN (
                           'SALES_INVOICE_WITH_WAYBILL','SALES_INVOICE','SALES_WAYBILL',
                           'SALES_RETURN','SALES_RETURN_WAYBILL',
                           'PURCHASE_INVOICE_WITH_WAYBILL','PURCHASE_INVOICE','PURCHASE_WAYBILL',
                           'PURCHASE_RETURN','PURCHASE_RETURN_WAYBILL',
                           'PRICE_DIFFERENCE')),
  status                 text          NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','POSTED','CANCELLED')),
  origin                 text          NOT NULL DEFAULT 'APP' CHECK (origin IN ('APP','LEGACY')),
  sicil_id               bigint        NOT NULL,
  document_date          date          NOT NULL,
  ship_date              date,
  due_date               date,
  sales_rep_user_id      bigint        REFERENCES users(id),
  linked_document_id     bigint,                        -- original of a return or price difference
  waybill_id             bigint,                        -- waybill billed by this invoice or return
  currency               char(3)       NOT NULL DEFAULT 'TRY' CHECK (currency = 'TRY'),
  gross_total            numeric(18,2) NOT NULL DEFAULT 0 CHECK (gross_total >= 0),
  discount_total         numeric(18,2) NOT NULL DEFAULT 0 CHECK (discount_total >= 0),
  net_total              numeric(18,2) NOT NULL DEFAULT 0 CHECK (net_total >= 0),
  vat_total              numeric(18,2) NOT NULL DEFAULT 0 CHECK (vat_total >= 0),
  grand_total            numeric(18,2) NOT NULL DEFAULT 0 CHECK (grand_total >= 0),
  description            text,
  sicil_snapshot         jsonb,                         -- name, address, tax data as printed; set on post
  credit_override_reason text,
  credit_override_by     bigint        REFERENCES users(id),
  credit_override_at     timestamptz,
  credit_override_excess numeric(18,2) CHECK (credit_override_excess > 0),
  posted_at              timestamptz,
  posted_by              bigint        REFERENCES users(id),
  cancelled_at           timestamptz,
  cancelled_by           bigint        REFERENCES users(id),
  cancel_date            date,                          -- date of the reversal entry and opposite movements
  cancel_reason          text,
  version                int           NOT NULL DEFAULT 1,
  created_by             bigint        REFERENCES users(id),
  created_at             timestamptz   NOT NULL DEFAULT now(),
  updated_at             timestamptz   NOT NULL DEFAULT now(),
  UNIQUE (org_id, document_no),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, depot_id)           REFERENCES depots (org_id, id),
  FOREIGN KEY (org_id, sicil_id)           REFERENCES sicils (organization_id, id),
  FOREIGN KEY (org_id, linked_document_id) REFERENCES documents (org_id, id),
  FOREIGN KEY (org_id, waybill_id)         REFERENCES documents (org_id, id),
  CHECK (net_total = gross_total - discount_total),
  CHECK (grand_total = net_total + vat_total),
  CHECK (due_date IS NULL OR due_date >= document_date),
  CHECK ((status = 'DRAFT') = (document_no IS NULL)),
  CHECK ((status = 'DRAFT') = (posted_at IS NULL)),
  CHECK ((status = 'CANCELLED') = (cancelled_at IS NOT NULL)),
  CHECK ((status = 'CANCELLED') = (cancel_date IS NOT NULL AND cancel_reason IS NOT NULL)),
  CHECK (cancel_date IS NULL OR cancel_date >= document_date),
  CHECK (num_nonnulls(credit_override_reason, credit_override_by, credit_override_at, credit_override_excess) IN (0, 4)),
  CHECK (credit_override_reason IS NULL OR length(btrim(credit_override_reason)) >= 3),
  CHECK (credit_override_at IS NULL OR status <> 'DRAFT'),
  CHECK (type <> 'PRICE_DIFFERENCE' OR linked_document_id IS NOT NULL),
  CHECK (linked_document_id IS NULL OR is_return(type) OR type = 'PRICE_DIFFERENCE'),
  CHECK (origin = 'LEGACY' OR NOT is_return(type) OR linked_document_id IS NOT NULL),
  CHECK (waybill_id IS NULL OR type IN ('SALES_INVOICE','PURCHASE_INVOICE','SALES_RETURN','PURCHASE_RETURN')),
  CHECK (waybill_id IS NULL OR waybill_id <> id)
);
-- D4 backstop for concurrent drafts; the trigger raises the typed error first.
CREATE UNIQUE INDEX documents_waybill_once ON documents (org_id, waybill_id)
  WHERE waybill_id IS NOT NULL AND status <> 'CANCELLED';
CREATE INDEX documents_sicil_idx  ON documents (org_id, sicil_id, document_date DESC, id DESC);
CREATE INDEX documents_depot_idx  ON documents (org_id, depot_id, status, document_date DESC, id DESC);
CREATE INDEX documents_date_idx   ON documents (org_id, document_date DESC, id DESC);
CREATE INDEX documents_posted_idx ON documents (org_id, posted_at DESC, id DESC) WHERE posted_at IS NOT NULL;
CREATE INDEX documents_linked_idx ON documents (org_id, linked_document_id) WHERE linked_document_id IS NOT NULL;
CREATE INDEX documents_no_trgm    ON documents USING gin (document_no gin_trgm_ops);

-- Amounts per line, half-up (round() on numeric rounds half away from zero; amounts are never negative):
-- gross = round(qty × price, 2); net = round(qty × price × (1 − d1/100) × (1 − d2/100), 2) from the unrounded
-- product; discount = gross − net; vat = round(net × vat%/100, 2). Document totals are Σ lines.
CREATE TABLE document_lines (
  id              bigserial     PRIMARY KEY,
  org_id          bigint        NOT NULL,
  document_id     bigint        NOT NULL,
  line_no         int           NOT NULL CHECK (line_no > 0),
  part_id         bigint        NOT NULL,
  qty             numeric(18,4) NOT NULL CHECK (qty > 0),
  unit_price      numeric(18,4) NOT NULL CHECK (unit_price >= 0),
  discount1_pct   numeric(7,4)  NOT NULL DEFAULT 0 CHECK (discount1_pct BETWEEN 0 AND 100),
  discount2_pct   numeric(7,4)  NOT NULL DEFAULT 0 CHECK (discount2_pct BETWEEN 0 AND 100),
  vat_pct         numeric(7,4)  NOT NULL DEFAULT 0 CHECK (vat_pct BETWEEN 0 AND 100),
  gross_amount    numeric(18,2) NOT NULL,
  discount_amount numeric(18,2) NOT NULL,
  net_amount      numeric(18,2) NOT NULL,
  vat_amount      numeric(18,2) NOT NULL,
  origin_line_id  bigint,                               -- line returned (returns) or re-priced (price difference)
  billed_line_id  bigint,                               -- waybill line billed by this line
  UNIQUE (document_id, line_no),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, document_id)    REFERENCES documents (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, part_id)        REFERENCES parts (org_id, id),
  FOREIGN KEY (org_id, origin_line_id) REFERENCES document_lines (org_id, id),
  FOREIGN KEY (org_id, billed_line_id) REFERENCES document_lines (org_id, id),
  CHECK (gross_amount = round(qty * unit_price, 2)),
  CHECK (net_amount = round(qty * unit_price * (100 - discount1_pct) * (100 - discount2_pct) * 0.0001, 2)),
  CHECK (discount_amount = gross_amount - net_amount),
  CHECK (vat_amount = round(net_amount * vat_pct * 0.01, 2))
);
CREATE INDEX document_lines_part_idx   ON document_lines (org_id, part_id, document_id);
CREATE INDEX document_lines_origin_idx ON document_lines (org_id, origin_line_id) WHERE origin_line_id IS NOT NULL;
CREATE INDEX document_lines_billed_idx ON document_lines (org_id, billed_line_id) WHERE billed_line_id IS NOT NULL;

-- D1: DRAFT → POSTED → CANCELLED. A DRAFT is edited or deleted; POSTED is fixed except for its cancellation.
CREATE OR REPLACE FUNCTION check_document_transition() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF TG_OP = 'DELETE' THEN
    IF OLD.status <> 'DRAFT' THEN
      RAISE EXCEPTION 'document % is % and cannot be deleted', OLD.id, OLD.status USING ERRCODE = 'restrict_violation';
    END IF;
    RETURN OLD;
  END IF;
  IF OLD.status <> 'DRAFT' AND NEW.status = OLD.status THEN
    RAISE EXCEPTION 'document % is % and cannot be edited', OLD.id, OLD.status USING ERRCODE = 'check_violation';
  END IF;
  IF NEW.status <> OLD.status AND NOT (
       (OLD.status = 'DRAFT'  AND NEW.status = 'POSTED')
    OR (OLD.status = 'POSTED' AND NEW.status = 'CANCELLED')) THEN
    RAISE EXCEPTION 'ERR_INVALID_STATE_TRANSITION: document % % -> %', OLD.id, OLD.status, NEW.status
      USING ERRCODE = 'check_violation';
  END IF;
  IF OLD.status = 'POSTED' AND (NEW.document_no, NEW.type, NEW.origin, NEW.sicil_id, NEW.depot_id, NEW.document_date,
       NEW.due_date, NEW.linked_document_id, NEW.waybill_id, NEW.gross_total, NEW.discount_total, NEW.vat_total,
       NEW.posted_at, NEW.credit_override_at)
     IS DISTINCT FROM (OLD.document_no, OLD.type, OLD.origin, OLD.sicil_id, OLD.depot_id, OLD.document_date,
       OLD.due_date, OLD.linked_document_id, OLD.waybill_id, OLD.gross_total, OLD.discount_total, OLD.vat_total,
       OLD.posted_at, OLD.credit_override_at) THEN
    RAISE EXCEPTION 'document % is POSTED; cancelling changes the cancel fields only', OLD.id
      USING ERRCODE = 'check_violation';
  END IF;
  IF NEW.version <> OLD.version + 1 THEN
    RAISE EXCEPTION 'document % update must bump version', OLD.id USING ERRCODE = 'check_violation';
  END IF;
  IF NEW.depot_id <> OLD.depot_id AND EXISTS (SELECT 1 FROM stock_reservations WHERE document_id = OLD.id) THEN
    RAISE EXCEPTION 'document % holds reservations in depot %', OLD.id, OLD.depot_id USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;

-- Lines change only while their document is a DRAFT (a cascade from a deleted draft finds no parent).
CREATE OR REPLACE FUNCTION check_document_line_change() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE v_status text;
BEGIN
  SELECT status INTO v_status FROM documents
   WHERE org_id = coalesce(NEW.org_id, OLD.org_id) AND id = coalesce(NEW.document_id, OLD.document_id);
  IF v_status IS NOT NULL AND v_status <> 'DRAFT' THEN
    RAISE EXCEPTION 'ERR_DOCUMENT_NOT_DRAFT: lines of document % are fixed', coalesce(NEW.document_id, OLD.document_id)
      USING ERRCODE = 'check_violation';
  END IF;
  RETURN coalesce(NEW, OLD);
END $$;

CREATE TRIGGER document_lines_draft_only
  BEFORE INSERT OR UPDATE OR DELETE ON document_lines
  FOR EACH ROW EXECUTE FUNCTION check_document_line_change();

-- D4: a bill matches its waybill (type, sicil, depot), bills every waybill line once with the same part and
-- quantity, needs the waybill POSTED, and a waybill is billed by one non-cancelled document.
CREATE OR REPLACE FUNCTION check_document_waybill() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE w documents%ROWTYPE;
BEGIN
  IF NEW.waybill_id IS NOT NULL AND NEW.status <> 'CANCELLED' THEN
    SELECT * INTO w FROM documents WHERE org_id = NEW.org_id AND id = NEW.waybill_id;
    IF w.type IS DISTINCT FROM billable_waybill_type(NEW.type)
       OR w.sicil_id <> NEW.sicil_id OR w.depot_id <> NEW.depot_id THEN
      RAISE EXCEPTION 'ERR_WAYBILL_INVALID: document % cannot bill document % (%)', NEW.id, NEW.waybill_id, w.type
        USING ERRCODE = 'check_violation';
    END IF;
    IF EXISTS (SELECT 1 FROM documents b                                                           -- M1
               WHERE b.org_id = NEW.org_id AND b.waybill_id = NEW.waybill_id                       -- M1
                 AND b.id <> NEW.id AND b.status <> 'CANCELLED') THEN                              -- M1
      RAISE EXCEPTION 'ERR_WAYBILL_BILLED: waybill % is billed already', NEW.waybill_id            -- M1
        USING ERRCODE = 'check_violation';                                                         -- M1
    END IF;                                                                                        -- M1
    IF NEW.status = 'POSTED' AND (TG_OP = 'INSERT' OR OLD.status = 'DRAFT') THEN
      IF w.status <> 'POSTED' THEN
        RAISE EXCEPTION 'ERR_WAYBILL_INVALID: waybill % is %', w.id, w.status USING ERRCODE = 'check_violation';
      END IF;
      IF EXISTS (  -- every bill line points to a waybill line with the same part and quantity, each once
           SELECT 1 FROM document_lines l
           LEFT JOIN document_lines wl ON wl.org_id = l.org_id AND wl.id = l.billed_line_id
          WHERE l.org_id = NEW.org_id AND l.document_id = NEW.id
            AND (wl.id IS NULL OR wl.document_id <> w.id OR wl.part_id <> l.part_id OR wl.qty <> l.qty))
         OR EXISTS (
           SELECT 1 FROM document_lines wl
          WHERE wl.org_id = NEW.org_id AND wl.document_id = w.id
            AND (SELECT count(*) FROM document_lines l
                  WHERE l.org_id = NEW.org_id AND l.document_id = NEW.id AND l.billed_line_id = wl.id) <> 1) THEN
        RAISE EXCEPTION 'ERR_WAYBILL_INVALID: lines of % do not bill waybill % line for line', NEW.id, w.id
          USING ERRCODE = 'check_violation';
      END IF;
    END IF;
  END IF;
  IF TG_OP = 'UPDATE' AND is_waybill(NEW.type) AND NEW.status = 'CANCELLED' AND OLD.status <> 'CANCELLED'
     AND EXISTS (SELECT 1 FROM documents b
                 WHERE b.org_id = NEW.org_id AND b.waybill_id = NEW.id AND b.status <> 'CANCELLED') THEN
    RAISE EXCEPTION 'ERR_WAYBILL_BILLED: waybill % is billed and cannot be cancelled', NEW.id
      USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;

-- On post: lines, totals = Σ lines, no reservation left, open year; links of returns and price differences;
-- D6 return cap. On cancel: nothing posted may still point at it.
CREATE OR REPLACE FUNCTION check_document_post() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
  o     documents%ROWTYPE;
  t     record;
  v_bad bigint;
BEGIN
  IF TG_OP = 'UPDATE' AND OLD.status = 'POSTED' AND NEW.status = 'CANCELLED' THEN
    IF EXISTS (SELECT 1 FROM documents x
               WHERE x.org_id = NEW.org_id AND x.linked_document_id = NEW.id AND x.status = 'POSTED') THEN
      RAISE EXCEPTION 'ERR_DOCUMENT_HAS_DEPENDENTS: posted returns or price differences point at %', NEW.id
        USING ERRCODE = 'check_violation';
    END IF;
    IF EXISTS (SELECT 1 FROM carry_forward_runs WHERE org_id = NEW.org_id AND reopened_at IS NULL
                AND year > extract(year FROM NEW.cancel_date)::int) THEN
      RAISE EXCEPTION 'ERR_YEAR_CLOSED: % is in a closed year', NEW.cancel_date USING ERRCODE = 'check_violation';
    END IF;
    RETURN NEW;
  END IF;
  IF NOT (NEW.status = 'POSTED' AND (TG_OP = 'INSERT' OR OLD.status = 'DRAFT')) THEN
    RETURN NEW;
  END IF;

  SELECT count(*) AS n, coalesce(sum(gross_amount), 0) AS g, coalesce(sum(discount_amount), 0) AS d,
         coalesce(sum(vat_amount), 0) AS v
    INTO t FROM document_lines WHERE org_id = NEW.org_id AND document_id = NEW.id;
  IF t.n = 0 THEN
    RAISE EXCEPTION 'ERR_DOCUMENT_EMPTY: document % has no lines', NEW.id USING ERRCODE = 'check_violation';
  END IF;
  IF (t.g, t.d, t.v) IS DISTINCT FROM (NEW.gross_total, NEW.discount_total, NEW.vat_total) THEN
    RAISE EXCEPTION 'document % totals are not the sum of its lines', NEW.id USING ERRCODE = 'check_violation';
  END IF;
  IF EXISTS (SELECT 1 FROM stock_reservations WHERE document_id = NEW.id) THEN
    RAISE EXCEPTION 'document % still holds reservations', NEW.id USING ERRCODE = 'check_violation';
  END IF;
  IF EXISTS (SELECT 1 FROM carry_forward_runs WHERE org_id = NEW.org_id AND reopened_at IS NULL
              AND year > extract(year FROM NEW.document_date)::int) THEN
    RAISE EXCEPTION 'ERR_YEAR_CLOSED: % is in a closed year', NEW.document_date USING ERRCODE = 'check_violation';
  END IF;
  IF NEW.origin = 'LEGACY' THEN
    RETURN NEW;
  END IF;

  IF NOT is_return(NEW.type) AND NEW.type <> 'PRICE_DIFFERENCE' THEN
    IF EXISTS (SELECT 1 FROM document_lines WHERE org_id = NEW.org_id AND document_id = NEW.id
                AND origin_line_id IS NOT NULL) THEN
      RAISE EXCEPTION 'ERR_LINKED_DOCUMENT_INVALID: % has no original', NEW.id USING ERRCODE = 'check_violation';
    END IF;
    RETURN NEW;
  END IF;

  -- The original is locked, so concurrent returns of it post one after the other.
  SELECT * INTO o FROM documents WHERE org_id = NEW.org_id AND id = NEW.linked_document_id FOR UPDATE;
  IF o.status IS DISTINCT FROM 'POSTED' OR o.sicil_id <> NEW.sicil_id
     OR (NEW.type = 'PRICE_DIFFERENCE' AND (o.depot_id <> NEW.depot_id OR o.type NOT IN
         ('SALES_INVOICE_WITH_WAYBILL','SALES_INVOICE','PURCHASE_INVOICE_WITH_WAYBILL','PURCHASE_INVOICE')))
     OR (is_return(NEW.type) AND (o.type <> ALL (returnable_types(NEW.type)) OR o.waybill_id IS NOT NULL)) THEN
    RAISE EXCEPTION 'ERR_LINKED_DOCUMENT_INVALID: % cannot link %', NEW.id, NEW.linked_document_id
      USING ERRCODE = 'check_violation';
  END IF;
  SELECT l.id INTO v_bad FROM document_lines l
    LEFT JOIN document_lines ol ON ol.org_id = l.org_id AND ol.id = l.origin_line_id
   WHERE l.org_id = NEW.org_id AND l.document_id = NEW.id
     AND (ol.id IS NULL OR ol.document_id <> o.id OR ol.part_id <> l.part_id)
   LIMIT 1;
  IF v_bad IS NOT NULL THEN
    RAISE EXCEPTION 'ERR_LINKED_DOCUMENT_INVALID: line % has no original line in %', v_bad, o.id
      USING ERRCODE = 'check_violation';
  END IF;

  IF is_return(NEW.type) AND NEW.waybill_id IS NOT NULL THEN
    -- A return that bills a return waybill takes back what the waybill took back; it is not counted again.
    IF EXISTS (SELECT 1 FROM document_lines l
                 JOIN document_lines wl ON wl.org_id = l.org_id AND wl.id = l.billed_line_id
                WHERE l.org_id = NEW.org_id AND l.document_id = NEW.id
                  AND wl.origin_line_id IS DISTINCT FROM l.origin_line_id) THEN
      RAISE EXCEPTION 'ERR_WAYBILL_INVALID: % and its return waybill return different lines', NEW.id
        USING ERRCODE = 'check_violation';
    END IF;
  ELSIF is_return(NEW.type) THEN
    -- D6: per original line, Σ qty of posted goods-moving returns (this one included) ≤ original qty.
    SELECT ol.id INTO v_bad
      FROM document_lines ol
     WHERE ol.org_id = NEW.org_id AND ol.document_id = o.id
       AND ol.id IN (SELECT origin_line_id FROM document_lines WHERE org_id = NEW.org_id AND document_id = NEW.id)
       AND ol.qty < (SELECT coalesce(sum(rl.qty), 0)                                              -- M2
                       FROM document_lines rl JOIN documents r ON r.org_id = rl.org_id AND r.id = rl.document_id
                      WHERE rl.org_id = NEW.org_id AND rl.origin_line_id = ol.id AND is_return(r.type)
                        AND r.waybill_id IS NULL AND (r.id = NEW.id OR r.status = 'POSTED'))
     LIMIT 1;
    IF v_bad IS NOT NULL THEN
      RAISE EXCEPTION 'ERR_RETURN_QTY_EXCEEDED: original line % is returned beyond its quantity', v_bad
        USING ERRCODE = 'check_violation';
    END IF;
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER documents_state_machine
  BEFORE UPDATE OR DELETE ON documents
  FOR EACH ROW EXECUTE FUNCTION check_document_transition();
CREATE TRIGGER documents_waybill_rules
  BEFORE INSERT OR UPDATE ON documents
  FOR EACH ROW EXECUTE FUNCTION check_document_waybill();
CREATE TRIGGER documents_post_rules
  BEFORE INSERT OR UPDATE ON documents
  FOR EACH ROW EXECUTE FUNCTION check_document_post();

-- D2: waybills never reach the ledger; a document entry needs a posted (or since cancelled) document, on its date.
CREATE OR REPLACE FUNCTION check_document_entry() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE d documents%ROWTYPE;
BEGIN
  IF NEW.source_type = 'document' THEN
    SELECT * INTO d FROM documents WHERE org_id = NEW.org_id AND id = NEW.source_id::bigint;
    IF d.id IS NULL OR is_waybill(d.type) OR d.status = 'DRAFT'
       OR d.depot_id <> NEW.depot_id OR d.document_date <> NEW.entry_date THEN
      RAISE EXCEPTION 'document % (%) cannot post this entry', NEW.source_id, coalesce(d.type, 'missing')
        USING ERRCODE = 'check_violation';
    END IF;
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER journal_entries_document_rules
  BEFORE INSERT ON journal_entries
  FOR EACH ROW EXECUTE FUNCTION check_document_entry();

-- D2/D3/D4: a document line moves stock once in the type's direction, on the document date and depot, with the
-- line's part and quantity; after cancellation once more in the opposite direction on the cancel date. Price
-- differences and documents that bill a waybill never move stock.
CREATE OR REPLACE FUNCTION check_document_movement() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
  d      documents%ROWTYPE;
  l      document_lines%ROWTYPE;
  v_kind text;
BEGIN
  IF NEW.source_type <> 'document' THEN
    RETURN NEW;
  END IF;
  SELECT * INTO l FROM document_lines WHERE org_id = NEW.org_id AND id = NEW.source_id::bigint;
  SELECT * INTO d FROM documents WHERE org_id = NEW.org_id AND id = l.document_id;
  v_kind := document_stock_kind(d.type);
  IF d.id IS NULL OR v_kind IS NULL OR d.waybill_id IS NOT NULL OR d.status = 'DRAFT'
     OR NEW.part_id <> l.part_id OR NEW.qty <> l.qty OR NEW.depot_id <> d.depot_id THEN
    RAISE EXCEPTION 'document line % (%) cannot move stock', NEW.source_id, coalesce(d.type, 'missing')
      USING ERRCODE = 'check_violation';
  END IF;
  IF NEW.kind = v_kind AND NEW.movement_date = d.document_date THEN
    RETURN NEW;
  END IF;
  IF NEW.kind <> v_kind AND NEW.kind IN ('IN','OUT') AND d.status = 'CANCELLED' AND NEW.movement_date = d.cancel_date
     AND EXISTS (SELECT 1 FROM stock_movements m WHERE m.org_id = NEW.org_id AND m.source_type = 'document'
                  AND m.source_id = NEW.source_id AND m.kind = v_kind) THEN
    RETURN NEW;
  END IF;
  RAISE EXCEPTION 'document line % cannot move % on %', NEW.source_id, NEW.kind, NEW.movement_date
    USING ERRCODE = 'check_violation';
END $$;

CREATE TRIGGER stock_movements_document_rules
  BEFORE INSERT ON stock_movements
  FOR EACH ROW EXECUTE FUNCTION check_document_movement();

-- D8 reservations. A draft that will move stock OUT holds, per line, what was available when it was saved.
-- Rows exist only while held: release = DELETE (post, delete, line change, sweeper). stock_levels.reserved is their
-- projection, written by apply_stock_reservation(); a reservation never takes more than available.
CREATE TABLE stock_reservations (
  id          bigserial     PRIMARY KEY,
  org_id      bigint        NOT NULL,
  document_id bigint        NOT NULL,
  line_id     bigint        NOT NULL UNIQUE,
  part_id     bigint        NOT NULL,
  depot_id    bigint        NOT NULL,
  qty         numeric(18,4) NOT NULL CHECK (qty > 0),
  reserved_at timestamptz   NOT NULL DEFAULT clock_timestamp(),
  expires_at  timestamptz   NOT NULL,
  FOREIGN KEY (org_id, document_id) REFERENCES documents (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, line_id)     REFERENCES document_lines (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, part_id)     REFERENCES parts (org_id, id),
  FOREIGN KEY (org_id, depot_id)    REFERENCES depots (org_id, id),
  CHECK (expires_at > reserved_at)
);
CREATE INDEX stock_reservations_expiry_idx   ON stock_reservations (expires_at);
CREATE INDEX stock_reservations_document_idx ON stock_reservations (document_id);
CREATE INDEX stock_reservations_level_idx    ON stock_reservations (part_id, depot_id);

CREATE OR REPLACE FUNCTION check_stock_reservation() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
  d documents%ROWTYPE;
  l document_lines%ROWTYPE;
BEGIN
  IF TG_OP = 'UPDATE' THEN
    IF (NEW.id, NEW.org_id, NEW.document_id, NEW.line_id, NEW.part_id, NEW.depot_id, NEW.qty, NEW.reserved_at)
       IS DISTINCT FROM (OLD.id, OLD.org_id, OLD.document_id, OLD.line_id, OLD.part_id, OLD.depot_id, OLD.qty,
                         OLD.reserved_at) THEN
      RAISE EXCEPTION 'reservation %: only expires_at changes', OLD.id USING ERRCODE = 'restrict_violation';
    END IF;
    RETURN NEW;
  END IF;
  SELECT * INTO l FROM document_lines WHERE org_id = NEW.org_id AND id = NEW.line_id;
  SELECT * INTO d FROM documents WHERE org_id = NEW.org_id AND id = l.document_id;
  IF d.id IS DISTINCT FROM NEW.document_id OR d.status <> 'DRAFT' OR document_stock_kind(d.type) IS DISTINCT FROM 'OUT'
     OR d.waybill_id IS NOT NULL OR NEW.part_id <> l.part_id OR NEW.depot_id <> d.depot_id OR NEW.qty > l.qty THEN
    RAISE EXCEPTION 'line % cannot reserve %', NEW.line_id, NEW.qty USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER stock_reservations_rules
  BEFORE INSERT OR UPDATE ON stock_reservations
  FOR EACH ROW EXECUTE FUNCTION check_stock_reservation();

CREATE OR REPLACE FUNCTION apply_stock_reservation() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    UPDATE stock_levels AS l SET reserved = l.reserved + NEW.qty, version = l.version + 1
     WHERE l.part_id = NEW.part_id AND l.depot_id = NEW.depot_id
       AND l.reserved + NEW.qty <= l.on_hand;                                                   -- M3
    IF NOT FOUND THEN
      RAISE EXCEPTION 'ERR_RESERVATION_EXCEEDS_AVAILABLE: part % depot % qty %', NEW.part_id, NEW.depot_id, NEW.qty
        USING ERRCODE = 'check_violation';
    END IF;
  ELSE
    UPDATE stock_levels AS l SET reserved = l.reserved - OLD.qty, version = l.version + 1
     WHERE l.part_id = OLD.part_id AND l.depot_id = OLD.depot_id;
  END IF;
  RETURN NULL;
END $$;
REVOKE ALL ON FUNCTION apply_stock_reservation() FROM PUBLIC;

CREATE TRIGGER stock_reservations_apply
  AFTER INSERT OR DELETE ON stock_reservations
  FOR EACH ROW EXECUTE FUNCTION apply_stock_reservation();

-- The stock_levels guard (0011) also admits the reservation projection, which may change `reserved` only.
CREATE OR REPLACE FUNCTION stock_levels_guard() RETURNS trigger
LANGUAGE plpgsql SET search_path = public, pg_temp AS $$
DECLARE
  stack text;
BEGIN
  IF TG_OP <> 'TRUNCATE' THEN
    GET DIAGNOSTICS stack = PG_CONTEXT;
    IF stack ~ ('^PL/pgSQL function stock_levels_guard\(\) line \d+ at GET DIAGNOSTICS\n'
               'SQL statement "INSERT INTO stock_levels AS l [^"]*"\n'
               'PL/pgSQL function apply_stock_movement\(\) line \d+ at SQL statement(\n|$)') THEN
      RETURN CASE TG_OP WHEN 'DELETE' THEN OLD ELSE NEW END;
    END IF;
    IF TG_OP = 'UPDATE' AND NEW.on_hand = OLD.on_hand AND NEW.part_id = OLD.part_id AND NEW.depot_id = OLD.depot_id
       AND stack ~ ('^PL/pgSQL function stock_levels_guard\(\) line \d+ at GET DIAGNOSTICS\n'
                    'SQL statement "UPDATE stock_levels AS l [^"]*"\n'
                    'PL/pgSQL function apply_stock_reservation\(\) line \d+ at SQL statement(\n|$)') THEN
      RETURN NEW;
    END IF;
  END IF;
  RAISE EXCEPTION 'stock_levels is written by the movement and reservation projections only'
    USING ERRCODE = 'insufficient_privilege';
END $$;

CREATE VIEW stock_reservation_drift AS
SELECT l.part_id, l.depot_id, l.reserved - coalesce(r.q, 0) AS drift
FROM stock_levels l
LEFT JOIN (SELECT part_id, depot_id, sum(qty) AS q FROM stock_reservations GROUP BY part_id, depot_id) r
  ON r.part_id = l.part_id AND r.depot_id = l.depot_id
WHERE l.reserved <> coalesce(r.q, 0);

-- Sweeper: releases reservations whose expires_at (last save + 48 h) has passed. Rows locked by a save or a post in
-- flight are skipped and picked up next run. A row is deleted once, so a rerun returns nothing (idempotent); the
-- caller emits StockReleased EXPIRED per document from the returned rows in the same transaction.
CREATE OR REPLACE FUNCTION release_expired_reservations(p_now timestamptz, p_limit int)
RETURNS TABLE (org_id bigint, document_id bigint, line_id bigint, part_id bigint, depot_id bigint, qty numeric(18,4))
LANGUAGE sql AS $$
  DELETE FROM stock_reservations r
   WHERE r.id IN (SELECT x.id FROM stock_reservations x WHERE x.expires_at <= p_now
                  ORDER BY x.expires_at, x.id LIMIT p_limit FOR UPDATE SKIP LOCKED)
  RETURNING r.org_id, r.document_id, r.line_id, r.part_id, r.depot_id, r.qty
$$;

-- Count valuation (ADR-0008, v1.4). Last purchase cost = net_amount / qty of the latest POSTED purchase invoice
-- line for the part in the company, any depot, dated on or before p_date (document_date, then posted_at, then line
-- id). Bills of a purchase waybill count; returns, waybills, price differences and cancelled invoices do not.
-- Fallback: the list price of the part's catalog on p_date (LIST_PRICE); neither → NONE.
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

REVOKE TRUNCATE ON documents, document_lines, stock_reservations FROM fikopar_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON documents, document_lines, stock_reservations TO fikopar_app;
GRANT SELECT ON stock_reservation_drift TO fikopar_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO fikopar_app;
