import type { DocumentStatus } from '../../api/types'
import type { ChipTone } from '../../components/ui/chip'

export const STATUS_TONE: Record<DocumentStatus, ChipTone> = {
  DRAFT: 'amber',
  POSTED: 'green',
  CANCELLED: 'red',
}
