import { useMemo, useRef, useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { ChevronDown } from 'lucide-react'
import { exportStockCountLines, getStockCount, printCountSheet } from '../api/endpoints'
import type { CountLinesOnly, GridRequest, StockCount } from '../api/types'
import { useToast } from '../context/ToastContext'
import { useApi } from '../hooks/useApi'
import { useDepot, withDepot } from '../hooks/useDepot'
import { useDepots } from '../hooks/useDepots'
import { useDownload } from '../hooks/useDownload'
import { useOrgId } from '../hooks/useOrgId'
import { formatDay } from '../lib/format'
import { cn } from '@/lib/utils'
import { Button } from '../components/ui/button'
import { Chip } from '../components/ui/chip'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '../components/ui/dropdown-menu'
import { Panel } from '../components/ui/panel'
import ConflictDialog from '../components/ConflictDialog'
import SelectCompany from '../components/layout/SelectCompany'
import CountGrid, { type Scope } from './count/CountGrid'
import AddLineDialog from './count/AddLineDialog'
import CancelDialog from './count/CancelDialog'
import PostDialog from './count/PostDialog'
import { STATUS_TONE } from './count/status'

const ONLY: Exclude<CountLinesOnly, 'ALL'>[] = ['DIFFERENCES', 'NOT_COUNTED', 'COUNTED']
const SHEETS = ['BLIND', 'WITH_SYSTEM', 'NOT_COUNTED'] as const

function Kbd({ children }: Readonly<{ children: string }>) {
  return (
    <span className="rounded border border-b-2 border-fk-input bg-fk-surface px-[5px] py-px font-mono text-2xs font-medium text-fk-secondary">
      {children}
    </span>
  )
}

export default function StockCountPage() {
  const orgId = useOrgId()
  const { countId } = useParams()
  return orgId ? <Count orgId={orgId} countId={Number(countId)} /> : <SelectCompany />
}

function Count({ orgId, countId }: Readonly<{ orgId: number; countId: number }>) {
  const { t, i18n } = useTranslation(['counts', 'common', 'grid'])
  const toast = useToast()
  const { depotId } = useDepot()
  const depots = useDepots()
  const download = useDownload()
  const [params, setParams] = useSearchParams()
  const [dialog, setDialog] = useState<'post' | 'cancel' | 'stale' | 'add' | null>(null)
  const [refreshKey, setRefreshKey] = useState(0)
  const head = useApi(() => getStockCount(orgId, countId), [orgId, countId])
  const requestRef = useRef<(() => GridRequest) | null>(null)
  const count = head.data

  const only = (params.get('only') as CountLinesOnly | null) ?? 'ALL'
  const shelfFrom = params.get('shelfFrom') ?? ''
  const shelfTo = params.get('shelfTo') ?? ''
  const scope = useMemo<Scope>(() => ({ only, shelfFrom, shelfTo }), [only, shelfFrom, shelfTo])
  const setParam = (key: string, value: string | null) =>
    setParams(
      (p) => {
        const next = new URLSearchParams(p)
        if (value) next.set(key, value)
        else next.delete(key)
        return next
      },
      { replace: true },
    )

  if (!count) {
    return head.error ? <p className="text-fk-negative">{head.error.code}</p> : null
  }
  const open = count.status === 'OPEN'
  const code = depots.find((d) => d.id === count.depotId)?.code ?? '—'
  const nf = (n: number) => n.toLocaleString(i18n.language === 'tr' ? 'tr-TR' : 'en-US')
  const pct = count.lineCount ? (count.countedCount / count.lineCount) * 100 : 0
  const started = formatDay(count.startedAt.slice(0, 10))
  const settle = (c: StockCount) => {
    setDialog(null)
    head.setData(c)
  }

  return (
    <div className="flex min-h-0 flex-1 flex-col gap-3">
      <div className="flex flex-wrap items-center gap-3">
        <Link
          to={withDepot('/stock-counts', depotId)}
          className="sr-only focus:not-sr-only focus:text-13"
        >
          {t('allCounts')}
        </Link>
        <h1 className="shrink-0 whitespace-nowrap font-mono text-22 font-medium">
          {count.countNo}
        </h1>
        <span className="shrink-0">
          <Chip tone={STATUS_TONE[count.status]}>{t(`status.${count.status}`)}</Chip>
        </span>
        <span className="flex min-w-0 flex-1 flex-wrap gap-x-1.5 text-sm text-fk-muted">
          <span className="whitespace-nowrap">{t('head.started', { code, date: started })}</span>
          {formatDay(count.countDate) !== started && (
            <span className="whitespace-nowrap">
              · {t('head.countDate', { date: formatDay(count.countDate) })}
            </span>
          )}
        </span>
        <div className="ml-auto flex shrink-0 items-center gap-3">
          <Button
            variant="outline"
            disabled={download.busy !== null}
            onClick={() =>
              void download.run('xlsx', `${count.countNo}.xlsx`, () =>
                exportStockCountLines(orgId, count.id, { ...requestRef.current?.(), ...scope }),
              )
            }
          >
            {t('grid:fk.excel')}
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" disabled={download.busy !== null}>
                {t('sheet.button')}
                <ChevronDown aria-hidden="true" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {SHEETS.map((s) => (
                <DropdownMenuItem
                  key={s}
                  onSelect={() =>
                    void download.run('sheet', `${count.countNo}-${s.toLowerCase()}.pdf`, () =>
                      printCountSheet(orgId, count.id, {
                        sheet: s === 'WITH_SYSTEM' ? 'WITH_SYSTEM' : 'BLIND',
                        only: s === 'NOT_COUNTED' ? 'NOT_COUNTED' : undefined,
                        shelfFrom: shelfFrom || undefined,
                        shelfTo: shelfTo || undefined,
                      }),
                    )
                  }
                >
                  {t(`sheet.${s}`)}
                </DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>
          {open && (
            <>
              <Button variant="outline" onClick={() => setDialog('cancel')}>
                {t('cancel.button')}
              </Button>
              <Button onClick={() => setDialog('post')}>{t('post.button')}</Button>
            </>
          )}
        </div>
      </div>
      <div className="flex items-center gap-4">
        <div
          role="progressbar"
          aria-label={t('head.progress')}
          aria-valuemin={0}
          aria-valuemax={count.lineCount}
          aria-valuenow={count.countedCount}
          className="h-2 flex-1 overflow-hidden rounded bg-fk-chip-grey"
        >
          <div className="h-2 bg-fk-accent" style={{ width: `${pct}%` }} />
        </div>
        <span className="min-w-[150px] whitespace-nowrap text-right font-mono text-sm tabular-nums">
          {t('head.lines', { counted: nf(count.countedCount), total: nf(count.lineCount) })}
        </span>
      </div>
      <div className="flex items-center gap-2">
        {ONLY.map((o) => (
          <Button
            key={o}
            variant="outline"
            size="sm"
            aria-pressed={only === o}
            onClick={() => setParam('only', only === o ? null : o)}
            className={cn(only === o && 'border-fk-text bg-fk-text text-white hover:bg-fk-text')}
          >
            {t(`only.${o}`)}
          </Button>
        ))}
        <ShelfInput
          label={t('shelfFrom')}
          value={shelfFrom}
          onCommit={(v) => setParam('shelfFrom', v)}
        />
        <ShelfInput label={t('shelfTo')} value={shelfTo} onCommit={(v) => setParam('shelfTo', v)} />
        {open && (
          <Button variant="outline" size="sm" onClick={() => setDialog('add')}>
            {t('add.button')}
          </Button>
        )}
        <div className="flex-1" />
        {open && (
          <span className="flex items-center gap-1.5 whitespace-nowrap text-xs text-fk-muted">
            {t('hint.type')} <Kbd>Enter</Kbd> {t('hint.next')} <Kbd>Esc</Kbd> {t('hint.undo')} ·{' '}
            {t('hint.saved')}
          </span>
        )}
      </div>
      <Panel className="flex min-h-0 shrink flex-col overflow-hidden">
        <CountGrid
          orgId={orgId}
          count={count}
          scope={scope}
          onSaved={head.reload}
          requestRef={requestRef}
          refreshKey={refreshKey}
        />
      </Panel>
      {dialog === 'post' && (
        <PostDialog
          orgId={orgId}
          count={count}
          depots={depots}
          onClose={() => setDialog(null)}
          onPosted={settle}
          onConflict={() => setDialog('stale')}
        />
      )}
      {dialog === 'cancel' && (
        <CancelDialog
          orgId={orgId}
          count={count}
          onClose={() => setDialog(null)}
          onCancelled={(c) => {
            toast.success(t('cancel.cancelled', { no: c.countNo }))
            settle(c)
          }}
          onConflict={() => setDialog('stale')}
        />
      )}
      {dialog === 'add' && (
        <AddLineDialog
          orgId={orgId}
          count={count}
          onClose={() => setDialog(null)}
          onAdded={(no) => {
            toast.success(t('add.added', { no }))
            setDialog(null)
            setRefreshKey((k) => k + 1)
            head.reload()
          }}
        />
      )}
      {dialog === 'stale' && (
        <ConflictDialog
          title={t('stale.title')}
          message={t('stale.message')}
          onReload={() => {
            setDialog(null)
            head.reload()
          }}
        />
      )}
    </div>
  )
}

function ShelfInput({
  label,
  value,
  onCommit,
}: Readonly<{ label: string; value: string; onCommit: (v: string | null) => void }>) {
  const [draft, setDraft] = useState<string | null>(null)
  const commit = () => {
    if (draft === null) return
    setDraft(null)
    if (draft.trim() !== value) onCommit(draft.trim() || null)
  }
  return (
    <input
      aria-label={label}
      placeholder={label}
      value={draft ?? value}
      onChange={(e) => setDraft(e.target.value)}
      onBlur={commit}
      onKeyDown={(e) => e.key === 'Enter' && commit()}
      className="h-[30px] w-[96px] rounded-md border border-fk-input bg-fk-surface px-2 font-mono text-13 outline-none placeholder:font-sans placeholder:text-fk-faint focus:border-fk-accent focus:ring-1 focus:ring-fk-accent"
    />
  )
}
