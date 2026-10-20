package com.foodrescue.controller;

import java.util.UUID;

import com.foodrescue.dto.notification.NotificationResponse;
import com.foodrescue.dto.notification.UnreadNotificationCountResponse;
import com.foodrescue.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Page<NotificationResponse> list(
        @AuthenticationPrincipal String userId,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return notificationService.list(UUID.fromString(userId), pageable);
    }

    @GetMapping("/unread-count")
    public UnreadNotificationCountResponse unreadCount(
        @AuthenticationPrincipal String userId
    ) {
        return notificationService.unreadCount(UUID.fromString(userId));
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markRead(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID notificationId
    ) {
        return notificationService.markRead(UUID.fromString(userId), notificationId);
    }

    @PatchMapping("/read-all")
    public UnreadNotificationCountResponse markAllRead(
        @AuthenticationPrincipal String userId
    ) {
        return notificationService.markAllRead(UUID.fromString(userId));
    }
}
