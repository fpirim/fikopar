import React, { useEffect, useRef } from 'react';
import { Control, FieldPathByValue, FieldValues, useController } from 'react-hook-form';
import { DatePicker, DatePickerProps, LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { TextField } from '@mui/material';
import { APP_LOCAL_DATE_FORMAT, EVRAK_DATE_FORMAT } from 'app/config/constants';

const CDatePicker = <TFieldValues extends FieldValues, TPath extends FieldPathByValue<TFieldValues, any>>({
  name,
  control,
  autoFocus,
  label,
  size,
  fullWidth,
  inputFormat = EVRAK_DATE_FORMAT,
  ...props
}: {
  control: Control<TFieldValues>;
  name: TPath;
  label: string;
  size?: 'small' | 'medium';
  inputFormat?: string;
  fullWidth?: boolean;
} & Omit<DatePickerProps<TFieldValues, any>, 'renderInput' | 'onChange' | 'value'>) => {
  const focussed = useRef(false);
  const componentRef = useRef<HTMLDivElement | HTMLInputElement>();
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
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <DatePicker<TFieldValues>
        inputRef={ref}
        inputFormat={APP_LOCAL_DATE_FORMAT}
        renderInput={params => (
          <TextField
            {...params}
            ref={componentRef}
            label={label}
            error={!!error}
            size={size}
            fullWidth={fullWidth}
            helperText={!!error && error.message}
          />
        )}
        value={field.value || null}
        onChange={value => {
          field.onChange(value && value.isValid() ? value.format(inputFormat) : null);
        }}
        {...props}
      />
    </LocalizationProvider>
  );
};

export default CDatePicker;
