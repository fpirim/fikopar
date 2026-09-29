//go:build integration

package router_test

import (
	"net/http"
	"sync"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/models"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type depotJSON struct {
	ID                 uint   `json:"id"`
	Code               string `json:"code"`
	Name               string `json:"name"`
	VatRate            string `json:"vatRate"`
	AllowNegativeStock bool   `json:"allowNegativeStock"`
	Version            int    `json:"version"`
}

func TestDepots_CreateAndListArePerOrg(t *testing.T) {
	app, admin := adminApp(t)
	u := app.seedUser(t, "d@x.com", "pw", models.RoleUser)
	orgA := app.seedOrg(t, "dep-a", u.ID)
	orgB := app.seedOrg(t, "dep-b", u.ID)
	token := app.login(t, "d@x.com", "pw")

	rec := app.post(t, orgPath(orgA, "/depots"), admin, `{"code":"1","name":"Merkez","vatRate":"20"}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	d := decode[depotJSON](t, rec)
	assert.Equal(t, "1", d.Code)
	assert.Equal(t, "20", d.VatRate)
	assert.Equal(t, 1, d.Version)

	listA := decode[[]depotJSON](t, app.do(t, http.MethodGet, orgPath(orgA, "/depots"), token, ""))
	listB := decode[[]depotJSON](t, app.do(t, http.MethodGet, orgPath(orgB, "/depots"), token, ""))
	assert.Len(t, listA, 1)
	assert.Empty(t, listB)

	// Same code in another org is fine; twice in one org is a conflict.
	require.Equal(t, http.StatusCreated, app.post(t, orgPath(orgB, "/depots"), admin, `{"code":"1","name":"Merkez"}`).Code)
	assert.Equal(t, http.StatusConflict, app.post(t, orgPath(orgA, "/depots"), admin, `{"code":"1","name":"Again"}`).Code)
}

func TestDepots_CreateRequiresAdmin(t *testing.T) {
	app := newTestApp(t)
	u := app.seedUser(t, "d2@x.com", "pw", models.RoleUser)
	org := app.seedOrg(t, "dep-user", u.ID)
	token := app.login(t, "d2@x.com", "pw")
	rec := app.post(t, orgPath(org, "/depots"), token, `{"code":"1","name":"x"}`)
	assert.Equal(t, http.StatusForbidden, rec.Code)
}

func TestDepots_CreateValidatesCode(t *testing.T) {
	app, admin := adminApp(t)
	org := app.seedOrg(t, "dep-val")
	rec := app.post(t, orgPath(org, "/depots"), admin, `{"code":"abc","name":"x"}`)
	assert.Equal(t, http.StatusBadRequest, rec.Code)
}

func TestIdempotency_KeyRequiredAndValidated(t *testing.T) {
	app, admin := adminApp(t)
	org := app.seedOrg(t, "idem-req")

	rec := app.req(t, http.MethodPost, orgPath(org, "/depots"), admin, `{"code":"1","name":"x"}`, nil)
	assert.Equal(t, http.StatusBadRequest, rec.Code)
	assert.Equal(t, "ERR_IDEMPOTENCY_KEY_REQUIRED", errCode(t, rec))

	rec = app.req(t, http.MethodPost, orgPath(org, "/depots"), admin, `{"code":"1","name":"x"}`, map[string]string{"Idempotency-Key": "not-a-uuid"})
	assert.Equal(t, http.StatusBadRequest, rec.Code)
}

func TestIdempotency_ReplayReusedAndInProgress(t *testing.T) {
	app, admin := adminApp(t)
	org := app.seedOrg(t, "idem")
	key := map[string]string{"Idempotency-Key": uuid.NewString()}
	body := `{"code":"1","name":"Merkez"}`

	first := app.req(t, http.MethodPost, orgPath(org, "/depots"), admin, body, key)
	require.Equal(t, http.StatusCreated, first.Code, first.Body.String())
	assert.Empty(t, first.Header().Get("Idempotent-Replayed"))

	again := app.req(t, http.MethodPost, orgPath(org, "/depots"), admin, body, key)
	require.Equal(t, http.StatusCreated, again.Code, again.Body.String())
	assert.Equal(t, "true", again.Header().Get("Idempotent-Replayed"))
	assert.Equal(t, decode[depotJSON](t, first).ID, decode[depotJSON](t, again).ID)

	var n int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM depots WHERE org_id = ?`, org).Scan(&n).Error)
	assert.Equal(t, int64(1), n)

	reused := app.req(t, http.MethodPost, orgPath(org, "/depots"), admin, `{"code":"2","name":"Other"}`, key)
	assert.Equal(t, http.StatusUnprocessableEntity, reused.Code)
	assert.Equal(t, "ERR_IDEMPOTENCY_KEY_REUSED", errCode(t, reused))

	pending := uuid.NewString()
	require.NoError(t, testDB.Exec(`INSERT INTO idempotency_keys (org_id, key, endpoint, request_hash) VALUES (?, ?, 'x', '\x00')`, org, pending).Error)
	rec := app.req(t, http.MethodPost, orgPath(org, "/depots"), admin, body, map[string]string{"Idempotency-Key": pending})
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_IDEMPOTENCY_IN_PROGRESS", errCode(t, rec))
}

// The same key in two orgs is two different requests.
func TestIdempotency_KeyIsScopedPerOrg(t *testing.T) {
	app, admin := adminApp(t)
	orgA := app.seedOrg(t, "idem-a")
	orgB := app.seedOrg(t, "idem-b")
	key := map[string]string{"Idempotency-Key": uuid.NewString()}
	a := app.req(t, http.MethodPost, orgPath(orgA, "/depots"), admin, `{"code":"1","name":"x"}`, key)
	b := app.req(t, http.MethodPost, orgPath(orgB, "/depots"), admin, `{"code":"1","name":"x"}`, key)
	require.Equal(t, http.StatusCreated, a.Code)
	require.Equal(t, http.StatusCreated, b.Code)
	assert.Empty(t, b.Header().Get("Idempotent-Replayed"))
}

// A failed attempt releases its key so the client can retry with the same key.
func TestIdempotency_FailedAttemptCanBeRetried(t *testing.T) {
	app, admin := adminApp(t)
	org := app.seedOrg(t, "idem-retry")
	key := map[string]string{"Idempotency-Key": uuid.NewString()}
	rec := app.req(t, http.MethodPost, orgPath(org, "/depots"), admin, `{"code":"bad!","name":"x"}`, key)
	require.Equal(t, http.StatusBadRequest, rec.Code)
	rec = app.req(t, http.MethodPost, orgPath(org, "/depots"), admin, `{"code":"bad!","name":"x"}`, key)
	assert.Equal(t, http.StatusBadRequest, rec.Code)
}

func TestIdempotency_ConcurrentDuplicatesCreateOnce(t *testing.T) {
	app, admin := adminApp(t)
	org := app.seedOrg(t, "idem-conc")
	key := map[string]string{"Idempotency-Key": uuid.NewString()}
	var wg sync.WaitGroup
	codes := make([]int, 8)
	for i := range codes {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			codes[i] = app.req(t, http.MethodPost, orgPath(org, "/depots"), admin, `{"code":"1","name":"x"}`, key).Code
		}(i)
	}
	wg.Wait()
	for _, c := range codes {
		assert.Contains(t, []int{http.StatusCreated, http.StatusConflict}, c)
	}
	var n int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM depots WHERE org_id = ?`, org).Scan(&n).Error)
	assert.Equal(t, int64(1), n)
}
