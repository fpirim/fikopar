import { forwardRef, type ForwardedRef } from 'react'
import { AgGridReact, type AgGridReactProps } from 'ag-grid-react'
import type { ColDef, RowHeightParams } from 'ag-grid-community'
import { useGridLocale } from '../../hooks/useGridLocale'
import { cn } from '@/lib/utils'

const svg = (body: string) =>
  `<svg width="12" height="12" viewBox="0 0 24 24" aria-hidden="true">${body}</svg>`
const ICONS = {
  sortAscending: svg(
    '<path d="M12 19V5M6 11l6-6 6 6" fill="none" stroke="currentColor" stroke-width="2.5"/>',
  ),
  sortDescending: svg(
    '<path d="M12 5v14M6 13l6 6 6-6" fill="none" stroke="currentColor" stroke-width="2.5"/>',
  ),
  filter: svg('<path d="M3 5h18l-7 8v6l-4-2v-4z" fill="currentColor"/>'),
}

const DEFAULT_COL: ColDef = {
  sortable: true,
  resizable: true,
  suppressMenu: true,
  flex: 1,
  minWidth: 40,
}

const rowHeight = (p: RowHeightParams) => (p.node.rowPinned ? 36 : 34)

export interface FkGridProps extends AgGridReactProps {
  className?: string
  height?: number | string
}

function FkGridInner({ className, height, ...props }: FkGridProps, ref: ForwardedRef<AgGridReact>) {
  const localeText = useGridLocale()
  return (
    <div className={cn('ag-theme-alpine fk-grid w-full', className)} style={{ height }}>
      <AgGridReact
        ref={ref}
        icons={ICONS}
        localeText={localeText}
        headerHeight={36}
        floatingFiltersHeight={34}
        getRowHeight={rowHeight}
        suppressDragLeaveHidesColumns
        suppressCellFocus={false}
        ensureDomOrder
        {...props}
        defaultColDef={{ ...DEFAULT_COL, ...props.defaultColDef }}
      />
    </div>
  )
}

export const FkGrid = forwardRef(FkGridInner)
