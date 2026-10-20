package com.foodrescue.dto.notification;

import java.time.Instant;
import java.util.UUID;

import com.foodrescue.entity.NotificationType;

public record NotificationResponse(
    UUID id,
    NotificationType type,
    String title,
    String message,
    String referenceType,
    UUID referenceId,
    Instant createdAt,
    Instant readAt
) {
}
