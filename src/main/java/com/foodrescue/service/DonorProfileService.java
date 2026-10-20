package com.foodrescue.service;

import java.util.Locale;
import java.util.UUID;
import java.math.BigDecimal;

import com.foodrescue.dto.profile.DonorProfileRequest;
import com.foodrescue.dto.profile.DonorProfileResponse;
import com.foodrescue.entity.DonorProfile;
import com.foodrescue.entity.UserAccount;
import com.foodrescue.exception.BadRequestException;
import com.foodrescue.exception.ResourceNotFoundException;
import com.foodrescue.repository.DonorProfileRepository;
import com.foodrescue.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DonorProfileService {

    private final DonorProfileRepository donorProfileRepository;
    private final UserAccountRepository userAccountRepository;

    public DonorProfileService(
        DonorProfileRepository donorProfileRepository,
        UserAccountRepository userAccountRepository
    ) {
        this.donorProfileRepository = donorProfileRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public DonorProfileResponse save(UUID userId, DonorProfileRequest request) {
        validateCoordinates(request.latitude(), request.longitude());
        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User account was not found"));
        DonorProfile profile = donorProfileRepository.findById(userId).orElseGet(DonorProfile::new);
        profile.setUser(user);
        profile.setOrganizationName(request.organizationName().trim());
        profile.setOrganizationType(request.organizationType());
        profile.setAddressLine1(request.addressLine1().trim());
        profile.setAddressLine2(trimToNull(request.addressLine2()));
        profile.setCity(request.city().trim());
        profile.setRegion(trimToNull(request.region()));
        profile.setPostalCode(request.postalCode().trim());
        profile.setCountryCode(request.countryCode().trim().toUpperCase(Locale.ROOT));
        profile.setLatitude(request.latitude());
        profile.setLongitude(request.longitude());
        return toResponse(donorProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public DonorProfileResponse get(UUID userId) {
        DonorProfile profile = donorProfileRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Donor profile was not found"));
        return toResponse(profile);
    }

    private DonorProfileResponse toResponse(DonorProfile profile) {
        return new DonorProfileResponse(
            profile.getId(),
            profile.getOrganizationName(),
            profile.getOrganizationType(),
            profile.getAddressLine1(),
            profile.getAddressLine2(),
            profile.getCity(),
            profile.getRegion(),
            profile.getPostalCode(),
            profile.getCountryCode(),
            profile.getLatitude(),
            profile.getLongitude()
        );
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new BadRequestException("Latitude and longitude must be provided together");
        }
    }
}
