import { cn } from '@/lib/utils'

export function TextField({
  label,
  value,
  onChange,
  className,
  mono,
  ...rest
}: Readonly<
  {
    label: string
    value: string
    onChange: (v: string) => void
    className?: string
    mono?: boolean
  } & Omit<React.InputHTMLAttributes<HTMLInputElement>, 'value' | 'onChange' | 'className'>
>) {
  return (
    <label className={cn('flex min-w-0 flex-col gap-1 text-xs text-fk-muted', className)}>
      {label}
      <input
        {...rest}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className={cn(
          'h-10 w-full rounded-md border border-fk-input bg-fk-surface px-2.5 text-sm text-fk-text outline-none focus:border-fk-accent focus:ring-1 focus:ring-fk-accent disabled:bg-fk-sunken disabled:text-fk-faint',
          mono && 'font-mono',
        )}
      />
    </label>
  )
}
