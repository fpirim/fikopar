import { test, expect } from './fixtures'

test('redirects unauthenticated users away from protected routes', async ({ page }) => {
  await page.goto('/admin/users')
  await expect(page).toHaveURL(/\/login$/)
})

test('redirects unauthenticated users from the dashboard', async ({ page }) => {
  await page.goto('/')
  await expect(page).toHaveURL(/\/login$/)
})
