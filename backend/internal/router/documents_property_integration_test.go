//go:build integration

package router_test

import (
	"fmt"
	"math/big"
	"math/rand"
	"strings"
	"testing"

	"github.com/fpirim/fikopar/backend/internal/models"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// roundCents rounds a non-negative rational half-up to cents.
func roundCents(r *big.Rat) string {
	c := new(big.Rat).Mul(r, big.NewRat(100, 1))
	q, m := new(big.Int).QuoRem(c.Num(), c.Denom(), new(big.Int))
	if new(big.Int).Mul(m, big.NewInt(2)).Cmp(c.Denom()) >= 0 {
		q.Add(q, big.NewInt(1))
	}
	s := fmt.Sprintf("%03d", q)
	return s[:len(s)-2] + "." + s[len(s)-2:]
}

func rat(s string) *big.Rat {
	r, ok := new(big.Rat).SetString(s)
	if !ok {
		panic(s)
	}
	return r
}

func money(t *testing.T, s string) models.Money {
	t.Helper()
	m, err := models.ParseMoney(s)
	require.NoError(t, err)
	return m
}

func randDec(r *rand.Rand, maxWhole, frac int) string {
	whole := r.Intn(maxWhole)
	if frac == 0 || r.Intn(3) == 0 {
		return fmt.Sprint(whole)
	}
	return fmt.Sprintf("%d.%0*d", whole, frac, r.Intn(pow10(frac)))
}

func pow10(n int) int {
	p := 1
	for i := 0; i < n; i++ {
		p *= 10
	}
	return p
}

// Document totals are Σ of the lines rounded half-up one by one, from the unrounded product; the ledger entry of
// every posted document balances and carries the grand total on the card account.
func TestDocumentsProperty_TotalsAreSumOfRoundedLinesAndEntriesBalance(t *testing.T) {
	f := newDocs(t)
	r := rand.New(rand.NewSource(7))
	vats := []string{"0", "1", "8", "10", "18", "20"}
	for n := 0; n < 30; n++ {
		var lines []string
		type want struct{ gross, net, vat string }
		var wants []want
		var sumG, sumN, sumV models.Money
		for i := 0; i < 1+r.Intn(6); i++ {
			qty := randDec(r, 500, 4)
			if qty == "0" {
				qty = "0.0001"
			}
			price, d1, d2, vat := randDec(r, 5000, 4), randDec(r, 100, 2), randDec(r, 100, 2), vats[r.Intn(len(vats))]
			lines = append(lines, lineJSON(f.p1, qty, price, fmt.Sprintf(`,"discount1Pct":"%s","discount2Pct":"%s","vatPct":"%s"`, d1, d2, vat)))
			prod := new(big.Rat).Mul(rat(qty), rat(price))
			g := roundCents(prod)
			disc := new(big.Rat).Mul(new(big.Rat).Sub(big.NewRat(100, 1), rat(d1)), new(big.Rat).Sub(big.NewRat(100, 1), rat(d2)))
			net := roundCents(new(big.Rat).Quo(new(big.Rat).Mul(prod, disc), big.NewRat(10000, 1)))
			v := roundCents(new(big.Rat).Quo(new(big.Rat).Mul(rat(net), rat(vat)), big.NewRat(100, 1)))
			wants = append(wants, want{g, net, v})
			sumG += money(t, g)
			sumN += money(t, net)
			sumV += money(t, v)
		}
		d := f.create(t, docBody("PURCHASE_INVOICE", f.depot2, f.supp, "2026-04-01", "", lines...))
		for i, w := range wants {
			l := d.Lines[i]
			require.Equal(t, []string{w.gross, w.net, w.vat}, []string{l.GrossAmount, l.NetAmount, l.VatAmount}, "line %d of %s", i, strings.Join(lines, ","))
			assert.Equal(t, (money(t, w.gross) - money(t, w.net)).String(), l.DiscountAmount)
		}
		assert.Equal(t, sumG.String(), d.GrossTotal)
		assert.Equal(t, sumN.String(), d.NetTotal)
		assert.Equal(t, sumV.String(), d.VatTotal)
		assert.Equal(t, (sumN + sumV).String(), d.GrandTotal)
		var vb models.Money
		for _, b := range d.VatBreakdown {
			vb += money(t, b.Vat)
		}
		assert.Equal(t, sumV, vb, "VAT breakdown sums the lines")

		if n%3 == 0 {
			p := f.postDoc(t, d)
			if p.JournalEntryID != nil {
				var dr, cr models.Money
				for _, row := range entryPostings(t, *p.JournalEntryID) {
					if row.Side == "DEBIT" {
						dr += row.Amount
					} else {
						cr += row.Amount
					}
					if row.Code == "C" {
						assert.Equal(t, p.GrandTotal, row.Amount.String())
					}
				}
				assert.Equal(t, dr, cr, "Σ debit = Σ credit")
			}
		}
	}
	var unbalanced int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM (SELECT p.entry_id FROM postings p JOIN journal_entries e ON e.id = p.entry_id
		WHERE e.org_id = ? GROUP BY p.entry_id HAVING sum(CASE p.side WHEN 'DEBIT' THEN p.amount ELSE -p.amount END) <> 0) x`, f.org).Scan(&unbalanced).Error)
	assert.Zero(t, unbalanced)
}

// After random posts and cancels of every stock-moving type, on hand per part and depot equals Σ movements and
// the model the test keeps; every entry balances.
func TestDocumentsProperty_OnHandFollowsPostAndCancelCycles(t *testing.T) {
	f := newDocs(t)
	r := rand.New(rand.NewSource(11))
	type key struct{ part, depot uint }
	model := map[key]int{{f.p1, f.depot}: 10}
	sign := map[string]int{"SALES_INVOICE_WITH_WAYBILL": -1, "SALES_INVOICE": -1, "SALES_WAYBILL": -1, "PURCHASE_RETURN_WAYBILL": -1,
		"PURCHASE_INVOICE_WITH_WAYBILL": 1, "PURCHASE_INVOICE": 1, "PURCHASE_WAYBILL": 1, "SALES_RETURN_WAYBILL": 1}
	types := []string{"SALES_INVOICE_WITH_WAYBILL", "SALES_INVOICE", "SALES_WAYBILL", "PURCHASE_INVOICE_WITH_WAYBILL",
		"PURCHASE_INVOICE", "PURCHASE_WAYBILL"}
	parts := []uint{f.p1, f.p2}
	depots := []uint{f.depot, f.depot2}
	type posted struct {
		doc     docJSON
		deltas  map[key]int
		returns bool
	}
	var live []posted
	apply := func(deltas map[key]int, s int) {
		for k, v := range deltas {
			model[k] += s * v
		}
	}
	for step := 0; step < 40; step++ {
		switch {
		case len(live) > 0 && r.Intn(4) == 0:
			i := r.Intn(len(live))
			p := live[i]
			if p.returns {
				continue
			}
			rec := f.cancelRaw(t, p.doc.ID, p.doc.Version, "property run")
			require.Equal(t, 200, rec.Code, rec.Body.String())
			apply(p.deltas, -1)
			live = append(live[:i], live[i+1:]...)
		case len(live) > 0 && r.Intn(4) == 0:
			// A return waybill of one line of a posted sale or purchase; the original may no longer be cancelled.
			i := r.Intn(len(live))
			o := live[i]
			if !o.returns && (o.doc.Type == "SALES_INVOICE_WITH_WAYBILL" || o.doc.Type == "SALES_INVOICE" || o.doc.Type == "SALES_WAYBILL" ||
				o.doc.Type == "PURCHASE_INVOICE_WITH_WAYBILL" || o.doc.Type == "PURCHASE_INVOICE" || o.doc.Type == "PURCHASE_WAYBILL") {
				typ, card := "SALES_RETURN_WAYBILL", f.cust
				if sign[o.doc.Type] > 0 {
					typ, card = "PURCHASE_RETURN_WAYBILL", f.supp
				}
				ol := o.doc.Lines[0]
				f.posted(t, docBody(typ, o.doc.DepotID, card, "2026-05-01", fmt.Sprintf(`,"linkedDocumentId":%d`, o.doc.ID),
					lineJSON(*ol.PartID, "1", "1", fmt.Sprintf(`,"originLineId":%d`, ol.ID))))
				apply(map[key]int{{*ol.PartID, o.doc.DepotID}: sign[typ]}, 1)
				live[i].returns = true
			}
		default:
			typ := types[r.Intn(len(types))]
			card := f.cust
			if sign[typ] > 0 {
				card = f.supp
			}
			depot := depots[r.Intn(len(depots))]
			deltas := map[key]int{}
			var lines []string
			for j := 0; j < 1+r.Intn(3); j++ {
				part := parts[r.Intn(len(parts))]
				qty := 1 + r.Intn(5)
				lines = append(lines, lineJSON(part, fmt.Sprint(qty), "3", ""))
				deltas[key{part, depot}] += sign[typ] * qty
			}
			d := f.posted(t, docBody(typ, depot, card, "2026-04-01", "", lines...))
			apply(deltas, 1)
			live = append(live, posted{doc: d, deltas: deltas})
		}
		noDrift(t)
	}
	for k, want := range model {
		assert.Equal(t, fmt.Sprint(want), f.onHand(t, k.part, k.depot), "part %d depot %d", k.part, k.depot)
	}
	var unbalanced int64
	require.NoError(t, testDB.Raw(`SELECT count(*) FROM (SELECT p.entry_id FROM postings p JOIN journal_entries e ON e.id = p.entry_id
		WHERE e.org_id = ? GROUP BY p.entry_id HAVING sum(CASE p.side WHEN 'DEBIT' THEN p.amount ELSE -p.amount END) <> 0) x`, f.org).Scan(&unbalanced).Error)
	assert.Zero(t, unbalanced)
}
