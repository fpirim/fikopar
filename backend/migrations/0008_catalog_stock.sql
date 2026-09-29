-- Contracts v1.2: catalogs as price lists, dated prices, price imports, stock ledger with a projected level,
-- negative stock, counts frozen at open (docs/adr/0011-contracts-v1-2.md, migrations/draft/0003_catalog_stock.sql).

CREATE EXTENSION IF NOT EXISTS pg_trgm;

ALTER TABLE depots DROP COLUMN allow_negative_stock;

-- Catalog events are company-wide.
ALTER TABLE outbox
  ALTER COLUMN depot_id DROP NOT NULL,
  DROP CONSTRAINT outbox_event_type_check,
  ADD CONSTRAINT outbox_event_type_check CHECK (event_type IN ('DocumentPosted','StockMoved','LedgerEntryPosted',
    'ReceiptPosted','InstrumentStatusChanged','PriceChanged','PriceListImported'));

CREATE TABLE catalogs (
  id                bigserial   PRIMARY KEY,
  org_id            bigint      NOT NULL REFERENCES organizations(id),
  code              int         NOT NULL CHECK (code BETWEEN 1 AND 9999),
  name              text        NOT NULL CHECK (name <> '' AND length(name) <= 60),
  supplier_sicil_id bigint,
  currency          char(3)     NOT NULL DEFAULT 'TRY' CHECK (currency = 'TRY'),
  is_active         boolean     NOT NULL DEFAULT true,
  content_version   bigint      NOT NULL DEFAULT 0,
  version           int         NOT NULL DEFAULT 1,
  created_at        timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, code),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, supplier_sicil_id) REFERENCES sicils (organization_id, id)
);

CREATE TABLE parts (
  id            bigserial     PRIMARY KEY,
  org_id        bigint        NOT NULL REFERENCES organizations(id),
  catalog_id    bigint        NOT NULL,
  stock_no      text          NOT NULL CHECK (stock_no <> '' AND length(stock_no) <= 15),
  stock_no_norm text          GENERATED ALWAYS AS (upper(regexp_replace(stock_no, '[^0-9A-Za-z]', '', 'g'))) STORED,
  name          text          NOT NULL CHECK (length(name) <= 60),
  -- Case- and dotless-i-insensitive name for word search.
  name_fold     text          GENERATED ALWAYS AS (lower(translate(name, 'IİıŞĞÜÖÇ', 'iiişğüöç'))) STORED,
  type          text,
  brand         text,
  unit          text          NOT NULL DEFAULT 'PCS',
  vat_pct       numeric(7,4)  NOT NULL DEFAULT 20 CHECK (vat_pct BETWEEN 0 AND 100),
  package_qty   int           NOT NULL DEFAULT 1 CHECK (package_qty > 0),
  is_active     boolean       NOT NULL DEFAULT true,
  version       int           NOT NULL DEFAULT 1,
  created_at    timestamptz   NOT NULL DEFAULT now(),
  updated_at    timestamptz   NOT NULL DEFAULT now(),
  UNIQUE (org_id, catalog_id, stock_no),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, catalog_id) REFERENCES catalogs (org_id, id)
);
CREATE INDEX parts_stock_no_prefix ON parts (org_id, stock_no_norm text_pattern_ops);
CREATE INDEX parts_stock_no_trgm   ON parts USING gin (stock_no_norm gin_trgm_ops);
CREATE INDEX parts_name_trgm       ON parts USING gin (name_fold gin_trgm_ops);
CREATE INDEX parts_catalog_idx     ON parts (org_id, catalog_id, stock_no);

CREATE TABLE part_depots (
  org_id   bigint        NOT NULL,
  part_id  bigint        NOT NULL,
  depot_id bigint        NOT NULL,
  min_qty  numeric(18,4) NOT NULL DEFAULT 0 CHECK (min_qty >= 0),
  max_qty  numeric(18,4) CHECK (max_qty IS NULL OR max_qty >= min_qty),
  shelf    text          NOT NULL DEFAULT '' CHECK (length(shelf) <= 20),
  version  int           NOT NULL DEFAULT 1,
  PRIMARY KEY (part_id, depot_id),
  FOREIGN KEY (org_id, part_id)  REFERENCES parts (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, depot_id) REFERENCES depots (org_id, id)
);
CREATE INDEX part_depots_shelf_idx ON part_depots (org_id, depot_id, shelf);

