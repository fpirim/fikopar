import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { ApiError } from '../../api/client'
import { updatePriceImport } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type { PriceImportMappingWrite, PriceImport, PriceImportTarget } from '../../api/types'
import ConflictDialog from '../../components/ConflictDialog'
import { Button } from '../../components/ui/button'
import { Panel } from '../../components/ui/panel'
import { cn } from '@/lib/utils'

const REQUIRED: PriceImportTarget[] = ['code', 'price']
const OPTIONAL: PriceImportTarget[] = [
  'description',
  'brand',
  'type',
  'manufacturerCode',
  'oem',
  'altCodes',
  'validFrom',
  'packQty',
  'vatPct',
]

type Columns = NonNullable<PriceImportMappingWrite['columns']>

function initial(job: PriceImport): Partial<Record<PriceImportTarget, string>> {
  const out: Partial<Record<PriceImportTarget, string>> = {}
  for (const [target, col] of Object.entries(job.mapping?.columns ?? {})) {
    const header = col.header ?? (col.index ? job.headers?.[col.index - 1] : undefined)
    if (header) out[target as PriceImportTarget] = header
  }
  return out
}

// Target → file column, with the first rows of the file; a fixed-width preset shows its positions.
export default function MappingStep({
  orgId,
  job,
  onSaved,
  onReload,
}: Readonly<{
  orgId: number
  job: PriceImport
  onSaved: (job: PriceImport) => void
  onReload: () => void
}>) {
  const { t } = useTranslation(['catalog', 'errors'])
  const [picked, setPicked] = useState(() => initial(job))
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [conflict, setConflict] = useState(false)
  const headers = job.headers ?? []
  const fixed = job.format === 'FIXED_WIDTH'
  const missing = REQUIRED.filter((r) => !picked[r])

  async function save() {
    const columns: Columns = {}
    for (const [target, header] of Object.entries(picked)) {
      if (header) columns[target as PriceImportTarget] = { header }
    }
    setBusy(true)
    setError('')
    try {
      onSaved(await updatePriceImport(orgId, job, { mapping: { columns } }))
    } catch (e) {
      if (e instanceof ApiError && e.status === 412) setConflict(true)
      else setError(errorText(t, e))
    } finally {
      setBusy(false)
    }
  }

  const row = (target: PriceImportTarget) => {
    const required = REQUIRED.includes(target)
    const unmapped = required && !picked[target]
    const label = t(`import.target.${target}`)
    const pos = job.mapping?.columns?.[target]
    return (
      <div key={target} className="flex items-center gap-3">
        <span className="w-[150px] shrink-0 text-sm">
          {label}
          {required && (
            <span className={cn('ml-1.5 text-xs', unmapped ? 'text-fk-negative' : 'text-fk-faint')}>
              {t('import.map.required')}
            </span>
          )}
        </span>
        {fixed ? (
          <span className="font-mono text-13 text-fk-muted">
            {pos?.start
              ? t('import.map.position', {
                  start: pos.start,
                  end: pos.start + (pos.length ?? 1) - 1,
                })
              : '—'}
          </span>
        ) : (
          <select
            aria-label={t('import.map.columnFor', { target: label })}
            aria-invalid={unmapped || undefined}
            value={picked[target] ?? ''}
            onChange={(e) => setPicked((p) => ({ ...p, [target]: e.target.value || undefined }))}
            className={cn(
              'h-[30px] w-[220px] rounded-md border border-fk-input bg-fk-surface px-2 text-13 outline-none focus:border-fk-accent',
              unmapped && 'border-fk-negative',
            )}
          >
            <option value="">{t('import.map.none')}</option>
            {headers.map((h) => (
              <option key={h} value={h}>
                {h}
              </option>
            ))}
          </select>
        )}
      </div>
    )
  }

  const th =
    'h-[30px] whitespace-nowrap border-b border-fk-border bg-fk-sunken px-2.5 text-left text-xs font-medium text-fk-secondary'
  return (
    <div className="flex items-start gap-4">
      <Panel className="w-[420px] shrink-0 p-4">
        <h2 className="text-15 font-semibold">{t('import.steps.map')}</h2>
        <p className="mb-3 mt-1 text-13 text-fk-muted">
          {fixed ? t('import.map.fixed') : t('import.map.intro')}
        </p>
        <div className="flex flex-col gap-2">{[...REQUIRED, ...OPTIONAL].map(row)}</div>
        {error && (
          <p role="alert" className="mt-3 text-sm text-fk-negative">
            {error}
          </p>
        )}
        {!fixed && (
          <div className="mt-4 flex justify-end">
            <Button onClick={save} disabled={busy || missing.length > 0} className="min-w-[140px]">
              {busy ? t('import.map.saving') : t('import.map.save')}
            </Button>
          </div>
        )}
      </Panel>
      <Panel className="min-w-0 flex-1 overflow-hidden">
        <h3 className="px-3.5 py-3 text-sm font-semibold">{t('import.map.sample')}</h3>
        <div className="overflow-x-auto">
          <table className="w-full border-separate border-spacing-0 text-13">
            <thead>
              <tr>
                {headers.map((h) => (
                  <th key={h} className={th}>
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {(job.sample ?? []).map((cells, i) => (
                <tr key={i}>
                  {headers.map((h, j) => (
                    <td
                      key={h}
                      className="h-[34px] whitespace-nowrap border-b border-fk-divider-soft px-2.5 font-mono"
                    >
                      {cells[j] ?? ''}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Panel>
      {conflict && (
        <ConflictDialog
          title={t('import.map.conflictTitle')}
          message={t('import.map.conflictMessage')}
          onReload={() => {
            setConflict(false)
            onReload()
          }}
        />
      )}
    </div>
  )
}
