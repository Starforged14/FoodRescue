export const registrationRoles = ['DONOR', 'NGO', 'VOLUNTEER'] as const

export type RegistrationRole = (typeof registrationRoles)[number]
export type AppRole = RegistrationRole | 'ADMIN'

export interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  userId: string
  email: string
  roles: AppRole[]
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest extends LoginRequest {
  firstName: string
  lastName: string
  phone?: string
  role: RegistrationRole
}

export interface ApiErrorResponse {
  message?: string
  details?: Record<string, string>
}

export type AuthUser = Pick<AuthResponse, 'userId' | 'email' | 'roles'>

export interface AuthSession extends AuthResponse {
  expiresAt: number
}
