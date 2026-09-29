import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { getJournalEntry, getJournalEntryTrail } from '../api/endpoints'
import type { JournalEntry, TrailItem } from '../api/types'
import { useToast } from '../context/ToastContext'
import { useApi } from '../hooks/useApi'
import { useDepot, withDepot } from '../hooks/useDepot'
import { useOrgId } from '../hooks/useOrgId'
import { formatDateTime, formatMoney, formatTime } from '../lib/format'
import { Button } from '../components/ui/button'
import { Chip } from '../components/ui/chip'
import { Panel } from '../components/ui/panel'
import SelectCompany from '../components/layout/SelectCompany'
import ReverseDialog from './journal/ReverseDialog'

const CURRENT = /^(120|320|102)\./

export default function JournalEntryPage() {
  const orgId = useOrgId()
  const { entryId } = useParams()
  if (!orgId) return <SelectCompany />
  return <Entry key={entryId} orgId={orgId} entryId={Number(entryId)} />
}

function Entry({ orgId, entryId }: Readonly<{ orgId: number; entryId: number }>) {
  const { t } = useTranslation('ledger')
  const toast = useToast()
  const navigate = useNavigate()
  const { depotId } = useDepot()
  const entry = useApi(() => getJournalEntry(orgId, entryId), [orgId, entryId]).data
  const trail = useApi(() => getJournalEntryTrail(orgId, entryId), [orgId, entryId]).data
  const [reversing, setReversing] = useState(false)
  if (!entry) return null

  const debit = entry.postings
    .filter((p) => p.side === 'DEBIT')
    .reduce((s, p) => s + Number(p.amount), 0)
  const credit = entry.postings
    .filter((p) => p.side === 'CREDIT')
    .reduce((s, p) => s + Number(p.amount), 0)
  const diff = debit - credit
  const link = (id: number, no: string) => (
    <Link to={withDepot(`/journal/${id}`, depotId)} className="font-mono">
      {no}
    </Link>
  )

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-3">
        <h1 className="font-mono text-22 font-medium">{entry.entryNo}</h1>
        {entry.reversedByEntryId ? (
          <Chip tone="amber">{t('journal.status.REVERSED')}</Chip>
        ) : (
          <Chip tone="green">{t('journal.status.POSTED')}</Chip>
        )}
        <Chip tone={Math.abs(diff) < 0.005 ? 'green' : 'red'}>
          {t('journal.sum', { value: formatMoney(diff) })}
        </Chip>
        <div className="flex-1" />
        <Button
          variant="danger"
          disabled={Boolean(entry.reversedByEntryId)}
          onClick={() => setReversing(true)}
        >
          {t('journal.reverse')}
        </Button>
      </div>
      <div className="flex gap-4">
        <div className="flex min-w-0 flex-1 flex-col gap-4">
          <Panel className="p-4">
            <dl className="m-0 grid grid-cols-[140px_minmax(0,1fr)] gap-y-[9px] text-grid">
              <Row label={t('journal.posted')}>
                <span className="font-mono">{formatDateTime(entry.createdAt)}</span>
              </Row>
              <Row label={t('journal.source')}>
                {entry.sourceNo && !entry.reversesEntryId ? (
                  <Link
                    to={withDepot(`/journal?q=${encodeURIComponent(entry.sourceNo)}`, depotId)}
                    className="font-mono"
                  >
                    {entry.sourceNo}
                  </Link>
                ) : null}
                {entry.sourceNo && !entry.reversesEntryId ? ' · ' : ''}
                {t(`journal.sourceType.${entry.sourceType}`)}
              </Row>
              <Row label={t('journal.description')}>{entry.description}</Row>
              {entry.idempotencyKey && (
                <Row label={t('journal.idempotency')}>
                  <span className="font-mono">{entry.idempotencyKey}</span>
                </Row>
              )}
              <Row label={t('journal.depot')}>
                <span className="font-mono">{entry.depotId}</span>
              </Row>
              {entry.reversesEntryId && (
                <Row label={t('journal.reverses')}>
                  {link(entry.reversesEntryId, entry.sourceNo ?? String(entry.reversesEntryId))}
                </Row>
              )}
              {entry.reversedByEntryId && (
                <Row label={t('journal.reversedBy')}>
                  {link(entry.reversedByEntryId, entryNo(entry, entry.reversedByEntryId))}
                </Row>
              )}
            </dl>
          </Panel>
          <Panel className="overflow-hidden">
            <Postings entry={entry} debit={debit} credit={credit} depotId={depotId} />
          </Panel>
        </div>
        <aside className="flex w-[360px] shrink-0 flex-col gap-3 rounded-lg border border-fk-border bg-fk-surface p-4">
          <h2 id="trail-h" className="text-15 font-semibold">
            {t('journal.trail')}
          </h2>
          <ol aria-labelledby="trail-h" className="flex flex-col gap-3">
            {[...(trail ?? [])].reverse().map((item, i) => (
              <TrailRow key={`${item.at}${item.kind}${i}`} item={item} />
            ))}
          </ol>
        </aside>
      </div>
      {reversing && (
        <ReverseDialog
          orgId={orgId}
          entry={entry}
          onClose={() => setReversing(false)}
          onReversed={(r) => {
            setReversing(false)
            toast.success(t('journal.reverseDialog.done', { no: r.entryNo }))
            navigate(withDepot(`/journal/${r.id}`, depotId))
          }}
        />
      )}
    </div>
  )
}

