-- FoodRescue PostgreSQL Schema Definition
-- Compatible with Hibernate 'ddl-auto: validate'

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    status VARCHAR(32) NOT NULL,
    email_verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- 2. User Roles Collection Table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(32) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_roles_user_role UNIQUE (user_id, role)
);

-- 3. Donor Profiles
CREATE TABLE IF NOT EXISTS donor_profiles (
    user_id UUID PRIMARY KEY,
    organization_name VARCHAR(200) NOT NULL,
    organization_type VARCHAR(32) NOT NULL,
    address_line1 VARCHAR(200) NOT NULL,
    address_line2 VARCHAR(200),
    city VARCHAR(100) NOT NULL,
    region VARCHAR(100),
    postal_code VARCHAR(20) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    latitude NUMERIC(9, 6),
    longitude NUMERIC(9, 6),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_donor_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. NGO Organizations
CREATE TABLE IF NOT EXISTS ngo_organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    registration_number VARCHAR(100),
    contact_email VARCHAR(320) NOT NULL,
    contact_phone VARCHAR(30),
    address_line1 VARCHAR(200) NOT NULL,
    address_line2 VARCHAR(200),
    city VARCHAR(100) NOT NULL,
    region VARCHAR(100),
    postal_code VARCHAR(20) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    latitude NUMERIC(9, 6),
    longitude NUMERIC(9, 6),
    verification_status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_ngo_registration_number UNIQUE (registration_number)
);

-- 5. NGO Memberships
CREATE TABLE IF NOT EXISTS ngo_memberships (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_ngo_memberships_org FOREIGN KEY (organization_id) REFERENCES ngo_organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_ngo_memberships_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_ngo_membership_org_user UNIQUE (organization_id, user_id)
);

-- 6. Volunteer Profiles
CREATE TABLE IF NOT EXISTS volunteer_profiles (
    user_id UUID PRIMARY KEY,
    availability_status VARCHAR(32) NOT NULL,
    max_travel_distance_km NUMERIC(6, 2),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_volunteer_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 7. Food Listings
CREATE TABLE IF NOT EXISTS food_listings (
    id UUID PRIMARY KEY,
    donor_user_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL,
    available_until TIMESTAMP WITH TIME ZONE NOT NULL,
    pickup_window_start TIMESTAMP WITH TIME ZONE NOT NULL,
    pickup_window_end TIMESTAMP WITH TIME ZONE NOT NULL,
    pickup_instructions TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_food_listings_donor FOREIGN KEY (donor_user_id) REFERENCES donor_profiles(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_food_listings_pickup_window CHECK (pickup_window_end > pickup_window_start)
);

CREATE INDEX IF NOT EXISTS idx_food_listings_status_deadline ON food_listings (status, available_until);
CREATE INDEX IF NOT EXISTS idx_food_listings_donor_created ON food_listings (donor_user_id, created_at);

-- 8. Food Listing Items
CREATE TABLE IF NOT EXISTS food_listing_items (
    id UUID PRIMARY KEY,
    listing_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(32) NOT NULL,
    quantity NUMERIC(12, 3) NOT NULL,
    unit VARCHAR(32) NOT NULL,
    allergen_notes TEXT,
    dietary_notes TEXT,
    best_before_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_food_listing_items_listing FOREIGN KEY (listing_id) REFERENCES food_listings(id) ON DELETE CASCADE,
    CONSTRAINT chk_food_listing_items_quantity CHECK (quantity > 0)
);

-- 9. Food Claims
CREATE TABLE IF NOT EXISTS food_claims (
    id UUID PRIMARY KEY,
    listing_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    claimed_by_user_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    fulfillment_mode VARCHAR(32) NOT NULL,
    claimed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    cancellation_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_food_claims_listing FOREIGN KEY (listing_id) REFERENCES food_listings(id) ON DELETE CASCADE,
    CONSTRAINT fk_food_claims_organization FOREIGN KEY (organization_id) REFERENCES ngo_organizations(id) ON DELETE CASCADE,
    CONSTRAINT fk_food_claims_claimed_by FOREIGN KEY (claimed_by_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_food_claims_org_status_claimed ON food_claims (organization_id, status, claimed_at);
CREATE INDEX IF NOT EXISTS idx_food_claims_listing_status ON food_claims (listing_id, status);

-- 10. Delivery Tasks
CREATE TABLE IF NOT EXISTS delivery_tasks (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    pickup_address_line1 VARCHAR(200) NOT NULL,
    pickup_address_line2 VARCHAR(200),
    pickup_city VARCHAR(100) NOT NULL,
    pickup_region VARCHAR(100),
    pickup_postal_code VARCHAR(20) NOT NULL,
    pickup_country_code VARCHAR(2) NOT NULL,
    dropoff_address_line1 VARCHAR(200) NOT NULL,
    dropoff_address_line2 VARCHAR(200),
    dropoff_city VARCHAR(100) NOT NULL,
    dropoff_region VARCHAR(100),
    dropoff_postal_code VARCHAR(20) NOT NULL,
    dropoff_country_code VARCHAR(2) NOT NULL,
    pickup_window_start TIMESTAMP WITH TIME ZONE NOT NULL,
    pickup_window_end TIMESTAMP WITH TIME ZONE NOT NULL,
    pickup_instructions TEXT,
    assigned_at TIMESTAMP WITH TIME ZONE,
    picked_up_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    failure_reason TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_delivery_tasks_claim FOREIGN KEY (claim_id) REFERENCES food_claims(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_delivery_tasks_status_created ON delivery_tasks (status, created_at);

-- 11. Delivery Assignments
CREATE TABLE IF NOT EXISTS delivery_assignments (
    id UUID PRIMARY KEY,
    delivery_task_id UUID NOT NULL,
    volunteer_user_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    offered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    responded_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_delivery_assignments_task FOREIGN KEY (delivery_task_id) REFERENCES delivery_tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_delivery_assignments_volunteer FOREIGN KEY (volunteer_user_id) REFERENCES volunteer_profiles(user_id) ON DELETE CASCADE
);

-- 12. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    reference_type VARCHAR(50) NOT NULL,
    reference_id UUID NOT NULL,
    read_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_created ON notifications (user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications (user_id, read_at);
