import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import path from 'node:path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.ts',
    css: false,
    include: ['src/**/*.{test,spec}.{ts,tsx}'],
    // Grid pages take 3–7 s under v8 coverage on a loaded machine; one waitFor alone may use asyncUtilTimeout (5 s).
    testTimeout: 10_000,
    // One worker per core oversubscribes (jsdom + AG Grid are CPU-bound, E-cores are slow): per-test time halves
    // at 50 % while the suite takes about the same, so parallel runs keep their tests far from the timeout.
    maxWorkers: '50%',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'lcov'],
      include: ['src/**/*.{ts,tsx}'],
      exclude: [
        'src/**/*.{test,spec}.{ts,tsx}',
        'src/test/**',
        'src/main.tsx',
        'src/types/**',
        'src/api/schema.d.ts',
        'src/mocks/**',
        // E2E-only: jsdom cannot drive the caret logic, and v8 + Istanbul line models clash in Sonar.
        'src/components/ui/NumericInput.tsx',
        'src/pages/NumericInputPlayground.tsx',
      ],
    },
  },
})
