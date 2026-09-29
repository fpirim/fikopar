import { useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { locate } from '../components/layout/nav'

export default function NotInBuildPage() {
  const { t } = useTranslation('shell')
  const where = locate(useLocation().pathname)
  return (
    <div className="flex flex-col gap-2">
      <h1 className="text-2xl font-semibold">{where ? t(`item.${where.item}`) : ''}</h1>
      <p className="text-sm text-fk-muted">{t('notInBuild')}</p>
    </div>
  )
}
