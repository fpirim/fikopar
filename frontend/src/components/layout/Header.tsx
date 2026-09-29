import { useState, useRef, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { LogOut, Building2, ChevronDown } from 'lucide-react'
import { useAuth } from '../../hooks/useAuth'
import { useOrganization } from '../../context/OrganizationContext'
import LanguageSwitcher from '../ui/LanguageSwitcher'

export default function Header() {
  const { t } = useTranslation(['nav', 'common', 'auth'])
  const { logout } = useAuth()
  const { user } = useAuth()
  const {
    organizations: orgs,
    selected: selectedOrg,
    selectedId,
    setSelectedId,
  } = useOrganization()
  const [open, setOpen] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)

  // Admins always get the picker (no auto-select); users with a single org do not need it.
  const isAdmin = user?.role === 'admin'
  const canOpen = orgs.length > 0 && (isAdmin || orgs.length > 1)

  useEffect(() => {
    function handleClick(e: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClick)
    return () => document.removeEventListener('mousedown', handleClick)
  }, [])

  return (
    <header className="flex items-center justify-between border-b bg-card px-6 py-4">
      <div className="relative" ref={dropdownRef}>
        <button
          onClick={() => canOpen && setOpen(!open)}
          className={`flex items-center gap-2 rounded-lg border bg-secondary px-3 py-1.5 text-sm font-medium transition-all ${canOpen ? 'hover:border-primary/50 cursor-pointer' : 'cursor-default'}`}
        >
          {selectedOrg?.logo_url ? (
            <img src={selectedOrg.logo_url} alt="" className="h-5 w-5 rounded object-cover" />
          ) : (
            <Building2 className="h-4 w-4 text-muted-foreground" />
          )}
          <span>{selectedOrg?.name || t('nav:selectOrganization')}</span>
          {canOpen && <ChevronDown className="h-3.5 w-3.5 text-muted-foreground" />}
        </button>

        {open && canOpen && (
          <div className="absolute left-0 top-full z-50 mt-1 w-56 rounded-lg border bg-card py-1 shadow-lg">
            {orgs.map((org) => (
              <button
                key={org.id}
                onClick={() => {
                  setSelectedId(org.id)
                  setOpen(false)
                }}
                className={`flex w-full items-center gap-2 px-3 py-2 text-left text-sm transition-all hover:bg-muted ${org.id === selectedId ? 'bg-primary/10 font-medium text-primary' : ''}`}
              >
                {org.logo_url ? (
                  <img src={org.logo_url} alt="" className="h-5 w-5 rounded object-cover" />
                ) : (
                  <Building2 className="h-4 w-4 text-muted-foreground shrink-0" />
                )}
                <span className="truncate">{org.name}</span>
              </button>
            ))}
          </div>
        )}
      </div>

      <div className="flex items-center gap-3">
        <LanguageSwitcher />
        <button
          onClick={logout}
          className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-muted-foreground transition-all duration-200 hover:bg-destructive/10 hover:text-destructive"
        >
          <LogOut className="h-4 w-4" />
          {t('common:logout')}
        </button>
      </div>
    </header>
  )
}
