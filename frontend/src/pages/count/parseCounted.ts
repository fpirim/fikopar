import { numberSeparators } from '../../lib/format'

const esc = (s: string) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')

// '' clears the count (null); undefined = not a quantity of 0 or more in the UI language
// (groups of three digits, the language's decimal separator, up to 4 decimals).
export function parseCounted(text: unknown): string | null | undefined {
  if (text === null || text === undefined) return null
  const t = String(text).trim()
  if (!t) return null
  const { group, decimal } = numberSeparators()
  const g = esc(group)
  const d = esc(decimal)
  const shape = new RegExp(`^(\\d{1,3}(${g}\\d{3})+|\\d{1,14})(${d}\\d{1,4})?$`)
  if (!shape.test(t)) return undefined
  const plain = t.split(group).join('').replace(decimal, '.')
  if (plain.split('.')[0].length > 14) return undefined
  return String(Number(plain))
}
