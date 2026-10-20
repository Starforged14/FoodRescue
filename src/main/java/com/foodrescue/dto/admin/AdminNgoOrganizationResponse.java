package com.foodrescue.dto.admin;

import java.time.Instant;
import java.util.UUID;

import com.foodrescue.entity.OrganizationStatus;

public record AdminNgoOrganizationResponse(
    UUID id,
    String name,
    String registrationNumber,
    String contactEmail,
    String contactPhone,
    String addressLine1,
    String addressLine2,
    String city,
    String region,
    String postalCode,
    String countryCode,
    OrganizationStatus verificationStatus,
    Instant createdAt
) {
}
