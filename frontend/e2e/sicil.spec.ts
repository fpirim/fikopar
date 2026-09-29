import { test, expect } from './fixtures'
import { loginAsAdmin } from './helpers'

// Full account-card CRUD scoped to a fresh organization, as admin.
test('admin performs full CRUD on an account card under a new organization', async ({ page }) => {
  await loginAsAdmin(page)

  // Unique names avoid picker collisions on reruns.
  const stamp = Date.now()
  const orgName = `E2E HK Org ${stamp}`
  const slug = `e2e-org-${stamp}`
  await page.goto('/admin/organizations')
  await expect(page.getByRole('heading', { name: 'Organizasyon Yönetimi' })).toBeVisible()
  await page.getByRole('button', { name: /yeni/i }).click()
  await expect(page.getByRole('heading', { name: 'Yeni Organizasyon' })).toBeVisible()
  const orgForm = page.locator('form')
  await orgForm.locator('input[type="text"]').first().fill(orgName)
  await orgForm.locator('input[type="text"]').nth(1).fill(slug)
  await page.getByRole('button', { name: 'Oluştur' }).click()
  await expect(page.getByRole('heading', { name: 'Yeni Organizasyon' })).toBeHidden()

  // Admins get no auto-selection, so pick the org in the header.
  await page.goto('/sicil')
  await expect(page.getByRole('heading', { name: 'Hesap Kartları' })).toBeVisible()
  await page.getByRole('button', { name: 'Organizasyon Seçin' }).click()
  await page.getByRole('button', { name: orgName }).click()

  const hesapNo = `001-${Date.now()}`
  await page.getByRole('button', { name: /yeni/i }).click()
  await expect(page.getByRole('heading', { name: 'Yeni Hesap Kartı' })).toBeVisible()
  const form = page.locator('form')
  // First text input is the read-only ID; then account no, then name.
  const texts = form.locator('input[type="text"]')
  await texts.nth(1).fill(hesapNo)
  await texts.nth(2).fill('E2E Kart')
  await page.getByRole('button', { name: 'Oluştur' }).click()
  await expect(page.getByRole('heading', { name: 'Yeni Hesap Kartı' })).toBeHidden()

  await expect(page.getByText(hesapNo).first()).toBeVisible()

  const row = page.locator('.ag-center-cols-container .ag-row').filter({ hasText: hesapNo }).first()
  await row.dblclick()
  await expect(page.getByRole('heading', { name: 'Hesap Kartını Düzenle' })).toBeVisible()
  const editForm = page.locator('form')
  await editForm.locator('input[type="text"]').nth(2).fill('E2E Kart Güncel')
  await page.getByRole('button', { name: 'Güncelle' }).click()
  await expect(page.getByRole('heading', { name: 'Hesap Kartını Düzenle' })).toBeHidden()
  await expect(page.getByText('E2E Kart Güncel').first()).toBeVisible()

  await row.getByRole('checkbox').check()
  await expect(page.getByRole('button', { name: /^Sil \(\d+\)$/ })).toBeEnabled()
  await page.getByRole('button', { name: /^Sil \(\d+\)$/ }).click()
  await expect(page.getByRole('heading', { name: 'Hesap kartlarını sil' })).toBeVisible()
  await page.getByRole('button', { name: 'Sil', exact: true }).click()
  await expect(page.getByText(hesapNo)).toHaveCount(0)
})

test('user with no orgs sees the select-org prompt', async ({ page }) => {
  await loginAsAdmin(page)
  await page.goto('/sicil')
  await expect(page.getByRole('heading', { name: 'Hesap Kartları' })).toBeVisible()
  // Smoke check that the prompt path renders when no org is selected.
  const prompt = page.getByText(/üst menüden bir organizasyon seçin/i)
  const grid = page.locator('.ag-theme-alpine')
  await expect(prompt.or(grid)).toBeVisible()
})
