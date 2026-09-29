import { defineConfig, devices } from '@playwright/test'

// Serves an Istanbul-instrumented dev server and proxies /api to a running backend (VITE_API_TARGET).
const apiTarget = process.env.VITE_API_TARGET || 'http://localhost:8080'

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  reporter: 'list',
  use: {
    baseURL: 'http://localhost:3000',
    trace: 'on-first-retry',
  },
  webServer: {
    command: 'vite --port 3000 --strictPort',
    url: 'http://localhost:3000',
    reuseExistingServer: false,
    timeout: 60_000,
    env: {
      VITE_COVERAGE: 'true',
      VITE_API_TARGET: apiTarget,
    },
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
})
