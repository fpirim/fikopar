// MSW specs run the browser on E2E_TODAY, by default the mock data's today (TODAY in src/mocks/data.ts).
export const mswToday = process.env.E2E_TODAY ?? '2026-09-29'

export function addDays(iso: string, days: number): string {
  const d = new Date(`${iso}T00:00:00Z`)
  d.setUTCDate(d.getUTCDate() + days)
  return d.toISOString().slice(0, 10)
}

// yyyy-mm-dd as the UI shows it (dd.mm.yyyy).
export function shown(iso: string): string {
  const [y, m, d] = iso.split('-')
  return `${d}.${m}.${y}`
}
