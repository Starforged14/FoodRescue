package com.foodrescue.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.math.BigDecimal;

import com.foodrescue.dto.ngo.NgoOrganizationRequest;
import com.foodrescue.dto.ngo.NgoOrganizationResponse;
import com.foodrescue.entity.MembershipStatus;
import com.foodrescue.entity.NgoMembership;
import com.foodrescue.entity.NgoOrganization;
import com.foodrescue.entity.OrganizationStatus;
import com.foodrescue.entity.UserAccount;
import com.foodrescue.exception.BadRequestException;
import com.foodrescue.exception.ResourceNotFoundException;
import com.foodrescue.repository.NgoMembershipRepository;
import com.foodrescue.repository.NgoOrganizationRepository;
import com.foodrescue.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NgoOrganizationService {

    private final NgoOrganizationRepository ngoOrganizationRepository;
    private final NgoMembershipRepository ngoMembershipRepository;
    private final UserAccountRepository userAccountRepository;

    public NgoOrganizationService(
        NgoOrganizationRepository ngoOrganizationRepository,
        NgoMembershipRepository ngoMembershipRepository,
        UserAccountRepository userAccountRepository
    ) {
        this.ngoOrganizationRepository = ngoOrganizationRepository;
        this.ngoMembershipRepository = ngoMembershipRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public NgoOrganizationResponse create(UUID userId, NgoOrganizationRequest request) {
        validateCoordinates(request.latitude(), request.longitude());
        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User account was not found"));

        NgoOrganization organization = new NgoOrganization();
        organization.setName(request.name().trim());
        organization.setRegistrationNumber(trimToNull(request.registrationNumber()));
        organization.setContactEmail(request.contactEmail().trim().toLowerCase(Locale.ROOT));
        organization.setContactPhone(trimToNull(request.contactPhone()));
        organization.setAddressLine1(request.addressLine1().trim());
        organization.setAddressLine2(trimToNull(request.addressLine2()));
        organization.setCity(request.city().trim());
        organization.setRegion(trimToNull(request.region()));
        organization.setPostalCode(request.postalCode().trim());
        organization.setCountryCode(request.countryCode().trim().toUpperCase(Locale.ROOT));
        organization.setLatitude(request.latitude());
        organization.setLongitude(request.longitude());
        organization.setVerificationStatus(OrganizationStatus.PENDING_REVIEW);
        NgoOrganization savedOrganization = ngoOrganizationRepository.save(organization);

        NgoMembership membership = new NgoMembership();
        membership.setOrganization(savedOrganization);
        membership.setUser(user);
        membership.setStatus(MembershipStatus.PENDING);
        ngoMembershipRepository.save(membership);

        return toResponse(savedOrganization, MembershipStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<NgoOrganizationResponse> getMyOrganizations(UUID userId) {
        return ngoMembershipRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(membership -> toResponse(membership.getOrganization(), membership.getStatus()))
            .toList();
    }

    @Transactional(readOnly = true)
    public NgoOrganizationResponse getOrganization(UUID userId, UUID organizationId) {
        NgoMembership membership = ngoMembershipRepository
            .findByOrganizationIdAndUserId(organizationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("NGO organization was not found"));
        return toResponse(membership.getOrganization(), membership.getStatus());
    }

    private NgoOrganizationResponse toResponse(
        NgoOrganization organization,
        MembershipStatus membershipStatus
    ) {
        return new NgoOrganizationResponse(
            organization.getId(),
            organization.getName(),
            organization.getRegistrationNumber(),
            organization.getContactEmail(),
            organization.getContactPhone(),
            organization.getAddressLine1(),
            organization.getAddressLine2(),
            organization.getCity(),
            organization.getRegion(),
            organization.getPostalCode(),
            organization.getCountryCode(),
            organization.getLatitude(),
            organization.getLongitude(),
            organization.getVerificationStatus(),
            membershipStatus
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
