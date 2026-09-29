package handlers

import (
	"context"
	"encoding/json"
	"net/http"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
)

type DepotHandler struct {
	svc  *services.DepotService
	idem *services.IdempotencyService
}

func NewDepotHandler(svc *services.DepotService, idem *services.IdempotencyService) *DepotHandler {
	return &DepotHandler{svc: svc, idem: idem}
}

func (h *DepotHandler) List(c echo.Context) error {
	depots, err := h.svc.List(c.Request().Context(), orgID(c))
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, depots)
}

func (h *DepotHandler) Create(c echo.Context) error {
	org := orgID(c)
	return idempotent(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var d models.Depot
		if err := json.Unmarshal(body, &d); err != nil {
			return 0, nil, apierror.InvalidRequest
		}
		return created(h.svc.Create(ctx, org, d))
	})
}
