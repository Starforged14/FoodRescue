import type {
  AdminDashboardMetrics,
  AdminNgoMembership,
  AdminNgoOrganization,
  DeliveryTask,
  DonorProfile,
  FoodClaim,
  FoodListing,
  NgoOrganization,
  VolunteerProfile,
} from '../types/platform'

export const mockDonorProfile: DonorProfile = {
  userId: 'demo-donor-id',
  organizationName: 'Green Table Bistro',
  organizationType: 'RESTAURANT',
  addressLine1: '124 Market Street',
  addressLine2: 'Suite 400',
  city: 'Portland',
  region: 'OR',
  postalCode: '97201',
  countryCode: 'US',
}

export const mockListings: FoodListing[] = [
  {
    id: 'mock-listing-1',
    title: 'Surplus Fresh Sandwiches & Salads',
    description: 'Freshly packed gourmet sandwiches, salads, and vegetable wraps from today lunch catering.',
    status: 'AVAILABLE',
    donorOrganizationName: 'Green Table Bistro',
    pickupCity: 'Portland',
    pickupRegion: 'OR',
    pickupCountryCode: 'US',
    pickupInstructions: 'Pick up at the rear kitchen entrance. Ring bell #3.',
    availableUntil: new Date(Date.now() + 86400000).toISOString(),
    pickupWindowStart: new Date(Date.now() + 3600000).toISOString(),
    pickupWindowEnd: new Date(Date.now() + 18000000).toISOString(),
    items: [
      {
        id: 'mock-item-1',
        name: 'Turkey & Cranberry Baguettes',
        category: 'PREPARED_MEAL',
        quantity: 24,
        unit: 'SERVING',
        allergenNotes: 'Contains Gluten, Dairy',
      },
      {
        id: 'mock-item-2',
        name: 'Mixed Mediterranean Salad Bowls',
        category: 'PRODUCE',
        quantity: 15,
        unit: 'BOX',
        dietaryNotes: 'Vegetarian, Gluten-Free',
      },
    ],
  },
  {
    id: 'mock-listing-2',
    title: 'Artisan Bakery Loaves & Croissants',
    description: 'Day-end baked sourdough loaves, baguettes, and fresh almond croissants.',
    status: 'AVAILABLE',
    donorOrganizationName: 'Sunrise Bakery',
    pickupCity: 'Portland',
    pickupRegion: 'OR',
    pickupCountryCode: 'US',
    pickupInstructions: 'Front counter with staff member on duty.',
    availableUntil: new Date(Date.now() + 86400000).toISOString(),
    pickupWindowStart: new Date(Date.now() + 7200000).toISOString(),
    pickupWindowEnd: new Date(Date.now() + 21600000).toISOString(),
    items: [
      {
        id: 'mock-item-3',
        name: 'Sourdough Country Loaves',
        category: 'BAKERY',
        quantity: 18,
        unit: 'PIECE',
      },
    ],
  },
]

export const mockNgoOrganization: NgoOrganization = {
  id: 'mock-org-1',
  name: 'Hope Community Shelter',
  registrationNumber: 'NGO-884920',
  verificationStatus: 'VERIFIED',
  membershipStatus: 'ACTIVE',
  contactEmail: 'coordinator@shelter.org',
  contactPhone: '+1 (503) 555-0199',
  addressLine1: '782 Hope Boulevard',
  city: 'Portland',
  region: 'OR',
  postalCode: '97205',
  countryCode: 'US',
}

export const mockVolunteerProfile: VolunteerProfile = {
  userId: 'demo-volunteer-id',
  availabilityStatus: 'AVAILABLE',
  maxTravelDistanceKm: 25,
}

export const mockDeliveryTasks: (DeliveryTask & {
  pickupCity: string
  pickupRegion?: string
  dropoffCity: string
  dropoffRegion?: string
})[] = [
  {
    id: 'mock-task-1',
    claimId: 'mock-claim-1',
    foodTitle: 'Surplus Fresh Sandwiches & Salads',
    status: 'OPEN',
    pickupWindowStart: new Date(Date.now() + 3600000).toISOString(),
    pickupWindowEnd: new Date(Date.now() + 18000000).toISOString(),
    pickupCity: 'Portland',
    pickupRegion: 'OR',
    dropoffCity: 'Portland',
    dropoffRegion: 'OR',
    pickupAddress: {
      addressLine1: '124 Market Street',
      city: 'Portland',
      region: 'OR',
      postalCode: '97201',
      countryCode: 'US',
    },
    dropoffAddress: {
      addressLine1: '782 Hope Boulevard',
      city: 'Portland',
      region: 'OR',
      postalCode: '97205',
      countryCode: 'US',
    },
  },
]

export const mockClaims: FoodClaim[] = [
  {
    id: 'mock-claim-1',
    listingId: 'mock-listing-1',
    listingTitle: 'Surplus Fresh Sandwiches & Salads',
    organizationId: 'mock-org-1',
    organizationName: 'Hope Community Shelter',
    claimedByUserId: 'demo-ngo-user',
    status: 'ACTIVE',
    fulfillmentMode: 'VOLUNTEER_DELIVERY',
    claimedAt: new Date(Date.now() - 1800000).toISOString(),
  },
]

export const mockAdminMetrics: AdminDashboardMetrics = {
  totalUsers: 142,
  usersByRole: {
    DONOR: 38,
    NGO: 24,
    VOLUNTEER: 78,
    ADMIN: 2,
  },
  listingsByStatus: {
    AVAILABLE: 18,
    CLAIMED: 12,
    COMPLETED: 240,
    EXPIRED: 42,
  },
  claimsByStatus: {
    ACTIVE: 12,
    COMPLETED: 240,
    CANCELLED: 15,
  },
  deliveriesByStatus: {
    OPEN: 5,
    ASSIGNED: 7,
    PICKED_UP: 3,
    DELIVERED: 220,
    FAILED: 5,
  },
  pendingNgoOrganizations: 1,
  pendingNgoMemberships: 1,
}

export const mockAdminPendingOrganizations: AdminNgoOrganization[] = [
  {
    id: 'mock-pending-org-1',
    name: 'Eastside Food Pantry',
    contactEmail: 'pantry@eastside.org',
    contactPhone: '+1-555-0144',
    addressLine1: '404 Grand Avenue',
    city: 'Portland',
    region: 'OR',
    postalCode: '97214',
    countryCode: 'US',
    verificationStatus: 'PENDING_REVIEW',
    registrationNumber: 'NGO-10928',
    createdAt: new Date(Date.now() - 172800000).toISOString(),
  },
]

export const mockAdminPendingMemberships: AdminNgoMembership[] = [
  {
    id: 'mock-pending-mem-1',
    organizationId: 'mock-pending-org-1',
    organizationName: 'Eastside Food Pantry',
    userId: 'mock-user-456',
    userEmail: 'julia.santos@eastside.org',
    firstName: 'Julia',
    lastName: 'Santos',
    status: 'PENDING',
    createdAt: new Date(Date.now() - 86400000).toISOString(),
  },
]
