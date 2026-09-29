import type { Organization } from './organization'

type Role = 'admin' | 'user'

export interface User {
  id: number
  email: string
  name: string
  role: Role
  avatar_url?: string
  organizations: Organization[]
  created_at: string
  updated_at: string
}