CREATE TABLE part_codes (
  id                 bigserial PRIMARY KEY,
  org_id             bigint    NOT NULL,
  part_id            bigint    NOT NULL,
  kind               text      NOT NULL CHECK (kind IN ('MANUFACTURER','OEM','ALT','EQUIVALENT')),
  code               text      NOT NULL CHECK (length(code) BETWEEN 1 AND 40),
  normalized_code    text      GENERATED ALWAYS AS (upper(regexp_replace(code, '[^0-9A-Za-z]', '', 'g'))) STORED,
  equivalent_part_id bigint,
  FOREIGN KEY (org_id, part_id)            REFERENCES parts (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, equivalent_part_id) REFERENCES parts (org_id, id),
  CHECK ((kind = 'EQUIVALENT') = (equivalent_part_id IS NOT NULL)),
  CHECK (equivalent_part_id IS NULL OR equivalent_part_id <> part_id),
  UNIQUE (part_id, kind, normalized_code)
);
CREATE INDEX part_codes_prefix_idx ON part_codes (org_id, normalized_code text_pattern_ops);
CREATE INDEX part_codes_trgm       ON part_codes USING gin (normalized_code gin_trgm_ops);
CREATE INDEX part_codes_part_idx   ON part_codes (part_id);
CREATE INDEX part_codes_equiv_idx  ON part_codes (equivalent_part_id) WHERE equivalent_part_id IS NOT NULL;

CREATE OR REPLACE FUNCTION parts_guard() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.catalog_id <> OLD.catalog_id THEN
    RAISE EXCEPTION 'part % cannot change catalog', OLD.id USING ERRCODE = 'restrict_violation';
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER parts_guard BEFORE UPDATE ON parts FOR EACH ROW EXECUTE FUNCTION parts_guard();

CREATE TABLE price_imports (
  id              bigserial   PRIMARY KEY,
  org_id          bigint      NOT NULL,
  catalog_id      bigint      NOT NULL,
  file_name       text        NOT NULL,
  file_sha256     char(64)    NOT NULL,
  file_size       int         NOT NULL CHECK (file_size BETWEEN 1 AND 10485760),
  format          text        NOT NULL CHECK (format IN ('CSV','XLSX','FIXED_WIDTH')),
  preset          text        CHECK (preset IN ('LISAKT')),
  mapping         jsonb       NOT NULL,
  valid_from      date        NOT NULL,
  missing         text        NOT NULL DEFAULT 'KEEP' CHECK (missing IN ('KEEP','DEACTIVATE')),
  status          text        NOT NULL DEFAULT 'PARSING'
                  CHECK (status IN ('PARSING','NEEDS_MAPPING','READY','FAILED','APPLYING','APPLIED','DISCARDED','EXPIRED')),
  failure_code    text,
  failure_detail  text,
  headers         jsonb       NOT NULL DEFAULT '[]',
  sample          jsonb       NOT NULL DEFAULT '[]',
  unmapped        text[]      NOT NULL DEFAULT '{}',
  catalog_version bigint,
  row_count       int         NOT NULL DEFAULT 0,
  counts          jsonb       NOT NULL DEFAULT '{}',
  result          jsonb,
  version         int         NOT NULL DEFAULT 1,
  created_by      bigint      REFERENCES users(id),
  created_at      timestamptz NOT NULL DEFAULT now(),
  expires_at      timestamptz NOT NULL DEFAULT now() + interval '7 days',
  applied_by      bigint      REFERENCES users(id),
  applied_at      timestamptz,
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, catalog_id) REFERENCES catalogs (org_id, id),
  CHECK ((status = 'APPLIED') = (applied_at IS NOT NULL)),
  CHECK ((status = 'FAILED') = (failure_code IS NOT NULL)),
  CHECK ((format = 'FIXED_WIDTH') = (preset IS NOT NULL))
);
CREATE INDEX price_imports_catalog_idx ON price_imports (org_id, catalog_id, created_at DESC);
CREATE INDEX price_imports_org_idx ON price_imports (org_id, id DESC);

CREATE OR REPLACE FUNCTION price_imports_transition() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF OLD.status IN ('APPLIED','DISCARDED','EXPIRED','FAILED') AND NEW.status IS DISTINCT FROM OLD.status
     OR OLD.status = 'APPLYING' AND NEW.status NOT IN ('APPLYING','APPLIED','READY')
     OR NEW.status = 'APPLIED' AND OLD.status <> 'APPLYING' THEN
    RAISE EXCEPTION 'import %: % -> % not allowed', OLD.id, OLD.status, NEW.status USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER price_imports_transition BEFORE UPDATE ON price_imports
  FOR EACH ROW EXECUTE FUNCTION price_imports_transition();

