import React, { useEffect, useRef } from 'react';
import { Control, FieldPathByValue, FieldValues, useController } from 'react-hook-form';
import PhoneInput, { PhoneInputProps } from 'react-phone-input-2';
import 'react-phone-input-2/lib/material.css';
import { styled } from '@mui/material/styles';
import { FormHelperText } from '@mui/material';

const PhoneInputComponent = styled(PhoneInput, {
  shouldForwardProp: prop => prop !== 'error' && prop !== 'fullWidth' && prop !== 'size' && prop !== 'disabled',
})<PhoneInputProps & { error?: boolean; fullWidth?: boolean; size?: 'small' | 'medium'; disabled?: boolean }>(
  ({ theme, error, fullWidth, size, disabled }) => ({
    '&.react-tel-input': {
      '.special-label': {
        left: '10px',
        top: '-9px',
        backgroundColor: theme.palette.background.paper,
        backgroundImage: 'linear-gradient(rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.08))',
        color: error ? theme.palette.error.main : theme.palette.text.secondary,
        fontSize: '0.75rem',
        '&:focus': {
          color: theme.palette.text.primary,
        },
      },
      '.form-control': {
        color: disabled ? theme.palette.text.disabled : theme.palette.text.primary,
        borderColor: error ? theme.palette.error.main : theme.palette.action.disabled,
        background: 'none',
        height: size === 'small' ? '1.4375em' : '3.4375em',
        width: fullWidth ? '100%' : 'auto',
        '&:hover': {
          borderColor: theme.palette.text.primary,
        },
        '&:focus': {
          borderColor: theme.palette.primary.main,
        },
      },
    },
  })
);

const CPhoneTextField = <TFieldValues extends FieldValues, TPath extends FieldPathByValue<TFieldValues, any>>({
  name,
  control,
  autoFocus,
  fullWidth,
  label = '',
  size,
  ...props
}: {
  control: Control<TFieldValues>;
  name: TPath;
  autoFocus?: boolean;
  fullWidth?: boolean;
  label?: string;
  size?: 'small' | 'medium';
} & PhoneInputProps) => {
  const focussed = useRef(false);
  const inputRef = useRef<HTMLInputElement>();
  const {
    field,
    fieldState: { error },
  } = useController({
    control,
    name,
  });

  useEffect(() => {
    if (inputRef.current && autoFocus && !props.disabled && !focussed.current) {
      setTimeout(() => inputRef.current.focus(), 100);
      focussed.current = true;
    }
  }, [autoFocus, props.disabled]);

  return (
    <>
      <PhoneInputComponent
        error={!!error}
        fullWidth={fullWidth}
        size={size}
        country={'tr'}
        placeholder={'+90 5xx xxx xx xx'}
        inputProps={{
          name,
          ref: inputRef,
        }}
        {...field}
        specialLabel={label}
        onChange={
          (value, country, event, formattedValue) => field.onChange('+' + value) //field.onChange(formattedValue.replace(/\s/g, ''))
        }
        {...props}
      />
      <FormHelperText sx={{ marginLeft: '14px', top: '4px' }} error={!!error}>
        {!!error && error.message}
      </FormHelperText>
    </>
  );
};

export default CPhoneTextField;
