package com.foodrescue.controller;

import java.net.URI;
import java.util.UUID;

import com.foodrescue.dto.listing.FoodListingRequest;
import com.foodrescue.dto.listing.FoodListingResponse;
import com.foodrescue.service.FoodListingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/donor/listings")
public class DonorFoodListingController {

    private final FoodListingService foodListingService;

    public DonorFoodListingController(FoodListingService foodListingService) {
        this.foodListingService = foodListingService;
    }

    @PostMapping
    public ResponseEntity<FoodListingResponse> create(
        @AuthenticationPrincipal String userId,
        @Valid @RequestBody FoodListingRequest request
    ) {
        FoodListingResponse response =
            foodListingService.create(UUID.fromString(userId), request);
        return ResponseEntity.created(URI.create("/api/v1/donor/listings/" + response.id()))
            .body(response);
    }

    @GetMapping
    public Page<FoodListingResponse> listMine(
        @AuthenticationPrincipal String userId,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return foodListingService.listDonorListings(UUID.fromString(userId), pageable);
    }

    @GetMapping("/{listingId}")
    public FoodListingResponse getMine(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID listingId
    ) {
        return foodListingService.getDonorListing(UUID.fromString(userId), listingId);
    }

    @PutMapping("/{listingId}")
    public FoodListingResponse update(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID listingId,
        @Valid @RequestBody FoodListingRequest request
    ) {
        return foodListingService.update(UUID.fromString(userId), listingId, request);
    }

    @PostMapping("/{listingId}/publish")
    public FoodListingResponse publish(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID listingId
    ) {
        return foodListingService.publish(UUID.fromString(userId), listingId);
    }

    @PostMapping("/{listingId}/withdraw")
    public FoodListingResponse withdraw(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID listingId
    ) {
        return foodListingService.withdraw(UUID.fromString(userId), listingId);
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<Void> deleteDraft(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID listingId
    ) {
        foodListingService.deleteDraft(UUID.fromString(userId), listingId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
