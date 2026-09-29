import type { Page } from '@playwright/test'

// Grid cells, filter selects and filter placeholders whose text does not fit: document numbers,
// dates, amounts and the columns in `cols` must never be cut. Cells are checked by overflow,
// selects and placeholders by the measured width of their text.
export async function clipped(page: Page, cols: string[] = []): Promise<string[]> {
  await page.evaluate(() => document.fonts.ready)
  return page.evaluate((cols) => {
    const out: string[] = []
    const cells = document.querySelectorAll<HTMLElement>('.fk-grid .ag-cell')
    for (const cell of cells) {
      const value = cell.querySelector<HTMLElement>('.ag-cell-value') ?? cell
      const text = cell.textContent?.trim() ?? ''
      const guarded =
        cell.matches('.fk-num, .font-mono') ||
        cols.includes(cell.getAttribute('col-id') ?? '') ||
        /\d{2}\.\d{2}\.\d{4}/.test(text)
      if (!guarded || !text) continue
      if (value.scrollWidth > value.clientWidth + 1 || cell.scrollWidth > cell.clientWidth + 1) {
        out.push(`${cell.getAttribute('col-id')}: ${text}`)
      }
    }
    const ctx = document.createElement('canvas').getContext('2d')!
    for (const sel of document.querySelectorAll<HTMLSelectElement>('.fk-grid select')) {
      const cs = getComputedStyle(sel)
      ctx.font = `${cs.fontWeight} ${cs.fontSize} ${cs.fontFamily}`
      const label = sel.selectedOptions[0]?.text ?? ''
      const room = sel.clientWidth - parseFloat(cs.paddingLeft) - parseFloat(cs.paddingRight) - 14
      if (ctx.measureText(label).width > room) out.push(`select ${sel.ariaLabel}: ${label}`)
    }
    for (const input of document.querySelectorAll<HTMLInputElement>(
      '.fk-grid input[placeholder]',
    )) {
      const cs = getComputedStyle(input)
      ctx.font = `${cs.fontWeight} ${cs.fontSize} ${cs.fontFamily}`
      const room = input.clientWidth - parseFloat(cs.paddingLeft) - parseFloat(cs.paddingRight)
      if (ctx.measureText(input.placeholder).width > room) {
        out.push(`placeholder ${input.ariaLabel}: ${input.placeholder}`)
      }
    }
    return out
  }, cols)
}
