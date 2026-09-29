import { describe, expect, it } from 'vitest'
import {
  cancelDocument,
  createDocument,
  getDocument,
  getStockCount,
  gridDocuments,
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
