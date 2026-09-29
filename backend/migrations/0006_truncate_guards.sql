-- Row triggers do not see TRUNCATE; append-only tables refuse it per statement.

DO $$
DECLARE t text;
BEGIN
  FOREACH t IN ARRAY ARRAY['postings', 'journal_entries', 'receipts', 'cash_vouchers', 'opening_balances',
                           'carry_forward_runs', 'cash_counts', 'instrument_events', 'audit_events', 'outbox'] LOOP
    EXECUTE format('CREATE TRIGGER %I BEFORE TRUNCATE ON %I FOR EACH STATEMENT EXECUTE FUNCTION forbid_mutation()',
                   t || '_no_truncate', t);
  END LOOP;
END $$;
