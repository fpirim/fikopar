-- Shared: account card extensions, depots, number series, idempotency, outbox, audit.

CREATE OR REPLACE FUNCTION forbid_mutation() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  RAISE EXCEPTION '% on % is not allowed (append-only)', TG_OP, TG_TABLE_NAME
    USING ERRCODE = 'restrict_violation';
END $$;

-- sicils is created by the app today (GORM); these columns back the account card and credit panel.
ALTER TABLE sicils
  ADD COLUMN credit_limit      numeric(18,2) CHECK (credit_limit IS NULL OR credit_limit >= 0),
  ADD COLUMN iskonto2          numeric(7,4)  NOT NULL DEFAULT 0 CHECK (iskonto2 BETWEEN 0 AND 100),
  ADD COLUMN sales_rep_user_id bigint        REFERENCES users(id),
  ADD COLUMN contacts          jsonb         NOT NULL DEFAULT '[]',
  ADD COLUMN emails            text[]        NOT NULL DEFAULT '{}',
  ADD COLUMN version           int           NOT NULL DEFAULT 1,   -- ETag / If-Match on updateSicil
  ADD CONSTRAINT sicils_org_id_uq UNIQUE (organization_id, id);

CREATE TABLE depots (
  id                   bigserial    PRIMARY KEY,
  org_id               bigint       NOT NULL REFERENCES organizations(id),
  code                 text         NOT NULL CHECK (code ~ '^[0-9A-Z]{1,2}$'),
  name                 text         NOT NULL,
  vat_rate             numeric(7,4) NOT NULL DEFAULT 0 CHECK (vat_rate BETWEEN 0 AND 100),
  version              int          NOT NULL DEFAULT 1,
  UNIQUE (org_id, code),
  UNIQUE (org_id, id)
);

-- One counter per (company, depot, prefix, year). Several document types may share a prefix
-- (SF: sales invoice with or without waybill). Rendered as SF-2026-1-000418.
-- Prefix list: docs/domain-model.md, "Number prefixes".
CREATE TABLE number_series (
  id         bigserial PRIMARY KEY,
  org_id     bigint    NOT NULL,
  depot_id   bigint    NOT NULL,
  prefix     text      NOT NULL CHECK (prefix IN (
               'SF','SIR','SI','SIRI','AF','AIR','AI','AIRI','FF','TAH','TED',
               'AC','AS','BC','BS','GDR','VRM','TRF','SAY','J')),
  year       int       NOT NULL CHECK (year BETWEEN 2000 AND 2999),
  width      int       NOT NULL DEFAULT 6 CHECK (width BETWEEN 4 AND 10),
  next_value bigint    NOT NULL DEFAULT 1 CHECK (next_value > 0),
  version    int       NOT NULL DEFAULT 1,
  UNIQUE (org_id, depot_id, prefix, year),
  FOREIGN KEY (org_id, depot_id) REFERENCES depots (org_id, id)
);

CREATE OR REPLACE FUNCTION number_series_forward_only() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.next_value < OLD.next_value THEN
    RAISE EXCEPTION 'number series % cannot move backwards', OLD.id USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$;

CREATE TRIGGER number_series_forward
  BEFORE UPDATE ON number_series
  FOR EACH ROW EXECUTE FUNCTION number_series_forward_only();

-- Row lock on the counter serialises concurrent callers; no max()+1, no string slicing.
CREATE OR REPLACE FUNCTION next_number(p_org bigint, p_depot bigint, p_prefix text, p_year int)
RETURNS text LANGUAGE plpgsql AS $$
DECLARE
  v_value bigint;
  v_width int;
  v_depot text;
BEGIN
  INSERT INTO number_series (org_id, depot_id, prefix, year, next_value)
  VALUES (p_org, p_depot, p_prefix, p_year, 2)
  ON CONFLICT (org_id, depot_id, prefix, year)
  DO UPDATE SET next_value = number_series.next_value + 1, version = number_series.version + 1
  RETURNING next_value - 1, width INTO v_value, v_width;
  SELECT code INTO v_depot FROM depots WHERE org_id = p_org AND id = p_depot;
  RETURN p_prefix || '-' || p_year || '-' || v_depot || '-' || lpad(v_value::text, v_width, '0');
END $$;

CREATE TABLE idempotency_keys (
  org_id        bigint      NOT NULL REFERENCES organizations(id),
  key           uuid        NOT NULL,
  endpoint      text        NOT NULL,
  request_hash  bytea       NOT NULL,
  status        text        NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS','COMPLETED')),
  response_code int,
  response_body jsonb,
  created_at    timestamptz NOT NULL DEFAULT now(),
  expires_at    timestamptz NOT NULL DEFAULT now() + interval '24 hours',
  PRIMARY KEY (org_id, key),
  CHECK (status = 'IN_PROGRESS' OR response_code IS NOT NULL)
);
CREATE INDEX idempotency_keys_expires_idx ON idempotency_keys (expires_at);

CREATE TABLE outbox (
  id                uuid        PRIMARY KEY,
  org_id            bigint      NOT NULL REFERENCES organizations(id),
  depot_id          bigint      NOT NULL,
  event_type        text        NOT NULL
                    CHECK (event_type IN ('DocumentPosted','StockMoved','LedgerEntryPosted','ReceiptPosted','InstrumentStatusChanged')),
  event_version     int         NOT NULL DEFAULT 1,
  aggregate_type    text        NOT NULL,
  aggregate_id      text        NOT NULL,
  aggregate_version int,
  payload           jsonb       NOT NULL,
  correlation_id    text,
  occurred_at       timestamptz NOT NULL DEFAULT now(),
  published_at      timestamptz,
  attempts          int         NOT NULL DEFAULT 0,
  last_error        text
);
-- Relay: SELECT ... WHERE published_at IS NULL ORDER BY occurred_at FOR UPDATE SKIP LOCKED.
CREATE INDEX outbox_unpublished_idx ON outbox (occurred_at) WHERE published_at IS NULL;

CREATE TABLE audit_events (
  id            bigserial   PRIMARY KEY,
  org_id        bigint      NOT NULL REFERENCES organizations(id),
  occurred_at   timestamptz NOT NULL DEFAULT now(),
  actor_user_id bigint      REFERENCES users(id),
  actor_ip      inet,
  request_id    text,
  action        text        NOT NULL,
  entity_type   text        NOT NULL,
  entity_id     text        NOT NULL,
  before        jsonb,
  after         jsonb
);
CREATE INDEX audit_events_entity_idx ON audit_events (org_id, entity_type, entity_id, occurred_at DESC);
CREATE INDEX audit_events_time_idx   ON audit_events (org_id, occurred_at DESC, id DESC);
CREATE TRIGGER audit_events_append_only
  BEFORE UPDATE OR DELETE ON audit_events
  FOR EACH ROW EXECUTE FUNCTION forbid_mutation();
