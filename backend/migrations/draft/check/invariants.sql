INSERT INTO organizations (name) VALUES ('ap');
INSERT INTO depots (org_id, code, name) VALUES (1, '1', 'D1'), (1, '2', 'D2');
INSERT INTO sicils (organization_id, hesap_no, hesap_adi) VALUES (1, 'C1', 'Cust'), (1, 'S1', 'Supp');
INSERT INTO parts (org_id, catalog_no, stock_no, name) VALUES (1, 1, 'P1', 'Filter');
INSERT INTO price_lists (org_id, code, name, kind, is_default) VALUES (1, 'L', 'List', 'SALES', true);
INSERT INTO prices (org_id, price_list_id, part_id, price, valid_from) VALUES (1, 1, 1, 50, '2026-01-01');

SELECT next_number(1, 1, 'SIRI', 2026) AS siri, next_number(1, 2, 'AIRI', 2026) AS airi;
DO $$ BEGIN
  PERFORM next_number(1, 1, 'DZ', 2026);
  RAISE EXCEPTION 'prefix check missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;

-- no purchase yet: list price fallback
SELECT * FROM last_purchase_cost(1, 1, '2026-09-29');

-- two purchase invoices, depots 1 and 2; latest wins
INSERT INTO documents (org_id, depot_id, type, sicil_id, document_date) VALUES
  (1, 1, 'PURCHASE_INVOICE', 2, '2026-03-01'),
  (1, 2, 'PURCHASE_INVOICE', 2, '2026-05-01');
INSERT INTO document_lines (org_id, document_id, line_no, part_id, qty, unit_price, discount1_pct, net_amount, vat_amount)
VALUES (1, 1, 1, 1, 10, 40, 0, 400, 80), (1, 2, 1, 1, 4, 30, 10, 108, 21.6);
UPDATE documents SET status = 'POSTED', document_no = 'AF-2026-1-000001', posted_at = now(), version = version + 1 WHERE id = 1;
UPDATE documents SET status = 'POSTED', document_no = 'AF-2026-2-000001', posted_at = now(), version = version + 1 WHERE id = 2;
SELECT * FROM last_purchase_cost(1, 1, '2026-09-29');   -- 27.0000 LAST_PURCHASE
SELECT * FROM last_purchase_cost(1, 1, '2026-04-01');   -- 40.0000
SELECT * FROM last_purchase_cost(1, 1, '2025-01-01');   -- NONE

-- sales return waybill (3), return invoice billing it (4)
INSERT INTO documents (org_id, depot_id, type, sicil_id, document_date) VALUES (1, 1, 'SALES_RETURN_WAYBILL', 1, '2026-06-01');
INSERT INTO document_lines (org_id, document_id, line_no, part_id, qty, unit_price, net_amount, vat_amount) VALUES (1, 3, 1, 1, 2, 60, 120, 24);
INSERT INTO documents (org_id, depot_id, type, sicil_id, document_date, waybill_id) VALUES (1, 1, 'SALES_RETURN', 1, '2026-06-02', 3);
INSERT INTO document_lines (org_id, document_id, line_no, part_id, qty, unit_price, net_amount, vat_amount) VALUES (1, 4, 1, 1, 2, 60, 120, 24);

DO $$ BEGIN  -- bill cannot post before the waybill
  UPDATE documents SET status = 'POSTED', document_no = 'SI-2026-1-000001', posted_at = now(), version = version + 1 WHERE id = 4;
  RAISE EXCEPTION 'unposted waybill missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;

UPDATE documents SET status = 'POSTED', document_no = 'SIRI-2026-1-000001', posted_at = now(), version = version + 1 WHERE id = 3;
INSERT INTO stock_movements (org_id, part_id, depot_id, kind, qty, source_type, source_id, movement_date)
VALUES (1, 1, 1, 'IN', 2, 'document', '3', '2026-06-01');   -- line 3 = waybill line

DO $$ BEGIN  -- return waybill never posts to the ledger
  INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description, source_type, source_id)
  VALUES (1, 1, 'J-X', '2026-06-01', 'x', 'document', '3');
  RAISE EXCEPTION 'waybill ledger post missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;

