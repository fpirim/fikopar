import { test, expect, grid, pinnedBottom, clipped, cutControls } from './fixtures'

const rows = (page: import('@playwright/test').Page) =>
  grid(page).locator('.ag-center-cols-container .ag-row')

test('parts grid filters by code and stock state and opens the part card', async ({ page }) => {
  await page.goto('/parts')
  await expect(pinnedBottom(page)).toContainText('1,988 of 1,988')
  await page.getByLabel('Filter OEM / alt.').fill('3774612')
  await expect(pinnedBottom(page)).toContainText('1 of 1,988 · 1 filter')
  await page.getByRole('button', { name: 'Clear filters' }).click()
  await page
    .getByRole('group', { name: 'Stock' })
    .getByRole('button', { name: 'Negative', exact: true })
    .click()
  await expect(page).toHaveURL(/stock=NEGATIVE/)
  await expect(grid(page)).toContainText('BR-2215')
  await expect(grid(page).getByText('NEGATIVE').first()).toBeVisible()
  await page.getByRole('link', { name: 'BR-2215' }).click()
  await expect(page.getByRole('heading', { name: 'Brake lining, rear' })).toBeVisible()
  await expect(page.getByRole('table', { name: 'Stock per depot' })).toContainText('−2')
})

test('the part card shows the last purchase cost and its invoice', async ({ page }) => {
  await page.goto('/parts/1001')
  const box = page.getByRole('region', { name: 'Last purchase cost' })
  await expect(box).toContainText('8,900.00')
  await expect(box).toContainText('26.09.2026')
  await expect(box).toContainText('AF-2026-1-000201')
  expect(await cutControls(page)).toEqual([])
  await page.goto('/parts/1009')
  await expect(page.getByRole('region', { name: 'Last purchase cost' })).toContainText(
    'No posted purchase invoice',
  )
})

test('depot switch scopes stock and shows min, max and shelf', async ({ page }) => {
  await page.goto('/parts')
  await expect(grid(page).getByRole('columnheader', { name: 'Shelf' })).toHaveCount(0)
  await page.getByRole('group', { name: 'Depot' }).getByRole('button', { name: 'Depot 1' }).click()
  await expect(grid(page).getByRole('columnheader', { name: 'Shelf' })).toBeVisible()
  await page.getByLabel('Filter Part').fill('HP-3774')
  await expect(rows(page)).toHaveCount(1)
  await expect(rows(page).first().locator('[col-id="shelf"]')).toHaveText('A-04-2')
  await expect(rows(page).first().locator('[col-id="onHand"]')).toHaveText('10')
})

// Grid header labels and plain table cells show their whole text.
const cutText = (page: import('@playwright/test').Page) =>
  page
    .locator('table th, table td, .ag-header-cell-text')
    .evaluateAll((els) =>
      els.filter((e) => e.scrollWidth > e.clientWidth + 1).map((e) => e.textContent),
    )

for (const lang of ['en', 'tr'] as const) {
  test.describe(lang, () => {
    test.use({ lang })
    test(`nothing is cut on the parts grid and the part card (${lang})`, async ({ page }) => {
      await page.goto('/parts?depot=1&stock=BELOW_MIN_OR_NEGATIVE')
      await expect(rows(page).first()).toBeVisible()
      expect(await clipped(page, ['stockNo', 'codes', 'shelf', 'catalogCode'])).toEqual([])
      expect(await cutText(page)).toEqual([])
      await page.goto('/parts/1001')
      await expect(grid(page)).toContainText('SF-2026-1-000418')
      expect(await clipped(page, ['label', 'depotId', 'sourceNo'])).toEqual([])
      expect(await cutText(page)).toEqual([])
    })
  })
}

test('transfer into negative stock warns and lands in the movements', async ({ page }) => {
  await page.goto('/parts/1001?depot=1')
  await expect(grid(page)).toContainText('SF-2026-1-000418')
  await page.getByRole('button', { name: 'Transfer' }).click()
  const dialog = page.getByRole('dialog')
  await expect(dialog.getByLabel('From depot')).toHaveValue('1')
  await expect(dialog.getByLabel('To depot')).toHaveValue('2')
  await dialog.getByLabel('Quantity').fill('12')
  await expect(
    dialog.getByText('Only 9 available in depot 1; on hand goes negative.'),
  ).toBeVisible()
  await dialog.getByLabel('Quantity').press('Enter')
  await expect(dialog).toBeHidden()
  const alert = page.getByRole('alert')
  await expect(alert).toContainText('Negative stock after TRF-2026-1-000052')
  await expect(alert).toContainText('Depot 1 · HP-3774: on hand −2')
  await expect(rows(page).first()).toContainText('TRF-2026-1-000052')
  await expect(rows(page).first().locator('[col-id="runningOnHand"]')).toHaveText('−2')
  await expect(page.getByRole('group', { name: 'Closing' })).toContainText('−2')
})

