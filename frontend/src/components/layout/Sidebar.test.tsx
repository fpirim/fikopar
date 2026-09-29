import { describe, expect, it } from 'vitest'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderApp } from '../../test/renderApp'

describe('Sidebar', () => {
  it('shows the wordmark and the six sections', async () => {
    renderApp('/balances')
    const nav = await screen.findByRole('navigation', { name: 'Ana menü' })
    expect(within(nav).getByText('FIKOPAR')).toBeInTheDocument()
    for (const s of ['Genel bakış', 'Ticaret', 'Cari', 'Finans', 'Stok', 'Sistem']) {
      expect(within(nav).getByText(s)).toBeInTheDocument()
    }
    const link = within(nav).getByRole('link', { name: /Bakiyeler/ })
    expect(link).toHaveAttribute('aria-current', 'page')
    expect(within(link).getByText('BAL')).toBeInTheDocument()
  })

  it('keeps the depot in nav links', async () => {
    renderApp('/balances?depot=2')
    const nav = await screen.findByRole('navigation', { name: 'Ana menü' })
    expect(within(nav).getByRole('link', { name: /Kasa defteri/ })).toHaveAttribute(
      'href',
      '/cash-book?depot=2',
    )
  })

  it('switches company from the switcher', async () => {
    renderApp('/balances')
    const button = await screen.findByRole('button', { name: /Demo Company A/ })
    expect(button).toHaveTextContent('Şirket · 1 / 2')
    await userEvent.click(button)
    await userEvent.click(await screen.findByRole('button', { name: 'Demo Company B' }))
    expect(await screen.findByRole('button', { name: /Demo Company B/ })).toHaveTextContent(
      'Şirket · 2 / 2',
    )
    expect(localStorage.getItem('selectedOrganizationId')).toBe('2')
  })
})