// Entry numbers are J-YEAR-NNNNNN with the id as counter in this build.
function entryNo(entry: JournalEntry, id: number) {
  return `${entry.entryNo.slice(0, 7)}${String(id).padStart(6, '0')}`
}

function Row({ label, children }: Readonly<{ label: string; children: React.ReactNode }>) {
  return (
    <>
      <dt className="text-fk-muted">{label}</dt>
      <dd className="m-0 min-w-0">{children}</dd>
    </>
  )
}

function Postings({
  entry,
  debit,
  credit,
  depotId,
}: Readonly<{ entry: JournalEntry; debit: number; credit: number; depotId: number | null }>) {
  const { t } = useTranslation('ledger')
  const th =
    'h-9 border-b border-r border-fk-border border-r-fk-divider bg-fk-sunken px-2.5 text-left text-xs font-medium text-fk-secondary'
  const td =
    'h-[34px] border-b border-r border-fk-divider border-r-fk-divider-soft px-2.5 text-grid'
  const amount = (side: 'DEBIT' | 'CREDIT', p: JournalEntry['postings'][number]) =>
    p.side === side ? (
      <td className={`${td} fk-num`}>{formatMoney(p.amount)}</td>
    ) : (
      <td className={`${td} fk-num text-fk-faint`}>—</td>
    )
  return (
    <table aria-label={t('journal.postings')} className="w-full border-separate border-spacing-0">
      <thead>
        <tr>
          <th className={`${th} w-12`}>{t('journal.col.no')}</th>
          <th className={th}>{t('journal.col.account')}</th>
          <th className={`${th} w-40 text-right`}>{t('journal.col.debit')}</th>
          <th className={`${th} w-40 text-right`}>{t('journal.col.credit')}</th>
        </tr>
      </thead>
      <tbody>
        {entry.postings.map((p) => (
          <tr key={p.id} className="hover:bg-fk-hover">
            <td className={`${td} font-mono`}>{p.lineNo}</td>
            <td className={td}>
              {CURRENT.test(p.accountCode ?? '') ? (
                <Link
                  to={withDepot(`/statements/${p.accountId}`, depotId)}
                  className="font-mono text-fk-accent hover:text-fk-accent-hover"
                >
                  {p.accountCode}
                </Link>
              ) : (
                <span className="font-mono">{p.accountCode}</span>
              )}{' '}
              <span>{p.accountName}</span>
            </td>
            {amount('DEBIT', p)}
            {amount('CREDIT', p)}
          </tr>
        ))}
        <tr className="bg-fk-sunken font-semibold">
          <td className={td} />
          <td className={td}>{t('total')}</td>
          <td className={`${td} fk-num`}>{formatMoney(debit)}</td>
          <td className={`${td} fk-num`}>{formatMoney(credit)}</td>
        </tr>
      </tbody>
    </table>
  )
}

function TrailRow({ item }: Readonly<{ item: TrailItem }>) {
  const { t } = useTranslation('ledger')
  return (
    <li className="flex gap-2.5 text-grid">
      <span aria-hidden="true" className="mt-1.5 h-2 w-2 shrink-0 rounded-full bg-fk-accent" />
      <div className="min-w-0">
        <div>{t(`journal.trailKind.${item.kind}`, { label: item.label })}</div>
        <div className="font-mono text-xs text-fk-muted">
          {[
            formatTime(item.at),
            item.actorUserId ? t('journal.trailUser', { user: item.actorUserId }) : null,
            item.ref ? t(`journal.trailRef.${item.kind}`, { ref: item.ref }) : null,
          ]
            .filter(Boolean)
            .join(' · ')}
        </div>
      </div>
    </li>
  )
}
