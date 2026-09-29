import { describe, expect, it } from 'vitest'
import {
  cancelDocument,
  createDocument,
  deleteDocument,
  getDashboard,
  getDocument,
  getPart,
  getSicilSummary,
  getStockCount,
  gridDocuments,
  gridParts,
  gridStockCountLines,
  gridStockMovements,
  postDocument,
  updateDocument,
} from '../api/endpoints'
import { ApiError } from '../api/client'
import type { DocumentWrite } from '../api/types'

const key = () => crypto.randomUUID()
const code = async (p: Promise<unknown>) => {
  try {
    await p
    return 'OK'
  } catch (e) {
    return e instanceof ApiError ? `${e.status} ${e.code}${e.field ? ` ${e.field}` : ''}` : 'ERR'
  }
}

// The synthetic documents behave like ADR-0013 says, so the screens show real rules.
describe('document mocks', () => {
  it('totals the grid without cancelled documents and filters per request', async () => {
    const all = await gridDocuments(1, { startRow: 0, endRow: 100 })
    expect(all.totalCount).toBe(14)
    const live = all.rows.filter((r) => r.status !== 'CANCELLED')
    const sum = live.reduce((s, r) => s + Number(r.grandTotal), 0).toFixed(2)
    expect(all.totals?.grandTotal).toBe(sum)
    const drafts = await gridDocuments(1, { startRow: 0, endRow: 10, statuses: ['DRAFT'] })
    expect(drafts.rows.map((r) => r.id).sort()).toEqual([1001, 1002])
    const bad = await code(
      gridDocuments(1, { startRow: 0, endRow: 1, sortModel: [{ colId: 'nope' as never }] }),
    )
    expect(bad).toBe('400 ERR_INVALID_REQUEST colId')
  })

  it('caps returns per original line and decreases by what is left', async () => {
    const orig = await getDocument(1, 391)
    const wp = orig.lines[1]
    expect(wp.returnedQty).toBe('1')
    const body: DocumentWrite = {
      type: 'SALES_RETURN',
      depotId: 1,
      sicilId: orig.sicilId,
      documentDate: '2026-09-29',
      linkedDocumentId: 391,
      lines: [{ partId: wp.partId, qty: '4', unitPrice: '3420', originLineId: wp.id }],
    }
    const ret = await createDocument(1, body, key())
    expect(await code(postDocument(1, ret, key()))).toBe('422 ERR_RETURN_QTY_EXCEEDED lines[0].qty')
    const fixed = await updateDocument(1, ret, { ...body, lines: [{ ...body.lines[0], qty: '3' }] })
    expect(await code(postDocument(1, fixed, key()))).toBe('OK')
    expect((await getDocument(1, 391)).lines[1].returnedQty).toBe('4')

    const dec: DocumentWrite = {
      type: 'PRICE_DIFFERENCE',
      priceDirection: 'DECREASE',
      depotId: 2,
      sicilId: (await getDocument(1, 108)).sicilId,
      documentDate: '2026-09-29',
      linkedDocumentId: 108,
      lines: [{ partId: null, description: 'x', qty: '1', unitPrice: '34140.01', vatPct: '20' }],
    }
    const ff = await createDocument(1, dec, key())
    expect(await code(postDocument(1, ff, key()))).toBe('422 ERR_PRICE_DIFFERENCE_EXCEEDED lines')
    const ok = await updateDocument(1, ff, {
      ...dec,
      lines: [{ ...dec.lines[0], unitPrice: '34140' }],
    })
    expect(await code(postDocument(1, ok, key()))).toBe('OK')
  })

  it('refuses bad links, a second bill, stale versions and closed years', async () => {
    const wb = await getDocument(1, 52)
    const bill: DocumentWrite = {
      type: 'SALES_INVOICE',
      depotId: 1,
      sicilId: wb.sicilId,
      documentDate: '2026-09-29',
      waybillDocumentId: 52,
      lines: [],
    }
    const first = await createDocument(1, bill, key())
    expect(first.lines.map((l) => l.billedLineId)).toEqual(wb.lines.map((l) => l.id))
    expect(first.effects?.stock).toEqual([])
    expect(await code(createDocument(1, bill, key()))).toBe('409 ERR_WAYBILL_BILLED')
    expect(await code(updateDocument(1, { ...first, version: 99 }, bill))).toBe(
      '412 ERR_VERSION_CONFLICT',
    )
    expect(
      await code(
        createDocument(1, { ...bill, waybillDocumentId: null, documentDate: '2025-12-31' }, key()),
      ),
    ).toBe('409 ERR_YEAR_CLOSED')
    expect(
      await code(
        createDocument(
          1,
          { ...bill, type: 'SALES_RETURN', waybillDocumentId: null, linkedDocumentId: 140 },
          key(),
        ),
      ),
    ).toBe('422 ERR_LINKED_DOCUMENT_INVALID linkedDocumentId')
    expect(await code(createDocument(1, { ...bill, priceDirection: 'INCREASE' }, key()))).toBe(
      '400 ERR_INVALID_REQUEST priceDirection',
    )
    expect(await code(cancelDocument(1, wb, 'Too early', key()))).toBe('409 ERR_WAYBILL_BILLED')
  })

  it('holds stock for an OUT draft and releases it on post', async () => {
    const d = await getDocument(1, 1001)
    const saved = await updateDocument(1, d, {
      type: d.type,
      depotId: 1,
      sicilId: d.sicilId,
      documentDate: d.documentDate,
      lines: d.lines.map((l) => ({
        partId: l.partId,
        qty: l.qty,
        unitPrice: l.unitPrice,
        discount1Pct: l.discount1Pct,
        discount2Pct: l.discount2Pct,
        vatPct: l.vatPct,
      })),
    })
    expect(saved.reservation?.expiresAt).toBeTruthy()
    expect(saved.lines.map((l) => l.reservedQty)).toEqual(['2', '3', '12', '0'])
    expect(saved.warnings.filter((w) => w.code === 'STOCK_SHORT').map((w) => w.lineNo)).toEqual([
      2, 3, 4,
    ])
    const posted = await postDocument(1, saved, key())
    expect(posted.reservation).toBeNull()
    expect(posted.warnings.map((w) => w.code)).toContain('NEGATIVE_STOCK')
    const moves = await gridStockMovements(1, {
      startRow: 0,
      endRow: 5,
      filterModel: { sourceNo: { filterType: 'text', type: 'equals', filter: posted.documentNo! } },
    })
    expect(moves.rows[0]).toMatchObject({ documentId: 1001, label: 'SALE', reversal: false })
  })

  it('values an open count at the last posted purchase cost', async () => {
    const count = await getStockCount(1, 2)
    const lines = await gridStockCountLines(1, count.id, {
      startRow: 0,
      endRow: 5,
      filterModel: { stockNo: { filterType: 'text', type: 'equals', filter: 'HP-3774' } },
    })
    expect(lines.rows[0]).toMatchObject({ unitCost: '8900.00', costSource: 'LAST_PURCHASE' })
  })
})

