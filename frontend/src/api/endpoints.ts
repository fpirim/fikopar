import { apiBlob, apiRequest, ifMatch, PDF, XLSX } from './client'
import type {
  Account,
  AccountPage,
  AccountPickerRole,
  AccountSelection,
  BalanceRow,
  BalancesRequest,
  CarryForward,
  CarryForwardOverride,
  CashBook,
  CashCount,
  CashCountCreate,
  CashVoucher,
  CashVoucherCreate,
  Depot,
  GridResponse,
  JournalEntry,
  JournalEntryPage,
  NumberSeries,
  Receipt,
  ReceiptCreate,
  ReceiptsRequest,
  SearchResult,
  Sicil,
  SicilSummary,
  SicilUpdate,
  Statement,
  TrailItem,
} from './types'

const org = (orgId: number) => `/api/orgs/${orgId}`
const data = <T>(p: Promise<{ data: T }>) => p.then((r) => r.data)

export const getHealth = () =>
  data(apiRequest<{ status: string; version: string }>('GET', '/healthz'))

export const listDepots = (orgId: number) =>
  data(apiRequest<Depot[]>('GET', `${org(orgId)}/depots`))

export const globalSearch = (orgId: number, q: string, signal?: AbortSignal) =>
  data(apiRequest<SearchResult>('GET', `${org(orgId)}/search`, { query: { q, limit: 5 }, signal }))

export const gridBalances = (orgId: number, body: BalancesRequest) =>
  data(
    apiRequest<GridResponse<BalanceRow>>('POST', `${org(orgId)}/ledger/balances/grid`, {
      body,
      safe: true,
    }),
  )

export const exportBalances = (orgId: number, body: BalancesRequest) =>
  apiBlob('POST', `${org(orgId)}/ledger/balances/grid`, XLSX, { body })

export const printStatements = (orgId: number, body: AccountSelection) =>
  apiBlob('POST', `${org(orgId)}/ledger/statements/print`, PDF, { body })

export const printReminderLetters = (orgId: number, body: AccountSelection) =>
  apiBlob('POST', `${org(orgId)}/ledger/reminder-letters`, PDF, { body })

export const getAccount = (orgId: number, accountId: number) =>
  data(apiRequest<Account>('GET', `${org(orgId)}/ledger/accounts/${accountId}`))

export const listAccountsByRole = (orgId: number, role: AccountPickerRole) =>
  data(
    apiRequest<AccountPage>('GET', `${org(orgId)}/ledger/accounts`, {
      query: { role, limit: 200 },
    }),
  ).then((p) => p.items.filter((a) => a.isActive))

export interface StatementQuery {
  from: string
  to: string
  depotId?: number | null
}

export const getAccountStatement = (orgId: number, accountId: number, query: StatementQuery) =>
  data(
    apiRequest<Statement>('GET', `${org(orgId)}/ledger/accounts/${accountId}/statement`, {
      query: { ...query },
    }),
  )

export const exportAccountStatement = (
  orgId: number,
  accountId: number,
  query: StatementQuery,
  accept: typeof PDF | typeof XLSX,
) =>
  apiBlob('GET', `${org(orgId)}/ledger/accounts/${accountId}/statement`, accept, {
    query: { ...query },
  })

export const getSicil = (orgId: number, sicilId: number) =>
  data(apiRequest<Sicil>('GET', `${org(orgId)}/sicils/${sicilId}`))

export const updateSicil = (orgId: number, sicil: Sicil, body: SicilUpdate) =>
  data(
    apiRequest<Sicil>('PUT', `${org(orgId)}/sicils/${sicil.id}`, {
      body,
      ifMatch: ifMatch(sicil.version),
    }),
  )

export const getSicilSummary = (orgId: number, sicilId: number, depotId?: number | null) =>
  data(
    apiRequest<SicilSummary>('GET', `${org(orgId)}/sicils/${sicilId}/summary`, {
      query: { depotId },
    }),
  )

export const listNumberSeries = (
  orgId: number,
  query: { year?: number; prefix?: string; depotId?: number | null },
) => data(apiRequest<NumberSeries[]>('GET', `${org(orgId)}/number-series`, { query }))

