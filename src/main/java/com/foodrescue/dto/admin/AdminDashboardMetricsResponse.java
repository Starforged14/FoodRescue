package com.foodrescue.dto.admin;

import java.util.Map;

import com.foodrescue.entity.AppRole;
import com.foodrescue.entity.ClaimStatus;
import com.foodrescue.entity.DeliveryStatus;
import com.foodrescue.entity.ListingStatus;

public record AdminDashboardMetricsResponse(
    long totalUsers,
    Map<AppRole, Long> usersByRole,
    Map<ListingStatus, Long> listingsByStatus,
    Map<ClaimStatus, Long> claimsByStatus,
    Map<DeliveryStatus, Long> deliveriesByStatus,
    long pendingNgoOrganizations,
    long pendingNgoMemberships
) {
}
