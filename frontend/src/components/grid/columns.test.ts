import { describe, expect, it } from 'vitest'
import { DOC_NO_CHARS, exprFilter, fitMono, monoWidth, numCol } from './columns'

describe('exprFilter', () => {
  it('maps kinds to AG Grid filters with a floating expression box', () => {
    expect(exprFilter({ kind: 'text' }).filter).toBe('agTextColumnFilter')
    expect(exprFilter({ kind: 'enum', options: [] }).filter).toBe('agTextColumnFilter')
    expect(exprFilter({ kind: 'number' }).filter).toBe('agNumberColumnFilter')
    expect(exprFilter({ kind: 'text' }).floatingFilterComponentParams).toMatchObject({
      suppressFilterButton: true,
    })
    expect(numCol.cellClass).toBe('fk-num')
  })

  it('compares yyyy-mm-dd cells against the filter day', () => {
    const cmp = exprFilter({ kind: 'date' }).filterParams.comparator
    const day = new Date(2026, 8, 15)
    expect(cmp(day, '2026-09-15')).toBe(0)
    expect(cmp(day, '2026-09-14')).toBe(-1)
    expect(cmp(day, '2026-09-16')).toBe(1)
    expect(cmp(day, null)).toBe(-1)
  })
})

describe('monoWidth', () => {
  it('fits monospace text with cell padding', () => {
    expect(monoWidth(10)).toBe(103)
    expect(monoWidth(10, true)).toBe(99)
    expect(monoWidth(DOC_NO_CHARS, true)).toBeGreaterThanOrEqual(
      Math.ceil('SIRI-2026-1-000001'.length * 8.1) + 18,
    )
  })

  it('sizes to the longest value', () => {
    expect(fitMono(['1,000.00', '12,345,678.00', null], 10)).toBe(monoWidth(13))
    expect(fitMono([], 10, true)).toBe(monoWidth(10, true))
  })
})
