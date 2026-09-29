import { useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Button } from './ui/button'
import { DateField } from './ui/date-field'
import { Popover, PopoverContent, PopoverTrigger } from './ui/popover'

export default function PeriodButton({
  from,
  to,
  onApply,
}: Readonly<{ from: string; to: string; onApply: (from: string, to: string) => void }>) {
  const { t } = useTranslation('ledger')
  const [open, setOpen] = useState(false)
  // DateField commits on blur, which runs before the Apply click; the ref sees it at once.
  const draft = useRef({ from, to })
  return (
    <Popover
      open={open}
      onOpenChange={(o) => {
        setOpen(o)
        if (o) draft.current = { from, to }
      }}
    >
      <PopoverTrigger asChild>
        <Button variant="outline">{t('statement.changePeriod')}</Button>
      </PopoverTrigger>
      <PopoverContent align="end" className="flex w-auto items-end gap-2 p-3">
        <DateField
          label={t('statement.from')}
          value={from}
          onChange={(v) => (draft.current = { ...draft.current, from: v })}
          inputClassName="w-[118px]"
        />
        <DateField
          label={t('statement.to')}
          value={to}
          onChange={(v) => (draft.current = { ...draft.current, to: v })}
          inputClassName="w-[118px]"
        />
        <Button
          onClick={() => {
            setOpen(false)
            onApply(draft.current.from, draft.current.to)
          }}
        >
          {t('statement.apply')}
        </Button>
      </PopoverContent>
    </Popover>
  )
}
