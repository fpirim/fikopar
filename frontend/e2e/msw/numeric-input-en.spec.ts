import { test, expect } from './fixtures'

test('NumericInput uses English separators in English', async ({ page }) => {
  await page.goto('/playground/numeric-input')
  const input = page.getByTestId('genel-wrap').locator('input')
  await input.click()
  await input.pressSequentially('1234567')
  await input.press('.')
  await input.pressSequentially('89')
  await input.blur()
  await expect(input).toHaveValue('1,234,567.89')
  await expect(page.getByTestId('genel-state')).toHaveText('state: 1234567.89')
})
