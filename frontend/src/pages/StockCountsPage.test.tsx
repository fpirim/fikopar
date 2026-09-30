import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderApp } from '../test/renderApp'
import { capture } from '../test/msw'
import { findDialog } from '../test/dialog'

const location = () => screen.getByTestId('location').textContent

describe('StockCountsPage', () => {
  it('lists counts with depot codes, status, progress and value', async () => {
    renderApp('/stock-counts')
    expect(await screen.findByRole('heading', { name: 'Stok sayımları' })).toBeInTheDocument()
    const open = (await screen.findByRole('link', { name: 'SAY-2026-1-000002' })).closest('tr')!
    expect(open).toHaveTextContent('SÜRÜYOR')
    expect(open).toHaveTextContent('28.09.2026')
    expect(open).toHaveTextContent(/412 \/ [\d.]+/)
    expect(screen.getAllByRole('link', { name: /^SAY-/ })[0]).toHaveTextContent('SAY-2026-1-000002')
    const done = screen.getByRole('link', { name: 'SAY-2026-1-000001' }).closest('tr')!
    expect(done).toHaveTextContent('KAPANDI')
    expect(done).toHaveTextContent('−8.100,00')
  })

  it('filters by the top-bar depot', async () => {
    const calls = capture('get', '/api/orgs/:orgId/stock/counts')
    renderApp('/stock-counts?depot=2')
    expect(await screen.findByText('Henüz sayım yok.')).toBeInTheDocument()
    expect(calls[0].url.searchParams.get('depotId')).toBe('2')
  })

  it('starts a count and opens it', async () => {
    const calls = capture('post', '/api/orgs/:orgId/stock/counts')
    renderApp('/stock-counts')
    await userEvent.click(await screen.findByRole('button', { name: 'Sayım başlat' }))
    const dialog = await findDialog('dialog')
    await userEvent.selectOptions(within(dialog).getByLabelText('Depo'), '2')
    await userEvent.type(within(dialog).getByLabelText('Raf başı'), 'E')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Başlat' }))
    await waitFor(() => expect(location()).toMatch(/^\/stock-counts\/\d+/))
    expect(calls[0].body).toMatchObject({ depotId: 2, shelfFrom: 'E' })
    expect(calls[0].headers.get('Idempotency-Key')).toBeTruthy()
    expect(await screen.findByRole('heading', { name: 'SAY-2026-2-000001' })).toBeInTheDocument()
  })

  it('links to the open count when the depot already has one', async () => {
    renderApp('/stock-counts')
    await userEvent.click(await screen.findByRole('button', { name: 'Sayım başlat' }))
    const dialog = await findDialog('dialog')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Başlat' }))
    expect(await within(dialog).findByText('Depo 1 için açık bir sayım var:')).toBeInTheDocument()
    await userEvent.click(within(dialog).getByRole('link', { name: 'SAY-2026-1-000002' }))
    await waitFor(() => expect(location()).toBe('/stock-counts/2'))
  })
})
