package com.foodrescue.dto.ngo;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record NgoOrganizationRequest(
    @NotBlank @Size(max = 200) String name,
    @Size(max = 100) String registrationNumber,
    @NotBlank @Email @Size(max = 320) String contactEmail,
    @Size(max = 30) String contactPhone,
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
