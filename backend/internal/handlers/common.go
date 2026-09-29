package handlers

import (
	"context"
	"io"
	"net/http"
	"strconv"
	"strings"

	"github.com/fpirim/fikopar/backend/internal/apierror"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/google/uuid"
	"github.com/labstack/echo/v4"
)

// Same key as middleware.OrgIDKey; handlers cannot import middleware.
func orgID(c echo.Context) uint { return c.Get("orgID").(uint) }

func pathID(c echo.Context, name string) (uint, error) {
	id, err := strconv.ParseUint(c.Param(name), 10, 32)
	if err != nil || id == 0 {
		return 0, apierror.InvalidID
	}
	return uint(id), nil
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
	res, err := idem.Do(c.Request().Context(), orgID(c), key, endpoint, body, func(ctx context.Context) (int, any, error) {
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
