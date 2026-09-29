package handlers

import (
	"context"
	"encoding/json"
	"net/http"
	"strconv"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/export"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
)

type CatalogHandler struct {
	svc  *services.CatalogService
	idem *services.IdempotencyService
}

func NewCatalogHandler(svc *services.CatalogService, idem *services.IdempotencyService) *CatalogHandler {
	return &CatalogHandler{svc: svc, idem: idem}
}

func unmarshal(body []byte, v any) error {
	if err := json.Unmarshal(body, v); err != nil {
		return apierror.InvalidRequest
	}
	return nil
}

func (h *CatalogHandler) ListCatalogs(c echo.Context) error {
	all, _ := strconv.ParseBool(c.QueryParam("includeInactive"))
	out, err := h.svc.ListCatalogs(c.Request().Context(), orgID(c), all)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, out)
}

func (h *CatalogHandler) CreateCatalog(c echo.Context) error {
	org := orgID(c)
	return idempotentETag(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in services.CatalogWrite
		if err := unmarshal(body, &in); err != nil {
			return 0, nil, err
		}
		return created(h.svc.CreateCatalog(ctx, org, in))
	})
}

func (h *CatalogHandler) GetCatalog(c echo.Context) error {
	id, err := pathID(c, "catalogId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.svc.GetCatalog(c.Request().Context(), orgID(c), id)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, out.Version)
	return c.JSON(http.StatusOK, out)
}

func (h *CatalogHandler) UpdateCatalog(c echo.Context) error {
	id, err := pathID(c, "catalogId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	var in services.CatalogWrite
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.svc.UpdateCatalog(reqCtx(c), orgID(c), id, version, in)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, out.Version)
	return c.JSON(http.StatusOK, out)
}

func (h *CatalogHandler) ListPrices(c echo.Context) error {
	id, err := pathID(c, "catalogId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	on, err := queryDate(c, "validOn")
	if err != nil {
		return apierror.Respond(c, err)
	}
	limit, _ := strconv.Atoi(c.QueryParam("limit"))
	out, err := h.svc.ListPrices(c.Request().Context(), orgID(c), id, on, limit, c.QueryParam("cursor"))
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, out)
}

func (h *CatalogHandler) SetPrices(c echo.Context) error {
	id, err := pathID(c, "catalogId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	org := orgID(c)
	return idempotent(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in services.SetPrices
		if err := unmarshal(body, &in); err != nil {
			return 0, nil, err
		}
		return created(h.svc.SetPrices(ctx, org, id, in))
	})
}

func (h *CatalogHandler) CreatePart(c echo.Context) error {
	org := orgID(c)
	return idempotentETag(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in services.PartWrite
		if err := unmarshal(body, &in); err != nil {
			return 0, nil, err
		}
		return created(h.svc.CreatePart(ctx, org, in))
	})
}

func (h *CatalogHandler) Lookup(c echo.Context) error {
	out, err := h.svc.Lookup(c.Request().Context(), orgID(c), c.QueryParam("code"))
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, out)
}

func (h *CatalogHandler) GetPart(c echo.Context) error {
	id, err := pathID(c, "partId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.svc.GetPart(c.Request().Context(), orgID(c), id)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, out.Version)
	return c.JSON(http.StatusOK, out)
}

func (h *CatalogHandler) UpdatePart(c echo.Context) error {
	id, err := pathID(c, "partId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	var in services.PartWrite
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.svc.UpdatePart(reqCtx(c), orgID(c), id, version, in)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, out.Version)
	return c.JSON(http.StatusOK, out)
}

func (h *CatalogHandler) PriceHistory(c echo.Context) error {
	id, err := pathID(c, "partId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.svc.PriceHistory(c.Request().Context(), orgID(c), id)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, out)
}

func (h *CatalogHandler) SetPartPrice(c echo.Context) error {
	id, err := pathID(c, "partId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	org := orgID(c)
	return idempotent(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in services.PriceWrite
		if err := unmarshal(body, &in); err != nil {
			return 0, nil, err
		}
		return h.svc.SetPartPrice(ctx, org, id, in)
	})
}

func (h *CatalogHandler) Search(c echo.Context) error {
	in := services.SearchRequest{Q: c.QueryParam("q"), Cursor: c.QueryParam("cursor")}
	var err error
	if in.CatalogID, err = queryUint(c, "catalogId"); err != nil {
		return apierror.Respond(c, err)
	}
	if in.InStock, err = queryUint(c, "inStockDepotId"); err != nil {
		return apierror.Respond(c, err)
	}
	if in.Date, err = queryDate(c, "date"); err != nil {
		return apierror.Respond(c, err)
	}
	if v := c.QueryParam("limit"); v != "" {
		if in.Limit, err = strconv.Atoi(v); err != nil || in.Limit == 0 {
			return apierror.Respond(c, apierror.WithField(apierror.InvalidRequest, "limit"))
		}
	}
	out, err := h.svc.Search(c.Request().Context(), orgID(c), in)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, out)
}

func (h *CatalogHandler) Resolve(c echo.Context) error {
	var in services.ResolveRequest
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.svc.Resolve(c.Request().Context(), orgID(c), in)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, out)
}

func (h *CatalogHandler) GridParts(c echo.Context) error {
	var in services.PartGridRequest
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	xlsx := accepts(c, mimeXLSX)
	g, err := h.svc.GridParts(c.Request().Context(), orgID(c), in, xlsx)
	if err != nil {
		return apierror.Respond(c, err)
	}
	if !xlsx {
		return c.JSON(http.StatusOK, g)
	}
	t := export.Table{Columns: []export.Column{{Title: "Stock no", Kind: export.Text}, {Title: "Name", Kind: export.Text},
		{Title: "Type", Kind: export.Text}, {Title: "Brand", Kind: export.Text}, {Title: "Catalog", Kind: export.Int},
		{Title: "Codes", Kind: export.Text}, {Title: "Unit", Kind: export.Text}, {Title: "VAT %", Kind: export.Text},
		{Title: "Package", Kind: export.Int}, {Title: "List price", Kind: export.Number}, {Title: "Valid from", Kind: export.DateCol},
		{Title: "On hand", Kind: export.Number}, {Title: "Reserved", Kind: export.Number}, {Title: "Available", Kind: export.Number},
		{Title: "Min", Kind: export.Number}, {Title: "Max", Kind: export.Number}, {Title: "Shelf", Kind: export.Text},
		{Title: "Active", Kind: export.Text}}}
	for _, r := range g.Rows {
		codes := make([]string, len(r.Codes))
		for i, pc := range r.Codes {
			codes[i] = pc.Code
		}
		t.Rows = append(t.Rows, []any{r.StockNo, r.Name, deref(r.Type), deref(r.Brand), r.CatalogCode, strings.Join(codes, ", "), r.Unit,
			r.VatPct, r.PackageQty, r.ListPrice, r.ListPriceValidFrom, r.OnHand, r.Reserved, r.Available, r.MinQty, r.MaxQty,
			deref(r.Shelf), strconv.FormatBool(r.IsActive)})
	}
	t.Totals = []any{"Total", nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, g.Totals["onHand"], g.Totals["reserved"], g.Totals["available"]}
	data, err := export.XLSX("Parts", t)
	return sendFile(c, mimeXLSX, "parts.xlsx", data, err)
}