UPDATE documents SET status = 'POSTED', document_no = 'SI-2026-1-000001', posted_at = now(), version = version + 1 WHERE id = 4;

DO $$ BEGIN  -- billed return must not move stock again
  INSERT INTO stock_movements (org_id, part_id, depot_id, kind, qty, source_type, source_id, movement_date)
  VALUES (1, 1, 1, 'IN', 2, 'document', '4', '2026-06-02');
  RAISE EXCEPTION 'double stock move missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;

DO $$ BEGIN  -- waybill billed once
  INSERT INTO documents (org_id, depot_id, type, sicil_id, document_date, waybill_id) VALUES (1, 1, 'SALES_RETURN', 1, '2026-06-03', 3);
  RAISE EXCEPTION 'second bill missed';
EXCEPTION WHEN unique_violation THEN NULL; END $$;

DO $$ BEGIN  -- wrong pair
  INSERT INTO documents (org_id, depot_id, type, sicil_id, document_date, waybill_id) VALUES (1, 1, 'SALES_INVOICE', 1, '2026-06-03', 3);
  RAISE EXCEPTION 'wrong pair missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;

DO $$ BEGIN  -- billed waybill cannot be cancelled
  UPDATE documents SET status = 'CANCELLED', cancelled_at = now(), version = version + 1 WHERE id = 3;
  RAISE EXCEPTION 'billed cancel missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;

-- cancel the bill first, then the waybill
UPDATE documents SET status = 'CANCELLED', cancelled_at = now(), version = version + 1 WHERE id = 4;
UPDATE documents SET status = 'CANCELLED', cancelled_at = now(), version = version + 1 WHERE id = 3;

-- count line cost snapshot
INSERT INTO stock_counts (org_id, depot_id, year, count_no) VALUES (1, 1, 2026, 'SAY-2026-1-000001');
INSERT INTO stock_count_lines (org_id, count_id, part_id, unit_cost, cost_source) VALUES (1, 1, 1, 27, 'LAST_PURCHASE');
DO $$ BEGIN
  INSERT INTO stock_count_lines (org_id, count_id, part_id, shelf, unit_cost, cost_source) VALUES (1, 1, 1, 'A', NULL, 'LAST_PURCHASE');
  RAISE EXCEPTION 'cost source check missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;

-- v1.1: sicil version, TRY-only entries, closed years, reopen
SELECT version FROM sicils WHERE id = 1;   -- 1
INSERT INTO accounts (org_id, code, name, type, normal_side) VALUES
  (1, '120.C1', 'Cust', 'ASSET', 'DEBIT'), (1, '500', 'Opening', 'EQUITY', 'CREDIT');

DO $$ BEGIN
  INSERT INTO accounts (org_id, code, name, type, normal_side, currency) VALUES (1, '120.X', 'X', 'ASSET', 'DEBIT', 'USD');
  RAISE EXCEPTION 'non-TRY account missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;

WITH e AS (INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description, source_type)
           VALUES (1, 1, 'J-2025-1-000001', '2025-06-01', 'sale', 'manual') RETURNING id)
INSERT INTO postings (org_id, entry_id, line_no, account_id, side, amount)
SELECT 1, e.id, n, a, s, 100 FROM e, (VALUES (1, 1, 'DEBIT'), (2, 2, 'CREDIT')) v(n, a, s);

-- carry forward into 2026: run 1 and its opening entry
INSERT INTO carry_forward_runs (org_id, year, entry_count, total_debit, total_credit) VALUES (1, 2026, 1, 100, 100);
WITH e AS (INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description, source_type, source_id)
           VALUES (1, 1, 'J-2026-1-000001', '2026-01-01', 'opening', 'opening', '2026:1:1:1') RETURNING id),
     p AS (INSERT INTO postings (org_id, entry_id, line_no, account_id, side, amount)
           SELECT 1, e.id, n, a, s, 100 FROM e, (VALUES (1, 1, 'DEBIT'), (2, 2, 'CREDIT')) v(n, a, s))
