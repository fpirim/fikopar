-- Codes and names match with the Turkish letters folded to ASCII (ı i İ → i/I, ş → s, ğ → g, ü → u, ö → o, ç → c):
-- "ısı" is ISI, not S; "salter" finds "Şalter". services.NormalizeCode uses the same table.

CREATE OR REPLACE FUNCTION fold_code(s text) RETURNS text
LANGUAGE sql IMMUTABLE STRICT PARALLEL SAFE AS $$
  SELECT pg_catalog.upper(pg_catalog.regexp_replace(
           pg_catalog.translate(s, 'ıİşŞğĞüÜöÖçÇ', 'iIsSgGuUoOcC'), '[^0-9A-Za-z]', '', 'g'))
$$;

CREATE OR REPLACE FUNCTION fold_name(s text) RETURNS text
LANGUAGE sql IMMUTABLE STRICT PARALLEL SAFE AS $$
  SELECT pg_catalog.lower(pg_catalog.translate(s, 'ıİşŞğĞüÜöÖçÇ', 'iIsSgGuUoOcC'))
$$;

-- PostgreSQL 16 cannot change a generated expression: drop and re-add, then rebuild the indexes.
ALTER TABLE parts DROP COLUMN stock_no_norm, DROP COLUMN name_fold;
ALTER TABLE parts
  ADD COLUMN stock_no_norm text GENERATED ALWAYS AS (fold_code(stock_no)) STORED,
  ADD COLUMN name_fold     text GENERATED ALWAYS AS (fold_name(name)) STORED;
CREATE INDEX parts_stock_no_prefix ON parts (org_id, stock_no_norm text_pattern_ops);
CREATE INDEX parts_stock_no_trgm   ON parts USING gin (stock_no_norm gin_trgm_ops);
CREATE INDEX parts_name_trgm       ON parts USING gin (name_fold gin_trgm_ops);

ALTER TABLE part_codes DROP COLUMN normalized_code;
ALTER TABLE part_codes ADD COLUMN normalized_code text GENERATED ALWAYS AS (fold_code(code)) STORED;
CREATE UNIQUE INDEX part_codes_code_uniq ON part_codes (part_id, kind, normalized_code) WHERE kind <> 'EQUIVALENT';
CREATE INDEX part_codes_prefix_idx ON part_codes (org_id, normalized_code text_pattern_ops);
CREATE INDEX part_codes_trgm       ON part_codes USING gin (normalized_code gin_trgm_ops);

-- stock_levels: the projection is recognised by the call stack, not by a setting any session can set. A row write
-- passes only as the projection's own INSERT directly under public.apply_stock_movement() (a same-named function
-- elsewhere shows schema-qualified, and the app role cannot create in public); TRUNCATE is refused for everyone.
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

CREATE OR REPLACE FUNCTION stock_levels_guard() RETURNS trigger
LANGUAGE plpgsql SET search_path = public, pg_temp AS $$
DECLARE
  stack text;
BEGIN
  IF TG_OP <> 'TRUNCATE' THEN
    GET DIAGNOSTICS stack = PG_CONTEXT;
    -- This guard, then the projection's own statement, then the projection itself.
    IF stack ~ ('^PL/pgSQL function stock_levels_guard\(\) line \d+ at GET DIAGNOSTICS\n'
               'SQL statement "INSERT INTO stock_levels AS l [^"]*"\n'
               'PL/pgSQL function apply_stock_movement\(\) line \d+ at SQL statement(\n|$)') THEN
      RETURN CASE TG_OP WHEN 'DELETE' THEN OLD ELSE NEW END;
    END IF;
  END IF;
  RAISE EXCEPTION 'stock_levels is written by the movement projection only' USING ERRCODE = 'insufficient_privilege';
END $$;

CREATE TRIGGER stock_levels_no_truncate BEFORE TRUNCATE ON stock_levels
  FOR EACH STATEMENT EXECUTE FUNCTION stock_levels_guard();
