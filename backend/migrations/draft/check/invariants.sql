INSERT INTO organizations (name) VALUES ('ap');
INSERT INTO depots (org_id, code, name) VALUES (1, '1', 'D1'), (1, '2', 'D2');
INSERT INTO sicils (organization_id, hesap_no, hesap_adi) VALUES (1, 'C1', 'Cust'), (1, 'S1', 'Supp');
INSERT INTO catalogs (org_id, code, name) VALUES (1, 1, 'Bosch');
INSERT INTO parts (org_id, catalog_id, stock_no, name) VALUES (1, 1, 'P1', 'Filter');
INSERT INTO prices (org_id, part_id, price, valid_from) VALUES (1, 1, 50, '2026-01-01');

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
INSERT INTO stock_counts (org_id, depot_id, count_date, year, count_no) VALUES (1, 1, '2026-09-29', 2026, 'SAY-2026-1-000001');
INSERT INTO stock_count_lines (org_id, count_id, part_id, system_qty, unit_cost, cost_source)
SELECT 1, 1, 1, on_hand_at(1, 1, 1, c.started_at), 27, 'LAST_PURCHASE' FROM stock_counts c WHERE c.id = 1;
DO $$ BEGIN
  INSERT INTO stock_count_lines (org_id, count_id, part_id, shelf, system_qty, unit_cost, cost_source) VALUES (1, 1, 1, 'A', 0, NULL, 'LAST_PURCHASE');
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

-- v1.2 prices: one row per (part, valid_from), append-only, latest valid_from <= D wins, future dates allowed
INSERT INTO prices (org_id, part_id, price, valid_from) VALUES (1, 1, 55, '2026-07-01'), (1, 1, 60, '2099-01-01');
DO $$ BEGIN
  IF (SELECT price FROM price_on(1, 1, '2026-06-30')) <> 50 THEN RAISE EXCEPTION 'price before change wrong'; END IF;
  IF (SELECT price FROM price_on(1, 1, '2026-07-01')) <> 55 THEN RAISE EXCEPTION 'price on change day wrong'; END IF;
  IF (SELECT price FROM price_on(1, 1, current_date)) <> 55 THEN RAISE EXCEPTION 'future price is current'; END IF;
  IF EXISTS (SELECT 1 FROM price_on(1, 1, '2025-12-31')) THEN RAISE EXCEPTION 'price before history'; END IF;
  IF EXISTS (SELECT 1 FROM price_periods a JOIN price_periods b ON a.part_id = b.part_id AND a.id <> b.id
             AND daterange(a.valid_from, a.valid_to, '[]') && daterange(b.valid_from, b.valid_to, '[]')) THEN
    RAISE EXCEPTION 'price periods overlap';
  END IF;
  IF (SELECT valid_to FROM price_periods WHERE part_id = 1 AND valid_from = '2026-01-01') <> '2026-06-30' THEN
    RAISE EXCEPTION 'valid_to wrong';
  END IF;
  IF (SELECT content_version FROM catalogs WHERE id = 1) < 4 THEN RAISE EXCEPTION 'catalog content_version not moved'; END IF;
END $$;
DO $$ BEGIN  -- same part, same valid_from
  INSERT INTO prices (org_id, part_id, price, valid_from) VALUES (1, 1, 51, '2026-07-01');
  RAISE EXCEPTION 'duplicate valid_from missed';
EXCEPTION WHEN unique_violation THEN NULL; END $$;
DO $$ BEGIN
  UPDATE prices SET price = 1 WHERE part_id = 1;
  RAISE EXCEPTION 'price update missed';
EXCEPTION WHEN restrict_violation THEN NULL; END $$;
DO $$ BEGIN
  INSERT INTO prices (org_id, part_id, price, valid_from, source) VALUES (1, 1, 51, '2026-08-01', 'IMPORT');
  RAISE EXCEPTION 'import price without job missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;
INSERT INTO catalogs (org_id, code, name) VALUES (1, 2, 'Mahle');
DO $$ BEGIN  -- one catalog per part, fixed
  UPDATE parts SET catalog_id = 2 WHERE id = 1;
  RAISE EXCEPTION 'catalog change missed';
