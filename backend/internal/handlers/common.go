package handlers

import (
	"context"
	"io"
	"net/http"
	"strconv"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/google/uuid"
	"github.com/labstack/echo/v4"
)

// Same key as middleware.OrgIDKey; handlers cannot import middleware.
func orgID(c echo.Context) uint { return c.Get("orgID").(uint) }

// reqCtx carries the caller into audit and outbox rows.
func reqCtx(c echo.Context) context.Context {
	a := services.Actor{
		RequestID: c.Response().Header().Get(echo.HeaderXRequestID),
		IP:        c.RealIP(),
	}
	if claims, ok := c.Get("user").(*services.Claims); ok {
		a.UserID = claims.UserID
	}
	return services.WithActor(c.Request().Context(), a)
}

func pathID(c echo.Context, name string) (uint, error) {
	id, err := strconv.ParseUint(c.Param(name), 10, 32)
	if err != nil || id == 0 {
		return 0, apierror.InvalidID
	}
	return uint(id), nil
}

// ifMatch reads the version from If-Match ("3" or W/"3").
func ifMatch(c echo.Context) (int, error) {
	h := strings.TrimSpace(c.Request().Header.Get("If-Match"))
	if h == "" {
		return 0, apierror.PreconditionRequired
	}
	h = strings.Trim(strings.TrimPrefix(h, "W/"), `"`)
	v, err := strconv.Atoi(h)
	if err != nil {
		return 0, apierror.VersionConflict
	}
	return v, nil
}

func setETag(c echo.Context, version int) {
	c.Response().Header().Set("ETag", `"`+strconv.Itoa(version)+`"`)
}

const maxBody = 1 << 20

// idempotent reads the raw body once, then runs op under the request's Idempotency-Key.
func idempotent(c echo.Context, idem *services.IdempotencyService, op func(ctx context.Context, body []byte) (int, any, error)) error {
	key := strings.TrimSpace(c.Request().Header.Get("Idempotency-Key"))
	if key == "" {
		return apierror.Respond(c, apierror.IdempotencyKeyRequired)
	}
	if _, err := uuid.Parse(key); err != nil {
		return apierror.Respond(c, apierror.WithField(apierror.InvalidRequest, "Idempotency-Key"))
	}
	body, err := io.ReadAll(io.LimitReader(c.Request().Body, maxBody))
	if err != nil {
		return apierror.Respond(c, apierror.InvalidRequest)
	}
	endpoint := c.Request().Method + " " + c.Request().URL.Path
	res, err := idem.Do(reqCtx(c), orgID(c), key, endpoint, body, func(ctx context.Context) (int, any, error) {
		return op(ctx, body)
	})
	if err != nil {
		return apierror.Respond(c, err)
	}
	if res.Replayed {
		c.Response().Header().Set("Idempotent-Replayed", "true")
	}
	return c.Blob(res.Status, echo.MIMEApplicationJSON, res.Body)
}

func created(v any, err error) (int, any, error) { return http.StatusCreated, v, err }

func queryUint(c echo.Context, name string) (uint, error) {
	v := c.QueryParam(name)
	if v == "" {
		return 0, nil
	}
	n, err := strconv.ParseUint(v, 10, 32)
	if err != nil {
		return 0, apierror.WithField(apierror.InvalidRequest, name)
	}
	return uint(n), nil
}

func bindJSON(c echo.Context, v any) error {
	if err := c.Bind(v); err != nil {
		return apierror.InvalidRequest
	}
	return nil
}

func queryDate(c echo.Context, name string) (*models.Date, error) {
	v := c.QueryParam(name)
	if v == "" {
		return nil, nil
	}
	d, err := models.ParseDate(v)
	if err != nil {
		return nil, apierror.WithField(apierror.InvalidRequest, name)
	}
	return &d, nil
}
