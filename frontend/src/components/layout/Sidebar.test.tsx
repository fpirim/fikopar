import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Sidebar from './Sidebar'
import { useAuth } from '../../hooks/useAuth'

vi.mock('../../hooks/useAuth', () => ({ useAuth: vi.fn() }))
const mockedUseAuth = vi.mocked(useAuth)

function renderSidebar() {
  return render(
    <MemoryRouter>
      <Sidebar />
    </MemoryRouter>,
  )
}

beforeEach(() => mockedUseAuth.mockReset())

describe('Sidebar', () => {
  it('hides admin links for a normal user', () => {
    mockedUseAuth.mockReturnValue({ user: { role: 'user' } } as never)
    renderSidebar()
    expect(screen.queryAllByText('Kullanıcılar')).toHaveLength(0)
    expect(screen.queryAllByText('Organizasyonlar')).toHaveLength(0)
    // Common links are always present.
    expect(screen.queryAllByText('Dashboard').length).toBeGreaterThan(0)
  })

  it('shows admin links for an admin', () => {
    mockedUseAuth.mockReturnValue({ user: { role: 'admin' } } as never)
    renderSidebar()
    expect(screen.queryAllByText('Kullanıcılar').length).toBeGreaterThan(0)
    expect(screen.queryAllByText('Organizasyonlar').length).toBeGreaterThan(0)
  })
})
