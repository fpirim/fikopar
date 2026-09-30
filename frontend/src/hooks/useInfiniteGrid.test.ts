import { describe, expect, it, vi } from 'vitest'
import { act, renderHook } from '@testing-library/react'
import type { GridReadyEvent, IDatasource } from 'ag-grid-community'
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

describe('useInfiniteGrid datasource', () => {
  it('does not fetch a block the grid asks for after the page unmounted', () => {
    const fetchPage = vi.fn(async () => ({ rows: [], totalCount: 0 }))
    const { result, unmount } = renderHook(() => useInfiniteGrid(fetchPage, []))
    let ds: IDatasource | undefined
    const { event } = fakeGrid(0)
    event.api.setDatasource = (d: IDatasource) => {
      ds = d
    }
    act(() => result.current.gridProps.onGridReady(event))
    unmount()
    ds!.getRows({ startRow: 0, endRow: 100, sortModel: [], filterModel: {} } as never)
    expect(fetchPage).not.toHaveBeenCalled()
  })
})
