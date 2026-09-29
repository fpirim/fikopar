import { test as base, expect, type Page } from '@playwright/test'

// Signed in as the MSW demo user (or an admin), company 1, English unless a test picks Turkish.
export const test = base.extend<{ page: Page; lang: 'en' | 'tr'; admin: boolean }>({
  lang: ['en', { option: true }],
  admin: [false, { option: true }],
  page: async ({ page, lang, admin }, use) => {
    await page.addInitScript(
      ([l, token]) => {
        localStorage.setItem('token', token)
        localStorage.setItem('selectedOrganizationId', '1')
        if (!localStorage.getItem('lang')) localStorage.setItem('lang', l)
      },
      [lang, admin ? 'msw-admin' : 'msw-token'] as const,
    )
    // eslint-disable-next-line react-hooks/rules-of-hooks
    await use(page)
  },
})

export { expect }

export const grid = (page: Page) => page.locator('.fk-grid').first()
export const pinnedBottom = (page: Page) => grid(page).locator('.ag-floating-bottom')

export { clipped } from '../clipped'
