export function BrandMark({ size = 18, dark = true }: Readonly<{ size?: number; dark?: boolean }>) {
  return (
    <svg width={size} height={size} viewBox="0 0 22 22" aria-hidden="true">
      <rect x="0" y="0" width="5" height="22" fill={dark ? '#8FB7D6' : '#1F5F8B'} />
      <rect x="7" y="0" width="15" height="5" fill={dark ? '#FFFFFF' : '#16181D'} />
      <rect x="7" y="8.5" width="10" height="5" fill={dark ? '#FFFFFF' : '#16181D'} />
    </svg>
  )
}
