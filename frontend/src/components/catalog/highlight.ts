export interface Segment {
  text: string
  hit: boolean
}

// One character in, one character out, so indices of the folded text match the original.
function fold(s: string): string {
  let out = ''
  for (const ch of s) out += (ch.toLocaleLowerCase('tr-TR')[0] ?? ch).replace('ı', 'i')
  return out
}

// Splits `text` into runs that match a word of `query` (case- and dotless-i-insensitive).
export function highlight(text: string, query: string): Segment[] {
  const chars = [...text]
  const folded = [...fold(text)]
  const hay = folded.join('')
  const hits = new Array<boolean>(chars.length).fill(false)
  for (const word of query.split(/\s+/).filter(Boolean)) {
    const needle = fold(word)
    let at = hay.indexOf(needle)
    while (at >= 0) {
      const start = [...hay.slice(0, at)].length
      for (let i = 0; i < [...needle].length; i++) hits[start + i] = true
      at = hay.indexOf(needle, at + 1)
    }
  }
  const out: Segment[] = []
  chars.forEach((ch, i) => {
    const last = out[out.length - 1]
    if (last && last.hit === hits[i]) last.text += ch
    else out.push({ text: ch, hit: hits[i] })
  })
  return out.length ? out : [{ text, hit: false }]
}
