//go:build integration

package router_test

import (
	"net/http"
	"net/url"
	"strconv"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// Turkish letters fold to ASCII in codes and names: "ısı" is not "S", "salter" finds "Şalter".
func TestV13_SearchFoldsTurkishLetters(t *testing.T) {
	f := newStock(t)
	f.part(t, "S-1", "Şalter", "")
	f.part(t, "S-2", "Çığ mili", "")
	f.part(t, "ISI-9", "Termostat", "")
	f.part(t, "ŞLT-4", "Kablo", `,"codes":[{"kind":"OEM","code":"ğüç-77"}]`)
	f.part(t, "X-1", "Sensör", "")
	for q, want := range map[string][]string{
		"ısı":      {"ISI-9"},
		"ISI":      {"ISI-9"},
		"salter":   {"S-1"},
		"ŞALTER":   {"S-1"},
		"cig":      {"S-2"},
		"çığ":      {"S-2"},
		"CIG MİLİ": {"S-2"},
		"slt4":     {"ŞLT-4"},
		"guc77":    {"ŞLT-4"},
		"sensor":   {"X-1"},
	} {
		r := f.search(t, "q="+url.QueryEscape(q))
		assert.Equal(t, want, stockNos(r.Items), q)
	}
	rec := f.req(t, http.MethodPost, f.path("/catalog/parts/resolve"), f.token, `{"codes":["GUC-77","isi 9"]}`, nil)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Contains(t, rec.Body.String(), `"ŞLT-4"`, rec.Body.String())
	assert.Contains(t, rec.Body.String(), `"ISI-9"`, rec.Body.String())
}

// Two EQUIVALENT links to the same part are a mistake: 422, not a silent collapse.
func TestV13_DuplicateEquivalentLinkIsRejected(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "EQ-1", "Filter A", "")
	eq := `{"kind":"EQUIVALENT","code":"x","equivalentPartId":` + idStr(a) + `}`
	rec := f.post(t, f.path("/catalog/parts"), f.token,
		`{"catalogId":`+idStr(f.catalog)+`,"stockNo":"B-1","name":"B","codes":[`+eq+`,{"kind":"OEM","code":"o1"},`+eq+`]}`)
	require.Equal(t, http.StatusUnprocessableEntity, rec.Code, rec.Body.String())
	assert.JSONEq(t, `{"code":"ERR_DUPLICATE_LINE","field":"codes[2].equivalentPartId"}`, rec.Body.String())

	b := f.part(t, "B-2", "B", `,"codes":[`+eq+`]`)
	rec = f.do(t, http.MethodGet, f.path("/catalog/parts/"+idStr(b)), f.token, "")
	v := decode[partV13](t, rec).Version
	rec = f.req(t, http.MethodPut, f.path("/catalog/parts/"+idStr(b)), f.token,
		`{"catalogId":`+idStr(f.catalog)+`,"stockNo":"B-2","name":"B","codes":[`+eq+`,`+eq+`]}`,
		map[string]string{"If-Match": `"` + strconv.Itoa(v) + `"`})
	require.Equal(t, http.StatusUnprocessableEntity, rec.Code, rec.Body.String())
	assert.Contains(t, rec.Body.String(), `"codes[1].equivalentPartId"`)
}

// LIKE wildcards in q are literal in every branch of the global search.
func TestV13_GlobalSearchAccountsTakeWildcardsLiterally(t *testing.T) {
	f := newStock(t)
	pct := f.sicil(t, "Y%1", "MUSTERI")
	f.sicil(t, "N1", "MUSTERI")
	f.sicil(t, "N2", "MUSTERI")
	accounts := func(q string) []uint {
		rec := f.do(t, http.MethodGet, f.path("/search?q="+url.QueryEscape(q)), f.token, "")
		require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
		out := []uint{}
		for _, a := range decode[searchJSON](t, rec).Accounts {
			out = append(out, a.SicilID)
		}
		return out
	}
	assert.Empty(t, accounts("%%"))
	assert.Empty(t, accounts("_1"))
	assert.Empty(t, accounts(`\N`))
	assert.Equal(t, []uint{pct}, accounts("y%"))
	assert.Equal(t, []uint{pct}, accounts("%1"))
}

// Two codes of one kind that fold to the same form are one code: 422, not a silent collapse.
func TestV13_CodesThatFoldAlikeAreRejected(t *testing.T) {
	f := newStock(t)
	rec := f.post(t, f.path("/catalog/parts"), f.token,
		`{"catalogId":`+idStr(f.catalog)+`,"stockNo":"C-1","name":"C","codes":[{"kind":"OEM","code":"ŞALTER-1"},`+
			`{"kind":"MANUFACTURER","code":"SALTER-1"},{"kind":"OEM","code":"salter 1"}]}`)
	require.Equal(t, http.StatusUnprocessableEntity, rec.Code, rec.Body.String())
	assert.JSONEq(t, `{"code":"ERR_DUPLICATE_LINE","field":"codes[2].code"}`, rec.Body.String())
	f.part(t, "C-2", "C", `,"codes":[{"kind":"OEM","code":"ŞALTER-1"},{"kind":"MANUFACTURER","code":"SALTER-1"}]`)
}
