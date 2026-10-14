import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { ApolloProvider } from '@apollo/client'
import { client } from './lib/apollo'
import { AppThemeProvider } from './lib/ThemeContext'
import App from './App'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ApolloProvider client={client}>
      <AppThemeProvider>
        <App />
      </AppThemeProvider>
    </ApolloProvider>
  </StrictMode>,
)
