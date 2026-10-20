package com.foodrescue.dto.listing;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FoodListingRequest(
    @NotBlank @Size(max = 200) String title,
    @Size(max = 5000) String description,
    @NotNull @Future Instant availableUntil,
    @NotNull @Future Instant pickupWindowStart,
    @NotNull @Future Instant pickupWindowEnd,
    @Size(max = 5000) String pickupInstructions,
    @NotEmpty @Size(max = 50) List<@Valid FoodListingItemRequest> items
) {
}
