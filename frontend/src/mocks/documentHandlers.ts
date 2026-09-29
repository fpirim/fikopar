import { http, HttpResponse, type JsonBodyType } from 'msw'
import type {
  Document,
  DocumentColId,
  DocumentLine,
  DocumentLineWrite,
  DocumentsRequest,
  DocumentStatus,
  DocumentSummary,
  DocumentType,
  DocumentWarning,
  DocumentWrite,
  GridRequest,
  Member,
  NumberSeries,
  PriceDirection,
  StockWarning,
} from '../api/types'
import {
  addDays,
  BILLS,
  cardSign,
  creditChecked,
  documentTotals,
  isPriceDiff,
  isReturn,
  isWaybill,
  lineAmounts,
  originTypes,
  PREFIX,
  sideOf,
  stockDirection,
} from '../lib/documents'
import { applyGrid } from './grid'
import { setDocumentReservations, setLastPurchaseCost, stockBridge } from './stockHandlers'
import { TODAY } from './data'

const O = '/api/orgs/:orgId'
const XLSX = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
const DEPOTS = [
  { id: 1, code: '1' },
  { id: 2, code: '2' },
]
const HOLD_MS = 48 * 3600 * 1000

// What the documents mock reads from the ledger mock.
export interface Card {
  sicilId: number
  accountId: number
  code: string
  name: string
  tipi: string
  balance: number
  limit: number | null
}

interface MockLine {
  id: number
  partId: number | null
  description: string | null
  qty: string
  unitPrice: string
  discount1Pct: string
  discount2Pct: string
  vatPct: string
  originLineId: number | null
  billedLineId: number | null
  reserved: number
}

export interface MockDoc {
  id: number
  type: DocumentType
  status: DocumentStatus
  documentNo: string | null
  depotId: number
  sicilId: number
  documentDate: string
  shipDate: string | null
  dueDate: string | null
  salesRepUserId: number | null
  linkedDocumentId: number | null
  priceDirection: PriceDirection | null
  waybillDocumentId: number | null
  description: string
  lines: MockLine[]
  version: number
  expiresAt: string | null
  postedAt: string | null
  cancelledAt: string | null
  cancelDate: string | null
  cancelReason: string | null
  creditOverride: Document['creditOverride']
  journalEntryId: number | null
  reversalEntryId: number | null
}

const members: Member[] = [
  { userId: 7, name: 'A. Kaya', email: 'a.kaya@fikopar.local' },
  { userId: 12, name: 'Demo', email: 'demo@fikopar.local' },
  { userId: 9, name: 'M. Demir', email: 'm.demir@fikopar.local' },
]

const state = {
  docs: [] as MockDoc[],
  line: 1,
  doc: 2000,
  entry: 6000,
  series: new Map<string, number>(),
  idem: new Map<string, { status: number; body: JsonBodyType }>(),
  cards: (() => []) as () => Card[],
}

const card = (sicilId: number) => state.cards().find((c) => c.sicilId === sicilId)
const doc = (id: number) => state.docs.find((d) => d.id === id)
const lineOf = (id: number | null) =>
  id === null ? undefined : state.docs.flatMap((d) => d.lines).find((l) => l.id === id)

type Seed = [
  stockNo: string | null,
  qty: number,
  price: number,
  d1?: number,
  d2?: number,
  opts?: { origin?: [number, number]; billed?: [number, number]; description?: string },
]

function seedLines(d: MockDoc, rows: Seed[]) {
  d.lines = rows.map(([stockNo, qty, price, d1 = 0, d2 = 0, o = {}], i) => {
    const partId = stockNo ? partByNo(stockNo) : null
    const originDoc = o.origin ? doc(o.origin[0]) : undefined
    const billedDoc = o.billed ? doc(o.billed[0]) : undefined
    return {
      id: d.id * 100 + i + 1,
      partId,
      description: o.description ?? null,
      qty: String(qty),
      unitPrice: String(price),
      discount1Pct: String(d1),
      discount2Pct: String(d2),
      vatPct: '20',
      originLineId: originDoc ? originDoc.lines[o.origin![1]].id : null,
      billedLineId: billedDoc ? billedDoc.lines[o.billed![1]].id : null,
      reserved: 0,
    }
  })
}

function partByNo(stockNo: string): number {
  const id = stockBridge.partByNo(stockNo)
  if (id === undefined) throw new Error(`mock part ${stockNo} missing`)
  return id
}

function base(
  id: number,
  type: DocumentType,
  status: DocumentStatus,
  no: string | null,
  sicilName: string,
  depotId: number,
  date: string,
): MockDoc {
  const c = state.cards().find((x) => x.name === sicilName)!
  return {
    id,
    type,
    status,
    documentNo: no,
    depotId,
    sicilId: c.sicilId,
    documentDate: date,
    shipDate: date,
    dueDate: isWaybill(type) ? null : addDays(date, 30),
    salesRepUserId: type.startsWith('SALES') ? 7 : null,
    linkedDocumentId: null,
    priceDirection: null,
    waybillDocumentId: null,
    description: '',
    lines: [],
    version: status === 'DRAFT' ? 3 : 2,
    expiresAt: null,
    postedAt: status === 'DRAFT' ? null : `${date}T09:02:17Z`,
    cancelledAt: null,
    cancelDate: null,
    cancelReason: null,
    creditOverride: null,
    journalEntryId: status === 'DRAFT' || isWaybill(type) ? null : 4000 + id,
    reversalEntryId: null,
  }
}