CREATE TABLE price_import_files (
  import_id bigint PRIMARY KEY REFERENCES price_imports(id) ON DELETE CASCADE,
  content   bytea  NOT NULL
);

CREATE TABLE price_import_rows (
  id               bigserial PRIMARY KEY,
  org_id           bigint    NOT NULL,
  import_id        bigint    NOT NULL,
  row_no           int       CHECK (row_no > 0),
  part_id          bigint,
  stock_no         text,
  parsed           jsonb     NOT NULL DEFAULT '{}',
  current          jsonb,
  changes          text[]    NOT NULL DEFAULT '{}'
                   CHECK (changes <@ ARRAY['NEW','PRICE','ATTRIBUTES','MISSING','DELETE']::text[]),
  changed_fields   text[]    NOT NULL DEFAULT '{}',
  price_change_pct numeric(9,1),
  errors           jsonb     NOT NULL DEFAULT '[]',
  warnings         jsonb     NOT NULL DEFAULT '[]',
  FOREIGN KEY (org_id, import_id) REFERENCES price_imports (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, part_id)   REFERENCES parts (org_id, id),
  CHECK ((row_no IS NULL) = ('MISSING' = ANY (changes)))
);
CREATE UNIQUE INDEX price_import_rows_no_uq ON price_import_rows (import_id, row_no) WHERE row_no IS NOT NULL;
CREATE INDEX price_import_rows_import_idx ON price_import_rows (import_id, id);
CREATE INDEX price_import_rows_changes_idx ON price_import_rows USING gin (changes);

CREATE TABLE prices (
  id         bigserial     PRIMARY KEY,
  org_id     bigint        NOT NULL,
  part_id    bigint        NOT NULL,
  price      numeric(18,4) NOT NULL CHECK (price >= 0),
  valid_from date          NOT NULL,
  source     text          NOT NULL DEFAULT 'MANUAL' CHECK (source IN ('MANUAL','IMPORT','LEGACY')),
  import_id  bigint,
  created_by bigint        REFERENCES users(id),
  created_at timestamptz   NOT NULL DEFAULT now(),
  UNIQUE (part_id, valid_from),
  FOREIGN KEY (org_id, part_id)   REFERENCES parts (org_id, id),
  FOREIGN KEY (org_id, import_id) REFERENCES price_imports (org_id, id),
  CHECK ((source = 'IMPORT') = (import_id IS NOT NULL))
);
CREATE TRIGGER prices_append_only
  BEFORE UPDATE OR DELETE ON prices
  FOR EACH ROW EXECUTE FUNCTION forbid_mutation();

CREATE OR REPLACE FUNCTION price_on(p_org bigint, p_part bigint, p_date date)
RETURNS TABLE (price numeric(18,4), valid_from date)
LANGUAGE sql STABLE AS $$
  SELECT pr.price, pr.valid_from FROM prices pr
  WHERE pr.org_id = p_org AND pr.part_id = p_part AND pr.valid_from <= p_date
  ORDER BY pr.valid_from DESC
  LIMIT 1
$$;

CREATE VIEW price_periods AS
SELECT pr.*, lead(pr.valid_from) OVER w - 1 AS valid_to,
       lag(pr.price) OVER w AS previous_price
FROM prices pr
WINDOW w AS (PARTITION BY pr.part_id ORDER BY pr.valid_from);

CREATE OR REPLACE FUNCTION bump_catalog_content() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF TG_TABLE_NAME = 'parts' THEN
    UPDATE catalogs SET content_version = content_version + 1 WHERE id = NEW.catalog_id;
  ELSE
    UPDATE catalogs SET content_version = content_version + 1
    WHERE id = (SELECT catalog_id FROM parts WHERE id = NEW.part_id);
  END IF;
  RETURN NULL;
END $$;
CREATE TRIGGER parts_bump_catalog  AFTER INSERT OR UPDATE ON parts
  FOR EACH ROW EXECUTE FUNCTION bump_catalog_content();
CREATE TRIGGER prices_bump_catalog AFTER INSERT ON prices
  FOR EACH ROW EXECUTE FUNCTION bump_catalog_content();

