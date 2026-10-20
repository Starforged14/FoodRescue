package com.foodrescue.mapper;

import com.foodrescue.dto.claim.FoodClaimResponse;
import com.foodrescue.dto.claim.PickupAddressResponse;
import com.foodrescue.entity.ClaimStatus;
import com.foodrescue.entity.FoodClaim;
import org.springframework.stereotype.Component;

@Component
public class FoodClaimMapper {

    public FoodClaimResponse toResponse(FoodClaim claim) {
        return new FoodClaimResponse(
            claim.getId(),
            claim.getListing().getId(),
            claim.getListing().getTitle(),
            claim.getOrganization().getId(),
            claim.getOrganization().getName(),
            claim.getClaimedBy().getId(),
            claim.getStatus(),
            claim.getFulfillmentMode(),
            claim.getClaimedAt(),
            claim.getCancelledAt(),
            claim.getCompletedAt(),
            claim.getCancellationReason(),
            hasPickupAccess(claim)
                ? new PickupAddressResponse(
                    claim.getListing().getDonor().getAddressLine1(),
                    claim.getListing().getDonor().getAddressLine2(),
                    claim.getListing().getDonor().getCity(),
                    claim.getListing().getDonor().getRegion(),
                    claim.getListing().getDonor().getPostalCode(),
                    claim.getListing().getDonor().getCountryCode(),
                    claim.getListing().getPickupInstructions()
                )
                : null
        );
    }

    private boolean hasPickupAccess(FoodClaim claim) {
        return claim.getStatus() == ClaimStatus.ACTIVE
            || claim.getStatus() == ClaimStatus.COMPLETED;
    }
}
