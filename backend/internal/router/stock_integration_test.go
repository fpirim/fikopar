//go:build integration

package router_test

import (
	"net/http"
	"sync"
	"testing"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type levelJSON struct {
	PartID    uint    `json:"partId"`
	StockNo   string  `json:"stockNo"`
	DepotID   *uint   `json:"depotId"`
	OnHand    string  `json:"onHand"`
	Reserved  string  `json:"reserved"`
	Available string  `json:"available"`
	MinQty    string  `json:"minQty"`
	MaxQty    *string `json:"maxQty"`
	Shelf     string  `json:"shelf"`
	BelowMin  bool    `json:"belowMin"`
	Negative  bool    `json:"negative"`
	Version   int     `json:"version"`
}

type partStockJSON struct {
	PartID uint        `json:"partId"`
	Depots []levelJSON `json:"depots"`
	Total  levelJSON   `json:"total"`
}

type warningJSON struct {
	Code        string `json:"code"`
	PartID      uint   `json:"partId"`
	StockNo     string `json:"stockNo"`
	DepotID     uint   `json:"depotId"`
	OnHandAfter string `json:"onHandAfter"`
}

type movementJSON struct {
	ID            uint    `json:"id"`
	PartID        uint    `json:"partId"`
	StockNo       string  `json:"stockNo"`
	DepotID       uint    `json:"depotId"`
	Kind          string  `json:"kind"`
	Label         string  `json:"label"`
	Qty           string  `json:"qty"`
	Delta         string  `json:"delta"`
	SourceType    string  `json:"sourceType"`
	SourceNo      *string `json:"sourceNo"`
	TransferID    *uint   `json:"transferId"`
	OtherDepotID  *uint   `json:"otherDepotId"`
	RunningOnHand *string `json:"runningOnHand"`
	Date          string  `json:"date"`
}

type transferJSON struct {
	ID          uint   `json:"id"`
	TransferNo  string `json:"transferNo"`
	FromDepotID uint   `json:"fromDepotId"`
	ToDepotID   uint   `json:"toDepotId"`
	Date        string `json:"date"`
	Lines       []struct {
		PartID uint   `json:"partId"`
		Qty    string `json:"qty"`
	} `json:"lines"`
	CreatedBy *string        `json:"createdBy"`
	Movements []movementJSON `json:"movements"`
	Warnings  []warningJSON  `json:"warnings"`
}

func (f *stockFix) partStock(t *testing.T, part uint) partStockJSON {
	t.Helper()
	rec := f.do(t, http.MethodGet, f.path("/catalog/parts/"+idStr(part)+"/stock"), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[partStockJSON](t, rec)
}

func TestStock_PartStockHasEveryDepotAndSettingsUpsert(t *testing.T) {
	f := newStock(t)
	p := f.part(t, "P-1", "P", "")
	f.move(t, p, f.depot, "IN", "5", "2026-03-01")
	s := f.partStock(t, p)
	require.Len(t, s.Depots, 2)
	assert.Equal(t, "5", s.Depots[0].OnHand)
	assert.Equal(t, "5", s.Depots[0].Available)
	assert.Equal(t, "0", s.Depots[1].OnHand)
	assert.Equal(t, 0, s.Depots[1].Version)
	assert.Nil(t, s.Total.DepotID)
	assert.Equal(t, "5", s.Total.OnHand)

	path := f.path("/catalog/parts/" + idStr(p) + "/depots/" + idStr(f.depot))
	rec := f.req(t, http.MethodPut, path, f.token, `{"minQty":"8","maxQty":"20","shelf":"A-04-2"}`, nil)
	assert.Equal(t, http.StatusPreconditionRequired, rec.Code)
	rec = f.req(t, http.MethodPut, path, f.token, `{"minQty":"8","maxQty":"2"}`, map[string]string{"If-Match": `"0"`})
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_INVALID_REQUEST", errCode(t, rec))
	rec = f.req(t, http.MethodPut, path, f.token, `{"minQty":"8","maxQty":"20","shelf":"A-04-2"}`, map[string]string{"If-Match": `"0"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Equal(t, `"1"`, rec.Header().Get("ETag"))
	l := decode[levelJSON](t, rec)
	assert.True(t, l.BelowMin)
	assert.Equal(t, "A-04-2", l.Shelf)
	rec = f.req(t, http.MethodPut, path, f.token, `{"minQty":"1"}`, map[string]string{"If-Match": `"0"`})
	assert.Equal(t, http.StatusPreconditionFailed, rec.Code)
	rec = f.req(t, http.MethodPut, path, f.token, `{"minQty":"1","shelf":"B"}`, map[string]string{"If-Match": `"1"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.False(t, decode[levelJSON](t, rec).BelowMin)
	assert.Nil(t, decode[levelJSON](t, rec).MaxQty)

	assert.Equal(t, http.StatusNotFound, f.req(t, http.MethodPut, f.path("/catalog/parts/"+idStr(p)+"/depots/999999"), f.token, `{}`,
		map[string]string{"If-Match": `"0"`}).Code)
	assert.Equal(t, http.StatusNotFound, f.do(t, http.MethodGet, f.path("/catalog/parts/999999/stock"), f.token, "").Code)
}

func TestStock_LevelsBelowMinAndNegative(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "A", "A", "")
	b := f.part(t, "B", "B", "")
	c := f.part(t, "C", "C", "")
	f.move(t, a, f.depot, "IN", "2", "2026-03-01")
	f.move(t, b, f.depot, "OUT", "3", "2026-03-01")
	f.move(t, c, f.depot, "OUT", "1", "2026-03-01")
	f.move(t, c, f.depot2, "IN", "9", "2026-03-01")
	require.Equal(t, http.StatusOK, f.req(t, http.MethodPut, f.path("/catalog/parts/"+idStr(a)+"/depots/"+idStr(f.depot)), f.token,
		`{"minQty":"5"}`, map[string]string{"If-Match": `"0"`}).Code)

	type page struct {
		Items      []levelJSON `json:"items"`
		NextCursor *string     `json:"nextCursor"`
	}
	all := decode[page](t, f.do(t, http.MethodGet, f.path("/stock/levels"), f.token, ""))
	assert.Len(t, all.Items, 4)
	neg := decode[page](t, f.do(t, http.MethodGet, f.path("/stock/levels?only=NEGATIVE"), f.token, ""))
	require.Len(t, neg.Items, 2)
	assert.Equal(t, "-3", neg.Items[0].OnHand)
	assert.True(t, neg.Items[0].Negative)
	below := decode[page](t, f.do(t, http.MethodGet, f.path("/stock/levels?only=BELOW_MIN"), f.token, ""))
	require.Len(t, below.Items, 1)
	assert.Equal(t, a, below.Items[0].PartID)
	either := decode[page](t, f.do(t, http.MethodGet, f.path("/stock/levels?only=BELOW_MIN_OR_NEGATIVE&limit=2"), f.token, ""))
	require.Len(t, either.Items, 2)
	assert.True(t, either.Items[0].Negative)
	assert.True(t, either.Items[1].Negative)
	require.NotNil(t, either.NextCursor)
	rest := decode[page](t, f.do(t, http.MethodGet, f.path("/stock/levels?only=BELOW_MIN_OR_NEGATIVE&limit=2&cursor="+*either.NextCursor), f.token, ""))
	require.Len(t, rest.Items, 1)
	assert.Equal(t, a, rest.Items[0].PartID)
	assert.Nil(t, rest.NextCursor)
	d2 := decode[page](t, f.do(t, http.MethodGet, f.path("/stock/levels?depotId="+idStr(f.depot2)), f.token, ""))
	require.Len(t, d2.Items, 1)
	assert.Equal(t, "9", d2.Items[0].OnHand)
	assert.Equal(t, http.StatusBadRequest, f.do(t, http.MethodGet, f.path("/stock/levels?only=X"), f.token, "").Code)
}

func TestTransfers_MoveBothSidesAtomicallyWithWarnings(t *testing.T) {
	f := newStock(t)
	rec := &recorder{}
	f.app.Relay.Register(rec)
	a := f.part(t, "A", "A", "")
	b := f.part(t, "B", "B", "")
	f.move(t, a, f.depot, "IN", "10", "2026-03-01")
	body := `{"fromDepotId":` + idStr(f.depot) + `,"toDepotId":` + idStr(f.depot2) + `,"date":"2026-03-05","note":"n",
		"lines":[{"partId":` + idStr(a) + `,"qty":"4"},{"partId":` + idStr(b) + `,"qty":"1.5"}]}`
	res := f.post(t, f.path("/stock/transfers"), f.token, body)
	require.Equal(t, http.StatusCreated, res.Code, res.Body.String())
	tr := decode[transferJSON](t, res)
	assert.Equal(t, "TRF-2026-1-000001", tr.TransferNo)
	assert.Len(t, tr.Movements, 4)
	require.Len(t, tr.Warnings, 1)
	assert.Equal(t, warningJSON{Code: "NEGATIVE_STOCK", PartID: b, StockNo: "B", DepotID: f.depot, OnHandAfter: "-1.5"}, tr.Warnings[0])
	assert.Equal(t, "6", f.onHand(t, a, f.depot))
	assert.Equal(t, "4", f.onHand(t, a, f.depot2))
	assert.Equal(t, "1.5", f.onHand(t, b, f.depot2))
	assert.Equal(t, "clerk", *tr.CreatedBy)

	got := decode[transferJSON](t, f.do(t, http.MethodGet, f.path("/stock/transfers/"+idStr(tr.ID)), f.token, ""))
	assert.Len(t, got.Movements, 4)
	assert.Empty(t, got.Warnings)
	require.Len(t, got.Lines, 2)
	assert.Equal(t, http.StatusNotFound, f.do(t, http.MethodGet, f.path("/stock/transfers/999999"), f.token, "").Code)

	f.relay(t)
	moved := rec.byType(t, "StockMoved")
	require.Len(t, moved, 4)
	for _, e := range moved {
		validateEvent(t, e)
	}

	inactive := f.part(t, "I", "I", `,"isActive":false`)
	for name, tc := range map[string]struct {
		body, code string
		status     int
	}{
		"same depot": {`{"fromDepotId":` + idStr(f.depot) + `,"toDepotId":` + idStr(f.depot) + `,"date":"2026-03-05","lines":[{"partId":` + idStr(a) + `,"qty":"1"}]}`, "ERR_SAME_DEPOT", 422},
		"unknown depot": {`{"fromDepotId":` + idStr(f.depot) + `,"toDepotId":999999,"date":"2026-03-05","lines":[{"partId":` + idStr(a) + `,"qty":"1"}]}`, "ERR_DEPOT_NOT_FOUND", 422},
		"unknown part":  {`{"fromDepotId":` + idStr(f.depot) + `,"toDepotId":` + idStr(f.depot2) + `,"date":"2026-03-05","lines":[{"partId":999999,"qty":"1"}]}`, "ERR_PART_NOT_FOUND", 422},
		"inactive part": {`{"fromDepotId":` + idStr(f.depot) + `,"toDepotId":` + idStr(f.depot2) + `,"date":"2026-03-05","lines":[{"partId":` + idStr(inactive) + `,"qty":"1"}]}`, "ERR_PART_INACTIVE", 422},
		"duplicate":     {`{"fromDepotId":` + idStr(f.depot) + `,"toDepotId":` + idStr(f.depot2) + `,"date":"2026-03-05","lines":[{"partId":` + idStr(a) + `,"qty":"1"},{"partId":` + idStr(a) + `,"qty":"2"}]}`, "ERR_DUPLICATE_LINE", 422},
		"zero qty":      {`{"fromDepotId":` + idStr(f.depot) + `,"toDepotId":` + idStr(f.depot2) + `,"date":"2026-03-05","lines":[{"partId":` + idStr(a) + `,"qty":"0"}]}`, "ERR_INVALID_REQUEST", 400},
		"no lines":      {`{"fromDepotId":` + idStr(f.depot) + `,"toDepotId":` + idStr(f.depot2) + `,"date":"2026-03-05","lines":[]}`, "ERR_INVALID_REQUEST", 400},
	} {
		r := f.post(t, f.path("/stock/transfers"), f.token, tc.body)
		assert.Equal(t, tc.status, r.Code, name+": "+r.Body.String())
		assert.Equal(t, tc.code, errCode(t, r), name)
	}
	assert.Equal(t, "6", f.onHand(t, a, f.depot), "failed transfers move nothing")
}

func TestTransfers_ClosedYearRejected(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "A", "A", "")
	require.NoError(t, testDB.Exec(`INSERT INTO carry_forward_runs (org_id, year, entry_count, total_debit, total_credit) VALUES (?, 2026, 0, 0, 0)`, f.org).Error)
	r := f.post(t, f.path("/stock/transfers"), f.token, `{"fromDepotId":`+idStr(f.depot)+`,"toDepotId":`+idStr(f.depot2)+`,"date":"2025-12-31","lines":[{"partId":`+idStr(a)+`,"qty":"1"}]}`)
	assert.Equal(t, http.StatusConflict, r.Code)
	assert.Equal(t, "ERR_YEAR_CLOSED", errCode(t, r))
}

// Opposite transfers of the same parts race; none deadlocks or loses a movement.
func TestTransfers_ParallelOnSamePartsKeepLevelsExact(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "A", "A", "")
	b := f.part(t, "B", "B", "")
	var wg sync.WaitGroup
	codes := make([]int, 20)
	for i := range codes {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			from, to := f.depot, f.depot2
			if i%2 == 1 {
				from, to = to, from
			}
			body := `{"fromDepotId":` + idStr(from) + `,"toDepotId":` + idStr(to) + `,"date":"2026-04-01",
				"lines":[{"partId":` + idStr(a) + `,"qty":"1"},{"partId":` + idStr(b) + `,"qty":"2"}]}`
			codes[i] = f.req(t, http.MethodPost, f.path("/stock/transfers"), f.token, body, map[string]string{"Idempotency-Key": uuid.NewString()}).Code
		}(i)
	}
	wg.Wait()
	for _, c := range codes {
		assert.Equal(t, http.StatusCreated, c)
	}
	assert.Equal(t, "0", f.onHand(t, a, f.depot))
	assert.Equal(t, "0", f.onHand(t, b, f.depot2))
	var n, drift int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM stock_movements WHERE org_id = ?`, f.org).Scan(&n).Error)
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM stock_level_drift`).Scan(&drift).Error)
	assert.EqualValues(t, 80, n)
	assert.Zero(t, drift)
	var numbers int64
	require.NoError(t, testDB.Raw(`SELECT count(DISTINCT transfer_no) FROM stock_transfers WHERE org_id = ?`, f.org).Scan(&numbers).Error)
	assert.EqualValues(t, 20, numbers)
}

