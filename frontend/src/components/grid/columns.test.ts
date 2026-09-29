import { describe, expect, it } from 'vitest'
import { exprFilter, numCol } from './columns'

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
