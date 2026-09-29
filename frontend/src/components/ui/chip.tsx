import type { ReactNode } from 'react'
import { cn } from '@/lib/utils'

export type ChipTone = 'blue' | 'grey' | 'amber' | 'green' | 'red'

const TONES: Record<ChipTone, string> = {
  blue: 'bg-fk-chip-blue text-fk-chip-blue-fg',
  grey: 'bg-fk-chip-grey text-fk-chip-grey-fg',
  amber: 'bg-fk-chip-amber text-fk-chip-amber-fg',
  green: 'bg-fk-chip-green text-fk-chip-green-fg',
  red: 'bg-fk-chip-red text-fk-chip-red-fg',
}

export function Chip({
  tone = 'grey',
  size = 'md',
  children,
  className,
}: Readonly<{ tone?: ChipTone; size?: 'sm' | 'md'; children: ReactNode; className?: string }>) {
  return (
    <span
      className={cn(
        'inline-flex shrink-0 items-center whitespace-nowrap rounded font-mono font-medium',
        size === 'sm' ? 'h-5 px-[7px] text-2xs' : 'h-[22px] px-2 text-xs',
        TONES[tone],
        className,
      )}
    >
      {children}
    </span>
  )
}
