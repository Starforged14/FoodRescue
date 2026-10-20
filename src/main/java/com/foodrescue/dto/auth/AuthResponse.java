package com.foodrescue.dto.auth;

import java.util.Set;
import java.util.UUID;

import com.foodrescue.entity.AppRole;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    UUID userId,
    String email,
    Set<AppRole> roles
) {
}
