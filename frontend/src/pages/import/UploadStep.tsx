import { useId, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { ApiError } from '../../api/client'
import { createPriceImport, listCatalogs, listPriceImportPresets } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type { PriceImportMappingWrite } from '../../api/types'
import { useApi } from '../../hooks/useApi'
import { useDepot, withDepot } from '../../hooks/useDepot'
import { useIdempotencyKey } from '../../hooks/useIdempotencyKey'
import { todayIso } from '../../lib/format'
import { Button } from '../../components/ui/button'
import { DateField } from '../../components/ui/date-field'
import { Field, selectCls } from '../../components/ui/field'
import { Panel } from '../../components/ui/panel'

const MAX_BYTES = 10 * 1024 * 1024

export default function UploadStep({ orgId }: Readonly<{ orgId: number }>) {
  const { t } = useTranslation(['catalog', 'errors'])
  const navigate = useNavigate()
  const { depotId } = useDepot()
  const idem = useIdempotencyKey()
  const ids = { catalog: useId(), file: useId(), format: useId(), delim: useId(), dec: useId() }
  const catalogs = useApi(() => listCatalogs(orgId), [orgId]).data ?? []
  const presets = useApi(() => listPriceImportPresets(orgId), [orgId]).data?.presets ?? []
  const [catalogId, setCatalogId] = useState<number | null>(null)
  const [validFrom, setValidFrom] = useState(todayIso())
  const [file, setFile] = useState<File | null>(null)
  const [preset, setPreset] = useState<'' | 'LISAKT'>('')
  const [delimiter, setDelimiter] = useState('')
  const [decimal, setDecimal] = useState<',' | '.'>(',')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const catalog = catalogId ?? catalogs[0]?.id ?? null

  function mapping(): PriceImportMappingWrite | undefined {
    if (preset) return { preset }
    const m: PriceImportMappingWrite = {}
    if (delimiter) m.delimiter = delimiter
    if (decimal !== ',') m.decimalSeparator = decimal
    return Object.keys(m).length ? m : undefined
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    if (!file || !catalog) {
      setError(t('import.upload.noFile'))
      return
    }
    if (file.size > MAX_BYTES) {
      setError(t('import.upload.tooLarge'))
      return
    }
    const up = { file, catalogId: catalog, validFrom, mapping: mapping() }
    setBusy(true)
    setError('')
    try {
      const key = idem.keyFor({ ...up, file: [file.name, file.size, file.lastModified] })
      const job = await createPriceImport(orgId, up, key)
      navigate(withDepot(`/parts/import/${job.id}`, depotId))
    } catch (err) {
      setError(
        err instanceof ApiError && err.code === 'ERR_FILE_TOO_LARGE'
          ? t('import.upload.tooLarge')
          : errorText(t, err),
      )
    } finally {
      setBusy(false)
    }
  }

  return (
    <Panel className="w-[440px] shrink-0 p-4">
      <form onSubmit={submit} className="grid grid-cols-2 gap-3">
        <Field label={t('import.upload.catalog')} htmlFor={ids.catalog} className="col-span-2">
          <select
            id={ids.catalog}
            className={selectCls}
            value={catalog ?? ''}
            onChange={(e) => setCatalogId(Number(e.target.value))}
          >
            {catalogs.map((c) => (
              <option key={c.id} value={c.id}>
                {c.code} · {c.name}
              </option>
            ))}
          </select>
        </Field>
        <DateField
          label={t('import.upload.validFrom')}
          value={validFrom}
          onChange={setValidFrom}
          inputClassName="h-10"
        />
        <Field label={t('import.upload.format')} htmlFor={ids.format}>
          <select
            id={ids.format}
            className={selectCls}
            value={preset}
            onChange={(e) => setPreset(e.target.value as '' | 'LISAKT')}
          >
            <option value="">{t('import.upload.auto')}</option>
            {presets.map((p) => (
              <option key={p.code} value={p.code} title={p.notes}>
                {t('import.upload.lisakt', { defaultValue: p.name })}
              </option>
            ))}
          </select>
        </Field>
        <Field label={t('import.upload.file')} htmlFor={ids.file} className="col-span-2">
          <input
            id={ids.file}
            type="file"
            accept=".csv,.xlsx,.txt"
            onChange={(e) => {
              const f = e.target.files?.[0] ?? null
              setFile(f)
              // .txt is only LISAKT; the server reads it as CSV without the preset.
              if (f?.name.toLowerCase().endsWith('.txt')) setPreset('LISAKT')
            }}
            className="h-10 w-full rounded-md border border-fk-input bg-fk-surface px-2 py-[7px] text-sm text-fk-text file:mr-3 file:rounded file:border-0 file:bg-fk-chip-grey file:px-2 file:py-0.5 file:text-13"
          />
          <span className="text-fk-faint">{t('import.upload.fileHint')}</span>
        </Field>
        {!preset && (
          <>
            <Field label={t('import.upload.delimiter')} htmlFor={ids.delim}>
              <select
                id={ids.delim}
                className={selectCls}
                value={delimiter}
                onChange={(e) => setDelimiter(e.target.value)}
              >
                <option value="">{t('import.upload.delimiterAuto')}</option>
                <option value=";">;</option>
                <option value=",">,</option>
                <option value={'\t'}>{t('import.upload.tab')}</option>
              </select>
            </Field>
            <Field label={t('import.upload.decimal')} htmlFor={ids.dec}>
              <select
                id={ids.dec}
                className={selectCls}
                value={decimal}
                onChange={(e) => setDecimal(e.target.value as ',' | '.')}
              >
                <option value=",">1.234,56</option>
                <option value=".">1,234.56</option>
              </select>
            </Field>
          </>
        )}
        {error && (
          <p role="alert" className="col-span-2 text-sm text-fk-negative">
            {error}
          </p>
        )}
        <div className="col-span-2 flex justify-end">
          <Button type="submit" disabled={busy} className="min-w-[152px]">
            {busy ? t('import.upload.uploading') : t('import.upload.submit')}
          </Button>
        </div>
      </form>
    </Panel>
  )
}
