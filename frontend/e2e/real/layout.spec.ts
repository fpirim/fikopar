import { test, expect, seeded, V11 } from './fixtures'
import { clipped } from '../clipped'

const TEXT_COLS = ['accountName', 'city', 'sicilTipi']

for (const lang of ['en', 'tr'] as const) {
  test(`balances show whole names, cities and filter hints (${lang})`, async ({ page }) => {
    await page.addInitScript((l) => localStorage.setItem('lang', l), lang)
    await page.goto('/balances?asOf=2026-09-29')
    await expect(page.locator('.fk-grid')).toContainText('Yılmaz Tarım Makinaları')
    expect(await clipped(page, TEXT_COLS)).toEqual([])
    const overflow = await page
      .locator('.fk-grid .ag-center-cols-viewport')
      .first()
      .evaluate((el) => el.scrollWidth - el.clientWidth)
    expect(overflow).toBeLessThanOrEqual(0)
  })

  test(
    `statement keeps the tax line whole and badges journal lines (${lang})`,
    V11,
    async ({ page }) => {
      await page.addInitScript((l) => localStorage.setItem('lang', l), lang)
      const id = seeded().accounts['120.10482']
      await page.goto(`/statements/${id}?from=2026-01-01&to=2026-09-29`)
      await expect(page.locator('.fk-grid').first()).toContainText(
        lang === 'en' ? 'JOURNAL' : 'YEVMİYE',
      )
      const tax = page.getByText(/^(Tax office|Vergi dairesi) Konya · \[TAX NO\]$/)
      const lines = await tax.evaluate(
        (el) => el.getBoundingClientRect().height / parseFloat(getComputedStyle(el).lineHeight),
      )
      expect(lines).toBeLessThan(1.5)
      expect(await clipped(page)).toEqual([])
    },
  )
}
