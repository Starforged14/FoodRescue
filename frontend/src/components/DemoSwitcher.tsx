import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import type { AppRole, LoginRequest } from '../types/auth'

interface DemoAccount {
  role: AppRole
  title: string
  label: string
  emoji: string
  email: string
  password: string
  badgeColor: string
}

const DEMO_ACCOUNTS: DemoAccount[] = [
  {
    role: 'DONOR',
    title: 'Donor',
    label: 'Green Table Bistro',
    emoji: '🍳',
    email: import.meta.env.VITE_DEMO_DONOR_EMAIL || 'donor@bistro.com',
    password: import.meta.env.VITE_DEMO_DONOR_PASSWORD || 'Password123!',
    badgeColor: 'bg-amber-100 text-amber-800 border-amber-300',
  },
  {
    role: 'NGO',
    title: 'NGO',
    label: 'Hope Shelter Org',
    emoji: '🏛️',
    email: import.meta.env.VITE_DEMO_NGO_EMAIL || 'coordinator@shelter.org',
    password: import.meta.env.VITE_DEMO_NGO_PASSWORD || 'Password123!',
    badgeColor: 'bg-blue-100 text-blue-800 border-blue-300',
  },
  {
    role: 'VOLUNTEER',
    title: 'Volunteer',
    label: 'Courier FastTrack',
    emoji: '🚚',
    email: import.meta.env.VITE_DEMO_VOLUNTEER_EMAIL || 'volunteer@rescue.org',
    password: import.meta.env.VITE_DEMO_VOLUNTEER_PASSWORD || 'Password123!',
    badgeColor: 'bg-emerald-100 text-emerald-800 border-emerald-300',
  },
  {
    role: 'ADMIN',
    title: 'Admin',
    label: 'Platform Overseer',
    emoji: '🛡️',
    email: import.meta.env.VITE_DEMO_ADMIN_EMAIL || 'admin@foodrescue.org',
    password: import.meta.env.VITE_DEMO_ADMIN_PASSWORD || 'Password123!',
    badgeColor: 'bg-purple-100 text-purple-800 border-purple-300',
  },
]

