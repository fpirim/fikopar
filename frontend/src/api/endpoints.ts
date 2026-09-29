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
  Catalog,
  CatalogHit,
  CatalogSearchResult,
  CountLinesRequest,
  Dashboard,
  Depot,
  Document,
  DocumentsRequest,
  DocumentSummary,
  DocumentWrite,
  GridResponse,
  ImportApply,
  ImportRowsRequest,
  JournalEntry,
  JournalEntryPage,
  Member,
  MovementsRequest,
  MovementsResponse,
  NumberSeries,
  Part,
  PartDepotSettings,
  PartRow,
  PartsRequest,
  PartStock,
  Price,
  PriceImport,
  PriceImportMappingWrite,
  PriceImportPreset,
  PriceImportRow,
  PriceWrite,
  Receipt,
  ReceiptCreate,
  ReceiptsRequest,
  SearchResult,
  Sicil,
  SicilCreate,
  SicilSummary,
  SicilUpdate,
  Statement,
  StockCount,
  StockCountFinalized,
  StockCountLine,
  StockCountOpen,
  StockLevel,
  StockTransfer,
  StockTransferCreate,
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

export const createSicil = (orgId: number, body: SicilCreate) =>
  data(apiRequest<Sicil>('POST', `${org(orgId)}/sicils`, { body }))

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

export const getDashboard = (orgId: number, depotId?: number | null) =>
  data(apiRequest<Dashboard>('GET', `${org(orgId)}/dashboard`, { query: { depotId } }))

const cat = (orgId: number) => `${org(orgId)}/catalog`
const stock = (orgId: number) => `${org(orgId)}/stock`

export const listCatalogs = (orgId: number) =>
  data(apiRequest<Catalog[]>('GET', `${cat(orgId)}/catalogs`))

export const gridParts = (orgId: number, body: PartsRequest) =>
  data(apiRequest<GridResponse<PartRow>>('POST', `${cat(orgId)}/parts/grid`, { body, safe: true }))

export const exportParts = (orgId: number, body: PartsRequest) =>
  apiBlob('POST', `${cat(orgId)}/parts/grid`, XLSX, { body })

export interface CatalogSearchQuery {
  q: string
  catalogId?: number | null
  date?: string
  inStockDepotId?: number | null
  limit?: number
  cursor?: string | null
}

export const searchCatalog = (orgId: number, query: CatalogSearchQuery, signal?: AbortSignal) =>
  data(
    apiRequest<CatalogSearchResult>('GET', `${cat(orgId)}/search`, {
      query: { ...query },
      signal,
    }),
  )

export interface ResolvedCode {
  code: string
  status: 'FOUND' | 'NOT_FOUND' | 'AMBIGUOUS'
  part?: CatalogHit
  candidates?: CatalogHit[]
}

export const resolvePartCodes = (orgId: number, codes: string[], depotId?: number | null) =>
  data(
    apiRequest<ResolvedCode[]>('POST', `${cat(orgId)}/parts/resolve`, {
      body: depotId ? { codes, depotId } : { codes },
      safe: true,
    }),
  )

export const getPart = (orgId: number, partId: number) =>
  apiRequest<Part>('GET', `${cat(orgId)}/parts/${partId}`)

export const getPartStock = (orgId: number, partId: number) =>
  data(apiRequest<PartStock>('GET', `${cat(orgId)}/parts/${partId}/stock`))

// `version` 0 creates the depot row.
export const setPartDepotSettings = (
  orgId: number,
  partId: number,
  depotId: number,
  body: PartDepotSettings,
  version: number,
) =>
  data(
    apiRequest<StockLevel>('PUT', `${cat(orgId)}/parts/${partId}/depots/${depotId}`, {
      body,
      ifMatch: ifMatch(version),
    }),
  )

export const getPartPriceHistory = (orgId: number, partId: number) =>
  data(apiRequest<Price[]>('GET', `${cat(orgId)}/parts/${partId}/prices`))

export const setPartPrice = (
  orgId: number,
  partId: number,
  body: PriceWrite,
  idempotencyKey: string,
) =>
  data(apiRequest<Price>('POST', `${cat(orgId)}/parts/${partId}/prices`, { body, idempotencyKey }))

export const gridStockMovements = (orgId: number, body: MovementsRequest) =>
  data(
    apiRequest<MovementsResponse>('POST', `${stock(orgId)}/movements/grid`, { body, safe: true }),
  )

