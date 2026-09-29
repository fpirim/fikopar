import React from 'react';
import { translate } from 'react-jhipster';
import LogoutIcon from '@mui/icons-material/Logout';
import { Button } from '@mui/material';

const accountMenuItemsAuthenticated = () => (
  <>
    <Button component="a" href="/account/settings" startIcon={<LogoutIcon />} color="inherit" variant="text" disableRipple>
      {translate('global.menu.account.settings')}
    </Button>

    <Button component="a" href="/account/password" startIcon={<LogoutIcon />} color="inherit" variant="text" disableRipple>
      {translate('global.menu.account.password')}
    </Button>

    <Button component="a" href="/logout" startIcon={<LogoutIcon />} color="inherit" variant="text" disableRipple>
      {translate('global.menu.account.logout')}
    </Button>
  </>
);

const accountMenuItems = () => (
  <>
    <Button component="a" href="/login" startIcon={<LogoutIcon />} color="inherit" variant="text" disableRipple>
      {translate('global.menu.account.login')}
    </Button>

    <Button component="a" href="/account/register" startIcon={<LogoutIcon />} color="inherit" variant="text" disableRipple>
      {translate('global.menu.account.register')}
    </Button>
  </>
);

const AccountMenu = ({ isAuthenticated = false }) => {
  return <>{isAuthenticated ? accountMenuItemsAuthenticated() : accountMenuItems()}</>;
};

export default AccountMenu;
