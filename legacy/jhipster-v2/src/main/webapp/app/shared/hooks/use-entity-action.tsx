import { translate } from 'react-jhipster';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@mui/material';
import React, { useEffect } from 'react';
import RefreshIcon from '@mui/icons-material/Refresh';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';

import { GridCallbackDetails, GridRowSelectionModel } from '@mui/x-data-grid-pro';
import { AsyncThunk } from '@reduxjs/toolkit';
import { AxiosResponse } from 'axios';
import { useAppDispatch } from 'app/config/store';
import CancelIcon from '@mui/icons-material/Cancel';

interface useEntityActionProps {
  loading: any;
  handleSyncList: (values?: any) => void;
  handleNew?: () => void;
  deleteEntities?: AsyncThunk<AxiosResponse<any, any>, any[], {}>;
  mapRowSelectionModel?: (gridRowSelectionModel: GridRowSelectionModel) => any[];
  deleteSuccess?: boolean;
  entityName: string;
  appPrefix?: boolean;
}

interface ConfirmDeleteDialogProps {
  open: boolean;
  entityName: string;
  handleClose: () => void;
  handleDelete: () => void;
  appPrefix?: boolean;
}
export const ConfirmDeleteDialog = ({ open, handleClose, handleDelete, entityName, appPrefix = true }: ConfirmDeleteDialogProps) => {
  return (
    <Dialog open={open} onClose={handleClose} aria-labelledby="delete-dialog-title" aria-describedby="delete-dialog-description">
      <DialogTitle id="delete-dialog-title">{translate(`entity.delete.title`)}</DialogTitle>
      <DialogContent>
        <DialogContentText id="delete-dialog-description">
          {translate(`${appPrefix ? 'fikoparApp.' : ''}${entityName}.delete.question`)}
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleDelete} startIcon={<DeleteIcon />} color="error">
          {translate('entity.action.delete')}
        </Button>
        <Button onClick={handleClose} startIcon={<CancelIcon />} autoFocus color="primary">
          {translate('entity.action.cancel')}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export const useEntityAction = ({
  handleSyncList,
  loading,
  handleNew,
  deleteEntities,
  deleteSuccess,
  entityName,
  appPrefix = true,
  mapRowSelectionModel,
}: useEntityActionProps) => {
  const dispatch = useAppDispatch();

  const [rowSelectionModel, setRowSelectionModel] = React.useState<GridRowSelectionModel>([]);

  const [showModal, setShowModal] = React.useState(false);

  const handleClose = () => setShowModal(false);

  useEffect(() => {
    if (deleteSuccess) {
      handleSyncList('');
    }
  }, [deleteSuccess]);

  const actions = (
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'flex-end',
        gap: 1,
      }}
    >
      <Button
        variant="contained"
        startIcon={<RefreshIcon />}
        color={loading ? 'secondary' : 'primary'}
        disabled={loading}
        onClick={handleSyncList}
      >
        {translate('entity.action.refresh')}
      </Button>
      {handleNew && (
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          color={loading ? 'secondary' : 'primary'}
          disabled={loading}
          onClick={handleNew}
        >
          {translate('entity.action.new')}
        </Button>
      )}
      {deleteEntities && (
        <Button
          variant="contained"
          startIcon={<DeleteIcon />}
          color={loading ? 'secondary' : 'error'}
          disabled={loading || !rowSelectionModel.length}
          onClick={() => setShowModal(true)}
        >
          {translate('entity.action.delete')}
        </Button>
      )}
      <ConfirmDeleteDialog
        open={showModal}
        handleClose={handleClose}
        handleDelete={() => {
          dispatch(deleteEntities(mapRowSelectionModel ? mapRowSelectionModel(rowSelectionModel) : rowSelectionModel));
          handleClose();
        }}
        entityName={entityName}
        appPrefix={appPrefix}
      />
    </Box>
  );

  const dataGridActionProps: {
    checkboxSelection: boolean;
    onRowSelectionModelChange: (rowSelectionModel: GridRowSelectionModel, details: GridCallbackDetails) => void | undefined;
    rowSelectionModel: GridRowSelectionModel | undefined;
  } = {
    checkboxSelection: !!deleteEntities,
    onRowSelectionModelChange: !!deleteEntities ? newRowSelectionModel => setRowSelectionModel(newRowSelectionModel) : undefined,
    rowSelectionModel: !!deleteEntities ? rowSelectionModel : undefined,
  };

  return { actions, dataGridActionProps };
};
