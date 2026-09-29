import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { ApiError } from '../../api/client'
import { cancelStockCount } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type { StockCount } from '../../api/types'
import { Button } from '../../components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog'
import { TextField } from '../../components/ui/text-field'

export default function CancelDialog({
  orgId,
  count,
  onClose,
  onCancelled,
  onConflict,
}: Readonly<{
  orgId: number
  count: StockCount
  onClose: () => void
  onCancelled: (c: StockCount) => void
  onConflict: () => void
}>) {
  const { t } = useTranslation(['counts', 'errors'])
  const [reason, setReason] = useState('')
  const [touched, setTouched] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const short = reason.trim().length < 3

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setTouched(true)
    if (short) return
    setBusy(true)
    setError('')
    try {
      onCancelled(await cancelStockCount(orgId, count, reason.trim()))
    } catch (err) {
      if (err instanceof ApiError && err.status === 412) onConflict()
      else setError(errorText(t, err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent aria-describedby={undefined} className="max-w-md">
        <DialogHeader>
          <DialogTitle>{t('cancel.title', { no: count.countNo })}</DialogTitle>
        </DialogHeader>
        <form onSubmit={submit} className="flex flex-col gap-3">
          <TextField label={t('cancel.reason')} value={reason} onChange={setReason} />
          {touched && short && (
            <p className="text-xs text-fk-negative">{t('cancel.reasonShort')}</p>
          )}
          {error && (
            <p role="alert" className="text-13 text-fk-negative">
              {error}
            </p>
          )}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>
              {t('cancel.keep')}
            </Button>
            <Button type="submit" variant="danger" disabled={busy} className="min-w-[96px]">
              {t('cancel.submit')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
