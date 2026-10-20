package com.foodrescue.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

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
import com.foodrescue.mapper.FoodClaimMapper;
import com.foodrescue.repository.FoodClaimRepository;
import com.foodrescue.repository.FoodListingRepository;
import com.foodrescue.repository.NgoMembershipRepository;
import com.foodrescue.repository.NgoOrganizationRepository;
import com.foodrescue.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FoodClaimServiceTest {

    @Mock
    private FoodClaimRepository foodClaimRepository;

    @Mock
    private FoodListingRepository foodListingRepository;

    @Mock
    private NgoMembershipRepository ngoMembershipRepository;

    @Mock
    private NgoOrganizationRepository ngoOrganizationRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private FoodClaimMapper foodClaimMapper;

    @Mock
    private DeliveryTaskService deliveryTaskService;

    @InjectMocks
    private FoodClaimService foodClaimService;

    private final UUID listingId = UUID.randomUUID();
    private final UUID organizationId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private FoodListing listing;
    private NgoOrganization organization;
    private UserAccount claimant;

    @BeforeEach
    void setUp() {
        listing = new FoodListing();
        listing.setId(listingId);
        listing.setTitle("Prepared meals");
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setAvailableUntil(Instant.now().plusSeconds(3600));

        organization = new NgoOrganization();
        organization.setId(organizationId);
        organization.setName("Community Kitchen");
        organization.setVerificationStatus(OrganizationStatus.VERIFIED);

        claimant = new UserAccount();
        claimant.setId(userId);

        when(foodListingRepository.findByIdForUpdate(listingId)).thenReturn(Optional.of(listing));
        when(ngoOrganizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(ngoMembershipRepository.existsByOrganizationIdAndUserIdAndStatus(
            organizationId,
            userId,
            MembershipStatus.ACTIVE
        )).thenReturn(true);
    }

    @Test
    void claimWithVolunteerDeliveryCreatesClaimAndDeliveryTask() {
        UUID claimId = UUID.randomUUID();
        when(foodClaimRepository.existsByListingIdAndStatus(listingId, ClaimStatus.ACTIVE))
            .thenReturn(false);
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(claimant));
        when(foodClaimRepository.save(any(FoodClaim.class))).thenAnswer(invocation -> {
            FoodClaim claim = invocation.getArgument(0);
            claim.setId(claimId);
            return claim;
        });
        FoodClaimResponse expectedResponse = new FoodClaimResponse(
            claimId,
            listingId,
            listing.getTitle(),
            organizationId,
            organization.getName(),
            userId,
            ClaimStatus.ACTIVE,
            FulfillmentMode.VOLUNTEER_DELIVERY,
            Instant.now(),
            null,
            null,
            null,
            null
        );
        when(foodClaimMapper.toResponse(any(FoodClaim.class))).thenReturn(expectedResponse);

        FoodClaimResponse response = foodClaimService.claim(
            userId,
            listingId,
            new CreateFoodClaimRequest(organizationId, FulfillmentMode.VOLUNTEER_DELIVERY)
        );

        assertEquals(expectedResponse, response);
        assertEquals(ListingStatus.CLAIMED, listing.getStatus());
        verify(foodClaimRepository).save(org.mockito.ArgumentMatchers.argThat(claim ->
            claim.getStatus() == ClaimStatus.ACTIVE
                && claim.getFulfillmentMode() == FulfillmentMode.VOLUNTEER_DELIVERY
                && claim.getOrganization() == organization
                && claim.getClaimedBy() == claimant
        ));
        verify(deliveryTaskService).createForClaim(org.mockito.ArgumentMatchers.argThat(claim ->
            claim.getId().equals(claimId)
                && claim.getFulfillmentMode() == FulfillmentMode.VOLUNTEER_DELIVERY
        ));
    }

    @Test
    void claimRejectsListingWithAnotherActiveClaim() {
        when(foodClaimRepository.existsByListingIdAndStatus(listingId, ClaimStatus.ACTIVE))
            .thenReturn(true);

        assertThrows(
            ConflictException.class,
            () -> foodClaimService.claim(
                userId,
                listingId,
                new CreateFoodClaimRequest(organizationId, FulfillmentMode.NGO_PICKUP)
            )
        );

        verify(foodClaimRepository, never()).save(any(FoodClaim.class));
        verify(deliveryTaskService, never()).createForClaim(any(FoodClaim.class));
    }
}
