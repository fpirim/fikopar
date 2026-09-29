import { useId, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { setPartPrice } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type { Part, PriceWrite } from '../../api/types'
import { useToast } from '../../context/ToastContext'
import { useIdempotencyKey } from '../../hooks/useIdempotencyKey'
import { formatDay, formatMoney, todayIso } from '../../lib/format'
import { Button } from '../../components/ui/button'
import { DateField } from '../../components/ui/date-field'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog'
import { Field } from '../../components/ui/field'
import { MoneyInput } from '../../components/ui/money-input'

// New list price from a date; a future date takes effect on that day.
export default function PriceDialog({
  orgId,
  part,
  onClose,
  onSaved,
}: Readonly<{ orgId: number; part: Part; onClose: () => void; onSaved: () => void }>) {
  const { t } = useTranslation(['parts', 'errors'])
  const toast = useToast()
  const idem = useIdempotencyKey()
  const priceId = useId()
  const [price, setPrice] = useState('')
  const [validFrom, setValidFrom] = useState(todayIso())
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function save(e: React.FormEvent) {
    e.preventDefault()
    if (!(Number(price) > 0)) return
    const body: PriceWrite = { price, validFrom }
    setBusy(true)
    setError('')
    try {
      await setPartPrice(orgId, part.id, body, idem.keyFor(body))
      toast.success(t('priceSaved', { price: formatMoney(price), date: formatDay(validFrom) }))
      onSaved()
    } catch (err) {
      setError(errorText(t, err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent aria-describedby={undefined} className="max-w-md">
        <DialogHeader>
          <DialogTitle>{t('newPriceTitle', { stockNo: part.stockNo })}</DialogTitle>
        </DialogHeader>
        <form onSubmit={save} className="grid grid-cols-2 gap-3">
          <Field label={t('price')} htmlFor={priceId}>
            <MoneyInput id={priceId} value={price} onChange={setPrice} className="text-sm" />
          </Field>
          <DateField
            label={t('validFrom')}
            value={validFrom}
            onChange={setValidFrom}
            inputClassName="h-10"
          />
          <p className="col-span-2 text-13 text-fk-muted">{t('futureHint')}</p>
          {error && (
            <p role="alert" className="col-span-2 text-13 text-fk-negative">
              {error}
            </p>
          )}
          <DialogFooter className="col-span-2">
            <Button type="button" variant="outline" onClick={onClose} className="min-w-[88px]">
              {t('cancel')}
            </Button>
            <Button type="submit" disabled={busy || !(Number(price) > 0)} className="min-w-[88px]">
              {t('save')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
