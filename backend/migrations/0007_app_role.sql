-- The server runs as fikopar_app: rows only, no DDL, no TRUNCATE. Migrations run as the owner.
-- The login and its password are set by the deployment (db.ProvisionAppRole).

DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'fikopar_app') THEN
    CREATE ROLE fikopar_app NOLOGIN;
  END IF;
END $$;

REVOKE CREATE ON SCHEMA public FROM PUBLIC;
GRANT USAGE ON SCHEMA public TO fikopar_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO fikopar_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO fikopar_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO fikopar_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO fikopar_app;

REVOKE ALL ON schema_migrations FROM fikopar_app;
GRANT SELECT ON schema_migrations TO fikopar_app;
REVOKE UPDATE, DELETE ON postings, journal_entries, cash_counts, instrument_events, audit_events FROM fikopar_app;
REVOKE DELETE ON receipts, cash_vouchers, opening_balances, carry_forward_runs, outbox, outbox_deliveries FROM fikopar_app;
-- SELECT … FOR UPDATE (reversals) needs UPDATE on some column; the append-only trigger still refuses it.
GRANT UPDATE (id) ON journal_entries TO fikopar_app;
