package com.foodrescue.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.foodrescue.entity.AssignmentStatus;
import com.foodrescue.entity.DeliveryAssignment;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, UUID> {

    @EntityGraph(attributePaths = {"deliveryTask", "deliveryTask.claim", "deliveryTask.claim.listing"})
    Page<DeliveryAssignment> findAllByVolunteerId(UUID volunteerUserId, Pageable pageable);

    List<DeliveryAssignment> findAllByDeliveryTaskIdAndStatus(
        UUID deliveryTaskId,
        AssignmentStatus status
    );

    Optional<DeliveryAssignment> findByDeliveryTaskIdAndVolunteerIdAndStatus(
        UUID deliveryTaskId,
        UUID volunteerUserId,
        AssignmentStatus status
    );

    Optional<DeliveryAssignment> findByDeliveryTaskIdAndVolunteerIdAndStatusIn(
        UUID deliveryTaskId,
        UUID volunteerUserId,
        Collection<AssignmentStatus> statuses
    );

    boolean existsByVolunteerIdAndStatusIn(
        UUID volunteerUserId,
        Collection<AssignmentStatus> statuses
    );
}
