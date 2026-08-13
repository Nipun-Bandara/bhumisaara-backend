package com.bandits.bhumisaara.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(
    name = "areas",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_area_name_district",
        columnNames = {"area_name", "district"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "area_id")
    private Long areaId;

    @Column(name = "area_name", nullable = false, length = 100)
    private String areaName;

    @Column(name = "district", nullable = false, length = 100)
    private String district;

    /**
     * Retired areas are deactivated, never deleted: farmers, requests and
     * handovers all point at an area, and dropping the row would orphan
     * history that has to stay auditable. An inactive area disappears from
     * {@code GET /areas} so nobody can be assigned to it, and the admin
     * coverage screen still shows it.
     * <p>
     * {@code @ColumnDefault} is required under {@code ddl-auto: update} — a
     * bare {@code ALTER TABLE} adding a NOT NULL column fails against a table
     * that already holds rows.
     */
    @Builder.Default
    @ColumnDefault("true")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}