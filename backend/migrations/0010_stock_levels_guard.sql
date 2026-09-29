-- stock_levels is a projection of stock_movements: only the movement trigger writes it, for the owner too.
-- apply_stock_movement() marks its own writes with a transaction-local setting.

CREATE OR REPLACE FUNCTION apply_stock_movement() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp AS $$
BEGIN
  PERFORM set_config('fikopar.stock_projection', 'on', true);
  INSERT INTO stock_levels AS l (org_id, part_id, depot_id, on_hand, version)
  VALUES (NEW.org_id, NEW.part_id, NEW.depot_id, movement_sign(NEW.kind) * NEW.qty, 1)
  ON CONFLICT (part_id, depot_id)
  DO UPDATE SET on_hand = l.on_hand + movement_sign(NEW.kind) * NEW.qty, version = l.version + 1;
  PERFORM set_config('fikopar.stock_projection', 'off', true);
  RETURN NULL;
END $$;
REVOKE ALL ON FUNCTION apply_stock_movement() FROM PUBLIC;

CREATE OR REPLACE FUNCTION stock_levels_guard() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF current_setting('fikopar.stock_projection', true) IS DISTINCT FROM 'on' THEN
    RAISE EXCEPTION 'stock_levels is written by the movement projection only' USING ERRCODE = 'insufficient_privilege';
  END IF;
  RETURN CASE TG_OP WHEN 'DELETE' THEN OLD ELSE NEW END;
END $$;
CREATE TRIGGER stock_levels_guard
  BEFORE INSERT OR UPDATE OR DELETE ON stock_levels
  FOR EACH ROW EXECUTE FUNCTION stock_levels_guard();
