import { describe, expect, it } from 'vitest'
import i18n from '../i18n'
import {
  addDays,
  billTypeFor,
  canPriceDiff,
  cardSign,
  centsToMoney,
  creditChecked,
  documentTotals,
  isPriceDiff,
  isReturn,
  isWaybill,
  lineAmounts,
  originTypes,
  parseDecimal,
  returnTypeFor,
  sideOf,
  stockDirection,
  toInput,
} from './documents'

describe('line amounts (ADR-0013 §7)', () => {
  it('rounds gross, net and VAT half-up per line', () => {
    expect(
      lineAmounts({
        qty: '2',
        unitPrice: '12400',
        discount1Pct: '10',
        discount2Pct: '0',
        vatPct: '20',
      }),
    ).toEqual({
      grossAmount: '24800.00',
      discountAmount: '2480.00',
      netAmount: '22320.00',
      vatAmount: '4464.00',
      lineTotal: '26784.00',
    })
  })

  it('takes net from the unrounded product, both discounts cascaded', () => {
    // 1 × 0.105 = 0.105 → gross 0.11; net 0.105 × 0.5 = 0.0525 → 0.05 (not 0.11 × 0.5 = 0.055 → 0.06)
    expect(
      lineAmounts({ qty: '1', unitPrice: '0.105', discount1Pct: '50', vatPct: '0' }),
    ).toMatchObject({ grossAmount: '0.11', netAmount: '0.05', discountAmount: '0.06' })
    // 7 × 1.2345 = 8.6415; × 0.95 × 0.97 = 7.9631… → 7.96 (cascaded, not 8 % off: 7.95)
    expect(
      lineAmounts({
        qty: '7',
        unitPrice: '1.2345',
        discount1Pct: '5',
        discount2Pct: '3',
        vatPct: '20',
      }),
    ).toMatchObject({
      grossAmount: '8.64',
      netAmount: '7.96',
      discountAmount: '0.68',
      vatAmount: '1.59',
    })
  })

  it('rounds .5 up, not to even', () => {
    // 1 × 0.125 = 0.125 → 0.13; VAT 10 % of 0.13 = 0.013 → 0.01
    expect(lineAmounts({ qty: '1', unitPrice: '0.125', vatPct: '10' })).toMatchObject({
      grossAmount: '0.13',
      netAmount: '0.13',
      vatAmount: '0.01',
    })
    // 0.5 × 0.05 = 0.025 → 0.03
    expect(lineAmounts({ qty: '0.5', unitPrice: '0.05', vatPct: '0' }).netAmount).toBe('0.03')
    // net 2.25, VAT 18 % = 0.405 → 0.41
    expect(lineAmounts({ qty: '1', unitPrice: '2.25', vatPct: '18' }).vatAmount).toBe('0.41')
  })

  it('keeps exact decimals where floats drift', () => {
    // 1.005 × 1 in floating point is 1.00499…; the line must still round to 1.01.
    expect(lineAmounts({ qty: '1', unitPrice: '1.005', vatPct: '0' }).grossAmount).toBe('1.01')
    expect(lineAmounts({ qty: '3', unitPrice: '0.1', vatPct: '20' })).toMatchObject({
      grossAmount: '0.30',
      vatAmount: '0.06',
    })
    expect(
      lineAmounts({ qty: '6', unitPrice: '375', discount2Pct: '4.4444', vatPct: '20' }).netAmount,
    ).toBe('2150.00')
  })

  it('handles large amounts without losing cents', () => {
    // 99 999 999 × 0.0005 = 49 999.9995 → 50 000.00; as floats the product is 49999.99949…
    expect(lineAmounts({ qty: '99999999', unitPrice: '0.0005', vatPct: '0' }).grossAmount).toBe(
      '50000.00',
    )
    expect(lineAmounts({ qty: '99999999', unitPrice: '99999.9999', vatPct: '20' })).toMatchObject({
      grossAmount: '9999999890000.00',
      vatAmount: '1999999978000.00',
    })
  })

  it('treats a 100 % discount as zero net', () => {
    expect(
      lineAmounts({ qty: '2', unitPrice: '10', discount1Pct: '100', vatPct: '20' }),
    ).toMatchObject({
      grossAmount: '20.00',
      discountAmount: '20.00',
      netAmount: '0.00',
      vatAmount: '0.00',
    })
  })
})

