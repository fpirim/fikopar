import { test as base, expect, type Page } from '@playwright/test'

// Signed in as the MSW demo user, company 1, English unless a test picks Turkish.
export const test = base.extend<{ page: Page; lang: 'en' | 'tr' }>({
  lang: ['en', { option: true }],
  page: async ({ page, lang }, use) => {
    await page.addInitScript((l) => {
      localStorage.setItem('token', 'msw-token')
      localStorage.setItem('selectedOrganizationId', '1')
      if (!localStorage.getItem('lang')) localStorage.setItem('lang', l)
    }, lang)
    // eslint-disable-next-line react-hooks/rules-of-hooks
    await use(page)
  },
})

export { expect }

export const grid = (page: Page) => page.locator('.fk-grid').first()
export const pinnedBottom = (page: Page) => grid(page).locator('.ag-floating-bottom')
