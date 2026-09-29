import path from 'node:path'
import { test, expect, seeded, V12, V15 } from './fixtures'
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

// SCREENS=1 npm run e2e:real:v15 -- screens-real: F3 documents on the real API.
const OUT3 = path.resolve(process.cwd(), '../docs/review/phase-3-screens')
const shot3 = (name: string) => path.join(OUT3, `${name}-real.png`)

for (const lang of ['en', 'tr'] as const) {
  const sfx = lang === 'tr' ? '-tr' : ''
  test.describe(`documents ${lang}`, () => {
    test.beforeEach(async ({ page }) => {
      await page.addInitScript((l) => localStorage.setItem('lang', l), lang)
    })

    test(`05 invoice editor with the catalog popup${sfx}`, V15, async ({ page }) => {
      await page.goto(`/invoices/${seeded().documents.draft}`)
      await expect(page.locator('.fk-doc-lines')).toContainText('HP-3774')
      await page.getByRole('button', { name: /^(Add line|Satır ekle)/ }).click()
      await expect(page.getByRole('combobox', { name: /^(Part search|Parça ara)/ })).toBeFocused()
      await page.keyboard.type('HP-37')
      const results = page.getByRole('listbox').last()
      await expect(results.locator('[data-stock-no="HP-3702"]')).toBeVisible()
      await page.screenshot({ path: shot3(`05-invoice-catalog${sfx}`), animations: 'disabled' })
    })

    test(`05b posted invoice${sfx}`, V15, async ({ page }) => {
      await page.goto(`/invoices/${seeded().documents.sale}`)
      await expect(page.getByText(/^(POSTED|KAYITLI)$/).first()).toBeVisible()
      await page.mouse.move(0, 0)
      expect(await clipped(page, ['part'])).toEqual([])
      await page.screenshot({ path: shot3(`05b-invoice-posted${sfx}`), animations: 'disabled' })
    })

    test(`05c credit limit${sfx}`, V15, async ({ page }) => {
      await page.goto(`/invoices/${seeded().documents.draft}`)
      await expect(page.locator('.fk-doc-lines')).toContainText('HP-3774')
      await page.getByRole('button', { name: /^(Post|Kaydet)$/ }).click()
      const dialog = page.getByRole('alertdialog')
      await expect(dialog).toContainText('250')
      await page.screenshot({ path: shot3(`05c-credit-limit${sfx}`), animations: 'disabled' })
      await page.keyboard.press('Escape')
    })

    test(`06 documents${sfx}`, V15, async ({ page }) => {
      await page.goto('/invoices')
      await expect(grid(page)).toContainText('Öztürk Tarım')
      await page.mouse.move(0, 0)
      expect(await clipped(page, ['documentNo', 'type', 'status'])).toEqual([])
      await page.screenshot({ path: shot3(`06-documents${sfx}`), animations: 'disabled' })
    })
  })
}
