import { useCallback, useMemo, useRef } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import type { AgGridReact } from 'ag-grid-react'
import type { ColDef, ICellRendererParams } from 'ag-grid-community'
import { exportParts, gridParts, listCatalogs } from '../api/endpoints'
import type { GridRequest, PartColId, PartRow, PartsRequest, StockOnly } from '../api/types'
import { useApi } from '../hooks/useApi'
import { useDepot, withDepot } from '../hooks/useDepot'
import { useDepots } from '../hooks/useDepots'
import { useDownload } from '../hooks/useDownload'
import { useInfiniteGrid } from '../hooks/useInfiniteGrid'
import { useOrgId } from '../hooks/useOrgId'
import { formatMoney, formatQty } from '../lib/format'
import { saveCsv } from '../lib/csv'
import { Button } from '../components/ui/button'
import { Chip } from '../components/ui/chip'
import { Panel } from '../components/ui/panel'
import { Segmented } from '../components/ui/segmented'
import { FkGrid } from '../components/grid/FkGrid'
import { exprFilter, monoWidth, numCol } from '../components/grid/columns'
import GridFooter from '../components/grid/GridFooter'
import ColumnsMenu from '../components/grid/ColumnsMenu'
import SelectCompany from '../components/layout/SelectCompany'

const STOCK: StockOnly[] = ['ALL', 'IN_STOCK', 'BELOW_MIN', 'NEGATIVE', 'BELOW_MIN_OR_NEGATIVE']
const MAX_BODY = 14
const QTY_W: Record<string, number> = {
  onHand: monoWidth(7),
  available: 100,
  minQty: monoWidth(4),
  maxQty: monoWidth(4),
}

type Row = PartRow & { label?: string }
type Qty = Extract<PartColId, 'onHand' | 'reserved' | 'available' | 'minQty' | 'maxQty'>

const codeList = (codes: PartRow['codes']) => {
  const pick = (kind: string) => (codes ?? []).filter((c) => c.kind === kind).map((c) => c.code)
  return [...pick('OEM'), ...pick('ALT')]
}

// First OEM (else ALT) code and how many more: "3774612M91 +1".
function codeSummary(codes: PartRow['codes']): string {
  const [first, ...rest] = codeList(codes)
  if (!first) return ''
  return rest.length ? `${first} +${rest.length}` : first
}

export default function PartsPage() {
  const orgId = useOrgId()
  return orgId ? <Parts orgId={orgId} /> : <SelectCompany />
}