export const gridReceipts = (orgId: number, body: ReceiptsRequest) =>
  data(
    apiRequest<GridResponse<Receipt>>('POST', `${org(orgId)}/receipts/grid`, { body, safe: true }),
  )

export const createReceipt = (orgId: number, body: ReceiptCreate, idempotencyKey: string) =>
  data(apiRequest<Receipt>('POST', `${org(orgId)}/receipts`, { body, idempotencyKey }))

export const printReceipt = (orgId: number, receiptId: number) =>
  apiBlob('GET', `${org(orgId)}/receipts/${receiptId}`, PDF)

export interface CashBookQuery {
  depotId: number
  from: string
  to: string
}

export const getCashBook = (orgId: number, query: CashBookQuery) =>
  data(apiRequest<CashBook>('GET', `${org(orgId)}/cash-book`, { query: { ...query } }))

export const exportCashBook = (orgId: number, query: CashBookQuery) =>
  apiBlob('GET', `${org(orgId)}/cash-book`, PDF, { query: { ...query } })

export const createCashVoucher = (orgId: number, body: CashVoucherCreate, idempotencyKey: string) =>
  data(apiRequest<CashVoucher>('POST', `${org(orgId)}/cash-vouchers`, { body, idempotencyKey }))

export const createCashCount = (orgId: number, body: CashCountCreate, idempotencyKey: string) =>
  data(apiRequest<CashCount>('POST', `${org(orgId)}/cash-counts`, { body, idempotencyKey }))

const cf = (orgId: number, year: number) => `${org(orgId)}/ledger/carry-forwards/${year}`

export const getCarryForward = (orgId: number, year: number, depotId?: number | null) =>
  data(apiRequest<CarryForward>('GET', cf(orgId, year), { query: { depotId } }))

export const exportCarryForward = (orgId: number, year: number, depotId?: number | null) =>
  apiBlob('GET', cf(orgId, year), PDF, { query: { depotId } })

// The preview a write answers with: its version, and the depot the screen shows.
export interface PreviewRef {
  version: number
  depotId: number | null
}

export const setCarryForwardOverride = (
  orgId: number,
  year: number,
  body: CarryForwardOverride,
  preview: PreviewRef,
) =>
  data(
    apiRequest<CarryForward>('PUT', `${cf(orgId, year)}/overrides`, {
      body,
      query: { previewDepotId: preview.depotId },
      ifMatch: ifMatch(preview.version),
    }),
  )

export const deleteCarryForwardOverride = (
  orgId: number,
  year: number,
  row: { accountId: number; depotId: number },
  preview: PreviewRef,
) =>
  data(
    apiRequest<CarryForward>('DELETE', `${cf(orgId, year)}/overrides`, {
      query: { accountId: row.accountId, depotId: row.depotId, previewDepotId: preview.depotId },
      ifMatch: ifMatch(preview.version),
    }),
  )

export const reopenCarryForward = (
  orgId: number,
  year: number,
  reason: string,
  version: number,
  idempotencyKey: string,
) =>
  data(
    apiRequest<CarryForward>('POST', `${cf(orgId, year)}/reopen`, {
      body: { reason },
      ifMatch: ifMatch(version),
      idempotencyKey,
    }),
  )

export const postCarryForward = (orgId: number, year: number, idempotencyKey: string) =>
  data(apiRequest<CarryForward>('POST', `${cf(orgId, year)}/post`, { idempotencyKey }))

export const listJournalEntries = (
  orgId: number,
  query: { depotId?: number | null; q?: string; cursor?: string; limit?: number },
) => data(apiRequest<JournalEntryPage>('GET', `${org(orgId)}/ledger/entries`, { query }))

export const getJournalEntry = (orgId: number, entryId: number) =>
  data(apiRequest<JournalEntry>('GET', `${org(orgId)}/ledger/entries/${entryId}`))

export const getJournalEntryTrail = (orgId: number, entryId: number) =>
  data(apiRequest<TrailItem[]>('GET', `${org(orgId)}/ledger/entries/${entryId}/trail`))

export const reverseJournalEntry = (
  orgId: number,
  entryId: number,
  reason: string,
  idempotencyKey: string,
) =>
  data(
    apiRequest<JournalEntry>('POST', `${org(orgId)}/ledger/entries/${entryId}/reversal`, {
      body: { reason },
      idempotencyKey,
    }),
  )
