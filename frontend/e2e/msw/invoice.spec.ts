import type { Page } from '@playwright/test'
import { test, expect, clipped, cutControls } from './fixtures'

const lines = (page: Page) => page.locator('.fk-doc-lines')
const cell = (page: Page, row: number, col: string) =>
  lines(page).locator(`.ag-center-cols-container .ag-row[row-index="${row}"] [col-id="${col}"]`)
const pinned = (page: Page) => lines(page).locator('.ag-floating-bottom')
// In-app navigation; a reload would restart the mock data.
const go = (page: Page, path: string) =>
  page.evaluate((p) => {
    history.pushState({}, '', p)
    dispatchEvent(new PopStateEvent('popstate'))
  }, path)
// Grids whose columns run past their width (a horizontal scrollbar hides the right-hand amounts).
const overflowing = (page: Page) =>
  page.evaluate(() =>
    [...document.querySelectorAll<HTMLElement>('.fk-grid .ag-center-cols-viewport')]
      .filter((v) => v.scrollWidth > v.clientWidth + 1)
      .map((v) => v.closest('.fk-grid')?.className ?? ''),
  )
const totals = (page: Page) => page.getByRole('region', { name: 'Totals' })

async function pickAccount(page: Page, text: string) {
  const account = page.getByRole('combobox', { name: 'Account' })
  await account.fill(text)
  await expect(
    page.getByRole('listbox', { name: 'Account' }).getByRole('option').first(),
  ).toBeVisible()
  await account.press('Enter')
  await expect(account).toHaveValue(/120\.\d+ · /)
}

test('an invoice typed from the keyboard: part popup, quantity, discounts, Tab to a new line, post', async ({
  page,
}) => {
  // A new document is dated today; the due date follows the card's 30 days from it.
  await page.clock.setFixedTime(new Date('2026-09-29T10:00:00+03:00'))
  await page.goto('/invoices/new?type=SALES_INVOICE')
  await expect(page.getByRole('heading', { level: 1 })).toHaveText('SF-2026-1-000419')
  await pickAccount(page, 'Yılmaz')
  await expect(page.getByLabel('Due')).toHaveValue('29.10.2026')
  await expect(page.getByLabel('Salesperson')).toHaveValue('7')

  await page.keyboard.press('Insert')
  const search = page.getByRole('combobox', { name: 'Part search' })
  await expect(search).toBeFocused()
  await page.keyboard.type('pump 37')
  const results = page.getByRole('listbox', { name: 'Catalog search results' })
  await expect(results.getByRole('option').first()).toBeVisible()
  await page.keyboard.press('ArrowDown')
  while (
    !(await results.locator('[data-selected=true]').getAttribute('data-stock-no'))?.startsWith(
      'HP-3774',
    )
  ) {
    await page.keyboard.press('ArrowDown')
  }
  await page.keyboard.press('Enter')

  // Picking fills price, VAT and the card's discounts, then edits the quantity.
  await expect(cell(page, 0, 'part')).toHaveText('HP-3774')
  await expect(cell(page, 0, 'price')).toHaveText('12,400.00')
  await expect(page.getByRole('textbox', { name: 'Qty, line 1' })).toBeFocused()
  await page.keyboard.type('2')
  await page.keyboard.press('Tab')
  await expect(page.getByRole('textbox', { name: 'Price, line 1' })).toBeFocused()
  await page.keyboard.press('Tab')
  await page.keyboard.type('10')
  await page.keyboard.press('Tab')
  await page.keyboard.type('0')
  await page.keyboard.press('Tab')
  await expect(page.getByRole('textbox', { name: 'VAT, line 1' })).toBeFocused()
  // Tab past the last cell opens line 2 in the part search.
  await page.keyboard.press('Tab')
  await expect(page.getByRole('combobox', { name: 'Part search' })).toBeFocused()
  await page.keyboard.type('CL-1150')
  await expect(results.locator('[data-stock-no="CL-1150"]')).toBeVisible()
  await page.keyboard.press('Enter')
  await expect(page.getByRole('textbox', { name: 'Qty, line 2' })).toBeFocused()
  await page.keyboard.type('4')
  await page.keyboard.press('Tab')
  await page.keyboard.press('Tab')
  await page.keyboard.type('5')
  await page.keyboard.press('Tab')
  await page.keyboard.type('0')
  await page.keyboard.press('Enter')

  await expect(cell(page, 0, 'net')).toHaveText('22,320.00')
  await expect(cell(page, 1, 'net')).toHaveText('13,870.00')
  await expect(pinned(page)).toContainText('2 lines · 6 units')
  await expect(pinned(page)).toContainText('36,190.00')
  await expect(totals(page)).toContainText('Subtotal36,190.00')
  await expect(totals(page)).toContainText('VAT 20%7,238.00')
  await expect(totals(page)).toContainText('Total43,428.00')
  await expect(page.getByRole('region', { name: 'On post' })).toContainText('+43,428.00 Dr')
  await expect(page.getByRole('region', { name: 'On post' })).toContainText('−6')
  // Short stock shows while typing, never blocks.
  await expect(page.getByRole('list', { name: 'Warnings' })).toContainText(
    'Line 2 · CL-1150: 4 requested, 3 in Depot 1',
  )

  await page.getByRole('button', { name: 'Post' }).click()
  await expect(page.getByText('SF-2026-1-000419 posted')).toBeVisible()
  await expect(page.getByText('POSTED', { exact: true })).toBeVisible()
  await expect(page).toHaveURL(/\/invoices\/\d+$/)
  await expect(page.getByRole('heading', { level: 1 })).toHaveText('SF-2026-1-000419')
  await expect(page.getByRole('button', { name: 'Cancel document' })).toBeVisible()
})

