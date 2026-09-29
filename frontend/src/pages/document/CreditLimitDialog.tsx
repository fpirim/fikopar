import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import type { CreditLimitExceeded } from '../../api/types'
import { formatMoney } from '../../lib/format'
import { Button } from '../../components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog'
import { KeyValue } from '../../components/ui/panel'
import { TextField } from '../../components/ui/text-field'

// 409 ERR_CREDIT_LIMIT_EXCEEDED: the numbers, and for an admin a way to post with a reason.
export default function CreditLimitDialog({
  details,
  admin,
  busy,
  error,
  onOverride,
  onClose,
}: Readonly<{
  details: CreditLimitExceeded
  admin: boolean
  busy: boolean
  error: string
  onOverride: (reason: string) => void
  onClose: () => void
}>) {
  const { t } = useTranslation(['documents', 'common'])
  const [reason, setReason] = useState('')
  const [touched, setTouched] = useState(false)
  const short = reason.trim().length < 3

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent role="alertdialog" className="max-w-md">
        <DialogHeader>
          <DialogTitle>{t('credit.title')}</DialogTitle>
          <DialogDescription>{t(admin ? 'credit.bodyAdmin' : 'credit.body')}</DialogDescription>
        </DialogHeader>
        <div className="flex flex-col gap-2 rounded-md border border-fk-border bg-fk-sunken px-3.5 py-3">
          <KeyValue label={t('credit.limit')}>{formatMoney(details.limit)}</KeyValue>
          <KeyValue label={t('credit.balance')}>{formatMoney(details.balance)}</KeyValue>
          <KeyValue label={t('credit.documentTotal')}>
            {formatMoney(details.documentTotal)}
          </KeyValue>
          <KeyValue label={t('credit.excess')} className="font-semibold text-fk-negative">
            {formatMoney(details.excess)}
          </KeyValue>
        </div>
        {admin && (
          <form
            id="credit-override"
            onSubmit={(e) => {
              e.preventDefault()
              setTouched(true)
              if (!short) onOverride(reason.trim())
            }}
          >
            <TextField
              label={t('credit.reason')}
              value={reason}
              onChange={setReason}
              maxLength={500}
              aria-invalid={(touched && short) || undefined}
            />
            {touched && short && (
              <span className="text-xs text-fk-negative">{t('credit.reasonShort')}</span>
            )}
          </form>
        )}
        {error && (
          <p role="alert" className="text-13 text-fk-negative">
            {error}
          </p>
        )}
        <DialogFooter>
          <Button type="button" variant="outline" onClick={onClose}>
            {t(admin ? 'common:cancel' : 'common:close')}
          </Button>
          {admin && (
            <Button type="submit" form="credit-override" disabled={busy} className="min-w-[140px]">
              {busy ? t('posting') : t('credit.override')}
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