-- Count valuation (ADR-0008). Documents arrive with F3, which adds the LAST_PURCHASE branch
-- (migrations/draft/0004_documents.sql); until then the catalog price is the fallback.
CREATE OR REPLACE FUNCTION last_purchase_cost(p_org bigint, p_part bigint, p_date date)
RETURNS TABLE (unit_cost numeric(18,4), cost_source text)
LANGUAGE sql STABLE AS $$
  SELECT c.unit_cost, c.cost_source FROM (
    (SELECT p.price, 'LIST_PRICE', 2 FROM price_on(p_org, p_part, p_date) p)
    UNION ALL
    SELECT NULL, 'NONE', 3
  ) AS c(unit_cost, cost_source, rank)
  ORDER BY c.rank
  LIMIT 1
$$;

CREATE TABLE stock_transfers (
  id            bigserial   PRIMARY KEY,
  org_id        bigint      NOT NULL,
  transfer_no   text        NOT NULL,
  from_depot_id bigint      NOT NULL,
  to_depot_id   bigint      NOT NULL,
  transfer_date date        NOT NULL,
  note          text,
  created_by    bigint      REFERENCES users(id),
  created_at    timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, transfer_no),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, from_depot_id) REFERENCES depots (org_id, id),
  FOREIGN KEY (org_id, to_depot_id)   REFERENCES depots (org_id, id),
  CHECK (from_depot_id <> to_depot_id)
);

CREATE TABLE stock_movements (
  id            bigserial     PRIMARY KEY,
  org_id        bigint        NOT NULL,
  part_id       bigint        NOT NULL,
  depot_id      bigint        NOT NULL,
  kind          text          NOT NULL
                CHECK (kind IN ('IN','OUT','ADJUST_IN','ADJUST_OUT','TRANSFER_IN','TRANSFER_OUT')),
  qty           numeric(18,4) NOT NULL CHECK (qty > 0),
  unit_cost     numeric(18,4) CHECK (unit_cost IS NULL OR unit_cost >= 0),
  source_type   text          NOT NULL CHECK (source_type IN ('document','stock_count','transfer','legacy_import')),
  source_id     text,
  transfer_id   bigint,
  movement_date date          NOT NULL,
  occurred_at   timestamptz   NOT NULL DEFAULT clock_timestamp(),
  created_by    bigint        REFERENCES users(id),
  FOREIGN KEY (org_id, part_id)     REFERENCES parts (org_id, id),
  FOREIGN KEY (org_id, depot_id)    REFERENCES depots (org_id, id),
  FOREIGN KEY (org_id, transfer_id) REFERENCES stock_transfers (org_id, id),
  CHECK ((kind LIKE 'TRANSFER_%') = (transfer_id IS NOT NULL)),
  CHECK ((kind LIKE 'TRANSFER_%') = (source_type = 'transfer')),
  CHECK ((kind LIKE 'ADJUST_%') = (source_type = 'stock_count'))
);
CREATE INDEX stock_movements_part_idx ON stock_movements (org_id, part_id, depot_id, movement_date, id);
CREATE INDEX stock_movements_time_idx ON stock_movements (org_id, part_id, depot_id, occurred_at);
CREATE INDEX stock_movements_date_idx ON stock_movements (org_id, movement_date DESC, id DESC);
CREATE UNIQUE INDEX stock_movements_source_uq ON stock_movements (org_id, source_type, source_id, kind)
  WHERE source_type IN ('document','stock_count');
CREATE UNIQUE INDEX stock_movements_transfer_uq ON stock_movements (transfer_id, part_id, kind)
  WHERE transfer_id IS NOT NULL;
CREATE TRIGGER stock_movements_append_only
  BEFORE UPDATE OR DELETE ON stock_movements
  FOR EACH ROW EXECUTE FUNCTION forbid_mutation();

CREATE OR REPLACE FUNCTION movement_sign(p_kind text) RETURNS int
LANGUAGE sql IMMUTABLE AS $$
  SELECT CASE WHEN p_kind IN ('IN','ADJUST_IN','TRANSFER_IN') THEN 1 ELSE -1 END
$$;

CREATE OR REPLACE FUNCTION check_movement_year_open() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF EXISTS (SELECT 1 FROM carry_forward_runs
             WHERE org_id = NEW.org_id AND reopened_at IS NULL
               AND year > extract(year FROM NEW.movement_date)::int) THEN
    RAISE EXCEPTION 'ERR_YEAR_CLOSED: % is in a closed year', NEW.movement_date USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER stock_movements_year_open
  BEFORE INSERT ON stock_movements
  FOR EACH ROW EXECUTE FUNCTION check_movement_year_open();

