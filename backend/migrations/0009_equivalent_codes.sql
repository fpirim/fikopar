-- Contracts v1.3: an EQUIVALENT code is the linked part's stock no. Links are unique per linked part, so one part
-- can point to two parts that share a stock no in different catalogs.

ALTER TABLE part_codes DROP CONSTRAINT part_codes_part_id_kind_normalized_code_key;
CREATE UNIQUE INDEX part_codes_code_uniq ON part_codes (part_id, kind, normalized_code) WHERE kind <> 'EQUIVALENT';
CREATE UNIQUE INDEX part_codes_equivalent_uniq ON part_codes (part_id, equivalent_part_id) WHERE kind = 'EQUIVALENT';

UPDATE part_codes pc SET code = p.stock_no
  FROM parts p
 WHERE pc.kind = 'EQUIVALENT' AND p.id = pc.equivalent_part_id AND pc.code IS DISTINCT FROM p.stock_no;

CREATE OR REPLACE FUNCTION parts_equivalent_code() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  UPDATE part_codes SET code = NEW.stock_no WHERE kind = 'EQUIVALENT' AND equivalent_part_id = NEW.id;
  RETURN NULL;
END $$;
CREATE TRIGGER parts_equivalent_code AFTER UPDATE OF stock_no ON parts
  FOR EACH ROW WHEN (OLD.stock_no IS DISTINCT FROM NEW.stock_no) EXECUTE FUNCTION parts_equivalent_code();
