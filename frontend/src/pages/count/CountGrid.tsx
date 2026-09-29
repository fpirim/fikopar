import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import type { AgGridReact } from 'ag-grid-react'
import type {
  CellValueChangedEvent,
  ColDef,
  ICellRendererParams,
  IRowNode,
  ValueSetterParams,
} from 'ag-grid-community'
import { AlertCircle, Check, Loader2 } from 'lucide-react'
import { ApiError } from '../../api/client'
import { getStockCountLine, gridStockCountLines, setStockCountLine } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type {
  CountLinesOnly,
  FilterModel,
  GridRequest,
  StockCount,
  StockCountLine,
} from '../../api/types'
import { useInfiniteGrid } from '../../hooks/useInfiniteGrid'
import { formatDateTime, formatMoney, formatQty } from '../../lib/format'
import { Chip } from '../../components/ui/chip'
import { FkGrid } from '../../components/grid/FkGrid'
import { exprFilter, monoWidth, numCol } from '../../components/grid/columns'
import GridFooter from '../../components/grid/GridFooter'
import CountedEditor from './CountedEditor'
import { parseCounted } from './parseCounted'
import LineConflictDialog from './LineConflictDialog'

const MAX_BODY = 15

type Row = StockCountLine & { label?: string }
type SaveState = { state: 'saving' | 'saved' | 'error'; message?: string }
interface Ctx {
  saves: Map<number, SaveState>
  labels: Record<SaveState['state'], string>
}

export interface Scope {
  only: CountLinesOnly
  shelfFrom: string
  shelfTo: string
}

const signCls = (v: unknown) => {
  const n = v === null || v === undefined ? 0 : Number(v)
  if (n < 0) return 'fk-neg'
  if (n > 0) return 'fk-pos'
  return 'fk-muted'
}

function CountedCell(p: ICellRendererParams<Row> & { context: Ctx }) {
  if (p.node.rowPinned) return <span>{formatQty(p.value)}</span>
  const save = p.data ? p.context.saves.get(p.data.id) : undefined
  const label = save
    ? save.state === 'error'
      ? p.context.labels.error.replace('{{reason}}', save.message ?? '')
      : p.context.labels[save.state]
    : undefined
  return (
    <span className="flex items-center justify-end gap-1.5">
      <span
        role={save ? 'img' : undefined}
        aria-label={label}
        title={label}
        className="flex w-3.5 shrink-0 justify-center"
      >
        {save?.state === 'saving' && <Loader2 className="h-3 w-3 animate-spin text-fk-faint" />}
        {save?.state === 'saved' && <Check className="h-3 w-3 text-fk-chip-green-fg" />}
        {save?.state === 'error' && <AlertCircle className="h-3 w-3 text-fk-negative" />}
      </span>
      <span className={p.value === null || p.value === undefined ? 'text-fk-faint' : ''}>
        {p.value === null || p.value === undefined ? '—' : formatQty(p.value)}
      </span>
    </span>
  )
}

function shelfSuffix(model: FilterModel | undefined, scope: Scope): string | null {
  const c = model?.shelf
  if (c?.filter) {
    const f = String(c.filter)
    if (c.type === 'startsWith') return `${f}*`
    if (c.type === 'equals') return f
    return `*${f}*`
  }
  if (scope.shelfFrom || scope.shelfTo) return `${scope.shelfFrom}..${scope.shelfTo}`
  return null
}