EXCEPTION WHEN restrict_violation THEN NULL; END $$;
INSERT INTO parts (org_id, catalog_id, stock_no, name) VALUES (1, 2, 'P1', 'Filter (Mahle)');   -- same stock no, other catalog

-- price import jobs: READY → APPLYING → APPLIED, terminal after that
INSERT INTO price_imports (org_id, catalog_id, file_name, file_sha256, file_size, format, mapping, valid_from, status, catalog_version)
VALUES (1, 1, 'bosch.csv', repeat('a', 64), 100, 'CSV', '{}', '2026-10-01', 'READY', 1);
INSERT INTO price_import_rows (org_id, import_id, row_no, part_id, changes) VALUES (1, 1, 2, 1, '{PRICE}');
DO $$ BEGIN
  INSERT INTO price_import_rows (org_id, import_id, row_no, part_id, changes) VALUES (1, 1, 3, 1, '{MISSING}');
  RAISE EXCEPTION 'missing row with row_no missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;
DO $$ BEGIN
  UPDATE price_imports SET status = 'APPLIED', applied_at = now() WHERE id = 1;
  RAISE EXCEPTION 'apply without APPLYING missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;
UPDATE price_imports SET status = 'APPLYING', version = version + 1 WHERE id = 1;
INSERT INTO prices (org_id, part_id, price, valid_from, source, import_id) VALUES (1, 1, 58, '2026-10-01', 'IMPORT', 1);
UPDATE price_imports SET status = 'APPLIED', applied_at = now(), version = version + 1 WHERE id = 1;
DO $$ BEGIN
  UPDATE price_imports SET status = 'READY', applied_at = NULL WHERE id = 1;
  RAISE EXCEPTION 'applied import reopened';
EXCEPTION WHEN check_violation THEN NULL; END $$;

-- v1.2 stock: negative on hand allowed, projection = Σ movements, movements immutable, closed years
INSERT INTO stock_movements (org_id, part_id, depot_id, kind, qty, source_type, source_id, movement_date)
VALUES (1, 1, 1, 'OUT', 5, 'legacy_import', 'x1', '2026-09-01');
DO $$ BEGIN
  IF (SELECT on_hand FROM stock_levels WHERE part_id = 1 AND depot_id = 1) <> -3 THEN RAISE EXCEPTION 'negative on hand wrong'; END IF;
  IF EXISTS (SELECT 1 FROM stock_level_drift) THEN RAISE EXCEPTION 'stock projection drift'; END IF;
END $$;
DO $$ BEGIN
  UPDATE stock_movements SET qty = 1 WHERE id = 1;
  RAISE EXCEPTION 'movement update missed';
EXCEPTION WHEN restrict_violation THEN NULL; END $$;
DO $$ BEGIN
  DELETE FROM stock_movements WHERE id = 1;
  RAISE EXCEPTION 'movement delete missed';
EXCEPTION WHEN restrict_violation THEN NULL; END $$;
DO $$ BEGIN  -- 2025 is closed
  INSERT INTO stock_movements (org_id, part_id, depot_id, kind, qty, source_type, source_id, movement_date)
  VALUES (1, 1, 1, 'IN', 1, 'legacy_import', 'x2', '2025-12-31');
  RAISE EXCEPTION 'closed-year movement missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;
DO $$ BEGIN  -- ADJUST only from counts
  INSERT INTO stock_movements (org_id, part_id, depot_id, kind, qty, source_type, source_id, movement_date)
  VALUES (1, 1, 1, 'ADJUST_IN', 1, 'legacy_import', 'x3', '2026-09-01');
  RAISE EXCEPTION 'adjust outside count missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;

-- transfer TRF: one OUT and one IN per part, same transaction
BEGIN;
INSERT INTO stock_transfers (org_id, transfer_no, from_depot_id, to_depot_id, transfer_date)
VALUES (1, 'TRF-2026-1-000001', 1, 2, '2026-09-02');
INSERT INTO stock_movements (org_id, part_id, depot_id, kind, qty, source_type, transfer_id, movement_date)
VALUES (1, 1, 1, 'TRANSFER_OUT', 4, 'transfer', 1, '2026-09-02'), (1, 1, 2, 'TRANSFER_IN', 4, 'transfer', 1, '2026-09-02');
COMMIT;
DO $$ BEGIN
  INSERT INTO stock_movements (org_id, part_id, depot_id, kind, qty, source_type, transfer_id, movement_date)
  VALUES (1, 1, 1, 'TRANSFER_OUT', 1, 'transfer', 1, '2026-09-02');
  RAISE EXCEPTION 'second transfer side missed';
