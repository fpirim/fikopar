import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import type { PriceImport } from '../../api/types'
import { useDepot, withDepot } from '../../hooks/useDepot'
import { formatDateTime } from '../../lib/format'
import { Panel } from '../../components/ui/panel'

const KEYS = [
  'partsCreated',
  'pricesInserted',
  'partsUpdated',
  'partsDeactivated',
  'rowsSkipped',
] as const

export default function ResultStep({ job }: Readonly<{ job: PriceImport }>) {
  const { t } = useTranslation('catalog')
  const { depotId } = useDepot()
  return (
    <Panel className="flex w-[440px] flex-col gap-3 p-4">
      <h2 className="text-15 font-semibold">{t('import.steps.result')}</h2>
      <ul aria-label={t('import.steps.result')} className="flex flex-col gap-2">
        {KEYS.map((k) => (
          <li key={k} className="flex justify-between gap-3 text-sm">
            <span>{t(`import.result.${k}`)}</span>
            <span className="fk-num font-mono">{job.result?.[k] ?? 0}</span>
          </li>
        ))}
      </ul>
      {job.appliedAt && (
        <p className="text-13 text-fk-muted">
          {t('import.result.applied', {
            at: formatDateTime(job.appliedAt),
            by: job.appliedBy ?? '—',
          })}
        </p>
      )}
      <div className="flex gap-4 text-sm">
        <Link to={withDepot('/parts', depotId)}>{t('import.result.toParts')}</Link>
        <Link to={withDepot('/parts/import', depotId)}>{t('import.result.again')}</Link>
      </div>
    </Panel>
  )
}