export default function CountGrid({
  orgId,
  count,
  scope,
  onSaved,
  requestRef,
  refreshKey,
}: Readonly<{
  orgId: number
  count: StockCount
  scope: Scope
  onSaved: () => void
  // The current filters and sort, for the Excel export.
  requestRef: { current: (() => GridRequest) | null }
  refreshKey: number
}>) {
  const { t, i18n } = useTranslation(['counts', 'grid', 'errors'])
  const gridRef = useRef<AgGridReact>(null)
  const open = count.status === 'OPEN'
  const [saves] = useState(() => new Map<number, SaveState>())
  // Totals re-read after a save; they belong to the page of rows they were read with.
  const [saved, setSaved] = useState<{ info: unknown; totals: Record<string, string> } | null>(null)
  const [conflict, setConflict] = useState<{
    node: IRowNode<Row>
    mine: string | null
    theirs: StockCountLine
  } | null>(null)

  const fetchPage = useCallback(
    (req: GridRequest) => gridStockCountLines(orgId, count.id, { ...req, ...scope }),
    [orgId, count.id, scope],
  )
  const grid = useInfiniteGrid<Row>(fetchPage, [orgId, count.id, scope, open, refreshKey])
  useEffect(() => {
    requestRef.current = grid.request
  }, [requestRef, grid.request])

  const refreshTotals = useCallback(async () => {
    const r = await gridStockCountLines(orgId, count.id, {
      ...grid.request(),
      ...scope,
      startRow: 0,
      endRow: 0,
    })
    if (r.totals) setSaved({ info: grid.info, totals: r.totals })
  }, [orgId, count.id, grid, scope])

  const mark = useCallback(
    (node: IRowNode<Row>, s: SaveState | null) => {
      if (!node.data) return
      if (s) saves.set(node.data.id, s)
      else saves.delete(node.data.id)
      gridRef.current?.api?.refreshCells({ rowNodes: [node], columns: ['countedQty'], force: true })
    },
    [saves],
  )

  const save = useCallback(
    async (node: IRowNode<Row>, previous: string | null) => {
      const line = node.data
      if (!line) return
      mark(node, { state: 'saving' })
      try {
        const saved = await setStockCountLine(orgId, count.id, line, {
          countedQty: line.countedQty ?? null,
        })
        node.setData(saved)
        mark(node, { state: 'saved' })
        onSaved()
        void refreshTotals()
      } catch (e) {
        if (e instanceof ApiError && e.status === 412) {
          const fresh = await getStockCountLine(orgId, count.id, line.id).catch(() => null)
          if (fresh) {
            node.setData(fresh)
            mark(node, null)
            setConflict({ node, mine: line.countedQty ?? null, theirs: fresh })
            onSaved()
            void refreshTotals()
            return
          }
        }
        node.setData({ ...line, countedQty: previous })
        mark(node, { state: 'error', message: errorText(t, e) })
      }
    },
    [orgId, count.id, mark, onSaved, refreshTotals, t],
  )

  const labels: Record<string, string> = useMemo(
    () => ({
      shelf: t('col.shelf'),
      stockNo: t('col.part'),
      name: t('col.name'),
      systemQty: t('col.system'),
      countedQty: t('col.counted'),
      difference: t('col.difference'),
      unitCost: t('col.unitCost'),
      value: t('col.value'),
      countedBy: t('col.countedBy'),
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, i18n.language],
  )

  const columnDefs = useMemo<ColDef<Row>[]>(
    () => [
      {
        colId: 'shelf',
        field: 'shelf',
        headerName: labels.shelf,
        sort: 'asc',
        sortIndex: 0,
        flex: 0,
        width: monoWidth(11),
        cellClass: 'font-mono',
        colSpan: (p) => (p.node?.rowPinned ? 3 : 1),
        ...exprFilter({ kind: 'text', placeholder: 'A-04', defaultOp: 'startsWith' }),
        cellRenderer: (p: ICellRendererParams<Row>) =>
          p.node.rowPinned ? <span className="font-sans">{p.data?.label}</span> : p.value,
      },
      {
        colId: 'stockNo',
        field: 'stockNo',
        headerName: labels.stockNo,
        sort: 'asc',
        sortIndex: 1,
        flex: 0,
        width: monoWidth(10),
        cellClass: 'font-mono',
        ...exprFilter({
          kind: 'text',
          placeholder: t('filter.startsWith'),
          defaultOp: 'startsWith',
        }),
      },
      {
        colId: 'name',
        field: 'name',
        headerName: labels.name,
        flex: 1,
        minWidth: 200,
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
        cellRenderer: (p: ICellRendererParams<Row>) => {
          const l = p.data
          if (!l || p.node.rowPinned) return null
          const moved = Number(l.movedSinceOpen)
          return (
            <span className="flex min-w-0 items-center gap-1.5">
              <span className="min-w-0 truncate">{l.name}</span>
              {moved !== 0 && (
                <span title={t('flag.movedTitle', { qty: formatQty(moved, { sign: true }) })}>
                  <Chip size="sm" tone="amber">
                    {t('flag.moved', { qty: formatQty(moved, { sign: true }) })}
                  </Chip>
                </span>
              )}
              {l.costSource !== 'LAST_PURCHASE' && (
                <span title={t(`flag.${l.costSource}Title`)}>
                  <Chip size="sm" tone={l.costSource === 'NONE' ? 'red' : 'grey'}>
                    {t(`flag.${l.costSource}`)}
                  </Chip>
                </span>
              )}
            </span>
          )
        },
      },
      {
        colId: 'systemQty',
        field: 'systemQty',
        headerName: labels.systemQty,
        flex: 0,
        width: monoWidth(8),
        ...numCol,
        ...exprFilter({ kind: 'number', placeholder: '=' }),
        valueFormatter: (p) => formatQty(p.value),
      },
      {
        colId: 'countedQty',
        field: 'countedQty',
        headerName: labels.countedQty,
        headerClass: 'fk-editable-head',
        flex: 0,
        width: monoWidth(8) + 22,
        ...numCol,
        cellClass: open ? 'fk-num fk-editable' : 'fk-num',
        ...exprFilter({ kind: 'number', placeholder: t('filter.blank') }),
        editable: (p) => open && !p.node.rowPinned,
        cellEditor: CountedEditor,
        cellEditorParams: { ariaLabel: (no: string) => t('countedOf', { no }) },
        cellRenderer: CountedCell,
        valueSetter: (p: ValueSetterParams<Row>) => {
          const v = parseCounted(p.newValue)
          if (v === undefined) {
            mark(p.node!, { state: 'error', message: t('save.invalid') })
            return false
          }
          if (v === (p.data.countedQty ?? null)) return false
          p.data.countedQty = v
          return true
        },
      },
      {
        colId: 'difference',
        field: 'difference',
        headerName: labels.difference,
        flex: 0,
        width: monoWidth(8),
        ...numCol,
        ...exprFilter({ kind: 'number', placeholder: t('grid:fk.nonZero') }),
        valueFormatter: (p) =>
          p.value === null || p.value === undefined ? '—' : formatQty(p.value, { sign: true }),
        cellClass: (p) => `fk-num ${signCls(p.value)}`,
      },
      {
        colId: 'unitCost',
        field: 'unitCost',
        headerName: labels.unitCost,
        flex: 0,
        width: monoWidth(10),
        sortable: false,
        ...numCol,
        valueFormatter: (p) =>
          p.node?.rowPinned
            ? ''
            : p.value === null || p.value === undefined
              ? '—'
              : formatMoney(p.value),
        tooltipValueGetter: (p) =>
          p.data && !p.node?.rowPinned ? t(`costFrom.${p.data.costSource}`) : undefined,
        cellClassRules: {
          'fk-muted': (p) => !p.node?.rowPinned && p.data?.costSource !== 'LAST_PURCHASE',
        },
      },
      {
        colId: 'value',
        field: 'value',
        headerName: labels.value,
        flex: 0,
        width: monoWidth(13),
        ...numCol,
        valueFormatter: (p) =>
          p.value === null || p.value === undefined ? '—' : formatMoney(p.value, { sign: true }),
        cellClass: (p) => `fk-num ${signCls(p.value)}`,
      },
      {
        colId: 'countedBy',
        field: 'countedBy',
        headerName: labels.countedBy,
        flex: 0,
        width: 132,
        tooltipValueGetter: (p) => formatDateTime(p.data?.countedAt),
        ...exprFilter({ kind: 'text', placeholder: t('filter.counter') }),
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [labels, open],
  )

  const info = grid.info
  const sum = (saved?.info === info ? saved?.totals : null) ?? info?.totals ?? null
  const pinned = useMemo<Row[]>(() => {
    if (!info) return []
    const shown = t('grid:fk.shownOf', {
      shown: info.totalCount.toLocaleString(i18n.language === 'tr' ? 'tr-TR' : 'en-US'),
      total: info.unfilteredCount.toLocaleString(i18n.language === 'tr' ? 'tr-TR' : 'en-US'),
    })
    const shelf = shelfSuffix(grid.request().filterModel, scope)
    return [
      {
        label: shelf ? `${shown} · ${t('shelfSuffix', { shelf })}` : shown,
        systemQty: sum?.systemQty ?? '',
        countedQty: sum?.countedQty ?? '',
        difference: sum?.difference ?? '',
        value: sum?.value ?? '',
      } as Row,
    ]
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [info, sum, scope, t, i18n.language])

  const total = info?.totalCount ?? 0
  const height = 36 + 34 + 36 + 2 + Math.max(1, Math.min(total, MAX_BODY)) * 34

  return (
    <>
      <FkGrid
        ref={gridRef}
        height={height}
        className="min-h-0 shrink"
        columnDefs={columnDefs}
        {...grid.gridProps}
        getRowId={(p) => String(p.data.id)}
        pinnedBottomRowData={pinned}
        context={{
          saves,
          labels: {
            saving: t('save.saving'),
            saved: t('save.saved'),
            error: t('save.failed', { reason: '{{reason}}' }),
          },
        }}
        enterNavigatesVertically
        enterNavigatesVerticallyAfterEdit
        stopEditingWhenCellsLoseFocus
        onCellValueChanged={(e: CellValueChangedEvent<Row>) =>
          void save(e.node, (e.oldValue as string | null) ?? null)
        }
      />
      <GridFooter
        sorted={grid.sorted.map(
          (s) => labels[s.colId]?.toLocaleLowerCase(i18n.language) ?? s.colId,
        )}
        filtered={Object.keys(labels)
          .filter((c) => grid.filtered.includes(c))
          .map((c) => labels[c])}
        range={grid.range}
        total={total}
        onClear={grid.clearFilters}
      />
      {conflict && (
        <LineConflictDialog
          stockNo={conflict.theirs.stockNo}
          mine={conflict.mine}
          theirs={conflict.theirs}
          onKeep={() => {
            const { node, mine, theirs } = conflict
            setConflict(null)
            node.setData({ ...theirs, countedQty: mine })
            void save(node, theirs.countedQty ?? null)
          }}
          onTake={() => setConflict(null)}
        />
      )}
    </>
  )
}
