import { useCallback, useMemo, useRef } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import type { AgGridReact } from 'ag-grid-react'
import type { ColDef, ICellRendererParams, RowClickedEvent } from 'ag-grid-community'
import { exportDocuments, gridDocuments } from '../api/endpoints'
import type {
  DocumentsRequest,
  DocumentStatus,
  DocumentSummary,
  DocumentType,
  GridRequest,
} from '../api/types'
import { useDepot, withDepot } from '../hooks/useDepot'
import { useDownload } from '../hooks/useDownload'
import { useInfiniteGrid } from '../hooks/useInfiniteGrid'
import { useOrgId } from '../hooks/useOrgId'
import { DOC_TYPES } from '../lib/documents'
import { formatDay, formatMoney, formatQty } from '../lib/format'
import { saveCsv } from '../lib/csv'
import { AccountPicker, type AccountHit } from '../components/AccountPicker'
import { Button } from '../components/ui/button'
import { Chip } from '../components/ui/chip'
import { DateField } from '../components/ui/date-field'
import { Panel } from '../components/ui/panel'
import { Segmented } from '../components/ui/segmented'
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '../components/ui/dropdown-menu'
import { FkGrid } from '../components/grid/FkGrid'
import { DOC_NO_CHARS, exprFilter, monoWidth, numCol } from '../components/grid/columns'
import GridFooter from '../components/grid/GridFooter'
import SelectCompany from '../components/layout/SelectCompany'
import { docTone } from './statement/docType'
import { STATUS_TONE } from './document/status'

const STATUSES: (DocumentStatus | 'ALL')[] = ['ALL', 'DRAFT', 'POSTED', 'CANCELLED']
const MAX_BODY = 14
const MONEY_W = monoWidth(13)

type Row = DocumentSummary & { label?: string }

export default function DocumentsPage() {
  const orgId = useOrgId()
  return orgId ? <Documents orgId={orgId} /> : <SelectCompany />
}

