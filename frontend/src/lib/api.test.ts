import { describe, it, expect, beforeEach, afterAll } from 'vitest'
import type { AxiosError, InternalAxiosRequestConfig } from 'axios'
import api from './api'

// Reach into the registered interceptor handlers to test their logic directly.
type Req = (c: InternalAxiosRequestConfig) => InternalAxiosRequestConfig
type Rej = (e: unknown) => unknown
const requestFulfilled = (api.interceptors.request as never as { handlers: { fulfilled: Req }[] })
  .handlers[0].fulfilled
const responseRejected = (api.interceptors.response as never as { handlers: { rejected: Rej }[] })
  .handlers[0].rejected

const originalLocation = window.location

function mockLocation(pathname: string) {
  Object.defineProperty(window, 'location', {
    configurable: true,
    writable: true,
    value: { href: '', pathname },
  })
}

beforeEach(() => {
  localStorage.clear()
  mockLocation('/dashboard')
})

afterAll(() => {
  Object.defineProperty(window, 'location', { configurable: true, value: originalLocation })
})

describe('request interceptor', () => {
  it('attaches the bearer token when present', () => {
    localStorage.setItem('token', 'abc')
    const cfg = requestFulfilled({ headers: {} } as InternalAxiosRequestConfig)
    expect(cfg.headers.Authorization).toBe('Bearer abc')
  })

  it('does not attach a header when no token', () => {
    const cfg = requestFulfilled({ headers: {} } as InternalAxiosRequestConfig)
    expect(cfg.headers.Authorization).toBeUndefined()
  })
})

describe('response interceptor 401 handling', () => {
  function reject(err: Partial<AxiosError>) {
    return expect(responseRejected(err)).rejects.toBeDefined()
  }

  it('redirects to /login when an authenticated session expires', async () => {
    localStorage.setItem('token', 'abc')
    await reject({ response: { status: 401 } as never, config: { url: '/users/me' } as never })
    expect(localStorage.getItem('token')).toBeNull()
    expect(window.location.href).toBe('/login')
  })

  it('does NOT redirect on a failed login (no token, login url)', async () => {
    await reject({ response: { status: 401 } as never, config: { url: '/auth/login' } as never })
    expect(window.location.href).toBe('')
  })

  it('does NOT redirect on a 401 when there was no token', async () => {
    await reject({ response: { status: 401 } as never, config: { url: '/users/me' } as never })
    expect(window.location.href).toBe('')
  })

  it('does not redirect when already on /login', async () => {
    localStorage.setItem('token', 'abc')
    mockLocation('/login')
    await reject({ response: { status: 401 } as never, config: { url: '/users/me' } as never })
    expect(window.location.href).toBe('')
  })
})
