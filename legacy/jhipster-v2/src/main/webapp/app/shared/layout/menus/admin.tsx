import React from 'react';
import { translate } from 'react-jhipster';
import { Button, Menu, MenuItem } from '@mui/material';
import VisibilityIcon from '@mui/icons-material/Visibility';
import SettingsIcon from '@mui/icons-material/Settings';
import AssignmentIcon from '@mui/icons-material/Assignment';
import FavoriteIcon from '@mui/icons-material/Favorite';
import MonitorHeartIcon from '@mui/icons-material/MonitorHeart';
import LibraryBooksIcon from '@mui/icons-material/LibraryBooks';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import { Link } from 'react-router-dom';

export const AdminMenu = ({ showOpenAPI, isAdmin }: { showOpenAPI: boolean; isAdmin: boolean }) => {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const items: { icon: any; link: string; value: string }[] = [];

  if (isAdmin) {
    items.push({
      icon: <VisibilityIcon />,
      link: `/admin/user-management`,
      value: translate('global.menu.admin.userManagement'),
    });

    items.push({
      icon: <VisibilityIcon />,
      link: `/admin/tracker`,
      value: translate('global.menu.admin.tracker'),
    });

    items.push({
      icon: <FavoriteIcon />,
      link: '/admin/health',
      value: translate('global.menu.admin.health'),
    });

    items.push({
      icon: <SettingsIcon />,
      link: '/admin/configuration',
      value: translate('global.menu.admin.configuration'),
    });

    items.push({
      icon: <MonitorHeartIcon />,
      link: '/admin/metrics',
      value: translate('global.menu.admin.metrics'),
    });

    items.push({
      icon: <AssignmentIcon />,
      link: '/admin/logs',
      value: translate('global.menu.admin.logs'),
    });
  }

  if (showOpenAPI) {
    items.push({
      icon: <LibraryBooksIcon />,
      link: `/admin/docs`,
      value: translate('global.menu.admin.apidocs'),
    });
  }

  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };

  return (
    <>
      <Button
        id="admin-menu-button"
        aria-controls={open ? 'admin-menu' : undefined}
        aria-haspopup="true"
        aria-expanded={open ? 'true' : undefined}
        color="inherit"
        startIcon={<AdminPanelSettingsIcon />}
        onClick={handleClick}
      >
        {translate('global.menu.admin.main')}
      </Button>

      <Menu
        id="admin-menu"
        aria-labelledby="admin-menu-button"
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
      >
        {items.map(item => (
          <MenuItem component={Link} key={item.link} to={item.link} onClick={handleClose}>
            {item.icon}&nbsp;{item.value}
          </MenuItem>
        ))}
      </Menu>
    </>
  );
};

export default AdminMenu;
