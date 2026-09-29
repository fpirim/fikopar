import type { NumberSeries } from '../api/types'

// PREFIX-YEAR-DEPOT-COUNTER, e.g. TAH-2026-1-000234.
export function formatNumber(s: NumberSeries, depotCode: string): string {
  return `${s.prefix}-${s.year}-${depotCode}-${String(s.nextValue).padStart(s.width, '0')}`
}
