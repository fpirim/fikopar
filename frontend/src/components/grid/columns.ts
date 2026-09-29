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

// IBM Plex Mono advances 0.6em; grid text is 13.5px.
const MONO_CH = 8.1

// Width that shows `chars` monospace characters uncut: text, cell padding on both sides, border.
export function monoWidth(chars: number, dense = false): number {
  return Math.ceil(chars * MONO_CH) + (dense ? 16 : 20) + 2
}

// Width of the longest value, at least `min` characters.
export function fitMono(
  values: readonly (string | null | undefined)[],
  min: number,
  dense = false,
): number {
  return monoWidth(Math.max(min, ...values.map((v) => v?.length ?? 0)), dense)
}

// PREFIX-YEAR-DEPOT-COUNTER with a four-letter prefix: SIRI-2026-1-000001.
export const DOC_NO_CHARS = 18
