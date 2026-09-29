import type {
  CatalogHit,
  Document,
  DocumentEffects,
  DocumentLine,
  DocumentLineWrite,
  DocumentType,
  DocumentWrite,
  PriceDirection,
  SicilSummary,
} from '../../api/types'
import type { AccountHit } from '../../components/AccountPicker'
import {
  addDays,
  BILLS,
  cardSign,
  documentTotals,
  isPriceDiff,
  isWaybill,
  lineAmounts,
  needsOriginal,
  stockDirection,
  type LineAmounts,
  type Totals,
} from '../../lib/documents'

export interface Header {
  type: DocumentType
  depotId: number | null
  documentDate: string
  shipDate: string | null
  dueDate: string | null
  // The user typed a due date; a new date or card no longer moves it.
  dueTouched: boolean
  // A loaded draft's date and due: a due that was the card's default keeps following the date.
  loadedDue?: { date: string; due: string | null }
  account: AccountHit | null
  // Starts as the card's salesperson; null when the card has none.
  salesRepUserId: number | null
  linkedDocumentId: number | null
  priceDirection: PriceDirection | null
  waybillDocumentId: number | null
  description: string
}

interface LineStock {
  depotId: number
  onHand: string
  available: string
}

export interface EditLine {
  key: string
  partId: number | null
  stockNo: string
  name: string
  description: string
  qty: string
  unitPrice: string
  discount1Pct: string
  discount2Pct: string
  vatPct: string
  originLineId: number | null
  billedLineId: number | null
  stock: LineStock[] | null
  // What this draft holds of the line (server, after a save).
  reservedQty: string
}

let seq = 0
export const newKey = () => `n${++seq}`

export function blankLine(defaults: Partial<EditLine> = {}): EditLine {
  return {
    key: newKey(),
    partId: null,
    stockNo: '',
    name: '',
    description: '',
    qty: '',
    unitPrice: '',
    discount1Pct: '0',
    discount2Pct: '0',
    vatPct: '20',
    originLineId: null,
    billedLineId: null,
    stock: null,
    reservedQty: '0',
    ...defaults,
  }
}

export function blankHeader(type: DocumentType, depotId: number | null, today: string): Header {
  return {
    type,
    depotId,
    documentDate: today,
    shipDate: isPriceDiff(type) ? null : today,
    dueDate: isWaybill(type) ? null : today,
    dueTouched: false,
    account: null,
    salesRepUserId: null,
    linkedDocumentId: null,
    priceDirection: isPriceDiff(type) ? 'INCREASE' : null,
    waybillDocumentId: null,
    description: '',
  }
}

export function headerOf(d: Document, account: AccountHit | null): Header {
  return {
    type: d.type,
    depotId: d.depotId,
    documentDate: d.documentDate,
    shipDate: d.shipDate ?? null,
    dueDate: d.dueDate ?? null,
    dueTouched: true,
    loadedDue: d.status === 'DRAFT' ? { date: d.documentDate, due: d.dueDate ?? null } : undefined,
    account,
    salesRepUserId: d.salesRepUserId ?? null,
    linkedDocumentId: d.linkedDocumentId ?? null,
    priceDirection: d.priceDirection ?? null,
    waybillDocumentId: d.waybillDocumentId ?? null,
    description: d.description ?? '',
  }
}

export function lineOf(l: DocumentLine): EditLine {
  return {
    key: `l${l.id}`,
    partId: l.partId ?? null,
    stockNo: l.stockNo ?? '',
    name: l.name ?? '',
    description: l.description ?? '',
    qty: l.qty,
    unitPrice: l.unitPrice,
    discount1Pct: l.discount1Pct ?? '0',
    discount2Pct: l.discount2Pct ?? '0',
    vatPct: l.vatPct ?? '0',
    originLineId: l.originLineId ?? null,
    billedLineId: l.billedLineId ?? null,
    stock: l.stockByDepot
      ? l.stockByDepot.map((s) => ({
          depotId: s.depotId,
          onHand: s.onHand,
          available: s.available,
        }))
      : null,
    reservedQty: l.reservedQty ?? '0',
  }
}

// A picked part fills the line: list price on the document date, the part's VAT, the card's discounts.
export function withPart(
  line: EditLine,
  hit: CatalogHit,
  card: Pick<SicilSummary, 'discount1Pct' | 'discount2Pct'> | null | undefined,
): EditLine {
  const same = line.partId === hit.partId
  return {
    ...line,
    partId: hit.partId,
    stockNo: hit.stockNo,
    name: hit.name,
    qty: line.qty || '1',
    unitPrice: same && line.unitPrice ? line.unitPrice : (hit.listPrice ?? '0'),
    discount1Pct: same ? line.discount1Pct : (card?.discount1Pct ?? '0'),
    discount2Pct: same ? line.discount2Pct : (card?.discount2Pct ?? '0'),
    vatPct: hit.vatPct ?? line.vatPct,
    stock: hit.stock.map((s) => ({ depotId: s.depotId, onHand: s.onHand, available: s.available })),
    reservedQty: same ? line.reservedQty : '0',
  }
}

