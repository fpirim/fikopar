import CAutoComplete from 'app/shared/core-components/core-autocomplete';
import React, { ReactNode, useDeferredValue, useEffect, useState } from 'react';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { Control, FieldPathByValue, FieldValues, PathValue } from 'react-hook-form';
import { AxiosResponse } from 'axios';
import { AsyncThunkAction } from '@reduxjs/toolkit';
import { IQueryParams } from 'app/shared/reducers/reducer.utils';
import { AutocompleteRenderOptionState } from '@mui/material/Autocomplete/Autocomplete';

const EntityAutoCompleteField = <
  TFieldValues extends FieldValues | undefined,
  TPath extends FieldPathByValue<TFieldValues, any>,
  TValue extends PathValue<TFieldValues, TPath>
>({
  name,
  control,
  label,
  disabled = false,
  autoFocus = false,
  fullWidth = false,
  size,
  entityName,
  sort = 'id,asc',
  fetchEntities,
  getOptionLabel,
  renderOption,
  searchParams,
}: {
  control: Control<TFieldValues>;
  disabled?: boolean;
  label: string;
  name: TPath;
  autoFocus?: boolean;
  fullWidth?: boolean;
  entityName: string;
  size?: 'small' | 'medium';
  sort?: string;
  fetchEntities: (arg: IQueryParams) => AsyncThunkAction<AxiosResponse<TValue[], any>, IQueryParams, {}>;
  getOptionLabel: (option: TValue) => string;
  renderOption: (props: React.HTMLAttributes<HTMLLIElement>, option: TValue, state: AutocompleteRenderOptionState) => ReactNode;
  searchParams: string[];
}) => {
  const dispatch = useAppDispatch();

  const loading: boolean = useAppSelector(state => state[entityName].loading);
  const entities: TValue[] = useAppSelector(state => state[entityName].entities);

  const [search, setSearch] = useState<string>('');
  const deferredSearch = useDeferredValue(search);

  useEffect(() => {
    dispatch(
      fetchEntities({
        ...(searchParams ? searchParams.reduce((acc, param) => ({ ...acc, [param]: deferredSearch }), {}) : {}),
        page: 0,
        size: 100,
        sort,
      })
    );
  }, [deferredSearch, sort, fetchEntities]);

  return (
    <CAutoComplete
      name={name}
      control={control}
      options={entities}
      loading={loading}
      isOptionEqualToValue={(option, value) => (option && value ? option.id === value.id : option === value)}
      getOptionLabel={getOptionLabel}
      renderOption={renderOption}
      label={label}
      size={size}
      autoFocus={autoFocus}
      fullWidth={fullWidth}
      disabled={disabled}
      freeSolo
      onKeyUp={e => {
        setSearch((e.target as HTMLInputElement).value);
      }}
    />
  );
};

export default EntityAutoCompleteField;
