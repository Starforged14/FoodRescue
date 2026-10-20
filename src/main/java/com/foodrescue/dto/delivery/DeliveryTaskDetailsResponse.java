package com.foodrescue.dto.delivery;

import java.time.Instant;
import java.util.UUID;

import com.foodrescue.dto.claim.PickupAddressResponse;
import com.foodrescue.entity.DeliveryStatus;

public record DeliveryTaskDetailsResponse(
    UUID id,
    UUID claimId,
    String foodTitle,
    DeliveryStatus status,
    Instant pickupWindowStart,
    Instant pickupWindowEnd,
    Instant assignedAt,
    Instant pickedUpAt,
    Instant deliveredAt,
    String failureReason,
    PickupAddressResponse pickupAddress,
    PickupAddressResponse dropoffAddress
) {
}
