import type { DocumentType, PriceDirection } from '../api/types'
import { numberSeparators } from './format'

export const DOC_TYPES: DocumentType[] = [
  'SALES_INVOICE_WITH_WAYBILL',
  'SALES_INVOICE',
  'SALES_WAYBILL',
  'SALES_RETURN',
  'SALES_RETURN_WAYBILL',
  'PURCHASE_INVOICE_WITH_WAYBILL',
  'PURCHASE_INVOICE',
  'PURCHASE_WAYBILL',
  'PURCHASE_RETURN',
  'PURCHASE_RETURN_WAYBILL',
  'PRICE_DIFFERENCE',
]

export const PREFIX: Record<DocumentType, string> = {
  SALES_INVOICE_WITH_WAYBILL: 'SF',
  SALES_INVOICE: 'SF',
  SALES_WAYBILL: 'SIR',
  SALES_RETURN: 'SI',
  SALES_RETURN_WAYBILL: 'SIRI',
  PURCHASE_INVOICE_WITH_WAYBILL: 'AF',
  PURCHASE_INVOICE: 'AF',
  PURCHASE_WAYBILL: 'AIR',
  PURCHASE_RETURN: 'AI',
  PURCHASE_RETURN_WAYBILL: 'AIRI',
  PRICE_DIFFERENCE: 'FF',
}

export type Side = 'SALES' | 'PURCHASE'

// Sales or purchase; a price difference takes it from its original.
export function sideOf(type: DocumentType, originType?: DocumentType | null): Side | null {
  if (type === 'PRICE_DIFFERENCE') return originType ? sideOf(originType) : null
  return type.startsWith('SALES') ? 'SALES' : 'PURCHASE'
}

export const isWaybill = (t: DocumentType) => t.endsWith('WAYBILL') && !t.includes('INVOICE')
export const isReturn = (t: DocumentType) => t.includes('RETURN')
export const isPriceDiff = (t: DocumentType) => t === 'PRICE_DIFFERENCE'

// The waybill type an invoice or return may bill.
export const BILLS: Partial<Record<DocumentType, DocumentType>> = {
  SALES_INVOICE: 'SALES_WAYBILL',
  PURCHASE_INVOICE: 'PURCHASE_WAYBILL',
  SALES_RETURN: 'SALES_RETURN_WAYBILL',
  PURCHASE_RETURN: 'PURCHASE_RETURN_WAYBILL',
}

export function billTypeFor(waybill: DocumentType): DocumentType | null {
  const hit = Object.entries(BILLS).find(([, w]) => w === waybill)
  return hit ? (hit[0] as DocumentType) : null
}

const SALES_ORIGINS: DocumentType[] = [
  'SALES_INVOICE_WITH_WAYBILL',
  'SALES_INVOICE',
  'SALES_WAYBILL',
]
const PURCHASE_ORIGINS: DocumentType[] = [
  'PURCHASE_INVOICE_WITH_WAYBILL',
  'PURCHASE_INVOICE',
  'PURCHASE_WAYBILL',
]
const INVOICES: DocumentType[] = [
  'SALES_INVOICE_WITH_WAYBILL',
  'SALES_INVOICE',
  'PURCHASE_INVOICE_WITH_WAYBILL',
  'PURCHASE_INVOICE',
]

// Types a return or price difference may link as its original.
export function originTypes(type: DocumentType): DocumentType[] {
  if (type === 'PRICE_DIFFERENCE') return INVOICES
  if (!isReturn(type)) return []
  return type.startsWith('SALES') ? SALES_ORIGINS : PURCHASE_ORIGINS
}

export const needsOriginal = (t: DocumentType) => originTypes(t).length > 0

export function returnTypeFor(original: DocumentType): DocumentType | null {
  if (SALES_ORIGINS.includes(original)) return 'SALES_RETURN'
  if (PURCHASE_ORIGINS.includes(original)) return 'PURCHASE_RETURN'
  return null
}

export const canPriceDiff = (t: DocumentType) => INVOICES.includes(t)

const OUT: DocumentType[] = [
  'SALES_INVOICE_WITH_WAYBILL',
  'SALES_INVOICE',
  'SALES_WAYBILL',
  'PURCHASE_RETURN',
  'PURCHASE_RETURN_WAYBILL',
]

// Stock the document moves on post; an invoice or return that bills a waybill moves none.
export function stockDirection(type: DocumentType, billsWaybill = false): 'IN' | 'OUT' | null {
  if (isPriceDiff(type) || (billsWaybill && BILLS[type])) return null
  return OUT.includes(type) ? 'OUT' : 'IN'
}

// Sign of the card posting: +1 the card is debited, −1 credited, 0 no entry (waybills).
export function cardSign(
  type: DocumentType,
  direction?: PriceDirection | null,
  originType?: DocumentType | null,
): -1 | 0 | 1 {
  if (isWaybill(type)) return 0
  if (type === 'PRICE_DIFFERENCE') {
    const side = sideOf(type, originType)
    if (!side || !direction) return 0
    const up = direction === 'INCREASE'
    return side === 'SALES' ? (up ? 1 : -1) : up ? -1 : 1
  }
  if (type.startsWith('SALES')) return isReturn(type) ? -1 : 1
  return isReturn(type) ? 1 : -1
}

