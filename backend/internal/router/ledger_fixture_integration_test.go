//go:build integration

package router_test

import (
	"net/http"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/models"

	"github.com/stretchr/testify/require"
)

type ledgerFix struct {
	*testApp
	admin, token string
	userID       uint
	org, depot   uint
}

func newLedger(t *testing.T) *ledgerFix {
	t.Helper()
	app, admin := adminApp(t)
	u := app.seedUser(t, "clerk@x.com", "pw", models.RoleUser)
	org := app.seedOrg(t, "ledger-org", u.ID)
	f := &ledgerFix{testApp: app, admin: admin, token: app.login(t, "clerk@x.com", "pw"), userID: u.ID, org: org}
	f.depot = f.newDepot(t, "1")
	return f
}

func (f *ledgerFix) path(rest string) string { return orgPath(f.org, rest) }

func (f *ledgerFix) newDepot(t *testing.T, code string) uint {
	t.Helper()
	rec := f.post(t, f.path("/depots"), f.admin, `{"code":"`+code+`","name":"Depot `+code+`"}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	return extractID(t, rec.Body.Bytes())
}

func (f *ledgerFix) account(t *testing.T, code, typ string) uint {
	t.Helper()
	rec := f.post(t, f.path("/ledger/accounts"), f.token, `{"code":"`+code+`","name":"Account `+code+`","type":"`+typ+`"}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	return extractID(t, rec.Body.Bytes())
}

func (f *ledgerFix) setRoles(t *testing.T, body string) {
	t.Helper()
	rec := f.req(t, http.MethodPut, f.path("/ledger/account-roles"), f.admin, body, nil)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
}

func (f *ledgerFix) sicil(t *testing.T, hesapNo, tipi string) uint {
	t.Helper()
	rec := f.do(t, http.MethodPost, f.path("/sicils"), f.token, `{"hesap_no":"`+hesapNo+`","hesap_adi":"Card `+hesapNo+`","sicil_tipi":"`+tipi+`","sehir":"Adana"}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	return extractID(t, rec.Body.Bytes())
}
