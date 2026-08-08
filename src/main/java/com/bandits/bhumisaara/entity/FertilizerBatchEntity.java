package com.bandits.bhumisaara.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fertilizer_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FertilizerBatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "token_id", nullable = false, unique = true)
    private String tokenId;

    @Column(name = "transaction_hash", nullable = false, unique = true)
    private String transactionHash;

    @Column(name = "importer_name", nullable = false)
    private String importerName;

    @Column(name = "fertilizer_type", nullable = false)
    private String fertilizerType;

    @Column(name = "volume_kg", nullable = false)
    private Integer volumeKg;

    @Column(name = "minted_by_user_id", nullable = false)
    private Long mintedByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
