//go:build integration

package router_test

import (
	"bytes"
	"mime/multipart"
	"net/http"
	"net/http/httptest"
	"strconv"
	"strings"
	"sync"
	"testing"

	"github.com/google/uuid"
	"github.com/labstack/echo/v4"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type importIssueJSON struct {
	Field string  `json:"field"`
	Code  string  `json:"code"`
	Value *string `json:"value"`
}

type importJSON struct {
	ID                  uint              `json:"id"`
	CatalogID           uint              `json:"catalogId"`
	CatalogCode         int               `json:"catalogCode"`
	FileName            string            `json:"fileName"`
	FileSha256          string            `json:"fileSha256"`
	FileSize            int               `json:"fileSize"`
	Format              string            `json:"format"`
	ValidFrom           string            `json:"validFrom"`
	Status              string            `json:"status"`
	FailureCode         *string           `json:"failureCode"`
	Headers             []string          `json:"headers"`
	Sample              [][]string        `json:"sample"`
	UnmappedTargets     []string          `json:"unmappedTargets"`
	Counts              map[string]int    `json:"counts"`
	CatalogVersion      *int64            `json:"catalogVersion"`
	DuplicateOfImportID *uint             `json:"duplicateOfImportId"`
	Result              map[string]int    `json:"result"`
	Mapping             map[string]any    `json:"mapping"`
	CreatedBy           *string           `json:"createdBy"`
	AppliedBy           *string           `json:"appliedBy"`
	Version             int               `json:"version"`
	Extra               map[string]string `json:"-"`
}

type importRowJSON struct {
	ID             uint              `json:"id"`
	RowNo          *int              `json:"rowNo"`
	PartID         *uint             `json:"partId"`
	StockNo        *string           `json:"stockNo"`
	Changes        []string          `json:"changes"`
	File           map[string]any    `json:"file"`
	Current        map[string]any    `json:"current"`
	ChangedFields  []string          `json:"changedFields"`
	PriceChangePct *string           `json:"priceChangePct"`
	Errors         []importIssueJSON `json:"errors"`
	Warnings       []importIssueJSON `json:"warnings"`
}

type importGrid struct {
	Rows            []importRowJSON `json:"rows"`
	TotalCount      int64           `json:"totalCount"`
	UnfilteredCount int64           `json:"unfilteredCount"`
}

func (f *stockFix) uploadWith(t *testing.T, key, name string, content []byte, fields map[string]string) *httptest.ResponseRecorder {
	t.Helper()
	var buf bytes.Buffer
	w := multipart.NewWriter(&buf)
	if name != "" {
		part, err := w.CreateFormFile("file", name)
		require.NoError(t, err)
		_, err = part.Write(content)
		require.NoError(t, err)
	}
	for k, v := range fields {
		require.NoError(t, w.WriteField(k, v))
	}
	require.NoError(t, w.Close())
	r := httptest.NewRequest(http.MethodPost, f.path("/catalog/imports"), bytes.NewReader(buf.Bytes()))
	r.Header.Set(echo.HeaderContentType, w.FormDataContentType())
	r.Header.Set(echo.HeaderAuthorization, "Bearer "+f.token)
	r.Header.Set("Idempotency-Key", key)
	rec := httptest.NewRecorder()
	f.e.ServeHTTP(rec, r)
	return rec
}

func (f *stockFix) upload(t *testing.T, name, content string, fields map[string]string) importJSON {
	t.Helper()
	if fields == nil {
		fields = map[string]string{}
	}
	if _, ok := fields["catalogId"]; !ok {
		fields["catalogId"] = idStr(f.catalog)
	}
	if _, ok := fields["validFrom"]; !ok {
		fields["validFrom"] = "2026-06-01"
	}
	rec := f.uploadWith(t, uuid.NewString(), name, []byte(content), fields)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	job := decode[importJSON](t, rec)
	assert.Equal(t, `"`+strconv.Itoa(job.Version)+`"`, rec.Header().Get("ETag"))
	return job
}

func (f *stockFix) importPath(id uint, rest string) string {
	return f.path("/catalog/imports/" + idStr(id) + rest)
}

func (f *stockFix) apply(t *testing.T, job importJSON, key, body string) *httptest.ResponseRecorder {
	t.Helper()
	return f.req(t, http.MethodPost, f.importPath(job.ID, "/apply"), f.token, body,
		map[string]string{"Idempotency-Key": key, "If-Match": `"` + strconv.Itoa(job.Version) + `"`})
}

func (f *stockFix) importRows(t *testing.T, id uint, body string) importGrid {
	t.Helper()
	rec := f.req(t, http.MethodPost, f.importPath(id, "/rows/grid"), f.token, body, nil)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[importGrid](t, rec)
}

func (f *stockFix) countRows(t *testing.T, sql string, args ...any) int64 {
	t.Helper()
	var n int64
	require.NoError(t, testDB.Raw(sql, args...).Scan(&n).Error)
	return n
}

func TestPriceImport_CSVPreviewApplyAndReplay(t *testing.T) {
	f := newStock(t)
	events := &recorder{}
	f.app.Relay.Register(events)
	a := f.part(t, "A-1", "Filtre", `,"brand":"X","initialPrice":{"price":"10","validFrom":"2026-01-01"}`)
	b := f.part(t, "B-1", "Kayış", `,"initialPrice":{"price":"5","validFrom":"2026-01-01"}`)
	c := f.part(t, "C-1", "Missing", "")
	f.relay(t)
	priceChanged := len(events.byType(t, "PriceChanged"))

	csv := "Stok Kodu;Açıklama;Fiyat;Marka\nA-1;Filtre;12;X\nB-1;Kayış yeni;5;\nD-1;Yeni parça;7,5;Y\n"
	job := f.upload(t, "liste.csv", csv, nil)
	assert.Equal(t, "READY", job.Status)
	assert.Equal(t, "CSV", job.Format)
	assert.Equal(t, 10, job.CatalogCode)
	assert.Len(t, job.FileSha256, 64)
	assert.Equal(t, "clerk", *job.CreatedBy)
	require.NotNil(t, job.CatalogVersion)
	assert.Equal(t, map[string]int{"rows": 3, "new": 1, "price": 1, "attributes": 1, "unchanged": 0, "missing": 1, "delete": 0,
		"errors": 0, "warnings": 0}, job.Counts)

	all := f.importRows(t, job.ID, `{"startRow":0,"endRow":50}`)
	require.Len(t, all.Rows, 4)
	assert.Equal(t, "A-1", *all.Rows[0].StockNo)
	assert.Equal(t, 2, *all.Rows[0].RowNo)
	assert.Nil(t, all.Rows[3].RowNo, "MISSING rows sort last")
	assert.Equal(t, []string{"MISSING"}, all.Rows[3].Changes)
	assert.Equal(t, c, *all.Rows[3].PartID)

	price := f.importRows(t, job.ID, `{"startRow":0,"endRow":50,"change":"PRICE"}`)
	require.Len(t, price.Rows, 1)
	assert.Equal(t, a, *price.Rows[0].PartID)
	assert.Equal(t, "20.0", *price.Rows[0].PriceChangePct)
	assert.Equal(t, "10", price.Rows[0].Current["price"])
	assert.Equal(t, "12", price.Rows[0].File["price"])
	assert.EqualValues(t, 4, price.UnfilteredCount)
	attrs := f.importRows(t, job.ID, `{"startRow":0,"endRow":50,"change":"ATTRIBUTES"}`)
	require.Len(t, attrs.Rows, 1)
	assert.Equal(t, []string{"description"}, attrs.Rows[0].ChangedFields)
	filtered := f.importRows(t, job.ID, `{"startRow":0,"endRow":50,"filterModel":{"price":{"filterType":"number","type":"greaterThan","filter":7}},
		"sortModel":[{"colId":"stockNo","sort":"desc"}]}`)
	require.Len(t, filtered.Rows, 2)
	assert.Equal(t, "D-1", *filtered.Rows[0].StockNo)

	key := uuid.NewString()
	rec := f.req(t, http.MethodPost, f.importPath(job.ID, "/apply"), f.token, `{"missing":"DEACTIVATE"}`, map[string]string{"Idempotency-Key": key})
	assert.Equal(t, http.StatusPreconditionRequired, rec.Code)
	rec = f.req(t, http.MethodPost, f.importPath(job.ID, "/apply"), f.token, `{"missing":"DEACTIVATE"}`,
		map[string]string{"Idempotency-Key": uuid.NewString(), "If-Match": `"99"`})
	assert.Equal(t, http.StatusPreconditionFailed, rec.Code)

	rec = f.apply(t, job, key, `{"missing":"DEACTIVATE"}`)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	applied := decode[importJSON](t, rec)
	assert.Equal(t, "APPLIED", applied.Status)
	assert.Equal(t, map[string]int{"partsCreated": 1, "pricesInserted": 2, "partsUpdated": 1, "partsDeactivated": 1, "rowsSkipped": 0}, applied.Result)
	assert.Equal(t, "clerk", *applied.AppliedBy)

	hist := decode[[]priceJSON](t, f.do(t, http.MethodGet, f.path("/catalog/parts/"+idStr(a)+"/prices"), f.token, ""))
	require.Len(t, hist, 2)
	assert.Equal(t, "12", *hist[0].Price)
	assert.Equal(t, "IMPORT", *hist[0].Source)
	bp := decode[partJSON](t, f.do(t, http.MethodGet, f.path("/catalog/parts/"+idStr(b)), f.token, ""))
	assert.Equal(t, "Kayış yeni", bp.Name)
	cp := decode[partJSON](t, f.do(t, http.MethodGet, f.path("/catalog/parts/"+idStr(c)), f.token, ""))
	assert.False(t, cp.IsActive)
	hits := decode[[]partJSON](t, f.do(t, http.MethodGet, f.path("/catalog/parts/lookup?code=D-1"), f.token, ""))
	require.Len(t, hits, 1)
	assert.Equal(t, "Yeni parça", hits[0].Name)
	assert.Equal(t, "Y", *hits[0].Brand)
	assert.EqualValues(t, 0, f.countRows(t, `SELECT count(*) FROM price_import_files WHERE import_id = ?`, job.ID))

	prices := f.countRows(t, `SELECT count(*) FROM prices`)
	rec = f.apply(t, job, key, `{"missing":"DEACTIVATE"}`)
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Equal(t, "true", rec.Header().Get("Idempotent-Replayed"))
	assert.Equal(t, "APPLIED", decode[importJSON](t, rec).Status)
	rec = f.apply(t, applied, uuid.NewString(), `{"missing":"DEACTIVATE"}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_INVALID_STATE_TRANSITION", errCode(t, rec))
	assert.Equal(t, prices, f.countRows(t, `SELECT count(*) FROM prices`))

	f.relay(t)
	imported := events.byType(t, "PriceListImported")
	require.Len(t, imported, 1)
	validateEvent(t, imported[0])
	assert.Len(t, events.byType(t, "PriceChanged"), priceChanged, "imports emit no PriceChanged")

	again := f.upload(t, "liste.csv", csv, nil)
	require.NotNil(t, again.DuplicateOfImportID)
	assert.Equal(t, job.ID, *again.DuplicateOfImportID)
}

func TestPriceImport_NeedsMappingThenPatch(t *testing.T) {
	f := newStock(t)
	job := f.upload(t, "x.csv", "Kod;Tutar\nA-1;4,5\nB-1;3\n", nil)
	assert.Equal(t, "NEEDS_MAPPING", job.Status)
	assert.Equal(t, []string{"price"}, job.UnmappedTargets)
	assert.Equal(t, []string{"Kod", "Tutar"}, job.Headers)
	assert.Equal(t, [][]string{{"A-1", "4,5"}, {"B-1", "3"}}, job.Sample)
	assert.Equal(t, 2, job.Counts["rows"])

	body := `{"mapping":{"columns":{"price":{"header":"Tutar"}}}}`
	path := f.importPath(job.ID, "")
	assert.Equal(t, http.StatusPreconditionRequired, f.req(t, http.MethodPatch, path, f.token, body, nil).Code)
	assert.Equal(t, http.StatusPreconditionFailed, f.req(t, http.MethodPatch, path, f.token, body, map[string]string{"If-Match": `"7"`}).Code)
	rec := f.req(t, http.MethodPatch, path, f.token, body, map[string]string{"If-Match": `"1"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	ready := decode[importJSON](t, rec)
	assert.Equal(t, "READY", ready.Status)
	assert.Equal(t, 2, ready.Version)
	assert.Equal(t, 2, ready.Counts["new"])
	assert.Empty(t, ready.UnmappedTargets)

	rec = f.req(t, http.MethodPatch, path, f.token, `{"validFrom":"2026-07-01"}`, map[string]string{"If-Match": `"2"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Equal(t, "2026-07-01", decode[importJSON](t, rec).ValidFrom)

	got := decode[importJSON](t, f.do(t, http.MethodGet, path, f.token, ""))
	assert.Equal(t, 3, got.Version)
	assert.Equal(t, http.StatusNotFound, f.do(t, http.MethodGet, f.importPath(999999, ""), f.token, "").Code)
}

func TestPriceImport_RowErrorsWarningsAndSkip(t *testing.T) {
	f := newStock(t)
	f.part(t, "E-1", "Exists", `,"initialPrice":{"price":"10","validFrom":"2026-06-01"}`)
	later := f.part(t, "L-1", "Later", `,"initialPrice":{"price":"10","validFrom":"2026-07-01"}`)
	csv := "code;price;validFrom\nN-1;1;\nN-1;2;\nX-1;abc;\nE-1;11;\nL-1;9;\n"
	job := f.upload(t, "e.csv", csv, nil)
	assert.Equal(t, "READY", job.Status)
	assert.Equal(t, 3, job.Counts["errors"])
	assert.Equal(t, 1, job.Counts["warnings"])

	errs := f.importRows(t, job.ID, `{"startRow":0,"endRow":50,"change":"ERROR"}`)
	require.Len(t, errs.Rows, 3)
	codes := map[string]string{}
	for _, r := range errs.Rows {
		codes[*r.StockNo+"/"+strconv.Itoa(*r.RowNo)] = r.Errors[0].Code
	}
	assert.Equal(t, map[string]string{"N-1/3": "DUPLICATE_IN_FILE", "X-1/4": "INVALID_NUMBER", "E-1/5": "PRICE_EXISTS"}, codes)
	warn := f.importRows(t, job.ID, `{"startRow":0,"endRow":50,"change":"WARNING"}`)
	require.Len(t, warn.Rows, 1)
	assert.Equal(t, "BACKDATED", warn.Rows[0].Warnings[0].Code)
	assert.Equal(t, later, *warn.Rows[0].PartID)

	rec := f.apply(t, job, uuid.NewString(), `{}`)
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_IMPORT_HAS_ERRORS", errCode(t, rec))
	rec = f.apply(t, job, uuid.NewString(), `{"skipErrorRows":true}`)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	res := decode[importJSON](t, rec).Result
	assert.Equal(t, 3, res["rowsSkipped"])
	assert.Equal(t, 1, res["partsCreated"])
	assert.Equal(t, 2, res["pricesInserted"])
	hist := decode[[]priceJSON](t, f.do(t, http.MethodGet, f.path("/catalog/parts/"+idStr(later)+"/prices"), f.token, ""))
	require.Len(t, hist, 2)
	assert.Equal(t, "2026-06-01", *hist[1].ValidFrom)
}

func TestPriceImport_StaleThenRefresh(t *testing.T) {
	f := newStock(t)
	p := f.part(t, "A-1", "A", "")
	job := f.upload(t, "a.csv", "code;price\nA-1;3\n", nil)
	require.Equal(t, http.StatusCreated, f.post(t, f.path("/catalog/parts/"+idStr(p)+"/prices"), f.token, `{"price":"2","validFrom":"2026-01-01"}`).Code)
	rec := f.apply(t, job, uuid.NewString(), `{}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_IMPORT_STALE", errCode(t, rec))
	assert.Equal(t, "READY", decode[importJSON](t, f.do(t, http.MethodGet, f.importPath(job.ID, ""), f.token, "")).Status)

	rec = f.req(t, http.MethodPatch, f.importPath(job.ID, ""), f.token, `{}`, map[string]string{"If-Match": `"1"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	fresh := decode[importJSON](t, rec)
	assert.Equal(t, 1, fresh.Counts["price"])
	rec = f.apply(t, fresh, uuid.NewString(), `{}`)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
}

// Several clerks press apply at once: one import, prices inserted once.
func TestPriceImport_ConcurrentApplyAppliesOnce(t *testing.T) {
	f := newStock(t)
	for i := 0; i < 5; i++ {
		f.part(t, "P-"+strconv.Itoa(i), "P", "")
	}
	job := f.upload(t, "p.csv", "code;price\nP-0;1\nP-1;2\nP-2;3\nP-3;4\nP-4;5\nNEW-1;6\n", nil)
	codes := make([]int, 6)
	var wg sync.WaitGroup
	for i := range codes {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			codes[i] = f.apply(t, job, uuid.NewString(), `{}`).Code
		}(i)
	}
	wg.Wait()
	ok := 0
	for _, c := range codes {
		if c == http.StatusOK {
			ok++
		} else {
			assert.Equal(t, http.StatusConflict, c)
		}
	}
	assert.Equal(t, 1, ok, "%v", codes)
	assert.EqualValues(t, 6, f.countRows(t, `SELECT count(*) FROM prices WHERE import_id = ?`, job.ID))
	assert.EqualValues(t, 1, f.countRows(t, `SELECT count(*) FROM parts WHERE stock_no = 'NEW-1'`))
}

func TestPriceImport_DiscardAndFinalStates(t *testing.T) {
	f := newStock(t)
	job := f.upload(t, "a.csv", "code;price\nA-1;3\n", nil)
	path := f.importPath(job.ID, "/discard")
	assert.Equal(t, http.StatusPreconditionRequired, f.req(t, http.MethodPost, path, f.token, "", nil).Code)
	assert.Equal(t, http.StatusPreconditionFailed, f.req(t, http.MethodPost, path, f.token, "", map[string]string{"If-Match": `"5"`}).Code)
	rec := f.req(t, http.MethodPost, path, f.token, "", map[string]string{"If-Match": `"1"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	d := decode[importJSON](t, rec)
	assert.Equal(t, "DISCARDED", d.Status)
	assert.EqualValues(t, 0, f.countRows(t, `SELECT count(*) FROM price_import_files WHERE import_id = ?`, job.ID))

	rec = f.apply(t, d, uuid.NewString(), `{}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_INVALID_STATE_TRANSITION", errCode(t, rec))
	rec = f.req(t, http.MethodPatch, f.importPath(job.ID, ""), f.token, `{}`, map[string]string{"If-Match": `"2"`})
	assert.Equal(t, http.StatusConflict, rec.Code)
	rec = f.req(t, http.MethodPost, path, f.token, "", map[string]string{"If-Match": `"2"`})
	assert.Equal(t, http.StatusConflict, rec.Code)

	old := f.upload(t, "b.csv", "code;price\nB-1;3\n", nil)
	require.NoError(t, testDB.Exec(`UPDATE price_imports SET expires_at = now() - interval '1 minute' WHERE id = ?`, old.ID).Error)
	got := decode[importJSON](t, f.do(t, http.MethodGet, f.importPath(old.ID, ""), f.token, ""))
	assert.Equal(t, "EXPIRED", got.Status)
	rec = f.apply(t, got, uuid.NewString(), `{}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
}

func lisaktLine(op, katno, stno, oem, name, price, deg, bkatno, bstno string) string {
	pad := func(s string, n int) string { return s + strings.Repeat(" ", n-len([]rune(s))) }
	return op + katno + pad(stno, 15) + pad("", 30) + pad(oem, 30) + pad("", 30) + pad(name, 60) + pad("", 30) +
		strings.Repeat("0", 32) + price + pad("", 10) + deg + "00001" + strings.Repeat("0", 15) + bkatno + pad(bstno, 15) +
		strings.Repeat("0", 16) + "\r\n"
}

func TestPriceImport_LISAKTEndToEnd(t *testing.T) {
	f := newStock(t)
	old := f.part(t, "OLD-1", "Old", "")
	upd := f.part(t, "UPD-1", "Update me", `,"initialPrice":{"price":"1","validFrom":"2026-01-01"}`)
	text := lisaktLine("1", "0010", "NEW-1", "OEM 9", "Yeni", "00000000025000", "20260301", "0010", "UPD-1") +
		lisaktLine("2", "0010", "UPD-1", "", "", "00000000030000", "00000000", "0000", "") +
		lisaktLine("0", "0010", "OLD-1", "", "", "00000000000000", "00000000", "0000", "") +
		lisaktLine("1", "0011", "BAD-1", "", "Bad", "00000000010000", "00000000", "0000", "") +
		lisaktLine("2", "0010", "GHOST-1", "", "Ghost", "00000000010000", "00000000", "0010", "NOPE")
	job := f.upload(t, "LISAKT.DAT", text, map[string]string{"mapping": `{"preset":"LISAKT"}`})
	assert.Equal(t, "READY", job.Status, "%+v", job)
	assert.Equal(t, "FIXED_WIDTH", job.Format)
	assert.Equal(t, 1, job.Counts["delete"])
	assert.Equal(t, 1, job.Counts["errors"])

	rows := f.importRows(t, job.ID, `{"startRow":0,"endRow":50,"change":"ERROR"}`)
	require.Len(t, rows.Rows, 1)
	assert.Equal(t, "CATALOG_MISMATCH", rows.Rows[0].Errors[0].Code)
	warn := f.importRows(t, job.ID, `{"startRow":0,"endRow":50,"change":"WARNING"}`)
	require.Len(t, warn.Rows, 1)
	var wcodes []string
	for _, w := range warn.Rows[0].Warnings {
		wcodes = append(wcodes, w.Code)
	}
	assert.ElementsMatch(t, []string{"OP_MISMATCH", "EQUIVALENT_NOT_FOUND"}, wcodes)

	rec := f.apply(t, job, uuid.NewString(), `{"skipErrorRows":true}`)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	res := decode[importJSON](t, rec).Result
	assert.Equal(t, 2, res["partsCreated"])
	assert.Equal(t, 1, res["partsDeactivated"])
	assert.False(t, decode[partJSON](t, f.do(t, http.MethodGet, f.path("/catalog/parts/"+idStr(old)), f.token, "")).IsActive)
	hist := decode[[]priceJSON](t, f.do(t, http.MethodGet, f.path("/catalog/parts/"+idStr(upd)+"/prices"), f.token, ""))
	require.Len(t, hist, 2)
	assert.Equal(t, "3", *hist[0].Price)
	assert.Equal(t, "2026-06-01", *hist[0].ValidFrom)
	hits := decode[[]partJSON](t, f.do(t, http.MethodGet, f.path("/catalog/parts/lookup?code=NEW-1"), f.token, ""))
	require.Len(t, hits, 1)
	var kinds []string
	for _, c := range hits[0].Codes {
		kinds = append(kinds, c.Kind+":"+c.Code)
	}
	assert.ElementsMatch(t, []string{"OEM:OEM 9", "EQUIVALENT:UPD-1"}, kinds)
	assert.Equal(t, "2.5", *hits[0].ListPrice)
}

func TestPriceImport_UploadValidationListAndPresets(t *testing.T) {
	f := newStock(t)
	rec := f.uploadWith(t, uuid.NewString(), "", nil, map[string]string{"catalogId": idStr(f.catalog), "validFrom": "2026-01-01"})
	assert.Equal(t, http.StatusBadRequest, rec.Code)
	assert.Equal(t, "ERR_NO_FILE_UPLOADED", errCode(t, rec))
	rec = f.uploadWith(t, uuid.NewString(), "a.csv", []byte("code;price\n"), map[string]string{"catalogId": "999999", "validFrom": "2026-01-01"})
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_CATALOG_NOT_FOUND", errCode(t, rec))
	rec = f.uploadWith(t, uuid.NewString(), "a.csv", []byte("code;price\n"), map[string]string{"catalogId": idStr(f.catalog)})
	assert.Equal(t, http.StatusBadRequest, rec.Code)
	rec = f.uploadWith(t, uuid.NewString(), "a.csv", []byte("x"), map[string]string{"catalogId": idStr(f.catalog), "validFrom": "2026-01-01", "mapping": "{"})
	assert.Equal(t, http.StatusBadRequest, rec.Code)

	bad := f.upload(t, "broken.xlsx", "not a zip", nil)
	assert.Equal(t, "FAILED", bad.Status)
	assert.Equal(t, "ERR_IMPORT_FORMAT", *bad.FailureCode)

	key := uuid.NewString()
	fields := map[string]string{"catalogId": idStr(f.catalog), "validFrom": "2026-01-01"}
	first := f.uploadWith(t, key, "a.csv", []byte("code;price\nA;1\n"), fields)
	require.Equal(t, http.StatusCreated, first.Code, first.Body.String())
	replay := f.uploadWith(t, key, "a.csv", []byte("code;price\nA;1\n"), fields)
	assert.Equal(t, "true", replay.Header().Get("Idempotent-Replayed"))
	assert.JSONEq(t, first.Body.String(), replay.Body.String())

	other := f.newCatalog(t, 11, "Other")
	f.upload(t, "b.csv", "code;price\nB;1\n", map[string]string{"catalogId": idStr(other)})
	page := decode[struct {
		Items      []importJSON `json:"items"`
		NextCursor *string      `json:"nextCursor"`
	}](t, f.do(t, http.MethodGet, f.path("/catalog/imports?limit=1&catalogId="+idStr(f.catalog)), f.token, ""))
	require.Len(t, page.Items, 1)
	assert.Equal(t, "a.csv", page.Items[0].FileName)
	require.NotNil(t, page.NextCursor)
	page2 := decode[struct {
		Items      []importJSON `json:"items"`
		NextCursor *string      `json:"nextCursor"`
	}](t, f.do(t, http.MethodGet, f.path("/catalog/imports?limit=1&catalogId="+idStr(f.catalog)+"&cursor="+*page.NextCursor), f.token, ""))
	require.Len(t, page2.Items, 1)
	assert.Equal(t, "broken.xlsx", page2.Items[0].FileName)
	assert.Nil(t, page2.NextCursor)

	presets := decode[struct {
		Presets []struct {
			Code    string         `json:"code"`
			Mapping map[string]any `json:"mapping"`
		} `json:"presets"`
		Aliases map[string][]string `json:"aliases"`
	}](t, f.do(t, http.MethodGet, f.path("/catalog/import-presets"), f.token, ""))
	require.Len(t, presets.Presets, 1)
	assert.Equal(t, "LISAKT", presets.Presets[0].Code)
	assert.Contains(t, presets.Aliases["price"], "fiyat")
}

func TestPriceImport_RowsGridXLSX(t *testing.T) {
	f := newStock(t)
	job := f.upload(t, "a.csv", "code;price\nA;1\nB;2\n", nil)
	rec := f.req(t, http.MethodPost, f.importPath(job.ID, "/rows/grid"), f.token, `{"change":"NEW"}`,
		map[string]string{echo.HeaderAccept: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Contains(t, sheetXML(t, rec.Body.Bytes()), ">B<")
	rec = f.req(t, http.MethodPost, f.importPath(job.ID, "/rows/grid"), f.token, `{"filterModel":{"nope":{"type":"equals","filter":"x"}}}`, nil)
	assert.Equal(t, http.StatusBadRequest, rec.Code)
	rec = f.req(t, http.MethodPost, f.importPath(999999, "/rows/grid"), f.token, `{}`, nil)
	assert.Equal(t, http.StatusNotFound, rec.Code)
}
