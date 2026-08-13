package com.bandits.bhumisaara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A farmer on the admin's credit issuance screen, for one season.
 * <p>
 * Rows that are already issued, or that have no wallet to mint to, are returned
 * rather than filtered out — the admin needs to see why a farmer can't be
 * selected, not just find them missing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibleFarmerResponseDTO {

    private Long farmerId;
    private String farmerName;
    private String fullName;
    /** Null until the farmer connects a wallet — that row cannot be issued to. */
    private String walletAddress;
    private Long areaId;
    private String areaName;
    private String district;

    /** True when this farmer already has an issuance for the selected season. */
    private Boolean alreadyIssued;
    /** The credits they were issued, when {@code alreadyIssued} is true. */
    private Integer issuedCreditsKg;
    private String issuanceTransactionHash;

    /** False when the farmer has no linked wallet — nothing can be minted to them. */
    private Boolean canIssue;
}
