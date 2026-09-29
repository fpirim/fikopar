import path from 'node:path'
import type { Page } from '@playwright/test'
import { test, expect, grid, pinnedBottom } from './fixtures'

// `npm run screens` writes review screenshots at 1440×900; skipped in the normal run.
const OUT = path.resolve(process.cwd(), '../docs/review/phase-2-screens')
const shot = (name: string) => path.join(OUT, `${name}.png`)
const counted = (page: Page, no: string) =>
  grid(page)
    .locator('.ag-center-cols-container .ag-row', { hasText: no })
    .locator('[col-id="countedQty"]')

test.skip(!process.env.SCREENS, 'screenshots only on demand')

async function shelfA0(page: Page, label = 'Filter Shelf') {
  await page.goto('/stock-counts/2')
  await page.getByLabel(label).fill('A-0')
  await expect(pinnedBottom(page)).toContainText('A-0*')
}

test('10 stock count', async ({ page }) => {
  await shelfA0(page)
  await counted(page, 'GS-4410').click()
  await page.keyboard.type('1')
  await page.screenshot({ path: shot('10-stock-count'), animations: 'disabled' })
})

test('10b count conflict', async ({ page }) => {
  await shelfA0(page)
  await counted(page, 'BE-0620').click()
  await page.keyboard.type('32')
  await page.keyboard.press('Enter')
  await expect(page.getByRole('alertdialog')).toBeVisible()
  await page.screenshot({ path: shot('10b-count-conflict'), animations: 'disabled' })
})

test('10c post differences', async ({ page }) => {
  await page.goto('/stock-counts/2')
  await page.getByRole('button', { name: 'Post differences' }).click()
  await expect(page.getByRole('dialog')).toBeVisible()
  await page.screenshot({ path: shot('10c-post-differences'), animations: 'disabled' })
})

test('10d counts list', async ({ page }) => {
  await page.goto('/stock-counts')
  await expect(page.getByRole('link', { name: 'SAY-2026-1-000002' })).toBeVisible()
  await page.screenshot({ path: shot('10d-counts-list'), animations: 'disabled' })
})

test('10f add part', async ({ page }) => {
  await page.goto('/stock-counts/2')
  await page.getByRole('button', { name: 'Add part' }).click()
  await page.getByRole('dialog').getByRole('combobox', { name: 'Part search' }).fill('pump')
  await expect(
    page.getByRole('listbox', { name: 'Catalog search results' }).getByRole('option').first(),
  ).toBeVisible()
  await page.screenshot({ path: shot('10f-add-part'), animations: 'disabled' })
})

test.describe('tr', () => {
  test.use({ lang: 'tr' })
  test('10e stock count TR', async ({ page }) => {
    await shelfA0(page, 'Raf filtresi')
    await page.screenshot({ path: shot('10e-stock-count-tr'), animations: 'disabled' })
  })
})
