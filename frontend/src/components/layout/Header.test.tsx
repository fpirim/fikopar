import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'

// Per-test shared state read by the useAuth/useOrganization mocks.
const orgsAll = [
  { id: 1, name: 'Acme', logo_url: null },
  { id: 2, name: 'Beta', logo_url: null },
]
const state: {
  role: 'admin' | 'user'
  orgs: typeof orgsAll
  selected: (typeof orgsAll)[number] | null
} = {
  role: 'user',
  orgs: orgsAll,
  selected: orgsAll[0],
}
const logout = vi.fn()
const setSelectedId = vi.fn()

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({
    user: { role: state.role, organizations: state.orgs },
    logout,
  }),
}))
vi.mock('../../context/ThemeContext', () => ({
  useTheme: () => ({ theme: 'light', toggleTheme: vi.fn() }),
}))
vi.mock('../../context/OrganizationContext', () => ({
  useOrganization: () => ({
    organizations: state.orgs,
    selected: state.selected,
    selectedId: state.selected?.id ?? null,
    setSelectedId,
    refresh: vi.fn(),
    isLoading: false,
  }),
}))

import Header from './Header'

beforeEach(() => {
  vi.clearAllMocks()
  localStorage.clear()
  state.role = 'user'
  state.orgs = orgsAll
  state.selected = orgsAll[0]
})

describe('Header — org picker', () => {
  it('user with multiple orgs: shows current org, dropdown lists others', async () => {
    const u = userEvent.setup()
    state.role = 'user'
    state.orgs = orgsAll
    state.selected = orgsAll[0]
    render(<Header />)

    expect(screen.getByText('Acme')).toBeInTheDocument()
    await u.click(screen.getByText('Acme'))
    expect(await screen.findByRole('button', { name: 'Beta' })).toBeInTheDocument()

    await u.click(screen.getByRole('button', { name: /çıkış/i }))
    expect(logout).toHaveBeenCalled()
  })

  it('user with a single org: dropdown is NOT openable (nothing to switch to)', async () => {
    const u = userEvent.setup()
    state.role = 'user'
    state.orgs = [orgsAll[0]]
    state.selected = orgsAll[0]
    render(<Header />)

    expect(screen.getByText('Acme')).toBeInTheDocument()
    await u.click(screen.getByText('Acme'))
    expect(screen.queryByRole('button', { name: 'Beta' })).not.toBeInTheDocument()
  })

  it('admin with no selection: clicking opens the dropdown so admin can pick', async () => {
    const u = userEvent.setup()
    state.role = 'admin'
    state.orgs = orgsAll
    state.selected = null
    render(<Header />)

    expect(screen.getByText('Organizasyon Seçin')).toBeInTheDocument()

    await u.click(screen.getByText('Organizasyon Seçin'))
    expect(await screen.findByRole('button', { name: 'Acme' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Beta' })).toBeInTheDocument()

    await u.click(screen.getByRole('button', { name: 'Acme' }))
    expect(setSelectedId).toHaveBeenCalledWith(1)
  })

  it('admin with a single org: dropdown still openable (no auto-select)', async () => {
    const u = userEvent.setup()
    state.role = 'admin'
    state.orgs = [orgsAll[0]]
    state.selected = null
    render(<Header />)

    await u.click(screen.getByText('Organizasyon Seçin'))
    const item = await screen.findByRole('button', { name: 'Acme' })
    await u.click(item)
    expect(setSelectedId).toHaveBeenCalledWith(1)
  })

  it('admin with zero orgs: dropdown stays closed', async () => {
    const u = userEvent.setup()
    state.role = 'admin'
    state.orgs = []
    state.selected = null
    render(<Header />)

    expect(screen.getByText('Organizasyon Seçin')).toBeInTheDocument()
    await u.click(screen.getByText('Organizasyon Seçin'))
    expect(screen.queryByRole('button', { name: 'Acme' })).not.toBeInTheDocument()
  })
})
