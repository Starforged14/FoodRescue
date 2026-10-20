import { Component, type ErrorInfo, type ReactNode } from 'react'

interface ErrorBoundaryProps {
  children: ReactNode
}

interface ErrorBoundaryState {
  hasError: boolean
  error: Error | null
}

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  public override state: ErrorBoundaryState = {
    hasError: false,
    error: null,
  }

  public static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error }
  }

  public override componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Uncaught error in FoodRescue UI:', error, errorInfo)
  }

  public override render() {
    if (this.state.hasError) {
      return (
        <div className="flex min-h-[60vh] flex-col items-center justify-center px-4 text-center">
          <div className="mx-auto max-w-md rounded-2xl border border-rose-200 bg-rose-50/70 p-6 shadow-sm">
            <span className="text-3xl">⚠️</span>
            <h2 className="mt-3 text-lg font-bold text-rose-950">Workspace Rendering Notice</h2>
            <p className="mt-2 text-xs text-rose-800">
              {this.state.error?.message || 'An unexpected rendering issue occurred in this view.'}
            </p>
            <div className="mt-5 flex justify-center gap-3">
              <button
                type="button"
                onClick={() => {
                  this.setState({ hasError: false, error: null })
                  window.location.reload()
                }}
                className="rounded-xl bg-[#244c37] px-4 py-2 text-xs font-semibold text-white shadow-sm hover:bg-[#193c2a]"
              >
                Reload Page
              </button>
              <button
                type="button"
                onClick={() => {
                  this.setState({ hasError: false, error: null })
                  window.location.href = '/'
                }}
                className="rounded-xl border border-gray-300 bg-white px-4 py-2 text-xs font-semibold text-gray-700 hover:bg-gray-50"
              >
                Go to Home
              </button>
            </div>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}
