import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import { ThemeProvider, CssBaseline } from '@mui/material'
import { buildTheme } from './theme'

type Mode = 'dark' | 'light'

const Ctx = createContext<{ mode: Mode; toggle: () => void }>({
  mode: 'dark',
  toggle: () => {},
})

export const useThemeMode = () => useContext(Ctx)

export function AppThemeProvider({ children }: { children: ReactNode }) {
  const [mode, setMode] = useState<Mode>(
    () => (localStorage.getItem('colorMode') as Mode | null) ?? 'dark',
  )

  function toggle() {
    setMode((prev) => {
      const next: Mode = prev === 'dark' ? 'light' : 'dark'
      localStorage.setItem('colorMode', next)
      return next
    })
  }

  const theme = useMemo(() => buildTheme(mode), [mode])

  return (
    <Ctx.Provider value={{ mode, toggle }}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        {children}
      </ThemeProvider>
    </Ctx.Provider>
  )
}
