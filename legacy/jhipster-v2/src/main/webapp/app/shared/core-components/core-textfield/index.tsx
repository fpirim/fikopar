import { TextField, TextFieldProps } from '@mui/material';
import React, { useEffect, useRef } from 'react';
import { Control, FieldPathByValue, FieldValues, useController } from 'react-hook-form';

const CTextField = <TFieldValues extends FieldValues, TPath extends FieldPathByValue<TFieldValues, any>>({
  name,
  control,
  autoFocus,
  ...props
}: {
  control: Control<TFieldValues>;
  name: TPath;
} & TextFieldProps) => {
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
    <TextField
      error={!!error}
      helperText={!!error && error.message}
      inputRef={ref}
      ref={componentRef}
      {...field}
      value={field.value || ''}
      {...props}
    />
  );
};

export default CTextField;
