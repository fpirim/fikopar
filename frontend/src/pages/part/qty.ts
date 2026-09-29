import { numberSeparators } from '../../lib/format'

// Quantity typed in the UI language → API Quantity ('8', '2.5'); '' when empty, null when invalid.
export function parseQty(text: string): string | null {
  const { group, decimal } = numberSeparators()
  const t = text.trim().split(group).join('').replace(decimal, '.')
  if (!t) return ''
  if (!/^\d{1,14}(\.\d{1,4})?$/.test(t)) return null
  return String(Number(t))
}
