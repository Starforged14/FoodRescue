package com.foodrescue.controller;

import java.util.UUID;

import com.foodrescue.dto.profile.DonorProfileRequest;
import com.foodrescue.dto.profile.DonorProfileResponse;
import com.foodrescue.service.DonorProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/donor/profile")
public class DonorProfileController {

    private final DonorProfileService donorProfileService;

    public DonorProfileController(DonorProfileService donorProfileService) {
        this.donorProfileService = donorProfileService;
    }

    @GetMapping
    public DonorProfileResponse getProfile(@AuthenticationPrincipal String userId) {
        return donorProfileService.get(UUID.fromString(userId));
    }

    @PutMapping
    public DonorProfileResponse saveProfile(
        @AuthenticationPrincipal String userId,
        @Valid @RequestBody DonorProfileRequest request
    ) {
        return donorProfileService.save(UUID.fromString(userId), request);
    }
}
