-- COBOL import runs, per-file reconciliation and quarantined records.

CREATE TABLE migration_runs (
  id               bigserial     PRIMARY KEY,
  org_id           bigint        NOT NULL REFERENCES organizations(id),
  run_no           text          NOT NULL,           -- MIG-0007
  dry_run          boolean       NOT NULL,
  status           text          NOT NULL DEFAULT 'RUNNING' CHECK (status IN ('RUNNING','SUCCEEDED','FAILED')),
  source           text          NOT NULL,           -- e.g. synthetic:seed-42
  records_read     bigint        NOT NULL DEFAULT 0,
  records_imported bigint        NOT NULL DEFAULT 0,
  records_rejected bigint        NOT NULL DEFAULT 0,
  ledger_diff      numeric(18,2),
  accounts_checked int,
  accounts_equal   int,
  started_at       timestamptz   NOT NULL DEFAULT now(),
  finished_at      timestamptz,
  started_by       bigint        REFERENCES users(id),
  UNIQUE (org_id, run_no),
  UNIQUE (org_id, id),
  CHECK (records_imported + records_rejected <= records_read),
  CHECK ((status = 'RUNNING') = (finished_at IS NULL))
);

CREATE TABLE migration_file_results (
  org_id        bigint  NOT NULL,
  run_id        bigint  NOT NULL,
  legacy_file   text    NOT NULL,                    -- CARHAR.DAT
  target        text    NOT NULL,                    -- journal_entry, posting
  read          bigint  NOT NULL,
  imported      bigint  NOT NULL,
  rejected      bigint  NOT NULL,
  control_rule  text    NOT NULL,                    -- "Σ DR = Σ CR"
  control_left  numeric(20,4),
  control_right numeric(20,4),
  result        text    NOT NULL CHECK (result IN ('MATCH','QUARANTINE','MISMATCH')),
  PRIMARY KEY (run_id, legacy_file),
  FOREIGN KEY (org_id, run_id) REFERENCES migration_runs (org_id, id) ON DELETE CASCADE,
  CHECK (imported + rejected = read)
);

CREATE TABLE migration_quarantine (
  id          bigserial PRIMARY KEY,
  org_id      bigint    NOT NULL,
  run_id      bigint    NOT NULL,
  legacy_file text      NOT NULL,
  record_key  text      NOT NULL,
  reason      text      NOT NULL,                    -- ERR_* code
  raw         bytea     NOT NULL,                    -- record as read, CP857
  decoded     jsonb,
  FOREIGN KEY (org_id, run_id) REFERENCES migration_runs (org_id, id) ON DELETE CASCADE
);
CREATE INDEX migration_quarantine_run_idx ON migration_quarantine (run_id, legacy_file);
