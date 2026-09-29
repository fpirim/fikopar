package handlers

import (
	"context"
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/fpirim/fikopar/backend/internal/apierror"
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

func (h *CatalogHandler) GridParts(c echo.Context) error { return notImplemented(c) }
func (h *CatalogHandler) Search(c echo.Context) error    { return notImplemented(c) }
func (h *CatalogHandler) Resolve(c echo.Context) error   { return notImplemented(c) }
