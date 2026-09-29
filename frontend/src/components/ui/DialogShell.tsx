import type { ReactNode } from 'react'
import { useTranslation } from 'react-i18next'

const MAX_WIDTH_CLASS: Record<'md' | 'lg' | 'xl' | '2xl' | '3xl' | '4xl', string> = {
  md: 'sm:max-w-md md:max-w-md',
  lg: 'sm:max-w-md md:max-w-lg',
  xl: 'sm:max-w-lg md:max-w-xl',
  '2xl': 'sm:max-w-xl md:max-w-2xl lg:max-w-3xl xl:max-w-4xl',
  '3xl': 'sm:max-w-xl md:max-w-2xl lg:max-w-4xl xl:max-w-5xl',
  '4xl': 'sm:max-w-2xl md:max-w-3xl lg:max-w-5xl xl:max-w-6xl 2xl:max-w-7xl',
}

export default function DialogShell({
  title,
  error,
  maxWidth = 'md',
  saving,
  submitLabel,
  onSubmit,
  onClose,
  children,
}: Readonly<{
  title: string
  error?: string
  maxWidth?: keyof typeof MAX_WIDTH_CLASS
  saving: boolean
  submitLabel: string
  onSubmit: () => void
  onClose: () => void
  children: ReactNode
}>) {
  const { t } = useTranslation('common')
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6">
      <button
        type="button"
        aria-label={t('close')}
        className="absolute inset-0 bg-black/50"
        onClick={onClose}
      />
      <div
        className={`relative flex w-full ${MAX_WIDTH_CLASS[maxWidth]} max-h-[calc(100vh-2rem)] sm:max-h-[calc(100vh-3rem)] flex-col rounded-xl border bg-card shadow-2xl`}
      >
        <div className="shrink-0 border-b px-6 pt-6 pb-4">
          <h2 className="text-lg font-bold">{title}</h2>
          {error && (
            <div className="mt-3 rounded-lg bg-destructive/10 px-4 py-2 text-sm text-destructive">
              {error}
            </div>
          )}
        </div>
        <form
          onSubmit={(e) => {
            e.preventDefault()
            onSubmit()
          }}
          className="flex min-h-0 flex-1 flex-col"
        >
          <div className="min-h-0 flex-1 space-y-4 overflow-y-auto px-6 py-4">{children}</div>
          <div className="shrink-0 flex justify-end gap-2 border-t px-6 py-4">
            <button
              type="button"
              onClick={onClose}
              disabled={saving}
              className="rounded-lg border px-4 py-2 text-sm font-medium transition-all hover:bg-muted disabled:opacity-40"
            >
              {t('cancel')}
            </button>
            <button
              type="submit"
              disabled={saving}
              className="rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-primary/30 active:scale-[0.98] disabled:pointer-events-none disabled:opacity-60"
            >
              {submitLabel}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
