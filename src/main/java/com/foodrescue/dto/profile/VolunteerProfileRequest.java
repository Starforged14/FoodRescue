package com.foodrescue.dto.profile;

import java.math.BigDecimal;

import com.foodrescue.entity.VolunteerAvailability;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record VolunteerProfileRequest(
    @NotNull VolunteerAvailability availabilityStatus,
    @DecimalMin("0.1") @DecimalMax("9999.99") BigDecimal maxTravelDistanceKm
) {
}
