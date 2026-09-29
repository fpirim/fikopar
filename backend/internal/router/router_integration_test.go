//go:build integration

package router_test

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/app"
	"github.com/fpirim/fikopar/backend/internal/config"
	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"
	"github.com/fpirim/fikopar/backend/internal/services"
	"github.com/fpirim/fikopar/backend/internal/testutil"

	"github.com/labstack/echo/v4"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type testApp struct {
	app  *app.App
	e    *echo.Echo
	auth *services.AuthService
	repo repository.UserRepository
}

func newTestApp(t *testing.T) *testApp {
	t.Helper()
	testutil.TruncateAll(t, testDB)

	cfg := &config.Config{
		JWTSecret:          "test-secret",
		JWTExpiryHours:     24,
		UploadDir:          t.TempDir(),
		LogoUploadDir:      t.TempDir(),
		CORSAllowedOrigins: "*",
	}

	a := app.New(testDB, cfg)
	return &testApp{app: a, e: a.Echo, auth: services.NewAuthService(cfg), repo: repository.NewUserRepository(testDB)}
}

// seedUser creates a user with a bcrypt-hashed password and returns it.
func (a *testApp) seedUser(t *testing.T, email, password string, role models.Role) *models.User {
	t.Helper()
	hash, err := a.auth.HashPassword(password)
	require.NoError(t, err)
	u := &models.User{Email: email, Password: hash, Name: email, Role: role}
	require.NoError(t, a.repo.Create(u))
	return u
}

func (a *testApp) do(t *testing.T, method, path, token, body string) *httptest.ResponseRecorder {
	t.Helper()
	var r *http.Request
	if body != "" {
		r = httptest.NewRequest(method, path, strings.NewReader(body))
		r.Header.Set(echo.HeaderContentType, echo.MIMEApplicationJSON)
	} else {
		r = httptest.NewRequest(method, path, nil)
	}
	if token != "" {
		r.Header.Set(echo.HeaderAuthorization, "Bearer "+token)
	}
	rec := httptest.NewRecorder()
	a.e.ServeHTTP(rec, r)
	return rec
}

func (a *testApp) login(t *testing.T, email, password string) string {
	t.Helper()
	rec := a.do(t, http.MethodPost, "/api/auth/login", "", `{"email":"`+email+`","password":"`+password+`"}`)
	require.Equal(t, http.StatusOK, rec.Code, "login should succeed: %s", rec.Body.String())
	var resp struct {
		Token string `json:"token"`
	}
	require.NoError(t, json.Unmarshal(rec.Body.Bytes(), &resp))
	require.NotEmpty(t, resp.Token)
	return resp.Token
}

func TestLogin_WrongPasswordRejected(t *testing.T) {
	app := newTestApp(t)
	app.seedUser(t, "admin@x.com", "correct", models.RoleAdmin)

	rec := app.do(t, http.MethodPost, "/api/auth/login", "", `{"email":"admin@x.com","password":"wrong"}`)
	assert.Equal(t, http.StatusUnauthorized, rec.Code)
}

func TestMe_RequiresToken(t *testing.T) {
	app := newTestApp(t)
	app.seedUser(t, "u@x.com", "pw", models.RoleUser)

	// Without a token.
	rec := app.do(t, http.MethodGet, "/api/users/me", "", "")
	assert.Equal(t, http.StatusUnauthorized, rec.Code)

	// With a token.
	token := app.login(t, "u@x.com", "pw")
	rec = app.do(t, http.MethodGet, "/api/users/me", token, "")
	assert.Equal(t, http.StatusOK, rec.Code)
}

// TestIDOR_UserByIDRoutesRemoved guards the security fix: /api/users/:id must
// no longer exist (it allowed any user to read/overwrite any account).
func TestIDOR_UserByIDRoutesRemoved(t *testing.T) {
	app := newTestApp(t)
	victim := app.seedUser(t, "victim@x.com", "pw", models.RoleAdmin)
	attacker := app.seedUser(t, "attacker@x.com", "pw", models.RoleUser)
	token := app.login(t, "attacker@x.com", "pw")

	path := "/api/users/" + itoa(victim.ID)
	getRec := app.do(t, http.MethodGet, path, token, "")
	assert.Equal(t, http.StatusNotFound, getRec.Code, "GET /users/:id must not exist")

	putRec := app.do(t, http.MethodPut, path, token, `{"email":"pwned@x.com","password":"pwned"}`)
	assert.Equal(t, http.StatusNotFound, putRec.Code, "PUT /users/:id must not exist")

	// Victim's credentials are untouched.
	_ = attacker
	rec := app.do(t, http.MethodPost, "/api/auth/login", "", `{"email":"victim@x.com","password":"pw"}`)
	assert.Equal(t, http.StatusOK, rec.Code, "victim password must be unchanged")
}

func TestAdminRBAC(t *testing.T) {
	app := newTestApp(t)
	app.seedUser(t, "admin@x.com", "pw", models.RoleAdmin)
	app.seedUser(t, "user@x.com", "pw", models.RoleUser)

	userToken := app.login(t, "user@x.com", "pw")
	rec := app.do(t, http.MethodGet, "/api/admin/users", userToken, "")
	assert.Equal(t, http.StatusForbidden, rec.Code, "normal user must not reach admin endpoints")

	adminToken := app.login(t, "admin@x.com", "pw")
	rec = app.do(t, http.MethodGet, "/api/admin/users", adminToken, "")
	assert.Equal(t, http.StatusOK, rec.Code, "admin must reach admin endpoints")
}

func TestMetricsEndpoint(t *testing.T) {
	app := newTestApp(t)
	// Generate some traffic so HTTP metrics exist.
	app.do(t, http.MethodGet, "/api/users/me", "", "")
	rec := app.do(t, http.MethodGet, "/metrics", "", "")
	assert.Equal(t, http.StatusOK, rec.Code)
	body := rec.Body.String()
	assert.Contains(t, body, "fikopar_", "should expose app HTTP metrics")
	assert.Contains(t, body, "go_goroutines", "should expose Go runtime metrics")
}

func TestPathTraversal_AvatarHandlerSafe(t *testing.T) {
	app := newTestApp(t)
	rec := app.do(t, http.MethodGet, "/api/uploads/avatars/..%2f..%2f..%2fetc%2fpasswd", "", "")
	assert.NotEqual(t, http.StatusOK, rec.Code, "traversal must not serve files outside the uploads dir")
	assert.NotContains(t, rec.Body.String(), "root:", "must not leak /etc/passwd contents")
}

// itoa avoids importing strconv just for one conversion in assertions above.
func itoa(u uint) string {
	if u == 0 {
		return "0"
	}
	var b [20]byte
	i := len(b)
	for u > 0 {
		i--
		b[i] = byte('0' + u%10)
		u /= 10
	}
	return string(b[i:])
}
