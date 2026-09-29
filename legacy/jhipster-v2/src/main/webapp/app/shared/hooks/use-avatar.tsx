import React, { useCallback, useEffect } from 'react';
import { toast } from 'react-toastify';
import { translate } from 'react-jhipster';
import { Avatar, Button, Grid } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import axios from 'axios';

export const useAvatar = (type: string, id?: any) => {
  const [avatar, setAvatar] = React.useState<Blob | null>(null);
  const [avatarUrl, setAvatarUrl] = React.useState<string | null>(null);

  useEffect(() => {
    setAvatarUrl(avatar ? URL.createObjectURL(avatar) : null);
  }, [avatar]);

  useEffect(() => {
    if (type && id) {
      axios
        .get(`api/${type}/avatar/${id}`, { responseType: 'blob' })
        .then(res => setAvatar(res.data))
        .catch(() => setAvatar(null));
    } else {
      setAvatar(null);
    }
  }, [type, id]);

  const deleteAvatar = useCallback(() => {
    if (type && id) {
      axios
        .delete(`api/${type}/avatar/${id}`)
        .then(() => setAvatar(null))
        .catch(err => console.error(err));
    }
  }, [type, id]);

  const AvatarComponent = (
    <Grid container spacing={2}>
      <Grid item sm={12} sx={{ display: 'flex', justifyContent: 'center' }}>
        <Avatar
          sx={{
            height: 100,
            width: 100,
          }}
          src={avatarUrl}
        />
      </Grid>
      <Grid item sm={12} sx={{ display: 'flex', justifyContent: 'center', gap: 1 }}>
        <Button
          variant="contained"
          color="primary"
          size="small"
          startIcon={<EditIcon />}
          onClick={() => document.getElementById('upload-avatar').click()}
        >
          {translate('entity.action.edit')}
        </Button>
        <input
          accept="image/*"
          id="upload-avatar"
          type="file"
          style={{ display: 'none' }}
          onChange={event => {
            if (event.target.files.length !== 0) {
              if (event.target.files[0].size > 2 * 1024 * 1024) toast.error(translate('error.avatarUploadSize'));
              else setAvatar(event.target.files[0]);
            }
          }}
        />
        <Button
          variant="contained"
          color="error"
          size="small"
          startIcon={<DeleteIcon />}
          disabled={id === null || id === undefined}
          onClick={deleteAvatar}
        >
          {translate('entity.action.delete')}
        </Button>
      </Grid>
    </Grid>
  );

  return { avatar, AvatarComponent };
};
