import React, { useEffect, useRef } from 'react';
import { Control, FieldPathByValue, FieldValues, useController } from 'react-hook-form';
import { DatePickerProps, DateTimePicker, LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { TextField } from '@mui/material';

const CDateTimePicker = <TFieldValues extends FieldValues, TPath extends FieldPathByValue<TFieldValues, any>>({
  name,
  control,
  autoFocus,
  label,
  size,
  fullwidth,
  ...props
}: {
  control: Control<TFieldValues>;
  name: TPath;
  label: string;
  size?: 'small' | 'medium';
  fullwidth?: boolean;
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
      <DateTimePicker
        inputRef={ref}
        inputFormat="LLLL"
        renderInput={params => (
          <TextField
            {...params}
            ref={componentRef}
            label={label}
            error={!!error}
            size={size}
            fullWidth={fullwidth}
            helperText={!!error && error.message}
          />
        )}
        value={field.value}
        onChange={value => field.onChange(value)}
        disableMaskedInput
        {...props}
      />
    </LocalizationProvider>
  );
};

export default CDateTimePicker;
