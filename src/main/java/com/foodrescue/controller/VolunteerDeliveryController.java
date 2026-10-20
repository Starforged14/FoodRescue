package com.foodrescue.controller;

import java.util.UUID;

import com.foodrescue.dto.delivery.DeliveryFailureRequest;
import com.foodrescue.dto.delivery.DeliveryTaskDetailsResponse;
import com.foodrescue.dto.delivery.DeliveryTaskSummaryResponse;
import com.foodrescue.service.DeliveryTaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/volunteer/deliveries")
public class VolunteerDeliveryController {

    private final DeliveryTaskService deliveryTaskService;

    public VolunteerDeliveryController(DeliveryTaskService deliveryTaskService) {
        this.deliveryTaskService = deliveryTaskService;
    }

    @GetMapping("/open")
    public Page<DeliveryTaskSummaryResponse> listOpenTasks(
        @PageableDefault(size = 20, sort = "pickupWindowStart", direction = Sort.Direction.ASC)
        Pageable pageable
    ) {
        return deliveryTaskService.listOpenTasks(pageable);
    }

    @GetMapping("/mine")
    public Page<DeliveryTaskSummaryResponse> listMyTasks(
        @AuthenticationPrincipal String userId,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return deliveryTaskService.listVolunteerTasks(UUID.fromString(userId), pageable);
    }

    @GetMapping("/{taskId}")
    public DeliveryTaskDetailsResponse getTask(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID taskId
    ) {
        return deliveryTaskService.getVolunteerTask(UUID.fromString(userId), taskId);
    }

    @PostMapping("/{taskId}/accept")
    public DeliveryTaskDetailsResponse accept(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID taskId
    ) {
        return deliveryTaskService.accept(UUID.fromString(userId), taskId);
    }

    @PostMapping("/{taskId}/pickup")
    public DeliveryTaskDetailsResponse markPickedUp(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID taskId
    ) {
        return deliveryTaskService.markPickedUp(UUID.fromString(userId), taskId);
    }

    @PostMapping("/{taskId}/delivered")
    public DeliveryTaskDetailsResponse markDelivered(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID taskId
    ) {
        return deliveryTaskService.markDelivered(UUID.fromString(userId), taskId);
    }

    @PostMapping("/{taskId}/fail")
    public DeliveryTaskDetailsResponse markFailed(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID taskId,
        @Valid @RequestBody DeliveryFailureRequest request
    ) {
        return deliveryTaskService.markFailed(UUID.fromString(userId), taskId, request);
    }

    @PostMapping("/{taskId}/cancel")
    public DeliveryTaskDetailsResponse cancelAssignment(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID taskId
    ) {
        return deliveryTaskService.cancelAssignment(UUID.fromString(userId), taskId);
    }
}
