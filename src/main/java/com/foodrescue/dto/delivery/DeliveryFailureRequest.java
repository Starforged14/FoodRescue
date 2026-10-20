package com.foodrescue.dto.delivery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeliveryFailureRequest(
    @NotBlank @Size(max = 2000) String reason
) {
}
