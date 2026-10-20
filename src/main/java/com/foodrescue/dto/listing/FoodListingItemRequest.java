package com.foodrescue.dto.listing;

import java.math.BigDecimal;
import java.time.Instant;

import com.foodrescue.entity.FoodCategory;
import com.foodrescue.entity.QuantityUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FoodListingItemRequest(
    @NotBlank @Size(max = 200) String name,
    @NotNull FoodCategory category,
    @NotNull @DecimalMin("0.001") BigDecimal quantity,
    @NotNull QuantityUnit unit,
    @Size(max = 2000) String allergenNotes,
    @Size(max = 2000) String dietaryNotes,
    Instant bestBeforeAt
) {
}
