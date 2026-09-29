import path from 'node:path'
import { test, expect, grid } from './fixtures'

// `npm run screens` writes review screenshots at 1440×900; skipped in the normal run.
const OUT = path.resolve(process.cwd(), '../docs/review/phase-2-screens')
const shot = (name: string) => path.join(OUT, `${name}.png`)
const fixture = (name: string) => path.resolve(process.cwd(), 'e2e/fixtures', name)

test.skip(!process.env.SCREENS, 'screenshots only on demand')

test('05p catalog search', async ({ page }) => {
  await page.goto('/balances?depot=1')
  await expect(page.getByRole('searchbox', { name: 'Search' })).toBeVisible()
  await page.keyboard.press('F2')
  await page.getByRole('combobox', { name: 'Part search' }).fill('pump 37')
  await expect(page.getByRole('option', { name: /HP-3702/ })).toBeVisible()
  await page.keyboard.press('ArrowDown')
  await page.screenshot({ path: shot('05p-catalog-search'), animations: 'disabled' })
})

test.describe('tr', () => {
  test.use({ lang: 'tr' })
  test('05q catalog search in Turkish', async ({ page }) => {
    await page.goto('/balances')
    await expect(page.getByRole('searchbox')).toBeVisible()
    await page.keyboard.press('ControlOrMeta+k')
    await page.getByRole('combobox', { name: 'Parça ara' }).fill('filtre')
    await page.getByRole('combobox', { name: 'Parça ara' }).fill('filter')
    await expect(page.getByRole('listbox').last().getByRole('option').first()).toBeVisible()
    await page.screenshot({ path: shot('05q-catalog-search-tr'), animations: 'disabled' })
  })
})

test('14 price list import', async ({ page }) => {
  await page.goto('/parts/import')
  await page.getByLabel('File').setInputFiles(fixture('price-list-unmapped.csv'))
  await page.getByRole('button', { name: 'Upload and preview' }).click()
  await page.getByLabel('Stock no column').selectOption('Kod')
  await page.getByLabel('Price column').selectOption('Tutar')
  await page.screenshot({ path: shot('14b-import-mapping'), animations: 'disabled' })
  await page.getByRole('button', { name: 'Save mapping' }).click()
  await page.getByRole('button', { name: 'Discard import' }).click()
  await page.getByRole('link', { name: 'New import' }).click()
  await expect(page.getByRole('region', { name: 'Recent imports' })).toContainText('DISCARDED')
  await page.getByLabel('File').setInputFiles(fixture('price-list.csv'))
  await page.screenshot({ path: shot('14-import-upload'), animations: 'disabled' })
  await page.getByRole('button', { name: 'Upload and preview' }).click()
  await expect(grid(page)).toContainText('HP-9001')
  await page.mouse.move(0, 0)
  await page.screenshot({ path: shot('14c-import-preview'), animations: 'disabled' })
  await page.getByRole('button', { name: 'Continue to apply' }).click()
  await page.getByRole('checkbox', { name: 'Skip error rows' }).check()
  await page.getByRole('button', { name: 'Apply', exact: true }).click()
  await page.getByRole('alertdialog').getByRole('button', { name: 'Apply' }).click()
  await expect(page.getByRole('list', { name: 'Result' })).toBeVisible()
  await page.screenshot({ path: shot('14d-import-result'), animations: 'disabled' })
})
