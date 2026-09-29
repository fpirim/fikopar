// Synthetic demo data for a real backend, written through the public API only (no SQL).
// Mirrors the MSW mockups: Demo Company A, depots 1 and 2, the featured accounts, 2025 activity
// that carries into 2026, and a few 2026 receipts and till vouchers. Safe to run twice; the e2e
// global setup passes a fresh slug so every run starts from an empty company.
//
//   E2E_API_URL=http://localhost:18080 node --experimental-strip-types e2e/real/seed.ts

import { randomUUID } from 'node:crypto'

type Json = Record<string, unknown>
type Tipi = 'MUSTERI' | 'FIRMA' | 'BANKA'

export interface Seeded {
  token: string
  orgId: number
  depots: Record<string, number>
  accounts: Record<string, number>
  sicils: Record<string, number>
  // v1.2 catalog and stock: catalog code → id, stock no → part id, the open count of depot 2.
  catalogs: Record<string, number>
  parts: Record<string, number>
  openCountId: number | null
  // v1.4 documents by role in the flows (sale, purchase, waybill, bill, return, increase, decrease, draft).
  documents: Record<string, number>
  // v1.5: the salesperson of card 10482.
  salesRep: { userId: number; name: string }
}

const API = process.env.E2E_API_URL || 'http://localhost:18080'
const ADMIN_EMAIL = process.env.E2E_ADMIN_EMAIL || 'admin@fikopar.local'
const ADMIN_PASSWORD = process.env.E2E_ADMIN_PASSWORD || 'admin123'

const LEDGER: [code: string, name: string, type: string][] = [
  ['100.01', 'Cash · Depot 1', 'ASSET'],
  ['100.02', 'Cash · Depot 2', 'ASSET'],
  ['102.00002', '[BANK A]', 'ASSET'],
  ['153.01', 'Merchandise', 'ASSET'],
  ['191.20', 'Input VAT', 'ASSET'],
  ['391.20', 'Output VAT', 'LIABILITY'],
  ['500.01', 'Opening balances', 'EQUITY'],
  ['600.01', 'Domestic sales', 'INCOME'],
  ['610.01', 'Sales returns', 'INCOME'],
  ['611.01', 'Sales discounts', 'INCOME'],
  ['689.01', 'Cash over / short', 'EXPENSE'],
  ['770.01', 'Fuel', 'EXPENSE'],
  ['770.02', 'Office supplies', 'EXPENSE'],
  ['770.03', 'Freight', 'EXPENSE'],
  ['770.04', 'Meals', 'EXPENSE'],
]

interface Party {
  no: string
  name: string
  city: string
  district?: string
  tipi: Tipi
  // Closing balance of 2025 per depot (debit positive); the 2026 opening once carried forward.
  closing: [number, number]
}

const PARTIES: Party[] = [
  {
    no: '10482',
    name: 'Yılmaz Tarım Makinaları',
    city: 'Konya',
    district: 'Selçuklu',
    tipi: 'MUSTERI',
    closing: [142300, 18170],
  },
  { no: '10711', name: 'Öztürk Tarım', city: 'Konya', tipi: 'MUSTERI', closing: [9815, 0] },
  {
    no: '10744',
    name: 'Çelik Ziraat Aletleri',
    city: 'Konya',
    tipi: 'MUSTERI',
    closing: [12400, 0],
  },
  {
    no: '10790',
    name: 'Ereğli Traktör Servis',
    city: 'Konya',
    tipi: 'MUSTERI',
    closing: [6200, 2100],
  },
  { no: '10801', name: 'Beyşehir Tarım', city: 'Konya', tipi: 'MUSTERI', closing: [8450, 0] },
  { no: '10517', name: 'Kaya Ziraat', city: 'Aksaray', tipi: 'MUSTERI', closing: [71500, 0] },
  { no: '10622', name: 'Aksoy Tarım', city: 'Niğde', tipi: 'MUSTERI', closing: [15200, 0] },
  { no: '00044', name: 'Ege Hidrolik San.', city: 'İzmir', tipi: 'FIRMA', closing: [0, -96000] },
  { no: '00051', name: 'Marmara Filtre', city: 'Bursa', tipi: 'FIRMA', closing: [-41200, 0] },
]

