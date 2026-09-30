import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import type { AgGridReact } from 'ag-grid-react'
import type { GridApi, IDatasource, IGetRowsParams } from 'ag-grid-community'
import api from '../lib/api'
import type { SelectAllState } from '../components/ui/SelectAllHeader'
import { delays } from '../lib/delays'

// extraBody is merged into grid/resolve POSTs (e.g. organization scoping).
export function useServerSideGrid<T extends { id: number }>(opts: {
  gridPath: string
  deletePath: (id: number) => string
  extraBody?: Record<string, unknown>
}) {
  const { gridPath, deletePath, extraBody } = opts
  // Stable key so a new-but-equal object does not re-trigger memoization.
  const extraBodyKey = JSON.stringify(extraBody ?? {})
  const gridRef = useRef<AgGridReact>(null)
  const [selectedRows, setSelectedRows] = useState<T[]>([])
  const [selectedCount, setSelectedCount] = useState(0)
  const [selectAllActive, setSelectAllActive] = useState(false)
  const selectAllRef = useRef(false)
  const selectedCountRef = useRef(0)
  const totalCountRef = useRef(0)
  // With select-all on: ids the user deselected. Rows not loaded yet count as selected.
  const toggledRef = useRef(new Set<string>())
  const [search, setSearch] = useState('')
  const searchRef = useRef('')
  const isFirstRun = useRef(true)

  const refresh = useCallback(() => {
    gridRef.current?.api?.purgeInfiniteCache()
  }, [])

  // Skip the first run: setting the datasource already fetches once.
  useEffect(() => {
    if (isFirstRun.current) {
      isFirstRun.current = false
      searchRef.current = search
      return
    }
    const t = setTimeout(() => {
      searchRef.current = search
      refresh()
    }, delays.gridSearch)
    return () => clearTimeout(t)
  }, [search, refresh])

  const getRowId = useCallback((params: { data: { id: number } }) => String(params.data.id), [])

  const datasource = useMemo<IDatasource>(
    () => ({
      getRows(params: IGetRowsParams) {
        api
          .post(gridPath, {
            ...extraBody,
            startRow: params.startRow,
            endRow: params.endRow,
            sortModel: params.sortModel,
            filterModel: params.filterModel,
            search: searchRef.current,
          })
          .then((resp) => {
            totalCountRef.current = resp.data.totalCount
            params.successCallback(resp.data.rows, resp.data.totalCount)
            if (selectAllRef.current) selectLoaded(gridRef.current?.api, toggledRef.current)
          })
          .catch(() => params.failCallback())
      },
    }),
    // The datasource is not rebuilt when the content is unchanged (via extraBodyKey).
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [gridPath, extraBodyKey],
  )

  const onGridReady = useCallback(
    (params: { api: { setDatasource: (d: IDatasource) => void } }) => {
      params.api.setDatasource(datasource)
    },
    [datasource],
  )

  const syncSelection = useCallback((gridApi: GridApi) => {
    // Collect the loaded selected rows (for immediate data).
    const rows: T[] = []
    const toggled = toggledRef.current
    gridApi.forEachNode((n) => {
      if (!n.data) return
      if (n.isSelected()) {
        rows.push(n.data)
        toggled.delete(String(n.data.id))
      } else if (selectAllRef.current) {
        toggled.add(String(n.data.id))
      }
    })
    setSelectedRows(rows)
    const count = selectAllRef.current
      ? Math.max(0, totalCountRef.current - toggled.size)
      : rows.length
    selectedCountRef.current = count
    setSelectedCount(count)
  }, [])

  const onSelectionChanged = useCallback(
    (e: { api: GridApi }) => syncSelection(e.api),
    [syncSelection],
  )

  const getSelectAllState = useCallback((): SelectAllState => {
    if (selectAllRef.current && toggledRef.current.size === 0) return 'all'
    return selectedCountRef.current > 0 ? 'some' : 'none'
  }, [])

  // selectAll covers ALL (filtered) rows ON THE SERVER; loaded rows are selected as well.
  const toggleSelectAll = useCallback(
    (checked: boolean) => {
      const gridApi = gridRef.current?.api
      selectAllRef.current = checked
      toggledRef.current = new Set()
      setSelectAllActive(checked)
      if (!gridApi) return
      if (checked) selectLoaded(gridApi, toggledRef.current)
      else gridApi.deselectAll()
      syncSelection(gridApi)
    },
    [syncSelection],
  )

  // selectAll spans every filtered row on the server, not just the loaded ones.
  const resolveSelectedRows = useCallback(async (): Promise<T[]> => {
    if (!selectAllActive) return selectedRows
    const resp = await api.post(gridPath, {
      ...extraBody,
      startRow: 0,
      endRow: totalCountRef.current || 100000,
      sortModel: [],
      filterModel: gridRef.current?.api?.getFilterModel?.() || {},
      search: searchRef.current,
    })
    let rows: T[] = resp.data.rows || []
    const excluded = toggledRef.current
    if (excluded.size) rows = rows.filter((r) => !excluded.has(String(r.id)))
    return rows
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectAllActive, selectedRows, gridPath, extraBodyKey])

  // Deletes selected rows in batches of 20; returns the success/failure counts.
  const deleteSelected = useCallback(async (): Promise<{ ok: number; failed: number }> => {
    const ids = (await resolveSelectedRows()).map((r) => r.id)
    const limit = 20
    let ok = 0
    let failed = 0
    for (let i = 0; i < ids.length; i += limit) {
      const results = await Promise.all(
        ids.slice(i, i + limit).map((id) =>
          api
            .delete(deletePath(id))
            .then(() => true)
            .catch(() => false),
        ),
      )
      results.forEach((r) => (r ? ok++ : failed++))
    }
    gridRef.current?.api.deselectAll()
    refresh()
    setSelectedRows([])
    setSelectedCount(0)
    selectedCountRef.current = 0
    selectAllRef.current = false
    toggledRef.current = new Set()
    setSelectAllActive(false)
    return { ok, failed }
  }, [resolveSelectedRows, deletePath, refresh])

  return {
    gridRef,
    selectedCount,
    search,
    setSearch,
    getRowId,
    onGridReady,
    onSelectionChanged,
    getSelectAllState,
    toggleSelectAll,
    resolveSelectedRows,
    deleteSelected,
    refresh,
  }
}

// Selects loaded rows that the user has not explicitly deselected.
function selectLoaded(gridApi: GridApi | undefined, excluded: Set<string>) {
  if (!gridApi) return
  const toSelect: { setSelected: (v: boolean) => void }[] = []
  gridApi.forEachNode((n) => {
    if (n.data && !n.isSelected() && !excluded.has(String(n.data.id))) toSelect.push(n)
  })
  toSelect.forEach((n) => n.setSelected(true))
}
