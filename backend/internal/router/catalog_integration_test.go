//go:build integration

package router_test

import (
	"net/http"
	"sync"
	"testing"
	"time"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type catalogJSON struct {
	ID              uint    `json:"id"`
	Code            int     `json:"code"`
	Name            string  `json:"name"`
	SupplierSicilID *uint   `json:"supplierSicilId"`
	SupplierName    *string `json:"supplierName"`
	IsActive        bool    `json:"isActive"`
	Currency        string  `json:"currency"`
	PartCount       int     `json:"partCount"`
	ContentVersion  int64   `json:"contentVersion"`
	Version         int     `json:"version"`
}

type codeJSON struct {
	Kind             string `json:"kind"`
	Code             string `json:"code"`
	NormalizedCode   string `json:"normalizedCode"`
	EquivalentPartID *uint  `json:"equivalentPartId"`
}

type partJSON struct {
	ID                 uint       `json:"id"`
	OrgID              uint       `json:"orgId"`
	CatalogID          uint       `json:"catalogId"`
	CatalogCode        int        `json:"catalogCode"`
	CatalogName        string     `json:"catalogName"`
	StockNo            string     `json:"stockNo"`
	Name               string     `json:"name"`
	Brand              *string    `json:"brand"`
	Unit               string     `json:"unit"`
	PackageQty         int        `json:"packageQty"`
	VatPct             string     `json:"vatPct"`
	IsActive           bool       `json:"isActive"`
	Codes              []codeJSON `json:"codes"`
	ListPrice          *string    `json:"listPrice"`
	ListPriceValidFrom *string    `json:"listPriceValidFrom"`
	NextPrice          *struct {
		Price     string `json:"price"`
		ValidFrom string `json:"validFrom"`
	} `json:"nextPrice"`
	OnHand  string `json:"onHand"`
	Version int    `json:"version"`
}

type priceJSON struct {
	ID            uint    `json:"id"`
	PartID        uint    `json:"partId"`
	CatalogID     uint    `json:"catalogId"`
	Price         *string `json:"price"`
	ValidFrom     *string `json:"validFrom"`
	ValidTo       *string `json:"validTo"`
	PreviousPrice *string `json:"previousPrice"`
	ChangePct     *string `json:"changePct"`
	IsCurrent     bool    `json:"isCurrent"`
	Source        *string `json:"source"`
	CreatedBy     *string `json:"createdBy"`
}

func day(offset int) string { return time.Now().AddDate(0, 0, offset).Format("2006-01-02") }

func TestCatalogs_CreateListUpdate(t *testing.T) {
	f := newStock(t)
	supplier := f.sicil(t, "S1", "FIRMA")
	rec := f.post(t, f.path("/catalog/catalogs"), f.token, `{"code":20,"name":"Mahle","supplierSicilId":`+idStr(supplier)+`}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	assert.Equal(t, `"1"`, rec.Header().Get("ETag"))
	c := decode[catalogJSON](t, rec)
	assert.Equal(t, "Card S1", *c.SupplierName)
	assert.Equal(t, "TRY", c.Currency)
	assert.True(t, c.IsActive)

	rec = f.post(t, f.path("/catalog/catalogs"), f.token, `{"code":20,"name":"Again"}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_CATALOG_CODE_EXISTS", errCode(t, rec))
	rec = f.post(t, f.path("/catalog/catalogs"), f.token, `{"code":21,"name":"X","supplierSicilId":999999}`)
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_SICIL_NOT_FOUND", errCode(t, rec))
	rec = f.post(t, f.path("/catalog/catalogs"), f.token, `{"code":0,"name":"X"}`)
	assert.Equal(t, http.StatusBadRequest, rec.Code)

	path := f.path("/catalog/catalogs/" + idStr(c.ID))
	rec = f.req(t, http.MethodPut, path, f.token, `{"code":20,"name":"Mahle 2","isActive":false}`, nil)
	assert.Equal(t, http.StatusPreconditionRequired, rec.Code)
	rec = f.req(t, http.MethodPut, path, f.token, `{"code":21,"name":"Mahle 2","isActive":false}`, map[string]string{"If-Match": `"1"`})
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_CATALOG_CODE_IMMUTABLE", errCode(t, rec))
	rec = f.req(t, http.MethodPut, path, f.token, `{"code":20,"name":"Mahle 2","isActive":false}`, map[string]string{"If-Match": `"1"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Equal(t, "Mahle 2", decode[catalogJSON](t, rec).Name)
	assert.Nil(t, decode[catalogJSON](t, rec).SupplierSicilID)
	rec = f.req(t, http.MethodPut, path, f.token, `{"code":20,"name":"Stale"}`, map[string]string{"If-Match": `"1"`})
	assert.Equal(t, http.StatusPreconditionFailed, rec.Code)

	list := decode[[]catalogJSON](t, f.do(t, http.MethodGet, f.path("/catalog/catalogs"), f.token, ""))
	require.Len(t, list, 1)
	assert.Equal(t, 10, list[0].Code)
	list = decode[[]catalogJSON](t, f.do(t, http.MethodGet, f.path("/catalog/catalogs?includeInactive=true"), f.token, ""))
	assert.Len(t, list, 2)
	assert.Equal(t, http.StatusNotFound, f.do(t, http.MethodGet, f.path("/catalog/catalogs/999999"), f.token, "").Code)
}

func TestParts_CreateGetUpdateDeactivate(t *testing.T) {
	f := newStock(t)
	other := f.part(t, "OTHER-1", "Other", "")
	rec := f.post(t, f.path("/catalog/parts"), f.token, `{"catalogId":`+idStr(f.catalog)+`,"stockNo":"0 451 103 079","name":"Yağ filtresi",
		"brand":"Bosch","vatPct":"20","packageQty":2,
		"codes":[{"kind":"OEM","code":"04.123-45"},{"kind":"ALT","code":"OLD-9"},{"kind":"EQUIVALENT","code":"OTHER-1","equivalentPartId":`+idStr(other)+`}],
		"initialPrice":{"price":"12.5","validFrom":"`+day(-10)+`"}}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	p := decode[partJSON](t, rec)
	assert.Equal(t, `"1"`, rec.Header().Get("ETag"))
	assert.Equal(t, 10, p.CatalogCode)
	assert.Equal(t, "PCS", p.Unit)
	assert.Equal(t, "20", p.VatPct)
	assert.Equal(t, "12.5", *p.ListPrice)
	assert.Equal(t, "0", p.OnHand)
	require.Len(t, p.Codes, 3)
	assert.Equal(t, "0412345", p.Codes[0].NormalizedCode)

	rec = f.post(t, f.path("/catalog/parts"), f.token, `{"catalogId":`+idStr(f.catalog)+`,"stockNo":"0 451 103 079","name":"Dup"}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_PART_CODE_EXISTS", errCode(t, rec))
	other2 := f.newCatalog(t, 11, "Other")
	f.partIn(t, other2, "0 451 103 079", "Same no, other catalog", "")
	rec = f.post(t, f.path("/catalog/parts"), f.token, `{"catalogId":999999,"stockNo":"X","name":"x"}`)
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_CATALOG_NOT_FOUND", errCode(t, rec))
	rec = f.post(t, f.path("/catalog/parts"), f.token, `{"catalogId":`+idStr(f.catalog)+`,"stockNo":"Y","name":"y","codes":[{"kind":"EQUIVALENT","code":"Z","equivalentPartId":999999}]}`)
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_PART_NOT_FOUND", errCode(t, rec))
	rec = f.post(t, f.path("/catalog/parts"), f.token, `{"catalogId":`+idStr(f.catalog)+`,"stockNo":"THIS-IS-TOO-LONG-1","name":"y"}`)
	assert.Equal(t, http.StatusBadRequest, rec.Code)

	path := f.path("/catalog/parts/" + idStr(p.ID))
	got := decode[partJSON](t, f.do(t, http.MethodGet, path, f.token, ""))
	assert.Equal(t, p.Name, got.Name)

	rec = f.req(t, http.MethodPut, path, f.token, `{"catalogId":`+idStr(other2)+`,"stockNo":"X","name":"x"}`, map[string]string{"If-Match": `"1"`})
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_PART_CATALOG_IMMUTABLE", errCode(t, rec))
	rec = f.req(t, http.MethodPut, path, f.token, `{"catalogId":`+idStr(f.catalog)+`,"stockNo":"OTHER-1","name":"x"}`, map[string]string{"If-Match": `"1"`})
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_PART_CODE_EXISTS", errCode(t, rec))
	rec = f.req(t, http.MethodPut, path, f.token, `{"catalogId":`+idStr(f.catalog)+`,"stockNo":"0 451 103 079","name":"Oil filter","isActive":false,
		"codes":[{"kind":"MANUFACTURER","code":"F 026"}],"initialPrice":{"price":"99","validFrom":"2020-01-01"}}`, map[string]string{"If-Match": `"1"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	u := decode[partJSON](t, rec)
	assert.False(t, u.IsActive)
	assert.Equal(t, 2, u.Version)
	require.Len(t, u.Codes, 1)
	assert.Equal(t, "12.5", *u.ListPrice, "initialPrice is ignored on update")
	rec = f.req(t, http.MethodPut, path, f.token, `{"catalogId":`+idStr(f.catalog)+`,"stockNo":"S","name":"x"}`, map[string]string{"If-Match": `"1"`})
	assert.Equal(t, http.StatusPreconditionFailed, rec.Code)
	assert.Equal(t, http.StatusNotFound, f.do(t, http.MethodGet, f.path("/catalog/parts/999999"), f.token, "").Code)
}

func TestParts_LookupMatchesNormalizedCodesWithEquivalents(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "A-1", "A", `,"codes":[{"kind":"OEM","code":"04.123-45"}]`)
	b := f.part(t, "B-1", "B", `,"codes":[{"kind":"EQUIVALENT","code":"A-1","equivalentPartId":`+idStr(a)+`}]`)
	hits := decode[[]partJSON](t, f.do(t, http.MethodGet, f.path("/catalog/parts/lookup?code=0412345"), f.token, ""))
	require.Len(t, hits, 1)
	assert.Equal(t, a, hits[0].ID)
	hits = decode[[]partJSON](t, f.do(t, http.MethodGet, f.path("/catalog/parts/lookup?code=b1"), f.token, ""))
	require.Len(t, hits, 1)
	assert.Equal(t, b, hits[0].ID)
	assert.Equal(t, http.StatusBadRequest, f.do(t, http.MethodGet, f.path("/catalog/parts/lookup?code=x"), f.token, "").Code)
}

func TestPrices_HistoryFutureAndConflicts(t *testing.T) {
	f := newStock(t)
	p := f.part(t, "P-1", "P", `,"initialPrice":{"price":"10","validFrom":"`+day(-30)+`"}`)
	path := f.path("/catalog/parts/" + idStr(p) + "/prices")

	rec2 := f.post(t, path, f.token, `{"price":"10.69","validFrom":"`+day(-1)+`"}`)
	require.Equal(t, http.StatusCreated, rec2.Code, rec2.Body.String())
	rec2 = f.post(t, path, f.token, `{"price":"12","validFrom":"`+day(30)+`"}`)
	require.Equal(t, http.StatusCreated, rec2.Code, rec2.Body.String())
	rec2 = f.post(t, path, f.token, `{"price":"12","validFrom":"`+day(30)+`"}`)
	assert.Equal(t, http.StatusOK, rec2.Code, "same price on the same date is a no-op")
	rec2 = f.post(t, path, f.token, `{"price":"13","validFrom":"`+day(30)+`"}`)
	assert.Equal(t, http.StatusConflict, rec2.Code)
	assert.Equal(t, "ERR_PRICE_EXISTS", errCode(t, rec2))
	rec2 = f.post(t, path, f.token, `{"price":"-1","validFrom":"`+day(1)+`"}`)
	assert.Equal(t, http.StatusBadRequest, rec2.Code)

	hist := decode[[]priceJSON](t, f.do(t, http.MethodGet, path, f.token, ""))
	require.Len(t, hist, 3)
	assert.Equal(t, "12", *hist[0].Price)
	assert.False(t, hist[0].IsCurrent)
	assert.Nil(t, hist[0].ValidTo)
	assert.Equal(t, "10.69", *hist[1].Price)
	assert.True(t, hist[1].IsCurrent)
	assert.Equal(t, day(29), *hist[1].ValidTo)
	assert.Equal(t, "6.9", *hist[1].ChangePct)
	assert.Equal(t, "MANUAL", *hist[1].Source)
	assert.Equal(t, "clerk", *hist[1].CreatedBy)
	assert.Equal(t, day(-2), *hist[2].ValidTo)

	part := decode[partJSON](t, f.do(t, http.MethodGet, f.path("/catalog/parts/"+idStr(p)), f.token, ""))
	assert.Equal(t, "10.69", *part.ListPrice)
	require.NotNil(t, part.NextPrice)
	assert.Equal(t, "12", part.NextPrice.Price)
	assert.Equal(t, day(30), part.NextPrice.ValidFrom)
	assert.Equal(t, http.StatusNotFound, f.post(t, f.path("/catalog/parts/999999/prices"), f.token, `{"price":"1","validFrom":"2026-01-01"}`).Code)
}

func TestPrices_EmitPriceChanged(t *testing.T) {
	f := newStock(t)
	rec := &recorder{}
	f.app.Relay.Register(rec)
	p := f.part(t, "P-1", "P", `,"initialPrice":{"price":"10","validFrom":"2026-01-01"}`)
	require.Equal(t, http.StatusCreated, f.post(t, f.path("/catalog/parts/"+idStr(p)+"/prices"), f.token, `{"price":"11","validFrom":"2026-02-01"}`).Code)
	f.relay(t)
	events := rec.byType(t, "PriceChanged")
	require.Len(t, events, 2)
	for _, e := range events {
		validateEvent(t, e)
	}
	last := decodeBytes[struct {
		DepotID *uint `json:"depotId"`
		Payload struct {
			Price         string  `json:"price"`
			PreviousPrice *string `json:"previousPrice"`
		} `json:"payload"`
	}](t, events[1])
	assert.Nil(t, last.DepotID)
	assert.Equal(t, "11", last.Payload.Price)
	assert.Equal(t, "10", *last.Payload.PreviousPrice)
}

func TestPrices_SetPricesAllOrNothing(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "A", "A", "")
	b := f.part(t, "B", "B", `,"initialPrice":{"price":"5","validFrom":"2026-03-01"}`)
	foreign := f.partIn(t, f.newCatalog(t, 11, "X"), "C", "C", "")
	path := f.path("/catalog/catalogs/" + idStr(f.catalog) + "/prices")

	rec := f.post(t, path, f.token, `{"validFrom":"2026-03-01","items":[{"partId":`+idStr(a)+`,"price":"1"},{"partId":`+idStr(foreign)+`,"price":"2"}]}`)
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_PART_NOT_IN_CATALOG", errCode(t, rec))
	rec = f.post(t, path, f.token, `{"validFrom":"2026-03-01","items":[{"partId":`+idStr(a)+`,"price":"1"},{"partId":`+idStr(b)+`,"price":"6"}]}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_PRICE_EXISTS", errCode(t, rec))
	assert.Empty(t, decode[[]priceJSON](t, f.do(t, http.MethodGet, f.path("/catalog/parts/"+idStr(a)+"/prices"), f.token, "")))

	rec = f.post(t, path, f.token, `{"validFrom":"2026-03-01","items":[{"partId":`+idStr(a)+`,"price":"1"},{"partId":`+idStr(b)+`,"price":"5"}]}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	assert.Len(t, decode[[]priceJSON](t, rec), 1, "equal price is a no-op")

	page := decode[struct {
		Items      []priceJSON `json:"items"`
		NextCursor *string     `json:"nextCursor"`
	}](t, f.do(t, http.MethodGet, path+"?validOn=2026-03-15&limit=1", f.token, ""))
	require.Len(t, page.Items, 1)
	assert.Equal(t, "1", *page.Items[0].Price)
	require.NotNil(t, page.NextCursor)
	page2 := decode[struct {
		Items []priceJSON `json:"items"`
	}](t, f.do(t, http.MethodGet, path+"?validOn=2026-02-01&cursor="+*page.NextCursor, f.token, ""))
	require.Len(t, page2.Items, 1)
	assert.Nil(t, page2.Items[0].Price)
}

// Two clerks set a different price for the same part and day at once: one wins, the other gets ERR_PRICE_EXISTS.
func TestPrices_ParallelSetSameDateOneWins(t *testing.T) {
	f := newStock(t)
	p := f.part(t, "P", "P", "")
	path := f.path("/catalog/parts/" + idStr(p) + "/prices")
	codes := make([]int, 6)
	var wg sync.WaitGroup
	for i := range codes {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			rec := f.req(t, http.MethodPost, path, f.token, `{"price":"`+idStr(uint(i+1))+`","validFrom":"2026-05-01"}`,
				map[string]string{"Idempotency-Key": uuid.NewString()})
			codes[i] = rec.Code
		}(i)
	}
	wg.Wait()
	created, conflicts := 0, 0
	for _, c := range codes {
		switch c {
		case http.StatusCreated:
			created++
		case http.StatusConflict:
			conflicts++
		}
	}
	assert.Equal(t, 1, created, "%v", codes)
	assert.Equal(t, 5, conflicts, "%v", codes)
	var n int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM prices WHERE part_id = ?`, p).Scan(&n).Error)
	assert.EqualValues(t, 1, n)
}
