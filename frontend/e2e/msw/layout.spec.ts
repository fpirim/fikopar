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
      })
    }
  })
}
