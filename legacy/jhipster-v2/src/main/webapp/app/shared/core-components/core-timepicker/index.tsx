import React, { useEffect, useRef } from 'react';
import { Control, FieldPathByValue, FieldValues, useController } from 'react-hook-form';
import { LocalizationProvider, TimePicker, TimePickerProps } from '@mui/x-date-pickers';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { TextField } from '@mui/material';
import dayjs, { Dayjs } from 'dayjs';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';
import { HOUR_FORMAT } from 'app/config/constants';
import { useAppSelector } from 'app/config/store';

dayjs.extend(utc);
dayjs.extend(timezone);

const CTimePicker = <TFieldValues extends FieldValues, TPath extends FieldPathByValue<TFieldValues, any>>({
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
  autoFocus?: boolean;
} & Omit<TimePickerProps<Dayjs, Dayjs>, 'renderInput' | 'onChange' | 'value'>) => {
  const account = useAppSelector(state => state.authentication.account);
  const locale = useAppSelector(state => state.locale.currentLocale);

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
      <TimePicker
        inputRef={ref}
        inputFormat={HOUR_FORMAT}
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
        value={field.value ? dayjs(field.value, HOUR_FORMAT).utc(true).locale(locale).tz(account.userDetail.timezone.zoneName) : null}
        onChange={value => field.onChange(value ? value.utc().format(HOUR_FORMAT) : null)}
        {...props}
      />
    </LocalizationProvider>
  );
};

export default CTimePicker;
