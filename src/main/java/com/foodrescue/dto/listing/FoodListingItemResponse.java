package com.foodrescue.dto.listing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.foodrescue.entity.FoodCategory;
import com.foodrescue.entity.QuantityUnit;

public record FoodListingItemResponse(
    UUID id,
    String name,
    FoodCategory category,
    BigDecimal quantity,
    QuantityUnit unit,
    String allergenNotes,
    String dietaryNotes,
    Instant bestBeforeAt
) {
}
