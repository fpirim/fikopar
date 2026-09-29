import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/node'
import { renderApp } from '../test/renderApp'
import { capture, captureDownloads } from '../test/msw'

const grid = () => document.querySelector('.fk-grid') as HTMLElement
const kpi = (label: string) => screen.getByRole('group', { name: label })

async function openRow(code: string, depot: string) {
  await userEvent.type(await screen.findByLabelText('Hesap filtresi'), code)
  await waitFor(() => expect(within(grid()).getAllByText(code).length).toBeGreaterThan(0))
  const row = within(grid())
    .getAllByRole('row')
    .find(
      (r) =>
        r.textContent?.includes(code) &&
        r.querySelector(`[col-id="depotId"]`)?.textContent === depot,
    )!
  await userEvent.dblClick(within(row).getByText(code))
  return screen.findByRole('dialog')
}

describe('CarryForwardPage', () => {
  it('previews the opening entries with checks and totals', async () => {
    renderApp('/carry-forward/2026')
    expect(
      await screen.findByRole('heading', { name: 'Cari devir 2025 → 2026' }),
    ).toBeInTheDocument()
    const steps = await screen.findByRole('list', { name: 'Yıl sonu adımları' })
    expect(within(steps).getByText('2025 belgelerini kapat')).toBeInTheDocument()
    await waitFor(() =>
      expect(within(steps).getByText('Cari bakiyeler').closest('li')).toHaveAttribute(
        'aria-current',
        'step',
      ),
    )
    expect(kpi('Bakiyeli hesaplar')).toHaveTextContent('411')
    expect(kpi('Kayıtlar (hesap × depo)')).toHaveTextContent('822')
    expect(screen.getByRole('button', { name: '822 açılış kaydını kaydet' })).toBeEnabled()
    await waitFor(() => expect(within(grid()).getAllByText('HAZIR').length).toBeGreaterThan(0))
  })

  it('overrides one opening amount with a reason', async () => {
    const puts = capture('put', '/api/orgs/:orgId/ledger/carry-forwards/:year/overrides')
    const gets = capture('get', '/api/orgs/:orgId/ledger/carry-forwards/:year')
    renderApp('/carry-forward/2026?depot=1')
    const dialog = await openRow('120.10517', '1')
    expect(within(dialog).getByLabelText('Açılış borç')).toHaveValue('71.500,00')
    const debit = within(dialog).getByLabelText('Açılış borç')
    await userEvent.clear(debit)
    await userEvent.type(debit, '70000')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Kaydet' }))
    expect(within(dialog).getByText('Gerekçe yazın (en az 3 karakter)')).toBeInTheDocument()
    await userEvent.type(within(dialog).getByLabelText('Gerekçe'), 'Disputed invoice')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Kaydet' }))
    await waitFor(() => expect(puts).toHaveLength(1))
    expect(puts[0].headers.get('If-Match')).toBe('"1"')
    expect(puts[0].url.searchParams.get('previewDepotId')).toBe('1')
    expect(puts[0].body).toEqual({
      accountId: 5006,
      depotId: 1,
      debit: '70000.00',
      credit: '0.00',
      reason: 'Disputed invoice',
    })
    await waitFor(() =>
      expect(within(grid()).getByText('DÜZELTİLDİ', { selector: 'span' })).toBeInTheDocument(),
    )
    expect(gets).toHaveLength(1)
  })

  it('offers a reload when the preview changed meanwhile', async () => {
    server.use(
      http.put('/api/orgs/:orgId/ledger/carry-forwards/:year/overrides', () =>
        HttpResponse.json({ code: 'ERR_VERSION_CONFLICT' }, { status: 412 }),
      ),
    )
    const gets = capture('get', '/api/orgs/:orgId/ledger/carry-forwards/:year')
    renderApp('/carry-forward/2026')
    const dialog = await openRow('120.10517', '1')
    await userEvent.type(within(dialog).getByLabelText('Gerekçe'), 'Disputed invoice')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Kaydet' }))
    const conflict = await screen.findByRole('alertdialog', { name: 'Önizleme değişti' })
    expect(conflict).toHaveTextContent('Başka biri önizlemeyi değiştirdi — yeniden yükleyin')
    await userEvent.click(within(conflict).getByRole('button', { name: 'Yeniden yükle' }))
    await waitFor(() => expect(gets).toHaveLength(2))
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('draws the stepper from steps[]', async () => {
    server.use(
      http.get('/api/orgs/:orgId/ledger/carry-forwards/:year', () =>
        HttpResponse.json({
          year: 2026,
          status: 'PREVIEW',
          version: 1,
          checks: [],
          accountCount: 0,
          entryCount: 0,
          totalDebit: '0.00',
          totalCredit: '0.00',
          rows: [],
          steps: [
            { code: 'CLOSE_DOCUMENTS', status: 'DONE' },
            { code: 'STOCK_COUNTS', status: 'BLOCKED' },
            { code: 'ACCOUNT_BALANCES', status: 'TODO' },
            { code: 'OPEN_YEAR', status: 'TODO' },
          ],
        }),
      ),
    )
    renderApp('/carry-forward/2026')
    const steps = await screen.findByRole('list', { name: 'Yıl sonu adımları' })
    await waitFor(() =>
      expect(within(steps).getByText('Stok sayımı kaydedildi').closest('li')).toHaveAttribute(
        'data-status',
        'BLOCKED',
      ),
    )
    expect(within(steps).getByText('Cari bakiyeler').closest('li')).not.toHaveAttribute(
      'aria-current',
    )
  })

  it('removes an override', async () => {
    const dels = capture('delete', '/api/orgs/:orgId/ledger/carry-forwards/:year/overrides')
    renderApp('/carry-forward/2026?depot=1')
    const dialog = await openRow('120.10711', '1')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Hesaplanan tutara dön' }))
    await waitFor(() => expect(dels).toHaveLength(1))
    expect(dels[0].url.searchParams.get('accountId')).toBe('5001')
    expect(dels[0].url.searchParams.get('depotId')).toBe('1')
    expect(dels[0].url.searchParams.get('previewDepotId')).toBe('1')
    expect(dels[0].headers.get('If-Match')).toBe('"1"')
    await waitFor(() =>
      expect(
        within(grid()).queryByText('DÜZELTİLDİ', { selector: 'span' }),
      ).not.toBeInTheDocument(),
    )
  })

  it('posts after confirmation and locks the preview', async () => {
    const posts = capture('post', '/api/orgs/:orgId/ledger/carry-forwards/:year/post')
    renderApp('/carry-forward/2026')
    await userEvent.click(await screen.findByRole('button', { name: '822 açılış kaydını kaydet' }))
    const dialog = await screen.findByRole('alertdialog', { name: 'Açılış kayıtlarını kaydet' })
    expect(dialog).toHaveTextContent('01.01.2026 tarihli 822 kayıt')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Kaydet' }))
    await waitFor(() => expect(posts).toHaveLength(1))
    expect(posts[0].headers.get('Idempotency-Key')).toBeTruthy()
    expect(await screen.findByText('2026 cari devri kaydedildi')).toBeInTheDocument()
    expect(await screen.findByText('Kaydedildi')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /açılış kaydını kaydet/ })).not.toBeInTheDocument()
    await waitFor(() => expect(within(grid()).getAllByText('KAYITLI').length).toBeGreaterThan(0))
  })

  it('downloads the preview PDF for the depot', async () => {
    const calls = capture('get', '/api/orgs/:orgId/ledger/carry-forwards/:year')
    const files = captureDownloads()
    renderApp('/carry-forward/2026?depot=2')
    await userEvent.click(await screen.findByRole('button', { name: 'Önizleme PDF' }))
    await waitFor(() => expect(files).toHaveLength(1))
    const pdf = calls.find((c) => c.headers.get('Accept') === 'application/pdf')!
    expect(pdf.url.searchParams.get('depotId')).toBe('2')
  })

  it('lets an admin reopen a posted year with a reason', async () => {
    const reopens = capture('post', '/api/orgs/:orgId/ledger/carry-forwards/:year/reopen')
    renderApp('/carry-forward/2025', { admin: true })
    expect(await screen.findByText('Kaydedildi')).toBeInTheDocument()
    await userEvent.click(await screen.findByRole('button', { name: 'Yeniden aç…' }))
    const dialog = await screen.findByRole('dialog', { name: '2025 devrini yeniden aç' })
    expect(dialog).toHaveTextContent('2024 yeniden kayda açılır')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Yeniden aç' }))
    expect(within(dialog).getByText('Gerekçe yazın (en az 3 karakter)')).toBeInTheDocument()
    expect(reopens).toHaveLength(0)
    await userEvent.type(within(dialog).getByLabelText('Gerekçe'), 'Late supplier invoice')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Yeniden aç' }))
    await waitFor(() => expect(reopens).toHaveLength(1))
    expect(reopens[0].body).toEqual({ reason: 'Late supplier invoice' })
    expect(reopens[0].headers.get('If-Match')).toBe('"3"')
    expect(reopens[0].headers.get('Idempotency-Key')).toBeTruthy()
    expect(await screen.findByText('Yeniden açıldı')).toBeInTheDocument()
    expect(screen.getByText('Late supplier invoice', { exact: false })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /açılış kaydını kaydet/ })).toBeEnabled()
  })

  it('refuses to reopen while the next year is carried forward', async () => {
    server.use(
      http.post('/api/orgs/:orgId/ledger/carry-forwards/:year/reopen', () =>
        HttpResponse.json({ code: 'ERR_YEAR_CLOSED' }, { status: 409 }),
      ),
    )
    renderApp('/carry-forward/2025', { admin: true })
    await userEvent.click(await screen.findByRole('button', { name: 'Yeniden aç…' }))
    const dialog = await screen.findByRole('dialog')
    await userEvent.type(within(dialog).getByLabelText('Gerekçe'), 'Late supplier invoice')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Yeniden aç' }))
    expect(await within(dialog).findByRole('alert')).toHaveTextContent(
      '2025 yılı kapalı; önce 2026 devrini yeniden açın',
    )
  })

  it('hides reopen from other users', async () => {
    renderApp('/carry-forward/2025')
    expect(await screen.findByText('Kaydedildi')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Yeniden aç…' })).not.toBeInTheDocument()
  })
})
