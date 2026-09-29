-- F3 runtime on top of 0012_documents: per-aggregate outbox order, TRUNCATE guards and app role grants.

-- The relay delivers the events of one aggregate in the order they were written (ADR-0013 §12). occurred_at is the
-- transaction start, equal for every event of one transaction, so a sequence orders them.
ALTER TABLE outbox ADD COLUMN seq bigserial;
CREATE INDEX outbox_pending_seq_idx ON outbox (seq) WHERE published_at IS NULL;
CREATE INDEX outbox_pending_aggregate_idx ON outbox (org_id, aggregate_type, aggregate_id, seq) WHERE published_at IS NULL;

-- Posted documents are fixed by row triggers, which TRUNCATE bypasses; reservations feed a projection.
CREATE TRIGGER documents_no_truncate BEFORE TRUNCATE ON documents
  FOR EACH STATEMENT EXECUTE FUNCTION forbid_mutation();
CREATE TRIGGER document_lines_no_truncate BEFORE TRUNCATE ON document_lines
  FOR EACH STATEMENT EXECUTE FUNCTION forbid_mutation();
CREATE TRIGGER stock_reservations_no_truncate BEFORE TRUNCATE ON stock_reservations
  FOR EACH STATEMENT EXECUTE FUNCTION forbid_mutation();

-- Reservations are inserted and deleted, never updated by the app; the sweeper deletes through FOR UPDATE.
REVOKE UPDATE ON stock_reservations FROM fikopar_app;
GRANT UPDATE (expires_at) ON stock_reservations TO fikopar_app;
GRANT USAGE, SELECT ON SEQUENCE outbox_seq_seq TO fikopar_app;
