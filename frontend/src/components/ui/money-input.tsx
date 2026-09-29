import { useState } from 'react'
import { formatMoney, parseAmount } from '@/lib/format'
import { cn } from '@/lib/utils'

// Amount box: free typing in the UI language, formatted when it loses focus; value is a Money string.
export function MoneyInput({
  value,
  onChange,
  ariaLabel,
  className,
  id,
  invalid,
}: Readonly<{
  value: string
  onChange: (money: string) => void
  ariaLabel?: string
  className?: string
  id?: string
  invalid?: boolean
}>) {
  const [draft, setDraft] = useState<string | null>(null)
  const bad = draft !== null && parseAmount(draft) === null
  return (
    <input
      id={id}
      type="text"
      inputMode="decimal"
      aria-label={ariaLabel}
      aria-invalid={bad || invalid || undefined}
      value={draft ?? formatMoney(value)}
      onFocus={() => setDraft(value ? formatMoney(value) : '')}
      onChange={(e) => {
        setDraft(e.target.value)
        const m = parseAmount(e.target.value)
        if (m !== null) onChange(m)
      }}
      onBlur={() => setDraft(null)}
      className={cn(
        'h-10 w-full rounded-md border border-fk-input bg-fk-surface px-2.5 text-right font-mono text-lg text-fk-text outline-none focus:border-fk-accent focus:ring-1 focus:ring-fk-accent aria-[invalid]:border-fk-negative',
        className,
      )}
    />
  )
}
