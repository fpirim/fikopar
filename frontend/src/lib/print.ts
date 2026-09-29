export interface PrintColumn<T> {
  header: string
  value: (row: T) => string
}

const escapeHtml = (s: string): string =>
  s.replace(
    /[&<>"]/g,
    (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' })[c] as string,
  )

const STYLES = `
  body { font-family: ui-sans-serif, system-ui, sans-serif; padding: 24px; color: #111; }
  h1 { font-size: 18px; margin: 0 0 16px; }
  table { border-collapse: collapse; width: 100%; font-size: 12px; }
  th, td { border: 1px solid #ccc; padding: 6px 8px; text-align: left; vertical-align: top; }
  thead { background: #f3f4f6; }
  @media print { body { padding: 0; } }`

export function printRows<T>(title: string, columns: PrintColumn<T>[], rows: T[]): void {
  if (rows.length === 0) return

  const head = columns.map((c) => `<th>${escapeHtml(c.header)}</th>`).join('')
  const rowHtml = (r: T): string => {
    const cells = columns.map((c) => `<td>${escapeHtml(c.value(r) ?? '')}</td>`).join('')
    return `<tr>${cells}</tr>`
  }
  const body = rows.map(rowHtml).join('')

  const w = window.open('', '_blank')
  if (!w) return

  // document.write is deprecated; build the DOM directly and let the opener trigger print.
  w.document.head.innerHTML = `<meta charset="utf-8" /><title>${escapeHtml(title)}</title><style>${STYLES}</style>`
  w.document.body.innerHTML = `<h1>${escapeHtml(title)} (${rows.length})</h1><table><thead><tr>${head}</tr></thead><tbody>${body}</tbody></table>`
  w.focus()
  w.print()
}