export const exportStockMovements = (orgId: number, body: MovementsRequest) =>
  apiBlob('POST', `${stock(orgId)}/movements/grid`, XLSX, { body })

export const transferStock = (orgId: number, body: StockTransferCreate, idempotencyKey: string) =>
  data(apiRequest<StockTransfer>('POST', `${stock(orgId)}/transfers`, { body, idempotencyKey }))

export const listStockCounts = (
  orgId: number,
  query: { year?: number; depotId?: number | null; status?: StockCount['status'] } = {},
) => data(apiRequest<StockCount[]>('GET', `${stock(orgId)}/counts`, { query }))

export const openStockCount = (orgId: number, body: StockCountOpen, idempotencyKey: string) =>
  data(apiRequest<StockCount>('POST', `${stock(orgId)}/counts`, { body, idempotencyKey }))

export const getStockCount = (orgId: number, countId: number) =>
  data(apiRequest<StockCount>('GET', `${stock(orgId)}/counts/${countId}`))

export const printCountSheet = (
  orgId: number,
  countId: number,
  query: {
    sheet?: 'BLIND' | 'WITH_SYSTEM'
    only?: 'ALL' | 'NOT_COUNTED'
    shelfFrom?: string
    shelfTo?: string
  },
) => apiBlob('GET', `${stock(orgId)}/counts/${countId}`, PDF, { query })

export const gridStockCountLines = (orgId: number, countId: number, body: CountLinesRequest) =>
  data(
    apiRequest<GridResponse<StockCountLine>>(
      'POST',
      `${stock(orgId)}/counts/${countId}/lines/grid`,
      { body, safe: true },
    ),
  )

export const exportStockCountLines = (orgId: number, countId: number, body: CountLinesRequest) =>
  apiBlob('POST', `${stock(orgId)}/counts/${countId}/lines/grid`, XLSX, { body })

export const addStockCountLine = (
  orgId: number,
  countId: number,
  body: { partId: number; shelf?: string; countedQty?: string },
  idempotencyKey: string,
) =>
  data(
    apiRequest<StockCountLine>('POST', `${stock(orgId)}/counts/${countId}/lines`, {
      body,
      idempotencyKey,
    }),
  )

export const getStockCountLine = (orgId: number, countId: number, lineId: number) =>
  data(apiRequest<StockCountLine>('GET', `${stock(orgId)}/counts/${countId}/lines/${lineId}`))

export const setStockCountLine = (
  orgId: number,
  countId: number,
  line: { id: number; version: number },
  body: { countedQty: string | null; shelf?: string },
) =>
  data(
    apiRequest<StockCountLine>('PATCH', `${stock(orgId)}/counts/${countId}/lines/${line.id}`, {
      body,
      ifMatch: ifMatch(line.version),
    }),
  )

export const finalizeStockCount = (
  orgId: number,
  count: { id: number; version: number },
  uncounted: 'SKIP' | 'ZERO',
  idempotencyKey: string,
) =>
  data(
    apiRequest<StockCountFinalized>('POST', `${stock(orgId)}/counts/${count.id}/finalize`, {
      body: { uncounted },
      ifMatch: ifMatch(count.version),
      idempotencyKey,
    }),
  )

export const cancelStockCount = (
  orgId: number,
  count: { id: number; version: number },
  reason: string,
) =>
  data(
    apiRequest<StockCount>('POST', `${stock(orgId)}/counts/${count.id}/cancel`, {
      body: { reason },
      ifMatch: ifMatch(count.version),
      safe: true,
    }),
  )

export const listPriceImportPresets = (orgId: number) =>
  data(
    apiRequest<{ presets: PriceImportPreset[]; aliases: Record<string, string[]> }>(
      'GET',
      `${cat(orgId)}/import-presets`,
    ),
  )

export const listPriceImports = (orgId: number, catalogId?: number | null) =>
  data(
    apiRequest<{ items: PriceImport[]; nextCursor?: string | null }>(
      'GET',
      `${cat(orgId)}/imports`,
      { query: { catalogId } },
    ),
  )

export interface PriceImportUpload {
  file: File
  catalogId: number
  validFrom: string
  mapping?: PriceImportMappingWrite
}

