import { useCallback, useEffect, useRef, useState, type ReactNode } from 'react'
import { useTranslation } from 'react-i18next'
import { Command as CommandPrimitive } from 'cmdk'
import { Search } from 'lucide-react'
import { searchCatalog } from '../../api/endpoints'
import type { CatalogHit, Depot } from '../../api/types'
import { formatDay, formatMoney, formatQty } from '@/lib/format'
import { cn } from '@/lib/utils'
import { Popover, PopoverAnchor, PopoverContent } from '../ui/popover'
import { highlight } from './highlight'

const PAGE = 20
const DEBOUNCE_MS = 200
const CODE_KINDS = ['OEM', 'ALT', 'MANUFACTURER', 'EQUIVALENT']

interface Result {
  query: string
  items: CatalogHit[]
  total: number
  capped: boolean
  date: string | null
  next: string | null
}

export interface CatalogSearchProps {
  orgId: number
  depots: Depot[]
  open: boolean
  onOpenChange: (open: boolean) => void
  query: string
  onQueryChange: (query: string) => void
  onPick: (hit: CatalogHit) => void
  // Price date (document date); defaults to today on the server.
  date?: string
  catalogId?: number | null
  inStockDepotId?: number | null
  // Positions the popup; the query input then sits at the top of the popup.
  anchor?: ReactNode
  align?: 'start' | 'end'
  pickHint?: string
  placeholder?: string
  inputClassName?: string
}

// OEM / alt. column: the code that matched, else the first OEM, ALT or manufacturer code.
function shownCode(hit: CatalogHit): string {
  if (hit.matchedCode && CODE_KINDS.includes(hit.matchKind)) return hit.matchedCode
  for (const kind of ['OEM', 'ALT', 'MANUFACTURER']) {
    const c = hit.codes?.find((x) => x.kind === kind)
    if (c) return c.code
  }
  return ''
}

function Marked({ text, query }: Readonly<{ text: string; query: string }>) {
  return (
    <>
      {highlight(text, query).map((s, i) =>
        s.hit ? (
          <mark key={i} className="rounded-sm bg-[#FCE9B2] text-inherit">
            {s.text}
          </mark>
        ) : (
          s.text
        ),
      )}
    </>
  )
}

function Kbd({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <kbd className="rounded border border-b-2 border-fk-input bg-fk-surface px-[5px] py-px font-mono text-2xs font-medium text-fk-secondary">
      {children}
    </kbd>
  )
}