INSERT INTO opening_balances (org_id, account_id, depot_id, year, debit, source, run_id, journal_entry_id)
SELECT 1, 1, 1, 2026, 100, 'CARRY_FORWARD', 1, e.id FROM e;

SELECT ledger_start(1, '2026-03-01');   -- 2026-01-01

DO $$ BEGIN  -- 2025 is closed
  INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description, source_type)
  VALUES (1, 1, 'J-2025-1-000002', '2025-12-31', 'late', 'manual');
  RAISE EXCEPTION 'closed year missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;

DO $$ BEGIN  -- one active run per year
  INSERT INTO carry_forward_runs (org_id, year, entry_count, total_debit, total_credit) VALUES (1, 2026, 0, 0, 0);
  RAISE EXCEPTION 'second active run missed';
EXCEPTION WHEN unique_violation THEN NULL; END $$;

DO $$ BEGIN  -- mixed currency
  INSERT INTO postings (org_id, entry_id, line_no, account_id, side, amount, currency) VALUES (1, 1, 9, 1, 'DEBIT', 1, 'USD');
  RAISE EXCEPTION 'non-TRY posting missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;

-- reopen 2026: reverse the opening entry on 1 January, void the opening row, mark the run
WITH o AS (SELECT journal_entry_id AS id FROM opening_balances WHERE run_id = 1),
     e AS (INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description, source_type, source_id, reverses_entry_id)
           SELECT 1, 1, 'J-2026-1-000002', '2026-01-01', 'reopen', 'reversal', o.id::text, o.id FROM o RETURNING id, reverses_entry_id),
     p AS (INSERT INTO postings (org_id, entry_id, line_no, account_id, side, amount)
           SELECT 1, e.id, n, a, s, 100 FROM e, (VALUES (1, 1, 'CREDIT'), (2, 2, 'DEBIT')) v(n, a, s))
UPDATE opening_balances b SET voided_by_entry_id = e.id FROM e WHERE b.journal_entry_id = e.reverses_entry_id;
UPDATE carry_forward_runs SET reopened_at = now(), reopen_reason = 'late invoice' WHERE id = 1;

SELECT ledger_start(1, '2026-03-01');   -- -infinity
BEGIN;   -- 2025 is open again
INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description, source_type)
VALUES (1, 1, 'J-2025-1-000003', '2025-12-31', 'late', 'manual');
ROLLBACK;

DO $$ BEGIN  -- a run reopens once
  UPDATE carry_forward_runs SET reopen_reason = 'again' WHERE id = 1;
  RAISE EXCEPTION 'second reopen missed';
EXCEPTION WHEN restrict_violation THEN NULL; END $$;

DO $$ BEGIN  -- a voided opening stays voided
  UPDATE opening_balances SET voided_by_entry_id = NULL WHERE run_id = 1;
  RAISE EXCEPTION 'unvoid missed';
EXCEPTION WHEN restrict_violation THEN NULL; END $$;

-- run 2 posts the year again
WITH r AS (INSERT INTO carry_forward_runs (org_id, year, entry_count, total_debit, total_credit)
           VALUES (1, 2026, 1, 90, 90) RETURNING id),
     e AS (INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description, source_type, source_id)
           SELECT 1, 1, 'J-2026-1-000003', '2026-01-01', 'opening', 'opening', '2026:' || r.id || ':1:1' FROM r RETURNING id),
     p AS (INSERT INTO postings (org_id, entry_id, line_no, account_id, side, amount)
           SELECT 1, e.id, n, a, s, 90 FROM e, (VALUES (1, 1, 'DEBIT'), (2, 2, 'CREDIT')) v(n, a, s))
INSERT INTO opening_balances (org_id, account_id, depot_id, year, debit, source, run_id, journal_entry_id)
SELECT 1, 1, 1, 2026, 90, 'CARRY_FORWARD', r.id, e.id FROM r, e;
SELECT count(*) FROM opening_balances WHERE year = 2026 AND voided_by_entry_id IS NULL;   -- 1

SELECT 'invariants ok';
