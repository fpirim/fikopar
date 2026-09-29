import { http, HttpResponse, type JsonBodyType } from 'msw'
import type {
  CatalogHit,
  CountLinesRequest,
  ImportApply,
  ImportRowsRequest,
  MovementsRequest,
  Part,
  PartCode,
  PartColId,
  PartDepotSettings,
  PartRow,
  PartsRequest,
  Price,
  PriceImport,
  PriceImportFields,
  PriceImportIssue,
  PriceImportMappingWrite,
  PriceImportColumnWrite,
  PriceImportMapping,
  PriceImportPreset,
  PriceImportRow,
  PriceImportTarget,
  PriceWrite,
  StockCount,
  StockCountLine,
  StockCountOpen,
  StockLevel,
  StockMovement,
  StockTransferCreate,
  StockWarning,
} from '../api/types'
import { applyGrid } from './grid'
import {
  countLines,
  createStockDb,
  delta,
  DEPOT_IDS,
  key,
  money4,
  onHandAt,
  priceRow,
  q,
  stamp,
  today,
  type MockCount,
  type MockCountLine,
  type MockMovement,
  type MockPart,
  type StockDb,
} from './catalogData'

const O = '/api/orgs/:orgId'
const XLSX = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
const m2 = (n: number) => (Math.round(n * 100) / 100).toFixed(2)
const err = (status: number, code: string, extra: Record<string, unknown> = {}) =>
  HttpResponse.json({ code, ...extra }, { status })
const etag = (version: number) => ({ ETag: `"${version}"` })
const binary = (accept: string) =>
  new HttpResponse('binary', { headers: { 'Content-Type': accept } })

const st: { db: StockDb; idem: Map<string, { status: number; body: JsonBodyType }> } = {
  db: createStockDb(),
  idem: new Map(),
}

export function resetStock() {
  st.db = createStockDb()
  st.idem = new Map()
  imports.length = 0
}

async function idempotent(request: Request, run: () => { status: number; body: JsonBodyType }) {
  const k = request.headers.get('Idempotency-Key')
  if (!k) return err(400, 'ERR_IDEMPOTENCY_KEY_REQUIRED')
  const hit = st.idem.get(k)
  if (hit) {
    return HttpResponse.json(hit.body, {
      status: hit.status,
      headers: { 'Idempotent-Replayed': 'true' },
    })
  }
  const res = run()
  if (res.status < 400) st.idem.set(k, res)
  return HttpResponse.json(res.body, { status: res.status })
}

