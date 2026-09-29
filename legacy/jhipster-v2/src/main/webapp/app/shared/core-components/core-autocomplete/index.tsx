import { Autocomplete, AutocompleteProps, TextField } from '@mui/material';
import React, { useEffect, useRef } from 'react';
import { Control, FieldPathByValue, FieldValues, useController } from 'react-hook-form';
import { translate } from 'react-jhipster';

const CAutoComplete = <
  T,
  TFieldValues extends FieldValues | undefined,
  TPath extends FieldPathByValue<TFieldValues, any>,
  Multiple extends boolean | undefined = false,
  DisableClearable extends boolean | undefined = false,
  FreeSolo extends boolean | undefined = false
>({
  name,
  control,
  options,
  getOptionLabel,
  label,
  loading,
  autoFocus,
  ...props
}: {
  control: Control<TFieldValues>;
  name: TPath;
  label: string;
  autoFocus?: boolean;
} & Omit<AutocompleteProps<T, Multiple, DisableClearable, FreeSolo>, 'renderInput'>) => {
  const focussed = useRef(false);
  const componentRef = useRef<HTMLDivElement>();
  const {
    field,
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
    <Autocomplete
      options={loading ? [] : options || []}
      getOptionLabel={getOptionLabel}
      isOptionEqualToValue={(option, value) => option === value}
      renderInput={params => (
        <TextField
          {...params}
          ref={componentRef}
          label={label}
          error={!!error}
          helperText={!!error && error.message}
          inputProps={{
            ...params.inputProps,
            onKeyDown: e => {
              // how to implement when enter key prevent to close dialog if inside
            },
          }}
        />
      )}
      value={field.value || null}
      ref={field.ref}
      onChange={(event, value) => field.onChange(value)}
      onBlur={field.onBlur}
      loading={loading}
      loadingText={translate('entity.action.loading')}
      noOptionsText={translate('entity.action.noOptions')}
      {...props}
    />
  );
};

export default CAutoComplete;