export const createPriceImport = (orgId: number, up: PriceImportUpload, idempotencyKey: string) => {
  const form = new FormData()
  form.append('file', up.file)
  form.append('catalogId', String(up.catalogId))
  form.append('validFrom', up.validFrom)
  if (up.mapping) {
    form.append('mapping', new Blob([JSON.stringify(up.mapping)], { type: 'application/json' }))
  }
  return data(
    apiRequest<PriceImport>('POST', `${cat(orgId)}/imports`, { body: form, idempotencyKey }),
  )
}

export const getPriceImport = (orgId: number, importId: number) =>
  data(apiRequest<PriceImport>('GET', `${cat(orgId)}/imports/${importId}`))

export const updatePriceImport = (
  orgId: number,
  job: { id: number; version: number },
  body: { mapping?: PriceImportMappingWrite; validFrom?: string },
) =>
  data(
    apiRequest<PriceImport>('PATCH', `${cat(orgId)}/imports/${job.id}`, {
      body,
      ifMatch: ifMatch(job.version),
    }),
  )

export const gridPriceImportRows = (orgId: number, importId: number, body: ImportRowsRequest) =>
  data(
    apiRequest<GridResponse<PriceImportRow>>(
      'POST',
      `${cat(orgId)}/imports/${importId}/rows/grid`,
      { body, safe: true },
    ),
  )

export const applyPriceImport = (
  orgId: number,
  job: { id: number; version: number },
  body: ImportApply,
  idempotencyKey: string,
) =>
  data(
    apiRequest<PriceImport>('POST', `${cat(orgId)}/imports/${job.id}/apply`, {
      body,
      ifMatch: ifMatch(job.version),
      idempotencyKey,
    }),
  )

export const discardPriceImport = (orgId: number, job: { id: number; version: number }) =>
  data(
    apiRequest<PriceImport>('POST', `${cat(orgId)}/imports/${job.id}/discard`, {
      ifMatch: ifMatch(job.version),
      safe: true,
    }),
  )

export const exportPriceImportRows = (orgId: number, importId: number, body: ImportRowsRequest) =>
  apiBlob('POST', `${cat(orgId)}/imports/${importId}/rows/grid`, XLSX, { body })

const docs = (orgId: number) => `${org(orgId)}/documents`

export const listMembers = (orgId: number) =>
  data(apiRequest<Member[]>('GET', `${org(orgId)}/members`))

export const gridDocuments = (orgId: number, body: DocumentsRequest) =>
  data(
    apiRequest<GridResponse<DocumentSummary>>('POST', `${docs(orgId)}/grid`, {
      body,
      safe: true,
    }),
  )

export const exportDocuments = (orgId: number, body: DocumentsRequest) =>
  apiBlob('POST', `${docs(orgId)}/grid`, XLSX, { body })

export const getDocument = (orgId: number, documentId: number) =>
  data(apiRequest<Document>('GET', `${docs(orgId)}/${documentId}`))

export const printDocument = (orgId: number, documentId: number) =>
  apiBlob('GET', `${docs(orgId)}/${documentId}`, PDF)

export const createDocument = (orgId: number, body: DocumentWrite, idempotencyKey: string) =>
  data(apiRequest<Document>('POST', docs(orgId), { body, idempotencyKey }))

export const updateDocument = (
  orgId: number,
  doc: { id: number; version: number },
  body: DocumentWrite,
) =>
  data(
    apiRequest<Document>('PUT', `${docs(orgId)}/${doc.id}`, {
      body,
      ifMatch: ifMatch(doc.version),
    }),
  )

export const deleteDocument = (orgId: number, doc: { id: number; version: number }) =>
  apiRequest<null>('DELETE', `${docs(orgId)}/${doc.id}`, { ifMatch: ifMatch(doc.version) })

export const postDocument = (
  orgId: number,
  doc: { id: number; version: number },
  idempotencyKey: string,
  creditOverride?: { reason: string },
) =>
  data(
    apiRequest<Document>('POST', `${docs(orgId)}/${doc.id}/post`, {
      body: creditOverride ? { creditOverride } : {},
      ifMatch: ifMatch(doc.version),
      idempotencyKey,
    }),
  )

export const cancelDocument = (
  orgId: number,
  doc: { id: number; version: number },
  reason: string,
  idempotencyKey: string,
) =>
  data(
    apiRequest<Document>('POST', `${docs(orgId)}/${doc.id}/cancel`, {
      body: { reason },
      ifMatch: ifMatch(doc.version),
      idempotencyKey,
    }),
  )
