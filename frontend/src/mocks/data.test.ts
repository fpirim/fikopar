import { describe, expect, it } from 'vitest'
import { createDb } from './data'

describe('createDb', () => {
  it('gives each database its own party balances', () => {
    const a = createDb()
    a.ledger.parties[0].debit[0] += 100
    a.ledger.parties[0].credit[1] += 100
    a.ledger.parties[0].opening[0] += 100
    const b = createDb()
    expect(b.ledger.parties[0].debit[0]).toBe(a.ledger.parties[0].debit[0] - 100)
    expect(b.ledger.parties[0].credit[1]).toBe(a.ledger.parties[0].credit[1] - 100)
    expect(b.ledger.parties[0].opening[0]).toBe(a.ledger.parties[0].opening[0] - 100)
  })
})
