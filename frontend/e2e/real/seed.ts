// Synthetic demo data for a real backend, written through the public API only (no SQL).
// Mirrors the MSW mockups: Demo Company A, depots 1 and 2, the featured accounts, 2025 activity
// that carries into 2026, and a few 2026 receipts and till vouchers. Safe to run twice.
//
//   E2E_API_URL=http://localhost:18080 node --experimental-strip-types e2e/real/seed.ts

import { randomUUID } from 'node:crypto'

type Json = Record<string, unknown>
type Tipi = 'MUSTERI' | 'FIRMA' | 'BANKA'

export interface Seeded {
  token: string
  orgId: number
  accounts: Record<string, number>
  sicils: Record<string, number>
}

const API = process.env.E2E_API_URL || 'http://localhost:18080'
const ADMIN_EMAIL = process.env.E2E_ADMIN_EMAIL || 'admin@fikopar.local'
const ADMIN_PASSWORD = process.env.E2E_ADMIN_PASSWORD || 'admin123'

const LEDGER: [code: string, name: string, type: string][] = [
  ['100.01', 'Cash · Depot 1', 'ASSET'],
  ['100.02', 'Cash · Depot 2', 'ASSET'],
  ['102.00002', '[BANK A]', 'ASSET'],
  ['391.20', 'Output VAT', 'LIABILITY'],
  ['500.01', 'Opening balances', 'EQUITY'],
  ['600.01', 'Domestic sales', 'INCOME'],
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

class Api {
  token = ''

  async call<T>(method: string, path: string, body?: unknown): Promise<T> {
    const headers: Record<string, string> = { Accept: 'application/json' }
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
  for (const code of ['1', '2']) {
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

async function sicils(api: Api, o: string): Promise<Record<string, number>> {
  const grid = await api.call<{ rows: { id: number; hesap_no: string }[] }>(
    'POST',
    `${o}/sicils/grid`,
    { startRow: 0, endRow: 1000 },
  )
  return Object.fromEntries(grid.rows.map((s) => [s.hesap_no, s.id]))
}

export async function seed(): Promise<Seeded> {
  const api = new Api()
  api.token = (
    await api.call<{ token: string }>('POST', '/api/auth/login', {
      email: ADMIN_EMAIL,
      password: ADMIN_PASSWORD,
    })
  ).token

  const orgId = await org(api, 'Demo Company A', 'demo-a')
  await org(api, 'Demo Company B', 'demo-b')
  const o = `/api/orgs/${orgId}`
  const depot = await depots(api, o)

  let acc = await accounts(api, o)
  for (const [code, name, type] of LEDGER) {
    if (!acc[code]) await api.call('POST', `${o}/ledger/accounts`, { code, name, type })
  }

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
      telefonlar: [{ tipi: 'IS', numara: '+90 332 000 00 00' }],
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

  await api.call('PUT', `${o}/ledger/account-roles`, [
    { role: 'CASH', depotId: depot['1'], accountId: acc['100.01'] },
    { role: 'CASH', depotId: depot['2'], accountId: acc['100.02'] },
    { role: 'SALES', depotId: null, accountId: acc['600.01'] },
    { role: 'OUTPUT_VAT', depotId: null, accountId: acc['391.20'] },
    { role: 'OPENING', depotId: null, accountId: acc['500.01'] },
    { role: 'CASH_OVER_SHORT', depotId: null, accountId: acc['689.01'] },
  ])

  const entries = await api.call<{ items: unknown[] }>('GET', `${o}/ledger/entries?limit=1`)
  if (entries.items.length === 0) await activity(api, o, depot, acc, sic)

  return { token: api.token, orgId, accounts: acc, sicils: sic }
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

if (import.meta.url === `file://${process.argv[1]}`) {
  await waitForApi()
  const s = await seed()
  console.log(`seeded org ${s.orgId} at ${API}`)
}
