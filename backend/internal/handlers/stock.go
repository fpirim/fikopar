package handlers

import (
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

func (h *StockHandler) PartStock(c echo.Context) error        { return notImplemented(c) }
func (h *StockHandler) SetDepotSettings(c echo.Context) error { return notImplemented(c) }
func (h *StockHandler) Levels(c echo.Context) error           { return notImplemented(c) }
func (h *StockHandler) Movements(c echo.Context) error        { return notImplemented(c) }
func (h *StockHandler) Transfer(c echo.Context) error         { return notImplemented(c) }
func (h *StockHandler) GetTransfer(c echo.Context) error      { return notImplemented(c) }
