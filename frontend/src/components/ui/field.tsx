import type { ReactNode } from 'react'
import { cn } from '@/lib/utils'

export function Field({
  label,
  htmlFor,
  error,
  className,
  children,
}: Readonly<{
  label: string
  htmlFor?: string
  error?: string | null
  className?: string
  children: ReactNode
}>) {
  return (
    <div className={cn('flex min-w-0 flex-col gap-1 text-xs text-fk-muted', className)}>
      <label htmlFor={htmlFor}>{label}</label>
      {children}
      {error && <span className="text-xs text-fk-negative">{error}</span>}
    </div>
  )
}

export const selectCls =
  'h-10 w-full rounded-md border border-fk-input bg-fk-surface px-2.5 text-sm text-fk-text outline-none focus:border-fk-accent focus:ring-1 focus:ring-fk-accent'
