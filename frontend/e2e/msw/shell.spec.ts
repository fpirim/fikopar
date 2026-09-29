import { test, expect } from './fixtures'

test('sidebar sections, company switch and depot in the URL', async ({ page }) => {
  await page.goto('/balances')
  const nav = page.getByRole('navigation', { name: 'Main' })
  for (const s of ['Overview', 'Trade', 'Accounts', 'Treasury', 'Stock', 'System']) {
    await expect(nav.getByText(s, { exact: true })).toBeVisible()
  }
  await expect(nav.getByRole('link', { name: /Balances/ })).toHaveAttribute('aria-current', 'page')

  await page.getByRole('group', { name: 'Depot' }).getByRole('button', { name: 'Depot 2' }).click()
  await expect(page).toHaveURL(/depot=2/)
  await nav.getByRole('link', { name: /Cash book/ }).click()
  await expect(page).toHaveURL(/\/cash-book\?depot=2/)
  await expect(page.getByText(/^Depot 2 · /)).toBeVisible()

  await page.getByRole('button', { name: /Demo Company A/ }).click()
  await page.getByRole('button', { name: 'Demo Company B', exact: true }).click()
  await expect(page.getByRole('button', { name: /^Company · 2 of 2 Demo Company B/ })).toBeVisible()
})

test('language switch keeps the screen and reformats numbers', async ({ page }) => {
  await page.goto('/cash-book?from=2026-09-22&to=2026-09-29')
  await expect(page.getByRole('group', { name: 'Closing' })).toContainText('46,815.50')
  await page.getByRole('group', { name: 'Language' }).getByRole('button', { name: 'TR' }).click()
  await expect(page.getByRole('heading', { name: 'Kasa defteri' })).toBeVisible()
  await expect(page.getByRole('group', { name: 'Kapanış' })).toContainText('46.815,50')
  await page.reload()
  await expect(page.getByRole('heading', { name: 'Kasa defteri' })).toBeVisible()
})

test('global search opens a statement from the keyboard', async ({ page }) => {
  await page.goto('/balances')
  const box = page.getByRole('searchbox', { name: 'Search' })
  await box.fill('Yılmaz')
  await expect(page.getByRole('option', { name: /Yılmaz Tarım Makinaları/ })).toBeVisible()
  await box.press('ArrowDown')
  await box.press('Enter')
  await expect(page).toHaveURL(/\/statements\/5000$/)
  await expect(page.getByRole('heading', { name: 'Yılmaz Tarım Makinaları' })).toBeVisible()
})

test('account menu signs out', async ({ page }) => {
  await page.goto('/balances')
  await page.getByRole('button', { name: 'Account menu' }).click()
  await expect(page.getByText('demo@fikopar.local')).toBeVisible()
  await page.getByRole('button', { name: 'Log out' }).click()
  await expect(page).toHaveURL(/\/login$/)
})
