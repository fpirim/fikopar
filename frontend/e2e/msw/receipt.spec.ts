import { test, expect, grid } from './fixtures'

test('cash collection posts, prints and shows in recent receipts and the cash book', async ({
  page,
}) => {
  await page.goto('/receipts')
  await expect(page.getByText('TAH-2026-1-000234')).toBeVisible()
  await expect(page.getByRole('radio', { name: /Cheque/ })).toBeDisabled()
  await expect(page.getByRole('radio', { name: /Promissory note/ })).toBeDisabled()
  await page.getByRole('combobox', { name: 'Account' }).fill('Kaya')
  await page.getByRole('option', { name: /Kaya Ziraat/ }).click()
  const aside = page.getByRole('complementary', { name: 'Account summary' })
  await expect(aside).toContainText('94,000.00 Dr')
  await page.getByLabel('Amount', { exact: true }).fill('15,000')
  await expect(aside).toContainText('79,000.00 Dr')
  await page.getByLabel('Description', { exact: true }).fill('Cash collection')
  const pdf = page.waitForEvent('download')
  await page.getByRole('button', { name: 'Post & print' }).click()
  expect((await pdf).suggestedFilename()).toBe('TAH-2026-1-000234.pdf')
  await expect(page.getByText('TAH-2026-1-000234 posted')).toBeVisible()
  await expect(page.getByText('TAH-2026-1-000235')).toBeVisible()
  await expect(grid(page).locator('.ag-row[row-index="0"]')).toContainText('TAH-2026-1-000234')

  await page
    .getByRole('navigation', { name: 'Main' })
    .getByRole('link', { name: /Cash book/ })
    .click()
  await expect(grid(page).getByText('TAH-2026-1-000234')).toBeVisible()
})

test('bank transfer needs a bank; methods move with arrow keys', async ({ page }) => {
  await page.goto('/receipts')
  await page.getByRole('radio', { name: /Cash/ }).focus()
  await page.keyboard.press('ArrowRight')
  await expect(page.getByRole('radio', { name: /Bank transfer/ })).toHaveAttribute(
    'aria-checked',
    'true',
  )
  await expect(page.getByLabel('Bank account')).toHaveValue(/\d+/)
  await page.getByRole('button', { name: 'Post & print' }).click()
  await expect(page.getByText('Choose an account')).toBeVisible()
  await expect(page.getByText('Enter an amount greater than zero')).toBeVisible()
})
