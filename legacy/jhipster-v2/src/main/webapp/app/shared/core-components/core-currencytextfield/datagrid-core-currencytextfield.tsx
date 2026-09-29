import CurrencyTextField, { CurrencyTextFieldProps } from 'app/shared/core-components/core-currencytextfield/currency-text-field';
import React from 'react';
import { GridRenderEditCellParams, useGridApiContext } from '@mui/x-data-grid-pro';

const DataGridCurrencyTextField = ({
  params,
  currencyTextFieldProps,
}: {
  params: GridRenderEditCellParams;
  currencyTextFieldProps?: CurrencyTextFieldProps;
}) => {
  const { id, value, field, hasFocus } = params;
  const apiRef = useGridApiContext();
  // const componentRef = useRef<HTMLDivElement>();

  // useLayoutEffect(() => {
  //   if (hasFocus) {
  //     componentRef.current.focus();
  //   }
  // }, [hasFocus]);

  return (
    <CurrencyTextField
      currencySymbol="₺"
      decimalCharacter=","
      digitGroupSeparator="."
      outputFormat="number"
      leadingZero="keep"
      decimalPlaces={2}
      // ref={componentRef}
      value={value}
      onChange={(_, val) => {
        apiRef.current.setEditCellValue({ id, field, value: val });
      }}
      autoFocus={hasFocus}
      {...currencyTextFieldProps}
    />
  );
};

export default DataGridCurrencyTextField;
