import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import type { ColumnApi } from 'ag-grid-community'
import { Button } from '../ui/button'
import { Popover, PopoverContent, PopoverTrigger } from '../ui/popover'

export default function ColumnsMenu({
  columnApi,
  columns,
}: Readonly<{
  columnApi: () => ColumnApi | null | undefined
  columns: { colId: string; label: string }[]
}>) {
  const { t } = useTranslation('grid')
  const [hidden, setHidden] = useState<Set<string>>(new Set())
  const toggle = (colId: string) => {
    const next = new Set(hidden)
    if (next.has(colId)) next.delete(colId)
    else next.add(colId)
    setHidden(next)
    columnApi()?.setColumnVisible(colId, !next.has(colId))
  }
  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button variant="outline" size="sm">
          {t('fk.columns')}
        </Button>
      </PopoverTrigger>
      <PopoverContent align="end" className="w-52 p-1.5">
        {columns.map((c) => (
          <label
            key={c.colId}
            className="flex cursor-pointer items-center gap-2 rounded px-2 py-1.5 text-sm hover:bg-fk-sunken"
          >
            <input
              type="checkbox"
              checked={!hidden.has(c.colId)}
              onChange={() => toggle(c.colId)}
              className="h-3.5 w-3.5 accent-[var(--fk-accent)]"
            />
            {c.label}
          </label>
        ))}
      </PopoverContent>
    </Popover>
  )
}
