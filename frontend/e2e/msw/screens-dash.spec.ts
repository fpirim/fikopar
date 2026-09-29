import path from 'node:path'
import { test, expect } from './fixtures'

// `npm run screens` writes review screenshots at 1440×900; skipped in the normal run.
const OUT = path.resolve(process.cwd(), '../docs/review/phase-2-screens')
const shot = (name: string) => path.join(OUT, `${name}.png`)

test.skip(!process.env.SCREENS, 'screenshots only on demand')

test('02 dashboard', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByRole('region', { name: 'Below minimum / negative' })).toContainText(
    'GS-3516',
  )
  await page.screenshot({ path: shot('02-dashboard'), animations: 'disabled' })
})

test.describe('tr', () => {
  test.use({ lang: 'tr' })
  test('02b dashboard in Turkish, depot 1', async ({ page }) => {
    await page.goto('/?depot=1')
    await expect(page.getByRole('region', { name: 'Minimum altı / eksi' })).toContainText('eksi')
    await page.screenshot({ path: shot('02b-dashboard-tr'), animations: 'disabled' })
  })
})

test('08f cash book across a carried year', async ({ page }) => {
  await page.goto('/cash-book?from=2025-12-01&to=2026-01-31&depot=1')
  await expect(page.locator('.fk-grid .ag-row.fk-row-reset')).toContainText('CARRIED')
  await page.screenshot({ path: shot('08f-cash-book-opening'), animations: 'disabled' })
})