// Lines the server takes: a part (or, on a price difference, an amount).
export function isFilled(l: EditLine, type: DocumentType): boolean {
  if (isPriceDiff(type))
    return l.unitPrice !== '' && (l.originLineId !== null || l.description.trim() !== '')
  return l.partId !== null && l.qty !== ''
}

export function amountsOf(l: EditLine, type: DocumentType): LineAmounts {
  const pd = isPriceDiff(type)
  return lineAmounts({
    qty: pd ? '1' : l.qty || '0',
    unitPrice: l.unitPrice || '0',
    discount1Pct: pd ? '0' : l.discount1Pct,
    discount2Pct: pd ? '0' : l.discount2Pct,
    vatPct: l.vatPct || '0',
  })
}

export function totalsOf(lines: EditLine[], type: DocumentType): Totals {
  return documentTotals(
    lines
      .filter((l) => isFilled(l, type))
      .map((l) => ({ ...amountsOf(l, type), vatPct: l.vatPct })),
  )
}

export function toWrite(h: Header, lines: EditLine[]): DocumentWrite {
  const pd = isPriceDiff(h.type)
  const filled = lines.filter((l) => isFilled(l, h.type))
  return {
    type: h.type,
    depotId: h.depotId!,
    sicilId: h.account!.sicilId,
    documentDate: h.documentDate,
    shipDate: h.shipDate,
    dueDate: isWaybill(h.type) ? null : h.dueDate,
    salesRepUserId: h.salesRepUserId,
    linkedDocumentId: needsOriginal(h.type) ? h.linkedDocumentId : null,
    priceDirection: pd ? h.priceDirection : null,
    waybillDocumentId: BILLS[h.type] ? h.waybillDocumentId : null,
    description: h.description,
    lines: filled.map((l): DocumentLineWrite => {
      if (pd) {
        return {
          ...(l.originLineId === null ? { partId: null, description: l.description.trim() } : {}),
          qty: '1',
          unitPrice: l.unitPrice,
          vatPct: l.vatPct,
          originLineId: l.originLineId,
        }
      }
      return {
        partId: l.partId,
        qty: l.qty,
        unitPrice: l.unitPrice,
        discount1Pct: l.discount1Pct,
        discount2Pct: l.discount2Pct,
        vatPct: l.vatPct,
        originLineId: l.originLineId,
        billedLineId: l.billedLineId,
      }
    }),
  }
}

// Due date follows the document date and the card's term until the user types one.
export function dueFor(h: Header, termDays: number | null | undefined): string | null {
  if (isWaybill(h.type)) return null
  const auto = addDays(h.documentDate, termDays ?? 0)
  if (!h.dueTouched) return auto
  const l = h.loadedDue
  const follows =
    l && termDays !== null && termDays !== undefined && h.dueDate === l.due
      ? addDays(l.date, termDays) === l.due
      : false
  return follows ? auto : h.dueDate
}

// STOCK_SHORT rule: on hand − reserved by other documents, in the document depot. What this draft
// holds of the part, on any of its lines, is added back to the depot's available.
export function availableHere(
  l: EditLine,
  depotId: number | null,
  lines: readonly EditLine[] = [l],
): number | null {
  const s = l.stock?.find((x) => x.depotId === depotId)
  if (!s) return null
  const own = lines
    .filter((x) => x.key !== l.key && x.partId !== null && x.partId === l.partId)
    .reduce((n, x) => n + Number(x.reservedQty || 0), Number(l.reservedQty || 0))
  return Number(s.available) + own
}

export type StockTone = 'ok' | 'short' | 'none'

export function stockTone(
  l: EditLine,
  depotId: number | null,
  lines: readonly EditLine[] = [l],
): StockTone | null {
  const a = availableHere(l, depotId, lines)
  if (a === null || !l.qty) return null
  if (a <= 0) return 'none'
  return a < Number(l.qty) ? 'short' : 'ok'
}

// "On post" before the server has seen the edits: the card posting, stock per depot, credit used after.
export function previewEffects(
  h: Header,
  lines: EditLine[],
  totals: Totals,
  card: SicilSummary | null | undefined,
  originType: DocumentType | null,
): DocumentEffects {
  const sign = cardSign(h.type, h.priceDirection, originType)
  const dir = stockDirection(h.type, h.waybillDocumentId !== null)
  const units = lines.filter((l) => isFilled(l, h.type)).reduce((s, l) => s + Number(l.qty || 0), 0)
  const limit = card?.creditLimit ? Number(card.creditLimit) : null
  const total = Number(totals.grandTotal)
  return {
    ledger:
      sign === 0 || total === 0 || !card
        ? []
        : [
            {
              role: null,
              accountCode: card.accountCode ?? '',
              accountName: h.account?.hesapAdi ?? '',
              side: sign > 0 ? 'DEBIT' : 'CREDIT',
              amount: totals.grandTotal,
            },
          ],
    stock:
      dir && h.depotId && units
        ? [{ depotId: h.depotId, qtyDelta: String(dir === 'OUT' ? -units : units) }]
        : [],
    creditUsedPctAfter:
      limit && card && sign !== 0
        ? Math.round(((Number(card.balance) + sign * total) / limit) * 100)
        : null,
  }
}
