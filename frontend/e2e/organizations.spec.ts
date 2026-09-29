import { test, expect } from './fixtures'
import { loginAsAdmin } from './helpers'

test.beforeEach(async ({ page }) => {
  await loginAsAdmin(page)
  await page.goto('/admin/organizations')
  await expect(page.getByRole('heading', { name: 'Organizasyon Yönetimi' })).toBeVisible()
})

// Full organization CRUD in one test; name and slug are unique per run (timestamp).
test('admin performs full CRUD on an organization', async ({ page }) => {
  const stamp = Date.now()
  const name = `E2E Org ${stamp}`
  const slug = `e2e-org-${stamp}`

  // First text input is the name, second the slug.
  await page.getByRole('button', { name: /yeni/i }).click()
  await expect(page.getByRole('heading', { name: 'Yeni Organizasyon' })).toBeVisible()
  const form = page.locator('form')
  await form.locator('input[type="text"]').first().fill(name)
  await form.locator('input[type="text"]').nth(1).fill(slug)
  await page.getByRole('button', { name: 'Oluştur' }).click()
  await expect(page.getByRole('heading', { name: 'Yeni Organizasyon' })).toBeHidden()

  await expect(page.getByText(slug).first()).toBeVisible()

  const row = page.locator('.ag-center-cols-container .ag-row').filter({ hasText: slug }).first()
  await row.dblclick()
  await expect(page.getByRole('heading', { name: 'Organizasyonu Düzenle' })).toBeVisible()
  await page.locator('form').locator('input[type="text"]').first().fill(`${name} Güncel`)
  await page.getByRole('button', { name: 'Güncelle' }).click()
  await expect(page.getByRole('heading', { name: 'Organizasyonu Düzenle' })).toBeHidden()
  await expect(row).toContainText('Güncel')

  await row.getByRole('checkbox').check()
  await expect(page.getByRole('button', { name: /^Sil \(\d+\)$/ })).toBeEnabled()
  await page.getByRole('button', { name: /^Sil \(\d+\)$/ }).click()
  await expect(page.getByRole('heading', { name: 'Organizasyonları sil' })).toBeVisible()
  await page.getByRole('button', { name: 'Sil', exact: true }).click()
  await expect(page.getByText(slug)).toHaveCount(0)
})
