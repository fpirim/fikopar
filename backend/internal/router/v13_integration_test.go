//go:build integration

package router_test

import (
	"net/http"
	"net/url"
	"strconv"
	"testing"

	"github.com/google/uuid"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestV13_SearchWordsMatchNameOrCode(t *testing.T) {
	f := newStock(t)
	f.part(t, "HP-3712", "Hydraulic pump", "")
	f.part(t, "HP-4100", "Hydraulic pump", "")
	f.part(t, "K-1", "Pump housing", `,"codes":[{"kind":"OEM","code":"oe-37.99"}]`)
	f.part(t, "K-2", "Water pump", `,"codes":[{"kind":"MANUFACTURER","code":"W-100"},{"kind":"ALT","code":"old 5537"}]`)
	f.part(t, "G-37", "Gear", "")
	f.part(t, "HP-3799", "Hydraulic pump", `,"isActive":false`)
	f.part(t, "K-3", "Oil filter", `,"codes":[{"kind":"ALT","code":"a-37"},{"kind":"OEM","code":"o-37"}]`)
	f.part(t, "P-37", "Oil seal", `,"codes":[{"kind":"OEM","code":"x-37"}]`)

	r := f.search(t, "q="+url.QueryEscape("pump 37"))
	require.Equal(t, []string{"HP-3712", "K-1", "K-2"}, stockNos(r.Items))
	assert.Equal(t, 3, r.Total)
	for _, h := range r.Items {
		assert.Equal(t, "NAME", h.MatchKind, h.StockNo)
	}
	require.NotNil(t, r.Items[0].MatchedCode)
	assert.Equal(t, "HP-3712", *r.Items[0].MatchedCode)
	require.NotNil(t, r.Items[1].MatchedCode)
	assert.Equal(t, "oe-37.99", *r.Items[1].MatchedCode)
	require.NotNil(t, r.Items[2].MatchedCode)
	assert.Equal(t, "old 5537", *r.Items[2].MatchedCode)

	r = f.search(t, "q="+url.QueryEscape("oil 37"))
	require.Equal(t, []string{"K-3", "P-37"}, stockNos(r.Items))
	assert.Equal(t, "o-37", *r.Items[0].MatchedCode, "OEM before ALT")
	assert.Equal(t, "P-37", *r.Items[1].MatchedCode, "stock no first")

	r = f.search(t, "q="+url.QueryEscape("37 PUMP hydraulıc"))
	assert.Equal(t, []string{"HP-3712"}, stockNos(r.Items), "every word, any order")

	r = f.search(t, "q="+url.QueryEscape("pump housing"))
	require.Equal(t, []string{"K-1"}, stockNos(r.Items))
	assert.Nil(t, r.Items[0].MatchedCode, "name only")

	r = f.search(t, "q="+url.QueryEscape("pump -- 37"))
	assert.Empty(t, r.Items, "a word without letters or digits matches the name only")

	r = f.search(t, "q="+url.QueryEscape("hp-37"))
	require.Equal(t, []string{"HP-3712"}, stockNos(r.Items))
	assert.Equal(t, "STOCK_NO", r.Items[0].MatchKind, "whole q ranks first")
}

type partRowV13 struct {
	ID           uint   `json:"id"`
	StockNo      string `json:"stockNo"`
	OnHand       string `json:"onHand"`
	StockByDepot []struct {
		DepotID   uint   `json:"depotId"`
		DepotCode string `json:"depotCode"`
		OnHand    string `json:"onHand"`
		Available string `json:"available"`
	} `json:"stockByDepot"`
}

func TestV13_PartsGridStockByDepot(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "A", "Alpha", "")
	f.part(t, "B", "Beta", "")
	f.move(t, a, f.depot, "IN", "5", "2026-03-01")
	f.move(t, a, f.depot2, "OUT", "2", "2026-03-02")
	grid := func(body string) []partRowV13 {
		rec := f.req(t, http.MethodPost, f.path("/catalog/parts/grid"), f.token, body, nil)
		require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
		return decode[struct {
			Rows []partRowV13 `json:"rows"`
		}](t, rec).Rows
	}

	rows := grid(`{"startRow":0,"endRow":10}`)
	require.Len(t, rows, 2)
	require.Len(t, rows[0].StockByDepot, 2)
	assert.Equal(t, "3", rows[0].OnHand)
	assert.Equal(t, f.depot, rows[0].StockByDepot[0].DepotID)
	assert.Equal(t, "1", rows[0].StockByDepot[0].DepotCode)
	assert.Equal(t, "5", rows[0].StockByDepot[0].OnHand)
	assert.Equal(t, "5", rows[0].StockByDepot[0].Available)
	assert.Equal(t, "2", rows[0].StockByDepot[1].DepotCode)
	assert.Equal(t, "-2", rows[0].StockByDepot[1].OnHand)
	require.Len(t, rows[1].StockByDepot, 2, "zeros included")
	assert.Equal(t, "0", rows[1].StockByDepot[1].OnHand)

	rows = grid(`{"startRow":0,"endRow":10,"depotId":` + idStr(f.depot) + `}`)
	require.Len(t, rows, 2)
	assert.Nil(t, rows[0].StockByDepot, "omitted with a depot")

	rec := f.req(t, http.MethodPost, f.path("/catalog/parts/grid"), f.token, `{"sortModel":[{"colId":"stockByDepot","sort":"asc"}]}`, nil)
	assert.Equal(t, http.StatusBadRequest, rec.Code)
}