function Parts({ orgId }: Readonly<{ orgId: number }>) {
  const { t, i18n } = useTranslation(['parts', 'grid'])
  const [params, setParams] = useSearchParams()
  const { depotId } = useDepot()
  const stock = (params.get('stock') as StockOnly | null) ?? 'ALL'
  const catalogId = params.get('catalog') ? Number(params.get('catalog')) : null
  const includeInactive = params.get('inactive') === '1'
  const gridRef = useRef<AgGridReact>(null)
  const download = useDownload()
  const catalogs = useApi(() => listCatalogs(orgId), [orgId]).data ?? []
  const all = useDepots()
  const depots = useMemo(() => [...all].sort((a, b) => a.code.localeCompare(b.code)), [all])

  const setParam = (key: string, value: string | null) =>
    setParams((p) => {
      const next = new URLSearchParams(p)
      if (value) next.set(key, value)
      else next.delete(key)
      return next
    })

  const scope = useMemo(
    () => ({ depotId, stock, catalogId, includeInactive }),
    [depotId, stock, catalogId, includeInactive],
  )
  const fetchPage = useCallback(
    (req: GridRequest) => gridParts(orgId, { ...(req as PartsRequest), ...scope }),
    [orgId, scope],
  )
  const grid = useInfiniteGrid<PartRow>(fetchPage, [orgId, scope])

  const qty = (field: Qty, withFilter = true): ColDef<Row> => ({
    colId: field,
    field,
    headerName: t(`col.${field}`),
    flex: 0,
    width: QTY_W[field],
    hide: (field === 'minQty' || field === 'maxQty') && !depotId,
    ...numCol,
    ...(withFilter ? exprFilter({ kind: 'number', placeholder: t('grid:fk.positive') }) : {}),
    valueFormatter: (p) => formatQty(p.value),
    cellClassRules: { 'fk-neg': (p) => Number(p.value) < 0 },
  })

  const columnDefs = useMemo<ColDef<Row>[]>(
    () => [
      {
        colId: 'stockNo',
        field: 'stockNo',
        headerName: t('col.stockNo'),
        sort: 'asc',
        flex: 0,
        width: monoWidth(10),
        cellClass: 'font-mono',
        colSpan: (p) => (p.node?.rowPinned ? 3 : 1),
        ...exprFilter({ kind: 'text', placeholder: t('startsWith'), defaultOp: 'startsWith' }),
        cellRenderer: (p: ICellRendererParams<Row>) =>
          p.node.rowPinned ? (
            <span className="font-sans">{p.data?.label}</span>
          ) : p.data ? (
            <Link to={withDepot(`/parts/${p.data.id}`, depotId)}>{p.value}</Link>
          ) : null,
      },
      {
        colId: 'name',
        field: 'name',
        headerName: t('col.name'),
        flex: 1,
        minWidth: 160,
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
      },
      {
        colId: 'brand',
        field: 'brand',
        headerName: t('col.brand'),
        flex: 0,
        width: 100,
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
      },
      {
        colId: 'catalogCode',
        field: 'catalogCode',
        headerName: t('col.catalogCode'),
        flex: 0,
        width: monoWidth(4),
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'text', placeholder: '=', defaultOp: 'equals' }),
      },
      {
        colId: 'codes',
        headerName: t('col.codes'),
        flex: 0,
        width: monoWidth(14),
        cellClass: 'font-mono text-fk-muted',
        sortable: false,
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
        valueGetter: (p) => (p.data && !p.node?.rowPinned ? codeSummary(p.data.codes) : ''),
      },
      qty('onHand'),
      ...(depotId
        ? []
        : depots.map(
            (d): ColDef<Row> => ({
              colId: `depot.${d.id}`,
              headerName: t('col.depot', { code: d.code }),
              flex: 0,
              width: monoWidth(6),
              sortable: false,
              ...numCol,
              valueGetter: (p) =>
                p.node?.rowPinned
                  ? null
                  : (p.data?.stockByDepot?.find((s) => s.depotId === d.id)?.onHand ?? null),
              valueFormatter: (p) => (p.value === null ? '' : formatQty(p.value)),
              cellClassRules: { 'fk-neg': (p) => Number(p.value) < 0 },
            }),
          )),
      qty('available'),
      qty('minQty'),
      qty('maxQty'),
      {
        colId: 'shelf',
        field: 'shelf',
        headerName: t('col.shelf'),
        flex: 0,
        width: monoWidth(7),
        hide: !depotId,
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'text', placeholder: 'A-0', defaultOp: 'startsWith' }),
      },
      {
        colId: 'listPrice',
        field: 'listPrice',
        headerName: t('col.listPrice'),
        flex: 0,
        width: monoWidth(10),
        ...numCol,
        ...exprFilter({ kind: 'number', placeholder: t('grid:fk.positive') }),
        valueFormatter: (p) => (p.node?.rowPinned ? '' : formatMoney(p.value)),
      },
      {
        colId: 'status',
        headerName: t('col.status'),
        flex: 0,
        width: 100,
        sortable: false,
        cellRenderer: (p: ICellRendererParams<Row>) => {
          const r = p.data
          if (!r || p.node.rowPinned) return null
          if (r.negative)
            return (
              <Chip size="sm" tone="red">
                {t('chip.NEGATIVE')}
              </Chip>
            )
          if (r.belowMin)
            return (
              <Chip size="sm" tone="amber">
                {t('chip.BELOW_MIN')}
              </Chip>
            )
          if (!r.isActive)
            return (
              <Chip size="sm" tone="grey">
                {t('chip.INACTIVE')}
              </Chip>
            )
          return null
        },
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, i18n.language, depotId, depots],
  )

  const labels = Object.fromEntries(
    columnDefs.filter((c) => c.headerName).map((c) => [c.colId!, c.headerName!]),
  )

  const pinned = useMemo<Row[]>(() => {
    const info = grid.info
    if (!info) return []
    const shown = t('grid:fk.shownOf', {
      shown: formatQty(info.totalCount),
      total: formatQty(info.unfilteredCount),
    })
    const label = grid.filtered.length
      ? `${shown} · ${t('grid:fk.filters', { count: grid.filtered.length })}`
      : shown
    const totals = info.totals
    return [
      {
        label,
        onHand: totals?.onHand ?? '',
        reserved: totals?.reserved ?? '',
        available: totals?.available ?? '',
      } as Row,
    ]
  }, [grid.info, grid.filtered, t])

  const total = grid.info?.totalCount ?? 0
  const height = 36 + 34 + 36 + 2 + Math.max(1, Math.min(total, MAX_BODY)) * 34

  const csvColumns = [
    { header: labels.stockNo, value: (r: PartRow) => r.stockNo },
    { header: labels.name, value: (r: PartRow) => r.name },
    { header: labels.brand, value: (r: PartRow) => r.brand ?? '' },
    { header: labels.catalogCode, value: (r: PartRow) => String(r.catalogCode) },
    { header: labels.codes, value: (r: PartRow) => codeList(r.codes).join(' | ') },
    { header: labels.onHand, value: (r: PartRow) => r.onHand },
    { header: labels.available, value: (r: PartRow) => r.available },
    ...(depotId
      ? [
          { header: labels.minQty, value: (r: PartRow) => r.minQty ?? '' },
          { header: labels.maxQty, value: (r: PartRow) => r.maxQty ?? '' },
          { header: labels.shelf, value: (r: PartRow) => r.shelf ?? '' },
        ]
      : []),
    { header: labels.listPrice, value: (r: PartRow) => r.listPrice ?? '' },
  ]

  return (
    <div className="flex min-h-0 flex-1 flex-col gap-3">
      <div className="flex items-end gap-3">
        <h1 className="flex-1 text-2xl font-semibold">{t('title')}</h1>
        <Button asChild variant="outline">
          <Link to={withDepot('/parts/import', depotId)}>{t('importPriceList')}</Link>
        </Button>
      </div>
      <div className="flex flex-wrap items-center gap-2">
        <Segmented
          label={t('stockFilter')}
          size="sm"
          value={stock}
          onChange={(v) => setParam('stock', v === 'ALL' ? null : v)}
          options={STOCK.map((v) => ({ value: v, label: t(`stock.${v}`) }))}
        />
        <select
          aria-label={t('catalog')}
          value={catalogId ?? ''}
          onChange={(e) => setParam('catalog', e.target.value || null)}
          className="h-[30px] min-w-[180px] rounded-md border border-fk-input bg-fk-surface px-2 text-13"
        >
          <option value="">{t('allCatalogs')}</option>
          {catalogs.map((c) => (
            <option key={c.id} value={c.id}>
              {c.code} · {c.name}
            </option>
          ))}
        </select>
        <label className="flex items-center gap-1.5 whitespace-nowrap text-13 text-fk-muted">
          <input
            type="checkbox"
            checked={includeInactive}
            onChange={(e) => setParam('inactive', e.target.checked ? '1' : null)}
            className="h-3.5 w-3.5 accent-[var(--fk-accent)]"
          />
          {t('includeInactive')}
        </label>
        <div className="flex-1" />
        <ColumnsMenu
          columnApi={() => gridRef.current?.columnApi}
          columns={columnDefs
            .filter((c) => c.headerName && c.colId !== 'stockNo' && !c.hide)
            .map((c) => ({ colId: c.colId!, label: c.headerName! }))}
        />
        <Button
          variant="outline"
          size="sm"
          disabled={download.busy !== null}
          onClick={async () => saveCsv(await grid.fetchAll(), csvColumns, 'parts.csv')}
        >
          {t('grid:fk.csv')}
        </Button>
        <Button
          variant="outline"
          size="sm"
          disabled={download.busy !== null}
          onClick={() =>
            download.run('xlsx', 'parts.xlsx', () =>
              exportParts(orgId, { ...(grid.request() as PartsRequest), ...scope }),
            )
          }
        >
          {t('grid:fk.excel')}
        </Button>
      </div>
      <Panel className="flex min-h-0 shrink flex-col overflow-hidden">
        <FkGrid
          ref={gridRef}
          height={height}
          className="min-h-0 shrink"
          columnDefs={columnDefs}
          {...grid.gridProps}
          getRowId={(p) => String(p.data.id)}
          pinnedBottomRowData={pinned}
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
      </Panel>
    </div>
  )
}
