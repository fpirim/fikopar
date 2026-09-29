import { useRef, useState } from 'react'

const MAX_SIZE = 2 * 1024 * 1024

export function useImagePicker(initialUrl: string) {
  const [pendingFile, setPendingFile] = useState<File | null>(null)
  const [pendingPreview, setPendingPreview] = useState<string | null>(null)
  const [pendingDelete, setPendingDelete] = useState(false)
  const fileRef = useRef<HTMLInputElement>(null)

  const currentSrc = pendingDelete ? null : pendingPreview || initialUrl || null

  const selectFile = (file: File): boolean => {
    if (file.size > MAX_SIZE) return false
    setPendingFile(file)
    setPendingPreview(URL.createObjectURL(file))
    setPendingDelete(false)
    return true
  }

  const clearImage = () => {
    if (pendingPreview) URL.revokeObjectURL(pendingPreview)
    setPendingFile(null)
    setPendingPreview(null)
    setPendingDelete(true)
  }

  return { pendingFile, pendingDelete, currentSrc, fileRef, selectFile, clearImage }
}
