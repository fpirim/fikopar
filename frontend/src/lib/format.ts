import i18n from '../i18n'
import { DEFAULT_LOCALE } from '../i18n/config'

const LOCALE_MAP: Record<string, string> = { en: 'en-US', tr: 'tr-TR' }

function intlLocale(): string {
  const lng = i18n.language?.split('-')[0] || DEFAULT_LOCALE
  return LOCALE_MAP[lng] || lng
}

export function formatDate(iso?: string | null): string {
  if (!iso) return ''
  return new Date(iso).toLocaleDateString(intlLocale())
}
