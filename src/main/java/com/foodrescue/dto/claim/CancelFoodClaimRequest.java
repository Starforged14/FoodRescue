package com.foodrescue.dto.claim;

import jakarta.validation.constraints.Size;

public record CancelFoodClaimRequest(
    @Size(max = 2000) String reason
) {
}
