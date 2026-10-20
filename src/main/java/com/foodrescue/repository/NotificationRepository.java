package com.foodrescue.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodrescue.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserIdAndReadAtIsNull(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("""
        update Notification notification
        set notification.readAt = :now,
            notification.updatedAt = :now,
            notification.version = notification.version + 1
        where notification.user.id = :userId
          and notification.readAt is null
        """)
    int markAllRead(
        @Param("userId") UUID userId,
        @Param("now") Instant now
    );
}
