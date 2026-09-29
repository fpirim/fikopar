import { useEffect, useRef } from 'react'
import PhoneInput2 from 'react-phone-input-2'
import { useTranslation } from 'react-i18next'
import 'react-phone-input-2/lib/style.css'
// Must come after style.css to win on specificity.
import './PhoneInput.css'

// Keeps the value as "+<digits>"; formatting is visual only.
export default function PhoneInput({
  value,
  onChange,
  disabled,
  required,
  placeholder = '+90 5xx xxx xx xx',
}: Readonly<{
  value: string
  onChange: (v: string) => void
  disabled?: boolean
  required?: boolean
  placeholder?: string
}>) {
  const { t } = useTranslation()
  // Portal-like escape: pin .country-list with position:fixed so the dialog's overflow does not clip it.
  const wrapRef = useRef<HTMLDivElement>(null)
  useEffect(() => {
    const wrap = wrapRef.current
    if (!wrap) return

    function position(list: HTMLElement) {
      const trigger = wrap!.querySelector<HTMLElement>('.flag-dropdown')
      if (!trigger) return
      const r = trigger.getBoundingClientRect()
      list.style.position = 'fixed'
      list.style.top = `${r.bottom + 2}px`
      list.style.left = `${r.left}px`
      list.style.width = 'max-content'
      list.style.minWidth = '260px'
      list.style.zIndex = '70'
    }

    let active: HTMLElement | null = null
    let onScroll: (() => void) | null = null

    const obs = new MutationObserver(() => {
      const list = wrap.querySelector<HTMLElement>('.country-list')
      if (list && list !== active) {
        active = list
        position(list)
        onScroll = () => position(list)
        window.addEventListener('scroll', onScroll, true)
        window.addEventListener('resize', onScroll)
      } else if (!list && active) {
        active = null
        if (onScroll) {
          window.removeEventListener('scroll', onScroll, true)
          window.removeEventListener('resize', onScroll)
          onScroll = null
        }
      }
    })
    obs.observe(wrap, { childList: true, subtree: true })
    return () => {
      obs.disconnect()
      if (onScroll) {
        window.removeEventListener('scroll', onScroll, true)
        window.removeEventListener('resize', onScroll)
      }
    }
  }, [])

  return (
    <div ref={wrapRef}>
      <PhoneInput2
        country={'tr'}
        value={value}
        onChange={(raw) => onChange(raw ? '+' + raw : '')}
        disabled={disabled}
        placeholder={placeholder}
        enableSearch
        disableSearchIcon
        searchPlaceholder="Ara..."
        searchNotFound={t('common:noResult')}
        countryCodeEditable={false}
        inputProps={{
          required,
          className:
            'w-full rounded-lg border bg-background py-2 pr-3 text-sm outline-none focus:ring-2 focus:ring-ring',
        }}
        buttonClass="!border !bg-background !rounded-l-lg"
        // z-[70]: above the DialogShell backdrop (z-50).
        dropdownClass="!bg-card !text-foreground !border !z-[70]"
        searchClass="!bg-card"
        // input pl: extra room for the flag button (~52px) — pl-[52px] in Tailwind.
        inputStyle={{ height: 'auto', width: '100%', paddingLeft: '52px' }}
        buttonStyle={{ borderRight: '1px solid hsl(var(--border))' }}
      />
    </div>
  )
}
