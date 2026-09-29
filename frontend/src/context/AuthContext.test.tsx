import { describe, it, expect, vi, beforeEach, afterAll } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import type { ReactNode } from 'react'
import { AuthProvider } from './AuthContext'
import { useAuth } from '../hooks/useAuth'
import api from '../lib/api'

vi.mock('../lib/api', () => ({ default: { get: vi.fn(), post: vi.fn() } }))
const mockedApi = vi.mocked(api, true)

const wrapper = ({ children }: { children: ReactNode }) => <AuthProvider>{children}</AuthProvider>

const originalLocation = window.location
beforeEach(() => {
  localStorage.clear()
  vi.clearAllMocks()
  Object.defineProperty(window, 'location', {
    configurable: true,
    writable: true,
    value: { href: '', pathname: '/' },
  })
})
afterAll(() => {
  Object.defineProperty(window, 'location', { configurable: true, value: originalLocation })
})

describe('AuthContext', () => {
  it('finishes loading with no user when there is no stored token', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper })
    await waitFor(() => expect(result.current.isLoading).toBe(false))
    expect(result.current.user).toBeNull()
    expect(mockedApi.get).not.toHaveBeenCalled()
  })

  it('hydrates the user from /users/me when a token is stored', async () => {
    localStorage.setItem('token', 'stored')
    mockedApi.get.mockResolvedValue({ data: { id: 1, email: 'a@b.com', name: 'A' } })

    const { result } = renderHook(() => useAuth(), { wrapper })
    await waitFor(() => expect(result.current.isLoading).toBe(false))
    expect(mockedApi.get).toHaveBeenCalledWith('/users/me')
    expect(result.current.user?.email).toBe('a@b.com')
  })

  it('login stores token + user', async () => {
    mockedApi.post.mockResolvedValue({ data: { token: 't', user: { id: 1, email: 'a@b.com' } } })

    const { result } = renderHook(() => useAuth(), { wrapper })
    await waitFor(() => expect(result.current.isLoading).toBe(false))

    await act(async () => {
      await result.current.login('a@b.com', 'pw')
    })

    expect(mockedApi.post).toHaveBeenCalledWith('/auth/login', { email: 'a@b.com', password: 'pw' })
    expect(result.current.token).toBe('t')
    expect(localStorage.getItem('token')).toBe('t')
  })

  it('logout clears token + user', async () => {
    localStorage.setItem('token', 't')
    mockedApi.get.mockResolvedValue({ data: { id: 1, email: 'a@b.com' } })

    const { result } = renderHook(() => useAuth(), { wrapper })
    await waitFor(() => expect(result.current.isLoading).toBe(false))

    act(() => result.current.logout())

    expect(result.current.token).toBeNull()
    expect(result.current.user).toBeNull()
    expect(localStorage.getItem('token')).toBeNull()
  })
})
