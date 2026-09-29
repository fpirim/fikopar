import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { getPriceImport } from '../api/endpoints'
import type { PriceImport } from '../api/types'
import { useApi } from '../hooks/useApi'
import { useDepot, withDepot } from '../hooks/useDepot'
import { useOrgId } from '../hooks/useOrgId'
import { formatDay } from '../lib/format'
import { Panel } from '../components/ui/panel'
import SelectCompany from '../components/layout/SelectCompany'
import Steps, { type Step } from './import/Steps'
import UploadStep from './import/UploadStep'
import RecentImports from './import/RecentImports'
import MappingStep from './import/MappingStep'
import PreviewStep from './import/PreviewStep'
import ApplyStep from './import/ApplyStep'
import ResultStep from './import/ResultStep'

const POLL_MS = 800

export default function PriceImportPage() {
  const orgId = useOrgId()
  const { importId } = useParams()
  if (!orgId) return <SelectCompany />
  return importId ? (
    <Job key={importId} orgId={orgId} importId={Number(importId)} />
  ) : (
    <Upload orgId={orgId} />
  )
}

function Title() {
  const { t } = useTranslation('catalog')
  return <h1 className="text-2xl font-semibold">{t('import.title')}</h1>
}

function Upload({ orgId }: Readonly<{ orgId: number }>) {
  return (
    <div className="flex flex-col gap-3.5">
      <Title />
      <Steps current="upload" />
      <div className="flex items-start gap-4">
        <UploadStep orgId={orgId} />
        <RecentImports orgId={orgId} />
      </div>
    </div>
  )
}

function Job({ orgId, importId }: Readonly<{ orgId: number; importId: number }>) {
  const { t } = useTranslation('catalog')
  const { depotId } = useDepot()
  const api = useApi(() => getPriceImport(orgId, importId), [orgId, importId])
  const job = api.data
  const setJob = api.setData
  const [view, setView] = useState<'map' | 'preview' | 'apply'>('preview')

  const busy = job?.status === 'APPLYING' || job?.status === 'PARSING'
  useEffect(() => {
    if (!busy) return
    const timer = setInterval(() => {
      getPriceImport(orgId, importId)
        .then(setJob)
        .catch(() => undefined)
    }, POLL_MS)
    return () => clearInterval(timer)
  }, [busy, orgId, importId, setJob])

  if (!job) {
    return (
      <div className="flex flex-col gap-3.5">
        <Title />
        {api.error && (
          <p role="alert" className="text-sm text-fk-negative">
            {t(`errors:${api.error.code}`, { defaultValue: t('errors:ERR_UNKNOWN') })}
          </p>
        )}
      </div>
    )
  }

  const step: Step =
    job.status === 'NEEDS_MAPPING'
      ? 'map'
      : job.status === 'APPLIED'
        ? 'result'
        : job.status === 'APPLYING'
          ? 'apply'
          : job.status === 'READY'
            ? view
            : 'upload'
  const update = (j: PriceImport, next: Step = 'preview') => {
    setJob(j)
    setView(next === 'map' || next === 'apply' ? next : 'preview')
  }
  const final = ['FAILED', 'DISCARDED', 'EXPIRED'].includes(job.status)

  return (
    <div className="flex min-h-0 flex-1 flex-col gap-3.5">
      <div className="flex items-end gap-3">
        <div className="min-w-0 flex-1">
          <Title />
          <p className="mt-1 truncate font-mono text-13 text-fk-muted">
            {t('import.job', {
              file: job.fileName,
              catalog: job.catalogCode,
              date: formatDay(job.validFrom),
            })}
          </p>
        </div>
      </div>
      <Steps
        current={step}
        onGo={job.status === 'READY' ? (s) => setView(s as typeof view) : undefined}
      />
      {final && (
        <Panel className="flex items-center gap-3 px-4 py-3.5 text-sm">
          <span className="flex-1">
            {job.status === 'FAILED'
              ? t('import.failed', {
                  reason: t(`errors:${job.failureCode ?? 'ERR_IMPORT_FORMAT'}`),
                })
              : t(job.status === 'DISCARDED' ? 'import.discarded' : 'import.expired')}
          </span>
          <Link to={withDepot('/parts/import', depotId)} className="text-sm">
            {t('import.result.again')}
          </Link>
        </Panel>
      )}
      {job.status === 'PARSING' && <p className="text-sm text-fk-muted">{t('search.loading')}</p>}
      {step === 'map' && (
        <MappingStep
          key={job.version}
          orgId={orgId}
          job={job}
          onSaved={update}
          onReload={api.reload}
        />
      )}
      {step === 'preview' && (
        <PreviewStep
          orgId={orgId}
          job={job}
          onEditMapping={() => setView('map')}
          onContinue={() => setView('apply')}
          onDiscarded={setJob}
        />
      )}
      {step === 'apply' && (
        <ApplyStep orgId={orgId} job={job} onDone={update} onReload={api.reload} />
      )}
      {step === 'result' && <ResultStep job={job} />}
    </div>
  )
}
