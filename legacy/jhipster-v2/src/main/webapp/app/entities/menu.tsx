import React from 'react';
import { translate } from 'react-jhipster';
import { Box, List, ListItemButton, ListItemIcon, ListItemText } from '@mui/material';
import { Link } from 'react-router-dom';
import PeopleIcon from '@mui/icons-material/People';
import BookmarksIcon from '@mui/icons-material/Bookmarks';
import BookmarkIcon from '@mui/icons-material/Bookmark';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import ReceiptIcon from '@mui/icons-material/Receipt';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import PaymentIcon from '@mui/icons-material/Payment';
import RequestQuoteIcon from '@mui/icons-material/RequestQuote';

const EntitiesMenu = () => {
  return (
    <Box
      sx={{
        height: 'calc(100vh - 4.1em)',
        display: 'flex',
        overflowX: 'hidden',
        overflowY: 'auto',
      }}
    >
      <List component="nav" sx={{ flexGrow: 1 }}>
        <ListItemButton component={Link} to="/katalog">
          <ListItemIcon>
            <BookmarksIcon />
          </ListItemIcon>
          <ListItemText primary={translate('global.menu.entities.katalog')} />
        </ListItemButton>
        <ListItemButton component={Link} to="/stok">
          <ListItemIcon>
            <BookmarkIcon />
          </ListItemIcon>
          <ListItemText primary={translate('global.menu.entities.stok')} />
        </ListItemButton>
        <ListItemButton component={Link} to="/caritoplam">
          <ListItemIcon>
            <AccountBalanceIcon />
          </ListItemIcon>
          <ListItemText primary={translate('global.menu.entities.cariToplam')} />
        </ListItemButton>
        <ListItemButton component={Link} to="/cari">
          <ListItemIcon>
            <AccountBalanceIcon />
          </ListItemIcon>
          <ListItemText primary={translate('global.menu.entities.cari')} />
        </ListItemButton>
        <ListItemButton component={Link} to="/sicil">
          <ListItemIcon>
            <PeopleIcon />
          </ListItemIcon>
          <ListItemText primary={translate('global.menu.entities.sicil')} />
        </ListItemButton>
        <ListItemButton component={Link} to="/hareket-sicil">
          <ListItemIcon>
            <ReceiptIcon />
          </ListItemIcon>
          <ListItemText primary={translate('global.menu.entities.hareketSicil')} />
        </ListItemButton>
        {/*<ListItemButton component={Link} to="/hareket-detay">*/}
        {/*  <ListItemText primary={translate('global.menu.entities.hareketDetay')}/>*/}
        {/*</ListItemButton>*/}
        <ListItemButton component={Link} to="/cari-devir">
          <ListItemIcon>
            <AccountBalanceWalletIcon />
          </ListItemIcon>
          <ListItemText primary={translate('global.menu.entities.cariDevir')} />
        </ListItemButton>
        <ListItemButton component={Link} to="/cek-senet">
          <ListItemIcon>
            <RequestQuoteIcon />
          </ListItemIcon>
          <ListItemText primary={translate('global.menu.entities.cekSenet')} />
        </ListItemButton>
        {/*<ListItemButton component={Link} to="/cek-senet-detay">*/}
        {/*  <ListItemText primary={translate('global.menu.entities.cekSenetDetay')}/>*/}
        {/*</ListItemButton>*/}
        <ListItemButton component={Link} to="/makbuz">
          <ListItemIcon>
            <PaymentIcon />
          </ListItemIcon>
          <ListItemText primary={translate('global.menu.entities.makbuz')} />
        </ListItemButton>
      </List>
    </Box>
  );
};

export default EntitiesMenu;