test('Esc in the part search cancels the edit and keeps focus in the cell; Del removes a line', async ({
  page,
}) => {
  await page.goto('/invoices/1001')
  await expect(cell(page, 3, 'part')).toHaveText('BR-2210')
  await cell(page, 1, 'qty').click()
  await page.keyboard.press('F2')
  const search = page.getByRole('combobox', { name: 'Part search' })
  await expect(search).toBeFocused()
  await page.keyboard.type('zz')
  await page.keyboard.press('Escape')
  await expect(search).toBeHidden()
  await expect(cell(page, 1, 'part')).toHaveText('CL-1150')
  await expect(cell(page, 1, 'part')).toBeFocused()
  await page.keyboard.press('Delete')
  await expect(pinned(page)).toContainText('3 lines · 28 units')
  await expect(page.getByRole('button', { name: 'Save draft' })).toBeEnabled()
})

test('a draft saves with If-Match and a reload shows it; delete asks first', async ({ page }) => {
  await page.goto('/invoices/1001')
  await cell(page, 2, 'qty').dblclick()
  await page.keyboard.press('ControlOrMeta+A')
  await page.keyboard.type('10')
  await page.keyboard.press('Enter')
  await expect(cell(page, 2, 'net')).toHaveText('2,650.00')
  const put = page.waitForRequest((r) => r.method() === 'PUT')
  await page.getByRole('button', { name: 'Save draft' }).click()
  expect((await put).headers()['if-match']).toBe('"3"')
  await expect(page.getByRole('button', { name: 'Save draft' })).toBeDisabled()
  await expect(page.getByText(/^Stock held until /)).toBeVisible()
  await go(page, '/invoices')
  await go(page, '/invoices/1001')
  await expect(cell(page, 2, 'qty')).toHaveText('10')

  await page.getByRole('button', { name: 'Delete' }).first().click()
  await expect(page.getByText('Delete this draft?')).toBeVisible()
  await page.getByRole('button', { name: 'Delete' }).last().click()
  await expect(page).toHaveURL(/\/invoices$/)
  await expect(page.getByText('Draft deleted')).toBeVisible()
})

