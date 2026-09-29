import { useMemo, useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import type { ColDef, ICellRendererParams } from 'ag-grid-community'
import { PDF, XLSX } from '../api/client'
import {
  exportAccountStatement,
  getAccount,
  getAccountStatement,
  getSicil,
  getSicilSummary,
} from '../api/endpoints'
import type { Sicil, SicilSummary, StatementLine } from '../api/types'
import { useApi } from '../hooks/useApi'
import { useDepot, withDepot } from '../hooks/useDepot'
import { useDownload } from '../hooks/useDownload'
import { useOrgId } from '../hooks/useOrgId'
import { formatBalance, formatDay, formatMoney, todayIso } from '../lib/format'
import { AccountPicker } from '../components/AccountPicker'
import { Button } from '../components/ui/button'
import { Chip, type ChipTone } from '../components/ui/chip'
import { KeyValue, Panel } from '../components/ui/panel'
import { FkGrid } from '../components/grid/FkGrid'
import { exprFilter, numCol } from '../components/grid/columns'
import SelectCompany from '../components/layout/SelectCompany'
import EditCardDialog from './statement/EditCardDialog'
import PeriodButton from '../components/PeriodButton'
import { docTone, dash } from './statement/docType'

const SUBTYPES = [
  'SALES_INVOICE',
  'PURCHASE_INVOICE',
  'SALES_RETURN',
  'PURCHASE_RETURN',
  'PRICE_DIFFERENCE',
  'COLLECTION',
  'PAYMENT',
  'RECEIVABLE_CHEQUE',
  'RECEIVABLE_NOTE',
  'PAYABLE_CHEQUE',
  'PAYABLE_NOTE',
]
const TIPI_TONE: Record<string, ChipTone> = { MUSTERI: 'blue', FIRMA: 'grey', BANKA: 'amber' }

export default function StatementPage() {
  const orgId = useOrgId()
  const { accountId } = useParams()
  if (!orgId) return <SelectCompany />
  if (!accountId) return <StatementPicker orgId={orgId} />
  return <Statement orgId={orgId} accountId={Number(accountId)} />
}

function StatementPicker({ orgId }: Readonly<{ orgId: number }>) {
  const { t } = useTranslation('ledger')
  const navigate = useNavigate()
  const { depotId } = useDepot()
  return (
    <div className="flex max-w-xl flex-col gap-3">
      <h1 className="text-2xl font-semibold">{t('statement.title')}</h1>
      <p className="text-sm text-fk-muted">{t('statement.pick')}</p>
      <AccountPicker
        orgId={orgId}
        label={t('statement.account')}
        autoFocus
        onPick={async (h) => {
          const s = await getSicilSummary(orgId, h.sicilId)
          navigate(withDepot(`/statements/${s.accountId}`, depotId))
        }}
      />
    </div>
  )
}

function Statement({ orgId, accountId }: Readonly<{ orgId: number; accountId: number }>) {
  const { t, i18n } = useTranslation(['ledger', 'balances', 'grid'])
  const [params, setParams] = useSearchParams()
  const { depotId } = useDepot()
  const today = todayIso()
  const from = params.get('from') ?? `${today.slice(0, 4)}-01-01`
  const to = params.get('to') ?? today
  const query = { from, to, depotId }
  const download = useDownload()

  const account = useApi(() => getAccount(orgId, accountId), [orgId, accountId]).data
  const sicilId = account?.sicilId ?? null
  const sicil = useApi(sicilId ? () => getSicil(orgId, sicilId) : null, [orgId, sicilId])
  const summary = useApi(sicilId ? () => getSicilSummary(orgId, sicilId, depotId) : null, [
    orgId,
    sicilId,
    depotId,
  ]).data
  const statement = useApi(
    () => getAccountStatement(orgId, accountId, query),
    [orgId, accountId, from, to, depotId],
  ).data

  const lang = i18n.language

  const columnDefs = useMemo<ColDef<StatementLine>[]>(() => {
    const money = (field: 'debit' | 'credit'): ColDef<StatementLine> => ({
      field,
      headerName: t(`statement.col.${field}`),
      flex: 0,
      width: 100,
      ...numCol,
      ...exprFilter({ kind: 'number', placeholder: t('grid:fk.positive') }),
      valueGetter: (p) => Number(p.data?.[field] ?? 0),
      valueFormatter: (p) => dash(p.value),
      cellClassRules: { 'fk-muted': (p) => !p.value },
    })
    return [
      {
        field: 'entryDate',
        headerName: t('statement.col.date'),
        sort: 'asc',
        flex: 0,
        width: 98,
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'date', placeholder: t('grid:fk.range_hint') }),
        valueFormatter: (p) => formatDay(p.value),
      },
      {
        field: 'documentNo',
        headerName: t('statement.col.document'),
        minWidth: 150,
        cellClass: 'font-mono',
        colSpan: (p) => (p.node?.rowPinned ? 3 : 1),
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
        cellRenderer: (p: ICellRendererParams<StatementLine>) =>
          p.node.rowPinned ? (
            <span className="font-sans">{t('statement.broughtForward')}</span>
          ) : p.value ? (
            <Link to={withDepot(`/journal?q=${encodeURIComponent(p.value)}`, depotId)}>
              {p.value}
            </Link>
          ) : (
            <span className="font-sans">{p.data?.description}</span>
          ),
      },
      {
        field: 'subtype',
        headerName: t('statement.col.type'),
        flex: 0,
        width: 124,
        ...exprFilter({
          kind: 'enum',
          anyLabel: t('grid:fk.any'),
          options: SUBTYPES.map((s) => ({ value: s, label: t(`docType.${s}`) })),
        }),
        cellRenderer: (p: ICellRendererParams<StatementLine>) =>
          p.value && !p.node.rowPinned ? (
            <Chip tone={docTone(p.value)}>
              {t(`docType.${p.value}`, { defaultValue: p.value })}
            </Chip>
          ) : null,
      },
      {
        field: 'dueDate',
        headerName: t('statement.col.due'),
        flex: 0,
        width: 98,
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'date', placeholder: t('grid:fk.before') }),
        valueFormatter: (p) => formatDay(p.value),
      },
      {
        colId: 'depotId',
        headerName: t('statement.col.depot'),
        flex: 0,
        width: 46,
        cellClass: 'font-mono',
        valueGetter: (p) => (p.data?.depotId ? String(p.data.depotId) : ''),
        ...exprFilter({
          kind: 'enum',
          anyLabel: t('grid:fk.all'),
          options: ['1', '2'].map((d) => ({ value: d, label: d })),
        }),
      },
      money('debit'),
      money('credit'),
      {
        field: 'runningBalance',
        headerName: t('statement.col.balance'),
        flex: 0,
        width: 100,
        sortable: false,
        ...numCol,
        valueFormatter: (p) => formatMoney(p.value),
      },
    ]
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [t, lang, depotId])

  const setPeriod = (f: string, tt: string) =>
    setParams((p) => {
      const next = new URLSearchParams(p)
      next.set('from', f)
      next.set('to', tt)
      return next
    })

  const bf = statement?.broughtForward.length ?? 0
  const lines = statement?.lines.length ?? 0
  const height = 36 + 34 + 2 + bf * 36 + Math.max(1, Math.min(lines, 12)) * 34
  const code = account?.code ?? ''
  const file = (ext: string) => `statement-${code}-${from}-${to}.${ext}`

  return (
    <div className="flex flex-col gap-3.5">
      <div className="flex items-end gap-4">
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2.5">
            <h1 className="truncate text-2xl font-semibold">{account?.name ?? ''}</h1>
            {code && <Chip tone="grey">{code}</Chip>}
            {sicil.data?.sicil_tipi && (
              <Chip tone={TIPI_TONE[sicil.data.sicil_tipi]}>
                {t(`balances:chip.${sicil.data.sicil_tipi}`)}
              </Chip>
            )}
          </div>
          <div className="mt-1.5 font-mono text-13 text-fk-muted">
            {t('statement.period', { from: formatDay(from), to: formatDay(to) })} ·{' '}
            {depotId ? t('statement.depot', { id: depotId }) : t('statement.allDepots')}
          </div>
        </div>
        <PeriodButton from={from} to={to} onApply={setPeriod} />
        <Button
          variant="outline"
          disabled={download.busy !== null}
          onClick={() =>
            download.run('xlsx', file('xlsx'), () =>
              exportAccountStatement(orgId, accountId, query, XLSX),
            )
          }
        >
          {t('grid:fk.excel')}
        </Button>
        <Button
          variant="outline"
          disabled={download.busy !== null}
          onClick={() =>
            download.run('pdf', file('pdf'), () =>
              exportAccountStatement(orgId, accountId, query, PDF),
            )
          }
        >
          {t('grid:fk.pdf')}
        </Button>
      </div>
      <div className="flex gap-4">
        <Panel className="flex-1 overflow-hidden">
          <FkGrid
            className="fk-dense"
            height={height}
            columnDefs={columnDefs}
            rowData={statement?.lines ?? []}
            pinnedTopRowData={statement?.broughtForward ?? []}
            getRowClass={(p) => (p.node.rowPinned ? 'fk-row-bf' : undefined)}
          />
          <DepotTotals totals={statement?.depotTotals ?? []} />
        </Panel>
        <aside className="flex w-[300px] shrink-0 flex-col gap-3.5">
          <CreditPanel summary={summary} />
          {sicil.data && (
            <CardPanel orgId={orgId} sicil={sicil.data} onSaved={(s) => sicil.setData(s)} />
          )}
        </aside>
      </div>
    </div>
  )
}

