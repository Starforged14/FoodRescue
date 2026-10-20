package com.foodrescue.mapper;

import com.foodrescue.dto.listing.FoodListingItemRequest;
import com.foodrescue.dto.listing.FoodListingItemResponse;
import com.foodrescue.dto.listing.FoodListingRequest;
import com.foodrescue.dto.listing.FoodListingResponse;
import com.foodrescue.entity.DonorProfile;
import com.foodrescue.entity.FoodListing;
import com.foodrescue.entity.FoodListingItem;
import org.springframework.stereotype.Component;

@Component
public class FoodListingMapper {

    public void applyRequest(FoodListing listing, FoodListingRequest request) {
        listing.setTitle(request.title().trim());
        listing.setDescription(trimToNull(request.description()));
        listing.setAvailableUntil(request.availableUntil());
        listing.setPickupWindowStart(request.pickupWindowStart());
        listing.setPickupWindowEnd(request.pickupWindowEnd());
        listing.setPickupInstructions(trimToNull(request.pickupInstructions()));
        listing.getItems().clear();
        for (FoodListingItemRequest itemRequest : request.items()) {
            FoodListingItem item = new FoodListingItem();
            item.setListing(listing);
            item.setName(itemRequest.name().trim());
            item.setCategory(itemRequest.category());
            item.setQuantity(itemRequest.quantity());
            item.setUnit(itemRequest.unit());
            item.setAllergenNotes(trimToNull(itemRequest.allergenNotes()));
            item.setDietaryNotes(trimToNull(itemRequest.dietaryNotes()));
            item.setBestBeforeAt(itemRequest.bestBeforeAt());
            listing.getItems().add(item);
        }
    }

    public FoodListingResponse toResponse(FoodListing listing) {
        return toResponse(listing, false);
    }

    public FoodListingResponse toDonorResponse(FoodListing listing) {
        return toResponse(listing, true);
    }

    private FoodListingResponse toResponse(FoodListing listing, boolean includePickupInstructions) {
        DonorProfile donor = listing.getDonor();
        return new FoodListingResponse(
            listing.getId(),
            donor.getOrganizationName(),
            listing.getTitle(),
            listing.getDescription(),
            listing.getStatus(),
            listing.getAvailableUntil(),
            listing.getPickupWindowStart(),
            listing.getPickupWindowEnd(),
            donor.getCity(),
            donor.getRegion(),
            donor.getCountryCode(),
            includePickupInstructions ? listing.getPickupInstructions() : null,
            listing.getItems().stream()
                .map(item -> new FoodListingItemResponse(
                    item.getId(),
                    item.getName(),
                    item.getCategory(),
                    item.getQuantity(),
                    item.getUnit(),
                    item.getAllergenNotes(),
                    item.getDietaryNotes(),
                    item.getBestBeforeAt()
                ))
                .toList()
        );
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
