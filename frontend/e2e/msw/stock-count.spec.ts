import type { Page } from '@playwright/test'
import { test, expect, grid, pinnedBottom, clipped, cutControls } from './fixtures'

const row = (page: Page, no: string) =>
  grid(page).locator('.ag-center-cols-container .ag-row', { hasText: no })
const counted = (page: Page, no: string) => row(page, no).locator('[col-id="countedQty"]')

async function shelfA0(page: Page) {
  await page.goto('/stock-counts/2')
  await page.getByLabel('Filter Shelf').fill('A-0')
  await expect(pinnedBottom(page)).toContainText(/7 of 1,\d{3} · shelf A-0\*/)
}

test('typing counts, Enter moves down, Esc undoes, saves survive leaving the page', async ({
  page,
}) => {
  await shelfA0(page)
  await expect(pinnedBottom(page)).toContainText('+270.00')
  await counted(page, 'GS-4410').click()
  await page.keyboard.type('17')
  await expect(page.getByLabel('Counted GS-4410')).toHaveValue('17')
  await page.keyboard.press('Enter')
  await expect(row(page, 'GS-4410').locator('[col-id="difference"]')).toHaveText('−1')
  await expect(counted(page, 'GS-4410').getByRole('img', { name: 'Saved' })).toBeVisible()
  await expect(counted(page, 'GS-4418')).toHaveClass(/ag-cell-focus/)

  await page.keyboard.type('4')
  await page.keyboard.press('Escape')
  await expect(counted(page, 'GS-4418')).toHaveText('—')

  await page.keyboard.type('9')
  await page.keyboard.press('Enter')
  await expect(row(page, 'GS-4418').locator('[col-id="difference"]')).toHaveText('0')
  await expect(pinnedBottom(page)).toContainText('52')
  await expect(page.getByText(/^414 \/ 1,\d{3} lines$/)).toBeVisible()

  await page.getByRole('link', { name: 'Stock counts' }).click()
  await page.getByRole('link', { name: 'SAY-2026-1-000002' }).click()
  await page.getByLabel('Filter Shelf').fill('A-0')
  await expect(counted(page, 'GS-4410')).toContainText('17')
  await expect(counted(page, 'GS-4418')).toContainText('9')
})

test('a line counted by someone else in between asks which count stays', async ({ page }) => {
  await shelfA0(page)
  await counted(page, 'BE-0620').click()
  await page.keyboard.type('32')
  await page.keyboard.press('Enter')
  const dialog = page.getByRole('alertdialog')
  await expect(dialog).toContainText('a.kaya counted 31 for BE-0620')
  await expect(counted(page, 'BE-0620')).toContainText('31')
  await dialog.getByRole('button', { name: 'Keep 32' }).click()
  await expect(counted(page, 'BE-0620')).toContainText('32')
  await expect(row(page, 'BE-0620')).toContainText('demo')
})

test('differences only, post with uncounted as zero, then read-only', async ({ page }) => {
  await page.goto('/stock-counts/2')
  await page.getByRole('button', { name: 'Differences only' }).click()
  await expect(pinnedBottom(page)).not.toContainText(/^1,\d{3} of/)
  await page.getByRole('button', { name: 'Post differences' }).click()
  const dialog = page.getByRole('dialog')
  await expect(dialog.getByRole('group', { name: 'Counted lines' })).toContainText('412')
  await dialog.getByRole('button', { name: 'Count as 0' }).click()
  await dialog.getByRole('button', { name: 'Post', exact: true }).click()
  await expect(dialog).toContainText(/SAY-2026-1-000002 posted · \d+ adjustments/)
  await dialog.getByRole('button', { name: 'Done' }).click()
  await expect(page.getByText('FINALIZED')).toBeVisible()
  await expect(page.getByRole('button', { name: 'Post differences' })).toHaveCount(0)
  await counted(page, 'A-').first().dblclick()
  await expect(page.locator('.ag-cell-inline-editing')).toHaveCount(0)
})

test('start a count for depot 2 and print its blind sheet', async ({ page }) => {
  await page.goto('/stock-counts')
  await page.getByRole('button', { name: 'Start count' }).click()
  const dialog = page.getByRole('dialog')
  await dialog.getByLabel('Depot').selectOption({ label: 'Depot 2' })
  await dialog.getByRole('button', { name: 'Start' }).click()
  await expect(page.getByRole('heading', { name: /SAY-2026-2-000001/ })).toBeVisible()
  await expect(page.getByText(/^0 \/ [\d,]+ lines$/)).toBeVisible()
  await page.getByRole('button', { name: 'Count sheet PDF' }).click()
  const pdf = page.waitForEvent('download')
  await page.getByRole('menuitem', { name: 'Blind sheet' }).click()
  expect((await pdf).suggestedFilename()).toBe('SAY-2026-2-000001-blind.pdf')
})

for (const lang of ['en', 'tr'] as const) {
  test.describe(lang, () => {
    test.use({ lang })
    test('count lines and headers are never cut', async ({ page }) => {
      await page.goto('/stock-counts/2')
      await expect(grid(page).locator('.ag-row').first()).toBeVisible()
      expect(await clipped(page, ['shelf', 'stockNo', 'countedBy'])).toEqual([])
      await page.getByLabel(lang === 'en' ? 'Filter Shelf' : 'Raf filtresi').fill('A-0')
      await expect(pinnedBottom(page)).toContainText('A-0*')
      expect(await clipped(page, ['shelf', 'stockNo', 'countedBy'])).toEqual([])
      const cutHeaders = await page.evaluate(() =>
        [...document.querySelectorAll<HTMLElement>('.fk-grid .ag-header-cell-text')]
          .filter((h) => h.scrollWidth > h.clientWidth + 1)
          .map((h) => h.textContent),
      )
      expect(cutHeaders).toEqual([])
      const header = page.locator('h1').locator('..')
      const box = await header.boundingBox()
      expect(box!.height).toBeLessThan(44)
    })
  })
}

test('a negative or malformed count is refused, not saved', async ({ page }) => {
  const saves: string[] = []
  page.on('request', (r) => {
    if (r.method() === 'PATCH') saves.push(r.postData() ?? '')
  })
  await shelfA0(page)
  await counted(page, 'GS-4410').click()
  await page.keyboard.type('-3')
  await page.keyboard.press('Enter')
  await expect(
    counted(page, 'GS-4410').getByRole('img', { name: /Enter a quantity of 0 or more/ }),
  ).toBeVisible()
  await counted(page, 'GS-4418').click()
  await page.keyboard.type('2,5')
  await page.keyboard.press('Enter')
  await expect(
    counted(page, 'GS-4418').getByRole('img', { name: /Enter a quantity of 0 or more/ }),
  ).toBeVisible()
  expect(saves).toEqual([])
})

test('Esc in a count cell keeps the focus on the cell', async ({ page }) => {
  await shelfA0(page)
  await counted(page, 'GS-4410').dblclick()
  await expect(page.getByLabel('Counted GS-4410')).toBeFocused()
  await page.keyboard.press('Escape')
  await expect(counted(page, 'GS-4410')).toBeFocused()
})

for (const lang of ['en', 'tr'] as const) {
  test.describe(`${lang} 1152`, () => {
    test.use({ lang, viewport: { width: 1152, height: 800 } })
    test('count header actions fit at 1152', async ({ page }) => {
      await page.goto('/stock-counts/2')
      await expect(grid(page).locator('.ag-row').first()).toBeVisible()
      expect(await cutControls(page)).toEqual([])
    })
  })
}
