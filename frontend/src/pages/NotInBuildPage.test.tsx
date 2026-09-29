import { describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import { renderApp } from '../test/renderApp'

describe('NotInBuildPage', () => {
  it('names the screen and says it is not in this build', async () => {
    renderApp('/cheques')
    expect(await screen.findByRole('heading', { name: 'Çek ve senetler' })).toBeInTheDocument()
    expect(screen.getByText('Bu sürümde henüz yok.')).toBeInTheDocument()
  })
})
