import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderApp } from '../test/renderApp'
import { capture, captureDownloads } from '../test/msw'
import { todayIso } from '../lib/format'

const WEEK = '/cash-book?from=2026-09-22&to=2026-09-29&depot=1'
const OPEN = `/cash-book?from=2026-09-22&to=${todayIso()}&depot=1`
const stat = (label: string) => screen.getByRole('group', { name: label })

describe('CashBookPage', () => {
  it('shows opening, in, out and closing with the running balance', async () => {
    renderApp(WEEK)
    expect(await screen.findByRole('heading', { name: 'Kasa defteri' })).toBeInTheDocument()
    expect(screen.getByText('Depo 1 · 22.09 – 29.09.2026')).toBeInTheDocument()
    await waitFor(() => expect(stat('Devir')).toHaveTextContent('38.215,50'))
    expect(stat('Giriş')).toHaveTextContent('+52.400,00')
    expect(stat('Çıkış')).toHaveTextContent('−43.800,00')
    expect(stat('Kapanış')).toHaveTextContent('46.815,50')
    const grid = document.querySelector('.fk-grid') as HTMLElement
    await waitFor(() => expect(within(grid).getByText('GDR-2026-1-000038')).toBeInTheDocument())
    expect(within(grid).getByText('Fuel · delivery van')).toBeInTheDocument()
    const bottom = grid.querySelector('.ag-floating-bottom') as HTMLElement
    expect(bottom).toHaveTextContent('Toplam')
    expect(bottom).toHaveTextContent('52.400,00')
    expect(bottom).toHaveTextContent('43.800,00')
    const top = grid.querySelector('.ag-floating-top') as HTMLElement
    expect(top).toHaveTextContent('Devir')
    expect(top).toHaveTextContent('38.215,50')
  })

  it('restarts the running balance at the carried opening of a new year', async () => {
    renderApp('/cash-book?from=2025-12-01&to=2026-01-31&depot=1')
    await waitFor(() => expect(stat('Kapanış')).toHaveTextContent('21.480,00'))
    expect(stat('Devir')).toHaveTextContent('0,00')
    expect(stat('Giriş')).toHaveTextContent('+4.000,00')
    const grid = document.querySelector('.fk-grid') as HTMLElement
    const chip = await within(grid).findByText('DEVİR')
    const row = chip.closest('.ag-row') as HTMLElement
    expect(row).toHaveClass('fk-row-reset')
    expect(row).toHaveTextContent('21.480,00')
    const bottom = grid.querySelector('.ag-floating-bottom') as HTMLElement
    expect(bottom).toHaveTextContent('4.000,00')
    expect(bottom).not.toHaveTextContent('25.480,00')
  })

  it('uses the top-bar depot', async () => {
    const calls = capture('get', '/api/orgs/:orgId/cash-book')
    renderApp(WEEK.replace('depot=1', 'depot=2'))
    expect(await screen.findByText('Depo 2 · 22.09 – 29.09.2026')).toBeInTheDocument()
    await waitFor(() => expect(calls[0].url.searchParams.get('depotId')).toBe('2'))
  })

  it('asks for a depot instead of guessing one', async () => {
    const calls = capture('get', '/api/orgs/:orgId/cash-book')
    renderApp('/cash-book?from=2026-09-22&to=2026-09-29')
    const prompt = await screen.findByRole('region', { name: 'Depo seçin' })
    expect(prompt).toHaveTextContent('Her deponun kendi kasası var')
    expect(screen.queryByRole('button', { name: 'Gider' })).not.toBeInTheDocument()
    expect(calls).toHaveLength(0)
    await userEvent.click(await within(prompt).findByRole('button', { name: 'Depo 2' }))
    expect(await screen.findByText('Depo 2 · 22.09 – 29.09.2026')).toBeInTheDocument()
    expect(screen.getByTestId('location')).toHaveTextContent('depot=2')
    await waitFor(() => expect(calls[0].url.searchParams.get('depotId')).toBe('2'))
  })

  it('explains a closed year on a voucher', async () => {
    renderApp(OPEN)
    await userEvent.click(await screen.findByRole('button', { name: 'Gider' }))
    const dialog = await screen.findByRole('dialog', { name: 'Kasadan gider' })
    const date = within(dialog).getByLabelText('Tarih')
    await userEvent.clear(date)
    await userEvent.type(date, '15.06.2024')
    await userEvent.type(within(dialog).getByLabelText('Tutar'), '250')
    await userEvent.type(within(dialog).getByLabelText('Açıklama'), 'Late receipt')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Kaydet' }))
    expect(await within(dialog).findByRole('alert')).toHaveTextContent(
      '2024 yılı kapalı; önce 2025 devrini yeniden açın',
    )
  })

  it('posts an expense voucher from the till', async () => {
    const posts = capture('post', '/api/orgs/:orgId/cash-vouchers')
    renderApp(OPEN)
    await waitFor(() => expect(stat('Çıkış')).toHaveTextContent('−43.800,00'))
    await userEvent.click(screen.getByRole('button', { name: 'Gider' }))
    const dialog = await screen.findByRole('dialog', { name: 'Kasadan gider' })
    expect(await within(dialog).findByText('Kayıtta numara: GDR-2026-1-000039')).toBeInTheDocument()
    const account = within(dialog).getByLabelText('Gider hesabı')
    await waitFor(() => expect(account).toHaveDisplayValue('770.01 · Fuel'))
    await userEvent.selectOptions(account, '7')
    await userEvent.type(within(dialog).getByLabelText('Tutar'), '250')
    await userEvent.type(within(dialog).getByLabelText('Açıklama'), 'Freight to Konya')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Kaydet' }))
    await waitFor(() => expect(posts).toHaveLength(1))
    expect(posts[0].body).toEqual({
      kind: 'EXPENSE',
      depotId: 1,
      date: todayIso(),
      accountId: 7,
      amount: '250.00',
      description: 'Freight to Konya',
    })
    expect(posts[0].headers.get('Idempotency-Key')).toBeTruthy()
    expect(await screen.findByText('GDR-2026-1-000039 kaydedildi')).toBeInTheDocument()
    await waitFor(() => expect(stat('Çıkış')).toHaveTextContent('−44.050,00'))
  })

  it('moves cash from the bank to the till', async () => {
    const posts = capture('post', '/api/orgs/:orgId/cash-vouchers')
    renderApp(OPEN)
    await userEvent.click(await screen.findByRole('button', { name: 'Bankaya yatır' }))
    const dialog = await screen.findByRole('dialog', { name: 'Kasa ile banka arası' })
    await userEvent.click(within(dialog).getByRole('button', { name: 'Banka → kasa' }))
    const bank = within(dialog).getByLabelText('Banka hesabı')
    await waitFor(() => expect(bank).toHaveDisplayValue('102.00002 · [BANK A]'))
    await userEvent.type(within(dialog).getByLabelText('Tutar'), '5000')
    await userEvent.type(within(dialog).getByLabelText('Açıklama'), 'Change for the till')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Kaydet' }))
    await waitFor(() => expect(posts).toHaveLength(1))
    expect(posts[0].body).toMatchObject({ kind: 'BANK_WITHDRAWAL', amount: '5000.00' })
    await waitFor(() => expect(stat('Giriş')).toHaveTextContent('+57.400,00'))
  })

  it('records a till count and posts the difference', async () => {
    const posts = capture('post', '/api/orgs/:orgId/cash-counts')
    renderApp(WEEK)
    await userEvent.click(await screen.findByRole('button', { name: 'Kasa sayımı' }))
    const dialog = await screen.findByRole('dialog', { name: 'Kasa sayımı · Depo 1' })
    await waitFor(() => expect(within(dialog).getByText('46.815,50')).toBeInTheDocument())
    await userEvent.type(within(dialog).getByLabelText('Sayılan'), '46800')
    expect(within(dialog).getByText('−15,50')).toBeInTheDocument()
    await userEvent.click(within(dialog).getByRole('checkbox'))
    await userEvent.type(within(dialog).getByLabelText('Not'), 'Evening count')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Sayımı kaydet' }))
    await waitFor(() => expect(posts).toHaveLength(1))
    expect(posts[0].body).toEqual({
      depotId: 1,
      countedAmount: '46800.00',
      postDifference: true,
      note: 'Evening count',
    })
    expect(await screen.findByText('Sayım kaydedildi, fark −15,50')).toBeInTheDocument()
  })

  it('changes the period and prints the book', async () => {
    const calls = capture('get', '/api/orgs/:orgId/cash-book')
    const files = captureDownloads()
    renderApp(WEEK)
    await userEvent.click(await screen.findByRole('button', { name: 'Dönemi değiştir' }))
    const from = await screen.findByLabelText('Başlangıç')
    await userEvent.clear(from)
    await userEvent.type(from, '01.09.2026')
    await userEvent.click(screen.getByRole('button', { name: 'Uygula' }))
    expect(await screen.findByText('Depo 1 · 01.09 – 29.09.2026')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'PDF' }))
    await waitFor(() => expect(files).toHaveLength(1))
    const pdf = calls.find((c) => c.headers.get('Accept') === 'application/pdf')!
    expect(pdf.url.searchParams.get('from')).toBe('2026-09-01')
  })
})
