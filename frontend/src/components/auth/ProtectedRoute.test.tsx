import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import ProtectedRoute from './ProtectedRoute'
import { useAuth } from '../../hooks/useAuth'

vi.mock('../../hooks/useAuth', () => ({ useAuth: vi.fn() }))
const mockedUseAuth = vi.mocked(useAuth)

function renderAt() {
  return render(
    <MemoryRouter initialEntries={['/secret']}>
      <Routes>
        <Route element={<ProtectedRoute />}>
          <Route path="/secret" element={<div>secret content</div>} />
        </Route>
        <Route path="/login" element={<div>login page</div>} />
      </Routes>
    </MemoryRouter>,
  )
}

beforeEach(() => mockedUseAuth.mockReset())

describe('ProtectedRoute', () => {
  it('renders neither content nor login while loading', () => {
    mockedUseAuth.mockReturnValue({ token: null, isLoading: true } as never)
    renderAt()
    expect(screen.queryByText('secret content')).toBeNull()
    expect(screen.queryByText('login page')).toBeNull()
  })

  it('redirects to /login when there is no token', () => {
    mockedUseAuth.mockReturnValue({ token: null, isLoading: false } as never)
    renderAt()
    expect(screen.getByText('login page')).toBeInTheDocument()
  })

  it('renders the protected outlet when authenticated', () => {
    mockedUseAuth.mockReturnValue({ token: 'abc', isLoading: false } as never)
    renderAt()
    expect(screen.getByText('secret content')).toBeInTheDocument()
  })
})
