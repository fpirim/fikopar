import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { ApiError } from '../../api/client'
import { finalizeStockCount } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type { Depot, StockCount, StockCountFinalized } from '../../api/types'
import { useIdempotencyKey } from '../../hooks/useIdempotencyKey'
import { formatDay, formatMoney, formatQty } from '../../lib/format'
import { cn } from '@/lib/utils'
import { Button } from '../../components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog'
import { Segmented } from '../../components/ui/segmented'

function Figure({
  label,
  value,
  tone,
}: Readonly<{ label: string; value: string; tone?: 'in' | 'out' }>) {
  return (
    <div role="group" aria-label={label} className="rounded-md border border-fk-border px-3 py-2">
      <div className="text-xs text-fk-muted">{label}</div>
      <div
        className={cn(
          'mt-0.5 whitespace-nowrap font-mono text-15 tabular-nums',
          tone === 'in' && 'text-fk-chip-green-fg',
          tone === 'out' && 'text-fk-negative',
        )}
      >
        {value}
      </div>
    </div>
  )
}

export default function PostDialog({
  orgId,
  count,
  depots,
  onClose,
  onPosted,
  onConflict,
}: Readonly<{
  orgId: number
  count: StockCount
  depots: Depot[]
  onClose: () => void
  onPosted: (c: StockCount) => void
  onConflict: () => void
}>) {
  const { t, i18n } = useTranslation(['counts', 'common', 'errors'])
  const idem = useIdempotencyKey()
  const [uncounted, setUncounted] = useState<'SKIP' | 'ZERO'>('SKIP')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [result, setResult] = useState<StockCountFinalized | null>(null)
  const nf = (n: number) => n.toLocaleString(i18n.language === 'tr' ? 'tr-TR' : 'en-US')
  const code = (id: number) => depots.find((d) => d.id === id)?.code ?? String(id)
  const open = count.lineCount - count.countedCount

  async function submit() {
    const body = { id: count.id, uncounted }
    setBusy(true)
    setError('')
    try {
      const r = await finalizeStockCount(orgId, count, uncounted, idem.keyFor(body))
      setResult(r)
    } catch (e) {
      if (e instanceof ApiError && e.status === 412) onConflict()
      else setError(errorText(t, e, { date: count.countDate }))
    } finally {
      setBusy(false)
    }
  }

  const done = () => (result ? onPosted(result.count) : onClose())

  return (
    <Dialog open onOpenChange={(o) => !o && done()}>
      <DialogContent aria-describedby={undefined} className="max-w-lg">
        <DialogHeader>
          <DialogTitle>{t('post.title', { no: count.countNo })}</DialogTitle>
        </DialogHeader>
        <div className="grid grid-cols-3 gap-2">
          <Figure label={t('post.counted')} value={nf(count.countedCount)} />
          <Figure label={t('post.notCounted')} value={nf(open)} />
          <Figure label={t('post.differences')} value={nf(count.differenceCount)} />
          <Figure label={t('post.valueIn')} value={formatMoney(count.valueIn ?? 0)} tone="in" />
          <Figure label={t('post.valueOut')} value={formatMoney(count.valueOut ?? 0)} tone="out" />
          <Figure
            label={t('post.net')}
            value={formatMoney(count.differenceValue ?? 0, { sign: true })}
          />
        </div>
        <p className="text-13 text-fk-muted">
          {t('post.costNote', { date: formatDay(count.countDate) })}
          {count.unvaluedLines ? (
            <span className="block text-fk-chip-amber-fg">
              {t('post.unvalued', { count: count.unvaluedLines })}
            </span>
          ) : null}
        </p>
        {!result && open > 0 && (
          <div className="flex items-center gap-3 text-13">
            <span className="text-fk-muted">{t('post.uncounted')}</span>
            <Segmented
              label={t('post.uncounted')}
              value={uncounted}
              onChange={setUncounted}
              options={(['SKIP', 'ZERO'] as const).map((v) => ({
                value: v,
                label: t(`post.${v}`),
              }))}
            />
          </div>
        )}
        {error && (
          <p role="alert" className="text-13 text-fk-negative">
            {error}
          </p>
        )}
        {result && (
          <div role="status" className="flex flex-col gap-1.5 text-13">
            <p className="font-medium">
              {t('post.posted', { no: result.count.countNo, count: result.movementCount })}
            </p>
            {result.warnings.length > 0 && (
              <>
                <p className="text-fk-negative">{t('post.negative')}</p>
                <ul className="max-h-32 overflow-y-auto font-mono text-fk-negative">
                  {result.warnings.map((w) => (
                    <li key={`${w.partId}-${w.depotId}`}>
                      {t('post.negativeLine', {
                        no: w.stockNo ?? w.partId,
                        code: code(w.depotId),
                        qty: formatQty(w.onHandAfter),
                      })}
                    </li>
                  ))}
                </ul>
              </>
            )}
          </div>
        )}
        <DialogFooter>
          {result ? (
            <Button onClick={done} className="min-w-[96px]">
              {t('post.done')}
            </Button>
          ) : (
            <>
              <Button variant="outline" onClick={onClose}>
                {t('common:cancel')}
              </Button>
              <Button onClick={submit} disabled={busy} className="min-w-[96px]">
                {t('post.submit')}
              </Button>
            </>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
