import { useCallback, useEffect, useRef, useState, type FormEvent, type ReactNode } from 'react'
import { api } from '../../lib/api'
import { getErrorMessage } from '../../lib/error-message'
import type {
  DeliveryTask,
  FoodClaim,
  FoodListing,
  FulfillmentMode,
  NgoOrganization,
  NgoOrganizationRequest,
  Page,
} from '../../types/platform'
import {
  DashboardTitle,
  EmptyState,
  Feedback,
  Panel,
  StatusBadge,
} from '../../components/DashboardUI'
import { buttonClass, fieldClass, formatDate, secondaryButtonClass } from '../../components/dashboard-styles'

interface OrganizationDraft {
  name: string
  registrationNumber: string
  contactEmail: string
  contactPhone: string
  addressLine1: string
  addressLine2: string
  city: string
  region: string
  postalCode: string
  countryCode: string
}

const blankOrganization: OrganizationDraft = {
  name: '',
  registrationNumber: '',
  contactEmail: '',
  contactPhone: '',
  addressLine1: '',
  addressLine2: '',
  city: '',
  region: '',
  postalCode: '',
  countryCode: '',
}

const fulfillmentModes: FulfillmentMode[] = ['NGO_PICKUP', 'VOLUNTEER_DELIVERY']

async function fetchClaims(selectedOrganizationId: string): Promise<FoodClaim[]> {
  const response = await api.get<Page<FoodClaim>>('/ngo/claims', {
    params: {
      organizationId: selectedOrganizationId,
      page: 0,
      size: 20,
      sort: 'claimedAt,desc',
    },
  })
  return response.data.content
}

async function fetchOrganizationDeliveries(organizationId: string): Promise<DeliveryTask[]> {
  const response = await api.get<Page<DeliveryTask>>('/ngo/deliveries', {
    params: {
      organizationId,
      page: 0,
      size: 20,
      sort: 'createdAt,desc',
    },
  })
  return response.data.content
}

