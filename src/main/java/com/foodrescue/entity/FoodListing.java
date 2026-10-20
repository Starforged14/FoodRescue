package com.foodrescue.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.Check;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "food_listings", indexes = {
    @Index(name = "idx_food_listings_status_deadline", columnList = "status, available_until"),
    @Index(name = "idx_food_listings_donor_created", columnList = "donor_user_id, created_at")
})
@Check(constraints = "pickup_window_end > pickup_window_start")
@Getter
@Setter
@NoArgsConstructor
public class FoodListing extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donor_user_id", nullable = false)
    private DonorProfile donor;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ListingStatus status;

    @Column(name = "available_until", nullable = false)
    private Instant availableUntil;

    @Column(name = "pickup_window_start", nullable = false)
    private Instant pickupWindowStart;

    @Column(name = "pickup_window_end", nullable = false)
    private Instant pickupWindowEnd;

    @Column(name = "pickup_instructions", columnDefinition = "text")
    private String pickupInstructions;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FoodListingItem> items = new ArrayList<>();

    @Version
    private long version;
}
