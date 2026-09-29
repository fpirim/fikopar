import { useTranslation } from 'react-i18next'

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
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <button
        type="button"
        aria-label={t('close')}
        className="absolute inset-0 bg-black/50"
        onClick={onClose}
      />
      <div className="relative w-full max-w-md rounded-xl border bg-card p-6 shadow-2xl">
        <h2 className="mb-2 text-lg font-bold">{title}</h2>
        <p className="mb-6 text-sm text-muted-foreground">{message}</p>
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
      </div>
    </div>
  )
}
