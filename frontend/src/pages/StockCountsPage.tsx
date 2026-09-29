import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { listStockCounts } from '../api/endpoints'
import { useApi } from '../hooks/useApi'
import { useDepot, withDepot } from '../hooks/useDepot'
import { useDepots } from '../hooks/useDepots'
import { useOrgId } from '../hooks/useOrgId'
import { formatDay, formatMoney } from '../lib/format'
import { Button } from '../components/ui/button'
import { Chip } from '../components/ui/chip'
import { Panel } from '../components/ui/panel'
import SelectCompany from '../components/layout/SelectCompany'
import StartCountDialog from './count/StartCountDialog'
import { STATUS_TONE } from './count/status'

const th = 'h-9 whitespace-nowrap border-b border-fk-border bg-fk-sunken px-2.5 text-xs font-medium'
const td = 'h-[34px] whitespace-nowrap border-b border-fk-divider px-2.5'

export default function StockCountsPage() {
  const orgId = useOrgId()
  return orgId ? <StockCounts orgId={orgId} /> : <SelectCompany />
}

function StockCounts({ orgId }: Readonly<{ orgId: number }>) {
  const { t, i18n } = useTranslation('counts')
  const { depotId } = useDepot()
  const depots = useDepots()
  const [starting, setStarting] = useState(false)
  const counts = useApi(() => listStockCounts(orgId, { depotId }), [orgId, depotId])
  const code = (id: number) => depots.find((d) => d.id === id)?.code ?? '—'
  const nf = (n: number) => n.toLocaleString(i18n.language === 'tr' ? 'tr-TR' : 'en-US')
  const rows = counts.data ?? []

  return (
    <div className="flex min-h-0 flex-1 flex-col gap-3">
      <div className="flex items-center gap-3">
        <h1 className="flex-1 text-2xl font-semibold">{t('title')}</h1>
        <Button onClick={() => setStarting(true)}>{t('start.button')}</Button>
      </div>
      <Panel className="overflow-hidden">
        <table className="w-full border-separate border-spacing-0 text-grid">
          <thead className="text-left text-fk-secondary">
            <tr>
              <th className={th}>{t('list.countNo')}</th>
              <th className={th}>{t('list.depot')}</th>
              <th className={th}>{t('list.countDate')}</th>
              <th className={th}>{t('list.status')}</th>
              <th className={`${th} w-[260px]`}>{t('list.progress')}</th>
              <th className={`${th} text-right`}>{t('list.differences')}</th>
              <th className={`${th} text-right`}>{t('list.value')}</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((c) => {
              const pct = c.lineCount ? Math.round((c.countedCount / c.lineCount) * 100) : 0
              return (
                <tr key={c.id} className="hover:bg-fk-hover">
                  <td className={`${td} font-mono`}>
                    <Link
                      className="text-fk-accent"
                      to={withDepot(`/stock-counts/${c.id}`, depotId)}
                    >
                      {c.countNo}
                    </Link>
                  </td>
                  <td className={`${td} font-mono`}>{code(c.depotId)}</td>
                  <td className={`${td} font-mono`}>{formatDay(c.countDate)}</td>
                  <td className={td}>
                    <Chip size="sm" tone={STATUS_TONE[c.status]}>
                      {t(`status.${c.status}`)}
                    </Chip>
                  </td>
                  <td className={td}>
                    <div className="flex items-center gap-3">
                      <div className="h-1.5 flex-1 overflow-hidden rounded bg-fk-chip-grey">
                        <div className="h-1.5 bg-fk-accent" style={{ width: `${pct}%` }} />
                      </div>
                      <span className="min-w-[96px] text-right font-mono tabular-nums">
                        {nf(c.countedCount)} / {nf(c.lineCount)}
                      </span>
                    </div>
                  </td>
                  <td className={`${td} text-right font-mono`}>{nf(c.differenceCount)}</td>
                  <td
                    className={`${td} text-right font-mono ${Number(c.differenceValue) < 0 ? 'text-fk-negative' : ''}`}
                  >
                    {formatMoney(c.differenceValue ?? 0, { sign: true })}
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
        {counts.data && !rows.length && (
          <p className="px-3 py-4 text-13 text-fk-muted">{t('none')}</p>
        )}
      </Panel>
      {starting && (
        <StartCountDialog
          orgId={orgId}
          depots={depots}
          depotId={depotId}
          counts={rows}
          onClose={() => setStarting(false)}
        />
      )}
    </div>
  )
}
