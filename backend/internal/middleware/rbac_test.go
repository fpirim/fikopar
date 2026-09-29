package middleware

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"fikopar/backend/internal/models"
	"fikopar/backend/internal/services"

	"github.com/labstack/echo/v4"
	"github.com/stretchr/testify/assert"
)

func runRBAC(t *testing.T, claims *services.Claims, required ...models.Role) (*httptest.ResponseRecorder, bool) {
	t.Helper()
	e := echo.New()
	req := httptest.NewRequest(http.MethodGet, "/", nil)
	rec := httptest.NewRecorder()
	c := e.NewContext(req, rec)
	if claims != nil {
		c.Set("user", claims)
	}

	called := false
	next := func(c echo.Context) error {
		called = true
		return c.String(http.StatusOK, "ok")
	}
	_ = RequireRole(required...)(next)(c)
	return rec, called
}

func TestRBAC_NoClaims(t *testing.T) {
	rec, called := runRBAC(t, nil, models.RoleAdmin)
	assert.Equal(t, http.StatusUnauthorized, rec.Code)
	assert.False(t, called)
}

func TestRBAC_WrongRoleForbidden(t *testing.T) {
	rec, called := runRBAC(t, &services.Claims{Role: string(models.RoleUser)}, models.RoleAdmin)
	assert.Equal(t, http.StatusForbidden, rec.Code)
	assert.False(t, called)
}

func TestRBAC_MatchingRoleAllowed(t *testing.T) {
	rec, called := runRBAC(t, &services.Claims{Role: string(models.RoleAdmin)}, models.RoleAdmin)
	assert.Equal(t, http.StatusOK, rec.Code)
	assert.True(t, called)
}
