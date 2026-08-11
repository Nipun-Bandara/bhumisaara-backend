package com.bandits.bhumisaara.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "distribution_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "distribution_id")
    private Long distributionId;

    @Column(name = "token_id", nullable = false)
    private String tokenId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    /**
     * The approved request this handover fulfilled — a farmer may hold several.
     * Nullable only because handovers recorded before requests were linked have
     * nothing to point at; every new row sets it.
     */
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "farmer_id", nullable = false)
    private Long farmerId;

    @Column(name = "officer_id", nullable = false)
    private Long officerId;

    @Column(name = "amount_dispensed_kg", nullable = false)
    private Integer amountDispensedKg;

    @Column(name = "burn_transaction_hash", nullable = false, unique = true)
    private String burnTransactionHash;

    /** Raised by the farmer when they say the handover never reached them. */
    @Builder.Default
    @ColumnDefault("false")
    @Column(name = "disputed", nullable = false)
    private Boolean disputed = false;

    @Column(name = "disputed_at")
    private LocalDateTime disputedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
