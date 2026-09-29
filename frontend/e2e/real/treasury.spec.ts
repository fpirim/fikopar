import { test, expect, V11 } from './fixtures'

test('cash book of depot 1 totals the seeded week', async ({ page }) => {
  await page.goto('/cash-book?from=2026-09-01&to=2026-09-30&depot=1')
  await expect(page.getByRole('group', { name: 'Closing' })).toContainText('65,335.50')
  await expect(page.locator('.fk-grid').first()).toContainText('Fuel · delivery van')
})

test('All depots asks for a depot', async ({ page }) => {
  await page.goto('/cash-book?from=2026-09-01&to=2026-09-30')
  const prompt = page.getByRole('region', { name: 'Choose a depot' })
  await prompt.getByRole('button', { name: 'Depot 1' }).click()
  await expect(page.getByRole('group', { name: 'Closing' })).toContainText('65,335.50')
})

test('recent receipts list the seeded collections', async ({ page }) => {
  await page.goto('/receipts')
  const recent = page.getByRole('region', { name: 'Recent receipts' })
  await expect(recent).toContainText('Kaya Ziraat')
  await expect(recent).toContainText(/TAH-2026-1-0+5/)
})

// Needs globalSearch, which the F1 backend does not serve yet.
test('cash collection posts and shows in the cash book', V11, async ({ page }) => {
  await page.goto('/receipts?depot=1')
  await page.getByRole('combobox', { name: 'Account' }).fill('Kaya')
  await page.getByRole('option', { name: /Kaya Ziraat/ }).click()
  await page.getByLabel('Amount', { exact: true }).fill('1,000')
  const pdf = page.waitForEvent('download')
  await page.getByRole('button', { name: 'Post & print' }).click()
  await pdf
  await expect(page.getByText(/TAH-2026-1-\d+ posted/)).toBeVisible()
})

test('carry forward posts, closes 2025, and reopens', V11, async ({ page }) => {
  await page.goto('/carry-forward/2026')
  await page.getByRole('button', { name: /Post \d+ opening entries/ }).click()
  await page.getByRole('alertdialog').getByRole('button', { name: 'Post' }).click()
  await expect(page.getByText('Carry forward 2026 posted')).toBeVisible()

  await page.goto('/cash-book?from=2025-12-01&to=2025-12-31&depot=1')
  await page.getByRole('button', { name: 'Expense' }).click()
  const dialog = page.getByRole('dialog')
  await dialog.getByLabel('Date').fill('31.12.2025')
  await dialog.getByLabel('Amount').fill('10')
  await dialog.getByLabel('Description').fill('Late receipt')
  await dialog.getByRole('button', { name: 'Post' }).click()
  await expect(dialog.getByRole('alert')).toContainText(
    '2025 is closed; reopen the 2026 carry-forward first',
  )

  await page.goto('/carry-forward/2026')
  await page.getByRole('button', { name: 'Reopen…' }).click()
  await page.getByRole('dialog').getByLabel('Reason').fill('Late supplier invoice')
  await page.getByRole('dialog').getByRole('button', { name: 'Reopen' }).click()
  await expect(page.getByText('Reopened', { exact: true })).toBeVisible()
})
