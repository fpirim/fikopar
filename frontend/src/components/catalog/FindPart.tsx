import { useEffect, useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useDepot, withDepot } from '../../hooks/useDepot'
import { useDepots } from '../../hooks/useDepots'
import { useOrgId } from '../../hooks/useOrgId'
import CatalogSearch from './CatalogSearch'

// ⌘K / Ctrl+K or F2 anywhere: the catalog search under the top-bar search; a pick opens the part.
export default function FindPart({ children }: Readonly<{ children: ReactNode }>) {
  const { t } = useTranslation('shell')
  const orgId = useOrgId()
  const depots = useDepots()
  const { depotId } = useDepot()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState('')

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      const k = e.key.toLowerCase()
      if (e.key === 'F2' || (k === 'k' && (e.metaKey || e.ctrlKey))) {
        e.preventDefault()
        setOpen(true)
      }
    }
    globalThis.addEventListener('keydown', onKey)
    return () => globalThis.removeEventListener('keydown', onKey)
  }, [])

  if (!orgId) return <>{children}</>
  return (
    <CatalogSearch
      orgId={orgId}
      depots={depots}
      open={open}
      onOpenChange={setOpen}
      query={query}
      onQueryChange={setQuery}
      anchor={children}
      align="end"
      pickHint={t('findPart.open')}
      onPick={(hit) => {
        setOpen(false)
        setQuery('')
        navigate(withDepot(`/parts/${hit.partId}`, depotId))
      }}
    />
  )
}
