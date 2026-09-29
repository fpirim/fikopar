import { numberSeparators } from '../../lib/format'

// '' clears the count (null); undefined = not a quantity.
export function parseCounted(text: unknown): string | null | undefined {
  if (text === null || text === undefined) return null
  const { group, decimal } = numberSeparators()
  const t = String(text).trim().split(group).join('').replace(decimal, '.')
  if (!t) return null
  if (!/^\d{1,14}(\.\d{1,4})?$/.test(t)) return undefined
  return String(Number(t))
}
