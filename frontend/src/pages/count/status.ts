import type { ChipTone } from '../../components/ui/chip'
import type { StockCount } from '../../api/types'

export const STATUS_TONE: Record<StockCount['status'], ChipTone> = {
  OPEN: 'amber',
  FINALIZED: 'green',
  CANCELLED: 'grey',
}
