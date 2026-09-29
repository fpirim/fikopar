import { useEffect, useId, useState } from 'react'
import { globalSearch } from '../api/endpoints'
import type { SearchResult } from '../api/types'
import { cn } from '@/lib/utils'

export type AccountHit = SearchResult['accounts'][number]

// Account combobox over the top-bar search endpoint (accounts group only).
export function AccountPicker({
  orgId,
  label,
  value,
  onPick,
  placeholder,
  className,
  inputClassName,
  autoFocus,
  hideLabel,
}: Readonly<{
  orgId: number
  label: string
  value?: AccountHit | null
  onPick: (hit: AccountHit) => void
  placeholder?: string
  className?: string
  inputClassName?: string
  autoFocus?: boolean
  hideLabel?: boolean
}>) {
  const listId = useId()
  const [q, setQ] = useState<string | null>(null)
  const [hits, setHits] = useState<AccountHit[]>([])
  const [active, setActive] = useState(0)
  const text = q ?? (value ? `${value.hesapNo} · ${value.hesapAdi}` : '')
  const open = q !== null && q.trim().length >= 2 && hits.length > 0

  useEffect(() => {
    if (q === null || q.trim().length < 2) return
    const ctrl = new AbortController()
    const timer = setTimeout(() => {
      globalSearch(orgId, q.trim(), ctrl.signal)
        .then((r) => {
          setHits(r.accounts)
          setActive(0)
        })
        .catch(() => undefined)
    }, 200)
    return () => {
      clearTimeout(timer)
      ctrl.abort()
    }
  }, [q, orgId])

  const pick = (h: AccountHit) => {
    setQ(null)
    setHits([])
    onPick(h)
  }

  return (
    <label className={cn('relative flex min-w-0 flex-col gap-1 text-xs text-fk-muted', className)}>
      {hideLabel ? <span className="sr-only">{label}</span> : label}
      <input
        role="combobox"
        aria-expanded={open}
        aria-controls={listId}
        aria-autocomplete="list"
        aria-activedescendant={open ? `${listId}-${active}` : undefined}
        value={text}
        placeholder={placeholder}
        autoFocus={autoFocus}
        onChange={(e) => setQ(e.target.value)}
        onBlur={() => setQ(null)}
        onKeyDown={(e) => {
          if (!open) return
          if (e.key === 'ArrowDown') {
            e.preventDefault()
            setActive((a) => (a + 1) % hits.length)
          } else if (e.key === 'ArrowUp') {
            e.preventDefault()
            setActive((a) => (a - 1 + hits.length) % hits.length)
          } else if (e.key === 'Enter') {
            e.preventDefault()
            pick(hits[active])
          } else if (e.key === 'Escape') {
            setQ(null)
          }
        }}
        className={cn(
          'h-10 w-full rounded-md border border-fk-input bg-fk-surface px-2.5 text-sm text-fk-text outline-none focus:border-fk-accent focus:ring-1 focus:ring-fk-accent',
          inputClassName,
        )}
      />
      {open && (
        <div
          id={listId}
          role="listbox"
          aria-label={label}
          className="absolute left-0 right-0 top-full z-50 mt-1 rounded-md border border-fk-border bg-fk-surface py-1 shadow-lg"
        >
          {hits.map((h, i) => (
            <div
              key={h.sicilId}
              id={`${listId}-${i}`}
              role="option"
              aria-selected={i === active}
              onMouseDown={(e) => e.preventDefault()}
              onClick={() => pick(h)}
              className={cn(
                'flex cursor-pointer items-center gap-3 px-3 py-1.5 text-13 text-fk-text hover:bg-fk-sunken',
                i === active && 'bg-fk-selected',
              )}
            >
              <span className="shrink-0 font-mono text-xs text-fk-accent">{h.hesapNo}</span>
              <span className="truncate">{h.hesapAdi}</span>
            </div>
          ))}
        </div>
      )}
    </label>
  )
}
