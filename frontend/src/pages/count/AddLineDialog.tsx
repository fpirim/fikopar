import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { addStockCountLine } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type { CatalogHit, StockCount } from '../../api/types'
import { useDepots } from '../../hooks/useDepots'
import { useIdempotencyKey } from '../../hooks/useIdempotencyKey'
import CatalogSearch from '../../components/catalog/CatalogSearch'
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
  const depots = useDepots().filter((d) => d.id === count.depotId)
  const [q, setQ] = useState('')
  const [open, setOpen] = useState(false)
  const [part, setPart] = useState<CatalogHit | null>(null)
  const [qty, setQty] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

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
          <div className="flex min-w-0 flex-col gap-1 text-xs text-fk-muted">
            <span>{t('add.part')}</span>
            <CatalogSearch
              orgId={orgId}
              depots={depots}
              open={open}
              onOpenChange={setOpen}
              query={q}
              onQueryChange={(v) => {
                setQ(v)
                setPart(null)
                setOpen(true)
              }}
              date={count.countDate}
              onPick={(hit) => {
                setPart(hit)
                setQ(hit.stockNo)
                setOpen(false)
              }}
            />
            {part && <p className="truncate text-13 text-fk-muted">{part.name}</p>}
          </div>
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
