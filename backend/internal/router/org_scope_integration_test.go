//go:build integration

package router_test

import (
	"net/http"
	"regexp"
	"strings"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/models"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

var pathParam = regexp.MustCompile(`:[A-Za-z]+`)

// Every route under /api/orgs/:orgId must reject a caller who is not a member of that org,
// before any handler runs. New routes are covered automatically.
func TestOrgScope_EveryOrgRouteRejectsNonMembers(t *testing.T) {
	app := newTestApp(t)
	u := app.seedUser(t, "a@x.com", "pw", models.RoleUser)
	_ = app.seedOrg(t, "scope-a", u.ID)
	orgB := app.seedOrg(t, "scope-b")
	token := app.login(t, "a@x.com", "pw")

	checked := 0
	for _, r := range app.e.Routes() {
		if !strings.HasPrefix(r.Path, "/api/orgs/:orgId") {
			continue
		}
		path := strings.Replace(r.Path, ":orgId", idStr(orgB), 1)
		path = pathParam.ReplaceAllString(path, "1")
		body := ""
		if r.Method != http.MethodGet && r.Method != http.MethodDelete {
			body = `{}`
		}
		rec := app.req(t, r.Method, path, token, body, map[string]string{
			"Idempotency-Key": uuid.NewString(),
			"If-Match":        `"1"`,
		})
		assert.Equal(t, http.StatusForbidden, rec.Code, "%s %s: %s", r.Method, r.Path, rec.Body.String())
		checked++
	}
	assert.GreaterOrEqual(t, checked, 7)
}

func TestOrgScope_AdminReachesAnyOrgButUnknownOrgIsNotFound(t *testing.T) {
	app, token := adminApp(t)
	org := app.seedOrg(t, "admin-any")

	rec := app.do(t, http.MethodGet, orgPath(org, "/depots"), token, "")
	assert.Equal(t, http.StatusOK, rec.Code, rec.Body.String())

	rec = app.do(t, http.MethodGet, orgPath(org+1000, "/depots"), token, "")
	assert.Equal(t, http.StatusNotFound, rec.Code)
	assert.Equal(t, "ERR_ORG_NOT_FOUND", errCode(t, rec))
}

func TestOrgScope_InvalidOrgID(t *testing.T) {
	app, token := adminApp(t)
	rec := app.do(t, http.MethodGet, "/api/orgs/abc/depots", token, "")
	assert.Equal(t, http.StatusBadRequest, rec.Code)
	assert.Equal(t, "ERR_INVALID_ID", errCode(t, rec))
}

func TestOrgScope_RevokedMembershipIsEffectiveImmediately(t *testing.T) {
	app := newTestApp(t)
	u := app.seedUser(t, "rev@x.com", "pw", models.RoleUser)
	org := app.seedOrg(t, "rev-org", u.ID)
	token := app.login(t, "rev@x.com", "pw")

	require.Equal(t, http.StatusOK, app.do(t, http.MethodGet, orgPath(org, "/depots"), token, "").Code)
	require.NoError(t, testDB.Exec(`DELETE FROM user_organizations WHERE user_id = ?`, u.ID).Error)
	assert.Equal(t, http.StatusForbidden, app.do(t, http.MethodGet, orgPath(org, "/depots"), token, "").Code)
}

func TestOrgScope_RequiresToken(t *testing.T) {
	app := newTestApp(t)
	org := app.seedOrg(t, "anon")
	assert.Equal(t, http.StatusUnauthorized, app.do(t, http.MethodGet, orgPath(org, "/depots"), "", "").Code)
}
