import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { renderApp } from '../test/renderApp'
import { capture, captureDownloads } from '../test/msw'
import { server } from '../mocks/node'

// HP-3774 (mockup 09) is part 1001; BR-2215 is 1009.
const HP = '/parts/1001'
const stockTable = () => screen.findByRole('table', { name: 'Depo bazında stok' })
const priceTable = () => screen.findByRole('table', { name: 'Liste fiyatları' })
// Leaf texts of each row, space separated.
const rowText = (table: HTMLElement) =>
  within(table)
    .getAllByRole('row')
    .map((r) =>
      Array.from(r.querySelectorAll('*'))
        .filter((el) => el.children.length === 0 && el.textContent?.trim())
        .map((el) => el.textContent!.trim())
        .join(' '),
    )

describe('PartPage', () => {
  it('shows the header with codes, stock per depot and a total row', async () => {
    renderApp(HP)
    expect(
      await screen.findByRole('heading', { name: 'Hydraulic pump, 3-cyl.' }),
    ).toBeInTheDocument()
    expect(screen.getByText('HP-3774')).toBeInTheDocument()
    expect(screen.getByText('OEM 3774612M91')).toBeInTheDocument()
    expect(screen.getByText('ALT K9-3774')).toBeInTheDocument()
    expect(screen.getByText(/Fiyat listesi: Hydraulics 2026/)).toBeInTheDocument()
    await waitFor(async () =>
      expect(rowText(await stockTable())).toEqual(
        expect.arrayContaining([
          expect.stringContaining('Depo 1 10 1 9 6 20 A-04-2'),
          expect.stringContaining('Depo 2 4 0 4 2 8 H-11-1'),
          expect.stringContaining('Toplam 14 1 13'),
        ]),
      ),
    )
  })

  it('links equivalents to their part', async () => {
    renderApp('/parts/1005')
    expect(await screen.findByRole('link', { name: 'CL-1210' })).toHaveAttribute(
      'href',
      '/parts/1006',
    )
  })

  it('lists price history newest first with change and the current price', async () => {
    renderApp(HP)
    await waitFor(async () =>
      expect(rowText(await priceTable()).slice(1)).toEqual([
        '01.09.2026 GEÇERLİ 12.400,00 +6,9%',
        '01.04.2026 11.600,00 +8,4%',
        '02.01.2026 10.700,00 —',
      ]),
    )
  })

  it('records a future price with an idempotency key', async () => {
    const calls = capture('post', '/api/orgs/:orgId/catalog/parts/:partId/prices')
    renderApp(HP)
    await priceTable()
    await userEvent.click(screen.getByRole('button', { name: 'Yeni fiyat' }))
    const dialog = await screen.findByRole('dialog')
    await userEvent.type(within(dialog).getByLabelText('Fiyat'), '13.100')
    const date = within(dialog).getByLabelText('Geçerlilik')
    await userEvent.clear(date)
    await userEvent.type(date, '15.10.2026{Enter}')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Kaydet' }))
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    expect(calls[0].body).toEqual({ price: '13100.00', validFrom: '2026-10-15' })
    expect(calls[0].headers.get('Idempotency-Key')).toBeTruthy()
    await waitFor(async () =>
      expect(rowText(await priceTable())[1]).toBe('15.10.2026 İLERİ 13.100,00 +5,6%'),
    )
    expect(screen.getByText('Sonraki: 15.10.2026 itibarıyla 13.100,00')).toBeInTheDocument()
  })

  it('explains a different price on an existing date', async () => {
    renderApp(HP)
    await priceTable()
    await userEvent.click(screen.getByRole('button', { name: 'Yeni fiyat' }))
    const dialog = await screen.findByRole('dialog')
    await userEvent.type(within(dialog).getByLabelText('Fiyat'), '12.000')
    const date = within(dialog).getByLabelText('Geçerlilik')
    await userEvent.clear(date)
    await userEvent.type(date, '01.09.2026{Enter}')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Kaydet' }))
    expect(await within(dialog).findByRole('alert')).toHaveTextContent(
      'O tarihte başlayan farklı bir fiyat var',
    )
  })

  it('saves depot settings with If-Match on the row version', async () => {
    const calls = capture('put', '/api/orgs/:orgId/catalog/parts/:partId/depots/:depotId')
    renderApp(HP)
    await stockTable()
    await userEvent.click(await screen.findByRole('button', { name: 'Depo 1 düzenle' }))
    const dialog = await screen.findByRole('dialog')
    const min = within(dialog).getByLabelText('Min')
    await userEvent.clear(min)
    await userEvent.type(min, '8')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Kaydet' }))
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    expect(calls[0].url.pathname).toMatch(/\/parts\/1001\/depots\/1$/)
    expect(calls[0].headers.get('If-Match')).toBe('"1"')
    expect(calls[0].body).toEqual({ minQty: '8', maxQty: '20', shelf: 'A-04-2' })
    await waitFor(async () =>
      expect(rowText(await stockTable())).toEqual(
        expect.arrayContaining([expect.stringContaining('Depo 1 10 1 9 8 20 A-04-2')]),
      ),
    )
  })

  it('creates the depot row with If-Match "0" when the part has none there', async () => {
    const calls = capture('put', '/api/orgs/:orgId/catalog/parts/:partId/depots/:depotId')
    renderApp('/parts/1006')
    await stockTable()
    await userEvent.click(await screen.findByRole('button', { name: 'Depo 2 düzenle' }))
    const dialog = await screen.findByRole('dialog')
    await userEvent.type(within(dialog).getByLabelText('Raf'), 'F-05-4')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Kaydet' }))
    await waitFor(() => expect(calls).toHaveLength(1))
    expect(calls[0].headers.get('If-Match')).toBe('"0"')
  })

  it('refuses max below min in the dialog', async () => {
    renderApp(HP)
    await stockTable()
    await userEvent.click(await screen.findByRole('button', { name: 'Depo 1 düzenle' }))
    const dialog = await screen.findByRole('dialog')
    const max = within(dialog).getByLabelText('Maks')
    await userEvent.clear(max)
    await userEvent.type(max, '3')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Kaydet' }))
    expect(await within(dialog).findByText('Maks, min değerinden küçük olamaz')).toBeVisible()
  })

  it('shows the server field error for max below min', async () => {
    server.use(
      http.put('/api/orgs/:orgId/catalog/parts/:partId/depots/:depotId', () =>
        HttpResponse.json({ code: 'ERR_INVALID_REQUEST', field: 'maxQty' }, { status: 422 }),
      ),
    )
    renderApp(HP)
    await stockTable()
    await userEvent.click(await screen.findByRole('button', { name: 'Depo 1 düzenle' }))
    const dialog = await screen.findByRole('dialog')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Kaydet' }))
    expect(await within(dialog).findByText('Maks, min değerinden küçük olamaz')).toBeVisible()
  })

  it('asks to reload when someone saved the depot first', async () => {
    server.use(
      http.put('/api/orgs/:orgId/catalog/parts/:partId/depots/:depotId', () =>
        HttpResponse.json({ code: 'ERR_VERSION_CONFLICT' }, { status: 412 }),
      ),
    )
    const reads = capture('get', '/api/orgs/:orgId/catalog/parts/:partId/stock')
    renderApp(HP)
    await stockTable()
    await userEvent.click(await screen.findByRole('button', { name: 'Depo 1 düzenle' }))
    await userEvent.click(
      within(await screen.findByRole('dialog')).getByRole('button', { name: 'Kaydet' }),
    )
    const alert = await screen.findByRole('alertdialog')
    expect(alert).toHaveTextContent('Ayarlar değişti')
    const before = reads.length
    await userEvent.click(within(alert).getByRole('button', { name: 'Yeniden yükle' }))
    await waitFor(() => expect(reads.length).toBeGreaterThan(before))
    expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()
  })

  it('lists movements with a running on-hand and transfer depots by code', async () => {
    const calls = capture('post', '/api/orgs/:orgId/stock/movements/grid')
    renderApp(HP)
    expect(await screen.findByText('SF-2026-1-000418')).toBeInTheDocument()
    expect(calls[0].body).toMatchObject({ partId: 1001, depotId: null })
    expect((await screen.findAllByText('1 → 2')).length).toBe(2)
    expect(screen.getAllByText('SATIŞ').length).toBeGreaterThan(0)
    const first = document.querySelector('.ag-row[row-index="0"] [col-id="runningOnHand"]')
    expect(first).toHaveTextContent('14')
    expect(screen.getByRole('group', { name: 'Kapanış' })).toHaveTextContent('14')
  })

  it('downloads the movements as Excel', async () => {
    const blobs = captureDownloads()
    const calls = capture('post', '/api/orgs/:orgId/stock/movements/grid')
    renderApp(HP)
    await screen.findByText('SF-2026-1-000418')
    await userEvent.click(screen.getByRole('button', { name: 'Excel' }))
    await waitFor(() => expect(blobs).toHaveLength(1))
    expect(calls[calls.length - 1].headers.get('Accept')).toContain('spreadsheetml')
  })

  it('transfers with a warning when the source depot goes negative', async () => {
    const calls = capture('post', '/api/orgs/:orgId/stock/transfers')
    renderApp(HP)
    await stockTable()
    await userEvent.click(screen.getByRole('button', { name: 'Transfer' }))
    const dialog = await screen.findByRole('dialog')
    expect(within(dialog).getByLabelText('Çıkış deposu')).toHaveValue('1')
    expect(within(dialog).getByLabelText('Giriş deposu')).toHaveValue('2')
    await userEvent.type(within(dialog).getByLabelText('Miktar'), '12')
    expect(
      within(dialog).getByText('Depo 1 içinde yalnız 9 var; mevcut eksiye düşer.'),
    ).toBeVisible()
    await userEvent.click(within(dialog).getByRole('button', { name: 'Transfer et' }))
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    expect(calls[0].body).toMatchObject({
      fromDepotId: 1,
      toDepotId: 2,
      lines: [{ partId: 1001, qty: '12' }],
    })
    expect(calls[0].headers.get('Idempotency-Key')).toBeTruthy()
    const warning = await screen.findByRole('alert')
    expect(warning).toHaveTextContent('TRF-2026-1-000052 sonrası eksi stok')
    expect(warning).toHaveTextContent('Depo 1 · HP-3774: mevcut −2')
    await waitFor(async () =>
      expect(rowText(await stockTable())).toEqual(
        expect.arrayContaining([expect.stringContaining('Depo 1 −2')]),
      ),
    )
    await userEvent.click(within(warning).getByRole('button', { name: 'Kapat' }))
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('refuses a transfer without a quantity', async () => {
    const calls = capture('post', '/api/orgs/:orgId/stock/transfers')
    renderApp(HP)
    await stockTable()
    await userEvent.click(screen.getByRole('button', { name: 'Transfer' }))
    const dialog = await screen.findByRole('dialog')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Transfer et' }))
    expect(within(dialog).getByText('Sıfırdan büyük bir miktar girin')).toBeVisible()
    expect(calls).toHaveLength(0)
  })

  it('shows a not-found part', async () => {
    renderApp('/parts/99999')
    expect(await screen.findByText('Parça bulunamadı')).toBeInTheDocument()
  })
})
