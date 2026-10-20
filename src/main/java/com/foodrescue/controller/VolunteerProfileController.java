package com.foodrescue.controller;

import java.util.UUID;

import com.foodrescue.dto.profile.VolunteerProfileRequest;
import com.foodrescue.dto.profile.VolunteerProfileResponse;
import com.foodrescue.service.VolunteerProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/volunteer/profile")
public class VolunteerProfileController {

    private final VolunteerProfileService volunteerProfileService;

    public VolunteerProfileController(VolunteerProfileService volunteerProfileService) {
        this.volunteerProfileService = volunteerProfileService;
    }

    @GetMapping
    public VolunteerProfileResponse getProfile(@AuthenticationPrincipal String userId) {
        return volunteerProfileService.get(UUID.fromString(userId));
    }

    @PutMapping
    public VolunteerProfileResponse saveProfile(
        @AuthenticationPrincipal String userId,
        @Valid @RequestBody VolunteerProfileRequest request
    ) {
        return volunteerProfileService.save(UUID.fromString(userId), request);
    }
}
