import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../../hooks/useAuth'
import { Popover, PopoverContent, PopoverTrigger } from '../ui/popover'

const itemCls =
  'flex w-full items-center rounded px-2.5 py-1.5 text-left text-sm hover:bg-fk-sunken focus-visible:bg-fk-sunken focus-visible:outline-none'

export default function AccountMenu() {
  const { t } = useTranslation(['shell', 'common'])
  const { user, logout } = useAuth()
  const [open, setOpen] = useState(false)
  const initial = (user?.name || user?.email || '?').charAt(0).toUpperCase()
  const links = [
    ['/profile', 'profile'],
    ['/sicil', 'sicil'],
    ...(user?.role === 'admin'
      ? [
          ['/admin/organizations', 'organizations'],
          ['/admin/users', 'users'],
        ]
      : []),
  ]

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <button
          type="button"
          aria-label={t('accountMenu')}
          className="h-8 w-8 shrink-0 rounded-full bg-fk-accent text-13 font-medium text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-fk-accent focus-visible:ring-offset-2"
        >
          {initial}
        </button>
      </PopoverTrigger>
      <PopoverContent align="end" className="w-56 p-1">
        <div className="border-b border-fk-divider px-2.5 pb-2 pt-1.5">
          <div className="truncate text-sm font-medium">{user?.name}</div>
          <div className="truncate font-mono text-xs text-fk-muted">{user?.email}</div>
        </div>
        <div className="py-1">
          {links.map(([to, key]) => (
            <Link key={to} to={to} className={itemCls} onClick={() => setOpen(false)}>
              {t(`item.${key}`)}
            </Link>
          ))}
        </div>
        <div className="border-t border-fk-divider pt-1">
          <button type="button" className={itemCls} onClick={logout}>
            {t('common:logout')}
          </button>
        </div>
      </PopoverContent>
    </Popover>
  )
}