test('a future price is added from the keyboard and marked', async ({ page }) => {
  await page.goto('/parts/1001')
  const prices = page.getByRole('table', { name: 'List prices' })
  await expect(prices).toContainText('12,400.00')
  await page.getByRole('button', { name: 'New price' }).click()
  const dialog = page.getByRole('dialog')
  await dialog.getByLabel('Price').fill('13,100')
  await page.keyboard.press('Tab')
  await page.keyboard.press('ControlOrMeta+a')
  await page.keyboard.type('15.10.2026')
  // Enter commits the date and saves; the focus goes back to New price.
  await page.keyboard.press('Enter')
  await expect(dialog).toBeHidden()
  await expect(page.getByRole('button', { name: 'New price' })).toBeFocused()
  await expect(prices.locator('tbody tr').first()).toContainText('15.10.2026')
  await expect(prices.locator('tbody tr').first()).toContainText('FUTURE')
  await expect(prices.locator('tbody tr').first()).toContainText('+5.6%')
  await expect(page.getByText('Next: 13,100.00 from 15.10.2026')).toBeVisible()
})

test('depot settings saved elsewhere first ask for a reload', async ({ page }) => {
  await page.goto('/parts/1001')
  const table = page.getByRole('table', { name: 'Stock per depot' })
  await expect(table).toContainText('A-04-2')
  await page.getByRole('button', { name: 'Edit depot 1' }).click()
  const dialog = page.getByRole('dialog')
  await dialog.getByLabel('Min').fill('7')
  // Another user saves version 1 first.
  const status = await page.evaluate(async () => {
    const res = await fetch('/api/orgs/1/catalog/parts/1001/depots/1', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'If-Match': '"1"',
        Authorization: `Bearer ${localStorage.getItem('token')}`,
      },
      body: JSON.stringify({ minQty: '9', maxQty: '20', shelf: 'A-04-3' }),
    })
    return res.status
  })
  expect(status).toBe(200)
  await dialog.getByRole('button', { name: 'Save' }).click()
  const alert = page.getByRole('alertdialog')
  await expect(alert).toContainText('Settings changed')
  await alert.getByRole('button', { name: 'Reload' }).click()
  await expect(alert).toBeHidden()
  await expect(table).toContainText('A-04-3')
})

test('movements filter by type', async ({ page }) => {
  await page.goto('/parts/1001')
  await expect(grid(page)).toContainText('SF-2026-1-000418')
  await page.getByLabel('Filter Type').selectOption('TRANSFER')
  await expect(rows(page)).toHaveCount(2)
  await expect(rows(page).first()).toContainText('1 → 2')
})

test('all depots shows on hand per depot side by side, nothing cut', async ({ page }) => {
  await page.goto('/parts')
  await page.getByLabel('Filter Part').fill('HP-3774')
  await expect(rows(page)).toHaveCount(1)
  await expect(grid(page).getByRole('columnheader', { name: 'Dp 1' })).toBeVisible()
  await expect(rows(page).first().locator('[col-id="depot.1"]')).toHaveText('10')
  await expect(rows(page).first().locator('[col-id="depot.2"]')).toHaveText('4')
  expect(await clipped(page, ['stockNo', 'codes', 'depot.1', 'depot.2'])).toEqual([])
  expect(await cutText(page)).toEqual([])
})

test('movements filter by the Out column on the server', async ({ page }) => {
  await page.goto('/parts/1001')
  await expect(grid(page)).toContainText('SF-2026-1-000418')
  await page.getByLabel('Filter Out').fill('>= 2')
  await expect
    .poll(async () => {
      const outs = await rows(page).locator('[col-id="qtyOut"]').allTextContents()
      return outs.length > 0 && outs.every((v) => Number(v) >= 2)
    })
    .toBe(true)
})

test('the equivalents card links both parts', async ({ page }) => {
  await page.goto('/parts/1005')
  const card = page.getByRole('region', { name: 'Equivalents' })
  await expect(card).toContainText('Clutch disc 12"')
  await card.getByRole('link', { name: 'CL-1210' }).click()
  await expect(page.getByRole('heading', { name: 'Clutch disc 12"' })).toBeVisible()
  await expect(
    page.getByRole('region', { name: 'Equivalents' }).getByRole('link', { name: 'CL-1150' }),
  ).toBeVisible()
})

for (const lang of ['en', 'tr'] as const) {
  test.describe(`${lang} 1152`, () => {
    test.use({ lang, viewport: { width: 1152, height: 800 } })
    test('parts toolbar and part card fit at 1152', async ({ page }) => {
      await page.goto('/parts')
      await expect(rows(page).first()).toBeVisible()
      expect(await cutControls(page)).toEqual([])
      await page.goto('/parts/1001')
      await expect(grid(page)).toContainText('SF-2026-1-000418')
      expect(await cutControls(page)).toEqual([])
    })
  })
}

test('a large price change fits its column', async ({ page }) => {
  await page.goto('/parts/1001')
  const prices = page.getByRole('table', { name: 'List prices' })
  await expect(prices).toContainText('12,400.00')
  await page.getByRole('button', { name: 'New price' }).click()
  const dialog = page.getByRole('dialog')
  await dialog.getByLabel('Price').fill('987,654,321')
  await page.keyboard.press('Tab')
  await page.keyboard.press('ControlOrMeta+a')
  await page.keyboard.type('15.10.2026')
  // Enter commits the date and saves; the focus goes back to New price.
  await page.keyboard.press('Enter')
  await expect(dialog).toBeHidden()
  await expect(prices.locator('tbody tr').first()).toContainText('987,654,321.00')
  expect(await cutText(page)).toEqual([])
  const room = await prices.evaluate((t) => {
    const panel = t.closest('section')!
    return t.scrollWidth <= panel.clientWidth + 1
  })
  expect(room).toBe(true)
})
