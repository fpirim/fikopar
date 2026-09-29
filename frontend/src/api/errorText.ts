import type { TFunction } from 'i18next'
import { ApiError } from './client'

// Translated message for an API error. `date` is the posting date a closed-year refusal is about.
export function errorText(t: TFunction, e: unknown, ctx: { date?: string } = {}): string {
  if (!(e instanceof ApiError)) return t('errors:ERR_UNKNOWN')
  if (e.code === 'ERR_YEAR_CLOSED' && ctx.date) {
    const year = Number(ctx.date.slice(0, 4))
    return t('errors:yearClosed', { year, next: year + 1 })
  }
  if (e.code === 'ERR_ENTRY_OWNED_BY_SOURCE' && e.owner?.sourceNo) {
    return t('errors:ownedBy', { no: e.owner.sourceNo })
  }
  return t(`errors:${e.code}`, { defaultValue: t('errors:ERR_UNKNOWN') })
}
