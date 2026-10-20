package com.foodrescue.repository;

import java.util.UUID;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodrescue.entity.VolunteerAvailability;
import com.foodrescue.entity.VolunteerProfile;

public interface VolunteerProfileRepository extends JpaRepository<VolunteerProfile, UUID> {

    boolean existsByIdAndAvailabilityStatus(UUID userId, VolunteerAvailability availabilityStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select volunteer from VolunteerProfile volunteer where volunteer.id = :userId")
    Optional<VolunteerProfile> findByIdForUpdate(@Param("userId") UUID userId);
}
