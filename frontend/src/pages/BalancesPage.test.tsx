import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import i18n from '../i18n'
import { renderApp } from '../test/renderApp'
import { capture, captureDownloads } from '../test/msw'

const pinned = () => document.querySelector('.ag-floating-bottom') as HTMLElement
const location = () => screen.getByTestId('location').textContent
const firstRow = () => screen.findAllByRole('link', { name: /^120\./ })

async function filterKonyaCustomers() {
  await userEvent.type(await screen.findByLabelText('Şehir filtresi'), 'Konya')
  await userEvent.selectOptions(screen.getByLabelText('Tip filtresi'), 'MUSTERI')
  await userEvent.type(screen.getByLabelText('Bakiye filtresi'), '> 10.000')
  await waitFor(() => expect(pinned()).toHaveTextContent('6 / 412 · 3 filtre'), { timeout: 3000 })
}

describe('BalancesPage', () => {
  it('loads balances as of the date with a pinned total row', async () => {
    const calls = capture('post', '/api/orgs/:orgId/ledger/balances/grid')
    renderApp('/balances?asOf=2026-09-29')
    expect(await screen.findByRole('heading', { name: 'Cari bakiyeler' })).toBeInTheDocument()
    await firstRow()
    await waitFor(() => expect(pinned()).toHaveTextContent('412 / 412'))
    expect(calls[0].body).toMatchObject({
      asOf: '2026-09-29',
      depotId: null,
      sicilTipi: null,
      startRow: 0,
      sortModel: [{ colId: 'accountCode', sort: 'asc' }],
    })
    expect(screen.getByLabelText('Tarih itibarıyla')).toHaveValue('29.09.2026')
  })

  it('combines column filters and sums the filtered rows', async () => {
    const calls = capture('post', '/api/orgs/:orgId/ledger/balances/grid')
    renderApp('/balances?asOf=2026-09-29')
    await filterKonyaCustomers()
    expect(pinned()).toHaveTextContent('1.467.065,00')
    expect(pinned()).toHaveTextContent('1.184.640,00')
    expect(pinned()).toHaveTextContent('282.425,00')
    expect(pinned()).toHaveTextContent('14.015,00')
    expect(calls[calls.length - 1].body).toMatchObject({
      filterModel: {
        city: { filterType: 'text', type: 'contains', filter: 'Konya' },
        sicilTipi: { filterType: 'text', type: 'equals', filter: 'MUSTERI' },
        balance: { filterType: 'number', type: 'greaterThan', filter: 10000 },
      },
    })
    expect(screen.getByText(/filtreler: Şehir, Tip, Bakiye/)).toBeInTheDocument()
    expect(screen.getByText('1–6 / 6')).toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: 'Filtreleri temizle' }))
    await waitFor(() => expect(pinned()).toHaveTextContent('412 / 412'))
    expect(screen.getByLabelText('Şehir filtresi')).toHaveValue('')
  })

  it('sends BalanceRow names as column ids', async () => {
    const calls = capture('post', '/api/orgs/:orgId/ledger/balances/grid')
    renderApp('/balances?asOf=2026-09-29')
    await userEvent.type(await screen.findByLabelText('Ünvan filtresi'), 'Tarım')
    await userEvent.type(screen.getByLabelText('Borç filtresi'), '> 1')
    await waitFor(() =>
      expect(calls[calls.length - 1].body).toMatchObject({
        filterModel: {
          accountName: { filterType: 'text', type: 'contains', filter: 'Tarım' },
          debitTotal: { filterType: 'number', type: 'greaterThan', filter: 1 },
        },
      }),
    )
    await waitFor(() => expect(pinned()).toHaveTextContent('2 filtre'))
    expect(screen.getByText(/filtreler: Ünvan, Borç/)).toBeInTheDocument()
  })

  it('formats totals in English', async () => {
    renderApp('/balances?asOf=2026-09-29')
    await i18n.changeLanguage('en')
    await userEvent.type(await screen.findByLabelText('Filter City'), 'Konya')
    await userEvent.selectOptions(screen.getByLabelText('Filter Type'), 'MUSTERI')
    await userEvent.type(screen.getByLabelText('Filter Balance'), '> 10,000')
    await waitFor(() => expect(pinned()).toHaveTextContent('6 of 412 · 3 filters'), {
      timeout: 3000,
    })
    expect(pinned()).toHaveTextContent('1,467,065.00')
  })

  it('narrows by account type, depot and date', async () => {
    const calls = capture('post', '/api/orgs/:orgId/ledger/balances/grid')
    renderApp('/balances?asOf=2026-09-29&depot=2')
    await firstRow()
    await userEvent.click(screen.getByRole('button', { name: 'Bankalar' }))
    await waitFor(() => expect(pinned()).toHaveTextContent('4 / 4'))
    expect(calls[calls.length - 1].body).toMatchObject({ sicilTipi: 'BANKA', depotId: 2 })
    expect(location()).toContain('type=BANKA')

    const asOf = screen.getByLabelText('Tarih itibarıyla')
    await userEvent.clear(asOf)
    await userEvent.type(asOf, '30.06.2026{Enter}')
    await waitFor(() => expect(calls[calls.length - 1].body).toMatchObject({ asOf: '2026-06-30' }))
  })

  it('prints statements for the selected accounts', async () => {
    const prints = capture('post', '/api/orgs/:orgId/ledger/statements/print')
    const letters = capture('post', '/api/orgs/:orgId/ledger/reminder-letters')
    const files = captureDownloads()
    renderApp('/balances?asOf=2026-09-29')
    await firstRow()
    const pdf = screen.getByRole('button', { name: 'Ekstreler PDF' })
    expect(pdf).toBeDisabled()
    const rows = screen.getAllByRole('row').filter((r) => r.getAttribute('row-index') !== null)
    await userEvent.click(within(rows[0]).getByRole('checkbox'))
    await userEvent.click(within(rows[1]).getByRole('checkbox'))
    expect(screen.getByText('2 seçili')).toBeInTheDocument()
    await userEvent.click(pdf)
    await waitFor(() => expect(files).toHaveLength(1))
    expect(prints[0].body).toMatchObject({ to: '2026-09-29', language: 'tr' })
    expect((prints[0].body as { accountIds: number[] }).accountIds).toHaveLength(2)
    expect(prints[0].headers.get('Accept')).toBe('application/pdf')
    await userEvent.click(screen.getByRole('button', { name: 'Hatırlatma mektupları' }))
    await waitFor(() => expect(letters).toHaveLength(1))
  })

  it('exports Excel from the server and CSV in the client', async () => {
    const grid = capture('post', '/api/orgs/:orgId/ledger/balances/grid')
    const files = captureDownloads()
    renderApp('/balances?asOf=2026-09-29')
    await filterKonyaCustomers()
    await userEvent.click(screen.getByRole('button', { name: 'Excel' }))
    await waitFor(() => expect(files).toHaveLength(1))
    const xlsx = grid.find((c) => c.headers.get('Accept')?.includes('spreadsheetml'))!
    expect(xlsx.body).toMatchObject({ startRow: 0, endRow: 6, filterModel: { city: {} } })

    await userEvent.click(screen.getByRole('button', { name: 'CSV' }))
    await waitFor(() => expect(files).toHaveLength(2))
    const csv = await files[1].text()
    expect(csv.split('\r\n')[0]).toBe(
      'Hesap;Ünvan;Şehir;Tip;Borç;Alacak;Bakiye;Vadesi geçen;Son hareket',
    )
    expect(csv).toContain(
      '120.10482;Yılmaz Tarım Makinaları;Konya;MUSTERI;1204880.00;1018540.00;186340.00;0.00;2026-09-29',
    )
    expect(csv.trim().split('\r\n')).toHaveLength(7)
  })

  it('hides a column from the column menu', async () => {
    renderApp('/balances?asOf=2026-09-29')
    await firstRow()
    await userEvent.click(screen.getByRole('button', { name: 'Sütunlar' }))
    await userEvent.click(await screen.findByRole('checkbox', { name: 'Şehir' }))
    await waitFor(() =>
      expect(screen.queryByRole('textbox', { name: 'Şehir filtresi' })).not.toBeInTheDocument(),
    )
  })

  it('opens the statement from the account link', async () => {
    renderApp('/balances?asOf=2026-09-29&depot=1')
    await userEvent.type(await screen.findByLabelText('Hesap filtresi'), '120.10482')
    await userEvent.click(await screen.findByRole('link', { name: '120.10482' }))
    expect(location()).toBe('/statements/5000?depot=1')
  })
})
