import { useId, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { ApiError } from '../../api/client'
import { applyPriceImport, updatePriceImport } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type { ImportApply, PriceImport } from '../../api/types'
import { useIdempotencyKey } from '../../hooks/useIdempotencyKey'
import ConflictDialog from '../../components/ConflictDialog'
import { Button } from '../../components/ui/button'
import { Checkbox } from '../../components/ui/checkbox'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog'
import { KeyValue, Panel } from '../../components/ui/panel'
import { Segmented } from '../../components/ui/segmented'

type Missing = NonNullable<ImportApply['missing']>

// Options, a confirmation with the summary, then one apply with If-Match and an idempotency key.
export default function ApplyStep({
  orgId,
  job,
  onDone,
  onReload,
}: Readonly<{
  orgId: number
  job: PriceImport
  onDone: (job: PriceImport, next: 'apply' | 'preview') => void
  onReload: () => void
}>) {
  const { t } = useTranslation(['catalog', 'errors'])
  const idem = useIdempotencyKey()
  const ids = { skip: useId(), attrs: useId() }
  const c = job.counts
  const [skip, setSkip] = useState(false)
  const [attributes, setAttributes] = useState(true)
  const [missing, setMissing] = useState<Missing>('KEEP')
  const [confirm, setConfirm] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [stale, setStale] = useState(false)
  const [conflict, setConflict] = useState(false)
  const running = job.status === 'APPLYING'
  const blocked = c.errors > 0 && !skip

  async function apply() {
    const body: ImportApply = { skipErrorRows: skip, applyAttributes: attributes, missing }
    setBusy(true)
    setError('')
    setStale(false)
    try {
      const key = idem.keyFor({ id: job.id, version: job.version, ...body })
      onDone(await applyPriceImport(orgId, job, body, key), 'apply')
    } catch (e) {
      if (e instanceof ApiError && e.code === 'ERR_IMPORT_STALE') setStale(true)
      if (e instanceof ApiError && e.status === 412) setConflict(true)
      else setError(errorText(t, e))
    } finally {
      setBusy(false)
      setConfirm(false)
    }
  }

  async function refresh() {
    setBusy(true)
    try {
      onDone(await updatePriceImport(orgId, job, {}), 'preview')
    } catch (e) {
      setError(errorText(t, e))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="flex items-start gap-4">
      <Panel className="flex w-[360px] shrink-0 flex-col gap-2 p-4">
        <h2 className="mb-1 text-15 font-semibold">{t('import.applyStep.summary')}</h2>
        <KeyValue label={t('import.count.new')}>{c.new}</KeyValue>
        <KeyValue label={t('import.count.price')}>{c.price}</KeyValue>
        <KeyValue label={t('import.count.attributes')}>{c.attributes}</KeyValue>
        <KeyValue label={t('import.count.unchanged')}>{c.unchanged}</KeyValue>
        <KeyValue label={t('import.count.missing')}>{c.missing}</KeyValue>
        <KeyValue label={t('import.count.errors')} className={c.errors ? 'text-fk-negative' : ''}>
          {c.errors}
        </KeyValue>
        <KeyValue label={t('import.count.warnings')}>{c.warnings}</KeyValue>
      </Panel>
      <Panel className="flex min-w-0 flex-1 flex-col gap-3.5 p-4">
        {c.errors > 0 && (
          <div className="flex items-start gap-2.5">
            <Checkbox
              id={ids.skip}
              checked={skip}
              onCheckedChange={(v) => setSkip(v === true)}
              className="mt-0.5"
            />
            <div className="text-sm">
              <label htmlFor={ids.skip}>{t('import.applyStep.skipErrors')}</label>
              <span className="block text-13 text-fk-muted">
                {t('import.applyStep.skipErrorsHint', { count: c.errors })}
              </span>
            </div>
          </div>
        )}
        <div className="flex items-center gap-2.5">
          <Checkbox
            id={ids.attrs}
            checked={attributes}
            onCheckedChange={(v) => setAttributes(v === true)}
          />
          <label htmlFor={ids.attrs} className="text-sm">
            {t('import.applyStep.attributes')}
          </label>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-sm">{t('import.applyStep.missing')}</span>
          <Segmented
            label={t('import.applyStep.missing')}
            value={missing}
            onChange={setMissing}
            options={[
              { value: 'KEEP' as const, label: t('import.applyStep.keep') },
              { value: 'DEACTIVATE' as const, label: t('import.applyStep.deactivate') },
            ]}
          />
        </div>
        {(error || stale) && (
          <div
            role="alert"
            className="flex items-center gap-3 rounded-md bg-fk-chip-red px-3 py-2 text-13 text-fk-chip-red-fg"
          >
            <span className="flex-1">{error}</span>
            {stale && (
              <Button variant="outline" size="sm" onClick={refresh} disabled={busy}>
                {t('import.applyStep.refresh')}
              </Button>
            )}
          </div>
        )}
        <div className="flex justify-end">
          <Button
            onClick={() => setConfirm(true)}
            disabled={blocked || busy || running}
            className="min-w-[120px]"
          >
            {running ? t('import.applyStep.applying') : t('import.applyStep.apply')}
          </Button>
        </div>
      </Panel>
      <Dialog open={confirm} onOpenChange={(o) => !busy && setConfirm(o)}>
        <DialogContent role="alertdialog" className="max-w-md">
          <DialogHeader>
            <DialogTitle>{t('import.applyStep.confirmTitle')}</DialogTitle>
            <DialogDescription>
              {t('import.applyStep.confirm', {
                new: c.new,
                price: c.price,
                attributes: attributes ? c.attributes : 0,
                missing: c.missing,
                missingAction: t(
                  missing === 'KEEP'
                    ? 'import.applyStep.keepText'
                    : 'import.applyStep.deactivateText',
                ),
              })}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setConfirm(false)} disabled={busy}>
              {t('import.applyStep.cancel')}
            </Button>
            <Button onClick={apply} disabled={busy} className="min-w-[120px]">
              {busy ? t('import.applyStep.applying') : t('import.applyStep.apply')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
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
