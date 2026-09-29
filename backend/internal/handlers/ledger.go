package handlers

import (
	"context"
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
)

type LedgerHandler struct {
	ledger *services.LedgerService
	series *services.NumberSeriesService
	idem   *services.IdempotencyService
}

func NewLedgerHandler(ledger *services.LedgerService, series *services.NumberSeriesService, idem *services.IdempotencyService) *LedgerHandler {
	return &LedgerHandler{ledger: ledger, series: series, idem: idem}
}

func (h *LedgerHandler) ListAccounts(c echo.Context) error {
	sicilID, err := queryUint(c, "sicilId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	limit, _ := strconv.Atoi(c.QueryParam("limit"))
	page, err := h.ledger.ListAccounts(c.Request().Context(), orgID(c), services.AccountQuery{
		Type: c.QueryParam("type"), SicilID: sicilID, Limit: limit, Cursor: c.QueryParam("cursor"),
	})
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, page)
}

func (h *LedgerHandler) CreateAccount(c echo.Context) error {
	org := orgID(c)
	return idempotent(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in services.AccountCreate
		if err := json.Unmarshal(body, &in); err != nil {
			return 0, nil, apierror.InvalidRequest
		}
		return created(h.ledger.CreateAccount(ctx, org, in))
	})
}

func (h *LedgerHandler) GetAccount(c echo.Context) error {
	id, err := pathID(c, "accountId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	a, err := h.ledger.GetAccount(c.Request().Context(), orgID(c), id)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, a.Version)
	return c.JSON(http.StatusOK, a)
}

func (h *LedgerHandler) UpdateAccount(c echo.Context) error {
	id, err := pathID(c, "accountId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	var in services.AccountUpdate
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	a, err := h.ledger.UpdateAccount(reqCtx(c), orgID(c), id, version, in)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, a.Version)
	return c.JSON(http.StatusOK, a)
}

func (h *LedgerHandler) ListAccountRoles(c echo.Context) error {
	roles, err := h.ledger.ListAccountRoles(c.Request().Context(), orgID(c))
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, roles)
}

func (h *LedgerHandler) SetAccountRoles(c echo.Context) error {
	var roles []models.AccountRole
	if err := bindJSON(c, &roles); err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.ledger.SetAccountRoles(reqCtx(c), orgID(c), roles)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, out)
}

func (h *LedgerHandler) ListNumberSeries(c echo.Context) error {
	year, err := queryUint(c, "year")
	if err != nil {
		return apierror.Respond(c, err)
	}
	depotID, err := queryUint(c, "depotId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.series.List(c.Request().Context(), orgID(c), repository.SeriesFilter{
		Year: int(year), Prefix: c.QueryParam("prefix"), DepotID: depotID,
	})
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, out)
}

func (h *LedgerHandler) UpdateNumberSeries(c echo.Context) error {
	id, err := pathID(c, "seriesId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	var in services.SeriesUpdate
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.series.Update(reqCtx(c), orgID(c), id, version, in)
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, out.Version)
	return c.JSON(http.StatusOK, out)
}

func (h *LedgerHandler) PostEntry(c echo.Context) error {
	org := orgID(c)
	return idempotent(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in services.EntryCreate
		if err := json.Unmarshal(body, &in); err != nil {
			return 0, nil, apierror.InvalidRequest
		}
		return created(h.ledger.PostManual(ctx, org, in))
	})
}

