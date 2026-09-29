import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { ApiError } from '../../api/client'
import { updateSicil } from '../../api/endpoints'
import type { Sicil } from '../../api/types'
import { useToast } from '../../context/ToastContext'
import { Button } from '../../components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog'
import { TextField } from '../../components/ui/text-field'

const FIELDS = [
  ['hesap_adi', 'name', 'col-span-2'],
  ['sehir', 'city', ''],
  ['ilce', 'district', ''],
  ['v_dairesi', 'taxOffice', ''],
  ['v_no', 'taxNo', ''],
  ['gun', 'term', ''],
] as const

type Draft = Record<(typeof FIELDS)[number][0], string>

export default function EditCardDialog({
  orgId,
  sicil,
  onClose,
  onSaved,
}: Readonly<{ orgId: number; sicil: Sicil; onClose: () => void; onSaved: (s: Sicil) => void }>) {
  const { t } = useTranslation(['ledger', 'common', 'errors'])
  const toast = useToast()
  const [draft, setDraft] = useState<Draft>(
    () => Object.fromEntries(FIELDS.map(([k]) => [k, String(sicil[k] ?? '')])) as Draft,
  )
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  async function save(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      const saved = await updateSicil(orgId, sicil.id, draft)
      toast.success(t('statement.saved'))
      onSaved(saved)
    } catch (err) {
      setError(t(`errors:${err instanceof ApiError ? err.code : 'ERR_UNKNOWN'}`))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent aria-describedby={undefined}>
        <DialogHeader>
          <DialogTitle>{t('statement.editTitle')}</DialogTitle>
        </DialogHeader>
        <form onSubmit={save} className="grid grid-cols-2 gap-3">
          {FIELDS.map(([k, label, span]) => (
            <TextField
              key={k}
              label={t(`statement.field.${label}`)}
              value={draft[k]}
              onChange={(v) => setDraft((d) => ({ ...d, [k]: v }))}
              className={span}
              mono={k === 'v_no' || k === 'gun'}
            />
          ))}
          {error && (
            <p role="alert" className="col-span-2 text-13 text-fk-negative">
              {error}
            </p>
          )}
          <DialogFooter className="col-span-2 mt-1">
            <Button type="button" variant="outline" onClick={onClose}>
              {t('common:cancel')}
            </Button>
            <Button type="submit" disabled={saving} className="min-w-[88px]">
              {t('statement.save')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
