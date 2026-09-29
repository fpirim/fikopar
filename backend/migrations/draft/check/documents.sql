-- Contracts v1.4 documents (0004_documents): line arithmetic, reservations (D8), billing once (D4), return cap (D6),
-- price differences with the decrease cap (D7), cancellation, last purchase cost, closed years. Runs after invariants.sql (2025 is closed).

INSERT INTO users (email, name, role) VALUES ('admin@example.test', 'Admin', 'admin');
INSERT INTO parts (org_id, catalog_id, stock_no, name) VALUES (1, 1, 'P3', 'Pump');                        -- part 3
INSERT INTO stock_movements (org_id, part_id, depot_id, kind, qty, source_type, source_id, movement_date)
VALUES (1, 3, 1, 'IN', 10, 'legacy_import', 'x10', '2026-01-05');

CREATE FUNCTION pg_temp.doc(p_type text, p_date date, p_linked bigint DEFAULT NULL, p_waybill bigint DEFAULT NULL,
                            p_sicil bigint DEFAULT 1, p_depot bigint DEFAULT 1, p_direction text DEFAULT NULL)
RETURNS bigint
LANGUAGE sql AS $$
  INSERT INTO documents (org_id, depot_id, type, sicil_id, document_date, linked_document_id, waybill_id,
                         price_direction)
  VALUES (1, p_depot, p_type, p_sicil, p_date, p_linked, p_waybill,
          CASE WHEN p_type = 'PRICE_DIFFERENCE' THEN coalesce(p_direction, 'INCREASE') ELSE p_direction END) RETURNING id
$$;

-- Adds a line with the contract arithmetic and keeps the draft's totals = Σ lines.
CREATE FUNCTION pg_temp.line(p_doc bigint, p_part bigint, p_qty numeric, p_price numeric, p_d1 numeric DEFAULT 0,
                             p_d2 numeric DEFAULT 0, p_vat numeric DEFAULT 20, p_origin bigint DEFAULT NULL,
                             p_billed bigint DEFAULT NULL) RETURNS bigint
LANGUAGE plpgsql AS $$
DECLARE v_id bigint; g numeric; n numeric;
BEGIN
  g := round(p_qty * p_price, 2);
  n := round(p_qty * p_price * (100 - p_d1) * (100 - p_d2) * 0.0001, 2);
  INSERT INTO document_lines (org_id, document_id, line_no, part_id, qty, unit_price, discount1_pct, discount2_pct,
                              vat_pct, gross_amount, discount_amount, net_amount, vat_amount, origin_line_id, billed_line_id)
  VALUES (1, p_doc, (SELECT coalesce(max(line_no), 0) + 1 FROM document_lines WHERE document_id = p_doc), p_part,
          p_qty, p_price, p_d1, p_d2, p_vat, g, g - n, n, round(n * p_vat * 0.01, 2), p_origin, p_billed)
  RETURNING id INTO v_id;
  PERFORM pg_temp.totals(p_doc);
  RETURN v_id;
END $$;

-- Price-difference line: amount only (qty 1, no discounts), part of the origin line or none.
CREATE FUNCTION pg_temp.fline(p_doc bigint, p_amount numeric, p_origin bigint DEFAULT NULL, p_vat numeric DEFAULT 20)
RETURNS bigint
LANGUAGE plpgsql AS $$
DECLARE v_id bigint; n numeric := round(p_amount, 2);
BEGIN
  INSERT INTO document_lines (org_id, document_id, line_no, part_id, qty, unit_price, vat_pct, gross_amount,
                              discount_amount, net_amount, vat_amount, origin_line_id, description)
  VALUES (1, p_doc, (SELECT coalesce(max(line_no), 0) + 1 FROM document_lines WHERE document_id = p_doc),
          (SELECT part_id FROM document_lines WHERE id = p_origin), 1, p_amount, p_vat, n, 0, n,
          round(n * p_vat * 0.01, 2), p_origin, 'Fiyat farkı')
  RETURNING id INTO v_id;
  PERFORM pg_temp.totals(p_doc);
  RETURN v_id;
END $$;

