import { useTranslation } from 'react-i18next'
import { SUPPORTED } from '../../i18n/config'

// Hidden while only one language is configured.
export default function LanguageSwitcher() {
  const { i18n, t } = useTranslation('nav')
  if (SUPPORTED.length < 2) return null
  return (
    <select
      value={i18n.language}
      onChange={(e) => i18n.changeLanguage(e.target.value)}
      aria-label={t('language')}
      className="h-9 rounded-lg border bg-secondary px-2 text-sm outline-none transition-all hover:border-primary/50 focus:ring-2 focus:ring-ring"
    >
      {SUPPORTED.map((l) => (
        <option key={l.code} value={l.code}>
          {l.label}
        </option>
      ))}
    </select>
  )
}
