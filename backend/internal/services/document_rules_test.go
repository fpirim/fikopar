package services

import (
	"testing"

	"github.com/fpirim/fikopar/backend/internal/models"
	"github.com/fpirim/fikopar/backend/internal/repository"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func dec(t *testing.T, s string) models.Dec4 {
	t.Helper()
	d, err := models.ParseDec4(s)
	require.NoError(t, err)
	return d
}

func decPtr(t *testing.T, s string) *models.Dec4 {
	d := dec(t, s)
	return &d
}

func TestLineAmounts_HalfUpPerLineFromTheUnroundedProduct(t *testing.T) {
	for _, c := range []struct {
		qty, price, d1, d2, vat string
		want                    [4]string
	}{
		{"3", "12.3456", "10", "5", "20", [4]string{"37.04", "5.37", "31.67", "6.33"}},
		{"1", "0.125", "0", "0", "0", [4]string{"0.13", "0.00", "0.13", "0.00"}},
		{"2", "100", "100", "0", "20", [4]string{"200.00", "200.00", "0.00", "0.00"}},
		{"0.0001", "0.0001", "0", "0", "18", [4]string{"0.00", "0.00", "0.00", "0.00"}},
		{"7", "3.3333", "12.5", "2.25", "8", [4]string{"23.33", "3.37", "19.96", "1.60"}},
	} {
		g, d, n, v := lineAmounts(dec(t, c.qty), dec(t, c.price), dec(t, c.d1), dec(t, c.d2), dec(t, c.vat))
		assert.Equal(t, c.want, [4]string{g.String(), d.String(), n.String(), v.String()}, "%+v", c)
	}
}

func TestPostingLines_BalanceAndCarryTheCardTotal(t *testing.T) {
	a := docAmounts{gross: 20000, discount: 2000, net: 18000, vat: 3600, total: 21600}
	for _, c := range []struct{ typ, dir, pd string }{
		{typeSalesInvoiceWaybill, dirSales, ""}, {typeSalesInvoice, dirSales, ""}, {typeSalesReturn, dirSales, ""},
		{typePurchaseInvoiceWaybill, dirPurchase, ""}, {typePurchaseInvoice, dirPurchase, ""}, {typePurchaseReturn, dirPurchase, ""},
		{typePriceDifference, dirSales, increase}, {typePriceDifference, dirSales, decrease},
		{typePriceDifference, dirPurchase, increase}, {typePriceDifference, dirPurchase, decrease},
	} {
		var dr, cr, card models.Money
		for _, l := range postingLines(c.typ, c.dir, c.pd, a) {
			if l.side == models.Debit {
				dr += l.amount
			} else {
				cr += l.amount
			}
			if l.role == "" {
				card = l.amount
			}
		}
		assert.Equal(t, dr, cr, "%+v", c)
		assert.Equal(t, a.total, card, "%+v", c)
	}
	assert.Empty(t, postingLines(typeSalesWaybill, dirSales, "", a), "waybills never post")
	sales := postingLines(typeSalesInvoice, dirSales, "", docAmounts{gross: 100, net: 100, total: 100})
	assert.Len(t, sales, 2, "zero discount and VAT are left out")
}

func TestCardDebitAndCreditCheckedTypes(t *testing.T) {
	assert.Equal(t, models.Money(100), cardDebit(typeSalesInvoice, dirSales, "", 100))
	assert.Equal(t, models.Money(-100), cardDebit(typeSalesReturn, dirSales, "", 100))
	assert.Equal(t, models.Money(-100), cardDebit(typePurchaseInvoice, dirPurchase, "", 100))
	assert.Equal(t, models.Money(100), cardDebit(typePurchaseReturn, dirPurchase, "", 100))
	assert.Equal(t, models.Money(100), cardDebit(typePriceDifference, dirSales, increase, 100))
	assert.Equal(t, models.Money(-100), cardDebit(typePriceDifference, dirSales, decrease, 100))
	assert.Equal(t, models.Money(0), cardDebit(typeSalesWaybill, dirSales, "", 100))
	assert.True(t, creditChecked(typeSalesInvoiceWaybill, dirSales, ""))
	assert.True(t, creditChecked(typePriceDifference, dirSales, increase))
	assert.False(t, creditChecked(typePriceDifference, dirSales, decrease))
	assert.False(t, creditChecked(typePriceDifference, dirPurchase, increase))
	assert.False(t, creditChecked(typeSalesWaybill, dirSales, ""))
}

func TestStockDirection(t *testing.T) {
	id := uint(1)
	assert.Equal(t, models.MoveOut, movesStock(typeSalesWaybill, nil))
	assert.Equal(t, models.MoveIn, movesStock(typeSalesReturnWaybill, nil))
	assert.Equal(t, "", movesStock(typeSalesInvoice, &id), "a bill moves nothing")
	assert.Equal(t, "", movesStock(typePriceDifference, nil))
	assert.True(t, reserves(typePurchaseReturnWaybill, nil))
	assert.False(t, reserves(typePurchaseInvoice, nil))
	assert.Equal(t, models.MoveIn, opposite(models.MoveOut))
	assert.Equal(t, models.MoveOut, opposite(models.MoveIn))
}

func TestPaymentTermDays(t *testing.T) {
	for in, want := range map[string]int{"30": 30, " 7 ": 7, "": 0, "net 30": 0, "-5": 0} {
		assert.Equal(t, want, paymentTermDays(in), in)
	}
}

func TestReturnCapField(t *testing.T) {
	o := uint(42)
	lines := []repository.LineRow{{}, {OriginLineID: &o}}
	assert.Equal(t, "lines[1].qty", returnCapField(" original line 42 is returned beyond its quantity", lines))
	assert.Equal(t, "lines", returnCapField(" something else", lines))
}

func TestUnitCost(t *testing.T) {
	assert.Equal(t, "27", unitCost(10800, dec(t, "4")).String())
	assert.Equal(t, "3.3333", unitCost(1000, dec(t, "3")).String())
}

func TestValidateWrite(t *testing.T) {
	date, err := models.ParseDate("2026-03-01")
	require.NoError(t, err)
	part := uint(1)
	ok := DocumentLineWrite{PartID: &part, Qty: decPtr(t, "1"), UnitPrice: decPtr(t, "1")}
	base := DocumentWrite{Type: typeSalesInvoice, DepotID: 1, SicilID: 1, DocumentDate: date, Lines: []DocumentLineWrite{ok}}
	require.NoError(t, validateWrite(base))
	inc := increase
	for field, mutate := range map[string]func(*DocumentWrite){
		"type":             func(w *DocumentWrite) { w.Type = "X" },
		"sicilId":          func(w *DocumentWrite) { w.SicilID = 0 },
		"documentDate":     func(w *DocumentWrite) { w.DocumentDate = models.Date{} },
		"priceDirection":   func(w *DocumentWrite) { w.PriceDirection = &inc },
		"linkedDocumentId": func(w *DocumentWrite) { w.LinkedDocumentID = &part },
		"lines[0].unitPrice": func(w *DocumentWrite) {
			w.Lines = []DocumentLineWrite{{PartID: &part, Qty: decPtr(t, "1"), UnitPrice: decPtr(t, "-1")}}
		},
		"lines[0].discount2Pct": func(w *DocumentWrite) {
			w.Lines = []DocumentLineWrite{{PartID: &part, Qty: decPtr(t, "1"), UnitPrice: decPtr(t, "1"), Discount2Pct: decPtr(t, "100.01")}}
		},
		"lines[0].vatPct": func(w *DocumentWrite) {
			w.Lines = []DocumentLineWrite{{PartID: &part, Qty: decPtr(t, "1"), UnitPrice: decPtr(t, "1"), VatPct: decPtr(t, "-1")}}
		},
	} {
		w := base
		mutate(&w)
		err := validateWrite(w)
		require.Error(t, err, field)
		assert.Contains(t, err.Error(), "ERR_INVALID_REQUEST", field)
	}
	pd := base
	pd.Type, pd.PriceDirection = typePriceDifference, &inc
	pd.Lines = []DocumentLineWrite{{Qty: decPtr(t, "1"), UnitPrice: decPtr(t, "5"), VatPct: decPtr(t, "20"), Discount2Pct: decPtr(t, "1")}}
	assert.Error(t, validateWrite(pd))
	pd.Lines[0].Discount2Pct, pd.Lines[0].BilledLineID = nil, &part
	assert.Error(t, validateWrite(pd))
	pd.Lines[0].BilledLineID = nil
	assert.NoError(t, validateWrite(pd))
}
