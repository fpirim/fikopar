import path from 'node:path'
import { test, expect, grid, pinnedBottom } from './fixtures'

// `npm run screens` writes review screenshots at 1440×900; skipped in the normal run.
const OUT = path.resolve(process.cwd(), '../docs/review/phase-2-screens')
const shot = (name: string) => path.join(OUT, `${name}.png`)

test.skip(!process.env.SCREENS, 'screenshots only on demand')

test('09 parts', async ({ page }) => {
  await page.goto('/parts?depot=1')
  await page.getByLabel('Filter Part').fill('HP-37')
  await expect(pinnedBottom(page)).toContainText('· 1 filter')
  await page.mouse.move(0, 0)
  await page.screenshot({ path: shot('09-parts'), animations: 'disabled' })
})

test('09b part', async ({ page }) => {
  await page.goto('/parts/1001')
  await expect(grid(page)).toContainText('SF-2026-1-000418')
  await page.mouse.move(0, 0)
  await page.screenshot({ path: shot('09b-part'), animations: 'disabled' })
})

test('09c transfer warning', async ({ page }) => {
  await page.goto('/parts/1001?depot=1')
  await expect(grid(page)).toContainText('SF-2026-1-000418')
  await page.getByRole('button', { name: 'Transfer' }).click()
  await page.getByRole('dialog').getByLabel('Quantity').fill('12')
  await page.getByRole('dialog').getByRole('button', { name: 'Transfer' }).click()
  await expect(page.getByRole('alert')).toContainText('on hand −2')
  await expect(grid(page)).toContainText('TRF-2026-1-000052')
  await page.mouse.move(0, 0)
  await page.screenshot({ path: shot('09c-transfer-warning'), animations: 'disabled' })
})

test('09d new price', async ({ page }) => {
  await page.goto('/parts/1001')
  await page.getByRole('button', { name: 'New price' }).click()
  const dialog = page.getByRole('dialog')
  await dialog.getByLabel('Price').fill('13,100')
  await dialog.getByLabel('Valid from').fill('15.10.2026')
  await dialog.getByLabel('Valid from').press('Tab')
  await page.screenshot({ path: shot('09d-new-price'), animations: 'disabled' })
})

test.describe('Turkish', () => {
  test.use({ lang: 'tr' })
  test('09e parts TR', async ({ page }) => {
    await page.goto('/parts?stock=BELOW_MIN_OR_NEGATIVE&depot=1')
    await expect(pinnedBottom(page)).toContainText('/')
    await page.mouse.move(0, 0)
    await page.screenshot({ path: shot('09e-parts-tr'), animations: 'disabled' })
  })
})
