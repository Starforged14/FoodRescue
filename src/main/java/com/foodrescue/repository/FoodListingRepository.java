package com.foodrescue.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodrescue.entity.FoodListing;
import com.foodrescue.entity.ListingStatus;

public interface FoodListingRepository extends JpaRepository<FoodListing, UUID> {

    long countByStatus(ListingStatus status);

    Page<FoodListing> findAllByStatusAndAvailableUntilAfter(
        ListingStatus status,
        Instant availableUntil,
        Pageable pageable
    );

    Page<FoodListing> findAllByDonorId(UUID donorUserId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select listing from FoodListing listing where listing.id = :listingId")
    Optional<FoodListing> findByIdForUpdate(@Param("listingId") UUID listingId);

    @Modifying
    @Query("""
        update FoodListing listing
        set listing.status = :expiredStatus,
            listing.updatedAt = :now,
            listing.version = listing.version + 1
        where listing.status in :expirableStatuses
          and listing.availableUntil <= :now
        """)
    int expireOverdueListings(
        @Param("expirableStatuses") List<ListingStatus> expirableStatuses,
        @Param("expiredStatus") ListingStatus expiredStatus,
        @Param("now") Instant now
    );
}
