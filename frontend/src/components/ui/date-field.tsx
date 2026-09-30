import { useState } from 'react'
import { formatDay, parseDay } from '@/lib/format'
import { cn } from '@/lib/utils'

// dd.MM.yyyy text input; commits an ISO date on Enter or blur, reverts junk.
export function DateField({
  label,
  value,
  onChange,
  className,
  inputClassName,
  ariaLabel,
}: Readonly<{
  label: string
  ariaLabel?: string
  value: string
  onChange: (iso: string) => void
  className?: string
  inputClassName?: string
}>) {
  const [draft, setDraft] = useState<string | null>(null)
  const commit = () => {
    if (draft === null) return
    const iso = parseDay(draft)
    setDraft(null)
    if (iso && iso !== value) onChange(iso)
  }
  return (
    <label className={cn('flex flex-col gap-1 text-xs text-fk-muted', className)}>
      {label}
      <input
        type="text"
        inputMode="numeric"
        aria-label={ariaLabel}
        value={draft ?? formatDay(value)}
        onChange={(e) => setDraft(e.target.value)}
        onBlur={commit}
        onKeyDown={(e) => e.key === 'Enter' && commit()}
        aria-invalid={draft !== null && draft.length >= 8 && !parseDay(draft) ? true : undefined}
        className={cn(
          'h-9 rounded-md border border-fk-input bg-fk-surface px-2.5 font-mono text-sm text-fk-text outline-none focus:border-fk-accent focus:ring-1 focus:ring-fk-accent aria-[invalid]:border-fk-negative',
          inputClassName,
        )}
      />
    </label>
  )
}
