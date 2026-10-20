package com.foodrescue.service;

import java.time.Instant;
import java.util.UUID;

import com.foodrescue.dto.claim.CancelFoodClaimRequest;
import com.foodrescue.dto.claim.CreateFoodClaimRequest;
import com.foodrescue.dto.claim.FoodClaimResponse;
import com.foodrescue.entity.ClaimStatus;
import com.foodrescue.entity.FoodClaim;
import com.foodrescue.entity.FoodListing;
import com.foodrescue.entity.FulfillmentMode;
import com.foodrescue.entity.ListingStatus;
import com.foodrescue.entity.MembershipStatus;
import com.foodrescue.entity.NgoOrganization;
import com.foodrescue.entity.OrganizationStatus;
import com.foodrescue.entity.UserAccount;
import com.foodrescue.exception.ConflictException;
import com.foodrescue.exception.ForbiddenException;
import com.foodrescue.exception.ResourceNotFoundException;
import com.foodrescue.mapper.FoodClaimMapper;
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
public class FoodClaimService {

    private final FoodClaimRepository foodClaimRepository;
    private final FoodListingRepository foodListingRepository;
    private final NgoMembershipRepository ngoMembershipRepository;
    private final NgoOrganizationRepository ngoOrganizationRepository;
    private final UserAccountRepository userAccountRepository;
    private final FoodClaimMapper foodClaimMapper;
    private final DeliveryTaskService deliveryTaskService;

    public FoodClaimService(
        FoodClaimRepository foodClaimRepository,
        FoodListingRepository foodListingRepository,
        NgoMembershipRepository ngoMembershipRepository,
        NgoOrganizationRepository ngoOrganizationRepository,
        UserAccountRepository userAccountRepository,
        FoodClaimMapper foodClaimMapper,
        DeliveryTaskService deliveryTaskService
    ) {
        this.foodClaimRepository = foodClaimRepository;
        this.foodListingRepository = foodListingRepository;
        this.ngoMembershipRepository = ngoMembershipRepository;
        this.ngoOrganizationRepository = ngoOrganizationRepository;
        this.userAccountRepository = userAccountRepository;
        this.foodClaimMapper = foodClaimMapper;
        this.deliveryTaskService = deliveryTaskService;
    }

    @Transactional
    public FoodClaimResponse claim(
        UUID userId,
        UUID listingId,
        CreateFoodClaimRequest request
    ) {
        FoodListing listing = foodListingRepository.findByIdForUpdate(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Food listing was not found"));

        NgoOrganization organization = ngoOrganizationRepository.findById(request.organizationId())
            .orElseThrow(() -> new ResourceNotFoundException("NGO organization was not found"));
        requireActiveMembership(organization.getId(), userId);
        if (organization.getVerificationStatus() != OrganizationStatus.VERIFIED) {
            throw new ForbiddenException("NGO organization is not verified");
        }

        Instant now = Instant.now();
        if (listing.getStatus() != ListingStatus.AVAILABLE
            || !listing.getAvailableUntil().isAfter(now)) {
            throw new ConflictException("Food listing is not available for claiming");
        }
        if (foodClaimRepository.existsByListingIdAndStatus(listingId, ClaimStatus.ACTIVE)) {
            throw new ConflictException("Food listing already has an active claim");
        }

        UserAccount claimant = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User account was not found"));
        FoodClaim claim = new FoodClaim();
        claim.setListing(listing);
        claim.setOrganization(organization);
        claim.setClaimedBy(claimant);
        claim.setStatus(ClaimStatus.ACTIVE);
        claim.setFulfillmentMode(request.fulfillmentMode());
        claim.setClaimedAt(now);
        listing.setStatus(ListingStatus.CLAIMED);

        FoodClaim savedClaim = foodClaimRepository.save(claim);
        deliveryTaskService.createForClaim(savedClaim);
        return foodClaimMapper.toResponse(savedClaim);
    }

    @Transactional(readOnly = true)
    public Page<FoodClaimResponse> listActiveClaims(
        UUID userId,
        UUID organizationId,
        Pageable pageable
    ) {
        requireActiveMembership(organizationId, userId);
        return foodClaimRepository.findAllByOrganizationIdAndStatus(
                organizationId,
                ClaimStatus.ACTIVE,
                pageable
            )
            .map(foodClaimMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public FoodClaimResponse getClaim(UUID userId, UUID claimId) {
        FoodClaim claim = foodClaimRepository.findById(claimId)
            .orElseThrow(() -> new ResourceNotFoundException("Food claim was not found"));
        requireActiveMembership(claim.getOrganization().getId(), userId);
        return foodClaimMapper.toResponse(claim);
    }

    @Transactional
    public FoodClaimResponse cancel(
        UUID userId,
        UUID claimId,
        CancelFoodClaimRequest request
    ) {
        FoodClaim claim = lockClaimWithListingFirst(claimId);
        requireActiveMembership(claim.getOrganization().getId(), userId);
        if (claim.getStatus() != ClaimStatus.ACTIVE) {
            throw new ConflictException("Only active claims can be cancelled");
        }

        Instant now = Instant.now();
        if (claim.getFulfillmentMode() == FulfillmentMode.VOLUNTEER_DELIVERY) {
            deliveryTaskService.cancelForClaim(claim.getId(), request.reason());
        }
        claim.setStatus(ClaimStatus.CANCELLED);
        claim.setCancelledAt(now);
        claim.setCancellationReason(trimToNull(request.reason()));
        FoodListing listing = claim.getListing();
        listing.setStatus(
            listing.getAvailableUntil().isAfter(now)
                ? ListingStatus.AVAILABLE
                : ListingStatus.EXPIRED
        );
        return foodClaimMapper.toResponse(claim);
    }

    @Transactional
    public FoodClaimResponse complete(UUID userId, UUID claimId) {
        FoodClaim claim = lockClaimWithListingFirst(claimId);
        requireActiveMembership(claim.getOrganization().getId(), userId);
        if (claim.getStatus() != ClaimStatus.ACTIVE) {
            throw new ConflictException("Only active claims can be completed");
        }
        if (claim.getFulfillmentMode() != FulfillmentMode.NGO_PICKUP) {
            throw new ConflictException("Volunteer delivery claims are completed through the delivery workflow");
        }

        Instant now = Instant.now();
        if (!claim.getListing().getAvailableUntil().isAfter(now)) {
            throw new ConflictException("Expired claims cannot be completed");
        }
        claim.setStatus(ClaimStatus.COMPLETED);
        claim.setCompletedAt(now);
        claim.getListing().setStatus(ListingStatus.COMPLETED);
        return foodClaimMapper.toResponse(claim);
    }

    private void requireActiveMembership(UUID organizationId, UUID userId) {
        if (!ngoMembershipRepository.existsByOrganizationIdAndUserIdAndStatus(
            organizationId,
            userId,
            MembershipStatus.ACTIVE
        )) {
            throw new ForbiddenException("An active membership in this NGO is required");
        }
    }

    private FoodClaim lockClaimWithListingFirst(UUID claimId) {
        UUID listingId = foodClaimRepository.findListingIdById(claimId)
            .orElseThrow(() -> new ResourceNotFoundException("Food claim was not found"));
        foodListingRepository.findByIdForUpdate(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Food listing was not found"));
        return foodClaimRepository.findByIdForUpdate(claimId)
            .orElseThrow(() -> new ResourceNotFoundException("Food claim was not found"));
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
