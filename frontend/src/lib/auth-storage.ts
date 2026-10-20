import type { AuthResponse, AuthSession } from '../types/auth'

const sessionKey = 'foodrescue.auth'
const supportedRoles = new Set(['DONOR', 'NGO', 'VOLUNTEER', 'ADMIN'])

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function isAuthSession(value: unknown): value is AuthSession {
  return (
    isRecord(value) &&
    typeof value.accessToken === 'string' &&
    typeof value.tokenType === 'string' &&
    typeof value.expiresIn === 'number' &&
    typeof value.expiresAt === 'number' &&
    typeof value.userId === 'string' &&
    typeof value.email === 'string' &&
    Array.isArray(value.roles) &&
    value.roles.every((role) => typeof role === 'string' && supportedRoles.has(role))
  )
}

export function saveAuthSession(response: AuthResponse): AuthSession {
  const session: AuthSession = {
    ...response,
    expiresAt: Date.now() + response.expiresIn * 1000,
  }
  window.localStorage.setItem(sessionKey, JSON.stringify(session))
  return session
}

export function readAuthSession(): AuthSession | null {
  const serialized = window.localStorage.getItem(sessionKey)
  if (!serialized) {
    return null
  }

  let parsed: unknown
  try {
    parsed = JSON.parse(serialized)
  } catch (error) {
    if (!(error instanceof SyntaxError)) {
      throw error
    }
    window.localStorage.removeItem(sessionKey)
    return null
  }

  if (!isAuthSession(parsed) || parsed.expiresAt <= Date.now()) {
    window.localStorage.removeItem(sessionKey)
    return null
  }

  return parsed
}

export function clearAuthSession(): void {
  window.localStorage.removeItem(sessionKey)
}
