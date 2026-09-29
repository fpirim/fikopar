package handlers

import (
	"context"
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/repository"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
)

type ReceiptHandler struct {
	receipts *services.ReceiptService
	cash     *services.CashService
	idem     *services.IdempotencyService
	exports  *ReportHandler
}

func NewReceiptHandler(receipts *services.ReceiptService, cash *services.CashService, idem *services.IdempotencyService, exports *ReportHandler) *ReceiptHandler {
	return &ReceiptHandler{receipts: receipts, cash: cash, idem: idem, exports: exports}
}

func decodeInto[T any](body []byte) (T, error) {
	var v T
	if err := json.Unmarshal(body, &v); err != nil {
		return v, apierror.InvalidRequest
	}
	return v, nil
}

func (h *ReceiptHandler) Create(c echo.Context) error {
	org := orgID(c)
	return idempotent(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		in, err := decodeInto[services.ReceiptCreate](body)
		if err != nil {
			return 0, nil, err
		}
		return created(h.receipts.Create(ctx, org, in))
	})
}

func (h *ReceiptHandler) Get(c echo.Context) error {
	id, err := pathID(c, "receiptId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	r, err := h.receipts.Get(c.Request().Context(), orgID(c), id)
	if err != nil {
		return apierror.Respond(c, err)
	}
	if accepts(c, mimePDF) {
		return h.exports.ReceiptPDF(c, r)
	}
	return c.JSON(http.StatusOK, r)
}

func (h *ReceiptHandler) Void(c echo.Context) error {
	id, err := pathID(c, "receiptId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	org := orgID(c)
	return idempotent(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		in, err := decodeInto[reasonBody](body)
		if err != nil {
			return 0, nil, err
		}
		return okStatus(h.receipts.Void(ctx, org, id, in.Reason))
	})
}

func (h *ReceiptHandler) Grid(c echo.Context) error {
	var req repository.ReceiptGridRequest
	if err := bindJSON(c, &req); err != nil {
		return apierror.Respond(c, err)
	}
	if accepts(c, mimeXLSX) {
		return h.exports.ReceiptGridXLSX(c, req)
	}
	resp, err := h.receipts.Grid(c.Request().Context(), orgID(c), req)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, resp)
}

func (h *ReceiptHandler) CashBook(c echo.Context) error {
	depotID, err := queryUint(c, "depotId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	from, err := queryDate(c, "from")
	if err != nil {
		return apierror.Respond(c, err)
	}
	to, err := queryDate(c, "to")
	if err != nil {
		return apierror.Respond(c, err)
	}
	book, err := h.cash.Book(c.Request().Context(), orgID(c), depotID, from, to)
	if err != nil {
		return apierror.Respond(c, err)
	}
	if accepts(c, mimePDF) || accepts(c, mimeXLSX) {
		return h.exports.CashBookExport(c, book)
	}
	return c.JSON(http.StatusOK, book)
}

func (h *ReceiptHandler) CreateVoucher(c echo.Context) error {
	org := orgID(c)
	return idempotent(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		in, err := decodeInto[services.CashVoucherCreate](body)
		if err != nil {
			return 0, nil, err
		}
		return created(h.cash.CreateVoucher(ctx, org, in))
	})
}

func (h *ReceiptHandler) VoidVoucher(c echo.Context) error {
	id, err := pathID(c, "voucherId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	org := orgID(c)
	return idempotent(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		in, err := decodeInto[reasonBody](body)
		if err != nil {
			return 0, nil, err
		}
		return okStatus(h.cash.VoidVoucher(ctx, org, id, in.Reason))
	})
}

func (h *ReceiptHandler) CreateCount(c echo.Context) error {
	org := orgID(c)
	return idempotent(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		in, err := decodeInto[services.CashCountCreate](body)
		if err != nil {
			return 0, nil, err
		}
		return created(h.cash.CreateCount(ctx, org, in))
	})
}

func (h *ReceiptHandler) ListCounts(c echo.Context) error {
	depotID, err := queryUint(c, "depotId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	limit, _ := strconv.Atoi(c.QueryParam("limit"))
	page, err := h.cash.ListCounts(c.Request().Context(), orgID(c), depotID, limit, c.QueryParam("cursor"))
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, page)
}
