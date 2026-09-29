import { useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import type { ColDef } from 'ag-grid-community'
import { useTranslation } from 'react-i18next'
import { User as UserIcon, X } from 'lucide-react'
import api from '../lib/api'
import { formatDate } from '../lib/format'
import { useToast } from '../context/ToastContext'
import { useApiError } from '../hooks/useApiError'
import { useImagePicker } from '../hooks/useImagePicker'
import CrudGridPage from '../components/ui/CrudGridPage'
import DialogShell from '../components/ui/DialogShell'
import ImagePicker from '../components/ui/ImagePicker'
import type { User } from '../types/user'
import type { Organization } from '../types/organization'

function UserDialog({
  user,
  allOrgs,
  onSaved,
  onClose,
}: Readonly<{
  user?: User
  allOrgs: Organization[]
  onSaved: () => void
  onClose: () => void
}>) {
  const [name, setName] = useState(user?.name || '')
  const [email, setEmail] = useState(user?.email || '')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState<'admin' | 'user'>(user?.role || 'user')
  const avatarUrlRef = useRef(user?.avatar_url || '')
  const picker = useImagePicker(avatarUrlRef.current)
  const [orgIds, setOrgIds] = useState<number[]>(user?.organizations?.map((o) => o.id) || [])
  const [orgQuery, setOrgQuery] = useState('')
  const [orgFocused, setOrgFocused] = useState(false)
  // Rendered through a portal so the dialog's scroll container does not clip it.
  const orgInputRef = useRef<HTMLInputElement>(null)
  const [orgDropdownPos, setOrgDropdownPos] = useState<{
    top: number
    left: number
    width: number
  } | null>(null)
  useLayoutEffect(() => {
    if (!orgFocused) return
    function update() {
      const el = orgInputRef.current
      if (!el) return
      const r = el.getBoundingClientRect()
      setOrgDropdownPos({ top: r.bottom + 4, left: r.left, width: r.width })
    }
    update()
    window.addEventListener('scroll', update, true)
    window.addEventListener('resize', update)
    return () => {
      window.removeEventListener('scroll', update, true)
      window.removeEventListener('resize', update)
    }
  }, [orgFocused])
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const toast = useToast()
  const { t } = useTranslation(['users', 'common', 'errors'])
  const apiError = useApiError()

  const toggleOrg = (id: number) => {
    setOrgIds((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]))
  }

  const handlePick = (file: File) => {
    if (!picker.selectFile(file)) {
      setError(t('errors:fileTooLarge'))
      return
    }
    setError('')
  }

  // Chips + searchable dropdown so thousands of orgs stay usable.
  const orgById = new Map(allOrgs.map((o) => [o.id, o]))
  const orgQ = orgQuery.trim().toLowerCase()
  // Even with an empty search (on focus) all unselected orgs are listed, sorted by name.
  const orgResults = allOrgs
    .filter((o) => !orgIds.includes(o.id) && (orgQ === '' || o.name.toLowerCase().includes(orgQ)))
    .sort((a, b) => a.name.localeCompare(b.name, 'tr'))
    .slice(0, 50)

  const handleSubmit = async () => {
    setError('')
    setSaving(true)
    try {
      let finalAvatarUrl = avatarUrlRef.current

      if (user) {
        if (picker.pendingDelete && !picker.pendingFile) {
          const delResp = await api.delete(`/admin/users/${user.id}/avatar`)
          finalAvatarUrl = delResp.data.avatar_url || ''
        }
        if (picker.pendingFile) {
          const fd = new FormData()
          fd.append('avatar', picker.pendingFile)
          const upResp = await api.post(`/admin/users/${user.id}/avatar`, fd, {
            headers: { 'Content-Type': 'multipart/form-data' },
          })
          finalAvatarUrl = upResp.data.avatar_url || ''
        }
        await api.put(`/admin/users/${user.id}`, {
          name,
          email,
          role,
          avatar_url: finalAvatarUrl || null,
          organization_ids: orgIds,
        })
      } else {
        const createResp = await api.post('/admin/users', {
          name,
          email,
          password: password || 'changeme',
          role,
          avatar_url: null,
          organization_ids: orgIds,
        })
        const newId = createResp.data.id
        if (picker.pendingFile) {
          const fd = new FormData()
          fd.append('avatar', picker.pendingFile)
          await api.post(`/admin/users/${newId}/avatar`, fd, {
            headers: { 'Content-Type': 'multipart/form-data' },
          })
        }
      }
      toast.success(user ? t('users:updated') : t('users:created'))
      onSaved()
    } catch (err) {
      const msg = apiError(err)
      setError(msg)
      toast.error(msg)
    } finally {
      setSaving(false)
    }
  }

  let submitLabel = t('common:create')
  if (saving) submitLabel = t('common:saving')
  else if (user) submitLabel = t('common:update')

  return (
    <DialogShell
      title={user ? t('users:dialog.editTitle') : t('users:dialog.createTitle')}
      error={error}
      maxWidth="lg"
      saving={saving}
      submitLabel={submitLabel}
      onSubmit={handleSubmit}
      onClose={onClose}
    >
      <ImagePicker
        src={picker.currentSrc}
        alt="avatar"
        rounded="full"
        placeholder={<UserIcon className="h-8 w-8 text-muted-foreground/50" />}
        fileRef={picker.fileRef}
        onPick={handlePick}
        onClear={picker.clearImage}
      />
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div className="space-y-2">
          <label className="text-sm font-medium">{t('common:field.name')}</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
            required
          />
        </div>
        <div className="space-y-2">
          <label className="text-sm font-medium">{t('common:field.email')}</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
            required
          />
        </div>
        <div className="space-y-2">
          <label className="text-sm font-medium">
            {t('common:field.password')}
            {user && t('users:dialog.passwordHint')}
          </label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
            {...(!user && { required: true })}
          />
        </div>
        <div className="space-y-2">
          <label className="text-sm font-medium">{t('common:field.role')}</label>
          <select
            value={role}
            onChange={(e) => setRole(e.target.value as 'admin' | 'user')}
            className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
          >
            <option value="user">{t('common:role.user')}</option>
            <option value="admin">{t('common:role.admin')}</option>
          </select>
        </div>
      </div>
      <div className="space-y-2">
        <label className="text-sm font-medium">{t('users:dialog.organizations')}</label>
        {orgIds.length > 0 && (
          <div className="flex flex-wrap gap-1.5">
            {orgIds.map((id) => (
              <span
                key={id}
                className="flex items-center gap-1 rounded-lg bg-primary/10 px-2 py-1 text-xs font-medium text-primary"
              >
                {orgById.get(id)?.name || `#${id}`}
                <button
                  type="button"
                  onClick={() => toggleOrg(id)}
                  className="transition-colors hover:text-destructive"
                  aria-label={t('common:remove')}
                >
                  <X className="h-3 w-3" />
                </button>
              </span>
            ))}
          </div>
        )}
        <div>
          <input
            ref={orgInputRef}
            type="text"
            value={orgQuery}
            onChange={(e) => setOrgQuery(e.target.value)}
            onFocus={() => setOrgFocused(true)}
            onBlur={() => setTimeout(() => setOrgFocused(false), 120)}
            placeholder={t('users:dialog.orgSearchPlaceholder')}
            className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
          />
          {orgFocused &&
            orgDropdownPos &&
            createPortal(
              <div
                data-dialog-float
                className="fixed z-[60] max-h-48 overflow-auto rounded-lg border bg-card py-1 shadow-lg"
                style={{
                  pointerEvents: 'auto',
                  top: orgDropdownPos.top,
                  left: orgDropdownPos.left,
                  width: orgDropdownPos.width,
                }}
              >
                {orgResults.length === 0 ? (
                  <div className="px-3 py-2 text-sm text-muted-foreground">
                    {orgQuery.trim() === ''
                      ? t('users:dialog.allSelected')
                      : t('users:dialog.noResult')}
                  </div>
                ) : (
                  orgResults.map((o) => (
                    <button
                      key={o.id}
                      type="button"
                      onMouseDown={(e) => {
                        e.preventDefault()
                        toggleOrg(o.id)
                        setOrgQuery('')
                      }}
                      className="block w-full px-3 py-1.5 text-left text-sm transition-colors hover:bg-muted"
                    >
                      {o.name}
                    </button>
                  ))
                )}
              </div>,
              document.body,
            )}
        </div>
      </div>
    </DialogShell>
  )
}

