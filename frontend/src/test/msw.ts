import { http } from 'msw'
import { server } from '../mocks/node'

export interface Captured {
  url: URL
  headers: Headers
  body: unknown
}

// Records matching requests and lets them fall through to the default handlers.
export function capture(method: 'get' | 'post' | 'put' | 'delete', path: string): Captured[] {
  const seen: Captured[] = []
  server.use(
    http[method](path, async ({ request }) => {
      const text = await request.clone().text()
      seen.push({
        url: new URL(request.url),
        headers: request.headers,
        body: text ? JSON.parse(text) : undefined,
      })
      return undefined
    }),
  )
  return seen
}

export function captureDownloads(): Blob[] {
  const blobs: Blob[] = []
  URL.createObjectURL = (b: Blob | MediaSource) => {
    blobs.push(b as Blob)
    return 'blob:test'
  }
  URL.revokeObjectURL = () => undefined
  HTMLAnchorElement.prototype.click = () => undefined
  return blobs
}
