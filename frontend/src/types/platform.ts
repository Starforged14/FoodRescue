export type ListingStatus =
  | 'DRAFT'
  | 'AVAILABLE'
  | 'CLAIMED'
  | 'COMPLETED'
  | 'EXPIRED'
  | 'WITHDRAWN'

export type ClaimStatus = 'ACTIVE' | 'CANCELLED' | 'COMPLETED' | 'EXPIRED'
export type FulfillmentMode = 'NGO_PICKUP' | 'VOLUNTEER_DELIVERY'
export type DeliveryStatus =
  | 'OPEN'
  | 'ASSIGNED'
  | 'PICKED_UP'
  | 'DELIVERED'
  | 'FAILED'
  | 'CANCELLED'
export type MembershipStatus = 'PENDING' | 'ACTIVE' | 'REJECTED' | 'REMOVED'
export type OrganizationStatus = 'PENDING_REVIEW' | 'VERIFIED' | 'REJECTED' | 'SUSPENDED'
export type VolunteerAvailability = 'AVAILABLE' | 'UNAVAILABLE' | 'SUSPENDED'
export type DonorType = 'RESTAURANT' | 'HOTEL' | 'GROCERY_STORE' | 'EVENT_HOST' | 'OTHER'
export type FoodCategory =
  | 'PREPARED_MEAL'
  | 'PRODUCE'
  | 'BAKERY'
  | 'DAIRY'
  | 'MEAT'
  | 'SEAFOOD'
  | 'PACKAGED_GOODS'
  | 'OTHER'
export type QuantityUnit =
  | 'KILOGRAM'
  | 'GRAM'
  | 'LITER'
  | 'MILLILITER'
  | 'PIECE'
  | 'SERVING'
  | 'BOX'

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface FoodListingItemRequest {
  name: string
  category: FoodCategory
  quantity: number
  unit: QuantityUnit
  allergenNotes?: string
  dietaryNotes?: string
  bestBeforeAt?: string
}

export interface FoodListingRequest {
  title: string
  description?: string
  availableUntil: string
  pickupWindowStart: string
  pickupWindowEnd: string
  pickupInstructions?: string
  items: FoodListingItemRequest[]
}

export interface FoodListingItem {
  id: string
  name: string
  category: FoodCategory
  quantity: number
  unit: QuantityUnit
  allergenNotes?: string
  dietaryNotes?: string
  bestBeforeAt?: string
}

export interface FoodListing {
  id: string
  donorOrganizationName: string
  title: string
  description?: string
  status: ListingStatus
  availableUntil: string
  pickupWindowStart: string
  pickupWindowEnd: string
  pickupCity: string
  pickupRegion?: string
  pickupCountryCode: string
  pickupInstructions?: string
  items: FoodListingItem[]
}

export interface DonorProfileRequest {
  organizationName: string
  organizationType: DonorType
  addressLine1: string
  addressLine2?: string
  city: string
  region?: string
  postalCode: string
  countryCode: string
  latitude?: number
  longitude?: number
}

export interface DonorProfile extends DonorProfileRequest {
  userId: string
}

export interface NgoOrganizationRequest {
  name: string
  registrationNumber?: string
  contactEmail: string
  contactPhone?: string
  addressLine1: string
  addressLine2?: string
  city: string
  region?: string
  postalCode: string
  countryCode: string
  latitude?: number
  longitude?: number
}

export interface NgoOrganization extends NgoOrganizationRequest {
  id: string
  verificationStatus: OrganizationStatus
  membershipStatus: MembershipStatus
}

export interface FoodClaim {
  id: string
  listingId: string
  listingTitle: string
  organizationId: string
  organizationName: string
  claimedByUserId: string
  status: ClaimStatus
  fulfillmentMode: FulfillmentMode
  claimedAt: string
  cancelledAt?: string
  completedAt?: string
  cancellationReason?: string
}

export interface VolunteerProfileRequest {
  availabilityStatus: VolunteerAvailability
  maxTravelDistanceKm?: number
}

export interface VolunteerProfile extends VolunteerProfileRequest {
  userId: string
}

export interface DeliveryTaskSummary {
  id: string
  claimId: string
  foodTitle: string
  status: DeliveryStatus
  pickupWindowStart: string
  pickupWindowEnd: string
  pickupCity: string
  pickupRegion?: string
  dropoffCity: string
  dropoffRegion?: string
}

export interface AddressDetails {
  addressLine1: string
  addressLine2?: string
  city: string
  region?: string
  postalCode: string
  countryCode: string
  pickupInstructions?: string
}

export interface DeliveryTask {
  id: string
  claimId: string
  foodTitle: string
  status: DeliveryStatus
  pickupWindowStart: string
  pickupWindowEnd: string
  assignedAt?: string
  pickedUpAt?: string
  deliveredAt?: string
  failureReason?: string
  pickupAddress: AddressDetails
  dropoffAddress: AddressDetails
}

export interface AdminDashboardMetrics {
  totalUsers: number
  usersByRole: Record<string, number>
  listingsByStatus: Record<string, number>
  claimsByStatus: Record<string, number>
  deliveriesByStatus: Record<string, number>
  pendingNgoOrganizations: number
  pendingNgoMemberships: number
}

export interface AdminNgoOrganization {
  id: string
  name: string
  registrationNumber?: string
  contactEmail: string
  contactPhone?: string
  addressLine1: string
  addressLine2?: string
  city: string
  region?: string
  postalCode: string
  countryCode: string
  verificationStatus: OrganizationStatus
  createdAt: string
}

export interface AdminNgoMembership {
  id: string
  organizationId: string
  organizationName: string
  userId: string
  userEmail: string
  firstName: string
  lastName: string
  status: MembershipStatus
  createdAt: string
}