export default function AdminUsersPage() {
  const { t } = useTranslation(['users', 'common'])
  const [allOrgs, setAllOrgs] = useState<Organization[]>([])

  useEffect(() => {
    api
      .get('/admin/organizations')
      .then((res) => setAllOrgs(res.data))
      .catch(() => {})
  }, [])

  const columnDefs = useMemo<ColDef[]>(
    () => [
      { field: 'id', headerName: t('users:grid.id'), width: 80, sort: 'asc' },
      {
        field: 'name',
        headerName: t('users:grid.name'),
        width: 180,
        cellRenderer: (params: any) => (
          <div className="flex items-center gap-2">
            {params.data?.avatar_url && (
              <img
                src={params.data.avatar_url}
                alt=""
                className="h-6 w-6 rounded-full object-cover"
              />
            )}
            <span>{params.value}</span>
          </div>
        ),
      },
      { field: 'email', headerName: t('users:grid.email'), width: 220 },
      {
        field: 'role',
        headerName: t('users:grid.role'),
        width: 120,
        cellRenderer: (params: any) => (
          <span
            className={`rounded-full px-2.5 py-1 text-xs font-medium ${params.value === 'admin' ? 'bg-primary/10 text-primary' : 'bg-muted text-muted-foreground'}`}
          >
            {params.value === 'admin' ? t('common:role.admin') : t('common:role.user')}
          </span>
        ),
      },
      {
        field: 'organizations',
        headerName: t('users:grid.organizations'),
        width: 280,
        valueGetter: (params: any) =>
          params.data?.organizations?.map((o: Organization) => o.name).join(', ') || '',
      },
      {
        field: 'created_at',
        headerName: t('users:grid.createdAt'),
        width: 140,
        valueFormatter: (p: any) => formatDate(p.value),
      },
    ],
    [t],
  )

  return (
    <CrudGridPage<User>
      title={t('users:title')}
      gridPath="/admin/users/grid"
      deletePath={(id) => `/admin/users/${id}`}
      columnDefs={columnDefs}
      printTitle={t('users:printTitle')}
      printColumns={[
        { header: t('users:grid.name'), value: (u) => u.name },
        { header: t('users:grid.email'), value: (u) => u.email },
        {
          header: t('users:grid.role'),
          value: (u) => (u.role === 'admin' ? t('common:role.admin') : t('common:role.user')),
        },
        {
          header: t('users:grid.organizations'),
          value: (u) => (u.organizations || []).map((o) => o.name).join(', '),
        },
        { header: t('users:grid.createdAt'), value: (u) => formatDate(u.created_at) },
      ]}
      messages={{
        deleteTitle: t('users:deleteTitle'),
        deleteConfirm: (c) => t('users:deleteConfirm', { count: c }),
        deletedN: (c) => t('users:deletedN', { count: c }),
        deleteFailedN: (c) => t('users:deleteFailedN', { count: c }),
        deleteFailed: t('users:deleteFailed'),
      }}
      renderDialog={({ row, onSaved, onClose }) => (
        <UserDialog user={row} allOrgs={allOrgs} onSaved={onSaved} onClose={onClose} />
      )}
    />
  )
}
