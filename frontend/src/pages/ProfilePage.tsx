import { useRef, useState } from 'react'
import { Upload, X } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../context/ToastContext'
import { useApiError } from '../hooks/useApiError'
import api from '../lib/api'

const MAX_SIZE = 2 * 1024 * 1024

export default function ProfilePage() {
  const { t } = useTranslation(['profile', 'common', 'errors'])
  const apiError = useApiError()
  const { user, refreshUser } = useAuth()
  const toast = useToast()
  const [name, setName] = useState(user?.name || '')
  const [email, setEmail] = useState(user?.email || '')
  const [password, setPassword] = useState('')
  const [pendingFile, setPendingFile] = useState<File | null>(null)
  const [pendingPreview, setPendingPreview] = useState<string | null>(null)
  const [pendingDelete, setPendingDelete] = useState(false)
  const [saving, setSaving] = useState(false)
  const fileRef = useRef<HTMLInputElement>(null)

  const currentSrc = pendingDelete ? null : pendingPreview || user?.avatar_url || null

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    if (file.size > MAX_SIZE) {
      toast.error(t('errors:fileTooLarge'))
      return
    }
    setPendingFile(file)
    setPendingPreview(URL.createObjectURL(file))
    setPendingDelete(false)
  }

  const handleDeleteImage = (e: React.MouseEvent) => {
    e.stopPropagation()
    if (pendingPreview) URL.revokeObjectURL(pendingPreview)
    setPendingFile(null)
    setPendingPreview(null)
    setPendingDelete(true)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    try {
      if (pendingDelete && !pendingFile) {
        await api.delete('/users/me/avatar')
      }
      if (pendingFile) {
        const fd = new FormData()
        fd.append('avatar', pendingFile)
        await api.post('/users/me/avatar', fd, {
          headers: { 'Content-Type': 'multipart/form-data' },
        })
      }
      const payload: Record<string, string> = { name, email }
      if (password) payload.password = password
      await api.put('/users/me', payload)
      await refreshUser()
      toast.success(t('profile:updated'))
    } catch (err) {
      toast.error(apiError(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="mx-auto max-w-md space-y-6">
      <h1 className="text-2xl font-bold">{t('profile:title')}</h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="flex flex-col items-center gap-4">
          <div className="relative">
            <button
              type="button"
              onClick={() => fileRef.current?.click()}
              className="group block overflow-hidden rounded-full border-2 border-dashed border-muted-foreground/30 transition-all hover:border-primary/50"
            >
              {currentSrc ? (
                <img
                  src={currentSrc}
                  alt="avatar"
                  className="h-24 w-24 rounded-full object-cover"
                />
              ) : (
                <div className="flex h-24 w-24 items-center justify-center rounded-full bg-muted">
                  <span className="text-2xl font-bold text-muted-foreground">
                    {user?.name?.[0]?.toUpperCase() || 'U'}
                  </span>
                </div>
              )}
              <div className="absolute inset-0 flex items-center justify-center rounded-full bg-black/0 transition-all group-hover:bg-black/40">
                <Upload className="h-6 w-6 text-white opacity-0 transition-all group-hover:opacity-100" />
              </div>
            </button>
            {currentSrc && (
              <button
                type="button"
                onClick={handleDeleteImage}
                className="absolute -right-2 -top-2 flex h-6 w-6 items-center justify-center rounded-full bg-destructive text-destructive-foreground shadow transition-all hover:scale-110"
              >
                <X className="h-3 w-3" />
              </button>
            )}
            <input
              ref={fileRef}
              type="file"
              accept="image/*"
              onChange={handleFileSelect}
              className="hidden"
            />
          </div>
        </div>

        <div className="space-y-4">
          <div className="space-y-2">
            <label className="text-sm font-medium">{t('common:field.name')}</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none transition-all duration-200 ring-offset-background focus:ring-2 focus:ring-ring focus:shadow-md focus:shadow-primary/20"
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium">{t('common:field.email')}</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none transition-all duration-200 ring-offset-background focus:ring-2 focus:ring-ring focus:shadow-md focus:shadow-primary/20"
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium">{t('profile:newPassword')}</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none transition-all duration-200 ring-offset-background focus:ring-2 focus:ring-ring focus:shadow-md focus:shadow-primary/20"
            />
          </div>
        </div>

        <button
          type="submit"
          disabled={saving}
          className="w-full rounded-lg bg-primary px-4 py-2.5 text-sm font-medium text-primary-foreground transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lg hover:shadow-primary/30 active:scale-[0.98] disabled:pointer-events-none disabled:opacity-60"
        >
          {saving ? t('common:saving') : t('common:update')}
        </button>
      </form>
    </div>
  )
}
