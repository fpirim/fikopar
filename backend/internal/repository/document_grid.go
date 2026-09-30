package repository

import (
	"github.com/fpirim/fikopar/backend/internal/models"
)

// DocumentGridRequest is gridDocuments: top-level filters plus AG Grid column filters (DocumentColId).
type DocumentGridRequest struct {
	ReportGridRequest
	Types      []string     `json:"types"`
	Statuses   []string     `json:"statuses"`
	SicilID    *uint        `json:"sicilId"`
	DepotID    *uint        `json:"depotId"`
	DateFrom   *models.Date `json:"dateFrom"`
	DateTo     *models.Date `json:"dateTo"`
	DocumentNo *string      `json:"documentNo"`
	// Billed set: waybills only, billed by a non-cancelled document or not.
	Billed *bool `json:"billed"`
	// BillsWaybill: documents that bill a waybill or not.
	BillsWaybill *bool `json:"billsWaybill"`
}

const documentGridSelect = `SELECT d.id, d.document_no, d.type, d.status, d.depot_id, dp.code AS depot_code, d.sicil_id,
	s.hesap_no, s.hesap_adi AS sicil_name, d.document_date, d.due_date, u.name AS sales_rep_name, d.gross_total,
	d.discount_total, d.net_total, d.vat_total, d.grand_total, l.document_no AS linked_document_no, d.price_direction,
	w.document_no AS waybill_document_no, d.credit_override_at IS NOT NULL AS credit_override, d.posted_at, d.version,
	d.org_id, d.waybill_id
	FROM documents d
	JOIN depots dp ON dp.id = d.depot_id
	JOIN sicils s ON s.id = d.sicil_id
	LEFT JOIN users u ON u.id = d.sales_rep_user_id
	LEFT JOIN documents l ON l.id = d.linked_document_id
	LEFT JOIN documents w ON w.id = d.waybill_id`

var documentSortCols = map[string]GridCol{
	"documentNo": {"document_no", ColText}, "type": {"type", ColText}, "status": {"status", ColText},
	"documentDate": {"document_date", ColDate}, "dueDate": {"due_date", ColDate}, "hesapNo": {"hesap_no", ColText},
	"sicilName": {"sicil_name", ColText}, "depotCode": {"depot_code", ColText}, "salesRepName": {"sales_rep_name", ColText},
	"grossTotal": {"gross_total", ColNumber}, "discountTotal": {"discount_total", ColNumber},
	"netTotal": {"net_total", ColNumber}, "vatTotal": {"vat_total", ColNumber}, "grandTotal": {"grand_total", ColNumber},
	"postedAt": {"posted_at", ColDate}, "linkedDocumentNo": {"linked_document_no", ColText},
	"waybillDocumentNo": {"waybill_document_no", ColText},
}

// documentFilterCols filters postedAt on its day; sorting keeps the instant.
var documentFilterCols = func() map[string]GridCol {
	m := make(map[string]GridCol, len(documentSortCols))
	for k, v := range documentSortCols {
		m[k] = v
	}
	m["postedAt"] = GridCol{"posted_at::date", ColDate}
	return m
}()

type DocumentGrid struct {
	Rows            []models.DocumentSummary
	TotalCount      int64
	UnfilteredCount int64
	Totals          models.DocumentTotals
}

const maxDocumentExport = 100_000

// GridDocuments pages the company's documents; all returns every filtered row (XLSX, at most 100 000).
func (q *Q) GridDocuments(orgID uint, req DocumentGridRequest, all bool) (*DocumentGrid, error) {
	if err := knownCols(req.ReportGridRequest, documentSortCols); err != nil {
		return nil, err
	}
	scope := ` WHERE d.org_id = ?`
	args := []any{orgID}
	if req.DepotID != nil {
		scope += ` AND d.depot_id = ?`
		args = append(args, *req.DepotID)
	}
	out := &DocumentGrid{}
	if err := q.db.Raw(`SELECT count(*) FROM documents d`+scope, args...).Row().Scan(&out.UnfilteredCount); err != nil {
		return nil, err
	}

	top := ` AND true`
	targs := []any{}
	if len(req.Types) > 0 {
		top += ` AND x.type = ANY(?::text[])`
		targs = append(targs, *textArray(req.Types))
	}
	if len(req.Statuses) > 0 {
		top += ` AND x.status = ANY(?::text[])`
		targs = append(targs, *textArray(req.Statuses))
	}
	if req.SicilID != nil {
		top += ` AND x.sicil_id = ?`
		targs = append(targs, *req.SicilID)
	}
	if req.DateFrom != nil {
		top += ` AND x.document_date >= ?`
		targs = append(targs, *req.DateFrom)
	}
	if req.DateTo != nil {
		top += ` AND x.document_date <= ?`
		targs = append(targs, *req.DateTo)
	}
	if req.DocumentNo != nil && *req.DocumentNo != "" {
		top += ` AND x.document_no ILIKE ?`
		targs = append(targs, "%"+escapeLike(*req.DocumentNo)+"%")
	}
	if req.Billed != nil {
		top += ` AND is_waybill(x.type) AND EXISTS (SELECT 1 FROM documents b WHERE b.org_id = x.org_id AND b.waybill_id = x.id
			AND b.status <> 'CANCELLED') = ?`
		targs = append(targs, *req.Billed)
	}
	if req.BillsWaybill != nil {
		top += ` AND (x.waybill_id IS NOT NULL) = ?`
		targs = append(targs, *req.BillsWaybill)
	}
	where, wargs, err := whereClause(req.FilterModel, documentFilterCols)
	if err != nil {
		return nil, err
	}
	search, sargs := searchClause(req.Search, []string{"document_no", "hesap_no", "sicil_name"})
	base := `SELECT * FROM (` + documentGridSelect + scope + `) x WHERE ` + where + ` AND ` + search + top
	fargs := append(append(append(append([]any{}, args...), wargs...), sargs...), targs...)

	if err := q.db.Raw(`SELECT count(*),
			coalesce(sum(gross_total) FILTER (WHERE status <> 'CANCELLED'), 0) AS gross_total,
			coalesce(sum(discount_total) FILTER (WHERE status <> 'CANCELLED'), 0) AS discount_total,
			coalesce(sum(net_total) FILTER (WHERE status <> 'CANCELLED'), 0) AS net_total,
			coalesce(sum(vat_total) FILTER (WHERE status <> 'CANCELLED'), 0) AS vat_total,
			coalesce(sum(grand_total) FILTER (WHERE status <> 'CANCELLED'), 0) AS grand_total
		FROM (`+base+`) c`, fargs...).Row().Scan(&out.TotalCount, &out.Totals.GrossTotal, &out.Totals.DiscountTotal,
		&out.Totals.NetTotal, &out.Totals.VatTotal, &out.Totals.GrandTotal); err != nil {
		return nil, err
	}
	sql := base + ` ORDER BY ` + orderClause(req.SortModel, documentSortCols, "document_date DESC, id DESC")
	if all {
		sql += ` LIMIT ?`
		fargs = append(fargs, maxDocumentExport)
	} else {
		size := req.EndRow - req.StartRow
		if size <= 0 {
			size = 100
		}
		sql += ` LIMIT ? OFFSET ?`
		fargs = append(fargs, size, req.StartRow)
	}
	out.Rows, err = list[models.DocumentSummary](q.db, sql, fargs...)
	return out, err
}
