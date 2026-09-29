import type { Page } from '@playwright/test'
import { test, expect } from './fixtures'

const popup = (page: Page) =>
  page.getByRole('listbox', { name: /Catalog search results|Katalog arama sonuçları/ })

// Cells whose text does not fit in the popup (codes, prices, stock).
async function cutCells(page: Page): Promise<string[]> {
  await page.evaluate(() => document.fonts.ready)
  return page.evaluate(() =>
    [...document.querySelectorAll<HTMLElement>('[cmdk-item] [role=cell]')]
      .filter((c) => c.scrollWidth > c.clientWidth + 1)
      .map((c) => c.textContent ?? ''),
  )
}

for (const [keys, label] of [
  ['ControlOrMeta+k', '⌘K'],
  ['F2', 'F2'],
] as const) {
  test(`${label} finds a part and opens its card`, async ({ page }) => {
    await page.goto('/balances?depot=1')
    await expect(page.getByRole('searchbox', { name: 'Search' })).toBeVisible()
    await page.keyboard.press(keys)
    const input = page.getByRole('combobox', { name: 'Part search' })
    await expect(input).toBeFocused()
    await input.fill('pump 37')
    await expect(popup(page).getByRole('option', { name: /HP-3702/ })).toBeVisible()
    await expect(page.getByText(/\d+ of \d+ · price list 29\.09\.2026/)).toBeVisible()
    const options = popup(page).getByRole('option')
    await expect(options.first()).toHaveAttribute('aria-selected', 'true')
    await page.keyboard.press('ArrowDown')
    await expect(options.nth(1)).toHaveAttribute('aria-selected', 'true')
    const no = await options.nth(1).getAttribute('data-stock-no')
    await page.keyboard.press('Enter')
    await expect(page).toHaveURL(/\/parts\/\d+\?depot=1$/)
    await expect(input).toBeHidden()
    expect(no).toMatch(/^[A-Z]{2}-\d{4}$/)
  })
}

test('Esc closes the popup and marks show the matched words', async ({ page }) => {
  await page.goto('/balances')
  await expect(page.getByRole('searchbox', { name: 'Search' })).toBeVisible()
  await page.keyboard.press('F2')
  const input = page.getByRole('combobox', { name: 'Part search' })
  await input.fill('pump 37')
  const row = popup(page).getByRole('option', { name: /HP-3702/ })
  await expect(row.locator('mark').first()).toBeVisible()
  await expect(row.locator('mark')).toContainText(['37'])
  await page.keyboard.press('Escape')
  await expect(input).toBeHidden()
})

for (const lang of ['en', 'tr'] as const) {
  test.describe(lang, () => {
    test.use({ lang })
    test('popup cells are never cut', async ({ page }) => {
      await page.goto('/balances')
      await expect(page.getByRole('searchbox')).toBeVisible()
      await page.keyboard.press('F2')
      const input = page.getByRole('combobox', { name: /Part search|Parça ara/ })
      await input.fill('pump 37')
      await expect(popup(page).getByRole('option').first()).toBeVisible()
      expect(await cutCells(page)).toEqual([])
      await input.fill('hydraulic')
      await expect(popup(page).getByRole('option').nth(10)).toBeVisible()
      expect(await cutCells(page)).toEqual([])
    })
  })
}
