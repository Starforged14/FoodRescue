import axios from 'axios'
import { useCallback, useEffect, useState, type FormEvent, type ReactNode } from 'react'
import { api } from '../../lib/api'
import { getErrorMessage } from '../../lib/error-message'
import type {
  DeliveryTask,
  DonorProfile,
  DonorProfileRequest,
  DonorType,
  FoodCategory,
  FoodListing,
  FoodListingRequest,
  FoodListingItemRequest,
  Page,
  QuantityUnit,
} from '../../types/platform'
import {
  DashboardTitle,
  EmptyState,
  Feedback,
  Panel,
  StatusBadge,
} from '../../components/DashboardUI'
import { buttonClass, fieldClass, formatDate, secondaryButtonClass } from '../../components/dashboard-styles'

interface ItemDraft {
  name: string
  category: FoodCategory
  quantity: string
  unit: QuantityUnit
  allergenNotes: string
  dietaryNotes: string
}

interface ListingDraft {
  title: string
  description: string
  availableUntil: string
  pickupWindowStart: string
  pickupWindowEnd: string
  pickupInstructions: string
}

interface DonorProfileDraft {
  organizationName: string
  organizationType: DonorType
  addressLine1: string
  addressLine2: string
  city: string
  region: string
  postalCode: string
  countryCode: string
}

const donorTypes: DonorType[] = ['RESTAURANT', 'HOTEL', 'GROCERY_STORE', 'EVENT_HOST', 'OTHER']
const foodCategories: FoodCategory[] = [
  'PREPARED_MEAL',
  'PRODUCE',
  'BAKERY',
  'DAIRY',
  'MEAT',
  'SEAFOOD',
  'PACKAGED_GOODS',
  'OTHER',
]
const quantityUnits: QuantityUnit[] = [
  'KILOGRAM',
  'GRAM',
  'LITER',
  'MILLILITER',
  'PIECE',
  'SERVING',
  'BOX',
]

const initialProfileDraft: DonorProfileDraft = {
  organizationName: '',
  organizationType: 'RESTAURANT',
  addressLine1: '',
  addressLine2: '',
  city: '',
  region: '',
  postalCode: '',
  countryCode: '',
}

function localDateTimeAfter(milliseconds: number): string {
  const date = new Date(Date.now() + milliseconds)
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset())
  return date.toISOString().slice(0, 16)
}

function newListingDraft(): ListingDraft {
  return {
    title: '',
    description: '',
    pickupWindowStart: localDateTimeAfter(60 * 60 * 1000),
    pickupWindowEnd: localDateTimeAfter(3 * 60 * 60 * 1000),
    availableUntil: localDateTimeAfter(6 * 60 * 60 * 1000),
    pickupInstructions: '',
  }
}

function newItemDraft(): ItemDraft {
  return {
    name: '',
    category: 'PREPARED_MEAL',
    quantity: '1',
    unit: 'SERVING',
    allergenNotes: '',
    dietaryNotes: '',
  }
}

