import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import type {
  ColumnApi,
  FilterChangedEvent,
  GridApi,
  GridReadyEvent,
  IDatasource,
  IGetRowsParams,
  SortChangedEvent,
} from 'ag-grid-community'
import type { FilterModel, GridRequest, GridResponse, SortModel } from '../api/types'

export interface GridInfo {
  totalCount: number
  unfilteredCount: number
  totals: Record<string, string> | null
}

// AG Grid keeps 'yyyy-mm-dd hh:mm:ss' in date models; the API takes 'yyyy-mm-dd'.
function toApiFilter(model: Record<string, FilterModel[string]>): FilterModel {
  const out: FilterModel = {}
  for (const [col, c] of Object.entries(model ?? {})) {
    out[col] =
      c.filterType === 'date'
        ? {
            ...c,
            dateFrom: c.dateFrom?.slice(0, 10) ?? null,
            dateTo: c.dateTo?.slice(0, 10) ?? null,
          }
        : c
  }
  return out
}

export function useInfiniteGrid<T>(
  fetchPage: (req: GridRequest) => Promise<GridResponse<T>>,
  deps: unknown[],
) {
  const apiRef = useRef<GridApi | null>(null)
  const columnApiRef = useRef<ColumnApi | null>(null)
  const fetchRef = useRef(fetchPage)
  useEffect(() => {
    fetchRef.current = fetchPage
  }, [fetchPage])
  const lastReq = useRef<{ sortModel: SortModel; filterModel: FilterModel }>({
    sortModel: [],
    filterModel: {},
  })
  const [info, setInfo] = useState<GridInfo | null>(null)
  const [filtered, setFiltered] = useState<string[]>([])
  const [sorted, setSorted] = useState<{ colId: string; sort: 'asc' | 'desc' }[]>([])
  const [range, setRange] = useState<[number, number]>([0, 0])

  const datasource = useMemo<IDatasource>(
    () => ({
      getRows(p: IGetRowsParams) {
        const req = {
          sortModel: p.sortModel as SortModel,
          filterModel: toApiFilter(p.filterModel),
        }
        lastReq.current = req
        fetchRef
          .current({ ...req, startRow: p.startRow, endRow: p.endRow })
          .then((r) => {
            setInfo({
              totalCount: r.totalCount,
              unfilteredCount: r.unfilteredCount ?? r.totalCount,
              totals: r.totals ?? null,
            })
            p.successCallback(r.rows, r.totalCount)
          })
          .catch(() => p.failCallback())
      },
    }),
    [],
  )

  const onGridReady = useCallback(
    (e: GridReadyEvent) => {
      apiRef.current = e.api
      columnApiRef.current = e.columnApi
      e.api.setDatasource(datasource)
      setSorted(
        e.columnApi
          .getColumnState()
          .filter((c) => c.sort)
          .map((c) => ({ colId: c.colId, sort: c.sort! })),
      )
    },
    [datasource],
  )

  const first = useRef(true)
  useEffect(() => {
    if (first.current) {
      first.current = false
      return
    }
    apiRef.current?.purgeInfiniteCache()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  const onFilterChanged = useCallback((e: FilterChangedEvent) => {
    setFiltered(Object.keys(e.api.getFilterModel() ?? {}))
  }, [])

  const onSortChanged = useCallback((e: SortChangedEvent) => {
    setSorted(
      e.columnApi
        .getColumnState()
        .filter((c) => c.sort)
        .map((c) => ({ colId: c.colId, sort: c.sort! })),
    )
  }, [])

  const onViewportChanged = useCallback(() => {
    const api = apiRef.current
    if (!api) return
    setRange([api.getFirstDisplayedRow(), api.getLastDisplayedRow()])
  }, [])

  const clearFilters = useCallback(() => apiRef.current?.setFilterModel(null), [])

  const request = useCallback(
    (): GridRequest => ({ ...lastReq.current, startRow: 0, endRow: info?.totalCount ?? 0 }),
    [info],
  )

  const fetchAll = useCallback(async () => (await fetchRef.current(request())).rows, [request])

  return {
    apiRef,
    info,
    filtered,
    sorted,
    range,
    gridProps: {
      rowModelType: 'infinite' as const,
      cacheBlockSize: 100,
      onGridReady,
      onFilterChanged,
      onSortChanged,
      onViewportChanged,
      onModelUpdated: onViewportChanged,
    },
    clearFilters,
    request,
    fetchAll,
  }
}
