import { forwardRef, type ForwardedRef } from 'react'
import { AgGridReact, type AgGridReactProps } from 'ag-grid-react'
import type { ColDef, RowHeightParams } from 'ag-grid-community'
import { useGridLocale } from '../../hooks/useGridLocale'
import ExprFloatingFilter, { type ExprFilterParams } from './ExprFloatingFilter'
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

const FILTER = {
  text: 'agTextColumnFilter',
  number: 'agNumberColumnFilter',
  date: 'agDateColumnFilter',
}

// Client-side date columns hold 'yyyy-mm-dd' strings.
const dateComparator = (midnight: Date, cell: string | null) => {
  if (!cell) return -1
  const [y, m, d] = cell.slice(0, 10).split('-').map(Number)
  const t = new Date(y, m - 1, d).getTime()
  return t === midnight.getTime() ? 0 : t < midnight.getTime() ? -1 : 1
}

type FilterProps = Pick<
  ColDef,
  | 'filter'
  | 'filterParams'
  | 'floatingFilter'
  | 'floatingFilterComponent'
  | 'floatingFilterComponentParams'
>

export function exprFilter(params: ExprFilterParams): FilterProps {
  const kind = params.kind === 'enum' ? 'text' : params.kind
  return {
    filter: FILTER[kind],
    filterParams: kind === 'date' ? { comparator: dateComparator } : undefined,
    floatingFilter: true,
    floatingFilterComponent: ExprFloatingFilter,
    floatingFilterComponentParams: { ...params, suppressFilterButton: true },
  }
}

export const numCol: Pick<ColDef, 'type' | 'cellClass'> = {
  type: 'rightAligned',
  cellClass: 'fk-num',
}

const DEFAULT_COL: ColDef = {
  sortable: true,
  resizable: true,
  suppressMenu: true,
  flex: 1,
  minWidth: 80,
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
