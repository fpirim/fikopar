import { useId, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { ApiError } from '../../api/client'
import { updateSicil } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type { Sicil, SicilUpdate } from '../../api/types'
import { useToast } from '../../context/ToastContext'
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
import { MoneyInput } from '../../components/ui/money-input'
import { TextField } from '../../components/ui/text-field'

const FIELDS = [
  ['hesap_adi', 'name', 'col-span-2'],
  ['sehir', 'city', ''],
  ['ilce', 'district', ''],
  ['v_dairesi', 'taxOffice', ''],
  ['v_no', 'taxNo', ''],
  ['gun', 'term', ''],
] as const

type Draft = Record<(typeof FIELDS)[number][0], string> & {
  credit_limit: string
  email: string
  contact: string
}

function draftOf(sicil: Sicil): Draft {
  return {
    ...(Object.fromEntries(FIELDS.map(([k]) => [k, String(sicil[k] ?? '')])) as Draft),
    credit_limit: sicil.credit_limit ?? '',
    email: sicil.emails?.[0] ?? '',
    contact: sicil.contacts?.[0]?.name ?? '',
  }
}

// The dialog edits the first email and the first contact's name; the rest of each list is kept.
function bodyOf(sicil: Sicil, d: Draft): SicilUpdate {
  const { credit_limit, email, contact, ...plain } = d
  const [first, ...contacts] = sicil.contacts ?? []
  const [, ...emails] = sicil.emails ?? []
  return {
    ...plain,
    credit_limit: credit_limit || null,
    emails: email.trim() ? [email.trim(), ...emails] : emails,
    contacts: contact.trim() ? [{ ...first, name: contact.trim() }, ...contacts] : contacts,
  }
}

export default function EditCardDialog({
  orgId,
  sicil,
  onClose,
  onSaved,
  onReload,
}: Readonly<{
  orgId: number
  sicil: Sicil
  onClose: () => void
  onSaved: (s: Sicil) => void
  onReload: () => void
}>) {
  const { t } = useTranslation(['ledger', 'common', 'errors'])
  const toast = useToast()
  const limitId = useId()
  const [draft, setDraft] = useState<Draft>(() => draftOf(sicil))
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [conflict, setConflict] = useState(false)
  const set = (k: keyof Draft) => (v: string) => setDraft((d) => ({ ...d, [k]: v }))

  async function save(e: React.FormEvent) {
    e.preventDefault()
    setSaving(true)
    setError('')
    try {
      const saved = await updateSicil(orgId, sicil, bodyOf(sicil, draft))
      toast.success(t('statement.saved'))
      onSaved(saved)
    } catch (err) {
      if (err instanceof ApiError && err.status === 412) setConflict(true)
      else setError(errorText(t, err))
    } finally {
      setSaving(false)
    }
  }

  if (conflict) {
    return (
      <ConflictDialog
        title={t('statement.conflictTitle')}
        message={t('statement.conflict')}
        onReload={onReload}
      />
    )
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
              onChange={set(k)}
              className={span}
              mono={k === 'v_no' || k === 'gun'}
            />
          ))}
          <Field label={t('statement.field.creditLimit')} htmlFor={limitId}>
            <MoneyInput
              id={limitId}
              value={draft.credit_limit}
              onChange={set('credit_limit')}
              className="text-sm"
            />
          </Field>
          <TextField
            label={t('statement.field.contact')}
            value={draft.contact}
            onChange={set('contact')}
          />
          <TextField
            label={t('statement.field.email')}
            value={draft.email}
            onChange={set('email')}
            className="col-span-2"
            type="email"
          />
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
