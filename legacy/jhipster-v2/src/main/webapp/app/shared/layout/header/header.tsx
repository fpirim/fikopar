import './header.scss';

import React, { useState } from 'react';
import { Translate } from 'react-jhipster';
import { styled } from '@mui/material/styles';
import MuiAppBar, { AppBarProps as MuiAppBarProps } from '@mui/material/AppBar';
import LoadingBar from 'react-redux-loading-bar';
import { Divider, IconButton, Toolbar, Typography } from '@mui/material';
import MuiDrawer from '@mui/material/Drawer';
import MenuIcon from '@mui/icons-material/Menu';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import { LocaleMenu } from 'app/shared/layout/menus';
import AccountMenu from 'app/shared/layout/menus/account';
import AdminMenu from 'app/shared/layout/menus/admin';
import EntitiesMenu from 'app/entities/menu';
import { ColorMenu } from 'app/shared/layout/menus/color-menu';

export interface IHeaderProps {
  isAuthenticated: boolean;
  isAdmin: boolean;
  ribbonEnv: string;
  isInProduction: boolean;
  isOpenAPIEnabled: boolean;
  currentLocale: string;
}

const drawerWidth: number = 270;

interface AppBarProps extends MuiAppBarProps {
  open?: boolean;
}

const AppBar = styled(MuiAppBar, {
  shouldForwardProp: prop => prop !== 'open',
})<AppBarProps>(({ theme, open }) => ({
  zIndex: theme.zIndex.drawer + 1,
  transition: theme.transitions.create(['width', 'margin'], {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.leavingScreen,
  }),
  ...(open && {
    marginLeft: drawerWidth,
    width: `calc(100% - ${drawerWidth}px)`,
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  }),
}));

const Drawer = styled(MuiDrawer, { shouldForwardProp: prop => prop !== 'open' })(({ theme, open }) => ({
  '& .MuiDrawer-paper': {
    position: 'relative',
    whiteSpace: 'nowrap',
    width: drawerWidth,
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
    boxSizing: 'border-box',
    ...(!open && {
      overflowX: 'hidden',
      transition: theme.transitions.create('width', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen,
      }),
      width: theme.spacing(7),
      [theme.breakpoints.up('sm')]: {
        width: theme.spacing(7),
      },
    }),
  },
}));

const Header = (props: IHeaderProps) => {
  const [menuOpen, setMenuOpen] = useState(false);
  const renderDevRibbon = () =>
    props.isInProduction === false ? (
      <div className="ribbon dev">
        <a href="">
          <Translate contentKey={`global.ribbon.${props.ribbonEnv}`} />
        </a>
      </div>
    ) : null;

  const toggleMenu = () => setMenuOpen(!menuOpen);

  return (
    <>
      <>
        {renderDevRibbon()}
        <LoadingBar className="loading-bar" />
      </>
      <AppBar position="absolute" open={menuOpen}>
        <Toolbar
          sx={{
            pr: '24px', // keep right padding when drawer closed
          }}
        >
          {props.isAuthenticated && (
            <IconButton
              edge="start"
              color="inherit"
              aria-label="open drawer"
              onClick={toggleMenu}
              sx={{
                marginRight: '36px',
                ...(menuOpen && { display: 'none' }),
              }}
            >
              <MenuIcon />
            </IconButton>
          )}
          <Typography component="h1" variant="h6" color="inherit" noWrap sx={{ flexGrow: 1 }}>
            Fikopar
          </Typography>
          <ColorMenu />
          <LocaleMenu currentLocale={props.currentLocale} />
          {props.isAdmin && <AdminMenu showOpenAPI={props.isOpenAPIEnabled} isAdmin={props.isAdmin} />}
          <AccountMenu isAuthenticated={props.isAuthenticated} />
        </Toolbar>
      </AppBar>

      {props.isAuthenticated && (
        <Drawer variant="permanent" open={menuOpen}>
          <Toolbar
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'flex-end',
              px: [1],
            }}
          >
            <IconButton onClick={toggleMenu}>
              <ChevronLeftIcon />
            </IconButton>
          </Toolbar>
          <Divider />
          <EntitiesMenu />
        </Drawer>
      )}
    </>
  );
};

export default Header;