function precondition(request: Request, version: number): Response | null {
  const want = request.headers.get('If-Match')
  if (!want) return err(428, 'ERR_PRECONDITION_REQUIRED')
  if (want.replace(/^W\//, '').replace(/"/g, '') !== String(version)) {
    return err(412, 'ERR_VERSION_CONFLICT')
  }
  return null
}

const norm = (s: string) =>
  s
    .toLocaleUpperCase('tr-TR')
    .replace(/İ/g, 'I')
    .replace(/[^0-9A-Z]/g, '')
const fold = (s: string) => s.toLocaleLowerCase('tr-TR').replace(/ı/g, 'i')

const part = (id: number) => st.db.parts.find((p) => p.id === id)

// Stock held by draft documents (documentHandlers registers the lookup).
let docReserved: (partId: number, depotId: number) => number = () => 0
export function setDocumentReservations(fn: (partId: number, depotId: number) => number) {
  docReserved = fn
}
const catalog = (id: number) => st.db.catalogs.find((c) => c.id === id)

function pricesOf(partId: number) {
  return st.db.prices
    .filter((p) => p.partId === partId)
    .sort((a, b) => a.validFrom.localeCompare(b.validFrom))
}

function priceOn(partId: number, date = today) {
  const rows = pricesOf(partId).filter((p) => p.validFrom <= date)
  return rows[rows.length - 1] ?? null
}

// On hand per (part, depot), rebuilt when movements are added.
const hand = { size: -1, db: null as StockDb | null, map: new Map<string, number>() }

function onHand(partId: number, depotId: number) {
  if (hand.size !== st.db.movements.length || hand.db !== st.db) {
    hand.map = new Map()
    for (const m of st.db.movements) {
      const k = key(m.partId, m.depotId)
      hand.map.set(k, (hand.map.get(k) ?? 0) + delta(m))
    }
    hand.size = st.db.movements.length
    hand.db = st.db
  }
  return hand.map.get(key(partId, depotId)) ?? 0
}

function level(p: MockPart, depotId: number): StockLevel {
  const s = st.db.settings.get(key(p.id, depotId))
  const hand = onHand(p.id, depotId)
  const reserved = (s?.reserved ?? 0) + docReserved(p.id, depotId)
  const min = s?.minQty ?? 0
  return {
    partId: p.id,
    stockNo: p.stockNo,
    name: p.name,
    catalogCode: catalog(p.catalogId)!.code,
    depotId,
    onHand: q(hand),
    reserved: q(reserved),
    available: q(hand - reserved),
    minQty: q(min),
    maxQty: s?.maxQty === null || s?.maxQty === undefined ? null : q(s.maxQty),
    shelf: s?.shelf ?? '',
    belowMin: min > 0 && hand < min,
    negative: hand < 0,
    version: s?.version ?? 0,
  }
}

const codesOut = (codes: PartCode[]): PartCode[] =>
  codes.map((c) => ({ ...c, normalizedCode: norm(c.code) }))

// Parts linked by EQUIVALENT codes, either direction, by stock no.
function equivalentsOf(p: MockPart): MockPart[] {
  const ids = new Set(
    p.codes
      .filter((c) => c.kind === 'EQUIVALENT' && c.equivalentPartId)
      .map((c) => c.equivalentPartId!),
  )
  for (const o of st.db.parts) {
    if (o.codes.some((c) => c.kind === 'EQUIVALENT' && c.equivalentPartId === p.id)) ids.add(o.id)
  }
  ids.delete(p.id)
  return [...ids]
    .map((id) => part(id))
    .filter((x): x is MockPart => Boolean(x))
    .sort((a, b) => a.stockNo.localeCompare(b.stockNo))
}

function toPart(p: MockPart): Part {
  const c = catalog(p.catalogId)!
  const current = priceOn(p.id)
  const next = pricesOf(p.id).find((x) => x.validFrom > today)
  return {
    id: p.id,
    orgId: 1,
    catalogId: p.catalogId,
    catalogCode: c.code,
    catalogName: c.name,
    stockNo: p.stockNo,
    name: p.name,
    type: p.type,
    brand: p.brand,
    unit: p.unit,
    packageQty: p.packageQty,
    vatPct: p.vatPct,
    isActive: p.isActive,
    codes: codesOut(p.codes),
    listPrice: current ? money4(current.price) : null,
    listPriceValidFrom: current?.validFrom ?? null,
    nextPrice: next ? { price: money4(next.price), validFrom: next.validFrom } : null,
    onHand: q(DEPOT_IDS.reduce((s, d) => s + onHand(p.id, d), 0)),
    equivalents: equivalentsOf(p).map((e) => ({
      partId: e.id,
      stockNo: e.stockNo,
      name: e.name,
      catalogId: e.catalogId,
      catalogName: catalog(e.catalogId)!.name,
    })),
    createdAt: '2026-01-02T08:00:00Z',
    updatedAt: p.updatedAt,
    version: p.version,
  }
}

function partRow(p: MockPart, depotId: number | null, priceDate = today): PartRow {
  const levels = (depotId ? [depotId] : DEPOT_IDS).map((d) => level(p, d))
  const sum = (k: 'onHand' | 'reserved' | 'available') =>
    q(levels.reduce((s, l) => s + Number(l[k]), 0))
  const one = depotId ? levels[0] : null
  const price = priceOn(p.id, priceDate)
  return {
    id: p.id,
    catalogId: p.catalogId,
    catalogCode: catalog(p.catalogId)!.code,
    stockNo: p.stockNo,
    name: p.name,
    type: p.type,
    brand: p.brand,
    codes: codesOut(p.codes),
    unit: p.unit,
    vatPct: p.vatPct,
    packageQty: p.packageQty,
    listPrice: price ? money4(price.price) : null,
    listPriceValidFrom: price?.validFrom ?? null,
    onHand: sum('onHand'),
    reserved: sum('reserved'),
    available: sum('available'),
    minQty: one ? one.minQty! : null,
    maxQty: one ? (one.maxQty ?? null) : null,
    shelf: one ? one.shelf || null : null,
    belowMin: levels.some((l) => l.belowMin),
    negative: levels.some((l) => l.negative),
    ...(depotId
      ? {}
      : {
          stockByDepot: levels.map((l) => ({
            depotId: l.depotId!,
            depotCode: String(l.depotId),
            onHand: l.onHand,
            available: l.available,
          })),
        }),
    isActive: p.isActive,
    updatedAt: p.updatedAt,
    version: p.version,
  }
}

const PART_COLS: Record<PartColId, (r: PartRow) => string | number | null | undefined> = {
  stockNo: (r) => r.stockNo,
  name: (r) => r.name,
  type: (r) => r.type,
  brand: (r) => r.brand,
  catalogCode: (r) => String(r.catalogCode),
  codes: (r) => (r.codes ?? []).map((c) => norm(c.code)).join(' '),
  unit: (r) => r.unit,
  vatPct: (r) => Number(r.vatPct),
  packageQty: (r) => r.packageQty,
  listPrice: (r) =>
    r.listPrice === null || r.listPrice === undefined ? null : Number(r.listPrice),
  listPriceValidFrom: (r) => r.listPriceValidFrom,
  onHand: (r) => Number(r.onHand),
  reserved: (r) => Number(r.reserved),
  available: (r) => Number(r.available),
  minQty: (r) => (r.minQty === null || r.minQty === undefined ? null : Number(r.minQty)),
  maxQty: (r) => (r.maxQty === null || r.maxQty === undefined ? null : Number(r.maxQty)),
  shelf: (r) => r.shelf,
  isActive: (r) => String(r.isActive),
  updatedAt: (r) => r.updatedAt,
}

function partsGrid(req: PartsRequest) {
  const depotId = req.depotId ?? null
  const scope = st.db.parts.filter(
    (p) => (req.includeInactive || p.isActive) && (!req.catalogId || p.catalogId === req.catalogId),
  )
  const all = scope.map((p) => partRow(p, depotId, req.priceDate))
  const stock = req.stock ?? 'ALL'
  const kept = all.filter((r) => {
    if (stock === 'IN_STOCK') return Number(r.onHand) > 0
    if (stock === 'BELOW_MIN') return r.belowMin
    if (stock === 'NEGATIVE') return r.negative
    if (stock === 'BELOW_MIN_OR_NEGATIVE') return r.belowMin || r.negative
    return true
  })
  const used = [...Object.keys(req.filterModel ?? {}), ...(req.sortModel ?? []).map((s) => s.colId)]
  if (used.some((c) => !c || !(c in PART_COLS))) return null
  // `codes` compares normalized codes.
  const filterModel = { ...req.filterModel }
  if (filterModel.codes) {
    filterModel.codes = {
      ...filterModel.codes,
      filter: norm(String(filterModel.codes.filter ?? '')),
    }
  }
  const sortModel = req.sortModel?.length
    ? req.sortModel
    : [
        { colId: 'stockNo' as const, sort: 'asc' as const },
        { colId: 'catalogCode' as const, sort: 'asc' as const },
      ]
  const { rows, filtered } = applyGrid(kept, { ...req, filterModel, sortModel }, PART_COLS, [
    'stockNo',
    'name',
    'brand',
    'codes',
  ])
  const sum = (k: 'onHand' | 'reserved' | 'available') =>
    q(filtered.reduce((s, r) => s + Number(r[k]), 0))
  return {
    rows,
    totalCount: filtered.length,
    unfilteredCount: kept.length,
    totals: { onHand: sum('onHand'), reserved: sum('reserved'), available: sum('available') },
  }
}

type Rank = 1 | 2 | 3 | 4

function hitOf(
  p: MockPart,
  matchKind: CatalogHit['matchKind'],
  matched: string | null,
  date: string,
): CatalogHit {
  const price = priceOn(p.id, date)
  const stock = DEPOT_IDS.map((d) => {
    const l = level(p, d)
    return { depotId: d, onHand: l.onHand, available: l.available }
  })
  const equivalents = p.codes
    .filter((c) => c.kind === 'EQUIVALENT' && c.equivalentPartId)
    .map((c) => part(c.equivalentPartId!)!)
    .filter(Boolean)
    .map((e) => ({
      partId: e.id,
      stockNo: e.stockNo,
      catalogCode: catalog(e.catalogId)!.code,
      onHandTotal: q(DEPOT_IDS.reduce((s, d) => s + onHand(e.id, d), 0)),
    }))
  return {
    partId: p.id,
    catalogId: p.catalogId,
    catalogCode: catalog(p.catalogId)!.code,
    stockNo: p.stockNo,
    name: p.name,
    brand: p.brand,
    type: p.type,
    codes: codesOut(p.codes),
    matchedCode: matched,
    matchKind,
    equivalents,
    stock,
    onHandTotal: q(stock.reduce((s, x) => s + Number(x.onHand), 0)),
    listPrice: price ? money4(price.price) : null,
    listPriceValidFrom: price?.validFrom ?? null,
    vatPct: p.vatPct,
    unit: p.unit,
    packageQty: p.packageQty,
  }
}

// Ranked search: the whole query against the stock no and codes (1 exact, 2 prefix, 3 substring),
// else every word in the name or in a code (4).
const KINDS: PartCode['kind'][] = ['MANUFACTURER', 'OEM', 'ALT', 'EQUIVALENT']

export function catalogSearch(
  text: string,
  opts: { catalogId?: number | null; date?: string; inStockDepotId?: number | null } = {},
) {
  const date = opts.date || today
  const nq = norm(text)
  const words = text.split(/\s+/).filter(Boolean)
  const found: { rank: Rank; hit: CatalogHit }[] = []
  for (const p of st.db.parts) {
    if (!p.isActive) continue
    if (opts.catalogId && p.catalogId !== opts.catalogId) continue
    if (opts.inStockDepotId && Number(level(p, opts.inStockDepotId).available) <= 0) continue
    const codes: [CatalogHit['matchKind'], string][] = [
      ['STOCK_NO', p.stockNo],
      ...KINDS.flatMap((k) =>
        p.codes
          .filter((c) => c.kind === k)
          .map((c) => [c.kind, c.code] as [CatalogHit['matchKind'], string]),
      ),
    ]
    let best: { rank: Rank; kind: CatalogHit['matchKind']; code: string } | null = null
    for (const [kind, code] of nq ? codes : []) {
      const n = norm(code)
      const rank: Rank | null =
        n === nq ? 1 : n.startsWith(nq) ? 2 : nq.length >= 3 && n.includes(nq) ? 3 : null
      if (rank && (!best || rank < best.rank)) best = { rank, kind, code }
    }
    if (!best) {
      const name = fold(p.name)
      const inCode = (w: string) =>
        norm(w) !== '' && codes.some(([, c]) => norm(c).includes(norm(w)))
      const all = words.every((w) => name.includes(fold(w)) || inCode(w))
      if (!all || !words.length) continue
      const code = codes.find(([, c]) => words.some((w) => norm(w) && norm(c).includes(norm(w))))
      best = { rank: 4, kind: 'NAME', code: code?.[1] ?? '' }
    }
    found.push({ rank: best.rank, hit: hitOf(p, best.kind, best.code || null, date) })
  }
  found.sort(
    (a, b) =>
      a.rank - b.rank ||
      a.hit.stockNo.localeCompare(b.hit.stockNo) ||
      a.hit.catalogCode - b.hit.catalogCode,
  )
  return { hits: found.map((f) => f.hit), date }
}

function movementOut(m: MockMovement, running: number | null): StockMovement {
  const p = part(m.partId)!
  return {
    id: m.id,
    partId: m.partId,
    stockNo: p.stockNo,
    name: p.name,
    depotId: m.depotId,
    kind: m.kind,
    label: m.label,
    qty: q(m.qty),
    delta: q(delta(m)),
    qtyIn: delta(m) > 0 ? q(m.qty) : null,
    qtyOut: delta(m) < 0 ? q(m.qty) : null,
    unitCost: m.unitCost === null ? null : money4(m.unitCost),
    sourceType: m.sourceType,
    sourceId: m.transferId ? String(m.transferId) : null,
    sourceNo: m.sourceNo,
    documentId: m.documentId,
    documentType: m.documentType ?? null,
    reversal: m.reversal ?? false,
    transferId: m.transferId,
    countId: m.countId,
    sicilName: m.sicilName,
    otherDepotId: m.otherDepotId,
    runningOnHand: running === null ? null : q(running),
    date: m.date,
    occurredAt: m.occurredAt,
  }
}

function movementsGrid(req: MovementsRequest) {
  const depotId = req.depotId ?? null
  const scoped = st.db.movements
    .filter((m) => (!req.partId || m.partId === req.partId) && (!depotId || m.depotId === depotId))
    .sort((a, b) => a.date.localeCompare(b.date) || a.id - b.id)
  const sort = req.sortModel?.[0]
  const byDate = !sort || sort.colId === 'date'
  const withRunning = Boolean(req.partId) && byDate && (req.sortModel?.length ?? 0) <= 1
  let run = 0
  const all = scoped.map((m) => {
    run += delta(m)
    return movementOut(m, withRunning ? run : null)
  })
  type R = StockMovement
  const cols: Record<string, (r: R) => string | number | null | undefined> = {
    date: (r) => r.date,
    kind: (r) => r.kind,
    label: (r) => r.label,
    sourceNo: (r) => r.sourceNo,
    sicilName: (r) => r.sicilName,
    stockNo: (r) => r.stockNo,
    name: (r) => r.name,
    depotId: (r) => String(r.depotId),
    qty: (r) => Number(r.qty),
    qtyIn: (r) => (r.qtyIn == null ? null : Number(r.qtyIn)),
    qtyOut: (r) => (r.qtyOut == null ? null : Number(r.qtyOut)),
    delta: (r) => Number(r.delta),
    unitCost: (r) => (r.unitCost ? Number(r.unitCost) : null),
  }
  const sortModel = req.sortModel?.length
    ? req.sortModel
    : [{ colId: 'date', sort: 'desc' as const }]
  // Same-day rows keep ledger (id) order under the date sort.
  const ordered =
    sortModel[0].colId === 'date' && sortModel[0].sort === 'desc' ? [...all].reverse() : all
  const { rows, filtered } = applyGrid(
    ordered,
    { ...req, sortModel: sortModel[0].colId === 'date' ? [] : sortModel },
    cols,
    ['sourceNo', 'sicilName', 'stockNo', 'name'],
  )
  const range = req.filterModel?.date
  let opening: number | null = null
  let closing: number | null = null
  if (req.partId) {
    const from = range?.dateFrom?.slice(0, 10) ?? null
    const to = (range?.type === 'inRange' ? range?.dateTo : range?.dateFrom)?.slice(0, 10) ?? null
    const upTo = (d: string | null, strict: boolean) =>
      scoped
        .filter((m) => d === null || (strict ? m.date < d : m.date <= d))
        .reduce((s, m) => s + delta(m), 0)
    opening =
      from && (range?.type === 'inRange' || range?.type === 'greaterThan') ? upTo(from, true) : 0
    closing = to && range?.type !== 'greaterThan' ? upTo(to, false) : upTo(null, false)
  }
  return {
    rows,
    totalCount: filtered.length,
    unfilteredCount: all.length,
    totals: {
      qtyIn: q(filtered.reduce((s, r) => s + Math.max(Number(r.delta), 0), 0)),
      qtyOut: q(filtered.reduce((s, r) => s + Math.max(-Number(r.delta), 0), 0)),
    },
    openingOnHand: opening === null ? null : q(opening),
    closingOnHand: closing === null ? null : q(closing),
  }
}

function negativeWarnings(pairs: [number, number][]): StockWarning[] {
  const seen = new Set<string>()
  const out: StockWarning[] = []
  for (const [partId, depotId] of pairs) {
    const k = key(partId, depotId)
    if (seen.has(k)) continue
    seen.add(k)
    const after = onHand(partId, depotId)
    if (after < 0) {
      out.push({
        code: 'NEGATIVE_STOCK',
        partId,
        stockNo: part(partId)!.stockNo,
        depotId,
        onHandAfter: q(after),
      })
    }
  }
  return out
}

function transfer(body: StockTransferCreate): { status: number; body: JsonBodyType } {
  if (body.fromDepotId === body.toDepotId) return { status: 422, body: { code: 'ERR_SAME_DEPOT' } }
  if (![body.fromDepotId, body.toDepotId].every((d) => DEPOT_IDS.includes(d))) {
    return { status: 422, body: { code: 'ERR_DEPOT_NOT_FOUND', field: 'toDepotId' } }
  }
  const ids = new Set<number>()
  for (const [i, l] of body.lines.entries()) {
    const p = part(l.partId)
    if (!p)
      return { status: 422, body: { code: 'ERR_PART_NOT_FOUND', field: `lines[${i}].partId` } }
    if (!p.isActive)
      return { status: 422, body: { code: 'ERR_PART_INACTIVE', field: `lines[${i}].partId` } }
    if (ids.has(l.partId))
      return { status: 422, body: { code: 'ERR_DUPLICATE_LINE', field: `lines[${i}].partId` } }
    if (!(Number(l.qty) > 0))
      return { status: 400, body: { code: 'ERR_INVALID_REQUEST', field: `lines[${i}].qty` } }
    ids.add(l.partId)
  }
  const id = st.db.seq.transfer++
  const transferNo = `TRF-${body.date.slice(0, 4)}-${body.fromDepotId}-${String(id).padStart(6, '0')}`
  const made: MockMovement[] = []
  for (const l of body.lines) {
    for (const [depotId, kind, other] of [
      [body.fromDepotId, 'TRANSFER_OUT', body.toDepotId],
      [body.toDepotId, 'TRANSFER_IN', body.fromDepotId],
    ] as const) {
      const m: MockMovement = {
        id: st.db.seq.movement++,
        partId: l.partId,
        depotId,
        kind,
        label: 'TRANSFER',
        qty: Number(l.qty),
        unitCost: null,
        sourceType: 'transfer',
        sourceNo: transferNo,
        sicilName: null,
        otherDepotId: other,
        transferId: id,
        countId: null,
        documentId: null,
        date: body.date,
        occurredAt: `${body.date}T12:00:00Z`,
      }
      st.db.movements.push(m)
      made.push(m)
    }
  }
  st.db.transfers.push({ id, transferNo, movementIds: made.map((m) => m.id), note: body.note })
  return {
    status: 201,
    body: {
      ...body,
      id,
      transferNo,
      createdBy: 'demo',
      createdAt: `${today}T12:00:00Z`,
      movements: made.map((m) => movementOut(m, null)),
      warnings: negativeWarnings(body.lines.map((l) => [l.partId, body.fromDepotId])),
    } as unknown as JsonBodyType,
  }
}

// Last purchase cost from posted purchase invoices (documentHandlers registers the lookup).
let lastPurchase: (partId: number, date: string) => number | null = () => null
export function setLastPurchaseCost(fn: (partId: number, date: string) => number | null) {
  lastPurchase = fn
}

function lineOut(c: MockCount, l: MockCountLine): StockCountLine {
  const now = onHand(l.partId, c.depotId)
  const moved = now - l.systemQty
  const diff = l.countedQty === null ? null : l.countedQty - l.systemQty
  // While open the cost is read live; a finalized line keeps its snapshot.
  const live = c.status === 'OPEN' ? lastPurchase(l.partId, c.countDate) : null
  const source = live === null ? l.costSource : 'LAST_PURCHASE'
  const cost = live ?? (l.unitCost === null ? null : Number(l.unitCost))
  const value = diff === null || cost === null || source === 'NONE' ? null : diff * cost
  return {
    ...l,
    unitCost: cost === null ? null : money4(cost),
    costSource: source,
    contended: undefined,
    systemQty: q(l.systemQty),
    countedQty: l.countedQty === null ? null : q(l.countedQty),
    difference: diff === null ? null : q(diff),
    movedSinceOpen: q(c.status === 'OPEN' ? moved : 0),
    onHandAfterPost: diff === null ? null : q(now + diff),
    value: value === null ? null : m2(value),
  } as StockCountLine
}

function countOut(c: MockCount): StockCount {
  const lines = c.lines.map((l) => lineOut(c, l))
  const diffs = lines.filter((l) => l.difference !== null && Number(l.difference) !== 0)
  const values = diffs.map((l) => Number(l.value ?? 0))
  const { lines: _lines, ...head } = c
  void _lines
  const finalized = c.status === 'FINALIZED' && !c.lines.length
  return {
    ...head,
    lineCount: finalized ? 1280 : lines.length,
    countedCount: finalized ? 1280 : lines.filter((l) => l.countedQty !== null).length,
    differenceCount: finalized ? c.differenceCount : diffs.length,
    differenceValue: finalized ? c.differenceValue : m2(values.reduce((s, v) => s + v, 0)),
    valueIn: finalized ? c.valueIn : m2(values.filter((v) => v > 0).reduce((s, v) => s + v, 0)),
    valueOut: finalized ? c.valueOut : m2(-values.filter((v) => v < 0).reduce((s, v) => s + v, 0)),
    unvaluedLines: finalized
      ? c.unvaluedLines
      : diffs.filter((l) => l.costSource !== 'LAST_PURCHASE').length,
  }
}

const count = (id: number) => st.db.counts.find((c) => c.id === id)

function countLinesGrid(c: MockCount, req: CountLinesRequest) {
  const only = req.only ?? 'ALL'
  const all = c.lines
    .filter((l) => {
      if (req.shelfFrom && l.shelf < req.shelfFrom) return false
      if (req.shelfTo && l.shelf > req.shelfTo && !l.shelf.startsWith(req.shelfTo)) return false
      if (only === 'NOT_COUNTED') return l.countedQty === null
      if (only === 'COUNTED') return l.countedQty !== null
      if (only === 'DIFFERENCES') return l.countedQty !== null && l.countedQty !== l.systemQty
      return true
    })
    .map((l) => lineOut(c, l))
  type R = StockCountLine
  const num = (v: string | null | undefined) => (v === null || v === undefined ? null : Number(v))
  const cols: Record<string, (r: R) => string | number | null | undefined> = {
    shelf: (r) => r.shelf,
    stockNo: (r) => r.stockNo,
    name: (r) => r.name,
    brand: (r) => r.brand,
    systemQty: (r) => Number(r.systemQty),
    countedQty: (r) => num(r.countedQty),
    difference: (r) => num(r.difference),
    movedSinceOpen: (r) => Number(r.movedSinceOpen),
    unitCost: (r) => num(r.unitCost),
    costSource: (r) => r.costSource,
    value: (r) => num(r.value),
    countedBy: (r) => r.countedBy,
    countedAt: (r) => r.countedAt,
  }
  const sortModel = req.sortModel?.length
    ? req.sortModel
    : [
        { colId: 'shelf', sort: 'asc' as const },
        { colId: 'stockNo', sort: 'asc' as const },
      ]
  const { rows, filtered } = applyGrid(all, { ...req, sortModel }, cols, ['stockNo', 'name'])
  const sum = (f: (r: R) => number | null) => filtered.reduce((s, r) => s + (f(r) ?? 0), 0)
  const values = filtered.map((r) => num(r.value) ?? 0)
  return {
    rows,
    totalCount: filtered.length,
    unfilteredCount: c.lines.length,
    totals: {
      systemQty: q(sum((r) => Number(r.systemQty))),
      countedQty: q(sum((r) => num(r.countedQty))),
      difference: q(sum((r) => num(r.difference))),
      value: m2(values.reduce((s, v) => s + v, 0)),
      valueIn: m2(values.filter((v) => v > 0).reduce((s, v) => s + v, 0)),
      valueOut: m2(-values.filter((v) => v < 0).reduce((s, v) => s + v, 0)),
    },
  }
}

function openCount(body: StockCountOpen): { status: number; body: JsonBodyType } {
  if (!DEPOT_IDS.includes(body.depotId)) {
    return { status: 422, body: { code: 'ERR_DEPOT_NOT_FOUND', field: 'depotId' } }
  }
  if (st.db.counts.some((c) => c.depotId === body.depotId && c.status === 'OPEN')) {
    return { status: 409, body: { code: 'ERR_STOCK_COUNT_OPEN_EXISTS' } }
  }
  const id = st.db.seq.count++
  const year = Number(body.countDate.slice(0, 4))
  const n = st.db.counts.filter((c) => c.depotId === body.depotId && c.year === year).length + 1
  const startedAt = `${today}T12:00:00Z`
  const c: MockCount = {
    ...body,
    id,
    countNo: `SAY-${year}-${body.depotId}-${String(n).padStart(6, '0')}`,
    year,
    status: 'OPEN',
    startedAt,
    differenceCount: 0,
    differenceValue: '0.00',
    valueIn: '0.00',
    valueOut: '0.00',
    unvaluedLines: 0,
    uncounted: null,
    createdBy: 'demo',
    finalizedAt: null,
    cancelledAt: null,
    cancelReason: null,
    version: 1,
    lines: countLines(st.db, body.depotId, startedAt, body.shelfFrom, body.shelfTo),
  }
  st.db.counts.unshift(c)
  return { status: 201, body: countOut(c) as unknown as JsonBodyType }
}

function finalize(
  c: MockCount,
  uncounted: 'SKIP' | 'ZERO',
): { status: number; body: JsonBodyType } {
  if (c.status !== 'OPEN') return { status: 409, body: { code: 'ERR_STOCK_COUNT_NOT_OPEN' } }
  const touched: [number, number][] = []
  let made = 0
  for (const l of c.lines) {
    const counted = l.countedQty ?? (uncounted === 'ZERO' ? 0 : null)
    if (counted === null) continue
    if (l.countedQty === null) stamp(l, 0, 'demo', `${today}T12:00:00Z`)
    const diff = counted - l.systemQty
    if (diff === 0) continue
    st.db.movements.push({
      id: st.db.seq.movement++,
      partId: l.partId,
      depotId: c.depotId,
      kind: diff > 0 ? 'ADJUST_IN' : 'ADJUST_OUT',
      label: 'COUNT',
      qty: Math.abs(diff),
      unitCost: l.unitCost === null ? null : Number(l.unitCost),
      sourceType: 'stock_count',
      sourceNo: c.countNo,
      sicilName: null,
      otherDepotId: null,
      transferId: null,
      countId: c.id,
      documentId: null,
      date: c.countDate,
      occurredAt: `${today}T12:00:00Z`,
    })
    touched.push([l.partId, c.depotId])
    made++
  }
  const head = countOut(c)
  Object.assign(c, {
    status: 'FINALIZED',
    uncounted,
    finalizedAt: `${today}T12:00:00Z`,
    version: c.version + 1,
    differenceCount: head.differenceCount,
    differenceValue: head.differenceValue,
    valueIn: head.valueIn,
    valueOut: head.valueOut,
    unvaluedLines: head.unvaluedLines,
  })
  return {
    status: 200,
    body: {
      count: countOut(c),
      movementCount: made,
      warnings: negativeWarnings(touched),
    } as unknown as JsonBodyType,
  }
}

// ---- Price list imports ----------------------------------------------------------------------

interface MockImport extends Omit<PriceImport, 'mapping'> {
  mapping?: PriceImportMappingWrite | null
  rows: PriceImportRow[]
  raw: string[][]
  applied?: boolean
}

const imports: MockImport[] = []

const columns = (c: Record<string, PriceImportColumnWrite>) =>
  c as PriceImportMappingWrite['columns']

const PRESETS = [
  {
    code: 'LISAKT',
    name: 'Legacy LISAKT price list (COBOL, fixed width, CP857)',
    mapping: {
      preset: 'LISAKT',
      format: 'FIXED_WIDTH',
      encoding: 'CP857',
      decimalSeparator: '.',
      dateFormat: 'yyyymmdd',
      headerRow: 0,
      listSeparator: '|',
      columns: columns({
        op: { start: 1, length: 1 },
        catalogCode: { start: 2, length: 4 },
        code: { start: 6, length: 15 },
        manufacturerCode: { start: 21, length: 30 },
        oem: { start: 51, length: 30 },
        type: { start: 81, length: 30 },
        description: { start: 111, length: 60 },
        brand: { start: 171, length: 30 },
        price: { start: 233, length: 14, scale: 4 },
        validFrom: { start: 257, length: 8 },
        packQty: { start: 265, length: 5 },
        equivalentCatalogCode: { start: 285, length: 4 },
        equivalentCode: { start: 289, length: 15 },
      }),
    },
    notes:
      'Record 319 bytes (OK-OP, ST-KAYIT, BAG). Ignored: MIN/MAX (201-232; per depot now), NOTE, SIRA, BISKONTO/BKDV. validFrom 00000000 → job validFrom. op 0 = DELETE, 1 insert, 2 update.',
  },
] as unknown as PriceImportPreset[]

const ALIASES: Record<string, string[]> = {
  code: ['code', 'stock no', 'stok no', 'stok kodu', 'stno'],
  description: ['description', 'açıklama', 'aciklama', 'name', 'ad'],
  brand: ['brand', 'marka'],
  type: ['type', 'tip'],
  manufacturerCode: ['manufacturer code', 'üretici kodu', 'prcno'],
  oem: ['oem', 'oem no', 'oemno'],
  altCodes: ['alt codes', 'alternatif', 'eski kod'],
  price: ['price', 'fiyat', 'liste fiyatı', 'list price'],
  validFrom: ['valid from', 'geçerlilik', 'tarih'],
  packQty: ['pack qty', 'ambalaj', 'paket'],
  vatPct: ['vat', 'kdv'],
}

function parseCsv(text: string, delimiter?: string): string[][] {
  const lines = text
    .replace(/^\uFEFF/, '')
    .split(/\r?\n/)
    .filter((l) => l.trim())
  const sep =
    delimiter ||
    ([';', '\t', ','] as const).reduce((a, b) =>
      (lines[0]?.split(b).length ?? 0) > (lines[0]?.split(a).length ?? 0) ? b : a,
    )
  return lines.map((l) => l.split(sep).map((c) => c.trim().replace(/^"|"$/g, '')))
}

function autoMap(headers: string[]): PriceImportMappingWrite['columns'] {
  const cols: Record<string, { header: string }> = {}
  for (const [target, names] of Object.entries(ALIASES)) {
    const h = headers.find((x) => names.some((n) => fold(n) === fold(x.trim())))
    if (h) cols[target] = { header: h }
  }
  return columns(cols)
}

function num(text: string | undefined, sep: ',' | '.'): number | null {
  if (!text) return null
  const t = sep === ',' ? text.replace(/\./g, '').replace(',', '.') : text.replace(/,/g, '')
  return /^-?\d+(\.\d+)?$/.test(t) ? Number(t) : Number.NaN
}

function day(text: string | undefined): string | null | undefined {
  if (!text) return null
  const d = /^(\d{2})\.(\d{2})\.(\d{4})$/.exec(text) ?? /^(\d{4})-(\d{2})-(\d{2})$/.exec(text)
  if (!d) return undefined
  return text.includes('.') ? `${d[3]}-${d[2]}-${d[1]}` : text
}

function buildRows(job: MockImport) {
  const mapping: PriceImportMappingWrite = job.mapping ?? {}
  const sep = mapping.decimalSeparator ?? ','
  const [head, ...data] = job.raw
  const col = (target: PriceImportTarget) => {
    const c = mapping.columns?.[target]
    if (!c) return -1
    if (c.index) return c.index - 1
    return head.findIndex((h) => fold(h) === fold(c.header ?? ''))
  }
  const idx = Object.fromEntries(
    (
      [
        'code',
        'description',
        'brand',
        'type',
        'oem',
        'altCodes',
        'price',
        'validFrom',
        'packQty',
      ] as const
    ).map((t) => [t, col(t)]),
  ) as Record<string, number>
  const cat = job.catalogId
  const seen = new Map<string, number>()
  const rows: PriceImportRow[] = []
  const inFile = new Set<number>()
  data.forEach((cells, i) => {
    const get = (t: string) => (idx[t] >= 0 ? cells[idx[t]]?.trim() || undefined : undefined)
    const stockNo = get('code') ?? null
    const errors: PriceImportIssue[] = []
    const warnings: PriceImportIssue[] = []
    if (!stockNo) errors.push({ field: 'code', code: 'REQUIRED', value: null })
    const price = num(get('price'), sep)
    if (price === null) errors.push({ field: 'price', code: 'REQUIRED', value: null })
    else if (Number.isNaN(price))
      errors.push({ field: 'price', code: 'INVALID_NUMBER', value: get('price') ?? null })
    else if (price < 0)
      errors.push({ field: 'price', code: 'NEGATIVE_PRICE', value: get('price') ?? null })
    const vf = day(get('validFrom'))
    if (vf === undefined)
      errors.push({ field: 'validFrom', code: 'INVALID_DATE', value: get('validFrom') ?? null })
    const validFrom = vf || job.validFrom
    if (stockNo && seen.has(stockNo)) {
      errors.push({ field: 'code', code: 'DUPLICATE_IN_FILE', value: stockNo })
    }
    if (stockNo) seen.set(stockNo, i)
    const existing = stockNo
      ? st.db.parts.find((p) => p.catalogId === cat && p.stockNo === stockNo)
      : undefined
    if (existing) inFile.add(existing.id)
    const current = existing ? priceOn(existing.id, validFrom) : null
    const file: PriceImportFields = {
      stockNo: stockNo ?? '',
      description: get('description') ?? null,
      brand: get('brand') ?? null,
      type: get('type') ?? null,
      manufacturerCode: null,
      oem: get('oem') ? [get('oem')!] : [],
      altCodes: get('altCodes') ? get('altCodes')!.split('|') : [],
      price: price === null || Number.isNaN(price) ? null : money4(price),
      validFrom,
      packQty: null,
      vatPct: null,
      equivalent: null,
    }
    const changes: PriceImportRow['changes'] = []
    const changedFields: PriceImportTarget[] = []
    if (!existing) changes.push('NEW')
    else {
      if (price !== null && !Number.isNaN(price) && (!current || current.price !== price)) {
        changes.push('PRICE')
        changedFields.push('price')
      }
      if (file.description && file.description !== existing.name) changedFields.push('description')
      if (file.brand && file.brand !== existing.brand) changedFields.push('brand')
      if (changedFields.some((f) => f !== 'price')) changes.push('ATTRIBUTES')
      const latest = pricesOf(existing.id).slice(-1)[0]
      if (latest && validFrom < latest.validFrom && changes.includes('PRICE')) {
        warnings.push({ field: 'validFrom', code: 'BACKDATED', value: validFrom })
      }
      if (!existing.isActive) warnings.push({ code: 'INACTIVE_PART', value: stockNo })
    }
    rows.push({
      id: i + 1,
      rowNo: i + 2,
      partId: existing?.id ?? null,
      stockNo,
      changes,
      file,
      current: existing
        ? {
            stockNo: existing.stockNo,
            description: existing.name,
            brand: existing.brand,
            type: existing.type,
            manufacturerCode: null,
            oem: existing.codes.filter((c) => c.kind === 'OEM').map((c) => c.code),
            altCodes: existing.codes.filter((c) => c.kind === 'ALT').map((c) => c.code),
            price: current ? money4(current.price) : null,
            validFrom: current?.validFrom ?? null,
            packQty: existing.packageQty,
            vatPct: existing.vatPct,
            equivalent: null,
          }
        : null,
      changedFields,
      priceChangePct:
        current && price !== null && !Number.isNaN(price) && current.price !== price
          ? ((price / current.price - 1) * 100).toFixed(1)
          : null,
      errors,
      warnings,
    })
  })
  let id = rows.length
  for (const p of st.db.parts) {
    if (p.catalogId !== cat || !p.isActive || inFile.has(p.id)) continue
    const current = priceOn(p.id)
    rows.push({
      id: ++id,
      rowNo: null,
      partId: p.id,
      stockNo: p.stockNo,
      changes: ['MISSING'],
      file: null,
      current: {
        stockNo: p.stockNo,
        description: p.name,
        brand: p.brand,
        price: current ? money4(current.price) : null,
        validFrom: current?.validFrom ?? null,
        oem: [],
        altCodes: [],
      },
      changedFields: [],
      priceChangePct: null,
      errors: [],
      warnings: [],
    })
  }
  job.rows = rows
  const has = (c: PriceImportRow['changes'][number]) =>
    rows.filter((r) => r.changes.includes(c)).length
  job.counts = {
    rows: data.length,
    new: has('NEW'),
    price: has('PRICE'),
    attributes: has('ATTRIBUTES'),
    unchanged: rows.filter((r) => r.rowNo !== null && !r.changes.length).length,
    missing: has('MISSING'),
    delete: has('DELETE'),
    errors: rows.filter((r) => r.errors.length).length,
    warnings: rows.filter((r) => r.warnings.length).length,
  }
  job.catalogVersion = catalog(cat)!.contentVersion
}

function jobOut(j: MockImport): PriceImport {
  const { rows: _r, raw: _raw, applied: _a, mapping, ...out } = j
  void _r
  void _raw
  void _a
  return { ...out, mapping: mapping ? effective(mapping, j.format) : null }
}

// Response side: the preset and the defaults filled in.
function effective(m: PriceImportMappingWrite, format: PriceImport['format']): PriceImportMapping {
  return {
    ...m,
    format: m.format ?? format,
    encoding: m.encoding ?? 'UTF-8',
    decimalSeparator: m.decimalSeparator ?? ',',
    dateFormat: m.dateFormat ?? 'dd.mm.yyyy',
    headerRow: m.headerRow ?? 1,
    listSeparator: m.listSeparator ?? '|',
    columns: m.columns ?? {},
  }
}

function resolveMapping(job: MockImport) {
  const cols = job.mapping?.columns ?? {}
  const missing = (['code', 'price'] as const).filter((t) => !cols[t])
  job.unmappedTargets = missing
  if (missing.length) {
    job.status = 'NEEDS_MAPPING'
    job.counts = {
      rows: job.raw.length - 1,
      new: 0,
      price: 0,
      attributes: 0,
      unchanged: 0,
      missing: 0,
      delete: 0,
      errors: 0,
      warnings: 0,
    }
    return
  }
  buildRows(job)
  job.status = 'READY'
}

// LISAKT: fixed-width records; the mock reads code, description, brand, price and date.
function lisaktRows(text: string): string[][] {
  const head = ['code', 'description', 'brand', 'price', 'validFrom']
  const rows = text
    .split(/\r?\n/)
    .filter((l) => l.length >= 250)
    .map((l) => {
      const p = Number(l.slice(232, 246)) / 10000
      const d = l.slice(256, 264)
      return [
        l.slice(5, 20).trim(),
        l.slice(110, 170).trim(),
        l.slice(170, 200).trim(),
        p.toFixed(2).replace('.', ','),
        d === '00000000' ? '' : `${d.slice(6, 8)}.${d.slice(4, 6)}.${d.slice(0, 4)}`,
      ]
    })
  return [head, ...rows]
}

async function createImport(request: Request): Promise<{ status: number; body: JsonBodyType }> {
  const form = await request.formData()
  const file = form.get('file') as File | null
  if (!file) return { status: 400, body: { code: 'ERR_NO_FILE_UPLOADED' } }
  if (file.size > 10 * 1024 * 1024) return { status: 400, body: { code: 'ERR_FILE_TOO_LARGE' } }
  const catalogId = Number(form.get('catalogId'))
  if (!catalog(catalogId))
    return { status: 422, body: { code: 'ERR_CATALOG_NOT_FOUND', field: 'catalogId' } }
  const mappingPart = form.get('mapping')
  const mapping = mappingPart
    ? (JSON.parse(
        typeof mappingPart === 'string' ? mappingPart : await mappingPart.text(),
      ) as PriceImportMappingWrite)
    : undefined
  const text = await file.text()
  const name = file.name.toLowerCase()
  const format =
    mapping?.preset === 'LISAKT' ? 'FIXED_WIDTH' : name.endsWith('.xlsx') ? 'XLSX' : 'CSV'
  const raw =
    format === 'FIXED_WIDTH'
      ? lisaktRows(text)
      : format === 'XLSX'
        ? [
            ['Stok No', 'Açıklama', 'Birim fiyat'],
            ['HP-3774', 'Hydraulic pump, 3-cyl.', '12.900,00'],
          ]
        : parseCsv(text, mapping?.delimiter)
  const headers = raw[0] ?? []
  const job: MockImport = {
    id: 700 + imports.length + 1,
    catalogId,
    catalogCode: catalog(catalogId)!.code,
    fileName: file.name,
    fileSha256: 'a'.repeat(64),
    fileSize: file.size,
    format,
    validFrom: String(form.get('validFrom')),
    status: 'PARSING',
    mapping: {
      ...(format === 'FIXED_WIDTH'
        ? PRESETS[0].mapping
        : { format, encoding: 'UTF-8', decimalSeparator: ',', headerRow: 1 }),
      ...mapping,
      columns:
        format === 'FIXED_WIDTH'
          ? columns(Object.fromEntries(headers.map((h) => [h, { header: h }])))
          : { ...autoMap(headers), ...mapping?.columns },
    },
    headers,
    sample: raw.slice(1, 6),
    unmappedTargets: [],
    counts: {
      rows: 0,
      new: 0,
      price: 0,
      attributes: 0,
      unchanged: 0,
      missing: 0,
      delete: 0,
      errors: 0,
      warnings: 0,
    },
    catalogVersion: null,
    duplicateOfImportId:
      imports.find((j) => j.catalogId === catalogId && j.applied && j.fileName === file.name)?.id ??
      null,
    result: null,
    createdBy: 'demo',
    createdAt: `${today}T12:00:00Z`,
    expiresAt: '2026-10-06T12:00:00Z',
    appliedBy: null,
    appliedAt: null,
    version: 1,
    rows: [],
    raw,
  }
  resolveMapping(job)
  imports.unshift(job)
  return { status: 201, body: jobOut(job) as unknown as JsonBodyType }
}

function applyImport(job: MockImport, body: ImportApply): { status: number; body: JsonBodyType } {
  if (job.status !== 'READY') return { status: 409, body: { code: 'ERR_INVALID_STATE_TRANSITION' } }
  const cat = catalog(job.catalogId)!
  if (job.catalogVersion !== cat.contentVersion)
    return { status: 409, body: { code: 'ERR_IMPORT_STALE' } }
  if (job.counts.errors && !body.skipErrorRows)
    return { status: 422, body: { code: 'ERR_IMPORT_HAS_ERRORS' } }
  const result = {
    partsCreated: 0,
    pricesInserted: 0,
    partsUpdated: 0,
    partsDeactivated: 0,
    rowsSkipped: 0,
  }
  for (const r of job.rows) {
    if (r.errors.length) {
      result.rowsSkipped++
      continue
    }
    const f = r.file
    if (r.changes.includes('NEW') && f) {
      const id = Math.max(...st.db.parts.map((p) => p.id)) + 1
      st.db.parts.push({
        id,
        catalogId: job.catalogId,
        stockNo: f.stockNo!,
        name: f.description ?? f.stockNo!,
        type: f.type ?? null,
        brand: f.brand ?? null,
        unit: 'PCS',
        packageQty: 1,
        vatPct: '20',
        isActive: true,
        codes: (f.oem ?? []).map((c) => ({ kind: 'OEM', code: c, equivalentPartId: null })),
        cost: null,
        costSource: 'LIST_PRICE',
        updatedAt: `${today}T12:00:00Z`,
        version: 1,
      })
      r.partId = id
      result.partsCreated++
    }
    if ((r.changes.includes('NEW') || r.changes.includes('PRICE')) && f?.price && r.partId) {
      st.db.prices.push({
        id: st.db.seq.price++,
        partId: r.partId,
        price: Number(f.price),
        validFrom: f.validFrom ?? job.validFrom,
        source: 'IMPORT',
        importId: job.id,
        createdBy: 'demo',
        createdAt: `${today}T12:00:00Z`,
      })
      result.pricesInserted++
    }
    if (r.changes.includes('ATTRIBUTES') && body.applyAttributes !== false && r.partId && f) {
      const p = part(r.partId)!
      if (f.description) p.name = f.description
      if (f.brand) p.brand = f.brand
      p.version++
      result.partsUpdated++
    }
    if (r.changes.includes('MISSING') && body.missing === 'DEACTIVATE' && r.partId) {
      part(r.partId)!.isActive = false
      result.partsDeactivated++
    }
  }
  cat.contentVersion++
  cat.lastImportAt = `${today}T12:00:00Z`
  cat.partCount = st.db.parts.filter((p) => p.catalogId === cat.id && p.isActive).length
  Object.assign(job, {
    status: 'APPLIED',
    result,
    applied: true,
    appliedBy: 'demo',
    appliedAt: `${today}T12:00:00Z`,
    version: job.version + 1,
  })
  return { status: 200, body: jobOut(job) as unknown as JsonBodyType }
}

function importRowsGrid(job: MockImport, req: ImportRowsRequest) {
  const change = req.change ?? 'ALL'
  const all = job.rows.filter((r) => {
    if (change === 'ALL') return true
    if (change === 'ERROR') return r.errors.length > 0
    if (change === 'WARNING') return r.warnings.length > 0
    if (change === 'UNCHANGED') return r.rowNo !== null && !r.changes.length
    return r.changes.includes(change)
  })
  type R = PriceImportRow
  const cols: Record<string, (r: R) => string | number | null | undefined> = {
    rowNo: (r) => r.rowNo ?? 1e9,
    stockNo: (r) => r.stockNo,
    description: (r) => r.file?.description ?? r.current?.description,
    brand: (r) => r.file?.brand ?? r.current?.brand,
    price: (r) => (r.file?.price ? Number(r.file.price) : null),
    currentPrice: (r) => (r.current?.price ? Number(r.current.price) : null),
    priceChangePct: (r) => (r.priceChangePct ? Number(r.priceChangePct) : null),
    validFrom: (r) => r.file?.validFrom,
    changes: (r) => r.changes.join(' '),
    errors: (r) => r.errors.map((e) => e.code).join(' '),
  }
  const sortModel = req.sortModel?.length
    ? req.sortModel
    : [{ colId: 'rowNo', sort: 'asc' as const }]
  const { rows, filtered } = applyGrid(all, { ...req, sortModel }, cols, ['stockNo', 'description'])
  return { rows, totalCount: filtered.length, unfilteredCount: all.length, totals: {} }
}

const imp = (id: unknown) => imports.find((j) => j.id === Number(id))

// Below minimum / negative for the dashboard: negative first, then onHand − min.
export function belowMinLevels(depotId: number | null) {
  const out: StockLevel[] = []
  for (const p of st.db.parts) {
    if (!p.isActive) continue
    for (const d of depotId ? [depotId] : DEPOT_IDS) {
      if (!st.db.settings.has(key(p.id, d)) && onHand(p.id, d) === 0) continue
      const l = level(p, d)
      if (l.belowMin || l.negative) out.push(l)
    }
  }
  const gap = (l: StockLevel) => Number(l.onHand) - Number(l.minQty ?? 0)
  out.sort(
    (a, b) =>
      Number(b.negative) - Number(a.negative) ||
      (a.negative ? Number(a.onHand) - Number(b.onHand) : gap(a) - gap(b)) ||
      (a.stockNo ?? '').localeCompare(b.stockNo ?? ''),
  )
  return out
}

function levelsList(url: URL) {
  const sp = url.searchParams
  const only = sp.get('only') ?? 'ALL'
  const depotId = sp.get('depotId') ? Number(sp.get('depotId')) : null
  const partId = sp.get('partId') ? Number(sp.get('partId')) : null
  let items: StockLevel[]
  if (only === 'ALL') {
    items = st.db.parts
      .filter((p) => !partId || p.id === partId)
      .flatMap((p) => (depotId ? [depotId] : DEPOT_IDS).map((d) => level(p, d)))
      .filter((l) => l.version || Number(l.onHand) !== 0)
  } else {
    items = belowMinLevels(depotId).filter(
      (l) =>
        (!partId || l.partId === partId) &&
        (only === 'BELOW_MIN_OR_NEGATIVE' || (only === 'NEGATIVE' ? l.negative : l.belowMin)),
    )
  }
  const start = Number(sp.get('cursor') ?? 0)
  const end = start + Number(sp.get('limit') ?? 50)
  return { items: items.slice(start, end), nextCursor: end < items.length ? String(end) : null }
}

function priceHistory(partId: number): Price[] {
  const rows = pricesOf(partId)
  const current = priceOn(partId)
  return rows
    .map((p, i) => {
      const prev = rows[i - 1]
      const next = rows[i + 1]
      const validTo = next
        ? new Date(Date.parse(next.validFrom) - 86400000).toISOString().slice(0, 10)
        : null
      const p2 = part(partId)!
      return {
        ...priceRow(p),
        catalogId: p2.catalogId,
        stockNo: p2.stockNo,
        name: p2.name,
        validTo,
        previousPrice: prev ? money4(prev.price) : null,
        changePct: prev ? ((p.price / prev.price - 1) * 100).toFixed(1) : null,
        isCurrent: current?.id === p.id,
      }
    })
    .reverse()
}

function setPrice(partId: number, body: PriceWrite): { status: number; body: JsonBodyType } {
  const p = part(partId)
  if (!p) return { status: 404, body: { code: 'ERR_PART_NOT_FOUND' } }
  if (!/^\d{1,14}(\.\d{1,4})?$/.test(body.price) || !body.validFrom) {
    return { status: 400, body: { code: 'ERR_INVALID_REQUEST', field: 'price' } }
  }
  const same = st.db.prices.find((x) => x.partId === partId && x.validFrom === body.validFrom)
  const price = Number(body.price)
  if (same && same.price !== price) return { status: 409, body: { code: 'ERR_PRICE_EXISTS' } }
  if (!same) {
    st.db.prices.push({
      id: st.db.seq.price++,
      partId,
      price,
      validFrom: body.validFrom,
      source: 'MANUAL',
      importId: null,
      createdBy: 'demo',
      createdAt: `${today}T12:00:00Z`,
    })
    catalog(p.catalogId)!.contentVersion++
  }
  const row = priceHistory(partId).find((x) => x.validFrom === body.validFrom)!
  return { status: same ? 200 : 201, body: row as unknown as JsonBodyType }
}

// What the documents mock needs from the catalog and stock.
export const stockBridge = {
  part: (id: number) => part(id),
  partByNo: (stockNo: string) => st.db.parts.find((p) => p.stockNo === stockNo)?.id,
  catalogCode: (id: number) => catalog(part(id)!.catalogId)!.code,
  level: (partId: number, depotId: number) => {
    const l = level(part(partId)!, depotId)
    return {
      onHand: Number(l.onHand),
      reserved: Number(l.reserved),
      available: Number(l.available),
    }
  },
  listPrice: (partId: number, date: string) => priceOn(partId, date)?.price ?? null,
  move(rows: Omit<MockMovement, 'id'>[]): StockWarning[] {
    for (const r of rows) st.db.movements.push({ ...r, id: st.db.seq.movement++ })
    return negativeWarnings(rows.map((r) => [r.partId, r.depotId]))
  },
  link(sourceNo: string, documentId: number, documentType: MockMovement['documentType']) {
    for (const m of st.db.movements) {
      if (m.sourceNo === sourceNo) Object.assign(m, { documentId, documentType })
    }
  },
}

export const stockHandlers = [
  http.get(`${O}/catalog/catalogs`, () => HttpResponse.json(st.db.catalogs)),
  http.post(`${O}/catalog/parts/grid`, async ({ request }) => {
    const accept = request.headers.get('Accept') ?? ''
    const body = (await request.json()) as PartsRequest
    const res = partsGrid(body)
    if (!res) return err(400, 'ERR_INVALID_REQUEST', { field: 'colId' })
    return accept.includes('spreadsheetml') ? binary(XLSX) : HttpResponse.json(res)
  }),
  http.post(`${O}/catalog/parts/resolve`, async ({ request }) => {
    const body = (await request.json()) as { codes: string[] }
    return HttpResponse.json(
      body.codes.map((code) => {
        const n = norm(code)
        const found = st.db.parts.filter(
          (p) => p.isActive && (norm(p.stockNo) === n || p.codes.some((c) => norm(c.code) === n)),
        )
        if (found.length === 1) {
          return { code, status: 'FOUND', part: hitOf(found[0], 'STOCK_NO', null, today) }
        }
        if (found.length > 1) {
          return {
            code,
            status: 'AMBIGUOUS',
            candidates: found.map((p) => hitOf(p, 'STOCK_NO', null, today)),
          }
        }
        return { code, status: 'NOT_FOUND' }
      }) as unknown as JsonBodyType,
    )
  }),
  http.get(`${O}/catalog/search`, ({ request }) => {
    const sp = new URL(request.url).searchParams
    const text = sp.get('q') ?? ''
    if (text.trim().length < 2) return err(400, 'ERR_INVALID_REQUEST', { field: 'q' })
    const { hits, date } = catalogSearch(text, {
      catalogId: sp.get('catalogId') ? Number(sp.get('catalogId')) : null,
      date: sp.get('date') ?? undefined,
      inStockDepotId: sp.get('inStockDepotId') ? Number(sp.get('inStockDepotId')) : null,
    })
    const start = Number(sp.get('cursor') ?? 0)
    const end = start + Number(sp.get('limit') ?? 20)
    return HttpResponse.json({
      items: hits.slice(start, end),
      total: Math.min(hits.length, 1000),
      totalCapped: hits.length > 1000,
      date,
      nextCursor: end < hits.length ? String(end) : null,
    })
  }),
  http.get(`${O}/catalog/parts/:partId`, ({ params }) => {
    const p = part(Number(params.partId))
    return p
      ? HttpResponse.json(toPart(p), { headers: etag(p.version) })
      : err(404, 'ERR_PART_NOT_FOUND')
  }),
  http.get(`${O}/catalog/parts/:partId/stock`, ({ params }) => {
    const p = part(Number(params.partId))
    if (!p) return err(404, 'ERR_PART_NOT_FOUND')
    const depots = DEPOT_IDS.map((d) => level(p, d))
    const sum = (k: 'onHand' | 'reserved' | 'available') =>
      q(depots.reduce((s, l) => s + Number(l[k]), 0))
    const hand = Number(sum('onHand'))
    return HttpResponse.json({
      partId: p.id,
      depots,
      total: {
        partId: p.id,
        depotId: null,
        onHand: sum('onHand'),
        reserved: sum('reserved'),
        available: sum('available'),
        belowMin: depots.some((l) => l.belowMin),
        negative: hand < 0,
      },
    })
  }),
  http.put(`${O}/catalog/parts/:partId/depots/:depotId`, async ({ params, request }) => {
    const p = part(Number(params.partId))
    const depotId = Number(params.depotId)
    if (!p || !DEPOT_IDS.includes(depotId)) return err(404, 'ERR_PART_NOT_FOUND')
    const k = key(p.id, depotId)
    const s = st.db.settings.get(k)
    const stale = precondition(request, s?.version ?? 0)
    if (stale) return stale
    const body = (await request.json()) as PartDepotSettings
    const min = Number(body.minQty ?? 0)
    const max = body.maxQty === undefined || body.maxQty === null ? null : Number(body.maxQty)
    if (max !== null && max < min) return err(422, 'ERR_INVALID_REQUEST', { field: 'maxQty' })
    st.db.settings.set(k, {
      minQty: min,
      maxQty: max,
      shelf: body.shelf ?? '',
      reserved: s?.reserved ?? 0,
      version: (s?.version ?? 0) + 1,
    })
    const l = level(p, depotId)
    return HttpResponse.json(l, { headers: etag(l.version!) })
  }),
  http.get(`${O}/catalog/parts/:partId/prices`, ({ params }) => {
    const p = part(Number(params.partId))
    return p ? HttpResponse.json(priceHistory(p.id)) : err(404, 'ERR_PART_NOT_FOUND')
  }),
  http.post(`${O}/catalog/parts/:partId/prices`, async ({ params, request }) => {
    const body = (await request.json()) as PriceWrite
    return idempotent(request, () => setPrice(Number(params.partId), body))
  }),

  http.get(`${O}/stock/levels`, ({ request }) =>
    HttpResponse.json(levelsList(new URL(request.url))),
  ),
  http.post(`${O}/stock/movements/grid`, async ({ request }) => {
    const accept = request.headers.get('Accept') ?? ''
    const body = (await request.json()) as MovementsRequest
    return accept.includes('spreadsheetml') ? binary(XLSX) : HttpResponse.json(movementsGrid(body))
  }),
  http.post(`${O}/stock/transfers`, async ({ request }) => {
    const body = (await request.json()) as StockTransferCreate
    return idempotent(request, () => transfer(body))
  }),
  http.get(`${O}/stock/transfers/:transferId`, ({ params }) => {
    const t = st.db.transfers.find((x) => x.id === Number(params.transferId))
    if (!t) return err(404, 'ERR_TRANSFER_NOT_FOUND')
    const ms = st.db.movements.filter((m) => t.movementIds.includes(m.id))
    const out = ms.find((m) => m.kind === 'TRANSFER_OUT')!
    return HttpResponse.json({
      id: t.id,
      transferNo: t.transferNo,
      fromDepotId: out.depotId,
      toDepotId: out.otherDepotId,
      date: out.date,
      note: t.note,
      lines: ms
        .filter((m) => m.kind === 'TRANSFER_OUT')
        .map((m) => ({ partId: m.partId, qty: q(m.qty) })),
      movements: ms.map((m) => movementOut(m, null)),
      warnings: [],
    })
  }),

  http.get(`${O}/stock/counts`, ({ request }) => {
    const sp = new URL(request.url).searchParams
    return HttpResponse.json(
      st.db.counts
        .filter(
          (c) =>
            (!sp.get('depotId') || c.depotId === Number(sp.get('depotId'))) &&
            (!sp.get('status') || c.status === sp.get('status')) &&
            (!sp.get('year') || c.year === Number(sp.get('year'))),
        )
        .sort((a, b) => b.startedAt.localeCompare(a.startedAt))
        .map(countOut),
    )
  }),
  http.post(`${O}/stock/counts`, async ({ request }) => {
    const body = (await request.json()) as StockCountOpen
    return idempotent(request, () => openCount(body))
  }),
  http.get(`${O}/stock/counts/:countId`, ({ params, request }) => {
    const c = count(Number(params.countId))
    if (!c) return err(404, 'ERR_STOCK_COUNT_NOT_FOUND')
    const accept = request.headers.get('Accept') ?? ''
    if (accept.includes('pdf')) return binary('application/pdf')
    const out = countOut(c)
    return HttpResponse.json(out, { headers: etag(out.version) })
  }),
  http.post(`${O}/stock/counts/:countId/lines/grid`, async ({ params, request }) => {
    const c = count(Number(params.countId))
    if (!c) return err(404, 'ERR_STOCK_COUNT_NOT_FOUND')
    const accept = request.headers.get('Accept') ?? ''
    const body = (await request.json()) as CountLinesRequest
    return accept.includes('spreadsheetml')
      ? binary(XLSX)
      : HttpResponse.json(countLinesGrid(c, body))
  }),
  http.post(`${O}/stock/counts/:countId/lines`, async ({ params, request }) => {
    const c = count(Number(params.countId))
    if (!c) return err(404, 'ERR_STOCK_COUNT_NOT_FOUND')
    const body = (await request.json()) as { partId: number; shelf?: string; countedQty?: string }
    return idempotent(request, () => {
      if (c.status !== 'OPEN') return { status: 409, body: { code: 'ERR_STOCK_COUNT_NOT_OPEN' } }
      const p = part(body.partId)
      if (!p) return { status: 422, body: { code: 'ERR_PART_NOT_FOUND', field: 'partId' } }
      if (!p.isActive) return { status: 422, body: { code: 'ERR_PART_INACTIVE', field: 'partId' } }
      if (c.lines.some((l) => l.partId === p.id)) {
        return { status: 409, body: { code: 'ERR_STOCK_COUNT_LINE_EXISTS' } }
      }
      const l: MockCountLine = {
        id: st.db.seq.line++,
        partId: p.id,
        stockNo: p.stockNo,
        name: p.name,
        brand: p.brand,
        unit: p.unit,
        shelf: body.shelf ?? st.db.settings.get(key(p.id, c.depotId))?.shelf ?? '',
        systemQty: onHandAt(st.db, p.id, c.depotId, c.startedAt),
        countedQty: null,
        difference: null,
        movedSinceOpen: '0',
        onHandAfterPost: null,
        added: true,
        unitCost: p.cost === null ? null : money4(p.cost),
        costSource: p.costSource,
        value: null,
        countedBy: null,
        countedAt: null,
        version: 0,
      }
      stamp(
        l,
        body.countedQty === undefined ? null : Number(body.countedQty),
        'demo',
        `${today}T12:00:00Z`,
      )
      c.lines.push(l)
      return { status: 201, body: lineOut(c, l) as unknown as JsonBodyType }
    })
  }),
  http.get(`${O}/stock/counts/:countId/lines/:lineId`, ({ params }) => {
    const c = count(Number(params.countId))
    if (!c) return err(404, 'ERR_STOCK_COUNT_NOT_FOUND')
    const l = c.lines.find((x) => x.id === Number(params.lineId))
    if (!l) return err(404, 'ERR_STOCK_COUNT_LINE_NOT_FOUND')
    return HttpResponse.json(lineOut(c, l), { headers: etag(l.version) })
  }),
  http.patch(`${O}/stock/counts/:countId/lines/:lineId`, async ({ params, request }) => {
    const c = count(Number(params.countId))
    const l = c?.lines.find((x) => x.id === Number(params.lineId))
    if (!c || !l) return err(404, 'ERR_STOCK_COUNT_LINE_NOT_FOUND')
    if (c.status !== 'OPEN') return err(409, 'ERR_STOCK_COUNT_NOT_OPEN')
    if (l.contended) {
      l.contended = false
      stamp(l, l.systemQty - 1, 'a.kaya', `${today}T11:58:00Z`)
    }
    const stale = precondition(request, l.version)
    if (stale) return stale
    const body = (await request.json()) as { countedQty: string | null; shelf?: string }
    if (body.countedQty !== null && !/^\d{1,14}(\.\d{1,4})?$/.test(body.countedQty)) {
      return err(400, 'ERR_INVALID_REQUEST', { field: 'countedQty' })
    }
    stamp(
      l,
      body.countedQty === null ? null : Number(body.countedQty),
      'demo',
      `${today}T12:00:00Z`,
    )
    if (body.shelf) l.shelf = body.shelf
    const out = lineOut(c, l)
    return HttpResponse.json(out, { headers: etag(l.version) })
  }),
  http.post(`${O}/stock/counts/:countId/finalize`, async ({ params, request }) => {
    const c = count(Number(params.countId))
    if (!c) return err(404, 'ERR_STOCK_COUNT_NOT_FOUND')
    const stale = precondition(request, c.version)
    if (stale) return stale
    const body = (await request.json()) as { uncounted: 'SKIP' | 'ZERO' }
    return idempotent(request, () => finalize(c, body.uncounted))
  }),
  http.post(`${O}/stock/counts/:countId/cancel`, async ({ params, request }) => {
    const c = count(Number(params.countId))
    if (!c) return err(404, 'ERR_STOCK_COUNT_NOT_FOUND')
    const stale = precondition(request, c.version)
    if (stale) return stale
    if (c.status !== 'OPEN') return err(409, 'ERR_STOCK_COUNT_NOT_OPEN')
    const body = (await request.json()) as { reason?: string }
    if (!body.reason || body.reason.trim().length < 3)
      return err(400, 'ERR_INVALID_REQUEST', { field: 'reason' })
    Object.assign(c, {
      status: 'CANCELLED',
      cancelledAt: `${today}T12:00:00Z`,
      cancelReason: body.reason.trim(),
      version: c.version + 1,
    })
    return HttpResponse.json(countOut(c), { headers: etag(c.version) })
  }),

  http.get(`${O}/catalog/import-presets`, () =>
    HttpResponse.json({ presets: PRESETS, aliases: ALIASES }),
  ),
  http.get(`${O}/catalog/imports`, ({ request }) => {
    const cat = new URL(request.url).searchParams.get('catalogId')
    return HttpResponse.json({
      items: imports.filter((j) => !cat || j.catalogId === Number(cat)).map(jobOut),
      nextCursor: null,
    })
  }),
  http.post(`${O}/catalog/imports`, async ({ request }) => {
    const k = request.headers.get('Idempotency-Key')
    if (!k) return err(400, 'ERR_IDEMPOTENCY_KEY_REQUIRED')
    const hit = st.idem.get(k)
    if (hit)
      return HttpResponse.json(hit.body, {
        status: hit.status,
        headers: { 'Idempotent-Replayed': 'true' },
      })
    const res = await createImport(request)
    if (res.status < 400) st.idem.set(k, res)
    return HttpResponse.json(res.body, { status: res.status })
  }),
  http.get(`${O}/catalog/imports/:importId`, ({ params }) => {
    const j = imp(params.importId)
    return j
      ? HttpResponse.json(jobOut(j), { headers: etag(j.version) })
      : err(404, 'ERR_IMPORT_NOT_FOUND')
  }),
  http.patch(`${O}/catalog/imports/:importId`, async ({ params, request }) => {
    const j = imp(params.importId)
    if (!j) return err(404, 'ERR_IMPORT_NOT_FOUND')
    const stale = precondition(request, j.version)
    if (stale) return stale
    if (j.status !== 'READY' && j.status !== 'NEEDS_MAPPING')
      return err(409, 'ERR_INVALID_STATE_TRANSITION')
    const body = (await request.json()) as { mapping?: PriceImportMappingWrite; validFrom?: string }
    if (body.mapping) j.mapping = { ...j.mapping, ...body.mapping }
    if (body.validFrom) j.validFrom = body.validFrom
    j.version++
    resolveMapping(j)
    return HttpResponse.json(jobOut(j), { headers: etag(j.version) })
  }),
  http.post(`${O}/catalog/imports/:importId/rows/grid`, async ({ params, request }) => {
    const j = imp(params.importId)
    if (!j) return err(404, 'ERR_IMPORT_NOT_FOUND')
    const accept = request.headers.get('Accept') ?? ''
    const body = (await request.json()) as ImportRowsRequest
    return accept.includes('spreadsheetml')
      ? binary(XLSX)
      : HttpResponse.json(importRowsGrid(j, body))
  }),
  http.post(`${O}/catalog/imports/:importId/apply`, async ({ params, request }) => {
    const j = imp(params.importId)
    if (!j) return err(404, 'ERR_IMPORT_NOT_FOUND')
    const k = request.headers.get('Idempotency-Key')
    const replay = k ? st.idem.get(k) : undefined
    if (replay)
      return HttpResponse.json(replay.body, {
        status: replay.status,
        headers: { 'Idempotent-Replayed': 'true' },
      })
    const stale = precondition(request, j.version)
    if (stale) return stale
    const body = (await request.json()) as ImportApply
    return idempotent(request, () => applyImport(j, body))
  }),
  http.post(`${O}/catalog/imports/:importId/discard`, ({ params, request }) => {
    const j = imp(params.importId)
    if (!j) return err(404, 'ERR_IMPORT_NOT_FOUND')
    const stale = precondition(request, j.version)
    if (stale) return stale
    if (j.status === 'APPLIED' || j.status === 'DISCARDED')
      return err(409, 'ERR_INVALID_STATE_TRANSITION')
    j.status = 'DISCARDED'
    j.version++
    return HttpResponse.json(jobOut(j), { headers: etag(j.version) })
  }),
]