test.describe('admin', () => {
  test.use({ admin: true })
  test('posting over the credit limit shows the numbers; an admin posts with a reason', async ({
    page,
  }) => {
    await page.goto('/invoices/1002')
    await page.getByRole('button', { name: 'Post' }).click()
    const dialog = page.getByRole('alertdialog')
    await expect(dialog).toContainText('Credit limit exceeded')
    await expect(dialog).toContainText('250,000.00')
    await expect(dialog).toContainText('36,315.00')
    await expect(dialog).toContainText('305,280.00')
    await expect(dialog).toContainText('91,595.00')
    await dialog.getByRole('button', { name: 'Post anyway' }).click()
    await expect(dialog).toContainText('At least 3 characters')
    await dialog.getByLabel('Reason for posting over the limit').fill('Paid by bank transfer today')
    await dialog.getByRole('button', { name: 'Post anyway' }).click()
    await expect(page.getByText('SF-2026-1-000419 posted')).toBeVisible()
    await expect(page.getByText('OVER LIMIT')).toBeVisible()
    await expect(
      page.getByText(/Posted over the credit limit by Demo: Paid by bank transfer today/),
    ).toBeVisible()
  })
})

test('a user without admin rights cannot override the credit limit', async ({ page }) => {
  await page.goto('/invoices/1002')
  await page.getByRole('button', { name: 'Post' }).click()
  const dialog = page.getByRole('alertdialog')
  await expect(dialog).toContainText('An admin can post it with a reason')
  await expect(dialog.getByRole('button', { name: 'Post anyway' })).toHaveCount(0)
  await dialog.getByRole('button', { name: 'Close' }).first().click()
  await expect(page.getByText('DRAFT', { exact: true })).toBeVisible()
})

test('cancel asks a reason and shows the reversal; returns block the original', async ({
  page,
}) => {
  await page.goto('/invoices/391')
  await page.getByRole('button', { name: 'Cancel document' }).click()
  await page.getByLabel('Reason').fill('Duplicate')
  await page.getByRole('dialog').getByRole('button', { name: 'Cancel document' }).click()
  await expect(
    page.getByRole('dialog').getByRole('link', { name: 'SI-2026-1-000012' }),
  ).toBeVisible()
  await expect(
    page.getByRole('dialog').getByRole('link', { name: 'FF-2026-1-000007' }),
  ).toBeVisible()
  await expect(page.getByRole('dialog')).toContainText('cancel them first')
  await page.goto('/invoices/415')
  await expect(page.getByText('Cancelled 27.09.2026 · Wrong customer')).toBeVisible()
  await expect(page.getByRole('link', { name: 'Reversal entry 4830' })).toBeVisible()

  await page.goto('/invoices/201')
  await page.getByRole('button', { name: 'Cancel document' }).click()
  await page.getByLabel('Reason').fill('Supplier error')
  await page.getByRole('dialog').getByRole('button', { name: 'Cancel document' }).click()
  await expect(page.getByText('AF-2026-1-000201 cancelled')).toBeVisible()
  await expect(page.getByText(/^Cancelled 29\.09\.2026 · Supplier error/)).toBeVisible()
  await expect(page.getByRole('link', { name: /^Reversal entry/ })).toBeVisible()
})

test('a waybill is billed line for line; the bill moves no stock', async ({ page }) => {
  await page.goto('/invoices/52')
  await page.getByRole('link', { name: 'Bill this waybill' }).click()
  await expect(page.getByLabel('Document type')).toHaveValue('SALES_INVOICE')
  await expect(page.getByLabel('Waybill ref.')).toHaveValue('52')
  await expect(cell(page, 0, 'part')).toHaveText('HP-3780')
  await expect(cell(page, 1, 'qty')).toHaveText('10')
  await expect(page.getByRole('region', { name: 'On post' })).toContainText('Stock—')
  await page.getByRole('button', { name: 'Save draft' }).click()
  await expect(page).toHaveURL(/\/invoices\/\d+$/)
  await expect(page.getByLabel('Waybill ref.')).toHaveValue('52')
  await page.getByRole('button', { name: 'Post' }).click()
  await expect(page.getByText(/SF-2026-1-000419 posted/)).toBeVisible()
  await expect(page.getByRole('link', { name: 'Bills SIR-2026-1-000052' })).toBeVisible()
  await page.getByRole('link', { name: 'Bills SIR-2026-1-000052' }).click()
  await expect(page.getByRole('link', { name: 'Bill this waybill' })).toHaveCount(0)
  await expect(page.getByRole('link', { name: 'Billed — open the invoice' })).toBeVisible()
})

