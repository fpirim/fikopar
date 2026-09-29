import { useEffect, useState, useRef } from 'react'
import { NavLink } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { LayoutDashboard, User, Users, Building2, Wallet, Menu } from 'lucide-react'
import { useAuth } from '../../hooks/useAuth'
import { APP_VERSION } from '../../lib/version'

export default function Sidebar() {
  const { t } = useTranslation('nav')
  const { user } = useAuth()
  const [hover, setHover] = useState(false)
  const [pinned, setPinned] = useState<boolean>(() => localStorage.getItem('sidebarPinned') === '1')
  useEffect(() => {
    localStorage.setItem('sidebarPinned', pinned ? '1' : '0')
  }, [pinned])
  // Pinning below lg squeezes the content; the stored preference returns on wider screens.
  const [canPin, setCanPin] = useState<boolean>(
    () => typeof globalThis !== 'undefined' && globalThis.matchMedia('(min-width: 1024px)').matches,
  )
  useEffect(() => {
    const mq = globalThis.matchMedia('(min-width: 1024px)')
    const onChange = (e: MediaQueryListEvent) => setCanPin(e.matches)
    mq.addEventListener('change', onChange)
    return () => mq.removeEventListener('change', onChange)
  }, [])
  const effectivePinned = canPin && pinned
  const hideTimer = useRef<ReturnType<typeof setTimeout> | null>(null)

  const links = [
    { to: '/', label: t('dashboard'), icon: LayoutDashboard },
    { to: '/profile', label: t('profile'), icon: User },
    { to: '/sicil', label: t('sicil'), icon: Wallet },
    ...(user?.role === 'admin'
      ? [
          { to: '/admin/organizations', label: t('organizations'), icon: Building2 },
          { to: '/admin/users', label: t('users'), icon: Users },
        ]
      : []),
  ]

  const showOverlay = () => {
    if (hideTimer.current !== null) clearTimeout(hideTimer.current)
    setHover(true)
  }

  const hideOverlay = () => {
    hideTimer.current = setTimeout(() => setHover(false), 80)
  }

  function NavContent({ showText, onItemHover }: { showText: boolean; onItemHover?: () => void }) {
    return (
      <>
        <div className="flex items-center px-1 py-4">
          <button
            onClick={() => canPin && setPinned(!pinned)}
            disabled={!canPin}
            title={canPin ? undefined : t('screenTooNarrow')}
            className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg transition-all hover:bg-muted disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Menu
              className={`h-5 w-5 transition-all ${effectivePinned ? 'text-primary' : 'text-muted-foreground'}`}
            />
          </button>
          <div
            className={`ml-1 flex flex-col leading-none transition-all duration-200 ${showText ? 'opacity-100' : 'w-0 overflow-hidden opacity-0'}`}
          >
            <span className="text-xl font-bold text-primary">fikopar</span>
            <span className="mt-0.5 text-[10px] text-muted-foreground">{APP_VERSION}</span>
          </div>
        </div>
        <nav className="space-y-1 px-1">
          {links.map((link) => {
            const Icon = link.icon
            return (
              <NavLink
                key={link.to}
                to={link.to}
                onMouseEnter={onItemHover}
                className={({ isActive }) =>
                  `group relative flex items-center gap-3 rounded-lg px-2 py-2.5 transition-all duration-200 ${
                    isActive
                      ? 'bg-primary/10 text-primary'
                      : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                  }`
                }
              >
                {({ isActive }) => (
                  <>
                    {isActive && (
                      <span className="absolute left-0 top-1/2 h-5 w-1 -translate-y-1/2 rounded-r-full bg-primary transition-all duration-200" />
                    )}
                    <Icon className="h-5 w-5 shrink-0 transition-transform duration-200 group-hover:scale-110" />
                    <span
                      className={`whitespace-nowrap text-sm font-medium transition-all duration-200 ${showText ? 'opacity-100' : 'opacity-0 w-0 overflow-hidden'}`}
                    >
                      {link.label}
                    </span>
                  </>
                )}
              </NavLink>
            )
          })}
        </nav>
      </>
    )
  }

  return (
    <div className="relative flex flex-col">
      {/* Strip — always in flex flow (pushes content) */}
      <aside
        className={`flex flex-1 flex-col border-r bg-card transition-all duration-200 ${effectivePinned ? 'w-64' : 'w-12'}`}
      >
        <NavContent
          showText={effectivePinned}
          onItemHover={effectivePinned ? undefined : showOverlay}
        />
      </aside>

      {/* Overlay — fixed at left-0, animates width on hover */}
      {!effectivePinned && (
        <div
          onMouseEnter={showOverlay}
          onMouseLeave={hideOverlay}
          className={`fixed left-0 top-0 z-50 h-screen border-r bg-card shadow-2xl transition-all duration-200 overflow-hidden ${hover ? 'w-64' : 'w-0 border-0 shadow-none'}`}
        >
          <div className="w-64">
            <NavContent showText={true} onItemHover={showOverlay} />
          </div>
        </div>
      )}
    </div>
  )
}
