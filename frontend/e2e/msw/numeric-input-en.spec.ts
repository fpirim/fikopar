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

// A caret move queued for an earlier key must not land after later keys (was 1,234,567.98).
test('NumericInput keeps up with fast typing', async ({ page }) => {
  await page.goto('/playground/numeric-input')
  const input = page.getByTestId('genel-wrap').locator('input')
  for (let i = 0; i < 20; i++) {
    await input.click()
    await page.keyboard.press('ControlOrMeta+a')
    await page.keyboard.type('1234567.89', { delay: 0 })
    await input.blur()
    await expect(input).toHaveValue('1,234,567.89')
    await expect(page.getByTestId('genel-state')).toHaveText('state: 1234567.89')
  }
})
