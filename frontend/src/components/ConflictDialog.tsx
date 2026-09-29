import { useTranslation } from 'react-i18next'
import { Button } from './ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from './ui/dialog'

// 412 on an If-Match write: someone saved first; the only way on is to reload.
export default function ConflictDialog({
  title,
  message,
  onReload,
}: Readonly<{ title: string; message: string; onReload: () => void }>) {
  const { t } = useTranslation('common')
  return (
    <Dialog open onOpenChange={(o) => !o && onReload()}>
      <DialogContent role="alertdialog" className="max-w-sm">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{message}</DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button onClick={onReload} className="min-w-[96px]">
            {t('reload')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
