//go:build integration

package router_test

import (
	"encoding/json"
	"fmt"
	"net/http"
	"strconv"
	"sync"
	"testing"
	"time"

	"github.com/fpirim/fikopar/backend/internal/models"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// parallel runs n requests at once and returns their status codes and error codes.
func parallel(n int, fn func(i int) (int, []byte)) ([]int, []string) {
	statuses := make([]int, n)
	codes := make([]string, n)
	var wg sync.WaitGroup
	start := make(chan struct{})
	for i := 0; i < n; i++ {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			<-start
			status, body := fn(i)
			statuses[i] = status
			var e struct {
				Code string `json:"code"`
			}
			_ = json.Unmarshal(body, &e)
			codes[i] = e.Code
		}(i)
	}
	close(start)
	wg.Wait()
	return statuses, codes
}

func count(statuses []int, want int) int {
	n := 0
	for _, s := range statuses {
		if s == want {
			n++
		}
	}
	return n
}

func TestDocumentsConcurrency_ParallelPostOfOneDraftPostsOnce(t *testing.T) {
	f := newDocs(t)
	d := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "2", "10", ""), lineJSON(f.p2, "1", "10", "")))
	statuses, codes := parallel(8, func(int) (int, []byte) {
		rec := f.postRaw(t, f.token, d.ID, d.Version, "")
		return rec.Code, rec.Body.Bytes()
	})
	require.Equal(t, 1, count(statuses, http.StatusOK), "%v %v", statuses, codes)
	for i, s := range statuses {
		if s != http.StatusOK {
			assert.Contains(t, []int{http.StatusConflict, http.StatusPreconditionFailed}, s, codes[i])
		}
	}
	var entries, moves, numbers int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM journal_entries WHERE source_type = 'document' AND source_id = ?`, idStr(d.ID)).Scan(&entries).Error)
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM stock_movements m JOIN document_lines l ON l.id::text = m.source_id
		WHERE m.source_type = 'document' AND l.document_id = ?`, d.ID).Scan(&moves).Error)
	require.NoError(t, testDB.Raw(`SELECT next_value - 1 FROM number_series WHERE org_id = ? AND prefix = 'SF'`, f.org).Scan(&numbers).Error)
	assert.EqualValues(t, 1, entries)
	assert.EqualValues(t, 2, moves)
	assert.EqualValues(t, 1, numbers, "one number, no gap")
	assert.Equal(t, "8", f.onHand(t, f.p1, f.depot))
	noDrift(t)
}

func TestDocumentsConcurrency_ParallelBillsOfOneWaybill(t *testing.T) {
	f := newDocs(t)
	w := f.posted(t, docBody("SALES_WAYBILL", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "2", "10", "")))
	statuses, codes := parallel(8, func(int) (int, []byte) {
		rec := f.createRaw(t, f.token, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-02", fmt.Sprintf(`,"waybillDocumentId":%d`, w.ID)))
		return rec.Code, rec.Body.Bytes()
	})
	require.Equal(t, 1, count(statuses, http.StatusCreated), "%v %v", statuses, codes)
	for i, s := range statuses {
		if s != http.StatusCreated {
			assert.Equal(t, http.StatusConflict, s)
			assert.Equal(t, "ERR_WAYBILL_BILLED", codes[i])
		}
	}
}

func TestDocumentsConcurrency_ParallelReturnsStayWithinTheOriginal(t *testing.T) {
	f := newDocs(t)
	o := f.posted(t, docBody("SALES_INVOICE_WITH_WAYBILL", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "5", "10", "")))
	drafts := make([]docJSON, 8)
	for i := range drafts {
		drafts[i] = f.create(t, docBody("SALES_RETURN_WAYBILL", f.depot, f.cust, "2026-03-02", fmt.Sprintf(`,"linkedDocumentId":%d`, o.ID),
			lineJSON(f.p1, "1", "10", fmt.Sprintf(`,"originLineId":%d`, o.Lines[0].ID))))
	}
	statuses, codes := parallel(len(drafts), func(i int) (int, []byte) {
		rec := f.postRaw(t, f.token, drafts[i].ID, drafts[i].Version, "")
		return rec.Code, rec.Body.Bytes()
	})
	assert.Equal(t, 5, count(statuses, http.StatusOK), "%v %v", statuses, codes)
	for i, s := range statuses {
		if s != http.StatusOK {
			assert.Equal(t, "ERR_RETURN_QTY_EXCEEDED", codes[i])
		}
	}
	assert.Equal(t, "5", *f.get(t, o.ID).Lines[0].ReturnedQty)
	assert.Equal(t, "10", f.onHand(t, f.p1, f.depot))
	noDrift(t)
}

