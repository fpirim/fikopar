import { render } from '@testing-library/react'
import { MemoryRouter, useLocation } from 'react-router-dom'
import { AuthProvider } from '../context/AuthContext'
import { OrganizationProvider } from '../context/OrganizationContext'
import { ToastProvider } from '../context/ToastContext'
import AppRoutes from '../routes'

function LocationProbe() {
  const { pathname, search } = useLocation()
  return <output data-testid="location">{pathname + search}</output>
}

// Full app on MSW at a route, signed in as the demo user of company 1.
export function renderApp(path: string) {
  localStorage.setItem('token', 'msw-token')
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
