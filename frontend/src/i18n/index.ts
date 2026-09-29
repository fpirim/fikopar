import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import LanguageDetector from 'i18next-browser-languagedetector'
import { DEFAULT_LOCALE, SUPPORTED } from './config'

import enCommon from './locales/en/common.json'
import enNav from './locales/en/nav.json'
import enAuth from './locales/en/auth.json'
import enDashboard from './locales/en/dashboard.json'
import enProfile from './locales/en/profile.json'
import enUsers from './locales/en/users.json'
import enOrganizations from './locales/en/organizations.json'
import enSicil from './locales/en/sicil.json'
import enErrors from './locales/en/errors.json'
import enGrid from './locales/en/grid.json'

import trCommon from './locales/tr/common.json'
import trNav from './locales/tr/nav.json'
import trAuth from './locales/tr/auth.json'
import trDashboard from './locales/tr/dashboard.json'
import trProfile from './locales/tr/profile.json'
import trUsers from './locales/tr/users.json'
import trOrganizations from './locales/tr/organizations.json'
import trSicil from './locales/tr/sicil.json'
import trErrors from './locales/tr/errors.json'
import trGrid from './locales/tr/grid.json'

const resources = {
  en: {
    common: enCommon,
    nav: enNav,
    auth: enAuth,
    dashboard: enDashboard,
    profile: enProfile,
    users: enUsers,
    organizations: enOrganizations,
    sicil: enSicil,
    errors: enErrors,
    grid: enGrid,
  },
  tr: {
    common: trCommon,
    nav: trNav,
    auth: trAuth,
    dashboard: trDashboard,
    profile: trProfile,
    users: trUsers,
    organizations: trOrganizations,
    sicil: trSicil,
    errors: trErrors,
    grid: trGrid,
  },
} as const

const defaultNS = 'common'

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    fallbackLng: DEFAULT_LOCALE,
    supportedLngs: SUPPORTED.map((l) => l.code),
    nonExplicitSupportedLngs: true,
    load: 'languageOnly',
    defaultNS,
    ns: Object.keys(resources[DEFAULT_LOCALE]),
    interpolation: { escapeValue: false },
    // Browser language is ignored on purpose: the app opens in English unless the user picked TR.
    detection: {
      order: ['localStorage'],
      lookupLocalStorage: 'lang',
      caches: ['localStorage'],
    },
    react: { useSuspense: false },
  })

const syncHtmlLang = (lng: string) => {
  if (typeof document !== 'undefined') document.documentElement.lang = lng
}
syncHtmlLang(i18n.resolvedLanguage || DEFAULT_LOCALE)
i18n.on('languageChanged', syncHtmlLang)

export default i18n