CREATE FUNCTION pg_temp.totals(p_doc bigint) RETURNS void
LANGUAGE sql AS $$
  UPDATE documents d SET gross_total = s.g, discount_total = s.d, net_total = s.g - s.d, vat_total = s.v,
                         grand_total = s.g - s.d + s.v, version = d.version + 1
    FROM (SELECT coalesce(sum(gross_amount), 0) g, coalesce(sum(discount_amount), 0) d, coalesce(sum(vat_amount), 0) v
            FROM document_lines WHERE document_id = p_doc) s
   WHERE d.id = p_doc
$$;

CREATE FUNCTION pg_temp.post(p_doc bigint) RETURNS void
LANGUAGE sql AS $$
  UPDATE documents SET status = 'POSTED', document_no = 'T-' || id, posted_at = now(), version = version + 1
   WHERE id = p_doc
$$;

CREATE FUNCTION pg_temp.cancel(p_doc bigint, p_date date) RETURNS void
LANGUAGE sql AS $$
  UPDATE documents SET status = 'CANCELLED', cancelled_at = now(), cancel_date = p_date, cancel_reason = 'test',
                       version = version + 1
   WHERE id = p_doc
$$;

CREATE FUNCTION pg_temp.reserve(p_line bigint, p_qty numeric, p_expires interval DEFAULT '48 hours') RETURNS void
LANGUAGE sql AS $$
  INSERT INTO stock_reservations (org_id, document_id, line_id, part_id, depot_id, qty, expires_at)
  SELECT 1, l.document_id, l.id, l.part_id, d.depot_id, p_qty, clock_timestamp() + p_expires
    FROM document_lines l JOIN documents d ON d.id = l.document_id WHERE l.id = p_line
$$;

CREATE FUNCTION pg_temp.move(p_line bigint, p_kind text, p_date date) RETURNS void
LANGUAGE sql AS $$
  INSERT INTO stock_movements (org_id, part_id, depot_id, kind, qty, source_type, source_id, movement_date)
  SELECT 1, l.part_id, d.depot_id, p_kind, l.qty, 'document', l.id::text, p_date
    FROM document_lines l JOIN documents d ON d.id = l.document_id WHERE l.id = p_line
$$;

-- Runs p_sql in a subtransaction; it must fail with a message LIKE p_like.
CREATE FUNCTION pg_temp.refuses(p_sql text, p_like text, p_what text) RETURNS void
LANGUAGE plpgsql AS $$
DECLARE v_err text;
BEGIN
  BEGIN
    EXECUTE p_sql;
  EXCEPTION WHEN others THEN v_err := SQLERRM;
  END;
  IF v_err IS NULL THEN RAISE EXCEPTION 'missed: %', p_what; END IF;
  IF v_err NOT LIKE p_like THEN RAISE EXCEPTION 'missed: % (got "%")', p_what, v_err; END IF;
END $$;

CREATE FUNCTION pg_temp.level(p_part bigint, p_depot bigint, OUT on_hand numeric, OUT reserved numeric)
LANGUAGE sql AS $$ SELECT on_hand, reserved FROM stock_levels WHERE part_id = p_part AND depot_id = p_depot $$;

-- Line arithmetic: half-up per line, from the unrounded product.
DO $$
DECLARE d bigint := pg_temp.doc('PURCHASE_INVOICE', '2026-02-01', p_sicil => 2); l bigint;
BEGIN
  l := pg_temp.line(d, 1, 3, 12.3456, 10, 5, 20);
  IF (SELECT (gross_amount, discount_amount, net_amount, vat_amount) FROM document_lines WHERE id = l)
     IS DISTINCT FROM (37.04, 5.37, 31.67, 6.33) THEN RAISE EXCEPTION 'line arithmetic wrong'; END IF;
  l := pg_temp.line(d, 1, 1, 0.125, 0, 0, 0);
  IF (SELECT gross_amount FROM document_lines WHERE id = l) <> 0.13 THEN RAISE EXCEPTION 'half-up wrong'; END IF;
  PERFORM pg_temp.refuses(format($q$UPDATE document_lines SET net_amount = net_amount + 0.01,
    discount_amount = discount_amount - 0.01 WHERE id = %s$q$, l), '%document_lines_check%', 'net check');
  PERFORM pg_temp.refuses(format($q$UPDATE document_lines SET vat_amount = 1 WHERE id = %s$q$, l), '%document_lines_check%',
    'vat check');
  UPDATE documents SET gross_total = gross_total + 1, net_total = net_total + 1, grand_total = grand_total + 1,
                       version = version + 1 WHERE id = d;
  PERFORM pg_temp.refuses(format('SELECT pg_temp.post(%s)', d), '%totals are not the sum%', 'totals = Σ lines');
  DELETE FROM documents WHERE id = d;
  d := pg_temp.doc('SALES_INVOICE', '2026-02-01');
  PERFORM pg_temp.refuses(format('SELECT pg_temp.post(%s)', d), 'ERR_DOCUMENT_EMPTY%', 'empty post');
  DELETE FROM documents WHERE id = d;
