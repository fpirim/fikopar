-- Parts, codes, price lists with history, and stock as a movement ledger per depot.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE parts (
  id          bigserial     PRIMARY KEY,
  org_id      bigint        NOT NULL REFERENCES organizations(id),
  catalog_no  int           NOT NULL,          -- legacy KATNO
  stock_no    text          NOT NULL,          -- legacy STNO
  name        text          NOT NULL,
  type        text,
  brand       text,
  unit        text          NOT NULL DEFAULT 'PCS',
  vat_pct     numeric(7,4)  NOT NULL DEFAULT 20 CHECK (vat_pct BETWEEN 0 AND 100),
  package_qty int           NOT NULL DEFAULT 1 CHECK (package_qty > 0),
  is_active   boolean       NOT NULL DEFAULT true,
  version     int           NOT NULL DEFAULT 1,
  created_at  timestamptz   NOT NULL DEFAULT now(),
  updated_at  timestamptz   NOT NULL DEFAULT now(),
  UNIQUE (org_id, catalog_no, stock_no),
  UNIQUE (org_id, id)
);
-- Catalog search: prefix and substring over stock no and name.
CREATE INDEX parts_stock_no_trgm ON parts USING gin (stock_no gin_trgm_ops);
CREATE INDEX parts_name_trgm     ON parts USING gin (name gin_trgm_ops);

-- Min/max and shelf are per depot.
CREATE TABLE part_depots (
  org_id   bigint        NOT NULL,
  part_id  bigint        NOT NULL,
  depot_id bigint        NOT NULL,
  min_qty  numeric(18,4) NOT NULL DEFAULT 0 CHECK (min_qty >= 0),
  max_qty  numeric(18,4) CHECK (max_qty IS NULL OR max_qty >= min_qty),
  shelf    text          NOT NULL DEFAULT '',
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
  kind               text      NOT NULL CHECK (kind IN ('MANUFACTURER','OEM','EQUIVALENT')),
  code               text      NOT NULL,
  -- "04.123-45" and "0412345" must match.
  normalized_code    text      GENERATED ALWAYS AS (upper(regexp_replace(code, '[^0-9A-Za-z]', '', 'g'))) STORED,
  equivalent_part_id bigint,
  FOREIGN KEY (org_id, part_id)            REFERENCES parts (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, equivalent_part_id) REFERENCES parts (org_id, id),
  CHECK ((kind = 'EQUIVALENT') = (equivalent_part_id IS NOT NULL)),
  CHECK (equivalent_part_id IS NULL OR equivalent_part_id <> part_id),
  UNIQUE (part_id, kind, code)
);
CREATE INDEX part_codes_lookup_idx ON part_codes (org_id, normalized_code);
CREATE INDEX part_codes_trgm       ON part_codes USING gin (normalized_code gin_trgm_ops);

CREATE TABLE price_lists (
  id                bigserial PRIMARY KEY,
  org_id            bigint    NOT NULL REFERENCES organizations(id),
  code              text      NOT NULL,
  name              text      NOT NULL,
  kind              text      NOT NULL CHECK (kind IN ('SALES','SUPPLIER')),
  supplier_sicil_id bigint,
  currency          char(3)   NOT NULL DEFAULT 'TRY',
  is_default        boolean   NOT NULL DEFAULT false,
  UNIQUE (org_id, code),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, supplier_sicil_id) REFERENCES sicils (organization_id, id),
  CHECK ((kind = 'SUPPLIER') = (supplier_sicil_id IS NOT NULL))
);
CREATE UNIQUE INDEX price_lists_one_default ON price_lists (org_id) WHERE is_default;

-- Append-only history; the price valid on a date is the latest valid_from <= date.
CREATE TABLE prices (
  id            bigserial     PRIMARY KEY,
  org_id        bigint        NOT NULL,
  price_list_id bigint        NOT NULL,
  part_id       bigint        NOT NULL,
  price         numeric(18,4) NOT NULL CHECK (price >= 0),
  valid_from    date          NOT NULL,
  created_by    bigint        REFERENCES users(id),
  created_at    timestamptz   NOT NULL DEFAULT now(),
  UNIQUE (price_list_id, part_id, valid_from),
  FOREIGN KEY (org_id, price_list_id) REFERENCES price_lists (org_id, id),
  FOREIGN KEY (org_id, part_id)       REFERENCES parts (org_id, id)
);
CREATE TRIGGER prices_append_only
  BEFORE UPDATE OR DELETE ON prices
  FOR EACH ROW EXECUTE FUNCTION forbid_mutation();

