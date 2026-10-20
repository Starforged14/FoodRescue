package com.foodrescue.repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodrescue.entity.MembershipStatus;
import com.foodrescue.entity.NgoMembership;

public interface NgoMembershipRepository extends JpaRepository<NgoMembership, UUID> {

    @EntityGraph(attributePaths = "organization")
    Optional<NgoMembership> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    boolean existsByOrganizationIdAndUserIdAndStatus(
        UUID organizationId,
        UUID userId,
        MembershipStatus status
    );

    @EntityGraph(attributePaths = "organization")
    List<NgoMembership> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByStatus(MembershipStatus status);

    @EntityGraph(attributePaths = {"organization", "user"})
    Page<NgoMembership> findAllByStatusOrderByCreatedAtAsc(
        MembershipStatus status,
        Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select membership from NgoMembership membership where membership.id = :membershipId")
    Optional<NgoMembership> findByIdForUpdate(@Param("membershipId") UUID membershipId);
}
