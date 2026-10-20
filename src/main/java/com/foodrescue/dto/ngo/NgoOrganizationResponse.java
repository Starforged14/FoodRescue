package com.foodrescue.dto.ngo;

import java.math.BigDecimal;
import java.util.UUID;

import com.foodrescue.entity.MembershipStatus;
import com.foodrescue.entity.OrganizationStatus;

public record NgoOrganizationResponse(
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
    BigDecimal latitude,
    BigDecimal longitude,
    OrganizationStatus verificationStatus,
    MembershipStatus membershipStatus
) {
}
