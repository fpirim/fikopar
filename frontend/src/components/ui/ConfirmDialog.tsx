import { useTranslation } from 'react-i18next'
import { Dialog, DialogContent, DialogDescription, DialogTitle } from './dialog'

export default function ConfirmDialog({
  open,
  title,
  message,
  confirmText,
  cancelText,
  destructive = false,
  loading = false,
  onConfirm,
  onClose,
}: Readonly<{
  open: boolean
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  destructive?: boolean
  loading?: boolean
  onConfirm: () => void
  onClose: () => void
}>) {
  const { t } = useTranslation('common')
  if (!open) return null
  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent className="max-w-[calc(100%-2rem)] gap-0 rounded-xl shadow-2xl sm:max-w-md">
        <DialogTitle className="mb-2 text-lg font-bold">{title}</DialogTitle>
        <DialogDescription className="mb-6 text-sm text-muted-foreground">
          {message}
        </DialogDescription>
        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="rounded-lg border px-4 py-2 text-sm font-medium transition-all hover:bg-muted disabled:opacity-40"
          >
            {cancelText ?? t('cancel')}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={loading}
            className={`rounded-lg px-4 py-2 text-sm font-medium transition-all hover:-translate-y-0.5 hover:shadow-lg active:scale-[0.98] disabled:pointer-events-none disabled:opacity-60 ${
              destructive
                ? 'bg-destructive text-destructive-foreground hover:shadow-destructive/30'
                : 'bg-primary text-primary-foreground hover:shadow-primary/30'
            }`}
          >
            {loading ? t('processing') : (confirmText ?? t('confirm'))}
          </button>
        </div>
      </DialogContent>
    </Dialog>
  )
}
