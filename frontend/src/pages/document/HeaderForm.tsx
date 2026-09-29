import { useId } from 'react'
import { useTranslation } from 'react-i18next'
import type { Depot, DocumentSummary, DocumentType, Member } from '../../api/types'
import { AccountPicker } from '../../components/AccountPicker'
import { DateField } from '../../components/ui/date-field'
import { Field } from '../../components/ui/field'
import { Segmented } from '../../components/ui/segmented'
import { BILLS, DOC_TYPES, isPriceDiff, isWaybill, needsOriginal } from '../../lib/documents'
import { formatDay } from '../../lib/format'
import { cn } from '@/lib/utils'
import type { Header } from './model'

const control =
  'h-9 w-full min-w-0 rounded-md border border-fk-input bg-fk-surface px-2.5 text-sm text-fk-text outline-none focus:border-fk-accent focus:ring-1 focus:ring-fk-accent disabled:bg-fk-sunken disabled:text-fk-secondary'

export type LinkOption = Pick<DocumentSummary, 'id' | 'documentNo'> & { documentDate?: string }

export interface HeaderFormProps {
  orgId: number
  header: Header
  due: string | null
  editable: boolean
  // Type and card are fixed once lines come from an original or a waybill.
  locked: boolean
  depots: Depot[]
  members: Member[]
  links: LinkOption[]
  linkError: string | null
  onChange: (patch: Partial<Header>) => void
  onType: (type: DocumentType) => void
  onLink: (id: number | null) => void
}

export default function HeaderForm(p: Readonly<HeaderFormProps>) {
  const { t } = useTranslation('documents')
  const ids = { type: useId(), depot: useId(), rep: useId(), link: useId() }
  const h = p.header
  const pd = isPriceDiff(h.type)
  const bills = Boolean(BILLS[h.type])
  const original = needsOriginal(h.type)
  const off = !p.editable
  const linkId = original ? h.linkedDocumentId : h.waybillDocumentId

  return (
    <section
      aria-label={t('header')}
      className="grid grid-cols-6 gap-3 rounded-lg border border-fk-border bg-fk-surface px-4 py-3.5"
    >
      <Field label={t('typeLabel')} htmlFor={ids.type} className="col-span-2">
        <select
          id={ids.type}
          className={control}
          value={h.type}
          disabled={off || p.locked}
          onChange={(e) => p.onType(e.target.value as DocumentType)}
        >
          {DOC_TYPES.map((d) => (
            <option key={d} value={d}>
              {t(`type.${d}`)}
            </option>
          ))}
        </select>
      </Field>
      <Field label={t('depot')} htmlFor={ids.depot}>
        <select
          id={ids.depot}
          className={cn(control, 'font-mono')}
          value={h.depotId ?? ''}
          disabled={off || p.locked}
          onChange={(e) => p.onChange({ depotId: Number(e.target.value) })}
        >
          {p.depots.map((d) => (
            <option key={d.id} value={d.id}>
              {t('depotN', { code: d.code })}
            </option>
          ))}
        </select>
      </Field>
      {off ? (
        <>
          <ReadOnly label={t('date')} value={formatDay(h.documentDate)} />
          <ReadOnly label={t('shipDate')} value={formatDay(h.shipDate)} />
          <ReadOnly label={t('due')} value={formatDay(p.due)} />
        </>
      ) : (
        <>
          <DateField
            label={t('date')}
            value={h.documentDate}
            onChange={(v) => p.onChange({ documentDate: v })}
            className="min-w-0"
          />
          <DateField
            label={t('shipDate')}
            value={h.shipDate ?? ''}
            onChange={(v) => p.onChange({ shipDate: v })}
            className="min-w-0"
          />
          {isWaybill(h.type) ? (
            <ReadOnly label={t('due')} value="" />
          ) : (
            <DateField
              label={t('due')}
              value={p.due ?? ''}
              onChange={(v) => p.onChange({ dueDate: v, dueTouched: true })}
              className="min-w-0"
            />
          )}
        </>
      )}
      {off || p.locked ? (
        <ReadOnly
          label={t('account')}
          value={
            h.account ? `${h.account.hesapNo} · ${h.account.hesapAdi}`.replace(/^ · /, '') : ''
          }
          className={pd ? 'col-span-2' : 'col-span-3'}
          sans
        />
      ) : (
        <AccountPicker
          orgId={p.orgId}
          label={t('account')}
          placeholder={t('accountPlaceholder')}
          value={h.account}
          onPick={(a) => p.onChange({ account: a })}
          className={pd ? 'col-span-2' : 'col-span-3'}
          inputClassName="h-9"
        />
      )}
      {pd && (
        <div className="flex min-w-0 flex-col gap-1 text-xs text-fk-muted">
          <span id="doc-direction">{t('direction')}</span>
          <Segmented
            label={t('direction')}
            value={h.priceDirection ?? 'INCREASE'}
            onChange={(v) => !off && p.onChange({ priceDirection: v })}
            options={(['INCREASE', 'DECREASE'] as const).map((v) => ({
              value: v,
              label: t(`dir.${v}`),
              disabled: off,
            }))}
            className="w-full [&>button]:flex-1"
          />
        </div>
      )}
      <Field label={t('salesRep')} htmlFor={ids.rep}>
        <select
          id={ids.rep}
          className={control}
          value={h.salesRepUserId ?? ''}
          disabled={off}
          onChange={(e) =>
            p.onChange({ salesRepUserId: e.target.value ? Number(e.target.value) : null })
          }
        >
          <option value="">{t('noSalesRep')}</option>
          {p.members.map((m) => (
            <option key={m.userId} value={m.userId}>
              {m.name}
            </option>
          ))}
        </select>
      </Field>
      {original || bills ? (
        <Field
          label={t(original ? 'original' : 'waybillRef')}
          htmlFor={ids.link}
          className="col-span-2"
          error={p.linkError}
        >
          <select
            id={ids.link}
            className={cn(control, 'font-mono')}
            value={linkId ?? ''}
            disabled={off || p.locked || !h.account || !h.depotId}
            onChange={(e) => p.onLink(e.target.value ? Number(e.target.value) : null)}
          >
            <option value="">{t(original ? 'pickOriginal' : 'optional')}</option>
            {p.links.map((d) => (
              <option key={d.id} value={d.id}>
                {d.documentDate ? `${d.documentNo} · ${formatDay(d.documentDate)}` : d.documentNo}
              </option>
            ))}
          </select>
        </Field>
      ) : (
        <ReadOnly
          label={t('waybillRef')}
          value=""
          className="col-span-2"
          placeholder={t('notBilled')}
        />
      )}
    </section>
  )
}

function ReadOnly({
  label,
  value,
  className,
  placeholder,
  sans,
}: Readonly<{
  label: string
  value: string
  className?: string
  placeholder?: string
  sans?: boolean
}>) {
  return (
    <label className={cn('flex min-w-0 flex-col gap-1 text-xs text-fk-muted', className)}>
      {label}
      <input
        readOnly
        value={value}
        placeholder={placeholder}
        className={cn(control, 'bg-fk-sunken text-fk-secondary', !sans && 'font-mono')}
      />
    </label>
  )
}
