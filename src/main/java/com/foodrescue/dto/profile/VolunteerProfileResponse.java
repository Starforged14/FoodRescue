package com.foodrescue.dto.profile;

import java.math.BigDecimal;
import java.util.UUID;

import com.foodrescue.entity.VolunteerAvailability;

public record VolunteerProfileResponse(
    UUID userId,
    VolunteerAvailability availabilityStatus,
    BigDecimal maxTravelDistanceKm
) {
}
