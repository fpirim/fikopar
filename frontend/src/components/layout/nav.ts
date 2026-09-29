interface NavItem {
  tag: string
  key: string
  to: string
}

export interface NavSection {
  key: string
  items: NavItem[]
}

export const NAV: NavSection[] = [
  { key: 'overview', items: [{ tag: 'DSH', key: 'dashboard', to: '/' }] },
  { key: 'trade', items: [{ tag: 'INV', key: 'invoices', to: '/invoices' }] },
  {
    key: 'accounts',
    items: [
      { tag: 'BAL', key: 'balances', to: '/balances' },
      { tag: 'STM', key: 'statements', to: '/statements' },
      { tag: 'OPN', key: 'carryForward', to: '/carry-forward' },
    ],
  },
  {
    key: 'treasury',
    items: [
      { tag: 'RCP', key: 'receipts', to: '/receipts' },
      { tag: 'CHQ', key: 'cheques', to: '/cheques' },
      { tag: 'CSH', key: 'cashBook', to: '/cash-book' },
    ],
  },
  {
    key: 'stock',
    items: [
      { tag: 'PRT', key: 'parts', to: '/parts' },
      { tag: 'CNT', key: 'counts', to: '/stock-counts' },
    ],
  },
  {
    key: 'system',
    items: [
      { tag: 'JRN', key: 'journal', to: '/journal' },
      { tag: 'MIG', key: 'migration', to: '/migration' },
    ],
  },
]

const EXTRA: Record<string, string> = {
  '/profile': 'profile',
  '/sicil': 'sicil',
  '/admin/organizations': 'organizations',
  '/admin/users': 'users',
}

// Section and item keys for the breadcrumb.
export function locate(pathname: string): { section?: string; item: string } | null {
  if (EXTRA[pathname]) return { item: EXTRA[pathname] }
  for (const s of NAV) {
    for (const i of s.items) {
      const hit =
        i.to === '/' ? pathname === '/' : pathname === i.to || pathname.startsWith(`${i.to}/`)
      if (hit) return { section: s.key, item: i.key }
    }
  }
  return null
}
