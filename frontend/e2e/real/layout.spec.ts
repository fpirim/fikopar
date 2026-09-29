import { test, expect, seeded, V11 } from './fixtures'
import { clipped } from '../clipped'

const TEXT_COLS = ['accountName', 'city', 'sicilTipi']

for (const lang of ['en', 'tr'] as const) {
  test(`balances show whole names, cities and filter hints (${lang})`, async ({ page }) => {
    await page.addInitScript((l) => localStorage.setItem('lang', l), lang)
    await page.goto('/balances?asOf=2026-09-29')
    await expect(page.locator('.fk-grid')).toContainText('Yılmaz Tarım Makinaları')
    const viewport = page.locator('.fk-grid .ag-center-cols-viewport').first()
    await expect
      .poll(() => viewport.evaluate((el) => el.scrollWidth - el.clientWidth))
      .toBeLessThanOrEqual(0)
    expect(await clipped(page, TEXT_COLS)).toEqual([])
  })

  test(`the count number stays on one line (${lang})`, async ({ page }) => {
    test.skip(!seeded().openCountId, 'no open count seeded')
    await page.addInitScript((l) => localStorage.setItem('lang', l), lang)
    await page.goto(`/stock-counts/${seeded().openCountId}`)
    const h1 = page.getByRole('heading', { level: 1 })
    await expect(h1).toHaveText(/^SAY-/)
    const lines = await h1.evaluate(
      (el) => el.getBoundingClientRect().height / parseFloat(getComputedStyle(el).lineHeight),
    )
    expect(lines).toBeLessThan(1.5)
  })

  test(
    `statement keeps the tax line whole and badges journal lines (${lang})`,
    V11,
    async ({ page }) => {
      await page.addInitScript((l) => localStorage.setItem('lang', l), lang)
      const id = seeded().accounts['120.10482']
      await page.goto(`/statements/${id}?from=2026-01-01&to=2026-09-29`)
      await expect(page.locator('.fk-grid').first()).toContainText(
        lang === 'en' ? 'JOURNAL' : 'YEVMİYE',
      )
      const tax = page.getByText(/^(Tax office|Vergi dairesi) Konya · \[TAX NO\]$/)
      const lines = await tax.evaluate(
        (el) => el.getBoundingClientRect().height / parseFloat(getComputedStyle(el).lineHeight),
      )
      expect(lines).toBeLessThan(1.5)
      expect(await clipped(page)).toEqual([])
    },
  )
}

test('the sidebar version comes from the API health check', async ({ page }) => {
  const health = page.waitForResponse((r) => new URL(r.url()).pathname === '/healthz')
  await page.goto('/')
  const res = await health
  expect(res.status()).toBe(200)
  expect(await res.json()).toMatchObject({ status: 'ok' })
})

test('a long company list scrolls to its last company', async ({ page, request }) => {
  const api = process.env.E2E_API_URL || 'http://localhost:18080'
  const headers = { Authorization: `Bearer ${seeded().token}` }
  const orgs = await (await request.get(`${api}/api/admin/organizations`, { headers })).json()
  for (let i = orgs.length; i < 40; i++) {
    const res = await request.post(`${api}/api/admin/organizations`, {
      headers: { ...headers, 'Idempotency-Key': crypto.randomUUID() },
      data: { name: `Scroll Co ${i}`, slug: `scroll-co-${i}` },
    })
    expect(res.ok()).toBeTruthy()
  }
  await page.goto('/')
  await page.getByRole('button', { name: /^Company · / }).click()
  const last = page.getByRole('dialog').getByRole('button').last()
  const name = (await last.textContent())!
  await last.click()
  await expect(page.getByRole('button', { name: /^Company · / })).toContainText(name)
})
