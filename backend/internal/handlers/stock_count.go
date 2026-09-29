package handlers

import (
	"context"
	"net/http"
	"strconv"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/export"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
)

type StockCountHandler struct {
	svc  *services.StockCountService
	idem *services.IdempotencyService
}

func NewStockCountHandler(svc *services.StockCountService, idem *services.IdempotencyService) *StockCountHandler {
	return &StockCountHandler{svc: svc, idem: idem}
}

func (h *StockCountHandler) List(c echo.Context) error {
	depot, err := queryUint(c, "depotId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	year := 0
	if v := c.QueryParam("year"); v != "" {
		if year, err = strconv.Atoi(v); err != nil {
			return apierror.Respond(c, apierror.WithField(apierror.InvalidRequest, "year"))
		}
	}
	out, err := h.svc.List(c.Request().Context(), orgID(c), repository.CountFilter{Year: year, DepotID: depot, Status: c.QueryParam("status")})
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, out)
}

func (h *StockCountHandler) Open(c echo.Context) error {
	org := orgID(c)
	return idempotentETag(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in services.StockCountOpen
		if err := unmarshal(body, &in); err != nil {
			return 0, nil, err
		}
		return created(h.svc.Open(ctx, org, in))
	})
}

func optQuery(c echo.Context, name string) *string {
	if v := c.QueryParam(name); v != "" {
		return &v
	}
	return nil
}

