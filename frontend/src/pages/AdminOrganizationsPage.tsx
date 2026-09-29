import { useMemo, useRef, useState } from 'react'
import type { ColDef } from 'ag-grid-community'
import { useTranslation } from 'react-i18next'
import { Building2 } from 'lucide-react'
import api from '../lib/api'
import { formatDate } from '../lib/format'
import { useToast } from '../context/ToastContext'
import { useApiError } from '../hooks/useApiError'
import { useImagePicker } from '../hooks/useImagePicker'
import CrudGridPage from '../components/ui/CrudGridPage'
import { useOrganization } from '../context/OrganizationContext'
import DialogShell from '../components/ui/DialogShell'
import ImagePicker from '../components/ui/ImagePicker'
import type { Organization } from '../types/organization'

function OrgDialog({
  org,
  onSaved,
  onClose,
}: Readonly<{
  org?: Organization
  onSaved: () => void
  onClose: () => void
}>) {
  const [name, setName] = useState(org?.name || '')
  const [slug, setSlug] = useState(org?.slug || '')
  const logoUrlRef = useRef(org?.logo_url || '')
  const picker = useImagePicker(logoUrlRef.current)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const toast = useToast()
  const { t } = useTranslation(['organizations', 'common', 'errors'])
  const apiError = useApiError()

  const handlePick = (file: File) => {
    if (!picker.selectFile(file)) {
      setError(t('errors:fileTooLarge'))
      return
    }
    setError('')
  }

  const handleSubmit = async () => {
    setError('')
    setSaving(true)
    try {
      let finalLogoUrl = logoUrlRef.current

      if (org) {
        if (picker.pendingDelete && !picker.pendingFile) {
          const delResp = await api.delete(`/admin/organizations/${org.id}/logo`)
          finalLogoUrl = delResp.data.logo_url || ''
        }
        if (picker.pendingFile) {
          const fd = new FormData()
          fd.append('logo', picker.pendingFile)
          const upResp = await api.post(`/admin/organizations/${org.id}/logo`, fd, {
            headers: { 'Content-Type': 'multipart/form-data' },
          })
          finalLogoUrl = upResp.data.logo_url || ''
        }
        await api.put(`/admin/organizations/${org.id}`, {
          name,
          slug,
          logo_url: finalLogoUrl || null,
        })
      } else {
        const createResp = await api.post('/admin/organizations', { name, slug })
        const newId = createResp.data.id
        if (picker.pendingFile) {
          const fd = new FormData()
          fd.append('logo', picker.pendingFile)
          await api.post(`/admin/organizations/${newId}/logo`, fd, {
            headers: { 'Content-Type': 'multipart/form-data' },
          })
        }
      }
      toast.success(org ? t('organizations:updated') : t('organizations:created'))
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
  else if (org) submitLabel = t('common:update')

  return (
    <DialogShell
      title={org ? t('organizations:dialog.editTitle') : t('organizations:dialog.createTitle')}
      error={error}
      maxWidth="md"
      saving={saving}
      submitLabel={submitLabel}
      onSubmit={handleSubmit}
      onClose={onClose}
    >
      <ImagePicker
        src={picker.currentSrc}
        alt="logo"
        rounded="xl"
        placeholder={<Building2 className="h-8 w-8 text-muted-foreground/50" />}
        fileRef={picker.fileRef}
        onPick={handlePick}
        onClear={picker.clearImage}
      />
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
        <label className="text-sm font-medium">{t('common:field.slug')}</label>
        <input
          type="text"
          value={slug}
          onChange={(e) => setSlug(e.target.value)}
          className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
          required
        />
      </div>
    </DialogShell>
  )
}

export default function AdminOrganizationsPage() {
  const { t } = useTranslation(['organizations', 'common'])
  const { refresh: refreshOrgs } = useOrganization()

  const columnDefs = useMemo<ColDef[]>(
    () => [
      { field: 'id', headerName: t('organizations:grid.id'), width: 80, sort: 'asc' },
      {
        field: 'name',
        headerName: t('organizations:grid.name'),
        width: 300,
        cellRenderer: (params: any) => (
          <div className="flex items-center gap-2">
            {params.data?.logo_url && (
              <img src={params.data.logo_url} alt="" className="h-6 w-6 rounded object-cover" />
            )}
            <span>{params.value}</span>
          </div>
        ),
      },
      { field: 'slug', headerName: t('organizations:grid.slug'), width: 250 },
      {
        field: 'created_at',
        headerName: t('organizations:grid.createdAt'),
        width: 200,
        valueFormatter: (p: any) => formatDate(p.value),
      },
    ],
    [t],
  )

  return (
    <CrudGridPage<Organization>
      title={t('organizations:title')}
      gridPath="/admin/organizations/grid"
      deletePath={(id) => `/admin/organizations/${id}`}
      columnDefs={columnDefs}
      printTitle={t('organizations:printTitle')}
      printColumns={[
        { header: t('organizations:grid.name'), value: (o) => o.name },
        { header: t('organizations:grid.slug'), value: (o) => o.slug },
        { header: t('organizations:grid.createdAt'), value: (o) => formatDate(o.created_at) },
      ]}
      messages={{
        deleteTitle: t('organizations:deleteTitle'),
        deleteConfirm: (c) => t('organizations:deleteConfirm', { count: c }),
        deletedN: (c) => t('organizations:deletedN', { count: c }),
        deleteFailedN: (c) => t('organizations:deleteFailedN', { count: c }),
        deleteFailed: t('organizations:deleteFailed'),
      }}
      renderDialog={({ row, onSaved, onClose }) => (
        <OrgDialog org={row} onSaved={onSaved} onClose={onClose} />
      )}
      onMutated={refreshOrgs}
    />
  )
}
