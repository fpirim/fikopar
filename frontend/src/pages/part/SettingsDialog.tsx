import { useId, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { ApiError } from '../../api/client'
import { setPartDepotSettings } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type { StockLevel } from '../../api/types'
import { useToast } from '../../context/ToastContext'
import { formatQty } from '../../lib/format'
import ConflictDialog from '../../components/ConflictDialog'
import { Button } from '../../components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog'
import { Field } from '../../components/ui/field'
import { parseQty } from './qty'

const inputCls =
  'h-10 w-full rounded-md border border-fk-input bg-fk-surface px-2.5 font-mono text-sm text-fk-text outline-none focus:border-fk-accent focus:ring-1 focus:ring-fk-accent aria-[invalid]:border-fk-negative'

// Min, max and shelf of one depot row; If-Match is the row version ("0" creates it).
export default function SettingsDialog({
  orgId,
  level,
  depotCode,
  onClose,
  onSaved,
  onReload,
}: Readonly<{
  orgId: number
  level: StockLevel
  depotCode: string
  onClose: () => void
  onSaved: () => void
  onReload: () => void
}>) {
  const { t } = useTranslation(['parts', 'errors'])
  const toast = useToast()
  const ids = { min: useId(), max: useId(), shelf: useId() }
  const [min, setMin] = useState(formatQty(level.minQty ?? '0'))
  const [max, setMax] = useState(level.maxQty ? formatQty(level.maxQty) : '')
  const [shelf, setShelf] = useState(level.shelf ?? '')
  const [maxError, setMaxError] = useState(false)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [conflict, setConflict] = useState(false)

  const minQty = parseQty(min)
  const maxQty = parseQty(max)

  async function save(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    if (minQty === null || maxQty === null) return
    const low = minQty || '0'
    if (maxQty && Number(maxQty) < Number(low)) {
      setMaxError(true)
      return
    }
    setMaxError(false)
    setBusy(true)
    try {
      await setPartDepotSettings(
        orgId,
        level.partId,
        level.depotId!,
        { minQty: low, maxQty: maxQty || null, shelf: shelf.trim() },
        level.version ?? 0,
      )
      toast.success(t('settings.saved', { code: depotCode }))
      onSaved()
    } catch (err) {
      if (err instanceof ApiError && err.status === 412) setConflict(true)
      else if (err instanceof ApiError && err.field === 'maxQty') setMaxError(true)
      else setError(errorText(t, err))
    } finally {
      setBusy(false)
    }
  }

  if (conflict) {
    return (
      <ConflictDialog
        title={t('settings.conflictTitle')}
        message={t('settings.conflict')}
        onReload={onReload}
      />
    )
  }

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent aria-describedby={undefined} className="max-w-md">
        <DialogHeader>
          <DialogTitle>{t('settings.title', { code: depotCode })}</DialogTitle>
        </DialogHeader>
        <form onSubmit={save} className="grid grid-cols-3 gap-3">
          <Field
            label={t('settings.minQty')}
            htmlFor={ids.min}
            error={minQty === null ? t('settings.invalidQty') : null}
          >
            <input
              id={ids.min}
              inputMode="decimal"
              value={min}
              onChange={(e) => setMin(e.target.value)}
              aria-invalid={minQty === null || undefined}
              className={`${inputCls} text-right`}
            />
          </Field>
          <Field
            label={t('settings.maxQty')}
            htmlFor={ids.max}
            error={
              maxQty === null
                ? t('settings.invalidQty')
                : maxError
                  ? t('settings.maxBelowMin')
                  : null
            }
          >
            <input
              id={ids.max}
              inputMode="decimal"
              value={max}
              onChange={(e) => setMax(e.target.value)}
              aria-invalid={maxQty === null || maxError || undefined}
              className={`${inputCls} text-right`}
            />
          </Field>
          <Field label={t('settings.shelf')} htmlFor={ids.shelf}>
            <input
              id={ids.shelf}
              value={shelf}
              maxLength={20}
              onChange={(e) => setShelf(e.target.value.toUpperCase())}
              className={inputCls}
            />
          </Field>
          {error && (
            <p role="alert" className="col-span-3 text-13 text-fk-negative">
              {error}
            </p>
          )}
          <DialogFooter className="col-span-3">
            <Button type="button" variant="outline" onClick={onClose} className="min-w-[88px]">
              {t('cancel')}
            </Button>
            <Button type="submit" disabled={busy} className="min-w-[88px]">
              {t('save')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