type movementV13 struct {
	StockNo string  `json:"stockNo"`
	Kind    string  `json:"kind"`
	Qty     string  `json:"qty"`
	QtyIn   *string `json:"qtyIn"`
	QtyOut  *string `json:"qtyOut"`
}

func TestV13_MovementsQtyInQtyOutColumns(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "A", "Alpha", "")
	f.move(t, a, f.depot, "IN", "10", "2026-01-10")
	f.move(t, a, f.depot, "OUT", "3", "2026-02-10")
	f.move(t, a, f.depot, "IN", "4", "2026-02-11")
	f.move(t, a, f.depot, "OUT", "20", "2026-03-10")
	grid := func(body string) ([]movementV13, int64) {
		rec := f.req(t, http.MethodPost, f.path("/stock/movements/grid"), f.token, body, nil)
		require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
		g := decode[struct {
			Rows       []movementV13 `json:"rows"`
			TotalCount int64         `json:"totalCount"`
		}](t, rec)
		return g.Rows, g.TotalCount
	}
	qtys := func(rows []movementV13) []string {
		out := make([]string, len(rows))
		for i, r := range rows {
			out[i] = r.Qty
		}
		return out
	}

	rows, n := grid(`{"filterModel":{"qtyIn":{"filterType":"number","type":"greaterThan","filter":5}}}`)
	assert.EqualValues(t, 1, n)
	require.Equal(t, []string{"10"}, qtys(rows))
	require.NotNil(t, rows[0].QtyIn)
	assert.Equal(t, "10", *rows[0].QtyIn)
	assert.Nil(t, rows[0].QtyOut)

	rows, _ = grid(`{"sortModel":[{"colId":"qtyOut","sort":"desc"}]}`)
	assert.Equal(t, []string{"20", "3", "4", "10"}, qtys(rows), "nulls last, then date desc")
	rows, _ = grid(`{"sortModel":[{"colId":"qtyIn","sort":"asc"}]}`)
	assert.Equal(t, []string{"4", "10", "20", "3"}, qtys(rows))

	rows, _ = grid(`{"filterModel":{"qtyOut":{"filterType":"number","type":"blank"}}}`)
	assert.Equal(t, []string{"4", "10"}, qtys(rows))
	rows, _ = grid(`{"filterModel":{"qtyOut":{"filterType":"number","type":"inRange","filter":1,"filterTo":5}}}`)
	require.Equal(t, []string{"3"}, qtys(rows))
	require.NotNil(t, rows[0].QtyOut)
	assert.Equal(t, "3", *rows[0].QtyOut)
}

