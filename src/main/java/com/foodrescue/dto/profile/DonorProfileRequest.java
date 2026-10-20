package com.foodrescue.dto.profile;

import java.math.BigDecimal;

import com.foodrescue.entity.DonorType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DonorProfileRequest(
    @NotBlank @Size(max = 200) String organizationName,
    @NotNull DonorType organizationType,
    @NotBlank @Size(max = 200) String addressLine1,
    @Size(max = 200) String addressLine2,
    @NotBlank @Size(max = 100) String city,
    @Size(max = 100) String region,
    @NotBlank @Size(max = 20) String postalCode,
    @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") String countryCode,
    @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
    @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude
) {
}
