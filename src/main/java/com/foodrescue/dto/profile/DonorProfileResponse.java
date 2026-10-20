package com.foodrescue.dto.profile;

import java.math.BigDecimal;
import java.util.UUID;

import com.foodrescue.entity.DonorType;

public record DonorProfileResponse(
    UUID userId,
    String organizationName,
    DonorType organizationType,
    String addressLine1,
    String addressLine2,
    String city,
    String region,
    String postalCode,
    String countryCode,
    BigDecimal latitude,
    BigDecimal longitude
) {
}
