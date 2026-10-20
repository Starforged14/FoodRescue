package com.foodrescue.service;

import java.time.Instant;
import java.util.List;

import com.foodrescue.entity.ClaimStatus;
import com.foodrescue.entity.ListingStatus;
import com.foodrescue.repository.FoodClaimRepository;
import com.foodrescue.repository.FoodListingRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableScheduling
public class ListingExpirationScheduler {

    private final FoodClaimRepository foodClaimRepository;
    private final FoodListingRepository foodListingRepository;
    private final DeliveryTaskService deliveryTaskService;

    public ListingExpirationScheduler(
        FoodClaimRepository foodClaimRepository,
        FoodListingRepository foodListingRepository,
        DeliveryTaskService deliveryTaskService
    ) {
        this.foodClaimRepository = foodClaimRepository;
        this.foodListingRepository = foodListingRepository;
        this.deliveryTaskService = deliveryTaskService;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireOverdueListings() {
        Instant now = Instant.now();
        deliveryTaskService.expireOverdueTasks(now);
        foodListingRepository.expireOverdueListings(
            List.of(ListingStatus.AVAILABLE, ListingStatus.CLAIMED),
            ListingStatus.EXPIRED,
            now
        );
        foodClaimRepository.expireClaimsForOverdueListings(
            ClaimStatus.ACTIVE,
            List.of(ListingStatus.CLAIMED, ListingStatus.EXPIRED),
            ClaimStatus.EXPIRED,
            now
        );
    }
}
