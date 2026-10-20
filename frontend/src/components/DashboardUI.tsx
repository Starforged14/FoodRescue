import type { ReactNode } from 'react'

const statusStyles: Record<string, string> = {
  ACTIVE: 'bg-[#e5f1e3] text-[#3f7048]',
  AVAILABLE: 'bg-[#e5f1e3] text-[#3f7048]',
  ASSIGNED: 'bg-[#e7edf6] text-[#4c668e]',
  CLAIMED: 'bg-[#f5eddc] text-[#8a6a2e]',
  COMPLETED: 'bg-[#e5f1e3] text-[#3f7048]',
  DELIVERED: 'bg-[#e5f1e3] text-[#3f7048]',
  DRAFT: 'bg-[#edf0ed] text-[#647268]',
  FAILED: 'bg-[#fae7e3] text-[#a54c40]',
  CANCELLED: 'bg-[#edf0ed] text-[#647268]',
  EXPIRED: 'bg-[#f5e9e5] text-[#9a6153]',
  OPEN: 'bg-[#e5f1e3] text-[#3f7048]',
  PENDING: 'bg-[#fff0d9] text-[#89622b]',
  PENDING_REVIEW: 'bg-[#fff0d9] text-[#89622b]',
  PICKED_UP: 'bg-[#e7edf6] text-[#4c668e]',
  REJECTED: 'bg-[#fae7e3] text-[#a54c40]',
  SUSPENDED: 'bg-[#fae7e3] text-[#a54c40]',
  UNAVAILABLE: 'bg-[#edf0ed] text-[#647268]',
  VERIFIED: 'bg-[#e5f1e3] text-[#3f7048]',
  WITHDRAWN: 'bg-[#edf0ed] text-[#647268]',
}

export function DashboardTitle({
  eyebrow,
  title,
  description,
}: {
  eyebrow: string
  title: string
  description: string
}) {
  return (
    <div className="mb-8">
      <p className="text-xs font-bold uppercase tracking-[0.16em] text-[#64805d]">{eyebrow}</p>
      <h1 className="mt-3 text-3xl font-semibold tracking-[-0.045em] text-[#203328] sm:text-4xl">
        {title}
      </h1>
      <p className="mt-3 max-w-3xl text-sm leading-6 text-[#718075]">{description}</p>
    </div>
  )
}

export function Panel({
  children,
  className = '',
  title,
  aside,
}: {
  children: ReactNode
  className?: string
  title: string
  aside?: ReactNode
}) {
  return (
    <section className={`rounded-3xl border border-[#e4e9e1] bg-white p-5 shadow-sm sm:p-6 ${className}`}>
      <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-lg font-semibold text-[#263b2c]">{title}</h2>
        {aside}
      </div>
      {children}
    </section>
  )
}

export function Feedback({
  error,
  message,
  onDismiss,
}: {
  error?: string
  message?: string
  onDismiss?: () => void
}) {
  const text = error || message
  if (!text) {
    return null
  }

  return (
    <div
      aria-live="polite"
      className={`mb-5 flex items-start justify-between gap-4 rounded-2xl border px-4 py-3 text-sm leading-5 ${
        error
          ? 'border-[#f0c9c2] bg-[#fff5f2] text-[#a33d32]'
          : 'border-[#cfe3cb] bg-[#f0f7ed] text-[#426c43]'
      }`}
      role={error ? 'alert' : 'status'}
    >
      <span>{text}</span>
      {onDismiss && (
        <button
          aria-label="Dismiss message"
          className="font-bold opacity-70 hover:opacity-100"
          onClick={onDismiss}
          type="button"
        >
          x
        </button>
      )}
    </div>
  )
}

export function StatusBadge({ status }: { status: string }) {
  const label = status.toLowerCase().replaceAll('_', ' ')
  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-[11px] font-bold capitalize ${
        statusStyles[status] ?? 'bg-[#edf0ed] text-[#647268]'
      }`}
    >
      {label}
    </span>
  )
}

export function MetricTile({
  label,
  value,
  detail,
  accent = 'bg-[#e8f0e2]',
}: {
  label: string
  value: number | string
  detail?: string
  accent?: string
}) {
  return (
    <article className="rounded-3xl border border-[#e4e9e1] bg-white p-5 shadow-sm">
      <span className={`mb-4 block size-2.5 rounded-full ${accent}`} />
      <p className="text-sm font-medium text-[#718075]">{label}</p>
      <p className="mt-1 text-3xl font-semibold tracking-tight text-[#203328]">{value}</p>
      {detail && <p className="mt-2 text-xs text-[#8a958c]">{detail}</p>}
    </article>
  )
}

export function EmptyState({
  title,
  description,
}: {
  title: string
  description: string
}) {
  return (
    <div className="rounded-2xl border border-dashed border-[#dce4da] bg-[#fbfcf9] px-5 py-8 text-center">
      <p className="font-semibold text-[#3d5143]">{title}</p>
      <p className="mx-auto mt-2 max-w-md text-sm leading-6 text-[#7b887d]">{description}</p>
    </div>
  )
}
