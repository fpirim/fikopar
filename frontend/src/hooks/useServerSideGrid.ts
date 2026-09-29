import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import type { AgGridReact } from 'ag-grid-react'
import type { IServerSideDatasource, IServerSideGetRowsParams } from 'ag-grid-community'
import api from '../lib/api'

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
  const totalCountRef = useRef(0)
  const toggledRef = useRef<string[]>([])
  const [search, setSearch] = useState('')
  const searchRef = useRef('')
  const isFirstRun = useRef(true)

  const refresh = useCallback(() => {
    gridRef.current?.api?.refreshServerSide({ purge: true })
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
    }, 300)
    return () => clearTimeout(t)
  }, [search, refresh])

  const getRowId = useCallback((params: { data: { id: number } }) => String(params.data.id), [])

  const datasource = useMemo<IServerSideDatasource>(
    () => ({
      getRows(params: IServerSideGetRowsParams) {
        api
          .post(gridPath, {
            ...extraBody,
            startRow: params.request.startRow,
            endRow: params.request.endRow,
            sortModel: params.request.sortModel,
            filterModel: params.request.filterModel,
            search: searchRef.current,
          })
          .then((resp) => {
            totalCountRef.current = resp.data.totalCount
            params.success({ rowData: resp.data.rows, rowCount: resp.data.totalCount })
          })
          .catch(() => params.fail())
      },
    }),
    // The datasource is not rebuilt when the content is unchanged (via extraBodyKey).
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [gridPath, extraBodyKey],
  )

  const onGridReady = useCallback(
    (params: { api: { setServerSideDatasource: (d: IServerSideDatasource) => void } }) => {
      params.api.setServerSideDatasource(datasource)
    },
    [datasource],
  )

  const onSelectionChanged = useCallback((e: any) => {
    // Collect the loaded selected rows (for immediate data).
    const rows: T[] = []
    e.api.forEachNode((n: any) => {
      if (n.isSelected() && n.data) rows.push(n.data)
    })
    setSelectedRows(rows)
    // SSRM selection state: the selectAll flag covers ALL (filtered) rows ON THE SERVER.
    const st = e.api.getServerSideSelectionState?.()
    if (st && typeof st.selectAll === 'boolean') {
      const toggled: string[] = st.toggledNodes || []
      toggledRef.current = toggled
      setSelectAllActive(st.selectAll)
      setSelectedCount(
        st.selectAll ? Math.max(0, totalCountRef.current - toggled.length) : toggled.length,
      )
    } else {
      setSelectAllActive(false)
      setSelectedCount(rows.length)
    }
  }, [])

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
    const excluded = new Set(toggledRef.current.map(Number))
    if (excluded.size) rows = rows.filter((r) => !excluded.has(r.id))
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
    resolveSelectedRows,
    deleteSelected,
    refresh,
  }
}
