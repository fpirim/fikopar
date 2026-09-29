import React, { useState } from 'react';

import { GridCallbackDetails, GridRowParams, MuiEvent } from '@mui/x-data-grid-pro';

interface useDialogEditProps {
  handleSyncList: (values?: any) => void;
}

export const useDialogEdit = ({ handleSyncList }: useDialogEditProps) => {
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState();

  const handleEdit = (params?: GridRowParams) => {
    setShowModal(true);
    if (params && params.row && params.row.id) {
      setEditId(params.row.id);
    } else {
      setEditId(null);
    }
  };

  const handleClose = () => {
    handleSyncList();
    setShowModal(false);
  };

  const dataGridEditProps: {
    onRowClick: (params: GridRowParams, event: MuiEvent<React.MouseEvent>, details: GridCallbackDetails) => void;
  } = {
    onRowClick: handleEdit,
  };

  return { handleClose, handleEdit, showModal, editId, dataGridEditProps };
};
