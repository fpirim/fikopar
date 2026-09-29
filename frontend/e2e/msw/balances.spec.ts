import { test, expect, grid, pinnedBottom } from './fixtures'

test('filters combine on the server and the pinned row sums them', async ({ page }) => {
  await page.goto('/balances?asOf=2026-09-29')
  await expect(pinnedBottom(page)).toContainText('412 of 412')
  await page.getByLabel('Filter City').fill('Konya')
  await page.getByLabel('Filter Type').selectOption('MUSTERI')
  await page.getByLabel('Filter Balance').fill('> 10,000')
  await expect(pinnedBottom(page)).toContainText('6 of 412 · 3 filters')
  await expect(pinnedBottom(page)).toContainText('1,467,065.00')
  await expect(pinnedBottom(page)).toContainText('282,425.00')
  await expect(page.getByText('1–6 of 6')).toBeVisible()
  await expect(page.getByText(/filters: City, Type, Balance/)).toBeVisible()

  await grid(page).getByRole('columnheader', { name: 'Balance' }).click()
  await expect(grid(page).locator('.ag-row[row-index="0"]')).toContainText('120.10833')

  await page.getByRole('button', { name: 'Clear filters' }).click()
  await expect(pinnedBottom(page)).toContainText('412 of 412')
})

test('grid cells take keyboard focus and arrows move it', async ({ page }) => {
  await page.goto('/balances?asOf=2026-09-29')
  const first = grid(page).locator('.ag-row[row-index="0"] [col-id="city"]')
  await first.click()
  await expect(first).toHaveClass(/ag-cell-focus/)
  await page.keyboard.press('ArrowDown')
  await expect(grid(page).locator('.ag-row[row-index="1"] [col-id="city"]')).toHaveClass(
    /ag-cell-focus/,
  )
})

test('selected rows print statements; Excel and CSV download', async ({ page }) => {
  await page.goto('/balances?asOf=2026-09-29')
  await page.getByLabel('Filter Account').fill('120.104')
  await expect(pinnedBottom(page)).toContainText('of 412')
  const rows = grid(page).locator('.ag-center-cols-container .ag-row')
  await rows.nth(0).locator('input[type=checkbox]').check()
  await rows.nth(1).locator('input[type=checkbox]').check()
  await expect(page.getByText('2 selected')).toBeVisible()
  const pdf = page.waitForEvent('download')
  await page.getByRole('button', { name: 'Statements PDF' }).click()
  expect((await pdf).suggestedFilename()).toBe('statements-2026-09-29.pdf')
  const xlsx = page.waitForEvent('download')
  await page.getByRole('button', { name: 'Excel' }).click()
  expect((await xlsx).suggestedFilename()).toBe('balances-2026-09-29.xlsx')
  const csv = page.waitForEvent('download')
  await page.getByRole('button', { name: 'CSV' }).click()
  const file = await csv
  expect(file.suggestedFilename()).toBe('balances-2026-09-29.csv')
})

test('type segment and as-of date narrow the request', async ({ page }) => {
  await page.goto('/balances?asOf=2026-09-29')
  await page
    .getByRole('group', { name: 'Account type' })
    .getByRole('button', { name: 'Banks' })
    .click()
  await expect(pinnedBottom(page)).toContainText('4 of 4')
  await expect(page).toHaveURL(/type=BANKA/)
  await grid(page).getByRole('link', { name: '102.00002' }).click()
  await expect(page).toHaveURL(/\/statements\/\d+/)
})
