package com.bandits.bhumisaara.entity;

import com.bandits.bhumisaara.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "fertilizer_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FertilizerRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    // Association rather than a bare id: the officer queue is scoped by the
    // farmer's area, which is only reachable by walking farmer -> area.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private UserEntity farmer;

    @Column(name = "season", nullable = false, length = 50)
    private String season;

    @Column(name = "fertilizer_type", nullable = false)
    private String fertilizerType;

    @Column(name = "requested_kg", nullable = false)
    private Integer requestedKg;

    // Null until reviewed; may be lower than requestedKg on partial approval.
    @Column(name = "approved_kg")
    private Integer approvedKg;

    /**
     * How much of {@link #approvedKg} the farmer has physically collected. The
     * ceiling every handover is checked against — a farmer approved for 50kg
     * cannot walk away with two 50kg sacks across two visits.
     * <p>
     * {@code @ColumnDefault} matters under {@code ddl-auto: update}: without it
     * the NOT NULL column can't be added to a table that already holds rows.
     */
    @Builder.Default
    @ColumnDefault("0")
    @Column(name = "collected_kg", nullable = false)
    private Integer collectedKg = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_officer_id")
    private UserEntity reviewedByOfficer;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // ─── Collection fields ───────────────────────────────────────────────────
    // Populated when an approved request is physically collected and the
    // matching ERC-1155 tokens are burned. No endpoint writes these yet — the
    // collection step still runs through POST /api/v1/distributions.
    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "sack_serial")
    private String sackSerial;

    @Column(name = "burn_tx_hash", unique = true)
    private String burnTxHash;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
