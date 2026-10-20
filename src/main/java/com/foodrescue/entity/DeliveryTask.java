package com.foodrescue.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "delivery_tasks", indexes = {
    @Index(name = "idx_delivery_tasks_status_created", columnList = "status, created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class DeliveryTask extends AuditableEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false, unique = true)
    private FoodClaim claim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeliveryStatus status;

    @Embedded
    @jakarta.persistence.AttributeOverrides({
        @jakarta.persistence.AttributeOverride(name = "addressLine1",
            column = @Column(name = "pickup_address_line1", nullable = false, length = 200)),
        @jakarta.persistence.AttributeOverride(name = "addressLine2",
            column = @Column(name = "pickup_address_line2", length = 200)),
        @jakarta.persistence.AttributeOverride(name = "city",
            column = @Column(name = "pickup_city", nullable = false, length = 100)),
        @jakarta.persistence.AttributeOverride(name = "region",
            column = @Column(name = "pickup_region", length = 100)),
        @jakarta.persistence.AttributeOverride(name = "postalCode",
            column = @Column(name = "pickup_postal_code", nullable = false, length = 20)),
        @jakarta.persistence.AttributeOverride(name = "countryCode",
            column = @Column(name = "pickup_country_code", nullable = false, length = 2))
    })
    private AddressSnapshot pickupAddress;

    @Embedded
    @jakarta.persistence.AttributeOverrides({
        @jakarta.persistence.AttributeOverride(name = "addressLine1",
            column = @Column(name = "dropoff_address_line1", nullable = false, length = 200)),
        @jakarta.persistence.AttributeOverride(name = "addressLine2",
            column = @Column(name = "dropoff_address_line2", length = 200)),
        @jakarta.persistence.AttributeOverride(name = "city",
            column = @Column(name = "dropoff_city", nullable = false, length = 100)),
        @jakarta.persistence.AttributeOverride(name = "region",
            column = @Column(name = "dropoff_region", length = 100)),
        @jakarta.persistence.AttributeOverride(name = "postalCode",
            column = @Column(name = "dropoff_postal_code", nullable = false, length = 20)),
        @jakarta.persistence.AttributeOverride(name = "countryCode",
            column = @Column(name = "dropoff_country_code", nullable = false, length = 2))
    })
    private AddressSnapshot dropoffAddress;

    @Column(name = "pickup_window_start", nullable = false)
    private Instant pickupWindowStart;

    @Column(name = "pickup_window_end", nullable = false)
    private Instant pickupWindowEnd;

    @Column(name = "pickup_instructions", columnDefinition = "text")
    private String pickupInstructions;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "picked_up_at")
    private Instant pickedUpAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "failure_reason", columnDefinition = "text")
    private String failureReason;

    @Version
    private long version;
}