function Documents({ orgId }: Readonly<{ orgId: number }>) {
  const { t, i18n } = useTranslation(['documents', 'grid'])
  const [params, setParams] = useSearchParams()
  const navigate = useNavigate()
  const { depotId } = useDepot()
  const gridRef = useRef<AgGridReact>(null)
  const download = useDownload()

  const types = useMemo(
    () => (params.get('types')?.split(',').filter(Boolean) ?? []) as DocumentType[],
    [params],
  )
  const status = (params.get('status') as DocumentStatus | null) ?? 'ALL'
  const sicilId = params.get('sicil') ? Number(params.get('sicil')) : null
  const sicilLabel = params.get('sicilName') ?? ''
  const dateFrom = params.get('from') ?? ''
  const dateTo = params.get('to') ?? ''
  const documentNo = params.get('no') ?? ''

  const setParam = (values: Record<string, string | null>) =>
    setParams((p) => {
      const next = new URLSearchParams(p)
      for (const [k, v] of Object.entries(values)) {
        if (v) next.set(k, v)
        else next.delete(k)
      }
      return next
    })

  const scope = useMemo(
    (): Omit<DocumentsRequest, 'startRow' | 'endRow'> => ({
      depotId,
      types,
      statuses: status === 'ALL' ? [] : [status],
      sicilId,
      dateFrom: dateFrom || null,
      dateTo: dateTo || null,
      documentNo: documentNo.trim() || null,
    }),
    [depotId, types, status, sicilId, dateFrom, dateTo, documentNo],
  )
  const fetchPage = useCallback(
    (req: GridRequest) => gridDocuments(orgId, { ...(req as DocumentsRequest), ...scope }),
    [orgId, scope],
  )
  const grid = useInfiniteGrid<Row>(fetchPage, [orgId, scope])

  const typeLabel = (v: DocumentType, dir?: string | null) =>
    v === 'PRICE_DIFFERENCE' && dir ? t(`typeShort.${v}_${dir}`) : t(`typeShort.${v}`)

  const money = (colId: keyof DocumentSummary, width = MONEY_W): ColDef<Row> => ({
    colId,
    field: colId,
    headerName: t(`list.${colId}`),
    flex: 0,
    width,
    ...numCol,
    ...exprFilter({ kind: 'number', placeholder: t('grid:fk.positive') }),
    valueFormatter: (p) => formatMoney(p.value),
    cellClassRules: { 'fk-muted': (p) => p.data?.status === 'CANCELLED' },
  })

  const columnDefs = useMemo<ColDef<Row>[]>(
    () => [
      {
        colId: 'documentNo',
        field: 'documentNo',
        headerName: t('list.documentNo'),
        flex: 0,
        width: monoWidth(DOC_NO_CHARS),
        cellClass: 'font-mono',
        colSpan: (p) => (p.node?.rowPinned ? 4 : 1),
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
        cellRenderer: (p: ICellRendererParams<Row>) => {
          if (p.node.rowPinned) return <span className="font-sans">{p.data?.label}</span>
          if (!p.data) return null
          return (
            <Link to={withDepot(`/invoices/${p.data.id}`, depotId)}>
              {p.value ?? <span className="font-sans text-fk-muted">{t('draftNo')}</span>}
            </Link>
          )
        },
      },
      {
        colId: 'type',
        field: 'type',
        headerName: t('list.type'),
        flex: 0,
        width: 148,
        ...exprFilter({
          kind: 'enum',
          anyLabel: t('grid:fk.any'),
          options: DOC_TYPES.map((v) => ({ value: v, label: t(`typeShort.${v}`) })),
        }),
        cellRenderer: (p: ICellRendererParams<Row>) =>
          p.data && !p.node.rowPinned ? (
            <Chip size="sm" tone={docTone(p.data.type)}>
              {typeLabel(p.data.type, p.data.priceDirection)}
            </Chip>
          ) : null,
      },
      {
        colId: 'status',
        field: 'status',
        headerName: t('list.status'),
        flex: 0,
        width: 112,
        ...exprFilter({
          kind: 'enum',
          anyLabel: t('grid:fk.any'),
          options: (['DRAFT', 'POSTED', 'CANCELLED'] as const).map((v) => ({
            value: v,
            label: t(`status.${v}`),
          })),
        }),
        cellRenderer: (p: ICellRendererParams<Row>) =>
          p.data && !p.node.rowPinned ? (
            <Chip size="sm" tone={STATUS_TONE[p.data.status]}>
              {t(`status.${p.data.status}`)}
            </Chip>
          ) : null,
      },
      {
        colId: 'documentDate',
        field: 'documentDate',
        headerName: t('list.documentDate'),
        sort: 'desc',
        flex: 0,
        width: monoWidth(10),
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'date', placeholder: t('grid:fk.range_hint') }),
        valueFormatter: (p) => formatDay(p.value),
      },
      {
        colId: 'hesapNo',
        field: 'hesapNo',
        headerName: t('list.hesapNo'),
        flex: 0,
        width: monoWidth(10),
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'text', placeholder: t('startsWith'), defaultOp: 'startsWith' }),
      },
      {
        colId: 'sicilName',
        field: 'sicilName',
        headerName: t('list.sicilName'),
        flex: 1,
        minWidth: 160,
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
      },
      {
        colId: 'depotCode',
        field: 'depotCode',
        headerName: t('list.depotCode'),
        flex: 0,
        width: 72,
        hide: Boolean(depotId),
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'text', placeholder: '=', defaultOp: 'equals' }),
      },
      {
        colId: 'dueDate',
        field: 'dueDate',
        headerName: t('list.dueDate'),
        flex: 0,
        width: monoWidth(10),
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'date', placeholder: t('grid:fk.range_hint') }),
        valueFormatter: (p) => formatDay(p.value),
      },
      money('netTotal'),
      money('vatTotal', monoWidth(11)),
      money('grandTotal'),
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, i18n.language, depotId],
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
    return [
      {
        label: `${shown} · ${t('list.totalsNote')}`,
        netTotal: info.totals?.netTotal ?? '',
        vatTotal: info.totals?.vatTotal ?? '',
        grandTotal: info.totals?.grandTotal ?? '',
      } as Row,
    ]
  }, [grid.info, t])

  const total = grid.info?.totalCount ?? 0
  const height = 36 + 34 + 36 + 2 + Math.max(1, Math.min(total, MAX_BODY)) * 34

  const csvColumns = [
    { header: labels.documentNo, value: (r: Row) => r.documentNo ?? '' },
    { header: labels.type, value: (r: Row) => typeLabel(r.type, r.priceDirection) },
    { header: labels.status, value: (r: Row) => t(`status.${r.status}`) },
    { header: labels.documentDate, value: (r: Row) => r.documentDate },
    { header: labels.hesapNo, value: (r: Row) => r.hesapNo },
    { header: labels.sicilName, value: (r: Row) => r.sicilName },
    { header: labels.depotCode, value: (r: Row) => r.depotCode },
    { header: labels.dueDate, value: (r: Row) => r.dueDate ?? '' },
    { header: labels.netTotal, value: (r: Row) => r.netTotal },
    { header: labels.vatTotal, value: (r: Row) => r.vatTotal },
    { header: labels.grandTotal, value: (r: Row) => r.grandTotal },
  ]

  const account: AccountHit | null = sicilId
    ? {
        sicilId,
        accountId: null,
        hesapNo: sicilLabel.split(' · ')[0] ?? '',
        hesapAdi: sicilLabel.split(' · ')[1] ?? '',
      }
    : null

  return (
    <div className="flex min-h-0 flex-1 flex-col gap-3">
      <div className="flex items-end gap-3">
        <h1 className="flex-1 text-2xl font-semibold">{t('list.title')}</h1>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button className="min-w-[120px]">{t('list.new')}</Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-[260px]">
            {DOC_TYPES.map((ty) => (
              <DropdownMenuItem
                key={ty}
                onSelect={() => navigate(withDepot(`/invoices/new?type=${ty}`, depotId))}
              >
                {t(`type.${ty}`)}
              </DropdownMenuItem>
            ))}
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
      <div className="flex flex-wrap items-end gap-2">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline" size="sm" className="min-w-[132px] justify-between">
              {types.length ? t('list.typesN', { count: types.length }) : t('list.allTypes')}
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="start" className="w-[260px]">
            {DOC_TYPES.map((ty) => (
              <DropdownMenuCheckboxItem
                key={ty}
                checked={types.includes(ty)}
                onSelect={(e) => e.preventDefault()}
                onCheckedChange={(on) =>
                  setParam({
                    types: (on ? [...types, ty] : types.filter((x) => x !== ty)).join(',') || null,
                  })
                }
              >
                {t(`type.${ty}`)}
              </DropdownMenuCheckboxItem>
            ))}
          </DropdownMenuContent>
        </DropdownMenu>
        <Segmented
          label={t('list.status')}
          size="sm"
          value={status}
          onChange={(v) => setParam({ status: v === 'ALL' ? null : v })}
          options={STATUSES.map((v) => ({
            value: v,
            label: v === 'ALL' ? t('list.allStatuses') : t(`status.${v}`),
          }))}
        />
        <AccountPicker
          orgId={orgId}
          label={t('list.accountFilter')}
          hideLabel
          placeholder={t('list.accountFilter')}
          value={account}
          onPick={(a) =>
            setParam({ sicil: String(a.sicilId), sicilName: `${a.hesapNo} · ${a.hesapAdi}` })
          }
          className="w-[240px] [&>input]:h-[30px] [&>input]:text-13"
        />
        {sicilId && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setParam({ sicil: null, sicilName: null })}
          >
            {t('list.clearAccount')}
          </Button>
        )}
        <DateField
          label=""
          value={dateFrom}
          ariaLabel={t('list.dateFrom')}
          onChange={(v) => setParam({ from: v })}
          inputClassName="h-[30px] w-[108px] text-13"
        />
        <span className="pb-1.5 text-13 text-fk-muted">–</span>
        <DateField
          label=""
          value={dateTo}
          ariaLabel={t('list.dateTo')}
          onChange={(v) => setParam({ to: v })}
          inputClassName="h-[30px] w-[108px] text-13"
        />
        <input
          aria-label={t('list.numberFilter')}
          placeholder={t('list.numberFilter')}
          value={documentNo}
          onChange={(e) => setParam({ no: e.target.value })}
          className="h-[30px] w-[150px] rounded-md border border-fk-input bg-fk-surface px-2 font-mono text-13 outline-none placeholder:font-sans focus:border-fk-accent"
        />
        <div className="flex-1" />
        <Button
          variant="outline"
          size="sm"
          disabled={download.busy !== null}
          onClick={async () => saveCsv(await grid.fetchAll(), csvColumns, 'documents.csv')}
        >
          {t('grid:fk.csv')}
        </Button>
        <Button
          variant="outline"
          size="sm"
          disabled={download.busy !== null}
          onClick={() =>
            download.run('xlsx', 'documents.xlsx', () =>
              exportDocuments(orgId, { ...(grid.request() as DocumentsRequest), ...scope }),
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
          onRowClicked={(e: RowClickedEvent<Row>) => {
            const target = e.event?.target as HTMLElement | undefined
            if (e.data && !e.node.rowPinned && !target?.closest('a')) {
              navigate(withDepot(`/invoices/${e.data.id}`, depotId))
            }
          }}
          rowClass="cursor-pointer"
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
