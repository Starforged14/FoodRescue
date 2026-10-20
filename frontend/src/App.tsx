import { Link, Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from './context/useAuth'
import { Brand } from './components/Brand'
import { DemoSwitcher } from './components/DemoSwitcher'
import { AuthPage } from './pages/AuthPage'
import { DashboardPage } from './pages/DashboardPage'
import { HomePage } from './pages/HomePage'

function RequireAuth({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const location = useLocation()

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return children
}

function SiteLayout() {
  const { user, signOut } = useAuth()
  const navigate = useNavigate()

  function handleSignOut() {
    signOut()
    navigate('/', { replace: true })
  }

  return (
    <div className="min-h-screen bg-[#f8f8f4] text-[#1d2b24]">
      <header className="relative z-10 border-b border-[#e4e9e1] bg-[#f8f8f4]/90 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-5 py-4 sm:px-8">
          <Brand />
          <nav className="flex items-center gap-3">
            {user ? (
              <>
                <Link
                  className="hidden text-sm font-semibold text-[#536258] transition hover:text-[#1d2b24] sm:inline"
                  to="/dashboard"
                >
                  My workspace
                </Link>
                <button
                  className="rounded-full border border-[#dce4da] px-4 py-2 text-sm font-semibold transition hover:border-[#9eae9d] hover:bg-white"
                  onClick={handleSignOut}
                  type="button"
                >
                  Sign out
                </button>
              </>
            ) : (
              <>
                <Link
                  className="rounded-full px-4 py-2 text-sm font-semibold text-[#536258] transition hover:bg-white hover:text-[#1d2b24]"
                  to="/login"
                >
                  Log in
                </Link>
                <Link
                  className="rounded-full bg-[#244c37] px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-[#193c2a]"
                  to="/register"
                >
                  Create an account
                </Link>
              </>
            )}
          </nav>
        </div>
      </header>

      <Routes>
        <Route element={<HomePage />} path="/" />
        <Route element={<AuthPage mode="login" />} path="/login" />
        <Route element={<AuthPage mode="register" />} path="/register" />
        <Route
          element={
            <RequireAuth>
              <DashboardPage />
            </RequireAuth>
          }
          path="/dashboard"
        />
        <Route element={<Navigate replace to="/" />} path="*" />
      </Routes>

      <DemoSwitcher />

      <footer className="border-t border-[#e4e9e1] px-5 py-6 text-center text-xs text-[#718075]">
        FoodRescue connects surplus food with the communities who need it.
      </footer>
    </div>
  )
}

export default function App() {
  return <SiteLayout />
}
