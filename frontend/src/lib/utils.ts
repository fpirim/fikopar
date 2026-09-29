import { clsx, type ClassValue } from 'clsx'
import { extendTailwindMerge } from 'tailwind-merge'

// Custom sizes from tailwind.config.js, so text-13 is not mistaken for a colour.
const twMerge = extendTailwindMerge({
  extend: { classGroups: { 'font-size': [{ text: ['2xs', '13', 'grid', '15', '22'] }] } },
})

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