function seed() {
  const docs: MockDoc[] = []
  state.docs = docs
  const add = (d: MockDoc, rows: Seed[]) => {
    docs.push(d)
    seedLines(d, rows)
    return d
  }
  add(
    base(
      391,
      'SALES_INVOICE_WITH_WAYBILL',
      'POSTED',
      'SF-2026-1-000391',
      'Yılmaz Tarım Makinaları',
      1,
      '2026-09-09',
    ),
    [
      ['HP-3702', 5, 9850, 0, 0],
      ['WP-3715', 4, 3420, 0, 0],
      ['FP-3709', 5, 1180, 0, 0],
    ],
  )
  add(
    base(
      112,
      'SALES_INVOICE',
      'POSTED',
      'SF-2026-2-000112',
      'Yılmaz Tarım Makinaları',
      2,
      '2026-09-24',
    ),
    [['FP-3709', 2, 1450, 0, 0]],
  )
  add(base(108, 'SALES_INVOICE', 'POSTED', 'SF-2026-2-000108', 'Aksoy Tarım', 2, '2026-09-22'), [
    ['HP-3774', 3, 12400, 5, 0],
  ])
  add(
    base(
      140,
      'PURCHASE_INVOICE',
      'POSTED',
      'AF-2026-2-000140',
      'Ege Hidrolik San.',
      2,
      '2026-06-10',
    ),
    [['HP-3774', 2, 8450, 0, 0]],
  )
  add(
    base(
      201,
      'PURCHASE_INVOICE',
      'POSTED',
      'AF-2026-1-000201',
      'Ege Hidrolik San.',
      1,
      '2026-09-26',
    ),
    [
      ['HP-3774', 10, 8900, 0, 0],
      ['CL-1150', 12, 2850, 0, 0],
    ],
  )
  add(
    base(
      418,
      'SALES_INVOICE_WITH_WAYBILL',
      'POSTED',
      'SF-2026-1-000418',
      'Yılmaz Tarım Makinaları',
      1,
      TODAY,
    ),
    [
      ['HP-3774', 2, 12400, 10, 0],
      ['CL-1150', 4, 2850, 5, 0],
      ['FL-0092', 20, 265, 0, 0],
      ['BR-2210', 6, 375, 0, 4.4444],
    ],
  )
  const si9 = base(
    9,
    'SALES_RETURN',
    'POSTED',
    'SI-2026-2-000009',
    'Yılmaz Tarım Makinaları',
    2,
    '2026-09-26',
  )
  si9.linkedDocumentId = 112
  si9.dueDate = '2026-09-26'
  add(si9, [['FP-3709', 2, 1450, 0, 0, { origin: [112, 0] }]])
  const si12 = base(
    12,
    'SALES_RETURN',
    'POSTED',
    'SI-2026-1-000012',
    'Yılmaz Tarım Makinaları',
    1,
    TODAY,
  )
  si12.linkedDocumentId = 391
  add(si12, [['WP-3715', 1, 3420, 0, 0, { origin: [391, 1] }]])
  const ff7 = base(
    7,
    'PRICE_DIFFERENCE',
    'POSTED',
    'FF-2026-1-000007',
    'Yılmaz Tarım Makinaları',
    1,
    '2026-09-12',
  )
  ff7.linkedDocumentId = 391
  ff7.priceDirection = 'INCREASE'
  add(ff7, [['HP-3702', 1, 250, 0, 0, { origin: [391, 0] }]])
  const ff8 = base(
    8,
    'PRICE_DIFFERENCE',
    'POSTED',
    'FF-2026-2-000008',
    'Aksoy Tarım',
    2,
    '2026-09-25',
  )
  ff8.linkedDocumentId = 108
  ff8.priceDirection = 'DECREASE'
  add(ff8, [[null, 1, 1200, 0, 0, { description: 'Volume rebate, September' }]])
  const sir = base(
    52,
    'SALES_WAYBILL',
    'POSTED',
    'SIR-2026-1-000052',
    'Kaya Ziraat',
    1,
    '2026-09-27',
  )
  add(sir, [
    ['HP-3780', 1, 15900, 0, 0],
    ['FL-0092', 10, 265, 0, 0],
  ])
  const cancelled = base(
    415,
    'SALES_INVOICE',
    'CANCELLED',
    'SF-2026-1-000415',
    'Öztürk Tarım',
    1,
    '2026-09-26',
  )
  Object.assign(cancelled, {
    cancelledAt: '2026-09-27T10:12:00Z',
    cancelDate: '2026-09-27',
    cancelReason: 'Wrong customer',
    reversalEntryId: 4830,
    version: 3,
  })
  add(cancelled, [['FL-0092', 4, 265, 0, 0]])
  const draft = base(
    1001,
    'SALES_INVOICE_WITH_WAYBILL',
    'DRAFT',
    null,
    'Yılmaz Tarım Makinaları',
    1,
    TODAY,
  )
  add(draft, [
    ['HP-3774', 2, 12400, 10, 0],
    ['CL-1150', 4, 2850, 5, 0],
    ['FL-0092', 20, 265, 0, 0],
    ['BR-2210', 6, 375, 0, 4.4444],
  ])
  const big = base(1002, 'SALES_INVOICE', 'DRAFT', null, 'Öztürk Tarım', 1, TODAY)
  add(big, [['HP-3780', 16, 15900, 0, 0]])
  state.series = new Map([
    ['SF:1', 419],
    ['SF:2', 113],
    ['AF:1', 202],
    ['AF:2', 141],
    ['SI:1', 13],
    ['SI:2', 10],
    ['FF:1', 8],
    ['FF:2', 9],
    ['SIR:1', 53],
  ])
  for (const d of docs) if (d.documentNo) stockBridge.link(d.documentNo, d.id, d.type)
}

