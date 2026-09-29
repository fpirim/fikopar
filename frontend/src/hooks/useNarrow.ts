import { useSyncExternalStore } from 'react'

const QUERY = '(max-width: 1299px)'

// Below 1300 px wide screens drop the less needed grid columns instead of scrolling sideways.
export function useNarrow(): boolean {
  return useSyncExternalStore(
    (notify) => {
      const mq = globalThis.matchMedia?.(QUERY)
      mq?.addEventListener('change', notify)
      return () => mq?.removeEventListener('change', notify)
    },
    () => globalThis.matchMedia?.(QUERY).matches ?? false,
  )
}
