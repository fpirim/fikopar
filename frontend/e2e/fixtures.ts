import { test as base, expect, type Page } from '@playwright/test'
import { mkdirSync, writeFileSync } from 'node:fs'
import { randomUUID } from 'node:crypto'
import path from 'node:path'

const NYC_DIR = path.resolve(process.cwd(), '.nyc_output')

// Collect Istanbul coverage (present only with VITE_COVERAGE=true) into .nyc_output.
async function dumpCoverage(page: Page) {
  let coverage: unknown
  try {
    coverage = await page.evaluate(
      () => (window as unknown as { __coverage__?: unknown }).__coverage__,
    )
  } catch {
    return // page blank / navigating — nothing to collect
  }
  if (coverage) {
    mkdirSync(NYC_DIR, { recursive: true })
    writeFileSync(path.join(NYC_DIR, `pw-${randomUUID()}.json`), JSON.stringify(coverage))
  }
}

// Navigation resets window.__coverage__, so snapshot before every goto and at the end.
export const test = base.extend<{ page: Page; lang: 'en' | 'tr' | null }>({
  lang: ['tr', { option: true }],
  page: async ({ page, lang }, use) => {
    if (lang) {
      await page.addInitScript((l) => {
        if (!localStorage.getItem('lang')) localStorage.setItem('lang', l)
      }, lang)
    }
    const originalGoto = page.goto.bind(page)
    page.goto = async (url: string, options?: Parameters<Page['goto']>[1]) => {
      await dumpCoverage(page)
      return originalGoto(url, options)
    }
    // `use` here is the Playwright fixture callback, not React's use() hook.
    // eslint-disable-next-line react-hooks/rules-of-hooks
    await use(page)
    await dumpCoverage(page)
  },
})

export { expect }
