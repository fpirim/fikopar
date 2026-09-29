import type { ChipTone } from '../../components/ui/chip'
import { formatMoney } from '../../lib/format'

export function docTone(subtype: string): ChipTone {
  if (subtype.includes('RETURN')) return 'amber'
  if (subtype.includes('INVOICE') || subtype === 'PRICE_DIFFERENCE') return 'blue'
  if (subtype === 'COLLECTION') return 'green'
  return 'grey'
}

export const dash = (v: number | string | null | undefined) =>
  Number(v ?? 0) === 0 ? '—' : formatMoney(v)
