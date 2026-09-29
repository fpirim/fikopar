import type { Page } from '@playwright/test'
import { test, expect, clipped } from './fixtures'

// Numbers, codes, chips and headings on the dashboard cards that do not fit their box.
async function cut(page: Page): Promise<string[]> {
  await page.evaluate(() => document.fonts.ready)
  return page.evaluate(() => {
    const out: string[] = []
    const sel = 'main .font-mono, main .fk-num, main h1, main h2, main [role=group] > div'
    for (const el of document.querySelectorAll<HTMLElement>(sel)) {
      const text = el.textContent?.trim() ?? ''
      if (text && el.scrollWidth > el.clientWidth + 1) out.push(text)
    }
    return out
  })
}

for (const lang of ['en', 'tr'] as const) {
  test.describe(lang, () => {
    test.use({ lang })
    test('nothing cut on the dashboard', async ({ page }) => {
      await page.goto('/')
      await expect(page.locator('main li').first()).toBeVisible()
      expect(await cut(page)).toEqual([])
    })
  })
}

test('below-minimum card lists negatives first and opens the parts grid', async ({ page }) => {
  await page.goto('/')
  const card = page.getByRole('region', { name: 'Below minimum / negative' })
  await expect(card).toContainText('365 below minimum · 24 negative')
  const rows = card.getByRole('listitem')
  await expect(rows).toHaveCount(10)
  await expect(rows.first()).toContainText('GS-3516')
  await expect(rows.first()).toContainText('−4 / 0')
  await card.getByRole('link', { name: 'All' }).click()
  await expect(page).toHaveURL(/\/parts\?stock=BELOW_MIN_OR_NEGATIVE$/)
})

test('a below-minimum row opens the part', async ({ page }) => {
  await page.goto('/')
  const card = page.getByRole('region', { name: 'Below minimum / negative' })
  await card.getByRole('link', { name: 'GS-3516' }).click()
  await expect(page).toHaveURL(/\/parts\/2775$/)
})

test('the top-bar depot scopes the dashboard', async ({ page }) => {
  await page.goto('/')
  await expect(page.getByRole('group', { name: 'Cash on hand' })).toContainText('64,765.50')
  await page.getByRole('group', { name: 'Depot' }).getByRole('button', { name: 'Depot 2' }).click()
  await expect(page).toHaveURL(/depot=2/)
  await expect(page.getByRole('group', { name: 'Cash on hand' })).toContainText('17,950.00')
  const card = page.getByRole('region', { name: 'Below minimum / negative' })
  await expect(card).toContainText('117 below minimum · 10 negative')
  await expect(card.getByRole('link', { name: 'All' })).toHaveAttribute(
    'href',
    '/parts?stock=BELOW_MIN_OR_NEGATIVE&depot=2',
  )
})

test('cash book marks the carried opening and restarts the balance there', async ({ page }) => {
  await page.goto('/cash-book?from=2025-12-01&to=2026-01-31&depot=1')
  const row = page.locator('.fk-grid .ag-row.fk-row-reset').first()
  await expect(row).toContainText('CARRIED')
  await expect(row).toContainText('21,480.00')
  await expect(page.getByRole('group', { name: 'In', exact: true })).toContainText('+4,000.00')
  await expect(page.getByRole('group', { name: 'Closing' })).toContainText('21,480.00')
  expect(await clipped(page)).toEqual([])
})
