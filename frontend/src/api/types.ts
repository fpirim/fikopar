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
