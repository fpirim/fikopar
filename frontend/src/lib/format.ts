import i18n from '../i18n'
import { DEFAULT_LOCALE } from '../i18n/config'

const LOCALE_MAP: Record<string, string> = { en: 'en-US', tr: 'tr-TR' }
const SIDES: Record<string, { dr: string; cr: string }> = {
  en: { dr: 'Dr', cr: 'Cr' },
  tr: { dr: 'B', cr: 'A' },
}

function lang(): string {
  return i18n.language?.split('-')[0] || DEFAULT_LOCALE
}

function intlLocale(): string {
  return LOCALE_MAP[lang()] || lang()
}

export function formatDate(iso?: string | null): string {
  if (!iso) return ''
  return new Date(iso).toLocaleDateString(intlLocale())
}

export function numberSeparators(): { group: string; decimal: string } {
  return lang() === 'tr' ? { group: '.', decimal: ',' } : { group: ',', decimal: '.' }
}

const MINUS = '−'

export function formatMoney(
  value: string | number | null | undefined,
  opts: { sign?: boolean; digits?: number } = {},
): string {
  if (value === null || value === undefined || value === '') return ''
  const n = typeof value === 'number' ? value : Number(value)
  if (Number.isNaN(n)) return ''
  const digits = opts.digits ?? 2
  const abs = Math.abs(n).toLocaleString(intlLocale(), {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits,
  })
  const zero = Number(Math.abs(n).toFixed(digits)) === 0
  if (zero) return abs
  if (n < 0) return MINUS + abs
  return opts.sign ? `+${abs}` : abs
}

export function formatBalance(value: string | number | null | undefined): string {
  if (value === null || value === undefined || value === '') return ''
  const n = Number(value)
  const abs = formatMoney(Math.abs(n))
  if (Number(Math.abs(n).toFixed(2)) === 0) return abs
  const side = SIDES[lang()] ?? SIDES.en
  return `${abs} ${n > 0 ? side.dr : side.cr}`
}

export function toMoney(n: number): string {
  const fixed = n.toFixed(2)
  return fixed === '-0.00' ? '0.00' : fixed
}

export function moneyToNumber(value: string | null | undefined): number {
  return value ? Number(value) : 0
}

const pad = (n: number) => String(n).padStart(2, '0')

export function formatDay(iso: string | null | undefined): string {
  if (!iso) return ''
  const [y, m, d] = iso.slice(0, 10).split('-')
  return `${d}.${m}.${y}`
}

export function parseDay(text: string): string | null {
  const t = text.trim()
  let y: number
  let m: number
  let d: number
  const iso = /^(\d{4})-(\d{2})-(\d{2})$/.exec(t)
  const dotted = /^(\d{1,2})[./](\d{1,2})[./](\d{4})$/.exec(t)
  if (iso) [y, m, d] = [Number(iso[1]), Number(iso[2]), Number(iso[3])]
  else if (dotted) [d, m, y] = [Number(dotted[1]), Number(dotted[2]), Number(dotted[3])]
  else return null
  const date = new Date(Date.UTC(y, m - 1, d))
  if (date.getUTCMonth() !== m - 1 || date.getUTCDate() !== d) return null
  return `${y}-${pad(m)}-${pad(d)}`
}

export function formatTime(ts: string | null | undefined): string {
  if (!ts) return ''
  const d = new Date(ts)
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

export function formatDateTime(ts: string | null | undefined): string {
  if (!ts) return ''
  const d = new Date(ts)
  return `${pad(d.getDate())}.${pad(d.getMonth() + 1)}.${d.getFullYear()} ${formatTime(ts)}`
}

export function todayIso(now = new Date()): string {
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
}

// '' for empty, null when not a number; otherwise a Money string.
export function parseAmount(text: string): string | null {
  const { group, decimal } = numberSeparators()
  const t = text.trim().split(group).join('').replace(decimal, '.')
  if (!t) return ''
  if (!/^-?\d+(\.\d*)?$/.test(t)) return null
  return toMoney(Number(t))
}
