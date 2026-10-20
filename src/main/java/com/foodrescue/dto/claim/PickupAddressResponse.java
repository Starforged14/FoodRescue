package com.foodrescue.dto.claim;

public record PickupAddressResponse(
    String addressLine1,
    String addressLine2,
    String city,
    String region,
    String postalCode,
    String countryCode,
    String pickupInstructions
) {
}
