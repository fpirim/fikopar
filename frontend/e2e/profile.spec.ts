import { test, expect } from './fixtures'
import { loginAsAdmin } from './helpers'

// 1x1 transparent PNG.
const PNG_BASE64 =
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=='

test('updates the profile name', async ({ page }) => {
  await loginAsAdmin(page)
  await page.goto('/profile')
  await expect(page.getByRole('heading', { name: 'Profil' })).toBeVisible()

  const nameInput = page.locator('input[type="text"]').first()
  await nameInput.fill('Admin Renamed')
  await page.getByRole('button', { name: 'Güncelle' }).click()
  await expect(page.getByText('Profil güncellendi')).toBeVisible()
})

// Regression: updating name/email must NOT wipe an existing avatar.
test('keeps the avatar after a name-only update', async ({ page }) => {
  await loginAsAdmin(page)
  await page.goto('/profile')

  // Upload an avatar and save.
  await page.locator('input[type="file"]').setInputFiles({
    name: 'avatar.png',
    mimeType: 'image/png',
    buffer: Buffer.from(PNG_BASE64, 'base64'),
  })
  await page.getByRole('button', { name: 'Güncelle' }).click()
  await expect(page.getByText('Profil güncellendi')).toBeVisible()
  await expect(page.locator('img[alt="avatar"]')).toBeVisible()

  // Now change only the name and save again.
  await page.locator('input[type="text"]').first().fill('Avatar Keeper')
  await page.getByRole('button', { name: 'Güncelle' }).click()
  // The first save's toast may still be up.
  await expect(page.getByText('Profil güncellendi').last()).toBeVisible()

  // Reload from the server: the avatar must still be there.
  await page.goto('/profile')
  await expect(page.locator('img[alt="avatar"]')).toBeVisible()
})
