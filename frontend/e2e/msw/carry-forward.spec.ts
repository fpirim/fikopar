import { test, expect, grid } from './fixtures'

test('override a row, then post the carry forward', async ({ page }) => {
  await page.goto('/carry-forward/2026')
  await expect(page.getByRole('heading', { name: 'Carry forward 2025 → 2026' })).toBeVisible()
  await expect(page.getByRole('group', { name: 'Entries (account × depot)' })).toContainText('822')
  await page.getByLabel('Filter Account').fill('120.10517')
  const rows = grid(page).locator('.ag-center-cols-container .ag-row')
  await expect(rows).toHaveCount(2)
  await rows.first().dblclick()
  const dialog = page.getByRole('dialog')
  await dialog.getByLabel('Opening debit').fill('70,000')
  await dialog.getByLabel('Reason').fill('Disputed invoice')
  await dialog.getByRole('button', { name: 'Save' }).click()
  await expect(
    grid(page).locator('.ag-center-cols-container').getByText('EDITED').first(),
  ).toBeVisible()

  await page.getByRole('button', { name: 'Post 822 opening entries' }).click()
  await page.getByRole('alertdialog').getByRole('button', { name: 'Post' }).click()
  await expect(page.getByText('Carry forward 2026 posted')).toBeVisible()
  await expect(page.getByText('Posted', { exact: true })).toBeVisible()
  await expect(
    grid(page).locator('.ag-center-cols-container').getByText('POSTED').first(),
  ).toBeVisible()
})

test('Enter on a focused row opens the override', async ({ page }) => {
  await page.goto('/carry-forward/2026')
  const cell = grid(page).locator('.ag-row[row-index="0"] [col-id="accountName"]')
  await cell.click()
  await page.keyboard.press('Enter')
  await expect(page.getByRole('dialog')).toContainText('Opening amount')
  await page.keyboard.press('Escape')
  const pdf = page.waitForEvent('download')
  await page.getByRole('button', { name: 'Preview PDF' }).click()
  expect((await pdf).suggestedFilename()).toBe('carry-forward-2026.pdf')
})
