// Debounce and poll intervals (ms), read at call time. Unit tests shorten them in src/test/setup.ts;
// delays.test.ts pins these values and BalancesPage.test.tsx checks the filter debounce with the real delay.
export const APP_DELAYS = {
  gridFilter: 350,
  gridSearch: 300,
  journalSearch: 300,
  globalSearch: 250,
  accountSearch: 200,
  catalogSearch: 200,
  importPoll: 800,
} as const

export const delays: { -readonly [K in keyof typeof APP_DELAYS]: number } = { ...APP_DELAYS }
