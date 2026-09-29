import type { components } from './schema'

type S = components['schemas']

export type GridRequest = S['GridRequest']
export type FilterModel = NonNullable<GridRequest['filterModel']>
export type SortModel = NonNullable<GridRequest['sortModel']>
export type GridResponse<T> = Omit<S['GridResponse'], 'rows'> & { rows: T[] }

export type User = S['User']
export type Organization = S['Organization']
export type Depot = S['Depot']
export type SearchResult = S['SearchResult']
export type ActivityItem = S['ActivityItem']
export type Sicil = S['Sicil']
export type SicilTipi = S['SicilTipi']
export type SicilSummary = S['SicilSummary']
export type SicilCreate = S['SicilCreate']
export type SicilUpdate = S['SicilUpdate']
export type Account = S['Account']
export type AccountPage = S['AccountPage']
export type BalanceRow = S['BalanceRow']
export type BalanceColId = S['BalanceColId']
export type BalanceTotals = S['BalanceTotals']
export type AccountPickerRole = S['AccountPickerRole']
export type EntryOwner = S['EntryOwner']
export type AccountSelection = S['AccountSelection']
export type Statement = S['Statement']
export type StatementLine = S['StatementLine']
export type NumberSeries = S['NumberSeries']
export type Receipt = S['Receipt']
export type ReceiptCreate = S['ReceiptCreate']
export type PaymentMethod = S['PaymentMethod']
export type CashBook = S['CashBook']
export type CashBookLine = S['CashBookLine']
export type CashVoucher = S['CashVoucher']
export type CashVoucherCreate = S['CashVoucherCreate']
export type CashCount = S['CashCount']
export type CashCountCreate = S['CashCountCreate']
export type CarryForward = S['CarryForward']
export type CarryForwardRow = CarryForward['rows'][number]
export type CarryForwardOverride = S['CarryForwardOverride']
export type JournalEntry = S['JournalEntry']
export type JournalEntryPage = S['JournalEntryPage']
export type Posting = S['Posting']
export type TrailItem = S['TrailItem']

export type BalancesRequest = GridRequest & {
  asOf: string
  depotId?: number | null
  sicilTipi?: SicilTipi | null
}

export type ReceiptsRequest = GridRequest & { depotId?: number | null }

export type Dashboard = S['Dashboard']
export type StockWarning = S['StockWarning']
export type Catalog = S['Catalog']
export type PartCode = S['PartCode']
export type Part = S['Part']
export type PartColId = S['PartColId']
export type PartRow = S['PartRow']
export type CatalogHit = S['CatalogHit']
export type CatalogSearchResult = S['CatalogSearchResult']
export type Price = S['Price']
export type PriceWrite = S['PriceWrite']
export type PriceImport = S['PriceImport']
export type PriceImportMapping = S['PriceImportMapping']
export type PriceImportMappingWrite = S['PriceImportMappingWrite']
export type PriceImportColumnWrite = S['PriceImportColumnWrite']
export type PriceImportTarget = S['PriceImportTarget']
export type PriceImportPreset = S['PriceImportPreset']
export type PriceImportRow = S['PriceImportRow']
export type PriceImportFields = S['PriceImportFields']
export type PriceImportIssue = S['PriceImportIssue']
export type PartDepotSettings = S['PartDepotSettings']
export type StockLevel = S['StockLevel']
export type StockMovement = S['StockMovement']
export type MovementKind = S['MovementKind']
export type StockTransferCreate = S['StockTransferCreate']
export type StockTransfer = S['StockTransfer']
export type StockCountOpen = S['StockCountOpen']
export type StockCount = S['StockCount']
export type StockCountLine = S['StockCountLine']
export type StockCountFinalized = S['StockCountFinalized']

export type PartStock = { partId: number; depots: StockLevel[]; total: StockLevel }
export type StockOnly = 'ALL' | 'IN_STOCK' | 'BELOW_MIN' | 'NEGATIVE' | 'BELOW_MIN_OR_NEGATIVE'
export type PartsRequest = Omit<GridRequest, 'sortModel' | 'filterModel'> & {
  sortModel?: { colId?: PartColId; sort?: 'asc' | 'desc' }[]
  filterModel?: Partial<Record<PartColId, FilterModel[string]>>
  catalogId?: number | null
  depotId?: number | null
  stock?: StockOnly
  includeInactive?: boolean
  priceDate?: string
}
export type MovementsRequest = GridRequest & { partId?: number; depotId?: number | null }
export type MovementsResponse = GridResponse<StockMovement> & {
  openingOnHand?: string | null
  closingOnHand?: string | null
}
export type CountLinesOnly = 'ALL' | 'DIFFERENCES' | 'NOT_COUNTED' | 'COUNTED'
export type CountLinesRequest = GridRequest & {
  only?: CountLinesOnly
  shelfFrom?: string
  shelfTo?: string
}
export type ImportChange =
  | 'ALL'
  | 'NEW'
  | 'PRICE'
  | 'ATTRIBUTES'
  | 'UNCHANGED'
  | 'MISSING'
  | 'DELETE'
  | 'ERROR'
  | 'WARNING'
export type ImportRowsRequest = GridRequest & { change?: ImportChange }
export type ImportApply = {
  skipErrorRows?: boolean
  applyAttributes?: boolean
  missing?: 'KEEP' | 'DEACTIVATE'
}
