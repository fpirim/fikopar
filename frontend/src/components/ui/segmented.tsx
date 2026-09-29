import { cn } from '@/lib/utils'

export interface SegmentOption<T> {
  value: T
  label: string
  disabled?: boolean
}

const SIZES = {
  sm: { box: 'p-[3px]', btn: 'h-7 px-2.5 text-xs' },
  md: { box: 'h-9 p-0.5', btn: 'h-[30px] px-3 text-13' },
  lg: { box: 'p-[3px]', btn: 'h-[34px] px-4 text-sm' },
}

export function Segmented<T extends string | number | null>({
  label,
  value,
  options,
  onChange,
  size = 'md',
  mono,
  className,
}: Readonly<{
  label: string
  value: T
  options: SegmentOption<T>[]
  onChange: (value: T) => void
  size?: keyof typeof SIZES
  mono?: boolean
  className?: string
}>) {
  const s = SIZES[size]
  return (
    <div
      role="group"
      aria-label={label}
      className={cn(
        'flex shrink-0 rounded-md border border-fk-input bg-fk-surface',
        s.box,
        className,
      )}
    >
      {options.map((o) => {
        const on = o.value === value
        return (
          <button
            key={String(o.value)}
            type="button"
            aria-pressed={on}
            disabled={o.disabled}
            onClick={() => onChange(o.value)}
            className={cn(
              'whitespace-nowrap rounded font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-fk-accent disabled:cursor-not-allowed disabled:opacity-50',
              s.btn,
              mono && 'font-mono',
              on ? 'bg-fk-text text-white' : 'text-fk-muted hover:text-fk-text',
            )}
          >
            {o.label}
          </button>
        )
      })}
    </div>
  )
}
