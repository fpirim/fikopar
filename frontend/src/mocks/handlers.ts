import { http, HttpResponse, type JsonBodyType } from 'msw'
import type {
  BalancesRequest,
  CarryForward,
  CarryForwardOverride,
  CashBook,
  CashCountCreate,
  CashVoucher,
  CashVoucherCreate,
  GridRequest,
  JournalEntry,
  Receipt,
  ReceiptCreate,
  SearchResult,
  Statement,
  StatementLine,
} from '../api/types'
import { applyGrid } from './grid'
import {
  balanceRow,
  createDb,
  demoUser,
  depots,
  ORG_ID,
  organizations,
  TODAY,
  type CashLine,
  type Db,
} from './data'

const m = (n: number) => n.toFixed(2)
const O = '/api/orgs/:orgId'
const err = (status: number, code: string) => HttpResponse.json({ code }, { status })

const db: { current: Db } = { current: createDb() }

export function resetDb() {
  db.current = createDb()
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
  const cols = {
    accountCode: (r: (typeof all)[number]) => r.accountCode,
    name: (r: (typeof all)[number]) => r.accountName,
    city: (r: (typeof all)[number]) => r.city,
    sicilTipi: (r: (typeof all)[number]) => r.sicilTipi,
    debit: (r: (typeof all)[number]) => Number(r.debitTotal),
    credit: (r: (typeof all)[number]) => Number(r.creditTotal),
    balance: (r: (typeof all)[number]) => Number(r.balance),
    overdue: (r: (typeof all)[number]) => Number(r.overdue),
    lastMoveDate: (r: (typeof all)[number]) => r.lastMoveDate,
  }
  const { rows, filtered } = applyGrid(all, req, cols, ['accountCode', 'name', 'city'])
  const sum = (k: 'debitTotal' | 'creditTotal' | 'balance' | 'overdue') =>
    m(filtered.reduce((s, r) => s + Number(r[k] ?? 0), 0))
  return {
    rows,
    totalCount: filtered.length,
    unfilteredCount: all.length,
    totals: {
      debit: sum('debitTotal'),
      credit: sum('creditTotal'),
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
  let running = 0
  const broughtForward: StatementLine[] = []
  for (const d of depotIds) {
    const bf = all
      .filter((l) => l.depotId === d && folded(l))
      .reduce((s, l) => s + Number(l.debit) - Number(l.credit), 0)
    if (bf === 0) continue
    running += bf
    broughtForward.push({
      entryId: null,
      entryDate: from,
      depotId: d,
      documentNo: null,
      sourceType: 'opening',
      description: 'Brought forward',
      debit: m(Math.max(bf, 0)),
      credit: m(Math.max(-bf, 0)),
      runningBalance: m(running),
    })
  }
  const lines = all
    .filter((l) => !folded(l) && l.entryDate <= to)
    .sort((a, b) => a.entryDate.localeCompare(b.entryDate))
    .map((l, i) => {
      running += Number(l.debit) - Number(l.credit)
      const { accountId, ...rest } = l
      return { ...rest, entryId: l.entryId ?? accountId * 100 + i, runningBalance: m(running) }
    })
  const depotTotals = depotIds.map((d) => {
    const rows = [...broughtForward, ...lines].filter((l) => l.depotId === d)
    const debit = rows.reduce((s, l) => s + Number(l.debit), 0)
    const credit = rows.reduce((s, l) => s + Number(l.credit), 0)
    return { depotId: d, debit: m(debit), credit: m(credit), balance: m(debit - credit) }
  })
  return { accountId, from, to, broughtForward, lines, depotTotals, closingBalance: m(running) }
}

function cashBook(depotId: number, from: string, to: string): CashBook {
  const all = db.current.cash.filter((l) => l.depotId === depotId)
  const openingBalance = all
    .filter((l) => l.date < from)
    .reduce((s, l) => s + Number(l.income) - Number(l.expense), 0)
  let running = openingBalance
  const lines = all
    .filter((l) => l.date >= from && l.date <= to)
    .sort((a, b) => a.date.localeCompare(b.date))
    .map((l) => {
      running += Number(l.income) - Number(l.expense)
      return { ...l, depotId: undefined, runningBalance: m(running) }
    })
  const totalIn = lines.reduce((s, l) => s + Number(l.income), 0)
  const totalOut = lines.reduce((s, l) => s + Number(l.expense), 0)
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

function carryForward(year: number, depotId: number | null): CarryForward {
  const rows = db.current.cfRows.filter((r) => !depotId || r.depotId === depotId)
  const live = rows.filter((r) => r.status !== 'SKIP')
  const posted = db.current.cfPosted
  return {
    year,
    status: posted ? 'POSTED' : 'PREVIEW',
    checks: [
      { code: 'NO_DRAFT_DOCUMENTS', ok: true, blocking: true },
      { code: 'STOCK_COUNTS_POSTED', ok: true, blocking: true },
      { code: 'LEDGER_BALANCED', ok: true, blocking: true },
      { code: 'YEAR_NOT_OPENED', ok: !posted, blocking: true },
    ],
    accountCount: new Set(live.map((r) => r.accountId)).size,
    entryCount: live.length,
    totalDebit: m(live.reduce((s, r) => s + Number(r.openingDebit), 0)),
    totalCredit: m(live.reduce((s, r) => s + Number(r.openingCredit), 0)),
    rows,
  }
}

function addCash(line: CashLine) {
  db.current.cash.push(line)
}

function createReceipt(body: ReceiptCreate): { status: number; body: JsonBodyType } {
  const p = party(body.sicilId)
  if (!p) return { status: 404, body: { code: 'ERR_SICIL_NOT_FOUND' } }
  if (!(Number(body.amount) > 0)) return { status: 422, body: { code: 'ERR_INVALID_AMOUNT' } }
  if (body.method === 'BANK_TRANSFER' && !body.bankAccountId) {
    return { status: 422, body: { code: 'ERR_BANK_ACCOUNT_REQUIRED' } }
  }
  if (body.method === 'CHEQUE' || body.method === 'NOTE') {
    return { status: 422, body: { code: 'ERR_INSTRUMENT_REQUIRED' } }
  }
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
  if (!account) return { status: 422, body: { code: 'ERR_ACCOUNT_NOT_FOUND' } }
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
  })
  const voucher: CashVoucher = { ...body, id: entryId, voucherNo, journalEntryId: entryId }
  return { status: 201, body: voucher }
}

function createCount(body: CashCountCreate): { status: number; body: JsonBodyType } {
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
      body: { code: 'ERR_ENTRY_OWNED_BY_SOURCE', detail: entry.sourceNo ?? '' },
    }
  }
  if (entry.reversedByEntryId) return { status: 409, body: { code: 'ERR_ENTRY_ALREADY_REVERSED' } }
  const id = nextEntryId()
  const reversal: JournalEntry = {
    ...entry,
    id,
    entryNo: `J-2026-${String(id).padStart(6, '0')}`,
    entryDate: TODAY,
    description: `Reversal of ${entry.entryNo}`,
    sourceType: 'reversal',
    sourceId: String(entry.id),
    sourceNo: entry.entryNo,
    reversesEntryId: entry.id,
    reversedByEntryId: null,
    idempotencyKey: null,
    createdAt: `${TODAY}T10:00:00`,
    postings: entry.postings.map((p) => ({
      ...p,
      id: p.id + 100000,
      side: p.side === 'DEBIT' ? 'CREDIT' : 'DEBIT',
    })),
  }
  entry.reversedByEntryId = id
  db.current.entries.unshift(reversal)
  return { status: 201, body: reversal }
}

