package com.foodrescue.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.foodrescue.entity.AssignmentStatus;
import com.foodrescue.entity.ClaimStatus;
import com.foodrescue.entity.DeliveryAssignment;
import com.foodrescue.entity.DeliveryStatus;
import com.foodrescue.entity.DeliveryTask;
import com.foodrescue.entity.DonorProfile;
import com.foodrescue.entity.FoodClaim;
import com.foodrescue.entity.FoodListing;
import com.foodrescue.entity.ListingStatus;
import com.foodrescue.entity.NgoOrganization;
import com.foodrescue.entity.UserAccount;
import com.foodrescue.entity.VolunteerAvailability;
import com.foodrescue.entity.VolunteerProfile;
import com.foodrescue.exception.ConflictException;
import com.foodrescue.mapper.DeliveryTaskMapper;
import com.foodrescue.repository.DeliveryAssignmentRepository;
import com.foodrescue.repository.DeliveryTaskRepository;
import com.foodrescue.repository.FoodClaimRepository;
import com.foodrescue.repository.FoodListingRepository;
import com.foodrescue.repository.NgoMembershipRepository;
import com.foodrescue.repository.VolunteerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryTaskServiceTest {

    @Mock
    private DeliveryTaskRepository deliveryTaskRepository;

    @Mock
    private DeliveryAssignmentRepository deliveryAssignmentRepository;

    @Mock
    private FoodClaimRepository foodClaimRepository;

    @Mock
    private FoodListingRepository foodListingRepository;

    @Mock
    private VolunteerProfileRepository volunteerProfileRepository;

    @Mock
    private NgoMembershipRepository ngoMembershipRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private DeliveryTaskMapper deliveryTaskMapper;

    @InjectMocks
    private DeliveryTaskService deliveryTaskService;

    private final UUID taskId = UUID.randomUUID();
    private final UUID claimId = UUID.randomUUID();
    private final UUID listingId = UUID.randomUUID();
    private final UUID volunteerId = UUID.randomUUID();
    private DeliveryTask task;
    private VolunteerProfile volunteer;

    @BeforeEach
    void setUp() {
        UserAccount donorUser = user(UUID.randomUUID());
        UserAccount ngoUser = user(UUID.randomUUID());
        DonorProfile donor = new DonorProfile();
        donor.setUser(donorUser);

        FoodListing listing = new FoodListing();
        listing.setId(listingId);
        listing.setTitle("Prepared meals");
        listing.setStatus(ListingStatus.CLAIMED);
        listing.setAvailableUntil(Instant.now().plusSeconds(3600));
        listing.setDonor(donor);

        NgoOrganization organization = new NgoOrganization();
        organization.setId(UUID.randomUUID());

        FoodClaim claim = new FoodClaim();
        claim.setId(claimId);
        claim.setListing(listing);
        claim.setOrganization(organization);
        claim.setClaimedBy(ngoUser);
        claim.setStatus(ClaimStatus.ACTIVE);

        task = new DeliveryTask();
        task.setId(taskId);
        task.setClaim(claim);
        task.setStatus(DeliveryStatus.OPEN);
        task.setPickupWindowStart(Instant.now().minusSeconds(60));
        task.setPickupWindowEnd(Instant.now().plusSeconds(1800));

        volunteer = new VolunteerProfile();
        volunteer.setId(volunteerId);
        volunteer.setAvailabilityStatus(VolunteerAvailability.AVAILABLE);

        when(deliveryTaskRepository.findListingIdById(taskId)).thenReturn(Optional.of(listingId));
        when(foodListingRepository.findByIdForUpdate(listingId)).thenReturn(Optional.of(listing));
        when(deliveryTaskRepository.findClaimIdById(taskId)).thenReturn(Optional.of(claimId));
        when(foodClaimRepository.findByIdForUpdate(claimId)).thenReturn(Optional.of(claim));
        when(deliveryTaskRepository.findByIdForUpdate(taskId)).thenReturn(Optional.of(task));
    }

    @Test
    void acceptAssignsTaskAndMarksVolunteerUnavailable() {
        when(volunteerProfileRepository.findByIdForUpdate(volunteerId)).thenReturn(Optional.of(volunteer));
        when(deliveryAssignmentRepository.existsByVolunteerIdAndStatusIn(
            eq(volunteerId),
            anyCollection()
        )).thenReturn(false);

        deliveryTaskService.accept(volunteerId, taskId);

        assertEquals(DeliveryStatus.ASSIGNED, task.getStatus());
        assertNotNull(task.getAssignedAt());
        assertEquals(VolunteerAvailability.UNAVAILABLE, volunteer.getAvailabilityStatus());

        ArgumentCaptor<DeliveryAssignment> assignment =
            ArgumentCaptor.forClass(DeliveryAssignment.class);
        verify(deliveryAssignmentRepository).save(assignment.capture());
        assertEquals(AssignmentStatus.ACCEPTED, assignment.getValue().getStatus());
        assertEquals(task, assignment.getValue().getDeliveryTask());
        assertEquals(volunteer, assignment.getValue().getVolunteer());
        verify(notificationService).notifyUser(
            eq(volunteerId),
            any(),
            eq("Delivery assigned"),
            any(),
            eq("DELIVERY_TASK"),
            eq(taskId)
        );
    }

    @Test
    void acceptRejectsTaskThatIsNoLongerOpen() {
        task.setStatus(DeliveryStatus.ASSIGNED);

        assertThrows(
            ConflictException.class,
            () -> deliveryTaskService.accept(volunteerId, taskId)
        );

        verify(volunteerProfileRepository, never()).findByIdForUpdate(any());
        verify(deliveryAssignmentRepository, never()).save(any(DeliveryAssignment.class));
    }

    @Test
    void deliveredTaskCompletesClaimAndRestoresVolunteerAvailability() {
        task.setStatus(DeliveryStatus.PICKED_UP);
        volunteer.setAvailabilityStatus(VolunteerAvailability.UNAVAILABLE);
        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setDeliveryTask(task);
        assignment.setVolunteer(volunteer);
        assignment.setStatus(AssignmentStatus.ACCEPTED);
        when(deliveryAssignmentRepository.findByDeliveryTaskIdAndVolunteerIdAndStatus(
            taskId,
            volunteerId,
            AssignmentStatus.ACCEPTED
        )).thenReturn(Optional.of(assignment));
        when(volunteerProfileRepository.findByIdForUpdate(volunteerId))
            .thenReturn(Optional.of(volunteer));

        deliveryTaskService.markDelivered(volunteerId, taskId);

        assertEquals(DeliveryStatus.DELIVERED, task.getStatus());
        assertEquals(ClaimStatus.COMPLETED, task.getClaim().getStatus());
        assertEquals(ListingStatus.COMPLETED, task.getClaim().getListing().getStatus());
        assertEquals(AssignmentStatus.COMPLETED, assignment.getStatus());
        assertEquals(VolunteerAvailability.AVAILABLE, volunteer.getAvailabilityStatus());
        assertNotNull(task.getDeliveredAt());
    }

    private UserAccount user(UUID id) {
        UserAccount user = new UserAccount();
        user.setId(id);
        return user;
    }
}
