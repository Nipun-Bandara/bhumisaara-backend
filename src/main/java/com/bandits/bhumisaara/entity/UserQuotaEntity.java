package com.bandits.bhumisaara.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_quotas", uniqueConstraints = @UniqueConstraint(columnNames = {"farmer_id", "fertilizer_type"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuotaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quota_id")
    private Long quotaId;

    @Column(name = "farmer_id", nullable = false)
    private Long farmerId;

    @Column(name = "fertilizer_type", nullable = false)
    private String fertilizerType;

    @Column(name = "remaining_kg", nullable = false)
    private Integer remainingKg;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }
}
