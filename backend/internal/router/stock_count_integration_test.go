//go:build integration

package router_test

import (
	"net/http"
	"strconv"
	"strings"
	"sync"
	"testing"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type countJSON struct {
	ID              uint    `json:"id"`
	DepotID         uint    `json:"depotId"`
	CountDate       string  `json:"countDate"`
	ShelfFrom       *string `json:"shelfFrom"`
	ShelfTo         *string `json:"shelfTo"`
	CountNo         string  `json:"countNo"`
	Year            int     `json:"year"`
	Status          string  `json:"status"`
	StartedAt       string  `json:"startedAt"`
	LineCount       int     `json:"lineCount"`
	CountedCount    int     `json:"countedCount"`
	DifferenceCount int     `json:"differenceCount"`
	DifferenceValue string  `json:"differenceValue"`
	ValueIn         string  `json:"valueIn"`
	ValueOut        string  `json:"valueOut"`
	UnvaluedLines   int     `json:"unvaluedLines"`
	Uncounted       *string `json:"uncounted"`
	CreatedBy       *string `json:"createdBy"`
	CancelReason    *string `json:"cancelReason"`
	Version         int     `json:"version"`
}

type countLineJSON struct {
	ID              uint    `json:"id"`
	PartID          uint    `json:"partId"`
	StockNo         string  `json:"stockNo"`
	Shelf           string  `json:"shelf"`
	SystemQty       string  `json:"systemQty"`
	CountedQty      *string `json:"countedQty"`
	Difference      *string `json:"difference"`
	MovedSinceOpen  string  `json:"movedSinceOpen"`
	OnHandAfterPost *string `json:"onHandAfterPost"`
	Added           bool    `json:"added"`
	UnitCost        *string `json:"unitCost"`
	CostSource      string  `json:"costSource"`
	Value           *string `json:"value"`
	CountedBy       *string `json:"countedBy"`
	Version         int     `json:"version"`
}

type countLinesPage struct {
	Rows       []countLineJSON   `json:"rows"`
	TotalCount int64             `json:"totalCount"`
	Totals     map[string]string `json:"totals"`
}

type countFinalizedJSON struct {
	Count         countJSON `json:"count"`
	MovementCount int       `json:"movementCount"`
	Warnings      []struct {
		Code        string `json:"code"`
		PartID      uint   `json:"partId"`
		DepotID     uint   `json:"depotId"`
		OnHandAfter string `json:"onHandAfter"`
	} `json:"warnings"`
}

func (f *stockFix) countPath(id uint, rest string) string {
	return f.path("/stock/counts/" + idStr(id) + rest)
}

func (f *stockFix) openCount(t *testing.T, depot uint, extra string) countJSON {
	t.Helper()
	rec := f.post(t, f.path("/stock/counts"), f.token, `{"depotId":`+idStr(depot)+`,"countDate":"2026-06-30"`+extra+`}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	c := decode[countJSON](t, rec)
	assert.Equal(t, `"`+strconv.Itoa(c.Version)+`"`, rec.Header().Get("ETag"))
	return c
}

func (f *stockFix) countLines(t *testing.T, id uint, body string) countLinesPage {
	t.Helper()
	rec := f.req(t, http.MethodPost, f.countPath(id, "/lines/grid"), f.token, body, nil)
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[countLinesPage](t, rec)
}

func (f *stockFix) lineOf(t *testing.T, id, part uint) countLineJSON {
	t.Helper()
	for _, l := range f.countLines(t, id, `{}`).Rows {
		if l.PartID == part {
			return l
		}
	}
	t.Fatalf("no line for part %d", part)
	return countLineJSON{}
}

func (f *stockFix) setLine(t *testing.T, count uint, line countLineJSON, qty string) countLineJSON {
	t.Helper()
	rec := f.req(t, http.MethodPatch, f.countPath(count, "/lines/"+idStr(line.ID)), f.token, `{"countedQty":`+qty+`}`,
		map[string]string{"If-Match": `"` + strconv.Itoa(line.Version) + `"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	out := decode[countLineJSON](t, rec)
	assert.Equal(t, `"`+strconv.Itoa(out.Version)+`"`, rec.Header().Get("ETag"))
	return out
}

func (f *stockFix) finalize(t *testing.T, c countJSON, uncounted string) countFinalizedJSON {
	t.Helper()
	rec := f.req(t, http.MethodPost, f.countPath(c.ID, "/finalize"), f.token, `{"uncounted":"`+uncounted+`"}`,
		map[string]string{"Idempotency-Key": uuid.NewString(), "If-Match": `"` + strconv.Itoa(c.Version) + `"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	out := decode[countFinalizedJSON](t, rec)
	assert.Equal(t, `"`+strconv.Itoa(out.Count.Version)+`"`, rec.Header().Get("ETag"))
	return out
}

func (f *stockFix) shelf(t *testing.T, part, depot uint, shelf string) {
	t.Helper()
	rec := f.req(t, http.MethodPut, f.path("/catalog/parts/"+idStr(part)+"/depots/"+idStr(depot)), f.token, `{"minQty":"0","shelf":"`+shelf+`"}`,
		map[string]string{"If-Match": `"0"`})
	if rec.Code != http.StatusOK { // setPartDepotSettings is built on another branch; seed directly.
		require.NoError(t, testDB.Exec(`INSERT INTO part_depots (org_id, part_id, depot_id, shelf) VALUES (?, ?, ?, ?)
			ON CONFLICT (part_id, depot_id) DO UPDATE SET shelf = excluded.shelf, version = part_depots.version + 1`, f.org, part, depot, shelf).Error)
	}
}

func (f *stockFix) driftCount(t *testing.T) int64 {
	t.Helper()
	var n int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM stock_level_drift`).Scan(&n).Error)
	return n
}

func TestStockCount_OpenFreezesSystemQuantity(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "A", "A", "")
	b := f.part(t, "B", "B", "")
	f.part(t, "C", "C nothing here", "")
	shelfOnly := f.part(t, "D", "D shelf only", "")
	f.move(t, a, f.depot, "IN", "10", "2026-06-01")
	f.move(t, b, f.depot, "IN", "4", "2026-06-01")
	f.move(t, b, f.depot2, "IN", "7", "2026-06-01")
	f.shelf(t, shelfOnly, f.depot, "A-01")

	c := f.openCount(t, f.depot, "")
	assert.Equal(t, "OPEN", c.Status)
	assert.Equal(t, "SAY-2026-1-000001", c.CountNo)
	assert.Equal(t, 2026, c.Year)
	assert.Equal(t, 3, c.LineCount)
	assert.Equal(t, 0, c.CountedCount)
	assert.Equal(t, "clerk", *c.CreatedBy)

	// Movements after the freeze do not change systemQty; they show as movedSinceOpen.
	f.move(t, a, f.depot, "OUT", "3", "2026-06-30")
	la := f.lineOf(t, c.ID, a)
	assert.Equal(t, "10", la.SystemQty)
	assert.Equal(t, "-3", la.MovedSinceOpen)
	assert.Nil(t, la.Difference)
	assert.Nil(t, la.OnHandAfterPost)

	la = f.setLine(t, c.ID, la, `"9"`)
	assert.Equal(t, "-1", *la.Difference)
	assert.Equal(t, "6", *la.OnHandAfterPost)
	assert.Equal(t, "clerk", *la.CountedBy)

	res := f.finalize(t, c, "SKIP")
	assert.Equal(t, 1, res.MovementCount)
	assert.Equal(t, "FINALIZED", res.Count.Status)
	assert.Equal(t, "SKIP", *res.Count.Uncounted)
	assert.Equal(t, "6", f.onHand(t, a, f.depot), "on hand after = on hand now + difference")
	assert.Equal(t, "4", f.onHand(t, b, f.depot), "uncounted line skipped")
	assert.Zero(t, f.driftCount(t))

	la = f.lineOf(t, c.ID, a)
	assert.Equal(t, "-3", la.MovedSinceOpen, "own adjustment is not a later movement")
	assert.Equal(t, "6", *la.OnHandAfterPost)
}

func TestStockCount_OneOpenPerDepotAndCancelReopens(t *testing.T) {
	f := newStock(t)
	c := f.openCount(t, f.depot, "")
	rec := f.post(t, f.path("/stock/counts"), f.token, `{"depotId":`+idStr(f.depot)+`,"countDate":"2026-06-30"}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_STOCK_COUNT_OPEN_EXISTS", errCode(t, rec))
	f.openCount(t, f.depot2, "")

	path := f.countPath(c.ID, "/cancel")
	assert.Equal(t, http.StatusPreconditionRequired, f.req(t, http.MethodPost, path, f.token, `{"reason":"wrong day"}`, nil).Code)
	rec = f.req(t, http.MethodPost, path, f.token, `{"reason":"x"}`, map[string]string{"If-Match": `"1"`})
	assert.Equal(t, http.StatusBadRequest, rec.Code)
	rec = f.req(t, http.MethodPost, path, f.token, `{"reason":"wrong day"}`, map[string]string{"If-Match": `"9"`})
	assert.Equal(t, http.StatusPreconditionFailed, rec.Code)
	rec = f.req(t, http.MethodPost, path, f.token, `{"reason":"wrong day"}`, map[string]string{"If-Match": `"1"`})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	cc := decode[countJSON](t, rec)
	assert.Equal(t, "CANCELLED", cc.Status)
	assert.Equal(t, "wrong day", *cc.CancelReason)
	assert.Equal(t, 2, cc.Version)
	rec = f.req(t, http.MethodPost, path, f.token, `{"reason":"wrong day"}`, map[string]string{"If-Match": `"2"`})
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_STOCK_COUNT_NOT_OPEN", errCode(t, rec))

	again := f.openCount(t, f.depot, "")
	assert.Equal(t, "SAY-2026-1-000002", again.CountNo)

	list := decode[[]countJSON](t, f.do(t, http.MethodGet, f.path("/stock/counts?depotId="+idStr(f.depot)), f.token, ""))
	require.Len(t, list, 2)
	assert.Equal(t, again.ID, list[0].ID, "newest first")
	list = decode[[]countJSON](t, f.do(t, http.MethodGet, f.path("/stock/counts?status=CANCELLED&year=2026"), f.token, ""))
	require.Len(t, list, 1)
	assert.Empty(t, decode[[]countJSON](t, f.do(t, http.MethodGet, f.path("/stock/counts?year=2025"), f.token, "")))
	assert.Equal(t, http.StatusBadRequest, f.do(t, http.MethodGet, f.path("/stock/counts?status=NOPE"), f.token, "").Code)
}

func TestStockCount_OpenValidation(t *testing.T) {
	f := newStock(t)
	rec := f.post(t, f.path("/stock/counts"), f.token, `{"depotId":999999,"countDate":"2026-06-30"}`)
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_DEPOT_NOT_FOUND", errCode(t, rec))
	rec = f.post(t, f.path("/stock/counts"), f.token, `{"depotId":`+idStr(f.depot)+`}`)
	assert.Equal(t, http.StatusBadRequest, rec.Code)
	rec = f.post(t, f.path("/stock/counts"), f.token, `{"depotId":`+idStr(f.depot)+`,"countDate":"2026-06-30","shelfFrom":"B","shelfTo":"A"}`)
	assert.Equal(t, http.StatusBadRequest, rec.Code)

	require.NoError(t, testDB.Exec(`INSERT INTO carry_forward_runs (org_id, year, entry_count, total_debit, total_credit) VALUES (?, 2027, 0, 0, 0)`, f.org).Error)
	rec = f.post(t, f.path("/stock/counts"), f.token, `{"depotId":`+idStr(f.depot)+`,"countDate":"2026-06-30"}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_YEAR_CLOSED", errCode(t, rec))
	assert.Equal(t, http.StatusNotFound, f.do(t, http.MethodGet, f.countPath(999999, ""), f.token, "").Code)
}

func TestStockCount_FinalizeInClosedYearIsRejected(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "A", "A", "")
	f.move(t, a, f.depot, "IN", "2", "2026-06-01")
	c := f.openCount(t, f.depot, "")
	f.setLine(t, c.ID, f.lineOf(t, c.ID, a), `"1"`)
	require.NoError(t, testDB.Exec(`INSERT INTO carry_forward_runs (org_id, year, entry_count, total_debit, total_credit) VALUES (?, 2027, 0, 0, 0)`, f.org).Error)
	rec := f.req(t, http.MethodPost, f.countPath(c.ID, "/finalize"), f.token, `{"uncounted":"SKIP"}`,
		map[string]string{"Idempotency-Key": uuid.NewString(), "If-Match": `"1"`})
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_YEAR_CLOSED", errCode(t, rec))
	assert.Equal(t, "OPEN", decode[countJSON](t, f.do(t, http.MethodGet, f.countPath(c.ID, ""), f.token, "")).Status)
}

func TestStockCount_ShelfRangeAndAddLine(t *testing.T) {
	f := newStock(t)
	parts := map[string]uint{}
	for _, s := range []string{"A-01", "B-01", "B-04-2", "C-01"} {
		p := f.part(t, "P"+s, "Part "+s, "")
		f.shelf(t, p, f.depot, s)
		parts[s] = p
	}
	c := f.openCount(t, f.depot, `,"shelfFrom":"B","shelfTo":"B"`)
	assert.Equal(t, 2, c.LineCount, "shelfTo matches as a prefix")
	assert.Equal(t, "B", *c.ShelfTo)

	off := f.part(t, "OFF", "Found on the shelf", "")
	f.move(t, off, f.depot, "IN", "5", "2026-06-01")
	f.shelf(t, off, f.depot, "Z-9")
	f.move(t, off, f.depot, "IN", "1", "2026-06-02")
	inactive := f.part(t, "INACT", "Inactive", `,"isActive":false`)

	path := f.countPath(c.ID, "/lines")
	rec := f.post(t, path, f.token, `{"partId":`+idStr(off)+`,"countedQty":"4"}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	l := decode[countLineJSON](t, rec)
	assert.True(t, l.Added)
	assert.Equal(t, "Z-9", l.Shelf)
	assert.Equal(t, "0", l.SystemQty, "frozen at startedAt, before the part moved")
	assert.Equal(t, "6", l.MovedSinceOpen)
	assert.Equal(t, "4", *l.CountedQty)
	assert.Equal(t, `"1"`, rec.Header().Get("ETag"))

	rec = f.post(t, path, f.token, `{"partId":`+idStr(off)+`}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_STOCK_COUNT_LINE_EXISTS", errCode(t, rec))
	rec = f.post(t, path, f.token, `{"partId":`+idStr(inactive)+`}`)
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_PART_INACTIVE", errCode(t, rec))
	rec = f.post(t, path, f.token, `{"partId":999999}`)
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code)
	assert.Equal(t, "ERR_PART_NOT_FOUND", errCode(t, rec))
	rec = f.post(t, path, f.token, `{"partId":`+idStr(parts["A-01"])+`,"shelf":"X-1"}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	assert.Equal(t, "X-1", decode[countLineJSON](t, rec).Shelf)

	page := f.countLines(t, c.ID, `{"shelfFrom":"B-02","shelfTo":"B"}`)
	require.Len(t, page.Rows, 1)
	assert.Equal(t, "B-04-2", page.Rows[0].Shelf)
	page = f.countLines(t, c.ID, `{"sortModel":[{"colId":"stockNo","sort":"desc"}],"startRow":0,"endRow":2}`)
	assert.Len(t, page.Rows, 2)
	assert.EqualValues(t, 4, page.TotalCount)
	assert.Equal(t, "PC-01", page.Rows[0].StockNo)
	page = f.countLines(t, c.ID, `{"filterModel":{"stockNo":{"filterType":"text","type":"contains","filter":"b-0"}}}`)
	assert.Len(t, page.Rows, 2)
	assert.Equal(t, http.StatusBadRequest, f.req(t, http.MethodPost, f.countPath(c.ID, "/lines/grid"), f.token,
		`{"sortModel":[{"colId":"nope","sort":"asc"}]}`, nil).Code)
}

func TestStockCount_AutosaveFlow(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "A", "A", "")
	f.move(t, a, f.depot, "IN", "3", "2026-06-01")
	c := f.openCount(t, f.depot, "")
	l := f.lineOf(t, c.ID, a)
	path := f.countPath(c.ID, "/lines/"+idStr(l.ID))

	assert.Equal(t, http.StatusPreconditionRequired, f.req(t, http.MethodPatch, path, f.token, `{"countedQty":"1"}`, nil).Code)
	l = f.setLine(t, c.ID, l, `"2"`)
	assert.Equal(t, 2, l.Version)
	rec := f.req(t, http.MethodPatch, path, f.token, `{"countedQty":"5"}`, map[string]string{"If-Match": `"1"`})
	assert.Equal(t, http.StatusPreconditionFailed, rec.Code)
	l = f.setLine(t, c.ID, l, `null`)
	assert.Nil(t, l.CountedQty)
	assert.Nil(t, l.CountedBy)
	rec = f.req(t, http.MethodPatch, path, f.token, `{"countedQty":"-1"}`, map[string]string{"If-Match": `"3"`})
	assert.Equal(t, http.StatusBadRequest, rec.Code)
	rec = f.req(t, http.MethodPatch, path, f.token, `{"countedQty":"1","shelf":"R-2"}`, map[string]string{"If-Match": `"3"`})
	require.Equal(t, http.StatusOK, rec.Code)
	assert.Equal(t, "R-2", decode[countLineJSON](t, rec).Shelf)
	rec = f.req(t, http.MethodPatch, f.countPath(c.ID, "/lines/999999"), f.token, `{"countedQty":"1"}`, map[string]string{"If-Match": `"1"`})
	assert.Equal(t, http.StatusNotFound, rec.Code)
	assert.Equal(t, "ERR_STOCK_COUNT_LINE_NOT_FOUND", errCode(t, rec))

	head := decode[countJSON](t, f.do(t, http.MethodGet, f.countPath(c.ID, ""), f.token, ""))
	assert.Equal(t, 1, head.Version, "header version does not move on autosave")
	assert.Equal(t, 1, head.CountedCount)

	f.finalize(t, head, "SKIP")
	rec = f.req(t, http.MethodPatch, path, f.token, `{"countedQty":"2"}`, map[string]string{"If-Match": `"5"`})
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_STOCK_COUNT_NOT_OPEN", errCode(t, rec))
	rec = f.post(t, f.countPath(c.ID, "/lines"), f.token, `{"partId":`+idStr(a)+`}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_STOCK_COUNT_NOT_OPEN", errCode(t, rec))
}

// Several counters save the same line with the same version: one wins, the others get 412.
func TestStockCount_ConcurrentAutosaveOneWins(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "A", "A", "")
	f.move(t, a, f.depot, "IN", "3", "2026-06-01")
	c := f.openCount(t, f.depot, "")
	l := f.lineOf(t, c.ID, a)
	codes := make([]int, 8)
	var wg sync.WaitGroup
	for i := range codes {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			codes[i] = f.req(t, http.MethodPatch, f.countPath(c.ID, "/lines/"+idStr(l.ID)), f.token,
				`{"countedQty":"`+strconv.Itoa(i)+`"}`, map[string]string{"If-Match": `"1"`}).Code
		}(i)
	}
	wg.Wait()
	ok, stale := 0, 0
	for _, code := range codes {
		switch code {
		case http.StatusOK:
			ok++
		case http.StatusPreconditionFailed:
			stale++
		}
	}
	assert.Equal(t, 1, ok, "%v", codes)
	assert.Equal(t, 7, stale, "%v", codes)
	assert.Equal(t, 2, f.lineOf(t, c.ID, a).Version)
}

