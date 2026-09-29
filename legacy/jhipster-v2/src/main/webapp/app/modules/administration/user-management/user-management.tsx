import React from 'react';
import { translate } from 'react-jhipster';
import { deleteEntities, getUsersAsAdmin } from './user-management.reducer';
import { useAppSelector } from 'app/config/store';
import { Box, Chip, Grid, Paper } from '@mui/material';
import { GridColDef, GridRowParams, GridRowSelectionModel } from '@mui/x-data-grid-pro';
import { IUser } from 'app/shared/model/user.model';
import { useNavEdit } from 'app/shared/hooks/use-nav-edit';
import { useEntityAction } from 'app/shared/hooks/use-entity-action';
import DataGrid from 'app/shared/core-components/datagrid';
import { usePagination } from 'app/shared/hooks/use-pagination';
import { useNavigate } from 'react-router-dom';
import { languages } from 'app/config/translation';
import DateCard from 'app/shared/components/date-card';

export const UserManagement = () => {
  const navigate = useNavigate();
  const entityName = 'userManagement';

  const entities = useAppSelector(state => state[entityName].entities);
  const totalItems = useAppSelector(state => state[entityName].totalItems);
  const loading = useAppSelector(state => state[entityName].loading);
  const updating = useAppSelector(state => state[entityName].updating);
  const deleting = useAppSelector(state => state[entityName].deleting);
  const deleteSuccess = useAppSelector(state => state[entityName].deleteSuccess);

  const { searchComponent, dataGridPaginationProps, handleSyncList } = usePagination({
    getEntities: getUsersAsAdmin,
    loading: loading || deleting || updating,
    searchParams: ['login.contains', 'firstName.contains', 'lastName.contains', 'email.contains'],
  });

  const handleEdit = (params?: GridRowParams) => {
    if (params && params.row && params.row.login) {
      navigate(`${params.row.login}`);
    } else {
      navigate(`new`);
    }
  };

  const { dataGridEditProps } = useNavEdit({ handleSyncList, handleEdit });

  const { actions, dataGridActionProps } = useEntityAction({
    handleSyncList,
    loading: loading || deleting || updating,
    handleNew: handleEdit,
    deleteEntities,
    deleteSuccess,
    entityName,
    mapRowSelectionModel: (gridRowSelectionModel: GridRowSelectionModel) =>
      gridRowSelectionModel.map(rowId => entities.find(entity => entity.id === rowId).login || null).filter(login => login !== null),
    appPrefix: false,
  });
  const columns: GridColDef<IUser>[] = [
    { field: 'id', headerName: translate(`global.field.id`), flex: 1 },
    { field: 'login', headerName: translate(`userManagement.login`), flex: 1 },
    { field: 'email', headerName: translate(`userManagement.email`), flex: 1 },
    {
      field: 'activated',
      headerName: translate(`userManagement.status`),
      flex: 1,
      renderCell: params => (
        <Chip
          variant="outlined"
          color={params.row.activated ? 'success' : 'error'}
          size="small"
          label={translate(params.row.activated ? 'userManagement.activated' : 'userManagement.deactivated')}
        />
      ),
    },
    {
      field: 'langKey',
      headerName: translate(`userManagement.langKey`),
      flex: 1,
      valueGetter: params => languages[params.row.langKey]?.name ?? '',
    },
    {
      field: 'authorities',
      headerName: translate(`userManagement.profiles`),
      flex: 1,
      sortable: false,
      renderCell: params => (
        <Box display="flex" flexDirection="column" gap={1}>
          {params.row.authorities?.map((authority, j) => (
            <Chip key={`${params.row.login}-auth-${j}`} variant="outlined" color="success" size="small" label={authority} />
          ))}
        </Box>
      ),
    },
    {
      field: 'createdDate',
      headerName: translate(`userManagement.createdDate`),
      flex: 1,
      renderCell: params => <DateCard date={params.row.createdDate} />,
    },
    { field: 'lastModifiedBy', headerName: translate(`userManagement.lastModifiedBy`), flex: 1 },
    {
      field: 'lastModifiedDate',
      headerName: translate(`userManagement.lastModifiedDate`),
      flex: 1,
      renderCell: params => <DateCard date={params.row.lastModifiedDate} />,
    },
  ];

  return (
    <Paper sx={{ padding: 2, margin: 2 }} elevation={3}>
      <Grid container spacing={2}>
        <Grid item sm={12}>
          <Box
            sx={{
              display: 'flex',
              gap: 1,
            }}
          >
            {searchComponent}
            {actions}
          </Box>
        </Grid>
        <Grid item sm={12}>
          <DataGrid
            columns={columns}
            rows={entities}
            rowCount={totalItems}
            disableColumnFilter
            disableColumnMenu
            disableRowSelectionOnClick
            disableCellFocusOutline
            {...dataGridEditProps}
            {...dataGridActionProps}
            {...dataGridPaginationProps}
          />
        </Grid>
      </Grid>
    </Paper>
  );
};

export default UserManagement;
