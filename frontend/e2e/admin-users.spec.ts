import { test, expect } from './fixtures'
import { ADMIN_EMAIL, loginAsAdmin } from './helpers'

test.beforeEach(async ({ page }) => {
  await loginAsAdmin(page)
  await page.goto('/admin/users')
  await expect(page.getByRole('heading', { name: 'Kullanıcı Yönetimi' })).toBeVisible()
})

// Full user CRUD in one test; the email is unique per run (timestamp).
test('admin performs full CRUD on a user', async ({ page }) => {
  const stamp = Date.now()
  const email = `e2e-${stamp}@example.com`
  const name = `E2E User ${stamp}`

  // Scope to the form: the page-level search box is also type=text.
  await page.getByRole('button', { name: /yeni/i }).click()
  await expect(page.getByRole('heading', { name: 'Yeni Kullanıcı' })).toBeVisible()
  const form = page.locator('form')
  await form.locator('input[type="text"]').first().fill(name)
  await form.locator('input[type="email"]').fill(email)
  await form.locator('input[type="password"]').fill('password123')
  await page.getByRole('button', { name: 'Oluştur' }).click()
  await expect(page.getByRole('heading', { name: 'Yeni Kullanıcı' })).toBeHidden()

  await expect(page.getByText(email).first()).toBeVisible()
  await expect(page.getByText(ADMIN_EMAIL).first()).toBeVisible()

  const row = page.locator('.ag-center-cols-container .ag-row').filter({ hasText: email }).first()
  await row.dblclick()
  await expect(page.getByRole('heading', { name: 'Kullanıcıyı Düzenle' })).toBeVisible()
  await page.locator('form').locator('input[type="text"]').first().fill(`${name} Güncel`)
  await page.getByRole('button', { name: 'Güncelle' }).click()
  await expect(page.getByRole('heading', { name: 'Kullanıcıyı Düzenle' })).toBeHidden()
  await expect(row).toContainText('Güncel')

  await row.getByRole('checkbox').check()
  await expect(page.getByRole('button', { name: /^Sil \(\d+\)$/ })).toBeEnabled()
  await page.getByRole('button', { name: /^Sil \(\d+\)$/ }).click()
  await expect(page.getByRole('heading', { name: 'Kullanıcıları sil' })).toBeVisible()
  await page.getByRole('button', { name: 'Sil', exact: true }).click()
  await expect(page.getByText(email)).toHaveCount(0)
})
