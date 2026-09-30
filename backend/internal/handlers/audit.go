package handlers

import (
	"net/http"
	"strconv"
	"time"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
)

type AuditHandler struct{ audit *services.AuditService }

func NewAuditHandler(audit *services.AuditService) *AuditHandler { return &AuditHandler{audit: audit} }

func (h *AuditHandler) List(c echo.Context) error {
	q := services.AuditQuery{EntityType: c.QueryParam("entityType"), EntityID: c.QueryParam("entityId"),
		Cursor: c.QueryParam("cursor")}
	var err error
	if q.ActorUserID, err = queryUint(c, "actorUserId"); err != nil {
		return apierror.Respond(c, err)
	}
	if q.From, err = queryTime(c, "from"); err != nil {
		return apierror.Respond(c, err)
	}
	if q.To, err = queryTime(c, "to"); err != nil {
		return apierror.Respond(c, err)
	}
	q.Limit, _ = strconv.Atoi(c.QueryParam("limit"))
	page, err := h.audit.List(c.Request().Context(), orgID(c), q)
	if err != nil {
		return apierror.Respond(c, err)
	}
	return c.JSON(http.StatusOK, page)
}

func queryTime(c echo.Context, name string) (*time.Time, error) {
	v := c.QueryParam(name)
	if v == "" {
		return nil, nil
	}
	t, err := time.Parse(time.RFC3339Nano, v)
	if err != nil {
		return nil, apierror.WithField(apierror.InvalidRequest, name)
	}
	return &t, nil
}
