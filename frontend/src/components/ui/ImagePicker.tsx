import type { ReactNode, RefObject } from 'react'
import { X } from 'lucide-react'
import { useTranslation } from 'react-i18next'

export default function ImagePicker({
  src,
  alt,
  rounded,
  placeholder,
  fileRef,
  onPick,
  onClear,
}: Readonly<{
  src: string | null
  alt: string
  rounded: 'full' | 'xl'
  placeholder: ReactNode
  fileRef: RefObject<HTMLInputElement | null>
  onPick: (file: File) => void
  onClear: (e: React.MouseEvent) => void
}>) {
  const { t } = useTranslation('common')
  const r = rounded === 'full' ? 'rounded-full' : 'rounded-xl'
  return (
    <div className="flex justify-center">
      <div className="relative">
        <button
          type="button"
          onClick={() => fileRef.current?.click()}
          className={`group block overflow-hidden border-2 border-dashed border-muted-foreground/30 transition-all hover:border-primary/50 ${r}`}
        >
          {src ? (
            <img src={src} alt={alt} className={`h-24 w-24 object-cover ${r}`} />
          ) : (
            <div className={`flex h-24 w-24 items-center justify-center bg-muted ${r}`}>
              {placeholder}
            </div>
          )}
          <div
            className={`absolute inset-0 flex items-center justify-center bg-black/0 transition-all group-hover:bg-black/40 ${r}`}
          >
            <span className="text-xs font-medium text-white opacity-0 transition-all group-hover:opacity-100">
              {t('change')}
            </span>
          </div>
        </button>
        {src && (
          <button
            type="button"
            onClick={onClear}
            className="absolute -right-2 -top-2 flex h-6 w-6 items-center justify-center rounded-full bg-destructive text-destructive-foreground shadow transition-all hover:scale-110"
          >
            <X className="h-3 w-3" />
          </button>
        )}
        <input
          ref={fileRef}
          type="file"
          accept="image/*"
          onChange={(e) => {
            const file = e.target.files?.[0]
            if (file) onPick(file)
          }}
          className="hidden"
        />
      </div>
    </div>
  )
}
