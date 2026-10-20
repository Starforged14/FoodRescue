package com.foodrescue.mapper;

import com.foodrescue.dto.claim.PickupAddressResponse;
import com.foodrescue.dto.delivery.DeliveryTaskDetailsResponse;
import com.foodrescue.dto.delivery.DeliveryTaskSummaryResponse;
import com.foodrescue.entity.AddressSnapshot;
import com.foodrescue.entity.DeliveryTask;
import org.springframework.stereotype.Component;

@Component
public class DeliveryTaskMapper {

    public DeliveryTaskSummaryResponse toSummary(DeliveryTask task) {
        return new DeliveryTaskSummaryResponse(
            task.getId(),
            task.getClaim().getId(),
            task.getClaim().getListing().getTitle(),
            task.getStatus(),
            task.getPickupWindowStart(),
            task.getPickupWindowEnd(),
            task.getPickupAddress().getCity(),
            task.getPickupAddress().getRegion(),
            task.getDropoffAddress().getCity(),
            task.getDropoffAddress().getRegion()
        );
    }

    public DeliveryTaskDetailsResponse toDetails(DeliveryTask task) {
        return new DeliveryTaskDetailsResponse(
            task.getId(),
            task.getClaim().getId(),
            task.getClaim().getListing().getTitle(),
            task.getStatus(),
            task.getPickupWindowStart(),
            task.getPickupWindowEnd(),
            task.getAssignedAt(),
            task.getPickedUpAt(),
            task.getDeliveredAt(),
            task.getFailureReason(),
            toAddress(task.getPickupAddress(), task.getPickupInstructions()),
            toAddress(task.getDropoffAddress(), null)
        );
    }

    private PickupAddressResponse toAddress(AddressSnapshot address, String instructions) {
        return new PickupAddressResponse(
            address.getAddressLine1(),
            address.getAddressLine2(),
            address.getCity(),
            address.getRegion(),
            address.getPostalCode(),
            address.getCountryCode(),
            instructions
        );
    }
}
