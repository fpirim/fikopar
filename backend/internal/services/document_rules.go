package services

import (
	"math/big"

	"github.com/fpirim/fikopar/backend/internal/models"
)

const (
	typeSalesInvoiceWaybill    = "SALES_INVOICE_WITH_WAYBILL"
	typeSalesInvoice           = "SALES_INVOICE"
	typeSalesWaybill           = "SALES_WAYBILL"
	typeSalesReturn            = "SALES_RETURN"
	typeSalesReturnWaybill     = "SALES_RETURN_WAYBILL"
	typePurchaseInvoiceWaybill = "PURCHASE_INVOICE_WITH_WAYBILL"
	typePurchaseInvoice        = "PURCHASE_INVOICE"
	typePurchaseWaybill        = "PURCHASE_WAYBILL"
	typePurchaseReturn         = "PURCHASE_RETURN"
	typePurchaseReturnWaybill  = "PURCHASE_RETURN_WAYBILL"
	typePriceDifference        = "PRICE_DIFFERENCE"

	dirSales    = "SALES"
	dirPurchase = "PURCHASE"
	increase    = "INCREASE"
	decrease    = "DECREASE"

	statusDraft     = "DRAFT"
	statusPosted    = "POSTED"
	statusCancelled = "CANCELLED"
)

// docKind is what a document type does (ADR-0013 §2–§6).
type docKind struct {
	prefix    string
	stock     string // OUT, IN or "" (never)
	ledger    bool
	direction string // SALES / PURCHASE; a price difference takes it from its original
	bills     string // waybill type it may bill
	originals []string
}

var (
	salesOriginals    = []string{typeSalesInvoiceWaybill, typeSalesInvoice, typeSalesWaybill}
	purchaseOriginals = []string{typePurchaseInvoiceWaybill, typePurchaseInvoice, typePurchaseWaybill}
	invoiceTypes      = []string{typeSalesInvoiceWaybill, typeSalesInvoice, typePurchaseInvoiceWaybill, typePurchaseInvoice}
)

var docKinds = map[string]docKind{
	typeSalesInvoiceWaybill:    {prefix: "SF", stock: models.MoveOut, ledger: true, direction: dirSales},
	typeSalesInvoice:           {prefix: "SF", stock: models.MoveOut, ledger: true, direction: dirSales, bills: typeSalesWaybill},
	typeSalesWaybill:           {prefix: "SIR", stock: models.MoveOut, direction: dirSales},
	typeSalesReturn:            {prefix: "SI", stock: models.MoveIn, ledger: true, direction: dirSales, bills: typeSalesReturnWaybill, originals: salesOriginals},
	typeSalesReturnWaybill:     {prefix: "SIRI", stock: models.MoveIn, direction: dirSales, originals: salesOriginals},
	typePurchaseInvoiceWaybill: {prefix: "AF", stock: models.MoveIn, ledger: true, direction: dirPurchase},
	typePurchaseInvoice:        {prefix: "AF", stock: models.MoveIn, ledger: true, direction: dirPurchase, bills: typePurchaseWaybill},
	typePurchaseWaybill:        {prefix: "AIR", stock: models.MoveIn, direction: dirPurchase},
	typePurchaseReturn:         {prefix: "AI", stock: models.MoveOut, ledger: true, direction: dirPurchase, bills: typePurchaseReturnWaybill, originals: purchaseOriginals},
	typePurchaseReturnWaybill:  {prefix: "AIRI", stock: models.MoveOut, direction: dirPurchase, originals: purchaseOriginals},
	typePriceDifference:        {prefix: "FF", ledger: true},
}

func (k docKind) isReturn() bool { return len(k.originals) > 0 }

func contains(list []string, v string) bool {
	for _, x := range list {
		if x == v {
			return true
		}
	}
	return false
}

func opposite(kind string) string {
	if kind == models.MoveIn {
		return models.MoveOut
	}
	return models.MoveIn
}

// movesStock: the type's direction, unless the document bills a waybill.
func movesStock(typ string, waybillID *uint) string {
	if waybillID != nil {
		return ""
	}
	return docKinds[typ].stock
}

// reserves: a draft that will move stock OUT holds it (D8).
func reserves(typ string, waybillID *uint) bool { return movesStock(typ, waybillID) == models.MoveOut }

// ledgerLine is one posting of the ADR-0013 table; role "" is the card's current account.
type ledgerLine struct {
	role   string
	side   models.Side
	amount models.Money
}

