import { useTranslation } from 'react-i18next'
import { Check } from 'lucide-react'
import type { CarryForward } from '../../api/types'
import { cn } from '@/lib/utils'

type Step = CarryForward['steps'][number]

// The check behind each step, for the blocked tooltip.
const CHECK: Record<Step['code'], string> = {
  CLOSE_DOCUMENTS: 'NO_DRAFT_DOCUMENTS',
  STOCK_COUNTS: 'STOCK_COUNTS_POSTED',
  ACCOUNT_BALANCES: 'LEDGER_BALANCED',
  OPEN_YEAR: 'YEAR_NOT_OPENED',
}

export default function Steps({ cf }: Readonly<{ cf: CarryForward }>) {
  const { t } = useTranslation('ledger')
  const blocked = (s: Step) => {
    const c = cf.checks.find((x) => x.code === CHECK[s.code])
    return c ? t('carryForward.blocked', { detail: c.detail ?? c.code }) : undefined
  }

  return (
    <ol
      aria-label={t('carryForward.steps')}
      className="flex items-center gap-3 rounded-lg border border-fk-border bg-fk-surface px-5 py-4"
    >
      {cf.steps.map((s, i) => (
        <li
          key={s.code}
          className="contents"
          data-status={s.status}
          aria-current={s.status === 'CURRENT' ? 'step' : undefined}
        >
          {i > 0 && (
            <span
              aria-hidden="true"
              className={cn(
                'h-0.5 flex-1',
                cf.steps[i - 1].status === 'DONE' ? 'bg-fk-accent' : 'bg-fk-input',
              )}
            />
          )}
          <span
            title={s.status === 'BLOCKED' ? blocked(s) : undefined}
            className={cn(
              'flex items-center gap-2 whitespace-nowrap text-sm',
              s.status === 'CURRENT' && 'font-semibold',
              s.status === 'TODO' && 'text-fk-muted',
              s.status === 'BLOCKED' && 'text-fk-negative',
            )}
          >
            <span
              className={cn(
                'inline-flex h-6 w-6 shrink-0 items-center justify-center rounded-full font-mono text-xs font-medium',
                s.status === 'DONE' && 'bg-fk-accent text-white',
                s.status === 'CURRENT' && 'border-2 border-fk-accent text-fk-accent',
                s.status === 'TODO' && 'bg-fk-divider text-fk-muted',
                s.status === 'BLOCKED' && 'border-2 border-fk-negative',
              )}
            >
              {s.status === 'DONE' ? <Check className="h-3.5 w-3.5" /> : i + 1}
            </span>
            {t(`carryForward.step.${s.code}`, {
              year: s.code === 'OPEN_YEAR' ? cf.year : cf.year - 1,
            })}
          </span>
        </li>
      ))}
    </ol>
  )
}
