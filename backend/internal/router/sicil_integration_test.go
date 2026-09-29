//go:build integration

package router_test

import (
	"encoding/json"
	"net/http"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/models"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func extractID(t *testing.T, body []byte) uint {
	t.Helper()
	var out struct {
		ID uint `json:"id"`
	}
	require.NoError(t, json.Unmarshal(body, &out))
	require.NotZero(t, out.ID)
	return out.ID
}

func (a *testApp) seedOrgWithUser(t *testing.T, userID uint, slug string) uint {
	t.Helper()
	return a.seedOrg(t, slug, userID)
}

func TestSicil_CreateAndGrid_AsMember(t *testing.T) {
	app := newTestApp(t)
	u := app.seedUser(t, "member@x.com", "pw", models.RoleUser)
	orgID := app.seedOrgWithUser(t, u.ID, "memberorg")
	token := app.login(t, "member@x.com", "pw")

	rec := app.do(t, http.MethodPost, orgPath(orgID, "/sicils"), token, `{"hesap_no":"320.C.001","hesap_adi":"Ceylan","sicil_tipi":"FIRMA"}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	assert.Contains(t, rec.Body.String(), `"organization_id":`+idStr(orgID))

	rec = app.do(t, http.MethodPost, orgPath(orgID, "/sicils/grid"), token, `{"startRow":0,"endRow":100}`)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Contains(t, rec.Body.String(), "Ceylan")
	assert.Contains(t, rec.Body.String(), `"totalCount":1`)
}

func TestSicil_OldUnscopedRoutesAreGone(t *testing.T) {
	app := newTestApp(t)
	u := app.seedUser(t, "old@x.com", "pw", models.RoleUser)
	orgID := app.seedOrgWithUser(t, u.ID, "oldorg")
	token := app.login(t, "old@x.com", "pw")
	rec := app.do(t, http.MethodPost, "/api/sicil", token, `{"organization_id":`+idStr(orgID)+`,"hesap_no":"1","hesap_adi":"x"}`)
	assert.Equal(t, http.StatusNotFound, rec.Code)
}

func TestSicil_Grid_ForbiddenForNonMember(t *testing.T) {
	app := newTestApp(t)
	owner := app.seedUser(t, "owner@x.com", "pw", models.RoleUser)
	orgOwner := app.seedOrgWithUser(t, owner.ID, "ownerorg")

	attacker := app.seedUser(t, "attacker@x.com", "pw", models.RoleUser)
	_ = app.seedOrgWithUser(t, attacker.ID, "attackerorg")
	attackerToken := app.login(t, "attacker@x.com", "pw")

	rec := app.do(t, http.MethodPost, orgPath(orgOwner, "/sicils/grid"), attackerToken, `{"startRow":0,"endRow":100}`)
	require.Equal(t, http.StatusForbidden, rec.Code, rec.Body.String())
	assert.Contains(t, rec.Body.String(), "ERR_FORBIDDEN")
}

// A card of org B addressed through org A's path must not be found, even by a member of both.
func TestSicil_CrossOrgIDIsNotFound(t *testing.T) {
	app := newTestApp(t)
	u := app.seedUser(t, "both@x.com", "pw", models.RoleUser)
	orgA := app.seedOrgWithUser(t, u.ID, "both-a")
	orgB := app.seedOrgWithUser(t, u.ID, "both-b")
	token := app.login(t, "both@x.com", "pw")

	rec := app.do(t, http.MethodPost, orgPath(orgB, "/sicils"), token, `{"hesap_no":"B1","hesap_adi":"B card"}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	cardB := extractID(t, rec.Body.Bytes())

	path := orgPath(orgA, "/sicils/"+idStr(cardB))
	assert.Equal(t, http.StatusNotFound, app.do(t, http.MethodGet, path, token, "").Code)
	assert.Equal(t, http.StatusNotFound, app.req(t, http.MethodPut, path, token, `{"hesap_adi":"hijacked"}`, map[string]string{"If-Match": `"1"`}).Code)
	assert.Equal(t, http.StatusNotFound, app.do(t, http.MethodDelete, path, token, "").Code)

	rec = app.do(t, http.MethodGet, orgPath(orgB, "/sicils/"+idStr(cardB)), token, "")
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Contains(t, rec.Body.String(), "B card")
}

func TestSicil_Update_OrganizationIDImmutable(t *testing.T) {
	app := newTestApp(t)
	u := app.seedUser(t, "u2@x.com", "pw", models.RoleUser)
	orgA := app.seedOrgWithUser(t, u.ID, "ua-org-a")
	other := app.seedOrg(t, "u2-other")
	token := app.login(t, "u2@x.com", "pw")

	rec := app.do(t, http.MethodPost, orgPath(orgA, "/sicils"), token, `{"hesap_no":"001","hesap_adi":"Card","sicil_tipi":"FIRMA"}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	cardID := extractID(t, rec.Body.Bytes())

	rec = app.req(t, http.MethodPut, orgPath(orgA, "/sicils/"+idStr(cardID)), token, `{"organization_id":`+idStr(other)+`,"hesap_adi":"Renamed"}`, map[string]string{"If-Match": `"1"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())

	rec = app.do(t, http.MethodGet, orgPath(orgA, "/sicils/"+idStr(cardID)), token, "")
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Contains(t, rec.Body.String(), `"organization_id":`+idStr(orgA))
	assert.Contains(t, rec.Body.String(), "Renamed")
}

func TestSicil_Delete_AsMemberAndForbiddenOtherwise(t *testing.T) {
	app := newTestApp(t)
	owner := app.seedUser(t, "o3@x.com", "pw", models.RoleUser)
	orgID := app.seedOrgWithUser(t, owner.ID, "o3-org")
	ownerToken := app.login(t, "o3@x.com", "pw")

	rec := app.do(t, http.MethodPost, orgPath(orgID, "/sicils"), ownerToken, `{"hesap_no":"D1","hesap_adi":"DelMe","sicil_tipi":"MUSTERI"}`)
	require.Equal(t, http.StatusCreated, rec.Code)
	cardID := extractID(t, rec.Body.Bytes())

	stranger := app.seedUser(t, "s3@x.com", "pw", models.RoleUser)
	_ = app.seedOrgWithUser(t, stranger.ID, "s3-org")
	strangerToken := app.login(t, "s3@x.com", "pw")
	rec = app.do(t, http.MethodDelete, orgPath(orgID, "/sicils/"+idStr(cardID)), strangerToken, "")
	assert.Equal(t, http.StatusForbidden, rec.Code)

	rec = app.do(t, http.MethodDelete, orgPath(orgID, "/sicils/"+idStr(cardID)), ownerToken, "")
	assert.Equal(t, http.StatusOK, rec.Code)
	assert.Contains(t, rec.Body.String(), "OK_SICIL_DELETED")
}

func TestSicil_CreateWithTelefonlar_AndUpdateReplacesThem(t *testing.T) {
	app := newTestApp(t)
	u := app.seedUser(t, "tel@x.com", "pw", models.RoleUser)
	orgID := app.seedOrgWithUser(t, u.ID, "tel-org")
	token := app.login(t, "tel@x.com", "pw")

	createBody := `{"hesap_no":"T1","hesap_adi":"WithPhones","sicil_tipi":"FIRMA",` +
		`"telefonlar":[{"tipi":"CEP","numara":"+90 555 111 22 33"},{"tipi":"IS","numara":"+90 212 444 55 66"}]}`
	rec := app.do(t, http.MethodPost, orgPath(orgID, "/sicils"), token, createBody)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	cardID := extractID(t, rec.Body.Bytes())
	assert.Contains(t, rec.Body.String(), "+90 555 111 22 33")

	rec = app.req(t, http.MethodPut, orgPath(orgID, "/sicils/"+idStr(cardID)), token, `{"telefonlar":[{"tipi":"EV","numara":"+90 312 000 00 00"}]}`, map[string]string{"If-Match": `"1"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())

	rec = app.do(t, http.MethodGet, orgPath(orgID, "/sicils/"+idStr(cardID)), token, "")
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Contains(t, rec.Body.String(), "+90 312 000 00 00")
	assert.NotContains(t, rec.Body.String(), "+90 555 111 22 33")
}

func TestSicil_RejectsInvalidTelefonTipi(t *testing.T) {
	app := newTestApp(t)
	u := app.seedUser(t, "tel2@x.com", "pw", models.RoleUser)
	orgID := app.seedOrgWithUser(t, u.ID, "tel2-org")
	token := app.login(t, "tel2@x.com", "pw")

	body := `{"hesap_no":"T1","hesap_adi":"X","sicil_tipi":"FIRMA","telefonlar":[{"tipi":"XYZ","numara":"+90"}]}`
	rec := app.do(t, http.MethodPost, orgPath(orgID, "/sicils"), token, body)
	require.Equal(t, http.StatusBadRequest, rec.Code, rec.Body.String())
	assert.Contains(t, rec.Body.String(), "ERR_INVALID_TELEFON_TIPI")
}
