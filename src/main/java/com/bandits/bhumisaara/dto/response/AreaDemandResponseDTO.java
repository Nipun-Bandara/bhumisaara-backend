package com.bandits.bhumisaara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One row of the government admin's distribution queue: what a single area is
 * owed of a single fertilizer type, and which officer receives it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AreaDemandResponseDTO {

    private Long areaId;
    private String areaName;
    private String district;
    private String fertilizerType;

    private Integer approvedKg;
    private Integer transferredKg;
    private Integer outstandingKg;

    /** Null when no officer is assigned to the area yet. */
    private Long officerId;
    private String officerName;

    /** Null when the officer has never connected a wallet — that row can't be transferred to. */
    private String officerWallet;
}
