//go:build integration

package router_test

import (
	"encoding/json"
	"net/http"
	"strconv"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"

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
	orgRepo := repository.NewOrganizationRepository(testDB)
	org := &models.Organization{Name: slug, Slug: slug}
	require.NoError(t, orgRepo.Create(org))
	require.NoError(t, a.repo.SetOrganizations(userID, []uint{org.ID}))
	return org.ID
}

func TestSicil_CreateAndGrid_AsMember(t *testing.T) {
	app := newTestApp(t)
	u := app.seedUser(t, "member@x.com", "pw", models.RoleUser)
	orgID := app.seedOrgWithUser(t, u.ID, "memberorg")
	token := app.login(t, "member@x.com", "pw")

	body := `{"organization_id":` + strconv.FormatUint(uint64(orgID), 10) +
		`,"hesap_no":"320.C.001","hesap_adi":"Ceylan","sicil_tipi":"FIRMA"}`
	rec := app.do(t, http.MethodPost, "/api/sicil", token, body)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())

	gridBody := `{"organization_id":` + strconv.FormatUint(uint64(orgID), 10) + `,"startRow":0,"endRow":100}`
	rec = app.do(t, http.MethodPost, "/api/sicil/grid", token, gridBody)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Contains(t, rec.Body.String(), "Ceylan")
	assert.Contains(t, rec.Body.String(), `"totalCount":1`)
}

func TestSicil_Grid_ForbiddenForNonMember(t *testing.T) {
	app := newTestApp(t)
	owner := app.seedUser(t, "owner@x.com", "pw", models.RoleUser)
	orgOwner := app.seedOrgWithUser(t, owner.ID, "ownerorg")

	attacker := app.seedUser(t, "attacker@x.com", "pw", models.RoleUser)
	_ = app.seedOrgWithUser(t, attacker.ID, "attackerorg")
	attackerToken := app.login(t, "attacker@x.com", "pw")

	gridBody := `{"organization_id":` + strconv.FormatUint(uint64(orgOwner), 10) + `,"startRow":0,"endRow":100}`
	rec := app.do(t, http.MethodPost, "/api/sicil/grid", attackerToken, gridBody)
	require.Equal(t, http.StatusForbidden, rec.Code, rec.Body.String())
	assert.Contains(t, rec.Body.String(), "ERR_FORBIDDEN")
}

func TestSicil_Update_OrganizationIDImmutable(t *testing.T) {
	app := newTestApp(t)
	u := app.seedUser(t, "u2@x.com", "pw", models.RoleUser)
	orgA := app.seedOrgWithUser(t, u.ID, "ua-org-a")
	// A second org the user cannot access
	orgRepo := repository.NewOrganizationRepository(testDB)
	other := &models.Organization{Name: "Other", Slug: "u2-other"}
	require.NoError(t, orgRepo.Create(other))
	token := app.login(t, "u2@x.com", "pw")

	createBody := `{"organization_id":` + strconv.FormatUint(uint64(orgA), 10) +
		`,"hesap_no":"001","hesap_adi":"Card","sicil_tipi":"FIRMA"}`
	rec := app.do(t, http.MethodPost, "/api/sicil", token, createBody)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())

	// The attacker tries to change organization_id; the request looks successful but the org does not change.
	cardID := extractID(t, rec.Body.Bytes())
	updBody := `{"organization_id":` + strconv.FormatUint(uint64(other.ID), 10) + `,"hesap_adi":"Renamed"}`
	rec = app.do(t, http.MethodPut, "/api/sicil/"+strconv.FormatUint(uint64(cardID), 10), token, updBody)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())

	rec = app.do(t, http.MethodGet, "/api/sicil/"+strconv.FormatUint(uint64(cardID), 10), token, "")
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Contains(t, rec.Body.String(), `"organization_id":`+strconv.FormatUint(uint64(orgA), 10))
	assert.NotContains(t, rec.Body.String(), `"organization_id":`+strconv.FormatUint(uint64(other.ID), 10))
}

