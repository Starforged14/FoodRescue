package com.foodrescue.controller;

import java.util.UUID;

import com.foodrescue.dto.delivery.DeliveryTaskDetailsResponse;
import com.foodrescue.service.DeliveryTaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ngo/deliveries")
public class NgoDeliveryController {

    private final DeliveryTaskService deliveryTaskService;

    public NgoDeliveryController(DeliveryTaskService deliveryTaskService) {
        this.deliveryTaskService = deliveryTaskService;
    }

    @GetMapping
    public Page<DeliveryTaskDetailsResponse> listOrganizationTasks(
        @AuthenticationPrincipal String userId,
        @RequestParam UUID organizationId,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return deliveryTaskService.listOrganizationTasks(
            UUID.fromString(userId),
            organizationId,
            pageable
        );
    }
}
