import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { reverseJournalEntry } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import { todayIso } from '../../lib/format'
import type { JournalEntry } from '../../api/types'
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

export default function ReverseDialog({
  orgId,
  entry,
  onClose,
  onReversed,
}: Readonly<{
  orgId: number
  entry: JournalEntry
  onClose: () => void
  onReversed: (reversal: JournalEntry) => void
}>) {
  const { t } = useTranslation(['ledger', 'common', 'errors'])
  const idem = useIdempotencyKey()
  const [reason, setReason] = useState('')
  const [touched, setTouched] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const short = reason.trim().length < 3
  const owned = entry.sourceType !== 'manual' && entry.sourceType !== 'reversal'

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setTouched(true)
    if (short) return
    const body = { reason: reason.trim() }
    setBusy(true)
    setError('')
    try {
      onReversed(await reverseJournalEntry(orgId, entry.id, body.reason, idem.keyFor(body)))
    } catch (err) {
      setError(errorText(t, err, { date: todayIso() }))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{t('journal.reverseDialog.title', { no: entry.entryNo })}</DialogTitle>
          <DialogDescription>{t('journal.reverseDialog.body')}</DialogDescription>
        </DialogHeader>
        {owned && (
          <p className="rounded-md bg-fk-chip-amber px-3 py-2 text-13 text-fk-chip-amber-fg">
            {t('journal.reverseDialog.owned', {
              source: entry.sourceNo ?? t(`journal.sourceType.${entry.sourceType}`),
            })}
          </p>
        )}
        <form onSubmit={submit} className="flex flex-col gap-3">
          <div>
            <TextField
              label={t('journal.reverseDialog.reason')}
              value={reason}
              onChange={setReason}
              autoFocus
              aria-invalid={(touched && short) || undefined}
            />
            {touched && short && (
              <span className="text-xs text-fk-negative">
                {t('journal.reverseDialog.reasonShort')}
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
            <Button type="submit" variant="danger" disabled={busy} className="min-w-[120px]">
              {t('journal.reverseDialog.submit')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
