package apierror

import (
	"errors"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/labstack/echo/v4"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func newCtx() (echo.Context, *httptest.ResponseRecorder) {
	e := echo.New()
	rec := httptest.NewRecorder()
	return e.NewContext(httptest.NewRequest(http.MethodGet, "/", nil), rec), rec
}

func TestAPIError_ErrorReturnsCode(t *testing.T) {
	assert.Equal(t, "ERR_INVALID_ID", InvalidID.Error())
}

func TestRespond_TypedAPIError(t *testing.T) {
	c, rec := newCtx()
	require.NoError(t, Respond(c, SlugExists))
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.JSONEq(t, `{"code":"ERR_SLUG_EXISTS"}`, rec.Body.String())
}

func TestRespond_UntypedErrorBecomesInternal(t *testing.T) {
	c, rec := newCtx()
	require.NoError(t, Respond(c, errors.New("boom")))
	assert.Equal(t, http.StatusInternalServerError, rec.Code)
	assert.JSONEq(t, `{"code":"ERR_INTERNAL"}`, rec.Body.String())
}

func TestRespond_DetailedCarriesFieldAndOwner(t *testing.T) {
	c, rec := newCtx()
	require.NoError(t, Respond(c, UnknownRef(SicilNotFound, "sicilId")))
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.JSONEq(t, `{"code":"ERR_SICIL_NOT_FOUND","field":"sicilId"}`, rec.Body.String())
	assert.Equal(t, http.StatusNotFound, SicilNotFound.Status, "the shared value is not changed")

	c, rec = newCtx()
	no := "TAH-2026-1-000001"
	require.NoError(t, Respond(c, WithOwner(EntryOwnedBySource, Owner{SourceType: "receipt", SourceID: "7", SourceNo: &no})))
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.JSONEq(t, `{"code":"ERR_ENTRY_OWNED_BY_SOURCE","owner":{"sourceType":"receipt","sourceId":"7","sourceNo":"TAH-2026-1-000001"}}`, rec.Body.String())
}
