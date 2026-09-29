import { test, expect } from './fixtures'

test('document entry: postings, trail and the owner refusal', async ({ page }) => {
  await page.goto('/journal/4833')
  await expect(page.getByRole('heading', { name: 'J-2026-1-004833' })).toBeVisible()
  await expect(page.getByText('Σ 0.00', { exact: true })).toBeVisible()
  await expect(page.getByRole('table', { name: 'Postings' })).toContainText('48,720.00')
  await expect(page.getByRole('list', { name: 'Trail' }).getByRole('listitem')).toHaveCount(4)
  await page.getByRole('button', { name: 'Reverse…' }).click()
  const dialog = page.getByRole('dialog')
  await dialog.getByLabel('Reason').fill('Wrong customer')
  await dialog.getByRole('button', { name: 'Post reversal' }).click()
  await expect(dialog.getByRole('alert')).toContainText('This entry belongs to SF-2026-1-000418; reverse it there')
})

test('manual entry reverses and links both ways', async ({ page }) => {
  await page.goto('/journal')
  await page.getByRole('searchbox', { name: 'Entry or document no.' }).fill('J-2026-1-004829')
  await page.getByRole('link', { name: 'J-2026-1-004829' }).click()
  await page.getByRole('button', { name: 'Reverse…' }).click()
  await page.getByRole('dialog').getByLabel('Reason').fill('Posted twice')
  await page.getByRole('dialog').getByRole('button', { name: 'Post reversal' }).click()
  await expect(page).toHaveURL(/\/journal\/4834$/)
  await expect(page.getByText('reversal', { exact: true })).toBeVisible()
  await page.getByRole('link', { name: 'J-2026-1-004829' }).click()
  await expect(page.getByText('REVERSED', { exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Reverse…' })).toBeDisabled()
})
