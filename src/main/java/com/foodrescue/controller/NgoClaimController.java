package com.foodrescue.controller;

import java.util.UUID;

import com.foodrescue.dto.claim.CancelFoodClaimRequest;
import com.foodrescue.dto.claim.FoodClaimResponse;
import com.foodrescue.service.FoodClaimService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ngo/claims")
public class NgoClaimController {

    private final FoodClaimService foodClaimService;

    public NgoClaimController(FoodClaimService foodClaimService) {
        this.foodClaimService = foodClaimService;
    }

    @GetMapping
    public Page<FoodClaimResponse> listActiveClaims(
        @AuthenticationPrincipal String userId,
        @RequestParam UUID organizationId,
        @PageableDefault(size = 20, sort = "claimedAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return foodClaimService.listActiveClaims(
            UUID.fromString(userId),
            organizationId,
            pageable
        );
    }

    @GetMapping("/{claimId}")
    public FoodClaimResponse getClaim(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID claimId
    ) {
        return foodClaimService.getClaim(UUID.fromString(userId), claimId);
    }

    @PostMapping("/{claimId}/cancel")
    public FoodClaimResponse cancel(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID claimId,
        @Valid @RequestBody CancelFoodClaimRequest request
    ) {
        return foodClaimService.cancel(UUID.fromString(userId), claimId, request);
    }

    @PostMapping("/{claimId}/complete")
    public FoodClaimResponse complete(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID claimId
    ) {
        return foodClaimService.complete(UUID.fromString(userId), claimId);
    }
}
