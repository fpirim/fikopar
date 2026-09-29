import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import api from '../lib/api'
import { useAuth } from '../hooks/useAuth'
import type { Organization } from '../types/organization'

// Admins load every org from the API; users take theirs from the JWT.
const LS_KEY = 'selectedOrganizationId'

interface OrganizationContextValue {
  organizations: Organization[]
  selectedId: number | null
  selected: Organization | null
  setSelectedId: (id: number) => void
  refresh: () => Promise<void>
  isLoading: boolean
}

const OrganizationContext = createContext<OrganizationContextValue | undefined>(undefined)

export function OrganizationProvider({ children }: Readonly<{ children: ReactNode }>) {
  const { user } = useAuth()
  const [allOrgs, setAllOrgs] = useState<Organization[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const [selectedId, setSelectedId] = useState<number | null>(() => {
    const stored = localStorage.getItem(LS_KEY)
    return stored ? Number(stored) : null
  })

  const refresh = useCallback(async () => {
    if (!user) {
      setAllOrgs([])
      return
    }
    if (user.role === 'admin') {
      setIsLoading(true)
      try {
        const res = await api.get('/admin/organizations')
        setAllOrgs(res.data)
      } catch {
        setAllOrgs([])
      } finally {
        setIsLoading(false)
      }
    } else {
      setAllOrgs(user.organizations || [])
    }
  }, [user])

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    refresh()
  }, [refresh])

  // Users always have an org selected; admins may have none ("view all"), never auto-locked.
  const selected = useMemo<Organization | null>(() => {
    if (allOrgs.length === 0) return null
    const found = allOrgs.find((o) => o.id === selectedId)
    if (found) return found
    if (user?.role === 'admin') return null
    return allOrgs[0]
  }, [allOrgs, selectedId, user])

  // No setState here: the `selected` memo already corrects a stale stored id.
  useEffect(() => {
    if (selected) {
      localStorage.setItem(LS_KEY, String(selected.id))
    } else {
      localStorage.removeItem(LS_KEY)
    }
  }, [selected])

  const value = useMemo<OrganizationContextValue>(
    () => ({
      organizations: allOrgs,
      selectedId: selected?.id ?? null,
      selected,
      setSelectedId,
      refresh,
      isLoading,
    }),
    [allOrgs, selected, refresh, isLoading],
  )

  return <OrganizationContext.Provider value={value}>{children}</OrganizationContext.Provider>
}

export function useOrganization() {
  const ctx = useContext(OrganizationContext)
  if (!ctx) throw new Error('useOrganization must be used within OrganizationProvider')
  return ctx
}
