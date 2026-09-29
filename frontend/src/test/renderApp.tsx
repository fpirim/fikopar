import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { AuthProvider } from '../context/AuthContext'
import { OrganizationProvider } from '../context/OrganizationContext'
import { ToastProvider } from '../context/ToastContext'
import AppRoutes from '../routes'
import LocationProbe from './LocationProbe'

// Full app on MSW at a route, signed in as the demo user (or an admin) of company 1.
export function renderApp(path: string, opts: { admin?: boolean } = {}) {
  localStorage.setItem('token', opts.admin ? 'msw-admin' : 'msw-token')
  localStorage.setItem('selectedOrganizationId', '1')
  return render(
    <MemoryRouter initialEntries={[path]}>
      <AuthProvider>
        <OrganizationProvider>
          <ToastProvider>
            <AppRoutes />
            <LocationProbe />
          </ToastProvider>
        </OrganizationProvider>
      </AuthProvider>
    </MemoryRouter>,
  )
}
