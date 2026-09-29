package handlers

import (
	"context"
	"encoding/json"
	"net/http"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/export"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
)

type DocumentHandler struct {
	svc  *services.DocumentService
	idem *services.IdempotencyService
}

func NewDocumentHandler(svc *services.DocumentService, idem *services.IdempotencyService) *DocumentHandler {
	return &DocumentHandler{svc: svc, idem: idem}
}

func (h *DocumentHandler) Members(c echo.Context) error {
	out, err := h.svc.Members(c.Request().Context(), orgID(c))
	if err != nil {
		return apierror.Respond(c, err)
	}
	if out == nil {
		out = []models.Member{}
	}
	return c.JSON(http.StatusOK, out)
}

func (h *DocumentHandler) Grid(c echo.Context) error {
	var in repository.DocumentGridRequest
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	xlsx := accepts(c, mimeXLSX)
	g, err := h.svc.Grid(c.Request().Context(), orgID(c), in, xlsx)
	if err != nil {
		return apierror.Respond(c, err)
	}
	if xlsx {
		data, err := export.XLSX("Documents", documentsTable(g))
		return sendFile(c, mimeXLSX, "documents.xlsx", data, err)
	}
	rows := g.Rows
	if rows == nil {
		rows = []models.DocumentSummary{}
	}
	t := g.Totals
	return c.JSON(http.StatusOK, map[string]any{
		"rows": rows, "totalCount": g.TotalCount, "unfilteredCount": g.UnfilteredCount,
		"totals": map[string]string{"grossTotal": t.GrossTotal.String(), "discountTotal": t.DiscountTotal.String(),
			"netTotal": t.NetTotal.String(), "vatTotal": t.VatTotal.String(), "grandTotal": t.GrandTotal.String()},
	})
}

func documentsTable(g *repository.DocumentGrid) export.Table {
	t := export.Table{Columns: []export.Column{{Title: "Number", Kind: export.Text}, {Title: "Type", Kind: export.Text},
		{Title: "Status", Kind: export.Text}, {Title: "Date", Kind: export.DateCol}, {Title: "Due", Kind: export.DateCol},
		{Title: "Account no", Kind: export.Text}, {Title: "Account", Kind: export.Text}, {Title: "Depot", Kind: export.Text},
		{Title: "Salesperson", Kind: export.Text}, {Title: "Gross", Kind: export.MoneyCol}, {Title: "Discount", Kind: export.MoneyCol},
		{Title: "Net", Kind: export.MoneyCol}, {Title: "VAT", Kind: export.MoneyCol}, {Title: "Total", Kind: export.MoneyCol},
		{Title: "Original", Kind: export.Text}, {Title: "Waybill", Kind: export.Text}}}
	for _, r := range g.Rows {
		t.Rows = append(t.Rows, []any{deref(r.DocumentNo), r.Type, r.Status, r.DocumentDate, r.DueDate, r.HesapNo, r.SicilName,
			r.DepotCode, deref(r.SalesRepName), r.GrossTotal, r.DiscountTotal, r.NetTotal, r.VatTotal, r.GrandTotal,
			deref(r.LinkedDocumentNo), deref(r.WaybillDocumentNo)})
	}
	x := g.Totals
	t.Totals = []any{"Total", nil, nil, nil, nil, nil, nil, nil, nil, x.GrossTotal, x.DiscountTotal, x.NetTotal, x.VatTotal, x.GrandTotal, nil, nil}
	return t
}

func (h *DocumentHandler) Create(c echo.Context) error {
	org := orgID(c)
	return idempotentETag(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in services.DocumentWrite
		if err := unmarshal(body, &in); err != nil {
			return 0, nil, err
		}
		return created(h.svc.Create(ctx, org, in))
	})
}

func (h *DocumentHandler) Get(c echo.Context) error {
	id, err := pathID(c, "documentId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	if accepts(c, mimePDF) {
		p, err := h.svc.Print(c.Request().Context(), orgID(c), id)
		if err != nil {
			return apierror.Respond(c, err)
		}
		name := "document-" + c.Param("documentId")
		if p.Document.DocumentNo != nil {
			name = *p.Document.DocumentNo
		}
		data, err := export.PDF(documentPDF(p))
		return sendFile(c, mimePDF, name+".pdf", data, err)
	}
	d, err := h.svc.Get(c.Request().Context(), orgID(c), id)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, d.Version)
	return c.JSON(http.StatusOK, d)
}

func (h *DocumentHandler) Update(c echo.Context) error {
	id, err := pathID(c, "documentId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	var in services.DocumentWrite
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	d, err := h.svc.Update(reqCtx(c), orgID(c), id, version, in)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, d.Version)
	return c.JSON(http.StatusOK, d)
}

func (h *DocumentHandler) Delete(c echo.Context) error {
	id, err := pathID(c, "documentId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	if err := h.svc.Delete(reqCtx(c), orgID(c), id, version); err != nil {
		return apierror.Respond(c, err)
	}
	return c.NoContent(http.StatusNoContent)
}

func (h *DocumentHandler) Post(c echo.Context) error {
	id, err := pathID(c, "documentId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	org := orgID(c)
	return idempotentETag(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in services.PostInput
		if len(strings.TrimSpace(string(body))) > 0 {
			if err := json.Unmarshal(body, &in); err != nil {
				return 0, nil, apierror.InvalidRequest
			}
		}
		return okStatus(h.svc.Post(ctx, org, id, version, in))
	})
}