CREATE TABLE stock_transfers (
  id            bigserial   PRIMARY KEY,
  org_id        bigint      NOT NULL,
  transfer_no   text        NOT NULL,              -- TRF series of the source depot
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
  id          bigserial     PRIMARY KEY,
  org_id      bigint        NOT NULL,
  part_id     bigint        NOT NULL,
  depot_id    bigint        NOT NULL,
  kind        text          NOT NULL
              CHECK (kind IN ('IN','OUT','ADJUST_IN','ADJUST_OUT','TRANSFER_IN','TRANSFER_OUT')),
  qty         numeric(18,4) NOT NULL CHECK (qty > 0),
  unit_cost   numeric(18,4) CHECK (unit_cost IS NULL OR unit_cost >= 0),
  source_type text          NOT NULL CHECK (source_type IN ('document','stock_count','transfer','legacy_import')),
  source_id   text,                            -- document: document_lines.id; stock_count: stock_count_lines.id
  transfer_id bigint,
  movement_date date        NOT NULL,
  occurred_at timestamptz   NOT NULL DEFAULT now(),
  created_by  bigint        REFERENCES users(id),
  FOREIGN KEY (org_id, part_id)     REFERENCES parts (org_id, id),
  FOREIGN KEY (org_id, depot_id)    REFERENCES depots (org_id, id),
  FOREIGN KEY (org_id, transfer_id) REFERENCES stock_transfers (org_id, id),
  CHECK ((kind LIKE 'TRANSFER_%') = (transfer_id IS NOT NULL)),
  CHECK ((kind LIKE 'TRANSFER_%') = (source_type = 'transfer'))
);
CREATE INDEX stock_movements_part_idx ON stock_movements (org_id, part_id, depot_id, movement_date DESC, id DESC);
-- A document line moves stock at most once per direction.
CREATE UNIQUE INDEX stock_movements_source_uq ON stock_movements (org_id, source_type, source_id, kind)
  WHERE source_type IN ('document','stock_count');
CREATE TRIGGER stock_movements_append_only
  BEFORE UPDATE OR DELETE ON stock_movements
  FOR EACH ROW EXECUTE FUNCTION forbid_mutation();

-- Projection updated with the movement under a row lock; on_hand = Σ in − Σ out.
-- reserved is held by B2B orders (F3); available = on_hand − reserved.
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

CREATE OR REPLACE FUNCTION check_stock_level() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.on_hand < 0 AND NOT (SELECT allow_negative_stock FROM depots WHERE id = NEW.depot_id) THEN
    RAISE EXCEPTION 'insufficient stock for part % in depot % (on_hand %)', NEW.part_id, NEW.depot_id, NEW.on_hand
      USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER stock_levels_non_negative
  BEFORE INSERT OR UPDATE ON stock_levels
  FOR EACH ROW EXECUTE FUNCTION check_stock_level();

CREATE TABLE stock_counts (
  id           bigserial   PRIMARY KEY,
  org_id       bigint      NOT NULL,
  depot_id     bigint      NOT NULL,
  year         int         NOT NULL CHECK (year BETWEEN 2000 AND 2999),
  count_no     text        NOT NULL,
  shelf_from   text,
  shelf_to     text,
  status       text        NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','FINALIZED','CANCELLED')),
  uncounted    text        CHECK (uncounted IN ('SKIP','ZERO')),  -- chosen at finalize
  finalized_at timestamptz,
  version      int         NOT NULL DEFAULT 1,
  created_by   bigint      REFERENCES users(id),
  created_at   timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, count_no),
  UNIQUE (org_id, id),
  FOREIGN KEY (org_id, depot_id) REFERENCES depots (org_id, id),
  CHECK ((status = 'FINALIZED') = (finalized_at IS NOT NULL)),
  CHECK ((status = 'FINALIZED') = (uncounted IS NOT NULL))
);

-- Lines are created when the count opens (parts of the depot in the shelf range).
-- counted_qty NULL = not counted yet. Autosave writes one line at a time.
CREATE TABLE stock_count_lines (
  id          bigserial     PRIMARY KEY,
  org_id      bigint        NOT NULL,
  count_id    bigint        NOT NULL,
  part_id     bigint        NOT NULL,
  shelf       text          NOT NULL DEFAULT '',
  counted_qty numeric(18,4) CHECK (counted_qty >= 0),
  counted_by  bigint        REFERENCES users(id),
  counted_at  timestamptz,
  system_qty  numeric(18,4),                   -- snapshot taken at finalize
  unit_cost   numeric(18,4) CHECK (unit_cost IS NULL OR unit_cost >= 0),  -- last_purchase_cost(); snapshot at finalize
  cost_source text          CHECK (cost_source IN ('LAST_PURCHASE','LIST_PRICE','NONE')),
  version     int           NOT NULL DEFAULT 1,
  UNIQUE (count_id, part_id, shelf),
  FOREIGN KEY (org_id, count_id) REFERENCES stock_counts (org_id, id) ON DELETE CASCADE,
  FOREIGN KEY (org_id, part_id)  REFERENCES parts (org_id, id),
  CHECK ((counted_qty IS NULL) = (counted_at IS NULL)),
  CHECK ((unit_cost IS NULL) = (cost_source IS NULL OR cost_source = 'NONE'))
);
