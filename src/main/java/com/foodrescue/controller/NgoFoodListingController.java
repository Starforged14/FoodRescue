package com.foodrescue.controller;

import java.net.URI;
import java.util.UUID;

import com.foodrescue.dto.claim.CreateFoodClaimRequest;
import com.foodrescue.dto.claim.FoodClaimResponse;
import com.foodrescue.dto.listing.FoodListingResponse;
import com.foodrescue.service.FoodClaimService;
import com.foodrescue.service.FoodListingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ngo/listings")
public class NgoFoodListingController {

    private final FoodListingService foodListingService;
    private final FoodClaimService foodClaimService;

    public NgoFoodListingController(
        FoodListingService foodListingService,
        FoodClaimService foodClaimService
    ) {
        this.foodListingService = foodListingService;
        this.foodClaimService = foodClaimService;
    }

    @GetMapping
    public Page<FoodListingResponse> browseAvailable(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return foodListingService.browseAvailable(pageable);
    }

    @GetMapping("/{listingId}")
    public FoodListingResponse getAvailable(@PathVariable UUID listingId) {
        return foodListingService.getAvailableListing(listingId);
    }

    @PostMapping("/{listingId}/claims")
    public ResponseEntity<FoodClaimResponse> claim(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID listingId,
        @Valid @RequestBody CreateFoodClaimRequest request
    ) {
        FoodClaimResponse response = foodClaimService.claim(
            UUID.fromString(userId),
            listingId,
            request
        );
        return ResponseEntity.created(URI.create("/api/v1/ngo/claims/" + response.id()))
            .body(response);
    }
}