type partV13 struct {
	ID    uint `json:"id"`
	Codes []struct {
		Kind             string `json:"kind"`
		Code             string `json:"code"`
		EquivalentPartID *uint  `json:"equivalentPartId"`
	} `json:"codes"`
	Equivalents []struct {
		PartID      uint   `json:"partId"`
		StockNo     string `json:"stockNo"`
		Name        string `json:"name"`
		CatalogID   uint   `json:"catalogId"`
		CatalogName string `json:"catalogName"`
	} `json:"equivalents"`
	Version int `json:"version"`
}

func TestV13_PartEquivalentsAndEquivalentCode(t *testing.T) {
	f := newStock(t)
	cat2 := f.newCatalog(t, 20, "Mahle")
	a := f.part(t, "EQ-1", "Filter A", "")
	b := f.partIn(t, cat2, "M-7", "Filter M", `,"codes":[{"kind":"EQUIVALENT","code":"anything","equivalentPartId":`+idStr(a)+`}]`)
	lone := f.part(t, "L-1", "Lone", "")
	get := func(id uint) partV13 {
		rec := f.do(t, http.MethodGet, f.path("/catalog/parts/"+idStr(id)), f.token, "")
		require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
		return decode[partV13](t, rec)
	}

	pb := get(b)
	require.Len(t, pb.Codes, 1)
	assert.Equal(t, "EQ-1", pb.Codes[0].Code, "EQUIVALENT code is the linked stock no")
	require.Len(t, pb.Equivalents, 1)
	assert.Equal(t, a, pb.Equivalents[0].PartID)
	assert.Equal(t, "EQ-1", pb.Equivalents[0].StockNo)
	assert.Equal(t, "Filter A", pb.Equivalents[0].Name)
	assert.Equal(t, f.catalog, pb.Equivalents[0].CatalogID)
	assert.Equal(t, "Bosch", pb.Equivalents[0].CatalogName)

	pa := get(a)
	require.Len(t, pa.Equivalents, 1, "either direction")
	assert.Equal(t, b, pa.Equivalents[0].PartID)
	assert.Equal(t, "Mahle", pa.Equivalents[0].CatalogName)

	rec := f.do(t, http.MethodGet, f.path("/catalog/parts/"+idStr(lone)), f.token, "")
	assert.Contains(t, rec.Body.String(), `"equivalents":[]`)

	rec = f.req(t, http.MethodPut, f.path("/catalog/parts/"+idStr(lone)), f.token,
		`{"catalogId":`+idStr(f.catalog)+`,"stockNo":"L-1","name":"Lone","codes":[{"kind":"EQUIVALENT","code":"x","equivalentPartId":`+idStr(b)+`}]}`,
		map[string]string{"If-Match": `"` + strconv.Itoa(get(lone).Version) + `"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	pl := decode[partV13](t, rec)
	assert.Equal(t, "M-7", pl.Codes[0].Code)
	require.Len(t, pl.Equivalents, 1)
	assert.Equal(t, "M-7", pl.Equivalents[0].StockNo)
}

func TestV13_DepotSettingsOmittedFieldsAreCleared(t *testing.T) {
	f := newStock(t)
	p := f.part(t, "P-1", "P", "")
	path := f.path("/catalog/parts/" + idStr(p) + "/depots/" + idStr(f.depot))
	rec := f.req(t, http.MethodPut, path, f.token, `{"minQty":"8","maxQty":"20","shelf":"A-04-2"}`, map[string]string{"If-Match": `"0"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	rec = f.req(t, http.MethodPut, path, f.token, `{}`, map[string]string{"If-Match": `"1"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	l := decode[levelJSON](t, rec)
	assert.Equal(t, "0", l.MinQty)
	assert.Nil(t, l.MaxQty)
	assert.Equal(t, "", l.Shelf)
	assert.Equal(t, 2, l.Version)
}

func TestV13_GetStockCountLine(t *testing.T) {
	f := newStock(t)
	p := f.part(t, "P-1", "P", "")
	q := f.part(t, "P-2", "Q", "")
	f.move(t, p, f.depot, "IN", "5", "2026-03-01")
	f.move(t, q, f.depot2, "IN", "1", "2026-03-01")
	c := f.openCount(t, f.depot, "")
	c2 := f.openCount(t, f.depot2, "")
	line := f.setLine(t, c.ID, f.lineOf(t, c.ID, p), `"4"`)

	rec := f.do(t, http.MethodGet, f.countPath(c.ID, "/lines/"+idStr(line.ID)), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	got := decode[countLineJSON](t, rec)
	assert.Equal(t, `"`+strconv.Itoa(line.Version)+`"`, rec.Header().Get("ETag"))
	assert.Equal(t, line.Version, got.Version)
	assert.Equal(t, "4", *got.CountedQty)
	assert.Equal(t, "-1", *got.Difference)
	assert.Equal(t, "P-1", got.StockNo)

	other := f.lineOf(t, c2.ID, q)
	rec = f.do(t, http.MethodGet, f.countPath(c.ID, "/lines/"+idStr(other.ID)), f.token, "")
	assert.Equal(t, http.StatusNotFound, rec.Code)
	assert.Equal(t, "ERR_STOCK_COUNT_LINE_NOT_FOUND", errCode(t, rec))
	rec = f.do(t, http.MethodGet, f.countPath(999999, "/lines/"+idStr(line.ID)), f.token, "")
	assert.Equal(t, http.StatusNotFound, rec.Code)
	assert.Equal(t, "ERR_STOCK_COUNT_NOT_FOUND", errCode(t, rec))

	f.finalize(t, c, "SKIP")
	rec = f.do(t, http.MethodGet, f.countPath(c.ID, "/lines/"+idStr(line.ID)), f.token, "")
	assert.Equal(t, http.StatusOK, rec.Code, "any status")
}

func TestV13_ImportMappingIsEffectiveOrNull(t *testing.T) {
	f := newStock(t)
	job := f.upload(t, "list.csv", "code;price\nA-1;10\n", nil)
	require.Equal(t, "READY", job.Status)
	for _, k := range []string{"format", "encoding", "decimalSeparator", "dateFormat", "headerRow", "listSeparator", "columns"} {
		assert.Contains(t, job.Mapping, k)
	}

	job = f.upload(t, "list.csv", "x;y\nA-1;10\n", nil)
	require.Equal(t, "NEEDS_MAPPING", job.Status)
	assert.Contains(t, job.Mapping, "columns", "empty columns are listed")

	rec := f.uploadWith(t, uuid.NewString(), "list.csv", []byte("code;price\nA-1;10\n"),
		map[string]string{"catalogId": idStr(f.catalog), "validFrom": "2026-06-01", "mapping": `{"encoding":"LATIN-9"}`})
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	assert.Equal(t, "FAILED", decode[importJSON](t, rec).Status)
	assert.Contains(t, rec.Body.String(), `"mapping":null`)
}

func TestV13_EquivalentCodeFollowsTheLinkedStockNo(t *testing.T) {
	f := newStock(t)
	cat2 := f.newCatalog(t, 20, "Mahle")
	a := f.part(t, "X-1", "Filter A", "")
	a2 := f.partIn(t, cat2, "X-1", "Filter A2", "")
	b := f.part(t, "B-1", "Filter B", `,"codes":[{"kind":"EQUIVALENT","code":"x","equivalentPartId":`+idStr(a)+`},`+
		`{"kind":"EQUIVALENT","code":"y","equivalentPartId":`+idStr(a2)+`}]`)
	get := func(id uint) partV13 {
		rec := f.do(t, http.MethodGet, f.path("/catalog/parts/"+idStr(id)), f.token, "")
		require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
		return decode[partV13](t, rec)
	}
	pb := get(b)
	require.Len(t, pb.Codes, 2, "same stock no in two catalogs, two links")
	require.Len(t, pb.Equivalents, 2)

	rec := f.req(t, http.MethodPut, f.path("/catalog/parts/"+idStr(a)), f.token,
		`{"catalogId":`+idStr(f.catalog)+`,"stockNo":"X-9","name":"Filter A"}`,
		map[string]string{"If-Match": `"` + strconv.Itoa(get(a).Version) + `"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	codes := map[uint]string{}
	for _, c := range get(b).Codes {
		codes[*c.EquivalentPartID] = c.Code
	}
	assert.Equal(t, map[uint]string{a: "X-9", a2: "X-1"}, codes)
	r := f.search(t, "q=X9")
	assert.Equal(t, []string{"B-1", "X-9"}, stockNos(r.Items))
}
