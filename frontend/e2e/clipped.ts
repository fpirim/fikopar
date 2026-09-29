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

// Grid column headers whose label does not fit.
export async function cutHeaders(page: Page): Promise<string[]> {
  await page.evaluate(() => document.fonts.ready)
  return page.evaluate(() =>
    [...document.querySelectorAll<HTMLElement>('.fk-grid .ag-header-cell-text')]
      .filter((h) => h.offsetParent !== null && h.scrollWidth > h.clientWidth + 1)
      .map((h) => h.textContent ?? ''),
  )
}

// Grids that scroll sideways.
export async function sideways(page: Page): Promise<string[]> {
  return page.evaluate(() =>
    [...document.querySelectorAll<HTMLElement>('.fk-grid .ag-center-cols-viewport')]
      .filter((v) => v.scrollWidth > v.clientWidth + 1)
      .map((v) => `${v.closest('.fk-grid')?.className ?? ''}: ${v.scrollWidth - v.clientWidth}px`),
  )
}

// Top bar parts (breadcrumb, search, depot and language switches, account) that are cut, run past the window or
// overlap each other.
export async function topbarClash(page: Page): Promise<string[]> {
  await page.evaluate(() => document.fonts.ready)
  return page.evaluate(() => {
    const header = document.querySelector('header')
    if (!header) return ['no header']
    const width = document.documentElement.clientWidth
    const parts = [...header.children].filter(
      (e): e is HTMLElement => e instanceof HTMLElement && e.getBoundingClientRect().width > 1,
    )
    const inner = [...header.querySelectorAll<HTMLElement>('button, input, [role=group]')]
    const out: string[] = []
    const name = (e: HTMLElement) => e.textContent?.trim() || e.ariaLabel || e.tagName
    for (const e of [...parts, ...inner]) {
      const r = e.getBoundingClientRect()
      if (r.right > width + 1) out.push(`past the window: ${name(e)}`)
      if (e.tagName !== 'INPUT' && e.scrollWidth > e.clientWidth + 1) out.push(`cut: ${name(e)}`)
    }
    for (let i = 0; i < parts.length; i++) {
      for (let j = i + 1; j < parts.length; j++) {
        const a = parts[i].getBoundingClientRect()
        const b = parts[j].getBoundingClientRect()
        const x = Math.min(a.right, b.right) - Math.max(a.left, b.left)
        const y = Math.min(a.bottom, b.bottom) - Math.max(a.top, b.top)
        if (x > 1 && y > 1) out.push(`overlap: ${name(parts[i])} / ${name(parts[j])}`)
      }
    }
    for (const e of inner) {
      const r = e.getBoundingClientRect()
      for (const o of inner) {
        if (o === e || o.contains(e) || e.contains(o)) continue
        const q = o.getBoundingClientRect()
        const x = Math.min(r.right, q.right) - Math.max(r.left, q.left)
        const y = Math.min(r.bottom, q.bottom) - Math.max(r.top, q.top)
        if (x > 1 && y > 1) out.push(`overlap: ${name(e)} / ${name(o)}`)
      }
    }
    return [...new Set(out)]
  })
}
