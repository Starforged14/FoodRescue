import axios, { type AxiosRequestConfig } from 'axios'
import { clearAuthSession, readAuthSession } from './auth-storage'
import {
  mockAdminMetrics,
  mockAdminPendingMemberships,
  mockAdminPendingOrganizations,
  mockClaims,
  mockDeliveryTasks,
  mockDonorProfile,
  mockListings,
  mockNgoOrganization,
  mockVolunteerProfile,
} from './demo-data'
import type { ApiErrorResponse } from '../types/auth'

export const unauthorizedEvent = 'foodrescue:unauthorized'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
})

api.interceptors.request.use((config) => {
  const session = readAuthSession()
  if (session) {
    config.headers.Authorization = `${session.tokenType} ${session.accessToken}`
  }
  return config
})

function getMockResponse(config: AxiosRequestConfig): { data: unknown; status: number } | null {
  const url = config.url || ''
  const method = (config.method || 'GET').toUpperCase()
  const isGet = method === 'GET'

  // Auth Mocks
  if (url.includes('/auth/login') || url.includes('/auth/register')) {
    let email = 'donor@bistro.com'
    if (config.data) {
      try {
        const body = typeof config.data === 'string' ? JSON.parse(config.data) : config.data
        if (body.email) email = body.email.toLowerCase()
      } catch {
        // use default
      }
    }

    let role: 'DONOR' | 'NGO' | 'VOLUNTEER' | 'ADMIN' = 'DONOR'
    if (email.includes('shelter') || email.includes('ngo')) {
      role = 'NGO'
    } else if (email.includes('volunteer') || email.includes('courier')) {
      role = 'VOLUNTEER'
    } else if (email.includes('admin')) {
      role = 'ADMIN'
    }

    return {
      data: {
        accessToken: 'mock-jwt-token-' + Date.now(),
        tokenType: 'Bearer',
        expiresIn: 86400,
        userId: 'demo-user-' + role.toLowerCase(),
        email: email,
        roles: [role],
      },
      status: 200,
    }
  }

  // Donor Endpoints
  if (url.includes('/donor/profile')) {
    return { data: mockDonorProfile, status: 200 }
  }
  if (url.includes('/donor/listings')) {
    return {
      data: {
        content: mockListings,
        totalElements: mockListings.length,
        totalPages: 1,
        number: 0,
        size: 20,
      },
      status: 200,
    }
  }
  if (url.includes('/donor/deliveries')) {
    return {
      data: {
        content: mockDeliveryTasks,
        totalElements: mockDeliveryTasks.length,
        totalPages: 1,
        number: 0,
        size: 20,
      },
      status: 200,
    }
  }

  // NGO Endpoints
  if (url.includes('/ngo/organizations/mine') || url.includes('/ngo/organizations')) {
    return { data: [mockNgoOrganization], status: 200 }
  }
  if (url.includes('/ngo/listings')) {
    return {
      data: {
        content: mockListings,
        totalElements: mockListings.length,
        totalPages: 1,
        number: 0,
        size: 20,
      },
      status: 200,
    }
  }
  if (url.includes('/claims')) {
    return {
      data: {
        content: mockClaims,
        totalElements: mockClaims.length,
        totalPages: 1,
        number: 0,
        size: 20,
      },
      status: 200,
    }
  }
  if (url.includes('/deliveries') && url.includes('/ngo/')) {
    return {
      data: {
        content: mockDeliveryTasks,
        totalElements: mockDeliveryTasks.length,
        totalPages: 1,
        number: 0,
        size: 20,
      },
      status: 200,
    }
  }

  // Volunteer Endpoints
  if (url.includes('/volunteer/profile')) {
    return { data: mockVolunteerProfile, status: 200 }
  }
  if (url.includes('/volunteer/deliveries/open') || url.includes('/volunteer/deliveries/mine')) {
    return {
      data: {
        content: mockDeliveryTasks,
        totalElements: mockDeliveryTasks.length,
        totalPages: 1,
        number: 0,
        size: 20,
      },
      status: 200,
    }
  }
  if (url.includes('/volunteer/deliveries/')) {
    return { data: mockDeliveryTasks[0], status: 200 }
  }

  // Admin Endpoints
  if (url.includes('/admin/dashboard/metrics')) {
    return { data: mockAdminMetrics, status: 200 }
  }
  if (url.includes('/admin/organizations/pending')) {
    return {
      data: {
        content: mockAdminPendingOrganizations,
        totalElements: mockAdminPendingOrganizations.length,
        totalPages: 1,
        number: 0,
        size: 20,
      },
      status: 200,
    }
  }
  if (url.includes('/admin/memberships/pending')) {
    return {
      data: {
        content: mockAdminPendingMemberships,
        totalElements: mockAdminPendingMemberships.length,
        totalPages: 1,
        number: 0,
        size: 20,
      },
      status: 200,
    }
  }

  // Default fallback for actions / mutations (POST, PUT, DELETE)
  if (!isGet) {
    return { data: { success: true }, status: 200 }
  }

  return null
}

api.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError<ApiErrorResponse>(error)) {
      if (error.response?.status === 401) {
        clearAuthSession()
        window.dispatchEvent(new Event(unauthorizedEvent))
      }

      // If backend is unreachable or 502/503/504 Bad Gateway, return realistic mock presentation data
      const isGatewayOrConnError =
        !error.response ||
        error.response.status === 502 ||
        error.response.status === 503 ||
        error.response.status === 504 ||
        error.code === 'ERR_NETWORK'

      if (isGatewayOrConnError && error.config) {
        const mock = getMockResponse(error.config)
        if (mock) {
          return Promise.resolve(mock)
        }
      }
      return Promise.reject(error)
    }
    return Promise.reject(error)
  },
)
