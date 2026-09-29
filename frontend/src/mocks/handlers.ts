import { http, HttpResponse, type JsonBodyType } from 'msw'
import type {
  BalanceColId,
  BalancesRequest,
  CarryForward,
  CarryForwardOverride,
  CashBook,
  CashCountCreate,
  CashVoucher,
  CashVoucherCreate,
  Dashboard,
  GridRequest,
  JournalEntry,
  Receipt,
  ReceiptCreate,
  SearchResult,
  Sicil,
  Statement,
  StatementLine,
} from '../api/types'
import { applyGrid } from './grid'
import { belowMinLevels, catalogSearch, resetStock, stockHandlers } from './stockHandlers'
import {
  documentHandlers,
  documentRef,
  documentSeries,
  memberName,
  resetDocuments,
  type Card,
} from './documentHandlers'
import {
  balanceRow,
  createDb,
  demoUser,
  depots,
  ORG_ID,
  organizations,
  TODAY,
  type CarryForwardRun,
  type CashLine,
  type Db,
} from './data'

const m = (n: number) => n.toFixed(2)
const O = '/api/orgs/:orgId'
const err = (status: number, code: string, extra: Record<string, unknown> = {}) =>
  HttpResponse.json({ code, ...extra }, { status })
const etag = (version: number) => ({ ETag: `"${version}"` })

const db: { current: Db } = { current: createDb() }

const cards = (): Card[] =>
  db.current.ledger.parties.map((p) => ({
    sicilId: p.sicilId,
    accountId: p.accountId,
    code: p.code,
    name: p.name,
    tipi: p.tipi,
    balance: Number(balanceRow(p, null).balance),
    limit: p.tipi === 'MUSTERI' ? 250000 : null,
    salesRepUserId:
      db.current.ledger.sicils.find((s) => s.id === p.sicilId)?.sales_rep_user_id ?? null,
  }))

resetDocuments(cards)

export function resetDb() {
  db.current = createDb()
  resetStock()
  resetDocuments(cards)
}

function nextNumber(prefix: string, depotId: number, year = 2026): string {
  const s = db.current.series.find(
    (x) => x.prefix === prefix && x.depotId === depotId && x.year === year,
  )!
  const value = s.nextValue
  s.nextValue += 1
  s.version += 1
  return `${prefix}-${year}-${depotId}-${String(value).padStart(s.width, '0')}`
}

function nextEntryId(): number {
  return Math.max(...db.current.entries.map((e) => e.id)) + 1
}

// Idempotency-Key replay: same key returns the stored response.
async function idempotent(
  request: Request,
  run: () => { status: number; body: JsonBodyType },
): Promise<Response> {
  const key = request.headers.get('Idempotency-Key')
  if (!key) return err(400, 'ERR_IDEMPOTENCY_KEY_REQUIRED')
  const hit = db.current.idempotency.get(key)
  if (hit) {
    return HttpResponse.json(hit.body as JsonBodyType, {
      status: hit.status,
      headers: { 'Idempotent-Replayed': 'true' },
    })
  }
  const res = run()
  if (res.status < 400) db.current.idempotency.set(key, res)
  return HttpResponse.json(res.body, { status: res.status })
}

const party = (sicilId: number) => db.current.ledger.parties.find((p) => p.sicilId === sicilId)

function balancesGrid(req: BalancesRequest) {
  const depotId = req.depotId ?? null
  const all = db.current.ledger.parties
    .filter((p) => !req.sicilTipi || p.tipi === req.sicilTipi)
    .map((p) => balanceRow(p, depotId))
  type R = (typeof all)[number]
  const cols: Record<BalanceColId, (r: R) => string | number | null | undefined> = {
    accountCode: (r) => r.accountCode,
    accountName: (r) => r.accountName,
    city: (r) => r.city,
    sicilTipi: (r) => r.sicilTipi,
    debitTotal: (r) => Number(r.debitTotal),
    creditTotal: (r) => Number(r.creditTotal),
    balance: (r) => Number(r.balance),
    overdue: (r) => Number(r.overdue),
    lastMoveDate: (r) => r.lastMoveDate,
  }
  const used = [...Object.keys(req.filterModel ?? {}), ...(req.sortModel ?? []).map((x) => x.colId)]
  if (used.some((c) => !c || !(c in cols))) return null
  const { rows, filtered } = applyGrid(all, req, cols, ['accountCode', 'accountName', 'city'])
  const sum = (k: 'debitTotal' | 'creditTotal' | 'balance' | 'overdue') =>
    m(filtered.reduce((s, r) => s + Number(r[k] ?? 0), 0))
  return {
    rows,
    totalCount: filtered.length,
    unfilteredCount: all.length,
    totals: {
      debitTotal: sum('debitTotal'),
      creditTotal: sum('creditTotal'),
      balance: sum('balance'),
      overdue: sum('overdue'),
    },
  }
}