func (h *LedgerHandler) GetEntry(c echo.Context) error {
	id, err := pathID(c, "entryId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	e, err := h.ledger.GetEntry(c.Request().Context(), orgID(c), id)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, e)
}

func (h *LedgerHandler) EntryTrail(c echo.Context) error {
	id, err := pathID(c, "entryId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	items, err := h.ledger.Trail(c.Request().Context(), orgID(c), id)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, items)
}

type reasonBody struct {
	Reason string `json:"reason"`
}

func (h *LedgerHandler) ReverseEntry(c echo.Context) error {
	id, err := pathID(c, "entryId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	org := orgID(c)
	return idempotent(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in reasonBody
		if err := json.Unmarshal(body, &in); err != nil {
			return 0, nil, apierror.InvalidRequest
		}
		return created(h.ledger.Reverse(ctx, org, id, in.Reason))
	})
}

func (h *LedgerHandler) ListEntries(c echo.Context) error {
	var q services.EntryQuery
	var err error
	if q.DepotID, err = queryUint(c, "depotId"); err != nil {
		return apierror.Respond(c, err)
	}
	if q.AccountID, err = queryUint(c, "accountId"); err != nil {
		return apierror.Respond(c, err)
	}
	if q.From, err = queryDate(c, "from"); err != nil {
		return apierror.Respond(c, err)
	}
	if q.To, err = queryDate(c, "to"); err != nil {
		return apierror.Respond(c, err)
	}
	q.Limit, _ = strconv.Atoi(c.QueryParam("limit"))
	q.Cursor = c.QueryParam("cursor")
	q.SourceType = c.QueryParam("sourceType")
	q.Search = c.QueryParam("q")
	page, err := h.ledger.ListEntries(c.Request().Context(), orgID(c), q)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, page)
}

func pathYear(c echo.Context) (int, error) {
	y, err := strconv.Atoi(c.Param("year"))
	if err != nil || y < 2000 || y > 2999 {
		return 0, apierror.WithField(apierror.InvalidRequest, "year")
	}
	return y, nil
}

func optionalDepot(c echo.Context) (*uint, error) {
	d, err := queryUint(c, "depotId")
	if err != nil || d == 0 {
		return nil, err
	}
	return &d, nil
}

func (h *LedgerHandler) GetCarryForward(c echo.Context) error {
	year, err := pathYear(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	depot, err := optionalDepot(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	cf, err := h.ledger.CarryForward(c.Request().Context(), orgID(c), year, depot)
	if err != nil {
		return apierror.Respond(c, err)
	}
	if accepts(c, mimePDF) {
		return carryForwardPDF(c, cf)
	}
	setETag(c, cf.Version)
	return c.JSON(http.StatusOK, cf)
}

// previewQuery reads If-Match and previewDepotId of the override endpoints.
func previewQuery(c echo.Context) (services.PreviewQuery, error) {
	var pq services.PreviewQuery
	d, err := queryUint(c, "previewDepotId")
	if err != nil {
		return pq, err
	}
	if d != 0 {
		pq.DepotID = &d
	}
	pq.Version, err = ifMatch(c)
	return pq, err
}

func respondPreview(c echo.Context, cf *services.CarryForward, err error) error {
	if err != nil {
		return apierror.Respond(c, err)
	}
	setETag(c, cf.Version)
	return c.JSON(http.StatusOK, cf)
}

func (h *LedgerHandler) SetCarryForwardOverride(c echo.Context) error {
	year, err := pathYear(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	var in services.OverrideInput
	if err := bindJSON(c, &in); err != nil {
		return apierror.Respond(c, err)
	}
	pq, err := previewQuery(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	cf, err := h.ledger.SetOverride(reqCtx(c), orgID(c), year, pq, in)
	return respondPreview(c, cf, err)
}

func (h *LedgerHandler) DeleteCarryForwardOverride(c echo.Context) error {
	year, err := pathYear(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	accountID, err := queryUint(c, "accountId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	depotID, err := queryUint(c, "depotId")
	if err != nil {
		return apierror.Respond(c, err)
	}
	pq, err := previewQuery(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	cf, err := h.ledger.DeleteOverride(reqCtx(c), orgID(c), year, pq, accountID, depotID)
	return respondPreview(c, cf, err)
}

func (h *LedgerHandler) PostCarryForward(c echo.Context) error {
	year, err := pathYear(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	org := orgID(c)
	return idempotentETag(c, h.idem, func(ctx context.Context, _ []byte) (int, any, error) {
		return created(h.ledger.PostCarryForward(ctx, org, year))
	})
}

func (h *LedgerHandler) ReopenCarryForward(c echo.Context) error {
	year, err := pathYear(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	version, err := ifMatch(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	org := orgID(c)
	return idempotentETag(c, h.idem, func(ctx context.Context, body []byte) (int, any, error) {
		var in reasonBody
		if err := json.Unmarshal(body, &in); err != nil {
			return 0, nil, apierror.InvalidRequest
		}
		return okStatus(h.ledger.ReopenCarryForward(ctx, org, year, version, in.Reason))
	})
}

func (h *LedgerHandler) ListOpeningBalances(c echo.Context) error {
	year, err := strconv.Atoi(c.QueryParam("year"))
	if err != nil {
		return apierror.Respond(c, apierror.WithField(apierror.InvalidRequest, "year"))
	}
	depot, err := optionalDepot(c)
	if err != nil {
		return apierror.Respond(c, err)
	}
	out, err := h.ledger.OpeningBalances(c.Request().Context(), orgID(c), year, depot)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, out)
}
