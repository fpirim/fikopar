import { useCallback, useEffect, useRef, useState } from 'react'
import { ApiError } from '../api/client'

export interface ApiState<T> {
  data: T | undefined
  error: ApiError | null
  loading: boolean
  reload: () => void
  setData: (data: T) => void
}

// Runs `load` when deps change; stale responses are dropped. `load` null means "not ready".
export function useApi<T>(load: (() => Promise<T>) | null, deps: unknown[]): ApiState<T> {
  const [data, setData] = useState<T>()
  const [error, setError] = useState<ApiError | null>(null)
  const [tick, setTick] = useState(0)
  const [done, setDone] = useState<string | null>(null)
  const seq = useRef(0)
  const key = `${JSON.stringify(deps)}#${tick}`

  useEffect(() => {
    if (!load) return
    const id = ++seq.current
    load()
      .then((d) => {
        if (id !== seq.current) return
        setData(d)
        setError(null)
      })
      .catch((e: unknown) => {
        if (id !== seq.current) return
        setError(e instanceof ApiError ? e : new ApiError(0, 'ERR_UNKNOWN'))
      })
      .finally(() => {
        if (id === seq.current) setDone(key)
      })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key, Boolean(load)])

  const reload = useCallback(() => setTick((t) => t + 1), [])
  return { data, error, loading: Boolean(load) && done !== key, reload, setData }
}