export function resetDocuments(cards: () => Card[]) {
  state.cards = cards
  state.line = 900000
  state.doc = 2000
  state.entry = 6000
  state.idem = new Map()
  seed()
}

// Stock held by drafts per (part, depot).
function held(partId: number, depotId: number, except?: number) {
  let n = 0
  for (const d of state.docs) {
    if (d.status !== 'DRAFT' || d.depotId !== depotId || d.id === except) continue
    for (const l of d.lines) if (l.partId === partId) n += l.reserved
  }
  return n
}
setDocumentReservations((p, d) => held(p, d))

const PURCHASES: DocumentType[] = ['PURCHASE_INVOICE', 'PURCHASE_INVOICE_WITH_WAYBILL']

// Net unit price of the latest posted purchase invoice line on or before `date`.
function lastPurchaseCost(partId: number, date: string): number | null {
  let best: { date: string; id: number; cost: number } | null = null
  for (const d of state.docs) {
    if (d.status !== 'POSTED' || !PURCHASES.includes(d.type) || d.documentDate > date) continue
    for (const l of d.lines) {
      if (l.partId !== partId) continue
      const cost = Number(amounts(l).netAmount) / Number(l.qty)
      if (!best || d.documentDate > best.date || (d.documentDate === best.date && l.id > best.id)) {
        best = { date: d.documentDate, id: l.id, cost }
      }
    }
  }
  return best?.cost ?? null
}
setLastPurchaseCost(lastPurchaseCost)

const holds = (d: MockDoc) =>
  d.status === 'DRAFT' && stockDirection(d.type, d.waybillDocumentId !== null) === 'OUT'

// Available in the depot without this draft's own hold.
function availableFor(d: MockDoc, partId: number) {
  const lv = stockBridge.level(partId, d.depotId)
  const own = d.lines.filter((l) => l.partId === partId).reduce((s, l) => s + l.reserved, 0)
  return lv.available + own
}

function reserve(d: MockDoc, at: string) {
  for (const l of d.lines) l.reserved = 0
  d.expiresAt = null
  if (!holds(d)) return
  const left = new Map<number, number>()
  for (const l of d.lines) {
    if (l.partId === null) continue
    const avail = left.get(l.partId) ?? Math.max(0, availableFor(d, l.partId))
    l.reserved = Math.max(0, Math.min(Number(l.qty), avail))
    left.set(l.partId, avail - l.reserved)
  }
  if (d.lines.some((l) => l.reserved > 0)) {
    d.expiresAt = new Date(Date.parse(at) + HOLD_MS).toISOString()
  }
}

const q = (n: number) => String(Math.round(n * 10000) / 10000)
const originType = (d: MockDoc) =>
  d.linkedDocumentId ? (doc(d.linkedDocumentId)?.type ?? null) : null

const goodsMovingReturn = (r: MockDoc) =>
  r.status === 'POSTED' && isReturn(r.type) && stockDirection(r.type, r.waybillDocumentId !== null)

function returnedQty(lineId: number, exceptDoc?: number) {
  let n = 0
  for (const r of state.docs) {
    if (r.id === exceptDoc || !goodsMovingReturn(r)) continue
    for (const l of r.lines) if (l.originLineId === lineId) n += Number(l.qty)
  }
  return n
}

function amounts(l: MockLine) {
  return lineAmounts({
    qty: l.qty,
    unitPrice: l.unitPrice,
    discount1Pct: l.discount1Pct,
    discount2Pct: l.discount2Pct,
    vatPct: l.vatPct,
  })
}

function totalsOf(d: MockDoc) {
  return documentTotals(d.lines.map((l) => ({ ...amounts(l), vatPct: l.vatPct })))
}

function numberOf(d: MockDoc) {
  return d.documentNo
}

function warnings(d: MockDoc): DocumentWarning[] {
  if (d.status !== 'DRAFT') return []
  const out: DocumentWarning[] = []
  d.lines.forEach((l, i) => {
    if (l.partId === null) return
    if (holds(d) && availableFor(d, l.partId) < Number(l.qty)) {
      out.push({
        code: 'STOCK_SHORT',
        lineNo: i + 1,
        partId: l.partId,
        depotId: d.depotId,
        requested: l.qty,
        available: q(availableFor(d, l.partId)),
      })
    }
    const list = stockBridge.listPrice(l.partId, d.documentDate)
    if (
      sideOf(d.type) === 'SALES' &&
      !isPriceDiff(d.type) &&
      list !== null &&
      Number(l.unitPrice) < list
    ) {
      out.push({ code: 'PRICE_BELOW_LIST', lineNo: i + 1, partId: l.partId })
    }
  })
  const over = creditExcess(d)
  if (over) out.push({ code: 'CREDIT_LIMIT' })
  return out
}