-- Written only by the movement trigger; the app role cannot touch it directly.
CREATE TABLE stock_levels (
  org_id   bigint        NOT NULL,
  part_id  bigint        NOT NULL,
  depot_id bigint        NOT NULL,
  on_hand  numeric(18,4) NOT NULL DEFAULT 0,
  reserved numeric(18,4) NOT NULL DEFAULT 0 CHECK (reserved >= 0),
  version  int           NOT NULL DEFAULT 0,
  PRIMARY KEY (part_id, depot_id),
  FOREIGN KEY (org_id, part_id)  REFERENCES parts (org_id, id),
  FOREIGN KEY (org_id, depot_id) REFERENCES depots (org_id, id)
);
CREATE INDEX stock_levels_negative_idx ON stock_levels (org_id, depot_id) WHERE on_hand < 0;
CREATE INDEX stock_levels_org_idx ON stock_levels (org_id, depot_id);

CREATE OR REPLACE FUNCTION apply_stock_movement() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
BEGIN
  INSERT INTO stock_levels AS l (org_id, part_id, depot_id, on_hand, version)
  VALUES (NEW.org_id, NEW.part_id, NEW.depot_id, movement_sign(NEW.kind) * NEW.qty, 1)
  ON CONFLICT (part_id, depot_id)
  DO UPDATE SET on_hand = l.on_hand + movement_sign(NEW.kind) * NEW.qty, version = l.version + 1;
  RETURN NULL;
END $$;
REVOKE ALL ON FUNCTION apply_stock_movement() FROM PUBLIC;
CREATE TRIGGER stock_movements_apply
  AFTER INSERT ON stock_movements
  FOR EACH ROW EXECUTE FUNCTION apply_stock_movement();

CREATE VIEW stock_level_drift AS
SELECT coalesce(l.part_id, m.part_id) AS part_id, coalesce(l.depot_id, m.depot_id) AS depot_id,
       coalesce(l.on_hand, 0) - coalesce(m.q, 0) AS drift
FROM stock_levels l
FULL JOIN (SELECT part_id, depot_id, sum(movement_sign(kind) * qty) AS q
           FROM stock_movements GROUP BY part_id, depot_id) m
  ON m.part_id = l.part_id AND m.depot_id = l.depot_id
WHERE coalesce(l.on_hand, 0) <> coalesce(m.q, 0);

CREATE OR REPLACE FUNCTION on_hand_at(p_org bigint, p_part bigint, p_depot bigint, p_at timestamptz)
RETURNS numeric(18,4)
LANGUAGE sql STABLE AS $$
  SELECT coalesce(sum(movement_sign(kind) * qty), 0) FROM stock_movements
  WHERE org_id = p_org AND part_id = p_part AND depot_id = p_depot AND occurred_at <= p_at
$$;

CREATE TABLE stock_counts (
  id            bigserial   PRIMARY KEY,
  org_id        bigint      NOT NULL,
  depot_id      bigint      NOT NULL,
  count_date    date        NOT NULL,
  year          int         NOT NULL CHECK (year BETWEEN 2000 AND 2999),
  count_no      text        NOT NULL,
  shelf_from    text,
  shelf_to      text,
  status        text        NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','FINALIZED','CANCELLED')),
  started_at    timestamptz NOT NULL DEFAULT clock_timestamp(),
  uncounted     text        CHECK (uncounted IN ('SKIP','ZERO')),
  finalized_at  timestamptz,
  cancelled_at  timestamptz,
  cancel_reason text,
  version       int         NOT NULL DEFAULT 1,
  created_by    bigint      REFERENCES users(id),
  created_at    timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, count_no),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, depot_id) REFERENCES depots (org_id, id),
  CHECK (year = extract(year FROM count_date)),
  CHECK (shelf_from IS NULL OR shelf_to IS NULL OR shelf_from <= shelf_to),
  CHECK ((status = 'FINALIZED') = (finalized_at IS NOT NULL)),
  CHECK ((status = 'FINALIZED') = (uncounted IS NOT NULL)),
  CHECK ((status = 'CANCELLED') = (cancelled_at IS NOT NULL))
);
CREATE UNIQUE INDEX stock_counts_one_open ON stock_counts (org_id, depot_id) WHERE status = 'OPEN';
CREATE INDEX stock_counts_org_idx ON stock_counts (org_id, count_date DESC, id DESC);

