-- Contracts v1.5 (ADR-0014): the part's last purchase is readable with its invoice (Part / PartRow
-- lastPurchaseCost). last_purchase() is the LAST_PURCHASE branch of last_purchase_cost(), which now reads it.

-- The latest POSTED purchase invoice line of the part in the company, any depot, dated on or before p_date
-- (document_date, then posted_at, then line id); unit cost = net_amount / qty. Bills of a purchase waybill count;
-- returns, waybills, price differences and cancelled invoices do not.
CREATE FUNCTION last_purchase(p_org bigint, p_part bigint, p_date date)
RETURNS TABLE (unit_cost numeric(18,4), document_date date, document_no text)
LANGUAGE sql STABLE AS $$
  SELECT round(l.net_amount / l.qty, 4), d.document_date, d.document_no
    FROM document_lines l
    JOIN documents d ON d.org_id = l.org_id AND d.id = l.document_id
   WHERE l.org_id = p_org AND l.part_id = p_part
     AND d.type IN ('PURCHASE_INVOICE_WITH_WAYBILL','PURCHASE_INVOICE')
     AND d.status = 'POSTED' AND d.document_date <= p_date
   ORDER BY d.document_date DESC, d.posted_at DESC, l.id DESC
   LIMIT 1
$$;

CREATE OR REPLACE FUNCTION last_purchase_cost(p_org bigint, p_part bigint, p_date date)
RETURNS TABLE (unit_cost numeric(18,4), cost_source text)
LANGUAGE sql STABLE AS $$
  SELECT c.unit_cost, c.cost_source FROM (
    SELECT lp.unit_cost, 'LAST_PURCHASE', 1 FROM last_purchase(p_org, p_part, p_date) lp
    UNION ALL
    (SELECT p.price, 'LIST_PRICE', 2 FROM price_on(p_org, p_part, p_date) p)
    UNION ALL
    SELECT NULL, 'NONE', 3
  ) AS c(unit_cost, cost_source, rank)
  ORDER BY c.rank
  LIMIT 1
$$;
