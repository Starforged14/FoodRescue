package com.foodrescue.service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.foodrescue.dto.delivery.DeliveryFailureRequest;
import com.foodrescue.dto.delivery.DeliveryTaskDetailsResponse;
import com.foodrescue.dto.delivery.DeliveryTaskSummaryResponse;
import com.foodrescue.entity.AddressSnapshot;
import com.foodrescue.entity.AssignmentStatus;
import com.foodrescue.entity.ClaimStatus;
import com.foodrescue.entity.DeliveryAssignment;
import com.foodrescue.entity.DeliveryStatus;
import com.foodrescue.entity.DeliveryTask;
import com.foodrescue.entity.FoodClaim;
import com.foodrescue.entity.FulfillmentMode;
import com.foodrescue.entity.ListingStatus;
import com.foodrescue.entity.MembershipStatus;
import com.foodrescue.entity.NotificationType;
import com.foodrescue.entity.VolunteerAvailability;
import com.foodrescue.entity.VolunteerProfile;
import com.foodrescue.exception.ConflictException;
import com.foodrescue.exception.ForbiddenException;
import com.foodrescue.exception.ResourceNotFoundException;
import com.foodrescue.mapper.DeliveryTaskMapper;
import com.foodrescue.repository.DeliveryAssignmentRepository;
import com.foodrescue.repository.DeliveryTaskRepository;
import com.foodrescue.repository.FoodClaimRepository;
import com.foodrescue.repository.FoodListingRepository;
import com.foodrescue.repository.NgoMembershipRepository;
import com.foodrescue.repository.VolunteerProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryTaskService {

    private static final List<AssignmentStatus> ACTIVE_ASSIGNMENT_STATUSES =
        List.of(AssignmentStatus.ACCEPTED);
    private static final Collection<DeliveryStatus> EXPIRABLE_TASK_STATUSES =
        List.of(DeliveryStatus.OPEN, DeliveryStatus.ASSIGNED, DeliveryStatus.PICKED_UP);
    private static final Collection<DeliveryStatus> PICKUP_WINDOW_TASK_STATUSES =
        List.of(DeliveryStatus.OPEN, DeliveryStatus.ASSIGNED);
    private static final int EXPIRATION_BATCH_SIZE = 100;

    private final DeliveryTaskRepository deliveryTaskRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final FoodClaimRepository foodClaimRepository;
    private final FoodListingRepository foodListingRepository;
    private final VolunteerProfileRepository volunteerProfileRepository;
    private final NgoMembershipRepository ngoMembershipRepository;
    private final NotificationService notificationService;
    private final DeliveryTaskMapper deliveryTaskMapper;

    public DeliveryTaskService(
        DeliveryTaskRepository deliveryTaskRepository,
        DeliveryAssignmentRepository deliveryAssignmentRepository,
        FoodClaimRepository foodClaimRepository,
        FoodListingRepository foodListingRepository,
        VolunteerProfileRepository volunteerProfileRepository,
        NgoMembershipRepository ngoMembershipRepository,
        NotificationService notificationService,
        DeliveryTaskMapper deliveryTaskMapper
    ) {
        this.deliveryTaskRepository = deliveryTaskRepository;
        this.deliveryAssignmentRepository = deliveryAssignmentRepository;
        this.foodClaimRepository = foodClaimRepository;
        this.foodListingRepository = foodListingRepository;
        this.volunteerProfileRepository = volunteerProfileRepository;
        this.ngoMembershipRepository = ngoMembershipRepository;
        this.notificationService = notificationService;
        this.deliveryTaskMapper = deliveryTaskMapper;
    }

    @Transactional
    public void createForClaim(FoodClaim claim) {
        if (claim.getFulfillmentMode() != FulfillmentMode.VOLUNTEER_DELIVERY) {
            return;
        }
        if (deliveryTaskRepository.existsByClaimId(claim.getId())) {
            throw new ConflictException("A delivery task already exists for this claim");
        }

        DeliveryTask task = new DeliveryTask();
        task.setClaim(claim);
        task.setStatus(DeliveryStatus.OPEN);
        task.setPickupAddress(toSnapshot(claim.getListing().getDonor()));
        task.setDropoffAddress(toSnapshot(claim.getOrganization()));
        task.setPickupWindowStart(claim.getListing().getPickupWindowStart());
        task.setPickupWindowEnd(claim.getListing().getPickupWindowEnd());
        task.setPickupInstructions(claim.getListing().getPickupInstructions());
        DeliveryTask savedTask = deliveryTaskRepository.save(task);
        notifyClaimParties(
            savedTask,
            NotificationType.DELIVERY_TASK_CREATED,
            "Volunteer delivery requested",
            "A volunteer delivery task was created for " + claim.getListing().getTitle()
        );
    }

    @Transactional(readOnly = true)
    public Page<DeliveryTaskSummaryResponse> listOpenTasks(Pageable pageable) {
        return deliveryTaskRepository.findOpenTasksForQueue(
                DeliveryStatus.OPEN,
                ClaimStatus.ACTIVE,
                ListingStatus.CLAIMED,
                Instant.now(),
                pageable
            )
            .map(deliveryTaskMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<DeliveryTaskSummaryResponse> listVolunteerTasks(UUID volunteerUserId, Pageable pageable) {
        return deliveryAssignmentRepository.findAllByVolunteerId(volunteerUserId, pageable)
            .map(DeliveryAssignment::getDeliveryTask)
            .map(deliveryTaskMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<DeliveryTaskDetailsResponse> listOrganizationTasks(
        UUID userId,
        UUID organizationId,
        Pageable pageable
    ) {
        requireActiveMembership(organizationId, userId);
        return deliveryTaskRepository.findAllByOrganizationId(organizationId, pageable)
            .map(deliveryTaskMapper::toDetails);
    }

    @Transactional(readOnly = true)
    public Page<DeliveryTaskDetailsResponse> listDonorTasks(UUID donorUserId, Pageable pageable) {
        return deliveryTaskRepository.findAllByDonorUserId(donorUserId, pageable)
            .map(deliveryTaskMapper::toDetails);
    }

    @Transactional(readOnly = true)
    public DeliveryTaskDetailsResponse getVolunteerTask(UUID volunteerUserId, UUID taskId) {
        DeliveryTask task = deliveryTaskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery task was not found"));
        boolean hasAccess = deliveryAssignmentRepository
            .findByDeliveryTaskIdAndVolunteerIdAndStatusIn(
                taskId,
                volunteerUserId,
                List.of(
                    AssignmentStatus.ACCEPTED,
                    AssignmentStatus.COMPLETED,
                    AssignmentStatus.CANCELLED
                )
            )
            .isPresent();
        if (!hasAccess) {
            throw new ResourceNotFoundException("Delivery task was not found");
        }
        return deliveryTaskMapper.toDetails(task);
    }

    @Transactional
    public DeliveryTaskDetailsResponse accept(UUID volunteerUserId, UUID taskId) {
        DeliveryTask task = lockTask(taskId);
        if (task.getStatus() != DeliveryStatus.OPEN
            || !task.getPickupWindowEnd().isAfter(Instant.now())
            || task.getClaim().getStatus() != ClaimStatus.ACTIVE
            || task.getClaim().getListing().getStatus() != ListingStatus.CLAIMED
            || !task.getClaim().getListing().getAvailableUntil().isAfter(Instant.now())) {
            throw new ConflictException("Delivery task is no longer available");
        }

        VolunteerProfile volunteer = lockVolunteer(volunteerUserId);
        if (volunteer.getAvailabilityStatus() != VolunteerAvailability.AVAILABLE) {
            throw new ConflictException("Volunteer profile is not available for assignment");
        }
        if (deliveryAssignmentRepository.existsByVolunteerIdAndStatusIn(
            volunteerUserId,
            ACTIVE_ASSIGNMENT_STATUSES
        )) {
            throw new ConflictException("Volunteer already has an active delivery assignment");
        }

        Instant now = Instant.now();
        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setDeliveryTask(task);
        assignment.setVolunteer(volunteer);
        assignment.setStatus(AssignmentStatus.ACCEPTED);
        assignment.setOfferedAt(now);
        assignment.setRespondedAt(now);
        deliveryAssignmentRepository.save(assignment);

        task.setStatus(DeliveryStatus.ASSIGNED);
        task.setAssignedAt(now);
        volunteer.setAvailabilityStatus(VolunteerAvailability.UNAVAILABLE);
        notificationService.notifyUser(
            volunteerUserId,
            NotificationType.DELIVERY_ASSIGNED,
            "Delivery assigned",
            "You accepted the delivery for " + task.getClaim().getListing().getTitle(),
            "DELIVERY_TASK",
            task.getId()
        );
        notifyClaimParties(
            task,
            NotificationType.DELIVERY_ASSIGNED,
            "Volunteer assigned",
            "A volunteer accepted the delivery for " + task.getClaim().getListing().getTitle()
        );
        return deliveryTaskMapper.toDetails(task);
    }

    @Transactional
    public DeliveryTaskDetailsResponse markPickedUp(UUID volunteerUserId, UUID taskId) {
        DeliveryTask task = lockTask(taskId);
        requireAssignedToVolunteer(task, volunteerUserId);
        requireActiveClaim(task);
        Instant now = Instant.now();
        if (now.isBefore(task.getPickupWindowStart()) || !now.isBefore(task.getPickupWindowEnd())) {
            throw new ConflictException("The pickup window is not currently open");
        }
        task.setStatus(DeliveryStatus.PICKED_UP);
        task.setPickedUpAt(now);
        notifyClaimParties(
            task,
            NotificationType.DELIVERY_PICKED_UP,
            "Food picked up",
            "The volunteer picked up " + task.getClaim().getListing().getTitle()
        );
        return deliveryTaskMapper.toDetails(task);
    }

    @Transactional
    public DeliveryTaskDetailsResponse markDelivered(UUID volunteerUserId, UUID taskId) {
        DeliveryTask task = lockTask(taskId);
        requireAssignedToVolunteer(task, volunteerUserId);
        requireActiveClaim(task);
        if (task.getStatus() != DeliveryStatus.PICKED_UP) {
            throw new ConflictException("Only picked-up deliveries can be marked delivered");
        }

        Instant now = Instant.now();
        if (!task.getClaim().getListing().getAvailableUntil().isAfter(now)) {
            throw new ConflictException("Expired claims cannot be marked delivered");
        }
        task.setStatus(DeliveryStatus.DELIVERED);
        task.setDeliveredAt(now);
        task.getClaim().setStatus(ClaimStatus.COMPLETED);
        task.getClaim().setCompletedAt(now);
        task.getClaim().getListing().setStatus(ListingStatus.COMPLETED);

        DeliveryAssignment assignment = getActiveAssignment(task, volunteerUserId);
        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment.setRespondedAt(now);
        lockVolunteer(volunteerUserId).setAvailabilityStatus(VolunteerAvailability.AVAILABLE);
        notifyClaimParties(
            task,
            NotificationType.DELIVERY_DELIVERED,
            "Food delivered",
            "The volunteer delivered " + task.getClaim().getListing().getTitle()
        );
        notificationService.notifyUser(
            volunteerUserId,
            NotificationType.DELIVERY_DELIVERED,
            "Delivery completed",
            "You completed the delivery for " + task.getClaim().getListing().getTitle(),
            "DELIVERY_TASK",
            task.getId()
        );
        return deliveryTaskMapper.toDetails(task);
    }

    @Transactional
    public DeliveryTaskDetailsResponse markFailed(
        UUID volunteerUserId,
        UUID taskId,
        DeliveryFailureRequest request
    ) {
        DeliveryTask task = lockTask(taskId);
        requireAssignedToVolunteer(task, volunteerUserId);
        if (task.getStatus() != DeliveryStatus.ASSIGNED
            && task.getStatus() != DeliveryStatus.PICKED_UP) {
            throw new ConflictException("Only assigned or picked-up deliveries can be marked failed");
        }

        boolean foodWasPickedUp = task.getStatus() == DeliveryStatus.PICKED_UP;
        requireActiveClaim(task);
        Instant now = Instant.now();
        task.setStatus(DeliveryStatus.FAILED);
        task.setFailureReason(request.reason().trim());
        DeliveryAssignment assignment = getActiveAssignment(task, volunteerUserId);
        assignment.setStatus(AssignmentStatus.CANCELLED);
        assignment.setRespondedAt(now);
        lockVolunteer(volunteerUserId).setAvailabilityStatus(VolunteerAvailability.AVAILABLE);

        if (foodWasPickedUp) {
            task.getClaim().setStatus(ClaimStatus.EXPIRED);
            task.getClaim().getListing().setStatus(ListingStatus.EXPIRED);
        }
        notifyClaimParties(
            task,
            NotificationType.DELIVERY_FAILED,
            "Delivery failed",
            "The delivery for " + task.getClaim().getListing().getTitle() + " could not be completed"
        );
        return deliveryTaskMapper.toDetails(task);
    }

    @Transactional
    public DeliveryTaskDetailsResponse cancelAssignment(UUID volunteerUserId, UUID taskId) {
        DeliveryTask task = lockTask(taskId);
        requireActiveClaim(task);
        if (task.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new ConflictException("Only assignments not yet picked up can be cancelled");
        }
        if (!task.getPickupWindowEnd().isAfter(Instant.now())) {
            throw new ConflictException("The pickup window has ended");
        }
        DeliveryAssignment assignment = getActiveAssignment(task, volunteerUserId);
        cancelAssignment(assignment, volunteerUserId);
        task.setStatus(DeliveryStatus.OPEN);
        task.setAssignedAt(null);
        notifyClaimParties(
            task,
            NotificationType.DELIVERY_CANCELLED,
            "Volunteer assignment cancelled",
            "The delivery for " + task.getClaim().getListing().getTitle() + " is available for another volunteer"
        );
        return deliveryTaskMapper.toDetails(task);
    }

    @Transactional
    public void cancelForClaim(UUID claimId, String reason) {
        DeliveryTask task = deliveryTaskRepository.findTaskIdByClaimId(claimId)
            .map(this::lockTask)
            .orElseThrow(() -> new ConflictException("Volunteer delivery task was not found"));
        if (task.getStatus() != DeliveryStatus.OPEN
            && task.getStatus() != DeliveryStatus.ASSIGNED
            && task.getStatus() != DeliveryStatus.FAILED) {
            throw new ConflictException("Delivery cannot be cancelled after pickup or completion");
        }

        if (task.getStatus() == DeliveryStatus.ASSIGNED) {
            DeliveryAssignment assignment = deliveryAssignmentRepository
                .findAllByDeliveryTaskIdAndStatus(task.getId(), AssignmentStatus.ACCEPTED)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ConflictException("Active volunteer assignment was not found"));
            notifyVolunteerOfCancellation(task, assignment.getVolunteer().getId());
            cancelAssignment(assignment, assignment.getVolunteer().getId());
        }
        task.setStatus(DeliveryStatus.CANCELLED);
        task.setFailureReason(trimToNull(reason));
        notifyClaimParties(
            task,
            NotificationType.DELIVERY_CANCELLED,
            "Delivery cancelled",
            "The delivery for " + task.getClaim().getListing().getTitle() + " was cancelled"
        );
    }

    @Transactional
    public void expireOverdueTasks(Instant now) {
        Pageable batch = PageRequest.of(0, EXPIRATION_BATCH_SIZE, Sort.by(Sort.Direction.ASC, "id"));
        while (true) {
            Page<UUID> overdueTaskIds = deliveryTaskRepository.findOverdueTaskIds(
                now,
                EXPIRABLE_TASK_STATUSES,
                PICKUP_WINDOW_TASK_STATUSES,
                batch
            );
            if (overdueTaskIds.isEmpty()) {
                return;
            }

            for (UUID taskId : overdueTaskIds.getContent()) {
                DeliveryTask task = lockTask(taskId);
                boolean listingExpired =
                    !task.getClaim().getListing().getAvailableUntil().isAfter(now);
                boolean missedPickupWindow =
                    PICKUP_WINDOW_TASK_STATUSES.contains(task.getStatus())
                        && !task.getPickupWindowEnd().isAfter(now);
                if (!EXPIRABLE_TASK_STATUSES.contains(task.getStatus())
                    || (!listingExpired && !missedPickupWindow)) {
                    continue;
                }
                if (task.getStatus() != DeliveryStatus.OPEN) {
                    DeliveryAssignment assignment = deliveryAssignmentRepository
                        .findAllByDeliveryTaskIdAndStatus(task.getId(), AssignmentStatus.ACCEPTED)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new ConflictException("Active volunteer assignment was not found"));
                    notifyVolunteerOfFailure(task, assignment.getVolunteer().getId());
                    cancelAssignment(assignment, assignment.getVolunteer().getId());
                }
                task.setStatus(DeliveryStatus.FAILED);
                task.setFailureReason(
                    missedPickupWindow && !listingExpired
                        ? "Delivery pickup window ended before the food was picked up"
                        : "Food claim expired before delivery was completed"
                );
                task.getClaim().setStatus(ClaimStatus.EXPIRED);
                task.getClaim().getListing().setStatus(ListingStatus.EXPIRED);
                notifyClaimParties(
                    task,
                    NotificationType.DELIVERY_FAILED,
                    "Delivery expired",
                    "The delivery for " + task.getClaim().getListing().getTitle() + " expired"
                );
            }
        }
    }

    private DeliveryTask lockTask(UUID taskId) {
        UUID listingId = deliveryTaskRepository.findListingIdById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery task was not found"));
        foodListingRepository.findByIdForUpdate(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Food listing was not found"));
        UUID claimId = deliveryTaskRepository.findClaimIdById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery task was not found"));
        foodClaimRepository.findByIdForUpdate(claimId)
            .orElseThrow(() -> new ResourceNotFoundException("Food claim was not found"));
        return deliveryTaskRepository.findByIdForUpdate(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery task was not found"));
    }

    private VolunteerProfile lockVolunteer(UUID volunteerUserId) {
        return volunteerProfileRepository.findByIdForUpdate(volunteerUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Volunteer profile was not found"));
    }

    private void requireAssignedToVolunteer(DeliveryTask task, UUID volunteerUserId) {
        if (task.getStatus() != DeliveryStatus.ASSIGNED
            && task.getStatus() != DeliveryStatus.PICKED_UP) {
            throw new ConflictException("Delivery task is not assigned for active work");
        }
        getActiveAssignment(task, volunteerUserId);
    }

    private DeliveryAssignment getActiveAssignment(DeliveryTask task, UUID volunteerUserId) {
        return deliveryAssignmentRepository
            .findByDeliveryTaskIdAndVolunteerIdAndStatus(
                task.getId(),
                volunteerUserId,
                AssignmentStatus.ACCEPTED
            )
            .orElseThrow(() -> new ForbiddenException("Delivery task is assigned to another volunteer"));
    }

    private void requireActiveClaim(DeliveryTask task) {
        if (task.getClaim().getStatus() != ClaimStatus.ACTIVE
            || task.getClaim().getListing().getStatus() != ListingStatus.CLAIMED
            || !task.getClaim().getListing().getAvailableUntil().isAfter(Instant.now())) {
            throw new ConflictException("Delivery claim is no longer active");
        }
    }

    private void notifyVolunteerOfCancellation(DeliveryTask task, UUID volunteerUserId) {
        notificationService.notifyUser(
            volunteerUserId,
            NotificationType.DELIVERY_CANCELLED,
            "Delivery cancelled",
            "The delivery for " + task.getClaim().getListing().getTitle() + " was cancelled",
            "DELIVERY_TASK",
            task.getId()
        );
    }

    private void notifyVolunteerOfFailure(DeliveryTask task, UUID volunteerUserId) {
        notificationService.notifyUser(
            volunteerUserId,
            NotificationType.DELIVERY_FAILED,
            "Delivery expired",
            "The delivery for " + task.getClaim().getListing().getTitle() + " expired",
            "DELIVERY_TASK",
            task.getId()
        );
    }

    private void requireActiveMembership(UUID organizationId, UUID userId) {
        if (!ngoMembershipRepository.existsByOrganizationIdAndUserIdAndStatus(
            organizationId,
            userId,
            MembershipStatus.ACTIVE
        )) {
            throw new ForbiddenException("An active membership in this NGO is required");
        }
    }

    private void cancelAssignment(DeliveryAssignment assignment, UUID volunteerUserId) {
        assignment.setStatus(AssignmentStatus.CANCELLED);
        assignment.setRespondedAt(Instant.now());
        lockVolunteer(volunteerUserId).setAvailabilityStatus(VolunteerAvailability.AVAILABLE);
    }

    private void notifyClaimParties(
        DeliveryTask task,
        NotificationType type,
        String title,
        String message
    ) {
        UUID organizationUserId = task.getClaim().getClaimedBy().getId();
        UUID donorUserId = task.getClaim().getListing().getDonor().getUser().getId();
        notificationService.notifyUser(
            organizationUserId,
            type,
            title,
            message,
            "DELIVERY_TASK",
            task.getId()
        );
        if (!donorUserId.equals(organizationUserId)) {
            notificationService.notifyUser(
                donorUserId,
                type,
                title,
                message,
                "DELIVERY_TASK",
                task.getId()
            );
        }
    }

    private AddressSnapshot toSnapshot(com.foodrescue.entity.DonorProfile donor) {
        return address(
            donor.getAddressLine1(),
            donor.getAddressLine2(),
            donor.getCity(),
            donor.getRegion(),
            donor.getPostalCode(),
            donor.getCountryCode()
        );
    }

    private AddressSnapshot toSnapshot(com.foodrescue.entity.NgoOrganization organization) {
        return address(
            organization.getAddressLine1(),
            organization.getAddressLine2(),
            organization.getCity(),
            organization.getRegion(),
            organization.getPostalCode(),
            organization.getCountryCode()
        );
    }

    private AddressSnapshot address(
        String addressLine1,
        String addressLine2,
        String city,
        String region,
        String postalCode,
        String countryCode
    ) {
        AddressSnapshot snapshot = new AddressSnapshot();
        snapshot.setAddressLine1(addressLine1);
        snapshot.setAddressLine2(addressLine2);
        snapshot.setCity(city);
        snapshot.setRegion(region);
        snapshot.setPostalCode(postalCode);
        snapshot.setCountryCode(countryCode);
        return snapshot;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