function DepotTotals({
  totals,
}: Readonly<{ totals: { depotId: number; debit: string; credit: string; balance: string }[] }>) {
  const { t } = useTranslation('ledger')
  const sum = (k: 'debit' | 'credit' | 'balance') => totals.reduce((s, r) => s + Number(r[k]), 0)
  const cell = 'h-[34px] border-b border-fk-divider px-2.5 text-grid'
  return (
    <table aria-label={t('statement.depotTotals')} className="w-full border-t border-fk-border">
      <tbody>
        {totals.map((r) => (
          <tr key={r.depotId}>
            <td className={`${cell} w-1/2 text-fk-muted`}>{t('depotN', { id: r.depotId })}</td>
            <td className={`${cell} fk-num`}>{formatMoney(r.debit)}</td>
            <td className={`${cell} fk-num`}>{formatMoney(r.credit)}</td>
            <td className={`${cell} fk-num w-40`}>{formatBalance(r.balance)}</td>
          </tr>
        ))}
        <tr className="bg-fk-sunken font-semibold">
          <td className={cell}>{t('total')}</td>
          <td className={`${cell} fk-num`}>{formatMoney(sum('debit'))}</td>
          <td className={`${cell} fk-num`}>{formatMoney(sum('credit'))}</td>
          <td className={`${cell} fk-num`}>{formatBalance(sum('balance'))}</td>
        </tr>
      </tbody>
    </table>
  )
}