test('a return starts from its invoice with what is left and refuses more', async ({ page }) => {
  await page.goto('/invoices/391')
  await page.getByRole('link', { name: 'Create return' }).click()
  await expect(page.getByLabel('Document type')).toHaveValue('SALES_RETURN')
  await expect(page.getByLabel('Original')).toHaveValue('391')
  // WP-3715: 4 sold, 1 already returned.
  await expect(cell(page, 1, 'part')).toHaveText('WP-3715')
  await expect(cell(page, 1, 'returnable')).toHaveText('3')
  await expect(cell(page, 1, 'qty')).toHaveText('3')
  await cell(page, 1, 'qty').dblclick()
  await page.keyboard.press('ControlOrMeta+A')
  await page.keyboard.type('4')
  await page.keyboard.press('Enter')
  await expect(page.getByText('Line 2: at most 3 can be returned')).toBeVisible()
  await expect(cell(page, 1, 'qty')).toHaveText('3')
  await expect(page.getByRole('region', { name: 'On post' })).toContainText('Cr')
  await expect(page.getByRole('region', { name: 'On post' })).toContainText('+13')
})

test('a price difference is amount only, both ways; a decrease over the invoice is refused', async ({
  page,
}) => {
  await page.goto('/invoices/108')
  await page.getByRole('link', { name: 'Price difference' }).click()
  await expect(page.getByLabel('Document type')).toHaveValue('PRICE_DIFFERENCE')
  await expect(page.getByRole('columnheader', { name: 'Amount' })).toBeVisible()
  await expect(page.getByRole('columnheader', { name: 'Qty' })).toHaveCount(0)
  await expect(cell(page, 0, 'origin')).toContainText('HP-3774')
  await cell(page, 0, 'amount').dblclick()
  await page.keyboard.type('500')
  await page.keyboard.press('Enter')
  await expect(page.getByRole('region', { name: 'On post' })).toContainText('+600.00 Dr')
  await page.getByRole('button', { name: 'Decrease' }).click()
  await expect(page.getByRole('region', { name: 'On post' })).toContainText('−600.00 Cr')
  await cell(page, 0, 'amount').dblclick()
  await page.keyboard.press('ControlOrMeta+A')
  await page.keyboard.type('40000')
  await page.keyboard.press('Enter')
  await page.getByRole('button', { name: 'Post' }).click()
  await expect(page.getByRole('alert')).toContainText(
    'The decrease is larger than what is left of the invoice',
  )
})

for (const lang of ['en', 'tr'] as const) {
  for (const width of [1440, 1152]) {
    test.describe(`${lang} ${width}`, () => {
      test.use({ lang, viewport: { width, height: 900 } })
      test('editor and list keep numbers, dates and buttons whole', async ({ page }) => {
        await page.goto('/invoices/1001')
        await expect(cell(page, 3, 'part')).toHaveText('BR-2210')
        expect(await clipped(page, ['part', 'name'])).toEqual([])
        expect(await cutControls(page)).toEqual([])
        expect(await overflowing(page)).toEqual([])
        await page.goto('/invoices/8')
        await expect(cell(page, 0, 'description')).toContainText('Volume rebate')
        expect(await clipped(page)).toEqual([])
        expect(await overflowing(page)).toEqual([])
        await page.goto('/invoices')
        await expect(page.locator('.fk-grid .ag-row').first()).toBeVisible()
        expect(await clipped(page, ['documentNo', 'type', 'status'])).toEqual([])
        expect(await cutControls(page)).toEqual([])
        expect(await overflowing(page)).toEqual([])
      })
    })
  }
}