function statement(accountId: number, from: string, to: string, depotId: number | null): Statement {
  const all = db.current.lines.filter(
    (l) => l.accountId === accountId && (!depotId || l.depotId === depotId),
  )
  const depotIds = depotId ? [depotId] : depots.map((d) => d.id!)
  const folded = (l: StatementLine) =>
    l.entryDate < from || (l.sourceType === 'opening' && l.entryDate <= from)
  const perDepot = new Map<number, number>()
  const running = () => [...perDepot.values()].reduce((s, v) => s + v, 0)
  const broughtForward: StatementLine[] = []
  for (const d of depotIds) {
    const bf = all
      .filter((l) => l.depotId === d && folded(l))
      .reduce((s, l) => s + Number(l.debit) - Number(l.credit), 0)
    if (bf === 0) continue
    perDepot.set(d, bf)
    broughtForward.push({
      entryId: null,
      entryDate: from,
      depotId: d,
      documentNo: null,
      sourceType: 'opening',
      description: 'Brought forward',
      debit: m(Math.max(bf, 0)),
      credit: m(Math.max(-bf, 0)),
      runningBalance: m(running()),
      resetsBalance: false,
    })
  }
  const lines = all
    .filter((l) => !folded(l) && l.entryDate <= to)
    .sort((a, b) => a.entryDate.localeCompare(b.entryDate))
    .map((l, i) => {
      const amount = Number(l.debit) - Number(l.credit)
      const resets = l.sourceType === 'opening'
      const d = l.depotId ?? 0
      perDepot.set(d, resets ? amount : (perDepot.get(d) ?? 0) + amount)
      const { accountId, ...rest } = l
      const ref = l.sourceType === 'document' ? documentRef(l.documentNo ?? null) : null
      return {
        ...rest,
        documentId: ref?.id ?? null,
        documentType: ref?.type ?? null,
        entryId: l.entryId ?? accountId * 100 + i,
        runningBalance: m(running()),
        resetsBalance: resets,
      }
    })
  const depotTotals = depotIds.map((d) => {
    const rows = lines.filter((l) => l.depotId === d && !l.resetsBalance)
    const debit = rows.reduce((s, l) => s + Number(l.debit), 0)
    const credit = rows.reduce((s, l) => s + Number(l.credit), 0)
    return { depotId: d, debit: m(debit), credit: m(credit), balance: m(perDepot.get(d) ?? 0) }
  })
  return { accountId, from, to, broughtForward, lines, depotTotals, closingBalance: m(running()) }
}

function cashBook(depotId: number, from: string, to: string): CashBook {
  const all = db.current.cash
    .filter((l) => l.depotId === depotId)
    .sort((a, b) => a.date.localeCompare(b.date))
  // An OPENING line of a carried year restarts the running balance.
  const step = (run: number, l: CashLine) => {
    const amount = Number(l.income) - Number(l.expense)
    return l.resetsBalance ? amount : run + amount
  }
  const openingBalance = all.filter((l) => l.date < from).reduce(step, 0)
  let running = openingBalance
  const lines = all
    .filter((l) => l.date >= from && l.date <= to)
    .map((l) => {
      running = step(running, l)
      return { ...l, depotId: undefined, runningBalance: m(running) }
    })
  const counted = lines.filter((l) => !l.resetsBalance)
  const totalIn = counted.reduce((s, l) => s + Number(l.income), 0)
  const totalOut = counted.reduce((s, l) => s + Number(l.expense), 0)
  return {
    depotId,
    from,
    to,
    openingBalance: m(openingBalance),
    totalIn: m(totalIn),
    totalOut: m(totalOut),
    lines,
    closingBalance: m(running),
  }
}

// Posting the carry-forward into Y closes Y − 1 and every earlier year.
function yearClosed(date: string): boolean {
  const year = Number(date.slice(0, 4))
  return Object.entries(db.current.cf).some(([y, r]) => r.status === 'POSTED' && year < Number(y))
}

function run(year: number): CarryForwardRun {
  db.current.cf[year] ??= {
    status: 'PREVIEW',
    version: 1,
    rows: [],
    postedAt: null,
    reopenedAt: null,
    reopenReason: null,
  }
  return db.current.cf[year]
}