function creditExcess(d: MockDoc) {
  if (!creditChecked(d.type, d.priceDirection, originType(d))) return null
  const c = card(d.sicilId)
  if (!c?.limit) return null
  const total = Number(totalsOf(d).grandTotal)
  const excess = c.balance + total - c.limit
  return excess > 0
    ? {
        limit: c.limit.toFixed(2),
        balance: c.balance.toFixed(2),
        documentTotal: total.toFixed(2),
        excess: excess.toFixed(2),
      }
    : null
}

function effects(d: MockDoc): Document['effects'] {
  const c = card(d.sicilId)
  const t = totalsOf(d)
  const sign = cardSign(d.type, d.priceDirection, originType(d))
  const dir = stockDirection(d.type, d.waybillDocumentId !== null)
  const byDepot = new Map<number, number>()
  if (dir) {
    for (const l of d.lines) byDepot.set(d.depotId, (byDepot.get(d.depotId) ?? 0) + Number(l.qty))
  }
  const after =
    c?.limit && sign !== 0
      ? Math.round(
          ((c.balance + (d.status === 'DRAFT' ? sign * Number(t.grandTotal) : 0)) / c.limit) * 100,
        )
      : null
  return {
    ledger:
      sign === 0 || Number(t.grandTotal) === 0
        ? []
        : [
            {
              role: null,
              accountCode: c?.code ?? '',
              accountName: c?.name ?? '',
              side: sign > 0 ? 'DEBIT' : 'CREDIT',
              amount: t.grandTotal,
            },
          ],
    stock: [...byDepot.entries()].map(([depotId, n]) => ({
      depotId,
      qtyDelta: q(dir === 'OUT' ? -n : n),
    })),
    creditUsedPctAfter: after,
  }
}

function lineOut(d: MockDoc, l: MockLine, i: number): DocumentLine {
  const p = l.partId === null ? null : stockBridge.part(l.partId)
  const a = amounts(l)
  const lv = l.partId === null ? null : stockBridge.level(l.partId, d.depotId)
  const moved = d.status === 'POSTED' && stockDirection(d.type, d.waybillDocumentId !== null)
  return {
    id: l.id,
    lineNo: i + 1,
    partId: l.partId,
    description: l.description,
    stockNo: p?.stockNo ?? '',
    name: p?.name ?? l.description ?? '',
    catalogCode: l.partId === null ? 0 : stockBridge.catalogCode(l.partId),
    qty: l.qty,
    unitPrice: l.unitPrice,
    discount1Pct: l.discount1Pct,
    discount2Pct: l.discount2Pct,
    vatPct: l.vatPct,
    originLineId: l.originLineId,
    billedLineId: l.billedLineId,
    ...(lv ? { onHand: q(lv.onHand), available: q(lv.available) } : {}),
    ...(d.status === 'DRAFT' && l.partId !== null
      ? {
          stockByDepot: DEPOTS.map((dep) => {
            const x = stockBridge.level(l.partId!, dep.id)
            return {
              depotId: dep.id,
              depotCode: dep.code,
              onHand: q(x.onHand),
              reserved: q(x.reserved),
              available: q(x.available),
            }
          }),
          reservedQty: q(l.reserved),
        }
      : {}),
    ...(moved ? { returnedQty: q(returnedQty(l.id)) } : {}),
    ...a,
  }
}

function out(d: MockDoc): Document {
  const t = totalsOf(d)
  const linked = d.linkedDocumentId ? doc(d.linkedDocumentId) : undefined
  const wb = d.waybillDocumentId ? doc(d.waybillDocumentId) : undefined
  const bill = state.docs.find((b) => b.waybillDocumentId === d.id && b.status !== 'CANCELLED')
  return {
    id: d.id,
    orgId: 1,
    documentNo: numberOf(d),
    type: d.type,
    status: d.status,
    depotId: d.depotId,
    sicilId: d.sicilId,
    sicilName: card(d.sicilId)?.name ?? '',
    documentDate: d.documentDate,
    shipDate: d.shipDate,
    dueDate: d.dueDate,
    salesRepUserId: d.salesRepUserId,
    salesRepName: members.find((m) => m.userId === d.salesRepUserId)?.name ?? null,
    linkedDocumentId: d.linkedDocumentId,
    linkedDocumentNo: linked?.documentNo ?? null,
    priceDirection: d.priceDirection,
    waybillDocumentId: d.waybillDocumentId,
    waybillDocumentNo: wb?.documentNo ?? null,
    billedByDocumentId: isWaybill(d.type) ? (bill?.id ?? null) : null,
    dependentDocumentIds: state.docs
      .filter((x) => x.linkedDocumentId === d.id && x.status === 'POSTED')
      .map((x) => x.id),
    currency: 'TRY',
    ...t,
    description: d.description,
    lines: d.lines.map((l, i) => lineOut(d, l, i)),
    reservation: d.expiresAt ? { expiresAt: d.expiresAt } : null,
    effects: effects(d),
    warnings: warnings(d),
    creditOverride: d.creditOverride,
    journalEntryId: d.journalEntryId,
    reversalEntryId: d.reversalEntryId,
    postedAt: d.postedAt,
    cancelledAt: d.cancelledAt,
    cancelDate: d.cancelDate,
    cancelReason: d.cancelReason,
    version: d.version,
  }
}

