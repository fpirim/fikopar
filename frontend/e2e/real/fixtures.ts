import { test as base, expect, type Page } from '@playwright/test'
import { seeded } from './state'

// Signed in as the seeded admin on Demo Company A, English.
export const test = base.extend<{ page: Page }>({
  page: async ({ page }, use) => {
    const { token, orgId } = seeded()
    await page.addInitScript(
      ([t, o]) => {
        localStorage.setItem('token', t)
        localStorage.setItem('selectedOrganizationId', o)
        localStorage.setItem('lang', 'en')
      },
      [token, String(orgId)] as const,
    )
    // eslint-disable-next-line react-hooks/rules-of-hooks
    await use(page)
  },
})

export { expect, seeded }

// Needs a backend on contracts v1.1 (run after the backend merge: npm run e2e:real:v11).
export const V11 = { tag: '@v11' }

// Needs a backend on contracts v1.2, catalog and stock (npm run e2e:real:v12).
export const V12 = { tag: '@v12' }

// Needs a backend on contracts v1.3 (npm run e2e:real:v13).
export const V13 = { tag: '@v13' }

// Needs a backend on contracts v1.4, documents (npm run e2e:real:v14).
export const V14 = { tag: '@v14' }
