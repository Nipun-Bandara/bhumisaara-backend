package com.bandits.bhumisaara.entity;

import com.bandits.bhumisaara.enums.SackStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sack_id")
    private Long sackId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    /**
     * Human-readable, unpredictable label printed on the physical sack —
     * {@code XXXX-XXXX-XXXX}. Never sequential: a guessable serial would let
     * anyone fabricate a sack that scans as genuine.
     */
    @Column(name = "serial", nullable = false, unique = true, length = 32)
    private String serial;

    @Column(name = "weight_kg", nullable = false)
    private Integer weightKg;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SackStatus status = SackStatus.AT_CENTRAL;

    /** Null only if custody is unknown; set to the minting admin on creation. */
    @Column(name = "held_by_user_id")
    private Long heldByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
