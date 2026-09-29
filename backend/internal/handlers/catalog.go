package handlers

import (
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

func (h *CatalogHandler) ListCatalogs(c echo.Context) error  { return notImplemented(c) }
func (h *CatalogHandler) CreateCatalog(c echo.Context) error { return notImplemented(c) }
func (h *CatalogHandler) GetCatalog(c echo.Context) error    { return notImplemented(c) }
func (h *CatalogHandler) UpdateCatalog(c echo.Context) error { return notImplemented(c) }
func (h *CatalogHandler) ListPrices(c echo.Context) error    { return notImplemented(c) }
func (h *CatalogHandler) SetPrices(c echo.Context) error     { return notImplemented(c) }
func (h *CatalogHandler) GridParts(c echo.Context) error     { return notImplemented(c) }
func (h *CatalogHandler) Search(c echo.Context) error        { return notImplemented(c) }
func (h *CatalogHandler) Resolve(c echo.Context) error       { return notImplemented(c) }
func (h *CatalogHandler) CreatePart(c echo.Context) error    { return notImplemented(c) }
func (h *CatalogHandler) Lookup(c echo.Context) error        { return notImplemented(c) }
func (h *CatalogHandler) GetPart(c echo.Context) error       { return notImplemented(c) }
func (h *CatalogHandler) UpdatePart(c echo.Context) error    { return notImplemented(c) }
func (h *CatalogHandler) PriceHistory(c echo.Context) error  { return notImplemented(c) }
func (h *CatalogHandler) SetPartPrice(c echo.Context) error  { return notImplemented(c) }
