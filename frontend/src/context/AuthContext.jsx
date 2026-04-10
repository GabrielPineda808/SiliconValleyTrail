import { useMemo } from 'react'
import { AuthContext } from './authContext'
import { useLocalStorage } from '../hooks/useLocalStorage'

export function AuthProvider({ children }) {
  const [token, setToken] = useLocalStorage('svt_jwt', '')

  const value = useMemo(
    () => ({
      token,
      setToken,
      logout: () => setToken(''),
      isAuthenticated: Boolean(token),
    }),
    [setToken, token],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
