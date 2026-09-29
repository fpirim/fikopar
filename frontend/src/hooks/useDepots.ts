import { listDepots } from '../api/endpoints'
import type { Depot } from '../api/types'
import { useApi } from './useApi'
import { useOrgId } from './useOrgId'

export function useDepots(): Depot[] {
  const orgId = useOrgId()
  return useApi(orgId ? () => listDepots(orgId) : null, [orgId]).data ?? []
}
