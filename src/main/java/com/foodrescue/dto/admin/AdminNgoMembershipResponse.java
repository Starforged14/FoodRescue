package com.foodrescue.dto.admin;

import java.time.Instant;
import java.util.UUID;

import com.foodrescue.entity.MembershipStatus;

public record AdminNgoMembershipResponse(
    UUID id,
    UUID organizationId,
    String organizationName,
    UUID userId,
    String userEmail,
    String firstName,
    String lastName,
    MembershipStatus status,
    Instant createdAt
) {
}
