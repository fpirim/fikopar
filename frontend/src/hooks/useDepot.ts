import { useCallback } from 'react'
import { useSearchParams } from 'react-router-dom'

// The top-bar depot lives in the URL (?depot=), so it survives reloads and links.
export function useDepot() {
  const [params, setParams] = useSearchParams()
  const raw = params.get('depot')
  const depotId = raw ? Number(raw) : null
  const setDepot = useCallback(
    (id: number | null) =>
      setParams((p) => {
        const next = new URLSearchParams(p)
        if (id) next.set('depot', String(id))
        else next.delete('depot')
        return next
      }),
    [setParams],
  )
  return { depotId, setDepot }
}

export function withDepot(to: string, depotId: number | null): string {
  if (!depotId) return to
  return `${to}${to.includes('?') ? '&' : '?'}depot=${depotId}`
}
