import { useLocation, useNavigate } from 'react-router-dom';
import React, { useEffect, useState } from 'react';
import { getSortState, overridePaginationStateWithQueryParams } from 'app/shared/util/pagination-utils';
import { ITEMS_PER_PAGE } from 'app/shared/util/pagination.constants';
import { useForm } from 'react-hook-form';
import { useAppDispatch } from 'app/config/store';
import { Box, Button, IconButton } from '@mui/material';
import { translate } from 'react-jhipster';
import CancelRoundedIcon from '@mui/icons-material/CancelRounded';
import SearchIcon from '@mui/icons-material/Search';
import { AsyncThunk } from '@reduxjs/toolkit';
import { AxiosResponse } from 'axios';
import { IQueryParams } from 'app/shared/reducers/reducer.utils';
import { GridFeatureMode, GridPaginationModel, GridSortModel } from '@mui/x-data-grid-pro';
import CTextField from 'app/shared/core-components/core-textfield';
import { GridSortDirection } from '@mui/x-data-grid/models/gridSortModel';

interface usePaginationProps<T> {
  getEntities: AsyncThunk<AxiosResponse<T[], any>, IQueryParams, {}>;
  loading: any;
  defaultParams?: any;
  searchParams?: string[];
  defaultSortBy?: string;
  defaultSortDir?: GridSortDirection;
}

export const usePagination = <T,>({
  getEntities,
  loading,
  defaultParams,
  searchParams,
  defaultSortBy = 'id',
  defaultSortDir = 'asc',
}: usePaginationProps<T>) => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const createSearchParams = (obj: Object, propStr: string = '') => {
    return Object.entries(obj).reduce((acc, [key, val]) => {
      const nestedPropStr = propStr + (propStr ? '.' : '') + key;
      if (val !== null) {
        if (typeof val === 'object') {
          return { ...acc, ...createSearchParams(val, nestedPropStr) };
        }
        return { ...acc, [nestedPropStr]: val };
      }
      return acc;
    }, {});
  };

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, '', defaultSortBy, defaultSortDir), location.search)
  );

  const { handleSubmit, control, reset, watch, setFocus } = useForm({
    defaultValues: {
      search: '',
    },
  });

  const getAllEntities = () => {
    dispatch(
      getEntities({
        ...(defaultParams ? createSearchParams(defaultParams) : {}),
        ...(searchParams ? searchParams.reduce((acc, param) => ({ ...acc, [param]: paginationState.search }), {}) : {}),
        page: paginationState.page,
        size: paginationState.pageSize,
        sort: `${paginationState.sortBy},${paginationState.sortDir}`,
      })
    );
  };

  const sortEntities = () => {
    getAllEntities();

    const queryParams = {
      ...(defaultParams ? createSearchParams(defaultParams) : {}),
      ...(searchParams ? searchParams.reduce((acc, param) => ({ ...acc, [param]: paginationState.search }), {}) : {}),
      page: paginationState.page,
      size: paginationState.pageSize,
      sort: `${paginationState.sortBy},${paginationState.sortDir}`,
    };

    const endURL = `?${Object.keys(queryParams)
      .map(key => `${key}=${queryParams[key]}`)
      .join('&')}`;

    if (location.search !== endURL) {
      navigate(`${location.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [
    defaultParams,
    paginationState.search,
    paginationState.page,
    paginationState.pageSize,
    paginationState.sortBy,
    paginationState.sortDir,
  ]);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const search = params.get('search');
    const page = params.get('page');
    const sortBy = params.get('sortBy');
    const sortDir = params.get('sortDir');
    if (search !== null && search !== undefined && page && sortBy && sortDir) {
      setPaginationState({
        ...paginationState,
        search,
        page: +page,
        sortBy,
        sortDir,
      });
      reset({ search });
    }
    setFocus('search');
  }, [location.search]);

  const handleSyncList = (values?: any) => {
    if (values && values.search !== null && values.search !== undefined) {
      setPaginationState({
        ...paginationState,
        search: values.search,
        page: 0,
      });
      reset({ search: values.search });
    } else {
      sortEntities();
    }
    setFocus('search');
  };

  const searchComponent = (
    <form
      style={{ flexGrow: 1 }}
      onSubmit={() => {
        handleSubmit(handleSyncList)();
      }}
    >
      <Box sx={{ display: 'flex', gap: 1 }}>
        <CTextField
          name="search"
          control={control}
          sx={{ flexGrow: 1 }}
          autoFocus
          fullWidth
          label={translate('entity.action.filter')}
          size="small"
          onKeyDown={e => {
            if (e.key === 'Escape' && !loading) {
              handleSyncList({ search: '' });
            }
          }}
          autoComplete="off"
          InputProps={{
            endAdornment: watch('search') && (
              <IconButton
                disabled={loading}
                onClick={() => {
                  handleSyncList({ search: '' });
                }}
              >
                <CancelRoundedIcon />
              </IconButton>
            ),
          }}
        />
        <Button variant="contained" startIcon={<SearchIcon />} type="submit" color={loading ? 'secondary' : 'primary'} disabled={loading}>
          {translate('entity.action.search')}
        </Button>
      </Box>
    </form>
  );

  const dataGridPaginationProps: {
    paginationMode: GridFeatureMode;
    pagination: boolean;
    pageSizeOptions: number[];
    sortModel: GridSortModel;
    onSortModelChange: (model: GridSortModel) => void;
    paginationModel: GridPaginationModel;
    onPaginationModelChange: (model: GridPaginationModel) => void;
    loading: boolean;
    sortingOrder: GridSortDirection[];
  } = {
    paginationMode: 'server',
    pagination: true,
    pageSizeOptions: [25, 50, 100],
    sortingOrder: ['desc', 'asc'],
    sortModel: [
      {
        field: paginationState.sortBy,
        sort: paginationState.sortDir as unknown as GridSortDirection,
      },
    ],
    onSortModelChange(model: GridSortModel) {
      if (model.length > 0) {
        setPaginationState({
          ...paginationState,
          sortBy: model.length > 0 ? model[0].field : 'id',
          sortDir: model.length > 0 ? model[0].sort : 'asc',
        });
      }
    },
    paginationModel: { page: paginationState.page, pageSize: paginationState.pageSize },
    onPaginationModelChange(model) {
      setPaginationState({
        ...paginationState,
        page: model.page,
        pageSize: model.pageSize,
      });
    },
    loading: !!loading,
  };

  return { searchComponent, dataGridPaginationProps, handleSyncList };
};
