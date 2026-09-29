import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { getDashboard, gridDocuments } from '../api/endpoints'
import { errorText } from '../api/errorText'
import type { Dashboard } from '../api/types'
import { useApi } from '../hooks/useApi'
import { useDepot, withDepot } from '../hooks/useDepot'
import { useDepots } from '../hooks/useDepots'
import { useOrgId } from '../hooks/useOrgId'
import { formatDay, formatMoney, formatQty, formatTime } from '../lib/format'
import { cn } from '@/lib/utils'
import { Chip, type ChipTone } from '../components/ui/chip'
import { Panel } from '../components/ui/panel'
import SelectCompany from '../components/layout/SelectCompany'
import { docTone } from './statement/docType'

const CHECK_TONE: Record<Dashboard['ledgerCheck']['status'], ChipTone> = {
  BALANCED: 'green',
  DRIFT: 'red',
  NOT_RUN: 'grey',
}

const dayTone = (days: number): ChipTone => (days < 0 ? 'red' : days <= 2 ? 'amber' : 'blue')

const TH =
  'h-9 whitespace-nowrap border-b border-r border-fk-border border-r-fk-divider bg-fk-sunken px-2.5 text-left text-xs font-medium text-fk-secondary'
const TD =
  'h-[34px] whitespace-nowrap border-b border-r border-fk-divider border-r-fk-divider-soft px-2.5 text-grid'

const btn =
  'inline-flex h-9 shrink-0 items-center whitespace-nowrap rounded-md border px-3.5 text-sm font-medium no-underline'

export default function DashboardPage() {
  const orgId = useOrgId()
  return orgId ? <Overview orgId={orgId} /> : <SelectCompany />
}

function Kpi({
  to,
  label,
  value,
  sub,
}: Readonly<{ to: string; label: string; value: string; sub: ReactNode }>) {
  return (
    <Link
      to={to}
      className="min-w-0 rounded-lg border border-fk-border bg-fk-surface px-[18px] py-4 text-fk-text no-underline hover:border-fk-input hover:text-fk-text"
    >
      <div role="group" aria-label={label}>
        <div className="text-13 text-fk-muted">{label}</div>
        <div className="mt-2 whitespace-nowrap font-mono text-2xl font-medium tabular-nums">
          {value}
        </div>
        <div className="mt-1.5 truncate text-13 text-fk-muted">{sub}</div>
      </div>
    </Link>
  )
}

function CardHead({
  id,
  title,
  extra,
  link,
}: Readonly<{ id: string; title: string; extra?: string; link?: ReactNode }>) {
  return (
    <div className="flex items-center gap-2">
      <h2 id={id} className="whitespace-nowrap text-base font-semibold">
        {title}
      </h2>
      {extra && <span className="truncate text-13 text-fk-muted">{extra}</span>}
      <div className="flex-1" />
      {link}
    </div>
  )
}

