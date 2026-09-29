import type {
  Catalog,
  MovementKind,
  PartCode,
  Price,
  StockCount,
  StockCountLine,
  StockMovement,
} from '../api/types'
import { TODAY } from './data'

// Synthetic catalog and stock: ~2,000 parts in four catalogs over depots 1 and 2. The parts the
// mockups show (HP-3774, CL-1150, …) are fixed; the rest come from a seeded generator.

function rng(seed: number) {
  let a = seed
  return () => {
    a = (a + 0x6d2b79f5) | 0
    let t = Math.imul(a ^ (a >>> 15), 1 | a)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

export const q = (n: number) => String(Math.round(n * 10000) / 10000)
export const money4 = (n: number) => n.toFixed(2)

export interface MockPart {
  id: number
  catalogId: number
  stockNo: string
  name: string
  type: string | null
  brand: string | null
  unit: string
  packageQty: number
  vatPct: string
  isActive: boolean
  codes: PartCode[]
  cost: number | null
  costSource: StockCountLine['costSource']
  updatedAt: string
  version: number
}

interface DepotSettings {
  minQty: number
  maxQty: number | null
  shelf: string
  reserved: number
  version: number
}

export interface MockPrice {
  id: number
  partId: number
  price: number
  validFrom: string
  source: 'MANUAL' | 'IMPORT' | 'LEGACY'
  importId: number | null
  createdBy: string | null
  createdAt: string
}

export interface MockMovement {
  id: number
  partId: number
  depotId: number
  kind: MovementKind
  label: NonNullable<StockMovement['label']>
  qty: number
  unitCost: number | null
  sourceType: StockMovement['sourceType']
  sourceNo: string | null
  sicilName: string | null
  otherDepotId: number | null
  transferId: number | null
  countId: number | null
  documentId: number | null
  documentType?: StockMovement['documentType']
  reversal?: boolean
  date: string
  occurredAt: string
}

export interface MockCountLine extends Omit<StockCountLine, 'systemQty' | 'countedQty'> {
  systemQty: number
  countedQty: number | null
  // Another counter saves this line once the page has loaded it (e2e 412 flow).
  contended?: boolean
}

export interface MockCount extends Omit<StockCount, 'lineCount' | 'countedCount'> {
  lines: MockCountLine[]
}

export interface StockDb {
  catalogs: Catalog[]
  parts: MockPart[]
  settings: Map<string, DepotSettings>
  prices: MockPrice[]
  movements: MockMovement[]
  counts: MockCount[]
  transfers: { id: number; transferNo: string; movementIds: number[]; note?: string }[]
  seq: { movement: number; price: number; transfer: number; count: number; line: number }
}

export const DEPOT_IDS = [1, 2]
export const key = (partId: number, depotId: number) => `${partId}:${depotId}`
const IN_KINDS: MovementKind[] = ['IN', 'ADJUST_IN', 'TRANSFER_IN']
export const delta = (m: MockMovement) => (IN_KINDS.includes(m.kind) ? m.qty : -m.qty)

const CATALOGS: [code: number, name: string, prefixes: [string, string, string[]][]][] = [
  [
    12,
    'Hydraulics 2026',
    [
      ['HP', 'Hydraulic pump', ['2-cyl.', '3-cyl.', '4-cyl.', 'gear type', 'tandem']],
      ['HV', 'Control valve', ['2-spool', '3-spool', 'relief', 'check', 'flow divider']],
      ['HC', 'Hydraulic cylinder', ['50×300', '63×400', '80×500', 'lift arm', 'steering']],
      ['HS', 'Hydraulic hose', ['1/4" 1 m', '3/8" 1.5 m', '1/2" 2 m', '3/4" 1 m', 'return line']],
    ],
  ],
  [
    20,
    'Filters & seals',
    [
      ['FL', 'Fuel filter', ['set', 'primary', 'secondary', 'in-line', 'water separator']],
      ['OF', 'Oil filter', ['engine', 'hydraulic', 'transmission', 'spin-on', 'cartridge']],
      ['AF', 'Air filter', ['outer', 'inner', 'cab', 'pre-cleaner', 'heavy duty']],
      ['GS', 'Gasket set', ['head', 'sump', 'full', 'valve cover', 'manifold']],
      ['SE', 'Seal kit', ['cylinder', 'pump', 'valve', 'steering', 'PTO']],
    ],
  ],
  [
    31,
    'Transmission',
    [
      ['CL', 'Clutch disc', ['10"', '11"', '12"', '13"', 'PTO']],
      ['BR', 'Brake lining', ['pair', 'set', 'rear', 'front', 'handbrake']],
      ['BE', 'V-belt', ['13×1000', '13×1250', '17×1400', '22×1600', 'fan']],
      ['BG', 'Bearing', ['6205', '6306', 'taper 30208', 'needle', 'pilot']],
      ['GR', 'Gear', ['2nd speed', '3rd speed', 'reverse', 'crown', 'pinion']],
    ],
  ],
  [
    45,
    'Engine parts',
    [
      ['WP', 'Water pump', ['', 'with gasket', 'repair kit', 'pulley', 'housing']],
      ['FP', 'Fuel lift pump', ['', 'mechanical', 'electric', 'hand primer', 'diaphragm']],
      ['PS', 'Piston set', ['STD', '+0.50', '+1.00', 'with rings', 'liner kit']],
      ['IN', 'Injector', ['nozzle', 'complete', 'holder', 'seal washer', 'return pipe']],
      ['TH', 'Thermostat', ['82 °C', '88 °C', 'housing', 'gasket', 'kit']],
    ],
  ],
]

const BRANDS = ['[BRAND A]', '[BRAND B]', '[BRAND C]', '[BRAND D]', '[BRAND E]']
const SHELF_ROWS = ['B', 'C', 'D', 'E', 'F', 'G', 'H']

// Parts the mockups show, with exact stock per depot [onHand, reserved, min, max, shelf].
interface Featured {
  stockNo: string
  name: string
  catalog: number
  oem?: string
  alt?: string
  price: number
  cost: number | null
  costSource?: StockCountLine['costSource']
  d1?: [number, number, number, number | null, string]
  d2?: [number, number, number, number | null, string]
}

const FEATURED: Featured[] = [
  {
    stockNo: 'HP-3702',
    name: 'Hydraulic pump, 2-cyl.',
    catalog: 12,
    oem: '3774611M91',
    price: 9850,
    cost: 7120,
    d1: [6, 0, 2, 12, 'A-04-1'],
    d2: [1, 0, 0, null, 'H-11-2'],
  },
  {
    stockNo: 'HP-3774',
    name: 'Hydraulic pump, 3-cyl.',
    catalog: 12,
    oem: '3774612M91',
    alt: 'K9-3774',
    price: 12400,
    cost: 8900,
    d1: [10, 1, 6, 20, 'A-04-2'],
    d2: [4, 0, 2, 8, 'H-11-1'],
  },
  {
    stockNo: 'HP-3780',
    name: 'Hydraulic pump, 4-cyl.',
    catalog: 12,
    alt: 'K9-3780',
    price: 15900,
    cost: 11400,
    d1: [0, 0, 1, 4, 'B-04-3'],
    d2: [2, 0, 0, null, 'H-11-3'],
  },
  {
    stockNo: 'WP-3715',
    name: 'Water pump',
    catalog: 45,
    oem: '3715B010',
    price: 3420,
    cost: 2480,
    d1: [14, 0, 4, 20, 'C-02-1'],
    d2: [5, 0, 2, 10, 'G-02-1'],
  },
  {
    stockNo: 'FP-3709',
    name: 'Fuel lift pump',
    catalog: 45,
    oem: 'F3709-2',
    price: 1180,
    cost: 840,
    d1: [22, 0, 6, 30, 'C-02-4'],
    d2: [0, 0, 0, null, 'G-02-4'],
  },
  {
    stockNo: 'CL-1150',
    name: 'Clutch disc 11"',
    catalog: 31,
    oem: '1150C11',
    price: 3650,
    cost: 2850,
    d1: [3, 0, 8, 16, 'A-05-1'],
    d2: [2, 0, 2, 6, 'F-05-1'],
  },
  {
    stockNo: 'CL-1210',
    name: 'Clutch disc 12"',
    catalog: 31,
    price: 4100,
    cost: 3120,
    d1: [5, 0, 2, 10, 'A-05-3'],
  },
  {
    stockNo: 'FL-0092',
    name: 'Fuel filter set',
    catalog: 20,
    oem: '0092FS',
    price: 265,
    cost: 181.5,
    d1: [12, 0, 40, 80, 'B-01-1'],
    d2: [30, 0, 20, 60, 'E-01-1'],
  },
  {
    stockNo: 'BR-2210',
    name: 'Brake lining pair',
    catalog: 31,
    price: 375,
    cost: 262,
    d1: [0, 0, 10, 30, 'B-02-2'],
    d2: [6, 0, 4, 12, 'F-02-2'],
  },
  {
    stockNo: 'BR-2215',
    name: 'Brake lining, rear',
    catalog: 31,
    price: 410,
    cost: 290,
    d1: [4, 0, 2, 10, 'B-02-3'],
    d2: [-2, 0, 2, 8, 'F-02-3'],
  },
  {
    stockNo: 'OF-0457',
    name: 'Oil filter, hydraulic',
    catalog: 20,
    price: 540,
    cost: 390,
    costSource: 'LIST_PRICE',
    d1: [-3, 0, 5, 20, 'B-03-1'],
  },
  {
    stockNo: 'GS-4410',
    name: 'Gasket set, head',
    catalog: 20,
    price: 1290,
    cost: 910,
    d1: [18, 0, 4, 24, 'A-06-1'],
  },
  {
    stockNo: 'GS-4418',
    name: 'Gasket set, sump',
    catalog: 20,
    price: 640,
    cost: 455,
    d1: [9, 0, 2, 12, 'A-06-2'],
  },
  {
    stockNo: 'BE-0620',
    name: 'V-belt 13×1250',
    catalog: 31,
    price: 185,
    cost: 128,
    d1: [32, 0, 10, 50, 'A-07-1'],
  },
]

const COUNT_STARTED = '2026-09-28T08:00:00Z'

// HP-3774 history (mockup 09): opening, the January count, a purchase into depot 2, a transfer,
// a sale in depot 2, a purchase and a sale in depot 1.
const HP3774: Omit<MockMovement, 'id' | 'partId'>[] = [
  mv(1, 'ADJUST_IN', 'OPENING', 5, '2026-01-01', 'legacy_import', null, null, 8100),
  mv(2, 'ADJUST_IN', 'OPENING', 3, '2026-01-01', 'legacy_import', null, null, 8100),
  {
    ...mv(
      1,
      'ADJUST_OUT',
      'COUNT',
      1,
      '2026-01-02',
      'stock_count',
      'SAY-2026-1-000001',
      null,
      8100,
    ),
    countId: 1,
  },
  mv(
    2,
    'IN',
    'PURCHASE',
    2,
    '2026-06-10',
    'document',
    'AF-2026-2-000140',
    'Ege Hidrolik San.',
    8450,
  ),
  {
    ...mv(
      1,
      'TRANSFER_OUT',
      'TRANSFER',
      2,
      '2026-09-15',
      'transfer',
      'TRF-2026-1-000051',
      null,
      null,
    ),
    otherDepotId: 2,
    transferId: 51,
  },
  {
    ...mv(
      2,
      'TRANSFER_IN',
      'TRANSFER',
      2,
      '2026-09-15',
      'transfer',
      'TRF-2026-1-000051',
      null,
      null,
    ),
    otherDepotId: 1,
    transferId: 51,
  },
  mv(2, 'OUT', 'SALE', 3, '2026-09-22', 'document', 'SF-2026-2-000108', 'Aksoy Tarım', null),
  mv(
    1,
    'IN',
    'PURCHASE',
    10,
    '2026-09-26',
    'document',
    'AF-2026-1-000201',
    'Ege Hidrolik San.',
    8900,
  ),
  mv(
    1,
    'OUT',
    'SALE',
    2,
    '2026-09-29',
    'document',
    'SF-2026-1-000418',
    'Yılmaz Tarım Makinaları',
    null,
  ),
]

function mv(
  depotId: number,
  kind: MovementKind,
  label: MockMovement['label'],
  qty: number,
  date: string,
  sourceType: MockMovement['sourceType'],
  sourceNo: string | null,
  sicilName: string | null,
  unitCost: number | null,
): Omit<MockMovement, 'id' | 'partId'> {
  return {
    depotId,
    kind,
    label,
    qty,
    unitCost,
    sourceType,
    sourceNo,
    sicilName,
    otherDepotId: null,
    transferId: null,
    countId: null,
    documentId: null,
    date,
    occurredAt: `${date}T${date === '2026-09-29' ? '11' : '09'}:30:00Z`,
  }
}

const CUSTOMERS = ['Yılmaz Tarım Makinaları', 'Kaya Ziraat', 'Aksoy Tarım', 'Öztürk Tarım']
const SUPPLIERS = ['Ege Hidrolik San.', 'Marmara Filtre']

export function createStockDb(size = 2000): StockDb {
  const r = rng(20260929)
  const pick = <T>(xs: T[]) => xs[Math.floor(r() * xs.length)]
  const catalogs: Catalog[] = CATALOGS.map(([code, name], i) => ({
    id: i + 1,
    code,
    name,
    supplierSicilId: null,
    supplierName: i === 0 ? 'Ege Hidrolik San.' : i === 1 ? 'Marmara Filtre' : null,
    isActive: true,
    currency: 'TRY',
    partCount: 0,
    contentVersion: 1,
    lastImportAt: i === 1 ? '2026-09-01T09:00:00Z' : null,
    version: 1,
  }))
  const catId = (code: number) => catalogs.find((c) => c.code === code)!.id
  const parts: MockPart[] = []
  const settings = new Map<string, DepotSettings>()
  const prices: MockPrice[] = []
  const movements: MockMovement[] = []
  const seq = { movement: 1, price: 1, transfer: 52, count: 3, line: 1 }
  const taken = new Set<string>()
  const onHand = new Map<string, number>()

  const addPart = (p: Omit<MockPart, 'id' | 'version' | 'updatedAt' | 'isActive'>) => {
    const part: MockPart = {
      ...p,
      id: 1000 + parts.length,
      isActive: true,
      updatedAt: '2026-09-01T09:00:00Z',
      version: 1,
    }
    parts.push(part)
    taken.add(p.stockNo)
    return part
  }
  const addPrices = (partId: number, current: number, rows: [string, number][]) => {
    for (const [validFrom, factor] of rows) {
      prices.push({
        id: seq.price++,
        partId,
        price: Math.round(current * factor * 100) / 100,
        validFrom,
        source: validFrom === '2026-01-02' ? 'LEGACY' : 'IMPORT',
        importId: null,
        createdBy: validFrom === '2026-01-02' ? null : 'demo',
        createdAt: `${validFrom}T08:00:00Z`,
      })
    }
  }
  const setDepot = (
    partId: number,
    depotId: number,
    [hand, reserved, min, max, shelf]: [number, number, number, number | null, string],
  ) => {
    settings.set(key(partId, depotId), { minQty: min, maxQty: max, shelf, reserved, version: 1 })
    onHand.set(key(partId, depotId), hand)
  }

  for (const f of FEATURED) {
    const codes: PartCode[] = []
    if (f.oem) codes.push({ kind: 'OEM', code: f.oem, equivalentPartId: null })
    if (f.alt) codes.push({ kind: 'ALT', code: f.alt, equivalentPartId: null })
    const p = addPart({
      catalogId: catId(f.catalog),
      stockNo: f.stockNo,
      name: f.name,
      type: null,
      brand: BRANDS[0],
      unit: 'PCS',
      packageQty: 1,
      vatPct: '20',
      codes,
      cost: f.cost,
      costSource: f.costSource ?? 'LAST_PURCHASE',
    })
    if (f.stockNo === 'HP-3774') {
      prices.push(
        ...(
          [
            ['2026-01-02', 10700, 'LEGACY'],
            ['2026-04-01', 11600, 'IMPORT'],
            ['2026-09-01', 12400, 'IMPORT'],
          ] as const
        ).map(([validFrom, price, source]) => ({
          id: seq.price++,
          partId: p.id,
          price,
          validFrom,
          source,
          importId: null,
          createdBy: source === 'LEGACY' ? null : 'demo',
          createdAt: `${validFrom}T08:00:00Z`,
        })),
      )
    } else {
      addPrices(p.id, f.price, [
        ['2026-01-02', 0.9],
        ['2026-09-01', 1],
      ])
    }
    if (f.d1) setDepot(p.id, 1, f.d1)
    if (f.d2) setDepot(p.id, 2, f.d2)
  }
  const hp = parts.find((p) => p.stockNo === 'HP-3774')!
  const cl = parts.find((p) => p.stockNo === 'CL-1150')!
  cl.codes.push({ kind: 'EQUIVALENT', code: 'CL-1210', equivalentPartId: 1006 })

  let n = 0
  while (parts.length < size) {
    const [code, , families] = CATALOGS[n % CATALOGS.length]
    const [prefix, base, variants] = families[Math.floor(n / CATALOGS.length) % families.length]
    n++
    const num = String(Math.floor(r() * 9000) + 1000)
    const stockNo = `${prefix}-${num}`
    if (taken.has(stockNo)) continue
    const variant = pick(variants)
    const price = Math.round((50 + r() * r() * 16000) * 100) / 100
    const codes: PartCode[] = []
    if (r() < 0.7) {
      codes.push({
        kind: 'OEM',
        code: `${num}${Math.floor(r() * 900) + 100}M${Math.floor(r() * 90) + 10}`,
        equivalentPartId: null,
      })
    }
    if (r() < 0.25)
      codes.push({
        kind: 'ALT',
        code: `K${Math.floor(r() * 9) + 1}-${num}`,
        equivalentPartId: null,
      })
    if (r() < 0.3) {
      codes.push({
        kind: 'MANUFACTURER',
        code: `${prefix}${num}-${Math.floor(r() * 90) + 10}`,
        equivalentPartId: null,
      })
    }
    const roll = r()
    const p = addPart({
      catalogId: catId(code),
      stockNo,
      name: variant ? `${base}, ${variant}` : base,
      type: r() < 0.4 ? variant || null : null,
      brand: pick(BRANDS),
      unit: prefix === 'HS' ? 'M' : 'PCS',
      packageQty: prefix === 'BG' || prefix === 'SE' ? 10 : 1,
      vatPct: '20',
      codes,
      cost: roll < 0.02 ? null : Math.round(price * (0.62 + r() * 0.12) * 100) / 100,
      costSource: roll < 0.02 ? 'NONE' : roll < 0.07 ? 'LIST_PRICE' : 'LAST_PURCHASE',
    })
    if (r() < 0.6) {
      addPrices(p.id, price, [
        ['2026-01-02', 0.88],
        ['2026-04-01', 0.95],
        ['2026-09-01', 1],
      ])
    } else {
      addPrices(p.id, price, [['2026-01-02', 1]])
    }
    if (r() < 0.04) addPrices(p.id, price, [['2026-10-15', 1.08]])
    const shelf = (row: string) =>
      `${row}-${String(Math.floor(r() * 30) + 1).padStart(2, '0')}-${Math.floor(r() * 4) + 1}`
    const hand = () => (r() < 0.2 ? 0 : Math.floor(r() * r() * 60) + 1)
    if (r() < 0.62) {
      const h = hand()
      const min = r() < 0.5 ? Math.floor(r() * 12) : 0
      setDepot(p.id, 1, [
        r() < 0.001 ? -Math.ceil(r() * 4) : h,
        0,
        min,
        min ? min * 3 : null,
        shelf(pick(SHELF_ROWS)),
      ])
    }
    if (r() < 0.55) {
      const min = r() < 0.4 ? Math.floor(r() * 8) : 0
      setDepot(p.id, 2, [
        r() < 0.001 ? -Math.ceil(r() * 3) : hand(),
        0,
        min,
        min ? min * 3 : null,
        shelf(pick(SHELF_ROWS)),
      ])
    }
  }
  // A few inactive parts (shown only with "include inactive").
  for (const p of parts.slice(-12)) p.isActive = false

  // Movements: HP-3774's history as in the mockup; every other level comes from an opening
  // balance plus, for some parts, a purchase and a sale.
  const push = (partId: number, m: Omit<MockMovement, 'id' | 'partId'>) =>
    movements.push({ ...m, id: seq.movement++, partId })
  for (const m of HP3774) push(hp.id, m)
  for (const p of parts) {
    if (p.id === hp.id) continue
    for (const d of DEPOT_IDS) {
      const target = onHand.get(key(p.id, d))
      if (target === undefined) continue
      const busy = r() < 0.3 && target > 2
      const bought = busy ? Math.floor(r() * 10) + 2 : 0
      const sold = busy ? Math.floor(r() * bought) + 1 : 0
      const opening = target - bought + sold
      if (opening !== 0) {
        push(
          p.id,
          mv(
            d,
            opening > 0 ? 'ADJUST_IN' : 'ADJUST_OUT',
            'OPENING',
            Math.abs(opening),
            '2026-01-01',
            'legacy_import',
            null,
            null,
            p.cost,
          ),
        )
      }
      if (bought) {
        const day = `2026-0${Math.floor(r() * 8) + 1}-1${Math.floor(r() * 9)}`
        push(
          p.id,
          mv(
            d,
            'IN',
            'PURCHASE',
            bought,
            day,
            'document',
            `AF-2026-${d}-000${100 + Math.floor(r() * 99)}`,
            pick(SUPPLIERS),
            p.cost,
          ),
        )
        const sale = `2026-09-0${Math.floor(r() * 9) + 1}`
        push(
          p.id,
          mv(
            d,
            'OUT',
            'SALE',
            sold,
            sale,
            'document',
            `SF-2026-${d}-000${300 + Math.floor(r() * 99)}`,
            pick(CUSTOMERS),
            null,
          ),
        )
      }
    }
  }

  for (const c of catalogs)
    c.partCount = parts.filter((p) => p.catalogId === c.id && p.isActive).length

  const db: StockDb = {
    catalogs,
    parts,
    settings,
    prices,
    movements,
    counts: [],
    transfers: [],
    seq,
  }
  db.counts.push(finalizedCount(), openCount(db))
  return db
}

export function onHandAt(db: StockDb, partId: number, depotId: number, at?: string): number {
  let sum = 0
  for (const m of db.movements) {
    if (m.partId !== partId || m.depotId !== depotId) continue
    if (at && m.occurredAt > at) continue
    sum += delta(m)
  }
  return sum
}

function finalizedCount(): MockCount {
  return {
    id: 1,
    countNo: 'SAY-2026-1-000001',
    depotId: 1,
    countDate: '2026-01-02',
    year: 2026,
    status: 'FINALIZED',
    startedAt: '2026-01-02T07:00:00Z',
    differenceCount: 1,
    differenceValue: '-8100.00',
    valueIn: '0.00',
    valueOut: '8100.00',
    unvaluedLines: 0,
    uncounted: 'SKIP',
    createdBy: 'm.demir',
    finalizedAt: '2026-01-02T17:40:00Z',
    cancelledAt: null,
    cancelReason: null,
    version: 2,
    lines: [],
  }
}

// SAY-2026-1-000002: depot 1, opened 28.09 over every part with a shelf there; 412 counted.
function openCount(db: StockDb): MockCount {
  const count: MockCount = {
    id: 2,
    countNo: 'SAY-2026-1-000002',
    depotId: 1,
    countDate: '2026-09-28',
    year: 2026,
    status: 'OPEN',
    startedAt: COUNT_STARTED,
    differenceCount: 0,
    differenceValue: '0.00',
    valueIn: '0.00',
    valueOut: '0.00',
    unvaluedLines: 0,
    uncounted: null,
    createdBy: 'm.demir',
    finalizedAt: null,
    cancelledAt: null,
    cancelReason: null,
    version: 1,
    lines: countLines(db, 1, COUNT_STARTED),
  }
  const exact: Record<string, number | null> = {
    'HP-3702': 6,
    'HP-3774': 12,
    'CL-1150': 2,
    'CL-1210': 6,
    'GS-4410': null,
    'GS-4418': null,
    'BE-0620': null,
  }
  let counted = Object.values(exact).filter((v) => v !== null).length
  const r = rng(7)
  for (const l of count.lines) {
    if (l.stockNo in exact) {
      stamp(l, exact[l.stockNo], 'm.demir', '2026-09-28T10:1' + (counted % 10) + ':00Z')
      continue
    }
    if (counted >= 412 || l.shelf < 'C') continue
    counted++
    const off = r() < 0.06 ? (r() < 0.5 ? -1 : 1) : 0
    stamp(
      l,
      Math.max(0, l.systemQty + off),
      r() < 0.7 ? 'm.demir' : 'a.kaya',
      '2026-09-28T14:00:00Z',
    )
  }
  const belt = count.lines.find((l) => l.stockNo === 'BE-0620')
  if (belt) belt.contended = true
  return count
}

export function countLines(db: StockDb, depotId: number, at: string, from?: string, to?: string) {
  const lines: MockCountLine[] = []
  for (const p of db.parts) {
    if (!p.isActive) continue
    const s = db.settings.get(key(p.id, depotId))
    const system = onHandAt(db, p.id, depotId, at)
    if (!s?.shelf && system === 0) continue
    const shelf = s?.shelf ?? ''
    if (from && shelf < from) continue
    if (to && shelf > to && !shelf.startsWith(to)) continue
    lines.push({
      id: db.seq.line++,
      partId: p.id,
      stockNo: p.stockNo,
      name: p.name,
      brand: p.brand,
      unit: p.unit,
      shelf,
      systemQty: system,
      countedQty: null,
      difference: null,
      movedSinceOpen: '0',
      onHandAfterPost: null,
      added: false,
      unitCost: p.cost === null ? null : money4(p.cost),
      costSource: p.costSource,
      value: null,
      countedBy: null,
      countedAt: null,
      version: 1,
    })
  }
  return lines.sort((a, b) => a.shelf.localeCompare(b.shelf) || a.stockNo.localeCompare(b.stockNo))
}

export function stamp(l: MockCountLine, qty: number | null, by: string | null, at: string | null) {
  l.countedQty = qty
  l.countedBy = qty === null ? null : by
  l.countedAt = qty === null ? null : at
  l.version += 1
}

export const today = TODAY
export const priceRow = (
  p: MockPrice,
): Omit<Price, 'isCurrent' | 'validTo' | 'previousPrice' | 'changePct'> => ({
  id: p.id,
  partId: p.partId,
  price: money4(p.price),
  validFrom: p.validFrom,
  source: p.source,
  importId: p.importId,
  createdBy: p.createdBy,
  createdAt: p.createdAt,
})
