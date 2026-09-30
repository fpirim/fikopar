import type { Page } from '@playwright/test'
import { test, expect, seeded, V14, V15 } from './fixtures'
import { clipped, cutControls } from '../clipped'

// F3 flows against the real API. Every test is @v14: run after the F3 backend and frontend merge.
const doc = (key: string) => seeded().documents[key]
const cell = (page: Page, row: number, col: string) =>
  page.locator(
    `.fk-doc-lines .ag-center-cols-container .ag-row[row-index="${row}"] [col-id="${col}"]`,
  )
const onPost = (page: Page) => page.getByRole('region', { name: /^(On post|Posted|Reversed)$/ })
async function pickAccount(page: Page, text: string) {
  const account = page.getByRole('combobox', { name: 'Account' })
  await account.fill(text)
  await page.getByRole('listbox', { name: 'Account' }).getByRole('option').first().click()
  await expect(account).toHaveValue(new RegExp(text))
}

test.describe('documents', () => {
  test(
    'an invoice typed from the keyboard posts with a number and moves stock',
    V14,
    async ({ page }) => {
      await page.goto('/invoices/new?type=SALES_INVOICE')
      const account = page.getByRole('combobox', { name: 'Account' })
      await account.fill('Beyşehir')
      await page.getByRole('listbox', { name: 'Account' }).getByRole('option').first().click()
      await expect(account).toHaveValue(/Beyşehir Tarım/)
      await page.keyboard.press('Insert')
      await expect(page.getByRole('combobox', { name: 'Part search' })).toBeFocused()
      await page.keyboard.type('GS-4410')
      const results = page.getByRole('listbox', { name: 'Catalog search results' })
      await expect(results.locator('[data-stock-no="GS-4410"]')).toBeVisible()
      await page.keyboard.press('Enter')
      await expect(page.getByRole('textbox', { name: 'Qty, line 1' })).toBeFocused()
      await page.keyboard.type('2')
      await page.keyboard.press('Enter')
      await expect(onPost(page)).toContainText('−2')
      await page.getByRole('button', { name: 'Save draft' }).click()
      await expect(page).toHaveURL(/\/invoices\/\d+$/)
      await expect(page.getByText(/^Stock held until /)).toBeVisible()
      await page.getByRole('button', { name: 'Post' }).click()
      await expect(page.getByText(/^SF-2026-\d+-\d{6} posted$/)).toBeVisible()
      await expect(page.getByText('POSTED', { exact: true })).toBeVisible()
      await expect(page.getByRole('link', { name: /^Entry \d+$/ })).toBeVisible({ timeout: 15_000 })
    },
  )

  test('the list shows every type with totals and opens a document', V14, async ({ page }) => {
    await page.goto('/invoices')
    const grid = page.locator('.fk-grid').first()
    await expect(grid).toContainText('SALES WAYBILL')
    await expect(grid).toContainText('PRICE DIFF. +')
    await expect(grid).toContainText('PRICE DIFF. −')
    await expect(grid).toContainText('SALES RETURN')
    await expect(grid.locator('.ag-floating-bottom')).toContainText('cancelled left out')
    expect(await clipped(page, ['documentNo', 'type', 'status'])).toEqual([])
    expect(await cutControls(page)).toEqual([])
    await page.getByRole('button', { name: 'DRAFT' }).click()
    await expect(grid).toContainText('Öztürk Tarım')
    await grid.getByRole('link', { name: 'Draft' }).first().click()
    await expect(page).toHaveURL(/\/invoices\/\d+$/)
    await expect(page.getByRole('button', { name: 'Save draft' })).toBeVisible()
    await expect(page.getByText('DRAFT', { exact: true })).toBeVisible()
  })

  test(
    'posting over the credit limit shows the numbers; the admin posts with a reason',
    V14,
    async ({ page }) => {
      await page.goto(`/invoices/${doc('draft')}`)
      await page.getByRole('button', { name: 'Post' }).click()
      const dialog = page.getByRole('alertdialog')
      await expect(dialog).toContainText('250,000.00')
      await expect(dialog).toContainText('Over the limit by')
      await dialog.getByLabel('Reason for posting over the limit').fill('Guaranteed by the owner')
      await dialog.getByRole('button', { name: 'Post anyway' }).click()
      await expect(page.getByText('OVER LIMIT')).toBeVisible()
      await expect(
        page.getByText(/Posted over the credit limit by .*: Guaranteed by the owner/),
      ).toBeVisible()
    },
  )

  test(
    'a return starts from the sale with what is left and cannot take more',
    V14,
    async ({ page }) => {
      await page.goto(`/invoices/${doc('sale')}`)
      await page.getByRole('link', { name: 'Create return' }).click()
      await expect(page.getByLabel('Original')).toHaveValue(String(doc('sale')))
      // HP-3702: 2 sold, 1 returned by the seed.
      await expect(cell(page, 0, 'returnable')).toHaveText('1')
      await cell(page, 0, 'qty').dblclick()
      await page.keyboard.press('ControlOrMeta+A')
      await page.keyboard.type('2')
      await page.keyboard.press('Enter')
      await expect(page.getByText('Line 1: at most 1 can be returned')).toBeVisible()
    },
  )

  test('the bill of a waybill points at it and moved no stock', V14, async ({ page }) => {
    await page.goto(`/invoices/${doc('bill')}`)
    await expect(page.getByRole('link', { name: /^Bills SIR-2026-/ })).toBeVisible()
    await expect(onPost(page)).toContainText('Stock—')
    await page.goto(`/invoices/${doc('waybill')}`)
    await expect(page.getByRole('link', { name: 'Billed — open the invoice' })).toBeVisible()
    await expect(page.getByRole('link', { name: 'Bill this waybill' })).toHaveCount(0)
  })

  test(
    'cancelling needs a reason, writes a reversal and is blocked by dependents',
    V14,
    async ({ page }) => {
      await page.goto(`/invoices/${doc('sale')}`)
      await page.getByRole('button', { name: 'Cancel document' }).click()
      await page.getByLabel('Reason').fill('Wrong depot')
      await page.getByRole('dialog').getByRole('button', { name: 'Cancel document' }).click()
      await expect(page.getByRole('dialog')).toContainText('cancel them first')
      await expect(page.getByRole('dialog').getByRole('link', { name: /^SI-2026-/ })).toBeVisible()
      await page.getByRole('dialog').getByRole('button', { name: 'Close' }).first().click()

      await page.goto(`/invoices/${doc('decrease')}`)
      await page.getByRole('button', { name: 'Cancel document' }).click()
      await page.getByLabel('Reason').fill('Rebate withdrawn')
      await page.getByRole('dialog').getByRole('button', { name: 'Cancel document' }).click()
      await expect(page.getByText('CANCELLED', { exact: true })).toBeVisible()
      await expect(
        page.getByText(/^Cancelled \d{2}\.\d{2}\.\d{4} · Rebate withdrawn/),
      ).toBeVisible()
      await expect(page.getByRole('link', { name: /^Reversal entry \d+$/ })).toBeVisible({
        timeout: 15_000,
      })
    },
  )

  test('dashboard, statement and movements link the documents', V14, async ({ page }) => {
    await page.goto('/')
    await expect(page.getByRole('table', { name: 'Latest documents' })).toContainText(/SF-2026-/)
    await page.getByRole('table', { name: 'Latest documents' }).getByRole('link').first().click()
    await expect(page).toHaveURL(/\/invoices\/\d+$/)
    await page.goto(`/parts/${seeded().parts['HP-3702']}`)
    const sale = page
      .locator('.fk-grid')
      .first()
      .getByRole('link', { name: /^SF-2026-/ })
      .first()
    await expect(sale).toBeVisible({ timeout: 15_000 })
    await sale.click()
    await expect(page).toHaveURL(/\/invoices\/\d+$/)
  })

  for (const width of [1440, 1152]) {
    test(`editor keeps numbers whole at ${width}`, V14, async ({ page }) => {
      await page.setViewportSize({ width, height: 900 })
      await page.goto(`/invoices/${doc('sale')}`)
      await expect(cell(page, 0, 'part')).toHaveText('HP-3702')
      expect(await clipped(page, ['part'])).toEqual([])
      expect(await cutControls(page)).toEqual([])
    })
  }

  test("a new invoice takes the card's salesperson and account no.", V15, async ({ page }) => {
    await page.goto('/invoices/new?type=SALES_INVOICE')
    await pickAccount(page, 'Yılmaz')
    await expect(page.getByLabel('Salesperson').locator('option:checked')).toHaveText(
      seeded().salesRep.name,
    )
    await page.goto(`/invoices/${doc('sale')}`)
    await expect(page.getByRole('textbox', { name: 'Account', exact: true })).toHaveValue(
      '10482 · Yılmaz Tarım Makinaları',
    )
  })

  test('pickers leave out billed waybills and bills of waybills', V15, async ({ page }) => {
    // Kaya Ziraat: its only waybill is billed, its only invoice bills it.
    await page.goto('/invoices/new?type=SALES_INVOICE')
    await pickAccount(page, 'Kaya Ziraat')
    await expect(page.getByLabel('Waybill ref.').locator('option')).toHaveCount(1)
    await page.goto('/invoices/new?type=SALES_RETURN')
    await pickAccount(page, 'Kaya Ziraat')
    await expect(page.getByLabel('Original').locator('option')).toHaveCount(1)
    await pickAccount(page, 'Yılmaz')
    await expect(page.getByLabel('Original').locator(`option[value="${doc('sale')}"]`)).toHaveCount(
      1,
    )
  })

  test('the part card shows the last purchase cost', V15, async ({ page }) => {
    await page.goto(`/parts/${seeded().parts['HP-3774']}`)
    const box = page.getByRole('region', { name: 'Last purchase cost' })
    await expect(box).toContainText('8,900.00')
    await expect(box).toContainText('20.09.2026')
    await expect(box).toContainText(/AF-2026-\d+-\d{6}/)
  })
})
