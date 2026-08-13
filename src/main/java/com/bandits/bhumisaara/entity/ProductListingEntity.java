package com.bandits.bhumisaara.entity;

import com.bandits.bhumisaara.enums.ListingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

/**
 * A product a private agro-dealer or organic fertilizer producer offers to
 * farmers in the marketplace.
 * <p>
 * {@code isOrganic} is not the seller's free choice: it is fixed by their role
 * (producers sell organic, dealers sell chemical) and re-derived server-side on
 * every write. It decides the credit conversion rate, so letting a dealer flag
 * a listing organic would let them charge 1 credit for 1.5kg they never grew.
 */
@Entity
@Table(name = "product_listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listing_id")
    private Long listingId;

    /** The owning seller, always taken from the JWT — never from a payload id. */
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "product_name", nullable = false, length = 120)
    private String productName;

    @Column(name = "fertilizer_type", nullable = false, length = 60)
    private String fertilizerType;

    @Column(name = "is_organic", nullable = false)
    private Boolean isOrganic;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "price_lkr_per_kg", nullable = false)
    private Integer priceLkrPerKg;

    /** Reduced when an order reserves stock, restored if that order is cancelled. */
    @Column(name = "available_kg", nullable = false)
    private Integer availableKg;

    @Builder.Default
    @ColumnDefault("true")
    @Column(name = "is_subsidy_eligible", nullable = false)
    private Boolean isSubsidyEligible = true;

    @Builder.Default
    @ColumnDefault("'ACTIVE'")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ListingStatus status = ListingStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
