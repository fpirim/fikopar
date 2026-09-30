import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import SidePanel from './SidePanel'

const totals = {
  grossTotal: '100.00',
  discountTotal: '0.00',
  netTotal: '100.00',
  vatTotal: '20.00',
  grandTotal: '120.00',
  vatBreakdown: [{ vatPct: '20', base: '100.00', vat: '20.00' }],
}
const effects = {
  ledger: [{ role: null, accountCode: '120.1', side: 'DEBIT' as const, amount: '120.00' }],
  stock: [],
  creditUsedPctAfter: 64,
}
const depots = [{ id: 1, code: '1', name: 'Depo 1' }]

describe('SidePanel', () => {
  it.each(['DRAFT', 'POSTED'] as const)('shows the credit used on a %s document', (status) => {
    render(<SidePanel totals={totals} effects={effects} status={status} depots={depots} />)
    expect(screen.getByText('Kullanılan kredi')).toBeInTheDocument()
    expect(screen.getByText('%64')).toBeInTheDocument()
  })

  it('leaves it out once the document is cancelled', () => {
    render(<SidePanel totals={totals} effects={effects} status="CANCELLED" depots={depots} />)
    expect(screen.queryByText('Kullanılan kredi')).not.toBeInTheDocument()
  })
})
