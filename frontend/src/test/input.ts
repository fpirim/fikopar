import userEvent from '@testing-library/user-event'

// One change event for the whole text: a grid filter applies once instead of per keystroke.
export async function fill(input: HTMLElement, text: string) {
  await userEvent.click(input)
  await userEvent.paste(text)
}