const GROUP: Record<Tipi, [prefix: string, type: string]> = {
  MUSTERI: ['120', 'ASSET'],
  FIRMA: ['320', 'LIABILITY'],
  BANKA: ['102', 'ASSET'],
}

// [date, depot, party no, net amount incl. VAT, description]
const SALES_2026: [string, number, string, number, string][] = [
  ['2026-09-09', 1, '10482', 72150, 'Sales invoice SF-2026-1-000391'],
  ['2026-09-24', 2, '10482', 3480, 'Sales invoice SF-2026-2-000112'],
  ['2026-09-29', 1, '10482', 48720, 'Sales invoice SF-2026-1-000418'],
  ['2026-09-18', 1, '10744', 22500, 'Sales invoice SF-2026-1-000402'],
  ['2026-09-02', 1, '10790', 9800, 'Sales invoice SF-2026-1-000377'],
]

// [date, depot, party no, kind, amount]
const RECEIPTS: [string, number, string, 'COLLECTION' | 'PAYMENT', number][] = [
  ['2026-09-08', 1, '10801', 'COLLECTION', 18000],
  ['2026-09-15', 1, '10482', 'COLLECTION', 40000],
  ['2026-09-23', 1, '10711', 'COLLECTION', 12400],
  ['2026-09-24', 1, '00051', 'PAYMENT', 25000],
  ['2026-09-26', 1, '10622', 'COLLECTION', 25000],
  ['2026-09-29', 1, '10517', 'COLLECTION', 15000],
]

const money = (n: number) => n.toFixed(2)

// Card 10482 has a salesperson (v1.5); documents of the card take it as their default.
const SALES_REP = 'Selin Arslan'
const SALES_REP_CARD = '10482'

class Api {
  token = ''