// Part picker over searchCatalog: debounced, ranked, keyset-paged; ↑↓ Enter Esc.
export default function CatalogSearch({
  orgId,
  depots,
  open,
  onOpenChange,
  query,
  onQueryChange,
  onPick,
  date,
  catalogId,
  inStockDepotId,
  anchor,
  align = 'start',
  pickHint,
  placeholder,
  inputClassName,
}: Readonly<CatalogSearchProps>) {
  const { t } = useTranslation('catalog')
  const [result, setResult] = useState<Result | null>(null)
  const [loading, setLoading] = useState(false)
  const [more, setMore] = useState(false)
  const [selected, setSelected] = useState('')
  const anchorRef = useRef<HTMLDivElement>(null)
  const text = query.trim()
  const ready = text.length >= 2

  useEffect(() => {
    if (!open || !ready) return
    const ctrl = new AbortController()
    const timer = setTimeout(() => {
      setLoading(true)
      searchCatalog(orgId, { q: text, date, catalogId, inStockDepotId, limit: PAGE }, ctrl.signal)
        .then((r) => {
          setResult({
            query: text,
            items: r.items,
            total: r.total,
            capped: r.totalCapped,
            date: r.date ?? null,
            next: r.nextCursor ?? null,
          })
          setSelected(r.items[0] ? `p${r.items[0].partId}` : '')
        })
        .catch(() => undefined)
        .finally(() => {
          if (!ctrl.signal.aborted) setLoading(false)
        })
    }, DEBOUNCE_MS)
    return () => {
      clearTimeout(timer)
      ctrl.abort()
    }
  }, [open, ready, text, orgId, date, catalogId, inStockDepotId])

  const loadMore = useCallback(() => {
    if (!result?.next || more) return
    setMore(true)
    searchCatalog(orgId, {
      q: result.query,
      date,
      catalogId,
      inStockDepotId,
      limit: PAGE,
      cursor: result.next,
    })
      .then((r) =>
        setResult((cur) =>
          cur && cur.query === result.query
            ? { ...cur, items: [...cur.items, ...r.items], next: r.nextCursor ?? null }
            : cur,
        ),
      )
      .catch(() => undefined)
      .finally(() => setMore(false))
  }, [result, more, orgId, date, catalogId, inStockDepotId])

  const shown = ready && result?.query === text ? result : null
  const cols = `110px minmax(0,1fr) 128px 104px ${depots.map(() => '52px').join(' ')} 104px`

  const input = (
    <div
      className={cn(
        'flex items-center gap-2',
        anchor
          ? 'h-11 border-b border-fk-border px-3'
          : 'h-9 rounded-md border border-fk-input bg-fk-surface px-3 focus-within:border-fk-accent',
        inputClassName,
      )}
    >
      <Search className="h-3.5 w-3.5 shrink-0 text-fk-muted" aria-hidden="true" />
      <CommandPrimitive.Input
        aria-label={t('search.label')}
        placeholder={placeholder ?? t('search.placeholder')}
        value={query}
        onValueChange={onQueryChange}
        onKeyDown={(e) => {
          if (e.key === 'Escape') {
            e.preventDefault()
            onOpenChange(false)
          }
        }}
        className="min-w-0 flex-1 bg-transparent font-mono text-sm text-fk-text outline-none placeholder:font-sans placeholder:text-fk-muted"
      />
    </div>
  )

  const status = !ready
    ? t('search.minChars')
    : !shown
      ? t('search.loading')
      : shown.items.length === 0
        ? t('search.none')
        : null

  const popup = (
    <>
      <div
        role="row"
        className="grid h-[30px] items-center border-b border-fk-border bg-fk-sunken text-2xs font-medium text-fk-muted"
        style={{ gridTemplateColumns: cols }}
      >
        <span role="columnheader" className="px-2.5">
          {t('search.col.part')}
        </span>
        <span role="columnheader" className="px-2.5">
          {t('search.col.description')}
        </span>
        <span role="columnheader" className="px-2.5">
          {t('search.col.codes')}
        </span>
        <span role="columnheader" className="px-2.5">
          {t('search.col.brand')}
        </span>
        {depots.map((d) => (
          <span key={d.id} role="columnheader" className="px-2.5 text-right">
            {t('search.col.depot', { code: d.code })}
          </span>
        ))}
        <span role="columnheader" className="px-2.5 text-right">
          {t('search.col.price')}
        </span>
      </div>
      <CommandPrimitive.List
        aria-label={t('search.results')}
        className="max-h-[340px] overflow-y-auto overflow-x-hidden"
        onScroll={(e) => {
          const el = e.currentTarget
          if (el.scrollTop + el.clientHeight >= el.scrollHeight - 40) loadMore()
        }}
      >
        {status && <div className="px-3 py-3 text-13 text-fk-muted">{status}</div>}
        {shown?.items.map((hit) => (
          <CommandPrimitive.Item
            key={hit.partId}
            value={`p${hit.partId}`}
            data-stock-no={hit.stockNo}
            onSelect={() => onPick(hit)}
            className="grid h-[34px] cursor-default items-center border-b border-fk-divider-soft text-13 data-[selected=true]:bg-fk-selected"
            style={{ gridTemplateColumns: cols }}
          >
            <span role="cell" className="truncate px-2.5 font-mono">
              <Marked text={hit.stockNo} query={text} />
            </span>
            <span role="cell" className="truncate px-2.5">
              <Marked text={hit.name} query={text} />
            </span>
            <span role="cell" className="truncate px-2.5 font-mono text-fk-muted">
              <Marked text={shownCode(hit)} query={text} />
            </span>
            <span role="cell" className="truncate px-2.5 text-fk-muted">
              {hit.brand ?? ''}
            </span>
            {depots.map((d) => {
              const s = hit.stock.find((x) => x.depotId === d.id)
              const low = !s || Number(s.available) <= 0
              return (
                <span
                  key={d.id}
                  role="cell"
                  className={cn('fk-num px-2.5 text-right font-mono', low && 'text-fk-negative')}
                >
                  {formatQty(s?.available ?? 0)}
                </span>
              )
            })}
            <span role="cell" className="whitespace-nowrap px-2.5 text-right font-mono">
              {hit.listPrice ? formatMoney(hit.listPrice) : '—'}
            </span>
          </CommandPrimitive.Item>
        ))}
        {more && <div className="px-3 py-2 text-13 text-fk-muted">{t('search.more')}</div>}
      </CommandPrimitive.List>
      <div className="flex items-center justify-between gap-3 bg-fk-sunken px-2.5 py-2 text-xs text-fk-muted">
        <span className="whitespace-nowrap font-mono">
          {shown
            ? t('search.footer', {
                shown: shown.items.length,
                total: shown.capped ? '1000+' : shown.total,
                date: formatDay(shown.date),
              })
            : ''}
          {loading && shown ? ' …' : ''}
        </span>
        <span className="flex items-center gap-1.5 whitespace-nowrap">
          <Kbd>↑↓</Kbd> {t('search.select')} <Kbd>Enter</Kbd> {pickHint ?? t('search.add')}{' '}
          <Kbd>Esc</Kbd> {t('search.close')}
        </span>
      </div>
    </>
  )

  const command = (children: ReactNode) => (
    <CommandPrimitive
      shouldFilter={false}
      loop
      value={selected}
      onValueChange={setSelected}
      label={t('search.label')}
      className={anchor ? undefined : 'min-w-0'}
    >
      {children}
    </CommandPrimitive>
  )
  const content = (children: ReactNode) => (
    <PopoverContent
      align={align}
      sideOffset={6}
      onOpenAutoFocus={(e) => {
        if (!anchor) e.preventDefault()
      }}
      onInteractOutside={(e) => {
        if (!anchor && anchorRef.current?.contains(e.target as Node)) e.preventDefault()
      }}
      className="w-[820px] max-w-[calc(100vw-32px)] overflow-hidden rounded-lg border-fk-input bg-fk-surface p-0 text-fk-text shadow-[0_12px_32px_rgba(22,24,29,0.16)]"
    >
      {children}
    </PopoverContent>
  )

  // With an anchor the command (and its key handling) lives in the popup only.
  if (anchor) {
    return (
      <Popover open={open} onOpenChange={onOpenChange}>
        <PopoverAnchor asChild>
          <div ref={anchorRef} className="min-w-0 shrink">
            {anchor}
          </div>
        </PopoverAnchor>
        {content(
          command(
            <>
              {input}
              {popup}
            </>,
          ),
        )}
      </Popover>
    )
  }
  return command(
    <Popover open={open} onOpenChange={onOpenChange}>
      <PopoverAnchor asChild>
        <div ref={anchorRef}>{input}</div>
      </PopoverAnchor>
      {content(popup)}
    </Popover>,
  )
}
