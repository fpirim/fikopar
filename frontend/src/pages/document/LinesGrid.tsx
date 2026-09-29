import {
  forwardRef,
  useCallback,
  useEffect,
  useImperativeHandle,
  useLayoutEffect,
  useMemo,
  useRef,
  type Ref,
} from 'react'
import { useTranslation } from 'react-i18next'
import type { AgGridReact } from 'ag-grid-react'
import type {
  CellKeyDownEvent,
  CellPosition,
  CellValueChangedEvent,
  ColDef,
  EditableCallbackParams,
  ColSpanParams,
  GridApi,
  ICellRendererParams,
  SuppressKeyboardEventParams,
  TabToNextCellParams,
  ValueSetterParams,
} from 'ag-grid-community'
import type { CatalogHit, Depot, DocumentLine, SicilSummary } from '../../api/types'
import { formatMoney, formatQty } from '../../lib/format'
import { parseDecimal } from '../../lib/documents'
import { FkGrid } from '../../components/grid/FkGrid'
import { monoWidth, numCol } from '../../components/grid/columns'
import { cn } from '@/lib/utils'
import PartEditor from './PartEditor'
import NumEditor from './NumEditor'
import { amountsOf, availableHere, blankLine, stockTone, withPart, type EditLine } from './model'

type Row = EditLine & { pinned?: { label: string; gross: string; discount: string; net: string } }

export interface LinesGridHandle {
  addLine: () => void
  deleteFocused: () => void
  stopEditing: () => void
  editPart: () => void
}

export interface LinesGridProps {
  orgId: number
  depots: Depot[]
  depotId: number | null
  date: string
  card: SicilSummary | null | undefined
  // Amount-only lines (price difference).
  amountOnly: boolean
  editable: boolean
  canAdd: boolean
  canDelete: boolean
  // Parts come from the original or the waybill; only prices and amounts change.
  partsFixed: boolean
  qtyFixed: boolean
  // Lines the grid starts from (a load or a save); edits flow out through onChange.
  seed: EditLine[]
  rows: EditLine[]
  onChange: (lines: EditLine[]) => void
  onInvalid: (message: string | null) => void
  originLines: DocumentLine[] | null
  // Returnable quantity per original line.
  caps: Map<number, number> | null
  badRow: number | null
}

const RN_W = 34
const QTY_W = monoWidth(5, true)
const PCT_W = monoWidth(4, true)
// Room for the Turkish headers (İsk1 %).
const DISC_W = monoWidth(6, true)
const MONEY_W = monoWidth(11, true)
const EDIT_KEYS = ['Enter', 'ArrowUp', 'ArrowDown', 'PageUp', 'PageDown']

// Column callbacks read the current props from the grid context.
const ctx = (s: { context: unknown }) => s.context as LinesGridProps

const money4 = (v: string | null | undefined) => {
  if (v === null || v === undefined || v === '') return ''
  const frac = v.split('.')[1] ?? ''
  return formatMoney(v, { digits: frac.replace(/0+$/, '').length > 2 ? 4 : 2 })
}
// A goods line shows its defaults only once it has a part.
const empty = (s: { data?: Row; node?: { rowPinned?: string | null } | null; context: unknown }) =>
  !s.node?.rowPinned && !ctx(s).amountOnly && s.data?.partId === null

const pct = (v: string | null | undefined) =>
  v === '' || v == null ? '' : formatQty(Math.round(Number(v) * 100) / 100)

