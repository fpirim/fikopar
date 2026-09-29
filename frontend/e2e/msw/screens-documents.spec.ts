import path from 'node:path'
import type { Page } from '@playwright/test'
import { test, expect } from './fixtures'

// `npm run screens` writes review screenshots at 1440×900; skipped in the normal run.
const OUT = path.resolve(process.cwd(), '../docs/review/phase-3-screens')
const shot = (name: string) => path.join(OUT, `${name}.png`)
const cell = (page: Page, row: number, col: string) =>
  page.locator(
    `.fk-doc-lines .ag-center-cols-container .ag-row[row-index="${row}"] [col-id="${col}"]`,
  )

test.skip(!process.env.SCREENS, 'screenshots only on demand')

async function draftWithPopup(page: Page, label: string) {
  await page.goto('/invoices/1001')
  await expect(cell(page, 3, 'part')).toHaveText('BR-2210')
  await cell(page, 3, 'qty').click()
  await page.keyboard.press('Insert')
  await expect(page.getByRole('combobox', { name: /Part search|Parça ara/ })).toBeFocused()
  await page.keyboard.type('pump 37')
  await expect(page.getByRole('listbox', { name: label }).getByRole('option').first()).toBeVisible()
}

test('05 invoice draft with the part popup', async ({ page }) => {
  await draftWithPopup(page, 'Catalog search results')
  await page.screenshot({ path: shot('05-invoice'), animations: 'disabled' })
})

test('05a invoice draft', async ({ page }) => {
  await page.goto('/invoices/1001')
  await expect(cell(page, 3, 'part')).toHaveText('BR-2210')
  await cell(page, 1, 'qty').click()
  await page.screenshot({ path: shot('05a-invoice-draft'), animations: 'disabled' })
})

test('05b posted invoice', async ({ page }) => {
  await page.goto('/invoices/418')
  await expect(cell(page, 0, 'part')).toHaveText('HP-3774')
  await page.screenshot({ path: shot('05b-invoice-posted'), animations: 'disabled' })
})

test.describe('admin', () => {
  test.use({ admin: true })
  test('05c credit limit', async ({ page }) => {
    await page.goto('/invoices/1002')
    await expect(cell(page, 0, 'part')).toHaveText('HP-3780')
    await page.getByRole('button', { name: 'Post' }).click()
    await expect(page.getByRole('alertdialog')).toBeVisible()
    await page.screenshot({ path: shot('05c-credit-limit'), animations: 'disabled' })
  })
})

test('05d price difference', async ({ page }) => {
  await page.goto('/invoices/new?type=PRICE_DIFFERENCE&original=391')
  await expect(cell(page, 0, 'origin')).toContainText('HP-3702')
  await cell(page, 0, 'amount').dblclick()
  await page.keyboard.type('250')
  await page.keyboard.press('Enter')
  await page.getByRole('button', { name: 'Decrease' }).click()
  await page.screenshot({ path: shot('05d-price-difference'), animations: 'disabled' })
})

test('05e return', async ({ page }) => {
  await page.goto('/invoices/new?type=SALES_RETURN&original=391')
  await expect(cell(page, 1, 'returnable')).toHaveText('3')
  await page.screenshot({ path: shot('05e-return'), animations: 'disabled' })
})

test('05f cancelled', async ({ page }) => {
  await page.goto('/invoices/415')
  await expect(cell(page, 0, 'part')).toHaveText('FL-0092')
  await page.screenshot({ path: shot('05f-cancelled'), animations: 'disabled' })
})

test('06 documents list', async ({ page }) => {
  await page.goto('/invoices')
  await expect(page.locator('.fk-grid .ag-floating-bottom')).toContainText('14 of 14')
  await page.screenshot({ path: shot('06-documents'), animations: 'disabled' })
})

test('01 dashboard latest documents', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByRole('link', { name: 'SF-2026-1-000418' })).toBeVisible()
  await page.screenshot({ path: shot('01-dashboard'), animations: 'disabled' })
})

test('09 part movements link documents', async ({ page }) => {
  await page.goto('/parts/1001')
  await expect(page.getByRole('link', { name: 'SF-2026-1-000418' })).toBeVisible()
  await page.screenshot({ path: shot('09b-part-movements'), animations: 'disabled' })
})

test.describe('1152', () => {
  test.use({ viewport: { width: 1152, height: 900 } })
  test('05 invoice at 1152', async ({ page }) => {
    await draftWithPopup(page, 'Catalog search results')
    await page.keyboard.press('Escape')
    await page.screenshot({ path: shot('05g-invoice-1152'), animations: 'disabled' })
  })
})

test.describe('tr', () => {
  test.use({ lang: 'tr' })
  test('05 invoice TR', async ({ page }) => {
    await draftWithPopup(page, 'Katalog arama sonuçları')
    await page.screenshot({ path: shot('05-invoice-tr'), animations: 'disabled' })
  })
  test('06 documents TR', async ({ page }) => {
    await page.goto('/invoices')
    await expect(page.locator('.fk-grid .ag-floating-bottom')).toContainText('14 / 14')
    await page.screenshot({ path: shot('06-documents-tr'), animations: 'disabled' })
  })
})