func TestDocumentsConcurrency_ReservationRacesAPost(t *testing.T) {
	f := newDocs(t)
	for round := 0; round < 6; round++ {
		a := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "3", "10", "")))
		f.move(t, f.p1, f.depot, "IN", "3", "2026-01-02")
		statuses, codes := parallel(3, func(i int) (int, []byte) {
			if i == 0 {
				rec := f.postRaw(t, f.token, a.ID, a.Version, "")
				return rec.Code, rec.Body.Bytes()
			}
			rec := f.createRaw(t, f.token, docBody("SALES_WAYBILL", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "4", "10", "")))
			return rec.Code, rec.Body.Bytes()
		})
		assert.Equal(t, []int{http.StatusOK, http.StatusCreated, http.StatusCreated}, statuses, "%v", codes)
		noDrift(t)
		var bad int64
		require.NoError(t, testDB.Raw(`SELECT count(*) FROM stock_levels WHERE reserved < 0`).Scan(&bad).Error)
		assert.Zero(t, bad)
	}
}

func TestDocumentsConcurrency_SweeperRacesUpdates(t *testing.T) {
	f := newDocs(t)
	drafts := make([]docJSON, 10)
	for i := range drafts {
		drafts[i] = f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "10", "")))
	}
	later := time.Now().Add(49 * time.Hour)
	statuses, codes := parallel(len(drafts)+2, func(i int) (int, []byte) {
		if i >= len(drafts) {
			if _, err := f.app.Sweeper.Sweep(t.Context(), later); err != nil {
				return 500, []byte(`{"code":"` + err.Error() + `"}`)
			}
			return 200, nil
		}
		rec := f.update(t, drafts[i].ID, drafts[i].Version, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-02", "", lineJSON(f.p1, "1", "10", "")))
		return rec.Code, rec.Body.Bytes()
	})
	assert.Equal(t, len(statuses), count(statuses, http.StatusOK), "%v %v", statuses, codes)
	noDrift(t)

	// Every reserved unit is released at most once: reserved = released + still held.
	var reserved, released, held models.Dec4
	require.NoError(t, testDB.Raw(`SELECT coalesce(sum((l->>'reserved')::numeric), 0) FROM outbox o, jsonb_array_elements(o.payload->'lines') l
		WHERE o.org_id = ? AND o.event_type = 'StockReserved'`, f.org).Scan(&reserved).Error)
	require.NoError(t, testDB.Raw(`SELECT coalesce(sum((l->>'qty')::numeric), 0) FROM outbox o, jsonb_array_elements(o.payload->'lines') l
		WHERE o.org_id = ? AND o.event_type = 'StockReleased'`, f.org).Scan(&released).Error)
	require.NoError(t, testDB.Raw(`SELECT coalesce(sum(qty), 0) FROM stock_reservations WHERE org_id = ?`, f.org).Scan(&held).Error)
	assert.Equal(t, reserved, released+held)
}

// Two posts whose reservations cover different parts release one level and then move both, in opposite orders
// unless the levels are locked in (part, depot) order first; a transfer of the same parts runs alongside.
func TestDocumentsConcurrency_OpposingPartOrdersDoNotDeadlock(t *testing.T) {
	f := newDocs(t)
	level := func(part uint, want int) {
		have, err := strconv.Atoi(f.onHand(t, part, f.depot))
		require.NoError(t, err)
		if have < want {
			f.move(t, part, f.depot, "IN", strconv.Itoa(want-have), "2026-01-02")
		} else if have > want {
			f.move(t, part, f.depot, "OUT", strconv.Itoa(have-want), "2026-01-02")
		}
	}
	for round := 0; round < 15; round++ {
		level(f.p1, 10)
		level(f.p2, 0)
		x := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p2, "1", "10", ""), lineJSON(f.p1, "10", "10", "")))
		level(f.p2, 1)
		y := f.create(t, docBody("SALES_INVOICE", f.depot, f.cust, "2026-03-01", "", lineJSON(f.p1, "1", "10", ""), lineJSON(f.p2, "1", "10", "")))
		require.Equal(t, []string{"0", "10"}, []string{x.Lines[0].ReservedQty, x.Lines[1].ReservedQty})
		require.Equal(t, []string{"0", "1"}, []string{y.Lines[0].ReservedQty, y.Lines[1].ReservedQty})
		statuses, codes := parallel(3, func(i int) (int, []byte) {
			switch i {
			case 0:
				rec := f.postRaw(t, f.token, x.ID, x.Version, "")
				return rec.Code, rec.Body.Bytes()
			case 1:
				rec := f.postRaw(t, f.token, y.ID, y.Version, "")
				return rec.Code, rec.Body.Bytes()
			}
			rec := f.post(t, f.path("/stock/transfers"), f.token, fmt.Sprintf(`{"fromDepotId":%d,"toDepotId":%d,"date":"2026-03-01",
				"lines":[{"partId":%d,"qty":"1"},{"partId":%d,"qty":"1"}]}`, f.depot2, f.depot, f.p2, f.p1))
			return rec.Code, rec.Body.Bytes()
		})
		require.Equal(t, []int{http.StatusOK, http.StatusOK, http.StatusCreated}, statuses, "round %d: %v", round, codes)
		noDrift(t)
	}
}
