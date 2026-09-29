import { describe, expect, it } from 'vitest'
import { useState } from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import i18n from '../../i18n'
import { MoneyInput } from './money-input'

function Harness({ initial = '' }: { initial?: string }) {
  const [v, setV] = useState(initial)
  return (
    <>
      <MoneyInput ariaLabel="Tutar" value={v} onChange={setV} />
      <output>{v}</output>
    </>
  )
}

describe('MoneyInput', () => {
  it('parses typing and formats on blur', async () => {
    render(<Harness />)
    const input = screen.getByLabelText('Tutar')
    await userEvent.type(input, '15.000,5')
    expect(screen.getByRole('status')).toHaveTextContent('15000.50')
    await userEvent.tab()
    expect(input).toHaveValue('15.000,50')
  })

  it('flags junk and keeps the last good value', async () => {
    render(<Harness initial="10.00" />)
    const input = screen.getByLabelText('Tutar')
    expect(input).toHaveValue('10,00')
    await userEvent.clear(input)
    await userEvent.type(input, '1,2,3')
    expect(input).toHaveAttribute('aria-invalid', 'true')
    await userEvent.tab()
    expect(input).toHaveValue('1,20')
  })

  it('uses English separators', async () => {
    await i18n.changeLanguage('en')
    render(<Harness initial="1234.5" />)
    expect(screen.getByLabelText('Tutar')).toHaveValue('1,234.50')
  })
})
