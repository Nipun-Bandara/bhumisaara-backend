package com.bandits.bhumisaara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An area as the operator sees it — the same three columns as
 * {@link AreaResponseDTO} plus the activation flag, which only the admin
 * screens need. Kept separate rather than added to the shared DTO so the
 * farmer and government pickers keep their minimal shape.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAreaResponseDTO {

    private Long areaId;

    private String areaName;

    private String district;

    private Boolean isActive;
}
