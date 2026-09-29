import { useOrganization } from '../context/OrganizationContext'

export function useOrgId(): number | null {
  return useOrganization().selectedId
}
