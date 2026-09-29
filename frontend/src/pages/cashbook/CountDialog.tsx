import { useId, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { errorText } from '../../api/errorText'
import { createCashCount, getCashBook } from '../../api/endpoints'
import type { CashCountCreate } from '../../api/types'
import { useToast } from '../../context/ToastContext'
import { useApi } from '../../hooks/useApi'
import { useIdempotencyKey } from '../../hooks/useIdempotencyKey'
import { formatMoney, todayIso } from '../../lib/format'
import { Button } from '../../components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog'
import { Field } from '../../components/ui/field'
import { KeyValue } from '../../components/ui/panel'
import { MoneyInput } from '../../components/ui/money-input'
import { TextField } from '../../components/ui/text-field'
import { cn } from '@/lib/utils'

export default function CountDialog({
  orgId,
  depotId,
  onClose,
  onSaved,
}: Readonly<{ orgId: number; depotId: number; onClose: () => void; onSaved: () => void }>) {
  const { t } = useTranslation(['treasury', 'common', 'errors'])
  const toast = useToast()
  const idem = useIdempotencyKey()
  const amountId = useId()
  const today = todayIso()
  const book = useApi(
    () => getCashBook(orgId, { depotId, from: today, to: today }),
    [orgId, depotId, today],
  ).data?.closingBalance
  const [counted, setCounted] = useState('')
  const [postDifference, setPostDifference] = useState(false)
  const [note, setNote] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const diff = counted && book !== undefined ? Number(counted) - Number(book) : null

  async function save(e: React.FormEvent) {
    e.preventDefault()
    if (counted === '') return
    const body: CashCountCreate = {
      depotId,
      countedAmount: counted,
      postDifference,
      ...(note.trim() ? { note: note.trim() } : {}),
    }
    setBusy(true)
    setError('')
    try {
      const c = await createCashCount(orgId, body, idem.keyFor(body))
      toast.success(t('cash.countDialog.saved', { diff: formatMoney(c.difference) }))
      onSaved()
    } catch (err) {
      setError(errorText(t, err, { date: today }))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent aria-describedby={undefined} className="max-w-md">
        <DialogHeader>
          <DialogTitle>{t('cash.countDialog.title', { id: depotId })}</DialogTitle>
        </DialogHeader>
        <form onSubmit={save} className="flex flex-col gap-3">
          <KeyValue label={t('cash.countDialog.book')}>
            {book === undefined ? '—' : formatMoney(book)}
          </KeyValue>
          <Field label={t('cash.countDialog.counted')} htmlFor={amountId}>
            <MoneyInput id={amountId} value={counted} onChange={setCounted} />
          </Field>
          <KeyValue label={t('cash.countDialog.difference')}>
            <span className={cn(diff !== null && diff < 0 && 'text-fk-negative')}>
              {diff === null ? '—' : formatMoney(diff)}
            </span>
          </KeyValue>
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={postDifference}
              onChange={(e) => setPostDifference(e.target.checked)}
              className="h-3.5 w-3.5 accent-[var(--fk-accent)]"
            />
            {t('cash.countDialog.postDifference')}
          </label>
          <TextField label={t('cash.countDialog.note')} value={note} onChange={setNote} />
          {error && (
            <p role="alert" className="text-13 text-fk-negative">
              {error}
            </p>
          )}
          <DialogFooter className="mt-1">
            <Button type="button" variant="outline" onClick={onClose}>
              {t('common:cancel')}
            </Button>
            <Button type="submit" disabled={busy || counted === ''} className="min-w-[120px]">
              {t('cash.countDialog.save')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
