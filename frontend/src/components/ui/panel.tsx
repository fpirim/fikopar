import type { HTMLAttributes } from 'react'
import { cn } from '@/lib/utils'

export function Panel({ className, ...props }: HTMLAttributes<HTMLElement>) {
  return (
    <section
      className={cn('min-w-0 rounded-lg border border-fk-border bg-fk-surface', className)}
      {...props}
    />
  )
}

export function KeyValue({
  label,
  children,
  className,
}: Readonly<{ label: string; children: React.ReactNode; className?: string }>) {
  return (
    <div className={cn('flex justify-between gap-3 text-sm', className)}>
      <span>{label}</span>
      <span className="fk-num">{children}</span>
    </div>
  )
}
