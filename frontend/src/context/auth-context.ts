import { createContext } from 'react'
import type { AppRole, AuthUser, LoginRequest, RegisterRequest } from '../types/auth'

export interface AuthContextValue {
  user: AuthUser | null
  signIn: (request: LoginRequest) => Promise<void>
  signUp: (request: RegisterRequest) => Promise<void>
  signOut: () => void
  quickSwitchRole: (credentials: LoginRequest, fallbackRoles?: AppRole[]) => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)