// Posting checks the credit limit: sales invoices and increases on a sale.
export function creditChecked(
  type: DocumentType,
  direction?: PriceDirection | null,
  originType?: DocumentType | null,
): boolean {
  if (type === 'SALES_INVOICE' || type === 'SALES_INVOICE_WITH_WAYBILL') return true
  return (
    type === 'PRICE_DIFFERENCE' && direction === 'INCREASE' && sideOf(type, originType) === 'SALES'
  )
}

export function addDays(iso: string, days: number): string {
  const [y, m, d] = iso.split('-').map(Number)
  const t = new Date(Date.UTC(y, m - 1, d + days))
  return t.toISOString().slice(0, 10)
}

// ---- Amounts: exact decimals, half-up per line (ADR-0013 §7) ----

const TEN = 10n

function scaled(value: string | number | null | undefined, scale: number): bigint {
  const text = String(value ?? '0').trim() || '0'
  const neg = text.startsWith('-')
  const [int, frac = ''] = text.replace(/^[-+]/, '').split('.')
  const digits = `${int || '0'}${frac.slice(0, scale).padEnd(scale, '0')}`
  const n = BigInt(digits)
  return neg ? -n : n
}

// num / den rounded half away from zero.
function divRound(num: bigint, den: bigint): bigint {
  const neg = num < 0n !== den < 0n
  const a = num < 0n ? -num : num
  const b = den < 0n ? -den : den
  const q = (a * 2n + b) / (2n * b)
  return neg ? -q : q
}

const pow = (n: number) => TEN ** BigInt(n)

export function centsToMoney(c: bigint): string {
  const neg = c < 0n
  const a = neg ? -c : c
  const s = a.toString().padStart(3, '0')
  return `${neg ? '-' : ''}${s.slice(0, -2)}.${s.slice(-2)}`
}

export interface LineInput {
  qty: string
  unitPrice: string
  discount1Pct?: string | null
  discount2Pct?: string | null
  vatPct: string
}

export interface LineAmounts {
  grossAmount: string
  discountAmount: string
  netAmount: string
  vatAmount: string
  lineTotal: string
}

export function lineAmounts(l: LineInput): LineAmounts {
  const qty = scaled(l.qty, 4)
  const price = scaled(l.unitPrice, 4)
  const k1 = scaled(100, 4) - scaled(l.discount1Pct, 4)
  const k2 = scaled(100, 4) - scaled(l.discount2Pct, 4)
  const gross = divRound(qty * price, pow(6))
  const net = divRound(qty * price * k1 * k2, pow(18))
  const vat = divRound(net * scaled(l.vatPct, 4), pow(6))
  return {
    grossAmount: centsToMoney(gross),
    discountAmount: centsToMoney(gross - net),
    netAmount: centsToMoney(net),
    vatAmount: centsToMoney(vat),
    lineTotal: centsToMoney(net + vat),
  }
}

export interface Totals {
  grossTotal: string
  discountTotal: string
  netTotal: string
  vatTotal: string
  grandTotal: string
  vatBreakdown: { vatPct: string; base: string; vat: string }[]
}

const pctKey = (v: string) => {
  const n = scaled(v, 4)
  const s = n.toString().padStart(5, '0')
  const frac = s.slice(-4).replace(/0+$/, '')
  return frac ? `${s.slice(0, -4)}.${frac}` : s.slice(0, -4)
}

export function documentTotals(lines: (LineAmounts & { vatPct: string })[]): Totals {
  const sum = (k: keyof LineAmounts) => lines.reduce((s, l) => s + scaled(l[k], 2), 0n)
  const rates = new Map<string, { base: bigint; vat: bigint }>()
  for (const l of lines) {
    const key = pctKey(l.vatPct)
    const r = rates.get(key) ?? { base: 0n, vat: 0n }
    r.base += scaled(l.netAmount, 2)
    r.vat += scaled(l.vatAmount, 2)
    rates.set(key, r)
  }
  const net = sum('netAmount')
  const vat = sum('vatAmount')
  return {
    grossTotal: centsToMoney(sum('grossAmount')),
    discountTotal: centsToMoney(sum('discountAmount')),
    netTotal: centsToMoney(net),
    vatTotal: centsToMoney(vat),
    grandTotal: centsToMoney(net + vat),
    vatBreakdown: [...rates.entries()]
      .sort((a, b) => Number(b[0]) - Number(a[0]))
      .map(([vatPct, r]) => ({ vatPct, base: centsToMoney(r.base), vat: centsToMoney(r.vat) })),
  }
}

const esc = (s: string) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')

// Typed number in the UI language (groups of three, the language's decimal separator):
// '' empty, null not a number of 0 or more with at most `decimals` decimals, else '1234.5'.
export function parseDecimal(text: string, decimals: number, opts: { max?: number } = {}) {
  const t = text.trim()
  if (!t) return ''
  const { group, decimal } = numberSeparators()
  const g = esc(group)
  const shape = new RegExp(
    `^(\\d{1,3}(${g}\\d{3})+|\\d{1,14})(${esc(decimal)}\\d{1,${decimals}})?$`,
  )
  if (!shape.test(t)) return null
  const plain = t.split(group).join('').replace(decimal, '.')
  if (plain.split('.')[0].length > 14) return null
  const out = String(Number(plain))
  if (opts.max !== undefined && Number(out) > opts.max) return null
  return out
}

// Value for an input in the UI language: '1234.5' → '1234,5' in Turkish.
export function toInput(value: string | null | undefined): string {
  if (value === null || value === undefined || value === '') return ''
  const n = String(Number(value))
  return n.replace('.', numberSeparators().decimal)
}
