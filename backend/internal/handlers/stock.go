package handlers

import (
	"context"
	"net/http"
	"strconv"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/export"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
)

type StockHandler struct {
	svc  *services.StockService
	idem *services.IdempotencyService
}

func NewStockHandler(svc *services.StockService, idem *services.IdempotencyService) *StockHandler {
	return &StockHandler{svc: svc, idem: idem}
}

func (h *StockHandler) PartStock(c echo.Context) error {
	id, err := pathID(c, "partId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.svc.PartStock(c.Request().Context(), orgID(c), id)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, out)
}

// SetDepotSettings takes If-Match "0" to create the depot row.
func (h *StockHandler) SetDepotSettings(c echo.Context) error {
	part, err := pathID(c, "partId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	depot, err := pathID(c, "depotId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	var in services.DepotSettingsWrite
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.svc.SetDepotSettings(reqCtx(c), orgID(c), part, depot, version, in)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, out.Version)
	return c.JSON(http.StatusOK, out)
}

func (h *StockHandler) Levels(c echo.Context) error {
	part, err := queryUint(c, "partId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	depot, err := queryUint(c, "depotId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	limit, _ := strconv.Atoi(c.QueryParam("limit"))
	out, err := h.svc.Levels(c.Request().Context(), orgID(c), part, depot, c.QueryParam("only"), limit, c.QueryParam("cursor"))
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, out)
}

func (h *StockHandler) Movements(c echo.Context) error {
	var in services.MovementGridRequest
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	xlsx := accepts(c, mimeXLSX)
	g, err := h.svc.Movements(c.Request().Context(), orgID(c), in, xlsx)
	if err != nil {
		return apierror.Respond(c, err)
	}
	if !xlsx {
		return c.JSON(http.StatusOK, g)
	}
	t := export.Table{Columns: []export.Column{{Title: "Date", Kind: export.DateCol}, {Title: "Type", Kind: export.Text},
		{Title: "Kind", Kind: export.Text}, {Title: "Number", Kind: export.Text}, {Title: "Stock no", Kind: export.Text},
		{Title: "Name", Kind: export.Text}, {Title: "Depot", Kind: export.Int}, {Title: "Qty", Kind: export.Number},
		{Title: "Delta", Kind: export.Number}, {Title: "Unit cost", Kind: export.Number}, {Title: "On hand", Kind: export.Number}}}
	for _, m := range g.Rows {
		t.Rows = append(t.Rows, []any{m.Date, m.Label, m.Kind, deref(m.SourceNo), m.StockNo, m.Name, m.DepotID, m.Qty, m.Delta,
			m.UnitCost, m.RunningOnHand})
	}
	data, err := export.XLSX("Movements", t)
	return sendFile(c, mimeXLSX, "stock-movements.xlsx", data, err)
}

func (h *StockHandler) Transfer(c echo.Context) error {
	org := orgID(c)
	return idempotent(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in services.TransferCreate
		if err := unmarshal(body, &in); err != nil {
			return 0, nil, err
		}
		return created(h.svc.Transfer(ctx, org, in))
	})
}

func (h *StockHandler) GetTransfer(c echo.Context) error {
	id, err := pathID(c, "transferId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.svc.GetTransfer(c.Request().Context(), orgID(c), id)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, out)
}
