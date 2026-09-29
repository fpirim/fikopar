import { useLocation, useNavigate } from 'react-router-dom';
import React, { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useAppDispatch } from 'app/config/store';
import { Box, Button, IconButton } from '@mui/material';
import { translate } from 'react-jhipster';
import CancelRoundedIcon from '@mui/icons-material/CancelRounded';
import SearchIcon from '@mui/icons-material/Search';
import { AsyncThunk } from '@reduxjs/toolkit';
import { AxiosResponse } from 'axios';
import { IQueryParams } from 'app/shared/reducers/reducer.utils';
import { GridSortModel } from '@mui/x-data-grid-pro';
import CTextField from 'app/shared/core-components/core-textfield';
import { GridSortDirection } from '@mui/x-data-grid/models/gridSortModel';
import { getSortState, overrideListStateWithQueryParams } from 'app/shared/util/list-utils';
import FileDownloadIcon from '@mui/icons-material/FileDownload';

interface userListProps<T> {
  getEntities: AsyncThunk<AxiosResponse<T[], any>, IQueryParams, any>;
  getExcelReport?: AsyncThunk<AxiosResponse<Blob, any>, IQueryParams, any>;
  loading: any;
  defaultParams?: any;
  searchParams?: string[];
  defaultSortBy?: string;
  defaultSortDir?: GridSortDirection;
}

export const useList = <T,>({
  getEntities,
  getExcelReport,
  loading,
  defaultParams,
  searchParams,
  defaultSortBy = 'id',
  defaultSortDir = 'asc',
}: userListProps<T>) => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const createSearchParams = (obj: any, propStr = '') => {
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

  const [listState, setListState] = useState(
    overrideListStateWithQueryParams(getSortState(location, '', defaultSortBy, defaultSortDir), location.search)
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
        ...(searchParams ? searchParams.reduce((acc, param) => ({ ...acc, [param]: listState.search }), {}) : {}),
        sort: `${listState.sortBy},${listState.sortDir}`,
      })
    );
  };

  const sortEntities = () => {
    getAllEntities();

    const queryParams = {
      ...(defaultParams ? createSearchParams(defaultParams) : {}),
      ...(searchParams ? searchParams.reduce((acc, param) => ({ ...acc, [param]: listState.search }), {}) : {}),
      sort: `${listState.sortBy},${listState.sortDir}`,
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
  }, [defaultParams, listState.search, listState.sortBy, listState.sortDir]);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const search = params.get('search');
    const sortBy = params.get('sortBy');
    const sortDir = params.get('sortDir');
    if (search !== null && search !== undefined && sortBy && sortDir) {
      setListState({
        ...listState,
        search,
        sortBy,
        sortDir,
      });
      reset({ search });
    }
    setFocus('search');
  }, [location.search]);

  const handleSyncList = (values?: any) => {
    if (values && values.search !== null && values.search !== undefined) {
      setListState({
        ...listState,
        search: values.search,
      });
      reset({ search: values.search });
    } else {
      sortEntities();
    }
    setFocus('search');
  };

  const handleExcelReport = () => {
    dispatch(
      getExcelReport({
        ...(defaultParams ? createSearchParams(defaultParams) : {}),
        ...(searchParams ? searchParams.reduce((acc, param) => ({ ...acc, [param]: listState.search }), {}) : {}),
        sort: `${listState.sortBy},${listState.sortDir}`,
      })
    )
      .unwrap()
      .then(response => {
        const href = URL.createObjectURL(response.data);

        // create "a" HTML element with href to file & click
        const link = document.createElement('a');
        link.href = href;
        link.setAttribute('download', 'cari.xlsx');
        document.body.appendChild(link);
        link.click();

        // clean up "a" element & remove ObjectURL
        document.body.removeChild(link);
        URL.revokeObjectURL(href);
      });
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

  const excelComponent = getExcelReport ? (
    <Box sx={{ display: 'flex', gap: 1 }}>
      <Button
        variant="contained"
        startIcon={<FileDownloadIcon />}
        color={loading ? 'secondary' : 'primary'}
        disabled={loading}
        onClick={handleExcelReport}
      >
        {translate('entity.action.excel')}
      </Button>
    </Box>
  ) : null;

  const dataGridListProps: {
    pagination: boolean;
    sortModel: GridSortModel;
    onSortModelChange: (model: GridSortModel) => void;
    loading: boolean;
    sortingOrder: GridSortDirection[];
  } = {
    pagination: false,
    sortingOrder: ['desc', 'asc'],
    sortModel: [
      {
        field: listState.sortBy,
        sort: listState.sortDir as unknown as GridSortDirection,
      },
    ],
    onSortModelChange(model: GridSortModel) {
      if (model.length > 0) {
        setListState({
          ...listState,
          sortBy: model.length > 0 ? model[0].field : 'id',
          sortDir: model.length > 0 ? model[0].sort : 'asc',
        });
      }
    },
    loading: !!loading,
  };

  return { searchComponent, excelComponent, dataGridListProps, handleSyncList };
};
