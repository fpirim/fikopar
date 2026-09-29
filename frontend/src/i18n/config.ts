// Add a language: an entry here + locales/<code>/ + registration in index.ts.
export const SUPPORTED = [
  { code: 'en', label: 'English' },
  { code: 'tr', label: 'Türkçe' },
] as const

export type LocaleCode = (typeof SUPPORTED)[number]['code']

export const DEFAULT_LOCALE: LocaleCode = 'en'
