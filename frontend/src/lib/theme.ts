import { createTheme, type PaletteMode } from '@mui/material/styles'

export function buildTheme(mode: PaletteMode) {
  const dark = mode === 'dark'
  return createTheme({
    palette: {
      mode,
      primary: { main: '#1a73e8', light: '#4a9eff', dark: '#0d5dc7' },
      secondary: { main: '#34a853' },
      error: { main: '#ea4335' },
      warning: { main: '#fbbc04', contrastText: '#202124' },
      success: { main: '#34a853' },
      background: dark
        ? { default: '#0f1116', paper: '#1c1f26' }
        : { default: '#f0f2f5', paper: '#ffffff' },
      divider: dark ? '#2d3139' : '#e0e0e0',
      text: dark
        ? { primary: '#e3e3e3', secondary: '#9aa0a6' }
        : { primary: '#202124', secondary: '#5f6368' },
    },
    shape: { borderRadius: 7 },
    typography: {
      fontFamily: '"Roboto", "Helvetica Neue", Arial, sans-serif',
      fontSize: 13,
      button: { textTransform: 'none' as const, fontWeight: 500 },
    },
    components: {
      MuiCssBaseline: {
        styleOverrides: {
          'html, body, #root': { height: '100%', margin: 0, padding: 0 },
          '::-webkit-scrollbar': { width: 6, height: 6 },
          '::-webkit-scrollbar-track': { background: 'transparent' },
          '::-webkit-scrollbar-thumb': {
            borderRadius: 3,
            backgroundColor: dark ? '#3c4049' : '#c1c1c1',
          },
          '::-webkit-scrollbar-thumb:hover': {
            backgroundColor: dark ? '#5f6368' : '#9aa0a6',
          },
        },
      },
      MuiButton: {
        styleOverrides: {
          root: { textTransform: 'none', borderRadius: 5, fontWeight: 500 },
          // Match the height of size="small" OutlinedInput so all controls align
          sizeSmall: { height: 37, padding: '0 12px' },
        },
      },
      MuiIconButton: {
        styleOverrides: { root: { borderRadius: 5 } },
      },
      MuiTableCell: {
        styleOverrides: {
          root: {
            borderBottomColor: dark ? '#2d3139' : '#e0e0e0',
            padding: '9px 16px',
            fontSize: '0.8125rem',
          },
          head: {
            fontWeight: 600,
            fontSize: '0.7rem',
            letterSpacing: '0.06em',
            textTransform: 'uppercase' as const,
            backgroundColor: dark ? '#0f1116' : '#f0f2f5',
            color: dark ? '#9aa0a6' : '#5f6368',
          },
          stickyHeader: {
            backgroundColor: dark ? '#0f1116' : '#f0f2f5',
          },
        },
      },
      MuiChip: {
        styleOverrides: {
          root: { borderRadius: 5, fontSize: '0.75rem', fontWeight: 500 },
          sizeSmall: { height: 24 },
          labelSmall: { paddingLeft: 8, paddingRight: 8 },
        },
      },
      MuiDialog: {
        styleOverrides: { paper: { borderRadius: 9 } },
      },
      MuiOutlinedInput: {
        styleOverrides: { root: { borderRadius: 5 } },
      },
      MuiAppBar: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            backgroundColor: dark ? '#1c1f26' : '#ffffff',
            color: dark ? '#e3e3e3' : '#202124',
            boxShadow: 'none',
          },
        },
      },
      MuiPaper: {
        styleOverrides: { root: { backgroundImage: 'none' } },
      },
      MuiPopover: {
        styleOverrides: { paper: { borderRadius: 7 } },
      },
    },
  })
}