describe('document totals', () => {
  const line = (qty: string, price: string, d1: string, d2: string, vat: string) => ({
    ...lineAmounts({ qty, unitPrice: price, discount1Pct: d1, discount2Pct: d2, vatPct: vat }),
    vatPct: vat,
  })

  it('sums lines and breaks VAT down per rate, highest first', () => {
    const t = documentTotals([
      line('2', '12400', '10', '0', '20'),
      line('4', '2850', '5', '0', '20'),
      line('20', '265', '0', '0', '20'),
      line('6', '375', '0', '4.4444', '20'),
      line('3', '100', '0', '0', '10'),
      line('1', '50', '0', '0', '10.00'),
    ])
    expect(t).toEqual({
      grossTotal: '44100.00',
      discountTotal: '3150.00',
      netTotal: '40950.00',
      vatTotal: '8155.00',
      grandTotal: '49105.00',
      vatBreakdown: [
        { vatPct: '20', base: '40600.00', vat: '8120.00' },
        { vatPct: '10', base: '350.00', vat: '35.00' },
      ],
    })
  })

  it('is Σ of rounded lines, not a rounded Σ', () => {
    const t = documentTotals([line('1', '0.125', '0', '0', '0'), line('1', '0.125', '0', '0', '0')])
    expect(t.netTotal).toBe('0.26')
  })

  it('is zero without lines', () => {
    expect(documentTotals([])).toMatchObject({ grandTotal: '0.00', vatBreakdown: [] })
  })

  it('formats cents', () => {
    expect(centsToMoney(-5n)).toBe('-0.05')
    expect(centsToMoney(123456n)).toBe('1234.56')
  })
})

