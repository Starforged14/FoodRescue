package com.foodrescue.dto.listing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.foodrescue.entity.ListingStatus;

public record FoodListingResponse(
    UUID id,
    String donorOrganizationName,
    String title,
    String description,
    ListingStatus status,
    Instant availableUntil,
    Instant pickupWindowStart,
    Instant pickupWindowEnd,
    String pickupCity,
    String pickupRegion,
    String pickupCountryCode,
    String pickupInstructions,
    List<FoodListingItemResponse> items
) {
}
