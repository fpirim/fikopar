import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { listPriceImports } from '../../api/endpoints'
import type { PriceImport } from '../../api/types'
import { useApi } from '../../hooks/useApi'
import { useDepot, withDepot } from '../../hooks/useDepot'
import { formatDateTime } from '../../lib/format'
import { Chip, type ChipTone } from '../../components/ui/chip'
import { Panel } from '../../components/ui/panel'

const STATUS_TONE: Record<PriceImport['status'], ChipTone> = {
  PARSING: 'grey',
  NEEDS_MAPPING: 'amber',
  READY: 'blue',
  FAILED: 'red',
  APPLYING: 'blue',
  APPLIED: 'green',
  DISCARDED: 'grey',
  EXPIRED: 'grey',
}

export default function RecentImports({ orgId }: Readonly<{ orgId: number }>) {
  const { t } = useTranslation('catalog')
  const { depotId } = useDepot()
  const jobs = useApi(() => listPriceImports(orgId), [orgId]).data?.items ?? []
  const th =
    'h-9 whitespace-nowrap border-b border-fk-border bg-fk-sunken px-2.5 text-xs font-medium text-fk-secondary'
  const td = 'h-[34px] whitespace-nowrap border-b border-fk-divider-soft px-2.5'
  return (
    <Panel
      role="region"
      aria-label={t('import.recent.title')}
      className="min-w-0 flex-1 overflow-hidden"
    >
      <h2 className="px-3.5 py-3 text-15 font-semibold">{t('import.recent.title')}</h2>
      {jobs.length === 0 ? (
        <p className="px-3.5 pb-3.5 text-13 text-fk-muted">{t('import.recent.none')}</p>
      ) : (
        <table className="w-full border-separate border-spacing-0 text-13">
          <thead>
            <tr>
              <th className={`${th} text-left`}>{t('import.recent.file')}</th>
              <th className={`${th} text-left`}>{t('import.recent.catalog')}</th>
              <th className={`${th} text-left`}>{t('import.recent.status')}</th>
              <th className={`${th} text-right`}>{t('import.recent.rows')}</th>
              <th className={`${th} text-left`}>{t('import.recent.created')}</th>
            </tr>
          </thead>
          <tbody>
            {jobs.map((j) => (
              <tr key={j.id}>
                <td className={`${td} max-w-[220px] truncate`}>
                  <Link to={withDepot(`/parts/import/${j.id}`, depotId)}>{j.fileName}</Link>
                </td>
                <td className={`${td} font-mono`}>{j.catalogCode}</td>
                <td className={td}>
                  <Chip size="sm" tone={STATUS_TONE[j.status]}>
                    {t(`import.status.${j.status}`)}
                  </Chip>
                </td>
                <td className={`${td} text-right font-mono`}>{j.counts.rows}</td>
                <td className={`${td} font-mono`}>{formatDateTime(j.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </Panel>
  )
}
