import { useId, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { ApiError } from '../../api/client'
import { openStockCount } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type { Depot, StockCount, StockCountOpen } from '../../api/types'
import { useIdempotencyKey } from '../../hooks/useIdempotencyKey'
import { withDepot } from '../../hooks/useDepot'
import { todayIso } from '../../lib/format'
import { Button } from '../../components/ui/button'
import { DateField } from '../../components/ui/date-field'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog'
import { Field, selectCls } from '../../components/ui/field'
import { TextField } from '../../components/ui/text-field'

export default function StartCountDialog({
  orgId,
  depots,
  depotId,
  counts,
  onClose,
}: Readonly<{
  orgId: number
  depots: Depot[]
  depotId: number | null
  counts: StockCount[]
  onClose: () => void
}>) {
  const { t } = useTranslation(['counts', 'common', 'errors'])
  const navigate = useNavigate()
  const idem = useIdempotencyKey()
  const depotField = useId()
  const byCode = [...depots].sort((a, b) => a.code.localeCompare(b.code))
  const [depot, setDepot] = useState<number | null>(depotId)
  const [countDate, setCountDate] = useState(todayIso())
  const [shelfFrom, setShelfFrom] = useState('')
  const [shelfTo, setShelfTo] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<{ text: string; open?: StockCount } | null>(null)
  const chosen = depot ?? byCode[0]?.id ?? null
  const code = (id: number) => depots.find((d) => d.id === id)?.code ?? String(id)

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    if (!chosen) return
    const body: StockCountOpen = {
      depotId: chosen,
      countDate,
      ...(shelfFrom.trim() && { shelfFrom: shelfFrom.trim() }),
      ...(shelfTo.trim() && { shelfTo: shelfTo.trim() }),
    }
    setBusy(true)
    setError(null)
    try {
      const c = await openStockCount(orgId, body, idem.keyFor(body))
      navigate(withDepot(`/stock-counts/${c.id}`, depotId))
    } catch (err) {
      if (err instanceof ApiError && err.code === 'ERR_STOCK_COUNT_OPEN_EXISTS') {
        setError({
          text: t('start.openExists', { code: code(chosen) }),
          open: counts.find((c) => c.depotId === chosen && c.status === 'OPEN'),
        })
      } else {
        setError({ text: errorText(t, err, { date: countDate }) })
      }
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent aria-describedby={undefined} className="max-w-md">
        <DialogHeader>
          <DialogTitle>{t('start.title')}</DialogTitle>
        </DialogHeader>
        <form onSubmit={submit} className="grid grid-cols-2 gap-3">
          <Field label={t('start.depot')} htmlFor={depotField}>
            <select
              id={depotField}
              className={selectCls}
              value={chosen ?? ''}
              onChange={(e) => setDepot(Number(e.target.value))}
            >
              {byCode.map((d) => (
                <option key={d.id} value={d.id}>
                  {t('depotN', { code: d.code })}
                </option>
              ))}
            </select>
          </Field>
          <DateField
            label={t('start.countDate')}
            value={countDate}
            onChange={setCountDate}
            inputClassName="h-10"
          />
          <TextField label={t('start.shelfFrom')} value={shelfFrom} onChange={setShelfFrom} mono />
          <TextField label={t('start.shelfTo')} value={shelfTo} onChange={setShelfTo} mono />
          <p className="col-span-2 text-13 text-fk-muted">{t('start.shelfHint')}</p>
          {error && (
            <p role="alert" className="col-span-2 text-13 text-fk-negative">
              <span>{error.text}</span>
              {error.open && (
                <>
                  {' '}
                  <Link
                    className="font-mono text-fk-accent"
                    to={withDepot(`/stock-counts/${error.open.id}`, depotId)}
                  >
                    {error.open.countNo}
                  </Link>
                </>
              )}
            </p>
          )}
          <DialogFooter className="col-span-2">
            <Button type="button" variant="outline" onClick={onClose}>
              {t('common:cancel')}
            </Button>
            <Button type="submit" disabled={busy || !chosen} className="min-w-[96px]">
              {t('start.submit')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