type docAmounts struct{ gross, discount, net, vat, total models.Money }

// postingLines is the ADR-0013 posting table; zero amounts are left out.
func postingLines(typ, direction, priceDirection string, a docAmounts) []ledgerLine {
	d, c := models.Debit, models.Credit
	var all []ledgerLine
	switch {
	case typ == typeSalesInvoiceWaybill || typ == typeSalesInvoice:
		all = []ledgerLine{{"", d, a.total}, {"SALES_DISCOUNTS", d, a.discount}, {"SALES", c, a.gross}, {"OUTPUT_VAT", c, a.vat}}
	case typ == typeSalesReturn:
		all = []ledgerLine{{"SALES_RETURNS", d, a.net}, {"OUTPUT_VAT", d, a.vat}, {"", c, a.total}}
	case typ == typePurchaseInvoiceWaybill || typ == typePurchaseInvoice:
		all = []ledgerLine{{"PURCHASES", d, a.net}, {"INPUT_VAT", d, a.vat}, {"", c, a.total}}
	case typ == typePurchaseReturn:
		all = []ledgerLine{{"", d, a.total}, {"PURCHASE_RETURNS", c, a.net}, {"INPUT_VAT", c, a.vat}}
	case typ == typePriceDifference && direction == dirSales && priceDirection == increase:
		all = []ledgerLine{{"", d, a.total}, {"PRICE_DIFFERENCE", c, a.net}, {"OUTPUT_VAT", c, a.vat}}
	case typ == typePriceDifference && direction == dirSales:
		all = []ledgerLine{{"SALES_DISCOUNTS", d, a.net}, {"OUTPUT_VAT", d, a.vat}, {"", c, a.total}}
	case typ == typePriceDifference && priceDirection == increase:
		all = []ledgerLine{{"PURCHASES", d, a.net}, {"INPUT_VAT", d, a.vat}, {"", c, a.total}}
	case typ == typePriceDifference:
		all = []ledgerLine{{"", d, a.total}, {"PURCHASES", c, a.net}, {"INPUT_VAT", c, a.vat}}
	}
	out := make([]ledgerLine, 0, len(all))
	for _, l := range all {
		if l.amount != 0 {
			out = append(out, l)
		}
	}
	return out
}

// cardDebit is the signed effect on the card's current account (debit positive).
func cardDebit(typ, direction, priceDirection string, total models.Money) models.Money {
	for _, l := range postingLines(typ, direction, priceDirection, docAmounts{total: total}) {
		if l.role == "" && l.side == models.Debit {
			return total
		}
		if l.role == "" {
			return -total
		}
	}
	return 0
}

// creditChecked: SF, SF with waybill and a price increase on a sale (ADR-0013 §9).
func creditChecked(typ, direction, priceDirection string) bool {
	return typ == typeSalesInvoiceWaybill || typ == typeSalesInvoice ||
		(typ == typePriceDifference && direction == dirSales && priceDirection == increase)
}

var (
	million     = big.NewInt(1_000_000)
	pctHundred  = models.Dec4(1_000_000)
	quintillion = new(big.Int).Exp(big.NewInt(10), big.NewInt(18), nil)
)

// divRound divides a non-negative numerator half-up.
func divRound(n, d *big.Int) models.Money {
	q, r := new(big.Int).QuoRem(n, d, new(big.Int))
	if new(big.Int).Mul(r, big.NewInt(2)).Cmp(d) >= 0 {
		q.Add(q, big.NewInt(1))
	}
	return models.Money(q.Int64())
}

// lineAmounts is the v1.4 rounding rule, per line and half-up to cents: gross = round(qty × price); net =
// round(qty × price × (1 − d1) × (1 − d2)) from the unrounded product; discount = gross − net; VAT = round(net × VAT%).
func lineAmounts(qty, price, d1, d2, vat models.Dec4) (gross, discount, net, tax models.Money) {
	gross = qty.Times(price)
	p := new(big.Int).Mul(big.NewInt(int64(qty)), big.NewInt(int64(price)))
	p.Mul(p, big.NewInt(int64(pctHundred-d1)))
	p.Mul(p, big.NewInt(int64(pctHundred-d2)))
	net = divRound(p, quintillion)
	tax = divRound(new(big.Int).Mul(big.NewInt(int64(net)), big.NewInt(int64(vat))), million)
	return gross, gross - net, net, tax
}