CREATE OR REPLACE FUNCTION stock_counts_transition() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF OLD.status <> 'OPEN' OR NEW.started_at <> OLD.started_at OR NEW.depot_id <> OLD.depot_id THEN
    RAISE EXCEPTION 'count % is % and cannot change', OLD.id, OLD.status USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;
CREATE TRIGGER stock_counts_transition BEFORE UPDATE ON stock_counts
  FOR EACH ROW EXECUTE FUNCTION stock_counts_transition();

CREATE TABLE stock_count_lines (
  id          bigserial     PRIMARY KEY,
  org_id      bigint        NOT NULL,
  count_id    bigint        NOT NULL,
  part_id     bigint        NOT NULL,
  shelf       text          NOT NULL DEFAULT '',
  system_qty  numeric(18,4) NOT NULL,
  counted_qty numeric(18,4) CHECK (counted_qty >= 0),
  counted_by  bigint        REFERENCES users(id),
  counted_at  timestamptz,
  added       boolean       NOT NULL DEFAULT false,
  unit_cost   numeric(18,4) CHECK (unit_cost IS NULL OR unit_cost >= 0),
  cost_source text          CHECK (cost_source IN ('LAST_PURCHASE','LIST_PRICE','NONE')),
  version     int           NOT NULL DEFAULT 1,
  UNIQUE (count_id, part_id),
  FOREIGN KEY (org_id, count_id) REFERENCES stock_counts (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, part_id)  REFERENCES parts (org_id, id),
  CHECK ((counted_qty IS NULL) = (counted_at IS NULL)),
  CHECK ((unit_cost IS NULL) = (cost_source IS NULL OR cost_source = 'NONE'))
);
CREATE INDEX stock_count_lines_shelf_idx ON stock_count_lines (count_id, shelf, part_id);

CREATE OR REPLACE FUNCTION stock_count_lines_guard() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF (SELECT status FROM stock_counts WHERE id = coalesce(NEW.count_id, OLD.count_id)) <> 'OPEN' THEN
    RAISE EXCEPTION 'ERR_STOCK_COUNT_NOT_OPEN: count %', coalesce(NEW.count_id, OLD.count_id)
      USING ERRCODE = 'check_violation';
  END IF;
  IF TG_OP = 'UPDATE' THEN
    IF NEW.system_qty <> OLD.system_qty OR NEW.part_id <> OLD.part_id OR NEW.count_id <> OLD.count_id THEN
      RAISE EXCEPTION 'count line %: system quantity, part and count are fixed', OLD.id USING ERRCODE = 'restrict_violation';
    END IF;
    IF NEW.version <> OLD.version + 1 THEN
      RAISE EXCEPTION 'count line %: version % -> % (must move by one)', OLD.id, OLD.version, NEW.version
        USING ERRCODE = 'check_violation';
    END IF;
  END IF;
  RETURN coalesce(NEW, OLD);
END $$;
CREATE TRIGGER stock_count_lines_guard
  BEFORE INSERT OR UPDATE OR DELETE ON stock_count_lines
  FOR EACH ROW EXECUTE FUNCTION stock_count_lines_guard();

-- Append-only tables refuse TRUNCATE (0006); the app role writes rows only (0007).
CREATE TRIGGER prices_no_truncate BEFORE TRUNCATE ON prices
  FOR EACH STATEMENT EXECUTE FUNCTION forbid_mutation();
CREATE TRIGGER stock_movements_no_truncate BEFORE TRUNCATE ON stock_movements
  FOR EACH STATEMENT EXECUTE FUNCTION forbid_mutation();

GRANT SELECT, INSERT, UPDATE, DELETE ON catalogs, parts, part_depots, part_codes, price_imports, price_import_files,
  price_import_rows, stock_transfers, stock_counts, stock_count_lines TO fikopar_app;
GRANT SELECT, INSERT ON prices, stock_movements TO fikopar_app;
GRANT SELECT ON stock_levels, price_periods, stock_level_drift TO fikopar_app;
REVOKE UPDATE, DELETE, TRUNCATE ON prices, stock_movements FROM fikopar_app;
REVOKE INSERT, UPDATE, DELETE, TRUNCATE ON stock_levels FROM fikopar_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO fikopar_app;
