import path from 'node:path'
import { test, expect, grid, pinnedBottom } from './fixtures'

// `npm run screens` writes review screenshots at 1440×900; skipped in the normal run.
const OUT = path.resolve(process.cwd(), '../docs/review/phase-1-screens')
const shot = (name: string) => path.join(OUT, `${name}.png`)

test.skip(!process.env.SCREENS, 'screenshots only on demand')

test('03 balances', async ({ page }) => {
  await page.goto('/balances?asOf=2026-09-29')
  await page.getByLabel('Filter City').fill('Konya')
  await page.getByLabel('Filter Type').selectOption('MUSTERI')
  await page.getByLabel('Filter Balance').fill('> 10,000')
  await expect(pinnedBottom(page)).toContainText('6 of 412 · 3 filters')
  const rows = grid(page).locator('.ag-center-cols-container .ag-row')
  await rows.nth(0).locator('input[type=checkbox]').check()
  await rows.nth(1).locator('input[type=checkbox]').check()
  await page.mouse.move(0, 0)
  await page.screenshot({ path: shot('03-balances'), animations: 'disabled' })
})

test('04 statement', async ({ page }) => {
  await page.goto('/statements/5000?from=2026-01-01&to=2026-09-29')
  await expect(page.getByRole('table', { name: 'Depot totals' })).toContainText('186,340.00')
  await page.screenshot({ path: shot('04-statement'), animations: 'disabled' })
})

test('06 receipt', async ({ page }) => {
  await page.goto('/receipts')
  await page.getByRole('combobox', { name: 'Account' }).fill('Kaya')
  await page.getByRole('option', { name: /Kaya Ziraat/ }).click()
  await page.getByLabel('Amount', { exact: true }).fill('15,000')
  await page.getByLabel('Description', { exact: true }).fill('Cash collection')
  await page.getByLabel('Description', { exact: true }).blur()
  await expect(page.getByText('79,000.00 Dr')).toBeVisible()
  await page.screenshot({ path: shot('06-receipt'), animations: 'disabled' })
})

test('08 cash book', async ({ page }) => {
  await page.goto('/cash-book?from=2026-09-22&to=2026-09-29&depot=1')
  await expect(pinnedBottom(page)).toContainText('46,815.50')
  await page.screenshot({ path: shot('08-cash-book'), animations: 'disabled' })
  await page.getByRole('button', { name: 'Expense' }).click()
  await expect(page.getByRole('dialog')).toContainText('GDR-2026-1-000039')
  await page.screenshot({ path: shot('08b-cash-book-expense'), animations: 'disabled' })
  await page.keyboard.press('Escape')
  await page.getByRole('button', { name: 'Bank deposit' }).click()
  await expect(page.getByRole('dialog')).toContainText('VRM-2026-1-000013')
  await page.screenshot({ path: shot('08c-cash-book-deposit'), animations: 'disabled' })
  await page.keyboard.press('Escape')
  await page.getByRole('button', { name: 'Count cash' }).click()
  await page.getByRole('dialog').getByLabel('Counted').fill('46,800')
  await page.screenshot({ path: shot('08d-cash-book-count'), animations: 'disabled' })
})

test('11 carry forward', async ({ page }) => {
  await page.goto('/carry-forward/2026')
  await expect(page.getByRole('group', { name: 'Accounts with balance' })).toContainText('411')
  await page.screenshot({ path: shot('11-carry-forward'), animations: 'disabled' })
  await page.getByLabel('Filter Account').fill('120.10711')
  const rows = grid(page).locator('.ag-center-cols-container .ag-row')
  await expect(rows).toHaveCount(2)
  await rows.first().dblclick()
  await expect(page.getByRole('dialog')).toBeVisible()
  await page.screenshot({ path: shot('11b-carry-forward-override'), animations: 'disabled' })
})

test('12 journal entry', async ({ page }) => {
  await page.goto('/journal/4833')
  await expect(page.getByRole('list', { name: 'Trail' }).getByRole('listitem')).toHaveCount(4)
  await page.screenshot({ path: shot('12-journal-entry'), animations: 'disabled' })
  await page.getByRole('button', { name: 'Reverse…' }).click()
  await page.getByRole('dialog').getByLabel('Reason').fill('Wrong customer')
  await page.getByRole('dialog').getByRole('button', { name: 'Post reversal' }).click()
  await expect(page.getByRole('dialog').getByRole('alert')).toBeVisible()
  await page.screenshot({ path: shot('12b-journal-reverse-refused'), animations: 'disabled' })
})

test('12c journal list and TR shell', async ({ page }) => {
  await page.goto('/journal')
  await expect(page.getByRole('link', { name: 'J-2026-1-004833' })).toBeVisible()
  await page.screenshot({ path: shot('12c-journal-list'), animations: 'disabled' })
  await page.getByRole('group', { name: 'Language' }).getByRole('button', { name: 'TR' }).click()
  await page.goto('/balances?asOf=2026-09-29&depot=1')
  await expect(pinnedBottom(page)).toContainText('412 / 412')
  await page.screenshot({ path: shot('03b-balances-tr-depot1'), animations: 'disabled' })
})