function summary(d: MockDoc): DocumentSummary {
  const t = totalsOf(d)
  const c = card(d.sicilId)
  return {
    id: d.id,
    documentNo: d.documentNo,
    type: d.type,
    status: d.status,
    depotId: d.depotId,
    depotCode: String(d.depotId),
    sicilId: d.sicilId,
    hesapNo: c?.code ?? '',
    sicilName: c?.name ?? '',
    documentDate: d.documentDate,
    dueDate: d.dueDate,
    salesRepName: members.find((m) => m.userId === d.salesRepUserId)?.name ?? null,
    grossTotal: t.grossTotal,
    discountTotal: t.discountTotal,
    netTotal: t.netTotal,
    vatTotal: t.vatTotal,
    grandTotal: t.grandTotal,
    linkedDocumentNo: d.linkedDocumentId ? (doc(d.linkedDocumentId)?.documentNo ?? null) : null,
    priceDirection: d.priceDirection,
    waybillDocumentNo: d.waybillDocumentId ? (doc(d.waybillDocumentId)?.documentNo ?? null) : null,
    creditOverride: Boolean(d.creditOverride),
    postedAt: d.postedAt,
    version: d.version,
  }
}

const COLS: Record<DocumentColId, (r: DocumentSummary) => string | number | null | undefined> = {
  documentNo: (r) => r.documentNo,
  type: (r) => r.type,
  status: (r) => r.status,
  documentDate: (r) => r.documentDate,
  dueDate: (r) => r.dueDate,
  hesapNo: (r) => r.hesapNo,
  sicilName: (r) => r.sicilName,
  depotCode: (r) => r.depotCode,
  salesRepName: (r) => r.salesRepName,
  grossTotal: (r) => Number(r.grossTotal),
  discountTotal: (r) => Number(r.discountTotal),
  netTotal: (r) => Number(r.netTotal),
  vatTotal: (r) => Number(r.vatTotal),
  grandTotal: (r) => Number(r.grandTotal),
  postedAt: (r) => r.postedAt,
  linkedDocumentNo: (r) => r.linkedDocumentNo,
  waybillDocumentNo: (r) => r.waybillDocumentNo,
}

function documentsGrid(req: DocumentsRequest) {
  const used = [
    ...Object.keys(req.filterModel ?? {}),
    ...(req.sortModel ?? []).map((s) => s.colId ?? ''),
  ]
  if (used.some((c) => !(c in COLS))) return null
  const scoped = state.docs.filter((d) => !req.depotId || d.depotId === req.depotId).map(summary)
  const filtered = scoped.filter(
    (r) =>
      (!req.types?.length || req.types.includes(r.type)) &&
      (!req.statuses?.length || req.statuses.includes(r.status)) &&
      (!req.sicilId || r.sicilId === req.sicilId) &&
      (!req.dateFrom || r.documentDate >= req.dateFrom) &&
      (!req.dateTo || r.documentDate <= req.dateTo) &&
      (!req.documentNo || (r.documentNo ?? '').includes(req.documentNo)),
  )
  const sortModel = req.sortModel?.length
    ? req.sortModel
    : [
        { colId: 'documentDate' as const, sort: 'desc' as const },
        { colId: 'documentNo' as const, sort: 'desc' as const },
      ]
  const byId = [...filtered].sort((a, b) => b.id - a.id)
  const { rows, filtered: all } = applyGrid(
    byId,
    { ...(req as GridRequest), sortModel } as GridRequest,
    COLS,
    ['documentNo', 'hesapNo', 'sicilName'],
  )
  const live = all.filter((r) => r.status !== 'CANCELLED')
  const sum = (k: 'grossTotal' | 'discountTotal' | 'netTotal' | 'vatTotal' | 'grandTotal') =>
    live.reduce((s, r) => s + Number(r[k]), 0).toFixed(2)
  return {
    rows,
    totalCount: all.length,
    unfilteredCount: scoped.length,
    totals: {
      grossTotal: sum('grossTotal'),
      discountTotal: sum('discountTotal'),
      netTotal: sum('netTotal'),
      vatTotal: sum('vatTotal'),
      grandTotal: sum('grandTotal'),
    },
  }
}

export function documentSeries(): NumberSeries[] {
  return [...state.series.entries()].map(([k, nextValue], i) => {
    const [prefix, depotId] = k.split(':')
    return {
      id: 500 + i,
      prefix: prefix as NumberSeries['prefix'],
      depotId: Number(depotId),
      year: 2026,
      width: 6,
      nextValue,
      version: nextValue,
    }
  })
}

// Number and type of a document by its number (statement lines link to it).
export function documentRef(no: string | null) {
  const d = no ? state.docs.find((x) => x.documentNo === no) : undefined
  return d ? { id: d.id, type: d.type } : null
}