describe('document types', () => {
  it('knows sides, waybills, returns and price differences', () => {
    expect(sideOf('SALES_RETURN_WAYBILL')).toBe('SALES')
    expect(sideOf('PURCHASE_INVOICE')).toBe('PURCHASE')
    expect(sideOf('PRICE_DIFFERENCE')).toBeNull()
    expect(sideOf('PRICE_DIFFERENCE', 'PURCHASE_INVOICE_WITH_WAYBILL')).toBe('PURCHASE')
    expect(isWaybill('SALES_WAYBILL')).toBe(true)
    expect(isWaybill('PURCHASE_RETURN_WAYBILL')).toBe(true)
    expect(isWaybill('SALES_INVOICE_WITH_WAYBILL')).toBe(false)
    expect(isReturn('PURCHASE_RETURN')).toBe(true)
    expect(isReturn('PURCHASE_INVOICE')).toBe(false)
    expect(isPriceDiff('PRICE_DIFFERENCE')).toBe(true)
  })

  it('moves stock by type, never on a bill or a price difference', () => {
    expect(stockDirection('SALES_INVOICE')).toBe('OUT')
    expect(stockDirection('SALES_INVOICE', true)).toBeNull()
    expect(stockDirection('SALES_INVOICE_WITH_WAYBILL', true)).toBe('OUT')
    expect(stockDirection('SALES_RETURN')).toBe('IN')
    expect(stockDirection('SALES_RETURN', true)).toBeNull()
    expect(stockDirection('SALES_RETURN_WAYBILL')).toBe('IN')
    expect(stockDirection('PURCHASE_RETURN_WAYBILL')).toBe('OUT')
    expect(stockDirection('PURCHASE_WAYBILL')).toBe('IN')
    expect(stockDirection('PRICE_DIFFERENCE')).toBeNull()
  })

  it('debits or credits the card per the posting table', () => {
    expect(cardSign('SALES_INVOICE')).toBe(1)
    expect(cardSign('SALES_RETURN')).toBe(-1)
    expect(cardSign('PURCHASE_INVOICE_WITH_WAYBILL')).toBe(-1)
    expect(cardSign('PURCHASE_RETURN')).toBe(1)
    expect(cardSign('SALES_WAYBILL')).toBe(0)
    expect(cardSign('PRICE_DIFFERENCE', 'INCREASE', 'SALES_INVOICE')).toBe(1)
    expect(cardSign('PRICE_DIFFERENCE', 'DECREASE', 'SALES_INVOICE')).toBe(-1)
    expect(cardSign('PRICE_DIFFERENCE', 'INCREASE', 'PURCHASE_INVOICE')).toBe(-1)
    expect(cardSign('PRICE_DIFFERENCE', 'DECREASE', 'PURCHASE_INVOICE')).toBe(1)
    expect(cardSign('PRICE_DIFFERENCE', null, 'PURCHASE_INVOICE')).toBe(0)
    expect(cardSign('PRICE_DIFFERENCE', 'INCREASE', null)).toBe(0)
  })

  it('checks the credit limit on sales invoices and increases on a sale', () => {
    expect(creditChecked('SALES_INVOICE')).toBe(true)
    expect(creditChecked('SALES_INVOICE_WITH_WAYBILL')).toBe(true)
    expect(creditChecked('SALES_WAYBILL')).toBe(false)
    expect(creditChecked('PRICE_DIFFERENCE', 'INCREASE', 'SALES_INVOICE')).toBe(true)
    expect(creditChecked('PRICE_DIFFERENCE', 'DECREASE', 'SALES_INVOICE')).toBe(false)
    expect(creditChecked('PRICE_DIFFERENCE', 'INCREASE', 'PURCHASE_INVOICE')).toBe(false)
  })

  it('links originals, bills and follow-ups', () => {
    expect(originTypes('SALES_RETURN')).toEqual([
      'SALES_INVOICE_WITH_WAYBILL',
      'SALES_INVOICE',
      'SALES_WAYBILL',
    ])
    expect(originTypes('PURCHASE_RETURN_WAYBILL')).toContain('PURCHASE_WAYBILL')
    expect(originTypes('PRICE_DIFFERENCE')).not.toContain('SALES_WAYBILL')
    expect(originTypes('SALES_INVOICE')).toEqual([])
    expect(billTypeFor('SALES_WAYBILL')).toBe('SALES_INVOICE')
    expect(billTypeFor('PURCHASE_RETURN_WAYBILL')).toBe('PURCHASE_RETURN')
    expect(billTypeFor('SALES_INVOICE')).toBeNull()
    expect(returnTypeFor('SALES_WAYBILL')).toBe('SALES_RETURN')
    expect(returnTypeFor('PURCHASE_INVOICE_WITH_WAYBILL')).toBe('PURCHASE_RETURN')
    expect(returnTypeFor('SALES_RETURN')).toBeNull()
    expect(canPriceDiff('PURCHASE_INVOICE')).toBe(true)
    expect(canPriceDiff('PURCHASE_WAYBILL')).toBe(false)
  })

  it('adds the payment term across month and year ends', () => {
    expect(addDays('2026-09-29', 30)).toBe('2026-10-29')
    expect(addDays('2026-12-15', 30)).toBe('2027-01-14')
    expect(addDays('2026-02-28', 1)).toBe('2026-03-01')
    expect(addDays('2026-09-29', 0)).toBe('2026-09-29')
  })
})

describe('number entry per language', () => {
  it('reads Turkish and English separators', async () => {
    await i18n.changeLanguage('tr')
    expect(parseDecimal('12.400,50', 4)).toBe('12400.5')
    expect(parseDecimal('2,5', 4)).toBe('2.5')
    expect(parseDecimal('2.5', 4)).toBeNull()
    expect(parseDecimal('1,23456', 4)).toBeNull()
    expect(parseDecimal('', 4)).toBe('')
    expect(parseDecimal('-3', 4)).toBeNull()
    expect(parseDecimal('101', 4, { max: 100 })).toBeNull()
    expect(toInput('12400.5')).toBe('12400,5')
    await i18n.changeLanguage('en')
    expect(parseDecimal('12,400.50', 2)).toBe('12400.5')
    expect(parseDecimal('2,5', 4)).toBeNull()
    expect(parseDecimal('abc', 2)).toBeNull()
    expect(toInput('4.4444')).toBe('4.4444')
    expect(toInput(null)).toBe('')
  })
})
