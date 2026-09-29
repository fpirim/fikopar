import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/node'
import { renderApp } from '../test/renderApp'
import { capture } from '../test/msw'
import { formatTime } from '../lib/format'

const kpi = (name: string) => screen.getByRole('group', { name })

describe('DashboardPage', () => {
  it('shows the four tiles from one dashboard call', async () => {
    renderApp('/')
    expect(await screen.findByRole('heading', { name: 'Bugün' })).toBeInTheDocument()
    expect(await screen.findByText('29.09.2026 · TRY')).toBeInTheDocument()
    await waitFor(() => expect(kpi('Alacaklar')).toHaveTextContent('2.602.417,00'))
    expect(kpi('Alacaklar')).toHaveTextContent('244 hesap')
    expect(kpi('Borçlar')).toHaveTextContent('596.181,00')
    expect(kpi('Borçlar')).toHaveTextContent('72 tedarikçi')
    expect(kpi('Kasadaki nakit')).toHaveTextContent('64.765,50')
    expect(kpi('Kasadaki nakit')).toHaveTextContent('Depo 1 · Depo 2')
    expect(kpi('Vadesi 30 gün içinde çekler')).toHaveTextContent('386.000,00')
    expect(within(kpi('Vadesi 30 gün içinde çekler')).getByText('3 vadesi geçmiş')).toHaveClass(
      'text-fk-negative',
    )
    expect(kpi('Alacaklar').closest('a')).toHaveAttribute('href', '/balances')
    expect(kpi('Kasadaki nakit').closest('a')).toHaveAttribute('href', '/cash-book')
    expect(screen.getByRole('link', { name: 'Yeni tahsilat' })).toHaveAttribute('href', '/receipts')
    expect(screen.getByRole('link', { name: 'Yeni fatura' })).toHaveAttribute('href', '/invoices')
  })

  it('lists the latest documents and the cheques due this week', async () => {
    renderApp('/')
    const latest = await screen.findByRole('table', { name: 'Son belgeler' })
    await waitFor(() => expect(within(latest).getAllByRole('row')).toHaveLength(7))
    const first = within(latest).getAllByRole('row')[1]
    expect(first).toHaveTextContent('SF-2026-1-000418')
    expect(first).toHaveTextContent('SATIŞ FAT.')
    expect(first).toHaveTextContent('Yılmaz Tarım Makinaları')
    expect(first).toHaveTextContent('48.720,00')
    const due = screen.getByRole('region', { name: 'Bu hafta vadesi gelenler' })
    expect(within(due).getByText('−4 G')).toHaveClass('bg-fk-chip-red')
    expect(within(due).getByText('2 G')).toHaveClass('bg-fk-chip-amber')
    expect(within(due).getByText('5 G')).toHaveClass('bg-fk-chip-blue')
    expect(due).toHaveTextContent('AC-000071 · Kaya Ziraat')
    expect(due).toHaveTextContent('24.000,00')
  })

  it('lists parts below minimum with negatives first and links to the parts grid', async () => {
    renderApp('/')
    const card = await screen.findByRole('region', { name: 'Minimum altı / eksi' })
    await waitFor(() => expect(card).toHaveTextContent('365 minimum altı · 5 eksi'))
    const rows = within(card).getAllByRole('listitem')
    expect(rows).toHaveLength(10)
    expect(rows[0]).toHaveTextContent('OF-0457')
    expect(rows[0]).toHaveTextContent('Oil filter, hydraulic')
    expect(rows[0]).toHaveTextContent('−3 / 5')
    expect(within(rows[0]).getByText('−3 / 5')).toHaveClass('text-fk-negative')
    expect(within(rows[0]).getByRole('link')).toHaveAttribute('href', '/parts/1010')
    expect(within(card).getByRole('link', { name: 'Tümü' })).toHaveAttribute(
      'href',
      '/parts?stock=BELOW_MIN_OR_NEGATIVE',
    )
    expect(screen.getByText('DENGEDE')).toBeInTheDocument()
    expect(
      screen.getByText(`Defter kontrolü ${formatTime('2026-09-29T02:00:00Z').slice(0, 5)}`),
    ).toBeInTheDocument()
  })

  it('follows the top-bar depot', async () => {
    const calls = capture('get', '/api/orgs/:orgId/dashboard')
    renderApp('/?depot=2')
    const card = await screen.findByRole('region', { name: 'Minimum altı / eksi' })
    await waitFor(() => expect(card).toHaveTextContent('115 minimum altı · 3 eksi'))
    expect(calls[0].url.searchParams.get('depotId')).toBe('2')
    expect(within(card).getByRole('link', { name: 'Tümü' })).toHaveAttribute(
      'href',
      '/parts?stock=BELOW_MIN_OR_NEGATIVE&depot=2',
    )
    expect(kpi('Kasadaki nakit')).toHaveTextContent('17.950,00')
    expect(kpi('Kasadaki nakit')).toHaveTextContent('Depo 2')
    await userEvent.click(within(card).getAllByRole('link')[0])
    expect(screen.getByTestId('location')).toHaveTextContent('depot=2')
  })

  it('shows dashes, not numbers, when the dashboard fails', async () => {
    server.use(
      http.get('/api/orgs/:orgId/dashboard', () =>
        HttpResponse.json({ code: 'ERR_INTERNAL' }, { status: 500 }),
      ),
    )
    renderApp('/')
    expect(await screen.findByRole('alert')).toHaveTextContent('Sunucu hatası')
    expect(kpi('Alacaklar')).toHaveTextContent('—')
    expect(kpi('Borçlar')).toHaveTextContent('—')
  })

  it('marks a ledger that has not been checked and an empty below-minimum list', async () => {
    server.use(
      http.get('/api/orgs/:orgId/dashboard', () =>
        HttpResponse.json({
          date: '2026-09-29',
          receivables: { total: '0.00', accounts: 0 },
          payables: { total: '0.00', accounts: 0 },
          cash: { total: '0.00', depots: [] },
          instrumentsDue30: { total: '0.00', overdueCount: 0 },
          latest: [],
          dueThisWeek: [],
          belowMin: [],
          belowMinCount: 0,
          negativeCount: 0,
          ledgerCheck: { status: 'DRIFT', checkedAt: null },
        }),
      ),
    )
    renderApp('/')
    expect(await screen.findByText('FARK VAR')).toHaveClass('bg-fk-chip-red')
    expect(screen.getByText('Defter kontrolü —')).toBeInTheDocument()
    const card = screen.getByRole('region', { name: 'Minimum altı / eksi' })
    expect(card).toHaveTextContent('Minimum altında parça yok')
    expect(screen.getByRole('region', { name: 'Bu hafta vadesi gelenler' })).toHaveTextContent(
      'Bu hafta vadesi gelen yok',
    )
    expect(kpi('Vadesi 30 gün içinde çekler')).toHaveTextContent('Vadesi geçmiş yok')
  })
})
