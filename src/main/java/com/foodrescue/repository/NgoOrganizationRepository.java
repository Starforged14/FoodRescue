package com.foodrescue.repository;

import java.util.UUID;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodrescue.entity.OrganizationStatus;
import com.foodrescue.entity.NgoOrganization;

public interface NgoOrganizationRepository extends JpaRepository<NgoOrganization, UUID> {

    long countByVerificationStatus(OrganizationStatus verificationStatus);

    Page<NgoOrganization> findAllByVerificationStatusOrderByCreatedAtAsc(
        OrganizationStatus verificationStatus,
        Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select organization from NgoOrganization organization where organization.id = :organizationId")
    Optional<NgoOrganization> findByIdForUpdate(
        @Param("organizationId") UUID organizationId
    );
}
