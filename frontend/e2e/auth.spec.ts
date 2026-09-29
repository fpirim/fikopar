import { test, expect } from './fixtures'
import { ADMIN_EMAIL, login, loginAsAdmin } from './helpers'

test('rejects wrong credentials with an inline error', async ({ page }) => {
  await login(page, ADMIN_EMAIL, 'definitely-wrong')
  await expect(page.getByText('Geçersiz e-posta veya şifre')).toBeVisible()
  // Must stay on the login page (no silent reload/redirect loop).
  await expect(page).toHaveURL(/\/login$/)
})

test('logs in as admin and reaches the dashboard', async ({ page }) => {
  await loginAsAdmin(page)
  await expect(page).toHaveURL(/\/$/)
  await expect(page.getByText('Hoş Geldin', { exact: true })).toBeVisible()
})

test('logs out back to the login page', async ({ page }) => {
  await loginAsAdmin(page)
  await page.getByRole('button', { name: 'Hesap menüsü' }).click()
  await page.getByRole('button', { name: /çıkış/i }).click()
  await expect(page).toHaveURL(/\/login$/)
})

test.describe('language', () => {
  test.use({ lang: null })

  test('defaults to English and switches to Turkish', async ({ page }) => {
    await page.goto('/login')
    await expect(page.getByRole('button', { name: 'Sign In' })).toBeVisible()

    await page.getByRole('combobox', { name: 'Language' }).selectOption('tr')
    await expect(page.getByRole('button', { name: 'Giriş Yap' })).toBeVisible()

    await page.reload()
    await expect(page.getByRole('button', { name: 'Giriş Yap' })).toBeVisible()
  })
})
