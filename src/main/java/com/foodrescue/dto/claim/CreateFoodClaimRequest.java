package com.foodrescue.dto.claim;

import java.util.UUID;

import com.foodrescue.entity.FulfillmentMode;
import jakarta.validation.constraints.NotNull;

public record CreateFoodClaimRequest(
    @NotNull UUID organizationId,
    @NotNull FulfillmentMode fulfillmentMode
) {
}