func TestStockCount_ParallelFinalizeOneWins(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "A", "A", "")
	f.move(t, a, f.depot, "IN", "3", "2026-06-01")
	c := f.openCount(t, f.depot, "")
	f.setLine(t, c.ID, f.lineOf(t, c.ID, a), `"5"`)
	codes := make([]int, 6)
	var wg sync.WaitGroup
	for i := range codes {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			codes[i] = f.req(t, http.MethodPost, f.countPath(c.ID, "/finalize"), f.token, `{"uncounted":"SKIP"}`,
				map[string]string{"Idempotency-Key": uuid.NewString(), "If-Match": `"1"`}).Code
		}(i)
	}
	wg.Wait()
	ok := 0
	for _, code := range codes {
		if code == http.StatusOK {
			ok++
		} else {
			assert.Contains(t, []int{http.StatusConflict, http.StatusPreconditionFailed}, code)
		}
	}
	assert.Equal(t, 1, ok, "%v", codes)
	var n int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM stock_movements WHERE source_type = 'stock_count'`).Scan(&n).Error)
	assert.EqualValues(t, 1, n)
	assert.Equal(t, "5", f.onHand(t, a, f.depot))
}

func TestStockCount_FinalizeReplayAndZeroWithWarnings(t *testing.T) {
	f := newStock(t)
	rec := &recorder{}
	f.app.Relay.Register(rec)
	a := f.part(t, "A", "A", `,"initialPrice":{"price":"12.3456","validFrom":"2026-01-01"}`)
	b := f.part(t, "B", "B", "")
	neg := f.part(t, "N", "N", "")
	f.move(t, a, f.depot, "IN", "3", "2026-06-01")
	f.move(t, b, f.depot, "IN", "2", "2026-06-01")
	f.move(t, neg, f.depot, "IN", "1", "2026-06-01")
	c := f.openCount(t, f.depot, "")
	f.setLine(t, c.ID, f.lineOf(t, c.ID, a), `"0"`)
	f.setLine(t, c.ID, f.lineOf(t, c.ID, neg), `"0"`)
	f.move(t, neg, f.depot, "OUT", "4", "2026-06-30") // sold after the freeze: ends at −4

	// Live valuation while open: list price fallback and NONE.
	la := f.lineOf(t, c.ID, a)
	assert.Equal(t, "LIST_PRICE", la.CostSource)
	assert.Equal(t, "12.3456", *la.UnitCost)
	assert.Equal(t, "-37.04", *la.Value)
	ln := f.lineOf(t, c.ID, neg)
	assert.Equal(t, "NONE", ln.CostSource)
	assert.Nil(t, ln.Value)
	head := decode[countJSON](t, f.do(t, http.MethodGet, f.countPath(c.ID, ""), f.token, ""))
	assert.Equal(t, 2, head.DifferenceCount)
	assert.Equal(t, "-37.04", head.DifferenceValue)
	assert.Equal(t, "0.00", head.ValueIn)
	assert.Equal(t, "37.04", head.ValueOut)
	assert.Equal(t, 2, head.UnvaluedLines)

	page := f.countLines(t, c.ID, `{"only":"DIFFERENCES"}`)
	assert.Len(t, page.Rows, 2)
	assert.Equal(t, "-37.04", page.Totals["value"])
	assert.Equal(t, "-4", page.Totals["difference"])
	assert.Equal(t, "4", page.Totals["systemQty"])
	assert.Len(t, f.countLines(t, c.ID, `{"only":"NOT_COUNTED"}`).Rows, 1)
	assert.Len(t, f.countLines(t, c.ID, `{"only":"COUNTED"}`).Rows, 2)

	key := uuid.NewString()
	send := func() *countFinalizedJSON {
		r := f.req(t, http.MethodPost, f.countPath(c.ID, "/finalize"), f.token, `{"uncounted":"ZERO"}`,
			map[string]string{"Idempotency-Key": key, "If-Match": `"1"`})
		require.Equal(t, http.StatusOK, r.Code, r.Body.String())
		assert.Equal(t, `"2"`, r.Header().Get("ETag"))
		out := decode[countFinalizedJSON](t, r)
		return &out
	}
	res := send()
	assert.Equal(t, 3, res.MovementCount)
	require.Len(t, res.Warnings, 1)
	assert.Equal(t, "NEGATIVE_STOCK", res.Warnings[0].Code)
	assert.Equal(t, neg, res.Warnings[0].PartID)
	assert.Equal(t, "-4", res.Warnings[0].OnHandAfter)
	assert.Equal(t, "ZERO", *res.Count.Uncounted)
	replay := send()
	assert.Equal(t, res, replay)

	assert.Equal(t, "0", f.onHand(t, a, f.depot))
	assert.Equal(t, "0", f.onHand(t, b, f.depot))
	assert.Equal(t, "-4", f.onHand(t, neg, f.depot))
	assert.Zero(t, f.driftCount(t))

	// After finalize the snapshot is read, not the live price.
	require.Equal(t, http.StatusCreated, f.post(t, f.path("/catalog/parts/"+idStr(a)+"/prices"), f.token, `{"price":"99","validFrom":"2026-02-01"}`).Code)
	la = f.lineOf(t, c.ID, a)
	assert.Equal(t, "12.3456", *la.UnitCost)
	assert.Equal(t, "LIST_PRICE", la.CostSource)

	var kinds []string
	require.NoError(t, testDB.Raw(`SELECT kind || ':' || trim_scale(qty)::text || ':' || coalesce(trim_scale(unit_cost)::text, '-') || ':' || movement_date::text
		FROM stock_movements WHERE source_type = 'stock_count' ORDER BY part_id`).Scan(&kinds).Error)
	assert.Equal(t, []string{"ADJUST_OUT:3:12.3456:2026-06-30", "ADJUST_OUT:2:-:2026-06-30", "ADJUST_OUT:1:-:2026-06-30"}, kinds)

	f.relay(t)
	moved := rec.byType(t, "StockMoved")
	require.Len(t, moved, 3)
	for _, e := range moved {
		validateEvent(t, e)
	}
}

func TestStockCount_ExportsSheetAndGrid(t *testing.T) {
	f := newStock(t)
	a := f.part(t, "A", "Ağ filtresi", "")
	f.move(t, a, f.depot, "IN", "3", "2026-06-01")
	f.shelf(t, a, f.depot, "A-1")
	c := f.openCount(t, f.depot, "")
	for _, q := range []string{"", "?sheet=WITH_SYSTEM&only=NOT_COUNTED&shelfFrom=A&shelfTo=B"} {
		rec := f.req(t, http.MethodGet, f.countPath(c.ID, q), f.token, "", map[string]string{"Accept": "application/pdf"})
		require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
		assert.Equal(t, "application/pdf", rec.Header().Get("Content-Type"))
		assert.True(t, strings.HasPrefix(rec.Body.String(), "%PDF"))
	}
	rec := f.req(t, http.MethodGet, f.countPath(c.ID, "?sheet=NOPE"), f.token, "", map[string]string{"Accept": "application/pdf"})
	assert.Equal(t, http.StatusBadRequest, rec.Code)
	rec = f.req(t, http.MethodPost, f.countPath(c.ID, "/lines/grid"), f.token, `{}`,
		map[string]string{"Accept": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"})
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	assert.Contains(t, sheetXML(t, rec.Body.Bytes()), "Ağ filtresi")
	rec = f.req(t, http.MethodGet, f.countPath(c.ID, ""), f.token, "", nil)
	assert.Equal(t, `"1"`, rec.Header().Get("ETag"))
	assert.Equal(t, http.StatusNotFound, f.req(t, http.MethodPost, f.countPath(999999, "/lines/grid"), f.token, `{}`, nil).Code)
}

func TestStockCount_OtherCompanyCountIsNotFound(t *testing.T) {
	f := newStock(t)
	c := f.openCount(t, f.depot, "")
	other := f.seedOrg(t, "count-other", f.userID)
	rec := f.do(t, http.MethodGet, orgPath(other, "/stock/counts/"+idStr(c.ID)), f.token, "")
	assert.Equal(t, http.StatusNotFound, rec.Code)
	assert.Equal(t, "ERR_STOCK_COUNT_NOT_FOUND", errCode(t, rec))
}
