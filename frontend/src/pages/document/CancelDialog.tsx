import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
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

export interface Dependent {
  id: number
  no: string
}

// Cancelling a posted document: a reason, then the reversal entry and opposite movements dated today.
export default function CancelDialog({
  number,
  busy,
  error,
  dependents,
  onCancel,
  onClose,
}: Readonly<{
  number: string
  busy: boolean
  error: string
  dependents: Dependent[]
  onCancel: (reason: string) => void
  onClose: () => void
}>) {
  const { t } = useTranslation(['documents', 'common'])
  const [reason, setReason] = useState('')
  const [touched, setTouched] = useState(false)
  const short = reason.trim().length < 3

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{t('cancelDialog.title', { no: number })}</DialogTitle>
          <DialogDescription>{t('cancelDialog.body')}</DialogDescription>
        </DialogHeader>
        <form
          id="cancel-document"
          onSubmit={(e) => {
            e.preventDefault()
            setTouched(true)
            if (!short) onCancel(reason.trim())
          }}
        >
          <TextField
            label={t('cancelDialog.reason')}
            value={reason}
            onChange={setReason}
            maxLength={500}
            autoFocus
            aria-invalid={(touched && short) || undefined}
          />
          {touched && short && (
            <span className="text-xs text-fk-negative">{t('cancelDialog.reasonShort')}</span>
          )}
        </form>
        {error && (
          <div role="alert" className="flex flex-col gap-1 text-13 text-fk-negative">
            <p>{error}</p>
            {dependents.length > 0 && (
              <ul className="flex flex-wrap gap-x-3 font-mono">
                {dependents.map((d) => (
                  <li key={d.id}>
                    <Link to={`/invoices/${d.id}`} onClick={onClose}>
                      {d.no}
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
        <DialogFooter>
          <Button type="button" variant="outline" onClick={onClose}>
            {t('common:close')}
          </Button>
          <Button
            type="submit"
            form="cancel-document"
            variant="destructive"
            disabled={busy}
            className="min-w-[150px]"
          >
            {busy ? t('cancelling') : t('cancelDialog.confirm')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
