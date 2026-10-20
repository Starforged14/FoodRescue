import axios from 'axios'
import type { ApiErrorResponse } from '../types/auth'

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const response = error.response
    if (!response) {
      return 'FoodRescue could not be reached. Check your connection and try again.'
    }

    const message = response.data?.message
    if (message) {
      return message
    }

    const details = response.data?.details
    if (details) {
      const firstDetail = Object.values(details)[0]
      if (firstDetail) {
        return firstDetail
      }
    }
  }

  if (error instanceof Error) {
    return error.message
  }

  return 'Something went wrong. Please try again.'
}
