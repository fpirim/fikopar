import React from 'react';
import { languages, locales } from 'app/config/translation';
import { IconButton, Menu, MenuItem } from '@mui/material';
import LanguageIcon from '@mui/icons-material/Language';
import { setLocale } from 'app/shared/reducers/locale';
import { useAppDispatch } from 'app/config/store';
import { Storage } from 'react-jhipster';

export const LocaleMenu = ({ currentLocale }: { currentLocale: string }) => {
  const dispatch = useAppDispatch();
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleChangeLocale = (locale: string) => {
    console.log('langKey', locale);
    Storage.session.set('locale', locale);
    dispatch(setLocale(locale));
    setAnchorEl(null);
  };

  return Object.keys(languages).length > 1 ? (
    <>
      <IconButton
        id="locale-button"
        aria-controls={open ? 'locale-menu' : undefined}
        aria-haspopup="true"
        aria-expanded={open ? 'true' : undefined}
        color="inherit"
        onClick={handleClick}
      >
        <LanguageIcon />
      </IconButton>
      <Menu
        id="locale-menu"
        aria-labelledby="locale-button"
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
        {locales.map(locale => (
          <MenuItem key={locale} onClick={() => handleChangeLocale(locale)}>
            {languages[locale].name}
          </MenuItem>
        ))}
      </Menu>
    </>
  ) : null;
};
