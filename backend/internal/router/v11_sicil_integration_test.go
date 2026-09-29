//go:build integration

package router_test

import (
	"net/http"
	"sync"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type sicilV11 struct {
	ID          uint     `json:"id"`
	HesapAdi    string   `json:"hesap_adi"`
	Version     int      `json:"version"`
	CreditLimit *string  `json:"credit_limit"`
	Emails      []string `json:"emails"`
	Contacts    []struct {
		Name  string `json:"name"`
		Role  string `json:"role"`
		Phone string `json:"phone"`
		Email string `json:"email"`
	} `json:"contacts"`
}

func TestV11_SicilVersionContactsEmailsAndCreditLimit(t *testing.T) {
	f := newLedger(t)
	rec := f.do(t, http.MethodPost, f.path("/sicils"), f.token, `{"hesap_no":"K1","hesap_adi":"Kaya Oto","sicil_tipi":"MUSTERI",
		"credit_limit":"50000.00","emails":["info@kaya.com"],"contacts":[{"name":"Ali Kaya","role":"Owner","phone":"+90 555","email":"ali@kaya.com"}]}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	assert.Equal(t, `"1"`, rec.Header().Get("ETag"))
	s := decode[sicilV11](t, rec)
	assert.Equal(t, 1, s.Version)
	require.NotNil(t, s.CreditLimit)
	assert.Equal(t, "50000.00", *s.CreditLimit)
	assert.Equal(t, []string{"info@kaya.com"}, s.Emails)
	require.Len(t, s.Contacts, 1)
	assert.Equal(t, "Ali Kaya", s.Contacts[0].Name)

	path := f.path("/sicils/" + idStr(s.ID))
	rec = f.do(t, http.MethodGet, path, f.token, "")
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Equal(t, `"1"`, rec.Header().Get("ETag"))
	assert.Equal(t, "ali@kaya.com", decode[sicilV11](t, rec).Contacts[0].Email)

	put := func(etag, body string) *httpRec {
		h := map[string]string{}
		if etag != "" {
			h["If-Match"] = etag
		}
		r := f.req(t, http.MethodPut, path, f.token, body, h)
		return &httpRec{r.Code, r.Header().Get("ETag"), r.Body.Bytes()}
	}
	body := `{"hesap_adi":"Kaya Otomotiv","credit_limit":null,"emails":["a@kaya.com","b@kaya.com"],"contacts":[]}`
	assert.Equal(t, http.StatusPreconditionRequired, put("", body).code)
	assert.Equal(t, http.StatusPreconditionFailed, put(`"7"`, body).code)
	r := put(`"1"`, body)
	require.Equal(t, http.StatusOK, r.code, string(r.body))
	assert.Equal(t, `"2"`, r.etag)
	s = decodeBytes[sicilV11](t, r.body)
	assert.Equal(t, 2, s.Version)
	assert.Equal(t, "Kaya Otomotiv", s.HesapAdi)
	assert.Nil(t, s.CreditLimit)
	assert.Equal(t, []string{"a@kaya.com", "b@kaya.com"}, s.Emails)
	assert.Empty(t, s.Contacts)
	assert.Equal(t, http.StatusPreconditionFailed, put(`"1"`, `{"hesap_adi":"stale"}`).code)

	for field, bad := range map[string]string{
		"credit_limit": `{"credit_limit":"-5.00"}`,
		"emails":       `{"emails":["not-an-email"]}`,
		"contacts":     `{"contacts":[{"role":"no name"}]}`,
	} {
		rec := f.req(t, http.MethodPut, path, f.token, bad, map[string]string{"If-Match": `"2"`})
		assert.Equal(t, http.StatusBadRequest, rec.Code, field)
		assert.Equal(t, field, decode[apiErr](t, rec).Field)
	}
	assert.Equal(t, http.StatusNotFound, f.req(t, http.MethodPut, f.path("/sicils/999999"), f.token, `{}`, map[string]string{"If-Match": `"1"`}).Code)

	codes := make([]int, 6)
	var wg sync.WaitGroup
	for i := range codes {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			codes[i] = f.req(t, http.MethodPut, path, f.token, `{"sehir":"Adana"}`, map[string]string{"If-Match": `"2"`}).Code
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

type httpRec struct {
	code int
	etag string
	body []byte
}