END $$;

-- D8 reservations: only from available stock (M3), projection guarded, released by post / delete / sweeper.
DO $$
DECLARE d bigint; l1 bigint; l2 bigint; w bigint; n int;
BEGIN
  d := pg_temp.doc('SALES_INVOICE_WITH_WAYBILL', '2026-02-02');
  l1 := pg_temp.line(d, 3, 4, 100);
  l2 := pg_temp.line(d, 3, 7, 100);
  PERFORM pg_temp.reserve(l1, 4);
  PERFORM pg_temp.refuses(format('SELECT pg_temp.reserve(%s, 7)', l2), 'ERR_RESERVATION_EXCEEDS_AVAILABLE%',
    'reservation beyond available');
  PERFORM pg_temp.reserve(l2, 6);                                     -- partial: 6 of 7, STOCK_SHORT 1 in the API
  IF (pg_temp.level(3, 1)).reserved <> 10 THEN RAISE EXCEPTION 'reserved projection wrong'; END IF;
  IF EXISTS (SELECT 1 FROM stock_reservation_drift) THEN RAISE EXCEPTION 'reservation drift'; END IF;
  PERFORM pg_temp.refuses('UPDATE stock_levels SET reserved = 0 WHERE part_id = 3', 'stock_levels is written%',
    'direct reserved write');
  PERFORM pg_temp.refuses(format('UPDATE stock_reservations SET qty = 1 WHERE line_id = %s', l1), 'reservation %',
    'reservation qty change');
  UPDATE stock_reservations SET expires_at = expires_at + interval '1 hour' WHERE line_id = l1;
  PERFORM pg_temp.refuses(format('SELECT pg_temp.post(%s)', d), '%still holds reservations%', 'post while reserved');
  DELETE FROM stock_reservations WHERE document_id = d;               -- the post transaction releases first
  PERFORM pg_temp.post(d);
  PERFORM pg_temp.move(l1, 'OUT', '2026-02-02');
  PERFORM pg_temp.move(l2, 'OUT', '2026-02-02');
  IF (pg_temp.level(3, 1)).on_hand <> -1 OR (pg_temp.level(3, 1)).reserved <> 0 THEN RAISE EXCEPTION 'post levels wrong'; END IF;
  PERFORM pg_temp.refuses(format($q$SELECT pg_temp.move(%s, 'OUT', '2026-02-02')$q$, l1), '%stock_movements_source_uq%',
    'second movement of a line');
  PERFORM pg_temp.refuses(format('DELETE FROM documents WHERE id = %s', d), '%cannot be deleted%', 'posted delete');
  PERFORM pg_temp.refuses(format('UPDATE documents SET description = %L, version = version + 1 WHERE id = %s', 'x', d),
    '%cannot be edited%', 'posted edit');
  PERFORM pg_temp.refuses(format('DELETE FROM document_lines WHERE id = %s', l1), 'ERR_DOCUMENT_NOT_DRAFT%',
    'posted line delete');

  -- Nothing available (on hand −1): no reservation at all.
  d := pg_temp.doc('SALES_WAYBILL', '2026-02-03');
  l1 := pg_temp.line(d, 3, 1, 0);
  PERFORM pg_temp.refuses(format('SELECT pg_temp.reserve(%s, 1)', l1), 'ERR_RESERVATION_EXCEEDS_AVAILABLE%',
    'reservation on negative stock');
  DELETE FROM documents WHERE id = d;

  -- Purchases and bills never reserve.
  d := pg_temp.doc('PURCHASE_INVOICE', '2026-02-03', p_sicil => 2);
  l1 := pg_temp.line(d, 1, 1, 1);
  PERFORM pg_temp.refuses(format('SELECT pg_temp.reserve(%s, 1)', l1), '%cannot reserve%', 'purchase reservation');
  DELETE FROM documents WHERE id = d;

  -- Sweeper and delete (stock back to 5 on hand in depot 1).
  INSERT INTO stock_movements (org_id, part_id, depot_id, kind, qty, source_type, source_id, movement_date)
  VALUES (1, 3, 1, 'IN', 6, 'legacy_import', 'x11', '2026-02-03');
  d := pg_temp.doc('SALES_INVOICE', '2026-02-03');
  l1 := pg_temp.line(d, 3, 2, 100);
  PERFORM pg_temp.reserve(l1, 2);
  SELECT count(*) INTO n FROM release_expired_reservations(now(), 100);
  IF n <> 0 THEN RAISE EXCEPTION 'sweeper released a live reservation'; END IF;
  SELECT count(*) INTO n FROM release_expired_reservations(now() + interval '49 hours', 100);
  IF n <> 1 THEN RAISE EXCEPTION 'sweeper missed an expired reservation'; END IF;
  SELECT count(*) INTO n FROM release_expired_reservations(now() + interval '49 hours', 100);
  IF n <> 0 THEN RAISE EXCEPTION 'sweeper released twice'; END IF;
  PERFORM pg_temp.reserve(l1, 2);
  DELETE FROM documents WHERE id = d;                                  -- cascade releases
  IF (pg_temp.level(3, 1)).reserved <> 0 OR EXISTS (SELECT 1 FROM stock_reservation_drift) THEN
    RAISE EXCEPTION 'delete did not release';
  END IF;