  async call<T>(method: string, path: string, body?: unknown, ifMatch?: number): Promise<T> {
    const headers: Record<string, string> = { Accept: 'application/json' }
    if (ifMatch !== undefined) headers['If-Match'] = `"${ifMatch}"`
    if (this.token) headers.Authorization = `Bearer ${this.token}`
    if (body !== undefined) headers['Content-Type'] = 'application/json'
    if (method === 'POST') headers['Idempotency-Key'] = randomUUID()
    const res = await fetch(`${API}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    })
    const text = await res.text()
    if (!res.ok) throw new Error(`${method} ${path} → ${res.status} ${text}`)
    return (text ? JSON.parse(text) : null) as T
  }
}

export async function waitForApi(timeoutMs = 120_000): Promise<void> {
  const until = Date.now() + timeoutMs
  for (;;) {
    try {
      if ((await fetch(`${API}/healthz`)).ok) return
    } catch {
      // not up yet
    }
    if (Date.now() > until) throw new Error(`API at ${API} did not answer /healthz`)
    await new Promise((r) => setTimeout(r, 1000))
  }
}

async function org(api: Api, name: string, slug: string): Promise<number> {
  const all = await api.call<{ id: number; slug: string }[]>('GET', '/api/admin/organizations')
  const hit = all.find((o) => o.slug === slug)
  if (hit) return hit.id
  return (await api.call<{ id: number }>('POST', '/api/admin/organizations', { name, slug })).id
}

async function depots(api: Api, o: string): Promise<Record<string, number>> {
  let list = await api.call<{ id: number; code: string }[]>('GET', `${o}/depots`)
  // Reverse code order: ids must not line up with codes, so id-for-code slips show up.
  for (const code of ['2', '1']) {
    if (list.some((d) => d.code === code)) continue
    await api.call('POST', `${o}/depots`, { code, name: `Depot ${code}`, vatRate: '20' })
  }
  list = await api.call('GET', `${o}/depots`)
  return Object.fromEntries(list.map((d) => [d.code, d.id]))
}

async function accounts(api: Api, o: string): Promise<Record<string, number>> {
  const out: Record<string, number> = {}
  let cursor: string | null = null
  do {
    const page: { items: { id: number; code: string }[]; nextCursor?: string | null } =
      await api.call('GET', `${o}/ledger/accounts?limit=200${cursor ? `&cursor=${cursor}` : ''}`)
    for (const a of page.items) out[a.code] = a.id
    cursor = page.nextCursor ?? null
  } while (cursor)
  return out
}

// A company member to be the salesperson of a card; created once per company.
async function member(
  api: Api,
  o: string,
  orgId: number,
  slug: string,
): Promise<{ userId: number; name: string }> {
  const find = async () =>
    (await api.call<{ userId: number; name: string }[]>('GET', `${o}/members`)).find(
      (m) => m.name === SALES_REP,
    )
  const hit = await find()
  if (hit) return hit
  await api.call('POST', '/api/admin/users', {
    email: `selin.arslan.${slug}@fikopar.local`,
    password: 'salesrep-2026',
    name: SALES_REP,
    role: 'user',
    organization_ids: [orgId],
  })
  const made = await find()
  if (!made) throw new Error(`salesperson ${SALES_REP} is not a member of ${o}`)
  return made
}

async function sicils(api: Api, o: string): Promise<Record<string, number>> {
  const grid = await api.call<{ rows: { id: number; hesap_no: string }[] }>(
    'POST',
    `${o}/sicils/grid`,
    { startRow: 0, endRow: 1000 },
  )
  return Object.fromEntries(grid.rows.map((s) => [s.hesap_no, s.id]))
}

export async function seed(slug = 'demo-a'): Promise<Seeded> {
  const api = new Api()
  api.token = (
    await api.call<{ token: string }>('POST', '/api/auth/login', {
      email: ADMIN_EMAIL,
      password: ADMIN_PASSWORD,
    })
  ).token

  const orgId = await org(api, 'Demo Company A', slug)
  await org(api, 'Demo Company B', 'demo-b')
  const o = `/api/orgs/${orgId}`
  const depot = await depots(api, o)

  let acc = await accounts(api, o)
  for (const [code, name, type] of LEDGER) {
    if (!acc[code]) await api.call('POST', `${o}/ledger/accounts`, { code, name, type })
  }

  const salesRep = await member(api, o, orgId, slug)

  let sic = await sicils(api, o)
  for (const p of PARTIES) {
    if (sic[p.no]) continue
    await api.call('POST', `${o}/sicils`, {
      hesap_no: p.no,
      hesap_adi: p.name,
      sehir: p.city,
      ilce: p.district ?? '',
      sicil_tipi: p.tipi,
      gun: '30',
      iskonto: 10,
      v_dairesi: p.city,
      v_no: '[TAX NO]',
      credit_limit: p.tipi === 'MUSTERI' ? '250000.00' : null,
      telefonlar: [{ tipi: 'IS', numara: '+90 332 000 00 00' }],
      sales_rep_user_id: p.no === SALES_REP_CARD ? salesRep.userId : null,
    })
  }
  sic = await sicils(api, o)
  acc = await accounts(api, o)
  for (const p of PARTIES) {
    const [prefix, type] = GROUP[p.tipi]
    const code = `${prefix}.${p.no}`
    if (!acc[code]) {
      await api.call('POST', `${o}/ledger/accounts`, {
        code,
        name: p.name,
        type,
        sicilId: sic[p.no],
      })
    }
  }
  acc = await accounts(api, o)

  const roles = [
    { role: 'CASH', depotId: depot['1'], accountId: acc['100.01'] },
    { role: 'CASH', depotId: depot['2'], accountId: acc['100.02'] },
    { role: 'SALES', depotId: null, accountId: acc['600.01'] },
    { role: 'OUTPUT_VAT', depotId: null, accountId: acc['391.20'] },
    { role: 'OPENING', depotId: null, accountId: acc['500.01'] },
    { role: 'CASH_OVER_SHORT', depotId: null, accountId: acc['689.01'] },
    // v1.4 posting table (ADR-0013 §2).
    { role: 'SALES_RETURNS', depotId: null, accountId: acc['610.01'] },
    { role: 'SALES_DISCOUNTS', depotId: null, accountId: acc['611.01'] },
    { role: 'PRICE_DIFFERENCE', depotId: null, accountId: acc['600.01'] },
    { role: 'PURCHASES', depotId: null, accountId: acc['153.01'] },
    { role: 'PURCHASE_RETURNS', depotId: null, accountId: acc['153.01'] },
    { role: 'INPUT_VAT', depotId: null, accountId: acc['191.20'] },
  ]
  await api.call('PUT', `${o}/ledger/account-roles`, roles)

  const entries = await api.call<{ items: unknown[] }>('GET', `${o}/ledger/entries?limit=1`)
  if (entries.items.length === 0) await activity(api, o, depot, acc, sic)

  const stocked = await stock(api, o, depot)
  const documents = await trade(api, o, depot, sic, stocked.parts)
  return {
    token: api.token,
    orgId,
    depots: depot,
    accounts: acc,
    sicils: sic,
    ...stocked,
    documents,
    salesRep,
  }
}

// Parts from the mockups; depot rows are [on hand, min, max, shelf].
interface SeedPart {
  stockNo: string
  name: string
  catalog: number
  price: number
  oem?: string
  alt?: string
  eq?: string
  d1?: [number, number, number, string]
  d2?: [number, number, number, string]
}

const CATALOGS: [number, string][] = [
  [12, 'Hydraulics 2026'],
  [20, 'Filters & seals'],
  [31, 'Transmission'],
]

const PARTS: SeedPart[] = [
  {
    stockNo: 'HP-3702',
    name: 'Hydraulic pump, 2-cyl.',
    catalog: 12,
    price: 9850,
    oem: '3774611M91',
    d1: [6, 2, 12, 'A-04-1'],
    d2: [1, 0, 4, 'H-11-2'],
  },
  {
    stockNo: 'HP-3774',
    name: 'Hydraulic pump, 3-cyl.',
    catalog: 12,
    price: 10700,
    oem: '3774612M91',
    alt: 'K9-3774',
    d1: [12, 6, 20, 'A-04-2'],
    d2: [2, 2, 8, 'H-11-1'],
  },
  {
    stockNo: 'CL-1150',
    name: 'Clutch disc 11"',
    catalog: 31,
    price: 3650,
    d1: [3, 8, 16, 'A-05-1'],
    d2: [2, 2, 6, 'F-05-1'],
  },
  {
    stockNo: 'CL-1210',
    name: 'Clutch disc 12"',
    catalog: 31,
    price: 4100,
    eq: 'CL-1150',
    d1: [5, 2, 10, 'A-05-3'],
  },
  {
    stockNo: 'FL-0092',
    name: 'Fuel filter set',
    catalog: 20,
    price: 265,
    d1: [12, 40, 80, 'B-01-1'],
    d2: [30, 20, 60, 'E-01-1'],
  },
  {
    stockNo: 'BR-2210',
    name: 'Brake lining pair',
    catalog: 31,
    price: 375,
    d1: [0, 10, 30, 'B-02-2'],
    d2: [6, 4, 12, 'F-02-2'],
  },
  {
    stockNo: 'OF-0457',
    name: 'Oil filter, hydraulic',
    catalog: 20,
    price: 540,
    d1: [2, 5, 20, 'B-03-1'],
  },
  {
    stockNo: 'GS-4410',
    name: 'Gasket set, head',
    catalog: 20,
    price: 1290,
    d1: [18, 4, 24, 'A-06-1'],
  },
  {
    stockNo: 'GS-4418',
    name: 'Gasket set, sump',
    catalog: 20,
    price: 640,
    d1: [9, 2, 12, 'A-06-2'],
  },
  {
    stockNo: 'BE-0620',
    name: 'V-belt 13×1250',
    catalog: 31,
    price: 185,
    d1: [32, 10, 50, 'A-07-1'],
    d2: [8, 0, 20, 'F-07-1'],
  },
]

const FAMILIES: [string, string, number][] = [
  ['HV', 'Control valve', 12],
  ['SE', 'Seal kit', 20],
  ['BG', 'Bearing', 31],
]

// Thirty more parts, three families of ten, both depots.
for (const [f, [prefix, base, catalog]] of FAMILIES.entries()) {
  for (let i = 0; i < 10; i++) {
    const n = 1000 + f * 100 + i * 7
    PARTS.push({
      stockNo: `${prefix}-${n}`,
      name: `${base}, ${['small', 'medium', 'large', 'heavy duty', 'kit'][i % 5]}`,
      catalog,
      price: 120 + i * 45 + f * 300,
      oem: i % 2 ? `${n}${prefix}${i}` : undefined,
      d1: [i + 2, i % 3 ? 0 : 3, 20, `C-${String(i + 1).padStart(2, '0')}-${f + 1}`],
      d2: [i % 4, 0, 10, `G-${String(i + 1).padStart(2, '0')}-${f + 1}`],
    })
  }
}

interface Row {
  id: number
  partId: number
  version: number
}

async function count(
  api: Api,
  o: string,
  depotId: number,
  countDate: string,
  target: Map<number, number>,
  finalize: boolean,
): Promise<number> {
  const c = await api.call<{ id: number; version: number }>('POST', `${o}/stock/counts`, {
    depotId,
    countDate,
  })
  const lines = await api.call<{ rows: Row[] }>('POST', `${o}/stock/counts/${c.id}/lines/grid`, {
    startRow: 0,
    endRow: 1000,
  })
  if (!finalize) return c.id
  for (const l of lines.rows) {
    const qty = target.get(l.partId)
    if (qty === undefined) continue
    await api.call(
      'PATCH',
      `${o}/stock/counts/${c.id}/lines/${l.id}`,
      { countedQty: String(qty) },
      l.version,
    )
  }
  await api.call('POST', `${o}/stock/counts/${c.id}/finalize`, { uncounted: 'SKIP' }, c.version)
  return c.id
}

async function stock(
  api: Api,
  o: string,
  depot: Record<string, number>,
): Promise<Pick<Seeded, 'catalogs' | 'parts' | 'openCountId'>> {
  const listCatalogs = () =>
    api.call<{ id: number; code: number }[]>('GET', `${o}/catalog/catalogs`)
  let cats: { id: number; code: number }[]
  try {
    cats = await listCatalogs()
  } catch (e) {
    // A backend before v1.2 has no catalog: seed the ledger only.
    if (String(e).includes('→ 404')) return { catalogs: {}, parts: {}, openCountId: null }
    throw e
  }
  const fresh = cats.length === 0
  for (const [code, name] of CATALOGS) {
    if (!cats.some((c) => c.code === code)) {
      await api.call('POST', `${o}/catalog/catalogs`, { code, name })
    }
  }
  cats = await listCatalogs()
  const catalogs = Object.fromEntries(cats.map((c) => [String(c.code), c.id]))

  const grid = await api.call<{ rows: { id: number; stockNo: string }[] }>(
    'POST',
    `${o}/catalog/parts/grid`,
    { startRow: 0, endRow: 1000, includeInactive: true },
  )
  const parts: Record<string, number> = Object.fromEntries(grid.rows.map((r) => [r.stockNo, r.id]))
  if (!fresh) {
    const open = await api.call<{ id: number }[]>(
      'GET',
      `${o}/stock/counts?status=OPEN&depotId=${depot['2']}`,
    )
    return { catalogs, parts, openCountId: open[0]?.id ?? null }
  }

  for (const p of PARTS) {
    const codes = [
      ...(p.oem ? [{ kind: 'OEM', code: p.oem }] : []),
      ...(p.alt ? [{ kind: 'ALT', code: p.alt }] : []),
      ...(p.eq ? [{ kind: 'EQUIVALENT', code: p.eq, equivalentPartId: parts[p.eq] }] : []),
    ]
    const made = await api.call<{ id: number }>('POST', `${o}/catalog/parts`, {
      catalogId: catalogs[String(p.catalog)],
      stockNo: p.stockNo,
      name: p.name,
      brand: '[BRAND A]',
      unit: 'PCS',
      packageQty: 1,
      vatPct: '20',
      codes,
      initialPrice: { price: money(p.price), validFrom: '2026-01-02' },
    })
    parts[p.stockNo] = made.id
    for (const [d, row] of [
      ['1', p.d1],
      ['2', p.d2],
    ] as const) {
      if (!row) continue
      await api.call(
        'PUT',
        `${o}/catalog/parts/${made.id}/depots/${depot[d]}`,
        { minQty: String(row[1]), maxQty: String(row[2]), shelf: row[3] },
        0,
      )
    }
  }

  // HP-3774 history as in mockup 09, plus a price that starts after today.
  const hp = parts['HP-3774']
  for (const [validFrom, price] of [
    ['2026-04-01', 11600],
    ['2026-09-01', 12400],
    ['2026-10-15', 13100],
  ] as const) {
    await api.call('POST', `${o}/catalog/parts/${hp}/prices`, { price: money(price), validFrom })
  }

  // On hand from a January count per depot, then a transfer that drives OF-0457 negative.
  for (const d of ['1', '2'] as const) {
    const target = new Map<number, number>()
    for (const p of PARTS) {
      const row = d === '1' ? p.d1 : p.d2
      if (row) target.set(parts[p.stockNo], row[0])
    }
    await count(api, o, depot[d], '2026-01-02', target, true)
  }
  await api.call('POST', `${o}/stock/transfers`, {
    fromDepotId: depot['1'],
    toDepotId: depot['2'],
    date: '2026-09-15',
    note: 'Seed transfer',
    lines: [
      { partId: parts['HP-3774'], qty: '2' },
      { partId: parts['OF-0457'], qty: '5' },
    ],
  })
  const openCountId = await count(api, o, depot['2'], '2026-09-28', new Map(), false)
  return { catalogs, parts, openCountId }
}

async function activity(
  api: Api,
  o: string,
  depot: Record<string, number>,
  acc: Record<string, number>,
  sic: Record<string, number>,
) {
  const party = (no: string) => {
    const p = PARTIES.find((x) => x.no === no)!
    return acc[`${GROUP[p.tipi][0]}.${no}`]
  }
  const entry = (depotId: number, entryDate: string, description: string, lines: Json[]) =>
    api.call('POST', `${o}/ledger/entries`, { depotId, entryDate, description, lines })

  // 2025: one balancing entry per party and depot, so 31.12.2025 closes at `closing`.
  for (const p of PARTIES) {
    for (const d of [0, 1] as const) {
      const amount = p.closing[d]
      if (!amount) continue
      const side = amount > 0 ? 'DEBIT' : 'CREDIT'
      const other = amount > 0 ? 'CREDIT' : 'DEBIT'
      await entry(depot[String(d + 1)], '2025-12-15', `2025 activity · ${p.name}`, [
        { accountId: party(p.no), side, amount: money(Math.abs(amount)) },
        {
          accountId: acc[amount > 0 ? '600.01' : '770.03'],
          side: other,
          amount: money(Math.abs(amount)),
        },
      ])
    }
  }

  for (const [date, d, no, gross, description] of SALES_2026) {
    const net = Math.round((gross / 1.2) * 100) / 100
    await entry(depot[String(d)], date, description, [
      { accountId: party(no), side: 'DEBIT', amount: money(gross) },
      { accountId: acc['600.01'], side: 'CREDIT', amount: money(net) },
      { accountId: acc['391.20'], side: 'CREDIT', amount: money(gross - net) },
    ])
  }

  for (const [date, d, no, kind, amount] of RECEIPTS) {
    await api.call('POST', `${o}/receipts`, {
      kind,
      depotId: depot[String(d)],
      sicilId: sic[no],
      date,
      method: 'CASH',
      amount: money(amount),
      currency: 'TRY',
      description: kind === 'COLLECTION' ? 'Collection' : 'Payment',
    })
  }

  const voucher = (
    kind: string,
    date: string,
    accountId: number,
    amount: number,
    description: string,
  ) =>
    api.call('POST', `${o}/cash-vouchers`, {
      kind,
      depotId: depot['1'],
      date,
      accountId,
      amount: money(amount),
      description,
    })
  await voucher('EXPENSE', '2026-09-15', acc['770.02'], 1264.5, 'Office supplies')
  await voucher('EXPENSE', '2026-09-25', acc['770.01'], 3800, 'Fuel · delivery van')
  await voucher('BANK_DEPOSIT', '2026-09-27', acc['102.00002'], 15000, 'Deposit to [BANK A]')
}

interface Doc {
  id: number
  version: number
  documentNo: string | null
  lines: { id: number; partId: number | null }[]
}

// v1.4 documents: a purchase, a sale, a waybill and its bill, a return, a price increase and a
// decrease, and a draft over Öztürk Tarım's credit limit. Posted through the API like the screens do.
async function trade(
  api: Api,
  o: string,
  depot: Record<string, number>,
  sic: Record<string, number>,
  parts: Record<string, number>,
): Promise<Record<string, number>> {
  const out: Record<string, number> = {}
  const existing = await api.call<{ rows: { id: number; type: string; status: string }[] }>(
    'POST',
    `${o}/documents/grid`,
    { startRow: 0, endRow: 100, sortModel: [{ colId: 'documentDate', sort: 'asc' }] },
  )
  if (existing.rows.length) {
    const byType = (t: string, s = 'POSTED') =>
      existing.rows.find((r) => r.type === t && r.status === s)?.id
    out.sale = byType('SALES_INVOICE_WITH_WAYBILL')!
    out.purchase = byType('PURCHASE_INVOICE')!
    out.waybill = byType('SALES_WAYBILL')!
    out.bill = byType('SALES_INVOICE')!
    out.return = byType('SALES_RETURN')!
    out.draft = byType('SALES_INVOICE', 'DRAFT')!
    return out
  }
  const d1 = depot['1']
  const create = (body: Json) => api.call<Doc>('POST', `${o}/documents`, body)
  const post = (d: Doc) => api.call<Doc>('POST', `${o}/documents/${d.id}/post`, {}, d.version)
  const line = (no: string, qty: number, price: number, extra: Json = {}) => ({
    partId: parts[no],
    qty: String(qty),
    unitPrice: money(price),
    discount1Pct: '0',
    discount2Pct: '0',
    vatPct: '20',
    ...extra,
  })

  const purchase = await post(
    await create({
      type: 'PURCHASE_INVOICE',
      depotId: d1,
      sicilId: sic['00044'],
      documentDate: '2026-09-20',
      lines: [line('HP-3774', 5, 8900), line('CL-1150', 10, 2850)],
    }),
  )
  out.purchase = purchase.id
  const sale = await post(
    await create({
      type: 'SALES_INVOICE_WITH_WAYBILL',
      depotId: d1,
      sicilId: sic['10482'],
      documentDate: '2026-09-22',
      lines: [
        line('HP-3702', 2, 9850, { discount1Pct: '10' }),
        line('CL-1150', 4, 3650, { discount1Pct: '5' }),
      ],
    }),
  )
  out.sale = sale.id
  const waybill = await post(
    await create({
      type: 'SALES_WAYBILL',
      depotId: d1,
      sicilId: sic['10517'],
      documentDate: '2026-09-23',
      lines: [line('CL-1210', 1, 4100)],
    }),
  )
  out.waybill = waybill.id
  // An empty bill copies the waybill line for line.
  const bill = await post(
    await create({
      type: 'SALES_INVOICE',
      depotId: d1,
      sicilId: sic['10517'],
      documentDate: '2026-09-24',
      waybillDocumentId: waybill.id,
      lines: [],
    }),
  )
  out.bill = bill.id
  const ret = await post(
    await create({
      type: 'SALES_RETURN',
      depotId: d1,
      sicilId: sic['10482'],
      documentDate: '2026-09-25',
      linkedDocumentId: sale.id,
      lines: [line('HP-3702', 1, 9850, { discount1Pct: '10', originLineId: sale.lines[0].id })],
    }),
  )
  out.return = ret.id
  const amount = (price: number, extra: Json) => ({
    qty: '1',
    unitPrice: money(price),
    vatPct: '20',
    ...extra,
  })
  out.increase = (
    await post(
      await create({
        type: 'PRICE_DIFFERENCE',
        priceDirection: 'INCREASE',
        depotId: d1,
        sicilId: sic['10482'],
        documentDate: '2026-09-26',
        linkedDocumentId: sale.id,
        lines: [amount(500, { originLineId: sale.lines[1].id })],
      }),
    )
  ).id
  out.decrease = (
    await post(
      await create({
        type: 'PRICE_DIFFERENCE',
        priceDirection: 'DECREASE',
        depotId: d1,
        sicilId: sic['00044'],
        documentDate: '2026-09-27',
        linkedDocumentId: purchase.id,
        lines: [amount(300, { partId: null, description: 'Volume rebate, September' })],
      }),
    )
  ).id
  out.draft = (
    await create({
      type: 'SALES_INVOICE',
      depotId: d1,
      sicilId: sic['10711'],
      documentDate: '2026-09-29',
      lines: [line('HP-3774', 25, 12400)],
    })
  ).id
  return out
}

if (import.meta.url === `file://${process.argv[1]}`) {
  await waitForApi()
  const s = await seed()
  console.log(`seeded org ${s.orgId} at ${API}`)
}
