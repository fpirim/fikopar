import path from 'node:path'
import { test, expect, seeded, V12 } from './fixtures'
import { clipped } from '../clipped'

// SCREENS=1 npm run e2e:real:v12 -- screens-real: real-API screenshots next to the MSW ones.
const OUT = path.resolve(process.cwd(), '../docs/review/phase-2-screens')
const shot = (name: string) => path.join(OUT, `${name}-real.png`)
const grid = (page: import('@playwright/test').Page) => page.locator('.fk-grid').first()

test.skip(!process.env.SCREENS, 'screenshots only on demand')

for (const lang of ['en', 'tr'] as const) {
  const sfx = lang === 'tr' ? '-tr' : ''
  test.describe(lang, () => {
    test.beforeEach(async ({ page }) => {
      await page.addInitScript((l) => localStorage.setItem('lang', l), lang)
    })

    test(`09 parts${sfx}`, V12, async ({ page }) => {
      await page.goto(`/parts?depot=${seeded().depots['1']}`)
      await expect(grid(page)).toContainText('HP-3774')
      await page.mouse.move(0, 0)
      expect(await clipped(page, ['stockNo', 'codes', 'shelf', 'catalogCode'])).toEqual([])
      await page.screenshot({ path: shot(`09-parts${sfx}`), animations: 'disabled' })
    })

    test(`09b part${sfx}`, V12, async ({ page }) => {
      await page.goto(`/parts/${seeded().parts['HP-3774']}`)
      await expect(grid(page)).toContainText(/TRF-2026-/)
      await page.mouse.move(0, 0)
      expect(await clipped(page)).toEqual([])
      await page.screenshot({ path: shot(`09b-part${sfx}`), animations: 'disabled' })
    })

    test(`10 stock count${sfx}`, V12, async ({ page }) => {
      await page.goto(`/stock-counts/${seeded().openCountId}`)
      const rows = grid(page).locator('.ag-center-cols-container .ag-row')
      await expect(rows.first()).toBeVisible()
      await page.mouse.move(0, 0)
      expect(await clipped(page, ['shelf', 'stockNo', 'name'])).toEqual([])
      await page.screenshot({ path: shot(`10-stock-count${sfx}`), animations: 'disabled' })
    })
  })
}
