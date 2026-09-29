import type { FilterModel } from '../api/types'
import { formatDay, numberSeparators, parseDay } from './format'

export type FilterKind = 'text' | 'number' | 'date'
export type Condition = FilterModel[string]
type Op = NonNullable<Condition['type']>

const OPS: [string, Op][] = [
  ['>=', 'greaterThanOrEqual'],
  ['<=', 'lessThanOrEqual'],
  ['!=', 'notEqual'],
  ['≠', 'notEqual'],
  ['>', 'greaterThan'],
  ['<', 'lessThan'],
  ['=', 'equals'],
]
const SYMBOL: Partial<Record<Op, string>> = {
  greaterThanOrEqual: '>=',
  lessThanOrEqual: '<=',
  notEqual: '≠',
  greaterThan: '>',
  lessThan: '<',
}

function parseNumber(text: string): number | null {
  const { group, decimal } = numberSeparators()
  const t = text.trim().split(group).join('').replace(decimal, '.')
  if (!/^-?\d+(\.\d+)?$/.test(t)) return null
  return Number(t)
}

function formatNumber(n: number): string {
  const { group, decimal } = numberSeparators()
  const [int, frac] = String(Math.abs(n)).split('.')
  let grouped = ''
  for (let i = 0; i < int.length; i++) {
    if (i > 0 && (int.length - i) % 3 === 0) grouped += group
    grouped += int[i]
  }
  return `${n < 0 ? '-' : ''}${grouped}${frac ? decimal + frac : ''}`
}

function splitOp(text: string): [Op | null, string] {
  for (const [sym, op] of OPS) {
    if (text.startsWith(sym)) return [op, text.slice(sym.length).trim()]
  }
  return [null, text]
}

// null: no filter; undefined: not understood.
export function parseCondition(
  kind: FilterKind,
  raw: string,
  defaultOp: Op = 'contains',
): Condition | null | undefined {
  const text = raw.trim()
  if (!text) return null
  if (kind === 'text') {
    if (text.startsWith('=')) return { filterType: 'text', type: 'equals', filter: text.slice(1) }
    if (text.startsWith('!'))
      return { filterType: 'text', type: 'notContains', filter: text.slice(1) }
    return { filterType: 'text', type: defaultOp, filter: text }
  }
  const range = text.split('..')
  if (range.length === 2) {
    if (kind === 'number') {
      const [a, b] = range.map(parseNumber)
      if (a === null || b === null) return undefined
      return { filterType: 'number', type: 'inRange', filter: a, filterTo: b }
    }
    const [a, b] = range.map(parseDay)
    if (!a || !b) return undefined
    return { filterType: 'date', type: 'inRange', dateFrom: a, dateTo: b }
  }
  const [op, rest] = splitOp(text)
  if (kind === 'number') {
    const n = parseNumber(rest)
    if (n === null) return undefined
    return { filterType: 'number', type: op ?? 'equals', filter: n }
  }
  const d = parseDay(rest)
  if (!d) return undefined
  return { filterType: 'date', type: op ?? 'equals', dateFrom: d, dateTo: null }
}

export function describeCondition(
  c: Condition | null | undefined,
  defaultOp: Op = 'contains',
): string {
  if (!c) return ''
  if (c.filterType === 'text') {
    const v = String(c.filter ?? '')
    if (c.type === 'equals' && defaultOp !== 'equals') return `=${v}`
    if (c.type === 'notContains') return `!${v}`
    return v
  }
  const fmt = (v: unknown) =>
    c.filterType === 'number' ? formatNumber(Number(v)) : formatDay(String(v ?? '').slice(0, 10))
  const a = c.filterType === 'number' ? c.filter : c.dateFrom
  const b = c.filterType === 'number' ? c.filterTo : c.dateTo
  if (c.type === 'inRange') return `${fmt(a)}..${fmt(b)}`
  const sym = SYMBOL[c.type as Op]
  return sym ? `${sym} ${fmt(a)}` : fmt(a)
}
