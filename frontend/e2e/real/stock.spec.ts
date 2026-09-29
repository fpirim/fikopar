import { test, expect, seeded, V12, V13 } from './fixtures'
import { lisakt } from '../fixtures/lisakt'

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
      // 12 counted in depot 1 and 2 in depot 2, the transfer moves stock between them, the seeded
      // purchase adds 5; later sales depend on the document specs, so the purchase row is checked.
      await expect(moves.locator('.ag-row', { hasText: /AF-2026-/ })).toContainText('19')
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

  test('a part off the sheet joins the count through the catalog search', V12, async ({ page }) => {
    const id = seeded().openCountId
    test.skip(!id, 'no open count seeded')
    await page.goto(`/stock-counts/${id}`)
    await page.getByRole('button', { name: 'Add part' }).click()
    const dialog = page.getByRole('dialog')
    await dialog.getByRole('combobox', { name: 'Part search' }).fill('clutch 12')
    const results = page.getByRole('listbox', { name: 'Catalog search results' })
    await results.getByRole('option', { name: /CL-1210/ }).click()
    await expect(dialog.getByRole('combobox', { name: 'Part search' })).toHaveValue('CL-1210')
    await dialog.getByLabel('Counted').fill('0')
    await dialog.getByRole('button', { name: 'Add', exact: true }).click()
    await expect(page.getByText('CL-1210 added to the count')).toBeVisible()
    await page.getByLabel('Filter Part').fill('CL-1210')
    const row = grid(page).locator('.ag-center-cols-container .ag-row', { hasText: 'CL-1210' })
    await expect(row.locator('[col-id="countedQty"]')).toHaveText('0')
  })

  test('count lines take a count on Enter and post the differences', V12, async ({ page }) => {
    const id = seeded().openCountId
    test.skip(!id, 'no open count seeded')
    await page.goto(`/stock-counts/${id}`)
    // Shelf order: BR-2210 on F-02-2 (6 on hand), then CL-1150 on F-05-1 (2 on hand).
    const counted = (no: string) =>
      grid(page)
        .locator('.ag-center-cols-container .ag-row', { hasText: no })
        .locator('[col-id="countedQty"]')
    await counted('BR-2210').click()
    await page.keyboard.type('6')
    await page.keyboard.press('Enter')
    await page.keyboard.type('1')
    await page.keyboard.press('Enter')
    await expect(counted('BR-2210')).toHaveText('6')
    await expect(grid(page).getByRole('img', { name: 'Saved' })).toHaveCount(2)
    await page.reload()
    await expect(counted('BR-2210')).toHaveText('6')
    await expect(counted('CL-1150')).toHaveText('1')
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

  test('a LISAKT .txt list in CP857 imports under the automatic format', V12, async ({ page }) => {
    await page.goto('/parts/import')
    const catalog = page.getByLabel('Catalog')
    const option = catalog.locator('option', { hasText: 'Hydraulics 2026' })
    await catalog.selectOption((await option.getAttribute('value'))!)
    await page.getByLabel('File').setInputFiles({
      name: 'LISAKT.txt',
      mimeType: 'text/plain',
      buffer: lisakt([
        { op: '2', catalog: 12, stockNo: 'HV-1007', name: 'Control valve, medium', price: 399 },
        {
          op: '1',
          catalog: 12,
          stockNo: 'HP-LIS1',
          name: 'Yağ soğutucu, İş makinası ŞÇÖÜ',
          brand: 'Güçlü',
          oem: 'OEM-LIS1',
          price: 2750.5,
        },
        { op: '1', catalog: 20, stockNo: 'SE-9999', name: 'Wrong catalog', price: 10 },
      ]),
    })
    await expect(page.getByLabel('Format')).toHaveValue('LISAKT')
    await page.getByRole('button', { name: 'Upload and preview' }).click()
    const tabs = page.getByRole('tablist', { name: 'Change' })
    // The wrong-catalog row is new and in error.
    await expect(tabs.getByRole('tab', { name: /New\s*2/ })).toBeVisible()
    await expect(tabs.getByRole('tab', { name: /Price\s*1/ })).toBeVisible()
    await expect(tabs.getByRole('tab', { name: /Errors\s*1/ })).toBeVisible()
    await expect(grid(page)).toContainText('Yağ soğutucu, İş makinası ŞÇÖÜ')
    await expect(grid(page)).toContainText('2,750.50')
    await page.getByRole('button', { name: 'Continue to apply' }).click()
    await page.getByRole('checkbox', { name: 'Skip error rows' }).check()
    await page.getByRole('button', { name: 'Apply', exact: true }).click()
    await page.getByRole('alertdialog').getByRole('button', { name: 'Apply' }).click()
    const result = page.getByRole('list', { name: 'Result' })
    await expect(result).toContainText('Parts created1')
    await expect(result).toContainText('Prices added2')
    await expect(result).toContainText('Rows skipped1')
    await page.goto('/parts')
    await page.getByLabel(/^Filter (Code|Part|Stock no\.?)$/).fill('HP-LIS1')
    await expect(grid(page)).toContainText('Yağ soğutucu, İş makinası ŞÇÖÜ')
  })

  test('the dashboard lists negative parts first', V12, async ({ page }) => {
    await page.goto('/')
    const card = page.getByRole('region', { name: 'Below minimum / negative' })
    await expect(card.getByRole('listitem').first()).toContainText('−')
    await expect(card.getByRole('listitem').filter({ hasText: 'OF-0457' })).toContainText('−3 / 5')
  })

  test(
    '"pump 37" finds the HP-37 pumps by a name word and a code fragment',
    V13,
    async ({ page }) => {
      await page.goto('/')
      await expect(page.getByRole('searchbox', { name: 'Search' })).toBeVisible()
      await page.keyboard.press('ControlOrMeta+k')
      await page.getByRole('combobox', { name: 'Part search' }).fill('pump 37')
      const list = page.getByRole('listbox', { name: 'Catalog search results' })
      await expect(list.getByRole('option', { name: /HP-3702/ }).first()).toBeVisible()
      await expect(list.getByRole('option', { name: /HP-3774/ }).first()).toBeVisible()
      await expect(list).not.toContainText('FL-0092')
    },
  )

  test('all depots shows on hand per depot side by side', V13, async ({ page }) => {
    await page.goto('/parts')
    await page.getByLabel(/^Filter (Code|Part|Stock no\.?)$/).fill('FL-0092')
    const row = grid(page).locator('.ag-center-cols-container .ag-row').first()
    await expect(row).toContainText('FL-0092')
    await expect(grid(page).getByRole('columnheader', { name: 'Dp 1' })).toBeVisible()
    await expect(grid(page).getByRole('columnheader', { name: 'Dp 2' })).toBeVisible()
    const d1 = Number(await row.locator('[col-id^="depot."]').first().textContent())
    const d2 = Number(await row.locator('[col-id^="depot."]').nth(1).textContent())
    expect(d1 + d2).toBe(Number(await row.locator('[col-id="onHand"]').textContent()))
  })

  test('movements filter by the Out column on the server', V13, async ({ page }) => {
    await page.goto(`/parts/${part('HP-3774')}`)
    await expect(grid(page)).toContainText(/TRF-2026-/)
    await page.getByLabel('Filter Out').fill('>= 1')
    await expect
      .poll(async () => {
        const outs = await grid(page)
          .locator('.ag-center-cols-container .ag-row [col-id="qtyOut"]')
          .allTextContents()
        return outs.length > 0 && outs.every((v) => Number(v) >= 1)
      })
      .toBe(true)
  })

  test('the equivalents card lists linked parts both ways', V13, async ({ page }) => {
    await page.goto(`/parts/${part('CL-1150')}`)
    const card = page.getByRole('region', { name: 'Equivalents' })
    await expect(card).toContainText('Clutch disc 12"')
    await expect(card).toContainText('Transmission')
    await card.getByRole('link', { name: 'CL-1210' }).click()
    await expect(page).toHaveURL(new RegExp(`/parts/${part('CL-1210')}`))
    await expect(
      page.getByRole('region', { name: 'Equivalents' }).getByRole('link', { name: 'CL-1150' }),
    ).toBeVisible()
  })
})
