import { test, expect, clipped } from './fixtures'

// Document numbers, dates and amounts are never cut, in either language.
const SCREENS = [
  '/balances?asOf=2026-09-29',
  '/statements/5000?from=2026-01-01&to=2026-09-29',
  '/statements/5000?from=2025-12-01&to=2026-09-29&depot=1',
  '/receipts',
  '/cash-book?from=2026-09-01&to=2026-09-29&depot=1',
  '/carry-forward/2026',
  '/journal',
]

for (const lang of ['en', 'tr'] as const) {
  test.describe(lang, () => {
    test.use({ lang })
    for (const url of SCREENS) {
      test(`nothing clipped on ${url}`, async ({ page }) => {
        await page.goto(url)
        await expect(page.locator('.fk-grid .ag-row').first()).toBeVisible()
        await page.waitForTimeout(300)
        expect(await clipped(page)).toEqual([])
        const overflow = await page
          .locator('.fk-grid .ag-center-cols-viewport')
          .first()
          .evaluate((el) => el.scrollWidth - el.clientWidth)
        expect(overflow).toBeLessThanOrEqual(0)
      })
    }
  })
}

// Names, cities and filter hints are text, but the mockup's must fit at 1440 too.
const BALANCES = {
  en: { city: 'Filter City', balance: 'Filter Balance', min: '> 10,000', shown: '10 of 412' },
  tr: { city: 'Şehir filtresi', balance: 'Bakiye filtresi', min: '> 10.000', shown: '10 / 412' },
}

const TEXT_COLS = ['accountName', 'city', 'sicilTipi']

for (const lang of ['en', 'tr'] as const) {
  test.describe(`${lang} text`, () => {
    test.use({ lang })

    test('balances show whole names, cities and filter hints', async ({ page }) => {
      const f = BALANCES[lang]
      await page.goto('/balances?asOf=2026-09-29')
      await expect(page.locator('.fk-grid .ag-row').first()).toBeVisible()
      await page.waitForTimeout(300)
      expect(await clipped(page, TEXT_COLS)).toEqual([])
      await page.getByLabel(f.city).fill('Konya')
      await page.getByLabel(f.balance).fill(f.min)
      await expect(page.locator('.fk-grid .ag-floating-bottom')).toContainText(f.shown)
      await expect(page.locator('.fk-grid')).toContainText('Yılmaz Tarım Makinaları')
      expect(await clipped(page, TEXT_COLS)).toEqual([])
      await page.goto('/balances?asOf=2026-09-29&type=FIRMA')
      await expect(page.locator('.fk-grid .ag-row').first()).toBeVisible()
      expect(await clipped(page, TEXT_COLS)).toEqual([])
    })

    test('statement card keeps the tax office and number on one line', async ({ page }) => {
      await page.goto('/statements/5000?from=2026-01-01&to=2026-09-29')
      const tax = page.getByText(/^(Tax office|Vergi dairesi) .+ · \[TAX NO\]$/)
      await expect(tax).toBeVisible()
      const box = await tax.evaluate((el) => ({
        lines: el.getBoundingClientRect().height / parseFloat(getComputedStyle(el).lineHeight),
        overflow: el.scrollWidth - el.clientWidth,
      }))
      expect(box.lines).toBeLessThan(1.5)
      expect(box.overflow).toBeLessThanOrEqual(0)
    })
  })
}
