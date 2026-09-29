import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { OrganizationProvider } from './context/OrganizationContext'
import { ToastProvider } from './context/ToastContext'
import AppRoutes from './routes'

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <OrganizationProvider>
          <ToastProvider>
            <AppRoutes />
          </ToastProvider>
        </OrganizationProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