END $$;

-- D4 billing: line for line, once (M1), no second stock move, waybill fixed while billed; free again after cancel.
DO $$
DECLARE w bigint; wl bigint; b bigint; bl bigint;
BEGIN
  w := pg_temp.doc('SALES_WAYBILL', '2026-03-01');
  wl := pg_temp.line(w, 3, 2, 100);
  b := pg_temp.doc('SALES_INVOICE', '2026-03-02', p_waybill => w);
  bl := pg_temp.line(b, 3, 1, 110, p_billed => wl);
  PERFORM pg_temp.refuses(format('SELECT pg_temp.post(%s)', b), 'ERR_WAYBILL_INVALID%', 'bill before waybill posts');
  PERFORM pg_temp.post(w);
  PERFORM pg_temp.move(wl, 'OUT', '2026-03-01');
  PERFORM pg_temp.refuses(format($q$SELECT pg_temp.doc('SALES_INVOICE', '2026-03-02', p_waybill => %s)$q$, w),
    'ERR_WAYBILL_BILLED%', 'billing once');
  PERFORM pg_temp.refuses(format($q$SELECT pg_temp.doc('SALES_RETURN', '2026-03-02', p_waybill => %s)$q$, w),
    'ERR_WAYBILL_INVALID%', 'wrong bill type');
  PERFORM pg_temp.refuses(format('SELECT pg_temp.post(%s)', b), 'ERR_WAYBILL_INVALID%', 'bill quantity mismatch');
  DELETE FROM document_lines WHERE id = bl;
  bl := pg_temp.line(b, 3, 2, 110, p_billed => wl);
  PERFORM pg_temp.post(b);
  PERFORM pg_temp.refuses(format($q$SELECT pg_temp.move(%s, 'OUT', '2026-03-02')$q$, bl), '%cannot move stock%',
    'billed stock move');
  PERFORM pg_temp.refuses(format($q$SELECT pg_temp.cancel(%s, '2026-03-03')$q$, w), 'ERR_WAYBILL_BILLED%',
    'billed waybill cancel');
  PERFORM pg_temp.cancel(b, '2026-03-03');
  b := pg_temp.doc('SALES_INVOICE', '2026-03-04', p_waybill => w);   -- billable again
  DELETE FROM documents WHERE id = b;
END $$;