function LinesGridInner(p: Readonly<LinesGridProps>, ref: Ref<LinesGridHandle>) {
  const { t, i18n } = useTranslation('documents')
  const grid = useRef<AgGridReact>(null)
  const api = () => grid.current?.api as GridApi<Row> | undefined
  const live = useRef(p)
  useLayoutEffect(() => {
    live.current = p
  })

  const collect = useCallback(() => {
    const out: EditLine[] = []
    api()?.forEachNode((n) => {
      if (n.data) out.push({ ...n.data })
    })
    return out
  }, [])
  const changed = useCallback(() => live.current.onChange(collect()), [collect])

  // A new row renders on a later frame (the grid grows with its lines); retry until it edits.
  const startEdit = useCallback((rowIndex: number, colKey: string) => {
    const a = api()
    if (!a) return
    let tries = 0
    const attempt = () => {
      a.ensureIndexVisible(rowIndex)
      a.setFocusedCell(rowIndex, colKey)
      a.startEditingCell({ rowIndex, colKey })
      if (!a.getEditingCells().length && ++tries < 10) setTimeout(attempt, 16)
    }
    setTimeout(attempt, 0)
  }, [])

  const firstCol = () => (live.current.amountOnly ? 'description' : 'part')

  const addLine = useCallback(
    (after?: number, edit = true) => {
      const a = api()
      if (!a || !live.current.canAdd) return null
      const count = a.getDisplayedRowCount()
      const at = after === undefined ? count : after + 1
      const card = live.current.card
      a.applyTransaction({
        add: [
          blankLine({
            discount1Pct: live.current.amountOnly ? '0' : (card?.discount1Pct ?? '0'),
            discount2Pct: live.current.amountOnly ? '0' : (card?.discount2Pct ?? '0'),
            qty: live.current.amountOnly ? '1' : '',
          }),
        ],
        addIndex: at,
      })
      changed()
      if (edit) startEdit(at, firstCol())
      return at
    },
    [changed, startEdit],
  )

  const deleteAt = useCallback(
    (rowIndex: number) => {
      const a = api()
      const node = a?.getDisplayedRowAtIndex(rowIndex)
      if (!a || !node?.data || !live.current.canDelete) return
      a.applyTransaction({ remove: [node.data] })
      changed()
      const left = a.getDisplayedRowCount()
      if (left) a.setFocusedCell(Math.min(rowIndex, left - 1), firstCol())
    },
    [changed],
  )

  useImperativeHandle(
    ref,
    () => ({
      addLine: () => {
        const f = api()?.getFocusedCell()
        addLine(f && !f.rowPinned ? f.rowIndex : undefined)
      },
      deleteFocused: () => {
        const f = api()?.getFocusedCell()
        if (f && !f.rowPinned) deleteAt(f.rowIndex)
      },
      stopEditing: () => api()?.stopEditing(),
      editPart: () => {
        const f = api()?.getFocusedCell()
        const count = api()?.getDisplayedRowCount() ?? 0
        if (f && !f.rowPinned) startEdit(f.rowIndex, firstCol())
        else if (count) startEdit(count - 1, firstCol())
        else addLine()
      },
    }),
    [addLine, deleteAt, startEdit],
  )

  const numSetter =
    (field: 'qty' | 'unitPrice' | 'discount1Pct' | 'discount2Pct' | 'vatPct', label: string) =>
    (s: ValueSetterParams<Row>) => {
      const pctField = field !== 'qty' && field !== 'unitPrice'
      const v = parseDecimal(
        String(s.newValue ?? ''),
        field === 'unitPrice' || field === 'qty' ? 4 : 4,
        {
          max: pctField ? 100 : undefined,
        },
      )
      const row = (s.node?.rowIndex ?? 0) + 1
      if (v === null || (v === '' && field !== 'qty')) {
        ctx(s).onInvalid(t('lineInvalid', { row, col: label }))
        return false
      }
      if (field === 'qty' && v !== '' && Number(v) <= 0) {
        ctx(s).onInvalid(t('lineInvalid', { row, col: label }))
        return false
      }
      const cap =
        field === 'qty' && s.data.originLineId !== null
          ? ctx(s).caps?.get(s.data.originLineId)
          : undefined
      if (cap !== undefined && Number(v) > cap) {
        ctx(s).onInvalid(t('overReturn', { row, max: formatQty(cap) }))
        return false
      }
      ctx(s).onInvalid(null)
      if (s.data[field] === v) return false
      s.data[field] = v
      return true
    }

  const editable =
    (extra: (r: Row, c: LinesGridProps) => boolean = () => true) =>
    (e: EditableCallbackParams<Row>) =>
      ctx(e).editable && !e.node.rowPinned && Boolean(e.data) && extra(e.data!, ctx(e))

  const pinnedSpan = (n: number) => (s: ColSpanParams<Row>) => (s.node?.rowPinned ? n : 1)

  const hasCaps = p.caps !== null
  const columnDefs = useMemo<ColDef<Row>[]>(() => {
    const rn: ColDef<Row> = {
      colId: 'rn',
      headerName: '',
      width: RN_W,
      flex: 0,
      sortable: false,
      cellClass: 'fk-rn',
      valueGetter: (s) => (s.node?.rowPinned ? '' : (s.node?.rowIndex ?? 0) + 1),
    }
    const numEditor = (label: string, text = false) => ({
      cellEditor: NumEditor,
      cellEditorParams: { ariaLabel: (row: number) => t('cellLabel', { col: label, row }), text },
    })
    const net: ColDef<Row> = {
      colId: 'net',
      headerName: t('col.net'),
      flex: 0,
      width: MONEY_W,
      ...numCol,
      valueGetter: (s) =>
        s.node?.rowPinned
          ? s.data?.pinned?.net
          : s.data
            ? amountsOf(s.data, p.amountOnly ? 'PRICE_DIFFERENCE' : 'SALES_INVOICE').netAmount
            : '',
      valueFormatter: (s) => (empty(s) ? '' : formatMoney(s.value)),
    }
    const vat: ColDef<Row> = {
      colId: 'vat',
      field: 'vatPct',
      headerName: t('col.vat'),
      flex: 0,
      width: PCT_W,
      ...numCol,
      editable: editable(),
      ...numEditor(t('col.vat')),
      valueSetter: numSetter('vatPct', t('col.vat')),
      valueFormatter: (s) => (s.node?.rowPinned || empty(s) ? '' : pct(s.value)),
    }
    if (p.amountOnly) {
      const origin = p.originLines ?? []
      const originLabel = (id: number | null) => {
        const o = origin.find((l) => l.id === id)
        return o ? `${o.stockNo} · ${o.name}` : ''
      }
      const amount: ColDef<Row>[] = [
        rn,
        {
          colId: 'origin',
          field: 'originLineId',
          headerName: t('col.originLine'),
          flex: 1,
          minWidth: 180,
          colSpan: pinnedSpan(3),
          editable: editable(() => origin.length > 0),
          cellEditor: 'agSelectCellEditor',
          cellEditorParams: { values: [null, ...origin.map((l) => l.id)] },
          valueFormatter: (s) => (s.node?.rowPinned ? '' : originLabel(s.value) || '—'),
          cellRenderer: (s: ICellRendererParams<Row>) =>
            s.node.rowPinned ? (
              <span>{s.data?.pinned?.label}</span>
            ) : (
              <span className={s.value ? 'font-mono' : 'text-fk-faint'}>
                {originLabel(s.value) || '—'}
              </span>
            ),
          valueSetter: (s) => {
            const id = s.newValue === null || s.newValue === '' ? null : Number(s.newValue)
            if (s.data.originLineId === id) return false
            const o = origin.find((l) => l.id === id)
            s.data.originLineId = id
            s.data.partId = o?.partId ?? null
            s.data.stockNo = o?.stockNo ?? ''
            s.data.name = o?.name ?? ''
            if (o) s.data.vatPct = o.vatPct ?? s.data.vatPct
            return true
          },
        },
        {
          colId: 'originNet',
          headerName: t('col.originNet'),
          flex: 0,
          width: MONEY_W,
          ...numCol,
          cellClass: 'fk-num fk-muted',
          valueGetter: (s) => origin.find((l) => l.id === s.data?.originLineId)?.netAmount ?? '',
          valueFormatter: (s) => (s.node?.rowPinned ? '' : s.value ? formatMoney(s.value) : '—'),
        },
        {
          colId: 'description',
          field: 'description',
          headerName: t('col.description'),
          flex: 1,
          minWidth: 140,
          editable: editable((r) => r.originLineId === null),
          ...numEditor(t('col.description'), true),
          valueSetter: (s) => {
            const v = String(s.newValue ?? '').slice(0, 200)
            if (s.data.description === v) return false
            s.data.description = v
            return true
          },
        },
        {
          colId: 'amount',
          field: 'unitPrice',
          headerName: t('col.amount'),
          flex: 0,
          width: MONEY_W,
          ...numCol,
          editable: editable(),
          ...numEditor(t('col.amount')),
          valueSetter: numSetter('unitPrice', t('col.amount')),
          valueGetter: (s) => (s.node?.rowPinned ? s.data?.pinned?.gross : s.data?.unitPrice),
          valueFormatter: (s) => money4(s.value),
        },
        vat,
        net,
      ]
      return amount
    }
    const showCap = hasCaps
    const goods: ColDef<Row>[] = [
      rn,
      {
        colId: 'part',
        field: 'stockNo',
        headerName: t('col.part'),
        flex: 0,
        width: monoWidth(12, true),
        cellClass: 'font-mono',
        colSpan: pinnedSpan(showCap ? 4 : 3),
        editable: editable((_r, c) => !c.partsFixed),
        cellEditor: PartEditor,
        cellEditorParams: {
          orgId: p.orgId,
          depots: p.depots,
          date: p.date,
          onPicked: (rowIndex: number) => startEdit(rowIndex, 'qty'),
        },
        suppressKeyboardEvent: (s: SuppressKeyboardEventParams<Row>) =>
          s.editing && EDIT_KEYS.includes(s.event.key),
        valueSetter: (s) => {
          const hit = s.newValue as CatalogHit | string | null
          if (!hit || typeof hit !== 'object') return false
          Object.assign(s.data, withPart(s.data, hit, ctx(s).card))
          return true
        },
        cellRenderer: (s: ICellRendererParams<Row>) =>
          s.node.rowPinned ? <span className="font-sans">{s.data?.pinned?.label}</span> : s.value,
      },
      {
        colId: 'name',
        field: 'name',
        headerName: t('col.description'),
        flex: 1,
        minWidth: 110,
        cellClass: 'text-fk-muted',
        tooltipField: 'name',
      },
      {
        colId: 'stock',
        headerName: t('col.stock'),
        flex: 0,
        width: monoWidth(5, true),
        ...numCol,
        valueGetter: (s) =>
          s.data && !s.node?.rowPinned ? availableHere(s.data, ctx(s).depotId, ctx(s).rows) : null,
        valueFormatter: (s) =>
          s.value === null || s.value === undefined ? '' : formatQty(s.value),
        cellClassRules: {
          'fk-stock-ok': (s) => !!s.data && stockTone(s.data, ctx(s).depotId, ctx(s).rows) === 'ok',
          'fk-stock-short': (s) =>
            !!s.data && stockTone(s.data, ctx(s).depotId, ctx(s).rows) === 'short',
          'fk-stock-none': (s) =>
            !!s.data && stockTone(s.data, ctx(s).depotId, ctx(s).rows) === 'none',
        },
        tooltipValueGetter: (s) =>
          s.data?.stock
            ?.map((x) => {
              const code = ctx(s).depots.find((d) => d.id === x.depotId)?.code ?? x.depotId
              return t('stockTip', { code, qty: formatQty(x.available) })
            })
            .join(' · ') ?? '',
      },
      ...(showCap
        ? [
            {
              colId: 'returnable',
              headerName: t('col.returnable'),
              flex: 0,
              width: monoWidth(5, true),
              ...numCol,
              cellClass: 'fk-num fk-muted',
              valueGetter: (s) =>
                s.data && !s.node?.rowPinned && s.data.originLineId !== null
                  ? (ctx(s).caps?.get(s.data.originLineId) ?? null)
                  : null,
              valueFormatter: (s) => (s.value === null ? '' : formatQty(s.value)),
            } as ColDef<Row>,
          ]
        : []),
      {
        colId: 'qty',
        field: 'qty',
        headerName: t('col.qty'),
        flex: 0,
        width: QTY_W,
        ...numCol,
        editable: editable((r, c) => r.partId !== null && !c.qtyFixed),
        ...numEditor(t('col.qty')),
        valueSetter: numSetter('qty', t('col.qty')),
        valueGetter: (s) => (s.node?.rowPinned ? '' : s.data?.qty),
        valueFormatter: (s) => formatQty(s.value),
      },
      {
        colId: 'price',
        field: 'unitPrice',
        headerName: t('col.price'),
        flex: 0,
        width: MONEY_W,
        ...numCol,
        editable: editable((r) => r.partId !== null),
        ...numEditor(t('col.price')),
        valueSetter: numSetter('unitPrice', t('col.price')),
        valueGetter: (s) => (s.node?.rowPinned ? s.data?.pinned?.gross : s.data?.unitPrice),
        valueFormatter: (s) => money4(s.value),
      },
      {
        colId: 'd1',
        field: 'discount1Pct',
        headerName: t('col.d1'),
        flex: 0,
        width: DISC_W,
        ...numCol,
        colSpan: pinnedSpan(3),
        editable: editable((r) => r.partId !== null),
        ...numEditor(t('col.d1')),
        valueSetter: numSetter('discount1Pct', t('col.d1')),
        valueGetter: (s) => (s.node?.rowPinned ? s.data?.pinned?.discount : s.data?.discount1Pct),
        valueFormatter: (s) =>
          s.node?.rowPinned ? formatMoney(-Number(s.value || 0)) : empty(s) ? '' : pct(s.value),
        cellClassRules: { 'fk-pinned-discount': (s) => Boolean(s.node?.rowPinned) },
      },
      {
        colId: 'd2',
        field: 'discount2Pct',
        headerName: t('col.d2'),
        flex: 0,
        width: DISC_W,
        ...numCol,
        editable: editable((r) => r.partId !== null),
        ...numEditor(t('col.d2')),
        valueSetter: numSetter('discount2Pct', t('col.d2')),
        valueFormatter: (s) => (empty(s) ? '' : pct(s.value)),
      },
      vat,
      net,
    ]
    return goods
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [t, i18n.language, p.amountOnly, p.originLines, hasCaps, p.orgId, p.depots, p.date])

  const pinned = useMemo<Row[]>(() => {
    const lines = p.rows
    let gross = 0
    let discount = 0
    let net = 0
    let units = 0
    let count = 0
    for (const l of lines) {
      if (!p.amountOnly && l.partId === null) continue
      if (p.amountOnly && l.unitPrice === '') continue
      const a = amountsOf(l, p.amountOnly ? 'PRICE_DIFFERENCE' : 'SALES_INVOICE')
      gross += Number(a.grossAmount)
      discount += Number(a.discountAmount)
      net += Number(a.netAmount)
      units += Number(l.qty || 0)
      count += 1
    }
    const label = p.amountOnly
      ? t('pinnedLines', { count })
      : t('pinnedUnits', { count, units: formatQty(units) })
    return [
      {
        ...blankLine(),
        key: 'pinned',
        pinned: {
          label,
          gross: gross.toFixed(2),
          discount: discount.toFixed(2),
          net: net.toFixed(2),
        },
      },
    ]
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [p.rows, p.amountOnly, t, i18n.language])

  // Cells read caps, the depot and the bad row from the context; redraw when those change.
  useEffect(() => {
    api()?.redrawRows()
  }, [p.badRow, p.caps, p.depotId, p.originLines])

  const onCellKeyDown = (e: CellKeyDownEvent<Row>) => {
    const ev = e.event as KeyboardEvent | undefined
    if (!ev || e.node.rowPinned || !live.current.editable) return
    const editing = (api()?.getEditingCells().length ?? 0) > 0
    if (ev.key === 'Insert') {
      ev.preventDefault()
      addLine(e.rowIndex ?? undefined)
    } else if (ev.key === 'Delete' && !editing) {
      ev.preventDefault()
      deleteAt(e.rowIndex ?? 0)
    } else if (ev.key === 'F2' && !editing) {
      ev.preventDefault()
      const col = live.current.partsFixed ? e.column.getColId() : firstCol()
      startEdit(e.rowIndex ?? 0, col)
    }
  }

  // Tab past the last cell opens a new line (or the empty one at the end) in the part search.
  const tabToNextCell = (s: TabToNextCellParams<Row>): CellPosition | null => {
    const end = !s.nextCellPosition || Boolean(s.nextCellPosition.rowPinned)
    if (!end || s.backwards || !live.current.canAdd || !live.current.editable) {
      return s.nextCellPosition
    }
    setTimeout(() => {
      const a = api()
      if (!a) return
      a.stopEditing()
      const count = a.getDisplayedRowCount()
      const last = a.getDisplayedRowAtIndex(count - 1)?.data
      const empty = last && (live.current.amountOnly ? last.unitPrice === '' : last.partId === null)
      if (empty) startEdit(count - 1, firstCol())
      else addLine()
    }, 0)
    return null
  }

  const onCellValueChanged = (e: CellValueChangedEvent<Row>) => {
    e.api.refreshCells({ rowNodes: [e.node], force: true })
    changed()
  }

  const height = 36 + 36 + 2 + Math.max(4, Math.min(p.rows.length, 12)) * 34

  return (
    <FkGrid
      ref={grid}
      height={height}
      columnDefs={columnDefs}
      rowData={p.seed}
      getRowId={(s) => s.data.key}
      pinnedBottomRowData={pinned}
      defaultColDef={{
        sortable: false,
        cellDataType: false,
        suppressKeyboardEvent: (s) =>
          !s.editing && ['Insert', 'Delete', 'F2'].includes(s.event.key) ? true : false,
      }}
      context={p}
      rowClassRules={{ 'fk-row-bad': (s) => s.rowIndex === ctx(s).badRow }}
      singleClickEdit={false}
      tooltipShowDelay={300}
      onCellKeyDown={onCellKeyDown}
      onCellValueChanged={onCellValueChanged}
      tabToNextCell={tabToNextCell}
      suppressMovableColumns
      overlayNoRowsTemplate={`<span class="text-fk-muted">${t('noLines')}</span>`}
      className={cn('fk-doc-lines fk-dense', !p.editable && 'fk-readonly')}
    />
  )
}

export const LinesGrid = forwardRef(LinesGridInner)