export function NgoDashboard() {
  const [organizations, setOrganizations] = useState<NgoOrganization[]>([])
  const [organizationDraft, setOrganizationDraft] = useState(blankOrganization)
  const [organizationId, setOrganizationId] = useState('')
  const organizationIdRef = useRef('')
  const [listings, setListings] = useState<FoodListing[]>([])
  const [claims, setClaims] = useState<FoodClaim[]>([])
  const [deliveries, setDeliveries] = useState<DeliveryTask[]>([])
  const [fulfillmentMode, setFulfillmentMode] = useState<FulfillmentMode>('NGO_PICKUP')
  const [loading, setLoading] = useState(true)
  const [savingOrganization, setSavingOrganization] = useState(false)
  const [busyListingId, setBusyListingId] = useState<string | null>(null)
  const [busyClaimId, setBusyClaimId] = useState<string | null>(null)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const loadOrganizationsAndListings = useCallback(async () => {
    try {
      const [organizationResponse, listingResponse] = await Promise.all([
        api.get<NgoOrganization[]>('/ngo/organizations/mine'),
        api.get<Page<FoodListing>>('/ngo/listings', {
          params: { page: 0, size: 20, sort: 'createdAt,desc' },
        }),
      ])
      const nextOrganizations = organizationResponse.data ?? []
      setOrganizations(nextOrganizations)
      setListings(listingResponse.data?.content ?? [])
      const currentSelection = nextOrganizations.find(
        (organization) => organization.id === organizationIdRef.current,
      )
      const selected =
        currentSelection ??
        nextOrganizations.find((organization) =>
          organization.verificationStatus === 'VERIFIED' && organization.membershipStatus === 'ACTIVE',
        ) ??
        nextOrganizations[0]
      const nextOrganizationId = selected?.id ?? ''
      organizationIdRef.current = nextOrganizationId
      setOrganizationId(nextOrganizationId)
      if (nextOrganizationId && selected?.verificationStatus === 'VERIFIED' && selected.membershipStatus === 'ACTIVE') {
        const [nextClaims, nextDeliveries] = await Promise.all([
          fetchClaims(nextOrganizationId),
          fetchOrganizationDeliveries(nextOrganizationId),
        ])
        setClaims(nextClaims)
        setDeliveries(nextDeliveries)
      } else {
        setClaims([])
        setDeliveries([])
      }
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    queueMicrotask(() => void loadOrganizationsAndListings())
  }, [loadOrganizationsAndListings])

  async function changeOrganization(selectedOrganizationId: string) {
    organizationIdRef.current = selectedOrganizationId
    setOrganizationId(selectedOrganizationId)
    setError('')
    const organization = organizations.find((item) => item.id === selectedOrganizationId)
    if (!organization || organization.verificationStatus !== 'VERIFIED' || organization.membershipStatus !== 'ACTIVE') {
      setClaims([])
      setDeliveries([])
      return
    }
    try {
      const [nextClaims, nextDeliveries] = await Promise.all([
        fetchClaims(selectedOrganizationId),
        fetchOrganizationDeliveries(selectedOrganizationId),
      ])
      setClaims(nextClaims)
      setDeliveries(nextDeliveries)
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    }
  }

  async function createOrganization(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSavingOrganization(true)
    setError('')
    setMessage('')
    const request: NgoOrganizationRequest = {
      ...organizationDraft,
      registrationNumber: organizationDraft.registrationNumber || undefined,
      contactPhone: organizationDraft.contactPhone || undefined,
      addressLine2: organizationDraft.addressLine2 || undefined,
      region: organizationDraft.region || undefined,
      countryCode: organizationDraft.countryCode.toUpperCase(),
    }
    try {
      await api.post<NgoOrganization>('/ngo/organizations', request)
      setOrganizationDraft(blankOrganization)
      setMessage('Organization submitted. An administrator must verify the organization and activate your membership before you can claim food.')
      await loadOrganizationsAndListings()
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setSavingOrganization(false)
    }
  }

  async function claimListing(listing: FoodListing) {
    const organization = organizations.find((item) => item.id === organizationId)
    if (!organization || organization.verificationStatus !== 'VERIFIED' || organization.membershipStatus !== 'ACTIVE') {
      setError('Choose an organization with verified status and an active membership before claiming.')
      return
    }
    setBusyListingId(listing.id)
    setError('')
    setMessage('')
    try {
      await api.post<FoodClaim>(`/ngo/listings/${listing.id}/claims`, {
        organizationId,
        fulfillmentMode,
      })
      setMessage(`You claimed "${listing.title}".`)
      await loadOrganizationsAndListings()
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setBusyListingId(null)
    }
  }

  async function updateClaim(claim: FoodClaim, action: 'cancel' | 'complete') {
    setBusyClaimId(claim.id)
    setError('')
    setMessage('')
    try {
      if (action === 'cancel') {
        await api.post(`/ngo/claims/${claim.id}/cancel`, { reason: 'Cancelled by organization' })
        setMessage('Claim cancelled.')
      } else {
        await api.post(`/ngo/claims/${claim.id}/complete`)
        setMessage('Pickup claim completed.')
      }
      await loadOrganizationsAndListings()
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setBusyClaimId(null)
    }
  }

  const activeOrganization = organizations.find((item) => item.id === organizationId)
  const canClaim =
    activeOrganization?.verificationStatus === 'VERIFIED' &&
    activeOrganization.membershipStatus === 'ACTIVE'

  return (
    <>
      <DashboardTitle
        description="Review local food offers, claim a listing for your organization, and keep an eye on pickup or volunteer delivery."
        eyebrow="Community organization"
        title="Find food for your community."
      />
      <Feedback error={error} message={message} onDismiss={() => { setError(''); setMessage('') }} />
      <div className="grid items-start gap-6 xl:grid-cols-[0.78fr_1.22fr]">
        <div className="space-y-6">
          <Panel
            aside={organizations.length > 0 ? <StatusBadge status={activeOrganization?.verificationStatus ?? 'PENDING_REVIEW'} /> : undefined}
            title="Your organizations"
          >
            {organizations.length === 0 ? (
              <EmptyState
                description="Register your community organization to request food. Verification and membership approval are required before claims."
                title="No organization linked yet"
              />
            ) : (
              <div className="space-y-3">
                <label className="block text-xs font-semibold text-[#526358]">
                  Active organization
                  <select
                    className={fieldClass}
                    onChange={(event) => void changeOrganization(event.target.value)}
                    value={organizationId}
                  >
                    {organizations.map((organization) => (
                      <option key={organization.id} value={organization.id}>
                        {organization.name} - {formatEnum(organization.verificationStatus)}
                      </option>
                    ))}
                  </select>
                </label>
                <div className="rounded-2xl bg-[#f7f9f5] p-4">
                  <div className="flex flex-wrap items-center gap-2">
                    <StatusBadge status={activeOrganization?.verificationStatus ?? 'PENDING_REVIEW'} />
                    {activeOrganization && <StatusBadge status={activeOrganization.membershipStatus} />}
                  </div>
                  <p className="mt-3 text-sm font-semibold text-[#35483a]">{activeOrganization?.name}</p>
                  <p className="mt-1 text-xs text-[#718075]">
                    {activeOrganization?.city}, {activeOrganization?.countryCode}
                  </p>
                  {!canClaim && (
                    <p className="mt-3 text-xs leading-5 text-[#8b6d39]">
                      Claims unlock after both organization verification and membership approval.
                    </p>
                  )}
                </div>
              </div>
            )}
            <details className="mt-5 rounded-2xl border border-[#e7ece4] bg-[#fcfdfb] p-4">
              <summary className="cursor-pointer text-sm font-bold text-[#3c7048]">
                {organizations.length > 0 ? 'Register another organization' : 'Register organization'}
              </summary>
              <form className="mt-4 grid gap-3 sm:grid-cols-2" onSubmit={createOrganization}>
                <FormField className="sm:col-span-2" label="Organization name">
                  <input className={fieldClass} maxLength={200} onChange={(e) => updateOrganizationDraft('name', e.target.value)} required value={organizationDraft.name} />
                </FormField>
                <FormField label="Registration number (optional)">
                  <input className={fieldClass} maxLength={100} onChange={(e) => updateOrganizationDraft('registrationNumber', e.target.value)} value={organizationDraft.registrationNumber} />
                </FormField>
                <FormField label="Contact email">
                  <input className={fieldClass} maxLength={320} onChange={(e) => updateOrganizationDraft('contactEmail', e.target.value)} required type="email" value={organizationDraft.contactEmail} />
                </FormField>
                <FormField label="Contact phone">
                  <input className={fieldClass} maxLength={30} onChange={(e) => updateOrganizationDraft('contactPhone', e.target.value)} type="tel" value={organizationDraft.contactPhone} />
                </FormField>
                <FormField label="Country code">
                  <input className={fieldClass} maxLength={2} onChange={(e) => updateOrganizationDraft('countryCode', e.target.value)} placeholder="US" required value={organizationDraft.countryCode} />
                </FormField>
                <FormField className="sm:col-span-2" label="Street address">
                  <input className={fieldClass} maxLength={200} onChange={(e) => updateOrganizationDraft('addressLine1', e.target.value)} required value={organizationDraft.addressLine1} />
                </FormField>
                <FormField className="sm:col-span-2" label="Address line 2 (optional)">
                  <input className={fieldClass} maxLength={200} onChange={(e) => updateOrganizationDraft('addressLine2', e.target.value)} value={organizationDraft.addressLine2} />
                </FormField>
                <FormField label="City">
                  <input className={fieldClass} maxLength={100} onChange={(e) => updateOrganizationDraft('city', e.target.value)} required value={organizationDraft.city} />
                </FormField>
                <FormField label="Region / state">
                  <input className={fieldClass} maxLength={100} onChange={(e) => updateOrganizationDraft('region', e.target.value)} value={organizationDraft.region} />
                </FormField>
                <FormField label="Postal code">
                  <input className={fieldClass} maxLength={20} onChange={(e) => updateOrganizationDraft('postalCode', e.target.value)} required value={organizationDraft.postalCode} />
                </FormField>
                <div className="flex items-end">
                  <button className={buttonClass} disabled={savingOrganization} type="submit">
                    {savingOrganization ? 'Submitting...' : 'Submit for review'}
                  </button>
                </div>
              </form>
            </details>
          </Panel>

          <Panel title="Active claims">
            {!canClaim && claims.length === 0 ? (
              <EmptyState
                description="Approved claims for an active organization will appear here."
                title="Claims are not available yet"
              />
            ) : claims.length === 0 ? (
              <EmptyState
                description="When your organization claims a listing, its fulfillment progress will appear here."
                title="No active claims"
              />
            ) : (
              <div className="space-y-3">
                {claims.map((claim) => (
                  <article className="rounded-2xl border border-[#e8ede6] p-4" key={claim.id}>
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <h3 className="font-semibold text-[#314536]">{claim.listingTitle}</h3>
                      <StatusBadge status={claim.status} />
                    </div>
                    <p className="mt-2 text-xs text-[#7b887d]">
                      {formatEnum(claim.fulfillmentMode)} - claimed {formatDate(claim.claimedAt)}
                    </p>
                    <div className="mt-3 flex flex-wrap gap-2">
                      {claim.fulfillmentMode === 'NGO_PICKUP' && (
                        <button
                          className={buttonClass}
                          disabled={busyClaimId === claim.id}
                          onClick={() => void updateClaim(claim, 'complete')}
                          type="button"
                        >
                          Mark picked up
                        </button>
                      )}
                      <button
                        className={secondaryButtonClass}
                        disabled={busyClaimId === claim.id}
                        onClick={() => void updateClaim(claim, 'cancel')}
                        type="button"
                      >
                        Cancel claim
                      </button>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </Panel>
          <Panel title="Volunteer delivery progress">
            {!canClaim && deliveries.length === 0 ? (
              <EmptyState
                description="Delivery tasks appear after your organization is approved and makes a volunteer-delivery claim."
                title="No delivery tasks yet"
              />
            ) : deliveries.length === 0 ? (
              <EmptyState
                description="Volunteer delivery task status will appear here after a claim is made."
                title="No delivery tasks yet"
              />
            ) : (
              <div className="space-y-3">
                {deliveries.map((delivery) => (
                  <article className="rounded-2xl border border-[#e8ede6] p-4" key={delivery.id}>
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <h3 className="font-semibold text-[#35483a]">{delivery.foodTitle}</h3>
                      <StatusBadge status={delivery.status} />
                    </div>
                    <p className="mt-2 text-xs text-[#718075]">
                      {delivery.pickupAddress.city} to {delivery.dropoffAddress.city} · Pickup {formatDate(delivery.pickupWindowStart)}
                    </p>
                    {delivery.failureReason && (
                      <p className="mt-2 text-xs text-[#a54c40]">{delivery.failureReason}</p>
                    )}
                  </article>
                ))}
              </div>
            )}
          </Panel>
        </div>

        <div className="space-y-6">
          <Panel
            aside={
              <label className="flex items-center gap-2 text-xs font-semibold text-[#718075]">
                Fulfillment
                <select className="rounded-xl border border-[#dfe6dc] bg-white px-2.5 py-2 text-xs text-[#34473a]" onChange={(e) => setFulfillmentMode(e.target.value as FulfillmentMode)} value={fulfillmentMode}>
                  {fulfillmentModes.map((mode) => <option key={mode} value={mode}>{formatEnum(mode)}</option>)}
                </select>
              </label>
            }
            title="Available food"
          >
            {loading ? (
              <LoadingState />
            ) : listings.length === 0 ? (
              <EmptyState
                description="New public listings will appear here when donors share surplus food."
                title="No available listings"
              />
            ) : (
              <div className="space-y-3">
                {listings.map((listing) => (
                  <article className="rounded-2xl border border-[#e8ede6] bg-[#fcfdfb] p-4 sm:p-5" key={listing.id}>
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div>
                        <p className="text-xs font-semibold text-[#718075]">{listing.donorOrganizationName}</p>
                        <h3 className="mt-1 text-lg font-semibold text-[#2d4333]">{listing.title}</h3>
                        <p className="mt-1 text-sm text-[#718075]">{listing.pickupCity}, {listing.pickupRegion ?? listing.pickupCountryCode}</p>
                      </div>
                      <StatusBadge status={listing.status} />
                    </div>
                    {listing.description && <p className="mt-3 text-sm leading-6 text-[#657369]">{listing.description}</p>}
                    <div className="mt-3 flex flex-wrap gap-1.5">
                      {listing.items.map((item) => (
                        <span className="rounded-full bg-[#edf2e9] px-2.5 py-1 text-[11px] font-semibold text-[#5c715b]" key={item.id}>
                          {item.name} - {item.quantity} {formatEnum(item.unit)}
                        </span>
                      ))}
                    </div>
                    <div className="mt-4 grid gap-2 text-xs text-[#718075] sm:grid-cols-2">
                      <p>Pickup window: {formatDate(listing.pickupWindowStart)} - {formatDate(listing.pickupWindowEnd)}</p>
                      <p>Food available until: {formatDate(listing.availableUntil)}</p>
                    </div>
                    <button
                      className={`${buttonClass} mt-4`}
                      disabled={!canClaim || busyListingId === listing.id}
                      onClick={() => void claimListing(listing)}
                      type="button"
                    >
                      {busyListingId === listing.id ? 'Claiming...' : 'Claim for organization'}
                    </button>
                  </article>
                ))}
              </div>
            )}
          </Panel>
        </div>
      </div>
    </>
  )

  function updateOrganizationDraft(field: keyof OrganizationDraft, value: string) {
    setOrganizationDraft((current) => ({ ...current, [field]: value }))
  }
}

function FormField({
  children,
  className = '',
  label,
}: {
  children: ReactNode
  className?: string
  label: string
}) {
  return (
    <label className={`block text-xs font-semibold text-[#526358] ${className}`}>
      {label}
      {children}
    </label>
  )
}

function LoadingState() {
  return <p className="py-8 text-center text-sm text-[#718075]">Loading organization activity...</p>
}

function formatEnum(value: string): string {
  return value.toLowerCase().replaceAll('_', ' ')
}
