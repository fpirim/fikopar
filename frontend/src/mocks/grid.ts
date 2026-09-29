import type { FilterModel, GridRequest, SortModel } from '../api/types'

type Cond = FilterModel[string]
type Getter<T> = (row: T) => string | number | null | undefined

const fold = (s: string) => s.toLocaleLowerCase('tr-TR')

function matchText(v: string, c: Cond): boolean {
  const f = fold(String(c.filter ?? ''))
  const s = fold(v)
  switch (c.type) {
    case 'equals':
      return s === f
    case 'notEqual':
      return s !== f
    case 'startsWith':
      return s.startsWith(f)
    case 'endsWith':
      return s.endsWith(f)
    case 'notContains':
      return !s.includes(f)
    case 'blank':
      return s === ''
    case 'notBlank':
      return s !== ''
    default:
      return s.includes(f)
  }
}

function compare(n: number, c: Cond, from: number, to: number): boolean {
  switch (c.type) {
    case 'notEqual':
      return n !== from
    case 'lessThan':
      return n < from
    case 'lessThanOrEqual':
      return n <= from
    case 'greaterThan':
      return n > from
    case 'greaterThanOrEqual':
      return n >= from
    case 'inRange':
      return n >= from && n <= to
    default:
      return n === from
  }
}

const day = (s: string | null | undefined) => (s ? Date.parse(s.slice(0, 10)) : Number.NaN)

export function matches(value: string | number | null | undefined, c: Cond): boolean {
  if (c.type === 'blank') return value === null || value === undefined || value === ''
  if (c.type === 'notBlank') return !(value === null || value === undefined || value === '')
  if (value === null || value === undefined) return false
  if (c.filterType === 'number') {
    return compare(Number(value), c, Number(c.filter), Number(c.filterTo))
  }
  if (c.filterType === 'date') {
    return compare(day(String(value)), c, day(c.dateFrom), day(c.dateTo))
  }
  return matchText(String(value), c)
}

export function applyGrid<T>(
  all: T[],
  req: GridRequest,
  cols: Record<string, Getter<T>>,
  searchCols: string[] = [],
): { rows: T[]; filtered: T[] } {
  const filterModel = req.filterModel ?? {}
  let rows = all.filter((r) =>
    Object.entries(filterModel).every(([col, c]) => !cols[col] || matches(cols[col](r), c)),
  )
  const q = fold(req.search?.trim() ?? '')
  if (q) {
    rows = rows.filter((r) =>
      searchCols.some((col) => fold(String(cols[col](r) ?? '')).includes(q)),
    )
  }
  rows = sortRows(rows, req.sortModel ?? [], cols)
  const start = req.startRow ?? 0
  const end = req.endRow ?? rows.length
  return { rows: rows.slice(start, end), filtered: rows }
}

function sortRows<T>(rows: T[], sortModel: SortModel, cols: Record<string, Getter<T>>): T[] {
  const keys = sortModel.filter((s) => s.colId && cols[s.colId])
  if (!keys.length) return rows
  return [...rows].sort((a, b) => {
    for (const k of keys) {
      const get = cols[k.colId!]
      const av = get(a)
      const bv = get(b)
      const an = typeof av === 'number' || (av !== '' && !Number.isNaN(Number(av)))
      const cmp =
        an && typeof bv !== 'undefined'
          ? Number(av) - Number(bv)
          : String(av ?? '').localeCompare(String(bv ?? ''), 'tr')
      if (cmp !== 0) return k.sort === 'desc' ? -cmp : cmp
    }
    return 0
  })
}