describe('document mocks, contracts v1.5', () => {
  const ids = (rows: { id: number }[]) => rows.map((r) => r.id).sort((a, b) => a - b)

  it('filters waybills by billed and documents by billing a waybill', async () => {
    const wb = await getDocument(1, 52)
    const open = await gridDocuments(1, { startRow: 0, endRow: 100, billed: false })
    expect(ids(open.rows)).toEqual([52])
    const bill = await createDocument(
      1,
      {
        type: 'SALES_INVOICE',
        depotId: 1,
        sicilId: wb.sicilId,
        documentDate: '2026-09-29',
        waybillDocumentId: 52,
        lines: [],
      },
      key(),
    )
    expect((await gridDocuments(1, { startRow: 0, endRow: 100, billed: false })).rows).toEqual([])
    expect(ids((await gridDocuments(1, { startRow: 0, endRow: 100, billed: true })).rows)).toEqual([
      52,
    ])
    const bills = await gridDocuments(1, { startRow: 0, endRow: 100, billsWaybill: true })
    expect(ids(bills.rows)).toEqual([bill.id])
    const rest = await gridDocuments(1, { startRow: 0, endRow: 100, billsWaybill: false })
    expect(rest.rows.map((r) => r.id)).not.toContain(bill.id)
    expect(rest.totalCount).toBe(14)
  })

  it('names the dependents of a cancel and of a draft another draft bills', async () => {
    const orig = await getDocument(1, 391)
    try {
      await cancelDocument(1, orig, 'Duplicate', key())
      expect.unreachable()
    } catch (e) {
      expect(e).toBeInstanceOf(ApiError)
      expect((e as ApiError).details).toEqual({
        dependents: expect.arrayContaining([
          { id: 12, documentNo: 'SI-2026-1-000012', type: 'SALES_RETURN' },
          { id: 7, documentNo: 'FF-2026-1-000007', type: 'PRICE_DIFFERENCE' },
        ]),
      })
    }
    const wbBody: DocumentWrite = {
      type: 'SALES_WAYBILL',
      depotId: 1,
      sicilId: orig.sicilId,
      documentDate: '2026-09-29',
      lines: [{ partId: orig.lines[0].partId, qty: '1', unitPrice: '10' }],
    }
    const wb = await createDocument(1, wbBody, key())
    expect(wb.hesapNo).toBe('120.10482')
    const bill = await createDocument(
      1,
      { ...wbBody, type: 'SALES_INVOICE', waybillDocumentId: wb.id, lines: [] },
      key(),
    )
    const dependents = [{ id: bill.id, documentNo: null, type: 'SALES_INVOICE' }]
    for (const call of [() => deleteDocument(1, wb), () => updateDocument(1, wb, wbBody)]) {
      const e = await call().catch((x: unknown) => x)
      expect(e).toBeInstanceOf(ApiError)
      expect((e as ApiError).code).toBe('ERR_DOCUMENT_HAS_DEPENDENTS')
      expect((e as ApiError).details).toEqual({ dependents })
    }
    await deleteDocument(1, bill)
    expect(await code(deleteDocument(1, wb))).toBe('OK')
  })

  it('puts the stock no. on every warning with a part', async () => {
    const d = await getDocument(1, 1001)
    for (const w of d.warnings) if (w.partId) expect(w.stockNo).toBeTruthy()
    const posted = await postDocument(1, d, key())
    const neg = posted.warnings.filter((w) => w.code === 'NEGATIVE_STOCK')
    expect(neg.length).toBeGreaterThan(0)
    for (const w of neg) expect(w.stockNo).toMatch(/^[A-Z]{2}-\d{4}$/)
  })

  it('carries the card salesperson, the account no. and the last purchase cost', async () => {
    const d = await getDocument(1, 391)
    expect(d.hesapNo).toBe('120.10482')
    const s = await getSicilSummary(1, d.sicilId)
    expect(s).toMatchObject({ salesRepUserId: 7, salesRepName: 'A. Kaya' })
    const hp = await getPart(1, 1001)
    expect(hp.lastPurchaseCost).toEqual({
      amount: '8900.0000',
      date: '2026-09-26',
      documentNo: 'AF-2026-1-000201',
    })
    expect((await getPart(1, 1009)).lastPurchaseCost).toBeNull()
    const rows = await gridParts(1, { startRow: 0, endRow: 200 })
    expect(rows.rows.find((r) => r.id === 1001)?.lastPurchaseCost?.documentNo).toBe(
      'AF-2026-1-000201',
    )
    expect(await getDashboard(1)).not.toHaveProperty('latest')
  })
})
