//go:build integration

package router_test

import (
	"net/http"
	"sync"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type accountJSON struct {
	ID         uint   `json:"id"`
	OrgID      uint   `json:"orgId"`
	Code       string `json:"code"`
	Name       string `json:"name"`
	Type       string `json:"type"`
	NormalSide string `json:"normalSide"`
	SicilID    *uint  `json:"sicilId"`
	Currency   string `json:"currency"`
	IsActive   bool   `json:"isActive"`
	Version    int    `json:"version"`
}

func TestAccounts_CreateGetAndETag(t *testing.T) {
	f := newLedger(t)
	id := f.account(t, "600", "INCOME")

	rec := f.do(t, http.MethodGet, f.path("/ledger/accounts/"+idStr(id)), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code)
	a := decode[accountJSON](t, rec)
	assert.Equal(t, "600", a.Code)
	assert.Equal(t, "CREDIT", a.NormalSide)
	assert.Equal(t, "TRY", a.Currency)
	assert.True(t, a.IsActive)
	assert.Equal(t, `"1"`, rec.Header().Get("ETag"))
}

func TestAccounts_UpdateRequiresCurrentVersion(t *testing.T) {
	f := newLedger(t)
	id := f.account(t, "770", "EXPENSE")
	path := f.path("/ledger/accounts/" + idStr(id))

	rec := f.req(t, http.MethodPatch, path, f.token, `{"name":"Renamed"}`, nil)
	assert.Equal(t, http.StatusPreconditionRequired, rec.Code)
	assert.Equal(t, "ERR_PRECONDITION_REQUIRED", errCode(t, rec))

	rec = f.req(t, http.MethodPatch, path, f.token, `{"name":"Renamed"}`, map[string]string{"If-Match": `"7"`})
	assert.Equal(t, http.StatusPreconditionFailed, rec.Code)
	assert.Equal(t, "ERR_VERSION_CONFLICT", errCode(t, rec))

	rec = f.req(t, http.MethodPatch, path, f.token, `{"name":"Renamed","code":"999","isActive":false}`, map[string]string{"If-Match": `"1"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	a := decode[accountJSON](t, rec)
	assert.Equal(t, "Renamed", a.Name)
	assert.Equal(t, "770", a.Code, "code is immutable")
	assert.False(t, a.IsActive)
	assert.Equal(t, 2, a.Version)
	assert.Equal(t, `"2"`, rec.Header().Get("ETag"))
}

// Two clerks save the same version at once: exactly one wins, the other gets 412.
func TestAccounts_ConcurrentUpdatesConflict(t *testing.T) {
	f := newLedger(t)
	id := f.account(t, "771", "EXPENSE")
	path := f.path("/ledger/accounts/" + idStr(id))
	codes := make([]int, 6)
	var wg sync.WaitGroup
	for i := range codes {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			codes[i] = f.req(t, http.MethodPatch, path, f.token, `{"name":"n`+idStr(uint(i))+`"}`, map[string]string{"If-Match": `"1"`}).Code
		}(i)
	}
	wg.Wait()
	won := 0
	for _, c := range codes {
		if c == http.StatusOK {
			won++
		} else {
			assert.Equal(t, http.StatusPreconditionFailed, c)
		}
	}
	assert.Equal(t, 1, won)
}

func TestAccounts_Validation(t *testing.T) {
	f := newLedger(t)
	f.account(t, "100", "ASSET")
	rec := f.post(t, f.path("/ledger/accounts"), f.token, `{"code":"100","name":"dup","type":"ASSET"}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_ACCOUNT_CODE_EXISTS", errCode(t, rec))

	for _, body := range []string{
		`{"code":"1x","name":"a","type":"ASSET"}`,
		`{"code":"101","name":"a","type":"BOGUS"}`,
		`{"code":"102","name":"","type":"ASSET"}`,
		`{"code":"103","name":"a","type":"ASSET","currency":"try"}`,
	} {
		rec := f.post(t, f.path("/ledger/accounts"), f.token, body)
		assert.Equal(t, http.StatusBadRequest, rec.Code, body)
	}
}

func TestAccounts_SicilMustBelongToOrg(t *testing.T) {
	f := newLedger(t)
	other := f.seedOrg(t, "acc-other", f.userID)
	rec := f.do(t, http.MethodPost, orgPath(other, "/sicils"), f.token, `{"hesap_no":"X","hesap_adi":"Foreign"}`)
	require.Equal(t, http.StatusCreated, rec.Code)
	foreign := extractID(t, rec.Body.Bytes())

	rec = f.post(t, f.path("/ledger/accounts"), f.token, `{"code":"120.X","name":"x","type":"ASSET","sicilId":`+idStr(foreign)+`}`)
	assert.Equal(t, http.StatusNotFound, rec.Code)
	assert.Equal(t, "ERR_SICIL_NOT_FOUND", errCode(t, rec))

	own := f.sicil(t, "C1", "MUSTERI")
	rec = f.post(t, f.path("/ledger/accounts"), f.token, `{"code":"120.C1","name":"x","type":"ASSET","sicilId":`+idStr(own)+`}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	assert.Equal(t, own, *decode[accountJSON](t, rec).SicilID)
}

func TestAccounts_ListFiltersAndKeysetPaging(t *testing.T) {
	f := newLedger(t)
	for _, c := range []string{"100", "102", "120", "320", "600"} {
		typ := "ASSET"
		if c == "320" {
			typ = "LIABILITY"
		}
		if c == "600" {
			typ = "INCOME"
		}
		f.account(t, c, typ)
	}
	type page struct {
		Items      []accountJSON `json:"items"`
		NextCursor *string       `json:"nextCursor"`
	}
	var codes []string
	path := f.path("/ledger/accounts?limit=2")
	for i := 0; i < 5; i++ {
		p := decode[page](t, f.do(t, http.MethodGet, path, f.token, ""))
		for _, a := range p.Items {
			codes = append(codes, a.Code)
		}
		if p.NextCursor == nil {
			break
		}
		path = f.path("/ledger/accounts?limit=2&cursor=" + *p.NextCursor)
	}
	assert.Equal(t, []string{"100", "102", "120", "320", "600"}, codes)

	p := decode[page](t, f.do(t, http.MethodGet, f.path("/ledger/accounts?type=INCOME"), f.token, ""))
	require.Len(t, p.Items, 1)
	assert.Equal(t, "600", p.Items[0].Code)
	assert.Nil(t, p.NextCursor)
}

func TestAccounts_ForeignAccountIsNotFound(t *testing.T) {
	f := newLedger(t)
	id := f.account(t, "100", "ASSET")
	other := f.seedOrg(t, "acc-foreign", f.userID)
	rec := f.do(t, http.MethodGet, orgPath(other, "/ledger/accounts/"+idStr(id)), f.token, "")
	assert.Equal(t, http.StatusNotFound, rec.Code)
	assert.Equal(t, "ERR_ACCOUNT_NOT_FOUND", errCode(t, rec))
}

func TestAccountRoles_SetAndList(t *testing.T) {
	f := newLedger(t)
	cash := f.account(t, "100", "ASSET")
	cash2 := f.account(t, "100.02", "ASSET")
	opening := f.account(t, "500", "EQUITY")

	body := `[{"role":"CASH","accountId":` + idStr(cash) + `},{"role":"CASH","depotId":` + idStr(f.depot) + `,"accountId":` + idStr(cash2) + `},{"role":"OPENING","accountId":` + idStr(opening) + `}]`
	rec := f.req(t, http.MethodPut, f.path("/ledger/account-roles"), f.token, body, nil)
	assert.Equal(t, http.StatusForbidden, rec.Code, "admin only")
	f.setRoles(t, body)

	type role struct {
		Role      string `json:"role"`
		DepotID   *uint  `json:"depotId"`
		AccountID uint   `json:"accountId"`
	}
	roles := decode[[]role](t, f.do(t, http.MethodGet, f.path("/ledger/account-roles"), f.token, ""))
	assert.Len(t, roles, 3)

	f.setRoles(t, `[{"role":"CASH","accountId":`+idStr(cash)+`}]`)
	roles = decode[[]role](t, f.do(t, http.MethodGet, f.path("/ledger/account-roles"), f.token, ""))
	assert.Len(t, roles, 1, "PUT replaces the whole map")

	for _, bad := range []string{
		`[{"role":"NOPE","accountId":` + idStr(cash) + `}]`,
		`[{"role":"CASH","accountId":999999}]`,
		`[{"role":"CASH","depotId":999999,"accountId":` + idStr(cash) + `}]`,
		`[{"role":"CASH","accountId":` + idStr(cash) + `},{"role":"CASH","accountId":` + idStr(cash2) + `}]`,
	} {
		rec := f.req(t, http.MethodPut, f.path("/ledger/account-roles"), f.admin, bad, nil)
		assert.Equal(t, http.StatusUnprocessableEntity, rec.Code, bad)
	}
}

type seriesJSON struct {
	ID        uint   `json:"id"`
	DepotID   uint   `json:"depotId"`
	Year      int    `json:"year"`
	Prefix    string `json:"prefix"`
	Width     int    `json:"width"`
	NextValue int64  `json:"nextValue"`
	Version   int    `json:"version"`
}

func TestNumberSeries_ListAndUpdate(t *testing.T) {
	f := newLedger(t)
	depot2 := f.newDepot(t, "2")
	for _, args := range [][]any{{f.depot, "TAH", 2026}, {f.depot, "TAH", 2026}, {depot2, "TED", 2026}, {f.depot, "TAH", 2025}} {
		require.NoError(t, testDB.Exec(`SELECT next_number(?, ?, ?, ?)`, f.org, args[0], args[1], args[2]).Error)
	}

	all := decode[[]seriesJSON](t, f.do(t, http.MethodGet, f.path("/number-series"), f.token, ""))
	assert.Len(t, all, 3)
	filtered := decode[[]seriesJSON](t, f.do(t, http.MethodGet, f.path("/number-series?year=2026&prefix=TAH&depotId="+idStr(f.depot)), f.token, ""))
	require.Len(t, filtered, 1)
	s := filtered[0]
	assert.Equal(t, int64(3), s.NextValue)
	assert.Equal(t, 6, s.Width)

	path := f.path("/number-series/" + idStr(s.ID))
	v := map[string]string{"If-Match": `"` + idStr(uint(s.Version)) + `"`}
	assert.Equal(t, http.StatusForbidden, f.req(t, http.MethodPatch, path, f.token, `{"width":8}`, v).Code)
	assert.Equal(t, http.StatusPreconditionRequired, f.req(t, http.MethodPatch, path, f.admin, `{"width":8}`, nil).Code)

	rec := f.req(t, http.MethodPatch, path, f.admin, `{"nextValue":2}`, v)
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_NUMBER_SERIES_BACKWARDS", errCode(t, rec))
	assert.Equal(t, http.StatusBadRequest, f.req(t, http.MethodPatch, path, f.admin, `{"width":3}`, v).Code)

	rec = f.req(t, http.MethodPatch, path, f.admin, `{"width":8,"nextValue":100}`, v)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	got := decode[seriesJSON](t, rec)
	assert.Equal(t, int64(100), got.NextValue)
	assert.Equal(t, 8, got.Width)

	assert.Equal(t, http.StatusPreconditionFailed, f.req(t, http.MethodPatch, path, f.admin, `{"width":9}`, v).Code)

	var no string
	require.NoError(t, testDB.Raw(`SELECT next_number(?, ?, 'TAH', 2026)`, f.org, f.depot).Scan(&no).Error)
	assert.Equal(t, "TAH-2026-1-00000100", no)
}
