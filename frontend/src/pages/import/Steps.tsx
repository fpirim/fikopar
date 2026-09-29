import { useTranslation } from 'react-i18next'
import { cn } from '@/lib/utils'

export type Step = 'upload' | 'map' | 'preview' | 'apply' | 'result'

const STEPS: Step[] = ['upload', 'map', 'preview', 'apply', 'result']
const MOVABLE: Step[] = ['map', 'preview', 'apply']

// Upload → Map columns → Preview → Apply → Result; `onGo` lets a ready job move between the middle steps.
export default function Steps({
  current,
  onGo,
}: Readonly<{ current: Step; onGo?: (step: Step) => void }>) {
  const { t } = useTranslation('catalog')
  const at = STEPS.indexOf(current)
  return (
    <ol aria-label={t('import.stepsLabel')} className="flex items-center gap-1.5">
      {STEPS.map((s, i) => (
        <li key={s} className="flex items-center gap-1.5">
          {i > 0 && <span aria-hidden="true" className="h-px w-6 bg-fk-border" />}
          <button
            type="button"
            aria-current={s === current ? 'step' : undefined}
            disabled={!onGo || !MOVABLE.includes(s) || s === current}
            onClick={() => onGo?.(s)}
            className={cn(
              'flex h-8 items-center gap-2 whitespace-nowrap rounded-md border px-2.5 text-13 font-medium',
              s === current
                ? 'border-fk-accent bg-fk-selected text-fk-accent'
                : 'border-fk-border bg-fk-surface text-fk-muted',
              onGo && MOVABLE.includes(s) && s !== current && 'hover:text-fk-text',
            )}
          >
            <span
              className={cn(
                'flex h-5 w-5 items-center justify-center rounded-full font-mono text-2xs',
                i < at || s === current
                  ? 'bg-fk-accent text-white'
                  : 'bg-fk-chip-grey text-fk-secondary',
              )}
            >
              {i + 1}
            </span>
            {t(`import.steps.${s}`)}
          </button>
        </li>
      ))}
    </ol>
  )
}
