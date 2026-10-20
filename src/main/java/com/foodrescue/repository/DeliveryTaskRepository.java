package com.foodrescue.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodrescue.entity.DeliveryStatus;
import com.foodrescue.entity.DeliveryTask;
import com.foodrescue.entity.ClaimStatus;
import com.foodrescue.entity.ListingStatus;

public interface DeliveryTaskRepository extends JpaRepository<DeliveryTask, UUID> {

    long countByStatus(DeliveryStatus status);

    @Query("select task.claim.id from DeliveryTask task where task.id = :taskId")
    Optional<UUID> findClaimIdById(@Param("taskId") UUID taskId);

    @Query("select task.claim.listing.id from DeliveryTask task where task.id = :taskId")
    Optional<UUID> findListingIdById(@Param("taskId") UUID taskId);

    @Query("select task.id from DeliveryTask task where task.claim.id = :claimId")
    Optional<UUID> findTaskIdByClaimId(@Param("claimId") UUID claimId);

    boolean existsByClaimId(UUID claimId);

    @EntityGraph(attributePaths = {"claim", "claim.listing"})
    @Query("""
        select task from DeliveryTask task
        where task.status = :status
          and task.pickupWindowEnd > :now
          and task.claim.status = :activeClaimStatus
          and task.claim.listing.status = :claimedListingStatus
          and task.claim.listing.availableUntil > :now
        """)
    Page<DeliveryTask> findOpenTasksForQueue(
        @Param("status") DeliveryStatus status,
        @Param("activeClaimStatus") ClaimStatus activeClaimStatus,
        @Param("claimedListingStatus") ListingStatus claimedListingStatus,
        @Param("now") Instant now,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"claim", "claim.listing"})
    @Query("select task from DeliveryTask task where task.claim.organization.id = :organizationId")
    Page<DeliveryTask> findAllByOrganizationId(
        @Param("organizationId") UUID organizationId,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"claim", "claim.listing"})
    @Query("select task from DeliveryTask task where task.claim.listing.donor.id = :donorUserId")
    Page<DeliveryTask> findAllByDonorUserId(
        @Param("donorUserId") UUID donorUserId,
        Pageable pageable
    );

    @Query("""
        select task.id from DeliveryTask task
        where task.status in :statuses
          and (
              task.claim.listing.availableUntil <= :now
              or (
                  task.status in :pickupWindowStatuses
                  and task.pickupWindowEnd <= :now
              )
          )
        """)
    Page<UUID> findOverdueTaskIds(
        @Param("now") Instant now,
        @Param("statuses") Collection<DeliveryStatus> statuses,
        @Param("pickupWindowStatuses") Collection<DeliveryStatus> pickupWindowStatuses,
        Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select task from DeliveryTask task where task.id = :taskId")
    Optional<DeliveryTask> findByIdForUpdate(@Param("taskId") UUID taskId);
}
