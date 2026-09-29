package middleware

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/config"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func testAuthService() *services.AuthService {
	return services.NewAuthService(&config.Config{JWTSecret: "secret", JWTExpiryHours: 24})
}

func runJWT(t *testing.T, authHeader string) (*httptest.ResponseRecorder, bool, echo.Context) {
	t.Helper()
	e := echo.New()
	req := httptest.NewRequest(http.MethodGet, "/", nil)
	if authHeader != "" {
		req.Header.Set("Authorization", authHeader)
	}
	rec := httptest.NewRecorder()
	c := e.NewContext(req, rec)

	called := false
	next := func(c echo.Context) error {
		called = true
		return c.String(http.StatusOK, "ok")
	}
	_ = JWT(testAuthService())(next)(c)
	return rec, called, c
}

func TestJWT_MissingHeader(t *testing.T) {
	rec, called, _ := runJWT(t, "")
	assert.Equal(t, http.StatusUnauthorized, rec.Code)
	assert.False(t, called)
}

func TestJWT_BadFormat(t *testing.T) {
	rec, called, _ := runJWT(t, "Token abc")
	assert.Equal(t, http.StatusUnauthorized, rec.Code)
	assert.False(t, called)
}

func TestJWT_InvalidToken(t *testing.T) {
	rec, called, _ := runJWT(t, "Bearer not-a-jwt")
	assert.Equal(t, http.StatusUnauthorized, rec.Code)
	assert.False(t, called)
}

func TestJWT_ValidTokenSetsClaims(t *testing.T) {
	auth := testAuthService()
	token, err := auth.GenerateToken(&models.User{ID: 42, Email: "a@b.com", Role: models.RoleAdmin})
	require.NoError(t, err)

	rec, called, c := runJWT(t, "Bearer "+token)
	assert.Equal(t, http.StatusOK, rec.Code)
	assert.True(t, called, "handler should run for a valid token")

	claims, ok := c.Get("user").(*services.Claims)
	require.True(t, ok, "claims must be stored in context")
	assert.Equal(t, uint(42), claims.UserID)
	assert.Equal(t, string(models.RoleAdmin), claims.Role)
}
