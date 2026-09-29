const currency_symbol = '₺';

const tlFormatWithTwoDecimal = new Intl.NumberFormat('tr-TR', {
  style: 'currency',
  currency: 'TRY',
  minimumFractionDigits: 2,
});

const tlFormatWithFourDecimal = new Intl.NumberFormat('tr-TR', {
  style: 'currency',
  currency: 'TRY',
  minimumFractionDigits: 2,
});

export const tlCurrencyOutput = {
  format: (value: number | bigint, showZero?: boolean) => {
    if (!showZero && value === 0) {
      return '';
    }
    return tlFormatWithTwoDecimal.format(value);
  },
};

export const tlCurrencyOutputWith4Decimal = {
  format: (value: number | bigint, showZero?: boolean) => {
    if (!showZero && value === 0) {
      return '';
    }
    return tlFormatWithFourDecimal.format(value);
  },
};

export const decimal4Output = new Intl.NumberFormat('tr-TR', {
  minimumFractionDigits: 4,
});

export const intOutput = new Intl.NumberFormat('tr-TR');

export const percentageOutput = new Intl.NumberFormat('tr-TR', {
  style: 'percent',
  minimumFractionDigits: 4,
});
