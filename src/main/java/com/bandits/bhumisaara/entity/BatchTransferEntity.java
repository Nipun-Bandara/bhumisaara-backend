package com.bandits.bhumisaara.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A government admin → agrarian service officer stock movement, proven by an
 * ERC-1155 {@code safeTransferFrom} on Polygon.
 * <p>
 * A transfer moves custody, it does not consume stock, so it never touches
 * {@code fertilizer_batches.volume_kg} — only the sacks change hands.
 */
@Entity
@Table(name = "batch_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchTransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Long transferId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "token_id", nullable = false)
    private String tokenId;

    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;

    @Column(name = "to_officer_id", nullable = false)
    private Long toOfficerId;

    @Column(name = "amount_kg", nullable = false)
    private Integer amountKg;

    /** Unique — also the replay guard against a double-submitted transfer. */
    @Column(name = "transaction_hash", nullable = false, unique = true)
    private String transactionHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