export function DonorDashboard() {
  const [profile, setProfile] = useState<DonorProfile | null>(null)
  const [profileDraft, setProfileDraft] = useState(initialProfileDraft)
  const [listings, setListings] = useState<FoodListing[]>([])
  const [deliveries, setDeliveries] = useState<DeliveryTask[]>([])
  const [listingDraft, setListingDraft] = useState(newListingDraft)
  const [items, setItems] = useState<ItemDraft[]>([newItemDraft()])
  const [loading, setLoading] = useState(true)
  const [savingProfile, setSavingProfile] = useState(false)
  const [savingListing, setSavingListing] = useState(false)
  const [busyListingId, setBusyListingId] = useState<string | null>(null)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  const loadDashboard = useCallback(async () => {
    const profileRequest = api.get<DonorProfile>('/donor/profile').then((response) => {
      setProfile(response.data)
      setProfileDraft({
        organizationName: response.data.organizationName,
        organizationType: response.data.organizationType,
        addressLine1: response.data.addressLine1,
        addressLine2: response.data.addressLine2 ?? '',
        city: response.data.city,
        region: response.data.region ?? '',
        postalCode: response.data.postalCode,
        countryCode: response.data.countryCode,
      })
    }).catch((requestError: unknown) => {
      if (axios.isAxiosError(requestError) && requestError.response?.status === 404) {
        setProfile(null)
        return
      }
      throw requestError
    })
    const listingsRequest = api.get<Page<FoodListing>>('/donor/listings', {
      params: { page: 0, size: 20, sort: 'createdAt,desc' },
    }).then((response) => setListings(response.data.content))
    const deliveriesRequest = api.get<Page<DeliveryTask>>('/donor/deliveries', {
      params: { page: 0, size: 20, sort: 'createdAt,desc' },
    }).then((response) => setDeliveries(response.data.content))

    try {
      await Promise.all([profileRequest, listingsRequest, deliveriesRequest])
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    queueMicrotask(() => void loadDashboard())
  }, [loadDashboard])

  function updateProfileDraft(field: keyof DonorProfileDraft, value: string) {
    setProfileDraft((current) => ({ ...current, [field]: value }))
  }

  async function saveProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSavingProfile(true)
    setError('')
    setMessage('')
    const request: DonorProfileRequest = {
      ...profileDraft,
      countryCode: profileDraft.countryCode.toUpperCase(),
      addressLine2: profileDraft.addressLine2 || undefined,
      region: profileDraft.region || undefined,
    }

    try {
      const response = await api.put<DonorProfile>('/donor/profile', request)
      setProfile(response.data)
      setMessage('Donor profile saved.')
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setSavingProfile(false)
    }
  }

  async function createListing(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSavingListing(true)
    setError('')
    setMessage('')
    const request: FoodListingRequest = {
      title: listingDraft.title.trim(),
      description: listingDraft.description || undefined,
      availableUntil: new Date(listingDraft.availableUntil).toISOString(),
      pickupWindowStart: new Date(listingDraft.pickupWindowStart).toISOString(),
      pickupWindowEnd: new Date(listingDraft.pickupWindowEnd).toISOString(),
      pickupInstructions: listingDraft.pickupInstructions || undefined,
      items: items.map((item): FoodListingItemRequest => ({
        name: item.name.trim(),
        category: item.category,
        quantity: Number(item.quantity),
        unit: item.unit,
        allergenNotes: item.allergenNotes || undefined,
        dietaryNotes: item.dietaryNotes || undefined,
      })),
    }

    try {
      await api.post<FoodListing>('/donor/listings', request)
      setListingDraft(newListingDraft())
      setItems([newItemDraft()])
      setMessage('Food listing saved as a draft. Publish it when it is ready to be claimed.')
      await loadDashboard()
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setSavingListing(false)
    }
  }

  async function updateListing(listing: FoodListing, action: 'publish' | 'withdraw' | 'delete') {
    setBusyListingId(listing.id)
    setError('')
    setMessage('')
    try {
      if (action === 'delete') {
        await api.delete(`/donor/listings/${listing.id}`)
      } else {
        await api.post(`/donor/listings/${listing.id}/${action}`)
      }
      setMessage(
        action === 'publish'
          ? 'Listing published.'
          : action === 'withdraw'
            ? 'Listing withdrawn.'
            : 'Draft deleted.',
      )
      await loadDashboard()
    } catch (requestError) {
      setError(getErrorMessage(requestError))
    } finally {
      setBusyListingId(null)
    }
  }

  return (
    <>
      <DashboardTitle
        description="Create food listings, manage their availability, and follow each donation through its current status."
        eyebrow="Donor"
        title="Share surplus with care."
      />
      <Feedback error={error} message={message} onDismiss={() => { setError(''); setMessage('') }} />
      <div className="grid items-start gap-6 xl:grid-cols-[0.82fr_1.18fr]">
        <div className="space-y-6">
          <Panel title={profile ? 'Donor profile' : 'Set up your donor profile'}>
            <form className="grid gap-3 sm:grid-cols-2" onSubmit={saveProfile}>
              <FormField className="sm:col-span-2" label="Organization name">
                <input className={fieldClass} maxLength={200} onChange={(e) => updateProfileDraft('organizationName', e.target.value)} required value={profileDraft.organizationName} />
              </FormField>
              <FormField label="Organization type">
                <select className={fieldClass} onChange={(e) => updateProfileDraft('organizationType', e.target.value)} value={profileDraft.organizationType}>
                  {donorTypes.map((type) => <option key={type} value={type}>{formatEnum(type)}</option>)}
                </select>
              </FormField>
              <FormField label="Country code">
                <input className={fieldClass} maxLength={2} onChange={(e) => updateProfileDraft('countryCode', e.target.value)} placeholder="US" required value={profileDraft.countryCode} />
              </FormField>
              <FormField className="sm:col-span-2" label="Street address">
                <input className={fieldClass} maxLength={200} onChange={(e) => updateProfileDraft('addressLine1', e.target.value)} required value={profileDraft.addressLine1} />
              </FormField>
              <FormField className="sm:col-span-2" label="Address line 2 (optional)">
                <input className={fieldClass} maxLength={200} onChange={(e) => updateProfileDraft('addressLine2', e.target.value)} value={profileDraft.addressLine2} />
              </FormField>
              <FormField label="City">
                <input className={fieldClass} maxLength={100} onChange={(e) => updateProfileDraft('city', e.target.value)} required value={profileDraft.city} />
              </FormField>
              <FormField label="Region / state">
                <input className={fieldClass} maxLength={100} onChange={(e) => updateProfileDraft('region', e.target.value)} value={profileDraft.region} />
              </FormField>
              <FormField label="Postal code">
                <input className={fieldClass} maxLength={20} onChange={(e) => updateProfileDraft('postalCode', e.target.value)} required value={profileDraft.postalCode} />
              </FormField>
              <div className="sm:col-span-2">
                <button className={buttonClass} disabled={savingProfile} type="submit">
                  {savingProfile ? 'Saving...' : profile ? 'Update profile' : 'Save profile'}
                </button>
              </div>
            </form>
          </Panel>

          <Panel title="Create a food listing">
            {!profile && (
              <p className="mb-4 rounded-xl bg-[#fff7e8] px-4 py-3 text-sm leading-5 text-[#816331]">
                Save your donor profile before posting food so volunteers and organizations have a pickup location.
              </p>
            )}
            <form className="space-y-4" onSubmit={createListing}>
              <FormField label="Listing title">
                <input className={fieldClass} maxLength={200} onChange={(e) => setListingDraft((d) => ({ ...d, title: e.target.value }))} required value={listingDraft.title} />
              </FormField>
              <FormField label="Description (optional)">
                <textarea className={`${fieldClass} min-h-20 resize-y`} maxLength={5000} onChange={(e) => setListingDraft((d) => ({ ...d, description: e.target.value }))} value={listingDraft.description} />
              </FormField>
              <div className="grid gap-3 sm:grid-cols-2">
                <FormField label="Pickup starts">
                  <input className={fieldClass} onChange={(e) => setListingDraft((d) => ({ ...d, pickupWindowStart: e.target.value }))} required type="datetime-local" value={listingDraft.pickupWindowStart} />
                </FormField>
                <FormField label="Pickup ends">
                  <input className={fieldClass} onChange={(e) => setListingDraft((d) => ({ ...d, pickupWindowEnd: e.target.value }))} required type="datetime-local" value={listingDraft.pickupWindowEnd} />
                </FormField>
                <FormField className="sm:col-span-2" label="Food available until">
                  <input className={fieldClass} onChange={(e) => setListingDraft((d) => ({ ...d, availableUntil: e.target.value }))} required type="datetime-local" value={listingDraft.availableUntil} />
                </FormField>
              </div>
              <FormField label="Pickup instructions (optional)">
                <textarea className={`${fieldClass} min-h-16 resize-y`} maxLength={5000} onChange={(e) => setListingDraft((d) => ({ ...d, pickupInstructions: e.target.value }))} value={listingDraft.pickupInstructions} />
              </FormField>

              <div className="border-t border-[#edf0eb] pt-4">
                <div className="mb-3 flex items-center justify-between">
                  <h3 className="text-sm font-bold text-[#34473a]">Food items</h3>
                  <button
                    className={secondaryButtonClass}
                    onClick={() => setItems((current) => [...current, newItemDraft()])}
                    type="button"
                  >
                    Add item
                  </button>
                </div>
                <div className="space-y-4">
                  {items.map((item, index) => (
                    <div className="rounded-2xl bg-[#f7f9f5] p-4" key={index}>
                      <div className="mb-3 flex items-center justify-between">
                        <p className="text-xs font-bold uppercase tracking-wide text-[#718075]">Item {index + 1}</p>
                        {items.length > 1 && (
                          <button
                            className="text-xs font-semibold text-[#a54c40] hover:underline"
                            onClick={() => setItems((current) => current.filter((_, itemIndex) => itemIndex !== index))}
                            type="button"
                          >
                            Remove
                          </button>
                        )}
                      </div>
                      <div className="grid gap-3 sm:grid-cols-2">
                        <FormField className="sm:col-span-2" label="Food name">
                          <input className={fieldClass} maxLength={200} onChange={(e) => updateItem(index, 'name', e.target.value)} required value={item.name} />
                        </FormField>
                        <FormField label="Category">
                          <select className={fieldClass} onChange={(e) => updateItem(index, 'category', e.target.value as FoodCategory)} value={item.category}>
                            {foodCategories.map((category) => <option key={category} value={category}>{formatEnum(category)}</option>)}
                          </select>
                        </FormField>
                        <div className="grid grid-cols-2 gap-2">
                          <FormField label="Quantity">
                            <input className={fieldClass} min="0.001" onChange={(e) => updateItem(index, 'quantity', e.target.value)} required step="0.001" type="number" value={item.quantity} />
                          </FormField>
                          <FormField label="Unit">
                            <select className={fieldClass} onChange={(e) => updateItem(index, 'unit', e.target.value as QuantityUnit)} value={item.unit}>
                              {quantityUnits.map((unit) => <option key={unit} value={unit}>{formatEnum(unit)}</option>)}
                            </select>
                          </FormField>
                        </div>
                        <FormField label="Allergen notes">
                          <input className={fieldClass} maxLength={2000} onChange={(e) => updateItem(index, 'allergenNotes', e.target.value)} value={item.allergenNotes} />
                        </FormField>
                        <FormField label="Dietary notes">
                          <input className={fieldClass} maxLength={2000} onChange={(e) => updateItem(index, 'dietaryNotes', e.target.value)} value={item.dietaryNotes} />
                        </FormField>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
              <button className={buttonClass} disabled={!profile || savingListing} type="submit">
                {savingListing ? 'Saving listing...' : 'Save as draft'}
              </button>
            </form>
          </Panel>
        </div>

        <div className="space-y-6">
          <Panel
            aside={<span className="text-xs font-medium text-[#879288]">{listings.length} shown</span>}
            title="Your listings"
          >
            {loading ? (
              <LoadingState />
            ) : listings.length === 0 ? (
              <EmptyState
                description="Your listings will appear here after you save your first food donation."
                title="Nothing posted yet"
              />
            ) : (
              <div className="space-y-3">
                {listings.map((listing) => (
                  <article className="rounded-2xl border border-[#e8ede6] bg-[#fcfdfb] p-4" key={listing.id}>
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div>
                        <h3 className="font-semibold text-[#2f4435]">{listing.title}</h3>
                        <p className="mt-1 text-xs text-[#849087]">{listing.pickupCity}, {listing.pickupCountryCode}</p>
                      </div>
                      <StatusBadge status={listing.status} />
                    </div>
                    <div className="mt-4 grid gap-2 text-xs text-[#718075] sm:grid-cols-2">
                      <p>Pickup: {formatDate(listing.pickupWindowStart)}</p>
                      <p>Available until: {formatDate(listing.availableUntil)}</p>
                    </div>
                    <div className="mt-3 flex flex-wrap gap-1.5">
                      {listing.items.map((item) => (
                        <span className="rounded-full bg-[#edf2e9] px-2.5 py-1 text-[11px] font-semibold text-[#5c715b]" key={item.id}>
                          {item.name} - {item.quantity} {formatEnum(item.unit)}
                        </span>
                      ))}
                    </div>
                    <div className="mt-4 flex flex-wrap gap-2">
                      {listing.status === 'DRAFT' && (
                        <>
                          <button
                            className={buttonClass}
                            disabled={busyListingId === listing.id}
                            onClick={() => void updateListing(listing, 'publish')}
                            type="button"
                          >
                            Publish
                          </button>
                          <button
                            className={secondaryButtonClass}
                            disabled={busyListingId === listing.id}
                            onClick={() => void updateListing(listing, 'delete')}
                            type="button"
                          >
                            Delete draft
                          </button>
                        </>
                      )}
                      {listing.status === 'AVAILABLE' && (
                        <button
                          className={secondaryButtonClass}
                          disabled={busyListingId === listing.id}
                          onClick={() => void updateListing(listing, 'withdraw')}
                          type="button"
                        >
                          Withdraw listing
                        </button>
                      )}
                    </div>
                  </article>
                ))}
              </div>
            )}
          </Panel>

          <Panel
            aside={<span className="text-xs font-medium text-[#879288]">{deliveries.length} shown</span>}
            title="Delivery progress"
          >
            {loading ? (
              <LoadingState />
            ) : deliveries.length === 0 ? (
              <EmptyState
                description="Volunteer delivery tasks for your claimed listings will appear here."
                title="No deliveries yet"
              />
            ) : (
              <div className="space-y-3">
                {deliveries.map((delivery) => (
                  <article className="rounded-2xl border border-[#e8ede6] bg-[#fcfdfb] p-4" key={delivery.id}>
                    <div className="flex flex-wrap items-center justify-between gap-3">
                      <h3 className="font-semibold text-[#2f4435]">{delivery.foodTitle}</h3>
                      <StatusBadge status={delivery.status} />
                    </div>
                    <p className="mt-2 text-xs text-[#718075]">
                      Pickup window: {formatDate(delivery.pickupWindowStart)}
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
      </div>
    </>
  )

  function updateItem<K extends keyof ItemDraft>(index: number, field: K, value: ItemDraft[K]) {
    setItems((current) => current.map((item, itemIndex) =>
      itemIndex === index ? { ...item, [field]: value } : item,
    ))
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
  return <p className="py-8 text-center text-sm text-[#718075]">Loading your dashboard...</p>
}

function formatEnum(value: string): string {
  return value.toLowerCase().replaceAll('_', ' ')
}
