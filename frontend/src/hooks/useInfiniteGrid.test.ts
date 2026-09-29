import { describe, expect, it } from 'vitest'
import { act, renderHook } from '@testing-library/react'
import type { GridReadyEvent } from 'ag-grid-community'
import { useInfiniteGrid } from './useInfiniteGrid'

function fakeGrid(rows: number) {
  const view = { top: 0, bottom: 34 }
  const api = {
    setDatasource: () => {},
    getVerticalPixelRange: () => view,
    getFirstDisplayedRow: () => 0,
    getLastDisplayedRow: () => rows - 1,
  }
  const columnApi = { getColumnState: () => [] }
  return { view, event: { api, columnApi } as unknown as GridReadyEvent }
}

describe('useInfiniteGrid range', () => {
  it('covers every row of a short grid once the grid grows to fit them', () => {
    const { result } = renderHook(() =>
      useInfiniteGrid(async () => ({ rows: [], totalCount: 4 }), []),
    )
    const { view, event } = fakeGrid(4)
    act(() => {
      result.current.gridProps.onGridReady(event)
      result.current.gridProps.onModelUpdated()
    })
    expect(result.current.range).toEqual([0, 0])

    view.bottom = 4 * 34
    act(() => result.current.gridProps.onGridSizeChanged())
    expect(result.current.range).toEqual([0, 3])
  })

  it('shows the scrolled window of a long grid', () => {
    const { result } = renderHook(() =>
      useInfiniteGrid(async () => ({ rows: [], totalCount: 412 }), []),
    )
    const { view, event } = fakeGrid(120)
    act(() => result.current.gridProps.onGridReady(event))
    view.top = 340
    view.bottom = 340 + 14 * 34
    act(() => result.current.gridProps.onBodyScroll())
    expect(result.current.range).toEqual([10, 23])
  })
})
