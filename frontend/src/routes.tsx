import { Routes, Route } from 'react-router-dom'
import AppLayout from './components/layout/AppLayout'
import ProtectedRoute from './components/auth/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import ProfilePage from './pages/ProfilePage'
import AdminUsersPage from './pages/AdminUsersPage'
import AdminOrganizationsPage from './pages/AdminOrganizationsPage'
import SicilPage from './pages/SicilPage'
import NumericInputPlayground from './pages/NumericInputPlayground'
import NotInBuildPage from './pages/NotInBuildPage'
import BalancesPage from './pages/BalancesPage'
import StatementPage from './pages/StatementPage'
import ReceiptPage from './pages/ReceiptPage'
import CashBookPage from './pages/CashBookPage'
import CarryForwardPage from './pages/CarryForwardPage'
import JournalPage from './pages/JournalPage'
import JournalEntryPage from './pages/JournalEntryPage'

const LATER = ['/invoices', '/cheques', '/parts', '/stock-counts', '/migration']

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/playground/numeric-input" element={<NumericInputPlayground />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/sicil" element={<SicilPage />} />
          <Route path="/admin/users" element={<AdminUsersPage />} />
          <Route path="/admin/organizations" element={<AdminOrganizationsPage />} />
          <Route path="/balances" element={<BalancesPage />} />
          <Route path="/receipts" element={<ReceiptPage />} />
          <Route path="/cash-book" element={<CashBookPage />} />
          <Route path="/carry-forward" element={<CarryForwardPage />} />
          <Route path="/journal" element={<JournalPage />} />
          <Route path="/journal/:entryId" element={<JournalEntryPage />} />
          <Route path="/carry-forward/:year" element={<CarryForwardPage />} />
          <Route path="/statements" element={<StatementPage />} />
          <Route path="/statements/:accountId" element={<StatementPage />} />
          {LATER.map((p) => (
            <Route key={p} path={p} element={<NotInBuildPage />} />
          ))}
        </Route>
      </Route>
    </Routes>
  )
}

export default AppRoutes
