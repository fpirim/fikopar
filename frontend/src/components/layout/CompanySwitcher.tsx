import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { ChevronDown } from 'lucide-react'
import { useOrganization } from '../../context/OrganizationContext'
import { Popover, PopoverContent, PopoverTrigger } from '../ui/popover'
import { cn } from '@/lib/utils'

export default function CompanySwitcher() {
  const { t } = useTranslation(['shell', 'nav'])
  const { organizations, selected, setSelectedId } = useOrganization()
  const [open, setOpen] = useState(false)
  const index = selected ? organizations.findIndex((o) => o.id === selected.id) + 1 : 0

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <button
          type="button"
          disabled={organizations.length === 0}
          className="mb-1 flex w-full items-center gap-2.5 rounded-md border border-fk-nav-border bg-fk-nav-raised px-2.5 py-2 text-left text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-fk-accent-light"
        >
          <span className="flex min-w-0 flex-1 flex-col gap-px">
            <span className="text-2xs text-fk-nav-muted">
              {selected ? t('companyOf', { index, count: organizations.length }) : t('company')}
            </span>
            <span className="truncate text-sm font-medium">
              {selected?.name ?? t('nav:selectOrganization')}
            </span>
          </span>
          <ChevronDown className="h-3.5 w-3.5 shrink-0 text-fk-nav-muted" aria-hidden="true" />
        </button>
      </PopoverTrigger>
      <PopoverContent align="start" className="w-[212px] p-1">
        {organizations.map((o) => (
          <button
            key={o.id}
            type="button"
            onClick={() => {
              setSelectedId(o.id)
              setOpen(false)
            }}
            className={cn(
              'flex w-full items-center rounded px-2.5 py-2 text-left text-sm hover:bg-fk-sunken focus-visible:bg-fk-sunken focus-visible:outline-none',
              o.id === selected?.id && 'font-medium text-fk-accent',
            )}
          >
            <span className="truncate">{o.name}</span>
          </button>
        ))}
      </PopoverContent>
    </Popover>
  )
}
