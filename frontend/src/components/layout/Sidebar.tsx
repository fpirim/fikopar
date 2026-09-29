import { NavLink } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { getHealth } from '../../api/endpoints'
import { useApi } from '../../hooks/useApi'
import { useDepot, withDepot } from '../../hooks/useDepot'
import { APP_VERSION } from '../../lib/version'
import { BrandMark } from './BrandMark'
import CompanySwitcher from './CompanySwitcher'
import { NAV } from './nav'
import { cn } from '@/lib/utils'

export default function Sidebar() {
  const { t } = useTranslation('shell')
  const { depotId } = useDepot()
  const health = useApi(getHealth, []).data
  const [version, build] = (health?.version ?? APP_VERSION).split('+')

  return (
    <nav
      aria-label={t('mainNav')}
      className="flex h-full w-[240px] shrink-0 flex-col overflow-y-auto bg-fk-nav px-3.5 py-[18px] text-white"
    >
      <div className="flex items-center gap-2.5 px-2.5 pb-4 pt-0.5">
        <BrandMark />
        <span className="fk-wordmark text-[17px] leading-none text-white">FIKOPAR</span>
      </div>
      <CompanySwitcher />
      {NAV.map((s) => (
        <div key={s.key}>
          <div className="px-2.5 pb-1 pt-3 text-2xs uppercase tracking-[0.08em] text-fk-nav-muted">
            {t(`section.${s.key}`)}
          </div>
          {s.items.map((i) => (
            <NavLink
              key={i.key}
              to={withDepot(i.to, depotId)}
              end={i.to === '/'}
              className={({ isActive }) =>
                cn(
                  'flex h-8 items-center gap-2.5 rounded-md px-2.5 text-sm text-fk-nav-text hover:bg-fk-nav-hover hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-fk-accent-light',
                  isActive && 'bg-fk-nav-active text-white',
                )
              }
            >
              <span className="w-[30px] font-mono text-2xs text-fk-accent-light">{i.tag}</span>
              <span>{t(`item.${i.key}`)}</span>
            </NavLink>
          ))}
        </div>
      ))}
      <div className="flex-1" />
      <div className="flex justify-between px-2.5 py-2 font-mono text-2xs text-fk-nav-muted">
        <span>{version.startsWith('v') || version === 'dev' ? version : `v${version}`}</span>
        {build && <span>{build}</span>}
      </div>
    </nav>
  )
}
