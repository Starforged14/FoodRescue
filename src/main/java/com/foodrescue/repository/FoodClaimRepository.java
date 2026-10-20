package com.foodrescue.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodrescue.entity.ClaimStatus;
import com.foodrescue.entity.FoodClaim;
import com.foodrescue.entity.ListingStatus;

public interface FoodClaimRepository extends JpaRepository<FoodClaim, UUID> {

    long countByStatus(ClaimStatus status);

    @Query("select claim.listing.id from FoodClaim claim where claim.id = :claimId")
    Optional<UUID> findListingIdById(@Param("claimId") UUID claimId);

    Page<FoodClaim> findAllByOrganizationIdAndStatus(
        UUID organizationId,
        ClaimStatus status,
        Pageable pageable
    );

    boolean existsByListingIdAndStatus(UUID listingId, ClaimStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select claim from FoodClaim claim where claim.id = :claimId")
    Optional<FoodClaim> findByIdForUpdate(@Param("claimId") UUID claimId);

    @Modifying
    @Query("""
        update FoodClaim claim
        set claim.status = :expiredStatus,
            claim.updatedAt = :now
        where claim.status = :activeStatus
          and claim.listing.id in (
              select listing.id from FoodListing listing
              where listing.status in :expirableListingStatuses
                and listing.availableUntil <= :now
          )
        """)
    int expireClaimsForOverdueListings(
        @Param("activeStatus") ClaimStatus activeStatus,
        @Param("expirableListingStatuses") List<ListingStatus> expirableListingStatuses,
        @Param("expiredStatus") ClaimStatus expiredStatus,
        @Param("now") Instant now
    );
}
