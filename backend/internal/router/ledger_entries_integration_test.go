//go:build integration

package router_test

import (
	"fmt"
	"math/rand"
	"net/http"
	"strings"
	"sync"
	"testing"

	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

type postingJSON struct {
	ID          uint   `json:"id"`
	LineNo      int    `json:"lineNo"`
	AccountID   uint   `json:"accountId"`
	AccountCode string `json:"accountCode"`
	Side        string `json:"side"`
	Amount      string `json:"amount"`
	Currency    string `json:"currency"`
}

type entryJSON struct {
	ID                uint          `json:"id"`
	OrgID             uint          `json:"orgId"`
	DepotID           uint          `json:"depotId"`
	EntryNo           string        `json:"entryNo"`
	EntryDate         string        `json:"entryDate"`
	Description       string        `json:"description"`
	SourceType        string        `json:"sourceType"`
	SourceID          *string       `json:"sourceId"`
	SourceNo          *string       `json:"sourceNo"`
	ReversesEntryID   *uint         `json:"reversesEntryId"`
	ReversedByEntryID *uint         `json:"reversedByEntryId"`
	IdempotencyKey    *string       `json:"idempotencyKey"`
	CreatedBy         *uint         `json:"createdBy"`
	Postings          []postingJSON `json:"postings"`
}

type ln struct {
	account uint
	side    string
	amount  string
}

func entryBody(depot uint, date string, lines ...ln) string {
	parts := make([]string, len(lines))
	for i, l := range lines {
		parts[i] = fmt.Sprintf(`{"accountId":%d,"side":%q,"amount":%q}`, l.account, l.side, l.amount)
	}
	return fmt.Sprintf(`{"depotId":%d,"entryDate":%q,"description":"test entry","lines":[%s]}`, depot, date, strings.Join(parts, ","))
}

func (f *ledgerFix) entry(t *testing.T, date string, lines ...ln) entryJSON {
	t.Helper()
	rec := f.post(t, f.path("/ledger/entries"), f.token, entryBody(f.depot, date, lines...))
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	return decode[entryJSON](t, rec)
}

func (f *ledgerFix) getEntry(t *testing.T, id uint) entryJSON {
	t.Helper()
	rec := f.do(t, http.MethodGet, f.path("/ledger/entries/"+idStr(id)), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	return decode[entryJSON](t, rec)
}

func (f *ledgerFix) countEntries(t *testing.T) int64 {
	t.Helper()
	var n int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM journal_entries WHERE org_id = ?`, f.org).Scan(&n).Error)
	return n
}

func TestJournal_PostManualEntry(t *testing.T) {
	f := newLedger(t)
	cash := f.account(t, "100", "ASSET")
	sales := f.account(t, "600", "INCOME")

	e := f.entry(t, "2026-03-05", ln{cash, "DEBIT", "150.00"}, ln{sales, "CREDIT", "150.00"})
	assert.Equal(t, "J-2026-1-000001", e.EntryNo)
	assert.Equal(t, "manual", e.SourceType)
	assert.Equal(t, "2026-03-05", e.EntryDate)
	require.NotNil(t, e.CreatedBy)
	assert.Equal(t, f.userID, *e.CreatedBy)
	require.NotNil(t, e.IdempotencyKey)
	require.Len(t, e.Postings, 2)
	assert.Equal(t, "100", e.Postings[0].AccountCode)
	assert.Equal(t, "150.00", e.Postings[0].Amount)
	assert.Equal(t, "TRY", e.Postings[0].Currency)

	got := f.getEntry(t, e.ID)
	assert.Equal(t, e.EntryNo, got.EntryNo)
	assert.Nil(t, got.ReversedByEntryID)

	assert.Equal(t, "J-2026-1-000002", f.entry(t, "2026-03-06", ln{cash, "DEBIT", "1.00"}, ln{sales, "CREDIT", "1.00"}).EntryNo)
}

func TestJournal_RejectsInvalidEntriesWithoutConsumingNumbers(t *testing.T) {
	f := newLedger(t)
	cash := f.account(t, "100", "ASSET")
	sales := f.account(t, "600", "INCOME")
	eur := f.post(t, f.path("/ledger/accounts"), f.token, `{"code":"102.EUR","name":"EUR bank","type":"ASSET","currency":"EUR"}`)
	require.Equal(t, http.StatusCreated, eur.Code)
	eurID := extractID(t, eur.Body.Bytes())
	inactive := f.account(t, "770", "EXPENSE")
	require.Equal(t, http.StatusOK, f.req(t, http.MethodPatch, f.path("/ledger/accounts/"+idStr(inactive)), f.token, `{"isActive":false}`, map[string]string{"If-Match": `"1"`}).Code)

	cases := []struct {
		name   string
		body   string
		status int
		code   string
	}{
		{"unbalanced", entryBody(f.depot, "2026-03-05", ln{cash, "DEBIT", "100.00"}, ln{sales, "CREDIT", "99.99"}), 422, "ERR_ENTRY_UNBALANCED"},
		{"one line", entryBody(f.depot, "2026-03-05", ln{cash, "DEBIT", "100.00"}), 422, "ERR_ENTRY_TOO_FEW_LINES"},
		{"zero amount", entryBody(f.depot, "2026-03-05", ln{cash, "DEBIT", "0.00"}, ln{sales, "CREDIT", "0.00"}), 400, "ERR_INVALID_AMOUNT"},
		{"negative amount", entryBody(f.depot, "2026-03-05", ln{cash, "DEBIT", "-5.00"}, ln{sales, "CREDIT", "-5.00"}), 400, "ERR_INVALID_AMOUNT"},
		{"bad side", entryBody(f.depot, "2026-03-05", ln{cash, "LEFT", "5.00"}, ln{sales, "CREDIT", "5.00"}), 400, "ERR_INVALID_REQUEST"},
		{"unknown account", entryBody(f.depot, "2026-03-05", ln{999999, "DEBIT", "5.00"}, ln{sales, "CREDIT", "5.00"}), 422, "ERR_ACCOUNT_NOT_FOUND"},
		{"inactive account", entryBody(f.depot, "2026-03-05", ln{inactive, "DEBIT", "5.00"}, ln{sales, "CREDIT", "5.00"}), 422, "ERR_ACCOUNT_INACTIVE"},
		{"currencies unbalanced", entryBody(f.depot, "2026-03-05", ln{eurID, "DEBIT", "5.00"}, ln{sales, "CREDIT", "5.00"}), 422, "ERR_ENTRY_UNBALANCED"},
		{"unknown depot", entryBody(999999, "2026-03-05", ln{cash, "DEBIT", "5.00"}, ln{sales, "CREDIT", "5.00"}), 422, "ERR_DEPOT_NOT_FOUND"},
		{"bad date", entryBody(f.depot, "2026-13-05", ln{cash, "DEBIT", "5.00"}, ln{sales, "CREDIT", "5.00"}), 400, "ERR_INVALID_REQUEST"},
		{"money as number", fmt.Sprintf(`{"depotId":%d,"entryDate":"2026-03-05","description":"x","lines":[{"accountId":%d,"side":"DEBIT","amount":5},{"accountId":%d,"side":"CREDIT","amount":5}]}`, f.depot, cash, sales), 400, "ERR_INVALID_REQUEST"},
	}
	for _, tc := range cases {
		rec := f.post(t, f.path("/ledger/entries"), f.token, tc.body)
		assert.Equal(t, tc.status, rec.Code, "%s: %s", tc.name, rec.Body.String())
		assert.Equal(t, tc.code, errCode(t, rec), tc.name)
	}
	assert.Zero(t, f.countEntries(t))
	assert.Equal(t, "J-2026-1-000001", f.entry(t, "2026-03-05", ln{cash, "DEBIT", "1.00"}, ln{sales, "CREDIT", "1.00"}).EntryNo)
}

// Property: random balanced entries through the API keep Σ debit = Σ credit and balance = Σ postings.
func TestJournal_RandomEntriesKeepLedgerBalanced(t *testing.T) {
	f := newLedger(t)
	accounts := []uint{f.account(t, "100", "ASSET"), f.account(t, "120.1", "ASSET"), f.account(t, "320.1", "LIABILITY"), f.account(t, "600", "INCOME"), f.account(t, "770", "EXPENSE")}
	rng := rand.New(rand.NewSource(7))
	for i := 0; i < 40; i++ {
		var lines []ln
		var total int64
		for j := 0; j < 1+rng.Intn(3); j++ {
			c := int64(1 + rng.Intn(500000))
			total += c
			lines = append(lines, ln{accounts[rng.Intn(len(accounts))], "DEBIT", fmt.Sprintf("%d.%02d", c/100, c%100)})
		}
		part := total / 2
		lines = append(lines, ln{accounts[rng.Intn(len(accounts))], "CREDIT", fmt.Sprintf("%d.%02d", (total-part)/100, (total-part)%100)})
		if part > 0 {
			lines = append(lines, ln{accounts[rng.Intn(len(accounts))], "CREDIT", fmt.Sprintf("%d.%02d", part/100, part%100)})
		}
		f.entry(t, fmt.Sprintf("2026-%02d-%02d", 1+rng.Intn(12), 1+rng.Intn(28)), lines...)
	}

	var drift, mismatched int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM account_balance_drift`).Scan(&drift).Error)
	assert.Zero(t, drift)
	require.NoError(t, testDB.Raw(`
		SELECT count(*) FROM accounts a
		WHERE a.org_id = ? AND coalesce((SELECT sum(debit_total - credit_total) FROM account_balances b WHERE b.account_id = a.id), 0)
		   <> coalesce((SELECT sum(CASE side WHEN 'DEBIT' THEN amount ELSE -amount END) FROM postings p WHERE p.account_id = a.id), 0)`, f.org).Scan(&mismatched).Error)
	assert.Zero(t, mismatched)
	var net string
	require.NoError(t, testDB.Raw(`SELECT sum(CASE side WHEN 'DEBIT' THEN amount ELSE -amount END)::text FROM postings WHERE org_id = ?`, f.org).Scan(&net).Error)
	assert.Equal(t, "0.00", net)
}

