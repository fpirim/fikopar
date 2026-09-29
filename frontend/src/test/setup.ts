import '@testing-library/jest-dom/vitest'
import { afterEach, beforeEach, vi } from 'vitest'
import { cleanup } from '@testing-library/react'
import i18n from '../i18n'

// Unit tests assert Turkish UI text; the app default (en) is covered in i18n.test.tsx.
beforeEach(async () => {
  await i18n.changeLanguage('tr')
})

afterEach(() => {
  cleanup()
  localStorage.clear()
})

// ThemeContext reads window.matchMedia for the initial theme; jsdom lacks it.
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