export function DemoSwitcher() {
  const { user, quickSwitchRole } = useAuth()
  const navigate = useNavigate()
  const [isOpen, setIsOpen] = useState(false)
  const [loadingRole, setLoadingRole] = useState<AppRole | null>(null)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)
  const [showHelper, setShowHelper] = useState(false)
  
  // Custom mock active OTP store for live demo demonstration
  const [pickupCode] = useState(() => Math.floor(100000 + Math.random() * 900000).toString())
  const [deliveryCode] = useState(() => Math.floor(100000 + Math.random() * 900000).toString())

  // Check if dev mode is enabled; defaults to true in development mode or if VITE_ENABLE_DEV_MODE is true
  const isDevMode =
    import.meta.env.DEV ||
    import.meta.env.VITE_ENABLE_DEV_MODE === 'true' ||
    import.meta.env.VITE_ENABLE_DEV_MODE === undefined

  if (!isDevMode) {
    return null
  }

  async function handleSwitch(account: DemoAccount) {
    setErrorMsg(null)
    setLoadingRole(account.role)
    try {
      const credentials: LoginRequest = {
        email: account.email,
        password: account.password,
      }
      await quickSwitchRole(credentials, [account.role])
      navigate('/dashboard', { replace: true })
    } catch (err: unknown) {
      const message =
        err instanceof Error
          ? err.message
          : 'Unable to switch role.'
      setErrorMsg(message)
    } finally {
      setLoadingRole(null)
    }
  }

  const activeRoleBadge = user?.roles[0] ?? 'GUEST'

  return (
    <aside aria-label="Dev Mode Controls" className="fixed bottom-4 right-4 z-50 flex flex-col items-end">
      {/* Helper Panel Modal / Card */}
      {isOpen && (
        <div className="mb-3 w-84 rounded-2xl border border-gray-200/80 bg-white/95 p-4 shadow-2xl backdrop-blur-md transition-all sm:w-96">
          <div className="flex items-center justify-between border-b border-gray-100 pb-3">
            <div className="flex items-center gap-2">
              <span className="flex h-2.5 w-2.5 rounded-full bg-emerald-500 animate-pulse" />
              <h2 className="text-sm font-bold text-gray-900 tracking-tight">Presentation & Dev Mode</h2>
            </div>
            <button
              onClick={() => setIsOpen(false)}
              className="rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition"
              title="Close Panel"
              type="button"
            >
              ✕
            </button>
          </div>

          {/* Active Session Info */}
          <div className="mt-3 rounded-xl bg-gray-50/90 p-3 border border-gray-100 text-xs">
            <div className="flex justify-between items-center mb-1">
              <span className="font-semibold text-gray-500 uppercase tracking-wider text-[10px]">Active Session</span>
              <span className="font-mono text-[11px] px-2 py-0.5 rounded-full bg-white border border-gray-200 text-gray-700 font-bold">
                {activeRoleBadge}
              </span>
            </div>
            <p className="truncate font-medium text-gray-800">
              {user ? user.email : 'Not logged in (Guest)'}
            </p>
          </div>

          {/* Error Notice */}
          {errorMsg && (
            <div className="mt-2.5 rounded-lg bg-red-50 p-2.5 text-xs text-red-600 border border-red-200">
              ⚠️ {errorMsg}
            </div>
          )}

          {/* Quick Switch Role Buttons */}
          <div className="mt-3.5">
            <p className="text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-2">
              Quick Switch Role
            </p>
            <div className="grid grid-cols-2 gap-2">
              {DEMO_ACCOUNTS.map((account) => {
                const isCurrent = user?.roles.includes(account.role)
                const isLoading = loadingRole === account.role

                return (
                  <button
                    key={account.role}
                    type="button"
                    onClick={() => handleSwitch(account)}
                    disabled={Boolean(loadingRole)}
                    className={`flex items-center gap-2.5 rounded-xl border p-2 text-left text-xs font-semibold transition-all ${
                      isCurrent
                        ? 'border-emerald-600 bg-emerald-50/70 text-emerald-950 ring-1 ring-emerald-500'
                        : 'border-gray-200 bg-white hover:border-gray-300 hover:bg-gray-50 text-gray-800'
                    } ${Boolean(loadingRole) ? 'opacity-60 cursor-not-allowed' : 'active:scale-98'}`}
                  >
                    <span className="text-lg">{account.emoji}</span>
                    <div className="min-w-0 flex-1">
                      <div className="font-bold flex items-center justify-between">
                        <span>{account.title}</span>
                        {isCurrent && <span className="text-[9px] text-emerald-700">● Active</span>}
                      </div>
                      <div className="truncate text-[10px] text-gray-500">{account.label}</div>
                    </div>
                    {isLoading && <span className="h-3 w-3 animate-spin rounded-full border-2 border-emerald-600 border-t-transparent" />}
                  </button>
                )
              })}
            </div>
          </div>

          {/* Verification Code / Demo Helper Section */}
          <div className="mt-3.5 border-t border-gray-100 pt-3">
            <button
              type="button"
              onClick={() => setShowHelper(!showHelper)}
              className="flex w-full items-center justify-between text-xs font-semibold text-gray-600 hover:text-gray-900 transition"
            >
              <span className="flex items-center gap-1.5">
                <span>🔑</span> Live Verification Codes (Demo)
              </span>
              <span>{showHelper ? '▲' : '▼'}</span>
            </button>

            {showHelper && (
              <div className="mt-2 space-y-2 rounded-xl bg-amber-50/70 p-3 border border-amber-200/70 text-xs">
                <div className="flex items-center justify-between">
                  <span className="text-amber-900 font-medium">Pickup OTP (Donor ➔ Volunteer):</span>
                  <span className="font-mono font-bold tracking-widest text-amber-950 bg-white px-2 py-0.5 rounded border border-amber-300">
                    {pickupCode}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-amber-900 font-medium">Delivery OTP (Volunteer ➔ NGO):</span>
                  <span className="font-mono font-bold tracking-widest text-amber-950 bg-white px-2 py-0.5 rounded border border-amber-300">
                    {deliveryCode}
                  </span>
                </div>
                <p className="text-[10px] text-amber-700 leading-tight">
                  Use these ready codes during live walkthroughs to verify pickup and delivery handovers seamlessly.
                </p>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Floating Trigger Button */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        type="button"
        className="flex items-center gap-2.5 rounded-full border border-gray-300 bg-white/95 px-4 py-2.5 text-xs font-bold text-gray-800 shadow-xl backdrop-blur-md transition-all hover:bg-gray-50 hover:shadow-2xl active:scale-95"
      >
        <span className="flex h-2 w-2 rounded-full bg-emerald-500 animate-ping" />
        <span className="text-sm">⚡</span>
        <span>Dev Switcher</span>
        <span className="rounded-full bg-gray-100 px-2 py-0.5 text-[10px] font-semibold text-gray-600 uppercase tracking-wider">
          {activeRoleBadge}
        </span>
      </button>
    </aside>
  )
}
