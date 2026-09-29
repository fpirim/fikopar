//go:build integration

package router_test

import (
	"net/http"
	"net/url"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type hitJSON struct {
	PartID      uint       `json:"partId"`
	CatalogCode int        `json:"catalogCode"`
	StockNo     string     `json:"stockNo"`
	Name        string     `json:"name"`
	Codes       []codeJSON `json:"codes"`
	MatchedCode *string    `json:"matchedCode"`
	MatchKind   string     `json:"matchKind"`
	Equivalents []struct {
		PartID      uint   `json:"partId"`
		StockNo     string `json:"stockNo"`
		OnHandTotal string `json:"onHandTotal"`
	} `json:"equivalents"`
	Stock []struct {
		DepotID   uint   `json:"depotId"`
		OnHand    string `json:"onHand"`
		Available string `json:"available"`
	} `json:"stock"`
	OnHandTotal        string  `json:"onHandTotal"`
	ListPrice          *string `json:"listPrice"`
	ListPriceValidFrom *string `json:"listPriceValidFrom"`
	VatPct             string  `json:"vatPct"`
	Unit               string  `json:"unit"`
}

type catalogSearchJSON struct {
	Items       []hitJSON `json:"items"`
	Total       int       `json:"total"`
	TotalCapped bool      `json:"totalCapped"`
	Date        string    `json:"date"`
	NextCursor  *string   `json:"nextCursor"`
}

func (f *stockFix) search(t *testing.T, query string) catalogSearchJSON {
	t.Helper()
	rec := f.do(t, http.MethodGet, f.path("/catalog/search?"+query), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[catalogSearchJSON](t, rec)
}

func stockNos(items []hitJSON) []string {
	out := make([]string, len(items))
	for i, h := range items {
		out[i] = h.StockNo
	}
	return out
}

func TestSearch_RanksExactPrefixSubstringName(t *testing.T) {
	f := newStock(t)
	cat2 := f.newCatalog(t, 20, "Mahle")
	exact := f.part(t, "ABC-123", "Hava filtresi", `,"initialPrice":{"price":"5","validFrom":"2026-01-01"}`)
	f.part(t, "ABC1234", "Yağ filtresi", "")
	f.part(t, "X-ABC123-Y", "Mazot filtresi", "")
	f.part(t, "ZZZ", "Filtre ABC123 uyumlu", "")
	oem := f.partIn(t, cat2, "M-9", "Kabin filtresi", `,"codes":[{"kind":"OEM","code":"abc.123"}]`)
	f.part(t, "OFF", "Inactive ABC123", `,"isActive":false`)
	f.move(t, exact, f.depot, "IN", "4", "2026-02-01")

	r := f.search(t, "q="+url.QueryEscape("abc 123")+"&date=2026-06-01")
	assert.Equal(t, []string{"ABC-123", "M-9", "ABC1234", "X-ABC123-Y", "ZZZ"}, stockNos(r.Items))
	assert.Equal(t, 5, r.Total)
	assert.False(t, r.TotalCapped)
	assert.Equal(t, "2026-06-01", r.Date)
	assert.Equal(t, "STOCK_NO", r.Items[0].MatchKind)
	assert.Equal(t, "OEM", r.Items[1].MatchKind)
	assert.Equal(t, "abc.123", *r.Items[1].MatchedCode)
	assert.Equal(t, "NAME", r.Items[4].MatchKind)
	require.Len(t, r.Items[0].Stock, 2)
	assert.Equal(t, "4", r.Items[0].Stock[0].OnHand)
	assert.Equal(t, "0", r.Items[0].Stock[1].OnHand)
	assert.Equal(t, "4", r.Items[0].OnHandTotal)
	assert.Equal(t, "5", *r.Items[0].ListPrice)
	assert.Nil(t, r.Items[1].ListPrice)
	assert.Equal(t, oem, r.Items[1].PartID)

	r = f.search(t, "q=abc123&catalogId="+idStr(cat2))
	assert.Equal(t, []string{"M-9"}, stockNos(r.Items))
	r = f.search(t, "q=abc123&inStockDepotId="+idStr(f.depot))
	assert.Equal(t, []string{"ABC-123"}, stockNos(r.Items))
	r = f.search(t, "q="+url.QueryEscape("YAĞ FİLTRESİ"))
	assert.Equal(t, []string{"ABC1234"}, stockNos(r.Items), "name words, Turkish case-insensitive")
	r = f.search(t, "q="+url.QueryEscape("filtresi kabın"))
	assert.Equal(t, []string{"M-9"}, stockNos(r.Items), "every word, dotless i")

	assert.Equal(t, http.StatusBadRequest, f.do(t, http.MethodGet, f.path("/catalog/search?q=a"), f.token, "").Code)
	assert.Equal(t, http.StatusBadRequest, f.do(t, http.MethodGet, f.path("/catalog/search?q=ab&limit=51"), f.token, "").Code)
}

func TestSearch_EquivalentsBothWaysAndPaging(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "EQ-1", "A", "")
	b := f.part(t, "EQ-2", "B", `,"codes":[{"kind":"EQUIVALENT","code":"EQ-1","equivalentPartId":`+idStr(a)+`}]`)
	f.move(t, b, f.depot2, "IN", "3", "2026-02-01")
	f.part(t, "EQ-3", "C", "")

	r := f.search(t, "q=EQ&limit=2")
	assert.Equal(t, []string{"EQ-1", "EQ-2"}, stockNos(r.Items))
	assert.Equal(t, 3, r.Total)
	require.Len(t, r.Items[0].Equivalents, 1)
	assert.Equal(t, b, r.Items[0].Equivalents[0].PartID)
	assert.Equal(t, "3", r.Items[0].Equivalents[0].OnHandTotal)
	require.Len(t, r.Items[1].Equivalents, 1)
	assert.Equal(t, a, r.Items[1].Equivalents[0].PartID)
	require.NotNil(t, r.NextCursor)
	r2 := f.search(t, "q=EQ&limit=2&cursor="+url.QueryEscape(*r.NextCursor))
	assert.Equal(t, []string{"EQ-3"}, stockNos(r2.Items))
	assert.Nil(t, r2.NextCursor)
	assert.Equal(t, http.StatusBadRequest, f.do(t, http.MethodGet, f.path("/catalog/search?q=EQ&cursor=@@"), f.token, "").Code)
}

func TestSearch_TotalIsCappedAt1000(t *testing.T) {
	f := newStock(t)
	require.NoError(t, testDB.Exec(`INSERT INTO parts (org_id, catalog_id, stock_no, name)
		SELECT ?, ?, 'CAP-' || g, 'Bulk' FROM generate_series(1, 1005) g`, f.org, f.catalog).Error)
	r := f.search(t, "q=CAP")
	assert.Equal(t, 1000, r.Total)
	assert.True(t, r.TotalCapped)
	assert.Len(t, r.Items, 20)
}

func TestResolve_FoundNotFoundAmbiguous(t *testing.T) {
	f := newStock(t)
	cat2 := f.newCatalog(t, 20, "Mahle")
	a := f.part(t, "R-1", "A", `,"codes":[{"kind":"OEM","code":"OEM-7"}]`)
	f.partIn(t, cat2, "R-2", "B", `,"codes":[{"kind":"ALT","code":"SHARED"}]`)
	f.part(t, "R-3", "C", `,"codes":[{"kind":"ALT","code":"shared"}]`)
	f.move(t, a, f.depot, "IN", "2", "2026-02-01")
	rec := f.req(t, http.MethodPost, f.path("/catalog/parts/resolve"), f.token, `{"codes":["oem 7","nope","SHARED","r1"],"depotId":`+idStr(f.depot)+`}`, nil)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	res := decode[[]struct {
		Code       string    `json:"code"`
		Status     string    `json:"status"`
		Part       *hitJSON  `json:"part"`
		Candidates []hitJSON `json:"candidates"`
	}](t, rec)
	require.Len(t, res, 4)
	assert.Equal(t, "FOUND", res[0].Status)
	assert.Equal(t, a, res[0].Part.PartID)
	assert.Equal(t, "OEM", res[0].Part.MatchKind)
	assert.Equal(t, "2", res[0].Part.Stock[0].OnHand)
	assert.Equal(t, "NOT_FOUND", res[1].Status)
	assert.Equal(t, "AMBIGUOUS", res[2].Status)
	assert.Len(t, res[2].Candidates, 2)
	assert.Equal(t, "FOUND", res[3].Status)
	assert.Equal(t, "nope", res[1].Code)
	assert.Equal(t, http.StatusBadRequest, f.req(t, http.MethodPost, f.path("/catalog/parts/resolve"), f.token, `{"codes":[]}`, nil).Code)
}

type partGrid struct {
	Rows []struct {
		ID          uint       `json:"id"`
		StockNo     string     `json:"stockNo"`
		CatalogCode int        `json:"catalogCode"`
		Codes       []codeJSON `json:"codes"`
		ListPrice   *string    `json:"listPrice"`
		OnHand      string     `json:"onHand"`
		Available   string     `json:"available"`
		MinQty      *string    `json:"minQty"`
		Shelf       *string    `json:"shelf"`
		BelowMin    bool       `json:"belowMin"`
		Negative    bool       `json:"negative"`
		IsActive    bool       `json:"isActive"`
	} `json:"rows"`
	TotalCount      int64             `json:"totalCount"`
	UnfilteredCount int64             `json:"unfilteredCount"`
	Totals          map[string]string `json:"totals"`
}

func (f *stockFix) partGrid(t *testing.T, body string) partGrid {
	t.Helper()
	rec := f.req(t, http.MethodPost, f.path("/catalog/parts/grid"), f.token, body, nil)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[partGrid](t, rec)
}

func TestPartsGrid_FiltersSortDepotScopeAndExcel(t *testing.T) {
	f := newStock(t)
	cat2 := f.newCatalog(t, 20, "Mahle")
	a := f.part(t, "A-1", "Alpha", `,"brand":"Bosch","codes":[{"kind":"OEM","code":"77.01-X"}],"initialPrice":{"price":"10","validFrom":"2026-01-01"}`)
	b := f.part(t, "B-1", "Beta", `,"brand":"Mahle"`)
	f.partIn(t, cat2, "A-1", "Alpha twin", "")
	f.part(t, "OFF", "Old", `,"isActive":false`)
	f.move(t, a, f.depot, "IN", "5", "2026-02-01")
	f.move(t, a, f.depot2, "OUT", "2", "2026-02-01")
	f.move(t, b, f.depot, "OUT", "1", "2026-02-01")
	require.Equal(t, http.StatusOK, f.req(t, http.MethodPut, f.path("/catalog/parts/"+idStr(a)+"/depots/"+idStr(f.depot)), f.token,
		`{"minQty":"10","shelf":"A-01"}`, map[string]string{"If-Match": `"0"`}).Code)

	g := f.partGrid(t, `{"startRow":0,"endRow":50}`)
	require.Len(t, g.Rows, 3)
	assert.Equal(t, []string{"A-1", "A-1", "B-1"}, []string{g.Rows[0].StockNo, g.Rows[1].StockNo, g.Rows[2].StockNo})
	assert.Equal(t, []int{10, 20}, []int{g.Rows[0].CatalogCode, g.Rows[1].CatalogCode})
	assert.Equal(t, "3", g.Rows[0].OnHand, "summed over depots")
	assert.True(t, g.Rows[0].BelowMin)
	assert.True(t, g.Rows[0].Negative, "negative in any depot")
	assert.Nil(t, g.Rows[0].MinQty, "min needs a depot")
	assert.Equal(t, "10", *g.Rows[0].ListPrice)
	require.Len(t, g.Rows[0].Codes, 1)
	assert.Equal(t, "2", g.Totals["onHand"])
	assert.EqualValues(t, 3, g.UnfilteredCount)

	g = f.partGrid(t, `{"startRow":0,"endRow":50,"depotId":`+idStr(f.depot)+`,"catalogId":`+idStr(f.catalog)+`}`)
	require.Len(t, g.Rows, 2)
	assert.Equal(t, "5", g.Rows[0].OnHand)
	assert.Equal(t, "10", *g.Rows[0].MinQty)
	assert.Equal(t, "A-01", *g.Rows[0].Shelf)
	assert.False(t, g.Rows[0].Negative)

	g = f.partGrid(t, `{"startRow":0,"endRow":50,"depotId":`+idStr(f.depot)+`,"stock":"NEGATIVE"}`)
	require.Len(t, g.Rows, 1)
	assert.Equal(t, "B-1", g.Rows[0].StockNo)
	g = f.partGrid(t, `{"startRow":0,"endRow":50,"stock":"IN_STOCK"}`)
	require.Len(t, g.Rows, 1)
	g = f.partGrid(t, `{"startRow":0,"endRow":50,"includeInactive":true,"filterModel":{"isActive":{"filterType":"text","type":"equals","filter":"false"}}}`)
	require.Len(t, g.Rows, 1)
	assert.False(t, g.Rows[0].IsActive)
	g = f.partGrid(t, `{"startRow":0,"endRow":50,"filterModel":{"codes":{"filterType":"text","type":"contains","filter":"7701x"}}}`)
	require.Len(t, g.Rows, 1)
	assert.Equal(t, a, g.Rows[0].ID)
	g = f.partGrid(t, `{"startRow":0,"endRow":50,"search":"mahle"}`)
	require.Len(t, g.Rows, 1)
	assert.Equal(t, b, g.Rows[0].ID)
	g = f.partGrid(t, `{"startRow":0,"endRow":50,"search":"77.01"}`)
	require.Len(t, g.Rows, 1)
	g = f.partGrid(t, `{"startRow":0,"endRow":50,"filterModel":{"onHand":{"filterType":"number","type":"lessThan","filter":0}},"sortModel":[{"colId":"name","sort":"desc"}]}`)
	require.Len(t, g.Rows, 1)
	g = f.partGrid(t, `{"startRow":1,"endRow":2,"sortModel":[{"colId":"stockNo","sort":"desc"}]}`)
	require.Len(t, g.Rows, 1)
	assert.Equal(t, "A-1", g.Rows[0].StockNo)
	assert.EqualValues(t, 3, g.TotalCount)
	assert.Equal(t, http.StatusBadRequest, f.req(t, http.MethodPost, f.path("/catalog/parts/grid"), f.token, `{"sortModel":[{"colId":"nope"}]}`, nil).Code)

	rec := f.req(t, http.MethodPost, f.path("/catalog/parts/grid"), f.token, `{"depotId":`+idStr(f.depot)+`}`,
		map[string]string{"Accept": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"})
	require.Equal(t, http.StatusOK, rec.Code)
	sheet := sheetXML(t, rec.Body.Bytes())
	assert.Contains(t, sheet, "Alpha twin")
	assert.Contains(t, sheet, "77.01-X")
}

func TestGlobalSearch_IncludesParts(t *testing.T) {
	f := newStock(t)
	f.part(t, "GS-100", "Global", `,"codes":[{"kind":"OEM","code":"GS.9"}]`)
	f.part(t, "OFF-GS", "Inactive", `,"isActive":false`)
	rec := f.do(t, http.MethodGet, f.path("/search?q=gs"), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	res := decode[struct {
		Parts []hitJSON `json:"parts"`
	}](t, rec)
	require.Len(t, res.Parts, 1)
	assert.Equal(t, "GS-100", res.Parts[0].StockNo)
	assert.Len(t, res.Parts[0].Stock, 2)
}
