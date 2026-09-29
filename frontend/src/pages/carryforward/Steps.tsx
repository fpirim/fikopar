import { useTranslation } from 'react-i18next'
import { Check } from 'lucide-react'
import type { CarryForward } from '../../api/types'
import { cn } from '@/lib/utils'

type State = 'done' | 'current' | 'todo' | 'blocked'

export default function Steps({ cf }: Readonly<{ cf: CarryForward }>) {
  const { t } = useTranslation('ledger')
  const posted = cf.status === 'POSTED'
  const check = (code: string) => cf.checks.find((c) => c.code === code)
  const steps: { code: string; year: number; done: boolean; failed?: string }[] = [
    {
      code: 'NO_DRAFT_DOCUMENTS',
      year: cf.year - 1,
      done: Boolean(check('NO_DRAFT_DOCUMENTS')?.ok),
    },
    {
      code: 'STOCK_COUNTS_POSTED',
      year: cf.year - 1,
      done: Boolean(check('STOCK_COUNTS_POSTED')?.ok),
    },
    { code: 'LEDGER_BALANCED', year: cf.year - 1, done: posted },
    { code: 'YEAR_NOT_OPENED', year: cf.year, done: posted },
  ]
  for (const s of steps) {
    const c = check(s.code)
    if (c && !c.ok && c.blocking && !posted) s.failed = c.detail ?? c.code
  }
  const current = steps.findIndex((s) => !s.done)
  const state = (i: number): State => {
    if (steps[i].failed) return 'blocked'
    if (steps[i].done) return 'done'
    return i === current ? 'current' : 'todo'
  }

  return (
    <ol
      aria-label={t('carryForward.steps')}
      className="flex items-center gap-3 rounded-lg border border-fk-border bg-fk-surface px-5 py-4"
    >
      {steps.map((s, i) => {
        const st = state(i)
        return (
          <li
            key={s.code}
            className="contents"
            aria-current={st === 'current' ? 'step' : undefined}
          >
            {i > 0 && (
              <span
                aria-hidden="true"
                className={cn('h-0.5 flex-1', steps[i - 1].done ? 'bg-fk-accent' : 'bg-fk-input')}
              />
            )}
            <span
              title={s.failed ? t('carryForward.blocked', { detail: s.failed }) : undefined}
              className={cn(
                'flex items-center gap-2 whitespace-nowrap text-sm',
                st === 'current' && 'font-semibold',
                st === 'todo' && 'text-fk-muted',
                st === 'blocked' && 'text-fk-negative',
              )}
            >
              <span
                className={cn(
                  'inline-flex h-6 w-6 items-center justify-center rounded-full font-mono text-xs font-medium',
                  st === 'done' && 'bg-fk-accent text-white',
                  st === 'current' && 'border-2 border-fk-accent text-fk-accent',
                  st === 'todo' && 'bg-fk-divider text-fk-muted',
                  st === 'blocked' && 'border-2 border-fk-negative',
                )}
              >
                {st === 'done' && posted && i > 1 ? <Check className="h-3.5 w-3.5" /> : i + 1}
              </span>
              {t(`carryForward.step.${s.code}`, { year: s.year })}
            </span>
          </li>
        )
      })}
    </ol>
  )
}