const fold = (s: string) => s.toLocaleLowerCase('tr-TR')

function search(q: string): SearchResult {
  const f = fold(q)
  const accounts = db.current.ledger.parties
    .filter((p) => fold(p.name).includes(f) || p.code.includes(f))
    .slice(0, 5)
    .map((p) => ({ sicilId: p.sicilId, hesapNo: p.code, hesapAdi: p.name }))
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
  return { accounts, parts: [], documents }
}

const num = (v: string | null) => (v ? Number(v) : null)

export const handlers = [
  http.get('/healthz', () => HttpResponse.json({ status: 'ok', version: '3.0.0+4f2c91a' })),
  http.post('/api/auth/login', async ({ request }) => {
    const body = (await request.json()) as { email: string; password: string }
    if (body.password !== 'demo') return err(401, 'ERR_INVALID_CREDENTIALS')
    return HttpResponse.json({ token: 'msw-token', user: demoUser })
  }),
  http.post('/api/auth/demo', () =>
    HttpResponse.json({ token: 'msw-token', user: demoUser, resetAt: `${TODAY}T23:00:00Z` }),
  ),
  http.get('/api/users/me', () => HttpResponse.json(demoUser)),
  http.get('/api/admin/organizations', () => HttpResponse.json(organizations)),
  http.get(`${O}/depots`, () => HttpResponse.json(depots)),
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
    return HttpResponse.json(balancesGrid(body))
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
    const items = db.current.ledger.accounts.filter(
      (a) =>
        (!q.get('type') || a.type === q.get('type')) &&
        (!q.get('sicilId') || a.sicilId === Number(q.get('sicilId'))),
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
    return s ? HttpResponse.json(s) : err(404, 'ERR_SICIL_NOT_FOUND')
  }),
  http.put(`${O}/sicils/:sicilId`, async ({ params, request }) => {
    const s = db.current.ledger.sicils.find((x) => x.id === Number(params.sicilId))
    if (!s) return err(404, 'ERR_SICIL_NOT_FOUND')
    Object.assign(s, (await request.json()) as object)
    return HttpResponse.json(s)
  }),
  http.get(`${O}/sicils/:sicilId/summary`, ({ params, request }) => {
    const p = party(Number(params.sicilId))
    if (!p) return err(404, 'ERR_SICIL_NOT_FOUND')
    const depotId = num(new URL(request.url).searchParams.get('depotId'))
    const row = balanceRow(p, depotId)
    const limit = p.tipi === 'MUSTERI' ? 250000 : null
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
      chequesInPortfolio: p.code === '120.10482' ? '55000.00' : '0.00',
      lastMoveDate: p.lastMove,
    })
  }),
  http.get(`${O}/number-series`, ({ request }) => {
    const q = new URL(request.url).searchParams
    return HttpResponse.json(
      db.current.series.filter(
        (s) =>
          (!q.get('prefix') || s.prefix === q.get('prefix')) &&
          (!q.get('depotId') || s.depotId === Number(q.get('depotId'))) &&
          (!q.get('year') || s.year === Number(q.get('year'))),
      ),
    )
  }),

  http.post(`${O}/receipts/grid`, async ({ request }) => {
    const body = (await request.json()) as GridRequest
    type R = Receipt
    const { rows, filtered } = applyGrid(
      db.current.receipts,
      body,
      {
        receiptNo: (r: R) => r.receiptNo,
        date: (r: R) => r.date,
        kind: (r: R) => r.kind,
        method: (r: R) => r.method,
        sicilName: (r: R) => r.sicilName,
        instrumentNo: (r: R) => r.instrumentNo,
        amount: (r: R) => Number(r.amount),
        depotId: (r: R) => r.depotId,
      },
      ['receiptNo', 'sicilName'],
    )
    return HttpResponse.json({
      rows,
      totalCount: filtered.length,
      unfilteredCount: db.current.receipts.length,
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
    const depotId = num(new URL(request.url).searchParams.get('depotId'))
    return HttpResponse.json(carryForward(Number(params.year), depotId))
  }),
  http.put(`${O}/ledger/carry-forwards/:year/overrides`, async ({ params, request }) => {
    const body = (await request.json()) as CarryForwardOverride
    if (db.current.cfPosted) return err(409, 'ERR_ALREADY_CARRIED_FORWARD')
    const row = db.current.cfRows.find(
      (r) => r.accountId === body.accountId && r.depotId === body.depotId,
    )
    if (!row) return err(404, 'ERR_ACCOUNT_NOT_FOUND')
    row.openingDebit = body.debit
    row.openingCredit = body.credit
    row.overrideReason = body.reason
    row.status = Number(body.debit) === 0 && Number(body.credit) === 0 ? 'SKIP' : 'EDITED'
    return HttpResponse.json(carryForward(Number(params.year), null))
  }),
  http.delete(`${O}/ledger/carry-forwards/:year/overrides`, ({ params, request }) => {
    const q = new URL(request.url).searchParams
    const row = db.current.cfRows.find(
      (r) => r.accountId === Number(q.get('accountId')) && r.depotId === Number(q.get('depotId')),
    )
    if (row) {
      const c = Number(row.closingBalance)
      row.openingDebit = m(Math.max(c, 0))
      row.openingCredit = m(Math.max(-c, 0))
      row.overrideReason = null
      row.status = c === 0 ? 'SKIP' : 'READY'
    }
    return HttpResponse.json(carryForward(Number(params.year), null))
  }),
  http.post(`${O}/ledger/carry-forwards/:year/post`, ({ params, request }) =>
    idempotent(request, () => {
      if (db.current.cfPosted) return { status: 409, body: { code: 'ERR_ALREADY_CARRIED_FORWARD' } }
      db.current.cfPosted = true
      let id = 6000
      for (const r of db.current.cfRows) {
        if (r.status === 'SKIP') continue
        r.status = 'POSTED'
        r.journalEntryId = id++
      }
      return {
        status: 201,
        body: carryForward(Number(params.year), null) as unknown as JsonBodyType,
      }
    }),
  ),

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
