import { useCallback, useRef } from 'react'
import { newIdempotencyKey } from '../api/client'

// Same body → same key (a retry replays); a changed body gets a fresh key.
export function useIdempotencyKey() {
  const last = useRef<{ body: string; key: string } | null>(null)
  const keyFor = useCallback((body: unknown) => {
    const json = JSON.stringify(body)
    if (last.current?.body !== json) last.current = { body: json, key: newIdempotencyKey() }
    return last.current.key
  }, [])
  const reset = useCallback(() => {
    last.current = null
  }, [])
  return { keyFor, reset }
}
