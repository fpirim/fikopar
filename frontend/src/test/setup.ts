import '@testing-library/jest-dom/vitest'
import { afterAll, afterEach, beforeAll, beforeEach, vi } from 'vitest'
import { cleanup, configure } from '@testing-library/react'
import i18n from '../i18n'
import { server } from '../mocks/node'
import { resetDb } from '../mocks/handlers'

configure({ asyncUtilTimeout: 5000 })

beforeAll(() => server.listen({ onUnhandledFrame: 'warn' }))
afterAll(() => server.close())

// Unit tests assert Turkish UI text; the app default (en) is covered in i18n.test.tsx.
beforeEach(async () => {
  await i18n.changeLanguage('tr')
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
