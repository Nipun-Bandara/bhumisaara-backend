package com.bandits.bhumisaara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A sack the officer may add to the handover in progress. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SackValidationResponseDTO {

    private String sackSerial;
    private Integer weightKg;

    /** Scanned so far including this sack. */
    private Integer totalScannedKg;

    /** How much of the approval is still unclaimed after this sack. */
    private Integer remainingKg;

    private Long batchId;
    private String tokenId;
}
