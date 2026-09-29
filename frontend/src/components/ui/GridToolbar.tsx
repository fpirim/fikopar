import { useTranslation } from 'react-i18next'
import { Plus, Printer, Search, Trash2, X } from 'lucide-react'

export default function GridToolbar({
  selectedCount,
  search,
  onSearchChange,
  onNew,
  onDelete,
  onPrint,
}: Readonly<{
  selectedCount: number
  search: string
  onSearchChange: (v: string) => void
  onNew: () => void
  onDelete: () => void
  onPrint: () => void
}>) {
  const { t } = useTranslation('common')
  return (
    <div className="flex flex-wrap items-center gap-2 shrink-0">
      <button
        onClick={onNew}
        className="group flex items-center gap-2 whitespace-nowrap rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-primary/30 active:scale-[0.98]"
      >
        <Plus className="h-4 w-4" />
        {t('new')}
      </button>
      <button
        onClick={() => selectedCount > 0 && onDelete()}
        disabled={selectedCount === 0}
        className="flex items-center gap-2 whitespace-nowrap rounded-lg border px-4 py-2 text-sm font-medium text-destructive transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-destructive/20 active:scale-[0.98] disabled:pointer-events-none disabled:opacity-40"
      >
        <Trash2 className="h-4 w-4" />
        {t('deleteSelected', { count: selectedCount })}
      </button>
      <button
        onClick={onPrint}
        disabled={selectedCount === 0}
        className="flex items-center gap-2 whitespace-nowrap rounded-lg border px-4 py-2 text-sm font-medium transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-primary/20 active:scale-[0.98] disabled:pointer-events-none disabled:opacity-40"
      >
        <Printer className="h-4 w-4" />
        {t('printSelected', { count: selectedCount })}
      </button>
      <div className="relative basis-full sm:basis-auto sm:ml-auto">
        <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <input
          type="text"
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
          placeholder={t('search')}
          className="w-full sm:w-56 rounded-lg border bg-background py-2 pl-9 pr-9 text-sm outline-none focus:ring-2 focus:ring-ring"
        />
        {search && (
          <button
            type="button"
            onClick={() => onSearchChange('')}
            aria-label={t('clear')}
            className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-0.5 text-muted-foreground transition-colors hover:text-foreground"
          >
            <X className="h-4 w-4" />
          </button>
        )}
      </div>
    </div>
  )
}
