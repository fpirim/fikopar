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

// Buttons, links and headings in the page that are cut, run past the window or overlap another control.
export async function cutControls(page: Page): Promise<string[]> {
  await page.evaluate(() => document.fonts.ready)
  return page.evaluate(() => {
    const out: string[] = []
    const width = document.documentElement.clientWidth
    const els = [
      ...document.querySelectorAll<HTMLElement>('main button, main a, main h1, main [role=group]'),
    ].filter((e) => {
      const r = e.getBoundingClientRect()
      return (
        e.offsetParent !== null &&
        r.width > 1 &&
        r.height > 1 &&
        !e.closest('.fk-grid, [role=dialog]')
      )
    })
    const boxes = els.map((e) => [e, e.getBoundingClientRect()] as const)
    for (const [e, r] of boxes) {
      const name = e.textContent?.trim() || e.ariaLabel || e.tagName
      if (e.scrollWidth > e.clientWidth + 1) out.push(`cut: ${name}`)
      if (r.right > width + 1) out.push(`past the window: ${name}`)
      let p: HTMLElement | null = e.parentElement
      while (p && p.tagName !== 'MAIN') {
        const cs = getComputedStyle(p)
        const pr = p.getBoundingClientRect()
        if (cs.overflowX !== 'visible' && (r.right > pr.right + 1 || r.left < pr.left - 1)) {
          out.push(`hidden by its container: ${name}`)
          break
        }
        p = p.parentElement
      }
    }
    for (let i = 0; i < boxes.length; i++) {
      for (let j = i + 1; j < boxes.length; j++) {
        const [a, ra] = boxes[i]
        const [b, rb] = boxes[j]
        if (a.contains(b) || b.contains(a)) continue
        const x = Math.min(ra.right, rb.right) - Math.max(ra.left, rb.left)
        const y = Math.min(ra.bottom, rb.bottom) - Math.max(ra.top, rb.top)
        if (x > 1 && y > 1) out.push(`overlap: ${a.textContent?.trim()} / ${b.textContent?.trim()}`)
      }
    }
    return out
  })
}
