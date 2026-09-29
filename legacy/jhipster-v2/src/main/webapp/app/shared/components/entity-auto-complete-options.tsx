import React from 'react';

import { IKatalog } from 'app/shared/model/katalog.model';
import { ISicil } from 'app/shared/model/sicil.model';
import { Box, Typography } from '@mui/material';

// const UserLabelOption = (option: IUser) => `${option.userDetail.firstname} ${option.userDetail.lastname}`;
// const UserRenderOption = (props: React.HTMLAttributes<HTMLLIElement>, option: IUser) => <li {...props}>
//   <UserCard user={option} size='small' /></li>;

const KatalogLabelOption = (option: IKatalog) => `${option ? option.katalogadi : ''}`;
const KatalogRenderOption = (props: React.HTMLAttributes<HTMLLIElement>, option: IKatalog) => (
  <li {...props}>{`${option.katalogadi} - ${option.id}`}</li>
);

const SicilLabelOption = (option: ISicil) => `${option ? `${option.hesapno} - ${option.hesapadi}` : ''}`;
const SicilRenderOption = (props: React.HTMLAttributes<HTMLLIElement>, option: ISicil) => (
  <li {...props}>
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <Typography variant="body1">{`${option.hesapadi} - ${option.hesapno}`}</Typography>
      <Typography variant="body2">{`${option.vda} - ${option.vno}/${option.tcno}`}</Typography>
    </Box>
  </li>
);

export const EntityAutoCompleteLabelOptions = {
  // UserLabelOption,
  KatalogLabelOption,
  SicilLabelOption,
};

export const EntityAutoCompleteRenderOptions = {
  // UserRenderOption,
  KatalogRenderOption,
  SicilRenderOption,
};
