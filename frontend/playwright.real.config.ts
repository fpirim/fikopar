import { defineConfig, devices } from '@playwright/test'

// Vite dev server against a real API (the e2e compose profile), seeded through the API.
//   npm run stack:e2e && npm run e2e:real        contract-conformant flows (backend v1)
//   npm run e2e:real:v11                          everything, incl. @v11 (backend v1.1)
const port = Number(process.env.REAL_PORT || 5176)
const api = process.env.E2E_API_URL || 'http://localhost:18080'

export default defineConfig({
  testDir: './e2e/real',
  globalSetup: './e2e/real/global-setup.ts',
  fullyParallel: false,
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? [['html', { open: 'never' }], ['list']] : 'list',
  use: {
    baseURL: `http://localhost:${port}`,
    viewport: { width: 1440, height: 900 },
    trace: 'on-first-retry',
  },
  webServer: {
    command: `vite --port ${port} --strictPort`,
    url: `http://localhost:${port}`,
    reuseExistingServer: !process.env.CI,
    timeout: 60_000,
    env: { VITE_API_TARGET: api },
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
})
