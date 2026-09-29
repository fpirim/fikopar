package handlers

import (
	"net/http"

	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
)

type PriceImportHandler struct {
	svc  *services.PriceImportService
	idem *services.IdempotencyService
}

func NewPriceImportHandler(svc *services.PriceImportService, idem *services.IdempotencyService) *PriceImportHandler {
	return &PriceImportHandler{svc: svc, idem: idem}
}

func notImplemented(c echo.Context) error {
	return c.JSON(http.StatusNotImplemented, map[string]string{"code": "ERR_INTERNAL"})
}

func (h *PriceImportHandler) List(c echo.Context) error     { return notImplemented(c) }
func (h *PriceImportHandler) Create(c echo.Context) error   { return notImplemented(c) }
func (h *PriceImportHandler) Get(c echo.Context) error      { return notImplemented(c) }
func (h *PriceImportHandler) Update(c echo.Context) error   { return notImplemented(c) }
func (h *PriceImportHandler) GridRows(c echo.Context) error { return notImplemented(c) }
func (h *PriceImportHandler) Apply(c echo.Context) error    { return notImplemented(c) }
func (h *PriceImportHandler) Discard(c echo.Context) error  { return notImplemented(c) }
func (h *PriceImportHandler) Presets(c echo.Context) error  { return notImplemented(c) }
