import { createRoot } from 'react-dom/client'
import * as Sentry from '@sentry/react'
import './index.css'
import './lib/ag-grid'
import i18n from './i18n'
import App from './App'
import { initSentry } from './lib/sentry'

initSentry()

async function mocks() {
  if (import.meta.env.DEV && import.meta.env.VITE_MSW === '1') {
    const { worker } = await import('./mocks/browser')
    await worker.start({ onUnhandledFrame: 'bypass', quiet: true })
  }
}

void mocks().then(() =>
  createRoot(document.getElementById('root')!).render(
    // Rendered outside the React tree, so use the global i18n instance.
    <Sentry.ErrorBoundary fallback={<p className="p-4">{i18n.t('errors:ERR_UNKNOWN')}</p>}>
      <App />
    </Sentry.ErrorBoundary>,
  ),
)
