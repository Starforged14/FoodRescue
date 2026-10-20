import { useCallback, useEffect, useState } from 'react'
import {
  DashboardTitle,
  EmptyState,
  Feedback,
  MetricTile,
  Panel,
  StatusBadge,
} from '../../components/DashboardUI'
import { buttonClass, secondaryButtonClass } from '../../components/dashboard-styles'
import { api } from '../../lib/api'
import { getErrorMessage } from '../../lib/error-message'
import type {
  AdminDashboardMetrics,
  AdminNgoMembership,
  AdminNgoOrganization,
  Page,
} from '../../types/platform'

export function AdminDashboard() {
  const [metrics, setMetrics] = useState<AdminDashboardMetrics | null>(null)
  const [organizations, setOrganizations] = useState<AdminNgoOrganization[]>([])
  const [memberships, setMemberships] = useState<AdminNgoMembership[]>([])
  const [loading, setLoading] = useState(true)
  const [busyId, setBusyId] = useState<string | null>(null)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const loadDashboard = useCallback(async () => {
    try {
      const [metricsResponse, organizationsResponse, membershipsResponse] = await Promise.all([
        api.get<AdminDashboardMetrics>('/admin/dashboard/metrics'),
        api.get<Page<AdminNgoOrganization>>('/admin/organizations/pending', {
          params: { page: 0, size: 20, sort: 'createdAt,asc' },
        }),
        api.get<Page<AdminNgoMembership>>('/admin/memberships/pending', {
          params: { page: 0, size: 20, sort: 'createdAt,asc' },
        }),
      ])
      setMetrics(metricsResponse.data)
      setOrganizations(organizationsResponse.data.content)
      setMemberships(membershipsResponse.data.content)
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    queueMicrotask(() => void loadDashboard())
  }, [loadDashboard])

  async function review(
    kind: 'organizations' | 'memberships',
    id: string,
    decision: 'approve' | 'reject',
  ) {
    setBusyId(id)
    setError('')
    setMessage('')
    try {
      await api.post(`/admin/${kind}/${id}/${decision}`)
      setMessage(decision === 'approve' ? 'Review approved.' : 'Review rejected.')
      await loadDashboard()
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setBusyId(null)
    }
  }

  const userCounts = metrics?.usersByRole ?? {}
  const listingCounts = metrics?.listingsByStatus ?? {}
  const claimCounts = metrics?.claimsByStatus ?? {}
  const deliveryCounts = metrics?.deliveriesByStatus ?? {}

  return (
    <>
      <DashboardTitle
        description="Review NGO registrations and memberships, and monitor platform activity across food listings, claims, and deliveries."
        eyebrow="Administrator"
        title="Platform overview."
      />
      <Feedback error={error} message={message} onDismiss={() => { setError(''); setMessage('') }} />

      {loading && !metrics ? (
        <LoadingState />
      ) : (
        <>
          <section className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
            <MetricTile detail="Across all account roles" label="Total accounts" value={metrics?.totalUsers ?? 0} />
            <MetricTile detail={`${userCounts.DONOR ?? 0} donors / ${userCounts.NGO ?? 0} NGOs / ${userCounts.VOLUNTEER ?? 0} volunteers`} label="Community accounts" value={(userCounts.DONOR ?? 0) + (userCounts.NGO ?? 0) + (userCounts.VOLUNTEER ?? 0)} accent="bg-[#f1d9a8]" />
            <MetricTile detail={`${listingCounts.AVAILABLE ?? 0} available now`} label="Food listings" value={sumValues(listingCounts)} accent="bg-[#b9d5ae]" />
            <MetricTile detail={`${deliveryCounts.OPEN ?? 0} waiting for a volunteer`} label="Delivery tasks" value={sumValues(deliveryCounts)} accent="bg-[#c8d3e7]" />
          </section>

          <section className="mt-6 grid gap-6 xl:grid-cols-2">
            <Panel
              aside={<StatusBadge status="PENDING_REVIEW" />}
              title={`NGO organization review (${metrics?.pendingNgoOrganizations ?? 0})`}
            >
              {organizations.length === 0 ? (
                <EmptyState
                  description="New organization registrations will appear here for verification."
                  title="No organizations awaiting review"
                />
              ) : (
                <div className="space-y-3">
                  {organizations.map((organization) => (
                    <article className="rounded-2xl border border-[#e8ede6] bg-[#fcfdfb] p-4" key={organization.id}>
                      <div className="flex flex-wrap items-start justify-between gap-3">
                        <div>
                          <h3 className="font-semibold text-[#304635]">{organization.name}</h3>
                          <p className="mt-1 text-xs text-[#718075]">{organization.contactEmail}</p>
                        </div>
                        <StatusBadge status={organization.verificationStatus} />
                      </div>
                      <p className="mt-3 text-sm leading-6 text-[#5f6e63]">
                        {organization.addressLine1}
                        {organization.addressLine2 ? `, ${organization.addressLine2}` : ''}
                        <br />
                        {organization.city}{organization.region ? `, ${organization.region}` : ''} {organization.postalCode}, {organization.countryCode}
                      </p>
                      <div className="mt-4 flex flex-wrap gap-2">
                        <button className={buttonClass} disabled={busyId === organization.id} onClick={() => void review('organizations', organization.id, 'approve')} type="button">
                          Approve organization
                        </button>
                        <button className={secondaryButtonClass} disabled={busyId === organization.id} onClick={() => void review('organizations', organization.id, 'reject')} type="button">
                          Reject
                        </button>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </Panel>

            <Panel
              aside={<StatusBadge status="PENDING" />}
              title={`NGO membership review (${metrics?.pendingNgoMemberships ?? 0})`}
            >
              {memberships.length === 0 ? (
                <EmptyState
                  description="Membership requests submitted with organization registrations will appear here."
                  title="No memberships awaiting review"
                />
              ) : (
                <div className="space-y-3">
                  {memberships.map((membership) => (
                    <article className="rounded-2xl border border-[#e8ede6] bg-[#fcfdfb] p-4" key={membership.id}>
                      <div className="flex flex-wrap items-start justify-between gap-3">
                        <div>
                          <h3 className="font-semibold text-[#304635]">{membership.firstName} {membership.lastName}</h3>
                          <p className="mt-1 text-xs text-[#718075]">{membership.userEmail}</p>
                        </div>
                        <StatusBadge status={membership.status} />
                      </div>
                      <p className="mt-3 text-sm text-[#5f6e63]">
                        Request to join <span className="font-semibold">{membership.organizationName}</span>
                      </p>
                      <div className="mt-4 flex flex-wrap gap-2">
                        <button className={buttonClass} disabled={busyId === membership.id} onClick={() => void review('memberships', membership.id, 'approve')} type="button">
                          Approve membership
                        </button>
                        <button className={secondaryButtonClass} disabled={busyId === membership.id} onClick={() => void review('memberships', membership.id, 'reject')} type="button">
                          Reject
                        </button>
                      </div>
                    </article>
                  ))}
                </div>
              )}
            </Panel>
          </section>

          <section className="mt-6 grid gap-6 lg:grid-cols-3">
            <CountPanel counts={listingCounts} title="Listings by status" />
            <CountPanel counts={claimCounts} title="Claims by status" />
            <CountPanel counts={deliveryCounts} title="Deliveries by status" />
          </section>
        </>
      )}
    </>
  )
}

function CountPanel({ counts, title }: { counts: Record<string, number>; title: string }) {
  const entries = Object.entries(counts).sort(([first], [second]) => first.localeCompare(second))
  return (
    <Panel title={title}>
      {entries.length === 0 ? (
        <EmptyState description="Metrics will appear after platform activity is recorded." title="No data yet" />
      ) : (
        <dl className="space-y-3">
          {entries.map(([status, count]) => (
            <div className="flex items-center justify-between gap-3" key={status}>
              <dt><StatusBadge status={status} /></dt>
              <dd className="text-sm font-bold tabular-nums text-[#34473a]">{count}</dd>
            </div>
          ))}
        </dl>
      )}
    </Panel>
  )
}

function LoadingState() {
  return (
    <div className="rounded-3xl border border-[#e4e9e1] bg-white px-5 py-12 text-center text-sm text-[#718075]">
      Loading platform activity...
    </div>
  )
}

function sumValues(counts: Record<string, number>): number {
  return Object.values(counts).reduce((total, count) => total + count, 0)
}
