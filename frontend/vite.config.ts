import { defineConfig, type Plugin } from 'vite'
import { readFileSync } from 'node:fs'
import react from '@vitejs/plugin-react'
import istanbul from 'vite-plugin-istanbul'
import path from 'node:path'

// VITE_COVERAGE=true instruments for E2E coverage; never on in normal builds.
const withCoverage = process.env.VITE_COVERAGE === 'true'

// Dev server only: MSW's worker is served from node_modules, never copied into dist.
function mswWorker(): Plugin {
  return {
    name: 'msw-worker',
    apply: 'serve',
    configureServer(server) {
      server.middlewares.use('/mockServiceWorker.js', (_req, res) => {
        res.setHeader('Content-Type', 'text/javascript')
        res.end(readFileSync(path.resolve(__dirname, 'node_modules/msw/lib/mockServiceWorker.js')))
      })
    },
  }
}

export default defineConfig({
  plugins: [
    react(),
    mswWorker(),
    ...(withCoverage
      ? [
          istanbul({
            include: 'src/*',
            extension: ['.ts', '.tsx'],
            requireEnv: false,
          }),
        ]
      : []),
  ],
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') },
  },
  server: {
    port: 5173,
    proxy: Object.fromEntries(
      ['/api', '/healthz'].map((p) => [
        p,
        { target: process.env.VITE_API_TARGET || 'http://localhost:8080', changeOrigin: true },
      ]),
    ),
  },
})
