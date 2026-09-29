import { useTranslation } from 'react-i18next'
import { getErrorCode } from '../lib/apiError'

export function useApiError() {
  const { t } = useTranslation('errors')
  return (err: unknown): string => {
    const code = getErrorCode(err)
    return t(code, { defaultValue: t('ERR_UNKNOWN') })
  }
}
