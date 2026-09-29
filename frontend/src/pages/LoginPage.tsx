import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { LogIn } from 'lucide-react'
import { useAuth } from '../hooks/useAuth'
import { useApiError } from '../hooks/useApiError'
import { APP_VERSION } from '../lib/version'
import LanguageSwitcher from '../components/ui/LanguageSwitcher'

export default function LoginPage() {
  const { t } = useTranslation(['auth', 'common'])
  const apiError = useApiError()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const { login } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      await login(email, password)
      navigate('/')
    } catch (err) {
      setError(apiError(err))
    }
  }

  return (
    <div className="relative flex min-h-screen items-center justify-center bg-muted px-4">
      <div className="absolute right-4 top-4 flex items-center gap-2">
        <LanguageSwitcher />
      </div>

      <form
        onSubmit={handleSubmit}
        className="group w-full max-w-sm space-y-6 rounded-2xl border bg-card p-8 shadow-xl transition-all duration-300 hover:shadow-2xl hover:shadow-primary/10"
      >
        <div className="text-center">
          <h1 className="text-3xl font-bold text-primary">fikopar</h1>
          <p className="text-xs text-muted-foreground">{APP_VERSION}</p>
          <p className="mt-2 text-sm text-muted-foreground">{t('auth:subtitle')}</p>
        </div>
        {error && (
          <div className="rounded-lg bg-destructive/10 px-4 py-3 text-sm text-destructive transition-all duration-200">
            {error}
          </div>
        )}
        <div className="space-y-2">
          <label className="text-sm font-medium">{t('common:field.email')}</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full rounded-lg border bg-background px-4 py-2.5 text-sm outline-none transition-all duration-200 ring-offset-background focus:ring-2 focus:ring-ring focus:shadow-md focus:shadow-primary/20"
            required
          />
        </div>
        <div className="space-y-2">
          <label className="text-sm font-medium">{t('common:field.password')}</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full rounded-lg border bg-background px-4 py-2.5 text-sm outline-none transition-all duration-200 ring-offset-background focus:ring-2 focus:ring-ring focus:shadow-md focus:shadow-primary/20"
            required
          />
        </div>
        <button
          type="submit"
          className="group/btn flex w-full items-center justify-center gap-2 rounded-lg bg-primary px-4 py-2.5 text-sm font-medium text-primary-foreground transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lg hover:shadow-primary/30 active:scale-[0.98]"
        >
          <LogIn className="h-4 w-4 transition-transform duration-200 group-hover/btn:-translate-x-1" />
          {t('auth:submit')}
        </button>
      </form>
    </div>
  )
}