func TestJournal_ReversalLinksBothWaysAndNetsToZero(t *testing.T) {
	f := newLedger(t)
	cash := f.account(t, "100", "ASSET")
	sales := f.account(t, "600", "INCOME")
	orig := f.entry(t, "2026-03-05", ln{cash, "DEBIT", "80.00"}, ln{sales, "CREDIT", "80.00"})

	rec := f.post(t, f.path("/ledger/entries/"+idStr(orig.ID)+"/reversal"), f.token, `{"reason":"wrong customer"}`)
	require.Equal(t, http.StatusCreated, rec.Code, rec.Body.String())
	rev := decode[entryJSON](t, rec)
	assert.Equal(t, "reversal", rev.SourceType)
	require.NotNil(t, rev.ReversesEntryID)
	assert.Equal(t, orig.ID, *rev.ReversesEntryID)
	assert.Contains(t, rev.Description, "wrong customer")
	require.Len(t, rev.Postings, 2)
	assert.Equal(t, "CREDIT", rev.Postings[0].Side)
	assert.Equal(t, cash, rev.Postings[0].AccountID)

	again := f.getEntry(t, orig.ID)
	require.NotNil(t, again.ReversedByEntryID)
	assert.Equal(t, rev.ID, *again.ReversedByEntryID)

	var bal string
	require.NoError(t, testDB.Raw(`SELECT (debit_total - credit_total)::text FROM account_balances WHERE account_id = ?`, cash).Scan(&bal).Error)
	assert.Equal(t, "0.00", bal)

	rec = f.post(t, f.path("/ledger/entries/"+idStr(orig.ID)+"/reversal"), f.token, `{"reason":"again"}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_ENTRY_ALREADY_REVERSED", errCode(t, rec))

	rec = f.post(t, f.path("/ledger/entries/"+idStr(rev.ID)+"/reversal"), f.token, `{"reason":"undo"}`)
	assert.Equal(t, http.StatusConflict, rec.Code)
	assert.Equal(t, "ERR_INVALID_STATE_TRANSITION", errCode(t, rec))

	rec = f.post(t, f.path("/ledger/entries/"+idStr(orig.ID+100)+"/reversal"), f.token, `{"reason":"none"}`)
	assert.Equal(t, http.StatusNotFound, rec.Code)
	rec = f.post(t, f.path("/ledger/entries/"+idStr(orig.ID)+"/reversal"), f.token, `{"reason":"x"}`)
	assert.Equal(t, http.StatusBadRequest, rec.Code, "reason has a minimum length")
}

func TestJournal_ConcurrentReversalsPostOnce(t *testing.T) {
	f := newLedger(t)
	cash := f.account(t, "100", "ASSET")
	sales := f.account(t, "600", "INCOME")
	orig := f.entry(t, "2026-03-05", ln{cash, "DEBIT", "10.00"}, ln{sales, "CREDIT", "10.00"})
	codes := make([]int, 6)
	var wg sync.WaitGroup
	for i := range codes {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			codes[i] = f.post(t, f.path("/ledger/entries/"+idStr(orig.ID)+"/reversal"), f.token, `{"reason":"race"}`).Code
		}(i)
	}
	wg.Wait()
	won := 0
	for _, c := range codes {
		if c == http.StatusCreated {
			won++
		} else {
			assert.Equal(t, http.StatusConflict, c)
		}
	}
	assert.Equal(t, 1, won)
	assert.Equal(t, int64(2), f.countEntries(t))
}

func TestJournal_IdempotentRetryPostsOnce(t *testing.T) {
	f := newLedger(t)
	cash := f.account(t, "100", "ASSET")
	sales := f.account(t, "600", "INCOME")
	key := map[string]string{"Idempotency-Key": uuid.NewString()}
	body := entryBody(f.depot, "2026-03-05", ln{cash, "DEBIT", "10.00"}, ln{sales, "CREDIT", "10.00"})

	first := f.req(t, http.MethodPost, f.path("/ledger/entries"), f.token, body, key)
	require.Equal(t, http.StatusCreated, first.Code, first.Body.String())
	second := f.req(t, http.MethodPost, f.path("/ledger/entries"), f.token, body, key)
	require.Equal(t, http.StatusCreated, second.Code)
	assert.Equal(t, "true", second.Header().Get("Idempotent-Replayed"))
	assert.Equal(t, decode[entryJSON](t, first).ID, decode[entryJSON](t, second).ID)
	assert.Equal(t, int64(1), f.countEntries(t))
	assert.Equal(t, key["Idempotency-Key"], *decode[entryJSON](t, first).IdempotencyKey)
}

func TestJournal_ListFiltersAndPaging(t *testing.T) {
	f := newLedger(t)
	depot2 := f.newDepot(t, "2")
	cash := f.account(t, "100", "ASSET")
	sales := f.account(t, "600", "INCOME")
	other := f.account(t, "770", "EXPENSE")
	e1 := f.entry(t, "2026-01-31", ln{cash, "DEBIT", "1.00"}, ln{sales, "CREDIT", "1.00"})
	e2 := f.entry(t, "2026-02-01", ln{cash, "DEBIT", "2.00"}, ln{sales, "CREDIT", "2.00"})
	e3 := f.entry(t, "2026-02-28", ln{other, "DEBIT", "3.00"}, ln{sales, "CREDIT", "3.00"})
	rec := f.post(t, f.path("/ledger/entries"), f.token, entryBody(depot2, "2026-02-15", ln{cash, "DEBIT", "4.00"}, ln{sales, "CREDIT", "4.00"}))
	require.Equal(t, http.StatusCreated, rec.Code)
	e4 := decode[entryJSON](t, rec)
	assert.Equal(t, "J-2026-2-000001", e4.EntryNo)

	type page struct {
		Items      []entryJSON `json:"items"`
		NextCursor *string     `json:"nextCursor"`
	}
	ids := func(p page) []uint {
		out := []uint{}
		for _, e := range p.Items {
			out = append(out, e.ID)
		}
		return out
	}
	get := func(q string) page {
		return decode[page](t, f.do(t, http.MethodGet, f.path("/ledger/entries"+q), f.token, ""))
	}

	p := get("?limit=3")
	assert.Equal(t, []uint{e3.ID, e4.ID, e2.ID}, ids(p))
	require.NotNil(t, p.NextCursor)
	p = get("?limit=3&cursor=" + *p.NextCursor)
	assert.Equal(t, []uint{e1.ID}, ids(p))
	assert.Nil(t, p.NextCursor)

	assert.Equal(t, []uint{e3.ID, e2.ID}, ids(get("?from=2026-02-01&to=2026-02-28&depotId="+idStr(f.depot))))
	assert.Equal(t, []uint{e3.ID}, ids(get("?accountId="+idStr(other))))
	assert.Equal(t, []uint{e4.ID}, ids(get("?q=J-2026-2")))
	assert.Equal(t, []uint{e3.ID, e4.ID, e2.ID, e1.ID}, ids(get("?sourceType=manual")))
	require.Len(t, get("?limit=1").Items[0].Postings, 2)
}

func TestJournal_ForeignEntryIsNotFound(t *testing.T) {
	f := newLedger(t)
	cash := f.account(t, "100", "ASSET")
	sales := f.account(t, "600", "INCOME")
	e := f.entry(t, "2026-03-05", ln{cash, "DEBIT", "1.00"}, ln{sales, "CREDIT", "1.00"})
	other := f.seedOrg(t, "entry-other", f.userID)
	for _, p := range []string{"/ledger/entries/" + idStr(e.ID), "/ledger/entries/" + idStr(e.ID) + "/trail"} {
		rec := f.do(t, http.MethodGet, orgPath(other, p), f.token, "")
		assert.Equal(t, http.StatusNotFound, rec.Code, p)
	}
	rec := f.post(t, orgPath(other, "/ledger/entries/"+idStr(e.ID)+"/reversal"), f.token, `{"reason":"steal"}`)
	assert.Equal(t, http.StatusNotFound, rec.Code)
	rec = f.post(t, orgPath(other, "/ledger/entries"), f.token, entryBody(f.depot, "2026-03-05", ln{cash, "DEBIT", "1.00"}, ln{sales, "CREDIT", "1.00"}))
	assert.Equal(t, http.StatusUnprocessableEntity, rec.Code, "another org's depot and accounts are unknown here")
}

func TestOutbox_LedgerEntryPostedMatchesContract(t *testing.T) {
	f := newLedger(t)
	rec := &recorder{}
	f.app.Relay.Register(rec)
	cash := f.account(t, "100", "ASSET")
	sales := f.account(t, "600", "INCOME")
	e := f.entry(t, "2026-03-05", ln{cash, "DEBIT", "12.50"}, ln{sales, "CREDIT", "12.50"})

	assert.Equal(t, 1, f.relay(t))
	events := rec.byType(t, "LedgerEntryPosted")
	require.Len(t, events, 1)
	validateEvent(t, events[0])
	env := decodeBytes[map[string]any](t, events[0])
	assert.Equal(t, idStr(e.ID), env["aggregateId"])
	payload := env["payload"].(map[string]any)
	assert.Equal(t, "12.50", payload["total"])
	assert.Equal(t, e.EntryNo, payload["entryNo"])
}

// At-least-once delivery: a redelivered event must not be applied twice by a consumer.
func TestOutbox_RedeliveryIsIdempotentAndFailuresRetry(t *testing.T) {
	f := newLedger(t)
	flaky := &recorder{failFirst: true}
	f.app.Relay.Register(flaky)
	cash := f.account(t, "100", "ASSET")
	sales := f.account(t, "600", "INCOME")
	f.entry(t, "2026-03-05", ln{cash, "DEBIT", "1.00"}, ln{sales, "CREDIT", "1.00"})

	assert.Equal(t, 0, f.relay(t), "a failing consumer keeps the event unpublished")
	var attempts int
	require.NoError(t, testDB.Raw(`SELECT attempts FROM outbox WHERE org_id = ?`, f.org).Scan(&attempts).Error)
	assert.Equal(t, 1, attempts)

	assert.Equal(t, 1, f.relay(t))
	assert.Equal(t, 0, f.relay(t))

	checked := func() int64 {
		var n int64
		require.NoError(t, testDB.Raw(`SELECT entries_checked FROM ledger_checks WHERE org_id = ?`, f.org).Scan(&n).Error)
		return n
	}
	assert.Equal(t, int64(1), checked())

	require.NoError(t, testDB.Exec(`UPDATE outbox SET published_at = NULL WHERE org_id = ?`, f.org).Error)
	assert.Equal(t, 1, f.relay(t))
	assert.Equal(t, int64(1), checked(), "redelivery must not be applied twice")
	assert.Len(t, flaky.envelopes, 1)

	var status string
	require.NoError(t, testDB.Raw(`SELECT status FROM ledger_checks WHERE org_id = ?`, f.org).Scan(&status).Error)
	assert.Equal(t, "BALANCED", status)
}

func TestJournal_TrailShowsAuditOutboxAndDelivery(t *testing.T) {
	f := newLedger(t)
	cash := f.account(t, "100", "ASSET")
	sales := f.account(t, "600", "INCOME")
	e := f.entry(t, "2026-03-05", ln{cash, "DEBIT", "1.00"}, ln{sales, "CREDIT", "1.00"})
	f.relay(t)

	type item struct {
		Kind        string  `json:"kind"`
		Label       string  `json:"label"`
		Ref         *string `json:"ref"`
		ActorUserID *uint   `json:"actorUserId"`
	}
	rec := f.do(t, http.MethodGet, f.path("/ledger/entries/"+idStr(e.ID)+"/trail"), f.token, "")
	require.Equal(t, http.StatusOK, rec.Code, rec.Body.String())
	items := decode[[]item](t, rec)
	kinds := map[string]string{}
	for _, it := range items {
		kinds[it.Kind] = it.Label
	}
	assert.Equal(t, "journal_entry.posted", kinds["AUDIT"])
	assert.Equal(t, "LedgerEntryPosted", kinds["OUTBOX"])
	assert.Equal(t, "ledger-check", kinds["DELIVERY"])
	require.NotEmpty(t, items)
	assert.Equal(t, "AUDIT", items[0].Kind)
	require.NotNil(t, items[0].ActorUserID)
	assert.Equal(t, f.userID, *items[0].ActorUserID)
}
