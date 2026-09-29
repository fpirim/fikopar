import type { AxiosError } from 'axios'

export function getErrorCode(err: unknown): string {
  const data = (err as AxiosError<{ code?: string }>)?.response?.data
  return data?.code || 'ERR_UNKNOWN'
}
