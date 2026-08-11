package com.bandits.bhumisaara.entity;

import jakarta.persistence.*;
import lombok.*;

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
}