function Overview({ orgId }: Readonly<{ orgId: number }>) {
  const { t, i18n } = useTranslation(['dashboard', 'documents', 'shell', 'errors'])
  const { depotId } = useDepot()
  const depots = useDepots()
  const { data, error } = useApi(() => getDashboard(orgId, depotId), [orgId, depotId])
  const latest = useApi(
    () =>
      gridDocuments(orgId, {
        startRow: 0,
        endRow: 10,
        statuses: ['POSTED', 'CANCELLED'],
        depotId,
        sortModel: [{ colId: 'postedAt', sort: 'desc' }],
      }),
    [orgId, depotId],
  ).data?.rows
  const code = (id: number) => depots.find((d) => d.id === id)?.code ?? String(id)
  const money = (v: string | undefined) => (v === undefined ? '—' : formatMoney(v))
  const to = (path: string) => withDepot(path, depotId)

  const cashSub = data
    ? data.cash.depots.map((d) => t('shell:depotN', { code: code(d.depotId) })).join(' · ') || '—'
    : '—'
  const overdue = data?.instrumentsDue30.overdueCount
  const check = data?.ledgerCheck

  return (
    <div className="flex flex-col gap-5">
      <div className="flex items-end justify-between gap-3">
        <div>
          <h1 className="text-[26px] font-semibold leading-8">{t('title')}</h1>
          <div className="mt-1 font-mono text-13 text-fk-muted">
            {data ? t('dateLine', { date: formatDay(data.date) }) : '—'}
          </div>
        </div>
        <div className="flex gap-2">
          <Link
            to={to('/receipts')}
            className={cn(btn, 'border-fk-input bg-fk-surface text-fk-text hover:text-fk-text')}
          >
            {t('newReceipt')}
          </Link>
          <Link
            to={to('/invoices/new?type=SALES_INVOICE_WITH_WAYBILL')}
            className={cn(
              btn,
              'border-fk-accent bg-fk-accent text-white hover:bg-fk-accent-hover hover:text-white',
            )}
          >
            {t('newInvoice')}
          </Link>
        </div>
      </div>
      {error && (
        <p role="alert" className="text-sm text-fk-negative">
          {errorText(t, error)}
        </p>
      )}
      <div className="grid grid-cols-4 gap-4">
        <Kpi
          to={to('/balances')}
          label={t('receivables')}
          value={money(data?.receivables.total)}
          sub={data ? t('receivablesSub', { count: data.receivables.accounts }) : '—'}
        />
        <Kpi
          to={to('/balances?type=FIRMA')}
          label={t('payables')}
          value={money(data?.payables.total)}
          sub={data ? t('payablesSub', { count: data.payables.accounts }) : '—'}
        />
        <Kpi
          to={to('/cash-book')}
          label={t('cash')}
          value={money(data?.cash.total)}
          sub={cashSub}
        />
        <Kpi
          to={to('/cheques')}
          label={t('cheques')}
          value={money(data?.instrumentsDue30.total)}
          sub={
            overdue === undefined ? (
              '—'
            ) : overdue > 0 ? (
              <span className="text-fk-negative">{t('overdue', { count: overdue })}</span>
            ) : (
              t('noOverdue')
            )
          }
        />
      </div>
      <div className="flex items-start gap-4">
        <Panel aria-labelledby="dsh-latest" className="flex-1 overflow-hidden">
          <div className="px-3.5 py-3">
            <CardHead
              id="dsh-latest"
              title={t('latest')}
              link={
                <Link to={to('/invoices')} className="text-sm">
                  {t('all')}
                </Link>
              }
            />
          </div>
          <table aria-labelledby="dsh-latest" className="w-full border-separate border-spacing-0">
            <thead>
              <tr>
                <th className={TH}>{t('col.number')}</th>
                <th className={TH}>{t('col.type')}</th>
                <th className={cn(TH, 'w-full')}>{t('col.account')}</th>
                <th className={TH}>{t('col.depot')}</th>
                <th className={cn(TH, 'text-right')}>{t('col.amount')}</th>
              </tr>
            </thead>
            <tbody>
              {latest?.map((d) => (
                <tr key={d.id} className="hover:bg-fk-hover">
                  <td className={cn(TD, 'font-mono')}>
                    <Link to={to(`/invoices/${d.id}`)}>{d.documentNo}</Link>
                  </td>
                  <td className={TD}>
                    <span className="flex items-center gap-1.5">
                      <Chip tone={docTone(d.type)}>
                        {t(
                          `documents:typeShort.${d.type}${d.type === 'PRICE_DIFFERENCE' && d.priceDirection ? `_${d.priceDirection}` : ''}`,
                        )}
                      </Chip>
                      {d.status === 'CANCELLED' && (
                        <Chip tone="red">{t('documents:status.CANCELLED')}</Chip>
                      )}
                    </span>
                  </td>
                  <td className={cn(TD, 'max-w-0 truncate')}>{d.sicilName}</td>
                  <td className={cn(TD, 'font-mono')}>{d.depotCode}</td>
                  <td
                    className={cn(
                      TD,
                      'fk-num',
                      d.status === 'CANCELLED' && 'text-fk-faint line-through',
                    )}
                  >
                    {formatMoney(d.grandTotal)}
                  </td>
                </tr>
              ))}
              {latest && !latest.length && (
                <tr>
                  <td colSpan={5} className={cn(TD, 'text-fk-muted')}>
                    {t('noLatest')}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </Panel>
        <aside className="flex w-[380px] shrink-0 flex-col gap-4">
          <Panel aria-labelledby="dsh-due" className="flex flex-col gap-2.5 px-4 py-3.5">
            <CardHead
              id="dsh-due"
              title={t('due')}
              link={
                <Link to={to('/cheques')} className="text-sm">
                  {t('all')}
                </Link>
              }
            />
            {data?.dueThisWeek.map((d) => (
              <div key={d.instrumentId} className="flex items-center gap-2.5 text-sm">
                <Chip tone={dayTone(d.daysToDue)} className="min-w-[52px] justify-center">
                  {t('days', { days: formatQty(d.daysToDue) })}
                </Chip>
                <span className="min-w-0 flex-1 truncate">
                  {d.instrumentNo} · {d.sicilName}
                </span>
                <span className="fk-num">{formatMoney(d.remaining)}</span>
              </div>
            ))}
            {data && !data.dueThisWeek.length && (
              <p className="text-sm text-fk-muted">{t('noDue')}</p>
            )}
          </Panel>
          <Panel aria-labelledby="dsh-below" className="flex flex-col gap-2.5 px-4 py-3.5">
            <CardHead
              id="dsh-below"
              title={t('belowMin')}
              link={
                <Link to={to('/parts?stock=BELOW_MIN_OR_NEGATIVE')} className="text-sm">
                  {t('all')}
                </Link>
              }
            />
            <div className="-mt-1.5 text-13 text-fk-muted">
              {data
                ? t('belowMinCounts', {
                    below: data.belowMinCount.toLocaleString(i18n.language),
                    negative: data.negativeCount.toLocaleString(i18n.language),
                  })
                : '—'}
            </div>
            <ul className="flex flex-col gap-2.5">
              {data?.belowMin.map((l) => (
                <li key={`${l.partId}:${l.depotId}`} className="flex items-center gap-2.5 text-sm">
                  <Link
                    to={to(`/parts/${l.partId}`)}
                    className="w-[76px] shrink-0 whitespace-nowrap font-mono"
                  >
                    {l.stockNo}
                  </Link>
                  <span className="min-w-0 flex-1 truncate">{l.name}</span>
                  {!depotId && l.depotId !== null && l.depotId !== undefined && (
                    <span className="shrink-0 font-mono text-13 text-fk-muted">
                      {t('depotShort', { code: code(l.depotId) })}
                    </span>
                  )}
                  <span className={cn('fk-num shrink-0', l.negative && 'text-fk-negative')}>
                    {formatQty(l.onHand)} / {formatQty(l.minQty ?? '0')}
                  </span>
                </li>
              ))}
            </ul>
            {data && !data.belowMin.length && (
              <p className="text-sm text-fk-muted">{t('noBelowMin')}</p>
            )}
          </Panel>
          {check && (
            <div className="flex items-center gap-2.5 px-1 text-13 text-fk-muted">
              <Chip tone={CHECK_TONE[check.status]}>{t(`check.${check.status}`)}</Chip>
              <span className="whitespace-nowrap">
                {t('ledgerCheck', {
                  time: check.checkedAt ? formatTime(check.checkedAt).slice(0, 5) : '—',
                })}
              </span>
            </div>
          )}
        </aside>
      </div>
    </div>
  )
}