-- D2 ledger: waybills never, drafts never, documents on their date.
INSERT INTO accounts (org_id, code, name, type, normal_side) VALUES (1, '600', 'Sales', 'INCOME', 'CREDIT');
DO $$
DECLARE w bigint; d bigint;
BEGIN
  SELECT id INTO w FROM documents WHERE type = 'SALES_WAYBILL' AND status = 'POSTED' LIMIT 1;
  PERFORM pg_temp.refuses(format($q$INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description,
    source_type, source_id) VALUES (1, 1, 'J-W', '2026-03-01', 'w', 'document', '%s')$q$, w), '%cannot post this entry%',
    'waybill entry');
  d := pg_temp.doc('SALES_INVOICE', '2026-03-05');
  PERFORM pg_temp.line(d, 3, 1, 50);
  PERFORM pg_temp.refuses(format($q$INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description,
    source_type, source_id) VALUES (1, 1, 'J-D', '2026-03-05', 'd', 'document', '%s')$q$, d), '%cannot post this entry%',
    'draft entry');
  PERFORM pg_temp.post(d);
  WITH e AS (INSERT INTO journal_entries (org_id, depot_id, entry_no, entry_date, description, source_type, source_id)
             VALUES (1, 1, 'J-2026-1-000100', '2026-03-05', 'T', 'document', d::text) RETURNING id)
  INSERT INTO postings (org_id, entry_id, line_no, account_id, side, amount)
  SELECT 1, e.id, n, (SELECT id FROM accounts WHERE code = c), s, 60
    FROM e, (VALUES (1, '120.C1', 'DEBIT'), (2, '600', 'CREDIT')) v(n, c, s);
END $$;

-- D6 returns: capped per original line (M2), billed returns not counted twice, price differences not counted,
-- the original fixed while returns point at it.
DO $$
DECLARE o bigint; ol bigint; r1 bigint; r1l bigint; r2 bigint; r2l bigint; ri bigint; r3 bigint; f bigint; x bigint;
BEGIN
  o := pg_temp.doc('SALES_INVOICE_WITH_WAYBILL', '2026-04-01');
  ol := pg_temp.line(o, 3, 5, 100);
  PERFORM pg_temp.post(o);
  f := pg_temp.doc('PRICE_DIFFERENCE', '2026-04-02', o);
  PERFORM pg_temp.fline(f, 10, ol);
  PERFORM pg_temp.post(f);
  PERFORM pg_temp.refuses(format($q$SELECT pg_temp.move(%s, 'OUT', '2026-04-02')$q$,
    (SELECT id FROM document_lines WHERE document_id = f)), '%cannot move stock%', 'price difference stock move');

  r1 := pg_temp.doc('SALES_RETURN_WAYBILL', '2026-04-03', o);
  r1l := pg_temp.line(r1, 3, 3, 100, p_origin => ol);
  PERFORM pg_temp.post(r1);
  r2 := pg_temp.doc('SALES_RETURN_WAYBILL', '2026-04-04', o);
  r2l := pg_temp.line(r2, 3, 3, 100, p_origin => ol);
  PERFORM pg_temp.refuses(format('SELECT pg_temp.post(%s)', r2), 'ERR_RETURN_QTY_EXCEEDED%', 'return cap');
  DELETE FROM document_lines WHERE id = r2l;
  r2l := pg_temp.line(r2, 3, 2, 100, p_origin => ol);
  PERFORM pg_temp.post(r2);                                            -- 3 + 2 = 5 returned

  ri := pg_temp.doc('SALES_RETURN', '2026-04-05', o, r1);             -- bills r1, counted once
  PERFORM pg_temp.line(ri, 3, 3, 100, p_origin => ol, p_billed => r1l);
  PERFORM pg_temp.post(ri);

  r3 := pg_temp.doc('SALES_RETURN_WAYBILL', '2026-04-06', o);
  PERFORM pg_temp.line(r3, 3, 1, 100, p_origin => ol);
  PERFORM pg_temp.refuses(format('SELECT pg_temp.post(%s)', r3), 'ERR_RETURN_QTY_EXCEEDED%', 'return cap when full');
  PERFORM pg_temp.refuses(format($q$SELECT pg_temp.cancel(%s, '2026-04-06')$q$, o), 'ERR_DOCUMENT_HAS_DEPENDENTS%',
    'original cancel with returns');
  PERFORM pg_temp.cancel(r2, '2026-04-06');                            -- frees 2
  PERFORM pg_temp.post(r3);

  x := pg_temp.doc('SALES_RETURN_WAYBILL', '2026-04-07', f);           -- a price difference is not an original
  PERFORM pg_temp.line(x, 3, 1, 1, p_origin => (SELECT id FROM document_lines WHERE document_id = f));
  PERFORM pg_temp.refuses(format('SELECT pg_temp.post(%s)', x), 'ERR_LINKED_DOCUMENT_INVALID%', 'return of a price difference');
  DELETE FROM documents WHERE id = x;
  x := pg_temp.doc('SALES_RETURN_WAYBILL', '2026-04-07', o);
  PERFORM pg_temp.line(x, 1, 1, 1, p_origin => ol);                    -- other part
  PERFORM pg_temp.refuses(format('SELECT pg_temp.post(%s)', x), 'ERR_LINKED_DOCUMENT_INVALID%', 'return of another part');
  DELETE FROM documents WHERE id = x;
  x := pg_temp.doc('PRICE_DIFFERENCE', '2026-04-07', o);
  PERFORM pg_temp.fline(x, 1, r1l);                                     -- a line of another document
  PERFORM pg_temp.refuses(format('SELECT pg_temp.post(%s)', x), 'ERR_LINKED_DOCUMENT_INVALID%',
    'price difference on a line of another document');
  DELETE FROM documents WHERE id = x;
  PERFORM pg_temp.refuses($q$SELECT pg_temp.doc('SALES_RETURN_WAYBILL', '2026-04-07')$q$, '%documents_check%',
    'unlinked return');
