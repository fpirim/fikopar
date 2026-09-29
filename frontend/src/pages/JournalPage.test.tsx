import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderApp } from '../test/renderApp'
import { capture } from '../test/msw'

const location = () => screen.getByTestId('location').textContent

describe('JournalEntryPage', () => {
  it('shows the entry, its postings and the trail', async () => {
    renderApp('/journal/4833')
    expect(await screen.findByRole('heading', { name: 'J-2026-004833' })).toBeInTheDocument()
    expect(screen.getByText('KAYITLI')).toBeInTheDocument()
    expect(screen.getByText('Σ 0,00')).toBeInTheDocument()
    const details = screen.getByText('Kayıt', { selector: 'dt' }).parentElement!
    expect(details).toHaveTextContent('29.09.2026 09:02:17')
    expect(details).toHaveTextContent('doc:SF-2026-1-000418:post')
    expect(within(details).getByText('SF-2026-1-000418')).toBeInTheDocument()
    const postings = screen.getByRole('table', { name: 'Kayıt satırları' })
    expect(within(postings).getByRole('link', { name: '120.10482' })).toHaveAttribute(
      'href',
      '/statements/5000',
    )
    expect(within(postings).getByText('Domestic sales')).toBeInTheDocument()
    expect(within(postings).getAllByText('48.720,00')).toHaveLength(3)
    const trail = await screen.findByRole('list', { name: 'İz' })
    const items = within(trail).getAllByRole('listitem')
    expect(items).toHaveLength(4)
    expect(items[0]).toHaveTextContent('İşlendi · ledger')
    expect(items[0]).toHaveTextContent('09:02:17 · teslim 1')
    expect(items[3]).toHaveTextContent('kullanıcı 12 · W/"6"')
  })

  it('explains that a document entry is reversed through its source', async () => {
    const posts = capture('post', '/api/orgs/:orgId/ledger/entries/:entryId/reversal')
    renderApp('/journal/4833')
    await userEvent.click(await screen.findByRole('button', { name: 'Ters kayıt…' }))
    const dialog = await screen.findByRole('dialog', { name: 'J-2026-004833 ters kaydı' })
    expect(dialog).toHaveTextContent('Bu kayıt SF-2026-1-000418 tarafından oluşturuldu')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Ters kaydı at' }))
    expect(within(dialog).getByText('Gerekçe yazın (en az 3 karakter)')).toBeInTheDocument()
    expect(posts).toHaveLength(0)
    await userEvent.type(within(dialog).getByLabelText('Gerekçe'), 'Wrong customer')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Ters kaydı at' }))
    expect(await within(dialog).findByRole('alert')).toHaveTextContent(
      'Bu kayıt bir belgeye ait; belgeden iptal edin',
    )
    expect(posts[0].body).toEqual({ reason: 'Wrong customer' })
    expect(posts[0].headers.get('Idempotency-Key')).toBeTruthy()
  })

  it('reverses a manual entry and links both ways', async () => {
    renderApp('/journal/4829')
    await userEvent.click(await screen.findByRole('button', { name: 'Ters kayıt…' }))
    const dialog = await screen.findByRole('dialog')
    await userEvent.type(within(dialog).getByLabelText('Gerekçe'), 'Posted twice')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Ters kaydı at' }))
    await waitFor(() => expect(location()).toBe('/journal/4834'))
    expect(await screen.findByRole('heading', { name: 'J-2026-004834' })).toBeInTheDocument()
    expect(await screen.findByText('Ters kayıt J-2026-004834 atıldı')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('link', { name: 'J-2026-004829' }))
    expect(await screen.findByText('TERS KAYITLI')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'J-2026-004834' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Ters kayıt…' })).toBeDisabled()
  })
})

describe('JournalPage', () => {
  it('finds entries by document number', async () => {
    const calls = capture('get', '/api/orgs/:orgId/ledger/entries')
    renderApp('/journal?q=TAH-2026-1-000233')
    expect(await screen.findByRole('heading', { name: 'Yevmiye' })).toBeInTheDocument()
    expect(screen.getByRole('searchbox', { name: 'Kayıt veya belge no' })).toHaveValue(
      'TAH-2026-1-000233',
    )
    await userEvent.click(await screen.findByRole('link', { name: 'J-2026-004832' }))
    expect(location()).toBe('/journal/4832')
    expect(calls[0].url.searchParams.get('q')).toBe('TAH-2026-1-000233')
  })

  it('lists newest first and searches as you type', async () => {
    renderApp('/journal')
    expect(await screen.findByRole('link', { name: 'J-2026-004833' })).toBeInTheDocument()
    await userEvent.type(screen.getByRole('searchbox', { name: 'Kayıt veya belge no' }), 'VRM')
    await waitFor(() => expect(location()).toBe('/journal?q=VRM'))
    await waitFor(() =>
      expect(screen.queryByRole('link', { name: 'J-2026-004833' })).not.toBeInTheDocument(),
    )
    expect(screen.getByRole('link', { name: 'J-2026-004826' })).toBeInTheDocument()
  })
})
