package middleware

import (
	"log/slog"
	"time"

	"fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
)

// SlogLogger logs every HTTP request as one structured JSON line (method, path,
// status, latency, request id, and user id when authenticated). This is the
// per-request "black box" record shipped to Loki via stdout.
func SlogLogger() echo.MiddlewareFunc {
	return func(next echo.HandlerFunc) echo.HandlerFunc {
		return func(c echo.Context) error {
			start := time.Now()
			err := next(c)
			if err != nil {
				// Commit the response (sets status) before logging; the central
				// error handler is a no-op once the response is committed.
				c.Error(err)
			}

			req := c.Request()
			res := c.Response()
			attrs := []any{
				slog.String("request_id", res.Header().Get(echo.HeaderXRequestID)),
				slog.String("method", req.Method),
				slog.String("path", req.URL.Path),
				slog.Int("status", res.Status),
				slog.Int64("latency_ms", time.Since(start).Milliseconds()),
				slog.String("ip", c.RealIP()),
			}
			if claims, ok := c.Get("user").(*services.Claims); ok && claims != nil {
				attrs = append(attrs, slog.Uint64("user_id", uint64(claims.UserID)))
			}
			if err != nil {
				attrs = append(attrs, slog.String("error", err.Error()))
				slog.Error("request", attrs...)
			} else {
				slog.Info("request", attrs...)
			}
			return err
		}
	}
}
