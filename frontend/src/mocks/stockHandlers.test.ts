import { describe, expect, it } from 'vitest'
import {
  gridParts,
  gridStockCountLines,
  gridStockMovements,
  getDashboard,
  getPartStock,
  getPartPriceHistory,
  listStockCounts,
  searchCatalog,
  setStockCountLine,
} from '../api/endpoints'
import { ApiError } from '../api/client'

// The synthetic data behind the F2 screens matches the mockups.
describe('stock mocks', () => {
  it('generates ~2,000 parts with HP-3774 as in mockup 09', async () => {
    const all = await gridParts(1, { startRow: 0, endRow: 1 })
    expect(all.unfilteredCount).toBeGreaterThan(1900)
    const hp = await gridParts(1, {
      startRow: 0,
      endRow: 5,
      filterModel: { stockNo: { filterType: 'text', type: 'equals', filter: 'HP-3774' } },
    })
    const id = hp.rows[0].id
    const stock = await getPartStock(1, id)
    expect(stock.depots.map((d) => [d.onHand, d.reserved, d.available, d.shelf])).toEqual([
      ['10', '1', '9', 'A-04-2'],
      ['4', '0', '4', 'H-11-1'],
    ])
    const prices = await getPartPriceHistory(1, id)
    expect(prices.map((p) => [p.validFrom, p.price, p.changePct])).toEqual([
      ['2026-09-01', '12400.00', '6.9'],
      ['2026-04-01', '11600.00', '8.4'],
      ['2026-01-02', '10700.00', null],
    ])
    const moves = await gridStockMovements(1, { partId: id, startRow: 0, endRow: 20 })
    expect(moves.rows.map((r) => r.runningOnHand)).toEqual([
      '14',
      '16',
      '6',
      '9',
      '7',
      '9',
      '7',
      '8',
      '5',
    ])
  })

  it('ranks the catalog search and finds "pump 37" by name and code', async () => {
    const r = await searchCatalog(1, { q: 'pump 37', limit: 50 })
    const found = r.items.map((h) => h.stockNo)
    for (const no of ['HP-3702', 'HP-3774', 'HP-3780', 'WP-3715', 'FP-3709']) {
      expect(found).toContain(no)
    }
    const exact = await searchCatalog(1, { q: '3774612M91' })
    expect(exact.items[0]).toMatchObject({ stockNo: 'HP-3774', matchKind: 'OEM' })
  })

  it('has the open count of depot 1 with 412 counted and the A-0 shelves of mockup 10', async () => {
    const counts = await listStockCounts(1, { status: 'OPEN' })
    expect(counts[0]).toMatchObject({ countNo: 'SAY-2026-1-000002', countedCount: 412 })
    expect(counts[0].lineCount).toBeGreaterThan(1100)
    const lines = await gridStockCountLines(1, counts[0].id, {
      startRow: 0,
      endRow: 50,
      filterModel: { shelf: { filterType: 'text', type: 'startsWith', filter: 'A-0' } },
    })
    expect(lines.rows.map((l) => l.stockNo)).toEqual([
      'HP-3702',
      'HP-3774',
      'CL-1150',
      'CL-1210',
      'GS-4410',
      'GS-4418',
      'BE-0620',
    ])
    expect(lines.totals).toMatchObject({ value: '270.00', difference: '0' })
    const belt = lines.rows[6]
    await expect(
      setStockCountLine(1, counts[0].id, belt, { countedQty: '32' }),
    ).rejects.toBeInstanceOf(ApiError)
  })

  it('lists below-minimum with negatives first on the dashboard', async () => {
    const d = await getDashboard(1)
    expect(d.belowMin[0].negative).toBe(true)
    expect(d.negativeCount).toBeGreaterThan(0)
    expect(d.belowMinCount).toBeGreaterThan(3)
  })
})
