package com.bandits.bhumisaara.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * The sacks that backed one handover to a farmer.
 * <p>
 * The unique constraint on {@code sack_serial} is the consume-once guarantee: a
 * physical sack can back exactly one handover, ever. It is enforced by the
 * database, not just by the service, so a replayed or concurrent request cannot
 * spend the same sack twice.
 */
@Entity
@Table(name = "handover_sacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HandoverSackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "distribution_id", nullable = false)
    private Long distributionId;

    @Column(name = "sack_serial", nullable = false, unique = true, length = 32)
    private String sackSerial;

    @Column(name = "weight_kg", nullable = false)
    private Integer weightKg;
}