END $$;

-- D7 price differences: amount-only lines, direction required, a DECREASE capped by the original's net minus
-- earlier decreases and goods-moving returns (M4), of its own lines or of the waybill it bills.
DO $$
DECLARE o bigint; ol bigint; r bigint; f bigint; d1 bigint; d2 bigint; w bigint; wl bigint; b bigint; a bigint;
BEGIN
  o := pg_temp.doc('SALES_INVOICE_WITH_WAYBILL', '2026-04-10');
  ol := pg_temp.line(o, 3, 2, 100);                                      -- net 200
  PERFORM pg_temp.post(o);
  PERFORM pg_temp.refuses(format($q$INSERT INTO documents (org_id, depot_id, type, sicil_id, document_date, linked_document_id)
    VALUES (1, 1, 'PRICE_DIFFERENCE', 1, '2026-04-10', %s)$q$, o), '%documents_check%', 'price difference without direction');
  PERFORM pg_temp.refuses($q$SELECT pg_temp.doc('SALES_INVOICE', '2026-04-10', p_direction => 'DECREASE')$q$,
    '%documents_check%', 'direction on an invoice');
  f := pg_temp.doc('PRICE_DIFFERENCE', '2026-04-11', o);
  PERFORM pg_temp.refuses(format('SELECT pg_temp.line(%s, 3, 2, 5, p_origin => %s)', f, ol), '%amount only%',
    'price difference line with qty');
  PERFORM pg_temp.refuses(format('SELECT pg_temp.line(%s, 3, 1, 5)', f), '%amount only%',
    'price difference line with a part and no origin');
  PERFORM pg_temp.refuses(format($q$INSERT INTO document_lines (org_id, document_id, line_no, qty, unit_price,
    gross_amount, discount_amount, net_amount, vat_amount) VALUES (1, %s, 9, 1, 1, 1, 0, 1, 0)$q$,
    pg_temp.doc('SALES_INVOICE', '2026-04-10')), '%needs a part%', 'invoice line without part');
  PERFORM pg_temp.fline(f, 50);                                          -- increase, no origin line
  PERFORM pg_temp.post(f);

  r := pg_temp.doc('SALES_RETURN', '2026-04-12', o);
  PERFORM pg_temp.line(r, 3, 1, 100, p_origin => ol);                    -- returns net 100
  PERFORM pg_temp.post(r);
  d1 := pg_temp.doc('PRICE_DIFFERENCE', '2026-04-13', o, p_direction => 'DECREASE');
  PERFORM pg_temp.fline(d1, 80, ol);
  PERFORM pg_temp.post(d1);                                              -- 100 + 80 ≤ 200
  d2 := pg_temp.doc('PRICE_DIFFERENCE', '2026-04-14', o, p_direction => 'DECREASE');
  PERFORM pg_temp.fline(d2, 20.01);
  PERFORM pg_temp.refuses(format('SELECT pg_temp.post(%s)', d2), 'ERR_PRICE_DIFFERENCE_EXCEEDED%', 'decrease cap');
  DELETE FROM document_lines WHERE document_id = d2;
  PERFORM pg_temp.fline(d2, 20);
  PERFORM pg_temp.post(d2);                                              -- exactly 200
  PERFORM pg_temp.refuses(format($q$SELECT pg_temp.cancel(%s, '2026-04-15')$q$, o), 'ERR_DOCUMENT_HAS_DEPENDENTS%',
    'original cancel with price differences');
  PERFORM pg_temp.cancel(d1, '2026-04-15');                              -- frees 80
  d1 := pg_temp.doc('PRICE_DIFFERENCE', '2026-04-15', o, p_direction => 'DECREASE');
  PERFORM pg_temp.fline(d1, 80);
  PERFORM pg_temp.post(d1);

  w := pg_temp.doc('SALES_WAYBILL', '2026-04-16');                       -- a bill: returns link its waybill
  wl := pg_temp.line(w, 3, 2, 100);
  PERFORM pg_temp.post(w);
  b := pg_temp.doc('SALES_INVOICE', '2026-04-16', p_waybill => w);
  PERFORM pg_temp.line(b, 3, 2, 100, p_billed => wl);
  PERFORM pg_temp.post(b);
  r := pg_temp.doc('SALES_RETURN_WAYBILL', '2026-04-17', w);
  PERFORM pg_temp.line(r, 3, 1, 100, p_origin => wl);
  PERFORM pg_temp.post(r);
  f := pg_temp.doc('PRICE_DIFFERENCE', '2026-04-18', b, p_direction => 'DECREASE');
  PERFORM pg_temp.fline(f, 100.01);
  PERFORM pg_temp.refuses(format('SELECT pg_temp.post(%s)', f), 'ERR_PRICE_DIFFERENCE_EXCEEDED%',
    'decrease cap counts returns of the billed waybill');
  DELETE FROM documents WHERE id = f;

  INSERT INTO parts (org_id, catalog_id, stock_no, name) VALUES (1, 1, 'P4', 'Valve') RETURNING id INTO wl;
  a := pg_temp.doc('PURCHASE_INVOICE', '2026-04-19', p_sicil => 2);
  PERFORM pg_temp.line(a, wl, 1, 100);
  PERFORM pg_temp.post(a);
  f := pg_temp.doc('PRICE_DIFFERENCE', '2026-04-19', a, p_sicil => 2, p_direction => 'DECREASE');
  PERFORM pg_temp.fline(f, 100.01);
  PERFORM pg_temp.refuses(format('SELECT pg_temp.post(%s)', f), 'ERR_PRICE_DIFFERENCE_EXCEEDED%', 'purchase decrease cap');
  DELETE FROM documents WHERE id = f;
