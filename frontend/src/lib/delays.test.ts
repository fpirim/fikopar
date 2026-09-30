import { describe, expect, it } from 'vitest'
import { APP_DELAYS } from './delays'

describe('app delays', () => {
  it('keeps the debounce and poll intervals the app runs with', () => {
    expect(APP_DELAYS).toEqual({
      gridFilter: 350,
      gridSearch: 300,
      journalSearch: 300,
      globalSearch: 250,
      accountSearch: 200,
      catalogSearch: 200,
      importPoll: 800,
    })
  })
})
