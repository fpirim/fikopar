import { useId, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { ApiError } from '../../api/client'
import {
  deleteCarryForwardOverride,
  setCarryForwardOverride,
  type PreviewRef,
} from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type { CarryForward, CarryForwardRow } from '../../api/types'
import { formatBalance } from '../../lib/format'
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

export default function OverrideDialog({
  orgId,
  year,
  row,
  preview,
  onClose,
  onSaved,
  onConflict,
}: Readonly<{
  orgId: number
  year: number
  row: CarryForwardRow
  preview: PreviewRef
  onClose: () => void
  onSaved: (cf: CarryForward) => void
  onConflict: () => void
}>) {
  const { t } = useTranslation(['ledger', 'common', 'errors'])
  const ids = { debit: useId(), credit: useId() }
  const [debit, setDebit] = useState(row.openingDebit)
  const [credit, setCredit] = useState(row.openingCredit)
  const [reason, setReason] = useState(row.overrideReason ?? '')
  const [touched, setTouched] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const short = reason.trim().length < 3

  async function run(fn: () => Promise<CarryForward>) {
    setBusy(true)
    setError('')
    try {
      onSaved(await fn())
    } catch (e) {
      if (e instanceof ApiError && e.status === 412) onConflict()
      else setError(errorText(t, e))
    } finally {
      setBusy(false)
    }
  }

  function save(e: React.FormEvent) {
    e.preventDefault()
    setTouched(true)
    if (short) return
    void run(() =>
      setCarryForwardOverride(
        orgId,
        year,
        {
          accountId: row.accountId,
          depotId: row.depotId,
          debit: debit || '0.00',
          credit: credit || '0.00',
          reason: reason.trim(),
        },
        preview,
      ),
    )
  }

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent aria-describedby={undefined} className="max-w-md">
        <DialogHeader>
          <DialogTitle>
            {t('carryForward.override.title', { code: row.accountCode, depot: row.depotId })}
          </DialogTitle>
          <p className="text-13 text-fk-muted">{row.accountName}</p>
        </DialogHeader>
        <form onSubmit={save} className="grid grid-cols-2 gap-3">
          <KeyValue label={t('carryForward.override.closing')} className="col-span-2">
            {formatBalance(row.closingBalance) || '0.00'}
          </KeyValue>
          <Field label={t('carryForward.override.debit')} htmlFor={ids.debit}>
            <MoneyInput id={ids.debit} value={debit} onChange={setDebit} />
          </Field>
          <Field label={t('carryForward.override.credit')} htmlFor={ids.credit}>
            <MoneyInput id={ids.credit} value={credit} onChange={setCredit} />
          </Field>
          <p className="col-span-2 text-xs text-fk-muted">{t('carryForward.override.skipHint')}</p>
          <div className="col-span-2">
            <TextField
              label={t('carryForward.override.reason')}
              value={reason}
              onChange={setReason}
              aria-invalid={(touched && short) || undefined}
            />
            {touched && short && (
              <span className="text-xs text-fk-negative">
                {t('carryForward.override.reasonShort')}
              </span>
            )}
          </div>
          {error && (
            <p role="alert" className="col-span-2 text-13 text-fk-negative">
              {error}
            </p>
          )}
          <DialogFooter className="col-span-2 mt-1 sm:justify-between">
            <Button
              type="button"
              variant="ghost"
              disabled={busy || row.status === 'READY'}
              onClick={() => void run(() => deleteCarryForwardOverride(orgId, year, row, preview))}
            >
              {t('carryForward.override.remove')}
            </Button>
            <div className="flex gap-2">
              <Button type="button" variant="outline" onClick={onClose}>
                {t('common:cancel')}
              </Button>
              <Button type="submit" disabled={busy} className="min-w-[80px]">
                {t('carryForward.override.save')}
              </Button>
            </div>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
