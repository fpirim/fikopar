package handlers

import (
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

func (h *StockCountHandler) List(c echo.Context) error      { return notImplemented(c) }
func (h *StockCountHandler) Open(c echo.Context) error      { return notImplemented(c) }
func (h *StockCountHandler) Get(c echo.Context) error       { return notImplemented(c) }
func (h *StockCountHandler) GridLines(c echo.Context) error { return notImplemented(c) }
func (h *StockCountHandler) AddLine(c echo.Context) error   { return notImplemented(c) }
func (h *StockCountHandler) SetLine(c echo.Context) error   { return notImplemented(c) }
func (h *StockCountHandler) Finalize(c echo.Context) error  { return notImplemented(c) }
func (h *StockCountHandler) Cancel(c echo.Context) error    { return notImplemented(c) }