END $$;

-- D1 cancellation: POSTED → CANCELLED only; opposite movement once, on the cancel date, after the cancel.
DO $$
DECLARE c bigint; cl bigint;
BEGIN
  c := pg_temp.doc('SALES_INVOICE_WITH_WAYBILL', '2026-05-01');
  cl := pg_temp.line(c, 3, 1, 100);
  PERFORM pg_temp.refuses(format($q$SELECT pg_temp.cancel(%s, '2026-05-01')$q$, c), 'ERR_INVALID_STATE_TRANSITION%',
    'draft cancel');
  PERFORM pg_temp.post(c);
  PERFORM pg_temp.move(cl, 'OUT', '2026-05-01');
  PERFORM pg_temp.refuses(format($q$SELECT pg_temp.move(%s, 'IN', '2026-05-02')$q$, cl), '%cannot move IN%',
    'opposite movement before cancel');
  PERFORM pg_temp.cancel(c, '2026-05-02');
  PERFORM pg_temp.refuses(format($q$SELECT pg_temp.move(%s, 'IN', '2026-05-03')$q$, cl), '%cannot move IN%',
    'opposite movement off the cancel date');
  PERFORM pg_temp.move(cl, 'IN', '2026-05-02');
  PERFORM pg_temp.refuses(format($q$SELECT pg_temp.move(%s, 'IN', '2026-05-02')$q$, cl), '%stock_movements_source_uq%',
    'second opposite movement');
  PERFORM pg_temp.refuses(format($q$UPDATE documents SET status = 'POSTED', cancelled_at = NULL, cancel_date = NULL,
    cancel_reason = NULL, version = version + 1 WHERE id = %s$q$, c), 'ERR_INVALID_STATE_TRANSITION%', 'uncancel');
