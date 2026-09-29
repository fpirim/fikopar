import { useTranslation } from 'react-i18next'
import { Button } from '../ui/button'

export default function GridFooter({
  sorted,
  filtered,
  range,
  total,
  onClear,
}: Readonly<{
  sorted: string[]
  filtered: string[]
  range: [number, number]
  total: number
  onClear: () => void
}>) {
  const { t } = useTranslation('grid')
  const parts = [
    sorted.length ? t('fk.sortedBy', { cols: sorted.join(', ') }) : t('fk.noSort'),
    filtered.length ? t('fk.filteredBy', { cols: filtered.join(', ') }) : null,
  ].filter(Boolean)
  const from = total ? range[0] + 1 : 0
  const to = total ? Math.min(range[1] + 1, total) : 0
  return (
    <div className="flex items-center gap-3 border-t border-fk-border px-3 py-2 text-13 text-fk-muted">
      <span className="min-w-0 flex-1 truncate">{parts.join(' · ')}</span>
      <Button variant="outline" size="sm" onClick={onClear} disabled={!filtered.length}>
        {t('fk.clearFilters')}
      </Button>
      <span className="min-w-[88px] whitespace-nowrap text-right font-mono">
        {t('fk.range', { from, to, total })}
      </span>
    </div>
  )
}