function carryForward(year: number, depotId: number | null): CarryForward {
  const r = run(year)
  const rows = r.rows.filter((x) => !depotId || x.depotId === depotId)
  const live = rows.filter((x) => x.status !== 'SKIP')
  const posted = r.status === 'POSTED'
  return {
    year,
    status: r.status,
    version: r.version,
    postedAt: r.postedAt,
    reopenedAt: r.reopenedAt,
    reopenReason: r.reopenReason,
    checks: [
      { code: 'NO_DRAFT_DOCUMENTS', ok: true, blocking: true },
      { code: 'STOCK_COUNTS_POSTED', ok: true, blocking: true },
      { code: 'LEDGER_BALANCED', ok: true, blocking: true },
      { code: 'YEAR_NOT_OPENED', ok: !posted, blocking: true },
    ],
    steps: [
      { code: 'CLOSE_DOCUMENTS', status: 'DONE' },
      { code: 'STOCK_COUNTS', status: 'DONE' },
      { code: 'ACCOUNT_BALANCES', status: posted ? 'DONE' : 'CURRENT' },
      { code: 'OPEN_YEAR', status: posted ? 'DONE' : 'TODO' },
    ],
    accountCount: new Set(live.map((x) => x.accountId)).size,
    entryCount: live.length,
    totalDebit: m(live.reduce((s, x) => s + Number(x.openingDebit), 0)),
    totalCredit: m(live.reduce((s, x) => s + Number(x.openingCredit), 0)),
    rows,
  }
}

