import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useImagePicker } from '../../hooks/useImagePicker'
import ImagePicker from './ImagePicker'

beforeEach(() => {
  // jsdom does not implement object URLs.
  URL.createObjectURL = vi.fn(() => 'blob:preview')
  URL.revokeObjectURL = vi.fn()
})

describe('useImagePicker', () => {
  it('accepts a file within the size limit and then clears it', () => {
    const { result } = renderHook(() => useImagePicker('/init.png'))
    expect(result.current.currentSrc).toBe('/init.png')

    const small = new File(['x'], 'a.png', { type: 'image/png' })
    act(() => {
      expect(result.current.selectFile(small)).toBe(true)
    })
    expect(result.current.pendingFile).toBe(small)
    expect(result.current.currentSrc).toBe('blob:preview')

    act(() => result.current.clearImage())
    expect(result.current.pendingDelete).toBe(true)
    expect(result.current.currentSrc).toBeNull()
  })

  it('rejects an oversized file', () => {
    const { result } = renderHook(() => useImagePicker(''))
    const big = new File([new Uint8Array(3 * 1024 * 1024)], 'big.png', { type: 'image/png' })
    act(() => {
      expect(result.current.selectFile(big)).toBe(false)
    })
    expect(result.current.pendingFile).toBeNull()
  })
})

describe('ImagePicker', () => {
  it('shows the image and supports clear + pick', async () => {
    const u = userEvent.setup()
    const onPick = vi.fn()
    const onClear = vi.fn()
    const ref = { current: null }
    const { container } = render(
      <ImagePicker
        src="/a.png"
        alt="avatar"
        rounded="full"
        placeholder={<span>ph</span>}
        fileRef={ref}
        onPick={onPick}
        onClear={onClear}
      />,
    )
    expect(screen.getByAltText('avatar')).toBeInTheDocument()

    const buttons = container.querySelectorAll('button')
    await u.click(buttons[1]) // the X clear button
    expect(onClear).toHaveBeenCalled()

    const input = container.querySelector('input[type="file"]') as HTMLInputElement
    await u.upload(input, new File(['x'], 'a.png', { type: 'image/png' }))
    expect(onPick).toHaveBeenCalled()
  })

  it('shows the placeholder when there is no image', () => {
    render(
      <ImagePicker
        src={null}
        alt="logo"
        rounded="xl"
        placeholder={<span>ph</span>}
        fileRef={{ current: null }}
        onPick={vi.fn()}
        onClear={vi.fn()}
      />,
    )
    expect(screen.getByText('ph')).toBeInTheDocument()
    expect(screen.queryByAltText('logo')).not.toBeInTheDocument()
  })
})