type Result = { status: number; body: JsonBodyType; headers?: Record<string, string> }
const fail = (status: number, code: string, extra: Record<string, unknown> = {}): Result => ({
  status,
  body: { code, ...extra },
})
const ok = (d: MockDoc, status = 200): Result => ({
  status,
  body: out(d) as unknown as JsonBodyType,
  headers: { ETag: `"${d.version}"` },
})

function validate(body: DocumentWrite, self?: MockDoc): Result | null {
  if (!card(body.sicilId)) return fail(422, 'ERR_SICIL_NOT_FOUND', { field: 'sicilId' })
  if (!DEPOTS.some((d) => d.id === body.depotId)) {
    return fail(422, 'ERR_DEPOT_NOT_FOUND', { field: 'depotId' })
  }
  if (body.documentDate < '2026-01-01') return fail(409, 'ERR_YEAR_CLOSED')
  const pd = isPriceDiff(body.type)
  if (pd !== Boolean(body.priceDirection)) {
    return fail(400, 'ERR_INVALID_REQUEST', { field: 'priceDirection' })
  }
  if (originTypes(body.type).length) {
    const o = body.linkedDocumentId ? doc(body.linkedDocumentId) : undefined
    if (
      !o ||
      o.status !== 'POSTED' ||
      !originTypes(body.type).includes(o.type) ||
      o.sicilId !== body.sicilId ||
      (pd && o.depotId !== body.depotId) ||
      (!pd && o.waybillDocumentId !== null)
    ) {
      return fail(422, 'ERR_LINKED_DOCUMENT_INVALID', { field: 'linkedDocumentId' })
    }
  } else if (body.linkedDocumentId) {
    return fail(422, 'ERR_LINKED_DOCUMENT_INVALID', { field: 'linkedDocumentId' })
  }
  if (body.waybillDocumentId) {
    const w = doc(body.waybillDocumentId)
    if (
      !w ||
      BILLS[body.type] !== w.type ||
      w.sicilId !== body.sicilId ||
      w.depotId !== body.depotId
    ) {
      return fail(422, 'ERR_WAYBILL_INVALID', { field: 'waybillDocumentId' })
    }
    const other = state.docs.find(
      (b) => b.waybillDocumentId === w.id && b.status !== 'CANCELLED' && b.id !== self?.id,
    )
    if (other) return fail(409, 'ERR_WAYBILL_BILLED')
  }
  for (const [i, l] of body.lines.entries()) {
    if (pd) {
      if (Number(l.qty) !== 1 || Number(l.discount1Pct ?? 0) || Number(l.discount2Pct ?? 0)) {
        return fail(400, 'ERR_INVALID_REQUEST', { field: `lines[${i}]` })
      }
      continue
    }
    if (!l.partId || !stockBridge.part(l.partId)) {
      return fail(422, 'ERR_PART_NOT_FOUND', { field: `lines[${i}].partId` })
    }
    if (l.description) return fail(400, 'ERR_INVALID_REQUEST', { field: `lines[${i}].description` })
  }
  return null
}

function apply(d: MockDoc, body: DocumentWrite) {
  const c = card(body.sicilId)!
  const pd = isPriceDiff(body.type)
  let lines: DocumentLineWrite[] = body.lines
  if (body.waybillDocumentId && !lines.length) {
    lines = doc(body.waybillDocumentId)!.lines.map((l) => ({ ...l, billedLineId: l.id }))
  }
  Object.assign(d, {
    type: body.type,
    depotId: body.depotId,
    sicilId: body.sicilId,
    documentDate: body.documentDate,
    shipDate: body.shipDate ?? null,
    dueDate: body.dueDate ?? addDays(body.documentDate, c.tipi === 'BANKA' ? 0 : 30),
    salesRepUserId: body.salesRepUserId ?? (c.tipi === 'MUSTERI' ? 7 : null),
    linkedDocumentId: body.linkedDocumentId ?? null,
    priceDirection: body.priceDirection ?? null,
    waybillDocumentId: body.waybillDocumentId ?? null,
    description: body.description ?? '',
  })
  d.lines = lines.map((l) => {
    const origin = lineOf(l.originLineId ?? null)
    const partId = pd ? (origin?.partId ?? null) : l.partId!
    const part = partId ? stockBridge.part(partId) : undefined
    return {
      id: state.line++,
      partId,
      description: pd ? (l.description ?? null) : null,
      qty: pd ? '1' : String(Number(l.qty)),
      unitPrice: String(Number(l.unitPrice)),
      discount1Pct: pd ? '0' : String(Number(l.discount1Pct ?? 10)),
      discount2Pct: pd ? '0' : String(Number(l.discount2Pct ?? 5)),
      vatPct: String(Number(l.vatPct ?? origin?.vatPct ?? part?.vatPct ?? 20)),
      originLineId: l.originLineId ?? null,
      billedLineId: l.billedLineId ?? null,
      reserved: 0,
    }
  })
}

function create(body: DocumentWrite): Result {
  const bad = validate(body)
  if (bad) return bad
  const d = base(
    state.doc++,
    body.type,
    'DRAFT',
    null,
    card(body.sicilId)!.name,
    body.depotId,
    body.documentDate,
  )
  d.version = 1
  state.docs.push(d)
  apply(d, body)
  reserve(d, new Date().toISOString())
  return ok(d, 201)
}