type movementGrid struct {
	Rows          []movementJSON    `json:"rows"`
	TotalCount    int64             `json:"totalCount"`
	Totals        map[string]string `json:"totals"`
	OpeningOnHand *string           `json:"openingOnHand"`
	ClosingOnHand *string           `json:"closingOnHand"`
}

func TestMovements_GridRunningOnHandAndOpeningClosing(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "A", "Alpha", "")
	b := f.part(t, "B", "Beta", "")
	f.move(t, a, f.depot, "IN", "10", "2026-01-10")
	f.move(t, a, f.depot, "OUT", "3", "2026-02-10")
	f.move(t, a, f.depot2, "IN", "1", "2026-02-11")
	f.move(t, a, f.depot, "OUT", "20", "2026-03-10")
	f.move(t, b, f.depot, "IN", "7", "2026-02-15")
	grid := func(body string) movementGrid {
		rec := f.req(t, http.MethodPost, f.path("/stock/movements/grid"), f.token, body, nil)
		require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
		return decode[movementGrid](t, rec)
	}

	g := grid(`{"startRow":0,"endRow":50,"partId":` + idStr(a) + `,"depotId":` + idStr(f.depot) + `,"sortModel":[{"colId":"date","sort":"asc"}]}`)
	require.Len(t, g.Rows, 3)
	assert.Equal(t, []string{"10", "7", "-13"}, []string{*g.Rows[0].RunningOnHand, *g.Rows[1].RunningOnHand, *g.Rows[2].RunningOnHand})
	assert.Equal(t, "-3", g.Rows[1].Delta)
	assert.Equal(t, "OPENING", g.Rows[0].Label)
	assert.Equal(t, "10", g.Totals["qtyIn"])
	assert.Equal(t, "23", g.Totals["qtyOut"])

	g = grid(`{"startRow":0,"endRow":50,"partId":` + idStr(a) + `,"depotId":null,
		"filterModel":{"date":{"filterType":"date","type":"inRange","dateFrom":"2026-02-01 00:00:00","dateTo":"2026-02-28"}}}`)
	require.Len(t, g.Rows, 2)
	assert.Equal(t, "2026-02-11", g.Rows[0].Date, "default sort date desc")
	assert.Equal(t, "8", *g.Rows[0].RunningOnHand, "all depots")
	assert.Equal(t, "10", *g.OpeningOnHand)
	assert.Equal(t, "8", *g.ClosingOnHand)

	g = grid(`{"startRow":0,"endRow":50,"sortModel":[{"colId":"qty","sort":"desc"}]}`)
	assert.Len(t, g.Rows, 5)
	assert.Nil(t, g.Rows[0].RunningOnHand, "no running on hand without part and date sort")
	assert.Nil(t, g.OpeningOnHand)
	g = grid(`{"startRow":0,"endRow":2,"filterModel":{"name":{"filterType":"text","type":"contains","filter":"bet"}}}`)
	assert.EqualValues(t, 1, g.TotalCount)
	assert.Equal(t, "B", g.Rows[0].StockNo)

	rec := f.req(t, http.MethodPost, f.path("/stock/movements/grid"), f.token, `{"partId":`+idStr(a)+`}`,
		map[string]string{"Accept": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"})
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Contains(t, sheetXML(t, rec.Body.Bytes()), "Alpha")
}
