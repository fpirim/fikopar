//go:build integration

package router_test

import (
	"net/http"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/models"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// adminApp returns an app with a seeded admin and that admin's token.
func adminApp(t *testing.T) (*testApp, string) {
	t.Helper()
	app := newTestApp(t)
	app.seedUser(t, "admin@x.com", "pw", models.RoleAdmin)
	return app, app.login(t, "admin@x.com", "pw")
}

func TestLogin_MalformedBody(t *testing.T) {
	app := newTestApp(t)
	rec := app.do(t, http.MethodPost, "/api/auth/login", "", "{not-json")
	assert.Equal(t, http.StatusBadRequest, rec.Code)
}

func TestUpdateMe_MalformedBody(t *testing.T) {
	app := newTestApp(t)
	app.seedUser(t, "u@x.com", "pw", models.RoleUser)
	token := app.login(t, "u@x.com", "pw")
	rec := app.do(t, http.MethodPut, "/api/users/me", token, "{not-json")
	assert.Equal(t, http.StatusBadRequest, rec.Code)
}

func TestAdminUsers_ErrorPaths(t *testing.T) {
	app, token := adminApp(t)

	// Malformed create body.
	rec := app.do(t, http.MethodPost, "/api/admin/users", token, "{bad")
	assert.Equal(t, http.StatusBadRequest, rec.Code)

	// Non-numeric id.
	rec = app.do(t, http.MethodPut, "/api/admin/users/abc", token, `{"name":"x"}`)
	assert.Equal(t, http.StatusBadRequest, rec.Code)

	// Valid id, nonexistent user.
	rec = app.do(t, http.MethodPut, "/api/admin/users/999999", token, `{"name":"x"}`)
	assert.Equal(t, http.StatusNotFound, rec.Code)

	// Delete with non-numeric id.
	rec = app.do(t, http.MethodDelete, "/api/admin/users/abc", token, "")
	assert.Equal(t, http.StatusBadRequest, rec.Code)
}

func TestAdminUsers_CreateUpdateDeleteHappyPath(t *testing.T) {
	app, token := adminApp(t)

	// Create.
	rec := app.do(t, http.MethodPost, "/api/admin/users", token,
		`{"email":"new@x.com","password":"pw","name":"New","role":"user"}`)
	assert.Equal(t, http.StatusCreated, rec.Code)

	// Grid lists users.
	rec = app.do(t, http.MethodPost, "/api/admin/users/grid", token, `{"startRow":0,"endRow":100}`)
	assert.Equal(t, http.StatusOK, rec.Code)
}

func TestAdminOrgs_ErrorPaths(t *testing.T) {
	app, token := adminApp(t)

	// Malformed create body.
	rec := app.do(t, http.MethodPost, "/api/admin/organizations", token, "{bad")
	assert.Equal(t, http.StatusBadRequest, rec.Code)

	// Invalid slug -> service rejects.
	rec = app.do(t, http.MethodPost, "/api/admin/organizations", token, `{"name":"X","slug":"Bad Slug"}`)
	assert.Equal(t, http.StatusBadRequest, rec.Code)

	// Non-numeric id on update.
	rec = app.do(t, http.MethodPut, "/api/admin/organizations/abc", token, `{"name":"x","slug":"x"}`)
	assert.Equal(t, http.StatusBadRequest, rec.Code)

	// Valid id, nonexistent org.
	rec = app.do(t, http.MethodPut, "/api/admin/organizations/999999", token, `{"name":"x","slug":"x"}`)
	assert.Equal(t, http.StatusNotFound, rec.Code)

	// Delete nonexistent org -> service surfaces the lookup error.
	rec = app.do(t, http.MethodDelete, "/api/admin/organizations/999999", token, "")
	assert.NotEqual(t, http.StatusOK, rec.Code)
}

func TestAdminOrgs_CreateUpdateDeleteHappyPath(t *testing.T) {
	app, token := adminApp(t)

	// Create.
	rec := app.do(t, http.MethodPost, "/api/admin/organizations", token, `{"name":"Acme","slug":"acme"}`)
	assert.Equal(t, http.StatusCreated, rec.Code)

	// List + grid.
	rec = app.do(t, http.MethodGet, "/api/admin/organizations", token, "")
	assert.Equal(t, http.StatusOK, rec.Code)
	rec = app.do(t, http.MethodPost, "/api/admin/organizations/grid", token, `{"startRow":0,"endRow":100}`)
	assert.Equal(t, http.StatusOK, rec.Code)

	// Update existing (id 1, freshly created).
	rec = app.do(t, http.MethodPut, "/api/admin/organizations/1", token, `{"name":"Acme Inc","slug":"acme"}`)
	assert.Equal(t, http.StatusOK, rec.Code)

	// Delete existing.
	rec = app.do(t, http.MethodDelete, "/api/admin/organizations/1", token, "")
	assert.Equal(t, http.StatusOK, rec.Code)
}

func TestAdmin_MalformedBodiesAndGrids(t *testing.T) {
	app, token := adminApp(t)

	// Create a user, then send a malformed update body for a valid id.
	rec := app.do(t, http.MethodPost, "/api/admin/users", token,
		`{"email":"u2@x.com","password":"pw","name":"U2","role":"user"}`)
	require.Equal(t, http.StatusCreated, rec.Code)
	rec = app.do(t, http.MethodPut, "/api/admin/users/2", token, "{bad")
	assert.Equal(t, http.StatusBadRequest, rec.Code)

	// Malformed grid bodies.
	rec = app.do(t, http.MethodPost, "/api/admin/users/grid", token, "{bad")
	assert.Equal(t, http.StatusBadRequest, rec.Code)
	rec = app.do(t, http.MethodPost, "/api/admin/organizations/grid", token, "{bad")
	assert.Equal(t, http.StatusBadRequest, rec.Code)

	// Create an org then send a malformed update body for a valid id.
	rec = app.do(t, http.MethodPost, "/api/admin/organizations", token, `{"name":"A","slug":"a"}`)
	require.Equal(t, http.StatusCreated, rec.Code)
	rec = app.do(t, http.MethodPut, "/api/admin/organizations/1", token, "{bad")
	assert.Equal(t, http.StatusBadRequest, rec.Code)
}

func TestAdminAvatarLogo_ErrorPaths(t *testing.T) {
	app, token := adminApp(t)

	// Non-numeric id -> 400.
	for _, p := range []string{"/api/admin/users/abc/avatar", "/api/admin/organizations/abc/logo"} {
		rec := app.do(t, http.MethodPost, p, token, "")
		assert.Equal(t, http.StatusBadRequest, rec.Code, "POST "+p)
		rec = app.do(t, http.MethodDelete, p, token, "")
		assert.Equal(t, http.StatusBadRequest, rec.Code, "DELETE "+p)
	}

	// Valid id, nonexistent target -> 404 (GetByID runs before file handling).
	for _, p := range []string{"/api/admin/users/999999/avatar", "/api/admin/organizations/999999/logo"} {
		rec := app.do(t, http.MethodPost, p, token, "")
		assert.Equal(t, http.StatusNotFound, rec.Code, "POST "+p)
		rec = app.do(t, http.MethodDelete, p, token, "")
		assert.Equal(t, http.StatusNotFound, rec.Code, "DELETE "+p)
	}
}

// TestUserSelf_StaleTokenNotFound covers the "user not found" branches in the
// self-service handlers: a token whose user has since been deleted.
func TestUserSelf_StaleTokenNotFound(t *testing.T) {
	app := newTestApp(t)
	u := app.seedUser(t, "ghost@x.com", "pw", models.RoleUser)
	token := app.login(t, "ghost@x.com", "pw")
	require.NoError(t, app.repo.Delete(u.ID))

	cases := []struct{ method, path string }{
		{http.MethodGet, "/api/users/me"},
		{http.MethodPut, "/api/users/me"},
		{http.MethodPost, "/api/users/me/avatar"},
		{http.MethodDelete, "/api/users/me/avatar"},
	}
	for _, c := range cases {
		body := ""
		if c.method == http.MethodPut {
			body = `{"name":"x"}`
		}
		rec := app.do(t, c.method, c.path, token, body)
		assert.Equal(t, http.StatusNotFound, rec.Code, c.method+" "+c.path)
	}
}
