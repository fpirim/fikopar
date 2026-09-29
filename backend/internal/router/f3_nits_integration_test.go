//go:build integration

package router_test

import (
	"net/http"
	"strconv"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestF3_CancelReversalDescription(t *testing.T) {
	f := newDocs(t)
	p := f.posted(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "10", "")))
	require.NotNil(t, p.JournalEntryID)
	c := f.cancel(t, p)
	require.NotNil(t, c.ReversalEntryID)
	orig := f.getEntry(t, *p.JournalEntryID)
	rev := f.getEntry(t, *c.ReversalEntryID)
	assert.Equal(t, "Reversal of "+orig.EntryNo+": wrong customer", rev.Description)
}

type sicilDiscounts struct {
	ID       uint    `json:"id"`
	Version  int     `json:"version"`
	Iskonto  float64 `json:"iskonto"`
	Iskonto2 float64 `json:"iskonto2"`
}

func TestF3_SicilIskonto2IsWritableAndTheLineDefault(t *testing.T) {
	f := newDocs(t)
	rec := f.do(t, http.MethodPost, f.path("/sicils"), f.token,
		`{"hesap_no":"C2","hesap_adi":"Card C2","sicil_tipi":"MUSTERI","iskonto":10,"iskonto2":2.5}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	s := decode[sicilDiscounts](t, rec)
	assert.InDelta(t, 2.5, s.Iskonto2, 0)
	got := decode[sicilDiscounts](t, f.do(t, http.MethodGet, f.path("/sicils/"+idStr(s.ID)), f.token, ""))
	assert.InDelta(t, 2.5, got.Iskonto2, 0)

	match := func(v int) map[string]string { return map[string]string{"If-Match": `"` + strconv.Itoa(v) + `"`} }
	rec = f.req(t, http.MethodPut, f.path("/sicils/"+idStr(s.ID)), f.token, `{"iskonto2":7}`, match(got.Version))
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	u := decode[sicilDiscounts](t, rec)
	assert.InDelta(t, 7, u.Iskonto2, 0)
	assert.InDelta(t, 10, u.Iskonto, 0, "a partial update keeps the first discount")

	for _, body := range []string{`{"iskonto2":100.5}`, `{"iskonto2":-1}`} {
		rec = f.req(t, http.MethodPut, f.path("/sicils/"+idStr(s.ID)), f.token, body, match(u.Version))
		assert.Equal(t, http.StatusBadRequest, rec.Code, body)
		assert.Equal(t, "ERR_INVALID_ISKONTO", errCode(t, rec), body)
	}
	rec = f.do(t, http.MethodPost, f.path("/sicils"), f.token, `{"hesap_no":"C3","hesap_adi":"Card C3","iskonto2":101}`)
	assert.Equal(t, http.StatusBadRequest, rec.Code)
	assert.Equal(t, "ERR_INVALID_ISKONTO", errCode(t, rec))

	d := f.create(t, docBody("SALES_INVOICE", f.depot, s.ID, "2026-03-01", "", lineJSON(f.p1, "1", "10", "")))
	require.Len(t, d.Lines, 1)
	assert.Equal(t, "7", d.Lines[0].Discount2Pct)
}