func (h *StockCountHandler) Get(c echo.Context) error {
	id, err := pathID(c, "countId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	count, err := h.svc.Get(c.Request().Context(), orgID(c), id)
	if err != nil {
		return apierror.Respond(c, err)
	}
	if !accepts(c, mimePDF) {
		setETag(c, count.Version)
		return c.JSON(http.StatusOK, count)
	}
	sheet, only := c.QueryParam("sheet"), c.QueryParam("only")
	if sheet != "" && sheet != "BLIND" && sheet != "WITH_SYSTEM" {
		return apierror.Respond(c, apierror.WithField(apierror.InvalidRequest, "sheet"))
	}
	if only != "" && only != "ALL" && only != "NOT_COUNTED" {
		return apierror.Respond(c, apierror.WithField(apierror.InvalidRequest, "only"))
	}
	lines, err := h.svc.SheetLines(c.Request().Context(), orgID(c), id, only == "NOT_COUNTED", optQuery(c, "shelfFrom"), optQuery(c, "shelfTo"))
	if err != nil {
		return apierror.Respond(c, err)
	}
	data, err := export.PDF(countSheet(count, lines, sheet == "WITH_SYSTEM"))
	return sendFile(c, mimePDF, count.CountNo+".pdf", data, err)
}

func countSheet(count *models.StockCount, lines []models.StockCountLine, withSystem bool) export.Document {
	cols := []export.Column{{Title: "Shelf", Kind: export.Text}, {Title: "Stock no", Kind: export.Text}, {Title: "Name", Kind: export.Text},
		{Title: "Brand", Kind: export.Text}, {Title: "Unit", Kind: export.Text}}
	if withSystem {
		cols = append(cols, export.Column{Title: "System", Kind: export.Number})
	}
	cols = append(cols, export.Column{Title: "Counted", Kind: export.Text})
	t := export.Table{Columns: cols}
	for _, l := range lines {
		row := []any{l.Shelf, l.StockNo, l.Name, deref(l.Brand), l.Unit}
		if withSystem {
			row = append(row, l.SystemQty)
		}
		t.Rows = append(t.Rows, append(row, "[            ]"))
	}
	return export.Document{Title: "Stock count " + count.CountNo, Tables: []export.Table{t}, Footer: count.CountNo,
		Meta: [][2]string{{"Depot", strconv.FormatUint(uint64(count.DepotID), 10)}, {"Count date", count.CountDate.String()},
			{"Started", count.StartedAt.UTC().Format("2006-01-02 15:04")}}}
}

func (h *StockCountHandler) GridLines(c echo.Context) error {
	id, err := pathID(c, "countId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	var in repository.CountLineGridRequest
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	xlsx := accepts(c, mimeXLSX)
	rows, totals, err := h.svc.GridLines(c.Request().Context(), orgID(c), id, in, xlsx)
	if err != nil {
		return apierror.Respond(c, err)
	}
	if xlsx {
		data, err := export.XLSX("Count", countLinesTable(rows, totals))
		return sendFile(c, mimeXLSX, "count-lines.xlsx", data, err)
	}
	if rows == nil {
		rows = []models.StockCountLine{}
	}
	return c.JSON(http.StatusOK, map[string]any{
		"rows": rows, "totalCount": totals.Count,
		"totals": map[string]string{
			"systemQty": totals.SystemQty.String(), "countedQty": totals.CountedQty.String(), "difference": totals.Difference.String(),
			"value": totals.Value.String(), "valueIn": totals.ValueIn.String(), "valueOut": totals.ValueOut.String(),
		},
	})
}

func countLinesTable(rows []models.StockCountLine, totals *repository.CountLineTotals) export.Table {
	t := export.Table{Columns: []export.Column{{Title: "Shelf", Kind: export.Text}, {Title: "Stock no", Kind: export.Text},
		{Title: "Name", Kind: export.Text}, {Title: "Brand", Kind: export.Text}, {Title: "System", Kind: export.Number},
		{Title: "Counted", Kind: export.Number}, {Title: "Difference", Kind: export.Number}, {Title: "Moved since open", Kind: export.Number},
		{Title: "Unit cost", Kind: export.Number}, {Title: "Cost source", Kind: export.Text}, {Title: "Value", Kind: export.MoneyCol}}}
	for _, l := range rows {
		var value any
		if l.Value != nil {
			value = *l.Value
		}
		t.Rows = append(t.Rows, []any{l.Shelf, l.StockNo, l.Name, deref(l.Brand), l.SystemQty, l.CountedQty, l.Difference,
			l.MovedSinceOpen, l.UnitCost, l.CostSource, value})
	}
	t.Totals = []any{"Total", nil, nil, nil, totals.SystemQty, totals.CountedQty, totals.Difference, nil, nil, nil, totals.Value}
	return t
}

func (h *StockCountHandler) AddLine(c echo.Context) error {
	id, err := pathID(c, "countId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	org := orgID(c)
	return idempotentETag(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in services.StockCountLineAdd
		if err := unmarshal(body, &in); err != nil {
			return 0, nil, err
		}
		return created(h.svc.AddLine(ctx, org, id, in))
	})
}

func (h *StockCountHandler) SetLine(c echo.Context) error {
	id, err := pathID(c, "countId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	line, err := pathID(c, "lineId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	var in services.StockCountLineSet
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.svc.SetLine(reqCtx(c), orgID(c), id, line, version, in)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, out.Version)
	return c.JSON(http.StatusOK, out)
}

// Finalize moves the header version by one; the ETag is sent on success and on replays.
func (h *StockCountHandler) Finalize(c echo.Context) error {
	id, err := pathID(c, "countId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	c.Response().Before(func() {
		if c.Response().Status == http.StatusOK {
			setETag(c, version+1)
		}
	})
	org := orgID(c)
	return idempotent(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in struct {
			Uncounted string `json:"uncounted"`
		}
		if err := unmarshal(body, &in); err != nil {
			return 0, nil, err
		}
		return okStatus(h.svc.Finalize(ctx, org, id, version, in.Uncounted))
	})
}

func (h *StockCountHandler) Cancel(c echo.Context) error {
	id, err := pathID(c, "countId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	var in struct {
		Reason string `json:"reason"`
	}
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.svc.Cancel(reqCtx(c), orgID(c), id, version, in.Reason)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, out.Version)
	return c.JSON(http.StatusOK, out)
}
