import { describe, expect, it } from 'vitest'
import {
  gridParts,
  gridStockCountLines,
  gridStockMovements,
  getDashboard,
  createPriceImport,
  getPart,
  getPartStock,
  getPartPriceHistory,
  getStockCountLine,
  setPartDepotSettings,
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

describe('stock mocks on contracts v1.3', () => {
  const hpRow = async (depotId: number | null) =>
    (
      await gridParts(1, {
        startRow: 0,
        endRow: 1,
        depotId,
        filterModel: { stockNo: { filterType: 'text', type: 'equals', filter: 'HP-3774' } },
      })
    ).rows[0]

  it('matches every search word against the name or a code and reports the code', async () => {
    const r = await searchCatalog(1, { q: 'pump 37', limit: 50 })
    const hp = r.items.find((h) => h.stockNo === 'HP-3774')!
    expect(hp).toMatchObject({ matchKind: 'NAME', matchedCode: 'HP-3774' })
    const name = await searchCatalog(1, { q: 'hydraulic pump', limit: 50 })
    expect(name.items.find((h) => h.stockNo === 'HP-3774')?.matchedCode).toBeNull()
    const dashes = await searchCatalog(1, { q: '-- pump', limit: 50 })
    expect(dashes.items.map((h) => h.stockNo)).toContain('HP-3774')
    expect((await searchCatalog(1, { q: '--' })).items).toEqual([])
  })

  it('adds stock per depot to parts grid rows over all depots only', async () => {
    expect((await hpRow(null)).stockByDepot).toEqual([
      { depotId: 1, depotCode: '1', onHand: '10', available: '9' },
      { depotId: 2, depotCode: '2', onHand: '4', available: '4' },
    ])
    expect((await hpRow(1)).stockByDepot).toBeUndefined()
  })

  it('filters and sorts movements by qtyIn and qtyOut', async () => {
    const id = (await hpRow(null)).id
    const all = await gridStockMovements(1, { partId: id, startRow: 0, endRow: 50 })
    for (const m of all.rows) {
      const d = Number(m.delta)
      expect(m.qtyIn).toBe(d > 0 ? m.qty : null)
      expect(m.qtyOut).toBe(d < 0 ? m.qty : null)
    }
    const outs = await gridStockMovements(1, {
      partId: id,
      startRow: 0,
      endRow: 50,
      filterModel: { qtyOut: { filterType: 'number', type: 'greaterThanOrEqual', filter: 2 } },
    })
    expect(outs.rows.length).toBeGreaterThan(0)
    expect(outs.rows.every((m) => Number(m.qtyOut) >= 2)).toBe(true)
    const blank = await gridStockMovements(1, {
      partId: id,
      startRow: 0,
      endRow: 50,
      filterModel: { qtyIn: { filterType: 'number', type: 'blank' } },
    })
    expect(blank.rows.every((m) => m.qtyIn === null)).toBe(true)
    const sorted = await gridStockMovements(1, {
      partId: id,
      startRow: 0,
      endRow: 50,
      sortModel: [{ colId: 'qtyIn', sort: 'desc' }],
    })
    const ins = sorted.rows.map((m) => m.qtyIn)
    const firstNull = ins.indexOf(null)
    expect(firstNull).toBeGreaterThan(0)
    expect(ins.slice(firstNull).every((v) => v === null)).toBe(true)
  })

  it('returns equivalents of a part both ways with the stock no as the code', async () => {
    const cl = (await getPart(1, 1005)).data
    expect(cl.codes?.find((c) => c.kind === 'EQUIVALENT')).toMatchObject({
      code: 'CL-1210',
      equivalentPartId: 1006,
    })
    expect(cl.equivalents).toEqual([
      expect.objectContaining({
        partId: 1006,
        stockNo: 'CL-1210',
        catalogName: expect.any(String),
      }),
    ])
    const back = (await getPart(1, 1006)).data
    expect(back.equivalents.map((e) => e.partId)).toEqual([1005])
  })

  it('clears omitted depot settings (PUT)', async () => {
    const stock = await getPartStock(1, 1001)
    const d1 = stock.depots[0]
    const saved = await setPartDepotSettings(1, 1001, 1, { minQty: '3' }, d1.version!)
    expect(saved).toMatchObject({ minQty: '3', maxQty: null, shelf: '' })
  })

  it('reads one count line with its version and 404s a line of another count', async () => {
    const lines = await gridStockCountLines(1, 2, { startRow: 0, endRow: 1 })
    const line = await getStockCountLine(1, 2, lines.rows[0].id)
    expect(line).toMatchObject({ id: lines.rows[0].id, version: lines.rows[0].version })
    await expect(getStockCountLine(1, 1, lines.rows[0].id)).rejects.toMatchObject({ status: 404 })
  })

  it('answers an import with the effective mapping, defaults filled', async () => {
    const file = new File(['Stok no;Fiyat\nHP-3774;12.400,00\n'], 'list.csv')
    const job = await createPriceImport(
      1,
      { file, catalogId: 1, validFrom: '2026-10-01', mapping: { delimiter: ';' } },
      crypto.randomUUID(),
    )
    expect(job.mapping).toMatchObject({
      format: 'CSV',
      encoding: 'UTF-8',
      decimalSeparator: ',',
      dateFormat: 'dd.mm.yyyy',
      headerRow: 1,
      listSeparator: '|',
      delimiter: ';',
    })
    expect(job.mapping?.columns).toBeDefined()
  })
})
