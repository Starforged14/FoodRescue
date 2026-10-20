package com.foodrescue.dto.claim;

import java.time.Instant;
import java.util.UUID;

import com.foodrescue.entity.ClaimStatus;
import com.foodrescue.entity.FulfillmentMode;

public record FoodClaimResponse(
    UUID id,
    UUID listingId,
    String listingTitle,
    UUID organizationId,
    String organizationName,
    UUID claimedByUserId,
    ClaimStatus status,
    FulfillmentMode fulfillmentMode,
    Instant claimedAt,
    Instant cancelledAt,
    Instant completedAt,
    String cancellationReason,
    PickupAddressResponse pickupAddress
) {
}
