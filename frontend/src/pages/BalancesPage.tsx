import { useCallback, useMemo, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import type { AgGridReact } from 'ag-grid-react'
import type {
  ColDef,
  ICellRendererParams,
  IHeaderParams,
  SelectionChangedEvent,
} from 'ag-grid-community'
import {
  exportBalances,
  gridBalances,
  printReminderLetters,
  printStatements,
} from '../api/endpoints'
import type { BalanceRow, GridRequest, SicilTipi } from '../api/types'
import { useDepot, withDepot } from '../hooks/useDepot'
import { useDownload } from '../hooks/useDownload'
import { useInfiniteGrid } from '../hooks/useInfiniteGrid'
import { useOrgId } from '../hooks/useOrgId'
import { formatDay, formatMoney, todayIso } from '../lib/format'
import { saveCsv } from '../lib/csv'
import { Button } from '../components/ui/button'
import { Chip, type ChipTone } from '../components/ui/chip'
import { DateField } from '../components/ui/date-field'
import { Panel } from '../components/ui/panel'
import { Segmented } from '../components/ui/segmented'
import { FkGrid, exprFilter, numCol } from '../components/grid/FkGrid'
import GridFooter from '../components/grid/GridFooter'
import ColumnsMenu from '../components/grid/ColumnsMenu'
import SelectCompany from '../components/layout/SelectCompany'

const TIPI: SicilTipi[] = ['MUSTERI', 'FIRMA', 'BANKA']
const TONE: Record<SicilTipi, ChipTone> = { MUSTERI: 'blue', FIRMA: 'grey', BANKA: 'amber' }
const MAX_BODY = 14

type Row = BalanceRow & { label?: string }

function SelectHeader(props: IHeaderParams & { ariaLabel: string }) {
  const [, force] = useState(0)
  let loaded = 0
  let selected = 0
  props.api.forEachNode((n) => {
    if (!n.data) return
    loaded++
    if (n.isSelected()) selected++
  })
  const all = loaded > 0 && selected === loaded
  return (
    <input
      type="checkbox"
      aria-label={props.ariaLabel}
      checked={all}
      ref={(el) => {
        if (el) el.indeterminate = selected > 0 && !all
      }}
      onChange={() => {
        props.api.forEachNode((n) => n.data && n.setSelected(!all))
        force((x) => x + 1)
      }}
      className="h-3.5 w-3.5 accent-[var(--fk-accent)]"
    />
  )
}

export default function BalancesPage() {
  const orgId = useOrgId()
  return orgId ? <Balances orgId={orgId} /> : <SelectCompany />
}

function Balances({ orgId }: Readonly<{ orgId: number }>) {
  const { t, i18n } = useTranslation(['balances', 'grid'])
  const [params, setParams] = useSearchParams()
  const { depotId } = useDepot()
  const asOf = params.get('asOf') ?? todayIso()
  const tipi = (params.get('type') as SicilTipi | null) ?? null
  const gridRef = useRef<AgGridReact>(null)
  const [selected, setSelected] = useState<BalanceRow[]>([])
  const download = useDownload()

  const setParam = (key: string, value: string | null) =>
    setParams((p) => {
      const next = new URLSearchParams(p)
      if (value) next.set(key, value)
      else next.delete(key)
      return next
    })

  const fetchPage = useCallback(
    (req: GridRequest) => gridBalances(orgId, { ...req, asOf, depotId, sicilTipi: tipi }),
    [orgId, asOf, depotId, tipi],
  )
  const grid = useInfiniteGrid<BalanceRow>(fetchPage, [orgId, asOf, depotId, tipi])

  const money = (
    field: keyof BalanceRow,
    colId: string,
    neg?: (n: number) => boolean,
  ): ColDef<Row> => ({
    colId,
    headerName: t(`col.${colId}`),
    field,
    flex: 0,
    width: colId === 'overdue' ? 108 : 124,
    ...numCol,
    ...exprFilter({ kind: 'number', placeholder: t('grid:fk.positive') }),
    valueFormatter: (p) => formatMoney(p.value),
    cellClassRules: neg ? { 'fk-neg': (p) => neg(Number(p.value)) } : undefined,
  })

  const columnDefs = useMemo<ColDef<Row>[]>(
    () => [
      {
        colId: 'select',
        headerName: '',
        width: 42,
        maxWidth: 42,
        minWidth: 42,
        flex: 0,
        sortable: false,
        resizable: false,
        checkboxSelection: (p) => !p.node.rowPinned,
        headerComponent: SelectHeader,
        headerComponentParams: { ariaLabel: t('selectAll') },
      },
      {
        colId: 'accountCode',
        field: 'accountCode',
        headerName: t('col.account'),
        sort: 'asc',
        flex: 0,
        width: 108,
        colSpan: (p) => (p.node?.rowPinned ? 4 : 1),
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'text', placeholder: '120.', defaultOp: 'startsWith' }),
        cellRenderer: (p: ICellRendererParams<Row>) =>
          p.node.rowPinned ? (
            <span className="font-sans">{p.data?.label}</span>
          ) : p.data ? (
            <Link to={withDepot(`/statements/${p.data.accountId}`, depotId)}>{p.value}</Link>
          ) : null,
      },
      {
        colId: 'name',
        field: 'accountName',
        headerName: t('col.name'),
        flex: 2,
        minWidth: 160,
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
        cellRenderer: (p: ICellRendererParams<Row>) =>
          p.data && !p.node.rowPinned ? (
            <Link to={withDepot(`/statements/${p.data.accountId}`, depotId)}>{p.value}</Link>
          ) : null,
      },
      {
        colId: 'city',
        field: 'city',
        headerName: t('col.city'),
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
      },
      {
        colId: 'sicilTipi',
        field: 'sicilTipi',
        headerName: t('col.type'),
        flex: 0,
        width: 112,
        ...exprFilter({
          kind: 'enum',
          anyLabel: t('grid:fk.any'),
          options: TIPI.map((v) => ({ value: v, label: t(`typeOption.${v}`) })),
        }),
        cellRenderer: (p: ICellRendererParams<Row>) =>
          p.value && !p.node.rowPinned ? (
            <Chip size="sm" tone={TONE[p.value as SicilTipi]}>
              {t(`chip.${p.value}`)}
            </Chip>
          ) : null,
      },
      money('debitTotal', 'debit'),
      money('creditTotal', 'credit'),
      money('balance', 'balance', (n) => n < 0),
      money('overdue', 'overdue', (n) => n > 0),
      {
        colId: 'lastMoveDate',
        field: 'lastMoveDate',
        headerName: t('col.lastMove'),
        flex: 0,
        width: 108,
        ...numCol,
        ...exprFilter({ kind: 'date', placeholder: t('grid:fk.before') }),
        valueFormatter: (p) => formatDay(p.value),
      },
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
    const shown = t('grid:fk.shownOf', { shown: info.totalCount, total: info.unfilteredCount })
    const label = grid.filtered.length
      ? `${shown} · ${t('grid:fk.filters', { count: grid.filtered.length })}`
      : shown
    return [
      {
        accountId: 0,
        label,
        debitTotal: info.totals?.debit ?? '',
        creditTotal: info.totals?.credit ?? '',
        balance: info.totals?.balance ?? '',
        overdue: info.totals?.overdue ?? '',
      },
    ]
  }, [grid.info, grid.filtered, t])

  const total = grid.info?.totalCount ?? 0
  const height = 36 + 34 + 36 + 2 + Math.max(1, Math.min(total, MAX_BODY)) * 34
  const selection = () => ({
    accountIds: selected.map((r) => r.accountId),
    to: asOf,
    depotId,
    language: (i18n.language?.startsWith('tr') ? 'tr' : 'en') as 'tr' | 'en',
  })

  const csvColumns = [
    { header: labels.accountCode, value: (r: BalanceRow) => r.accountCode ?? '' },
    { header: labels.name, value: (r: BalanceRow) => r.accountName ?? '' },
    { header: labels.city, value: (r: BalanceRow) => r.city ?? '' },
    { header: labels.sicilTipi, value: (r: BalanceRow) => r.sicilTipi ?? '' },
    { header: labels.debit, value: (r: BalanceRow) => r.debitTotal },
    { header: labels.credit, value: (r: BalanceRow) => r.creditTotal },
    { header: labels.balance, value: (r: BalanceRow) => r.balance },
    { header: labels.overdue, value: (r: BalanceRow) => r.overdue ?? '' },
    { header: labels.lastMoveDate, value: (r: BalanceRow) => r.lastMoveDate ?? '' },
  ]

  return (
    <div className="flex min-h-0 flex-1 flex-col gap-3">
      <div className="flex items-end gap-3">
        <h1 className="flex-1 text-2xl font-semibold">{t('title')}</h1>
        <DateField
          label={t('asOf')}
          value={asOf}
          onChange={(v) => setParam('asOf', v)}
          inputClassName="w-[120px]"
        />
        <Segmented
          label={t('accountType')}
          value={tipi}
          onChange={(v) => setParam('type', v)}
          options={[
            { value: null, label: t('types.all') },
            ...TIPI.map((v) => ({ value: v, label: t(`types.${v}`) })),
          ]}
        />
      </div>
      <div className="flex items-center gap-2">
        <span className="min-w-[72px] whitespace-nowrap text-13 text-fk-muted">
          {t('selected', { count: selected.length })}
        </span>
        <Button
          variant="outline"
          size="sm"
          disabled={!selected.length || download.busy !== null}
          onClick={() =>
            download.run('stm', `statements-${asOf}.pdf`, () => printStatements(orgId, selection()))
          }
        >
          {t('statementsPdf')}
        </Button>
        <Button
          variant="outline"
          size="sm"
          disabled={!selected.length || download.busy !== null}
          onClick={() =>
            download.run('rem', `reminders-${asOf}.pdf`, () =>
              printReminderLetters(orgId, selection()),
            )
          }
        >
          {t('reminderLetters')}
        </Button>
        <div className="flex-1" />
        <ColumnsMenu
          columnApi={() => gridRef.current?.columnApi}
          columns={columnDefs
            .filter((c) => c.headerName && c.colId !== 'accountCode')
            .map((c) => ({ colId: c.colId!, label: c.headerName! }))}
        />
        <Button
          variant="outline"
          size="sm"
          disabled={download.busy !== null}
          onClick={async () => saveCsv(await grid.fetchAll(), csvColumns, `balances-${asOf}.csv`)}
        >
          {t('grid:fk.csv')}
        </Button>
        <Button
          variant="outline"
          size="sm"
          disabled={download.busy !== null}
          onClick={() =>
            download.run('xlsx', `balances-${asOf}.xlsx`, () =>
              exportBalances(orgId, { ...grid.request(), asOf, depotId, sicilTipi: tipi }),
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
          getRowId={(p) => String(p.data.accountId)}
          rowSelection="multiple"
          suppressRowClickSelection
          pinnedBottomRowData={pinned}
          onSelectionChanged={(e: SelectionChangedEvent<Row>) => {
            setSelected(e.api.getSelectedRows())
            e.api.refreshHeader()
          }}
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
