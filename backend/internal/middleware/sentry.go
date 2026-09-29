package middleware

import (
	"errors"
	"net/http"

	"github.com/getsentry/sentry-go"
	"github.com/labstack/echo/v4"
)

// Sentry reports handler errors and panics to Sentry/GlitchTip. It uses the
// sentry-go core (no framework-version coupling) so it stays compatible with
// echo v4. Mount it inside Recover so panics still yield a clean 500.
func Sentry() echo.MiddlewareFunc {
	return func(next echo.HandlerFunc) echo.HandlerFunc {
		return func(c echo.Context) error {
			hub := sentry.CurrentHub().Clone()
			ctx := sentry.SetHubOnContext(c.Request().Context(), hub)
			c.SetRequest(c.Request().WithContext(ctx))
			hub.Scope().SetRequest(c.Request())
			defer func() {
				if r := recover(); r != nil {
					hub.RecoverWithContext(ctx, r)
					panic(r) // let the Recover middleware return the 500
				}
			}()
			if err := next(c); err != nil {
				if shouldReport(err) {
					hub.CaptureException(err)
				}
				return err
			}
			return nil
		}
	}
}

// shouldReport decides whether an error is worth sending to Sentry/GlitchTip.
// Client errors (4xx echo.HTTPError — e.g. 404 missing static file, unmatched
// route, 405) are expected and only add noise; they stay in logs/metrics. We
// report server faults (5xx) and any non-HTTP (unexpected) error. Panics are
// captured separately via the recover path above and are unaffected.
func shouldReport(err error) bool {
	var he *echo.HTTPError
	if errors.As(err, &he) {
		return he.Code >= http.StatusInternalServerError
	}
	return true
}
