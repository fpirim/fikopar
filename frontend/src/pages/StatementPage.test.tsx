import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import i18n from '../i18n'
import { renderApp } from '../test/renderApp'
import { capture, captureDownloads } from '../test/msw'

const URL_ = '/statements/5000?from=2026-01-01&to=2026-09-29'
const location = () => screen.getByTestId('location').textContent
const grid = () => document.querySelector('.fk-grid') as HTMLElement

describe('StatementPage', () => {
  it('shows brought-forward rows, running balance and depot totals', async () => {
    const calls = capture('get', '/api/orgs/:orgId/ledger/accounts/:accountId/statement')
    renderApp(URL_)
    expect(
      await screen.findByRole('heading', { name: 'Yılmaz Tarım Makinaları' }),
    ).toBeInTheDocument()
    expect(screen.getByText('120.10482')).toBeInTheDocument()
    expect(await screen.findByText('MÜŞTERİ')).toBeInTheDocument()
    expect(screen.getByText('01.01.2026 – 29.09.2026 · tüm depolar')).toBeInTheDocument()
    await waitFor(() => expect(within(grid()).getAllByText('Devreden')).toHaveLength(2))
    expect(within(grid()).getAllByText('142.300,00')).toHaveLength(2)
    expect(within(grid()).getByText('SF-2026-1-000418')).toBeInTheDocument()
    expect(within(grid()).getAllByText('186.340,00').length).toBeGreaterThan(0)
    const totals = screen.getByRole('table', { name: 'Depo toplamları' })
    expect(within(totals).getByText('168.170,00 B')).toBeInTheDocument()
    expect(within(totals).getByText('284.820,00')).toBeInTheDocument()
    expect(within(totals).getByText('98.480,00')).toBeInTheDocument()
    expect(within(totals).getByText('186.340,00 B')).toBeInTheDocument()
    expect(calls[0].url.searchParams.get('from')).toBe('2026-01-01')
    expect(calls[0].url.searchParams.has('depotId')).toBe(false)
  })

  it('fills the credit panel and the account card', async () => {
    renderApp(URL_)
    const credit = await screen.findByRole('region', { name: 'Kredi' })
    await waitFor(() => expect(within(credit).getByText('250.000,00')).toBeInTheDocument())
    expect(within(credit).getByRole('progressbar')).toHaveAttribute('aria-valuenow', '75')
    expect(within(credit).getByText('30 gün')).toBeInTheDocument()
    expect(within(credit).getByText('%10 + %5')).toBeInTheDocument()
    expect(within(credit).getByText('55.000,00')).toBeInTheDocument()
    const card = screen.getByRole('region', { name: 'Hesap kartı' })
    expect(within(card).getByText('Mehmet Yılmaz · purchasing')).toBeInTheDocument()
    expect(within(card).getByText('+90 332 000 00 00')).toBeInTheDocument()
    expect(within(card).getByText('Selçuklu / Konya')).toBeInTheDocument()
    expect(within(card).getByText('Vergi dairesi Selçuk · [TAX NO]')).toBeInTheDocument()
  })

  it('follows the depot switch and the period', async () => {
    const calls = capture('get', '/api/orgs/:orgId/ledger/accounts/:accountId/statement')
    renderApp(`${URL_}&depot=1`)
    expect(await screen.findByText('01.01.2026 – 29.09.2026 · depo 1')).toBeInTheDocument()
    await waitFor(() => expect(calls[0].url.searchParams.get('depotId')).toBe('1'))
    await waitFor(() => expect(within(grid()).getAllByText('Devreden')).toHaveLength(1))

    await userEvent.click(screen.getByRole('button', { name: 'Dönemi değiştir' }))
    const from = await screen.findByLabelText('Başlangıç')
    await userEvent.clear(from)
    await userEvent.type(from, '10.09.2026')
    await userEvent.click(screen.getByRole('button', { name: 'Uygula' }))
    await waitFor(() => expect(location()).toContain('from=2026-09-10'))
    await waitFor(() =>
      expect(calls[calls.length - 1].url.searchParams.get('from')).toBe('2026-09-10'),
    )
    expect(await screen.findByText('10.09.2026 – 29.09.2026 · depo 1')).toBeInTheDocument()
  })

  it('filters lines by type in the client', async () => {
    renderApp(URL_)
    await waitFor(() => expect(within(grid()).getByText('TAH-2026-1-000219')).toBeInTheDocument())
    await userEvent.selectOptions(screen.getByLabelText('Tip filtresi'), 'SALES_INVOICE')
    await waitFor(() =>
      expect(within(grid()).queryByText('TAH-2026-1-000219')).not.toBeInTheDocument(),
    )
    expect(within(grid()).getByText('SF-2026-1-000418')).toBeInTheDocument()
  })

  it('downloads Excel and PDF from the server', async () => {
    const calls = capture('get', '/api/orgs/:orgId/ledger/accounts/:accountId/statement')
    const files = captureDownloads()
    renderApp(URL_)
    await screen.findByRole('heading', { name: 'Yılmaz Tarım Makinaları' })
    await userEvent.click(screen.getByRole('button', { name: 'Excel' }))
    await userEvent.click(screen.getByRole('button', { name: 'PDF' }))
    await waitFor(() => expect(files).toHaveLength(2))
    const accepts = calls.map((c) => c.headers.get('Accept'))
    expect(accepts).toContain('application/pdf')
    expect(accepts.some((a) => a?.includes('spreadsheetml'))).toBe(true)
  })

  it('edits the account card', async () => {
    const puts = capture('put', '/api/orgs/:orgId/sicils/:sicilId')
    renderApp(URL_)
    await userEvent.click(await screen.findByRole('button', { name: 'Kartı düzenle' }))
    const dialog = await screen.findByRole('dialog', { name: 'Hesap kartını düzenle' })
    const city = within(dialog).getByLabelText('İlçe')
    await userEvent.clear(city)
    await userEvent.type(city, 'Karatay')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Kaydet' }))
    await waitFor(() => expect(puts).toHaveLength(1))
    expect(puts[0].body).toMatchObject({ ilce: 'Karatay', sehir: 'Konya', gun: '30' })
    expect(await screen.findByText('Karatay / Konya')).toBeInTheDocument()
  })

  it('picks an account when none is open', async () => {
    renderApp('/statements')
    await userEvent.type(await screen.findByRole('combobox', { name: 'Hesap' }), 'Kaya')
    await userEvent.click(await screen.findByRole('option', { name: /Kaya Ziraat/ }))
    await waitFor(() => expect(location()).toBe('/statements/5006'))
  })

  it('writes the period in English', async () => {
    renderApp(URL_)
    await i18n.changeLanguage('en')
    expect(await screen.findByText('01.01.2026 – 29.09.2026 · all depots')).toBeInTheDocument()
    expect(await screen.findByText('186,340.00 Dr')).toBeInTheDocument()
  })
})
