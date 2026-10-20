package com.foodrescue.service;

import java.time.Instant;
import java.util.UUID;

import com.foodrescue.dto.listing.FoodListingRequest;
import com.foodrescue.dto.listing.FoodListingResponse;
import com.foodrescue.entity.DonorProfile;
import com.foodrescue.entity.FoodListing;
import com.foodrescue.entity.ListingStatus;
import com.foodrescue.exception.BadRequestException;
import com.foodrescue.exception.ConflictException;
import com.foodrescue.exception.ResourceNotFoundException;
import com.foodrescue.mapper.FoodListingMapper;
import com.foodrescue.repository.DonorProfileRepository;
import com.foodrescue.repository.FoodListingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FoodListingService {

    private final DonorProfileRepository donorProfileRepository;
    private final FoodListingRepository foodListingRepository;
    private final FoodListingMapper foodListingMapper;

    public FoodListingService(
        DonorProfileRepository donorProfileRepository,
        FoodListingRepository foodListingRepository,
        FoodListingMapper foodListingMapper
    ) {
        this.donorProfileRepository = donorProfileRepository;
        this.foodListingRepository = foodListingRepository;
        this.foodListingMapper = foodListingMapper;
    }

    @Transactional
    public FoodListingResponse create(UUID donorUserId, FoodListingRequest request) {
        validateRequestTimes(request);
        DonorProfile donor = donorProfileRepository.findById(donorUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Donor profile was not found"));

        FoodListing listing = new FoodListing();
        listing.setDonor(donor);
        listing.setStatus(ListingStatus.DRAFT);
        foodListingMapper.applyRequest(listing, request);
        return foodListingMapper.toDonorResponse(foodListingRepository.save(listing));
    }

    @Transactional(readOnly = true)
    public Page<FoodListingResponse> listDonorListings(UUID donorUserId, Pageable pageable) {
        return foodListingRepository.findAllByDonorId(donorUserId, pageable)
            .map(foodListingMapper::toDonorResponse);
    }

    @Transactional(readOnly = true)
    public FoodListingResponse getDonorListing(UUID donorUserId, UUID listingId) {
        FoodListing listing = foodListingRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Food listing was not found"));
        if (!listing.getDonor().getId().equals(donorUserId)) {
            throw new ResourceNotFoundException("Food listing was not found");
        }
        return foodListingMapper.toDonorResponse(listing);
    }

    @Transactional(readOnly = true)
    public Page<FoodListingResponse> browseAvailable(Pageable pageable) {
        return foodListingRepository.findAllByStatusAndAvailableUntilAfter(
                ListingStatus.AVAILABLE,
                Instant.now(),
                pageable
            )
            .map(foodListingMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public FoodListingResponse getAvailableListing(UUID listingId) {
        FoodListing listing = foodListingRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Food listing was not found"));
        if (listing.getStatus() != ListingStatus.AVAILABLE
            || !listing.getAvailableUntil().isAfter(Instant.now())) {
            throw new ResourceNotFoundException("Food listing was not found");
        }
        return foodListingMapper.toResponse(listing);
    }

    @Transactional
    public FoodListingResponse update(UUID donorUserId, UUID listingId, FoodListingRequest request) {
        validateRequestTimes(request);
        FoodListing listing = getListingForDonorUpdate(donorUserId, listingId);
        if (listing.getStatus() != ListingStatus.DRAFT && listing.getStatus() != ListingStatus.AVAILABLE) {
            throw new ConflictException("Only draft or available listings can be edited");
        }
        if (!listing.getAvailableUntil().isAfter(Instant.now())) {
            throw new ConflictException("Expired listings cannot be edited");
        }
        foodListingMapper.applyRequest(listing, request);
        return foodListingMapper.toDonorResponse(listing);
    }

    @Transactional
    public FoodListingResponse publish(UUID donorUserId, UUID listingId) {
        FoodListing listing = getListingForDonorUpdate(donorUserId, listingId);
        if (listing.getStatus() != ListingStatus.DRAFT) {
            throw new ConflictException("Only draft listings can be published");
        }
        Instant now = Instant.now();
        if (!listing.getAvailableUntil().isAfter(now)
            || !listing.getPickupWindowStart().isAfter(now)
            || !listing.getPickupWindowEnd().isAfter(listing.getPickupWindowStart())) {
            throw new BadRequestException("Listing availability and pickup times are invalid");
        }
        if (listing.getItems().isEmpty()) {
            throw new BadRequestException("A listing must contain at least one food item");
        }
        listing.setStatus(ListingStatus.AVAILABLE);
        return foodListingMapper.toDonorResponse(listing);
    }

    @Transactional
    public FoodListingResponse withdraw(UUID donorUserId, UUID listingId) {
        FoodListing listing = getListingForDonorUpdate(donorUserId, listingId);
        if (listing.getStatus() != ListingStatus.AVAILABLE) {
            throw new ConflictException("Only available listings can be withdrawn");
        }
        listing.setStatus(
            listing.getAvailableUntil().isAfter(Instant.now())
                ? ListingStatus.WITHDRAWN
                : ListingStatus.EXPIRED
        );
        return foodListingMapper.toDonorResponse(listing);
    }

    @Transactional
    public void deleteDraft(UUID donorUserId, UUID listingId) {
        FoodListing listing = getListingForDonorUpdate(donorUserId, listingId);
        if (listing.getStatus() != ListingStatus.DRAFT) {
            throw new ConflictException("Only draft listings can be deleted");
        }
        foodListingRepository.delete(listing);
    }

    private FoodListing getListingForDonorUpdate(UUID donorUserId, UUID listingId) {
        FoodListing listing = foodListingRepository.findByIdForUpdate(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Food listing was not found"));
        if (!listing.getDonor().getId().equals(donorUserId)) {
            throw new ResourceNotFoundException("Food listing was not found");
        }
        return listing;
    }

    private void validateRequestTimes(FoodListingRequest request) {
        if (!request.pickupWindowEnd().isAfter(request.pickupWindowStart())) {
            throw new BadRequestException("Pickup window end must be after its start");
        }
    }
}
