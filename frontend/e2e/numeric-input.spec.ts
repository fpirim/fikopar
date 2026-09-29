import { test, expect } from './fixtures'

// NumericInput is tested only here: jsdom cannot emulate AutoNumeric's async focus/format flow.

const URL = '/playground/numeric-input'

test.describe('NumericInput — real browser behaviour', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(URL)
    await expect(page.locator('h1', { hasText: 'NumericInput Playground' })).toBeVisible()
  })

  // ---------- Initial display ----------

  test('initial 40 + decimalPlaces=4 + currencySymbol="%": "%40,0000"', async ({ page }) => {
    const input = page.getByTestId('iskonto-wrap').locator('input')
    await expect(input).toHaveValue('%40,0000')
  })

  test('initial 0 + decimalPlaces=0: "0" (padding YOK)', async ({ page }) => {
    const input = page.getByTestId('gun-wrap').locator('input')
    await expect(input).toHaveValue('0')
  })

  test('initial 1 + decimalPlaces=4: "%1,0000" (trailing-zero padding aktif)', async ({ page }) => {
    const input = page.getByTestId('tekbir-wrap').locator('input')
    await expect(input).toHaveValue('%1,0000')
  })

  test('empty initial shows "0,00" (emptyInputBehavior=zero)', async ({ page }) => {
    const input = page.getByTestId('bos-wrap').locator('input')
    await expect(input).toHaveValue('0,00')
  })

  test('large number: "." grouping + "," decimal (Turkish format)', async ({ page }) => {
    const input = page.getByTestId('buyuk-wrap').locator('input')
    await expect(input).toHaveValue('1.234.567,89')
  })

  test('disabled is reflected on the input', async ({ page }) => {
    const input = page.getByTestId('disabled-wrap').locator('input')
    await expect(input).toBeDisabled()
  })

  test('empty currencySymbol renders clean (5,2350 → 5,2350)', async ({ page }) => {
    const input = page.getByTestId('ucretsiz-wrap').locator('input')
    await expect(input).toHaveValue('5,2350')
  })

  test('initial below maximumValue is kept (5, dp=0, max=9999 → "5")', async ({ page }) => {
    const input = page.getByTestId('limit-wrap').locator('input')
    await expect(input).toHaveValue('5')
  })

  // ---------- onChange ----------

  test('onChange updates state with a numeric string', async ({ page }) => {
    const input = page.getByTestId('iskonto-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.press('7')
    await expect(input).toHaveValue('%7,0000')
    await expect(page.getByTestId('iskonto-state')).toHaveText('state: 7')
  })

  // ---------- all selected + single digit ----------

  test('discount all selected + "3" → "%3,0000" immediately (no flash)', async ({ page }) => {
    const input = page.getByTestId('iskonto-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.press('3')
    await expect(input).toHaveValue('%3,0000')
  })

  test('days all selected + "30" → "30" (no padding)', async ({ page }) => {
    const input = page.getByTestId('gun-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.type('30')
    await expect(input).toHaveValue('30')
  })

  test('days 90/120 (payment term) stay below the maximum', async ({ page }) => {
    const input = page.getByTestId('gun-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.type('120')
    await expect(input).toHaveValue('120')
  })

  // ---------- Cursor positioning ----------

  test('discount: caret stays left of the separator after one digit', async ({ page }) => {
    const input = page.getByTestId('iskonto-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.press('5')
    const sel = await input.evaluate((el: HTMLInputElement) => el.selectionStart)
    const dec = await input.evaluate((el: HTMLInputElement) => el.value.indexOf(','))
    expect(sel).toBe(dec)
  })

  test('discount: consecutive digits left of the separator fill the integer part', async ({
    page,
  }) => {
    const input = page.getByTestId('iskonto-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.press('5')
    await page.keyboard.press('1')
    await expect(input).toHaveValue('%51,0000')
  })

  test('discount: "," moves the caret right of the separator, format kept', async ({ page }) => {
    const input = page.getByTestId('iskonto-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.press('5')
    await page.keyboard.press(',')
    await expect(input).toHaveValue('%5,0000')
    const sel = await input.evaluate((el: HTMLInputElement) => el.selectionStart)
    expect(sel).toBe('%5,0000'.indexOf(',') + 1)
  })

  test('discount: digits after the separator fill the decimal part', async ({ page }) => {
    const input = page.getByTestId('iskonto-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.press('5')
    await page.keyboard.press(',')
    await page.keyboard.press('1')
    await expect(input).toHaveValue('%5,1000')
  })

  // ---------- Backspace / Delete ----------

  test('iskonto Backspace integer: digit silinir (replace YOK)', async ({ page }) => {
    const input = page.getByTestId('iskonto-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(3, 3))
    await page.keyboard.press('Backspace')
    await expect(input).toHaveValue('%4,0000')
  })

  test('discount: Backspace on the last integer digit normalises to "0"', async ({ page }) => {
    const input = page.getByTestId('tekbir-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(2, 2))
    await page.keyboard.press('Backspace')
    await expect(input).toHaveValue('%0,0000')
  })

  test('decimal Backspace replaces the digit with "0", padding kept', async ({ page }) => {
    const input = page.getByTestId('ucretsiz-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(5, 5))
    await page.keyboard.press('Backspace')
    await expect(input).toHaveValue('5,2300')
  })

  test('all selected + Backspace: %0,0000, caret left of separator (not select-all)', async ({
    page,
  }) => {
    const input = page.getByTestId('iskonto-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.press('Backspace')
    await expect(input).toHaveValue('%0,0000')
    const sel = await input.evaluate((el: HTMLInputElement) => ({
      start: el.selectionStart,
      end: el.selectionEnd,
    }))
    const dec = '%0,0000'.indexOf(',')
    expect(sel).toEqual({ start: dec, end: dec })
  })

  test('all selected + Delete: %0,0000, caret left of separator', async ({ page }) => {
    const input = page.getByTestId('iskonto-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.press('Delete')
    await expect(input).toHaveValue('%0,0000')
    const sel = await input.evaluate((el: HTMLInputElement) => ({
      start: el.selectionStart,
      end: el.selectionEnd,
    }))
    const dec = '%0,0000'.indexOf(',')
    expect(sel).toEqual({ start: dec, end: dec })
  })

  test('focus selectAll + Backspace: forced padding gives %0,0000 even if AutoNumeric drops to "%"', async ({
    page,
  }) => {
    const input = page.getByTestId('iskonto-wrap').locator('input')
    await input.click()
    await page.keyboard.press('Backspace')
    await expect(input).toHaveValue('%0,0000')
  })

  test('clear all + blur normalises to "%0,0000"', async ({ page }) => {
    const input = page.getByTestId('iskonto-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.press('Backspace')
    await page.locator('h1').click()
    await expect(input).toHaveValue('%0,0000')
  })

  test('padZeroOnEmpty=false: empty initial stays empty while blurred', async ({ page }) => {
    const input = page.getByTestId('boskalsin-wrap').locator('input')
    await expect(input).toHaveValue('')
    await expect(page.getByTestId('boskalsin-state')).toHaveText('state: ""')
  })

  test('padZeroOnEmpty=false: shows padded "0,00" while focused', async ({ page }) => {
    const input = page.getByTestId('boskalsin-wrap').locator('input')
    await input.focus()
    await expect(input).toHaveValue('0,00')
  })

  test('padZeroOnEmpty=false: blur with value 0 leaves the display empty', async ({ page }) => {
    const input = page.getByTestId('boskalsin-wrap').locator('input')
    await input.click()
    await page.keyboard.type('12')
    await expect(input).toHaveValue('12,00')
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.press('Backspace')
    await expect(input).toHaveValue('0,00')
    await page.locator('h1').click()
    await expect(input).toHaveValue('')
    await expect(page.getByTestId('boskalsin-state')).toHaveText('state: ""')
  })

  test('padZeroOnEmpty=false + initial "0": display looks empty', async ({ page }) => {
    const input = page.getByTestId('sifirinit-wrap').locator('input')
    await expect(input).toHaveValue('')
  })

  test('padZeroOnEmpty=false: no lone "%" after clear + blur', async ({ page }) => {
    const input = page.getByTestId('sifirinit-wrap').locator('input')
    await input.click()
    await expect(input).toHaveValue('%0,0000')
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.press('Backspace')
    await expect(input).toHaveValue('%0,0000')
    await page.locator('h1').click()
    await expect(input).toHaveValue('')
  })

  test('SicilDialog discount: initial 0 + min/max + symbol → empty after blur', async ({
    page,
  }) => {
    const input = page.getByTestId('sicil-iskonto-wrap').locator('input')
    await expect(input).toHaveValue('')
    await input.click()
    await expect(input).toHaveValue('%0,0000')
    await page.locator('h1').click()
    await expect(input).toHaveValue('')
  })

  test('SicilDialog discount: %20,0000 → selectAll Backspace → Tab → empty (no "%")', async ({
    page,
  }) => {
    const input = page.getByTestId('sicil-iskonto-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.type('20')
    await expect(input).toHaveValue('%20,0000')
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.press('Backspace')
    await expect(input).toHaveValue('%0,0000')
    await page.keyboard.press('Tab')
    await expect(input).toHaveValue('')
  })

  test('repro: %0,0000 → "1" "2" blur → focus → Backspace → blur stays empty', async ({ page }) => {
    const input = page.getByTestId('sicil-iskonto-wrap').locator('input')
    await input.click()
    await expect(input).toHaveValue('%0,0000')
    await page.keyboard.press('1')
    await page.keyboard.press('2')
    await expect(input).toHaveValue('%12,0000')
    await page.locator('h1').click()
    await input.click()
    await page.keyboard.press('Backspace')
    await expect(input).toHaveValue('%0,0000')
    await page.locator('h1').click()
    await expect(input).toHaveValue('')
  })

  test('Backspace on integer "0" is a no-op ("%0,0000" unchanged)', async ({ page }) => {
    const input = page.getByTestId('sicil-iskonto-wrap').locator('input')
    await input.click()
    await expect(input).toHaveValue('%0,0000')
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(2, 2))
    await page.keyboard.press('Backspace')
    await expect(input).toHaveValue('%0,0000')
  })

  test('SicilDialog discount: type, clear, blur → no "%" residue', async ({ page }) => {
    const input = page.getByTestId('sicil-iskonto-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.press('5')
    await expect(input).toHaveValue('%5,0000')
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, el.value.length))
    await page.keyboard.press('Backspace')
    await page.locator('h1').click()
    await expect(input).toHaveValue('')
  })

  test('leading zero: "5" left of the separator turns "0,0000" into "5,0000"', async ({ page }) => {
    const input = page.getByTestId('sifir-wrap').locator('input')
    await expect(input).toHaveValue('0,0000')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(1, 1))
    await page.keyboard.press('5')
    await expect(input).toHaveValue('5,0000')
  })

  test('Delete keeps the caret and zeroes the digit to its right', async ({ page }) => {
    const input = page.getByTestId('ucretsiz-wrap').locator('input')
    await input.click()
    await input.evaluate((el: HTMLInputElement) => el.setSelectionRange(0, 0))
    await page.keyboard.press('Delete')
    await expect(input).toHaveValue('0,2350')
    const sel = await input.evaluate((el: HTMLInputElement) => el.selectionStart)
    expect(sel).toBe(0)
  })
})
