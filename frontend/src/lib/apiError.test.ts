import { describe, it, expect } from 'vitest'
import { getErrorCode } from './apiError'

describe('getErrorCode', () => {
  it('extracts the backend code from an axios error', () => {
    expect(getErrorCode({ response: { data: { code: 'ERR_SLUG_EXISTS' } } })).toBe(
      'ERR_SLUG_EXISTS',
    )
  })

  it('falls back to ERR_UNKNOWN when there is no code', () => {
    expect(getErrorCode(new Error('boom'))).toBe('ERR_UNKNOWN')
    expect(getErrorCode(undefined)).toBe('ERR_UNKNOWN')
    expect(getErrorCode({ response: { data: {} } })).toBe('ERR_UNKNOWN')
  })
})
