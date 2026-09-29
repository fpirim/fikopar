import { test, expect, grid } from './fixtures'

test('statement with brought forward, depot totals and exports', async ({ page }) => {
  await page.goto('/statements/5000?from=2026-01-01&to=2026-09-29')
  await expect(page.getByRole('heading', { name: 'Yılmaz Tarım Makinaları' })).toBeVisible()
  await expect(page.getByText('01.01.2026 – 29.09.2026 · all depots')).toBeVisible()
  await expect(grid(page).locator('.ag-floating-top')).toContainText('Brought forward')
  await expect(grid(page).locator('.ag-floating-top')).toContainText('142,300.00')
  const totals = page.getByRole('table', { name: 'Depot totals' })
  await expect(totals).toContainText('168,170.00 Dr')
  await expect(totals).toContainText('186,340.00 Dr')
  await expect(page.getByRole('progressbar', { name: 'Credit used' })).toHaveAttribute(
    'aria-valuenow',
    '75',
  )

  await page.getByLabel('Filter Type').selectOption('SALES_INVOICE')
  await expect(grid(page).getByText('TAH-2026-1-000219')).toHaveCount(0)
  await expect(grid(page).getByText('SF-2026-1-000418')).toBeVisible()

  const xlsx = page.waitForEvent('download')
  await page.getByRole('button', { name: 'Excel' }).click()
  expect((await xlsx).suggestedFilename()).toBe('statement-120.10482-2026-01-01-2026-09-29.xlsx')
  const pdf = page.waitForEvent('download')
  await page.getByRole('button', { name: 'PDF' }).click()
  expect((await pdf).suggestedFilename()).toMatch(/\.pdf$/)
})

test('period and depot drive the statement', async ({ page }) => {
  await page.goto('/statements/5000?from=2026-01-01&to=2026-09-29&depot=2')
  await expect(page.getByText('01.01.2026 – 29.09.2026 · depot 2')).toBeVisible()
  await expect(page.getByRole('table', { name: 'Depot totals' })).toContainText('18,170.00 Dr')
  await page.getByRole('button', { name: 'Change period' }).click()
  await page.getByLabel('From').fill('25.09.2026')
  await page.getByRole('button', { name: 'Apply' }).click()
  await expect(page).toHaveURL(/from=2026-09-25/)
  await expect(grid(page).locator('.ag-floating-top')).toContainText('21,650.00')
})

test('edit card and pick another account', async ({ page }) => {
  await page.goto('/statements/5000')
  await page.getByRole('button', { name: 'Edit card' }).click()
  const dialog = page.getByRole('dialog', { name: 'Edit account card' })
  await dialog.getByLabel('District').fill('Meram')
  await dialog.getByRole('button', { name: 'Save' }).click()
  await expect(page.getByText('Meram / Konya')).toBeVisible()

  await page.goto('/statements')
  await page.getByRole('combobox', { name: 'Account' }).fill('Kaya')
  await page.getByRole('option', { name: /Kaya Ziraat/ }).click()
  await expect(page).toHaveURL(/\/statements\/5006$/)
  await expect(page.getByRole('heading', { name: 'Kaya Ziraat' })).toBeVisible()
})
