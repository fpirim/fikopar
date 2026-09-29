import { useEffect, useId, useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Search } from 'lucide-react'
import { getSicilSummary, globalSearch } from '../../api/endpoints'
import type { SearchResult } from '../../api/types'
import { useDepot, withDepot } from '../../hooks/useDepot'
import { useOrgId } from '../../hooks/useOrgId'
import { cn } from '@/lib/utils'

interface Hit {
  group: 'accounts' | 'documents' | 'parts'
  key: string
  code: string
  label: string
  open: () => Promise<string>
}

export default function GlobalSearch() {
  const { t } = useTranslation('shell')
  const orgId = useOrgId()
  const navigate = useNavigate()
  const { depotId } = useDepot()
  const listId = useId()
  const [q, setQ] = useState('')
  const [result, setResult] = useState<SearchResult | null>(null)
  const [open, setOpen] = useState(false)
  const [active, setActive] = useState(-1)
  const boxRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const text = q.trim()
    if (!orgId || text.length < 2) return
    const ctrl = new AbortController()
    const timer = setTimeout(() => {
      globalSearch(orgId, text, ctrl.signal)
        .then((r) => {
          setResult(r)
          setActive(-1)
          setOpen(true)
        })
        .catch(() => undefined)
    }, 250)
    return () => {
      clearTimeout(timer)
      ctrl.abort()
    }
  }, [q, orgId])

  const shown = q.trim().length >= 2 ? result : null

  const hits = useMemo<Hit[]>(() => {
    if (!shown || !orgId) return []
    return [
      ...shown.accounts.map<Hit>((a) => ({
        group: 'accounts',
        key: `a${a.sicilId}`,
        code: a.hesapNo,
        label: a.hesapAdi,
        open: async () => `/statements/${(await getSicilSummary(orgId, a.sicilId)).accountId}`,
      })),
      ...shown.documents.map<Hit>((d) => ({
        group: 'documents',
        key: `d${d.kind}${d.id}`,
        code: d.number,
        label: d.sicilName ?? '',
        open: async () => `/journal?q=${encodeURIComponent(d.number)}`,
      })),
      ...shown.parts.map<Hit>((p) => ({
        group: 'parts',
        key: `p${p.partId}`,
        code: p.stockNo,
        label: p.name,
        open: async () => '/parts',
      })),
    ]
  }, [shown, orgId])

  async function choose(hit: Hit) {
    setOpen(false)
    setQ('')
    navigate(withDepot(await hit.open(), depotId))
  }

  function onKeyDown(e: React.KeyboardEvent) {
    if (e.key === 'ArrowDown' && hits.length) {
      e.preventDefault()
      setOpen(true)
      setActive((a) => (a + 1) % hits.length)
    } else if (e.key === 'ArrowUp' && hits.length) {
      e.preventDefault()
      setActive((a) => (a <= 0 ? hits.length - 1 : a - 1))
    } else if (e.key === 'Enter' && open && hits[Math.max(active, 0)]) {
      e.preventDefault()
      void choose(hits[Math.max(active, 0)])
    } else if (e.key === 'Escape') {
      setOpen(false)
    }
  }

  const groups = (['accounts', 'documents', 'parts'] as const).filter((g) =>
    hits.some((h) => h.group === g),
  )

  return (
    <div
      ref={boxRef}
      className="relative w-[280px] shrink"
      onBlur={(e) => {
        if (!boxRef.current?.contains(e.relatedTarget as Node)) setOpen(false)
      }}
    >
      <label className="flex h-9 items-center gap-2 rounded-md border border-fk-input px-3 text-sm text-fk-muted focus-within:border-fk-accent focus-within:ring-1 focus-within:ring-fk-accent">
        <Search className="h-3.5 w-3.5 shrink-0" aria-hidden="true" />
        <input
          type="search"
          aria-label={t('search')}
          aria-expanded={open && shown !== null}
          aria-controls={listId}
          aria-activedescendant={active >= 0 ? `${listId}-${active}` : undefined}
          placeholder={t('searchPlaceholder')}
          value={q}
          onChange={(e) => setQ(e.target.value)}
          onFocus={() => shown && setOpen(true)}
          onKeyDown={onKeyDown}
          className="min-w-0 flex-1 bg-transparent text-fk-text outline-none placeholder:text-fk-muted"
        />
      </label>
      {open && shown && (
        <div
          id={listId}
          role="listbox"
          aria-label={t('searchResults')}
          className="absolute right-0 top-full z-50 mt-1 max-h-96 w-[360px] overflow-y-auto rounded-md border border-fk-border bg-fk-surface py-1 shadow-lg"
        >
          {hits.length === 0 && (
            <div className="px-3 py-2 text-13 text-fk-muted">{t('searchNone')}</div>
          )}
          {groups.map((g) => (
            <div key={g} role="group" aria-label={t(`hits.${g}`)}>
              <div className="px-3 pb-1 pt-2 text-2xs uppercase tracking-[0.08em] text-fk-faint">
                {t(`hits.${g}`)}
              </div>
              {hits.map((h, i) =>
                h.group === g ? (
                  <div
                    key={h.key}
                    id={`${listId}-${i}`}
                    role="option"
                    tabIndex={-1}
                    aria-selected={i === active}
                    onMouseDown={(e) => e.preventDefault()}
                    onClick={() => void choose(h)}
                    className={cn(
                      'flex cursor-pointer items-center gap-3 px-3 py-1.5 text-13 hover:bg-fk-sunken',
                      i === active && 'bg-fk-selected',
                    )}
                  >
                    <span className="shrink-0 font-mono text-xs text-fk-accent">{h.code}</span>
                    <span className="truncate">{h.label}</span>
                  </div>
                ) : null,
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
