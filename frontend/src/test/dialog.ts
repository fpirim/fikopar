import { screen, waitFor, type ByRoleOptions } from '@testing-library/react'
import { expect } from 'vitest'

// A Radix modal turns off pointer events on <body> in an effect and turns them back on for itself one render
// later. findByRole resolves as soon as the dialog is in the DOM, so a click right away can land in between and
// fail user-event's pointer-events check. Wait for the dialog to take pointer events.
export async function findDialog(
  role: 'dialog' | 'alertdialog',
  options?: ByRoleOptions,
): Promise<HTMLElement> {
  const dialog = await screen.findByRole(role, options)
  await waitFor(() => expect(dialog.style.pointerEvents).toBe('auto'))
  return dialog
}
