import { createContext, useContext, useState, ReactNode } from 'react'
import { login as apiLogin, logout as apiLogout } from '../api/auth'
import type { LoginRequest } from '../api/types'

interface AuthContextValue {
  currentEmail: string | null
  isAuthenticated: boolean
  login: (req: LoginRequest) => Promise<void>
  loginWithToken: (token: string, email: string) => void
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

function decodeEmail(token: string): string | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.sub ?? null
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [currentEmail, setCurrentEmail] = useState<string | null>(() => {
    const token = localStorage.getItem('jwt')
    return token ? decodeEmail(token) : null
  })

  const login = async (req: LoginRequest) => {
    const { token, email } = await apiLogin(req)
    localStorage.setItem('jwt', token)
    setCurrentEmail(email)
  }

  // Used by registration flow: token already obtained, no need for a second API call
  const loginWithToken = (token: string, email: string) => {
    localStorage.setItem('jwt', token)
    setCurrentEmail(email)
  }

  const logout = async () => {
    await apiLogout()
    localStorage.removeItem('jwt')
    setCurrentEmail(null)
  }

  return (
    <AuthContext.Provider value={{ currentEmail, isAuthenticated: currentEmail !== null, login, loginWithToken, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
