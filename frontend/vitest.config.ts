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
