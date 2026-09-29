import { useState } from 'react'
import NumericInput from '../components/ui/NumericInput'

// E2E fixture page for NumericInput scenarios (see e2e/numeric-input.spec.ts).
export default function NumericInputPlayground() {
  const [iskonto, setIskonto] = useState('40')
  const [gun, setGun] = useState('0')
  const [genel, setGenel] = useState('0')
  const [ucretsiz, setUcretsiz] = useState('5.2350')
  const [bos, setBos] = useState('')
  const [buyuk, setBuyuk] = useState('1234567.89')
  const [tekBir, setTekBir] = useState('1')
  const [limitTest, setLimitTest] = useState('5')
  const [bosKalsin, setBosKalsin] = useState('')
  const [sifirInitial, setSifirInitial] = useState('0')

  return (
    <div className="mx-auto max-w-md space-y-6 p-8">
      <h1 className="text-xl font-bold">NumericInput Playground</h1>

      <section>
        <span className="block text-sm font-medium">Discount (4 decimals, %)</span>
        <div data-testid="iskonto-wrap">
          <NumericInput
            value={iskonto}
            onChange={setIskonto}
            decimalPlaces={4}
            minimumValue="0"
            maximumValue="100"
            currencySymbol="%"
          />
        </div>
        <p data-testid="iskonto-state" className="mt-1 text-xs text-muted-foreground">
          state: {iskonto}
        </p>
      </section>

      <section>
        <span className="block text-sm font-medium">Days (integer, 0-9999)</span>
        <div data-testid="gun-wrap">
          <NumericInput
            value={gun}
            onChange={setGun}
            decimalPlaces={0}
            minimumValue="0"
            maximumValue="9999"
            currencySymbol=""
          />
        </div>
        <p data-testid="gun-state" className="mt-1 text-xs text-muted-foreground">
          state: {gun}
        </p>
      </section>

      <section>
        <span className="block text-sm font-medium">General (2 decimals, no symbol)</span>
        <div data-testid="genel-wrap">
          <NumericInput value={genel} onChange={setGenel} decimalPlaces={2} />
        </div>
        <p data-testid="genel-state" className="mt-1 text-xs text-muted-foreground">
          state: {genel}
        </p>
      </section>

      <section>
        <span className="block text-sm font-medium">Pre-loaded (5,2350)</span>
        <div data-testid="ucretsiz-wrap">
          <NumericInput value={ucretsiz} onChange={setUcretsiz} decimalPlaces={4} />
        </div>
        <p data-testid="ucretsiz-state" className="mt-1 text-xs text-muted-foreground">
          state: {ucretsiz}
        </p>
      </section>

      <section>
        <span className="block text-sm font-medium">Empty initial</span>
        <div data-testid="bos-wrap">
          <NumericInput value={bos} onChange={setBos} decimalPlaces={2} />
        </div>
        <p data-testid="bos-state" className="mt-1 text-xs text-muted-foreground">
          state: &quot;{bos}&quot;
        </p>
      </section>

      <section>
        <span className="block text-sm font-medium">Large number (1234567,89)</span>
        <div data-testid="buyuk-wrap">
          <NumericInput value={buyuk} onChange={setBuyuk} decimalPlaces={2} />
        </div>
      </section>

      <section>
        <span className="block text-sm font-medium">Disabled (5)</span>
        <div data-testid="disabled-wrap">
          <NumericInput value="5" onChange={() => {}} decimalPlaces={2} disabled />
        </div>
      </section>

      <section>
        <span className="block text-sm font-medium">Tek (initial 1, dp=4, %)</span>
        <div data-testid="tekbir-wrap">
          <NumericInput value={tekBir} onChange={setTekBir} decimalPlaces={4} currencySymbol="%" />
        </div>
      </section>

      <section>
        <span className="block text-sm font-medium">
          padZeroOnEmpty=false (empty initial, stays empty on blur)
        </span>
        <div data-testid="boskalsin-wrap">
          <NumericInput
            value={bosKalsin}
            onChange={setBosKalsin}
            decimalPlaces={2}
            padZeroOnEmpty={false}
          />
        </div>
        <p data-testid="boskalsin-state" className="mt-1 text-xs text-muted-foreground">
          state: &quot;{bosKalsin}&quot;
        </p>
      </section>

      <section>
        <span className="block text-sm font-medium">
          padZeroOnEmpty=false + initial &quot;0&quot; (should look empty)
        </span>
        <div data-testid="sifirinit-wrap">
          <NumericInput
            value={sifirInitial}
            onChange={setSifirInitial}
            decimalPlaces={4}
            currencySymbol="%"
            padZeroOnEmpty={false}
          />
        </div>
        <p data-testid="sifirinit-state" className="mt-1 text-xs text-muted-foreground">
          state: &quot;{sifirInitial}&quot;
        </p>
      </section>

      <section>
        <span className="block text-sm font-medium">SicilDialog discount replica</span>
        <div data-testid="sicil-iskonto-wrap">
          <NumericInput
            value={sifirInitial}
            onChange={setSifirInitial}
            currencySymbol="%"
            decimalPlaces={4}
            minimumValue="0"
            maximumValue="100"
            padZeroOnEmpty={false}
          />
        </div>
      </section>

      <section>
        <span className="block text-sm font-medium">Leading-zero testi (initial 0, dp=4)</span>
        <div data-testid="sifir-wrap">
          <NumericInput value="0" onChange={() => {}} decimalPlaces={4} />
        </div>
      </section>

      <section>
        <span className="block text-sm font-medium">Limit testi (initial 5, dp=0, max 9999)</span>
        <div data-testid="limit-wrap">
          <NumericInput
            value={limitTest}
            onChange={setLimitTest}
            decimalPlaces={0}
            minimumValue="0"
            maximumValue="9999"
            currencySymbol=""
          />
        </div>
      </section>
    </div>
  )
}
