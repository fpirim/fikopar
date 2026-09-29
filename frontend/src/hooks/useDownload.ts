import { useCallback, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { ApiError } from '../api/client'
import { saveBlob } from '../lib/csv'
import { useToast } from '../context/ToastContext'

// Wraps a Blob request: saves the file, toasts the translated error code.
export function useDownload() {
  const { t } = useTranslation('errors')
  const toast = useToast()
  const [busy, setBusy] = useState<string | null>(null)
  const run = useCallback(
    async (key: string, filename: string, get: () => Promise<Blob>) => {
      setBusy(key)
      try {
        saveBlob(await get(), filename)
      } catch (e) {
        toast.error(t(e instanceof ApiError ? e.code : 'ERR_UNKNOWN'))
      } finally {
        setBusy(null)
      }
    },
    [t, toast],
  )
  return { busy, run }
}
