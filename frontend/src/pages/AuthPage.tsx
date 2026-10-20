import { useState, type FormEvent, type ReactNode } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { Brand } from '../components/Brand'
import { useAuth } from '../context/useAuth'
import { getErrorMessage } from '../lib/error-message'
import { registrationRoles, type RegistrationRole } from '../types/auth'

interface AuthPageProps {
  mode: 'login' | 'register'
}

export function AuthPage({ mode }: AuthPageProps) {
  const isRegister = mode === 'register'
  const { user, signIn, signUp } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [phone, setPhone] = useState('')
  const [role, setRole] = useState<RegistrationRole>('DONOR')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (user) {
    return <Navigate replace to="/dashboard" />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    if (isRegister && password !== confirmPassword) {
      setError('Your passwords do not match.')
      return
    }
    if (new TextEncoder().encode(password).length > 72) {
      setError('Password must not exceed 72 UTF-8 bytes.')
      return
    }

    setIsSubmitting(true)
    try {
      if (isRegister) {
        await signUp({ email, password, firstName, lastName, phone: phone || undefined, role })
      } else {
        await signIn({ email, password })
      }

      const requestedPath = (location.state as { from?: unknown } | null)?.from
      const destination =
        typeof requestedPath === 'string' && requestedPath.startsWith('/')
          ? requestedPath
          : '/dashboard'
      navigate(destination, { replace: true })
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="mx-auto grid max-w-7xl flex-1 items-center gap-12 px-5 py-12 sm:px-8 lg:grid-cols-[0.9fr_1.1fr] lg:gap-20 lg:py-20">
      <section className="hidden max-w-lg lg:block">
        <div className="inline-flex items-center gap-2 rounded-full bg-[#e7efe2] px-3.5 py-2 text-xs font-bold uppercase tracking-[0.13em] text-[#4f7650]">
          <span className="size-2 rounded-full bg-[#79a76e]" />
          Good food, shared locally
        </div>
        <h1 className="mt-6 text-5xl font-semibold leading-[1.08] tracking-[-0.055em] text-[#1d2b24]">
          {isRegister ? 'Be part of a better food story.' : 'Welcome back to the table.'}
        </h1>
        <p className="mt-6 max-w-md text-base leading-7 text-[#68766c]">
          {isRegister
            ? 'Create an account as a donor, community organization, or volunteer and help make local food sharing easier.'
            : 'Sign in to continue coordinating surplus food with your local community.'}
        </p>
        <div className="mt-10 grid gap-3">
          {[
            ['01', 'A clear path from surplus to shared'],
            ['02', 'Local organizations and volunteers'],
            ['03', 'Food handoffs with more coordination'],
          ].map(([number, label]) => (
            <div className="flex items-center gap-3 text-sm font-medium text-[#526358]" key={number}>
              <span className="flex size-8 items-center justify-center rounded-xl bg-white text-[11px] font-bold text-[#64805d] shadow-sm">
                {number}
              </span>
              {label}
            </div>
          ))}
        </div>
      </section>

      <section className="mx-auto w-full max-w-xl rounded-[2rem] border border-[#e4e9e1] bg-white p-6 shadow-[0_24px_70px_-40px_rgba(31,55,38,0.28)] sm:p-10">
        <div className="mb-8 lg:hidden">
          <Brand />
        </div>
        <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#64805d]">
          {isRegister ? 'Get started' : 'Good to see you'}
        </p>
        <h2 className="mt-2 text-3xl font-semibold tracking-[-0.04em] text-[#203328]">
          {isRegister ? 'Create your account' : 'Log in'}
        </h2>
        <p className="mt-2 text-sm leading-6 text-[#718075]">
          {isRegister
            ? 'Your account is the first step toward a better local handoff.'
            : 'Enter your account details to continue.'}
        </p>

        <form className="mt-8 space-y-4" onSubmit={handleSubmit}>
          {isRegister && (
            <div className="grid gap-4 sm:grid-cols-2">
              <FormField label="First name">
                <input
                  autoComplete="given-name"
                  className={inputClass}
                  maxLength={100}
                  onChange={(event) => setFirstName(event.target.value)}
                  required
                  value={firstName}
                />
              </FormField>
              <FormField label="Last name">
                <input
                  autoComplete="family-name"
                  className={inputClass}
                  maxLength={100}
                  onChange={(event) => setLastName(event.target.value)}
                  required
                  value={lastName}
                />
              </FormField>
            </div>
          )}

          <FormField label="Email address">
            <input
              autoComplete="email"
              className={inputClass}
              maxLength={320}
              onChange={(event) => setEmail(event.target.value)}
              required
              type="email"
              value={email}
            />
          </FormField>

          {isRegister && (
            <>
              <FormField label="I am joining as">
                <select
                  className={inputClass}
                  onChange={(event) => setRole(event.target.value as RegistrationRole)}
                  value={role}
                >
                  {registrationRoles.map((registrationRole) => (
                    <option key={registrationRole} value={registrationRole}>
                      {roleLabels[registrationRole]}
                    </option>
                  ))}
                </select>
              </FormField>
              <FormField label="Phone number (optional)">
                <input
                  autoComplete="tel"
                  className={inputClass}
                  maxLength={30}
                  onChange={(event) => setPhone(event.target.value)}
                  type="tel"
                  value={phone}
                />
              </FormField>
            </>
          )}

          <FormField label="Password">
            <input
              autoComplete={isRegister ? 'new-password' : 'current-password'}
              className={inputClass}
              maxLength={72}
              minLength={isRegister ? 12 : 1}
              onChange={(event) => setPassword(event.target.value)}
              required
              type="password"
              value={password}
            />
            {isRegister && (
              <span className="mt-1.5 block text-xs text-[#839087]">
                Use at least 12 characters.
              </span>
            )}
          </FormField>

          {isRegister && (
            <FormField label="Confirm password">
              <input
                autoComplete="new-password"
                className={inputClass}
                maxLength={72}
                minLength={12}
                onChange={(event) => setConfirmPassword(event.target.value)}
                required
                type="password"
                value={confirmPassword}
              />
            </FormField>
          )}

          {error && (
            <div
              aria-live="polite"
              className="rounded-2xl border border-[#f0c9c2] bg-[#fff5f2] px-4 py-3 text-sm leading-5 text-[#a33d32]"
              role="alert"
            >
              {error}
            </div>
          )}

          <button
            className="flex w-full items-center justify-center gap-2 rounded-full bg-[#244c37] px-5 py-3.5 text-sm font-bold text-white shadow-sm transition hover:bg-[#193c2a] disabled:cursor-not-allowed disabled:opacity-60"
            disabled={isSubmitting}
            type="submit"
          >
            {isSubmitting ? 'Please wait...' : isRegister ? 'Create account' : 'Log in'}
            {!isSubmitting && <span aria-hidden="true">-&gt;</span>}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-[#718075]">
          {isRegister ? 'Already have an account?' : 'New to FoodRescue?'}{' '}
          <Link
            className="font-bold text-[#3c7048] underline-offset-4 hover:underline"
            to={isRegister ? '/login' : '/register'}
          >
            {isRegister ? 'Log in' : 'Create an account'}
          </Link>
        </p>
        <p className="mt-5 text-center text-xs leading-5 text-[#89948b]">
          By continuing, you agree to use FoodRescue responsibly and follow safe
          food handling practices.
        </p>
      </section>
    </main>
  )
}

const inputClass =
  'mt-1.5 w-full rounded-2xl border border-[#dfe6dc] bg-[#fcfdfb] px-4 py-3 text-sm text-[#24372a] outline-none transition placeholder:text-[#a0aaa1] focus:border-[#71936c] focus:ring-4 focus:ring-[#e8f0e5]'

const roleLabels: Record<RegistrationRole, string> = {
  DONOR: 'Food donor',
  NGO: 'Community organization',
  VOLUNTEER: 'Delivery volunteer',
}

function FormField({ children, label }: { children: ReactNode; label: string }) {
  return (
    <label className="block text-sm font-semibold text-[#34473a]">
      {label}
      {children}
    </label>
  )
}
