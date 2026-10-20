package com.foodrescue.dto.delivery;

import java.time.Instant;
import java.util.UUID;

import com.foodrescue.entity.DeliveryStatus;

public record DeliveryTaskSummaryResponse(
    UUID id,
    UUID claimId,
    String foodTitle,
    DeliveryStatus status,
    Instant pickupWindowStart,
    Instant pickupWindowEnd,
    String pickupCity,
    String pickupRegion,
    String dropoffCity,
    String dropoffRegion
) {
}
