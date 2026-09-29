import { Users, User, Activity, Building2 } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../hooks/useAuth'

export default function DashboardPage() {
  const { t } = useTranslation(['dashboard', 'common'])
  const { user } = useAuth()
  const orgs = user?.organizations || []

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">{t('dashboard:title')}</h1>
        <p className="text-muted-foreground mt-1">{t('dashboard:welcome')}</p>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <div className="group rounded-xl border bg-card p-6 transition-all duration-200 hover:-translate-y-1 hover:shadow-lg hover:shadow-primary/10">
          <div className="flex items-center justify-between">
            <div className="rounded-lg bg-primary/10 p-3 transition-transform duration-200 group-hover:scale-110">
              <User className="h-6 w-6 text-primary" />
            </div>
          </div>
          <div className="mt-4">
            <p className="text-sm text-muted-foreground">{t('dashboard:greeting')}</p>
            <p className="text-xl font-semibold">{user?.name}</p>
          </div>
        </div>

        <div className="group rounded-xl border bg-card p-6 transition-all duration-200 hover:-translate-y-1 hover:shadow-lg hover:shadow-primary/10">
          <div className="flex items-center justify-between">
            <div className="rounded-lg bg-primary/10 p-3 transition-transform duration-200 group-hover:scale-110">
              <Users className="h-6 w-6 text-primary" />
            </div>
          </div>
          <div className="mt-4">
            <p className="text-sm text-muted-foreground">{t('common:field.role')}</p>
            <p className="text-xl font-semibold capitalize">{user?.role}</p>
          </div>
        </div>

        <div className="group rounded-xl border bg-card p-6 transition-all duration-200 hover:-translate-y-1 hover:shadow-lg hover:shadow-primary/10">
          <div className="flex items-center justify-between">
            <div className="rounded-lg bg-primary/10 p-3 transition-transform duration-200 group-hover:scale-110">
              <Activity className="h-6 w-6 text-primary" />
            </div>
          </div>
          <div className="mt-4">
            <p className="text-sm text-muted-foreground">{t('dashboard:status')}</p>
            <p className="text-xl font-semibold text-green-600">{t('common:active')}</p>
          </div>
        </div>
      </div>

      {orgs.length > 0 && (
        <div className="rounded-xl border bg-card p-6 transition-all duration-200 hover:shadow-lg hover:shadow-primary/5">
          <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
            <Building2 className="h-5 w-5 text-primary" />
            {t('dashboard:yourOrganizations')}
          </h2>
          <div className="flex flex-wrap gap-3">
            {orgs.map((org) => (
              <div
                key={org.id}
                className="rounded-lg bg-primary/5 px-4 py-2.5 border transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md hover:shadow-primary/10"
              >
                <p className="font-medium text-sm">{org.name}</p>
                <p className="text-xs text-muted-foreground">{org.slug}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {user?.role === 'admin' && (
        <div className="rounded-xl border bg-card p-6 transition-all duration-200 hover:shadow-lg hover:shadow-primary/5">
          <h2 className="text-lg font-semibold mb-2">{t('dashboard:adminPanel')}</h2>
          <p className="text-sm text-muted-foreground">{t('dashboard:adminPanelDesc')}</p>
        </div>
      )}
    </div>
  )
}
