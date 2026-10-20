package com.foodrescue.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.foodrescue.entity.NgoOrganization;
import com.foodrescue.entity.OrganizationStatus;
import com.foodrescue.exception.ConflictException;
import com.foodrescue.repository.DeliveryTaskRepository;
import com.foodrescue.repository.FoodClaimRepository;
import com.foodrescue.repository.FoodListingRepository;
import com.foodrescue.repository.NgoMembershipRepository;
import com.foodrescue.repository.NgoOrganizationRepository;
import com.foodrescue.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private FoodListingRepository foodListingRepository;

    @Mock
    private FoodClaimRepository foodClaimRepository;

    @Mock
    private DeliveryTaskRepository deliveryTaskRepository;

    @Mock
    private NgoOrganizationRepository ngoOrganizationRepository;

    @Mock
    private NgoMembershipRepository ngoMembershipRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void approveOrganizationTransitionsPendingReviewToVerified() {
        UUID organizationId = UUID.randomUUID();
        NgoOrganization organization = new NgoOrganization();
        organization.setId(organizationId);
        organization.setName("Community Kitchen");
        organization.setVerificationStatus(OrganizationStatus.PENDING_REVIEW);
        when(ngoOrganizationRepository.findByIdForUpdate(organizationId))
            .thenReturn(Optional.of(organization));

        var response = adminService.approveOrganization(organizationId);

        assertEquals(OrganizationStatus.VERIFIED, organization.getVerificationStatus());
        assertEquals(OrganizationStatus.VERIFIED, response.verificationStatus());
        verify(ngoOrganizationRepository).findByIdForUpdate(organizationId);
    }

    @Test
    void organizationReviewRejectsAlreadyReviewedRecords() {
        UUID organizationId = UUID.randomUUID();
        NgoOrganization organization = new NgoOrganization();
        organization.setId(organizationId);
        organization.setVerificationStatus(OrganizationStatus.VERIFIED);
        when(ngoOrganizationRepository.findByIdForUpdate(organizationId))
            .thenReturn(Optional.of(organization));

        assertThrows(
            ConflictException.class,
            () -> adminService.rejectOrganization(organizationId)
        );
    }
}
