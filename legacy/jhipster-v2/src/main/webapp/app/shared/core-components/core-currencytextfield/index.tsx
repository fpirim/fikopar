import React, { useEffect, useRef } from 'react';
import { Control, FieldPathByValue, FieldValues, useController } from 'react-hook-form';
import CurrencyTextField, { CurrencyTextFieldProps } from 'app/shared/core-components/core-currencytextfield/currency-text-field';

const CCurrencyTextField = <TFieldValues extends FieldValues, TPath extends FieldPathByValue<TFieldValues, any>>({
  name,
  control,
  autoFocus,
  ...props
}: {
  control: Control<TFieldValues>;
  name: TPath;
} & CurrencyTextFieldProps) => {
  const focussed = useRef(false);
  const componentRef = useRef<HTMLDivElement>();
  const {
    field: { ref, ...field },
    fieldState: { error },
  } = useController({
    control,
    name,
  });

  useEffect(() => {
    if (componentRef.current && autoFocus && !props.disabled && !focussed.current) {
      const el = componentRef.current.getElementsByTagName('input')[0];
      el.focus();
      setTimeout(() => {
        el.select();
      }, 100);
      focussed.current = true;
    }
  }, [autoFocus, props.disabled]);

  return (
    <CurrencyTextField
      error={!!error}
      helperText={!!error && error.message}
      inputRef={ref}
      ref={componentRef}
      onChange={(_, value) => field.onChange(value)}
      value={field.value || ''}
      currencySymbol="₺"
      decimalCharacter=","
      digitGroupSeparator="."
      outputFormat="number"
      leadingZero="keep"
      decimalPlaces={2}
      {...props}
    />
  );
};

export default CCurrencyTextField;
