package com.foodrescue.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.foodrescue.dto.ngo.NgoOrganizationRequest;
import com.foodrescue.dto.ngo.NgoOrganizationResponse;
import com.foodrescue.service.NgoOrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ngo/organizations")
public class NgoOrganizationController {

    private final NgoOrganizationService ngoOrganizationService;

    public NgoOrganizationController(NgoOrganizationService ngoOrganizationService) {
        this.ngoOrganizationService = ngoOrganizationService;
    }

    @PostMapping
    public ResponseEntity<NgoOrganizationResponse> createOrganization(
        @AuthenticationPrincipal String userId,
        @Valid @RequestBody NgoOrganizationRequest request
    ) {
        NgoOrganizationResponse response =
            ngoOrganizationService.create(UUID.fromString(userId), request);
        return ResponseEntity.created(URI.create("/api/v1/ngo/organizations/" + response.id()))
            .body(response);
    }

    @GetMapping("/mine")
    public List<NgoOrganizationResponse> getMyOrganizations(
        @AuthenticationPrincipal String userId
    ) {
        return ngoOrganizationService.getMyOrganizations(UUID.fromString(userId));
    }

    @GetMapping("/{organizationId}")
    public NgoOrganizationResponse getOrganization(
        @AuthenticationPrincipal String userId,
        @PathVariable UUID organizationId
    ) {
        return ngoOrganizationService.getOrganization(UUID.fromString(userId), organizationId);
    }
}
