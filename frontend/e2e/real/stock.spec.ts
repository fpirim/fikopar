import { test, expect, seeded, V12 } from './fixtures'

// F2 flows against the real API. Every test is @v12: run after the F2 backend and frontend merge.
const grid = (page: import('@playwright/test').Page) => page.locator('.fk-grid').first()
const part = (no: string) => seeded().parts[no]

test.describe('catalog and stock', () => {
  test('parts grid filters by code and lists below minimum and negative', V12, async ({ page }) => {
    await page.goto('/parts')
    await page.getByLabel(/^Filter (Code|Part|Stock no\.?)$/).fill('HP-37')
    await expect(grid(page)).toContainText('HP-3774')
    await expect(grid(page)).not.toContainText('FL-0092')
    await page.goto('/parts?stock=BELOW_MIN_OR_NEGATIVE')
    await expect(grid(page)).toContainText('OF-0457')
    await expect(grid(page)).toContainText('FL-0092')
    await expect(grid(page)).not.toContainText('GS-4410')
  })

  test(
    'part card shows codes, stock per depot, prices and running on hand',
    V12,
    async ({ page }) => {
      await page.goto(`/parts/${part('HP-3774')}`)
      await expect(page.getByRole('heading', { name: /Hydraulic pump, 3-cyl\./ })).toBeVisible()
      await expect(page.getByText('OEM 3774612M91')).toBeVisible()
      await expect(page.getByText('ALT K9-3774')).toBeVisible()
      const main = page.locator('main')
      await expect(main).toContainText('A-04-2')
      await expect(main).toContainText('12,400.00')
      await expect(main).toContainText('13,100.00')
      const moves = grid(page)
      await expect(moves).toContainText(/TRF-2026-/)
      await expect(moves).toContainText(/SAY-2026-/)
      // 12 counted in depot 1 and 2 in depot 2; the transfer moves stock between them.
      await expect(moves.locator('.ag-row').first()).toContainText('14')
    },
  )

  test('a transfer that leaves stock below zero warns but posts', V12, async ({ page }) => {
    await page.goto(`/parts/${part('BR-2210')}`)
    await page.getByRole('button', { name: 'Transfer' }).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByLabel(/^(Qty|Quantity)$/).fill('9')
    await dialog.getByRole('button', { name: /^(Transfer|Post)$/ }).click()
    await expect(page.getByText(/negative/i).first()).toBeVisible()
    await expect(page.getByText(/TRF-2026-/).first()).toBeVisible()
  })

  test('count lines take a count on Enter and post the differences', V12, async ({ page }) => {
    const id = seeded().openCountId
    test.skip(!id, 'no open count seeded')
    await page.goto(`/stock-counts/${id}`)
    const rows = grid(page).locator('.ag-center-cols-container .ag-row')
    await expect(rows.first()).toBeVisible()
    await rows.first().locator('[col-id="countedQty"]').click()
    await page.keyboard.type('5')
    await page.keyboard.press('Enter')
    await page.keyboard.type('1')
    await page.keyboard.press('Enter')
    await expect(rows.first().locator('[col-id="countedQty"]')).toHaveText('5')
    await expect(grid(page).getByRole('img', { name: 'Saved' })).toHaveCount(2)
    await page.reload()
    await expect(rows.first().locator('[col-id="countedQty"]')).toHaveText('5')
    await expect(rows.nth(1).locator('[col-id="countedQty"]')).toHaveText('1')
    await page.getByRole('button', { name: 'Post differences' }).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByRole('button', { name: 'Post', exact: true }).click()
    await expect(dialog.getByRole('status')).toContainText(/ posted · 1 adjustment$/)
    await dialog.getByRole('button', { name: 'Done' }).click()
    await expect(page.getByText('FINALIZED').first()).toBeVisible()
  })

  test('catalog search opens from the keyboard and ranks the OEM code', V12, async ({ page }) => {
    await page.goto('/')
    await expect(page.getByRole('searchbox', { name: 'Search' })).toBeVisible()
    await page.keyboard.press('ControlOrMeta+k')
    await page.getByRole('combobox', { name: 'Part search' }).fill('3774612M91')
    const hit = page
      .getByRole('listbox', { name: 'Catalog search results' })
      .getByRole('option', { name: /HP-3774/ })
    await expect(hit.first()).toBeVisible()
    await page.keyboard.press('Enter')
    await expect(page).toHaveURL(new RegExp(`/parts/${part('HP-3774')}`))
  })

  test('a CSV price list previews its diff and applies once', V12, async ({ page }) => {
    await page.goto('/parts/import')
    const csv = [
      'Stok No;Açıklama;Fiyat',
      'HP-3702;Hydraulic pump, 2-cyl.;10.250,00',
      'HP-NEW1;Hydraulic pump, test;1.000,00',
    ].join('\n')
    const catalog = page.getByLabel(/catalog/i).first()
    const option = catalog.locator('option', { hasText: 'Hydraulics 2026' })
    await catalog.selectOption((await option.getAttribute('value'))!)
    await page.locator('input[type=file]').setInputFiles({
      name: 'hydraulics.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(csv, 'utf8'),
    })
    await page.getByRole('button', { name: 'Upload and preview' }).click()
    const tabs = page.getByRole('tablist', { name: 'Change' })
    await expect(tabs.getByRole('tab', { name: /New\s*1/ })).toBeVisible()
    await expect(tabs.getByRole('tab', { name: /Price\s*1/ })).toBeVisible()
    await page.getByRole('button', { name: 'Continue to apply' }).click()
    await page.getByRole('button', { name: 'Apply', exact: true }).click()
    const confirm = page.getByRole('alertdialog')
    await expect(confirm).toContainText('1 new parts, 1 price changes')
    await confirm.getByRole('button', { name: 'Apply' }).click()
    const result = page.getByRole('list', { name: 'Result' })
    await expect(result).toContainText('Parts created1')
    await expect(result).toContainText('Prices added2')
    await page.goto('/parts')
    await page.getByLabel(/^Filter (Code|Part|Stock no\.?)$/).fill('HP-NEW1')
    await expect(grid(page)).toContainText('HP-NEW1')
  })

  test('the dashboard lists negative parts first', V12, async ({ page }) => {
    await page.goto('/')
    const card = page.getByRole('region', { name: 'Below minimum / negative' })
    await expect(card.getByRole('listitem').first()).toContainText('−')
    await expect(card.getByRole('listitem').filter({ hasText: 'OF-0457' })).toContainText('−3 / 5')
  })
})
