import { Box, AppBar, Toolbar, Typography, IconButton, Tooltip } from '@mui/material'
import LightModeIcon from '@mui/icons-material/LightMode'
import DarkModeOutlinedIcon from '@mui/icons-material/DarkModeOutlined'
import { useThemeMode } from './lib/ThemeContext'
import CompareView from './views/CompareView'
import FAB from './components/FAB'

export default function App() {
  const { mode, toggle } = useThemeMode()

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100vh', overflow: 'hidden' }}>
      <AppBar position="static" elevation={0} sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Toolbar variant="dense" sx={{ minHeight: 56, px: 3 }}>
          <Typography
            variant="h6"
            sx={{ flexGrow: 1, fontWeight: 700, letterSpacing: '-0.5px', fontSize: '1rem' }}
          >
            Brickfolio
          </Typography>
          <Tooltip title={`Switch to ${mode === 'dark' ? 'light' : 'dark'} mode`}>
            <IconButton onClick={toggle} size="small" color="inherit">
              {mode === 'dark' ? (
                <LightModeIcon fontSize="small" />
              ) : (
                <DarkModeOutlinedIcon fontSize="small" />
              )}
            </IconButton>
          </Tooltip>
        </Toolbar>
      </AppBar>

      <Box sx={{ flex: 1, overflow: 'hidden' }}>
        <CompareView />
      </Box>

      <FAB />
    </Box>
  )
}
