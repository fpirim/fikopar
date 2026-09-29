import { useTranslation } from 'react-i18next'

export function useGridLocale(): Record<string, string> {
  const { t } = useTranslation('grid')
  return t('localeText', { returnObjects: true }) as unknown as Record<string, string>
}
