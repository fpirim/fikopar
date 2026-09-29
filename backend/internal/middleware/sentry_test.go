package middleware

import (
	"errors"
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/labstack/echo/v4"
	"github.com/stretchr/testify/assert"
)

func TestShouldReport(t *testing.T) {
	tests := []struct {
		name string
		err  error
		want bool
	}{
		{"404 not found", echo.NewHTTPError(http.StatusNotFound), false},
		{"405 method not allowed", echo.NewHTTPError(http.StatusMethodNotAllowed), false},
		{"400 bad request", echo.NewHTTPError(http.StatusBadRequest), false},
		{"401 unauthorized", echo.NewHTTPError(http.StatusUnauthorized), false},
		{"500 internal", echo.NewHTTPError(http.StatusInternalServerError), true},
		{"502 bad gateway", echo.NewHTTPError(http.StatusBadGateway), true},
		{"plain (non-HTTP) error", errors.New("boom"), true},
		{"wrapped 404", fmt.Errorf("ctx: %w", echo.NewHTTPError(http.StatusNotFound)), false},
		{"wrapped 500", fmt.Errorf("ctx: %w", echo.NewHTTPError(http.StatusInternalServerError)), true},
	}
	for _, tc := range tests {
		t.Run(tc.name, func(t *testing.T) {
			assert.Equal(t, tc.want, shouldReport(tc.err))
		})
	}
}

func TestSentry_PassesThroughAndRepanics(t *testing.T) {
	e := echo.New()
	run := func(h echo.HandlerFunc) error {
		c := e.NewContext(httptest.NewRequest(http.MethodGet, "/", nil), httptest.NewRecorder())
		return Sentry()(h)(c)
	}
	assert.NoError(t, run(func(echo.Context) error { return nil }))
	assert.Error(t, run(func(echo.Context) error { return errors.New("boom") }))
	assert.Error(t, run(func(echo.Context) error { return echo.NewHTTPError(http.StatusNotFound) }))
	assert.Panics(t, func() { _ = run(func(echo.Context) error { panic("bad") }) })
}
