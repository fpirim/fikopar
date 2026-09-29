import type { CreditLimitExceeded, EntryOwner } from './types'

type QueryValue = string | number | boolean | null | undefined

export interface RequestOptions {
  query?: Record<string, QueryValue>
  body?: unknown
  accept?: string
  ifMatch?: string | null
  idempotencyKey?: string
  // Query-style POST (grid, print): no Idempotency-Key.
  safe?: boolean
  signal?: AbortSignal
}

export interface ApiResult<T> {
  data: T
  etag: string | null
  replayed: boolean
}

export class ApiError extends Error {
  readonly status: number
  readonly code: string
  readonly field?: string
  readonly owner?: EntryOwner
  readonly details?: CreditLimitExceeded

  constructor(
    status: number,
    code: string,
    field?: string,
    owner?: EntryOwner,
    details?: CreditLimitExceeded,
  ) {
    super(code)
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.field = field
    this.owner = owner
    this.details = details
  }
}

// ETag is "<version>"; send it back as If-Match.
export const ifMatch = (version: number) => `"${version}"`

export const XLSX = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
export const PDF = 'application/pdf'

export function newIdempotencyKey(): string {
  return globalThis.crypto.randomUUID()
}

function buildUrl(path: string, query?: Record<string, QueryValue>): string {
  const url = new URL(path, globalThis.location?.origin ?? 'http://localhost')
  for (const [k, v] of Object.entries(query ?? {})) {
    if (v !== undefined && v !== null && v !== '') url.searchParams.set(k, String(v))
  }
  return url.toString()
}

async function send(method: string, path: string, opts: RequestOptions): Promise<Response> {
  const headers = new Headers({ Accept: opts.accept ?? 'application/json' })
  const token = localStorage.getItem('token')
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const form = opts.body instanceof FormData
  if (opts.body !== undefined && !form) headers.set('Content-Type', 'application/json')
  if (opts.ifMatch) headers.set('If-Match', opts.ifMatch)
  if (method === 'POST' && !opts.safe) {
    headers.set('Idempotency-Key', opts.idempotencyKey ?? newIdempotencyKey())
  }
  const res = await fetch(buildUrl(path, opts.query), {
    method,
    headers,
    body:
      form || opts.body === undefined
        ? (opts.body as FormData | undefined)
        : JSON.stringify(opts.body),
    signal: opts.signal,
  })
  if (!res.ok) throw await toError(res)
  return res
}

async function toError(res: Response): Promise<ApiError> {
  if (res.status === 401 && localStorage.getItem('token')) {
    localStorage.removeItem('token')
    if (globalThis.location.pathname !== '/login') globalThis.location.assign('/login')
  }
  try {
    const body = (await res.json()) as {
      code?: string
      field?: string
      owner?: EntryOwner
      details?: CreditLimitExceeded
    }
    return new ApiError(
      res.status,
      body.code ?? 'ERR_UNKNOWN',
      body.field,
      body.owner,
      body.details,
    )
  } catch {
    return new ApiError(res.status, 'ERR_UNKNOWN')
  }
}

export async function apiRequest<T>(
  method: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE',
  path: string,
  opts: RequestOptions = {},
): Promise<ApiResult<T>> {
  const res = await send(method, path, opts)
  const text = res.status === 204 ? '' : await res.text()
  return {
    data: (text ? JSON.parse(text) : null) as T,
    etag: res.headers.get('ETag'),
    replayed: res.headers.get('Idempotent-Replayed') === 'true',
  }
}

export async function apiBlob(
  method: 'GET' | 'POST',
  path: string,
  accept: string,
  opts: Omit<RequestOptions, 'accept'> = {},
): Promise<Blob> {
  const res = await send(method, path, { safe: true, ...opts, accept })
  return res.blob()
}
