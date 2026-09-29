import { useTranslation } from 'react-i18next'
import type { StockCountLine } from '../../api/types'
import { formatQty, formatTime } from '../../lib/format'
import { Button } from '../../components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog'

// 412 on a line: someone else saved it first. Keep ours (saved again on their version) or take theirs.
export default function LineConflictDialog({
  stockNo,
  mine,
  theirs,
  onKeep,
  onTake,
}: Readonly<{
  stockNo: string
  mine: string | null
  theirs: StockCountLine
  onKeep: () => void
  onTake: () => void
}>) {
  const { t } = useTranslation('counts')
  const q = (v: string | null | undefined) => (v === null || v === undefined ? '—' : formatQty(v))
  return (
    <Dialog open onOpenChange={(o) => !o && onTake()}>
      <DialogContent role="alertdialog" className="max-w-md">
        <DialogHeader>
          <DialogTitle>{t('conflict.title')}</DialogTitle>
          <DialogDescription>
            {t('conflict.message', {
              by: theirs.countedBy ?? '—',
              qty: q(theirs.countedQty),
              no: stockNo,
              time: formatTime(theirs.countedAt).slice(0, 5),
              mine: q(mine),
            })}
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="outline" onClick={onTake} className="min-w-[96px]">
            {t('conflict.take', { qty: q(theirs.countedQty) })}
          </Button>
          <Button onClick={onKeep} className="min-w-[96px]">
            {t('conflict.keep', { qty: q(mine) })}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
