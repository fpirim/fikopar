import { useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { SUPPORTED } from '../../i18n/config'
import { useAuth } from '../../hooks/useAuth'
import { useDepot } from '../../hooks/useDepot'
import { useDepots } from '../../hooks/useDepots'
import { Chip } from '../ui/chip'
import { Segmented } from '../ui/segmented'
import FindPart from '../catalog/FindPart'
import AccountMenu from './AccountMenu'
import GlobalSearch from './GlobalSearch'
import { locate } from './nav'

export default function Topbar() {
  const { t, i18n } = useTranslation('shell')
  const { pathname } = useLocation()
  const { user } = useAuth()
  const { depotId, setDepot } = useDepot()
  const depots = useDepots()
  const where = locate(pathname)
  const crumb = where
    ? [where.section && t(`section.${where.section}`), t(`item.${where.item}`)]
        .filter(Boolean)
        .join(' / ')
    : ''
  const demo = import.meta.env.VITE_DEMO === '1' || user?.email?.startsWith('demo@')
  const lang = i18n.language?.split('-')[0]

  return (
    <header className="flex h-16 shrink-0 items-center gap-3.5 border-b border-fk-border bg-fk-surface px-8">
      <div className="whitespace-nowrap text-sm text-fk-muted">{crumb}</div>
      <div className="flex-1" />
      <FindPart>
        <GlobalSearch />
      </FindPart>
      <Segmented
        label={t('depot')}
        size="sm"
        value={depotId}
        onChange={setDepot}
        options={[
          { value: null, label: t('allDepots') },
          ...depots.map((d) => ({ value: d.id!, label: t('depotN', { code: d.code }) })),
        ]}
      />
      {demo && (
        <Chip tone="amber" size="sm" className="h-[22px] px-2">
          {t('demo')}
        </Chip>
      )}
      <Segmented
        label={t('language')}
        size="sm"
        mono
        value={lang}
        onChange={(l) => void i18n.changeLanguage(l)}
        options={SUPPORTED.map((l) => ({ value: l.code, label: l.code.toUpperCase() }))}
      />
      <AccountMenu />
    </header>
  )
}
