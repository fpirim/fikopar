import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { ApiError } from '../../api/client'
import { reopenCarryForward } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type { CarryForward } from '../../api/types'
import { useIdempotencyKey } from '../../hooks/useIdempotencyKey'
import { Button } from '../../components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog'
import { TextField } from '../../components/ui/text-field'

export default function ReopenDialog({
  orgId,
  cf,
  onClose,
  onReopened,
  onConflict,
}: Readonly<{
  orgId: number
  cf: CarryForward
  onClose: () => void
  onReopened: (cf: CarryForward) => void
  onConflict: () => void
}>) {
  const { t } = useTranslation(['ledger', 'common', 'errors'])
  const idem = useIdempotencyKey()
  const [reason, setReason] = useState('')
  const [touched, setTouched] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const short = reason.trim().length < 3

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setTouched(true)
    if (short) return
    const body = { year: cf.year, reason: reason.trim() }
    setBusy(true)
    setError('')
    try {
      onReopened(
        await reopenCarryForward(orgId, cf.year, body.reason, cf.version, idem.keyFor(body)),
      )
    } catch (err) {
      if (err instanceof ApiError && err.status === 412) onConflict()
      else setError(errorText(t, err, { date: `${cf.year}-01-01` }))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{t('carryForward.reopenDialog.title', { year: cf.year })}</DialogTitle>
          <DialogDescription>
            {t('carryForward.reopenDialog.body', { prev: cf.year - 1 })}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={submit} className="flex flex-col gap-3">
          <div>
            <TextField
              label={t('carryForward.reopenDialog.reason')}
              value={reason}
              onChange={setReason}
              autoFocus
              aria-invalid={(touched && short) || undefined}
            />
            {touched && short && (
              <span className="text-xs text-fk-negative">
                {t('carryForward.reopenDialog.reasonShort')}
              </span>
            )}
          </div>
          {error && (
            <p role="alert" className="text-13 text-fk-negative">
              {error}
            </p>
          )}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>
              {t('common:cancel')}
            </Button>
            <Button type="submit" variant="danger" disabled={busy} className="min-w-[112px]">
              {t('carryForward.reopenDialog.submit')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