function update(d: MockDoc, body: DocumentWrite): Result {
  if (d.status !== 'DRAFT') return fail(409, 'ERR_DOCUMENT_NOT_DRAFT')
  const bad = validate(body, d)
  if (bad) return bad
  apply(d, body)
  d.version += 1
  reserve(d, new Date().toISOString())
  return ok(d)
}

function movements(d: MockDoc, reversal: boolean, date: string): StockWarning[] {
  const dir = stockDirection(d.type, d.waybillDocumentId !== null)
  if (!dir) return []
  const out = reversal ? dir === 'IN' : dir === 'OUT'
  const side = sideOf(d.type)
  const label = isReturn(d.type)
    ? side === 'SALES'
      ? 'SALES_RETURN'
      : 'PURCHASE_RETURN'
    : side === 'SALES'
      ? 'SALE'
      : 'PURCHASE'
  const name = card(d.sicilId)?.name ?? null
  return stockBridge.move(
    d.lines
      .filter((l) => l.partId !== null)
      .map((l) => ({
        partId: l.partId!,
        depotId: d.depotId,
        kind: out ? 'OUT' : 'IN',
        label,
        qty: Number(l.qty),
        unitCost: out ? null : Number(amounts(l).netAmount) / Number(l.qty),
        sourceType: 'document',
        sourceNo: d.documentNo,
        sicilName: name,
        otherDepotId: null,
        transferId: null,
        countId: null,
        documentId: d.id,
        documentType: d.type,
        reversal,
        date,
        occurredAt: new Date().toISOString(),
      })),
  )
}

function post(d: MockDoc, admin: boolean, override?: { reason?: string }): Result {
  if (d.status !== 'DRAFT') return fail(409, 'ERR_DOCUMENT_NOT_DRAFT')
  if (d.documentDate < '2026-01-01') return fail(409, 'ERR_YEAR_CLOSED')
  if (!d.lines.length) return fail(422, 'ERR_DOCUMENT_EMPTY')
  if (d.waybillDocumentId) {
    const other = state.docs.find(
      (b) => b.waybillDocumentId === d.waybillDocumentId && b.status === 'POSTED',
    )
    if (other) return fail(409, 'ERR_WAYBILL_BILLED')
  }
  if (isReturn(d.type) && stockDirection(d.type, d.waybillDocumentId !== null)) {
    for (const [i, l] of d.lines.entries()) {
      const o = lineOf(l.originLineId)
      if (o && returnedQty(o.id, d.id) + Number(l.qty) > Number(o.qty)) {
        return fail(422, 'ERR_RETURN_QTY_EXCEEDED', { field: `lines[${i}].qty` })
      }
    }
  }
  if (d.type === 'PRICE_DIFFERENCE' && d.priceDirection === 'DECREASE') {
    const o = doc(d.linkedDocumentId!)!
    const decreases = state.docs
      .filter(
        (x) =>
          x.linkedDocumentId === o.id &&
          x.status === 'POSTED' &&
          x.type === 'PRICE_DIFFERENCE' &&
          x.priceDirection === 'DECREASE',
      )
      .reduce((s, x) => s + Number(totalsOf(x).netTotal), 0)
    const returned = state.docs
      .filter((x) => x.linkedDocumentId === o.id && goodsMovingReturn(x))
      .reduce((s, x) => s + Number(totalsOf(x).netTotal), 0)
    const cap = Number(totalsOf(o).netTotal) - decreases - returned
    if (Number(totalsOf(d).netTotal) > cap + 0.001) {
      return fail(422, 'ERR_PRICE_DIFFERENCE_EXCEEDED', { field: 'lines' })
    }
  }
  const excess = creditExcess(d)
  if (excess) {
    if (!override?.reason) return fail(409, 'ERR_CREDIT_LIMIT_EXCEEDED', { details: excess })
    if (!admin) return fail(403, 'ERR_FORBIDDEN')
    d.creditOverride = {
      reason: override.reason,
      userId: 12,
      userName: 'Demo',
      at: new Date().toISOString(),
      excess: excess.excess,
    }
  }
  const key = `${PREFIX[d.type]}:${d.depotId}`
  const next = state.series.get(key) ?? 1
  state.series.set(key, next + 1)
  d.documentNo = `${PREFIX[d.type]}-${d.documentDate.slice(0, 4)}-${d.depotId}-${String(next).padStart(6, '0')}`
  for (const l of d.lines) l.reserved = 0
  d.expiresAt = null
  d.status = 'POSTED'
  d.postedAt = new Date().toISOString()
  d.version += 1
  if (!isWaybill(d.type)) d.journalEntryId = state.entry++
  const warn = movements(d, false, d.documentDate)
  const res = ok(d)
  ;(res.body as { warnings: unknown[] }).warnings = warn
  return res
}

