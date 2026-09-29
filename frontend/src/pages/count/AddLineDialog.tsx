import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { addStockCountLine, searchCatalog } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type { CatalogHit, StockCount } from '../../api/types'
import { useIdempotencyKey } from '../../hooks/useIdempotencyKey'
import { cn } from '@/lib/utils'
import { Button } from '../../components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog'
import { TextField } from '../../components/ui/text-field'
import { parseCounted } from './parseCounted'

// A part found on a shelf that is not on the sheet.
export default function AddLineDialog({
  orgId,
  count,
  onClose,
  onAdded,
}: Readonly<{
  orgId: number
  count: StockCount
  onClose: () => void
  onAdded: (stockNo: string) => void
}>) {
  const { t } = useTranslation(['counts', 'common', 'errors'])
  const idem = useIdempotencyKey()
  const [q, setQ] = useState('')
  const [hits, setHits] = useState<CatalogHit[]>([])
  const [part, setPart] = useState<CatalogHit | null>(null)
  const [qty, setQty] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    const text = q.trim()
    if (text.length < 2) return
    const ctrl = new AbortController()
    const timer = setTimeout(() => {
      searchCatalog(orgId, { q: text, limit: 5 }, ctrl.signal)
        .then((r) => setHits(r.items))
        .catch(() => undefined)
    }, 250)
    return () => {
      clearTimeout(timer)
      ctrl.abort()
    }
  }, [orgId, q])

  const counted = parseCounted(qty)

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    if (!part || counted === undefined) return
    const body = { partId: part.partId, ...(counted !== null && { countedQty: counted }) }
    setBusy(true)
    setError('')
    try {
      await addStockCountLine(orgId, count.id, body, idem.keyFor(body))
      onAdded(part.stockNo)
    } catch (err) {
      setError(errorText(t, err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent aria-describedby={undefined} className="max-w-md">
        <DialogHeader>
          <DialogTitle>{t('add.title', { no: count.countNo })}</DialogTitle>
        </DialogHeader>
        <form onSubmit={submit} className="flex flex-col gap-3">
          <TextField label={t('add.part')} value={q} onChange={setQ} mono autoFocus />
          {q.trim().length >= 2 && (
            <div role="listbox" aria-label={t('add.part')} className="flex flex-col">
              {hits.map((h) => (
                <button
                  key={h.partId}
                  type="button"
                  role="option"
                  aria-selected={part?.partId === h.partId}
                  onClick={() => setPart(h)}
                  className={cn(
                    'flex items-center gap-3 rounded px-2 py-1.5 text-left text-13 hover:bg-fk-sunken',
                    part?.partId === h.partId && 'bg-fk-selected',
                  )}
                >
                  <span className="w-[88px] shrink-0 font-mono">{h.stockNo}</span>
                  <span className="min-w-0 truncate">{h.name}</span>
                </button>
              ))}
            </div>
          )}
          <TextField
            label={t('add.counted')}
            value={qty}
            onChange={setQty}
            mono
            inputMode="decimal"
          />
          {counted === undefined && <p className="text-xs text-fk-negative">{t('save.invalid')}</p>}
          {error && (
            <p role="alert" className="text-13 text-fk-negative">
              {error}
            </p>
          )}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>
              {t('common:cancel')}
            </Button>
            <Button type="submit" disabled={busy || !part} className="min-w-[96px]">
              {t('add.submit')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