function CreditPanel({ summary }: Readonly<{ summary: SicilSummary | undefined }>) {
  const { t } = useTranslation('ledger')
  const pct = summary?.creditUsedPct ?? null
  return (
    <Panel aria-labelledby="credit-h" className="flex flex-col gap-2.5 p-4">
      <h2 id="credit-h" className="text-15 font-semibold">
        {t('statement.credit')}
      </h2>
      <KeyValue label={t('statement.limit')}>
        {summary?.creditLimit ? formatMoney(summary.creditLimit) : '—'}
      </KeyValue>
      <div
        role="progressbar"
        aria-label={t('statement.used')}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={pct ?? undefined}
        className="h-1.5 overflow-hidden rounded-sm bg-fk-divider"
      >
        <div
          className={pct !== null && pct > 100 ? 'h-1.5 bg-fk-negative' : 'h-1.5 bg-fk-accent'}
          style={{ width: `${Math.min(pct ?? 0, 100)}%` }}
        />
      </div>
      <KeyValue label={t('statement.term')}>
        {summary?.paymentTermDays != null
          ? t('statement.days', { count: summary.paymentTermDays })
          : '—'}
      </KeyValue>
      <KeyValue label={t('statement.discount')}>
        {summary?.discount1Pct
          ? t('statement.discountValue', { a: summary.discount1Pct, b: summary.discount2Pct ?? 0 })
          : '—'}
      </KeyValue>
      <KeyValue label={t('statement.cheques')}>
        {summary ? formatMoney(summary.chequesInPortfolio) : '—'}
      </KeyValue>
    </Panel>
  )
}

function CardPanel({
  orgId,
  sicil,
  onSaved,
}: Readonly<{ orgId: number; sicil: Sicil; onSaved: (s: Sicil) => void }>) {
  const { t } = useTranslation('ledger')
  const [editing, setEditing] = useState(false)
  const contact = sicil.contacts?.[0]
  const place = [sicil.ilce, sicil.sehir].filter(Boolean).join(' / ')
  return (
    <Panel aria-labelledby="card-h" className="flex flex-col gap-2 p-4 text-sm">
      <h2 id="card-h" className="mb-0.5 text-15 font-semibold">
        {t('statement.card')}
      </h2>
      {contact && <div>{[contact.name, contact.role].filter(Boolean).join(' · ')}</div>}
      {sicil.telefonlar?.map((p) => (
        <div key={`${p.tipi}${p.numara}`} className="font-mono text-13">
          {p.numara}
        </div>
      ))}
      {place && <div className="text-fk-muted">{place}</div>}
      {(sicil.v_dairesi || sicil.v_no) && (
        <div className="font-mono text-13 text-fk-muted">
          {t('statement.taxOffice', { office: sicil.v_dairesi ?? '', no: sicil.v_no ?? '' })}
        </div>
      )}
      <Button variant="outline" className="mt-1 self-start" onClick={() => setEditing(true)}>
        {t('statement.editCard')}
      </Button>
      {editing && (
        <EditCardDialog
          orgId={orgId}
          sicil={sicil}
          onClose={() => setEditing(false)}
          onSaved={(s) => {
            onSaved(s)
            setEditing(false)
          }}
        />
      )}
    </Panel>
  )
}
