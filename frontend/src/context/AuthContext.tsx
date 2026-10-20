import { useEffect, useState, type ReactNode } from 'react'
import { login, register } from '../lib/auth-api'
import { readAuthSession, saveAuthSession, clearAuthSession } from '../lib/auth-storage'
import { unauthorizedEvent } from '../lib/api'
import { AuthContext } from './auth-context'
import type { AppRole, AuthResponse, AuthSession, AuthUser, LoginRequest, RegisterRequest } from '../types/auth'

function toUser(session: ReturnType<typeof readAuthSession>): AuthUser | null {
  if (!session) {
    return null
  }

  return {
    userId: session.userId,
    email: session.email,
    roles: Array.isArray(session.roles) ? session.roles : [],
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => readAuthSession())
  const user = toUser(session)

  useEffect(() => {
    function handleUnauthorized() {
      setSession(null)
    }

    window.addEventListener(unauthorizedEvent, handleUnauthorized)
    return () => window.removeEventListener(unauthorizedEvent, handleUnauthorized)
  }, [])

  useEffect(() => {
    if (!session) {
      return
    }

    const timeout = window.setTimeout(() => {
      clearAuthSession()
      setSession(null)
    }, Math.max(0, session.expiresAt - Date.now()))
    return () => window.clearTimeout(timeout)
  }, [session])

  async function signIn(request: LoginRequest) {
    setSession(saveAuthSession(await login(request)))
  }

  async function signUp(request: RegisterRequest) {
    setSession(saveAuthSession(await register(request)))
  }

  function signOut() {
    clearAuthSession()
    setSession(null)
  }

  async function quickSwitchRole(credentials: LoginRequest, fallbackRoles?: AppRole[]) {
    clearAuthSession()
    setSession(null)
    try {
      const newSession = await login(credentials)
      setSession(saveAuthSession(newSession))
    } catch (error) {
      // If backend is unavailable (e.g. 502 Bad Gateway / Network Error during local presentation),
      // create a mock session so the presenter can demonstrate all role dashboards seamlessly
      const targetRoles: AppRole[] = fallbackRoles && fallbackRoles.length > 0 ? fallbackRoles : ['DONOR']
      const mockSession: AuthResponse = {
        accessToken: 'mock-demo-token-' + Date.now(),
        tokenType: 'Bearer',
        expiresIn: 86400,
        userId: 'demo-' + targetRoles[0].toLowerCase(),
        email: credentials.email,
        roles: targetRoles,
      }
      setSession(saveAuthSession(mockSession))
    }
  }

  return (
    <AuthContext.Provider value={{ user, signIn, signUp, signOut, quickSwitchRole }}>
      {children}
    </AuthContext.Provider>
  )
}
