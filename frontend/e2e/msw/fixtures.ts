import { test as base, expect, type Page } from '@playwright/test'

// Signed in as the MSW demo user (or an admin), company 1, English unless a test picks Turkish.
export const test = base.extend<{ page: Page; lang: 'en' | 'tr'; admin: boolean }>({
  lang: ['en', { option: true }],
  admin: [false, { option: true }],
  page: async ({ page, lang, admin }, use) => {
    await page.addInitScript(([l, token]) => {
      localStorage.setItem('token', token)
      localStorage.setItem('selectedOrganizationId', '1')
      if (!localStorage.getItem('lang')) localStorage.setItem('lang', l)
    }, [lang, admin ? 'msw-admin' : 'msw-token'] as const)
    // eslint-disable-next-line react-hooks/rules-of-hooks
    await use(page)
  },
})

export { expect }

export const grid = (page: Page) => page.locator('.fk-grid').first()
export const pinnedBottom = (page: Page) => grid(page).locator('.ag-floating-bottom')

// Grid cells and filter selects whose text does not fit: document numbers, dates and amounts
// must never be cut. Cells are checked by overflow, selects by the width of the chosen label.
export async function clipped(page: Page): Promise<string[]> {
  await page.evaluate(() => document.fonts.ready)
  return page.evaluate(() => {
    const out: string[] = []
    const cells = document.querySelectorAll<HTMLElement>('.fk-grid .ag-cell')
    for (const cell of cells) {
      const value = cell.querySelector<HTMLElement>('.ag-cell-value') ?? cell
      const text = cell.textContent?.trim() ?? ''
      const guarded = cell.matches('.fk-num, .font-mono') || /\d{2}\.\d{2}\.\d{4}/.test(text)
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
      const room =
        sel.clientWidth - parseFloat(cs.paddingLeft) - parseFloat(cs.paddingRight) - 14
      if (ctx.measureText(label).width > room) out.push(`select ${sel.ariaLabel}: ${label}`)
    }
    return out
  })
}
