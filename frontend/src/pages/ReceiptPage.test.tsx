import { describe, expect, it } from 'vitest'
import { http, HttpResponse } from 'msw'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { server } from '../mocks/node'
import { renderApp } from '../test/renderApp'
import { capture, captureDownloads } from '../test/msw'
import { todayIso } from '../lib/format'
import { fill } from '../test/input'

async function pickKaya() {
  await userEvent.type(await screen.findByRole('combobox', { name: 'Hesap' }), 'Kaya')
  await userEvent.click(await screen.findByRole('option', { name: /Kaya Ziraat/ }))
}

const aside = () => screen.getByRole('complementary', { name: 'Hesap özeti' })

describe('ReceiptPage', () => {
  it('previews the next number per type and depot', async () => {
    renderApp('/receipts')
    expect(await screen.findByText('TAH-2026-1-000234')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Tediye' }))
    expect(await screen.findByText('TED-2026-1-000064')).toBeInTheDocument()
    await userEvent.selectOptions(screen.getByRole('combobox', { name: 'Depo' }), '2')
    expect(await screen.findByText('TED-2026-2-000065')).toBeInTheDocument()
  })

  it('defaults to the first depot by code and shows codes, not ids', async () => {
    // ids run against the codes, as on a real database
    server.use(
      http.get('/api/orgs/:orgId/depots', () =>
        HttpResponse.json([
          { id: 1, code: '2', name: 'Şube', vatRate: '20', allowNegativeStock: false, version: 1 },
          {
            id: 2,
            code: '1',
            name: 'Merkez',
            vatRate: '20',
            allowNegativeStock: false,
            version: 1,
          },
        ]),
      ),
    )
    const series = capture('get', '/api/orgs/:orgId/number-series')
    const posts = capture('post', '/api/orgs/:orgId/receipts')
    renderApp('/receipts')
    const methods = await screen.findByRole('radiogroup', { name: 'Ödeme şekli' })
    await waitFor(() =>
      expect(within(methods).getByText('Kasa defteri · Depo 1')).toBeInTheDocument(),
    )
    expect(screen.getByRole('combobox', { name: 'Depo' })).toHaveValue('2')
    expect(series.map((c) => c.url.searchParams.get('depotId'))).not.toContain('1')
    await pickKaya()
    await userEvent.type(screen.getByLabelText('Tutar'), '100')
    await userEvent.click(screen.getByRole('button', { name: 'Kaydet ve yazdır' }))
    await waitFor(() => expect(posts).toHaveLength(1))
    expect(posts[0].body).toMatchObject({ depotId: 2 })

    await userEvent.selectOptions(screen.getByRole('combobox', { name: 'Depo' }), '1')
    expect(await within(methods).findByText('Kasa defteri · Depo 2')).toBeInTheDocument()
  })

  it('offers cash and bank transfer; cheque and note are off', async () => {
    renderApp('/receipts')
    const methods = await screen.findByRole('radiogroup', { name: 'Ödeme şekli' })
    expect(within(methods).getByRole('radio', { name: /Nakit/ })).toBeChecked()
    expect(within(methods).getByRole('radio', { name: /Çek/ })).toBeDisabled()
    expect(within(methods).getByRole('radio', { name: /Senet/ })).toBeDisabled()
    expect(within(methods).queryByText(/F4/)).not.toBeInTheDocument()
  })

  it('shows the balance before and after the receipt', async () => {
    renderApp('/receipts')
    await pickKaya()
    await waitFor(() => expect(within(aside()).getAllByText('94.000,00 B')).toHaveLength(2))
    expect(within(aside()).getByText('24.000,00')).toBeInTheDocument()
    await userEvent.type(screen.getByLabelText('Tutar'), '15000')
    expect(within(aside()).getByText('79.000,00 B')).toBeInTheDocument()
    expect(within(aside()).getByRole('link', { name: 'Ekstre' })).toHaveAttribute(
      'href',
      '/statements/5006',
    )
  })

  it('posts a cash collection with an idempotency key, then prints it', async () => {
    const posts = capture('post', '/api/orgs/:orgId/receipts')
    const pdfs = capture('get', '/api/orgs/:orgId/receipts/:receiptId')
    const files = captureDownloads()
    renderApp('/receipts')
    await pickKaya()
    await userEvent.type(screen.getByLabelText('Tutar'), '15000')
    await userEvent.type(screen.getByLabelText('Açıklama'), 'Nakit tahsilat')
    await userEvent.click(screen.getByRole('button', { name: 'Kaydet ve yazdır' }))
    await waitFor(() => expect(files).toHaveLength(1))
    expect(posts[0].body).toEqual({
      kind: 'COLLECTION',
      depotId: 1,
      sicilId: 1006,
      date: todayIso(),
      method: 'CASH',
      amount: '15000.00',
      currency: 'TRY',
      description: 'Nakit tahsilat',
    })
    expect(posts[0].headers.get('Idempotency-Key')).toMatch(/^[0-9a-f-]{36}$/)
    expect(pdfs[0].headers.get('Accept')).toBe('application/pdf')
    expect(await screen.findByText('TAH-2026-1-000234 kaydedildi')).toBeInTheDocument()
    expect(await screen.findByText('TAH-2026-1-000235')).toBeInTheDocument()
    expect(screen.getByLabelText('Tutar')).toHaveValue('')
  })

  it('reuses the key when the same receipt is retried', async () => {
    let first = true
    server.use(
      http.post('/api/orgs/:orgId/receipts', () => {
        if (!first) return undefined
        first = false
        return HttpResponse.json({ code: 'ERR_INTERNAL' }, { status: 503 })
      }),
    )
    const posts = capture('post', '/api/orgs/:orgId/receipts')
    captureDownloads()
    renderApp('/receipts')
    await pickKaya()
    await userEvent.type(screen.getByLabelText('Tutar'), '500')
    await userEvent.click(screen.getByRole('button', { name: 'Kaydet ve yazdır' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('Sunucu hatası')
    await userEvent.click(screen.getByRole('button', { name: 'Kaydet ve yazdır' }))
    await waitFor(() => expect(posts).toHaveLength(2))
    expect(posts[1].headers.get('Idempotency-Key')).toBe(posts[0].headers.get('Idempotency-Key'))
  })

  it('needs a bank account for a bank transfer', async () => {
    const accounts = capture('get', '/api/orgs/:orgId/ledger/accounts')
    const posts = capture('post', '/api/orgs/:orgId/receipts')
    captureDownloads()
    renderApp('/receipts')
    await pickKaya()
    await userEvent.click(screen.getByRole('radio', { name: /Havale/ }))
    const bank = await screen.findByLabelText('Banka hesabı')
    await waitFor(() => expect(bank).toHaveDisplayValue('102.00002 · [BANK A]'))
    await userEvent.type(screen.getByLabelText('Tutar'), '2500,5')
    await userEvent.click(screen.getByRole('button', { name: 'Kaydet ve yazdır' }))
    await waitFor(() => expect(posts).toHaveLength(1))
    expect(accounts).toHaveLength(1)
    expect(accounts[0].url.searchParams.get('role')).toBe('BANK')
    expect(accounts[0].url.searchParams.has('cursor')).toBe(false)
    expect(posts[0].body).toMatchObject({
      method: 'BANK_TRANSFER',
      amount: '2500.50',
      bankAccountId: expect.any(Number),
    })
  })

  it('explains a closed year', async () => {
    captureDownloads()
    renderApp('/receipts')
    await pickKaya()
    const date = screen.getByLabelText('Tarih')
    await userEvent.clear(date)
    await userEvent.type(date, '31.12.2024')
    await userEvent.type(screen.getByLabelText('Tutar'), '100')
    await userEvent.click(screen.getByRole('button', { name: 'Kaydet ve yazdır' }))
    expect(await screen.findByRole('alert')).toHaveTextContent(
      '2024 yılı kapalı; önce 2025 devrini yeniden açın',
    )
  })

  it('shows a method that the server does not offer yet', async () => {
    server.use(
      http.post('/api/orgs/:orgId/receipts', () =>
        HttpResponse.json({ code: 'ERR_METHOD_NOT_AVAILABLE', field: 'method' }, { status: 400 }),
      ),
    )
    renderApp('/receipts')
    await pickKaya()
    await userEvent.type(screen.getByLabelText('Tutar'), '100')
    await userEvent.click(screen.getByRole('button', { name: 'Kaydet ve yazdır' }))
    const methods = screen.getByRole('radiogroup', { name: 'Ödeme şekli' })
    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Bu ödeme şekli henüz kullanılamıyor',
    )
    expect(methods).toHaveAttribute('aria-invalid', 'true')
  })

  it('validates before posting', async () => {
    const posts = capture('post', '/api/orgs/:orgId/receipts')
    renderApp('/receipts')
    await userEvent.click(await screen.findByRole('button', { name: 'Kaydet ve yazdır' }))
    expect(await screen.findByText('Hesap seçin')).toBeInTheDocument()
    expect(screen.getByText('Sıfırdan büyük bir tutar girin')).toBeInTheDocument()
    expect(posts).toHaveLength(0)
  })

  it('lists recent receipts with filters', async () => {
    const grid = capture('post', '/api/orgs/:orgId/receipts/grid')
    renderApp('/receipts?depot=2')
    const panel = await screen.findByRole('region', { name: 'Son makbuzlar' })
    await waitFor(() => expect(within(panel).getByText('TED-2026-2-000064')).toBeInTheDocument())
    expect(grid[0].body).toMatchObject({ depotId: 2 })
    expect((grid[0].body as { filterModel: object }).filterModel).not.toHaveProperty('depotId')
    await fill(within(panel).getByLabelText('No filtresi'), 'TED')
    await waitFor(() =>
      expect(grid[grid.length - 1].body).toMatchObject({
        filterModel: { receiptNo: { type: 'contains', filter: 'TED' } },
      }),
    )
  })
})
