import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'

vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    user: {
      name: 'Admin',
      role: 'admin',
      organizations: [{ id: 1, name: 'Acme', slug: 'acme' }],
    },
  }),
}))

import DashboardPage from './DashboardPage'

describe('DashboardPage', () => {
  it('renders greeting, status, the user orgs and the admin panel', () => {
    render(<DashboardPage />)
    expect(screen.getByRole('heading', { name: 'Dashboard' })).toBeInTheDocument()
    expect(screen.getByText('Hoş Geldin')).toBeInTheDocument()
    expect(screen.getByText('Aktif')).toBeInTheDocument()
    expect(screen.getByText('Organizasyonların')).toBeInTheDocument()
    expect(screen.getByText('Acme')).toBeInTheDocument()
    expect(screen.getByText('Admin Paneli')).toBeInTheDocument()
  })
})
