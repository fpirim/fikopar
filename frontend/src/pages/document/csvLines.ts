import { parseDecimal } from '../../lib/documents'

export interface CsvLine {
  code: string
  qty: string
  price: string
}

// Invoice lines from a CSV: code; qty; price (price optional). A header row and blank lines are skipped;
// ';' or tab separate cells ('.' / ',' are decimal marks), ',' only when no ';' is present.
export function parseLinesCsv(text: string): CsvLine[] {
  const rows = text
    .replace(/^\uFEFF/, '')
    .split(/\r?\n/)
    .map((r) => r.trim())
    .filter(Boolean)
  const sep = rows.some((r) => r.includes(';'))
    ? ';'
    : rows.some((r) => r.includes('\t'))
      ? '\t'
      : ','
  const out: CsvLine[] = []
  for (const r of rows) {
    const [code = '', qty = '', price = ''] = r
      .split(sep)
      .map((c) => c.trim().replace(/^"|"$/g, ''))
    const q = parseDecimal(qty, 4)
    if (!code || (qty && q === null && out.length === 0)) continue
    if (q === null || q === '') continue
    const p = parseDecimal(price, 4)
    out.push({ code, qty: q, price: p ?? '' })
  }
  return out
}
