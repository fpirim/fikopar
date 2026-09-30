import { describe, expect, it } from 'vitest'
import { fireEvent, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderApp } from '../../test/renderApp'

const grid = () => document.querySelector('.fk-doc-lines') as HTMLElement
const cell = (row: number, col: string) =>
  grid().querySelector(
    `.ag-center-cols-container .ag-row[row-index="${row}"] [col-id="${col}"]`,
  ) as HTMLElement
const text = (row: number, col: string) => cell(row, col)?.textContent?.trim()
const rowCount = () => grid().querySelectorAll('.ag-center-cols-container .ag-row').length
const key = (el: HTMLElement, k: string) => fireEvent.keyDown(el, { key: k, code: k })

async function open(path: string) {
  renderApp(path)
  await waitFor(() => expect(text(0, 'rn')).toBe('1'))
}

async function edit(row: number, col: string, value: string) {
  await userEvent.dblClick(cell(row, col))
  const input = within(cell(row, col)).getByRole('textbox')
  await userEvent.clear(input)
  await userEvent.type(input, `${value}{Enter}`)
}

describe('LinesGrid', () => {
  it('edits quantity, price and discounts in the language of the UI and recomputes the line', async () => {
    await open('/invoices/1001')
    await edit(2, 'qty', '10')
    expect(text(2, 'net')).toBe('2.650,00')
    await edit(2, 'price', '1.234,5')
    expect(text(2, 'price')).toBe('1.234,50')
    expect(text(2, 'net')).toBe('12.345,00')
    await edit(2, 'd1', '10')
    await edit(2, 'd2', '10')
    expect(text(2, 'net')).toBe('9.999,45')
    await edit(2, 'vat', '10')
    expect(screen.getByRole('region', { name: 'Toplamlar' })).toHaveTextContent('KDV %10999,95')
    expect(screen.getByRole('button', { name: 'Taslağı kaydet' })).toBeEnabled()
    expect(grid().querySelector('.ag-floating-bottom')).toHaveTextContent('4 satır · 22 adet')
  })

  it('refuses text, zero quantities and discounts over 100 with a message on the line', async () => {
    await open('/invoices/1001')
    await edit(1, 'qty', 'abc')
    expect(await screen.findByText('Satır 2 · Miktar: geçerli bir sayı değil')).toBeInTheDocument()
    expect(text(1, 'qty')).toBe('4')
    await edit(1, 'qty', '0')
    expect(text(1, 'qty')).toBe('4')
    await edit(1, 'd1', '101')
    expect(screen.getByText('Satır 2 · İsk1 %: geçerli bir sayı değil')).toBeInTheDocument()
    await edit(1, 'd1', '')
    expect(text(1, 'd1')).toBe('5')
    await edit(1, 'qty', '5')
    expect(screen.queryByText(/geçerli bir sayı değil/)).toBeNull()
  })

  it('Ins opens the part search on a new line; a pick fills it and moves to the quantity', async () => {
    await open('/invoices/1001')
    await userEvent.click(cell(0, 'qty'))
    key(cell(0, 'qty'), 'Insert')
    const search = await screen.findByRole('combobox', { name: 'Parça ara' })
    await waitFor(() => expect(search).toHaveFocus())
    expect(rowCount()).toBe(5)
    await userEvent.type(search, 'HP-3780')
    await userEvent.click(await screen.findByRole('option', { name: /HP-3780/ }))
    await waitFor(() => expect(text(1, 'part')).toBe('HP-3780'))
    expect(text(1, 'price')).toBe('15.900,00')
    expect(text(1, 'd1')).toBe('10')
    expect(await screen.findByRole('textbox', { name: 'Miktar, satır 2' })).toBeInTheDocument()
  })

  it('Del removes the focused line; F2 opens the part search and Esc gives the part back', async () => {
    await open('/invoices/1001')
    await userEvent.click(cell(1, 'price'))
    key(cell(1, 'price'), 'Delete')
    await waitFor(() => expect(rowCount()).toBe(3))
    expect(text(1, 'part')).toBe('FL-0092')
    await userEvent.click(cell(0, 'qty'))
    key(cell(0, 'qty'), 'F2')
    const search = await screen.findByRole('combobox', { name: 'Parça ara' })
    await userEvent.type(search, 'zz{Escape}')
    await waitFor(() => expect(screen.queryByRole('combobox', { name: 'Parça ara' })).toBeNull())
    expect(text(0, 'part')).toBe('HP-3774')
  })

  it('the toolbar adds and deletes lines too, and the editor catches F2 outside the grid', async () => {
    await open('/invoices/1001')
    await userEvent.click(screen.getByRole('button', { name: /Satır ekle/ }))
    expect(await screen.findByRole('combobox', { name: 'Parça ara' })).toBeInTheDocument()
    await userEvent.keyboard('{Escape}')
    expect(rowCount()).toBe(5)
    await userEvent.click(cell(4, 'rn'))
    await userEvent.click(screen.getByRole('button', { name: 'Sil Del' }))
    await waitFor(() => expect(rowCount()).toBe(4))
    const date = screen.getByLabelText('Tarih')
    date.focus()
    key(date, 'F2')
    expect(await screen.findByRole('combobox', { name: 'Parça ara' })).toBeInTheDocument()
  })

  it('a posted document is read only', async () => {
    await open('/invoices/418')
    await userEvent.dblClick(cell(0, 'qty'))
    expect(within(cell(0, 'qty')).queryByRole('textbox')).toBeNull()
    expect(screen.getByRole('button', { name: /Satır ekle/ })).toBeDisabled()
  })

  it('price-difference lines take an amount and a description', async () => {
    await open('/invoices/new?type=PRICE_DIFFERENCE&original=108')
    await waitFor(() => expect(text(0, 'origin')).toContain('HP-3774'))
    expect(text(0, 'originNet')).toBe('35.340,00')
    await edit(0, 'amount', '500')
    expect(text(0, 'net')).toBe('500,00')
    await userEvent.click(screen.getByRole('button', { name: /Satır ekle/ }))
    const desc = await screen.findByRole('textbox', { name: 'Açıklama, satır 2' })
    await userEvent.type(desc, 'Navlun farkı{Enter}')
    await waitFor(() => expect(text(1, 'description')).toBe('Navlun farkı'))
    expect(text(1, 'origin')).toBe('—')
    await edit(1, 'amount', '100')
    expect(screen.getByRole('region', { name: 'Toplamlar' })).toHaveTextContent('Toplam720,00')
  })

  it('a return line cannot take more than is left', async () => {
    await open('/invoices/new?type=SALES_RETURN&original=391')
    await waitFor(() => expect(text(1, 'returnable')).toBe('3'))
    await edit(1, 'qty', '4')
    expect(screen.getByText('Satır 2: en çok 3 iade edilebilir')).toBeInTheDocument()
    expect(text(1, 'qty')).toBe('3')
    await edit(1, 'qty', '2')
    expect(text(1, 'qty')).toBe('2')
  })
})
