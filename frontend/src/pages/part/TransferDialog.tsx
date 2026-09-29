import { useId, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { transferStock } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type { Depot, Part, StockLevel, StockTransfer, StockTransferCreate } from '../../api/types'
import { useIdempotencyKey } from '../../hooks/useIdempotencyKey'
import { formatQty, todayIso } from '../../lib/format'
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
import { parseQty } from './qty'

// Moves this part between two depots (TRF); negative stock is allowed and reported afterwards.
export default function TransferDialog({
  orgId,
  part,
  depots,
  levels,
  fromDepotId,
  onClose,
  onPosted,
}: Readonly<{
  orgId: number
  part: Part
  depots: Depot[]
  levels: StockLevel[]
  fromDepotId: number
  onClose: () => void
  onPosted: (t: StockTransfer) => void
}>) {
  const { t } = useTranslation(['parts', 'errors'])
  const idem = useIdempotencyKey()
  const ids = { from: useId(), to: useId(), qty: useId() }
  const [from, setFrom] = useState(fromDepotId)
  const [to, setTo] = useState(depots.find((d) => d.id !== fromDepotId)?.id ?? fromDepotId)
  const [date, setDate] = useState(todayIso())
  const [qty, setQty] = useState('')
  const [note, setNote] = useState('')
  const [touched, setTouched] = useState(false)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const code = (id: number) => depots.find((d) => d.id === id)?.code ?? String(id)
  const available = Number(levels.find((l) => l.depotId === from)?.available ?? 0)
  const amount = parseQty(qty)
  const badQty = !amount || !(Number(amount) > 0)

  async function post(e: React.FormEvent) {
    e.preventDefault()
    setTouched(true)
    if (badQty || from === to) return
    const body: StockTransferCreate = {
      fromDepotId: from,
      toDepotId: to,
      date,
      note: note.trim() || undefined,
      lines: [{ partId: part.id, qty: amount! }],
    }
    setBusy(true)
    setError('')
    try {
      onPosted(await transferStock(orgId, body, idem.keyFor(body)))
    } catch (err) {
      setError(errorText(t, err, { date }))
    } finally {
      setBusy(false)
    }
  }

  const pickFrom = (id: number) => {
    setFrom(id)
    if (id === to) setTo(depots.find((d) => d.id !== id)?.id ?? id)
  }

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent aria-describedby={undefined} className="max-w-lg">
        <DialogHeader>
          <DialogTitle>{t('transfer.title', { stockNo: part.stockNo })}</DialogTitle>
        </DialogHeader>
        <form onSubmit={post} className="grid grid-cols-2 gap-3">
          <Field label={t('transfer.from')} htmlFor={ids.from}>
            <select
              id={ids.from}
              className={selectCls}
              value={from}
              onChange={(e) => pickFrom(Number(e.target.value))}
            >
              {depots.map((d) => (
                <option key={d.id} value={d.id}>
                  {t('depotN', { code: d.code })}
                </option>
              ))}
            </select>
          </Field>
          <Field
            label={t('transfer.to')}
            htmlFor={ids.to}
            error={from === to ? t('errors:ERR_SAME_DEPOT') : null}
          >
            <select
              id={ids.to}
              className={selectCls}
              value={to}
              onChange={(e) => setTo(Number(e.target.value))}
            >
              {depots.map((d) => (
                <option key={d.id} value={d.id}>
                  {t('depotN', { code: d.code })}
                </option>
              ))}
            </select>
          </Field>
          <DateField
            label={t('transfer.date')}
            value={date}
            onChange={setDate}
            inputClassName="h-10"
          />
          <Field
            label={t('transfer.qty')}
            htmlFor={ids.qty}
            error={touched && badQty ? t('transfer.invalidQty') : null}
          >
            <input
              id={ids.qty}
              inputMode="decimal"
              value={qty}
              onChange={(e) => setQty(e.target.value)}
              aria-invalid={(touched && badQty) || undefined}
              className="h-10 w-full rounded-md border border-fk-input bg-fk-surface px-2.5 text-right font-mono text-sm text-fk-text outline-none focus:border-fk-accent focus:ring-1 focus:ring-fk-accent aria-[invalid]:border-fk-negative"
            />
          </Field>
          <p
            className={
              !badQty && Number(amount) > available
                ? 'col-span-2 text-13 text-fk-negative'
                : 'col-span-2 text-13 text-fk-muted'
            }
          >
            {!badQty && Number(amount) > available
              ? t('transfer.overAvailable', { qty: formatQty(available), code: code(from) })
              : t('transfer.available', { qty: formatQty(available), code: code(from) })}
          </p>
          <TextField
            label={t('transfer.note')}
            value={note}
            onChange={setNote}
            className="col-span-2"
          />
          {error && (
            <p role="alert" className="col-span-2 text-13 text-fk-negative">
              {error}
            </p>
          )}
          <DialogFooter className="col-span-2">
            <Button type="button" variant="outline" onClick={onClose} className="min-w-[88px]">
              {t('cancel')}
            </Button>
            <Button type="submit" disabled={busy} className="min-w-[104px]">
              {t('transfer.submit')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
