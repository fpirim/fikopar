import type { ReactNode } from 'react'
import { cn } from '@/lib/utils'

export function Stat({
  label,
  value,
  tone,
}: Readonly<{ label: string; value: ReactNode; tone?: 'in' | 'out' }>) {
  return (
    <div
      role="group"
      aria-label={label}
      className="min-w-0 rounded-lg border border-fk-border bg-fk-surface px-4 py-3.5"
    >
      <div className="text-13 text-fk-muted">{label}</div>
      <div
        className={cn(
          'mt-1.5 truncate font-mono text-22 font-medium tabular-nums',
          tone === 'in' && 'text-fk-chip-green-fg',
          tone === 'out' && 'text-fk-negative',
        )}
      >
        {value}
      </div>
    </div>
  )
}
