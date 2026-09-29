import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, waitFor, act } from '@testing-library/react'
import { OrganizationProvider, useOrganization } from './OrganizationContext'
import api from '../lib/api'

vi.mock('../lib/api', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() },
}))

const authState: { user: any } = { user: null }
vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({ user: authState.user }),
}))

function Probe({ onState }: { onState: (s: ReturnType<typeof useOrganization>) => void }) {
  const s = useOrganization()
  onState(s)
  return null
}

function renderWith(spy: (s: ReturnType<typeof useOrganization>) => void) {
  return render(
    <OrganizationProvider>
      <Probe onState={spy} />
    </OrganizationProvider>,
  )
}

beforeEach(() => {
  vi.clearAllMocks()
  localStorage.clear()
  authState.user = null
})

describe('OrganizationContext', () => {
  it('admin: loads all orgs from /admin/organizations without auto-selecting', async () => {
    authState.user = { id: 1, role: 'admin', organizations: [] }
    ;(api.get as any).mockResolvedValue({
      data: [
        { id: 10, name: 'A' },
        { id: 20, name: 'B' },
      ],
    })

    let latest: any
    renderWith((s) => (latest = s))
    await waitFor(() => expect(latest.organizations).toHaveLength(2))
    expect(api.get).toHaveBeenCalledWith('/admin/organizations')
    expect(latest.selected).toBeNull()
    expect(latest.selectedId).toBeNull()
  })

  it('admin: selects the stored id when it is valid', async () => {
    localStorage.setItem('selectedOrganizationId', '20')
    authState.user = { id: 1, role: 'admin', organizations: [] }
    ;(api.get as any).mockResolvedValue({
      data: [
        { id: 10, name: 'A' },
        { id: 20, name: 'B' },
      ],
    })

    let latest: any
    renderWith((s) => (latest = s))
    await waitFor(() => expect(latest.selected?.id).toBe(20))
  })

  it('refresh() calls /admin/organizations again', async () => {
    authState.user = { id: 1, role: 'admin', organizations: [] }
    ;(api.get as any).mockResolvedValue({ data: [{ id: 10, name: 'A' }] })

    let latest: any
    renderWith((s) => (latest = s))
    await waitFor(() => expect(latest.organizations).toHaveLength(1))
    expect(api.get).toHaveBeenCalledTimes(1)
    ;(api.get as any).mockResolvedValue({
      data: [
        { id: 10, name: 'A' },
        { id: 30, name: 'C' },
      ],
    })
    await act(async () => {
      await latest.refresh()
    })
    expect(api.get).toHaveBeenCalledTimes(2)
    expect(latest.organizations).toHaveLength(2)
  })

  it('user: uses own orgs from the JWT, never calls /admin, defaults to the first org', async () => {
    authState.user = {
      id: 2,
      role: 'user',
      organizations: [{ id: 5, name: 'Mine' }],
    }

    let latest: any
    renderWith((s) => (latest = s))
    await waitFor(() => expect(latest.organizations).toHaveLength(1))
    expect(api.get).not.toHaveBeenCalled()
    expect(latest.selected?.id).toBe(5)
  })

  it('restores the selection from localStorage', async () => {
    localStorage.setItem('selectedOrganizationId', '20')
    authState.user = {
      id: 3,
      role: 'user',
      organizations: [
        { id: 10, name: 'A' },
        { id: 20, name: 'B' },
      ],
    }

    let latest: any
    renderWith((s) => (latest = s))
    await waitFor(() => expect(latest.selected?.id).toBe(20))
  })

  it('setSelectedId writes to localStorage', async () => {
    authState.user = {
      id: 4,
      role: 'user',
      organizations: [
        { id: 10, name: 'A' },
        { id: 20, name: 'B' },
      ],
    }

    let latest: any
    renderWith((s) => (latest = s))
    await waitFor(() => expect(latest.organizations).toHaveLength(2))

    act(() => latest.setSelectedId(20))
    await waitFor(() => expect(localStorage.getItem('selectedOrganizationId')).toBe('20'))
  })
})
