import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError, apiBlob, apiRequest } from './client'

const fetchMock = vi.fn()

function json(body: unknown, init: ResponseInit = {}) {
  return new Response(JSON.stringify(body), {
    status: 200,
    ...init,
    headers: { 'Content-Type': 'application/json', ...(init.headers ?? {}) },
  })
}

beforeEach(() => {
  fetchMock.mockReset()
  vi.stubGlobal('fetch', fetchMock)
})

afterEach(() => {
  vi.unstubAllGlobals()
})

function lastCall() {
  const [url, init] = fetchMock.mock.calls[fetchMock.mock.calls.length - 1]
  return { url: String(url), init: init as RequestInit, headers: new Headers(init.headers) }
}

describe('apiRequest', () => {
  it('sends the bearer token and serialises the query', async () => {
    localStorage.setItem('token', 'tkn')
    fetchMock.mockResolvedValue(json({ ok: 1 }))
    const res = await apiRequest<{ ok: number }>('GET', '/api/orgs/1/cash-book', {
      query: { depotId: 2, from: '2026-09-22', skip: undefined, none: null },
    })
    const { url, headers } = lastCall()
    expect(url).toMatch(/\/api\/orgs\/1\/cash-book\?depotId=2&from=2026-09-22$/)
    expect(headers.get('Authorization')).toBe('Bearer tkn')
    expect(res.data).toEqual({ ok: 1 })
  })

  it('returns the ETag and passes If-Match', async () => {
    fetchMock.mockResolvedValue(json({}, { headers: { ETag: '"7"' } }))
    const res = await apiRequest('PUT', '/api/x', { body: { a: 1 }, ifMatch: '"6"' })
    expect(res.etag).toBe('"7"')
    const { headers, init } = lastCall()
    expect(headers.get('If-Match')).toBe('"6"')
    expect(headers.get('Content-Type')).toBe('application/json')
    expect(init.body).toBe('{"a":1}')
  })

  it('adds an Idempotency-Key to unsafe POSTs and keeps a caller key', async () => {
    fetchMock.mockImplementation(async () => json({}, { status: 201 }))
    await apiRequest('POST', '/api/x', { body: {} })
    const generated = lastCall().headers.get('Idempotency-Key')
    expect(generated).toMatch(/^[0-9a-f-]{36}$/)

    await apiRequest('POST', '/api/x', { body: {}, idempotencyKey: 'k-1' })
    expect(lastCall().headers.get('Idempotency-Key')).toBe('k-1')
  })

  it('leaves query POSTs without an Idempotency-Key', async () => {
    fetchMock.mockResolvedValue(json({ rows: [], totalCount: 0 }))
    await apiRequest('POST', '/api/grid', { body: {}, safe: true })
    expect(lastCall().headers.has('Idempotency-Key')).toBe(false)
  })

  it('flags replayed responses', async () => {
    fetchMock.mockResolvedValue(json({}, { headers: { 'Idempotent-Replayed': 'true' } }))
    const res = await apiRequest('POST', '/api/x', { body: {} })
    expect(res.replayed).toBe(true)
  })

  it('throws ApiError with the error code', async () => {
    fetchMock.mockResolvedValue(
      json({ code: 'ERR_ENTRY_OWNED_BY_SOURCE', detail: 'x' }, { status: 409 }),
    )
    const err = await apiRequest('POST', '/api/x', { body: {} }).catch((e) => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.status).toBe(409)
    expect(err.code).toBe('ERR_ENTRY_OWNED_BY_SOURCE')
  })

  it('falls back to ERR_UNKNOWN when the body is not JSON', async () => {
    fetchMock.mockResolvedValue(new Response('boom', { status: 502 }))
    const err = await apiRequest('GET', '/api/x').catch((e) => e)
    expect(err.code).toBe('ERR_UNKNOWN')
  })

  it('returns null data on 204', async () => {
    fetchMock.mockResolvedValue(new Response(null, { status: 204 }))
    const res = await apiRequest('DELETE', '/api/x')
    expect(res.data).toBeNull()
  })

  it('drops the token and goes to /login on 401 with a session', async () => {
    localStorage.setItem('token', 'old')
    const assign = vi.fn()
    vi.stubGlobal('location', { ...window.location, pathname: '/balances', assign })
    fetchMock.mockResolvedValue(json({ code: 'ERR_INVALID_TOKEN' }, { status: 401 }))
    await apiRequest('GET', '/api/x').catch(() => undefined)
    expect(localStorage.getItem('token')).toBeNull()
    expect(assign).toHaveBeenCalledWith('/login')
  })
})

describe('apiBlob', () => {
  it('asks for the media type and returns the blob', async () => {
    fetchMock.mockResolvedValue(
      new Response('%PDF', { headers: { 'Content-Type': 'application/pdf' } }),
    )
    const blob = await apiBlob('GET', '/api/r', 'application/pdf')
    expect(lastCall().headers.get('Accept')).toBe('application/pdf')
    expect(await blob.text()).toBe('%PDF')
  })

  it('throws ApiError on failure', async () => {
    fetchMock.mockResolvedValue(json({ code: 'ERR_FORBIDDEN' }, { status: 403 }))
    const err = await apiBlob('POST', '/api/r', 'application/pdf', { body: {} }).catch((e) => e)
    expect(err.code).toBe('ERR_FORBIDDEN')
  })
})
