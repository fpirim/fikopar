import { useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import type { ColDef, ICellRendererParams } from 'ag-grid-community'
import { exportCashBook, getCashBook } from '../api/endpoints'
import type { CashBookLine } from '../api/types'
import { useApi } from '../hooks/useApi'
import { useDepot } from '../hooks/useDepot'
import { useDepots } from '../hooks/useDepots'
import { useDownload } from '../hooks/useDownload'
import { useOrgId } from '../hooks/useOrgId'
import { formatDay, formatMoney, todayIso } from '../lib/format'
import PeriodButton from '../components/PeriodButton'
import { Button } from '../components/ui/button'
import { Panel } from '../components/ui/panel'
import { Stat } from '../components/ui/stat'
import { FkGrid, exprFilter, numCol } from '../components/grid/FkGrid'
import SelectCompany from '../components/layout/SelectCompany'
import VoucherDialog from './cashbook/VoucherDialog'
import CountDialog from './cashbook/CountDialog'
import { dash } from './statement/docType'

type Row = CashBookLine & { label?: string }

function weekAgo(iso: string): string {
  const d = new Date(`${iso}T00:00:00`)
  d.setDate(d.getDate() - 7)
  return todayIso(d)
}

export default function CashBookPage() {
  const orgId = useOrgId()
  return orgId ? <CashBook orgId={orgId} /> : <SelectCompany />
}

function CashBook({ orgId }: Readonly<{ orgId: number }>) {
  const { t, i18n } = useTranslation(['treasury', 'grid'])
  const [params, setParams] = useSearchParams()
  const { depotId: topDepot } = useDepot()
  const depots = useDepots()
  const depotId = topDepot ?? depots[0]?.id ?? 1
  const depotCode = depots.find((d) => d.id === depotId)?.code ?? String(depotId)
  const to = params.get('to') ?? todayIso()
  const from = params.get('from') ?? weekAgo(to)
  const [dialog, setDialog] = useState<'expense' | 'bank' | 'count' | null>(null)
  const download = useDownload()

  const book = useApi(() => getCashBook(orgId, { depotId, from, to }), [orgId, depotId, from, to])
  const data = book.data

  const columnDefs = useMemo<ColDef<Row>[]>(() => {
    const money = (field: 'income' | 'expense', key: string): ColDef<Row> => ({
      field,
      headerName: t(`cash.col.${key}`),
      flex: 0,
      width: 120,
      ...numCol,
      ...exprFilter({ kind: 'number', placeholder: t('grid:fk.positive') }),
      valueGetter: (p) => (p.data?.[field] === undefined ? null : Number(p.data[field])),
      valueFormatter: (p) => (p.value === null ? '' : dash(p.value)),
      cellClassRules: { 'fk-muted': (p) => p.value === 0 },
    })
    return [
      {
        field: 'date',
        headerName: t('cash.col.date'),
        sort: 'asc',
        flex: 0,
        width: 118,
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'date', placeholder: t('grid:fk.range_hint') }),
        valueFormatter: (p) => formatDay(p.value),
      },
      {
        field: 'documentNo',
        headerName: t('cash.col.document'),
        minWidth: 170,
        cellClass: 'font-mono',
        colSpan: (p) => (p.node?.rowPinned ? 3 : 1),
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
        cellRenderer: (p: ICellRendererParams<Row>) =>
          p.node.rowPinned ? (
            <span className="font-sans font-semibold">{p.data?.label}</span>
          ) : (
            p.value
          ),
      },
      {
        field: 'sicilName',
        headerName: t('cash.col.account'),
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
        valueFormatter: (p) => p.value ?? '—',
        cellClassRules: { 'fk-muted': (p) => !p.value && !p.node.rowPinned },
      },
      {
        field: 'description',
        headerName: t('cash.col.description'),
        flex: 1.4,
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
      },
      money('income', 'in'),
      money('expense', 'out'),
      {
        field: 'runningBalance',
        headerName: t('cash.col.balance'),
        flex: 0,
        width: 124,
        sortable: false,
        ...numCol,
        valueFormatter: (p) => formatMoney(p.value),
      },
    ]
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [t, i18n.language])

  const pinnedTop = useMemo<Row[]>(
    () =>
      data
        ? [
            {
              date: data.from,
              kind: 'OTHER',
              label: t('cash.opening'),
              description: '',
              journalEntryId: 0,
              runningBalance: data.openingBalance,
            } as Row,
          ]
        : [],
    [data, t],
  )
  const pinnedBottom = useMemo<Row[]>(
    () =>
      data
        ? [
            {
              label: t('cash.total'),
              income: data.totalIn,
              expense: data.totalOut,
              runningBalance: data.closingBalance,
            } as Row,
          ]
        : [],
    [data, t],
  )

  const setPeriod = (f: string, tt: string) =>
    setParams((p) => {
      const next = new URLSearchParams(p)
      next.set('from', f)
      next.set('to', tt)
      return next
    })

  const sameYear = from.slice(0, 4) === to.slice(0, 4)
  const lines = data?.lines.length ?? 0
  const height = 36 + 34 + 2 + 36 * 2 + Math.max(1, Math.min(lines, 10)) * 34
  const posted = () => {
    setDialog(null)
    book.reload()
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-3">
        <h1 className="text-2xl font-semibold">{t('cash.title')}</h1>
        <span className="font-mono text-13 text-fk-muted">
          {t('cash.period', {
            depot: t('cash.depotN', { id: depotCode }),
            from: sameYear ? formatDay(from).slice(0, 5) : formatDay(from),
            to: formatDay(to),
          })}
        </span>
        <div className="flex-1" />
        <PeriodButton from={from} to={to} onApply={setPeriod} />
        <Button
          variant="outline"
          disabled={download.busy !== null}
          onClick={() =>
            download.run('pdf', `cash-book-${depotCode}-${from}-${to}.pdf`, () =>
              exportCashBook(orgId, { depotId, from, to }),
            )
          }
        >
          {t('grid:fk.pdf')}
        </Button>
        <Button variant="outline" onClick={() => setDialog('expense')}>
          {t('cash.expense')}
        </Button>
        <Button variant="outline" onClick={() => setDialog('bank')}>
          {t('cash.deposit')}
        </Button>
        <Button onClick={() => setDialog('count')}>{t('cash.count')}</Button>
      </div>
      <div className="grid grid-cols-4 gap-4">
        <Stat label={t('cash.opening')} value={data ? formatMoney(data.openingBalance) : '—'} />
        <Stat
          label={t('cash.in')}
          tone="in"
          value={data ? formatMoney(data.totalIn, { sign: true }) : '—'}
        />
        <Stat
          label={t('cash.out')}
          tone="out"
          value={data ? formatMoney(-Number(data.totalOut)) : '—'}
        />
        <Stat label={t('cash.closing')} value={data ? formatMoney(data.closingBalance) : '—'} />
      </div>
      <Panel className="overflow-hidden">
        <FkGrid
          height={height}
          columnDefs={columnDefs}
          rowData={data?.lines ?? []}
          pinnedTopRowData={pinnedTop}
          pinnedBottomRowData={pinnedBottom}
          getRowClass={(p) => (p.node.rowPinned === 'top' ? 'fk-row-bf' : undefined)}
        />
      </Panel>
      {(dialog === 'expense' || dialog === 'bank') && (
        <VoucherDialog
          orgId={orgId}
          depotId={depotId}
          depotCode={depotCode}
          mode={dialog}
          onClose={() => setDialog(null)}
          onPosted={posted}
        />
      )}
      {dialog === 'count' && (
        <CountDialog
          orgId={orgId}
          depotId={depotId}
          onClose={() => setDialog(null)}
          onSaved={posted}
        />
      )}
    </div>
  )
}