END $$;

-- Credit override is all or nothing and only on a posted document.
DO $$
DECLARE d bigint;
BEGIN
  d := pg_temp.doc('SALES_INVOICE', '2026-05-05');
  PERFORM pg_temp.line(d, 3, 1, 100);
  PERFORM pg_temp.refuses(format($q$UPDATE documents SET credit_override_reason = 'ok boss', version = version + 1
    WHERE id = %s$q$, d), '%documents_check%', 'partial override');
  UPDATE documents SET status = 'POSTED', document_no = 'T-' || id, posted_at = now(), credit_override_reason = 'known customer',
         credit_override_by = 1, credit_override_at = now(), credit_override_excess = 20, version = version + 1
   WHERE id = d;
END $$;

-- Last purchase cost (v1.4): latest posted purchase invoice line, any depot; returns and cancelled invoices ignored.
DO $$
DECLARE a1 bigint; a2 bigint; a3 bigint; ai bigint; l2 bigint;
BEGIN
  IF (SELECT cost_source FROM last_purchase_cost(1, 1, '2026-06-01')) <> 'LIST_PRICE' THEN
    RAISE EXCEPTION 'list price fallback missed';
  END IF;
  a1 := pg_temp.doc('PURCHASE_INVOICE', '2026-06-01', p_sicil => 2);
  PERFORM pg_temp.line(a1, 1, 10, 40);
  PERFORM pg_temp.post(a1);
  a2 := pg_temp.doc('PURCHASE_INVOICE_WITH_WAYBILL', '2026-06-10', p_sicil => 2, p_depot => 2);
  l2 := pg_temp.line(a2, 1, 4, 30, 10);
  PERFORM pg_temp.post(a2);
  ai := pg_temp.doc('PURCHASE_RETURN', '2026-06-11', a2, p_sicil => 2, p_depot => 2);
  PERFORM pg_temp.line(ai, 1, 1, 5, p_origin => l2);
  PERFORM pg_temp.post(ai);
  a3 := pg_temp.doc('PURCHASE_INVOICE', '2026-06-12', p_sicil => 2);
  PERFORM pg_temp.line(a3, 1, 1, 99);
  PERFORM pg_temp.post(a3);
  PERFORM pg_temp.cancel(a3, '2026-06-12');
  IF (SELECT (unit_cost, cost_source) FROM last_purchase_cost(1, 1, '2026-06-30'))
     IS DISTINCT FROM (27.0000::numeric, 'LAST_PURCHASE'::text) THEN RAISE EXCEPTION 'last purchase cost wrong'; END IF;
  IF (SELECT unit_cost FROM last_purchase_cost(1, 1, '2026-06-05')) <> 40 THEN RAISE EXCEPTION 'cost on date wrong'; END IF;
  IF (SELECT cost_source FROM last_purchase_cost(1, 3, '2026-06-30')) <> 'NONE' THEN RAISE EXCEPTION 'NONE missed'; END IF;
END $$;

-- Closed years: 2025 is closed by the active 2026 run.
DO $$
DECLARE d bigint;
BEGIN
  d := pg_temp.doc('SALES_INVOICE', '2025-12-31');
  PERFORM pg_temp.line(d, 3, 1, 100);
  PERFORM pg_temp.refuses(format('SELECT pg_temp.post(%s)', d), 'ERR_YEAR_CLOSED%', 'post in a closed year');
  DELETE FROM documents WHERE id = d;
  IF EXISTS (SELECT 1 FROM stock_level_drift) OR EXISTS (SELECT 1 FROM stock_reservation_drift)
     OR EXISTS (SELECT 1 FROM stock_levels WHERE reserved <> 0) THEN RAISE EXCEPTION 'stock drift'; END IF;
END $$;

SELECT 'documents ok';