function cancel(d: MockDoc, reason: string): Result {
  if (d.status !== 'POSTED') return fail(409, 'ERR_INVALID_STATE_TRANSITION')
  if (
    isWaybill(d.type) &&
    state.docs.some((b) => b.waybillDocumentId === d.id && b.status !== 'CANCELLED')
  ) {
    return fail(409, 'ERR_WAYBILL_BILLED')
  }
  if (state.docs.some((x) => x.linkedDocumentId === d.id && x.status === 'POSTED')) {
    return fail(409, 'ERR_DOCUMENT_HAS_DEPENDENTS')
  }
  d.status = 'CANCELLED'
  d.cancelledAt = new Date().toISOString()
  d.cancelDate = TODAY
  d.cancelReason = reason
  d.version += 1
  if (d.journalEntryId) d.reversalEntryId = state.entry++
  const warn = movements(d, true, TODAY)
  const res = ok(d)
  ;(res.body as { warnings: unknown[] }).warnings = warn
  return res
}

async function idempotent(request: Request, run: () => Result): Promise<Response> {
  const k = request.headers.get('Idempotency-Key')
  if (!k) return HttpResponse.json({ code: 'ERR_IDEMPOTENCY_KEY_REQUIRED' }, { status: 400 })
  const hit = state.idem.get(k)
  if (hit) {
    return HttpResponse.json(hit.body, {
      status: hit.status,
      headers: { 'Idempotent-Replayed': 'true' },
    })
  }
  const res = run()
  if (res.status < 400) state.idem.set(k, res)
  return HttpResponse.json(res.body, { status: res.status, headers: res.headers })
}

function precondition(request: Request, version: number): Response | null {
  const want = request.headers.get('If-Match')
  if (!want) return HttpResponse.json({ code: 'ERR_PRECONDITION_REQUIRED' }, { status: 428 })
  if (want.replace(/^W\//, '').replace(/"/g, '') !== String(version)) {
    return HttpResponse.json({ code: 'ERR_VERSION_CONFLICT' }, { status: 412 })
  }
  return null
}

const reply = (r: Result) => HttpResponse.json(r.body, { status: r.status, headers: r.headers })
const notFound = () => HttpResponse.json({ code: 'ERR_DOCUMENT_NOT_FOUND' }, { status: 404 })
const pdf = () => new HttpResponse('%PDF-1.7', { headers: { 'Content-Type': 'application/pdf' } })

export const documentHandlers = [
  http.get(`${O}/members`, () => HttpResponse.json(members)),
  http.post(`${O}/documents/grid`, async ({ request }) => {
    const body = (await request.json()) as DocumentsRequest
    if (request.headers.get('Accept')?.includes('spreadsheetml')) {
      return new HttpResponse('xlsx', { headers: { 'Content-Type': XLSX } })
    }
    const res = documentsGrid(body)
    return res
      ? HttpResponse.json(res as unknown as JsonBodyType)
      : HttpResponse.json({ code: 'ERR_INVALID_REQUEST', field: 'colId' }, { status: 400 })
  }),
  http.post(`${O}/documents`, async ({ request }) => {
    const body = (await request.json()) as DocumentWrite
    return idempotent(request, () => create(body))
  }),
  http.get(`${O}/documents/:documentId`, ({ params, request }) => {
    const d = doc(Number(params.documentId))
    if (!d) return notFound()
    if (request.headers.get('Accept')?.includes('pdf')) return pdf()
    return reply(ok(d))
  }),
  http.put(`${O}/documents/:documentId`, async ({ params, request }) => {
    const d = doc(Number(params.documentId))
    if (!d) return notFound()
    const stale = precondition(request, d.version)
    if (stale) return stale
    return reply(update(d, (await request.json()) as DocumentWrite))
  }),
  http.delete(`${O}/documents/:documentId`, ({ params, request }) => {
    const d = doc(Number(params.documentId))
    if (!d) return notFound()
    const stale = precondition(request, d.version)
    if (stale) return stale
    if (d.status !== 'DRAFT') return reply(fail(409, 'ERR_DOCUMENT_NOT_DRAFT'))
    state.docs = state.docs.filter((x) => x !== d)
    return new HttpResponse(null, { status: 204 })
  }),
  http.post(`${O}/documents/:documentId/post`, async ({ params, request }) => {
    const d = doc(Number(params.documentId))
    if (!d) return notFound()
    const text = await request.text()
    const body = (text ? JSON.parse(text) : {}) as { creditOverride?: { reason?: string } }
    const admin = request.headers.get('Authorization') === 'Bearer msw-admin'
    const k = request.headers.get('Idempotency-Key')
    if (!k || !state.idem.has(k)) {
      const stale = precondition(request, d.version)
      if (stale) return stale
    }
    return idempotent(request, () => post(d, admin, body.creditOverride))
  }),
  http.post(`${O}/documents/:documentId/cancel`, async ({ params, request }) => {
    const d = doc(Number(params.documentId))
    if (!d) return notFound()
    const body = (await request.json()) as { reason?: string }
    if (!body.reason || body.reason.trim().length < 3) {
      return HttpResponse.json({ code: 'ERR_INVALID_REQUEST', field: 'reason' }, { status: 400 })
    }
    const k = request.headers.get('Idempotency-Key')
    if (!k || !state.idem.has(k)) {
      const stale = precondition(request, d.version)
      if (stale) return stale
    }
    return idempotent(request, () => cancel(d, body.reason!.trim()))
  }),
]
