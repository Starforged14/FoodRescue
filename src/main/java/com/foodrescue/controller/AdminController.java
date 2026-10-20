package com.foodrescue.controller;

import java.util.UUID;

import com.foodrescue.dto.admin.AdminDashboardMetricsResponse;
import com.foodrescue.dto.admin.AdminNgoMembershipResponse;
import com.foodrescue.dto.admin.AdminNgoOrganizationResponse;
import com.foodrescue.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard/metrics")
    public AdminDashboardMetricsResponse getDashboardMetrics() {
        return adminService.getDashboardMetrics();
    }

    @GetMapping("/organizations/pending")
    public Page<AdminNgoOrganizationResponse> listPendingOrganizations(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
        Pageable pageable
    ) {
        return adminService.listPendingOrganizations(pageable);
    }

    @PostMapping("/organizations/{organizationId}/approve")
    public AdminNgoOrganizationResponse approveOrganization(@PathVariable UUID organizationId) {
        return adminService.approveOrganization(organizationId);
    }

    @PostMapping("/organizations/{organizationId}/reject")
    public AdminNgoOrganizationResponse rejectOrganization(@PathVariable UUID organizationId) {
        return adminService.rejectOrganization(organizationId);
    }

    @GetMapping("/memberships/pending")
    public Page<AdminNgoMembershipResponse> listPendingMemberships(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
        Pageable pageable
    ) {
        return adminService.listPendingMemberships(pageable);
    }

    @PostMapping("/memberships/{membershipId}/approve")
    public AdminNgoMembershipResponse approveMembership(@PathVariable UUID membershipId) {
        return adminService.approveMembership(membershipId);
    }

    @PostMapping("/memberships/{membershipId}/reject")
    public AdminNgoMembershipResponse rejectMembership(@PathVariable UUID membershipId) {
        return adminService.rejectMembership(membershipId);
    }
}
