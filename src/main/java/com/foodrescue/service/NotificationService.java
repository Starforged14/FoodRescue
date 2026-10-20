package com.foodrescue.service;

import java.time.Instant;
import java.util.UUID;

import com.foodrescue.dto.notification.NotificationResponse;
import com.foodrescue.dto.notification.UnreadNotificationCountResponse;
import com.foodrescue.entity.Notification;
import com.foodrescue.entity.NotificationType;
import com.foodrescue.entity.UserAccount;
import com.foodrescue.exception.ResourceNotFoundException;
import com.foodrescue.repository.NotificationRepository;
import com.foodrescue.repository.UserAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserAccountRepository userAccountRepository;

    public NotificationService(
        NotificationRepository notificationRepository,
        UserAccountRepository userAccountRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void notifyUser(
        UUID userId,
        NotificationType type,
        String title,
        String message,
        String referenceType,
        UUID referenceId
    ) {
        UserAccount user = userAccountRepository.getReferenceById(userId);
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(UUID userId, Pageable pageable) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse unreadCount(UUID userId) {
        return new UnreadNotificationCountResponse(
            notificationRepository.countByUserIdAndReadAtIsNull(userId)
        );
    }

    @Transactional
    public NotificationResponse markRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification was not found"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
        }
        return toResponse(notification);
    }

    @Transactional
    public UnreadNotificationCountResponse markAllRead(UUID userId) {
        notificationRepository.markAllRead(userId, Instant.now());
        return unreadCount(userId);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getType(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getReferenceType(),
            notification.getReferenceId(),
            notification.getCreatedAt(),
            notification.getReadAt()
        );
    }
}
