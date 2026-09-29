import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react'
import { useTranslation } from 'react-i18next'
import { AlertCircle, CheckCircle2, Info, X } from 'lucide-react'

type ToastType = 'success' | 'error' | 'info'

interface Toast {
  id: number
  type: ToastType
  message: string
}

interface ToastContextType {
  show: (type: ToastType, message: string) => void
  success: (message: string) => void
  error: (message: string) => void
  info: (message: string) => void
}

const ToastContext = createContext<ToastContextType | undefined>(undefined)

const AUTO_DISMISS_MS = 4000

export function ToastProvider({ children }: Readonly<{ children: ReactNode }>) {
  const [toasts, setToasts] = useState<Toast[]>([])
  const idRef = useRef(0)

  const remove = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  const show = useCallback(
    (type: ToastType, message: string) => {
      const id = ++idRef.current
      setToasts((prev) => [...prev, { id, type, message }])
      setTimeout(() => remove(id), AUTO_DISMISS_MS)
    },
    [remove],
  )

  const value = useMemo<ToastContextType>(
    () => ({
      show,
      success: (m) => show('success', m),
      error: (m) => show('error', m),
      info: (m) => show('info', m),
    }),
    [show],
  )

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="pointer-events-none fixed right-4 top-4 z-[100] flex w-80 max-w-[calc(100vw-2rem)] flex-col gap-2">
        {toasts.map((t) => (
          <ToastItem key={t.id} toast={t} duration={AUTO_DISMISS_MS} onClose={() => remove(t.id)} />
        ))}
      </div>
    </ToastContext.Provider>
  )
}

const STYLES = {
  success: {
    Icon: CheckCircle2,
    icon: 'text-green-600',
    box: 'border-green-500/40 bg-green-50 text-green-900',
    bar: 'bg-green-500',
  },
  error: {
    Icon: AlertCircle,
    icon: 'text-red-600',
    box: 'border-red-500/40 bg-red-50 text-red-900',
    bar: 'bg-red-500',
  },
  info: {
    Icon: Info,
    icon: 'text-blue-600',
    box: 'border-blue-500/40 bg-blue-50 text-blue-900',
    bar: 'bg-blue-500',
  },
} as const

function ToastItem({
  toast,
  duration,
  onClose,
}: Readonly<{
  toast: Toast
  duration: number
  onClose: () => void
}>) {
  const { t } = useTranslation('common')
  const s = STYLES[toast.type]
  const { Icon } = s
  return (
    <output
      className={`pointer-events-auto relative flex items-start gap-3 overflow-hidden rounded-lg border p-3 pb-4 shadow-lg animate-[toast-in_0.2s_ease-out] ${s.box}`}
    >
      <Icon className={`mt-0.5 h-5 w-5 shrink-0 ${s.icon}`} />
      <p className="flex-1 break-words text-sm">{toast.message}</p>
      <button
        type="button"
        onClick={onClose}
        aria-label={t('close')}
        className="shrink-0 rounded p-0.5 opacity-70 transition-opacity hover:opacity-100"
      >
        <X className="h-4 w-4" />
      </button>
      <div
        className={`absolute bottom-0 left-0 h-1 animate-[toast-progress_linear_forwards] ${s.bar}`}
        style={{ animationDuration: `${duration}ms` }}
      />
    </output>
  )
}

export function useToast() {
  const ctx = useContext(ToastContext)
  if (!ctx) throw new Error('useToast must be used within ToastProvider')
  return ctx
}
