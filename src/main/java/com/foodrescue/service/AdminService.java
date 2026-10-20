package com.foodrescue.service;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import com.foodrescue.dto.admin.AdminDashboardMetricsResponse;
import com.foodrescue.dto.admin.AdminNgoMembershipResponse;
import com.foodrescue.dto.admin.AdminNgoOrganizationResponse;
import com.foodrescue.entity.AppRole;
import com.foodrescue.entity.ClaimStatus;
import com.foodrescue.entity.DeliveryStatus;
import com.foodrescue.entity.ListingStatus;
import com.foodrescue.entity.MembershipStatus;
import com.foodrescue.entity.NgoMembership;
import com.foodrescue.entity.NgoOrganization;
import com.foodrescue.entity.OrganizationStatus;
import com.foodrescue.exception.ConflictException;
import com.foodrescue.exception.ResourceNotFoundException;
import com.foodrescue.repository.DeliveryTaskRepository;
import com.foodrescue.repository.FoodClaimRepository;
import com.foodrescue.repository.FoodListingRepository;
import com.foodrescue.repository.NgoMembershipRepository;
import com.foodrescue.repository.NgoOrganizationRepository;
import com.foodrescue.repository.UserAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserAccountRepository userAccountRepository;
    private final FoodListingRepository foodListingRepository;
    private final FoodClaimRepository foodClaimRepository;
    private final DeliveryTaskRepository deliveryTaskRepository;
    private final NgoOrganizationRepository ngoOrganizationRepository;
    private final NgoMembershipRepository ngoMembershipRepository;

    public AdminService(
        UserAccountRepository userAccountRepository,
        FoodListingRepository foodListingRepository,
        FoodClaimRepository foodClaimRepository,
        DeliveryTaskRepository deliveryTaskRepository,
        NgoOrganizationRepository ngoOrganizationRepository,
        NgoMembershipRepository ngoMembershipRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.foodListingRepository = foodListingRepository;
        this.foodClaimRepository = foodClaimRepository;
        this.deliveryTaskRepository = deliveryTaskRepository;
        this.ngoOrganizationRepository = ngoOrganizationRepository;
        this.ngoMembershipRepository = ngoMembershipRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardMetricsResponse getDashboardMetrics() {
        Map<AppRole, Long> usersByRole = new EnumMap<>(AppRole.class);
        for (AppRole role : AppRole.values()) {
            usersByRole.put(role, userAccountRepository.countUsersByRole(role));
        }

        Map<ListingStatus, Long> listingsByStatus = new EnumMap<>(ListingStatus.class);
        for (ListingStatus status : ListingStatus.values()) {
            listingsByStatus.put(status, foodListingRepository.countByStatus(status));
        }

        Map<ClaimStatus, Long> claimsByStatus = new EnumMap<>(ClaimStatus.class);
        for (ClaimStatus status : ClaimStatus.values()) {
            claimsByStatus.put(status, foodClaimRepository.countByStatus(status));
        }

        Map<DeliveryStatus, Long> deliveriesByStatus = new EnumMap<>(DeliveryStatus.class);
        for (DeliveryStatus status : DeliveryStatus.values()) {
            deliveriesByStatus.put(status, deliveryTaskRepository.countByStatus(status));
        }

        return new AdminDashboardMetricsResponse(
            userAccountRepository.count(),
            Map.copyOf(usersByRole),
            Map.copyOf(listingsByStatus),
            Map.copyOf(claimsByStatus),
            Map.copyOf(deliveriesByStatus),
            ngoOrganizationRepository.countByVerificationStatus(OrganizationStatus.PENDING_REVIEW),
            ngoMembershipRepository.countByStatus(MembershipStatus.PENDING)
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminNgoOrganizationResponse> listPendingOrganizations(Pageable pageable) {
        return ngoOrganizationRepository
            .findAllByVerificationStatusOrderByCreatedAtAsc(
                OrganizationStatus.PENDING_REVIEW,
                pageable
            )
            .map(this::toOrganizationResponse);
    }

    @Transactional
    public AdminNgoOrganizationResponse approveOrganization(UUID organizationId) {
        return reviewOrganization(organizationId, OrganizationStatus.VERIFIED);
    }

    @Transactional
    public AdminNgoOrganizationResponse rejectOrganization(UUID organizationId) {
        return reviewOrganization(organizationId, OrganizationStatus.REJECTED);
    }

    @Transactional(readOnly = true)
    public Page<AdminNgoMembershipResponse> listPendingMemberships(Pageable pageable) {
        return ngoMembershipRepository.findAllByStatusOrderByCreatedAtAsc(
                MembershipStatus.PENDING,
                pageable
            )
            .map(this::toMembershipResponse);
    }

    @Transactional
    public AdminNgoMembershipResponse approveMembership(UUID membershipId) {
        return reviewMembership(membershipId, MembershipStatus.ACTIVE);
    }

    @Transactional
    public AdminNgoMembershipResponse rejectMembership(UUID membershipId) {
        return reviewMembership(membershipId, MembershipStatus.REJECTED);
    }

    private AdminNgoOrganizationResponse reviewOrganization(
        UUID organizationId,
        OrganizationStatus decision
    ) {
        NgoOrganization organization = ngoOrganizationRepository.findByIdForUpdate(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("NGO organization was not found"));
        if (organization.getVerificationStatus() != OrganizationStatus.PENDING_REVIEW) {
            throw new ConflictException("Only organizations pending review can be reviewed");
        }
        organization.setVerificationStatus(decision);
        return toOrganizationResponse(organization);
    }

    private AdminNgoMembershipResponse reviewMembership(
        UUID membershipId,
        MembershipStatus decision
    ) {
        NgoMembership membership = ngoMembershipRepository.findByIdForUpdate(membershipId)
            .orElseThrow(() -> new ResourceNotFoundException("NGO membership was not found"));
        if (membership.getStatus() != MembershipStatus.PENDING) {
            throw new ConflictException("Only memberships pending review can be reviewed");
        }
        membership.setStatus(decision);
        if (decision == MembershipStatus.ACTIVE) {
            membership.setJoinedAt(java.time.Instant.now());
        }
        return toMembershipResponse(membership);
    }

    private AdminNgoOrganizationResponse toOrganizationResponse(NgoOrganization organization) {
        return new AdminNgoOrganizationResponse(
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
            organization.getVerificationStatus(),
            organization.getCreatedAt()
        );
    }

    private AdminNgoMembershipResponse toMembershipResponse(NgoMembership membership) {
        return new AdminNgoMembershipResponse(
            membership.getId(),
            membership.getOrganization().getId(),
            membership.getOrganization().getName(),
            membership.getUser().getId(),
            membership.getUser().getEmail(),
            membership.getUser().getFirstName(),
            membership.getUser().getLastName(),
            membership.getStatus(),
            membership.getCreatedAt()
        );
    }
}
