import React from 'react';

import { GridCallbackDetails, GridRowParams, MuiEvent } from '@mui/x-data-grid-pro';
import { useNavigate } from 'react-router-dom';

interface useNavEditProps {
  handleSyncList: (values?: any) => void;
  handleEdit?: (params?: GridRowParams) => void;
}

export const useNavEdit = ({ handleSyncList, handleEdit }: useNavEditProps) => {
  const navigate = useNavigate();

  const handleInsideEdit = (params?: GridRowParams) => {
    if (params && params.row && params.row.id) {
      navigate(`${params.row.id}`);
    } else {
      navigate(`new`);
    }
  };

  const dataGridEditProps: {
    onRowClick: (params: GridRowParams, event: MuiEvent<React.MouseEvent>, details: GridCallbackDetails) => void;
  } = {
    onRowClick: handleEdit ?? handleInsideEdit,
  };

  return { handleEdit: handleEdit ?? handleInsideEdit, dataGridEditProps };
};
