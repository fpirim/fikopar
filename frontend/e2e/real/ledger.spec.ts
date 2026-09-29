import { test, expect, seeded, V11 } from './fixtures'

const grid = (page: import('@playwright/test').Page) => page.locator('.fk-grid').first()

test('balances list the seeded accounts as of a date', async ({ page }) => {
  await page.goto('/balances?asOf=2026-09-29')
  const row = grid(page).locator('.ag-row', { hasText: 'Yılmaz Tarım Makinaları' })
  await expect(row).toContainText('244,820.00')
  await expect(grid(page)).toContainText('Marmara Filtre')
})

test(
  'balances filter by BalanceRow column ids and sum the filtered rows',
  V11,
  async ({ page }) => {
    await page.goto('/balances?asOf=2026-09-29')
    await page.getByLabel('Filter Name').fill('Tarım')
    const pinned = grid(page).locator('.ag-floating-bottom')
    await expect(pinned).toContainText('4 of 9 · 1 filter')
    await expect(pinned).toContainText('318,285.00')
    await expect(pinned).toContainText('222,885.00')
  },
)

test('statement shows brought-forward rows and the collection', async ({ page }) => {
  const id = seeded().accounts['120.10482']
  await page.goto(`/statements/${id}?from=2026-01-01&to=2026-09-29`)
  await expect(page.getByRole('heading', { name: 'Yılmaz Tarım Makinaları' })).toBeVisible()
  await expect(grid(page)).toContainText('142,300.00')
  await expect(grid(page)).toContainText(/TAH-2026-1-0+2/)
})

test(
  'statement closes at the as-of balance, depot totals from lines only',
  V11,
  async ({ page }) => {
    const id = seeded().accounts['120.10482']
    await page.goto(`/statements/${id}?from=2026-01-01&to=2026-09-29`)
    const totals = page.getByRole('table', { name: 'Depot totals' })
    await expect(totals).toContainText('244,820.00 Dr')
    await expect(totals).toContainText('124,350.00')
  },
)

test('statement labels depots by code, not by database id', async ({ page }) => {
  const { accounts, depots } = seeded()
  // Seeded in reverse code order, so on a fresh database the ids never equal the codes.
  expect(depots['1']).not.toBe(1)
  expect(depots['2']).not.toBe(2)
  await page.goto(
    `/statements/${accounts['120.10482']}?from=2026-01-01&to=2026-09-29&depot=${depots['1']}`,
  )
  await expect(page.getByText('01.01.2026 – 29.09.2026 · depot 1')).toBeVisible()
  const totals = page.getByRole('table', { name: 'Depot totals' })
  await expect(totals.locator('tr').first().locator('td').first()).toHaveText('Depot 1')
  const dp = grid(page).locator('.ag-cell[col-id="depotId"]')
  await expect(dp.first()).toHaveText('1')
  expect(new Set(await dp.allTextContents())).toEqual(new Set(['1']))
})

test(
  'account card saves with If-Match and a second editor gets a reload',
  V11,
  async ({ page, browser }) => {
    const id = seeded().accounts['120.10482']
    await page.goto(`/statements/${id}`)
    const other = await browser.newPage()
    const { token, orgId } = seeded()
    await other.addInitScript(
      ([t, o]) => {
        localStorage.setItem('token', t)
        localStorage.setItem('selectedOrganizationId', o)
        localStorage.setItem('lang', 'en')
      },
      [token, String(orgId)] as const,
    )
    await other.goto(`/statements/${id}`)
    for (const p of [page, other]) {
      await p.getByRole('button', { name: 'Edit card' }).click()
    }
    await page.getByRole('dialog').getByLabel('District').fill('Karatay')
    await page.getByRole('dialog').getByRole('button', { name: 'Save' }).click()
    await expect(page.getByText('Karatay / Konya')).toBeVisible()
    await other.getByRole('dialog').getByRole('button', { name: 'Save' }).click()
    await expect(other.getByRole('alertdialog')).toContainText('Someone else changed this card')
    await other.close()
  },
)

test('journal lists entries and opens one', async ({ page }) => {
  await page.goto('/journal')
  const first = grid(page).getByRole('link').first()
  const no = (await first.textContent())!.trim()
  await first.click()
  await expect(page.getByRole('heading', { name: no })).toBeVisible()
  await expect(page.getByRole('table', { name: 'Postings' })).toBeVisible()
})

test('journal trail names the login', V11, async ({ page }) => {
  await page.goto('/journal')
  await grid(page).getByRole('link').first().click()
  await expect(page.getByRole('list', { name: 'Trail' })).toContainText('admin')
})
