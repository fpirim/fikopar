import type {
  Account,
  ActivityItem,
  BalanceRow,
  CarryForwardRow,
  CashBookLine,
  Depot,
  JournalEntry,
  NumberSeries,
  Organization,
  Posting,
  Receipt,
  Sicil,
  SicilTipi,
  StatementLine,
  TrailItem,
  User,
} from '../api/types'

export const ORG_ID = 1
export const TODAY = '2026-09-29'

function rng(seed: number) {
  let a = seed
  return () => {
    a = (a + 0x6d2b79f5) | 0
    let t = Math.imul(a ^ (a >>> 15), 1 | a)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

const m = (n: number) => n.toFixed(2)

export const organizations: Organization[] = [
  { id: 1, name: 'Demo Company A', slug: 'demo-a' },
  { id: 2, name: 'Demo Company B', slug: 'demo-b' },
]

export const demoUser: User = {
  id: 12,
  email: 'demo@fikopar.local',
  name: 'Demo',
  role: 'user',
  organizations,
}

export const depots: Depot[] = [
  { id: 1, code: '1', name: 'Depot 1', vatRate: '20', allowNegativeStock: false, version: 1 },
  { id: 2, code: '2', name: 'Depot 2', vatRate: '20', allowNegativeStock: false, version: 1 },
]

interface Party {
  sicilId: number
  accountId: number
  code: string
  name: string
  city: string
  district?: string
  tipi: SicilTipi
  debit: [number, number]
  credit: [number, number]
  overdue: number
  lastMove: string
  opening: [number, number]
}

const FEATURED: Omit<Party, 'sicilId' | 'accountId'>[] = [
  {
    code: '120.10482',
    name: 'Yılmaz Tarım Makinaları',
    city: 'Konya',
    district: 'Selçuklu',
    tipi: 'MUSTERI',
    debit: [1083230, 121650],
    credit: [915060, 103480],
    overdue: 0,
    lastMove: '2026-09-29',
    opening: [142300, 18170],
  },
  {
    code: '120.10711',
    name: 'Öztürk Tarım',
    city: 'Konya',
    tipi: 'MUSTERI',
    debit: [78215, 20000],
    credit: [51900, 10000],
    overdue: 9815,
    lastMove: '2026-09-26',
    opening: [9815, 0],
  },
  {
    code: '120.10744',
    name: 'Çelik Ziraat Aletleri',
    city: 'Konya',
    tipi: 'MUSTERI',
    debit: [54600, 0],
    credit: [32100, 0],
    overdue: 0,
    lastMove: '2026-09-18',
    opening: [12400, 0],
  },
  {
    code: '120.10790',
    name: 'Ereğli Traktör Servis',
    city: 'Konya',
    tipi: 'MUSTERI',
    debit: [31020, 10000],
    credit: [21400, 6000],
    overdue: 4200,
    lastMove: '2026-09-02',
    opening: [6200, 2100],
  },
  {
    code: '120.10801',
    name: 'Beyşehir Tarım',
    city: 'Konya',
    tipi: 'MUSTERI',
    debit: [38450, 0],
    credit: [26000, 0],
    overdue: 0,
    lastMove: '2026-09-21',
    opening: [8450, 0],
  },
  {
    code: '120.10833',
    name: 'Karapınar Yedek Parça',
    city: 'Konya',
    tipi: 'MUSTERI',
    debit: [29900, 0],
    credit: [18700, 0],
    overdue: 0,
    lastMove: '2026-09-11',
    opening: [4300, 0],
  },
  {
    code: '120.10517',
    name: 'Kaya Ziraat',
    city: 'Aksaray',
    tipi: 'MUSTERI',
    debit: [236000, 0],
    credit: [142000, 0],
    overdue: 24000,
    lastMove: '2026-09-29',
    opening: [71500, 0],
  },
  {
    code: '120.10603',
    name: 'Demir Traktör Yedek Parça',
    city: 'Karaman',
    tipi: 'MUSTERI',
    debit: [148500, 42000],
    credit: [102500, 30000],
    overdue: 0,
    lastMove: '2026-09-28',
    opening: [0, 0],
  },
  {
    code: '120.10622',
    name: 'Aksoy Tarım',
    city: 'Niğde',
    tipi: 'MUSTERI',
    debit: [88400, 0],
    credit: [71000, 0],
    overdue: 3100,
    lastMove: '2026-09-26',
    opening: [15200, 0],
  },
  {
    code: '320.00044',
    name: 'Ege Hidrolik San.',
    city: 'İzmir',
    tipi: 'FIRMA',
    debit: [0, 420000],
    credit: [0, 516000],
    overdue: 0,
    lastMove: '2026-09-27',
    opening: [0, -96000],
  },
  {
    code: '320.00051',
    name: 'Marmara Filtre',
    city: 'Bursa',
    tipi: 'FIRMA',
    debit: [212000, 0],
    credit: [248500, 0],
    overdue: 0,
    lastMove: '2026-09-26',
    opening: [-41200, 0],
  },
]

const FIRST = [
  'Anadolu',
  'Toros',
  'Bereket',
  'Hasat',
  'Yeşilova',
  'Kuzey',
  'Güney',
  'Birlik',
  'Atlas',
  'Kartal',
  'Göksu',
  'Meram',
  'Ilgın',
  'Kulu',
  'Cihanbeyli',
  'Seydişehir',
  'Akşehir',
  'Bozkır',
  'Sarayönü',
  'Kadınhanı',
  'Altınekin',
  'Emirgazi',
  'Hadim',
  'Taşkent',
  'Derbent',
  'Doğanhisar',
  'Hüyük',
  'Tuzlukçu',
  'Yunak',
  'Çumra',
  'Ortakent',
  'Gölyaka',
  'Obruk',
  'Sultanhanı',
  'Eskil',
  'Ulukışla',
  'Bor',
  'Ereğli',
]
const SECOND = [
  'Tarım',
  'Ziraat',
  'Traktör',
  'Makina',
  'Yedek Parça',
  'Tarım Aletleri',
  'Hidrolik',
  'Motor',
  'Tarım Ürünleri',
  'Ticaret',
  'Rulman',
  'Filtre',
]
const SUFFIX = ['', '', ' Ltd.', ' San.', ' Tic.', ' A.Ş.']
const CITIES = [
  'Konya',
  'Aksaray',
  'Karaman',
  'Niğde',
  'Ankara',
  'Eskişehir',
  'Afyon',
  'Kayseri',
  'Nevşehir',
  'Isparta',
  'Adana',
  'Mersin',
]
const SUPPLIER_CITIES = ['İzmir', 'Bursa', 'İstanbul', 'Ankara', 'Kocaeli', 'Manisa']

function synthParties(): Omit<Party, 'sicilId' | 'accountId'>[] {
  const r = rng(412)
  const out: Omit<Party, 'sicilId' | 'accountId'>[] = []
  const used = new Set(FEATURED.map((f) => f.name))
  const codes = new Set(FEATURED.map((f) => f.code))
  const pick = <T>(a: T[]) => a[Math.floor(r() * a.length)]
  const date = () => {
    const d = 1 + Math.floor(r() * 270)
    return new Date(Date.UTC(2026, 0, d)).toISOString().slice(0, 10)
  }
  let cust = 10100
  let sup = 60
  while (out.length < 401 - 4) {
    const supplier = r() < 0.17
    let name = `${pick(FIRST)} ${pick(SECOND)}${pick(SUFFIX)}`
    if (used.has(name)) name = `${name} ${out.length}`
    used.add(name)
    const city = supplier ? pick(SUPPLIER_CITIES) : pick(CITIES)
    const turnover = Math.round(3000 + r() ** 2 * 180000)
    let bal = Math.round((r() - 0.25) * turnover * 0.4)
    if (!supplier && city === 'Konya' && bal > 10000) bal = Math.round(bal / 25)
    if (supplier) bal = -Math.abs(bal)
    const d1 = Math.round(turnover * (0.5 + r() * 0.4))
    const debit: [number, number] = [d1, turnover - d1]
    const credit: [number, number] = [
      d1 - Math.round(bal * 0.7),
      turnover - d1 - (bal - Math.round(bal * 0.7)),
    ]
    let code = supplier
      ? `320.${String(sup++).padStart(5, '0')}`
      : `120.${cust + Math.floor(r() * 9)}`
    while (codes.has(code))
      code = `${code.slice(0, 4)}${String(Number(code.slice(4)) + 1).padStart(5, '0')}`
    codes.add(code)
    out.push({
      code,
      name,
      city,
      tipi: supplier ? 'FIRMA' : 'MUSTERI',
      debit,
      credit,
      overdue: bal > 0 && r() < 0.2 ? Math.round(bal * r()) : 0,
      lastMove: date(),
      opening: [Math.round(bal * 0.5), Math.round(bal * 0.2)],
    })
    cust += 10
  }
  const banks: [string, string][] = [
    ['102.00002', '[BANK A]'],
    ['102.00003', '[BANK B]'],
    ['102.00004', '[BANK C]'],
    ['102.00005', '[BANK D]'],
  ]
  for (const [code, name] of banks) {
    const inflow = Math.round(200000 + r() * 600000)
    const out1 = Math.round(inflow * (0.6 + r() * 0.3))
    out.push({
      code,
      name,
      city: 'Konya',
      tipi: 'BANKA',
      debit: [inflow, 0],
      credit: [out1, 0],
      overdue: 0,
      lastMove: '2026-09-27',
      opening: [Math.round((inflow - out1) / 2), 0],
    })
  }
  return out
}

export interface Ledger {
  parties: Party[]
  sicils: Sicil[]
  accounts: Account[]
}

function buildLedger(): Ledger {
  const parties: Party[] = [...FEATURED, ...synthParties()].map((p, i) => ({
    ...p,
    sicilId: 1000 + i,
    accountId: 5000 + i,
  }))
  const sicils: Sicil[] = parties.map((p) => ({
    id: p.sicilId,
    organization_id: ORG_ID,
    hesap_no: p.code.split('.')[1],
    hesap_adi: p.name,
    sehir: p.city,
    ilce: p.district ?? '',
    sicil_tipi: p.tipi,
    gun: '30',
    iskonto: 10,
    iskonto2: 5,
    v_dairesi: p.city === 'Konya' ? 'Selçuk' : p.city,
    v_no: '[TAX NO]',
    credit_limit: p.tipi === 'MUSTERI' ? '250000.00' : undefined,
    telefonlar: [
      { tipi: 'IS', numara: '+90 332 000 00 00' },
      { tipi: 'CEP', numara: '+90 532 000 00 00' },
    ],
    contacts: [
      { name: p.code === '120.10482' ? 'Mehmet Yılmaz' : 'Ali Demir', role: 'purchasing' },
    ],
    account_id: p.accountId,
  }))
  const accounts: Account[] = [
    ...parties.map<Account>((p) => ({
      id: p.accountId,
      orgId: ORG_ID,
      code: p.code,
      name: p.name,
      type: p.tipi === 'FIRMA' ? 'LIABILITY' : 'ASSET',
      normalSide: p.tipi === 'FIRMA' ? 'CREDIT' : 'DEBIT',
      sicilId: p.sicilId,
      currency: 'TRY',
      isActive: true,
      version: 1,
    })),
    ...(
      [
        [1, '100.01', 'Cash · Depot 1', 'ASSET'],
        [2, '100.02', 'Cash · Depot 2', 'ASSET'],
        [3, '600.01', 'Domestic sales', 'INCOME'],
        [4, '391.20', 'Output VAT', 'LIABILITY'],
        [5, '770.01', 'Fuel', 'EXPENSE'],
        [6, '770.02', 'Office supplies', 'EXPENSE'],
        [7, '770.03', 'Freight', 'EXPENSE'],
        [8, '770.04', 'Meals', 'EXPENSE'],
        [9, '689.01', 'Cash over / short', 'EXPENSE'],
        [10, '500.01', 'Opening balances', 'EQUITY'],
      ] as const
    ).map<Account>(([id, code, name, type]) => ({
      id,
      orgId: ORG_ID,
      code,
      name,
      type,
      normalSide: type === 'ASSET' || type === 'EXPENSE' ? 'DEBIT' : 'CREDIT',
      sicilId: null,
      currency: 'TRY',
      isActive: true,
      version: 1,
    })),
  ]
  return { parties, sicils, accounts }
}

export function balanceRow(p: Party, depotId: number | null): BalanceRow {
  const idx = depotId ? [depotId - 1] : [0, 1]
  const debit = idx.reduce((s, i) => s + p.debit[i], 0)
  const credit = idx.reduce((s, i) => s + p.credit[i], 0)
  return {
    accountId: p.accountId,
    accountCode: p.code,
    accountName: p.name,
    sicilId: p.sicilId,
    sicilTipi: p.tipi,
    city: p.city,
    depotId,
    debitTotal: m(debit),
    creditTotal: m(credit),
    balance: m(debit - credit),
    overdue: m(depotId === 2 ? 0 : p.overdue),
    lastMoveDate: p.lastMove,
  }
}

export interface LedgerLine extends StatementLine {
  accountId: number
}

function line(
  accountId: number,
  entryDate: string,
  depotId: number,
  documentNo: string | null,
  sourceType: StatementLine['sourceType'],
  subtype: string | null,
  dueDate: string | null,
  description: string,
  amount: number,
): LedgerLine {
  return {
    accountId,
    entryId: null,
    entryDate,
    depotId,
    documentNo,
    sourceType,
    subtype,
    dueDate,
    description,
    debit: m(Math.max(amount, 0)),
    credit: m(Math.max(-amount, 0)),
    runningBalance: '0.00',
  }
}

const YILMAZ_LINES: [string, number, string, string, string, string, number][] = [
  ['2026-09-09', 1, 'SF-2026-1-000391', 'SALES_INVOICE', '2026-10-09', 'Sales invoice', 72150],
  ['2026-09-15', 1, 'TAH-2026-1-000219', 'COLLECTION', '2026-09-15', 'Collection', -40000],
  [
    '2026-09-20',
    1,
    'AC-2026-1-000084',
    'RECEIVABLE_CHEQUE',
    '2026-10-01',
    'Cheque received',
    -55000,
  ],
  ['2026-09-24', 2, 'SF-2026-2-000112', 'SALES_INVOICE', '2026-10-24', 'Sales invoice', 3480],
  ['2026-09-26', 2, 'SI-2026-2-000009', 'SALES_RETURN', '2026-09-26', 'Sales return', -3480],
  ['2026-09-29', 1, 'SF-2026-1-000418', 'SALES_INVOICE', '2026-10-29', 'Sales invoice', 48720],
]

const SOURCE: Record<string, StatementLine['sourceType']> = {
  SALES_INVOICE: 'document',
  SALES_RETURN: 'document',
  COLLECTION: 'receipt',
  PAYMENT: 'receipt',
  RECEIVABLE_CHEQUE: 'instrument',
}

function ledgerLines(parties: Party[]): LedgerLine[] {
  const out: LedgerLine[] = []
  const r = rng(7)
  for (const p of parties) {
    for (const d of [0, 1]) {
      if (p.opening[d] !== 0) {
        out.push(
          line(
            p.accountId,
            '2026-01-01',
            d + 1,
            null,
            'opening',
            null,
            null,
            'Opening',
            p.opening[d],
          ),
        )
      }
    }
    if (p.code === '120.10482') {
      for (const [date, dep, no, sub, due, desc, amt] of YILMAZ_LINES) {
        out.push(line(p.accountId, date, dep, no, SOURCE[sub], sub, due, desc, amt))
      }
      continue
    }
    for (const d of [0, 1]) {
      const target = p.debit[d] - p.credit[d] - p.opening[d]
      if (p.debit[d] === 0 && p.credit[d] === 0) continue
      const month = 3 + Math.floor(r() * 6)
      const date = `2026-${String(month).padStart(2, '0')}-${String(1 + Math.floor(r() * 27)).padStart(2, '0')}`
      const inv = Math.round(Math.abs(target) + 1000 + r() * 20000)
      const n = 100 + Math.floor(r() * 300)
      const dep = d + 1
      if (p.tipi === 'FIRMA') {
        out.push(
          line(
            p.accountId,
            date,
            dep,
            `AF-2026-${dep}-000${n}`,
            'document',
            'PURCHASE_INVOICE',
            date,
            'Purchase invoice',
            -inv,
          ),
          line(
            p.accountId,
            p.lastMove,
            dep,
            `TED-2026-${dep}-000${n}`,
            'receipt',
            'PAYMENT',
            p.lastMove,
            'Payment',
            inv + target,
          ),
        )
      } else {
        out.push(
          line(
            p.accountId,
            date,
            dep,
            `SF-2026-${dep}-000${n}`,
            'document',
            'SALES_INVOICE',
            date,
            'Sales invoice',
            inv,
          ),
          line(
            p.accountId,
            p.lastMove,
            dep,
            `TAH-2026-${dep}-000${n}`,
            'receipt',
            'COLLECTION',
            p.lastMove,
            'Collection',
            target - inv,
          ),
        )
      }
    }
  }
  return out
}

export interface CashLine extends CashBookLine {
  depotId: number
}

function cash(
  depotId: number,
  date: string,
  kind: CashBookLine['kind'],
  documentNo: string | null,
  sicilName: string | null,
  description: string,
  amount: number,
  sicilId: number | null = null,
): CashLine {
  return {
    depotId,
    date,
    kind,
    documentNo,
    sicilId,
    sicilName,
    description,
    journalEntryId: 0,
    income: m(Math.max(amount, 0)),
    expense: m(Math.max(-amount, 0)),
    runningBalance: '0.00',
  }
}

function cashLines(): CashLine[] {
  return [
    cash(1, '2026-01-01', 'OTHER', null, null, 'Opening', 21480),
    cash(1, '2026-09-08', 'COLLECTION', 'TAH-2026-1-000207', 'Beyşehir Tarım', 'Collection', 18000),
    cash(1, '2026-09-15', 'EXPENSE', 'GDR-2026-1-000036', null, 'Office supplies', -1264.5),
    cash(1, '2026-09-23', 'COLLECTION', 'TAH-2026-1-000224', 'Öztürk Tarım', 'Collection', 12400),
    cash(1, '2026-09-24', 'PAYMENT', 'TED-2026-1-000061', 'Marmara Filtre', 'Payment', -25000),
    cash(1, '2026-09-25', 'EXPENSE', 'GDR-2026-1-000038', null, 'Fuel · delivery van', -3800),
    cash(1, '2026-09-26', 'COLLECTION', 'TAH-2026-1-000228', 'Aksoy Tarım', 'Collection', 25000),
    cash(1, '2026-09-27', 'BANK_DEPOSIT', 'VRM-2026-1-000012', null, 'Deposit to [BANK A]', -15000),
    cash(1, '2026-09-29', 'COLLECTION', 'TAH-2026-1-000233', 'Kaya Ziraat', 'Collection', 15000),
    cash(2, '2026-01-01', 'OTHER', null, null, 'Opening', 8200),
    cash(
      2,
      '2026-09-21',
      'COLLECTION',
      'TAH-2026-2-000081',
      'Demir Traktör Yedek Parça',
      'Collection',
      6400,
    ),
    cash(2, '2026-09-25', 'EXPENSE', 'GDR-2026-2-000014', null, 'Freight', -950),
    cash(2, '2026-09-28', 'COLLECTION', 'TAH-2026-2-000083', 'Aksoy Tarım', 'Collection', 4300),
  ]
}

function receipts(ledger: Ledger): Receipt[] {
  const by = (name: string) => ledger.parties.find((p) => p.name === name)!
  const rows: [
    string,
    string,
    'COLLECTION' | 'PAYMENT',
    Receipt['method'],
    string,
    string | null,
    number,
  ][] = [
    ['TAH-2026-1-000233', '2026-09-29', 'COLLECTION', 'CASH', 'Kaya Ziraat', null, 15000],
    [
      'TAH-2026-1-000232',
      '2026-09-28',
      'COLLECTION',
      'CHEQUE',
      'Demir Traktör Yedek Parça',
      'AC-2026-2-000091',
      62500,
    ],
    [
      'TED-2026-2-000064',
      '2026-09-27',
      'PAYMENT',
      'BANK_TRANSFER',
      'Ege Hidrolik San.',
      null,
      60000,
    ],
    [
      'TED-2026-1-000063',
      '2026-09-26',
      'PAYMENT',
      'ENDORSEMENT',
      'Marmara Filtre',
      'AC-2026-1-000079',
      22000,
    ],
    ['TAH-2026-1-000228', '2026-09-26', 'COLLECTION', 'CASH', 'Aksoy Tarım', null, 25000],
    ['TED-2026-1-000061', '2026-09-24', 'PAYMENT', 'CASH', 'Marmara Filtre', null, 25000],
    ['TAH-2026-1-000224', '2026-09-23', 'COLLECTION', 'CASH', 'Öztürk Tarım', null, 12400],
    [
      'TAH-2026-1-000219',
      '2026-09-15',
      'COLLECTION',
      'CASH',
      'Yılmaz Tarım Makinaları',
      null,
      40000,
    ],
    ['TAH-2026-1-000207', '2026-09-08', 'COLLECTION', 'CASH', 'Beyşehir Tarım', null, 18000],
  ]
  const out: Receipt[] = rows.map(([no, date, kind, method, name, inst, amt], i) => {
    const p = by(name)
    return {
      id: 900 - i,
      orgId: ORG_ID,
      receiptNo: no,
      kind,
      depotId: Number(no.split('-')[2]),
      sicilId: p.sicilId,
      sicilName: p.name,
      date,
      method,
      amount: m(amt),
      currency: 'TRY',
      origin: inst ? 'INSTRUMENT' : 'MANUAL',
      instrumentNo: inst,
      instrumentId: inst ? 300 + i : null,
      description: kind === 'COLLECTION' ? 'Collection' : 'Payment',
      journalEntryId: 4820 - i,
      bankAccountId: method === 'BANK_TRANSFER' ? by('[BANK A]').accountId : undefined,
    }
  })
  const r = rng(99)
  const customers = ledger.parties.filter((p) => p.tipi === 'MUSTERI')
  for (let i = 0; i < 40; i++) {
    const p = customers[Math.floor(r() * customers.length)]
    const dep = r() < 0.7 ? 1 : 2
    const d = new Date(Date.UTC(2026, 7, 1 + Math.floor(i / 1.4))).toISOString().slice(0, 10)
    out.push({
      id: 800 - i,
      orgId: ORG_ID,
      receiptNo: `TAH-2026-${dep}-000${160 + i}`,
      kind: 'COLLECTION',
      depotId: dep,
      sicilId: p.sicilId,
      sicilName: p.name,
      date: d,
      method: r() < 0.6 ? 'CASH' : 'BANK_TRANSFER',
      amount: m(Math.round(1000 + r() * 30000)),
      currency: 'TRY',
      origin: 'MANUAL',
      instrumentNo: null,
      instrumentId: null,
      description: 'Collection',
      journalEntryId: 4700 - i,
    })
  }
  return out
}

function series(): NumberSeries[] {
  const s: [string, number, number][] = [
    ['TAH', 1, 234],
    ['TAH', 2, 84],
    ['TED', 1, 64],
    ['TED', 2, 65],
    ['GDR', 1, 39],
    ['GDR', 2, 15],
    ['VRM', 1, 13],
    ['VRM', 2, 4],
    ['J', 1, 4834],
    ['J', 2, 4834],
  ]
  return s.map(([prefix, depotId, nextValue], i) => ({
    id: i + 1,
    prefix: prefix as NumberSeries['prefix'],
    depotId,
    year: 2026,
    width: prefix === 'J' ? 6 : 6,
    nextValue,
    version: 1,
  }))
}

function posting(
  lineNo: number,
  a: Account,
  side: Posting['side'],
  amount: number,
  id: number,
): Posting {
  return {
    id,
    lineNo,
    accountId: a.id,
    accountCode: a.code,
    accountName: a.name,
    side,
    amount: m(amount),
    currency: 'TRY',
  }
}

function entries(ledger: Ledger): JournalEntry[] {
  const acc = (code: string) => ledger.accounts.find((a) => a.code === code)!
  const e = (
    id: number,
    date: string,
    time: string,
    depotId: number,
    description: string,
    sourceType: JournalEntry['sourceType'],
    sourceNo: string | null,
    lines: [string, Posting['side'], number][],
  ): JournalEntry => ({
    id,
    orgId: ORG_ID,
    depotId,
    entryNo: `J-2026-${String(id).padStart(6, '0')}`,
    entryDate: date,
    description,
    sourceType,
    sourceId: sourceNo,
    sourceNo,
    idempotencyKey: sourceNo ? `doc:${sourceNo}:post` : `manual:${id}`,
    reversesEntryId: null,
    reversedByEntryId: null,
    createdBy: 12,
    createdAt: `${date}T${time}`,
    postings: lines.map(([code, side, amt], i) =>
      posting(i + 1, acc(code), side, amt, id * 10 + i),
    ),
  })
  return [
    e(
      4833,
      '2026-09-29',
      '09:02:17',
      1,
      'Sales invoice SF-2026-1-000418',
      'document',
      'SF-2026-1-000418',
      [
        ['120.10482', 'DEBIT', 48720],
        ['600.01', 'CREDIT', 40600],
        ['391.20', 'CREDIT', 8120],
      ],
    ),
    e(
      4832,
      '2026-09-29',
      '08:41:03',
      1,
      'Collection TAH-2026-1-000233',
      'receipt',
      'TAH-2026-1-000233',
      [
        ['100.01', 'DEBIT', 15000],
        ['120.10517', 'CREDIT', 15000],
      ],
    ),
    e(4829, '2026-09-28', '17:20:44', 1, 'Rounding difference, September', 'manual', null, [
      ['689.01', 'DEBIT', 0.5],
      ['100.01', 'CREDIT', 0.5],
    ]),
    e(
      4826,
      '2026-09-27',
      '15:05:10',
      1,
      'Deposit to [BANK A]',
      'cash_voucher',
      'VRM-2026-1-000012',
      [
        ['102.00002', 'DEBIT', 15000],
        ['100.01', 'CREDIT', 15000],
      ],
    ),
    e(
      4824,
      '2026-09-25',
      '11:47:52',
      1,
      'Fuel · delivery van',
      'cash_voucher',
      'GDR-2026-1-000038',
      [
        ['770.01', 'DEBIT', 3800],
        ['100.01', 'CREDIT', 3800],
      ],
    ),
  ]
}

function trails(): Record<number, TrailItem[]> {
  return {
    4833: [
      {
        at: '2026-09-29T09:02:16',
        kind: 'AUDIT',
        label: 'document.posted',
        ref: 'W/"6"',
        actorUserId: 12,
      },
      {
        at: '2026-09-29T09:02:16',
        kind: 'STOCK',
        label: 'stock.moved',
        ref: '4 lines · depot 1',
        actorUserId: null,
      },
      {
        at: '2026-09-29T09:02:16',
        kind: 'OUTBOX',
        label: 'DocumentPosted',
        ref: '88213',
        actorUserId: null,
      },
      { at: '2026-09-29T09:02:17', kind: 'DELIVERY', label: 'ledger', ref: '1', actorUserId: null },
    ],
  }
}

function carryForwardRows(ledger: Ledger): CarryForwardRow[] {
  const rows: CarryForwardRow[] = []
  const r = rng(2025)
  const cf = (p: Party, depotId: number, closing: number): CarryForwardRow => ({
    accountId: p.accountId,
    accountCode: p.code,
    accountName: p.name,
    depotId,
    closingBalance: m(closing),
    openingDebit: m(Math.max(closing, 0)),
    openingCredit: m(Math.max(-closing, 0)),
    status: closing === 0 ? 'SKIP' : 'READY',
    overrideReason: null,
    journalEntryId: null,
  })
  for (const p of ledger.parties) {
    if (p.code === '120.10603') {
      rows.push(cf(p, 1, 0))
      continue
    }
    for (const d of [1, 2]) {
      const o = p.opening[d - 1]
      rows.push(cf(p, d, o !== 0 ? o : Math.round((r() - 0.3) * 20000) || 100))
    }
  }
  const edited = rows.find((x) => x.accountCode === '120.10711' && x.depotId === 1)!
  edited.status = 'EDITED'
  edited.overrideReason = 'Disputed invoice written back'
  return rows
}

export interface Db {
  ledger: Ledger
  lines: LedgerLine[]
  cash: CashLine[]
  receipts: Receipt[]
  series: NumberSeries[]
  entries: JournalEntry[]
  trails: Record<number, TrailItem[]>
  cfRows: CarryForwardRow[]
  cfPosted: boolean
  idempotency: Map<string, { status: number; body: unknown }>
  activity: ActivityItem[]
}

export function createDb(): Db {
  const ledger = buildLedger()
  return {
    ledger,
    lines: ledgerLines(ledger.parties),
    cash: cashLines(),
    receipts: receipts(ledger),
    series: series(),
    entries: entries(ledger),
    trails: trails(),
    cfRows: carryForwardRows(ledger),
    cfPosted: false,
    idempotency: new Map(),
    activity: [
      {
        kind: 'DOCUMENT',
        id: 418,
        number: 'SF-2026-1-000418',
        subtype: 'SALES_INVOICE',
        sicilId: ledger.parties[0].sicilId,
        sicilName: ledger.parties[0].name,
        date: TODAY,
        depotId: 1,
        amount: '48720.00',
      },
    ],
  }
}