func (h *DocumentHandler) Cancel(c echo.Context) error {
	id, err := pathID(c, "documentId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	org := orgID(c)
	return idempotentETag(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in reasonBody
		if err := unmarshal(body, &in); err != nil {
			return 0, nil, err
		}
		return okStatus(h.svc.Cancel(ctx, org, id, version, in.Reason))
	})
}

var documentTitles = map[string]string{
	"SALES_INVOICE_WITH_WAYBILL": "Sales invoice with waybill", "SALES_INVOICE": "Sales invoice",
	"SALES_WAYBILL": "Sales waybill", "SALES_RETURN": "Sales return invoice", "SALES_RETURN_WAYBILL": "Sales return waybill",
	"PURCHASE_INVOICE_WITH_WAYBILL": "Purchase invoice with waybill", "PURCHASE_INVOICE": "Purchase invoice",
	"PURCHASE_WAYBILL": "Purchase waybill", "PURCHASE_RETURN": "Purchase return invoice",
	"PURCHASE_RETURN_WAYBILL": "Purchase return waybill", "PRICE_DIFFERENCE": "Price difference invoice",
}

// documentPDF is the printed invoice or waybill: company and card, lines, VAT per rate and totals. A draft prints
// without a number and marked DRAFT, a cancelled document marked CANCELLED.
func documentPDF(p *services.DocumentPrint) export.Document {
	d := p.Document
	title := documentTitles[d.Type]
	if d.PriceDirection != nil {
		title += " (" + strings.ToLower(*d.PriceDirection) + ")"
	}
	switch {
	case d.DocumentNo == nil:
		title += " — DRAFT"
	case d.Status == "CANCELLED":
		title += " " + *d.DocumentNo + " — CANCELLED"
	default:
		title += " " + *d.DocumentNo
	}
	card := d.Snapshot
	meta := [][2]string{{"Company", p.Company}, {"Depot", p.DepotCode}, {"Date", d.DocumentDate.String()}}
	if d.DueDate != nil {
		meta = append(meta, [2]string{"Due date", d.DueDate.String()})
	}
	if d.ShipDate != nil {
		meta = append(meta, [2]string{"Ship date", d.ShipDate.String()})
	}
	meta = append(meta, [2]string{"Account", card.HesapNo + " " + card.HesapAdi})
	if addr := strings.TrimSpace(strings.Join([]string{card.Adres, card.Ilce, card.Sehir}, " ")); addr != "" {
		meta = append(meta, [2]string{"Address", addr})
	}
	if card.VDairesi != "" || card.VNo != "" || card.TCNo != "" {
		meta = append(meta, [2]string{"Tax office / no", strings.TrimSpace(card.VDairesi + " " + card.VNo + " " + card.TCNo)})
	}
	if d.SalesRepName != nil {
		meta = append(meta, [2]string{"Salesperson", *d.SalesRepName})
	}
	if d.LinkedDocumentNo != nil {
		meta = append(meta, [2]string{"Original", *d.LinkedDocumentNo})
	}
	if d.WaybillDocumentNo != nil {
		meta = append(meta, [2]string{"Waybill", *d.WaybillDocumentNo})
	}
	if d.Description != "" {
		meta = append(meta, [2]string{"Note", d.Description})
	}
	lines := export.Table{Columns: []export.Column{{Title: "#", Kind: export.Int}, {Title: "Stock no", Kind: export.Text},
		{Title: "Description", Kind: export.Text}, {Title: "Qty", Kind: export.Number}, {Title: "Unit price", Kind: export.Number},
		{Title: "Disc. 1 %", Kind: export.Number}, {Title: "Disc. 2 %", Kind: export.Number}, {Title: "VAT %", Kind: export.Number},
		{Title: "Net", Kind: export.MoneyCol}}}
	for _, l := range d.Lines {
		name := l.Name
		if l.Description != nil && *l.Description != "" {
			name = *l.Description
		}
		lines.Rows = append(lines.Rows, []any{l.LineNo, l.StockNo, name, l.Qty, l.UnitPrice, l.Discount1Pct, l.Discount2Pct, l.VatPct, l.NetAmount})
	}
	vat := export.Table{Heading: "VAT", Columns: []export.Column{{Title: "VAT %", Kind: export.Number},
		{Title: "Base", Kind: export.MoneyCol}, {Title: "VAT", Kind: export.MoneyCol}}}
	for _, b := range d.VatBreakdown {
		vat.Rows = append(vat.Rows, []any{b.VatPct, b.Base, b.Vat})
	}
	totals := export.Table{Heading: "Totals", Columns: []export.Column{{Title: "Gross", Kind: export.MoneyCol},
		{Title: "Discount", Kind: export.MoneyCol}, {Title: "Net", Kind: export.MoneyCol}, {Title: "VAT", Kind: export.MoneyCol},
		{Title: "Total " + d.Currency, Kind: export.MoneyCol}},
		Rows: [][]any{{d.GrossTotal, d.DiscountTotal, d.NetTotal, d.VatTotal, d.GrandTotal}}}
	footer := p.Company
	if d.DocumentNo != nil {
		footer += " " + *d.DocumentNo
	}
	return export.Document{Title: title, Meta: meta, Tables: []export.Table{lines, vat, totals}, Footer: footer}
}
