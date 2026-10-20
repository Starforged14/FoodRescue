package com.foodrescue.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodrescue.entity.FoodListingItem;

public interface FoodListingItemRepository extends JpaRepository<FoodListingItem, UUID> {

    List<FoodListingItem> findAllByListingId(UUID listingId);
}
