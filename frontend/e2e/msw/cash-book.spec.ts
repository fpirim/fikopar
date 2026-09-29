import { test, expect, grid, pinnedBottom } from './fixtures'

test('cash book totals match the mockup week', async ({ page }) => {
  await page.goto('/cash-book?from=2026-09-22&to=2026-09-29&depot=1')
  await expect(page.getByText('Depot 1 · 22.09 – 29.09.2026')).toBeVisible()
  await expect(page.getByRole('group', { name: 'Opening', exact: true })).toContainText('38,215.50')
  await expect(page.getByRole('group', { name: 'In', exact: true })).toContainText('+52,400.00')
  await expect(page.getByRole('group', { name: 'Out', exact: true })).toContainText('−43,800.00')
  await expect(pinnedBottom(page)).toContainText('46,815.50')
  await page.getByLabel('Filter Document').fill('GDR')
  await expect(grid(page).locator('.ag-center-cols-container .ag-row')).toHaveCount(1)
})

test('expense and deposit vouchers post to the till', async ({ page }) => {
  await page.goto('/cash-book?from=2026-09-22&to=2099-12-31&depot=1')
  await page.getByRole('button', { name: 'Expense' }).click()
  let dialog = page.getByRole('dialog', { name: 'Expense from the till' })
  await expect(dialog).toContainText('GDR-2026-1-000039')
  await dialog.getByLabel('Amount').fill('250')
  await dialog.getByLabel('Description').fill('Parcel')
  await dialog.getByRole('button', { name: 'Post' }).click()
  await expect(page.getByText('GDR-2026-1-000039 posted')).toBeVisible()
  await expect(page.getByRole('group', { name: 'Out', exact: true })).toContainText('−44,050.00')

  await page.getByRole('button', { name: 'Bank deposit' }).click()
  dialog = page.getByRole('dialog', { name: 'Cash to the bank' })
  await dialog.getByLabel('Amount').fill('10000')
  await dialog.getByLabel('Description').fill('Deposit')
  await dialog.getByRole('button', { name: 'Post' }).click()
  await expect(page.getByText('VRM-2026-1-000013 posted')).toBeVisible()
  await expect(page.getByRole('group', { name: 'Out', exact: true })).toContainText('−54,050.00')
})

test('till count records the difference', async ({ page }) => {
  await page.goto('/cash-book?from=2026-09-22&to=2026-09-29&depot=1')
  await page.getByRole('button', { name: 'Count cash' }).click()
  const dialog = page.getByRole('dialog', { name: 'Count cash · Depot 1' })
  await expect(dialog).toContainText('46,815.50')
  await dialog.getByLabel('Counted').fill('46,820')
  await expect(dialog).toContainText('4.50')
  await dialog.getByRole('button', { name: 'Record count' }).click()
  await expect(page.getByText('Count recorded, difference 4.50')).toBeVisible()
})

test('All depots asks for a depot before showing a till', async ({ page }) => {
  await page.goto('/cash-book?from=2026-09-22&to=2026-09-29')
  const prompt = page.getByRole('region', { name: 'Choose a depot' })
  await expect(prompt).toContainText('Each depot has its own till')
  await prompt.getByRole('button', { name: 'Depot 1' }).click()
  await expect(page).toHaveURL(/depot=1/)
  await expect(page.getByRole('group', { name: 'Closing' })).toContainText('46,815.50')
})
