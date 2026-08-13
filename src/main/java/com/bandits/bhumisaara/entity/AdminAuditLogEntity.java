package com.bandits.bhumisaara.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One privileged action taken by a {@code SYSTEM_ADMIN}.
 * <p>
 * <strong>Append-only.</strong> There is no update or delete endpoint, and
 * there must never be one: the value of this table is entirely in the fact
 * that nobody — including the admin who wrote a row — can go back and change
 * it. {@code AuditService} is the only writer.
 * <p>
 * Rows are written inside the same transaction as the action they describe, so
 * an action and its log commit or roll back together. A ban with no audit row
 * is impossible by construction rather than by discipline.
 */
@Entity
@Table(name = "admin_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    /** The acting admin, always resolved from the JWT — never from a payload. */
    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    /** A stable machine-readable verb from {@code AuditService.Action}. */
    @Column(name = "action", nullable = false, length = 60)
    private String action;

    /** The user acted upon, when the action targets one. */
    @Column(name = "target_user_id")
    private Long targetUserId;

    /** A non-user target, e.g. {@code "area:7"}. */
    @Column(name = "target_entity", length = 100)
    private String targetEntity;

    /**
     * Human-readable context — what changed, and from what to what. Truncated
     * to fit rather than rejected: losing the tail of a description is a far
     * better outcome than losing the whole audit row.
     */
    @Column(name = "details", length = 500)
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
