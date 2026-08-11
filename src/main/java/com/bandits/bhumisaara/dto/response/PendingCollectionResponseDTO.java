package com.bandits.bhumisaara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * One approved request waiting to be collected at the officer's centre.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingCollectionResponseDTO {

    private Long requestId;
    private Long farmerId;
    private String farmerName;

    /** Null until the farmer connects a wallet — that row can't be served yet. */
    private String farmerWallet;

    private String fertilizerType;
    private Integer approvedKg;
    private Integer collectedKg;
    /** {@code approvedKg - collectedKg}; what this visit may still hand over. */
    private Integer remainingKg;

    private LocalDateTime approvedAt;
}