EXCEPTION WHEN unique_violation THEN NULL; END $$;
DO $$ BEGIN
  IF (SELECT on_hand FROM stock_levels WHERE part_id = 1 AND depot_id = 1) <> -7
  OR (SELECT on_hand FROM stock_levels WHERE part_id = 1 AND depot_id = 2) <> 4 THEN RAISE EXCEPTION 'transfer levels wrong'; END IF;
  IF EXISTS (SELECT 1 FROM stock_level_drift) THEN RAISE EXCEPTION 'stock projection drift'; END IF;
END $$;

-- count SAY-2026-1-000001: system quantity frozen at open (2), later movements stay on top
DO $$ BEGIN
  IF (SELECT system_qty FROM stock_count_lines WHERE count_id = 1 AND part_id = 1) <> 2 THEN RAISE EXCEPTION 'system qty not frozen'; END IF;
END $$;
DO $$ BEGIN  -- one open count per depot
  INSERT INTO stock_counts (org_id, depot_id, count_date, year, count_no) VALUES (1, 1, '2026-09-30', 2026, 'SAY-2026-1-000002');
  RAISE EXCEPTION 'second open count missed';
EXCEPTION WHEN unique_violation THEN NULL; END $$;
UPDATE stock_count_lines SET counted_qty = 1, counted_at = now(), version = version + 1 WHERE count_id = 1 AND part_id = 1;
DO $$ BEGIN  -- autosave must move the line version by one
  UPDATE stock_count_lines SET counted_qty = 3, counted_at = now() WHERE count_id = 1 AND part_id = 1;
  RAISE EXCEPTION 'unversioned line update missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;
DO $$ BEGIN
  UPDATE stock_count_lines SET counted_qty = 3, version = version + 2 WHERE count_id = 1 AND part_id = 1;
  RAISE EXCEPTION 'version skip missed';
EXCEPTION WHEN check_violation THEN NULL; END $$;
DO $$ BEGIN
  UPDATE stock_count_lines SET system_qty = 9, version = version + 1 WHERE count_id = 1 AND part_id = 1;
  RAISE EXCEPTION 'system qty change missed';
EXCEPTION WHEN restrict_violation THEN NULL; END $$;
-- finalize: snapshot cost, ADJUST (counted 1 − system 2 = −1), close the count
BEGIN;
UPDATE stock_count_lines l SET unit_cost = c.unit_cost, cost_source = c.cost_source, version = version + 1
FROM last_purchase_cost(1, 1, '2026-09-29') c WHERE l.count_id = 1;
INSERT INTO stock_movements (org_id, part_id, depot_id, kind, qty, unit_cost, source_type, source_id, movement_date)
SELECT 1, part_id, 1, 'ADJUST_OUT', system_qty - counted_qty, unit_cost, 'stock_count', id::text, '2026-09-29'
FROM stock_count_lines WHERE count_id = 1;
UPDATE stock_counts SET status = 'FINALIZED', uncounted = 'SKIP', finalized_at = now(), version = version + 1 WHERE id = 1;
COMMIT;
DO $$ BEGIN
  IF (SELECT on_hand FROM stock_levels WHERE part_id = 1 AND depot_id = 1) <> -8 THEN RAISE EXCEPTION 'count adjust wrong'; END IF;
  IF EXISTS (SELECT 1 FROM stock_level_drift) THEN RAISE EXCEPTION 'stock projection drift'; END IF;
END $$;
DO $$ BEGIN
  UPDATE stock_count_lines SET counted_qty = 2, version = version + 1 WHERE count_id = 1;
  RAISE EXCEPTION 'line saved after finalize';
EXCEPTION WHEN check_violation THEN NULL; END $$;
DO $$ BEGIN
  UPDATE stock_counts SET status = 'OPEN', uncounted = NULL, finalized_at = NULL WHERE id = 1;
  RAISE EXCEPTION 'finalized count reopened';
EXCEPTION WHEN check_violation THEN NULL; END $$;

SELECT 'invariants ok';
