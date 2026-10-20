package com.foodrescue.service;

import java.util.List;
import java.util.UUID;

import com.foodrescue.dto.profile.VolunteerProfileRequest;
import com.foodrescue.dto.profile.VolunteerProfileResponse;
import com.foodrescue.entity.AssignmentStatus;
import com.foodrescue.entity.UserAccount;
import com.foodrescue.entity.VolunteerAvailability;
import com.foodrescue.entity.VolunteerProfile;
import com.foodrescue.exception.ConflictException;
import com.foodrescue.exception.ForbiddenException;
import com.foodrescue.exception.ResourceNotFoundException;
import com.foodrescue.repository.DeliveryAssignmentRepository;
import com.foodrescue.repository.UserAccountRepository;
import com.foodrescue.repository.VolunteerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VolunteerProfileService {

    private final VolunteerProfileRepository volunteerProfileRepository;
    private final UserAccountRepository userAccountRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;

    public VolunteerProfileService(
        VolunteerProfileRepository volunteerProfileRepository,
        UserAccountRepository userAccountRepository,
        DeliveryAssignmentRepository deliveryAssignmentRepository
    ) {
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.userAccountRepository = userAccountRepository;
        this.deliveryAssignmentRepository = deliveryAssignmentRepository;
    }

    @Transactional
    public VolunteerProfileResponse save(UUID userId, VolunteerProfileRequest request) {
        if (request.availabilityStatus() == VolunteerAvailability.SUSPENDED) {
            throw new ForbiddenException("Volunteers cannot change their status to suspended");
        }
        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User account was not found"));
        VolunteerProfile profile = volunteerProfileRepository.findByIdForUpdate(userId)
            .orElseGet(VolunteerProfile::new);
        if (deliveryAssignmentRepository.existsByVolunteerIdAndStatusIn(
            userId,
            List.of(AssignmentStatus.ACCEPTED)
        )) {
            throw new ConflictException("Volunteer profile cannot be changed while a delivery is assigned");
        }
        profile.setUser(user);
        profile.setAvailabilityStatus(request.availabilityStatus());
        profile.setMaxTravelDistanceKm(request.maxTravelDistanceKm());
        return toResponse(volunteerProfileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public VolunteerProfileResponse get(UUID userId) {
        VolunteerProfile profile = volunteerProfileRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Volunteer profile was not found"));
        return toResponse(profile);
    }

    private VolunteerProfileResponse toResponse(VolunteerProfile profile) {
        return new VolunteerProfileResponse(
            profile.getId(),
            profile.getAvailabilityStatus(),
            profile.getMaxTravelDistanceKm()
        );
    }
}
