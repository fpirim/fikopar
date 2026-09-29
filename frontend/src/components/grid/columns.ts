import type { ColDef } from 'ag-grid-community'
import ExprFloatingFilter, { type ExprFilterParams } from './ExprFloatingFilter'

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
  if (t === midnight.getTime()) return 0
  return t < midnight.getTime() ? -1 : 1
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