func TestSicil_Delete_AsMemberAndForbiddenOtherwise(t *testing.T) {
	app := newTestApp(t)
	owner := app.seedUser(t, "o3@x.com", "pw", models.RoleUser)
	orgID := app.seedOrgWithUser(t, owner.ID, "o3-org")
	ownerToken := app.login(t, "o3@x.com", "pw")

	createBody := `{"organization_id":` + strconv.FormatUint(uint64(orgID), 10) + `,"hesap_no":"D1","hesap_adi":"DelMe","sicil_tipi":"MUSTERI"}`
	rec := app.do(t, http.MethodPost, "/api/sicil", ownerToken, createBody)
	require.Equal(t, http.StatusCreated, rec.Code)
	cardID := extractID(t, rec.Body.Bytes())

	// A foreign user tries to delete → 403
	stranger := app.seedUser(t, "s3@x.com", "pw", models.RoleUser)
	_ = app.seedOrgWithUser(t, stranger.ID, "s3-org")
	strangerToken := app.login(t, "s3@x.com", "pw")
	rec = app.do(t, http.MethodDelete, "/api/sicil/"+strconv.FormatUint(uint64(cardID), 10), strangerToken, "")
	assert.Equal(t, http.StatusForbidden, rec.Code)

	// Sahip siler → 200
	rec = app.do(t, http.MethodDelete, "/api/sicil/"+strconv.FormatUint(uint64(cardID), 10), ownerToken, "")
	assert.Equal(t, http.StatusOK, rec.Code)
}

func TestSicil_CreateWithTelefonlar_AndUpdateReplacesThem(t *testing.T) {
	app := newTestApp(t)
	u := app.seedUser(t, "tel@x.com", "pw", models.RoleUser)
	orgID := app.seedOrgWithUser(t, u.ID, "tel-org")
	token := app.login(t, "tel@x.com", "pw")

	createBody := `{"organization_id":` + strconv.FormatUint(uint64(orgID), 10) +
		`,"hesap_no":"T1","hesap_adi":"WithPhones","sicil_tipi":"FIRMA",` +
		`"telefonlar":[{"tipi":"CEP","numara":"+90 555 111 22 33"},{"tipi":"IS","numara":"+90 212 444 55 66"}]}`
	rec := app.do(t, http.MethodPost, "/api/sicil", token, createBody)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	cardID := extractID(t, rec.Body.Bytes())
	assert.Contains(t, rec.Body.String(), "+90 555 111 22 33")
	assert.Contains(t, rec.Body.String(), `"CEP"`)
	assert.Contains(t, rec.Body.String(), `"IS"`)

	updBody := `{"telefonlar":[{"tipi":"EV","numara":"+90 312 000 00 00"}]}`
	rec = app.do(t, http.MethodPut, "/api/sicil/"+strconv.FormatUint(uint64(cardID), 10), token, updBody)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())

	rec = app.do(t, http.MethodGet, "/api/sicil/"+strconv.FormatUint(uint64(cardID), 10), token, "")
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Contains(t, rec.Body.String(), "+90 312 000 00 00")
	assert.Contains(t, rec.Body.String(), `"EV"`)
	assert.NotContains(t, rec.Body.String(), "+90 555 111 22 33", "the old phone must have been removed")
}

func TestSicil_RejectsInvalidTelefonTipi(t *testing.T) {
	app := newTestApp(t)
	u := app.seedUser(t, "tel2@x.com", "pw", models.RoleUser)
	orgID := app.seedOrgWithUser(t, u.ID, "tel2-org")
	token := app.login(t, "tel2@x.com", "pw")

	body := `{"organization_id":` + strconv.FormatUint(uint64(orgID), 10) +
		`,"hesap_no":"T1","hesap_adi":"X","sicil_tipi":"FIRMA",` +
		`"telefonlar":[{"tipi":"XYZ","numara":"+90"}]}`
	rec := app.do(t, http.MethodPost, "/api/sicil", token, body)
	require.Equal(t, http.StatusBadRequest, rec.Code, rec.Body.String())
	assert.Contains(t, rec.Body.String(), "ERR_INVALID_TELEFON_TIPI")
}
