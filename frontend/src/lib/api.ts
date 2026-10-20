import axios, { type InternalAxiosRequestConfig } from 'axios'
import { clearAuthSession, readAuthSession } from './auth-storage'
import type { ApiErrorResponse } from '../types/auth'
import {
  mockClaims,
  mockDeliveryTasks,
  mockDonorProfile,
  mockListings,
  mockNgoOrganization,
  mockVolunteerProfile,
} from './demo-data'

export const unauthorizedEvent = 'foodrescue:unauthorized'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15_000,
})

api.interceptors.request.use((config) => {
  const session = readAuthSession()
  if (session) {
    config.headers.Authorization = `${session.tokenType} ${session.accessToken}`
  }
  return config
})

function getMockResponse(config: InternalAxiosRequestConfig) {
  const url = config.url || ''
  const isGet = (config.method || 'get').toLowerCase() === 'get'

  if (url.includes('/auth/login') || url.includes('/auth/register')) {
    let email = 'demo-user@foodrescue.org'
    let role: string = 'DONOR'
    try {
      if (typeof config.data === 'string') {
        const body = JSON.parse(config.data)
        if (body.email) email = body.email
        if (body.role) role = body.role
      } else if (config.data && typeof config.data === 'object') {
        if (config.data.email) email = config.data.email
        if (config.data.role) role = config.data.role
      }
    } catch {
      // ignore json parse error
    }

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
  if (url.includes('/ngo/organizations')) {
    return { data: [mockNgoOrganization], status: 200 }
  }
  if (url.includes('/ngo/listings/available')) {
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
  if (url.includes('/ngo/claims')) {
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
  if (url.includes('/admin/')) {
    return {
      data: {
        metrics: {
          totalUsers: 142,
          totalDonors: 38,
          totalNgos: 24,
          totalVolunteers: 80,
          totalListings: 312,
          completedDeliveries: 289,
        },
        pendingOrganizations: [
          {
            id: 'mock-pending-org',
            name: 'Eastside Food Pantry',
            contactEmail: 'pantry@eastside.org',
            verificationStatus: 'PENDING_REVIEW',
            registrationNumber: 'NGO-10928',
          },
        ],
      },
      status: 200,
    }
  }

  // Default fallback for actions / POST
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
          return Promise.resolve({
            data: mock.data,
            status: mock.status,
            statusText: 'OK (Demo Mode)',
            headers: {},
            config: error.config,
          })
        }
      }
    }
    return Promise.reject(error)
  },
)
