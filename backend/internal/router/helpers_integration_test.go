//go:build integration

package router_test

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"strconv"
	"strings"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"

	"github.com/google/uuid"
	"github.com/labstack/echo/v4"
	"github.com/stretchr/testify/require"
)

func (a *testApp) req(t *testing.T, method, path, token, body string, headers map[string]string) *httptest.ResponseRecorder {
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
	for k, v := range headers {
		r.Header.Set(k, v)
	}
	rec := httptest.NewRecorder()
	a.e.ServeHTTP(rec, r)
	return rec
}

// post sends an unsafe POST with a fresh Idempotency-Key.
func (a *testApp) post(t *testing.T, path, token, body string) *httptest.ResponseRecorder {
	t.Helper()
	return a.req(t, http.MethodPost, path, token, body, map[string]string{"Idempotency-Key": uuid.NewString()})
}

func (a *testApp) seedOrg(t *testing.T, slug string, members ...uint) uint {
	t.Helper()
	org := &models.Organization{Name: slug, Slug: slug}
	require.NoError(t, repository.NewOrganizationRepository(testDB).Create(org))
	for _, u := range members {
		require.NoError(t, testDB.Exec(`INSERT INTO user_organizations (user_id, organization_id) VALUES (?, ?)`, u, org.ID).Error)
	}
	return org.ID
}

func orgPath(org uint, rest string) string {
	return "/api/orgs/" + strconv.FormatUint(uint64(org), 10) + rest
}

func decode[T any](t *testing.T, rec *httptest.ResponseRecorder) T {
	t.Helper()
	var v T
	require.NoError(t, json.Unmarshal(rec.Body.Bytes(), &v), rec.Body.String())
	return v
}

func errCode(t *testing.T, rec *httptest.ResponseRecorder) string {
	t.Helper()
	return decode[struct {
		Code string `json:"code"`
	}](t, rec).Code
}

func idStr(id uint) string { return strconv.FormatUint(uint64(id), 10) }
