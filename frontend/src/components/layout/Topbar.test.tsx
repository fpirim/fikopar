import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import i18n from '../../i18n'
import { renderApp } from '../../test/renderApp'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/node'

const location = () => screen.getByTestId('location').textContent

describe('Topbar', () => {
  it('shows the breadcrumb of the current screen', async () => {
    renderApp('/balances')
    const bar = await screen.findByRole('banner')
    expect(within(bar).getByText('Cari / Bakiyeler')).toBeInTheDocument()
  })

  it('sets the depot in the URL', async () => {
    renderApp('/balances?asOf=2026-09-29')
    const group = await screen.findByRole('group', { name: 'Depo' })
    const all = within(group).getByRole('button', { name: 'Tüm depolar' })
    expect(all).toHaveAttribute('aria-pressed', 'true')
    await userEvent.click(await within(group).findByRole('button', { name: 'Depo 2' }))
    expect(location()).toBe('/balances?asOf=2026-09-29&depot=2')
    expect(within(group).getByRole('button', { name: 'Depo 2' })).toHaveAttribute(
      'aria-pressed',
      'true',
    )
    await userEvent.click(all)
    expect(location()).toBe('/balances?asOf=2026-09-29')
  })

  it('switches the language', async () => {
    renderApp('/balances')
    const group = await screen.findByRole('group', { name: 'Dil' })
    await userEvent.click(within(group).getByRole('button', { name: 'EN' }))
    expect(i18n.language).toBe('en')
    expect(await screen.findByText('Accounts / Balances')).toBeInTheDocument()
  })

  it('shows DEMO for the demo user and logs out from the account menu', async () => {
    renderApp('/balances')
    expect(await screen.findByText('DEMO')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Hesap menüsü' }))
    expect(await screen.findByText('demo@fikopar.local')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Profil' })).toHaveAttribute('href', '/profile')
    expect(screen.getByRole('button', { name: 'Çıkış' })).toBeInTheDocument()
  })

  it('searches accounts and opens the statement', async () => {
    server.use(
      http.get('/api/orgs/:orgId/sicils/:sicilId/summary', () =>
        HttpResponse.json({ code: 'ERR_INTERNAL' }, { status: 500 }),
      ),
    )
    renderApp('/balances')
    const box = await screen.findByRole('searchbox', { name: 'Ara' })
    await userEvent.type(box, 'Yılmaz')
    const hit = await screen.findByRole('option', { name: /Yılmaz Tarım Makinaları/ })
    await userEvent.click(hit)
    await waitFor(() => expect(location()).toBe('/statements/5000'))
  })

  it('opens a document hit in the journal', async () => {
    renderApp('/balances')
    const box = await screen.findByRole('searchbox', { name: 'Ara' })
    await userEvent.type(box, 'TAH-2026-1-000233')
    await screen.findByRole('option', { name: /TAH-2026-1-000233/ })
    await userEvent.keyboard('{ArrowDown}{Enter}')
    await waitFor(() => expect(location()).toBe('/journal?q=TAH-2026-1-000233'))
  })
})
