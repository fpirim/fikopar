import React from 'react';
import { IconButton, useTheme } from '@mui/material';
import { ColorModeContext } from 'app/app';
import Brightness4Icon from '@mui/icons-material/Brightness4';
import Brightness7Icon from '@mui/icons-material/Brightness7';

export const ColorMenu = () => {
  const theme = useTheme();
  const colorMode = React.useContext(ColorModeContext);
  return (
    <IconButton sx={{ ml: 1 }} onClick={colorMode.toggleColorMode} color="inherit">
      {theme.palette.mode === 'dark' ? <Brightness7Icon /> : <Brightness4Icon />}
    </IconButton>
  );
};
