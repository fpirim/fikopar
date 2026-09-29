import { Checkbox, FormControl, FormControlLabel, FormHelperText } from '@mui/material';
import { CheckboxProps } from '@mui/material/Checkbox/Checkbox';
import { Control, FieldPathByValue, FieldValues, useController } from 'react-hook-form';
import React, { useEffect, useRef } from 'react';

const CCheckField = <TFieldValues extends FieldValues, TPath extends FieldPathByValue<TFieldValues, any>>({
  name,
  control,
  autoFocus,
  label = '',
  ...props
}: {
  control: Control<TFieldValues>;
  name: TPath;
  label?: string;
} & CheckboxProps) => {
  const focussed = useRef(false);
  const componentRef = useRef<HTMLButtonElement>();
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
    <FormControl error={!!error}>
      <FormControlLabel
        control={
          <Checkbox
            checked={Boolean(field.value)}
            onChange={field.onChange}
            onBlur={field.onBlur}
            autoFocus={autoFocus}
            inputRef={ref}
            ref={componentRef}
            {...props}
          />
        }
        label={label}
      />
      <FormHelperText>{error && error.message}</FormHelperText>
    </FormControl>
  );
};

export default CCheckField;
