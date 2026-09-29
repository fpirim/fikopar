import { describe, expect, it } from 'vitest'
import type { CatalogHit, SicilSummary } from '../../api/types'
import {
  availableHere,
  blankHeader,
  blankLine,
  dueFor,
  isFilled,
  previewEffects,
  stockTone,
  toWrite,
  totalsOf,
  withPart,
} from './model'

const card = {
  sicilId: 1,
  accountId: 5,
  accountCode: '120.1',
  balance: '186340.00',
  overdue: '0.00',
  creditLimit: '250000.00',
  creditUsedPct: 75,
  paymentTermDays: 30,
  discount1Pct: '10',
  discount2Pct: '5',
  chequesInPortfolio: '0.00',
} as SicilSummary
const account = { sicilId: 1, accountId: 5, hesapNo: '120.1', hesapAdi: 'Card' }
const hit = {
  partId: 9,
  stockNo: 'HP-1',
  name: 'Pump',
  listPrice: '100.0000',
  vatPct: '10',
  stock: [
    { depotId: 1, onHand: '5', available: '4' },
    { depotId: 2, onHand: '0', available: '0' },
  ],
} as unknown as CatalogHit

describe('document model', () => {
  it('fills a picked part with list price, part VAT and card discounts; a re-pick keeps edits', () => {
    const l = withPart(blankLine(), hit, card)
    expect(l).toMatchObject({
      partId: 9,
      qty: '1',
      unitPrice: '100.0000',
      discount1Pct: '10',
      discount2Pct: '5',
      vatPct: '10',
    })
    const edited = { ...l, unitPrice: '90', discount1Pct: '0', qty: '3' }
    expect(withPart(edited, hit, card)).toMatchObject({
      unitPrice: '90',
      discount1Pct: '0',
      qty: '3',
    })
    expect(withPart(blankLine(), { ...hit, listPrice: null }, null)).toMatchObject({
      unitPrice: '0',
      discount1Pct: '0',
    })
  })

  it('writes goods lines with parts and amount-only price-difference lines', () => {
    const h = { ...blankHeader('SALES_INVOICE', 1, '2026-09-29'), account, dueDate: '2026-10-29' }
    const lines = [withPart(blankLine(), hit, card), blankLine()]
    const body = toWrite(h, lines)
    expect(body.lines).toHaveLength(1)
    expect(body).toMatchObject({ sicilId: 1, linkedDocumentId: null, priceDirection: null })

    const ff = { ...blankHeader('PRICE_DIFFERENCE', 1, '2026-09-29'), account, linkedDocumentId: 7 }
    const w = toWrite(ff, [
      blankLine({ originLineId: 70, unitPrice: '50', vatPct: '20' }),
      blankLine({ description: 'Rebate', unitPrice: '10' }),
      blankLine({ description: 'No amount' }),
    ])
    expect(w).toMatchObject({ priceDirection: 'INCREASE', linkedDocumentId: 7, shipDate: null })
    expect(w.lines).toEqual([
      { qty: '1', unitPrice: '50', vatPct: '20', originLineId: 70 },
      {
        partId: null,
        description: 'Rebate',
        qty: '1',
        unitPrice: '10',
        vatPct: '20',
        originLineId: null,
      },
    ])

    const wb = { ...blankHeader('SALES_WAYBILL', 1, '2026-09-29'), account, dueDate: '2026-10-01' }
    expect(toWrite(wb, [])).toMatchObject({ dueDate: null, waybillDocumentId: null })
  })

  it('knows filled lines per type', () => {
    expect(isFilled(blankLine({ partId: 1, qty: '1' }), 'SALES_INVOICE')).toBe(true)
    expect(isFilled(blankLine({ partId: 1 }), 'SALES_INVOICE')).toBe(false)
    expect(isFilled(blankLine({ unitPrice: '5' }), 'PRICE_DIFFERENCE')).toBe(false)
    expect(isFilled(blankLine({ unitPrice: '5', originLineId: 3 }), 'PRICE_DIFFERENCE')).toBe(true)
  })

  it('moves the due date with the date until typed', () => {
    const h = blankHeader('SALES_INVOICE', 1, '2026-12-15')
    expect(dueFor(h, 30)).toBe('2027-01-14')
    expect(dueFor(h, null)).toBe('2026-12-15')
    expect(dueFor({ ...h, dueTouched: true, dueDate: '2027-02-01' }, 30)).toBe('2027-02-01')
    expect(dueFor(blankHeader('PURCHASE_WAYBILL', 1, '2026-12-15'), 30)).toBeNull()
  })

  it('colours stock by what the line may take, its own hold included', () => {
    const l = withPart(blankLine(), hit, card)
    expect(availableHere({ ...l, reservedQty: '2' }, 1)).toBe(6)
    expect(availableHere(l, 3)).toBeNull()
    expect(stockTone({ ...l, qty: '4' }, 1)).toBe('ok')
    expect(stockTone({ ...l, qty: '5' }, 1)).toBe('short')
    expect(stockTone({ ...l, qty: '1' }, 2)).toBe('none')
    expect(stockTone({ ...l, qty: '' }, 1)).toBeNull()
  })

  it('previews the card posting, stock and credit used after', () => {
    const h = { ...blankHeader('SALES_INVOICE', 1, '2026-09-29'), account }
    const lines = [withPart(blankLine({ qty: '' }), hit, card)].map((l) => ({ ...l, qty: '3' }))
    const t = totalsOf(lines, h.type)
    expect(t.grandTotal).toBe('282.15')
    expect(previewEffects(h, lines, t, card, null)).toEqual({
      ledger: [
        { role: null, accountCode: '120.1', accountName: 'Card', side: 'DEBIT', amount: '282.15' },
      ],
      stock: [{ depotId: 1, qtyDelta: '-3' }],
      creditUsedPctAfter: 75,
    })
    const ret = { ...h, type: 'SALES_RETURN' as const }
    expect(previewEffects(ret, lines, t, card, null)).toMatchObject({
      ledger: [{ side: 'CREDIT' }],
      stock: [{ qtyDelta: '3' }],
    })
    const bill = { ...h, waybillDocumentId: 4 }
    expect(previewEffects(bill, lines, t, card, null).stock).toEqual([])
    const wb = { ...h, type: 'SALES_WAYBILL' as const }
    expect(previewEffects(wb, lines, t, card, null)).toMatchObject({
      ledger: [],
      creditUsedPctAfter: null,
    })
    expect(previewEffects(h, lines, t, null, null).ledger).toEqual([])
  })
})
