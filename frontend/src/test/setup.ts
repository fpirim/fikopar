import '@testing-library/jest-dom/vitest'
import { Blob as NodeBlob, File as NodeFile } from 'node:buffer'
import { afterAll, afterEach, beforeAll, beforeEach, vi } from 'vitest'
import { cleanup, configure } from '@testing-library/react'
import i18n from '../i18n'
import { server } from '../mocks/node'
import { resetDb } from '../mocks/handlers'
import { APP_DELAYS, delays } from '../lib/delays'

configure({ asyncUtilTimeout: 5000 })

// Node's fetch only serializes its own FormData / Blob / File; jsdom's arrive as "[object FormData]".
const NodeFormData = (await new Response(new URLSearchParams('x=1')).formData()).constructor
Object.assign(globalThis, { FormData: NodeFormData, Blob: NodeBlob, File: NodeFile })

beforeAll(() => server.listen({ onUnhandledFrame: 'warn' }))
afterAll(() => server.close())

// Unit tests assert Turkish UI text; the app default (en) is covered in i18n.test.tsx.
beforeEach(async () => {
  await i18n.changeLanguage('tr')
  // No debounce waits in unit tests (a test that checks one sets it back from APP_DELAYS); polls stay
  // long enough for the in-between state to show.
  for (const k of Object.keys(APP_DELAYS) as (keyof typeof APP_DELAYS)[]) delays[k] = 20
  delays.importPoll = 100
})

afterEach(() => {
  cleanup()
  localStorage.clear()
  server.resetHandlers()
  resetDb()
})

// jsdom lacks window.matchMedia.
if (!window.matchMedia) {
  window.matchMedia = vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    addListener: vi.fn(),
    removeListener: vi.fn(),
    dispatchEvent: vi.fn(),
  }))
}

// cmdk and Radix measure and scroll elements; jsdom has neither API.
if (!window.ResizeObserver) {
  window.ResizeObserver = class {
    observe() {}
    unobserve() {}
    disconnect() {}
  }
}
window.HTMLElement.prototype.scrollIntoView ??= vi.fn()