// If-Match against the preview version: 428 without, 412 when stale.
function precondition(request: Request, version: number): Response | null {
  const want = request.headers.get('If-Match')
  if (!want) return err(428, 'ERR_PRECONDITION_REQUIRED')
  if (want.replace(/^W\//, '').replace(/"/g, '') !== String(version)) {
    return err(412, 'ERR_VERSION_CONFLICT')
  }
  return null
}

const cfJson = (year: number, depotId: number | null, status = 200) => {
  const body = carryForward(year, depotId)
  return HttpResponse.json(body, { status, headers: etag(body.version) })
}

function addCash(line: CashLine) {
  db.current.cash.push(line)
}

function createReceipt(body: ReceiptCreate): { status: number; body: JsonBodyType } {
  const p = party(body.sicilId)
  if (!p) return { status: 422, body: { code: 'ERR_SICIL_NOT_FOUND', field: 'sicilId' } }
  if (!(Number(body.amount) > 0)) return { status: 422, body: { code: 'ERR_INVALID_AMOUNT' } }
  if (body.method === 'CHEQUE' || body.method === 'NOTE') {
    return { status: 400, body: { code: 'ERR_METHOD_NOT_AVAILABLE', field: 'method' } }
  }
  if (body.method === 'BANK_TRANSFER' && !body.bankAccountId) {
    return { status: 422, body: { code: 'ERR_BANK_ACCOUNT_REQUIRED' } }
  }
  if (yearClosed(body.date)) return { status: 409, body: { code: 'ERR_YEAR_CLOSED' } }
  const collection = body.kind === 'COLLECTION'
  const receiptNo = nextNumber(collection ? 'TAH' : 'TED', body.depotId)
  const amount = Number(body.amount)
  const d = body.depotId - 1
  if (collection) p.credit[d] += amount
  else p.debit[d] += amount
  const entryId = nextEntryId()
  db.current.lines.push({
    accountId: p.accountId,
    entryId,
    entryDate: body.date,
    depotId: body.depotId,
    documentNo: receiptNo,
    sourceType: 'receipt',
    subtype: body.kind,
    dueDate: body.date,
    description: body.description || (collection ? 'Collection' : 'Payment'),
    debit: m(collection ? 0 : amount),
    credit: m(collection ? amount : 0),
    runningBalance: '0.00',
    resetsBalance: false,
  })
  if (body.method === 'CASH') {
    addCash({
      depotId: body.depotId,
      date: body.date,
      kind: body.kind,
      documentNo: receiptNo,
      sicilId: p.sicilId,
      sicilName: p.name,
      description: body.description || (collection ? 'Collection' : 'Payment'),
      journalEntryId: entryId,
      income: m(collection ? amount : 0),
      expense: m(collection ? 0 : amount),
      runningBalance: '0.00',
      resetsBalance: false,
    })
  }
  const receipt: Receipt = {
    ...body,
    id: Math.max(...db.current.receipts.map((r) => r.id)) + 1,
    orgId: ORG_ID,
    receiptNo,
    origin: 'MANUAL',
    instrumentId: null,
    instrumentNo: null,
    sicilName: p.name,
    journalEntryId: entryId,
    voidedByEntryId: null,
  }
  db.current.receipts.unshift(receipt)
  return { status: 201, body: receipt }
}

function createVoucher(body: CashVoucherCreate): { status: number; body: JsonBodyType } {
  if (!(Number(body.amount) > 0)) return { status: 422, body: { code: 'ERR_INVALID_AMOUNT' } }
  const account = db.current.ledger.accounts.find((a) => a.id === body.accountId)
  if (!account) return { status: 422, body: { code: 'ERR_ACCOUNT_NOT_FOUND', field: 'accountId' } }
  if (yearClosed(body.date)) return { status: 409, body: { code: 'ERR_YEAR_CLOSED' } }
  const voucherNo = nextNumber(body.kind === 'EXPENSE' ? 'GDR' : 'VRM', body.depotId)
  const entryId = nextEntryId()
  const out = body.kind !== 'BANK_WITHDRAWAL'
  addCash({
    depotId: body.depotId,
    date: body.date,
    kind: body.kind,
    documentNo: voucherNo,
    sicilId: null,
    sicilName: null,
    description: body.description,
    journalEntryId: entryId,
    income: m(out ? 0 : Number(body.amount)),
    expense: m(out ? Number(body.amount) : 0),
    runningBalance: '0.00',
    resetsBalance: false,
  })
  const voucher: CashVoucher = { ...body, id: entryId, voucherNo, journalEntryId: entryId }
  return { status: 201, body: voucher }
}

function createCount(body: CashCountCreate): { status: number; body: JsonBodyType } {
  if (yearClosed(TODAY)) return { status: 409, body: { code: 'ERR_YEAR_CLOSED' } }
  const book = Number(cashBook(body.depotId, '2000-01-01', '2999-12-31').closingBalance)
  const difference = Number(body.countedAmount) - book
  let journalEntryId: number | null = null
  if (body.postDifference && difference !== 0) {
    journalEntryId = nextEntryId()
    addCash({
      depotId: body.depotId,
      date: TODAY,
      kind: 'CASH_COUNT',
      documentNo: null,
      sicilId: null,
      sicilName: null,
      description: body.note || 'Till count difference',
      journalEntryId,
      income: m(Math.max(difference, 0)),
      expense: m(Math.max(-difference, 0)),
      runningBalance: '0.00',
      resetsBalance: false,
    })
  }
  return {
    status: 201,
    body: {
      ...body,
      id: Date.now() % 100000,
      countedAt: `${TODAY}T18:00:00`,
      bookBalance: m(book),
      difference: m(difference),
      journalEntryId,
      countedBy: demoUser.id,
    },
  }
}

function reverse(entry: JournalEntry): { status: number; body: JsonBodyType } {
  if (entry.sourceType !== 'manual') {
    return {
      status: 409,
      body: {
        code: 'ERR_ENTRY_OWNED_BY_SOURCE',
        owner: {
          sourceType: entry.sourceType,
          sourceId: entry.sourceId ?? String(entry.id),
          sourceNo: entry.sourceNo ?? null,
        },
      },
    }
  }
  if (entry.reversedByEntryId) return { status: 409, body: { code: 'ERR_ENTRY_ALREADY_REVERSED' } }
  if (yearClosed(TODAY)) return { status: 409, body: { code: 'ERR_YEAR_CLOSED' } }
  const id = nextEntryId()
  const entryNo = nextNumber('J', entry.depotId)
  const reversal: JournalEntry = {
    ...entry,
    id,
    entryNo,
    entryDate: TODAY,
    description: `Reversal of ${entry.entryNo}`,
    sourceType: 'reversal',
    sourceId: String(entry.id),
    sourceNo: null,
    reversesEntryId: entry.id,
    reversesEntryNo: entry.entryNo,
    reversedByEntryId: null,
    reversedByEntryNo: null,
    idempotencyKey: null,
    createdAt: `${TODAY}T10:00:00`,
    postings: entry.postings.map((p) => ({
      ...p,
      id: p.id + 100000,
      side: p.side === 'DEBIT' ? 'CREDIT' : 'DEBIT',
    })),
  }
  entry.reversedByEntryId = id
  entry.reversedByEntryNo = entryNo
  db.current.entries.unshift(reversal)
  return { status: 201, body: reversal }
}

const fold = (s: string) => s.toLocaleLowerCase('tr-TR')

function search(q: string): SearchResult {
  const f = fold(q)
  const accounts = db.current.ledger.parties
    .filter((p) => fold(p.name).includes(f) || p.code.includes(f))
    .slice(0, 5)
    .map((p) => ({
      sicilId: p.sicilId,
      accountId: p.accountId,
      hesapNo: p.code,
      hesapAdi: p.name,
    }))
  const documents = [
    ...db.current.activity,
    ...db.current.receipts.map((r) => ({
      kind: 'RECEIPT' as const,
      id: r.id,
      number: r.receiptNo,
      subtype: r.kind,
      sicilId: r.sicilId,
      sicilName: r.sicilName ?? null,
      date: r.date,
      depotId: r.depotId,
      amount: r.amount,
    })),
  ]
    .filter((d) => fold(d.number).includes(f))
    .slice(0, 5)
  const parts = f.length >= 2 ? catalogSearch(q).hits.slice(0, 5) : []
  return { accounts, parts, documents }
}

function dashboard(depotId: number | null): Dashboard {
  const rows = db.current.ledger.parties.map((p) => balanceRow(p, depotId))
  const sum = (tipi: string, sign: 1 | -1) => {
    const hit = rows.filter((r) => r.sicilTipi === tipi && sign * Number(r.balance) > 0)
    return { total: m(hit.reduce((s, r) => s + sign * Number(r.balance), 0)), accounts: hit.length }
  }
  const cashDepots = depots
    .filter((d) => !depotId || d.id === depotId)
    .map((d) => ({
      depotId: d.id!,
      balance: cashBook(d.id!, '2000-01-01', TODAY).closingBalance,
    }))
  const below = belowMinLevels(depotId)
  return {
    date: TODAY,
    receivables: sum('MUSTERI', 1),
    payables: sum('FIRMA', -1),
    cash: {
      total: m(cashDepots.reduce((s, c) => s + Number(c.balance), 0)),
      depots: cashDepots,
    },
    instrumentsDue30: { total: '386000.00', overdueCount: 3 },
    dueThisWeek: [
      {
        instrumentId: 71,
        instrumentNo: 'AC-000071',
        sicilName: 'Kaya Ziraat',
        dueDate: '2026-09-25',
        daysToDue: -4,
        remaining: '24000.00',
      },
      {
        instrumentId: 84,
        instrumentNo: 'AC-000084',
        sicilName: 'Demir Traktör',
        dueDate: '2026-10-01',
        daysToDue: 2,
        remaining: '55000.00',
      },
      {
        instrumentId: 12,
        instrumentNo: 'AS-000012',
        sicilName: 'Öztürk Tarım',
        dueDate: '2026-10-04',
        daysToDue: 5,
        remaining: '18500.00',
      },
    ],
    belowMin: below.slice(0, 10),
    belowMinCount: below.filter((l) => l.belowMin).length,
    negativeCount: below.filter((l) => l.negative).length,
    ledgerCheck: { status: 'BALANCED', checkedAt: `${TODAY}T02:00:00Z` },
  }
}

const num = (v: string | null) => (v ? Number(v) : null)
const ROLE_PREFIX: Record<string, string> = {
  CASH: '100',
  BANK: '102',
  CUSTOMER: '120',
  SUPPLIER: '320',
  EXPENSE: '770',
}

export const handlers = [
  ...stockHandlers,
  ...documentHandlers,
  http.get('/healthz', () => HttpResponse.json({ status: 'ok', version: '3.0.0+4f2c91a' })),
  http.post('/api/auth/login', async ({ request }) => {
    const body = (await request.json()) as { email: string; password: string }
    if (body.password !== 'demo') return err(401, 'ERR_INVALID_CREDENTIALS')
    return HttpResponse.json({ token: 'msw-token', user: demoUser })
  }),
  http.post('/api/auth/demo', () =>
    HttpResponse.json({ token: 'msw-token', user: demoUser, resetAt: `${TODAY}T23:00:00Z` }),
  ),
  http.get('/api/users/me', ({ request }) =>
    HttpResponse.json(
      request.headers.get('Authorization') === 'Bearer msw-admin'
        ? { ...demoUser, role: 'admin' }
        : demoUser,
    ),
  ),
  http.get('/api/admin/organizations', () => HttpResponse.json(organizations)),
  http.get(`${O}/depots`, () => HttpResponse.json(depots)),
  http.get(`${O}/dashboard`, ({ request }) =>
    HttpResponse.json(dashboard(num(new URL(request.url).searchParams.get('depotId')))),
  ),
  http.get(`${O}/search`, ({ request }) =>
    HttpResponse.json(search(new URL(request.url).searchParams.get('q') ?? '')),
  ),

  http.post(`${O}/ledger/balances/grid`, async ({ request }) => {
    const body = (await request.json()) as BalancesRequest
    if (request.headers.get('Accept')?.includes('spreadsheetml')) {
      return new HttpResponse('xlsx', {
        headers: { 'Content-Type': request.headers.get('Accept')! },
      })
    }
    const res = balancesGrid(body)
    return res ? HttpResponse.json(res) : err(400, 'ERR_INVALID_REQUEST', { field: 'colId' })
  }),
  http.post(
    `${O}/ledger/statements/print`,
    () => new HttpResponse('%PDF-1.7', { headers: { 'Content-Type': 'application/pdf' } }),
  ),
  http.post(
    `${O}/ledger/reminder-letters`,
    () => new HttpResponse('%PDF-1.7', { headers: { 'Content-Type': 'application/pdf' } }),
  ),
  http.get(`${O}/ledger/accounts`, ({ request }) => {
    const q = new URL(request.url).searchParams
    const prefix = q.get('codePrefix') ?? ROLE_PREFIX[q.get('role') ?? ''] ?? ''
    const items = db.current.ledger.accounts.filter(
      (a) =>
        (!q.get('type') || a.type === q.get('type')) &&
        (!q.get('sicilId') || a.sicilId === Number(q.get('sicilId'))) &&
        a.code.startsWith(prefix),
    )
    const start = Number(q.get('cursor') ?? 0)
    const end = start + Number(q.get('limit') ?? 50)
    return HttpResponse.json({
      items: items.slice(start, end),
      nextCursor: end < items.length ? String(end) : null,
    })
  }),
  http.get(`${O}/ledger/accounts/:accountId`, ({ params }) => {
    const a = db.current.ledger.accounts.find((x) => x.id === Number(params.accountId))
    if (!a) return err(404, 'ERR_ACCOUNT_NOT_FOUND')
    return HttpResponse.json(a, { headers: { ETag: `"${a.version}"` } })
  }),
  http.get(`${O}/ledger/accounts/:accountId/statement`, ({ params, request }) => {
    const q = new URL(request.url).searchParams
    const accept = request.headers.get('Accept') ?? ''
    if (!accept.includes('json'))
      return new HttpResponse('binary', { headers: { 'Content-Type': accept } })
    return HttpResponse.json(
      statement(Number(params.accountId), q.get('from')!, q.get('to')!, num(q.get('depotId'))),
    )
  }),
  http.get(`${O}/sicils/:sicilId`, ({ params }) => {
    const s = db.current.ledger.sicils.find((x) => x.id === Number(params.sicilId))
    return s ? HttpResponse.json(s, { headers: etag(s.version) }) : err(404, 'ERR_SICIL_NOT_FOUND')
  }),
  http.put(`${O}/sicils/:sicilId`, async ({ params, request }) => {
    const s = db.current.ledger.sicils.find((x) => x.id === Number(params.sicilId))
    if (!s) return err(404, 'ERR_SICIL_NOT_FOUND')
    const stale = precondition(request, s.version)
    if (stale) return stale
    const patch = (await request.json()) as Partial<Sicil>
    if (patch.sales_rep_user_id != null && !memberName(patch.sales_rep_user_id)) {
      return err(422, 'ERR_USER_NOT_FOUND', { field: 'sales_rep_user_id' })
    }
    Object.assign(s, patch, {
      ...(patch.sales_rep_user_id === undefined
        ? {}
        : { sales_rep_name: memberName(patch.sales_rep_user_id) }),
      id: s.id,
      organization_id: s.organization_id,
      version: s.version + 1,
    })
    return HttpResponse.json(s, { headers: etag(s.version) })
  }),
  http.get(`${O}/sicils/:sicilId/summary`, ({ params, request }) => {
    const p = party(Number(params.sicilId))
    if (!p) return err(404, 'ERR_SICIL_NOT_FOUND')
    const depotId = num(new URL(request.url).searchParams.get('depotId'))
    const row = balanceRow(p, depotId)
    const limit = p.tipi === 'MUSTERI' ? 250000 : null
    const rep = db.current.ledger.sicils.find((s) => s.id === p.sicilId)?.sales_rep_user_id ?? null
    return HttpResponse.json({
      sicilId: p.sicilId,
      accountId: p.accountId,
      accountCode: p.code,
      balance: row.balance,
      overdue: row.overdue ?? '0.00',
      creditLimit: limit ? m(limit) : null,
      creditUsedPct: limit ? Math.round((Number(row.balance) / limit) * 100) : null,
      paymentTermDays: 30,
      discount1Pct: '10',
      discount2Pct: '5',
      salesRepUserId: rep,
      salesRepName: memberName(rep),
      chequesInPortfolio: p.code === '120.10482' ? '55000.00' : '0.00',
      lastMoveDate: p.lastMove,
    })
  }),
  http.get(`${O}/number-series`, ({ request }) => {
    const q = new URL(request.url).searchParams
    return HttpResponse.json(
      [...db.current.series, ...documentSeries()].filter(
        (s) =>
          (!q.get('prefix') || s.prefix === q.get('prefix')) &&
          (!q.get('depotId') || s.depotId === Number(q.get('depotId'))) &&
          (!q.get('year') || s.year === Number(q.get('year'))),
      ),
    )
  }),

  http.post(`${O}/receipts/grid`, async ({ request }) => {
    const body = (await request.json()) as GridRequest & { depotId?: number | null }
    type R = Receipt
    const scoped = db.current.receipts.filter((r) => !body.depotId || r.depotId === body.depotId)
    const { rows, filtered } = applyGrid(
      scoped,
      body,
      {
        receiptNo: (r: R) => r.receiptNo,
        date: (r: R) => r.date,
        kind: (r: R) => r.kind,
        method: (r: R) => r.method,
        sicilName: (r: R) => r.sicilName,
        instrumentNo: (r: R) => r.instrumentNo,
        amount: (r: R) => Number(r.amount),
      },
      ['receiptNo', 'sicilName'],
    )
    return HttpResponse.json({
      rows,
      totalCount: filtered.length,
      unfilteredCount: scoped.length,
    })
  }),
  http.post(`${O}/receipts`, async ({ request }) => {
    const body = (await request.json()) as ReceiptCreate
    return idempotent(request, () => createReceipt(body))
  }),
  http.get(`${O}/receipts/:receiptId`, ({ params }) => {
    const r = db.current.receipts.find((x) => x.id === Number(params.receiptId))
    if (!r) return err(404, 'ERR_RECEIPT_NOT_FOUND')
    return new HttpResponse('%PDF-1.7', { headers: { 'Content-Type': 'application/pdf' } })
  }),

  http.get(`${O}/cash-book`, ({ request }) => {
    const q = new URL(request.url).searchParams
    const accept = request.headers.get('Accept') ?? ''
    if (!accept.includes('json'))
      return new HttpResponse('binary', { headers: { 'Content-Type': accept } })
    if (!q.get('depotId')) return err(400, 'ERR_DEPOT_REQUIRED', { field: 'depotId' })
    return HttpResponse.json(cashBook(Number(q.get('depotId')), q.get('from')!, q.get('to')!))
  }),
  http.post(`${O}/cash-vouchers`, async ({ request }) => {
    const body = (await request.json()) as CashVoucherCreate
    return idempotent(request, () => createVoucher(body))
  }),
  http.post(`${O}/cash-counts`, async ({ request }) => {
    const body = (await request.json()) as CashCountCreate
    return idempotent(request, () => createCount(body))
  }),

  http.get(`${O}/ledger/carry-forwards/:year`, ({ params, request }) => {
    const accept = request.headers.get('Accept') ?? ''
    if (accept.includes('pdf')) {
      return new HttpResponse('%PDF-1.7', { headers: { 'Content-Type': 'application/pdf' } })
    }
    return cfJson(Number(params.year), num(new URL(request.url).searchParams.get('depotId')))
  }),
  http.put(`${O}/ledger/carry-forwards/:year/overrides`, async ({ params, request }) => {
    const year = Number(params.year)
    const r = run(year)
    const body = (await request.json()) as CarryForwardOverride
    if (r.status === 'POSTED') return err(409, 'ERR_ALREADY_CARRIED_FORWARD')
    const stale = precondition(request, r.version)
    if (stale) return stale
    if (!body.reason || body.reason.trim().length < 3) {
      return err(400, 'ERR_INVALID_REQUEST', { field: 'reason' })
    }
    const row = r.rows.find((x) => x.accountId === body.accountId && x.depotId === body.depotId)
    if (!row) return err(422, 'ERR_ACCOUNT_NOT_FOUND', { field: 'accountId' })
    row.openingDebit = body.debit
    row.openingCredit = body.credit
    row.overrideReason = body.reason
    row.status = Number(body.debit) === 0 && Number(body.credit) === 0 ? 'SKIP' : 'EDITED'
    r.version += 1
    return cfJson(year, num(new URL(request.url).searchParams.get('previewDepotId')))
  }),
  http.delete(`${O}/ledger/carry-forwards/:year/overrides`, ({ params, request }) => {
    const year = Number(params.year)
    const r = run(year)
    if (r.status === 'POSTED') return err(409, 'ERR_ALREADY_CARRIED_FORWARD')
    const stale = precondition(request, r.version)
    if (stale) return stale
    const q = new URL(request.url).searchParams
    const row = r.rows.find(
      (x) => x.accountId === Number(q.get('accountId')) && x.depotId === Number(q.get('depotId')),
    )
    if (row) {
      const c = Number(row.closingBalance)
      row.openingDebit = m(Math.max(c, 0))
      row.openingCredit = m(Math.max(-c, 0))
      row.overrideReason = null
      row.status = c === 0 ? 'SKIP' : 'READY'
      r.version += 1
    }
    return cfJson(year, num(q.get('previewDepotId')))
  }),
  http.post(`${O}/ledger/carry-forwards/:year/post`, ({ params, request }) =>
    idempotent(request, () => {
      const year = Number(params.year)
      const r = run(year)
      if (r.status === 'POSTED')
        return { status: 409, body: { code: 'ERR_ALREADY_CARRIED_FORWARD' } }
      r.status = 'POSTED'
      r.postedAt = `${TODAY}T10:00:00Z`
      r.version += 1
      let id = 6000
      for (const x of r.rows) {
        if (x.status === 'SKIP') continue
        x.status = 'POSTED'
        x.journalEntryId = id++
      }
      return { status: 201, body: carryForward(year, null) as unknown as JsonBodyType }
    }),
  ),
  http.post(`${O}/ledger/carry-forwards/:year/reopen`, async ({ params, request }) => {
    const year = Number(params.year)
    const r = run(year)
    const stale = precondition(request, r.version)
    if (stale) return stale
    const body = (await request.json()) as { reason?: string }
    if (!body.reason || body.reason.trim().length < 3) {
      return err(400, 'ERR_INVALID_REQUEST', { field: 'reason' })
    }
    return idempotent(request, () => {
      if (r.status !== 'POSTED') return { status: 409, body: { code: 'ERR_NOT_CARRIED_FORWARD' } }
      if (db.current.cf[year + 1]?.status === 'POSTED') {
        return { status: 409, body: { code: 'ERR_YEAR_CLOSED' } }
      }
      r.status = 'REOPENED'
      r.reopenedAt = `${TODAY}T10:00:00Z`
      r.reopenReason = body.reason!.trim()
      r.version += 1
      for (const x of r.rows) {
        if (x.status !== 'POSTED') continue
        x.status = x.overrideReason ? 'EDITED' : 'READY'
        x.journalEntryId = null
      }
      return { status: 200, body: carryForward(year, null) as unknown as JsonBodyType }
    })
  }),

  http.get(`${O}/ledger/entries`, ({ request }) => {
    const q = new URL(request.url).searchParams
    const text = fold(q.get('q') ?? '')
    const depotId = num(q.get('depotId'))
    const items = db.current.entries
      .filter((e) => !depotId || e.depotId === depotId)
      .filter((e) => !text || fold(`${e.entryNo} ${e.sourceNo ?? ''}`).includes(text))
      .sort((a, b) => b.id - a.id)
    return HttpResponse.json({ items, nextCursor: null })
  }),
  http.get(`${O}/ledger/entries/:entryId`, ({ params }) => {
    const e = db.current.entries.find((x) => x.id === Number(params.entryId))
    return e ? HttpResponse.json(e) : err(404, 'ERR_ENTRY_NOT_FOUND')
  }),
  http.get(`${O}/ledger/entries/:entryId/trail`, ({ params }) => {
    const e = db.current.entries.find((x) => x.id === Number(params.entryId))
    if (!e) return err(404, 'ERR_ENTRY_NOT_FOUND')
    return HttpResponse.json(
      db.current.trails[e.id] ?? [
        {
          at: e.createdAt,
          kind: 'AUDIT',
          label: 'ledger.entry.posted',
          ref: null,
          actorUserId: 12,
          actorLogin: 'demo',
        },
        { at: e.createdAt, kind: 'DELIVERY', label: 'ledger', ref: '1', actorUserId: null },
      ],
    )
  }),
  http.post(`${O}/ledger/entries/:entryId/reversal`, async ({ params, request }) => {
    const body = (await request.json()) as { reason?: string }
    const e = db.current.entries.find((x) => x.id === Number(params.entryId))
    if (!e) return err(404, 'ERR_ENTRY_NOT_FOUND')
    if (!body.reason || body.reason.trim().length < 3) return err(400, 'ERR_INVALID_REQUEST')
    return idempotent(request, () => reverse(e))
  }),
]
