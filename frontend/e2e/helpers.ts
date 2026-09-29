import { Page, expect } from '@playwright/test'

export const ADMIN_EMAIL = process.env.E2E_ADMIN_EMAIL || 'admin@fikopar.local'
const ADMIN_PASSWORD = process.env.E2E_ADMIN_PASSWORD || 'admin123'

export async function login(page: Page, email: string, password: string) {
  await page.goto('/login')
  await page.locator('input[type="email"]').fill(email)
  await page.locator('input[type="password"]').fill(password)
  await page.getByRole('button', { name: /giriş yap/i }).click()
}

export async function loginAsAdmin(page: Page) {
  await login(page, ADMIN_EMAIL, ADMIN_PASSWORD)
  await expect(page).toHaveURL(/\/$/)
  await expect(page.getByRole('banner')).toContainText(/Gösterge paneli|Dashboard/)
}
